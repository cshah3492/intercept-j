package com.chiragshah.interceptj.interceptor;

import java.time.Instant;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.chiragshah.interceptj.audit.AuditLogger;
import com.chiragshah.interceptj.config.InterceptJProperties;
import com.chiragshah.interceptj.model.AuditEvent;
import com.chiragshah.interceptj.model.PolicyDecision;
import com.chiragshah.interceptj.model.ToolArguments;
import com.chiragshah.interceptj.model.ToolExecutionStatus;
import com.chiragshah.interceptj.model.ToolRequest;
import com.chiragshah.interceptj.model.ToolResult;
import com.chiragshah.interceptj.service.AuthorizationService;

@Aspect
@Component
@Order(0)
@ConditionalOnProperty(
        prefix = "interceptj",
        name = {
                "enforcement.enabled",
                "audit.enabled"
        },
        havingValue = "true")
public class ToolExecutionAspect {

    private final AuthorizationService authorizationService;
    private final AuditLogger auditLogger;
    private final InterceptJProperties properties;

    public ToolExecutionAspect(
            AuthorizationService authorizationService,
            AuditLogger auditLogger,
            InterceptJProperties properties) {

        this.authorizationService = authorizationService;
        this.auditLogger = auditLogger;
        this.properties = properties;
    }

    @Around(
        "@annotation("
        + "com.chiragshah.interceptj.interceptor."
        + "ProtectedToolExecution)")
    public Object enforcePolicy(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        long startTime = System.nanoTime();

        ToolRequest<? extends ToolArguments> request =
                findToolRequest(joinPoint);

        PolicyDecision decision =
                authorizationService.authorize(request);

        if (!decision.isAllowed()) {

            recordAudit(
                    request,
                    decision,
                    ToolExecutionStatus.BLOCKED,
                    decision.reason(),
                    startTime);

            throw new ToolAuthorizationDeniedException(
                    decision);
        }

        try {

        	Object result = joinPoint.proceed();

        	ToolExecutionStatus executionStatus =
        	        ToolExecutionStatus.EXECUTED;

        	if (result instanceof ToolResult<?> toolResult) {
        	    executionStatus = toolResult.status();
        	}

        	recordAudit(
        	        request,
        	        decision,
        	        executionStatus,
        	        decision.reason(),
        	        startTime);

        	return result;

        } catch (Throwable exception) {

            recordAudit(
                    request,
                    decision,
                    ToolExecutionStatus.FAILED,
                    decision.reason()
                            + "; execution failed: "
                            + exception.getClass()
                                    .getSimpleName(),
                    startTime);

            throw exception;
        }
    }

    private void recordAudit(
            ToolRequest<? extends ToolArguments> request,
            PolicyDecision decision,
            ToolExecutionStatus status,
            String reason,
            long startTime) {

        long elapsed =
                Math.max(0, System.nanoTime() - startTime);

        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                request.requestId(),
                request.scenarioId(),
                properties.getMode(),
                request.toolName(),
                request.userContext().userId(),
                decision.outcome(),
                status,
                reason,
                Instant.now(),
                elapsed);

        auditLogger.record(event);
    }

    private ToolRequest<? extends ToolArguments> findToolRequest(
            ProceedingJoinPoint joinPoint) {

        for (Object argument : joinPoint.getArgs()) {

            if (argument instanceof ToolRequest<?> request) {

                @SuppressWarnings("unchecked")
                ToolRequest<? extends ToolArguments>
                        typedRequest =
                        (ToolRequest<? extends ToolArguments>)
                                request;

                return typedRequest;
            }
        }

        throw new IllegalStateException(
                "Protected tool execution requires a ToolRequest.");
    }
}