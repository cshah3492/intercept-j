package com.chiragshah.interceptj.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ToolRegistryTest {

    @Autowired
    private ToolRegistry toolRegistry;

    @Test
    void shouldRegisterAllEnterpriseTools() {

        assertEquals(3, toolRegistry.size());

        assertTrue(toolRegistry.contains(
                CalculatorTool.TOOL_NAME));

        assertTrue(toolRegistry.contains(
                CustomerLookupTool.TOOL_NAME));

        assertTrue(toolRegistry.contains(
                AdministrativeActionTool.TOOL_NAME));
    }
}