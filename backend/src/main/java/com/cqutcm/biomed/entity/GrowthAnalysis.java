package com.cqutcm.biomed.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;

@TableName("growth_analysis")
public class GrowthAnalysis {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    @TableField("analysis_name")
    private String analysisName;
    @TableField("herb_name")
    private String herbName;
    @TableField("district")
    private String district;
    @TableField("indicator")
    private String indicator;
    @TableField("baseline")
    private String baseline;
    @TableField("current_value")
    private String currentValue;
    @TableField("difference_desc")
    private String differenceDesc;
    @TableField("trend")
    private String trend;
    @TableField("conclusion")
    private String conclusion;
    @TableField("analyst_name")
    private String analystName;
    @TableField("analyzed_at")
    private LocalDateTime analyzedAt;
    @TableField("status")
    private String status;
    @TableField("reviewer_name")
    private String reviewerName;
    @TableField("review_comment")
    private String reviewComment;
    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;
    @Version
    @TableField("version")
    private Integer version;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(value = "updated_at", fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAnalysisName() { return analysisName; }
    public void setAnalysisName(String analysisName) { this.analysisName = analysisName; }
    public String getHerbName() { return herbName; }
    public void setHerbName(String herbName) { this.herbName = herbName; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getIndicator() { return indicator; }
    public void setIndicator(String indicator) { this.indicator = indicator; }
    public String getBaseline() { return baseline; }
    public void setBaseline(String baseline) { this.baseline = baseline; }
    public String getCurrentValue() { return currentValue; }
    public void setCurrentValue(String currentValue) { this.currentValue = currentValue; }
    public String getDifferenceDesc() { return differenceDesc; }
    public void setDifferenceDesc(String differenceDesc) { this.differenceDesc = differenceDesc; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
    public String getAnalystName() { return analystName; }
    public void setAnalystName(String analystName) { this.analystName = analystName; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

