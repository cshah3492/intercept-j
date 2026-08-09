package com.chiragshah.interceptj.tool;

import java.util.Objects;

import com.chiragshah.interceptj.model.ToolArguments;

import jakarta.validation.constraints.NotNull;

public record CalculatorArguments(

        double firstNumber,

        double secondNumber,

        @NotNull
        CalculatorOperation operation) implements ToolArguments {

    public CalculatorArguments {
        Objects.requireNonNull(operation, "operation cannot be null");
    }
}