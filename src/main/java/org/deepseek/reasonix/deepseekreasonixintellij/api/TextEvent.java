package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public  class TextEvent extends ApiEvent implements Serializable {
    @JsonProperty("text")
    public String text;

    public TextEvent() {
        this.kind = "text";
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
