package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AskRequestEvent extends ApiEvent implements Serializable {
    @JsonProperty("ask")
    public WireAsk ask;

    public AskRequestEvent() {
        this.kind = "ask_request";
    }

    public WireAsk getAsk() {
        return ask;
    }

    public void setAsk(WireAsk ask) {
        this.ask = ask;
    }
}