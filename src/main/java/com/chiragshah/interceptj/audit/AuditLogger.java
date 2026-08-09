package com.chiragshah.interceptj.audit;

import java.util.List;

import com.chiragshah.interceptj.model.AuditEvent;

public interface AuditLogger {

    void record(AuditEvent event);

    List<AuditEvent> getEvents();

    void clear();
}