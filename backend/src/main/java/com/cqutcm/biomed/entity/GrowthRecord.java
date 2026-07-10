package com.cqutcm.biomed.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("growth_record")
public class GrowthRecord {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    @TableField("herb_name")
    private String herbName;
    @TableField("district")
    private String district;
    @TableField("temperature")
    private BigDecimal temperature;
    @TableField("humidity")
    private BigDecimal humidity;
    @TableField("soil_ph")
    private BigDecimal soilPh;
    @TableField("growth_stage")
    private String growthStage;
    @TableField("collect_source")
    private String collectSource;
    @TableField("recorder_name")
    private String recorderName;
    @TableField("recorder_role")
    private String recorderRole;
    @TableField("recorded_at")
    private LocalDateTime recordedAt;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(value = "updated_at", fill = FieldFill.UPDATE)
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

