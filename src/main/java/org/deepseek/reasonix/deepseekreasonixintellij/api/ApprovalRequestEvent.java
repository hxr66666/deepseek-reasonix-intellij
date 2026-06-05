package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApprovalRequestEvent extends ApiEvent implements Serializable {
    @JsonProperty("approval")
    public WireApproval approval;

    public ApprovalRequestEvent() {
        this.kind = "approval_request";
    }

    public WireApproval getApproval() {
        return approval;
    }

    public void setApproval(WireApproval approval) {
        this.approval = approval;
    }
}