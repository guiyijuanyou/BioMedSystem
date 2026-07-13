package com.cqutcm.biomed.service;

import com.cqutcm.biomed.config.AppDataPathResolver;
import com.cqutcm.biomed.entity.SysUser;
import com.cqutcm.biomed.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

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
    private final HerbBatchMapper batchMapper;
    private final LabSampleMapper sampleMapper;
    private final GrowthRecordMapper growthMapper;
    private final TraceEventMapper traceMapper;
    private final SpectrumComparisonMapper spectrumMapper;
    private final GrowthAnalysisMapper analysisMapper;
    private final CourseMapper courseMapper;
    private final TeachingResourceMapper resourceMapper;
    private final ResearchProjectMapper projectMapper;
    private final ProjectApplicationMapper projectApplicationMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final TrainingMaterialMapper trainingMapper;
    private final EvaluationRecordMapper evaluationMapper;
    private final AchievementRecordMapper achievementMapper;
    private final AchievementStandardMapper standardMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final Path backupDir;

    public BackupService(HerbMapper herbMapper, HerbBatchMapper batchMapper,
                         LabSampleMapper sampleMapper,
                         GrowthRecordMapper growthMapper,
                         TraceEventMapper traceMapper, SpectrumComparisonMapper spectrumMapper,
                         GrowthAnalysisMapper analysisMapper, CourseMapper courseMapper,
                         TeachingResourceMapper resourceMapper, ResearchProjectMapper projectMapper,
                         ProjectApplicationMapper projectApplicationMapper,
                         ProjectMemberMapper projectMemberMapper,
                         TrainingMaterialMapper trainingMapper, EvaluationRecordMapper evaluationMapper,
                         AchievementRecordMapper achievementMapper, AchievementStandardMapper standardMapper,
                         SysUserMapper userMapper, SysRoleMapper roleMapper,
                         SysUserRoleMapper userRoleMapper, JdbcTemplate jdbc,
                         ObjectMapper objectMapper,
                         AppDataPathResolver pathResolver,
                         @Value("${app.backup-dir:data}") String backupDir) {
        this.herbMapper = herbMapper;
        this.batchMapper = batchMapper;
        this.sampleMapper = sampleMapper;
        this.growthMapper = growthMapper;
        this.traceMapper = traceMapper;
        this.spectrumMapper = spectrumMapper;
        this.analysisMapper = analysisMapper;
        this.courseMapper = courseMapper;
        this.resourceMapper = resourceMapper;
        this.projectMapper = projectMapper;
        this.projectApplicationMapper = projectApplicationMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.trainingMapper = trainingMapper;
        this.evaluationMapper = evaluationMapper;
        this.achievementMapper = achievementMapper;
        this.standardMapper = standardMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.backupDir = pathResolver.resolve(backupDir);
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
            Files.createDirectories(backupDir);
            String timestamp = LocalDateTime.now().format(FILE_FMT);
            Path target = backupDir.resolve("backup-" + timestamp + ".json");
            Map<String, Object> backup = new LinkedHashMap<>();
            backup.put("herbsNormalized", herbMapper.findAll());
            backup.put("herbBatchesNormalized", batchMapper.findAll());
            backup.put("labSamplesNormalized", sampleMapper.findAll());
            backup.put("growthRecords", growthMapper.findAll());
            backup.put("traceEvents", traceMapper.findAll());
            backup.put("spectrumComparisons", spectrumMapper.findAll());
            backup.put("growthAnalyses", analysisMapper.findAll());
            backup.put("coursesNormalized", courseMapper.findAll());
            backup.put("teachingResourcesNormalized", resourceMapper.findAll());
            backup.put("researchProjects", projectMapper.findAll());
            backup.put("projectApplications", projectApplicationMapper.findAll());
            backup.put("projectMembers", projectMemberMapper.findAll());
            backup.put("trainingsNormalized", trainingMapper.findAll());
            backup.put("evaluationsNormalized", evaluationMapper.findAll());
            backup.put("achievementsNormalized", achievementMapper.findAll());
            backup.put("standardsNormalized", standardMapper.findAll());
            backup.put("evaluationIssues", jdbc.queryForList("SELECT * FROM evaluation_issue"));
            backup.put("improvementTrainingTasks", jdbc.queryForList("SELECT * FROM improvement_training_task"));
            backup.put("achievementEvidences", jdbc.queryForList("SELECT * FROM achievement_evidence"));
            backup.put("usersNormalized", userMapper.findAll().stream().map(this::safeUserProfile).toList());
            backup.put("rolesNormalized", roleMapper.findAll());
            backup.put("userRolesNormalized", userRoleMapper.findAll());
            backup.put("qualityMetricDefinitions", jdbc.queryForList("SELECT * FROM quality_metric_definition"));
            backup.put("batchMetricResults", jdbc.queryForList("SELECT * FROM batch_metric_result"));
            backup.put("evaluationSchemes", jdbc.queryForList("SELECT * FROM evaluation_scheme"));
            backup.put("evaluationSchemeItems", jdbc.queryForList("SELECT * FROM evaluation_scheme_item"));
            backup.put("multiMetricEvaluations", jdbc.queryForList("SELECT * FROM multi_metric_evaluation"));
            backup.put("multiMetricEvaluationDetails", jdbc.queryForList("SELECT * FROM multi_metric_evaluation_detail"));
            backup.put("trainingMaterialTags", jdbc.queryForList("SELECT * FROM training_material_tag"));
            backup.put("improvementRecommendations", jdbc.queryForList("SELECT * FROM improvement_recommendation"));
            backup.put("achievementScoringRules", jdbc.queryForList("SELECT * FROM achievement_scoring_rule"));
            backup.put("achievementQuantifications", jdbc.queryForList("SELECT * FROM achievement_quantification"));
            backup.put("mobileCollectionDevices", jdbc.queryForList("SELECT id,device_code,device_name,owner_name,platform_name,status,last_seen_at,created_by,created_at FROM mobile_collection_device"));
            backup.put("mobileSyncRecords", jdbc.queryForList("SELECT * FROM mobile_sync_record"));
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
