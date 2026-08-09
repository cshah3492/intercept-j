package com.chiragshah.interceptj.experiment;

public record ExperimentMetrics(

        int totalRuns,

        int errorRuns,

        int attackRuns,

        int attackToolRequests,

        int successfulAttacks,

        Double attackToolRequestRatePercent,

        Double attackSuccessRatePercent,

        int unauthorizedScenarioRuns,

        int unauthorizedExecutions,

        Double unauthorizedToolExecutionRatePercent,

        int policyEvaluatedRuns,

        int correctlyEnforcedRuns,

        Double policyEnforcementRatePercent,

        int falsePositives,

        int falseNegatives,

        Double falsePositiveRatePercent,

        Double falseNegativeRatePercent) {
}