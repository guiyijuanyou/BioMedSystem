package com.cqutcm.biomed.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("evaluation_record")
public class EvaluationRecord {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    @TableField("batch_id")
    private String batchId;
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
    @TableField("subject_owner_name")
    private String subjectOwnerName;
    @TableField("evaluator_name")
    private String evaluatorName;
    @TableField("evaluator_role")
    private String evaluatorRole;
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
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
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
    public String getSubjectOwnerName() { return subjectOwnerName; }
    public void setSubjectOwnerName(String subjectOwnerName) { this.subjectOwnerName = subjectOwnerName; }
    public String getEvaluatorName() { return evaluatorName; }
    public void setEvaluatorName(String evaluatorName) { this.evaluatorName = evaluatorName; }
    public String getEvaluatorRole() { return evaluatorRole; }
    public void setEvaluatorRole(String evaluatorRole) { this.evaluatorRole = evaluatorRole; }
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

