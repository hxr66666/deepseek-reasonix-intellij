package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AskAnswer {
    @JsonProperty("questionId")
    public String questionId;
    @JsonProperty("selected")
    public List<String> selected;

    public AskAnswer() {}

    public AskAnswer(String questionId, List<String> selected) {
        this.questionId = questionId;
        this.selected = selected;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public List<String> getSelected() {
        return selected;
    }

    public void setSelected(List<String> selected) {
        this.selected = selected;
    }
}