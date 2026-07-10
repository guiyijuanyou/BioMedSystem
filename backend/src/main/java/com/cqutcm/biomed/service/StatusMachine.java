package com.cqutcm.biomed.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 统一状态机：草稿 → 待审核 → 已通过/已驳回 → 已发布 → 已归档
 */
public final class StatusMachine {

    public static final String DRAFT = "草稿";
    public static final String PENDING_REVIEW = "待审核";
    public static final String APPROVED = "已通过";
    public static final String REJECTED = "已驳回";
    public static final String PUBLISHED = "已发布";
    public static final String ARCHIVED = "已归档";

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            DRAFT, Set.of(PENDING_REVIEW),
            PENDING_REVIEW, Set.of(APPROVED, REJECTED),
            APPROVED, Set.of(PUBLISHED, REJECTED, ARCHIVED),
            REJECTED, Set.of(DRAFT, PENDING_REVIEW),
            PUBLISHED, Set.of(ARCHIVED),
            ARCHIVED, Set.of()
    );

    /** 普通用户（非管理员）创建时的初始状态 */
    public static final String INITIAL_NON_ADMIN = DRAFT;

    /** 管理员创建时的初始状态 */
    public static final String INITIAL_ADMIN = APPROVED;

    /** 允许学生查看的状态 */
    public static final Set<String> STUDENT_VISIBLE = Set.of(PUBLISHED);

    /** 已审核锁定状态（结果不可再被修改） */
    public static final Set<String> LOCKED_STATUSES = Set.of(APPROVED, PUBLISHED, ARCHIVED);

    /** 获取初始状态 */
    public static String initialStatus(boolean isAdmin) {
        return isAdmin ? INITIAL_ADMIN : INITIAL_NON_ADMIN;
    }

    /** 校验状态流转是否合法 */
    public static void assertTransition(String fromStatus, String toStatus, String resourceLabel) {
        String from = fromStatus == null || fromStatus.isBlank() ? DRAFT : fromStatus;
        Set<String> allowed = ALLOWED_TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(toStatus)) {
            throw new StateConflictException(
                    String.format("%s 不允许从 '%s' 流转到 '%s'", resourceLabel, from, toStatus));
        }
    }

    public static void assertOwnerEditable(String status, String resourceLabel) {
        String current = status == null || status.isBlank() ? DRAFT : status;
        if (!DRAFT.equals(current) && !REJECTED.equals(current)) {
            throw new StateConflictException(resourceLabel + " 在当前状态下不可编辑");
        }
    }

    /** 管理员可执行的审核操作 */
    public static void assertRequireAdmin(String actorRole) {
        if (!"admin".equals(actorRole)) {
            throw new AuthorizationDeniedException("仅管理员可执行审核操作");
        }
    }

    /** 是否处于已审核锁定状态 */
    public static boolean isLocked(String status) {
        return status != null && LOCKED_STATUSES.contains(status);
    }

    /** 学生是否可查看该状态的内容 */
    public static boolean isStudentVisible(String status) {
        return status != null && STUDENT_VISIBLE.contains(status);
    }

    /** 获取审核操作允许的目标状态列表 */
    public static List<String> reviewTargets(String fromStatus) {
        Set<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(fromStatus, Set.of());
        return allowed.stream()
                .filter(s -> APPROVED.equals(s) || REJECTED.equals(s) || PUBLISHED.equals(s))
                .toList();
    }

    private StatusMachine() {}
}
