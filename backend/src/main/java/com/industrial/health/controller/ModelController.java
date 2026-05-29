package com.industrial.health.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.industrial.health.config.HealthProperties;
import com.industrial.health.ml.DecisionTreeService;
import com.industrial.health.model.Alert;
import com.industrial.health.repo.AlertRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ModelController {
    private final DecisionTreeService dt;
    private final AlertRepository alertRepo;
    private final HealthProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    public ModelController(DecisionTreeService dt, AlertRepository a, HealthProperties p) {
        this.dt = dt; this.alertRepo = a; this.props = p;
    }

    @PostMapping("/api/model/train")
    public DecisionTreeService.TrainResult train() throws Exception {
        return dt.train();
    }

    @GetMapping("/api/model/info")
    public DecisionTreeService.TrainResult info() {
        return dt.info();
    }

    @PostMapping("/api/predict")
    public Map<String, Object> predict(@RequestBody Map<String, Double> features) throws Exception {
        DecisionTreeService.PredictResult r = dt.predict(features);
        boolean alert = props.getAlertLabels().contains(r.label());
        if (alert) {
            Alert a = new Alert();
            a.setLevel(r.label());
            a.setPredicted(r.label());
            a.setConfidence(r.confidence());
            a.setFeaturesJson(mapper.writeValueAsString(features));
            alertRepo.save(a);
        }
        return Map.of(
            "label", r.label(),
            "confidence", r.confidence(),
            "alert", alert
        );
    }
}
