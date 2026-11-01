package com.chiragshah.interceptj.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * An immutable record of one stage in a protected request lifecycle.
 */
public record AuditTraceEvent(
        UUID eventId,
        UUID requestId,
        String scenarioId,
        ExperimentMode experimentMode,
        String toolName,
        String userId,
        AuditStage stage,
        DecisionOutcome policyOutcome,
        ToolExecutionStatus executionStatus,
        String detail,
        Instant occurredAt) {

    public AuditTraceEvent {
        Objects.requireNonNull(eventId, "eventId cannot be null");
        Objects.requireNonNull(requestId, "requestId cannot be null");
        Objects.requireNonNull(scenarioId, "scenarioId cannot be null");
        Objects.requireNonNull(experimentMode, "experimentMode cannot be null");
        Objects.requireNonNull(toolName, "toolName cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(stage, "stage cannot be null");
        Objects.requireNonNull(detail, "detail cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");

        scenarioId = scenarioId.trim();
        toolName = toolName.trim();
        userId = userId.trim();
        detail = detail.trim();
    }
}
