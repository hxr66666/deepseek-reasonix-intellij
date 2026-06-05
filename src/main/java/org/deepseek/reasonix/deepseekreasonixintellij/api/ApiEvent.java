package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "kind"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TurnStartedEvent.class, name = "turn_started"),
        @JsonSubTypes.Type(value = ReasoningEvent.class, name = "reasoning"),
        @JsonSubTypes.Type(value = TextEvent.class, name = "text"),
        @JsonSubTypes.Type(value = MessageEvent.class, name = "message"),
        @JsonSubTypes.Type(value = TurnDoneEvent.class, name = "turn_done"),
        @JsonSubTypes.Type(value = ToolDispatchEvent.class, name = "tool_dispatch"),
        @JsonSubTypes.Type(value = ToolResultEvent.class, name = "tool_result"),
        @JsonSubTypes.Type(value = ToolProgressEvent.class, name = "tool_progress"),
        @JsonSubTypes.Type(value = ApprovalRequestEvent.class, name = "approval_request"),
        @JsonSubTypes.Type(value = AskRequestEvent.class, name = "ask_request"),
        @JsonSubTypes.Type(value = UsageEvent.class, name = "usage"),
        @JsonSubTypes.Type(value = NoticeEvent.class, name = "notice"),
        @JsonSubTypes.Type(value = PhaseEvent.class, name = "phase"),
        @JsonSubTypes.Type(value = CompactionStartedEvent.class, name = "compaction_started"),
        @JsonSubTypes.Type(value = CompactionDoneEvent.class, name = "compaction_done"),
        @JsonSubTypes.Type(value = GenericEvent.class, name = "generic")
})
public abstract class ApiEvent implements Serializable {
    @JsonProperty("kind")
    public String kind;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}