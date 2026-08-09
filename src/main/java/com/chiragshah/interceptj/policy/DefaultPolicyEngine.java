package com.chiragshah.interceptj.policy;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.chiragshah.interceptj.model.DecisionOutcome;
import com.chiragshah.interceptj.model.PolicyDecision;
import com.chiragshah.interceptj.model.ToolArguments;
import com.chiragshah.interceptj.model.ToolRequest;
import com.chiragshah.interceptj.model.UserContext;
import com.chiragshah.interceptj.tool.AdministrativeActionArguments;
import com.chiragshah.interceptj.tool.AdministrativeActionTool;
import com.chiragshah.interceptj.tool.CalculatorTool;
import com.chiragshah.interceptj.tool.CustomerLookupArguments;
import com.chiragshah.interceptj.tool.CustomerLookupTool;
import com.chiragshah.interceptj.tool.EnterpriseTool;
import com.chiragshah.interceptj.tool.ToolRegistry;

@Service
public class DefaultPolicyEngine implements PolicyEngine {

    private final ToolRegistry toolRegistry;

    public DefaultPolicyEngine(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @Override
    public PolicyDecision evaluate(
            ToolRequest<? extends ToolArguments> request) {

        long startTime = System.nanoTime();

        Evaluation evaluation = determineDecision(request);

        long elapsed = System.nanoTime() - startTime;

        return new PolicyDecision(
                request.requestId(),
                evaluation.outcome(),
                evaluation.policyId(),
                evaluation.reason(),
                Instant.now(),
                Math.max(0, elapsed));
    }

    private Evaluation determineDecision(
            ToolRequest<? extends ToolArguments> request) {

        String toolName = request.toolName();

        // Tool whitelist enforcement
        if (!toolRegistry.contains(toolName)) {
            return deny(
                    "POLICY-WHITELIST-001",
                    "Requested tool is not registered.");
        }

        EnterpriseTool<?, ?> registeredTool =
                toolRegistry.getRequiredTool(toolName);

        // Type-safe argument enforcement
        if (!registeredTool.getArgumentType()
                .isInstance(request.arguments())) {

            return deny(
                    "POLICY-TYPE-001",
                    "Tool argument type does not match registered tool.");
        }

        return switch (toolName) {

            case CalculatorTool.TOOL_NAME ->
                    allow(
                            "POLICY-CALC-001",
                            "Calculator access permitted.");

            case CustomerLookupTool.TOOL_NAME ->
                    evaluateCustomerLookup(request);

            case AdministrativeActionTool.TOOL_NAME ->
                    evaluateAdministrativeAction(request);

            default ->
                    deny(
                            "POLICY-WHITELIST-002",
                            "No policy exists for requested tool.");
        };
    }

    private Evaluation evaluateCustomerLookup(
            ToolRequest<? extends ToolArguments> request) {

        UserContext user = request.userContext();

        if (!hasAnyRole(user, "ANALYST", "MANAGER")) {
            return deny(
                    "POLICY-CUSTOMER-RBAC-001",
                    "Customer lookup requires ANALYST or MANAGER role.");
        }

        if (!(request.arguments()
                instanceof CustomerLookupArguments arguments)) {

            return deny(
                    "POLICY-CUSTOMER-TYPE-001",
                    "Invalid customer lookup arguments.");
        }

        String userRegion = getAttribute(user, "region");

        if (userRegion == null
                || !userRegion.equalsIgnoreCase(
                        arguments.requestedRegion())) {

            return deny(
                    "POLICY-CUSTOMER-ABAC-001",
                    "User region does not match requested region.");
        }

        return allow(
                "POLICY-CUSTOMER-ALLOW-001",
                "Customer lookup authorized.");
    }

    private Evaluation evaluateAdministrativeAction(
            ToolRequest<? extends ToolArguments> request) {

        UserContext user = request.userContext();

        if (!hasAnyRole(user, "ADMIN")) {
            return deny(
                    "POLICY-ADMIN-RBAC-001",
                    "Administrative action requires ADMIN role.");
        }

        if (!(request.arguments()
                instanceof AdministrativeActionArguments)) {

            return deny(
                    "POLICY-ADMIN-TYPE-001",
                    "Invalid administrative action arguments.");
        }

        String clearance = getAttribute(user, "clearance");

        if (clearance == null
                || !clearance.equalsIgnoreCase("HIGH")) {

            return deny(
                    "POLICY-ADMIN-ABAC-001",
                    "Administrative action requires HIGH clearance.");
        }

        return allow(
                "POLICY-ADMIN-ALLOW-001",
                "Administrative action authorized.");
    }

    private boolean hasAnyRole(
            UserContext user,
            String... requiredRoles) {

        for (String requiredRole : requiredRoles) {
            boolean found = user.roles()
                    .stream()
                    .anyMatch(role ->
                            role.equalsIgnoreCase(requiredRole));

            if (found) {
                return true;
            }
        }

        return false;
    }

    private String getAttribute(
            UserContext user,
            String attributeName) {

        return user.attributes()
                .entrySet()
                .stream()
                .filter(entry ->
                        entry.getKey()
                                .equalsIgnoreCase(attributeName))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private Evaluation allow(
            String policyId,
            String reason) {

        return new Evaluation(
                DecisionOutcome.ALLOW,
                policyId,
                reason);
    }

    private Evaluation deny(
            String policyId,
            String reason) {

        return new Evaluation(
                DecisionOutcome.DENY,
                policyId,
                reason);
    }

    private record Evaluation(
            DecisionOutcome outcome,
            String policyId,
            String reason) {
    }
}