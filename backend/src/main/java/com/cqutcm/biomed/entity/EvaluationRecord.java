package com.cqutcm.biomed.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EvaluationRecord {
    private String id;
    private String herbName;
    private String indicator;
    private BigDecimal score;
    private String result;
    private String applicationMaterial;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHerbName() { return herbName; }
    public void setHerbName(String herbName) { this.herbName = herbName; }
    public String getIndicator() { return indicator; }
    public void setIndicator(String indicator) { this.indicator = indicator; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getApplicationMaterial() { return applicationMaterial; }
    public void setApplicationMaterial(String applicationMaterial) { this.applicationMaterial = applicationMaterial; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
