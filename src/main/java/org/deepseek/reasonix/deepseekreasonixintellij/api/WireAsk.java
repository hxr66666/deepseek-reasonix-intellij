package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WireAsk {
    @JsonProperty("id")
    public String id;
    @JsonProperty("questions")
    public List<WireAskQuestion> questions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<WireAskQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<WireAskQuestion> questions) {
        this.questions = questions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WireAskQuestion {
        @JsonProperty("id")
        public String id;
        @JsonProperty("header")
        public String header;
        @JsonProperty("prompt")
        public String prompt;
        @JsonProperty("options")
        public List<WireAskOption> options;
        @JsonProperty("multi")
        public boolean multi;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public List<WireAskOption> getOptions() {
            return options;
        }

        public void setOptions(List<WireAskOption> options) {
            this.options = options;
        }

        public boolean isMulti() {
            return multi;
        }

        public void setMulti(boolean multi) {
            this.multi = multi;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WireAskOption {
        @JsonProperty("label")
        public String label;
        @JsonProperty("description")
        public String description;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}