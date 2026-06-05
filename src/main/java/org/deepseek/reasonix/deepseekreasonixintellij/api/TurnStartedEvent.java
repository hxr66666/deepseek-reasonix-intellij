package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public  class TurnStartedEvent extends ApiEvent implements Serializable {
    public TurnStartedEvent() {
        this.kind = "turn_started";
    }
}
