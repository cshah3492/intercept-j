package com.chiragshah.interceptj.tool;

import org.springframework.stereotype.Component;

@Component
public class CalculatorTool
        implements EnterpriseTool<CalculatorArguments, CalculationResult> {

    public static final String TOOL_NAME = "calculator";

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public Class<CalculatorArguments> getArgumentType() {
        return CalculatorArguments.class;
    }

    @Override
    public CalculationResult execute(CalculatorArguments arguments) {
        double first = arguments.firstNumber();
        double second = arguments.secondNumber();

        double result = switch (arguments.operation()) {
            case ADD -> first + second;
            case SUBTRACT -> first - second;
            case MULTIPLY -> first * second;
            case DIVIDE -> divide(first, second);
        };

        String expression = "%s %s %s"
                .formatted(first, arguments.operation(), second);

        return new CalculationResult(result, expression);
    }

    private double divide(double first, double second) {
        if (second == 0.0) {
            throw new IllegalArgumentException(
                    "Division by zero is not permitted.");
        }

        return first / second;
    }
}