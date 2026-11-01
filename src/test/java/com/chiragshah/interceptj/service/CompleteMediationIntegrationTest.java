package com.chiragshah.interceptj.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationDeniedException;

import com.chiragshah.interceptj.audit.InMemoryAuditLogger;
import com.chiragshah.interceptj.interceptor.ToolAuthorizationDeniedException;
import com.chiragshah.interceptj.model.AuditStage;
import com.chiragshah.interceptj.model.ToolExecutionStatus;
import com.chiragshah.interceptj.model.ToolRequest;
import com.chiragshah.interceptj.model.ToolResult;
import com.chiragshah.interceptj.model.UserContext;
import com.chiragshah.interceptj.tool.CalculatorArguments;
import com.chiragshah.interceptj.tool.CalculatorOperation;
import com.chiragshah.interceptj.tool.CalculatorTool;
import com.chiragshah.interceptj.tool.CustomerLookupArguments;
import com.chiragshah.interceptj.tool.CustomerLookupTool;
import com.chiragshah.interceptj.tool.AdministrativeAction;
import com.chiragshah.interceptj.tool.AdministrativeActionArguments;
import com.chiragshah.interceptj.tool.AdministrativeActionTool;

@SpringBootTest(properties = {
        "interceptj.mode=ai-protected",
        "interceptj.ai.enabled=true",
        "interceptj.enforcement.enabled=true",
        "interceptj.audit.enabled=true",
        "spring.ai.model.chat=none",
        "logging.level.com.chiragshah.interceptj.audit=OFF"
})
class CompleteMediationIntegrationTest {

    @Autowired
    private CalculatorTool calculatorTool;

    @Autowired
    private CustomerLookupTool customerLookupTool;

    @Autowired
    private AdministrativeActionTool administrativeActionTool;

    @Autowired
    private ToolExecutionGateway gateway;

    @Autowired
    private GatewayExecutionContext executionContext;

    @Autowired
    private InMemoryAuditLogger auditLogger;

    @BeforeEach
    void clearAuditEvents() {
        auditLogger.clear();
    }

    @Test
    void directEnterpriseToolInvocationIsRejected() {
        assertThrows(
                AuthorizationDeniedException.class,
                () -> calculatorTool.execute(arguments()));

        assertThrows(
                AuthorizationDeniedException.class,
                () -> customerLookupTool.execute(
                        new CustomerLookupArguments(
                                "CUST-1001",
                                "EAST")));

        assertThrows(
                AuthorizationDeniedException.class,
                () -> administrativeActionTool.execute(
                        new AdministrativeActionArguments(
                                AdministrativeAction.RESTART_SERVICE,
                                "test-service",
                                "Complete mediation test request")));
    }

    @Test
    void gatewayInvocationExecutesAndAlwaysClosesItsScope() {
        ToolRequest<CalculatorArguments> request =
                new ToolRequest<>(
                        UUID.randomUUID(),
                        "MEDIATION-TEST-001",
                        CalculatorTool.TOOL_NAME,
                        arguments(),
                        new UserContext(
                                "test-user",
                                Set.of("USER"),
                                Map.of()),
                        Instant.now());

        ToolResult<Object> result = gateway.execute(request);

        assertEquals(ToolExecutionStatus.EXECUTED, result.status());
        assertFalse(executionContext.isActive());
        assertIterableEquals(
                List.of(
                        AuditStage.REQUEST_RECEIVED,
                        AuditStage.AUTHORIZATION_DECIDED,
                        AuditStage.EXECUTION_COMPLETED),
                auditLogger.getTraceEvents()
                        .stream()
                        .map(event -> event.stage())
                        .toList());
    }

    @Test
    void deniedGatewayInvocationRecordsAllStagesWithoutExecuting() {
        ToolRequest<AdministrativeActionArguments> request =
                new ToolRequest<>(
                        UUID.randomUUID(),
                        "MEDIATION-TEST-002",
                        AdministrativeActionTool.TOOL_NAME,
                        new AdministrativeActionArguments(
                                AdministrativeAction.RESTART_SERVICE,
                                "test-service",
                                "Unauthorized lifecycle test"),
                        new UserContext(
                                "non-admin-user",
                                Set.of("USER"),
                                Map.of("clearance", "LOW")),
                        Instant.now());

        assertThrows(
                ToolAuthorizationDeniedException.class,
                () -> gateway.execute(request));

        assertFalse(executionContext.isActive());
        assertIterableEquals(
                List.of(
                        AuditStage.REQUEST_RECEIVED,
                        AuditStage.AUTHORIZATION_DECIDED,
                        AuditStage.EXECUTION_COMPLETED),
                auditLogger.getTraceEvents()
                        .stream()
                        .map(event -> event.stage())
                        .toList());
        assertEquals(
                ToolExecutionStatus.BLOCKED,
                auditLogger.getTraceEvents().get(2).executionStatus());
    }

    private CalculatorArguments arguments() {
        return new CalculatorArguments(
                7,
                6,
                CalculatorOperation.MULTIPLY);
    }
}
