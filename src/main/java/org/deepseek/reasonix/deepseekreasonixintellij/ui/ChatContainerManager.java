package org.deepseek.reasonix.deepseekreasonixintellij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.intellij.plugins.markdown.ui.preview.html.MarkdownUtil;
import org.intellij.plugins.markdown.ui.preview.jcef.MarkdownJCEFHtmlPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聊天容器管理器：单例/单持久面板复用方案
 * 整个 ToolWindow 只初始化 1 个 MarkdownJCEFHtmlPanel，通过动态拼接 HTML 来展示整场对话
 */
public class ChatContainerManager {

    private static final Logger LOG = Logger.getInstance(ChatContainerManager.class);

    private final Project project;
    private final MarkdownJCEFHtmlPanel jcefPanel;

    // 缓存历史对话记录（结构化数据）
    private final List<ChatMessage> chatHistory = Collections.synchronizedList(new ArrayList<>());

    // 线程安全的 Markdown 拼接缓冲区
    private final StringBuffer fullMarkdown = new StringBuffer();

    // 复用的虚拟文件（避免每次调用都创建新对象）
    private final VirtualFile dummyFile = new LightVirtualFile("ai_chat.md", MarkdownFileType.INSTANCE, "");

    public ChatContainerManager(@NotNull Project project) {
        this.project = project;
        // 核心：全生命周期只创建【这一个】重型浏览器面板
        this.jcefPanel = new MarkdownJCEFHtmlPanel(project, null);
        LOG.info("ChatContainerManager created with MarkdownJCEFHtmlPanel");
    }

    public JComponent getComponent() {
        return jcefPanel.getComponent();
    }

    /**
     * 当收到用户消息，或者 AI 产生新的流式响应时，高频调用此方法
     * @param currentAiStreamingText 当前正在流式吐字的那【单条】AI 回复的最新完整文本
     */
    public void updateChatWindow(String currentAiStreamingText, boolean isStreaming) {
        // 将整个对话拼接成一个 Markdown 文本，让 MarkdownJCEFHtmlPanel 统一渲染
        if (chatHistory.isEmpty()) {
            return;
        }
        fullMarkdown.setLength(0); // 清空缓冲区

        // 1. 渲染历史消息
        for (ChatMessage msg : chatHistory) {
            if ("user".equals(msg.role)) {
                fullMarkdown.append("**You**\n\n").append(msg.rawContent).append("\n\n---\n\n");
            } else if ("assistant".equals(msg.role)) {
                fullMarkdown.append("**Assistant**\n\n").append(msg.rawContent).append("\n\n---\n\n");
            } else if ("tool".equals(msg.role)) {
                // 工具调用使用特殊样式标记
                fullMarkdown.append("<div class=\"tool-call-block\">").append(msg.rawContent).append("</div>\n\n");
            }
        }

        // 2. 当前流式内容
        if (currentAiStreamingText != null && !currentAiStreamingText.isEmpty()) {
            String fixedMarkdown = currentAiStreamingText;
            if (countOccurrences(fixedMarkdown, "```") % 2 == 1) {
                fixedMarkdown += "\n```"; // 容错闭合
            }
            fullMarkdown.append("**Assistant**\n\n").append(fixedMarkdown);
            if (isStreaming) {
                fullMarkdown.append(" ▋");
            }
        }

        String markdownText = fullMarkdown.toString();
        final String htmlContent;
        try {
            htmlContent = MarkdownUtil.INSTANCE.generateMarkdownHtml(dummyFile, markdownText, project);
        } catch (Exception e) {
            LOG.error("Failed to render markdown", e);
            return;
        }

        // 4. 注入工具调用块的自定义样式
        String styledHtml = injectToolCallStyles(htmlContent);

        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                jcefPanel.setHtml(styledHtml, 0, (VirtualFile) null);
            } catch (Exception e) {
                LOG.error("Failed to set HTML in JCEF panel", e);
            }
        });
    }

    /**
     * 为工具调用块注入自定义 CSS 样式
     */
    private String injectToolCallStyles(String html) {
        String toolStyles = """
            <style>
            .tool-call-block {
                background: linear-gradient(135deg, rgba(30, 40, 60, 0.8), rgba(25, 30, 45, 0.9));
                border: 1px solid rgba(100, 150, 255, 0.2);
                border-left: 3px solid rgba(100, 150, 255, 0.6);
                border-radius: 8px;
                padding: 12px 16px;
                margin: 8px 0;
                font-family: 'JetBrains Mono', 'Fira Code', monospace;
                font-size: 12px;
                color: #c8ccd4;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
            }
            .tool-call-block pre {
                background: rgba(20, 25, 35, 0.6);
                border: 1px solid rgba(60, 70, 90, 0.4);
                border-radius: 4px;
                padding: 8px 12px;
                margin: 6px 0;
                overflow-x: auto;
            }
            .tool-call-block code {
                font-family: 'JetBrains Mono', 'Fira Code', monospace;
                font-size: 11px;
                color: #a8c7fa;
            }
            .tool-call-block strong {
                color: #6496ff;
                font-weight: 600;
            }
            </style>
            """;

        // 在 </head> 前插入样式，如果没有 </head> 则加在 <body> 后
        if (html.contains("</head>")) {
            return html.replace("</head>", toolStyles + "</head>");
        } else if (html.contains("<body>")) {
            return html.replace("<body>", "<body>" + toolStyles);
        }
        return toolStyles + html;
    }

    /**
     * 当 AI 彻底回复结束（done）时调用，把这条记录固化进历史列表中
     */
    public void commitAiMessage(String finalMarkdown) {
        chatHistory.add(new ChatMessage("assistant", finalMarkdown));
        // 固化后再刷新一次，移除光标
        updateChatWindow("", false);
    }

    public void commitUserMessage(String userText) {
        chatHistory.add(new ChatMessage("user", userText));
        updateChatWindow("", false);
    }

    /**
     * 添加工具调用消息到历史
     */
    public void appendToolCallMarkdown(String markdown) {
        chatHistory.add(new ChatMessage("tool", markdown));
        updateChatWindow("", false);
    }

    /**
     * 清空所有历史消息
     */
    public void clearHistory() {
        chatHistory.clear();
        updateChatWindow("", false);
    }

    /**
     * 加载历史消息（从 API 获取后批量渲染）
     */
    public void loadHistory(List<HistoryEntry> history) {
        chatHistory.clear();
        for (HistoryEntry entry : history) {
            if (entry.content == null || entry.content.isEmpty()) continue;
            chatHistory.add(new ChatMessage(entry.role, entry.content));
        }
        updateChatWindow("", false);
    }

    private int countOccurrences(String text, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    /**
     * 历史消息条目
     */
    public static class HistoryEntry {
        public final String role;
        public final String content;

        public HistoryEntry(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    private static class ChatMessage {
        final String role;
        final String rawContent;

        ChatMessage(String role, String rawContent) {
            this.role = role;
            this.rawContent = rawContent;
        }
    }
}
