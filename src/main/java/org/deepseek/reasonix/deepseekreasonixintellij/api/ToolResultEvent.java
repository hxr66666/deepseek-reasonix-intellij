package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolResultEvent extends ApiEvent implements Serializable {
    @JsonProperty("tool")
    public WireTool tool;

    public ToolResultEvent() {
        this.kind = "tool_result";
    }

    public WireTool getTool() {
        return tool;
    }

    public void setTool(WireTool tool) {
        this.tool = tool;
    }
}