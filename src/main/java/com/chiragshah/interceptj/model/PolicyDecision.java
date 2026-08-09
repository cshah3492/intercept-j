package com.chiragshah.interceptj.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PolicyDecision(

        @NotNull
        UUID requestId,

        @NotNull
        DecisionOutcome outcome,

        @NotBlank
        String policyId,

        @NotBlank
        String reason,

        @NotNull
        Instant decidedAt,

        @PositiveOrZero
        long evaluationTimeNanos) {

    public PolicyDecision {
        Objects.requireNonNull(requestId, "requestId cannot be null");
        Objects.requireNonNull(outcome, "outcome cannot be null");
        Objects.requireNonNull(policyId, "policyId cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");
        Objects.requireNonNull(decidedAt, "decidedAt cannot be null");

        policyId = policyId.trim();
        reason = reason.trim();

        if (evaluationTimeNanos < 0) {
            throw new IllegalArgumentException(
                    "evaluationTimeNanos cannot be negative");
        }
    }

    public boolean isAllowed() {
        return outcome == DecisionOutcome.ALLOW;
    }
}