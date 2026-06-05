package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WireUsage {
    @JsonProperty("promptTokens")
    public int promptTokens;
    @JsonProperty("completionTokens")
    public int completionTokens;
    @JsonProperty("totalTokens")
    public int totalTokens;
    @JsonProperty("cacheHitTokens")
    public int cacheHitTokens;
    @JsonProperty("cacheMissTokens")
    public int cacheMissTokens;
    @JsonProperty("reasoningTokens")
    public int reasoningTokens;
    @JsonProperty("sessionCacheHitTokens")
    public int sessionCacheHitTokens;
    @JsonProperty("sessionCacheMissTokens")
    public int sessionCacheMissTokens;
    @JsonProperty("costUsd")
    public double costUsd;

    public int getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }

    public int getCacheHitTokens() {
        return cacheHitTokens;
    }

    public void setCacheHitTokens(int cacheHitTokens) {
        this.cacheHitTokens = cacheHitTokens;
    }

    public int getCacheMissTokens() {
        return cacheMissTokens;
    }

    public void setCacheMissTokens(int cacheMissTokens) {
        this.cacheMissTokens = cacheMissTokens;
    }

    public int getReasoningTokens() {
        return reasoningTokens;
    }

    public void setReasoningTokens(int reasoningTokens) {
        this.reasoningTokens = reasoningTokens;
    }

    public int getSessionCacheHitTokens() {
        return sessionCacheHitTokens;
    }

    public void setSessionCacheHitTokens(int sessionCacheHitTokens) {
        this.sessionCacheHitTokens = sessionCacheHitTokens;
    }

    public int getSessionCacheMissTokens() {
        return sessionCacheMissTokens;
    }

    public void setSessionCacheMissTokens(int sessionCacheMissTokens) {
        this.sessionCacheMissTokens = sessionCacheMissTokens;
    }

    public double getCostUsd() {
        return costUsd;
    }

    public void setCostUsd(double costUsd) {
        this.costUsd = costUsd;
    }
}