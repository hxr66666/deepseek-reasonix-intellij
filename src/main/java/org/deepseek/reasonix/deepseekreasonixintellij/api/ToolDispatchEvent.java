package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolDispatchEvent extends ApiEvent implements Serializable {
    @JsonProperty("tool")
    public WireTool tool;

    public ToolDispatchEvent() {
        this.kind = "tool_dispatch";
    }

    public WireTool getTool() {
        return tool;
    }

    public void setTool(WireTool tool) {
        this.tool = tool;
    }
}