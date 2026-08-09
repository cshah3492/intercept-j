package com.chiragshah.interceptj.agent;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.chiragshah.interceptj.config.InterceptJProperties;
import com.chiragshah.interceptj.experiment.ExperimentObservationRecorder;
import com.chiragshah.interceptj.experiment.ToolInvocationObservation;
import com.chiragshah.interceptj.interceptor.ToolAuthorizationDeniedException;
import com.chiragshah.interceptj.model.ToolArguments;
import com.chiragshah.interceptj.model.ToolExecutionStatus;
import com.chiragshah.interceptj.model.ToolRequest;
import com.chiragshah.interceptj.model.ToolResult;
import com.chiragshah.interceptj.model.UserContext;
import com.chiragshah.interceptj.service.ToolExecutionGateway;
import com.chiragshah.interceptj.tool.AdministrativeAction;
import com.chiragshah.interceptj.tool.AdministrativeActionArguments;
import com.chiragshah.interceptj.tool.AdministrativeActionTool;
import com.chiragshah.interceptj.tool.CalculatorArguments;
import com.chiragshah.interceptj.tool.CalculatorOperation;
import com.chiragshah.interceptj.tool.CalculatorTool;

import com.chiragshah.interceptj.tool.CustomerLookupArguments;
import com.chiragshah.interceptj.tool.CustomerLookupTool;

@Component
@Profile({"ai-unprotected", "ai-protected"})
public class AiToolBridge {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AiToolBridge.class);

    private final ToolExecutionGateway toolExecutionGateway;
    private final ExperimentObservationRecorder observationRecorder;
    private final InterceptJProperties properties;

    public AiToolBridge(
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

    @Tool(
        name = "calculator",
        description = """
                Performs arithmetic using the enterprise calculator.
                Supported operations are ADD, SUBTRACT,
                MULTIPLY, and DIVIDE.
                """
    )
    public ToolResult<Object> calculate(

            @ToolParam(
                description = "First numeric operand")
            double firstNumber,

            @ToolParam(
                description = "Second numeric operand")
            double secondNumber,

            @ToolParam(
                description =
                    "Arithmetic operation: ADD, SUBTRACT, MULTIPLY, or DIVIDE")
            CalculatorOperation operation,

            ToolContext toolContext) {

        UserContext userContext =
                trustedUser(toolContext);

        CalculatorArguments arguments =
                new CalculatorArguments(
                        firstNumber,
                        secondNumber,
                        operation);

        ToolRequest<CalculatorArguments> request =
                new ToolRequest<>(
                        UUID.randomUUID(),
                        trustedScenarioId(toolContext),
                        CalculatorTool.TOOL_NAME,
                        arguments,
                        userContext,
                        Instant.now());

        LOGGER.info(
                "AI requested calculator tool: "
                        + "firstNumber={}, secondNumber={}, "
                        + "operation={}, user={}",
                firstNumber,
                secondNumber,
                operation,
                userContext.userId());

        return executeAndRecord(request);
    }

    @Tool(
        name = "administrative-action",
        description = """
                Performs a simulated administrative action on an
                enterprise resource. Supported actions are
                RESTART_SERVICE, ROTATE_CREDENTIALS, and
                DISABLE_ACCOUNT.
                """
    )
    public ToolResult<Object> administrativeAction(

            @ToolParam(
                description =
                    "Administrative action to request")
            AdministrativeAction action,

            @ToolParam(
                description =
                    "Enterprise resource on which to perform the action")
            String targetResource,

            @ToolParam(
                description =
                    "Reason for requesting the administrative action")
            String justification,

            ToolContext toolContext) {

        UserContext userContext =
                trustedUser(toolContext);

        AdministrativeActionArguments arguments =
                new AdministrativeActionArguments(
                        action,
                        targetResource,
                        justification);

        ToolRequest<AdministrativeActionArguments> request =
                new ToolRequest<>(
                        UUID.randomUUID(),
                        trustedScenarioId(toolContext),
                        AdministrativeActionTool.TOOL_NAME,
                        arguments,
                        userContext,
                        Instant.now());

        LOGGER.info(
                "AI requested administrative tool: "
                        + "action={}, targetResource={}, user={}",
                action,
                targetResource,
                userContext.userId());

        return executeAndRecord(request);
    }
    
    @Tool(
    	    name = "customer-lookup",
    	    description = """
    	            Retrieves a simulated enterprise customer record.
    	            The request requires a customer ID and requested region.
    	            """
    	)
    	public ToolResult<Object> customerLookup(

    	        @ToolParam(
    	            description =
    	                "Customer identifier, for example CUST-1001")
    	        String customerId,

    	        @ToolParam(
    	            description =
    	                "Requested customer region, for example EAST or WEST")
    	        String requestedRegion,

    	        ToolContext toolContext) {

    	    UserContext userContext =
    	            trustedUser(toolContext);

    	    CustomerLookupArguments arguments =
    	            new CustomerLookupArguments(
    	                    customerId,
    	                    requestedRegion);

    	    ToolRequest<CustomerLookupArguments> request =
    	            new ToolRequest<>(
    	                    UUID.randomUUID(),
    	                    trustedScenarioId(toolContext),
    	                    CustomerLookupTool.TOOL_NAME,
    	                    arguments,
    	                    userContext,
    	                    Instant.now());

    	    LOGGER.info(
    	            "AI requested customer lookup: "
    	                    + "customerId={}, requestedRegion={}, user={}",
    	            customerId,
    	            requestedRegion,
    	            userContext.userId());

    	    return executeAndRecord(request);
    	}

    private ToolResult<Object> executeAndRecord(
            ToolRequest<? extends ToolArguments> request) {

        long startTime = System.nanoTime();

        try {

            ToolResult<Object> result =
                    toolExecutionGateway.execute(request);

            recordObservation(
                    request,
                    result.status(),
                    elapsed(startTime));

            return result;

        } catch (ToolAuthorizationDeniedException exception) {

            recordObservation(
                    request,
                    ToolExecutionStatus.BLOCKED,
                    elapsed(startTime));

            throw exception;

        } catch (RuntimeException exception) {

            recordObservation(
                    request,
                    ToolExecutionStatus.FAILED,
                    elapsed(startTime));

            throw exception;
        }
    }

    private void recordObservation(
            ToolRequest<? extends ToolArguments> request,
            ToolExecutionStatus status,
            long durationNanos) {

        observationRecorder.record(
                new ToolInvocationObservation(
                        UUID.randomUUID(),
                        request.requestId(),
                        request.scenarioId(),
                        properties.getMode(),
                        request.toolName(),
                        request.userContext().userId(),
                        request.userContext().roles(),
                        status,
                        Instant.now(),
                        durationNanos));

        LOGGER.info(
                "EXPERIMENT scenario={}, mode={}, "
                        + "tool={}, user={}, status={}, "
                        + "durationNanos={}",
                request.scenarioId(),
                properties.getMode(),
                request.toolName(),
                request.userContext().userId(),
                status,
                durationNanos);
    }

    private UserContext trustedUser(
            ToolContext toolContext) {

        if (toolContext == null) {
            throw new IllegalStateException(
                    "Trusted tool context is required.");
        }

        Object value =
                toolContext.getContext()
                        .get("userContext");

        if (!(value instanceof UserContext userContext)) {
            throw new IllegalStateException(
                    "Trusted user context is required.");
        }

        return userContext;
    }

    private String trustedScenarioId(
            ToolContext toolContext) {

        if (toolContext == null) {
            throw new IllegalStateException(
                    "Trusted tool context is required.");
        }

        Object value =
                toolContext.getContext()
                        .get("scenarioId");

        if (!(value instanceof String scenarioId)
                || scenarioId.isBlank()) {

            throw new IllegalStateException(
                    "Trusted scenario ID is required.");
        }

        return scenarioId.trim();
    }

    private long elapsed(
            long startTime) {

        return Math.max(
                0,
                System.nanoTime() - startTime);
    }
}