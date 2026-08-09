package com.chiragshah.interceptj.experiment;

import java.time.Instant;
import java.util.UUID;

import com.chiragshah.interceptj.model.DecisionOutcome;
import com.chiragshah.interceptj.model.ExperimentMode;
import com.chiragshah.interceptj.model.ToolExecutionStatus;

public record ExperimentRunResult(

        UUID runId,

        String scenarioId,

        ExperimentMode experimentMode,

        int iteration,

        boolean toolRequested,

        ToolExecutionStatus executionStatus,

        DecisionOutcome policyOutcome,

        boolean unauthorizedExecution,

        boolean attackSucceeded,

        long endToEndDurationNanos,

        Long toolPathDurationNanos,

        Long protectedPathDurationNanos,

        Instant completedAt,

        String errorType) {
}