package com.cqutcm.biomed.service;

import com.cqutcm.biomed.entity.SysUser;
import com.cqutcm.biomed.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final HerbMapper herbMapper;
    private final GrowthRecordMapper growthMapper;
    private final TraceEventMapper traceMapper;
    private final SpectrumComparisonMapper spectrumMapper;
    private final GrowthAnalysisMapper analysisMapper;
    private final CourseMapper courseMapper;
    private final TeachingResourceMapper resourceMapper;
    private final ResearchProjectMapper projectMapper;
    private final TrainingMaterialMapper trainingMapper;
    private final EvaluationRecordMapper evaluationMapper;
    private final AchievementRecordMapper achievementMapper;
    private final AchievementStandardMapper standardMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final ObjectMapper objectMapper;

    public BackupService(HerbMapper herbMapper, GrowthRecordMapper growthMapper,
                         TraceEventMapper traceMapper, SpectrumComparisonMapper spectrumMapper,
                         GrowthAnalysisMapper analysisMapper, CourseMapper courseMapper,
                         TeachingResourceMapper resourceMapper, ResearchProjectMapper projectMapper,
                         TrainingMaterialMapper trainingMapper, EvaluationRecordMapper evaluationMapper,
                         AchievementRecordMapper achievementMapper, AchievementStandardMapper standardMapper,
                         SysUserMapper userMapper, SysRoleMapper roleMapper,
                         SysUserRoleMapper userRoleMapper, ObjectMapper objectMapper) {
        this.herbMapper = herbMapper;
        this.growthMapper = growthMapper;
        this.traceMapper = traceMapper;
        this.spectrumMapper = spectrumMapper;
        this.analysisMapper = analysisMapper;
        this.courseMapper = courseMapper;
        this.resourceMapper = resourceMapper;
        this.projectMapper = projectMapper;
        this.trainingMapper = trainingMapper;
        this.evaluationMapper = evaluationMapper;
        this.achievementMapper = achievementMapper;
        this.standardMapper = standardMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.objectMapper = objectMapper;
    }

    /** 每天凌晨 3:00 自动备份 */
    @Scheduled(cron = "0 0 3 * * ?")
    public void autoBackup() {
        try {
            Path path = execute();
            log.info("Scheduled backup completed → {}", path);
        } catch (Exception e) {
            log.error("Scheduled backup failed", e);
        }
    }

    /** 手动备份，返回文件路径 */
    public Path manualBackup() {
        return execute();
    }

    private Path execute() {
        try {
            Files.createDirectories(Path.of("data"));
            String timestamp = LocalDateTime.now().format(FILE_FMT);
            Path target = Path.of("data", "backup-" + timestamp + ".json");
            Map<String, Object> backup = new LinkedHashMap<>();
            backup.put("herbsNormalized", herbMapper.findAll());
            backup.put("growthRecords", growthMapper.findAll());
            backup.put("traceEvents", traceMapper.findAll());
            backup.put("spectrumComparisons", spectrumMapper.findAll());
            backup.put("growthAnalyses", analysisMapper.findAll());
            backup.put("coursesNormalized", courseMapper.findAll());
            backup.put("teachingResourcesNormalized", resourceMapper.findAll());
            backup.put("researchProjects", projectMapper.findAll());
            backup.put("trainingsNormalized", trainingMapper.findAll());
            backup.put("evaluationsNormalized", evaluationMapper.findAll());
            backup.put("achievementsNormalized", achievementMapper.findAll());
            backup.put("standardsNormalized", standardMapper.findAll());
            backup.put("usersNormalized", userMapper.findAll().stream().map(this::safeUserProfile).toList());
            backup.put("rolesNormalized", roleMapper.findAll());
            backup.put("userRolesNormalized", userRoleMapper.findAll());
            Files.writeString(target, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(backup),
                    StandardCharsets.UTF_8);
            return target;
        } catch (Exception ex) {
            throw new IllegalStateException("backup failed", ex);
        }
    }

    private Map<String, Object> safeUserProfile(SysUser user) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("displayName", user.getDisplayName());
        profile.put("department", user.getDepartment());
        profile.put("status", user.getStatus());
        profile.put("createdAt", user.getCreatedAt());
        profile.put("updatedAt", user.getUpdatedAt());
        return profile;
    }
}
