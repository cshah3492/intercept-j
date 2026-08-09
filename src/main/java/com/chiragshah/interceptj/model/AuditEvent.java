package com.chiragshah.interceptj.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AuditEvent(

        @NotNull
        UUID eventId,

        @NotNull
        UUID requestId,

        @NotBlank
        String scenarioId,

        @NotNull
        ExperimentMode experimentMode,

        @NotBlank
        String toolName,

        @NotBlank
        String userId,

        @NotNull
        DecisionOutcome policyOutcome,

        @NotNull
        ToolExecutionStatus executionStatus,

        @NotBlank
        String reason,

        @NotNull
        Instant occurredAt,

        @PositiveOrZero
        long totalDurationNanos) {

    public AuditEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(requestId, "requestId cannot be null");
        Objects.requireNonNull(scenarioId, "scenarioId cannot be null");
        Objects.requireNonNull(experimentMode, "experimentMode cannot be null");
        Objects.requireNonNull(toolName, "toolName cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(policyOutcome, "policyOutcome cannot be null");
        Objects.requireNonNull(executionStatus, "executionStatus cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");

        scenarioId = scenarioId.trim();
        toolName = toolName.trim();
        userId = userId.trim();
        reason = reason.trim();

        if (totalDurationNanos < 0) {
            throw new IllegalArgumentException(
                    "totalDurationNanos cannot be negative");
        }
    }
}