package com.cqutcm.biomed.entity;

import java.time.LocalDateTime;

public class ResearchProject {
    private String id;
    private String title;
    private String leaderName;
    private String requirements;
    private String status;
    private String stage;
    private String transformation;
    private String applicantRequests;
    private String approvedMembers;
    private String rejectedApplicants;
    private LocalDateTime createdAt;
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
