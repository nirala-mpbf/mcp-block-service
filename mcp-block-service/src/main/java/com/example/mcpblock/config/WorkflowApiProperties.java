package com.example.mcpblock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "workflow.api")
public record WorkflowApiProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout) {

    public WorkflowApiProperties {
        if (baseUrl == null) {
            throw new IllegalArgumentException("workflow.api.base-url is required");
        }
        String scheme = baseUrl.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("workflow.api.base-url must use HTTP or HTTPS");
        }
        if (baseUrl.getHost() == null || baseUrl.getHost().isBlank()) {
            throw new IllegalArgumentException("workflow.api.base-url must contain a valid host");
        }
        if (connectTimeout == null || connectTimeout.isNegative() || connectTimeout.isZero()) {
            connectTimeout = Duration.ofSeconds(5);
        }
        if (readTimeout == null || readTimeout.isNegative() || readTimeout.isZero()) {
            readTimeout = Duration.ofSeconds(30);
        }
    }
}
