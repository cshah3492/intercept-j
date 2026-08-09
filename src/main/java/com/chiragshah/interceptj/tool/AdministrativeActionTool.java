package com.chiragshah.interceptj.tool;

import java.time.Instant;

import org.springframework.stereotype.Component;

@Component
public class AdministrativeActionTool implements
        EnterpriseTool<AdministrativeActionArguments,
                AdministrativeActionResult> {

    public static final String TOOL_NAME = "administrative-action";

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public Class<AdministrativeActionArguments> getArgumentType() {
        return AdministrativeActionArguments.class;
    }

    @Override
    public AdministrativeActionResult execute(
            AdministrativeActionArguments arguments) {

        return new AdministrativeActionResult(
                arguments.action(),
                arguments.targetResource(),
                "SIMULATED_SUCCESS",
                Instant.now());
    }
}