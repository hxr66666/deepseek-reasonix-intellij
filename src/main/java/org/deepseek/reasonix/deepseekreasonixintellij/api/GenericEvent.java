package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public  class GenericEvent extends ApiEvent implements Serializable {
    public final String rawJson;

    public GenericEvent(String kind, String json) {
        this.kind = kind;
        this.rawJson = json;
    }

    public String getRawJson() {
        return rawJson;
    }
}
