package com.chiragshah.interceptj.experiment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.chiragshah.interceptj.model.DecisionOutcome;

class ExperimentScenarioCatalogTest {

    private final ExperimentScenarioCatalog catalog =
            new ExperimentScenarioCatalog();

    @Test
    void catalogShouldContainAllTwelveScenarios() {

        assertEquals(12, catalog.getAll().size());

        Set<String> scenarioIds = catalog.getAll()
                .stream()
                .map(ExperimentScenarioDefinition::scenarioId)
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "CALC-001",
                "ADMIN-AUTH-001",
                "ADMIN-UNAUTH-001",
                "CUSTOMER-AUTH-001",
                "CUSTOMER-RBAC-001",
                "CUSTOMER-ABAC-001",
                "PI-001",
                "PI-002",
                "PI-003",
                "PI-004",
                "PI-005",
                "PI-006"
        ), scenarioIds);
    }

    @Test
    void promptInjectionScenariosShouldAllExpectDeny() {

        long promptInjectionCount = catalog.getAll()
                .stream()
                .filter(ExperimentScenarioDefinition::isAttackScenario)
                .peek(scenario ->
                        assertEquals(
                                DecisionOutcome.DENY,
                                scenario.expectedPolicyOutcome()))
                .count();

        assertEquals(6, promptInjectionCount);
    }

    @Test
    void customerAuthorizationScenariosShouldHaveCorrectExpectations() {

        assertEquals(
                DecisionOutcome.ALLOW,
                catalog.getRequired("CUSTOMER-AUTH-001")
                        .expectedPolicyOutcome());

        assertEquals(
                DecisionOutcome.DENY,
                catalog.getRequired("CUSTOMER-RBAC-001")
                        .expectedPolicyOutcome());

        assertEquals(
                DecisionOutcome.DENY,
                catalog.getRequired("CUSTOMER-ABAC-001")
                        .expectedPolicyOutcome());
    }
}