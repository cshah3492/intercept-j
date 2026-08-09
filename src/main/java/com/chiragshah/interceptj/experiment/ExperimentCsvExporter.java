package com.chiragshah.interceptj.experiment;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ExperimentCsvExporter {

    public String export(
            List<ExperimentRunResult> results) {

        StringBuilder csv =
                new StringBuilder();

        csv.append(
                "runId,scenarioId,experimentMode,iteration,"
                + "toolRequested,executionStatus,policyOutcome,"
                + "unauthorizedExecution,attackSucceeded,"
                + "endToEndDurationNanos,toolPathDurationNanos,"
                + "protectedPathDurationNanos,completedAt,errorType\n");

        for (ExperimentRunResult result : results) {

            append(csv, result.runId());
            append(csv, result.scenarioId());
            append(csv, result.experimentMode());
            append(csv, result.iteration());
            append(csv, result.toolRequested());
            append(csv, result.executionStatus());
            append(csv, result.policyOutcome());
            append(csv, result.unauthorizedExecution());
            append(csv, result.attackSucceeded());
            append(csv, result.endToEndDurationNanos());
            append(csv, result.toolPathDurationNanos());
            append(csv, result.protectedPathDurationNanos());
            append(csv, result.completedAt());

            csv.append(
                    csvValue(result.errorType()));

            csv.append('\n');
        }

        return csv.toString();
    }

    private void append(
            StringBuilder csv,
            Object value) {

        csv.append(csvValue(value));
        csv.append(',');
    }

    private String csvValue(
            Object value) {

        if (value == null) {
            return "";
        }

        String text =
                value.toString()
                        .replace("\"", "\"\"");

        return "\""
                + text
                + "\"";
    }
}