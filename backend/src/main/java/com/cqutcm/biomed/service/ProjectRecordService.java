package com.cqutcm.biomed.service;

import com.cqutcm.biomed.entity.ProjectApplication;
import com.cqutcm.biomed.entity.ProjectMember;
import com.cqutcm.biomed.entity.ResearchProject;
import com.cqutcm.biomed.mapper.ProjectApplicationMapper;
import com.cqutcm.biomed.mapper.ProjectMemberMapper;
import com.cqutcm.biomed.mapper.ResearchProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProjectRecordService {
    private static final String PENDING_REVIEW = StatusMachine.PENDING_REVIEW;
    private static final String PENDING_APPROVAL = "待审批";
    private static final String APPROVED = StatusMachine.APPROVED;
    private static final String REJECTED = StatusMachine.REJECTED;

    private final ResearchProjectMapper projectMapper;
    private final ProjectApplicationMapper applicationMapper;
    private final ProjectMemberMapper memberMapper;
    private final PermissionService permissionService;

    public ProjectRecordService(ResearchProjectMapper projectMapper,
                                ProjectApplicationMapper applicationMapper,
                                ProjectMemberMapper memberMapper,
                                PermissionService permissionService) {
        this.projectMapper = projectMapper;
        this.applicationMapper = applicationMapper;
        this.memberMapper = memberMapper;
        this.permissionService = permissionService;
    }

    public List<Map<String, Object>> list(PermissionService.Actor actor) {
        return projectMapper.findAllAsMap().stream()
                .filter(project -> !permissionService.isStudent(actor)
                        || StatusMachine.isStudentVisible(text(project.get("status"))))
                .map(project -> decorate(project, actor)).toList();
    }

    public Map<String, Object> create(Map<String, Object> payload) {
        PermissionService.Actor actor = permissionService.actor(payload);
        Map<String, Object> cleaned = clean(payload);
        requireText(cleaned, "title", "课题名称不能为空");
        if (!"admin".equals(actor.role())) {
            cleaned.put("leader", actor.name());
            cleaned.put("status", StatusMachine.DRAFT);
        } else if (text(cleaned.get("status")).isBlank() || PENDING_REVIEW.equals(cleaned.get("status"))) {
            cleaned.put("status", StatusMachine.APPROVED);
        }
        clearWorkflowFields(cleaned);
        String id = UUID.randomUUID().toString();
        cleaned.put("id", id);
        cleaned.put("createdAt", LocalDateTime.now().toString());
        projectMapper.insertMap(cleaned);
        return decorate(cleaned, actor);
    }

    public Map<String, Object> update(Map<String, Object> payload) {
        String id = id(payload);
        PermissionService.Actor actor = permissionService.actor(payload);
        Map<String, Object> existing = existingProject(id);
        if ("student".equals(actor.role())) {
            throw new AuthorizationDeniedException("students must use the project application endpoint");
        }
        if (!"admin".equals(actor.role())) {
            requireProjectOwner(existing, actor);
            StatusMachine.assertOwnerEditable(text(existing.get("status")), "研究课题");
        }

        Map<String, Object> cleaned = clean(payload);
        requireText(cleaned, "title", "课题名称不能为空");
        preserveWorkflowFields(cleaned, existing);
        cleaned.put("leader", existing.get("leader"));
        cleaned.put("status", existing.get("status"));
        cleaned.put("version", existing.getOrDefault("version", 0));
        cleaned.put("id", id);
        if (projectMapper.updateMap(cleaned) == 0) throw conflict();
        cleaned.put("version", ((Number) cleaned.get("version")).intValue() + 1);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return decorate(cleaned, actor);
    }

    @Transactional
    public void delete(String id, PermissionService.Actor actor) {
        Map<String, Object> existing = existingProject(id);
        if (!"admin".equals(actor.role())) {
            requireProjectOwner(existing, actor);
            StatusMachine.assertOwnerEditable(text(existing.get("status")), "研究课题");
        }
        applicationMapper.deleteByProjectId(id);
        memberMapper.deleteByProjectId(id);
        if (projectMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("project not found");
        }
    }

    @Transactional
    public Map<String, Object> apply(String projectId, PermissionService.Actor actor, Map<String, Object> payload) {
        if (!"student".equals(actor.role())) {
            throw new AuthorizationDeniedException("only students can apply to projects");
        }
        Map<String, Object> project = existingProject(projectId);
        if (!isPublished(project.get("status"))) {
            throw new IllegalArgumentException("project is not published");
        }
        boolean alreadyMember = memberMapper.findByProjectId(projectId).stream()
                .anyMatch(member -> actor.name().equals(member.getMemberName()));
        boolean alreadyApplied = applicationMapper.findByProjectId(projectId).stream()
                .anyMatch(application -> actor.name().equals(application.getStudentName()));
        if (alreadyMember || alreadyApplied) {
            throw new IllegalArgumentException("project application already exists");
        }

        ProjectApplication application = new ProjectApplication();
        application.setId(UUID.randomUUID().toString());
        application.setProjectId(projectId);
        application.setStudentName(actor.name());
        application.setStatus(PENDING_APPROVAL);
        application.setApplyReason(text(payload.get("applyReason")));
        application.setReviewComment("");
        application.setAppliedAt(LocalDateTime.now());
        applicationMapper.insert(application);
        return applicationToMap(application);
    }

    @Transactional
    public Map<String, Object> reviewApplication(String projectId, String applicationId,
                                                 PermissionService.Actor actor, Map<String, Object> payload) {
        Map<String, Object> project = existingProject(projectId);
        if (!"admin".equals(actor.role())) {
            requireProjectOwner(project, actor);
        }
        ProjectApplication application = applicationMapper.findById(applicationId);
        if (application == null || !projectId.equals(application.getProjectId())) {
            throw new IllegalArgumentException("project application not found");
        }
        if (!PENDING_APPROVAL.equals(application.getStatus())) {
            throw new IllegalArgumentException("project application has already been reviewed");
        }

        String action = text(payload.get("action"));
        if (!"approve".equals(action) && !"reject".equals(action)) {
            throw new IllegalArgumentException("action must be approve or reject");
        }
        application.setStatus("approve".equals(action) ? APPROVED : REJECTED);
        application.setReviewComment(text(payload.get("reviewComment")));
        application.setReviewedAt(LocalDateTime.now());
        applicationMapper.update(application);

        if ("approve".equals(action)) {
            boolean exists = memberMapper.findByProjectId(projectId).stream()
                    .anyMatch(member -> application.getStudentName().equals(member.getMemberName()));
            if (!exists) {
                ProjectMember member = new ProjectMember();
                member.setId(UUID.randomUUID().toString());
                member.setProjectId(projectId);
                member.setMemberName(application.getStudentName());
                member.setMemberRole("student");
                member.setJoinedAt(LocalDateTime.now());
                memberMapper.insert(member);
            }
        }
        return applicationToMap(application);
    }

    public long count() {
        return projectMapper.count();
    }

    public Map<String, Object> submit(String id, PermissionService.Actor actor) {
        ResearchProject project = requiredProject(id);
        requireProjectOwner(entityToMap(project), actor);
        StatusMachine.assertTransition(project.getStatus(), StatusMachine.PENDING_REVIEW, "研究课题");
        project.setStatus(StatusMachine.PENDING_REVIEW);
        if (projectMapper.update(project) == 0) throw conflict();
        return decorate(entityToMap(project), actor);
    }

    public Map<String, Object> review(String id, PermissionService.Actor actor,
                                      String targetStatus, String comment) {
        StatusMachine.assertRequireAdmin(actor.role());
        ResearchProject project = requiredProject(id);
        StatusMachine.assertTransition(project.getStatus(), targetStatus, "研究课题");
        project.setStatus(targetStatus);
        project.setReviewerName(actor.name());
        project.setReviewComment(comment);
        project.setReviewedAt(LocalDateTime.now());
        if (projectMapper.update(project) == 0) throw conflict();
        return decorate(entityToMap(project), actor);
    }

    private Map<String, Object> decorate(Map<String, Object> source, PermissionService.Actor actor) {
        Map<String, Object> project = new LinkedHashMap<>(source);
        String projectId = text(project.get("id"));
        List<ProjectApplication> allApplications = applicationMapper.findByProjectId(projectId);
        List<ProjectMember> members = memberMapper.findByProjectId(projectId);
        boolean canReview = "admin".equals(actor.role()) || actor.name().equals(text(project.get("leader")));
        List<ProjectApplication> visibleApplications = canReview
                ? allApplications
                : allApplications.stream().filter(a -> actor.name().equals(a.getStudentName())).toList();
        project.put("applications", visibleApplications.stream().map(this::applicationToMap).toList());
        project.put("applicantRequests", joinNames(visibleApplications.stream()
                .filter(a -> PENDING_APPROVAL.equals(a.getStatus())).map(ProjectApplication::getStudentName).toList()));
        project.put("approvedMembers", joinNames(members.stream().map(ProjectMember::getMemberName).toList()));
        project.put("rejectedApplicants", joinNames(visibleApplications.stream()
                .filter(a -> REJECTED.equals(a.getStatus())).map(ProjectApplication::getStudentName).toList()));
        return project;
    }

    private Map<String, Object> applicationToMap(ProjectApplication application) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", application.getId());
        result.put("projectId", application.getProjectId());
        result.put("studentName", application.getStudentName());
        result.put("status", application.getStatus());
        result.put("applyReason", application.getApplyReason());
        result.put("reviewComment", application.getReviewComment());
        result.put("appliedAt", application.getAppliedAt());
        result.put("reviewedAt", application.getReviewedAt());
        return result;
    }

    private Map<String, Object> existingProject(String id) {
        ResearchProject project = projectMapper.findById(id);
        if (project == null) throw new IllegalArgumentException("project not found");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", project.getId());
        result.put("title", project.getTitle());
        result.put("leader", project.getLeaderName());
        result.put("requirements", project.getRequirements());
        result.put("status", project.getStatus());
        result.put("reviewerName", project.getReviewerName());
        result.put("reviewComment", project.getReviewComment());
        result.put("reviewedAt", project.getReviewedAt());
        result.put("stage", project.getStage());
        result.put("transformation", project.getTransformation());
        result.put("applicantRequests", project.getApplicantRequests());
        result.put("approvedMembers", project.getApprovedMembers());
        result.put("rejectedApplicants", project.getRejectedApplicants());
        result.put("createdAt", project.getCreatedAt());
        result.put("updatedAt", project.getUpdatedAt());
        result.put("version", project.getVersion());
        return result;
    }

    private ResearchProject requiredProject(String id) {
        ResearchProject project = projectMapper.findById(id);
        if (project == null) throw new IllegalArgumentException("project not found");
        return project;
    }

    private Map<String, Object> entityToMap(ResearchProject project) {
        return existingProject(project.getId());
    }

    private StateConflictException conflict() {
        return new StateConflictException("研究课题已被其他用户修改，请刷新后重试");
    }

    private void requireProjectOwner(Map<String, Object> project, PermissionService.Actor actor) {
        if (!("teacher".equals(actor.role()) || "researcher".equals(actor.role()))
                || !actor.name().equals(text(project.get("leader")))) {
            throw new AuthorizationDeniedException("only the project leader or admin can perform this action");
        }
    }

    private Map<String, Object> clean(Map<String, Object> payload) {
        Map<String, Object> cleaned = new LinkedHashMap<>(payload);
        cleaned.remove("id");
        cleaned.remove("createdAt");
        cleaned.remove("updatedAt");
        cleaned.remove("applications");
        cleaned.remove("_actorName");
        cleaned.remove("_actorRole");
        cleaned.putIfAbsent("status", PENDING_REVIEW);
        cleaned.putIfAbsent("requirements", "");
        cleaned.putIfAbsent("stage", "");
        cleaned.putIfAbsent("transformation", "");
        return cleaned;
    }

    private void clearWorkflowFields(Map<String, Object> project) {
        project.put("applicantRequests", "");
        project.put("approvedMembers", "");
        project.put("rejectedApplicants", "");
    }

    private void preserveWorkflowFields(Map<String, Object> target, Map<String, Object> existing) {
        target.put("applicantRequests", existing.getOrDefault("applicantRequests", ""));
        target.put("approvedMembers", existing.getOrDefault("approvedMembers", ""));
        target.put("rejectedApplicants", existing.getOrDefault("rejectedApplicants", ""));
    }

    private String id(Map<String, Object> payload) {
        String id = text(payload.get("id"));
        if (id.isBlank()) throw new IllegalArgumentException("missing id for project update");
        return id;
    }

    private void requireText(Map<String, Object> payload, String key, String message) {
        if (text(payload.get(key)).isBlank()) throw new IllegalArgumentException(message);
    }

    private boolean isPublished(Object status) {
        String value = text(status);
        return value.contains("已发布") || value.contains("已通过");
    }

    private String joinNames(List<String> names) {
        return names.stream().filter(name -> name != null && !name.isBlank()).distinct()
                .collect(java.util.stream.Collectors.joining("、"));
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
