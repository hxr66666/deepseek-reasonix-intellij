package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompactionStartedEvent extends ApiEvent implements Serializable {
    @JsonProperty("compaction")
    public WireCompaction compaction;

    public CompactionStartedEvent() {
        this.kind = "compaction_started";
    }

    public WireCompaction getCompaction() {
        return compaction;
    }

    public void setCompaction(WireCompaction compaction) {
        this.compaction = compaction;
    }
}