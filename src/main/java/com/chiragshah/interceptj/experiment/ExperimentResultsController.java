package com.chiragshah.interceptj.experiment;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentResultsController {

    private final ExperimentObservationRecorder recorder;

    public ExperimentResultsController(
            ExperimentObservationRecorder recorder) {

        this.recorder = recorder;
    }

    @GetMapping("/observations")
    public List<ToolInvocationObservation> observations() {

        return recorder.getObservations();
    }
}