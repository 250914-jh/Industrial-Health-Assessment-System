package com.industrial.health.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert")
public class Alert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String level;      // warning / fault
    private String predicted;  // 预测标签
    private Double confidence; // 置信度

    @Lob @Column(columnDefinition = "TEXT")
    private String featuresJson;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLevel() { return level; }
    public void setLevel(String v) { this.level = v; }
    public String getPredicted() { return predicted; }
    public void setPredicted(String v) { this.predicted = v; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double v) { this.confidence = v; }
    public String getFeaturesJson() { return featuresJson; }
    public void setFeaturesJson(String v) { this.featuresJson = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
