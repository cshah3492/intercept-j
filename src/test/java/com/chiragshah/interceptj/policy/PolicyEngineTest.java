package com.chiragshah.interceptj.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chiragshah.interceptj.model.DecisionOutcome;
import com.chiragshah.interceptj.model.ToolArguments;
import com.chiragshah.interceptj.model.ToolRequest;
import com.chiragshah.interceptj.model.UserContext;
import com.chiragshah.interceptj.tool.AdministrativeAction;
import com.chiragshah.interceptj.tool.AdministrativeActionArguments;
import com.chiragshah.interceptj.tool.AdministrativeActionTool;
import com.chiragshah.interceptj.tool.CalculatorArguments;
import com.chiragshah.interceptj.tool.CalculatorOperation;
import com.chiragshah.interceptj.tool.CalculatorTool;
import com.chiragshah.interceptj.tool.CustomerLookupArguments;
import com.chiragshah.interceptj.tool.CustomerLookupTool;
import com.chiragshah.interceptj.tool.ToolRegistry;

class PolicyEngineTest {

    private PolicyEngine policyEngine;

    @BeforeEach
    void setUp() {

        ToolRegistry registry = new ToolRegistry(
                List.of(
                        new CalculatorTool(),
                        new CustomerLookupTool(),
                        new AdministrativeActionTool()));

        policyEngine = new DefaultPolicyEngine(registry);
    }

    @Test
    void shouldAllowCalculator() {

        UserContext user = user(
                Set.of("USER"),
                Map.of());

        ToolRequest<CalculatorArguments> request = request(
                CalculatorTool.TOOL_NAME,
                new CalculatorArguments(
                        10,
                        5,
                        CalculatorOperation.ADD),
                user);

        assertEquals(
                DecisionOutcome.ALLOW,
                policyEngine.evaluate(request).outcome());
    }

    @Test
    void shouldAllowCustomerLookupForMatchingRegion() {

        UserContext user = user(
                Set.of("ANALYST"),
                Map.of("region", "EAST"));

        ToolRequest<CustomerLookupArguments> request = request(
                CustomerLookupTool.TOOL_NAME,
                new CustomerLookupArguments(
                        "CUST-1001",
                        "EAST"),
                user);

        assertEquals(
                DecisionOutcome.ALLOW,
                policyEngine.evaluate(request).outcome());
    }

    @Test
    void shouldDenyCustomerLookupForWrongRegion() {

        UserContext user = user(
                Set.of("ANALYST"),
                Map.of("region", "WEST"));

        ToolRequest<CustomerLookupArguments> request = request(
                CustomerLookupTool.TOOL_NAME,
                new CustomerLookupArguments(
                        "CUST-1001",
                        "EAST"),
                user);

        assertEquals(
                DecisionOutcome.DENY,
                policyEngine.evaluate(request).outcome());
    }

    @Test
    void shouldDenyAdministrativeActionForNonAdmin() {

        UserContext user = user(
                Set.of("USER"),
                Map.of("clearance", "LOW"));

        ToolRequest<AdministrativeActionArguments> request = request(
                AdministrativeActionTool.TOOL_NAME,
                new AdministrativeActionArguments(
                        AdministrativeAction.RESTART_SERVICE,
                        "demo-service",
                        "Research experiment request"),
                user);

        assertEquals(
                DecisionOutcome.DENY,
                policyEngine.evaluate(request).outcome());
    }

    @Test
    void shouldAllowAdministrativeActionForAuthorizedAdmin() {

        UserContext user = user(
                Set.of("ADMIN"),
                Map.of("clearance", "HIGH"));

        ToolRequest<AdministrativeActionArguments> request = request(
                AdministrativeActionTool.TOOL_NAME,
                new AdministrativeActionArguments(
                        AdministrativeAction.RESTART_SERVICE,
                        "demo-service",
                        "Authorized research experiment"),
                user);

        assertEquals(
                DecisionOutcome.ALLOW,
                policyEngine.evaluate(request).outcome());
    }

    @Test
    void shouldDenyUnknownTool() {

        UserContext user = user(
                Set.of("ADMIN"),
                Map.of("clearance", "HIGH"));

        ToolRequest<CalculatorArguments> request = request(
                "unregistered-dangerous-tool",
                new CalculatorArguments(
                        1,
                        1,
                        CalculatorOperation.ADD),
                user);

        assertEquals(
                DecisionOutcome.DENY,
                policyEngine.evaluate(request).outcome());
    }

    private UserContext user(
            Set<String> roles,
            Map<String, String> attributes) {

        return new UserContext(
                "test-user",
                roles,
                attributes);
    }

    private <T extends ToolArguments> ToolRequest<T> request(
            String toolName,
            T arguments,
            UserContext user) {

        return new ToolRequest<>(
                UUID.randomUUID(),
                "POLICY-TEST",
                toolName,
                arguments,
                user,
                Instant.now());
    }
}