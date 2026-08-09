package com.chiragshah.interceptj.experiment;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.chiragshah.interceptj.model.ExperimentMode;
import com.chiragshah.interceptj.model.ToolExecutionStatus;

public record ToolInvocationObservation(

        UUID observationId,

        UUID requestId,

        String scenarioId,

        ExperimentMode experimentMode,

        String toolName,

        String userId,

        Set<String> roles,

        ToolExecutionStatus executionStatus,

        Instant observedAt,

        long durationNanos) {

    public ToolInvocationObservation {

        Objects.requireNonNull(
                observationId,
                "observationId cannot be null");

        Objects.requireNonNull(
                requestId,
                "requestId cannot be null");

        Objects.requireNonNull(
                scenarioId,
                "scenarioId cannot be null");

        Objects.requireNonNull(
                experimentMode,
                "experimentMode cannot be null");

        Objects.requireNonNull(
                toolName,
                "toolName cannot be null");

        Objects.requireNonNull(
                userId,
                "userId cannot be null");

        Objects.requireNonNull(
                roles,
                "roles cannot be null");

        Objects.requireNonNull(
                executionStatus,
                "executionStatus cannot be null");

        Objects.requireNonNull(
                observedAt,
                "observedAt cannot be null");

        if (durationNanos < 0) {
            throw new IllegalArgumentException(
                    "durationNanos cannot be negative");
        }

        scenarioId = scenarioId.trim();
        toolName = toolName.trim();
        userId = userId.trim();
        roles = Set.copyOf(roles);
    }
}