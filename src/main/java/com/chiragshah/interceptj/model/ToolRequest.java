package com.chiragshah.interceptj.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ToolRequest<T extends ToolArguments>(

        @NotNull
        UUID requestId,

        @NotBlank
        String scenarioId,

        @NotBlank
        String toolName,

        @NotNull
        @Valid
        T arguments,

        @NotNull
        @Valid
        UserContext userContext,

        @NotNull
        Instant requestedAt) {

    public ToolRequest {
        Objects.requireNonNull(requestId, "requestId cannot be null");
        Objects.requireNonNull(scenarioId, "scenarioId cannot be null");
        Objects.requireNonNull(toolName, "toolName cannot be null");
        Objects.requireNonNull(arguments, "arguments cannot be null");
        Objects.requireNonNull(userContext, "userContext cannot be null");
        Objects.requireNonNull(requestedAt, "requestedAt cannot be null");

        scenarioId = scenarioId.trim();
        toolName = toolName.trim();
    }
}