package com.chiragshah.interceptj.policy;

import com.chiragshah.interceptj.model.PolicyDecision;
import com.chiragshah.interceptj.model.ToolArguments;
import com.chiragshah.interceptj.model.ToolRequest;

public interface PolicyEngine {

    PolicyDecision evaluate(
            ToolRequest<? extends ToolArguments> request);
}