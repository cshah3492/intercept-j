package com.chiragshah.interceptj.service;

import com.chiragshah.interceptj.model.PolicyDecision;
import com.chiragshah.interceptj.model.ToolArguments;
import com.chiragshah.interceptj.model.ToolRequest;

public interface AuthorizationService {

    PolicyDecision authorize(
            ToolRequest<? extends ToolArguments> request);
}