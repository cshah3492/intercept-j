package com.chiragshah.interceptj.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.chiragshah.interceptj.model.AuditEvent;
import com.chiragshah.interceptj.model.AuditStage;
import com.chiragshah.interceptj.model.AuditTraceEvent;
import com.chiragshah.interceptj.model.DecisionOutcome;
import com.chiragshah.interceptj.model.ExperimentMode;
import com.chiragshah.interceptj.model.ToolExecutionStatus;

class AuditLoggerTest {

    @Test
    void shouldRecordAuditEvent() {

        AuditLogger logger =
                new InMemoryAuditLogger();

        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "AUDIT-TEST-001",
                ExperimentMode.AI_PROTECTED,
                "calculator",
                "test-user",
                DecisionOutcome.ALLOW,
                ToolExecutionStatus.EXECUTED,
                "Authorized test execution.",
                Instant.now(),
                1000);

        logger.record(event);

        assertEquals(1, logger.getEvents().size());
        assertEquals(
                "AUDIT-TEST-001",
                logger.getEvents().get(0).scenarioId());
    }

    @Test
    void shouldRecordLifecycleTraceEvent() {
        AuditLogger logger = new InMemoryAuditLogger();

        AuditTraceEvent event = new AuditTraceEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "AUDIT-TRACE-001",
                ExperimentMode.AI_PROTECTED,
                "calculator",
                "test-user",
                AuditStage.REQUEST_RECEIVED,
                null,
                ToolExecutionStatus.NOT_ATTEMPTED,
                "Request received.",
                Instant.now());

        logger.recordTrace(event);

        assertEquals(1, logger.getTraceEvents().size());
        assertEquals(
                AuditStage.REQUEST_RECEIVED,
                logger.getTraceEvents().get(0).stage());
    }
}
