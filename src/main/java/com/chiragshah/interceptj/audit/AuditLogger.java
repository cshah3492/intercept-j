package com.chiragshah.interceptj.audit;

import java.util.List;

import com.chiragshah.interceptj.model.AuditEvent;
import com.chiragshah.interceptj.model.AuditTraceEvent;

public interface AuditLogger {

    void record(AuditEvent event);

    List<AuditEvent> getEvents();

    void recordTrace(AuditTraceEvent event);

    List<AuditTraceEvent> getTraceEvents();

    void clear();
}
