package com.chiragshah.interceptj.experiment;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/experiments")
@Profile({"ai-unprotected", "ai-protected"})
public class ExperimentRunnerController {

    private final AiExperimentRunner experimentRunner;
    private final ExperimentRunResultStore resultStore;
    private final ExperimentCsvExporter csvExporter;

    public ExperimentRunnerController(
            AiExperimentRunner experimentRunner,
            ExperimentRunResultStore resultStore,
            ExperimentCsvExporter csvExporter) {

        this.experimentRunner =
                experimentRunner;

        this.resultStore =
                resultStore;

        this.csvExporter =
                csvExporter;
    }

    @PostMapping("/run")
    public List<ExperimentRunResult> run(
            @RequestParam String scenarioId,
            @RequestParam(defaultValue = "1")
            int repetitions) {

        List<ExperimentRunResult> results =
                experimentRunner.runScenario(
                        scenarioId,
                        repetitions);

        resultStore.addAll(results);

        return results;
    }

    @GetMapping("/results")
    public List<ExperimentRunResult> results() {

        return resultStore.getResults();
    }

    @GetMapping(
        value = "/results.csv",
        produces = "text/csv")
    public String csv() {

        return csvExporter.export(
                resultStore.getResults());
    }

    @DeleteMapping("/results")
    public Map<String, Object> clearResults() {

        resultStore.clear();

        return Map.of(
                "status", "cleared",
                "resultCount",
                resultStore.size());
    }
}