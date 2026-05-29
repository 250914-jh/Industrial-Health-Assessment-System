package com.industrial.health.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "health")
public class HealthProperties {
    private List<String> featureColumns;
    private String labelColumn;
    private Map<String, Integer> labelMapping;
    private List<String> alertLabels;

    public List<String> getFeatureColumns() { return featureColumns; }
    public void setFeatureColumns(List<String> v) { this.featureColumns = v; }
    public String getLabelColumn() { return labelColumn; }
    public void setLabelColumn(String v) { this.labelColumn = v; }
    public Map<String, Integer> getLabelMapping() { return labelMapping; }
    public void setLabelMapping(Map<String, Integer> v) { this.labelMapping = v; }
    public List<String> getAlertLabels() { return alertLabels; }
    public void setAlertLabels(List<String> v) { this.alertLabels = v; }
}
