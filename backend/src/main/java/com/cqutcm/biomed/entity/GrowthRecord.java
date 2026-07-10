package com.cqutcm.biomed.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GrowthRecord {
    private String id;
    private String herbName;
    private String district;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal soilPh;
    private String growthStage;
    private String collectSource;
    private String recorderName;
    private String recorderRole;
    private LocalDateTime recordedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHerbName() { return herbName; }
    public void setHerbName(String herbName) { this.herbName = herbName; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    public BigDecimal getHumidity() { return humidity; }
    public void setHumidity(BigDecimal humidity) { this.humidity = humidity; }
    public BigDecimal getSoilPh() { return soilPh; }
    public void setSoilPh(BigDecimal soilPh) { this.soilPh = soilPh; }
    public String getGrowthStage() { return growthStage; }
    public void setGrowthStage(String growthStage) { this.growthStage = growthStage; }
    public String getCollectSource() { return collectSource; }
    public void setCollectSource(String collectSource) { this.collectSource = collectSource; }
    public String getRecorderName() { return recorderName; }
    public void setRecorderName(String recorderName) { this.recorderName = recorderName; }
    public String getRecorderRole() { return recorderRole; }
    public void setRecorderRole(String recorderRole) { this.recorderRole = recorderRole; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
