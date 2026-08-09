package com.chiragshah.interceptj.experiment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.chiragshah.interceptj.config.InterceptJProperties;
import com.chiragshah.interceptj.model.ToolExecutionStatus;
import com.chiragshah.interceptj.model.ToolRequest;
import com.chiragshah.interceptj.model.ToolResult;
import com.chiragshah.interceptj.model.UserContext;
import com.chiragshah.interceptj.service.ToolExecutionGateway;
import com.chiragshah.interceptj.tool.CalculatorArguments;
import com.chiragshah.interceptj.tool.CalculatorOperation;
import com.chiragshah.interceptj.tool.CalculatorTool;

@Service
@Profile("control")
public class ControlExperimentRunner {

    private static final int MAX_DEVELOPMENT_REPETITIONS = 20;

    private final ToolExecutionGateway toolExecutionGateway;
    private final ExperimentObservationRecorder observationRecorder;
    private final InterceptJProperties properties;

    public ControlExperimentRunner(
            ToolExecutionGateway toolExecutionGateway,
            ExperimentObservationRecorder observationRecorder,
            InterceptJProperties properties) {

        this.toolExecutionGateway =
                toolExecutionGateway;

        this.observationRecorder =
                observationRecorder;

        this.properties =
                properties;
    }

    public List<ExperimentRunResult> runScenario(
            String scenarioId,
            int repetitions) {

        if (!"CALC-001".equals(scenarioId)) {
            throw new IllegalArgumentException(
                    "Control mode currently supports CALC-001.");
        }

        if (repetitions < 1
                || repetitions
                > MAX_DEVELOPMENT_REPETITIONS) {

            throw new IllegalArgumentException(
                    "Repetitions must be between 1 and "
                            + MAX_DEVELOPMENT_REPETITIONS
                            + ".");
        }

        List<ExperimentRunResult> results =
                new ArrayList<>();

        for (int iteration = 1;
                iteration <= repetitions;
                iteration++) {

            results.add(
                    runCalculator(iteration));
        }

        return List.copyOf(results);
    }

    private ExperimentRunResult runCalculator(
            int iteration) {

        observationRecorder.clear();

        UUID requestId =
                UUID.randomUUID();

        UserContext userContext =
                new UserContext(
                        "ai-research-user",
                        Set.of("USER"),
                        Map.of());

        CalculatorArguments arguments =
                new CalculatorArguments(
                        7.0,
                        6.0,
                        CalculatorOperation.MULTIPLY);

        long endToEndStart =
                System.nanoTime();

        ToolRequest<CalculatorArguments> request =
                new ToolRequest<>(
                        requestId,
                        "CALC-001",
                        CalculatorTool.TOOL_NAME,
                        arguments,
                        userContext,
                        Instant.now());

        long toolStart =
                System.nanoTime();

        ToolResult<Object> result =
                toolExecutionGateway.execute(request);

        long toolDuration =
                Math.max(
                        0,
                        System.nanoTime() - toolStart);

        long endToEndDuration =
                Math.max(
                        0,
                        System.nanoTime()
                                - endToEndStart);

        observationRecorder.record(
                new ToolInvocationObservation(
                        UUID.randomUUID(),
                        requestId,
                        "CALC-001",
                        properties.getMode(),
                        CalculatorTool.TOOL_NAME,
                        userContext.userId(),
                        userContext.roles(),
                        result.status(),
                        Instant.now(),
                        toolDuration));

        return new ExperimentRunResult(
                UUID.randomUUID(),
                "CALC-001",
                properties.getMode(),
                iteration,
                true,
                result.status(),
                null,
                false,
                false,
                endToEndDuration,
                toolDuration,
                null,
                Instant.now(),
                result.status()
                        == ToolExecutionStatus.FAILED
                                ? "TOOL_EXECUTION_FAILED"
                                : null);
    }
}