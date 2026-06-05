package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public  class MessageEvent extends ApiEvent implements Serializable {
    public MessageEvent() {
        this.kind = "message";
    }
}
