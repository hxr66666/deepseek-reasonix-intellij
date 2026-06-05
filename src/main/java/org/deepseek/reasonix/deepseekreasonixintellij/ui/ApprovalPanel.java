package org.deepseek.reasonix.deepseekreasonixintellij.ui;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

public class ApprovalPanel extends JPanel {
    private static final Color BG_450 = new Color(45, 45, 50);
    private static final Color BG_500 = new Color(50, 50, 55);
    private static final Color BLUE = new Color(100, 150, 255);
    private static final Color RED = new Color(200, 80, 80);
    private static final Color TEXT_PRIMARY = new Color(220, 220, 225);
    private static final Color TEXT_SECONDARY = new Color(150, 150, 160);

    private final String approvalId;
    private final String tool;
    private final String subject;
    private final BiConsumer<Boolean, Boolean> onApproval;

    public ApprovalPanel(String approvalId, String tool, String subject, BiConsumer<Boolean, Boolean> onApproval) {
        this.approvalId = approvalId;
        this.tool = tool;
        this.subject = subject;
        this.onApproval = onApproval;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG_450);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 3, 1, 1, BLUE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Tool name
        JLabel toolLabel = new JLabel("Tool Call: " + tool);
        toolLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        toolLabel.setForeground(TEXT_PRIMARY);
        toolLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Subject
        JLabel subjectLabel = new JLabel(truncate(subject, 200));
        subjectLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subjectLabel.setForeground(TEXT_SECONDARY);
        subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setBackground(BG_450);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Deny button
        JButton denyBtn = createButton("Deny", RED, false);
        denyBtn.addActionListener(e -> {
            onApproval.accept(false, false);
            setEnabled(false);
        });

        // Allow once button
        JButton allowOnceBtn = createButton("Allow Once", TEXT_PRIMARY, true);
        allowOnceBtn.addActionListener(e -> {
            onApproval.accept(true, false);
            setEnabled(false);
        });

        // Allow session button
        JButton allowSessionBtn = createButton("Allow Session", BLUE, true);
        allowSessionBtn.addActionListener(e -> {
            onApproval.accept(true, true);
            setEnabled(false);
        });

        buttonsPanel.add(denyBtn);
        buttonsPanel.add(Box.createHorizontalStrut(8));
        buttonsPanel.add(allowOnceBtn);
        buttonsPanel.add(Box.createHorizontalStrut(8));
        buttonsPanel.add(allowSessionBtn);
        buttonsPanel.add(Box.createHorizontalGlue());

        add(toolLabel);
        add(Box.createVerticalStrut(6));
        add(subjectLabel);
        add(buttonsPanel);
    }

    private JButton createButton(String text, Color textColor, boolean outlined) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(textColor);
        if (outlined) {
            button.setBackground(BG_450);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(textColor, 1),
                    BorderFactory.createEmptyBorder(6, 14, 6, 14)
            ));
        } else {
            button.setBackground(textColor);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        }
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Component c : getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof JPanel) {
                for (Component cc : ((JPanel) c).getComponents()) {
                    cc.setEnabled(enabled);
                }
            }
        }
    }
}
