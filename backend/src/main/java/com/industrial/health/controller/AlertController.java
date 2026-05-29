package com.industrial.health.controller;

import com.industrial.health.config.HealthProperties;
import com.industrial.health.model.Alert;
import com.industrial.health.repo.AlertRepository;
import com.industrial.health.repo.SensorRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class AlertController {
    private final AlertRepository alertRepo;
    private final SensorRecordRepository recordRepo;
    private final HealthProperties props;

    public AlertController(AlertRepository a, SensorRecordRepository r, HealthProperties p) {
        this.alertRepo = a; this.recordRepo = r; this.props = p;
    }

    @GetMapping("/api/alerts")
    public Page<Alert> list(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size) {
        return alertRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    @GetMapping("/api/dashboard/stats")
    public Map<String, Object> stats() {
        Map<String, Long> labelDist = new LinkedHashMap<>();
        for (String k : props.getLabelMapping().keySet()) {
            labelDist.put(k, recordRepo.countByLabel(k));
        }
        Map<String, Long> alertDist = new LinkedHashMap<>();
        for (String k : props.getAlertLabels()) {
            alertDist.put(k, alertRepo.countByLevel(k));
        }
        Map<String, Object> ret = new HashMap<>();
        ret.put("totalRecords", recordRepo.count());
        ret.put("totalAlerts", alertRepo.count());
        ret.put("labelDistribution", labelDist);
        ret.put("alertDistribution", alertDist);
        return ret;
    }
}
