package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.mapper.*;
import com.cqutcm.biomed.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ResourceController {
    private final AuthService authService;
    private final CourseRecordService courseService;
    private final GrowthRecordService growthService;
    private final PermissionService permissionService;
    private final ProjectRecordService projectService;
    private final ResearchDataService researchService;
    private final StructuredRecordService structuredService;
    private final TraceEventService traceService;

    private final HerbMapper herbMapper;
    private final GrowthRecordMapper growthMapper;
    private final TraceEventMapper traceMapper;
    private final CourseMapper courseMapper;
    private final TeachingResourceMapper resourceMapper;
    private final ResearchProjectMapper projectMapper;
    private final SpectrumComparisonMapper spectrumMapper;
    private final GrowthAnalysisMapper analysisMapper;
    private final TrainingMaterialMapper trainingMapper;
    private final EvaluationRecordMapper evaluationMapper;
    private final AchievementRecordMapper achievementMapper;
    private final AchievementStandardMapper standardMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final ObjectMapper objectMapper;

    public ResourceController(AuthService authService, CourseRecordService courseService,
                              GrowthRecordService growthService, PermissionService permissionService,
                              ProjectRecordService projectService, ResearchDataService researchService,
                              StructuredRecordService structuredService, TraceEventService traceService,
                              HerbMapper herbMapper, GrowthRecordMapper growthMapper,
                              TraceEventMapper traceMapper, CourseMapper courseMapper,
                              TeachingResourceMapper resourceMapper, ResearchProjectMapper projectMapper,
                              SpectrumComparisonMapper spectrumMapper, GrowthAnalysisMapper analysisMapper,
                              TrainingMaterialMapper trainingMapper, EvaluationRecordMapper evaluationMapper,
                              AchievementRecordMapper achievementMapper, AchievementStandardMapper standardMapper,
                              SysUserMapper userMapper, SysRoleMapper roleMapper,
                              SysUserRoleMapper userRoleMapper, ObjectMapper objectMapper) {
        this.authService = authService;
        this.courseService = courseService;
        this.growthService = growthService;
        this.permissionService = permissionService;
        this.projectService = projectService;
        this.researchService = researchService;
        this.structuredService = structuredService;
        this.traceService = traceService;
        this.herbMapper = herbMapper;
        this.growthMapper = growthMapper;
        this.traceMapper = traceMapper;
        this.courseMapper = courseMapper;
        this.resourceMapper = resourceMapper;
        this.projectMapper = projectMapper;
        this.spectrumMapper = spectrumMapper;
        this.analysisMapper = analysisMapper;
        this.trainingMapper = trainingMapper;
        this.evaluationMapper = evaluationMapper;
        this.achievementMapper = achievementMapper;
        this.standardMapper = standardMapper;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("courseCount", courseService.courseCount());
        summary.put("teachingResourceCount", courseService.resourceCount());
        summary.put("growthRecordCount", growthService.count());
        summary.put("traceEventCount", traceService.count());
        summary.put("spectrumComparisonCount", researchService.spectrumCount());
        summary.put("growthAnalysisCount", researchService.analysisCount());
        summary.put("projectCount", projectService.count());
        summary.put("herbCount", structuredService.count("herbs"));
        summary.put("evaluationCount", structuredService.count("evaluations"));
        summary.put("achievementCount", structuredService.count("achievements"));
        summary.put("latestBackup", LocalDateTime.now().toString());
        return summary;
    }

    @PostMapping("/backup")
    public Map<String, String> backup(@RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        if (!"admin".equals(actor.role())) {
            throw new IllegalArgumentException("only admin can backup");
        }
        Path target = doBackup();
        return Map.of("message", "backup completed", "file", target.toString());
    }

    private Path doBackup() {
        try {
            Files.createDirectories(Path.of("data"));
            Path target = Path.of("data", "backup-" + System.currentTimeMillis() + ".json");
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
            backup.put("usersNormalized", userMapper.findAll());
            backup.put("rolesNormalized", roleMapper.findAll());
            Files.writeString(target, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(backup), StandardCharsets.UTF_8);
            return target;
        } catch (Exception ex) {
            throw new IllegalStateException("backup failed", ex);
        }
    }

    @GetMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}")
    public Map<String, Object> list(@PathVariable String resourceType) {
        if ("projects".equals(resourceType)) {
            return Map.of("items", projectService.list());
        }
        if ("growth-records".equals(resourceType)) {
            return Map.of("items", growthService.list());
        }
        if ("trace-events".equals(resourceType)) {
            return Map.of("items", traceService.list());
        }
        if ("spectrum-comparisons".equals(resourceType)) {
            return Map.of("items", researchService.listSpectrum());
        }
        if ("growth-analysis".equals(resourceType)) {
            return Map.of("items", researchService.listAnalysis());
        }
        if ("courses".equals(resourceType)) {
            return Map.of("items", courseService.listCourses());
        }
        if ("teaching-resources".equals(resourceType)) {
            return Map.of("items", courseService.listResources());
        }
        if (structuredService.supports(resourceType)) {
            return Map.of("items", structuredService.list(resourceType));
        }
        throw new IllegalArgumentException("unsupported resource type: " + resourceType);
    }

    @PostMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}")
    public Map<String, Object> create(
            @PathVariable String resourceType,
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        attachActor(payload, actor);
        permissionService.assertCanCreate(resourceType, actor);
        if ("projects".equals(resourceType)) {
            return projectService.create(payload);
        }
        if ("growth-records".equals(resourceType)) {
            return growthService.create(payload);
        }
        if ("trace-events".equals(resourceType)) {
            return traceService.create(payload);
        }
        if ("spectrum-comparisons".equals(resourceType)) {
            return researchService.createSpectrum(payload);
        }
        if ("growth-analysis".equals(resourceType)) {
            return researchService.createAnalysis(payload);
        }
        if ("courses".equals(resourceType)) {
            return courseService.createCourse(payload);
        }
        if ("teaching-resources".equals(resourceType)) {
            return courseService.createResource(payload);
        }
        if (structuredService.supports(resourceType)) {
            return structuredService.create(resourceType, payload);
        }
        throw new IllegalArgumentException("unsupported resource type: " + resourceType);
    }

    @PutMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}")
    public Map<String, Object> update(
            @PathVariable String resourceType,
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        attachActor(payload, actor);
        permissionService.assertCanUpdate(resourceType, actor);
        if ("projects".equals(resourceType)) {
            return projectService.update(payload);
        }
        if ("growth-records".equals(resourceType)) {
            return growthService.update(payload);
        }
        if ("trace-events".equals(resourceType)) {
            return traceService.update(payload);
        }
        if ("spectrum-comparisons".equals(resourceType)) {
            return researchService.updateSpectrum(payload);
        }
        if ("growth-analysis".equals(resourceType)) {
            return researchService.updateAnalysis(payload);
        }
        if ("courses".equals(resourceType)) {
            return courseService.updateCourse(payload);
        }
        if ("teaching-resources".equals(resourceType)) {
            return courseService.updateResource(payload);
        }
        if (structuredService.supports(resourceType)) {
            return structuredService.update(resourceType, payload);
        }
        throw new IllegalArgumentException("unsupported resource type: " + resourceType);
    }

    @DeleteMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}/{id}")
    public Map<String, Object> delete(
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        permissionService.assertCanDelete(resourceType, actor);
        if ("projects".equals(resourceType)) {
            projectService.delete(id);
            return Map.of("message", "project deleted", "id", id);
        }
        if ("growth-records".equals(resourceType)) {
            growthService.delete(id, actor.name(), actor.role());
            return Map.of("message", "growth record deleted", "id", id);
        }
        if ("trace-events".equals(resourceType)) {
            traceService.delete(id);
            return Map.of("message", "trace event deleted", "id", id);
        }
        if ("spectrum-comparisons".equals(resourceType)) {
            researchService.deleteSpectrum(id);
            return Map.of("message", "spectrum comparison deleted", "id", id);
        }
        if ("growth-analysis".equals(resourceType)) {
            researchService.deleteAnalysis(id);
            return Map.of("message", "growth analysis deleted", "id", id);
        }
        if ("courses".equals(resourceType)) {
            courseService.deleteCourse(id);
            return Map.of("message", "course deleted", "id", id);
        }
        if ("teaching-resources".equals(resourceType)) {
            courseService.deleteResource(id);
            return Map.of("message", "teaching resource deleted", "id", id);
        }
        if (structuredService.supports(resourceType)) {
            structuredService.delete(resourceType, id);
            return Map.of("message", "structured record deleted", "id", id);
        }
        throw new IllegalArgumentException("unsupported resource type: " + resourceType);
    }

    private void attachActor(Map<String, Object> payload, PermissionService.Actor actor) {
        payload.put("_actorName", actor.name());
        payload.put("_actorRole", actor.role());
    }
}
