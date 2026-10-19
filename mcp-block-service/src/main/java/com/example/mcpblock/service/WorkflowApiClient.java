package com.example.mcpblock.service;

import com.example.mcpblock.config.WorkflowApiProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

@Service
public class WorkflowApiClient {

    private static final Set<String> ALLOWED_PATHS = Set.of(
            "/workflows/otp/verify",
            "/workflows/submit-profile",
            "/workflows/enter-account-statement",
            "/workflows/udyam-document"
    );

    private final RestClient restClient;
    private final WorkflowApiProperties properties;
    private final ObjectMapper objectMapper;

    public WorkflowApiClient(
            RestClient workflowRestClient,
            WorkflowApiProperties properties,
            ObjectMapper objectMapper) {
        this.restClient = workflowRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String postReferenceId(String path, String referenceId) {
        if (referenceId == null || referenceId.isBlank()) {
            return json(Map.of(
                    "success", false,
                    "httpStatus", 400,
                    "endpoint", safePath(path),
                    "error", "referenceId is required"
            ));
        }

        if (path == null || !ALLOWED_PATHS.contains(path)) {
            return json(Map.of(
                    "success", false,
                    "httpStatus", 400,
                    "endpoint", path == null ? "" : path,
                    "error", "Workflow endpoint is not allowlisted"
            ));
        }

        try {
            return restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Map.of("referenceId", referenceId))
                    .exchange((request, response) -> {
                        int status = response.getStatusCode().value();
                        String responseBody;
                        try {
                            responseBody = StreamUtils.copyToString(
                                    response.getBody(), StandardCharsets.UTF_8);
                        } catch (IOException e) {
                            responseBody = "Unable to read workflow response body";
                        }

                        return json(Map.of(
                                "success", response.getStatusCode().is2xxSuccessful(),
                                "httpStatus", status,
                                "endpoint", path,
                                "workflowBaseUrl", properties.baseUrl().toString(),
                                "responseBody", responseBody == null ? "" : responseBody
                        ));
                    });
        } catch (RestClientException ex) {
            // Do not include request headers or credentials in tool output.
            return json(Map.of(
                    "success", false,
                    "httpStatus", 502,
                    "endpoint", safePath(path),
                    "error", "Workflow service could not be reached. Check workflow.api.base-url and network access.",
                    "exceptionType", ex.getClass().getSimpleName()
            ));
        }
    }

    private String safePath(String path) {
        return path == null ? "" : path;
    }

    private String json(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
        return "{\"success\":false,\"error\":\"Could not serialize workflow request\"}";
    }
    }
}
