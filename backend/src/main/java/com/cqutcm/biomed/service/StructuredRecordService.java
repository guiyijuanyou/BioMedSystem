package com.cqutcm.biomed.service;

import com.cqutcm.biomed.entity.*;
import com.cqutcm.biomed.mapper.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StructuredRecordService {

    private static final Set<String> SUPPORTED = Set.of(
            "herbs", "trainings", "evaluations", "achievements", "standards", "users"
    );

    private final HerbMapper herbMapper;
    private final TrainingMaterialMapper trainingMapper;
    private final EvaluationRecordMapper evaluationMapper;
    private final AchievementRecordMapper achievementMapper;
    private final AchievementStandardMapper standardMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionService permissionService;
    private final BatchCatalogService batchCatalogService;

    public StructuredRecordService(HerbMapper herbMapper, TrainingMaterialMapper trainingMapper,
                                   EvaluationRecordMapper evaluationMapper,
                                   AchievementRecordMapper achievementMapper,
                                   AchievementStandardMapper standardMapper,
                                   SysUserMapper userMapper, SysRoleMapper roleMapper,
                                   SysUserRoleMapper userRoleMapper,
                                   PasswordEncoder passwordEncoder,
                                   PermissionService permissionService,
                                   BatchCatalogService batchCatalogService) {
        this.herbMapper = herbMapper;
        this.trainingMapper = trainingMapper;
        this.evaluationMapper = evaluationMapper;
        this.achievementMapper = achievementMapper;
        this.standardMapper = standardMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.permissionService = permissionService;
        this.batchCatalogService = batchCatalogService;
    }

    public boolean supports(String resourceType) {
        return SUPPORTED.contains(resourceType);
    }

    public List<Map<String, Object>> list(String resourceType) {
        return switch (resourceType) {
            case "herbs" -> herbMapper.findAll().stream().map(this::herbToMap).toList();
            case "trainings" -> trainingMapper.findAll().stream().map(this::trainingToMap).toList();
            case "evaluations" -> evaluationMapper.findAll().stream().map(this::evaluationToMap).toList();
            case "achievements" -> achievementMapper.findAll().stream().map(this::achievementToMap).toList();
            case "standards" -> standardMapper.findAll().stream().map(this::standardToMap).toList();
            case "users" -> userMapper.findAllAsMap().stream().map(this::sanitizeUserMap).toList();
            default -> List.of();
        };
    }

    public List<Map<String, Object>> list(String resourceType, PermissionService.Actor actor) {
        List<Map<String, Object>> all = list(resourceType);
        if (!(resourceType.equals("trainings") || resourceType.equals("evaluations") || resourceType.equals("achievements"))) {
            return all;
        }
        if (permissionService.isAdmin(actor)) return all;
        String ownerField = switch (resourceType) {
            case "trainings" -> "trainer";
            case "evaluations" -> "evaluator";
            default -> "owner";
        };
        return all.stream().filter(item -> StatusMachine.isStudentVisible(str(item, "status"))
                || actor.name().equals(str(item, ownerField))).toList();
    }

    // ---- 创建（含所有权） ----

    public Map<String, Object> create(String resourceType, Map<String, Object> payload) {
        // 从 payload 中提取 actor 信息（由 controller 注入）
        String actorName = String.valueOf(payload.getOrDefault("_actorName", ""));
        String actorRole = String.valueOf(payload.getOrDefault("_actorRole", ""));
        PermissionService.Actor actor = new PermissionService.Actor(actorName, actorRole);

        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        switch (resourceType) {
            case "herbs": {
                Herb h = mapToHerb(payload); h.setId(id); h.setCreatedAt(now);
                herbMapper.insert(h); return herbToMap(h);
            }
            case "trainings": {
                TrainingMaterial t = mapToTraining(payload, actor); t.setId(id); t.setCreatedAt(now);
                trainingMapper.insert(t); return trainingToMap(t);
            }
            case "evaluations": {
                batchCatalogService.applyBatchContext(payload);
                EvaluationRecord e = mapToEvaluation(payload, actor); e.setId(id); e.setCreatedAt(now);
                requireEvaluationSubject(e, actor);
                evaluationMapper.insert(e); return evaluationToMap(e);
            }
            case "achievements": {
                AchievementRecord a = mapToAchievement(payload, actor); a.setId(id); a.setCreatedAt(now);
                achievementMapper.insert(a); return achievementToMap(a);
            }
            case "standards": {
                AchievementStandard s = mapToStandard(payload); s.setId(id); s.setCreatedAt(now);
                standardMapper.insert(s); return standardToMap(s);
            }
            case "users": {
                SysUser u = mapToUser(payload); u.setId(id); u.setCreatedAt(now);
                userMapper.insert(u);
                handleUserRoles(payload, id);
                return userToMap(u);
            }
            default: throw new IllegalArgumentException("unsupported resource type: " + resourceType);
        }
    }

    // ---- 更新（含所有权与审核保护） ----

    public Map<String, Object> update(String resourceType, Map<String, Object> payload) {
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) throw new IllegalArgumentException("missing id for update");

        String actorName = String.valueOf(payload.getOrDefault("_actorName", ""));
        String actorRole = String.valueOf(payload.getOrDefault("_actorRole", ""));
        PermissionService.Actor actor = new PermissionService.Actor(actorName, actorRole);
        switch (resourceType) {
            case "herbs": {
                Herb h = mapToHerb(payload); h.setId(id);
                herbMapper.update(h); return herbToMap(h);
            }
            case "trainings": {
                TrainingMaterial existing = trainingMapper.findById(id);
                if (existing == null) throw new IllegalArgumentException("training material not found");
                // 培训负责人仅维护自身培训
                permissionService.assertTrainingOwnership(actor, existing.getTrainerName());
                StatusMachine.assertOwnerEditable(existing.getStatus(), "培训素材");
                TrainingMaterial t = mapToTraining(payload, actor);
                t.setId(id);
                preserveTrainingReviewFields(t, existing, actor);
                t.setVersion(existing.getVersion());
                if (trainingMapper.update(t) == 0) throw conflict("培训素材");
                return trainingToMap(t);
            }
            case "evaluations": {
                EvaluationRecord existing = evaluationMapper.findById(id);
                if (existing == null) throw new IllegalArgumentException("evaluation record not found");
                permissionService.assertEvaluationOwnership(actor, existing.getEvaluatorName());
                StatusMachine.assertOwnerEditable(existing.getStatus(), "评价记录");
                payload.putIfAbsent("batchId", existing.getBatchId());
                batchCatalogService.applyBatchContext(payload);
                EvaluationRecord e = mapToEvaluation(payload, actor);
                e.setId(id);
                requireEvaluationSubject(e, actor);
                preserveEvaluationFields(e, existing, actor);
                e.setVersion(existing.getVersion());
                if (evaluationMapper.update(e) == 0) throw conflict("评价记录");
                return evaluationToMap(e);
            }
            case "achievements": {
                AchievementRecord existing = achievementMapper.findById(id);
                if (existing == null) throw new IllegalArgumentException("achievement record not found");
                if (!permissionService.isAdmin(actor)) {
                    permissionService.assertAchievementOwnership(actor, existing.getOwnerName());
                    StatusMachine.assertOwnerEditable(existing.getStatus(), "业绩记录");
                }
                AchievementRecord a = mapToAchievement(payload, actor);
                a.setId(id);
                preserveAchievementFields(a, existing, actor);
                a.setVersion(existing.getVersion());
                if (achievementMapper.update(a) == 0) throw conflict("业绩记录");
                return achievementToMap(a);
            }
            case "standards": {
                AchievementStandard s = mapToStandard(payload); s.setId(id);
                standardMapper.update(s); return standardToMap(s);
            }
            case "users": {
                SysUser existing = userMapper.findById(id);
                if (existing == null) throw new IllegalArgumentException("user not found");
                SysUser u = mapToUser(payload); u.setId(id);
                if (u.getPasswordHash() == null || u.getPasswordHash().isBlank()) {
                    u.setPasswordHash(existing.getPasswordHash());
                }
                userMapper.update(u);
                handleUserRoles(payload, id);
                return userToMap(u);
            }
            default: throw new IllegalArgumentException("unsupported resource type: " + resourceType);
        }
    }

    // ---- 删除 ----

    public void delete(String resourceType, String id, PermissionService.Actor actor) {
        switch (resourceType) {
            case "herbs" -> herbMapper.deleteById(id);
            case "trainings" -> {
                TrainingMaterial record = trainingMapper.findById(id);
                if (record == null) throw new IllegalArgumentException("training material not found");
                permissionService.assertTrainingOwnership(actor, record.getTrainerName());
                if (!permissionService.isAdmin(actor)) StatusMachine.assertOwnerEditable(record.getStatus(), "培训素材");
                trainingMapper.deleteById(id);
            }
            case "evaluations" -> {
                EvaluationRecord record = evaluationMapper.findById(id);
                if (record == null) throw new IllegalArgumentException("evaluation record not found");
                permissionService.assertEvaluationOwnership(actor, record.getEvaluatorName());
                if (!permissionService.isAdmin(actor)) StatusMachine.assertOwnerEditable(record.getStatus(), "评价记录");
                evaluationMapper.deleteById(id);
            }
            case "achievements" -> {
                AchievementRecord record = achievementMapper.findById(id);
                if (record == null) throw new IllegalArgumentException("achievement record not found");
                permissionService.assertAchievementOwnership(actor, record.getOwnerName());
                if (!permissionService.isAdmin(actor)) StatusMachine.assertOwnerEditable(record.getStatus(), "业绩记录");
                achievementMapper.deleteById(id);
            }
            case "standards" -> standardMapper.deleteById(id);
            case "users" -> { userRoleMapper.deleteByUserId(id); userMapper.deleteById(id); }
        }
    }

    public Map<String, Object> submit(String resourceType, String id, PermissionService.Actor actor) {
        return switch (resourceType) {
            case "trainings" -> {
                TrainingMaterial record = requiredTraining(id);
                permissionService.assertTrainingOwnership(actor, record.getTrainerName());
                StatusMachine.assertTransition(record.getStatus(), StatusMachine.PENDING_REVIEW, "培训素材");
                record.setStatus(StatusMachine.PENDING_REVIEW);
                if (trainingMapper.update(record) == 0) throw conflict("培训素材");
                yield trainingToMap(record);
            }
            case "evaluations" -> {
                EvaluationRecord record = requiredEvaluation(id);
                permissionService.assertEvaluationOwnership(actor, record.getEvaluatorName());
                StatusMachine.assertTransition(record.getStatus(), StatusMachine.PENDING_REVIEW, "评价记录");
                record.setStatus(StatusMachine.PENDING_REVIEW);
                if (evaluationMapper.update(record) == 0) throw conflict("评价记录");
                yield evaluationToMap(record);
            }
            case "achievements" -> {
                AchievementRecord record = requiredAchievement(id);
                permissionService.assertAchievementOwnership(actor, record.getOwnerName());
                StatusMachine.assertTransition(record.getStatus(), StatusMachine.PENDING_REVIEW, "业绩记录");
                record.setStatus(StatusMachine.PENDING_REVIEW);
                if (achievementMapper.update(record) == 0) throw conflict("业绩记录");
                yield achievementToMap(record);
            }
            default -> throw new IllegalArgumentException("resource type does not support submission");
        };
    }

    public Map<String, Object> review(String resourceType, String id, PermissionService.Actor actor,
                                      String targetStatus, String comment, Map<String, Object> payload) {
        StatusMachine.assertRequireAdmin(actor.role());
        LocalDateTime now = LocalDateTime.now();
        return switch (resourceType) {
            case "trainings" -> {
                TrainingMaterial record = requiredTraining(id);
                StatusMachine.assertTransition(record.getStatus(), targetStatus, "培训素材");
                record.setStatus(targetStatus); record.setReviewerName(actor.name());
                record.setReviewComment(comment); record.setReviewedAt(now);
                if (trainingMapper.update(record) == 0) throw conflict("培训素材");
                yield trainingToMap(record);
            }
            case "evaluations" -> {
                EvaluationRecord record = requiredEvaluation(id);
                StatusMachine.assertTransition(record.getStatus(), targetStatus, "评价记录");
                record.setStatus(targetStatus); record.setReviewerName(actor.name());
                record.setReviewComment(comment); record.setReviewedAt(now);
                if (evaluationMapper.update(record) == 0) throw conflict("评价记录");
                yield evaluationToMap(record);
            }
            case "achievements" -> {
                AchievementRecord record = requiredAchievement(id);
                StatusMachine.assertTransition(record.getStatus(), targetStatus, "业绩记录");
                if (payload.containsKey("category")) record.setCategory(str(payload, "category"));
                if (payload.containsKey("level")) record.setLevelName(str(payload, "level"));
                if (StatusMachine.APPROVED.equals(targetStatus)
                        && (record.getCategory() == null || record.getCategory().isBlank()
                        || record.getLevelName() == null || record.getLevelName().isBlank())) {
                    throw new IllegalArgumentException("通过业绩审核前必须确定分类和级别");
                }
                record.setStatus(targetStatus); record.setReviewerName(actor.name());
                record.setReviewComment(comment); record.setReviewedAt(now);
                if (achievementMapper.update(record) == 0) throw conflict("业绩记录");
                yield achievementToMap(record);
            }
            default -> throw new IllegalArgumentException("resource type does not support review");
        };
    }

    public long count(String resourceType) {
        return switch (resourceType) {
            case "herbs" -> herbMapper.count();
            case "trainings" -> trainingMapper.count();
            case "evaluations" -> evaluationMapper.count();
            case "achievements" -> achievementMapper.count();
            case "standards" -> standardMapper.count();
            default -> 0;
        };
    }

    // ---- 字段保护方法 ----

    private void preserveTrainingReviewFields(TrainingMaterial target, TrainingMaterial existing,
                                               PermissionService.Actor actor) {
        target.setStatus(existing.getStatus());
        target.setReviewerName(existing.getReviewerName());
        target.setReviewComment(existing.getReviewComment());
        target.setReviewedAt(existing.getReviewedAt());
        if (!permissionService.isAdmin(actor)) {
            target.setTrainerName(existing.getTrainerName()); // 保持原负责人
        }
    }

    private void preserveEvaluationFields(EvaluationRecord target, EvaluationRecord existing,
                                           PermissionService.Actor actor) {
        target.setStatus(existing.getStatus());
        target.setReviewerName(existing.getReviewerName());
        target.setReviewComment(existing.getReviewComment());
        target.setReviewedAt(existing.getReviewedAt());
        if (!permissionService.isAdmin(actor)) {
            target.setEvaluatorName(existing.getEvaluatorName());
            target.setEvaluatorRole(existing.getEvaluatorRole());
        }
    }

    private void preserveAchievementFields(AchievementRecord target, AchievementRecord existing,
                                            PermissionService.Actor actor) {
        target.setStatus(existing.getStatus());
        target.setReviewerName(existing.getReviewerName());
        target.setReviewComment(existing.getReviewComment());
        target.setReviewedAt(existing.getReviewedAt());
        if (!permissionService.isAdmin(actor)) {
            target.setOwnerName(existing.getOwnerName());
        }
    }

    // ---- Map-to-Entity converters ----

    private Herb mapToHerb(Map<String, Object> m) {
        Herb h = new Herb();
        h.setName(str(m, "name")); h.setDistrict(str(m, "district"));
        h.setLongitude(toBigDecimal(m, "longitude")); h.setLatitude(toBigDecimal(m, "latitude"));
        h.setScaleDesc(str(m, "scale")); h.setEnvironment(str(m, "environment"));
        h.setTraceCode(str(m, "traceCode"));
        return h;
    }

    private TrainingMaterial mapToTraining(Map<String, Object> m, PermissionService.Actor actor) {
        TrainingMaterial t = new TrainingMaterial();
        t.setTitle(str(m, "title"));
        t.setAudience(str(m, "audience"));
        t.setTracking(str(m, "tracking"));
        if (permissionService.isAdmin(actor)) {
            t.setTrainerName(str(m, "trainer"));
            String status = str(m, "status");
            t.setStatus(status == null || status.isBlank() ? StatusMachine.initialStatus(true) : status);
            t.setReviewComment(str(m, "reviewComment"));
        } else {
            // 培训负责人由会话强制确定
            t.setTrainerName(actor.name());
            t.setStatus(StatusMachine.initialStatus(false));
        }
        return t;
    }

    private EvaluationRecord mapToEvaluation(Map<String, Object> m, PermissionService.Actor actor) {
        EvaluationRecord e = new EvaluationRecord();
        e.setBatchId(str(m, "batchId"));
        e.setHerbName(str(m, "herbName")); e.setIndicator(str(m, "indicator"));
        e.setScore(toBigDecimal(m, "score")); e.setResult(str(m, "result"));
        e.setApplicationMaterial(str(m, "applicationMaterial"));
        e.setSubjectOwnerName(str(m, "subjectOwner"));
        if (permissionService.isAdmin(actor)) {
            e.setEvaluatorName(str(m, "evaluator"));
            e.setEvaluatorRole(str(m, "evaluatorRole"));
            String status = str(m, "status");
            e.setStatus(status == null || status.isBlank() ? StatusMachine.initialStatus(true) : status);
            e.setReviewComment(str(m, "reviewComment"));
        } else {
            // 评价人由会话强制确定
            e.setEvaluatorName(actor.name());
            e.setEvaluatorRole(permissionService.roleLabel(actor.role()));
            e.setStatus(StatusMachine.initialStatus(false));
        }
        return e;
    }

    private AchievementRecord mapToAchievement(Map<String, Object> m, PermissionService.Actor actor) {
        AchievementRecord a = new AchievementRecord();
        a.setTitle(str(m, "title"));
        if (permissionService.isAdmin(actor)) {
            a.setOwnerName(str(m, "owner"));
            a.setCategory(str(m, "category"));
            a.setLevelName(str(m, "level"));
            String status = str(m, "status");
            a.setStatus(status == null || status.isBlank() ? StatusMachine.initialStatus(true) : status);
            a.setReviewComment(str(m, "reviewComment"));
        } else {
            // 所属人不可自行认定级别/分类
            a.setOwnerName(actor.name());
            a.setStatus(StatusMachine.initialStatus(false));
            // 分类/级别留空，由管理员或规则引擎确认
        }
        return a;
    }

    private AchievementStandard mapToStandard(Map<String, Object> m) {
        AchievementStandard s = new AchievementStandard();
        s.setName(str(m, "name")); s.setCategory(str(m, "category"));
        s.setLevelRule(str(m, "levelRule"));
        String date = str(m, "effectiveDate");
        if (date != null && !date.isBlank()) {
            try { s.setEffectiveDate(LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)); }
            catch (Exception ignored) {}
        }
        return s;
    }

    private SysUser mapToUser(Map<String, Object> m) {
        SysUser u = new SysUser();
        u.setUsername(str(m, "username")); u.setDisplayName(str(m, "name"));
        String rawPassword = str(m, "password");
        if (rawPassword != null && !rawPassword.isBlank() && !rawPassword.startsWith("$2a$")) {
            rawPassword = passwordEncoder.encode(rawPassword);
        }
        u.setPasswordHash(rawPassword); u.setDepartment(str(m, "department"));
        String status = str(m, "status");
        u.setStatus(status != null && !status.isBlank() ? status : "enabled");
        u.setPhone(str(m, "phone"));
        u.setEmail(str(m, "email"));
        u.setAvatarUrl(str(m, "avatarUrl"));
        u.setTitle(str(m, "title"));
        u.setResearchArea(str(m, "researchArea"));
        u.setBio(str(m, "bio"));
        return u;
    }

    // ---- Entity-to-Map converters ----

    private Map<String, Object> herbToMap(Herb h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", h.getId()); m.put("name", h.getName()); m.put("district", h.getDistrict());
        m.put("longitude", h.getLongitude()); m.put("latitude", h.getLatitude());
        m.put("scale", h.getScaleDesc()); m.put("environment", h.getEnvironment());
        m.put("traceCode", h.getTraceCode());
        if (h.getCreatedAt() != null) m.put("createdAt", h.getCreatedAt().toString());
        if (h.getUpdatedAt() != null) m.put("updatedAt", h.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> trainingToMap(TrainingMaterial t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId()); m.put("title", t.getTitle()); m.put("trainer", t.getTrainerName());
        m.put("audience", t.getAudience()); m.put("tracking", t.getTracking());
        m.put("status", t.getStatus());
        m.put("reviewerName", t.getReviewerName());
        m.put("reviewComment", t.getReviewComment());
        if (t.getReviewedAt() != null) m.put("reviewedAt", t.getReviewedAt().toString());
        m.put("version", t.getVersion());
        if (t.getCreatedAt() != null) m.put("createdAt", t.getCreatedAt().toString());
        if (t.getUpdatedAt() != null) m.put("updatedAt", t.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> evaluationToMap(EvaluationRecord e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId()); m.put("batchId", e.getBatchId());
        m.put("herbName", e.getHerbName()); m.put("indicator", e.getIndicator());
        m.put("score", e.getScore()); m.put("result", e.getResult());
        m.put("applicationMaterial", e.getApplicationMaterial());
        m.put("subjectOwner", e.getSubjectOwnerName());
        m.put("evaluator", e.getEvaluatorName());
        m.put("evaluatorRole", e.getEvaluatorRole());
        m.put("status", e.getStatus());
        m.put("reviewerName", e.getReviewerName());
        m.put("reviewComment", e.getReviewComment());
        if (e.getReviewedAt() != null) m.put("reviewedAt", e.getReviewedAt().toString());
        m.put("version", e.getVersion());
        if (e.getCreatedAt() != null) m.put("createdAt", e.getCreatedAt().toString());
        if (e.getUpdatedAt() != null) m.put("updatedAt", e.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> achievementToMap(AchievementRecord a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId()); m.put("title", a.getTitle()); m.put("owner", a.getOwnerName());
        m.put("category", a.getCategory()); m.put("level", a.getLevelName());
        m.put("status", a.getStatus());
        m.put("reviewerName", a.getReviewerName());
        m.put("reviewComment", a.getReviewComment());
        if (a.getReviewedAt() != null) m.put("reviewedAt", a.getReviewedAt().toString());
        m.put("version", a.getVersion());
        if (a.getCreatedAt() != null) m.put("createdAt", a.getCreatedAt().toString());
        if (a.getUpdatedAt() != null) m.put("updatedAt", a.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> standardToMap(AchievementStandard s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("name", s.getName()); m.put("category", s.getCategory());
        m.put("levelRule", s.getLevelRule());
        m.put("effectiveDate", s.getEffectiveDate() != null ? s.getEffectiveDate().toString() : "");
        if (s.getCreatedAt() != null) m.put("createdAt", s.getCreatedAt().toString());
        if (s.getUpdatedAt() != null) m.put("updatedAt", s.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> userToMap(SysUser u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId()); m.put("username", u.getUsername()); m.put("name", u.getDisplayName());
        m.put("password", ""); m.put("department", u.getDepartment());
        m.put("status", u.getStatus());
        m.put("phone", u.getPhone());
        m.put("email", u.getEmail());
        m.put("avatarUrl", u.getAvatarUrl());
        m.put("title", u.getTitle());
        m.put("researchArea", u.getResearchArea());
        m.put("bio", u.getBio());
        if (u.getCreatedAt() != null) m.put("createdAt", u.getCreatedAt().toString());
        if (u.getUpdatedAt() != null) m.put("updatedAt", u.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> sanitizeUserMap(Map<String, Object> source) {
        Map<String, Object> sanitized = new LinkedHashMap<>(source);
        sanitized.remove("password");
        sanitized.remove("passwordHash");
        sanitized.put("password", "");
        // 保留资料字段
        sanitized.putIfAbsent("phone", null);
        sanitized.putIfAbsent("email", null);
        sanitized.putIfAbsent("avatarUrl", null);
        sanitized.putIfAbsent("title", null);
        sanitized.putIfAbsent("researchArea", null);
        sanitized.putIfAbsent("bio", null);
        return sanitized;
    }

    private void handleUserRoles(Map<String, Object> payload, String userId) {
        List<String> roleCodes = new ArrayList<>();
        Object rolesObj = payload.get("roles");
        if (rolesObj instanceof List<?> roles) {
            for (Object role : roles) {
                roleCodes.add(String.valueOf(role).trim());
            }
        }
        Object roleObj = payload.get("role");
        if (roleObj instanceof String roleStr && !roleStr.isBlank()) {
            roleCodes.add(roleStr.trim());
        }
        if (roleCodes.isEmpty()) return;
        userRoleMapper.deleteByUserId(userId);
        for (String roleCode : roleCodes) {
            SysRole sysRole = roleMapper.findByCode(roleCode);
            if (sysRole == null) {
                sysRole = roleMapper.findByCode("student");
            }
            if (sysRole != null) {
                userRoleMapper.insert(userId, sysRole.getId());
            }
        }
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private BigDecimal toBigDecimal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd;
        try { return new BigDecimal(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private void requireEvaluationSubject(EvaluationRecord record, PermissionService.Actor actor) {
        if (record.getSubjectOwnerName() == null || record.getSubjectOwnerName().isBlank()) {
            throw new IllegalArgumentException("请填写被评价成果负责人");
        }
        permissionService.assertEvaluationNotSelf(actor, record.getSubjectOwnerName());
    }

    private TrainingMaterial requiredTraining(String id) {
        TrainingMaterial record = trainingMapper.findById(id);
        if (record == null) throw new IllegalArgumentException("training material not found");
        return record;
    }

    private EvaluationRecord requiredEvaluation(String id) {
        EvaluationRecord record = evaluationMapper.findById(id);
        if (record == null) throw new IllegalArgumentException("evaluation record not found");
        return record;
    }

    private AchievementRecord requiredAchievement(String id) {
        AchievementRecord record = achievementMapper.findById(id);
        if (record == null) throw new IllegalArgumentException("achievement record not found");
        return record;
    }

    private StateConflictException conflict(String label) {
        return new StateConflictException(label + "已被其他用户修改，请刷新后重试");
    }
}
