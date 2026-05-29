package com.industrial.health.controller;

import com.industrial.health.config.HealthProperties;
import com.industrial.health.model.SensorRecord;
import com.industrial.health.repo.SensorRecordRepository;
import com.industrial.health.service.DataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/data")
public class DataController {
    private final DataService dataService;
    private final SensorRecordRepository repo;
    private final HealthProperties props;

    public DataController(DataService ds, SensorRecordRepository r, HealthProperties p) {
        this.dataService = ds; this.repo = r; this.props = p;
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws Exception {
        int n = dataService.importCsv(file);
        return Map.of("imported", n, "total", repo.count());
    }

    @GetMapping("/list")
    public Page<SensorRecord> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return repo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }

    @GetMapping("/schema")
    public Map<String, Object> schema() {
        return Map.of(
            "features", props.getFeatureColumns(),
            "label", props.getLabelColumn(),
            "labels", props.getLabelMapping().keySet()
        );
    }

    @DeleteMapping("/all")
    public Map<String, Object> clear() {
        long c = repo.count();
        repo.deleteAll();
        return Map.of("deleted", c);
    }
}
