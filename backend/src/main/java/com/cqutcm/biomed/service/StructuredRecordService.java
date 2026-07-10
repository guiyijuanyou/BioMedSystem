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

    public StructuredRecordService(HerbMapper herbMapper, TrainingMaterialMapper trainingMapper,
                                   EvaluationRecordMapper evaluationMapper,
                                   AchievementRecordMapper achievementMapper,
                                   AchievementStandardMapper standardMapper,
                                   SysUserMapper userMapper, SysRoleMapper roleMapper,
                                   SysUserRoleMapper userRoleMapper,
                                   PasswordEncoder passwordEncoder) {
        this.herbMapper = herbMapper;
        this.trainingMapper = trainingMapper;
        this.evaluationMapper = evaluationMapper;
        this.achievementMapper = achievementMapper;
        this.standardMapper = standardMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
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

    public Map<String, Object> create(String resourceType, Map<String, Object> payload) {
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        switch (resourceType) {
            case "herbs": {
                Herb h = mapToHerb(payload); h.setId(id); h.setCreatedAt(now);
                herbMapper.insert(h); return herbToMap(h);
            }
            case "trainings": {
                TrainingMaterial t = mapToTraining(payload); t.setId(id); t.setCreatedAt(now);
                trainingMapper.insert(t); return trainingToMap(t);
            }
            case "evaluations": {
                EvaluationRecord e = mapToEvaluation(payload); e.setId(id); e.setCreatedAt(now);
                evaluationMapper.insert(e); return evaluationToMap(e);
            }
            case "achievements": {
                AchievementRecord a = mapToAchievement(payload); a.setId(id); a.setCreatedAt(now);
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

    public Map<String, Object> update(String resourceType, Map<String, Object> payload) {
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) throw new IllegalArgumentException("missing id for update");
        switch (resourceType) {
            case "herbs": {
                Herb h = mapToHerb(payload); h.setId(id);
                herbMapper.update(h); return herbToMap(h);
            }
            case "trainings": {
                TrainingMaterial t = mapToTraining(payload); t.setId(id);
                trainingMapper.update(t); return trainingToMap(t);
            }
            case "evaluations": {
                EvaluationRecord e = mapToEvaluation(payload); e.setId(id);
                evaluationMapper.update(e); return evaluationToMap(e);
            }
            case "achievements": {
                String actorRole = String.valueOf(payload.getOrDefault("_actorRole", "admin"));
                if (!"admin".equals(actorRole)) {
                    AchievementRecord existing = achievementMapper.findById(id);
                    if (existing != null) {
                        payload.put("status", existing.getStatus());
                    }
                }
                AchievementRecord a = mapToAchievement(payload); a.setId(id);
                achievementMapper.update(a); return achievementToMap(a);
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

    public void delete(String resourceType, String id) {
        switch (resourceType) {
            case "herbs" -> herbMapper.deleteById(id);
            case "trainings" -> trainingMapper.deleteById(id);
            case "evaluations" -> evaluationMapper.deleteById(id);
            case "achievements" -> achievementMapper.deleteById(id);
            case "standards" -> standardMapper.deleteById(id);
            case "users" -> { userRoleMapper.deleteByUserId(id); userMapper.deleteById(id); }
        }
    }

    public long count(String resourceType) {
        return switch (resourceType) {
            case "herbs" -> herbMapper.countByTable("herb");
            case "trainings" -> trainingMapper.count();
            case "evaluations" -> evaluationMapper.count();
            case "achievements" -> achievementMapper.count();
            case "standards" -> standardMapper.countByTable("achievement_standard");
            default -> 0;
        };
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

    private TrainingMaterial mapToTraining(Map<String, Object> m) {
        TrainingMaterial t = new TrainingMaterial();
        t.setTitle(str(m, "title")); t.setTrainerName(str(m, "trainer"));
        t.setAudience(str(m, "audience")); t.setTracking(str(m, "tracking"));
        return t;
    }

    private EvaluationRecord mapToEvaluation(Map<String, Object> m) {
        EvaluationRecord e = new EvaluationRecord();
        e.setHerbName(str(m, "herbName")); e.setIndicator(str(m, "indicator"));
        e.setScore(toBigDecimal(m, "score")); e.setResult(str(m, "result"));
        e.setApplicationMaterial(str(m, "applicationMaterial"));
        return e;
    }

    private AchievementRecord mapToAchievement(Map<String, Object> m) {
        AchievementRecord a = new AchievementRecord();
        a.setTitle(str(m, "title")); a.setOwnerName(str(m, "owner"));
        a.setCategory(str(m, "category")); a.setLevelName(str(m, "level"));
        a.setStatus(str(m, "status"));
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
        if (t.getCreatedAt() != null) m.put("createdAt", t.getCreatedAt().toString());
        if (t.getUpdatedAt() != null) m.put("updatedAt", t.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> evaluationToMap(EvaluationRecord e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId()); m.put("herbName", e.getHerbName()); m.put("indicator", e.getIndicator());
        m.put("score", e.getScore()); m.put("result", e.getResult());
        m.put("applicationMaterial", e.getApplicationMaterial());
        if (e.getCreatedAt() != null) m.put("createdAt", e.getCreatedAt().toString());
        if (e.getUpdatedAt() != null) m.put("updatedAt", e.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> achievementToMap(AchievementRecord a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId()); m.put("title", a.getTitle()); m.put("owner", a.getOwnerName());
        m.put("category", a.getCategory()); m.put("level", a.getLevelName());
        m.put("status", a.getStatus());
        if (a.getCreatedAt() != null) m.put("createdAt", a.getCreatedAt().toString());
        if (a.getUpdatedAt() != null) m.put("updatedAt", a.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> standardToMap(AchievementStandard s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("name", s.getName()); m.put("category", s.getCategory());
        m.put("levelRule", s.getLevelRule()); m.put("effectiveDate", s.getEffectiveDate() != null ? s.getEffectiveDate().toString() : "");
        if (s.getCreatedAt() != null) m.put("createdAt", s.getCreatedAt().toString());
        if (s.getUpdatedAt() != null) m.put("updatedAt", s.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> userToMap(SysUser u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId()); m.put("username", u.getUsername()); m.put("name", u.getDisplayName());
        m.put("password", ""); m.put("department", u.getDepartment());
        m.put("status", u.getStatus());
        if (u.getCreatedAt() != null) m.put("createdAt", u.getCreatedAt().toString());
        if (u.getUpdatedAt() != null) m.put("updatedAt", u.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> sanitizeUserMap(Map<String, Object> source) {
        Map<String, Object> sanitized = new LinkedHashMap<>(source);
        sanitized.remove("password");
        sanitized.remove("passwordHash");
        sanitized.put("password", "");
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
}
