package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillInfo {
    @JsonProperty("name")
    public String name;
    @JsonProperty("scope")
    public String scope;
    @JsonProperty("subagent")
    public boolean subagent;
    @JsonProperty("description")
    public String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isSubagent() {
        return subagent;
    }

    public void setSubagent(boolean subagent) {
        this.subagent = subagent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}