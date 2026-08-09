package com.chiragshah.interceptj.interceptor;

import com.chiragshah.interceptj.model.PolicyDecision;

public class ToolAuthorizationDeniedException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final PolicyDecision policyDecision;

    public ToolAuthorizationDeniedException(
            PolicyDecision policyDecision) {

        super("Tool execution denied: "
                + policyDecision.reason());

        this.policyDecision = policyDecision;
    }

    public PolicyDecision getPolicyDecision() {
        return policyDecision;
    }
}