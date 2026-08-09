package com.chiragshah.interceptj.experiment;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

@Component
public class ExperimentRunResultStore {

    private final List<ExperimentRunResult> results =
            new CopyOnWriteArrayList<>();

    public void addAll(
            List<ExperimentRunResult> newResults) {

        results.addAll(newResults);
    }

    public List<ExperimentRunResult> getResults() {

        return List.copyOf(results);
    }

    public void clear() {

        results.clear();
    }

    public int size() {

        return results.size();
    }
}