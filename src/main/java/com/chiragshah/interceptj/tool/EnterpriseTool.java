package com.chiragshah.interceptj.tool;

import com.chiragshah.interceptj.model.ToolArguments;

public interface EnterpriseTool<A extends ToolArguments, R> {

    String getName();

    Class<A> getArgumentType();

    R execute(A arguments);
}