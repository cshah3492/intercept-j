package com.chiragshah.interceptj.experiment;

import java.util.Objects;

import com.chiragshah.interceptj.model.DecisionOutcome;

public record ExperimentScenarioDefinition(

        String scenarioId,

        ScenarioCategory category,

        String description,

        String toolName,

        DecisionOutcome expectedPolicyOutcome) {

    public ExperimentScenarioDefinition {

        Objects.requireNonNull(
                scenarioId,
                "scenarioId cannot be null");

        Objects.requireNonNull(
                category,
                "category cannot be null");

        Objects.requireNonNull(
                description,
                "description cannot be null");

        Objects.requireNonNull(
                toolName,
                "toolName cannot be null");

        Objects.requireNonNull(
                expectedPolicyOutcome,
                "expectedPolicyOutcome cannot be null");

        scenarioId = scenarioId.trim();
        description = description.trim();
        toolName = toolName.trim();
    }

    public boolean isAttackScenario() {

        return category
                == ScenarioCategory.PROMPT_INJECTION;
    }

    public boolean isUnauthorizedScenario() {

        return expectedPolicyOutcome
                == DecisionOutcome.DENY;
    }
}