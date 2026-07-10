package com.cqutcm.biomed.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class PermissionService {
    private static final String STUDENT_LABEL = "\u5b66\u751f";

    private static final Set<String> STUDENT_CREATABLE = Set.of("growth-records");
    private static final Set<String> STUDENT_EDITABLE = Set.of("growth-records");
    private static final Set<String> TEACHER_BLOCKED = Set.of("users", "standards");
    private static final Set<String> RESEARCHER_BLOCKED = Set.of("trainings", "users", "standards", "courses");
    private static final Set<String> REVIEW_ONLY_CREATE_BLOCKED = Set.of("teaching-resources");
    private static final Set<String> STUDENT_READABLE = Set.of(
            "herbs", "growth-records", "trace-events", "teaching-resources",
            "courses", "projects", "trainings"
    );

    // ---- 通用权限 ----

    public void assertCanRead(String resourceType, Actor actor) {
        if ("admin".equals(actor.role())) return;
        if ("users".equals(resourceType)) {
            throw new AuthorizationDeniedException("only admin can read users");
        }
        if ("student".equals(actor.role()) && !STUDENT_READABLE.contains(resourceType)) {
            throw new AuthorizationDeniedException("student cannot read " + resourceType);
        }
    }

    public Actor actor(Map<String, Object> payload) {
        return new Actor(
                String.valueOf(payload.getOrDefault("_actorName", "")),
                String.valueOf(payload.getOrDefault("_actorRole", ""))
        );
    }

    public Actor actor(String name, String role) {
        return new Actor(name == null ? "" : name, role == null ? "" : role);
    }

    public boolean isAdmin(Actor actor) {
        return "admin".equals(actor.role());
    }

    public boolean isTeacher(Actor actor) {
        return "teacher".equals(actor.role());
    }

    public boolean isResearcher(Actor actor) {
        return "researcher".equals(actor.role());
    }

    public boolean isStudent(Actor actor) {
        return "student".equals(actor.role());
    }

    public void assertCanCreate(String resourceType, Actor actor) {
        if ("admin".equals(actor.role())) {
            if (REVIEW_ONLY_CREATE_BLOCKED.contains(resourceType)) {
                throw new AuthorizationDeniedException("admin reviews this resource instead of uploading it");
            }
            return;
        }
        if ("student".equals(actor.role()) && !STUDENT_CREATABLE.contains(resourceType)) {
            throw new AuthorizationDeniedException("student cannot create " + resourceType);
        }
        if ("teacher".equals(actor.role()) && TEACHER_BLOCKED.contains(resourceType)) {
            throw new AuthorizationDeniedException("teacher cannot create " + resourceType);
        }
        if ("researcher".equals(actor.role()) && RESEARCHER_BLOCKED.contains(resourceType)) {
            throw new AuthorizationDeniedException("researcher cannot create " + resourceType);
        }
    }

    public void assertCanUpdate(String resourceType, Actor actor) {
        if ("admin".equals(actor.role())) return;
        if ("student".equals(actor.role()) && !STUDENT_EDITABLE.contains(resourceType)) {
            throw new AuthorizationDeniedException("student cannot edit " + resourceType);
        }
        if ("teacher".equals(actor.role()) && TEACHER_BLOCKED.contains(resourceType)) {
            throw new AuthorizationDeniedException("teacher cannot edit " + resourceType);
        }
        if ("researcher".equals(actor.role()) && RESEARCHER_BLOCKED.contains(resourceType)) {
            throw new AuthorizationDeniedException("researcher cannot edit " + resourceType);
        }
    }

    public void assertCanDelete(String resourceType, Actor actor) {
        if ("admin".equals(actor.role())) return;
        if ("student".equals(actor.role())) {
            throw new AuthorizationDeniedException("student cannot delete records");
        }
        if ("teacher".equals(actor.role()) && TEACHER_BLOCKED.contains(resourceType)) {
            throw new AuthorizationDeniedException("teacher cannot delete " + resourceType);
        }
        if ("researcher".equals(actor.role()) && RESEARCHER_BLOCKED.contains(resourceType)) {
            throw new AuthorizationDeniedException("researcher cannot delete " + resourceType);
        }
    }

    // ---- 模块级所有权校验 ----

    /** 课程：教师仅可编辑自身课程 */
    public void assertCourseOwnership(Actor actor, String ownerField, String label) {
        if (isAdmin(actor)) return;
        if (!actor.name().equals(ownerField)) {
            throw new AuthorizationDeniedException(
                    String.format("%s 仅可编辑自身的%s", roleLabel(actor.role()), label));
        }
    }

    /** 教学资源：上传者仅可编辑自身资源，管理员仅审核不可伪装上传者 */
    public void assertResourceUploader(Actor actor, String uploaderName) {
        if (isAdmin(actor)) {
            throw new AuthorizationDeniedException("管理员仅负责审核教学资源，不可作为上传者创建");
        }
        // 非管理员强制使用当前用户作为上传者
    }

    /** 图谱比对/数据分析：创建人由登录会话强制确定 */
    public void assertResearchDataOwnership(Actor actor, String operatorField) {
        if (isAdmin(actor)) return;
        if (!isTeacher(actor) && !isResearcher(actor)) {
            throw new AuthorizationDeniedException("仅教师和科研人员可操作图谱与分析数据");
        }
        if (!actor.name().equals(operatorField)) {
            throw new AuthorizationDeniedException("仅可修改自身创建的图谱与分析记录");
        }
    }

    /** 培训：培训负责人仅维护自身培训 */
    public void assertTrainingOwnership(Actor actor, String trainerField) {
        if (isAdmin(actor)) return;
        if (!isTeacher(actor) && !isResearcher(actor)) {
            throw new AuthorizationDeniedException("仅教师和科研人员可维护培训材料");
        }
        if (!actor.name().equals(trainerField)) {
            throw new AuthorizationDeniedException("仅可维护自身的培训材料");
        }
    }

    public void assertEvaluationOwnership(Actor actor, String evaluatorName) {
        if (isAdmin(actor)) return;
        if (!actor.name().equals(evaluatorName)) {
            throw new AuthorizationDeniedException("仅评价记录的创建人可以修改或删除");
        }
    }

    public void assertAchievementOwnership(Actor actor, String ownerName) {
        if (isAdmin(actor)) return;
        if (!actor.name().equals(ownerName)) {
            throw new AuthorizationDeniedException("仅业绩申报人可以修改或删除");
        }
    }

    /** 评价：评价人员不可评价自身负责成果 */
    public void assertEvaluationNotSelf(Actor actor, String ownerField) {
        if (isAdmin(actor)) return;
        if (actor.name().equals(ownerField)) {
            throw new AuthorizationDeniedException("评价人员不可评价自身负责的成果");
        }
    }

    /** 业绩：所属人不可自行认定级别 */
    public void assertAchievementNotSelfCertify(Actor actor, String ownerField) {
        if (isAdmin(actor)) return;
        if (actor.name().equals(ownerField)) {
            throw new AuthorizationDeniedException("业绩所属人不可自行认定级别/分类");
        }
    }

    /** 审核操作：审计字段仅管理员可设置 */
    public void assertReviewFieldsProtected(Actor actor) {
        if (!isAdmin(actor)) {
            throw new AuthorizationDeniedException("审核字段(status/reviewComment/reviewedAt)仅管理员可设置");
        }
    }

    /** 通用：伪造 owner/status/reviewer 等字段一律无效，直接拒绝 */
    public void assertNoFieldForgery(Map<String, Object> payload, Actor actor, String... protectedFields) {
        if (isAdmin(actor)) return;
        for (String field : protectedFields) {
            if (payload.containsKey(field)) {
                throw new AuthorizationDeniedException(
                        String.format("字段 '%s' 不允许由当前角色直接提交", field));
            }
        }
    }

    public boolean canEditOwnedRecord(Map<String, Object> record, Actor actor, String ownerField, String ownerRoleField) {
        if ("admin".equals(actor.role())) return true;
        if ("student".equals(actor.role())) return isOwn(record, actor, ownerField);
        if ("teacher".equals(actor.role()) || "researcher".equals(actor.role())) {
            return isOwn(record, actor, ownerField) || isStudentRecord(record, ownerField, ownerRoleField);
        }
        return false;
    }

    public boolean canDeleteOwnedRecord(Map<String, Object> record, Actor actor, String ownerField) {
        if ("admin".equals(actor.role())) return true;
        return isOwn(record, actor, ownerField);
    }

    public String roleLabel(String role) {
        return switch (role) {
            case "student" -> STUDENT_LABEL;
            case "teacher" -> "\u6559\u5e08";
            case "researcher" -> "\u79d1\u7814\u4eba\u5458";
            case "admin" -> "\u7ba1\u7406\u5458";
            default -> role;
        };
    }

    private boolean isOwn(Map<String, Object> record, Actor actor, String ownerField) {
        return String.valueOf(record.getOrDefault(ownerField, "")).equals(actor.name());
    }

    private boolean isStudentRecord(Map<String, Object> record, String ownerField, String ownerRoleField) {
        String role = String.valueOf(record.getOrDefault(ownerRoleField, ""));
        String owner = String.valueOf(record.getOrDefault(ownerField, ""));
        return role.equals(STUDENT_LABEL) || role.equals("student") || owner.contains(STUDENT_LABEL);
    }

    public record Actor(String name, String role) {}
}
