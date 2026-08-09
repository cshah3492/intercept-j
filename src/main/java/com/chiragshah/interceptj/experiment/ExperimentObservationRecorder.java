package com.chiragshah.interceptj.experiment;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

@Component
public class ExperimentObservationRecorder {

    private final List<ToolInvocationObservation> observations =
            new CopyOnWriteArrayList<>();

    public void record(
            ToolInvocationObservation observation) {

        observations.add(observation);
    }

    public List<ToolInvocationObservation> getObservations() {

        return List.copyOf(observations);
    }

    public List<ToolInvocationObservation> findByScenarioId(
            String scenarioId) {

        return observations.stream()
                .filter(observation ->
                        observation.scenarioId()
                                .equals(scenarioId))
                .toList();
    }

    public void clear() {
        observations.clear();
    }

    public int size() {
        return observations.size();
    }
}