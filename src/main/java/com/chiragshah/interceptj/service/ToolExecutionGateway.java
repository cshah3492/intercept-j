package com.chiragshah.interceptj.service;

import java.time.Instant;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.chiragshah.interceptj.interceptor.ProtectedToolExecution;
import com.chiragshah.interceptj.model.ToolArguments;
import com.chiragshah.interceptj.model.ToolExecutionStatus;
import com.chiragshah.interceptj.model.ToolRequest;
import com.chiragshah.interceptj.model.ToolResult;
import com.chiragshah.interceptj.tool.EnterpriseTool;
import com.chiragshah.interceptj.tool.ToolRegistry;

@Service
public class ToolExecutionGateway {

    private final ToolRegistry toolRegistry;

    public ToolExecutionGateway(
            ToolRegistry toolRegistry) {

        this.toolRegistry = toolRegistry;
    }

    @ProtectedToolExecution
    public ToolResult<Object> execute(
            ToolRequest<? extends ToolArguments> request) {

        Objects.requireNonNull(
                request,
                "Tool request cannot be null.");

        long startTime = System.nanoTime();

        try {

            EnterpriseTool<?, ?> tool =
                    toolRegistry.getRequiredTool(
                            request.toolName());

            if (!tool.getArgumentType()
                    .isInstance(request.arguments())) {

                return failedResult(
                        request,
                        "Tool argument type mismatch.",
                        startTime);
            }

            Object output =
                    invokeTool(
                            tool,
                            request.arguments());

            return new ToolResult<>(
                    request.requestId(),
                    ToolExecutionStatus.EXECUTED,
                    output,
                    "Tool executed successfully.",
                    Instant.now(),
                    elapsed(startTime));

        } catch (RuntimeException exception) {

            return failedResult(
                    request,
                    "Tool execution failed: "
                            + exception.getClass()
                                    .getSimpleName(),
                    startTime);
        }
    }

    @SuppressWarnings({
            "rawtypes",
            "unchecked"
    })
    private Object invokeTool(
            EnterpriseTool tool,
            ToolArguments arguments) {

        return tool.execute(arguments);
    }

    private ToolResult<Object> failedResult(
            ToolRequest<? extends ToolArguments> request,
            String message,
            long startTime) {

        return new ToolResult<>(
                request.requestId(),
                ToolExecutionStatus.FAILED,
                null,
                message,
                Instant.now(),
                elapsed(startTime));
    }

    private long elapsed(long startTime) {
        return Math.max(
                0,
                System.nanoTime() - startTime);
    }
}