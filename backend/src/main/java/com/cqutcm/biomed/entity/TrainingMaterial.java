package com.cqutcm.biomed.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;

@TableName("training_material")
public class TrainingMaterial {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    @TableField("title")
    private String title;
    @TableField("trainer_name")
    private String trainerName;
    @TableField("course_id")
    private String courseId;
    @TableField("course_title")
    private String courseTitle;
    @TableField("batch_id")
    private String batchId;
    @TableField("herb_name")
    private String herbName;
    @TableField("district")
    private String district;
    @TableField("training_type")
    private String trainingType;
    @TableField("completion_rate")
    private String completionRate;
    @TableField("source_issue")
    private String sourceIssue;
    @TableField("audience")
    private String audience;
    @TableField("tracking")
    private String tracking;
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
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getHerbName() { return herbName; }
    public void setHerbName(String herbName) { this.herbName = herbName; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getTrainingType() { return trainingType; }
    public void setTrainingType(String trainingType) { this.trainingType = trainingType; }
    public String getCompletionRate() { return completionRate; }
    public void setCompletionRate(String completionRate) { this.completionRate = completionRate; }
    public String getSourceIssue() { return sourceIssue; }
    public void setSourceIssue(String sourceIssue) { this.sourceIssue = sourceIssue; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public String getTracking() { return tracking; }
    public void setTracking(String tracking) { this.tracking = tracking; }
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

