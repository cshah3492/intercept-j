package com.chiragshah.interceptj.tool;

import com.chiragshah.interceptj.model.ToolArguments;
import org.springframework.security.access.prepost.PreAuthorize;

public interface EnterpriseTool<A extends ToolArguments, R> {

    String getName();

    Class<A> getArgumentType();

    @PreAuthorize("@gatewayExecutionContext.isActive()")
    R execute(A arguments);
}
