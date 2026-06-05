package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsageEvent extends ApiEvent implements Serializable {
    @JsonProperty("usage")
    public WireUsage usage;

    public UsageEvent() {
        this.kind = "usage";
    }

    public WireUsage getUsage() {
        return usage;
    }

    public void setUsage(WireUsage usage) {
        this.usage = usage;
    }
}