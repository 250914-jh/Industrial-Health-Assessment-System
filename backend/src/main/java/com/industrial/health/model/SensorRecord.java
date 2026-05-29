package com.industrial.health.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_record")
public class SensorRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 以 JSON 字符串保存所有特征 {"temperature":65.2,...} */
    @Lob @Column(columnDefinition = "TEXT")
    private String featuresJson;

    private String label;
    private LocalDateTime collectedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFeaturesJson() { return featuresJson; }
    public void setFeaturesJson(String v) { this.featuresJson = v; }
    public String getLabel() { return label; }
    public void setLabel(String v) { this.label = v; }
    public LocalDateTime getCollectedAt() { return collectedAt; }
    public void setCollectedAt(LocalDateTime v) { this.collectedAt = v; }
}
