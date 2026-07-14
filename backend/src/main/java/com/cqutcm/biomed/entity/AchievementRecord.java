package com.cqutcm.biomed.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;

@TableName("achievement_record")
public class AchievementRecord {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    @TableField("title")
    private String title;
    @TableField("owner_name")
    private String ownerName;
    @TableField("source_module")
    private String sourceModule;
    @TableField("batch_id")
    private String batchId;
    @TableField("herb_name")
    private String herbName;
    @TableField("project_title")
    private String projectTitle;
    @TableField("standard_id")
    private String standardId;
    @TableField("category")
    private String category;
    @TableField("level_name")
    private String levelName;
    @TableField("score")
    private String score;
    @TableField("evidence")
    private String evidence;
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
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getHerbName() { return herbName; }
    public void setHerbName(String herbName) { this.herbName = herbName; }
    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
    public String getStandardId() { return standardId; }
    public void setStandardId(String standardId) { this.standardId = standardId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
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

