package com.cqutcm.biomed.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("research_project")
public class ResearchProject {
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    @TableField("title")
    private String title;
    @TableField("leader_name")
    private String leaderName;
    @TableField("requirements")
    private String requirements;
    @TableField("status")
    private String status;
    @TableField("stage")
    private String stage;
    @TableField("transformation")
    private String transformation;
    @TableField("applicant_requests")
    private String applicantRequests;
    @TableField("approved_members")
    private String approvedMembers;
    @TableField("rejected_applicants")
    private String rejectedApplicants;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(value = "updated_at", fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getTransformation() { return transformation; }
    public void setTransformation(String transformation) { this.transformation = transformation; }
    public String getApplicantRequests() { return applicantRequests; }
    public void setApplicantRequests(String applicantRequests) { this.applicantRequests = applicantRequests; }
    public String getApprovedMembers() { return approvedMembers; }
    public void setApprovedMembers(String approvedMembers) { this.approvedMembers = approvedMembers; }
    public String getRejectedApplicants() { return rejectedApplicants; }
    public void setRejectedApplicants(String rejectedApplicants) { this.rejectedApplicants = rejectedApplicants; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

