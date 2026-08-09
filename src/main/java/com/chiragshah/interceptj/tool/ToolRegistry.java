package com.chiragshah.interceptj.tool;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ToolRegistry {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ToolRegistry.class);

    private final Map<String, EnterpriseTool<?, ?>> tools;

    public ToolRegistry(List<EnterpriseTool<?, ?>> availableTools) {

        Map<String, EnterpriseTool<?, ?>> registry = new TreeMap<>();

        for (EnterpriseTool<?, ?> tool : availableTools) {

            String name = tool.getName();

            if (name == null || name.isBlank()) {
                throw new IllegalStateException(
                        "Registered tool must have a valid name.");
            }

            EnterpriseTool<?, ?> existing =
                    registry.putIfAbsent(name, tool);

            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate tool name: " + name);
            }
        }

        this.tools = Collections.unmodifiableMap(registry);

        LOGGER.info("Registered enterprise tools: {}", tools.keySet());
    }

    public EnterpriseTool<?, ?> getRequiredTool(String toolName) {

        if (toolName == null || toolName.isBlank()) {
            throw new IllegalArgumentException(
                    "Tool name cannot be blank.");
        }

        EnterpriseTool<?, ?> tool = tools.get(toolName);

        if (tool == null) {
            throw new IllegalArgumentException(
                    "Unknown enterprise tool: " + toolName);
        }

        return tool;
    }

    public boolean contains(String toolName) {
        return toolName != null && tools.containsKey(toolName);
    }

    public Set<String> getRegisteredToolNames() {
        return tools.keySet();
    }

    public int size() {
        return tools.size();
    }
}