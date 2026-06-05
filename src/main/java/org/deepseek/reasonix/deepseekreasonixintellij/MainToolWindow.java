package org.deepseek.reasonix.deepseekreasonixintellij;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.deepseek.reasonix.deepseekreasonixintellij.api.ApiEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.ApprovalRequestEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.AskAnswer;
import org.deepseek.reasonix.deepseekreasonixintellij.api.AskRequestEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.BranchesInfo;
import org.deepseek.reasonix.deepseekreasonixintellij.api.Checkpoint;
import org.deepseek.reasonix.deepseekreasonixintellij.api.CompactionDoneEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.NoticeEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.PhaseEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.ReasoningEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.ReasonixApiClient;
import org.deepseek.reasonix.deepseekreasonixintellij.api.SessionInfo;
import org.deepseek.reasonix.deepseekreasonixintellij.api.SkillInfo;
import org.deepseek.reasonix.deepseekreasonixintellij.api.StatusInfo;
import org.deepseek.reasonix.deepseekreasonixintellij.api.TextEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.ToolDispatchEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.ToolProgressEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.ToolResultEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.TurnDoneEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.UsageEvent;
import org.deepseek.reasonix.deepseekreasonixintellij.api.WireApproval;
import org.deepseek.reasonix.deepseekreasonixintellij.api.WireAsk;
import org.deepseek.reasonix.deepseekreasonixintellij.api.WireCompaction;
import org.deepseek.reasonix.deepseekreasonixintellij.api.WireTool;
import org.deepseek.reasonix.deepseekreasonixintellij.api.WireUsage;
import org.deepseek.reasonix.deepseekreasonixintellij.api.HistoryMessage;
import org.deepseek.reasonix.deepseekreasonixintellij.ui.ApprovalPanel;
import org.deepseek.reasonix.deepseekreasonixintellij.ui.ChatContainerManager;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;

public class MainToolWindow implements Disposable {
    private static final Logger LOG = Logger.getInstance(MainToolWindow.class);
    private final Project project;
    private final ReasonixApiClient apiClient;
    private JPanel contentPanel;
    private JBTextArea inputTextArea;
    private JButton sendButton;
    private JButton stopButton;
    private JLabel statusLabel;
    private JLabel ctxUsedLabel;
    private JLabel balanceLabel;
    private JLabel modelLabel;
    private JLabel cacheLabel;
    private JLabel costLabel;
    private JPanel messageArea; // 用于辅助组件（Approval 等）
    private ChatContainerManager chatContainer;
    private JPanel usagePanel; // 计费信息面板（单例复用）
    private JLabel usageTotalLabel;
    private JLabel usageInLabel;
    private JLabel usageOutLabel;
    private JLabel usageCostLabel;
    private JPanel welcomePanel;
    private JScrollPane scrollPane;
    private JPanel sessionList;
    private java.util.concurrent.ScheduledExecutorService scheduler;

    private boolean running = false;

    private StringBuilder currentText = new StringBuilder();
    private JPanel currentReasoningPanel = null;

    public MainToolWindow(Project project) {
        this.project = project;
        this.apiClient = new ReasonixApiClient();
        initComponents();
        initApiClient();
    }

    private void initApiClient() {
        apiClient.setEventConsumer(this::handleApiEvent);
        apiClient.connect();
        
        // Create scheduler first
        scheduler = Executors.newSingleThreadScheduledExecutor();
        loadSessions();
        loadStatus();
        
    }

    private void loadStatus() {
        // Schedule periodic refresh every 5 seconds, with initial delay of 1.5 seconds
        scheduler.scheduleAtFixedRate(() -> {
            try {
                refreshStatus();
            } catch (Exception e) {
                LOG.warn("Failed to refresh status", e);
            }
        }, 5000, 5000, TimeUnit.MILLISECONDS);
    }

    private void refreshStatus() {
        StatusInfo status = apiClient.getStatus();
        if (status != null) {
            SwingUtilities.invokeLater(() -> updateStatus(status));
        }
    }

    private void loadSessions() {
        // Schedule periodic refresh every 10 seconds, with initial delay of 1 second
        scheduler.scheduleAtFixedRate(() -> {
            try {
                refreshSessions();
            } catch (Exception e) {
                LOG.warn("Failed to refresh sessions", e);
            }
        }, 10000, 10000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void refreshSessions() {
        List<SessionInfo> sessions = apiClient.getSessions();
        
        SwingUtilities.invokeLater(() -> {
            sessionList.removeAll();
            
            if (sessions == null || sessions.isEmpty()) {
                JBLabel emptyLabel = new JBLabel("No sessions");
                emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                emptyLabel.setForeground(new Color(100, 100, 110));
                emptyLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                sessionList.add(emptyLabel, BorderLayout.CENTER);
            } else {
                JPanel listPanel = new JPanel();
                listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
                listPanel.setBackground(new Color(35, 35, 40));
                
                for (SessionInfo session : sessions) {
                    JButton sessionBtn = new JButton(session.title != null ? session.title : session.name);
                    sessionBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    sessionBtn.setForeground(session.current ? new Color(100, 150, 255) : new Color(200, 200, 210));
                    sessionBtn.setBackground(session.current ? new Color(100, 150, 255, 20) : new Color(45, 45, 50));
                    sessionBtn.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                    sessionBtn.setFocusPainted(false);
                    sessionBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                    
                    sessionBtn.addActionListener(e -> {
                        LOG.info("Session button clicked: " + session.name + ", running=" + running + ", current=" + session.current);
                        if (running) {
                            LOG.info("Cannot switch session while running");
                            return;
                        }
                        if (session.current) {
                            LOG.info("Session is already current, skipping: " + session.name);
                            return;
                        }
                        
                        // 在后台线程执行，避免阻塞 EDT
                        new Thread(() -> {
                            try {
                                LOG.info("Switching to session: " + session.name + ", path: " + session.path);
                                
                                // 使用 /resume 接口切换 session（与原 HTML 一致）
                                ReasonixApiClient.ApiResult result = apiClient.resume(session.path);
                                LOG.info("resume result: " + result + ", isSuccess=" + (result != null && result.isSuccess()));
                                
                                if (result == null || !result.isSuccess()) {
                                    LOG.warn("Failed to resume session: " + session.name + ", code: " + (result != null ? result.code() : -1));
                                    return;
                                }
                                
                                // 在 EDT 中更新 UI
                                SwingUtilities.invokeLater(() -> {
                                    // 清空聊天容器
                                    chatContainer.clearHistory();
                                    welcomePanel.setVisible(true);
                                    
                                    // 刷新 session 列表
                                    refreshSessions();
                                });
                                
                                // 加载历史消息（已经在后台线程）
                                loadHistoryMessages();
                            } catch (Exception ex) {
                                LOG.error("Failed to switch session", ex);
                            }
                        }, "session-switcher").start();
                    });
                    
                    listPanel.add(sessionBtn);
                    listPanel.add(Box.createVerticalStrut(2));
                }
                
                JScrollPane scroll = new JScrollPane(listPanel);
                scroll.setBorder(BorderFactory.createEmptyBorder());
                scroll.setBackground(new Color(35, 35, 40));
                scroll.getVerticalScrollBar().setBackground(new Color(45, 45, 50));
                sessionList.add(scroll, BorderLayout.CENTER);
            }
            
            sessionList.revalidate();
            sessionList.repaint();
        });
    }

    private void handleApiEvent(ApiEvent event) {
        if (event == null) return;

        // Process events on EDT
        SwingUtilities.invokeLater(() -> {
            switch (event.kind) {
                case "turn_started" -> onTurnStarted();
                case "reasoning" -> {
                    ReasoningEvent re = (ReasoningEvent) event;
                    appendReasoning(re.reasoning);
                }
                case "text" -> {
                    TextEvent te = (TextEvent) event;
                    String text = te.text;
                    LOG.info(text);
                    appendText(text);
                }
                case "message" -> onMessageComplete();
                case "turn_done" -> {
                    TurnDoneEvent te = (TurnDoneEvent) event;
                    onTurnDone(te.err);
                }
                case "tool_dispatch" -> {
                    ToolDispatchEvent te = (ToolDispatchEvent) event;
                    WireTool tool = te.tool;
                    onToolDispatch(tool != null ? tool.id : null, tool != null ? tool.name : null, tool != null ? tool.args : null);
                }
                case "tool_result" -> {
                    ToolResultEvent te = (ToolResultEvent) event;
                    WireTool tool = te.tool;
                    onToolResult(tool != null ? tool.id : null, tool != null ? tool.output : null, tool != null ? tool.err : null, tool != null && tool.truncated);
                }
                case "tool_progress" -> {
                    ToolProgressEvent te = (ToolProgressEvent) event;
                    WireTool tool = te.tool;
                    onToolProgress(tool != null ? tool.id : null, tool != null ? tool.output : null);
                }
                case "approval_request" -> {
                    ApprovalRequestEvent ae = (ApprovalRequestEvent) event;
                    WireApproval approval = ae.approval;
                    showApproval(approval != null ? approval.id : null, approval != null ? approval.tool : null, approval != null ? approval.subject : null);
                }
                case "usage" -> {
                    UsageEvent ue = (UsageEvent) event;
                    WireUsage usage = ue.usage;
                    onUsage(usage != null ? usage.totalTokens : 0, usage != null ? usage.promptTokens : 0, usage != null ? usage.completionTokens : 0, usage != null ? usage.costUsd : 0.0);
                }
                case "notice" -> {
                    NoticeEvent ne = (NoticeEvent) event;
                    showNotice(ne.level, ne.text);
                }
                case "phase" -> {
                    PhaseEvent pe = (PhaseEvent) event;
                    showPhase(pe.text);
                }
                case "ask_request" -> {
                    AskRequestEvent ae = (AskRequestEvent) event;
                    WireAsk ask = ae.ask;
                    if (ask != null) {
                        showAskRequest(ask);
                    }
                }
                case "compaction_started" -> showNotice("info", "Compacting session...");
                case "compaction_done" -> {
                    CompactionDoneEvent ce = (CompactionDoneEvent) event;
                    WireCompaction compaction = ce.compaction;
                    if (compaction != null && compaction.summary != null) {
                        showNotice("info", "Session compacted: " + compaction.summary);
                    }
                }
            }
        });
    }

    private void onTurnStarted() {
        running = true;
        updateRunningState();
        hideWelcome();
        
        // Clear previous state
        currentText.setLength(0);
        currentReasoningPanel = null;
    }

    private void appendReasoning(String reasoning) {
        LOG.info("appendReasoning called - reasoning length: " + (reasoning != null ? reasoning.length() : 0));
        
        if (reasoning == null || reasoning.isEmpty()) {
            return;
        }
        
        // 过滤特殊字符和不可显示字符
        String cleanReasoning = reasoning
            .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "") // 移除控制字符
            .replaceAll("(?i)nul1?\\s*", "") // 移除 "nul" 或 "nul1" 这样的乱码
            .replaceAll("\\\\n", "\n") // 转换 \n 为换行
            .replaceAll("\\\\t", "\t"); // 转换 \t 为制表符
        
        if (cleanReasoning.trim().isEmpty()) {
            return;
        }
        
        // reasoning 通过 ChatContainerManager 的 HTML 渲染，暂不单独处理
        LOG.info("Reasoning text received, length: " + cleanReasoning.length());
    }

    private JPanel createReasoningPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(35, 35, 40));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setBackground(new Color(35, 35, 40));

        JLabel toggleBtn = new JLabel("Thinking... ▶");
        toggleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        toggleBtn.setForeground(new Color(120, 120, 130));
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(25, 25, 30));
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(60, 60, 70)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        content.setVisible(false); // 默认折叠，与 HTML 行为一致

        JTextArea contentLabel = new JTextArea();
        contentLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        contentLabel.setForeground(new Color(180, 180, 190));
        contentLabel.setBackground(new Color(25, 25, 30));
        contentLabel.setText("");
        contentLabel.setLineWrap(true);
        contentLabel.setWrapStyleWord(true);
        contentLabel.setEditable(false);
        contentLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        content.putClientProperty("contentLabel", contentLabel);
        content.add(contentLabel);

        toggleBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            boolean expanded = false; // 默认折叠
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                expanded = !expanded;
                content.setVisible(expanded);
                toggleBtn.setText((expanded ? "Thinking... ▼" : "Thinking... ▶"));
            }
        });

        header.add(toggleBtn);
        panel.add(header);
        panel.add(content);
        panel.putClientProperty("contentLabel", contentLabel);

        return panel;
    }

    private void appendText(String text) {
        LOG.info("appendText called - text length: " + text.length());
        
        currentText.append(text);
        LOG.info("Calling updateChatWindow with text length: " + currentText.length());
        
        // 通过 ChatContainerManager 更新流式内容
        chatContainer.updateChatWindow(currentText.toString(), true);
        
        LOG.info("appendText completed");
    }

    /**
     * 加载并渲染历史消息（参考 HTML 中的 /history 渲染逻辑）
     */
    private void loadHistoryMessages() {
        LOG.info("loadHistoryMessages called");
        
        // 在后台线程调用 API
        new Thread(() -> {
            try {
                List<HistoryMessage> history = apiClient.getHistory();
                LOG.info("History loaded, message count: " + (history != null ? history.size() : 0));
                
                // 在 EDT 中更新 UI
                SwingUtilities.invokeLater(() -> {
                    if (history == null || history.isEmpty()) {
                        return;
                    }
                    
                    // 隐藏欢迎面板
                    welcomePanel.setVisible(false);
                    
                    // 重置状态
                    currentText = new StringBuilder();
                    currentReasoningPanel = null;
                    
                    // 转换为 ChatContainerManager 的历史格式
                    List<ChatContainerManager.HistoryEntry> entries = new java.util.ArrayList<>();
                    for (HistoryMessage msg : history) {
                        if (msg.content == null || msg.content.isEmpty()) continue;
                        entries.add(new ChatContainerManager.HistoryEntry(msg.role, msg.content));
                    }
                    
                    chatContainer.loadHistory(entries);
                    LOG.info("History messages rendered");
                });
            } catch (Exception e) {
                LOG.error("Failed to load history", e);
            }
        }, "history-loader").start();
    }

    private void onMessageComplete() {
        if (currentText.length() > 0) {
            chatContainer.commitAiMessage(currentText.toString());
        }
        currentText.setLength(0);
        currentReasoningPanel = null;
    }

    private void onTurnDone(String err) {
        running = false;
        updateRunningState();
        
        if (err != null && !err.isEmpty()) {
            showError(err);
        }
        
        // Refresh status
        StatusInfo status = apiClient.getStatus();
        if (status != null) {
            updateStatus(status);
        }
    }

    private void onToolDispatch(String id, String name, String args) {
        // 工具调用改为 Markdown 格式渲染
        String markdown = String.format("**🔧 %s**\n\n```json\n%s\n```\n", name != null ? name : "Unknown", args != null ? args : "{}");
        chatContainer.appendToolCallMarkdown(markdown);
    }

    private void onToolResult(String id, String output, String err, boolean truncated) {
        // 工具结果追加到 Markdown
        String result = err != null && !err.isEmpty() ? "Error: " + err : (output != null ? output : "");
        if (truncated) {
            result += "\n\n*[truncated]*";
        }
        String markdown = String.format("**Result**\n\n```\n%s\n```\n", result);
        chatContainer.appendToolCallMarkdown(markdown);
    }

    private void onToolProgress(String id, String output) {
        // 工具进度追加到 Markdown
        String markdown = String.format("```\n%s\n```\n", output != null ? output : "");
        chatContainer.appendToolCallMarkdown(markdown);
    }

    private void showApproval(String id, String tool, String subject) {
        ApprovalPanel approval = new ApprovalPanel(id, tool, subject, (allow, session) -> {
            apiClient.approve(id, allow, session);
        });
        messageArea.add(approval);
        messageArea.revalidate();
        scrollToBottom();
    }

    private void onUsage(int totalTokens, int promptTokens, int completionTokens, double costUsd) {
        // 更新标签内容
        usageTotalLabel.setText("Total " + formatTokens(totalTokens));
        usageInLabel.setText("In " + formatTokens(promptTokens));
        usageOutLabel.setText("Out " + formatTokens(completionTokens));
        usageCostLabel.setText(costUsd > 0 ? "Cost $" + String.format("%.4f", costUsd) : "Cost —");
    }

    private JLabel createUsageLabel(String prefix, String value, boolean accent) {
        JLabel lbl = new JLabel();
        lbl.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        lbl.setForeground(accent ? new Color(100, 150, 255) : new Color(120, 120, 130));
        lbl.setText(prefix + " " + value);
        return lbl;
    }

    private JLabel createUsageLabel(String prefix, String value) {
        return createUsageLabel(prefix, value, false);
    }

    private String formatTokens(int tokens) {
        if (tokens >= 1000) {
            return String.format("%.1fk", tokens / 1000.0);
        }
        return String.valueOf(tokens);
    }

    private void showNotice(String level, String text) {
        JPanel notice = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        notice.setBackground(new Color(45, 45, 50));
        notice.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, 
                        "warn".equals(level) ? new Color(200, 180, 100) : new Color(80, 80, 90)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(200, 200, 210));
        notice.add(label);

        messageArea.add(notice);
        messageArea.revalidate();
        scrollToBottom();
    }

    private void showPhase(String text) {
        JLabel phase = new JLabel(text);
        phase.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        phase.setForeground(new Color(100, 100, 110));
        phase.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        phase.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setBackground(new Color(30, 30, 35));
        wrapper.add(phase);

        messageArea.add(wrapper);
        messageArea.revalidate();
        scrollToBottom();
    }

    private void showAskRequest(WireAsk ask) {
        if (ask.questions == null || ask.questions.isEmpty()) return;

        // For simplicity, handle the first question
        WireAsk.WireAskQuestion question = ask.questions.get(0);
        if (question.options == null || question.options.isEmpty()) return;

        // Create a simple panel for ask request
        JPanel askPanel = new JPanel();
        askPanel.setLayout(new BoxLayout(askPanel, BoxLayout.Y_AXIS));
        askPanel.setBackground(new Color(40, 40, 45));
        askPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 90)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel promptLabel = new JLabel(question.prompt);
        promptLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        promptLabel.setForeground(Color.WHITE);
        askPanel.add(promptLabel);
        askPanel.add(Box.createVerticalStrut(10));

        List<String> selectedOptions = new java.util.ArrayList<>();
        java.util.List<JButton> optionButtons = new java.util.ArrayList<>();

        for (WireAsk.WireAskOption option : question.options) {
            JButton btn = new JButton(option.label);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(50, 50, 55));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 70, 75)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            btn.setFocusPainted(false);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);

            btn.addActionListener(e -> {
                if (question.multi) {
                    // Toggle selection for multi-select
                    if (selectedOptions.contains(option.label)) {
                        selectedOptions.remove(option.label);
                        btn.setBackground(new Color(50, 50, 55));
                    } else {
                        selectedOptions.add(option.label);
                        btn.setBackground(new Color(100, 150, 255, 80));
                    }
                } else {
                    // Single select - submit immediately
                    java.util.List<AskAnswer> answers = new java.util.ArrayList<>();
                    answers.add(new AskAnswer(question.id, java.util.Collections.singletonList(option.label)));
                    apiClient.answer(ask.id, answers);

                    // Disable all buttons
                    for (JButton b : optionButtons) {
                        b.setEnabled(false);
                    }
                }
            });

            optionButtons.add(btn);
            askPanel.add(btn);
            askPanel.add(Box.createVerticalStrut(5));
        }

        // Add submit button for multi-select
        if (question.multi) {
            JButton submitBtn = new JButton("Submit");
            submitBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            submitBtn.setForeground(Color.WHITE);
            submitBtn.setBackground(new Color(100, 150, 255));
            submitBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            submitBtn.setFocusPainted(false);
            submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

            submitBtn.addActionListener(e -> {
                if (!selectedOptions.isEmpty()) {
                    java.util.List<AskAnswer> answers = new java.util.ArrayList<>();
                    answers.add(new AskAnswer(question.id, new java.util.ArrayList<>(selectedOptions)));
                    apiClient.answer(ask.id, answers);
                    submitBtn.setEnabled(false);
                    for (JButton b : optionButtons) {
                        b.setEnabled(false);
                    }
                }
            });

            askPanel.add(Box.createVerticalStrut(5));
            askPanel.add(submitBtn);
        }

        messageArea.add(askPanel);
        messageArea.revalidate();
        scrollToBottom();
    }

    private void showError(String err) {
        JPanel error = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        error.setBackground(new Color(50, 30, 30));
        error.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(200, 80, 80)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        JLabel label = new JLabel("✗ " + err);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(255, 150, 150));
        error.add(label);

        messageArea.add(error);
        messageArea.revalidate();
        scrollToBottom();
    }

    private void hideWelcome() {
        welcomePanel.setVisible(false);
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void updateStatus(StatusInfo status) {
        if (status.label != null) {
            modelLabel.setText(status.label);
        }
        if (status.used >= 0) {
            ctxUsedLabel.setText(formatTokens(status.used) + " tok");
        }
        if (status.window > 0) {
            // Update window label if available
        }
        if (status.getBalanceDisplay() != null) {
            balanceLabel.setText(status.getBalanceDisplay());
        }
        // Update cache hit rate
        int total = status.cacheHit + status.cacheMiss;
        if (total > 0) {
            int hitRate = (int) (status.cacheHit * 100.0 / total);
            cacheLabel.setText(hitRate + "%");
        }
        // Update cost from lastUsage
        if (status.lastUsage != null && status.lastUsage.costUsd > 0) {
            costLabel.setText(String.format("%.4f$", status.lastUsage.costUsd));
        }
    }

    private void initComponents() {
        contentPanel = createMainPanel();
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JBSplitter splitter = new JBSplitter(false, 0.3f);
        splitter.setFirstComponent(createSidebar());
        splitter.setSecondComponent(createContentArea());

        panel.add(splitter, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(25, 25, 30));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setMinimumSize(new Dimension(180, 0));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        brand.setBackground(new Color(25, 25, 30));
        brand.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 55)));

        JBLabel logo = new JBLabel("R");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logo.setForeground(Color.WHITE);
        logo.setBackground(new Color(100, 150, 255));
        logo.setOpaque(true);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(26, 26));

        JBLabel name = new JBLabel("Reasonix");
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(Color.WHITE);

        brand.add(logo);
        brand.add(Box.createHorizontalStrut(8));
        brand.add(name);

        JPanel nav = new JPanel(new GridLayout(4, 1, 0, 4));
        nav.setBackground(new Color(25, 25, 30));
        nav.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        
        JButton newSessionBtn = createNavButton("New Session", true);
        newSessionBtn.addActionListener(e -> onNewSession());
        
        JButton compactBtn = createNavButton("Compact", false);
        compactBtn.addActionListener(e -> onCompact());
        
        JButton rewindBtn = createNavButton("Rewind", false);
        rewindBtn.addActionListener(e -> onRewind());
        
        JButton branchesBtn = createNavButton("Branches", false);
        branchesBtn.addActionListener(e -> onBranches());
        
        nav.add(newSessionBtn);
        nav.add(compactBtn);
        nav.add(rewindBtn);
        nav.add(branchesBtn);

        JPanel sessionsSection = new JPanel(new BorderLayout());
        sessionsSection.setBackground(new Color(25, 25, 30));
        sessionsSection.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 55)));

        JBLabel sessionsLabel = new JBLabel("SESSIONS");
        sessionsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sessionsLabel.setForeground(new Color(120, 120, 130));
        sessionsLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        sessionsSection.add(sessionsLabel, BorderLayout.NORTH);

        sessionList = new JPanel(new BorderLayout());
        sessionList.setBackground(new Color(35, 35, 40));
        sessionList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JBLabel loadingLabel = new JBLabel("Loading...");
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        loadingLabel.setForeground(new Color(100, 100, 110));
        loadingLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sessionList.add(loadingLabel, BorderLayout.CENTER);
        sessionsSection.add(sessionList, BorderLayout.CENTER);

        JPanel statusSection = createStatusSection();

        JPanel sidebarContent = new JPanel(new BorderLayout());
        sidebarContent.add(brand, BorderLayout.NORTH);
        sidebarContent.add(nav, BorderLayout.NORTH);
        sidebarContent.add(sessionsSection, BorderLayout.CENTER);
        sidebarContent.add(statusSection, BorderLayout.SOUTH);

        sidebar.add(sidebarContent, BorderLayout.CENTER);
        return sidebar;
    }

    private JButton createNavButton(String text, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(isPrimary ? Color.WHITE : new Color(200, 200, 210));
        button.setBackground(isPrimary ? new Color(100, 150, 255) : new Color(35, 35, 40));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isPrimary ? new Color(100, 150, 255) : new Color(50, 50, 55)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        return button;
    }

    private JPanel createStatusSection() {
        JPanel statusSection = new JPanel(new BorderLayout());
        statusSection.setBackground(new Color(25, 25, 30));
        statusSection.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 55)));

        JBLabel statusTitleLabel = new JBLabel("STATUS");
        statusTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        statusTitleLabel.setForeground(new Color(120, 120, 130));
        statusTitleLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));

        JPanel ctxBar = new JPanel(new BorderLayout());
        ctxBar.setBackground(new Color(25, 25, 30));
        ctxBar.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JProgressBar ctxProgress = new JProgressBar(0, 100);
        ctxProgress.setValue(0);
        ctxProgress.setStringPainted(false);
        ctxProgress.setBackground(new Color(45, 45, 50));
        ctxProgress.setForeground(new Color(100, 150, 255));
        ctxProgress.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        ctxBar.add(ctxProgress, BorderLayout.NORTH);

        JPanel ctxLabels = new JPanel(new BorderLayout());
        ctxLabels.setBackground(new Color(25, 25, 30));
        ctxUsedLabel = new JLabel("0 tok");
        ctxUsedLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        ctxUsedLabel.setForeground(new Color(100, 100, 110));
        JLabel ctxWindowLabel = new JLabel("0 tok");
        ctxWindowLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        ctxWindowLabel.setForeground(new Color(100, 100, 110));
        ctxWindowLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        ctxLabels.add(ctxUsedLabel, BorderLayout.WEST);
        ctxLabels.add(ctxWindowLabel, BorderLayout.EAST);
        ctxBar.add(ctxLabels, BorderLayout.CENTER);

        JPanel metrics = new JPanel(new GridLayout(1, 3, 0, 0));
        metrics.setBackground(new Color(25, 25, 30));
        metrics.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

        cacheLabel = new JLabel("—", SwingConstants.CENTER);
        cacheLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        cacheLabel.setForeground(Color.WHITE);

        costLabel = new JLabel("—", SwingConstants.CENTER);
        costLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        costLabel.setForeground(Color.WHITE);

        balanceLabel = new JLabel("—", SwingConstants.CENTER);
        balanceLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 11));
        balanceLabel.setForeground(new Color(100, 150, 255));

        metrics.add(createMetricItem(cacheLabel, "Cache"));
        metrics.add(createMetricItem(costLabel, "Cost"));
        metrics.add(createMetricItem(balanceLabel, "Balance"));

        JPanel modelStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
        modelStatus.setBackground(new Color(25, 25, 30));
        modelStatus.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));

        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(5, 5));
        dot.setBackground(apiClient.isConnected() ? new Color(100, 200, 100) : new Color(200, 100, 100));

        modelLabel = new JLabel("-");
        modelLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        modelLabel.setForeground(new Color(100, 100, 110));

        modelStatus.add(dot);
        modelStatus.add(modelLabel);

        JPanel statusContent = new JPanel();
        statusContent.setLayout(new BoxLayout(statusContent, BoxLayout.Y_AXIS));
        statusContent.setBackground(new Color(25, 25, 30));
        statusContent.add(statusTitleLabel);
        statusContent.add(ctxBar);
        statusContent.add(metrics);
        statusContent.add(modelStatus);

        statusSection.add(statusContent, BorderLayout.CENTER);
        return statusSection;
    }

    private JPanel createMetricItem(JLabel value, String label) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 30));
        panel.add(value);
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lbl.setForeground(new Color(100, 100, 110));
        panel.add(lbl);
        return panel;
    }

    private JPanel createContentArea() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(30, 30, 35));

        // 使用 ChatContainerManager（MarkdownJCEFHtmlPanel）作为消息渲染区域
        chatContainer = new ChatContainerManager(project);

        // 辅助组件面板（Approval 等）
        messageArea = new JPanel();
        messageArea.setLayout(new BoxLayout(messageArea, BoxLayout.Y_AXIS));
        messageArea.setBackground(new Color(30, 30, 35));
        messageArea.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        // 主面板：JCEF 在上，辅助组件在下
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 35));
        mainPanel.add(chatContainer.getComponent(), BorderLayout.CENTER);
        mainPanel.add(messageArea, BorderLayout.SOUTH);

        scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(30, 30, 35));
        scrollPane.getViewport().setBackground(new Color(30, 30, 35));
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        welcomePanel = createWelcomePanel();
        
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        centerWrapper.add(welcomePanel, BorderLayout.SOUTH);
        
        content.add(centerWrapper, BorderLayout.CENTER);

        // 底部固定区域：计费面板 + 输入框
        JPanel bottomArea = new JPanel(new BorderLayout());
        bottomArea.setBackground(new Color(30, 30, 35));

        // 计费面板（固定不动，不随内容滚动）
        usagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        usagePanel.setBackground(new Color(30, 30, 35));
        usagePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 50, 55)),
                BorderFactory.createEmptyBorder(4, 20, 4, 20)));

        usageTotalLabel = createUsageLabel("Total", "—");
        usageInLabel = createUsageLabel("In", "—", true);
        usageOutLabel = createUsageLabel("Out", "—");
        usageCostLabel = createUsageLabel("Cost", "—");

        usagePanel.add(usageTotalLabel);
        usagePanel.add(usageInLabel);
        usagePanel.add(usageOutLabel);
        usagePanel.add(usageCostLabel);

        bottomArea.add(usagePanel, BorderLayout.NORTH);

        JPanel footer = createFooter();
        bottomArea.add(footer, BorderLayout.SOUTH);

        content.add(bottomArea, BorderLayout.SOUTH);

        return content;
    }

    private JPanel createWelcomePanel() {
        JPanel welcome = new JPanel();
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
        welcome.setBackground(new Color(30, 30, 35));
        welcome.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

        JBLabel logo = new JBLabel("R");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(Color.WHITE);
        logo.setBackground(new Color(100, 150, 255));
        logo.setOpaque(true);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setVerticalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(52, 52));

        JBLabel title = new JBLabel("Reasonix");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JBLabel tag = new JBLabel("AI coding agent");
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tag.setForeground(new Color(200, 200, 210));
        tag.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcome.add(Box.createVerticalGlue());
        welcome.add(logo);
        welcome.add(Box.createVerticalStrut(16));
        welcome.add(title);
        welcome.add(Box.createVerticalStrut(6));
        welcome.add(tag);
        welcome.add(Box.createVerticalGlue());

        return welcome;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(new Color(30, 30, 35));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 12, 20));

        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setBackground(new Color(30, 30, 35));

        JButton autoBtn = createToolbarButton("Auto", true);
        autoBtn.addActionListener(e -> setMode("auto"));
        JButton planBtn = createToolbarButton("Plan", false);
        planBtn.addActionListener(e -> setMode("plan"));
        JButton yoloBtn = createToolbarButton("YOLO", false);
        yoloBtn.addActionListener(e -> setMode("yolo"));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        statusLabel.setForeground(new Color(150, 150, 160));

        JPanel statusDot = new JPanel();
        statusDot.setPreferredSize(new Dimension(5, 5));
        statusDot.setMaximumSize(new Dimension(5, 5));
        statusDot.setBackground(apiClient.isConnected() ? new Color(100, 180, 100) : new Color(100, 100, 110));

        toolbar.add(autoBtn);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(planBtn);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(yoloBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(statusDot);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(statusLabel);
        toolbar.add(Box.createHorizontalGlue());

        JPanel composer = new JPanel();
        composer.setLayout(new BoxLayout(composer, BoxLayout.X_AXIS));
        composer.setBackground(new Color(40, 40, 45));
        composer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 65)),
                BorderFactory.createEmptyBorder(4, 12, 4, 4)
        ));

        JLabel caret = new JLabel("\u203A");
        caret.setFont(new Font("JetBrains Mono", Font.BOLD, 16));
        caret.setForeground(new Color(100, 150, 255));
        caret.setPreferredSize(new Dimension(20, 24));

        inputTextArea = new JBTextArea();
        inputTextArea.setText("");
        inputTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputTextArea.setForeground(Color.WHITE);
        inputTextArea.setBackground(new Color(40, 40, 45));
        inputTextArea.setCaretColor(Color.WHITE);
        inputTextArea.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setRows(1);
        inputTextArea.setMinimumSize(new Dimension(100, 24));
        
        // Add keyboard shortcuts
        inputTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    onSend();
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    if (running) {
                        onStop();
                    }
                }
            }
        });

        sendButton = new JButton("↑");
        sendButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sendButton.setForeground(Color.WHITE);
        sendButton.setPreferredSize(new Dimension(34, 34));
        sendButton.setMaximumSize(new Dimension(34, 34));
        sendButton.setBackground(new Color(100, 150, 255));
        sendButton.setBorder(BorderFactory.createEmptyBorder());
        sendButton.setFocusPainted(false);
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.setToolTipText("Send (Enter)");
        sendButton.addActionListener(e -> onSend());

        stopButton = new JButton("■");
        stopButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        stopButton.setForeground(new Color(200, 80, 80));
        stopButton.setPreferredSize(new Dimension(34, 34));
        stopButton.setMaximumSize(new Dimension(34, 34));
        stopButton.setBackground(new Color(60, 50, 50));
        stopButton.setBorder(BorderFactory.createEmptyBorder());
        stopButton.setFocusPainted(false);
        stopButton.setVisible(false);
        stopButton.setToolTipText("Cancel (Esc)");
        stopButton.addActionListener(e -> onStop());

        composer.add(caret);
        composer.add(inputTextArea);
        composer.add(Box.createHorizontalStrut(7));
        composer.add(sendButton);
        composer.add(stopButton);

        footer.add(toolbar);
        footer.add(Box.createVerticalStrut(8));
        footer.add(composer);

        return footer;
    }

    private JButton createToolbarButton(String text, boolean isActive) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setForeground(isActive ? new Color(100, 150, 255) : new Color(150, 150, 160));
        button.setBackground(isActive ? new Color(100, 150, 255, 30) : new Color(35, 35, 40));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isActive ? new Color(100, 150, 255, 50) : new Color(50, 50, 55)),
                BorderFactory.createEmptyBorder(4, 9, 4, 9)
        ));
        button.setFocusPainted(false);
        return button;
    }

    private void onSend() {
        String text = inputTextArea.getText().trim();
        if (text.isEmpty() || running) return;

        // Handle slash commands
        if (text.startsWith("/")) {
            handleSlashCommand(text);
            inputTextArea.setText("");
            return;
        }

        // Add user message
        chatContainer.commitUserMessage(text);
        inputTextArea.setText("");
        
        running = true;
        updateRunningState();

        // Send to API
        apiClient.submit(text);
    }

    private void handleSlashCommand(String text) {
        String[] parts = text.split(" ", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "/compact" -> apiClient.compact();
            case "/new" -> onNewSession();
            case "/rewind" -> onRewind();
            case "/tree" -> onBranches();
            case "/model" -> {
                if (!args.isEmpty()) {
                    apiClient.submit("/model " + args);
                } else {
                    showNotice("warn", "Usage: /model <model-name>");
                }
            }
            case "/effort" -> {
                if (!args.isEmpty()) {
                    apiClient.submit("/effort " + args);
                } else {
                    showNotice("warn", "Usage: /effort <level>");
                }
            }
            case "/skill" -> {
                List<SkillInfo> skills = apiClient.getSkills();
                if (skills != null) {
                    StringBuilder sb = new StringBuilder("Available skills:\n");
                    for (SkillInfo skill : skills) {
                        sb.append("- ").append(skill.name);
                        if (skill.description != null) {
                            sb.append(": ").append(skill.description);
                        }
                        sb.append("\n");
                    }
                    showNotice("info", sb.toString());
                }
            }
            case "/forget" -> {
                if (!args.isEmpty()) {
                    apiClient.forget(args);
                } else {
                    showNotice("warn", "Usage: /forget <memory-name>");
                }
            }
            case "/help" -> showNotice("info", 
                "Available commands:\n" +
                "/compact - Compact session\n" +
                "/new - New session\n" +
                "/rewind - Rewind to checkpoint\n" +
                "/tree - Show branch tree\n" +
                "/model <name> - Switch model\n" +
                "/effort <level> - Set effort level\n" +
                "/skill - List skills\n" +
                "/forget <name> - Forget memory\n" +
                "/help - Show this help");
            default -> showNotice("warn", "Unknown command: " + command + ". Type /help for available commands.");
        }
    }

    private void onStop() {
        running = false;
        updateRunningState();
        apiClient.cancel();
    }

    private void updateRunningState() {
        sendButton.setVisible(!running);
        stopButton.setVisible(running);
        statusLabel.setText(running ? "Thinking..." : "Ready");
    }

    private void setMode(String mode) {
        if (mode.equals("plan")) {
            apiClient.plan(true);
            apiClient.bypass(false);
        } else if (mode.equals("yolo")) {
            apiClient.plan(false);
            apiClient.bypass(true);
        } else {
            // auto mode - disable both
            apiClient.plan(false);
            apiClient.bypass(false);
        }
        
        // Refresh status after mode change
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            fetchStatus();
        });
    }

    private void fetchStatus() {
        StatusInfo status = apiClient.getStatus();
        if (status != null) {
            updateStatusDisplay(status);
        }
    }

    private void updateStatusDisplay(StatusInfo status) {
        if (status.label != null) {
            modelLabel.setText(status.label);
        }
        if (status.getBalanceDisplay() != null) {
            balanceLabel.setText(status.getBalanceDisplay());
        }
        ctxUsedLabel.setText(status.used + "/" + status.window);
    }

    private void onNewSession() {
        apiClient.newSession();
        chatContainer.clearHistory();
        welcomePanel.setVisible(true);
    }

    private void onCompact() {
        apiClient.compact();
    }

    private void onRewind() {
        List<Checkpoint> checkpoints = apiClient.getCheckpoints();
        if (checkpoints == null || checkpoints.isEmpty()) {
            showNotice("info", "No checkpoints available");
            return;
        }
        // TODO: Show checkpoint selector dialog
        // For now, just rewind to the first checkpoint
        apiClient.rewind(0);
        chatContainer.clearHistory();
    }

    private void onBranches() {
        BranchesInfo branches = apiClient.getBranches();
        if (branches == null) {
            showNotice("warn", "Failed to load branches");
            return;
        }

        // Show tree first
        if (branches.tree != null) {
            showNotice("info", "Branch Tree:\n" + branches.tree);
        }

        // Show branches list
        if (branches.branches != null && !branches.branches.isEmpty()) {
            StringBuilder sb = new StringBuilder("Sessions:\n");
            for (BranchesInfo.BranchItem branch : branches.branches) {
                sb.append("- ").append(branch.id);
                if (branch.turns > 0) {
                    sb.append(" (").append(branch.turns).append(" turns)");
                }
                if (branch.preview != null && !branch.preview.isEmpty()) {
                    sb.append(": ").append(branch.preview);
                }
                sb.append("\n");
            }
            showNotice("info", sb.toString());
        }
    }

    public JPanel getContent() {
        return contentPanel;
    }

    public void dispose() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        apiClient.shutdown();
    }
}