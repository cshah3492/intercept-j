package com.chiragshah.interceptj.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ToolResult<T>(

        @NotNull
        UUID requestId,

        @NotNull
        ToolExecutionStatus status,

        T output,

        String message,

        @NotNull
        Instant completedAt,

        @PositiveOrZero
        long executionTimeNanos) {

    public ToolResult {
        Objects.requireNonNull(requestId, "requestId cannot be null");
        Objects.requireNonNull(status, "status cannot be null");
        Objects.requireNonNull(completedAt, "completedAt cannot be null");

        if (executionTimeNanos < 0) {
            throw new IllegalArgumentException(
                    "executionTimeNanos cannot be negative");
        }
    }

    public boolean wasExecuted() {
        return status == ToolExecutionStatus.EXECUTED;
    }
}