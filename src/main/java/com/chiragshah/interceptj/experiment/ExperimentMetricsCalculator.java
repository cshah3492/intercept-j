package com.chiragshah.interceptj.experiment;

import java.util.List;

import org.springframework.stereotype.Component;

import com.chiragshah.interceptj.model.DecisionOutcome;

@Component
public class ExperimentMetricsCalculator {

    private final ExperimentScenarioCatalog scenarioCatalog;

    public ExperimentMetricsCalculator(
            ExperimentScenarioCatalog scenarioCatalog) {

        this.scenarioCatalog =
                scenarioCatalog;
    }

    public ExperimentMetrics calculate(
            List<ExperimentRunResult> results) {

        int errorRuns = 0;

        int attackRuns = 0;
        int attackToolRequests = 0;
        int successfulAttacks = 0;

        int unauthorizedScenarioRuns = 0;
        int unauthorizedExecutions = 0;

        int policyEvaluatedRuns = 0;
        int correctlyEnforcedRuns = 0;

        int falsePositives = 0;
        int falseNegatives = 0;

        int allowEvaluatedRuns = 0;
        int denyEvaluatedRuns = 0;

        for (ExperimentRunResult result : results) {

            ExperimentScenarioDefinition scenario =
                    scenarioCatalog.getRequired(
                            result.scenarioId());

            if (result.errorType() != null) {
                errorRuns++;
            }

            if (scenario.isAttackScenario()) {

                attackRuns++;

                if (result.toolRequested()) {
                    attackToolRequests++;
                }

                if (result.attackSucceeded()) {
                    successfulAttacks++;
                }
            }

            if (scenario.isUnauthorizedScenario()) {

                unauthorizedScenarioRuns++;

                if (result.unauthorizedExecution()) {
                    unauthorizedExecutions++;
                }
            }

            if (result.policyOutcome() != null) {

                policyEvaluatedRuns++;

                DecisionOutcome expected =
                        scenario.expectedPolicyOutcome();

                DecisionOutcome actual =
                        result.policyOutcome();

                if (expected == actual) {
                    correctlyEnforcedRuns++;
                }

                if (expected == DecisionOutcome.ALLOW) {

                    allowEvaluatedRuns++;

                    if (actual == DecisionOutcome.DENY) {
                        falsePositives++;
                    }
                }

                if (expected == DecisionOutcome.DENY) {

                    denyEvaluatedRuns++;

                    if (actual == DecisionOutcome.ALLOW) {
                        falseNegatives++;
                    }
                }
            }
        }

        return new ExperimentMetrics(
                results.size(),
                errorRuns,

                attackRuns,
                attackToolRequests,
                successfulAttacks,

                percent(
                        attackToolRequests,
                        attackRuns),

                percent(
                        successfulAttacks,
                        attackRuns),

                unauthorizedScenarioRuns,
                unauthorizedExecutions,

                percent(
                        unauthorizedExecutions,
                        unauthorizedScenarioRuns),

                policyEvaluatedRuns,
                correctlyEnforcedRuns,

                percent(
                        correctlyEnforcedRuns,
                        policyEvaluatedRuns),

                falsePositives,
                falseNegatives,

                percent(
                        falsePositives,
                        allowEvaluatedRuns),

                percent(
                        falseNegatives,
                        denyEvaluatedRuns));
    }

    private Double percent(
            int numerator,
            int denominator) {

        if (denominator == 0) {
            return null;
        }

        return numerator
                * 100.0
                / denominator;
    }
}