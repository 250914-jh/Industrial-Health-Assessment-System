package com.industrial.health.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.industrial.health.config.HealthProperties;
import com.industrial.health.model.SensorRecord;
import com.industrial.health.repo.SensorRecordRepository;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DataService {
    private final SensorRecordRepository repo;
    private final HealthProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    public DataService(SensorRecordRepository repo, HealthProperties props) {
        this.repo = repo; this.props = props;
    }

    public int importCsv(MultipartFile file) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] header = reader.readNext();
            if (header == null) throw new IllegalArgumentException("CSV 为空");

            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < header.length; i++) idx.put(header[i].trim(), i);

            List<String> feats = props.getFeatureColumns();
            String labelCol = props.getLabelColumn();
            for (String f : feats) {
                if (!idx.containsKey(f)) throw new IllegalArgumentException("缺少特征列: " + f);
            }
            if (!idx.containsKey(labelCol)) throw new IllegalArgumentException("缺少标签列: " + labelCol);

            int count = 0;
            String[] line;
            List<SensorRecord> batch = new ArrayList<>();
            while ((line = reader.readNext()) != null) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String f : feats) {
                    String v = line[idx.get(f)].trim();
                    row.put(f, v.isEmpty() ? 0.0 : Double.parseDouble(v));
                }
                SensorRecord r = new SensorRecord();
                r.setFeaturesJson(mapper.writeValueAsString(row));
                r.setLabel(line[idx.get(labelCol)].trim());
                batch.add(r);
                count++;
                if (batch.size() >= 500) { repo.saveAll(batch); batch.clear(); }
            }
            if (!batch.isEmpty()) repo.saveAll(batch);
            return count;
        }
    }
}
