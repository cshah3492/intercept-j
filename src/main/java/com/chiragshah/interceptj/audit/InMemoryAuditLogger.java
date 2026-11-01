package com.chiragshah.interceptj.audit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.chiragshah.interceptj.model.AuditEvent;
import com.chiragshah.interceptj.model.AuditTraceEvent;

@Component
@ConditionalOnProperty(
        prefix = "interceptj.audit",
        name = "enabled",
        havingValue = "true")
public class InMemoryAuditLogger implements AuditLogger {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(InMemoryAuditLogger.class);

    private final List<AuditEvent> events =
            new CopyOnWriteArrayList<>();

    private final List<AuditTraceEvent> traceEvents =
            new CopyOnWriteArrayList<>();

    @Override
    public void record(AuditEvent event) {

        events.add(event);

        LOGGER.info(
                "AUDIT requestId={}, scenario={}, mode={}, "
                        + "tool={}, user={}, decision={}, status={}, "
                        + "durationNanos={}",
                event.requestId(),
                event.scenarioId(),
                event.experimentMode(),
                event.toolName(),
                event.userId(),
                event.policyOutcome(),
                event.executionStatus(),
                event.totalDurationNanos());
    }

    @Override
    public List<AuditEvent> getEvents() {
        return List.copyOf(events);
    }

    @Override
    public void recordTrace(AuditTraceEvent event) {
        traceEvents.add(event);

        LOGGER.info(
                "AUDIT_TRACE requestId={}, scenario={}, mode={}, "
                        + "tool={}, user={}, stage={}, decision={}, status={}",
                event.requestId(),
                event.scenarioId(),
                event.experimentMode(),
                event.toolName(),
                event.userId(),
                event.stage(),
                event.policyOutcome(),
                event.executionStatus());
    }

    @Override
    public List<AuditTraceEvent> getTraceEvents() {
        return List.copyOf(traceEvents);
    }

    @Override
    public void clear() {
        events.clear();
        traceEvents.clear();
    }
}
