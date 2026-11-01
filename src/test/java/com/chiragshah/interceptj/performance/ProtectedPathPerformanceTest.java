package com.chiragshah.interceptj.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.chiragshah.interceptj.audit.InMemoryAuditLogger;
import com.chiragshah.interceptj.model.AuditEvent;
import com.chiragshah.interceptj.model.ToolExecutionStatus;
import com.chiragshah.interceptj.model.ToolNames;
import com.chiragshah.interceptj.model.ToolRequest;
import com.chiragshah.interceptj.model.UserContext;
import com.chiragshah.interceptj.service.ToolExecutionGateway;
import com.chiragshah.interceptj.tool.CalculatorArguments;
import com.chiragshah.interceptj.tool.CalculatorOperation;

@SpringBootTest(properties = {
        "interceptj.mode=ai-protected",
        "interceptj.ai.enabled=true",
        "interceptj.enforcement.enabled=true",
        "interceptj.audit.enabled=true",
        "spring.ai.model.chat=none",
        "logging.level.com.chiragshah.interceptj.audit=OFF"
})
class ProtectedPathPerformanceTest {

    private static final int WARMUP_INVOCATIONS = 200;
    private static final int MEASURED_INVOCATIONS = 1_000;

    @Autowired
    private ToolExecutionGateway gateway;

    @Autowired
    private InMemoryAuditLogger auditLogger;

    private final CalculatorArguments arguments =
            new CalculatorArguments(
                    7,
                    6,
                    CalculatorOperation.MULTIPLY);

    private final UserContext userContext =
            new UserContext(
                    "performance-test-user",
                    Set.of("USER"),
                    Map.of());

    @BeforeEach
    void clearAuditEvents() {
        auditLogger.clear();
    }

    @Test
    void measureProtectedPathAfterWarmup() throws IOException {
        for (int index = 0; index < WARMUP_INVOCATIONS; index++) {
            assertExecuted(execute("PERF-WARMUP"));
        }

        auditLogger.clear();

        for (int index = 0; index < MEASURED_INVOCATIONS; index++) {
            assertExecuted(execute("PERF-MEASURED"));
        }

        List<AuditEvent> events = auditLogger.getEvents();

        assertEquals(MEASURED_INVOCATIONS, events.size());

        long[] durations = events.stream()
                .mapToLong(AuditEvent::totalDurationNanos)
                .toArray();

        writeResults(durations);
    }

    private ToolExecutionStatus execute(String scenarioId) {
        ToolRequest<CalculatorArguments> request =
                new ToolRequest<>(
                        UUID.randomUUID(),
                        scenarioId,
                        ToolNames.CALCULATOR,
                        arguments,
                        userContext,
                        Instant.now());

        return gateway.execute(request).status();
    }

    private void assertExecuted(ToolExecutionStatus status) {
        assertEquals(ToolExecutionStatus.EXECUTED, status);
    }

    private void writeResults(long[] durations) throws IOException {
        Path outputDirectory =
                Path.of("target", "benchmark-results");

        Files.createDirectories(outputDirectory);

        StringBuilder rawCsv =
                new StringBuilder("invocation,duration_nanos\n");

        for (int index = 0; index < durations.length; index++) {
            rawCsv.append(index + 1)
                    .append(',')
                    .append(durations[index])
                    .append('\n');
        }

        Files.writeString(
                outputDirectory.resolve(
                        "complete-mediation-performance.csv"),
                rawCsv);

        long[] sortedDurations =
                Arrays.copyOf(durations, durations.length);

        Arrays.sort(sortedDurations);

        double mean = Arrays.stream(durations)
                .average()
                .orElseThrow();

        double variance = Arrays.stream(durations)
                .mapToDouble(duration -> {
                    double difference = duration - mean;
                    return difference * difference;
                })
                .average()
                .orElseThrow();

        String summary = String.format(
                Locale.ROOT,
                """
                warmup_invocations=%d
                measured_invocations=%d
                mean_ms=%.6f
                median_ms=%.6f
                standard_deviation_ms=%.6f
                p95_ms=%.6f
                p99_ms=%.6f
                """,
                        WARMUP_INVOCATIONS,
                        MEASURED_INVOCATIONS,
                        nanosToMillis(mean),
                        nanosToMillis(median(sortedDurations)),
                        nanosToMillis(Math.sqrt(variance)),
                        nanosToMillis(percentile(
                                sortedDurations,
                                0.95)),
                        nanosToMillis(percentile(
                                sortedDurations,
                                0.99)));

        Files.writeString(
                outputDirectory.resolve(
                        "complete-mediation-performance-summary.txt"),
                summary);

        System.out.println("Protected-path benchmark\n" + summary);
    }

    private double median(long[] sortedDurations) {
        int middle = sortedDurations.length / 2;

        if (sortedDurations.length % 2 == 0) {
            return (sortedDurations[middle - 1]
                    + sortedDurations[middle]) / 2.0;
        }

        return sortedDurations[middle];
    }

    private long percentile(
            long[] sortedDurations,
            double percentile) {

        int index = (int) Math.ceil(
                percentile * sortedDurations.length) - 1;

        return sortedDurations[Math.max(0, index)];
    }

    private double nanosToMillis(double nanos) {
        return nanos / 1_000_000.0;
    }
}
