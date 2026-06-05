package org.deepseek.reasonix.deepseekreasonixintellij.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class AskDialog extends DialogWrapper {
    private String askId;
    private List<QuestionData> questions;
    private boolean submitted = false;
    private List<String> selectedAnswers;

    private static final Color BG_350 = new Color(35, 35, 40);
    private static final Color BG_450 = new Color(45, 45, 50);
    private static final Color GRAY_600 = new Color(60, 60, 65);
    private static final Color GRAY_1500 = new Color(150, 150, 160);
    private static final Color LINK_COLOR = new Color(100, 150, 255);

    private static class QuestionData {
        String prompt;
        List<OptionData> options;
        boolean multiSelect;

        QuestionData(String prompt, List<OptionData> options, boolean multiSelect) {
            this.prompt = prompt;
            this.options = options;
            this.multiSelect = multiSelect;
        }
    }

    private static class OptionData {
        String label;
        String description;

        OptionData(String label, String description) {
            this.label = label;
            this.description = description;
        }
    }

    public AskDialog(String id, List<QuestionData> questions) {
        super(true);
        this.askId = id;
        this.questions = questions;
        this.selectedAnswers = new ArrayList<>();

        setTitle("Answer Required");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JBPanel<?> content = new JBPanel<>();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_450);
        content.setBorder(JBUI.Borders.empty(16, 20));

        for (int i = 0; i < questions.size(); i++) {
            QuestionData q = questions.get(i);
            JBPanel<?> questionPanel = createQuestionPanel(q, i);
            content.add(questionPanel);
            if (i < questions.size() - 1) {
                content.add(Box.createVerticalStrut(12));
            }
        }

        return content;
    }

    private JBPanel<?> createQuestionPanel(QuestionData q, int questionIndex) {
        JBPanel<?> panel = new JBPanel<>();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_450);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JBLabel promptLabel = new JBLabel(q.prompt);
        promptLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        promptLabel.setForeground(Color.WHITE);
        promptLabel.setBorder(JBUI.Borders.empty(0, 0, 10, 0));

        JBPanel<?> optionsPanel = new JBPanel<>();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(BG_450);

        Set<Integer> selected = new HashSet<>();

        for (int i = 0; i < q.options.size(); i++) {
            OptionData opt = q.options.get(i);
            JBPanel<?> optionPanel = new JBPanel<>();
            optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.X_AXIS));
            optionPanel.setBackground(BG_350);
            optionPanel.setBorder(BorderFactory.createCompoundBorder(
                JBUI.Borders.customLine(GRAY_600),
                JBUI.Borders.empty(8, 10)
            ));
            optionPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JBCheckBox checkbox = new JBCheckBox();
            checkbox.setBackground(BG_350);
            checkbox.setBorder(JBUI.Borders.empty());
            checkbox.setFocusPainted(false);

            JBPanel<?> textPanel = new JBPanel<>();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setBackground(BG_350);

            JBLabel label = new JBLabel(opt.label);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(Color.WHITE);

            textPanel.add(label);

            if (opt.description != null && !opt.description.isEmpty()) {
                JBLabel desc = new JBLabel(opt.description);
                desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                desc.setForeground(GRAY_1500);
                desc.setBorder(JBUI.Borders.empty(2, 0, 0, 0));
                textPanel.add(desc);
            }

            optionPanel.add(checkbox);
            optionPanel.add(Box.createHorizontalStrut(8));
            optionPanel.add(textPanel);

            final int idx = i;
            optionPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (q.multiSelect) {
                        if (selected.contains(idx)) {
                            selected.remove(idx);
                            checkbox.setSelected(false);
                            optionPanel.setBorder(BorderFactory.createCompoundBorder(
                                JBUI.Borders.customLine(GRAY_600),
                                JBUI.Borders.empty(8, 10)
                            ));
                        } else {
                            selected.add(idx);
                            checkbox.setSelected(true);
                            optionPanel.setBorder(BorderFactory.createCompoundBorder(
                                JBUI.Borders.customLine(LINK_COLOR),
                                JBUI.Borders.empty(8, 10)
                            ));
                        }
                    } else {
                        selected.clear();
                        for (Component c : optionsPanel.getComponents()) {
                            if (c instanceof JBPanel) {
                                ((JBPanel<?>) c).setBorder(BorderFactory.createCompoundBorder(
                                    JBUI.Borders.customLine(GRAY_600),
                                    JBUI.Borders.empty(8, 10)
                                ));
                            }
                        }
                        selected.add(idx);
                        checkbox.setSelected(true);
                        optionPanel.setBorder(BorderFactory.createCompoundBorder(
                            JBUI.Borders.customLine(LINK_COLOR),
                            JBUI.Borders.empty(8, 10)
                        ));
                    }
                }
            });

            optionsPanel.add(optionPanel);
            optionsPanel.add(Box.createVerticalStrut(5));
        }

        panel.add(promptLabel);
        panel.add(optionsPanel);

        return panel;
    }

    @Override
    protected void doOKAction() {
        submitted = true;
        super.doOKAction();
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public List<String> getSelectedAnswers() {
        return selectedAnswers;
    }

    public String getAskId() {
        return askId;
    }
}