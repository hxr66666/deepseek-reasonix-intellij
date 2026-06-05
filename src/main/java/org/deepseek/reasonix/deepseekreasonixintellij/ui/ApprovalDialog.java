package org.deepseek.reasonix.deepseekreasonixintellij.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ApprovalDialog extends JDialog {
    private String approvalId;
    private boolean approved = false;
    private boolean sessionApprove = false;

    public ApprovalDialog(String id, String toolName, String subject) {
        this.approvalId = id;
        setTitle("Approval Required");
        setModal(true);
        setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(45, 45, 50));
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(new Color(45, 45, 50));

        JLabel icon = new JLabel("\u26A0");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        icon.setForeground(new Color(255, 200, 80));

        JLabel title = new JLabel("Approval Required");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(Color.WHITE);

        header.add(icon);
        header.add(Box.createHorizontalStrut(8));
        header.add(title);

        // Subject
        JPanel subjectPanel = new JPanel();
        subjectPanel.setLayout(new BoxLayout(subjectPanel, BoxLayout.Y_AXIS));
        subjectPanel.setBackground(new Color(35, 35, 40));
        subjectPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 65)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JTextArea subjectText = new JTextArea(toolName + (subject != null ? " — " + subject : ""));
        subjectText.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        subjectText.setForeground(new Color(200, 200, 210));
        subjectText.setBackground(new Color(35, 35, 40));
        subjectText.setEditable(false);
        subjectText.setLineWrap(true);
        subjectText.setWrapStyleWord(true);
        subjectText.setBorder(BorderFactory.createEmptyBorder());
        subjectPanel.add(subjectText);

        // Buttons
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.setBackground(new Color(45, 45, 50));
        buttons.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton allowBtn = new JButton("Allow");
        allowBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        allowBtn.setForeground(new Color(100, 150, 255));
        allowBtn.setBackground(new Color(100, 150, 255, 30));
        allowBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 150, 255)),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        allowBtn.setFocusPainted(false);
        allowBtn.addActionListener(e -> {
            approved = true;
            sessionApprove = false;
            dispose();
        });

        JButton sessionBtn = new JButton("Session");
        sessionBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sessionBtn.setForeground(new Color(100, 150, 255));
        sessionBtn.setBackground(new Color(100, 150, 255, 30));
        sessionBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 150, 255)),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        sessionBtn.setFocusPainted(false);
        sessionBtn.addActionListener(e -> {
            approved = true;
            sessionApprove = true;
            dispose();
        });

        JButton denyBtn = new JButton("Deny");
        denyBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        denyBtn.setForeground(new Color(200, 200, 210));
        denyBtn.setBackground(new Color(35, 35, 40));
        denyBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 65)),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        denyBtn.setFocusPainted(false);
        denyBtn.addActionListener(e -> {
            approved = false;
            dispose();
        });

        buttons.add(allowBtn);
        buttons.add(Box.createHorizontalStrut(8));
        buttons.add(sessionBtn);
        buttons.add(Box.createHorizontalStrut(8));
        buttons.add(denyBtn);
        buttons.add(Box.createHorizontalGlue());

        // Keyboard hints
        JPanel hints = new JPanel();
        hints.setLayout(new BoxLayout(hints, BoxLayout.X_AXIS));
        hints.setBackground(new Color(45, 45, 50));
        hints.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel hintY = new JLabel("[Y] Allow  ");
        hintY.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        hintY.setForeground(new Color(100, 100, 110));

        JLabel hintA = new JLabel("[A] Session  ");
        hintA.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        hintA.setForeground(new Color(100, 100, 110));

        JLabel hintN = new JLabel("[N] Deny");
        hintN.setFont(new Font("JetBrains Mono", Font.PLAIN, 10));
        hintN.setForeground(new Color(100, 100, 110));

        hints.add(hintY);
        hints.add(hintA);
        hints.add(hintN);

        content.add(header);
        content.add(Box.createVerticalStrut(12));
        content.add(subjectPanel);
        content.add(buttons);
        content.add(hints);

        setContentPane(content);
        pack();
        setLocationRelativeTo(null);
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean isSessionApprove() {
        return sessionApprove;
    }

    public String getApprovalId() {
        return approvalId;
    }
}
