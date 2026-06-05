package org.deepseek.reasonix.deepseekreasonixintellij.utils;

import com.intellij.openapi.diagnostic.Logger;
import org.deepseek.reasonix.deepseekreasonixintellij.MainToolWindow;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.intellij.plugins.markdown.ui.preview.html.MarkdownUtil;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;

public class MarkdownUtils {
    private static final Logger LOG = Logger.getInstance(MarkdownUtils.class);
    /**
     * 适配 利用内存轻量级虚拟文件调用官方 MarkdownUtil 渲染 HTML
     * 这样可以无缝获得 IDEA 内置的高级代码块高亮能力
     */
    @NotNull
    public static String renderToHtml(@NotNull Project project, @NotNull String markdownContent) {
        try {
            // 1. 在内存中创建一个虚拟的 Markdown 文件（不需要保存到磁盘）
            // 参数说明：文件名，文件类型（必须是官方的 Markdown 文本类型），内容
            VirtualFile dummyFile = new LightVirtualFile(
                    "ai_chat_bubble.md",
                    MarkdownFileType.INSTANCE,
                    markdownContent
            );

            // 2. 调用你反编译出来的全新官方 API：
            // public final fun generateMarkdownHtml(file: VirtualFile, text: String, project: Project?): String
            String markdownHtml = MarkdownUtil.INSTANCE.generateMarkdownHtml(dummyFile, markdownContent, project);
            LOG.info("renderToHtml: "+markdownHtml);
            return markdownHtml;

        } catch (Exception e) {
            // 极端情况下的防崩溃降级处理
            return "<div>" + markdownContent.replace("\n", "<br>") + "</div>";
        }
    }
}
