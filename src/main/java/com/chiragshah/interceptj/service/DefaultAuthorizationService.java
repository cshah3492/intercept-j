package com.chiragshah.interceptj.service;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.chiragshah.interceptj.model.DecisionOutcome;
import com.chiragshah.interceptj.model.PolicyDecision;
import com.chiragshah.interceptj.model.ToolArguments;
import com.chiragshah.interceptj.model.ToolRequest;
import com.chiragshah.interceptj.policy.PolicyEngine;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
public class DefaultAuthorizationService
        implements AuthorizationService {

    private static final String VALIDATION_POLICY_ID =
            "POLICY-VALIDATION-001";

    private final Validator validator;
    private final PolicyEngine policyEngine;

    public DefaultAuthorizationService(
            Validator validator,
            PolicyEngine policyEngine) {

        this.validator = validator;
        this.policyEngine = policyEngine;
    }

    @Override
    public PolicyDecision authorize(
            ToolRequest<? extends ToolArguments> request) {

        Objects.requireNonNull(
                request,
                "Tool request cannot be null.");

        long startTime = System.nanoTime();

        Set<? extends ConstraintViolation<?>> violations =
                validator.validate(request);

        if (!violations.isEmpty()) {

            String reason = violations.stream()
                    .map(violation ->
                            violation.getPropertyPath()
                                    + ": "
                                    + violation.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));

            long elapsed = System.nanoTime() - startTime;

            return new PolicyDecision(
                    request.requestId(),
                    DecisionOutcome.DENY,
                    VALIDATION_POLICY_ID,
                    reason,
                    Instant.now(),
                    Math.max(0, elapsed));
        }

        return policyEngine.evaluate(request);
    }
}