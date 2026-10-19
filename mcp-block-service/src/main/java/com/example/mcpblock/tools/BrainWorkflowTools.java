package com.example.mcpblock.tools;

import com.example.mcpblock.service.WorkflowApiClient;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class BrainWorkflowTools {

    private final WorkflowApiClient workflowApiClient;

    public BrainWorkflowTools(WorkflowApiClient workflowApiClient) {
        this.workflowApiClient = workflowApiClient;
    }

    @McpTool(
            name = "verify_otp",
            title = "Verify OTP workflow step",
            description = "Calls POST /workflows/otp/verify with {referenceId}. Returns the actual workflow HTTP status and response body. Only call when the orchestration layer has authorized Step 1."
    )
    public String verifyOtp(
            @McpToolParam(description = "Workflow reference/user ID, for example R2", required = true)
            String referenceId) {
        return workflowApiClient.postReferenceId(
                "/workflows/otp/verify", referenceId);
    }

    @McpTool(
            name = "submit_profile",
            title = "Submit profile details workflow step",
            description = "Calls POST /workflows/submit-profile with {referenceId}. Returns the actual workflow HTTP status and response body. Only call when the orchestration layer has authorized Step 2."
    )
    public String submitProfile(
            @McpToolParam(description = "Workflow reference/user ID, for example R2", required = true)
            String referenceId) {
        return workflowApiClient.postReferenceId(
                "/workflows/submit-profile", referenceId);
    }

    @McpTool(
            name = "submit_account_statement",
            title = "Submit account statement workflow step",
            description = "Calls POST /workflows/enter-account-statement with {referenceId}. Returns the actual workflow HTTP status and response body. Only call when the orchestration layer has authorized Step 3."
    )
    public String submitAccountStatement(
            @McpToolParam(description = "Workflow reference/user ID, for example R2", required = true)
            String referenceId) {
        return workflowApiClient.postReferenceId(
                "/workflows/enter-account-statement", referenceId);
    }

    @McpTool(
            name = "submit_udyam_document",
            title = "Submit Udyam document workflow step",
            description = "Calls POST /workflows/udyam-document with {referenceId}. Returns the actual workflow HTTP status and response body. Only call when the orchestration layer has authorized Step 4."
    )
    public String submitUdyamDocument(
            @McpToolParam(description = "Workflow reference/user ID, for example R2", required = true)
            String referenceId) {
        return workflowApiClient.postReferenceId(
                "/workflows/udyam-document", referenceId);
    }
}
