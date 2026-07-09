package com.cqutcm.biomed.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class PermissionService {
    private static final String STUDENT_LABEL = "\u5b66\u751f";

    private static final Set<String> STUDENT_CREATABLE = Set.of("growth-records");
    private static final Set<String> STUDENT_EDITABLE = Set.of("growth-records", "projects");
    private static final Set<String> TEACHER_BLOCKED = Set.of("users", "standards");
    private static final Set<String> RESEARCHER_BLOCKED = Set.of("trainings", "users", "standards", "courses");
    private static final Set<String> REVIEW_ONLY_CREATE_BLOCKED = Set.of("teaching-resources");

    public Actor actor(Map<String, Object> payload) {
        return new Actor(
                String.valueOf(payload.getOrDefault("_actorName", "")),
                String.valueOf(payload.getOrDefault("_actorRole", "admin"))
        );
    }

    public Actor actor(String name, String role) {
        return new Actor(name == null ? "" : name, role == null || role.isBlank() ? "admin" : role);
    }

    public void assertCanCreate(String resourceType, Actor actor) {
        if ("admin".equals(actor.role())) {
            if (REVIEW_ONLY_CREATE_BLOCKED.contains(resourceType)) {
                throw new IllegalArgumentException("admin reviews this resource instead of uploading it");
            }
            return;
        }
        if ("student".equals(actor.role()) && !STUDENT_CREATABLE.contains(resourceType)) {
            throw new IllegalArgumentException("student cannot create " + resourceType);
        }
        if ("teacher".equals(actor.role()) && TEACHER_BLOCKED.contains(resourceType)) {
            throw new IllegalArgumentException("teacher cannot create " + resourceType);
        }
        if ("researcher".equals(actor.role()) && RESEARCHER_BLOCKED.contains(resourceType)) {
            throw new IllegalArgumentException("researcher cannot create " + resourceType);
        }
    }

    public void assertCanUpdate(String resourceType, Actor actor) {
        if ("admin".equals(actor.role())) return;
        if ("student".equals(actor.role()) && !STUDENT_EDITABLE.contains(resourceType)) {
            throw new IllegalArgumentException("student cannot edit " + resourceType);
        }
        if ("teacher".equals(actor.role()) && TEACHER_BLOCKED.contains(resourceType)) {
            throw new IllegalArgumentException("teacher cannot edit " + resourceType);
        }
        if ("researcher".equals(actor.role()) && RESEARCHER_BLOCKED.contains(resourceType)) {
            throw new IllegalArgumentException("researcher cannot edit " + resourceType);
        }
    }

    public void assertCanDelete(String resourceType, Actor actor) {
        if ("admin".equals(actor.role())) return;
        if ("student".equals(actor.role())) {
            throw new IllegalArgumentException("student cannot delete records");
        }
        if ("teacher".equals(actor.role()) && TEACHER_BLOCKED.contains(resourceType)) {
            throw new IllegalArgumentException("teacher cannot delete " + resourceType);
        }
        if ("researcher".equals(actor.role()) && RESEARCHER_BLOCKED.contains(resourceType)) {
            throw new IllegalArgumentException("researcher cannot delete " + resourceType);
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
        return role.equals(STUDENT_LABEL) || owner.contains(STUDENT_LABEL);
    }

    public record Actor(String name, String role) {}
}
