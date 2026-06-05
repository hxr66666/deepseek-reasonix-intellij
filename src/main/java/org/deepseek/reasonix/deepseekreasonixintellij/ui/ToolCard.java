package org.deepseek.reasonix.deepseekreasonixintellij.ui;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class ToolCard extends JBPanel<JBPanel<?>> {
    private final String toolName;
    private final String toolArgs;
    private boolean isOpen = false;
    private boolean isSuccess = false;
    private String output = "";

    private JBPanel<?> headerPanel;
    private JBPanel<?> bodyPanel;
    private JBLabel iconLabel;
    private JBLabel nameLabel;
    private JBLabel subjectLabel;

    private static final Color BG_350 = new Color(35, 35, 40);
    private static final Color BG_400 = new Color(40, 40, 45);
    private static final Color GRAY_500 = new Color(50, 50, 55);
    private static final Color GRAY_600 = new Color(60, 60, 65);
    private static final Color GRAY_1000 = new Color(100, 100, 110);
    private static final Color GRAY_1200 = new Color(120, 120, 130);
    private static final Color GRAY_2000 = new Color(200, 200, 210);

    public ToolCard(String id, String name, String args) {
        this.toolName = name;
        this.toolArgs = args;

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_400);
        setBorder(JBUI.Borders.empty(8, 12));

        createHeader();
        createBody();

        add(headerPanel, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
        bodyPanel.setVisible(false);
    }

    private void createHeader() {
        headerPanel = new JBPanel<>();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setBackground(BG_400);
        headerPanel.setBorder(JBUI.Borders.empty(7, 12));
        headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        iconLabel = new JBLabel("");
        iconLabel.setPreferredSize(new Dimension(18, 18));
        iconLabel.setBackground(GRAY_600);
        iconLabel.setOpaque(true);
        iconLabel.setBorder(JBUI.Borders.empty());
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);

        nameLabel = new JBLabel(toolName != null ? toolName : "Unknown");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLabel.setForeground(Color.WHITE);

        String subjectText = toolArgs != null ? toolArgs.substring(0, Math.min(toolArgs.length(), 80)) : "";
        subjectLabel = new JBLabel(subjectText);
        subjectLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        subjectLabel.setForeground(GRAY_1200);
        subjectLabel.setBorder(JBUI.Borders.empty(0, 8));

        JBPanel<?> spacer = new JBPanel<>();
        spacer.setOpaque(false);

        JBLabel chevron = new JBLabel("\u25BC");
        chevron.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        chevron.setForeground(GRAY_1000);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createHorizontalStrut(8));
        headerPanel.add(nameLabel);
        headerPanel.add(subjectLabel);
        headerPanel.add(spacer);
        headerPanel.add(chevron);

        headerPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                toggle();
            }
        });
    }

    private void createBody() {
        bodyPanel = new JBPanel<>();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(BG_350);
        bodyPanel.setBorder(BorderFactory.createCompoundBorder(
            JBUI.Borders.customLine(GRAY_500, 1, 0, 0, 0),
            JBUI.Borders.empty(8, 12)
        ));
        bodyPanel.setVisible(false);
    }

    public void toggle() {
        isOpen = !isOpen;
        bodyPanel.setVisible(isOpen);
        headerPanel.revalidate();
        revalidate();
    }

    public void setSuccess(boolean success) {
        this.isSuccess = success;
        if (success) {
            iconLabel.setBackground(new Color(80, 180, 120, 50));
            nameLabel.setForeground(new Color(100, 200, 140));
        } else {
            iconLabel.setBackground(new Color(200, 80, 80, 50));
            nameLabel.setForeground(new Color(200, 100, 100));
        }
    }

    /**
     * Set the result of the tool execution
     * @param output The output text
     * @param err Error message (null if no error)
     * @param truncated Whether the output was truncated
     */
    public void setResult(String output, String err, boolean truncated) {
        this.output = output != null ? output : "";
        if (err != null && !err.isEmpty()) {
            this.output = "Error: " + err;
            setSuccess(false);
        } else {
            setSuccess(true);
        }
        if (truncated) {
            this.output += "\n...[truncated]";
        }
        updateBody();
    }

    public void appendOutput(String text) {
        output += text;
        updateBody();
    }

    public void setOutput(String text) {
        this.output = text;
        updateBody();
    }

    private void updateBody() {
        bodyPanel.removeAll();
        JBTextArea textArea = new JBTextArea(output.substring(0, Math.min(output.length(), 2000)));
        textArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        textArea.setForeground(GRAY_2000);
        textArea.setBackground(BG_350);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(JBUI.Borders.empty());
        bodyPanel.add(textArea);

        if (output.length() > 2000) {
            JBLabel truncated = new JBLabel("\n...[truncated]");
            truncated.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
            truncated.setForeground(GRAY_1200);
            bodyPanel.add(truncated);
        }

        bodyPanel.revalidate();
        bodyPanel.repaint();
    }
}