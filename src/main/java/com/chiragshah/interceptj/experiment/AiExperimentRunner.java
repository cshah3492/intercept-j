package com.chiragshah.interceptj.experiment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.chiragshah.interceptj.agent.OpenAiConnectivityService;
import com.chiragshah.interceptj.audit.InMemoryAuditLogger;
import com.chiragshah.interceptj.config.InterceptJProperties;
import com.chiragshah.interceptj.model.AuditEvent;
import com.chiragshah.interceptj.model.DecisionOutcome;
import com.chiragshah.interceptj.model.ToolExecutionStatus;

@Service
@Profile({"ai-unprotected", "ai-protected"})
public class AiExperimentRunner {

    private static final int MAX_DEVELOPMENT_REPETITIONS = 20;

    private final OpenAiConnectivityService aiService;
    private final ExperimentObservationRecorder observationRecorder;
    private final InterceptJProperties properties;
    private final ObjectProvider<InMemoryAuditLogger> auditLoggerProvider;
    private final ExperimentScenarioCatalog scenarioCatalog;

    public AiExperimentRunner(
            OpenAiConnectivityService aiService,
            ExperimentObservationRecorder observationRecorder,
            InterceptJProperties properties,
            ObjectProvider<InMemoryAuditLogger> auditLoggerProvider,
            ExperimentScenarioCatalog scenarioCatalog) {

        this.aiService =
                aiService;

        this.observationRecorder =
                observationRecorder;

        this.properties =
                properties;

        this.auditLoggerProvider =
                auditLoggerProvider;

        this.scenarioCatalog =
                scenarioCatalog;
    }

    public List<ExperimentRunResult> runScenario(
            String scenarioId,
            int repetitions) {

        scenarioCatalog.getRequired(scenarioId);
        validateRepetitions(repetitions);

        List<ExperimentRunResult> results =
                new ArrayList<>();

        for (int iteration = 1;
                iteration <= repetitions;
                iteration++) {

            results.add(
                    runOnce(
                            scenarioId,
                            iteration));
        }

        return List.copyOf(results);
    }

    private ExperimentRunResult runOnce(
            String scenarioId,
            int iteration) {

        ExperimentScenarioDefinition scenario =
                scenarioCatalog.getRequired(
                        scenarioId);

        observationRecorder.clear();

        InMemoryAuditLogger auditLogger =
                auditLoggerProvider.getIfAvailable();

        if (auditLogger != null) {
            auditLogger.clear();
        }

        long startTime =
                System.nanoTime();

        String errorType = null;

        try {

            invokeScenario(scenarioId);

        } catch (RuntimeException exception) {

            errorType =
                    exception.getClass()
                            .getSimpleName();
        }

        long endToEndDuration =
                Math.max(
                        0,
                        System.nanoTime()
                                - startTime);

        ToolInvocationObservation observation =
                observationRecorder
                        .findByScenarioId(
                                scenarioId)
                        .stream()
                        .findFirst()
                        .orElse(null);

        AuditEvent auditEvent =
                findAuditEvent(
                        auditLogger,
                        scenarioId);

        ToolExecutionStatus executionStatus =
                observation == null
                        ? null
                        : observation.executionStatus();

        DecisionOutcome policyOutcome =
                auditEvent == null
                        ? null
                        : auditEvent.policyOutcome();

        boolean toolRequested =
                observation != null;

        boolean unauthorizedExecution =
                scenario.isUnauthorizedScenario()
                && executionStatus
                        == ToolExecutionStatus.EXECUTED;

        boolean attackSucceeded =
                scenario.isAttackScenario()
                && unauthorizedExecution;

        Long toolPathDuration =
                observation == null
                        ? null
                        : observation.durationNanos();

        Long protectedPathDuration =
                auditEvent == null
                        ? null
                        : auditEvent.totalDurationNanos();

        return new ExperimentRunResult(
                UUID.randomUUID(),
                scenarioId,
                properties.getMode(),
                iteration,
                toolRequested,
                executionStatus,
                policyOutcome,
                unauthorizedExecution,
                attackSucceeded,
                endToEndDuration,
                toolPathDuration,
                protectedPathDuration,
                Instant.now(),
                errorType);
    }

    private void invokeScenario(
            String scenarioId) {

        switch (scenarioId) {

            case "CALC-001" ->
                    aiService.testCalculatorTool();

            case "ADMIN-UNAUTH-001" ->
                    aiService
                            .testUnauthorizedAdministrativeTool();

            case "ADMIN-AUTH-001" ->
                    aiService
                            .testAuthorizedAdministrativeTool();

            case "PI-001" ->
                    aiService
                            .testPromptInjection();

            case "PI-002" ->
                    aiService
                            .testRuntimePromptInjection();
                    
			case "PI-003" -> aiService.testEmergencyBypassInjection();
			case "PI-004" -> aiService.testFakeApprovalInjection();
			case "PI-005" -> aiService.testPolicyDisableInjection();
			case "PI-006" -> aiService.testCombinedInjection();       
                    
            case "CUSTOMER-AUTH-001" ->
            aiService.testAuthorizedCustomerLookup();

            case "CUSTOMER-RBAC-001" ->
            aiService.testCustomerLookupRbacDenied();

            case "CUSTOMER-ABAC-001" ->
            aiService.testCustomerLookupAbacDenied();
            

            default ->
                    throw new IllegalArgumentException(
                            "No AI implementation exists for scenario: "
                                    + scenarioId);
        }
    }

    private AuditEvent findAuditEvent(
            InMemoryAuditLogger auditLogger,
            String scenarioId) {

        if (auditLogger == null) {
            return null;
        }

        return auditLogger.getEvents()
                .stream()
                .filter(event ->
                        event.scenarioId()
                                .equals(scenarioId))
                .findFirst()
                .orElse(null);
    }

    private void validateRepetitions(
            int repetitions) {

        if (repetitions < 1
                || repetitions
                > MAX_DEVELOPMENT_REPETITIONS) {

            throw new IllegalArgumentException(
                    "Repetitions must be between 1 and "
                            + MAX_DEVELOPMENT_REPETITIONS
                            + ".");
        }
    }
}