package com.cqutcm.biomed.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("evaluation_record")
public class EvaluationRecord {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    @TableField("herb_name")
    private String herbName;
    @TableField("indicator")
    private String indicator;
    @TableField("score")
    private BigDecimal score;
    @TableField("result")
    private String result;
    @TableField("application_material")
    private String applicationMaterial;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(value = "updated_at", fill = FieldFill.UPDATE)
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

