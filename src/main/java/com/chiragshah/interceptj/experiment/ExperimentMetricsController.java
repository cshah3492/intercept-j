package com.chiragshah.interceptj.experiment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentMetricsController {

    private final ExperimentRunResultStore resultStore;
    private final ExperimentMetricsCalculator metricsCalculator;

    public ExperimentMetricsController(
            ExperimentRunResultStore resultStore,
            ExperimentMetricsCalculator metricsCalculator) {

        this.resultStore =
                resultStore;

        this.metricsCalculator =
                metricsCalculator;
    }

    @GetMapping("/metrics")
    public ExperimentMetrics metrics() {

        return metricsCalculator.calculate(
                resultStore.getResults());
    }
}