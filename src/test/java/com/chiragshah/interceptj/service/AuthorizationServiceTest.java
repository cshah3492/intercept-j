package com.chiragshah.interceptj.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chiragshah.interceptj.model.DecisionOutcome;
import com.chiragshah.interceptj.model.ToolRequest;
import com.chiragshah.interceptj.model.UserContext;
import com.chiragshah.interceptj.policy.DefaultPolicyEngine;
import com.chiragshah.interceptj.policy.PolicyEngine;
import com.chiragshah.interceptj.tool.AdministrativeActionTool;
import com.chiragshah.interceptj.tool.CalculatorArguments;
import com.chiragshah.interceptj.tool.CalculatorOperation;
import com.chiragshah.interceptj.tool.CalculatorTool;
import com.chiragshah.interceptj.tool.CustomerLookupTool;
import com.chiragshah.interceptj.tool.ToolRegistry;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class AuthorizationServiceTest {

    private ValidatorFactory validatorFactory;
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {

        validatorFactory =
                Validation.buildDefaultValidatorFactory();

        Validator validator =
                validatorFactory.getValidator();

        ToolRegistry registry = new ToolRegistry(
                List.of(
                        new CalculatorTool(),
                        new CustomerLookupTool(),
                        new AdministrativeActionTool()));

        PolicyEngine policyEngine =
                new DefaultPolicyEngine(registry);

        authorizationService =
                new DefaultAuthorizationService(
                        validator,
                        policyEngine);
    }

    @AfterEach
    void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldAllowValidCalculatorRequest() {

        ToolRequest<CalculatorArguments> request =
                new ToolRequest<>(
                        UUID.randomUUID(),
                        "AUTH-TEST-001",
                        CalculatorTool.TOOL_NAME,
                        new CalculatorArguments(
                                10,
                                5,
                                CalculatorOperation.ADD),
                        new UserContext(
                                "test-user",
                                Set.of("USER"),
                                Map.of()),
                        Instant.now());

        assertEquals(
                DecisionOutcome.ALLOW,
                authorizationService
                        .authorize(request)
                        .outcome());
    }

    @Test
    void shouldDenyBlankScenarioId() {

        ToolRequest<CalculatorArguments> request =
                new ToolRequest<>(
                        UUID.randomUUID(),
                        " ",
                        CalculatorTool.TOOL_NAME,
                        new CalculatorArguments(
                                10,
                                5,
                                CalculatorOperation.ADD),
                        new UserContext(
                                "test-user",
                                Set.of("USER"),
                                Map.of()),
                        Instant.now());

        assertEquals(
                DecisionOutcome.DENY,
                authorizationService
                        .authorize(request)
                        .outcome());
    }
}