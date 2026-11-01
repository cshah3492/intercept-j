package com.chiragshah.interceptj.service;

import java.util.Objects;

import org.springframework.stereotype.Component;

/**
 * Tracks the narrow execution scope established by ToolExecutionGateway.
 * Enterprise tools reject invocations made outside this scope.
 */
@Component
public final class GatewayExecutionContext {

    private final ThreadLocal<Integer> depth =
            ThreadLocal.withInitial(() -> 0);

    public boolean isActive() {
        return depth.get() > 0;
    }

    ExecutionScope enter() {
        depth.set(depth.get() + 1);
        return new ExecutionScope(this);
    }

    private void exit() {
        int currentDepth = depth.get();

        if (currentDepth <= 1) {
            depth.remove();
            return;
        }

        depth.set(currentDepth - 1);
    }

    static final class ExecutionScope implements AutoCloseable {

        private GatewayExecutionContext context;

        private ExecutionScope(GatewayExecutionContext context) {
            this.context = Objects.requireNonNull(context);
        }

        @Override
        public void close() {
            if (context != null) {
                context.exit();
                context = null;
            }
        }
    }
}
