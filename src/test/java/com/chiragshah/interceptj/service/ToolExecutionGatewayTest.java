package com.chiragshah.interceptj.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chiragshah.interceptj.model.ToolExecutionStatus;
import com.chiragshah.interceptj.model.ToolRequest;
import com.chiragshah.interceptj.model.ToolResult;
import com.chiragshah.interceptj.model.UserContext;
import com.chiragshah.interceptj.tool.AdministrativeActionTool;
import com.chiragshah.interceptj.tool.CalculatorArguments;
import com.chiragshah.interceptj.tool.CalculatorOperation;
import com.chiragshah.interceptj.tool.CalculatorTool;
import com.chiragshah.interceptj.tool.CustomerLookupTool;
import com.chiragshah.interceptj.tool.ToolRegistry;

class ToolExecutionGatewayTest {

    private ToolExecutionGateway gateway;

    @BeforeEach
    void setUp() {

        ToolRegistry registry =
                new ToolRegistry(
                        List.of(
                                new CalculatorTool(),
                                new CustomerLookupTool(),
                                new AdministrativeActionTool()));

        gateway = new ToolExecutionGateway(registry);
    }

    @Test
    void shouldExecuteRegisteredCalculatorTool() {

        ToolRequest<CalculatorArguments> request =
                new ToolRequest<>(
                        UUID.randomUUID(),
                        "GATEWAY-TEST-001",
                        CalculatorTool.TOOL_NAME,
                        new CalculatorArguments(
                                10,
                                5,
                                CalculatorOperation.ADD),
                        new UserContext(
                                "test-user",
                                Set.of("USER"),
                                Map.of()),
                        Instant.now());

        ToolResult<Object> result =
                gateway.execute(request);

        assertEquals(
                ToolExecutionStatus.EXECUTED,
                result.status());
    }

    @Test
    void shouldFailUnknownTool() {

        ToolRequest<CalculatorArguments> request =
                new ToolRequest<>(
                        UUID.randomUUID(),
                        "GATEWAY-TEST-002",
                        "unknown-tool",
                        new CalculatorArguments(
                                10,
                                5,
                                CalculatorOperation.ADD),
                        new UserContext(
                                "test-user",
                                Set.of("USER"),
                                Map.of()),
                        Instant.now());

        ToolResult<Object> result =
                gateway.execute(request);

        assertEquals(
                ToolExecutionStatus.FAILED,
                result.status());
    }
}