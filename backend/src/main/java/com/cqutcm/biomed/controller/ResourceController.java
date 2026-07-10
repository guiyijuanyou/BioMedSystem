package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.dto.*;
import com.cqutcm.biomed.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResourceController {
    private final AuthService authService;
    private final BackupService backupService;
    private final CourseRecordService courseService;
    private final GrowthRecordService growthService;
    private final PermissionService permissionService;
    private final ProjectRecordService projectService;
    private final ResearchDataService researchService;
    private final StructuredRecordService structuredService;
    private final TraceEventService traceService;

    public ResourceController(AuthService authService, BackupService backupService,
                              CourseRecordService courseService, GrowthRecordService growthService,
                              PermissionService permissionService, ProjectRecordService projectService,
                              ResearchDataService researchService, StructuredRecordService structuredService,
                              TraceEventService traceService) {
        this.authService = authService;
        this.backupService = backupService;
        this.courseService = courseService;
        this.growthService = growthService;
        this.permissionService = permissionService;
        this.projectService = projectService;
        this.researchService = researchService;
        this.structuredService = structuredService;
        this.traceService = traceService;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireActor(authorization);
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("courseCount", courseService.courseCount());
        s.put("teachingResourceCount", courseService.resourceCount());
        s.put("growthRecordCount", growthService.count());
        s.put("traceEventCount", traceService.count());
        s.put("spectrumComparisonCount", researchService.spectrumCount());
        s.put("growthAnalysisCount", researchService.analysisCount());
        s.put("projectCount", projectService.count());
        s.put("herbCount", structuredService.count("herbs"));
        s.put("evaluationCount", structuredService.count("evaluations"));
        s.put("achievementCount", structuredService.count("achievements"));
        s.put("latestBackup", LocalDateTime.now().toString());
        return s;
    }

    @PostMapping("/backup")
    public Map<String, String> backup(@RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        if (!"admin".equals(a.role())) throw new AuthorizationDeniedException("only admin can backup");
        return Map.of("message", "backup completed", "file", backupService.manualBackup().toString());
    }

    @GetMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}")
    public Map<String, Object> list(@PathVariable String resourceType,
                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanRead(resourceType, a);
        return switch (resourceType) {
            case "projects" -> Map.of("items", projectService.list(a));
            case "growth-records" -> Map.of("items", growthService.list());
            case "trace-events" -> Map.of("items", traceService.list());
            case "spectrum-comparisons" -> Map.of("items", researchService.listSpectrumForActor(a));
            case "growth-analysis" -> Map.of("items", researchService.listAnalysisForActor(a));
            case "courses" -> Map.of("items", courseService.listCoursesForActor(a));
            case "teaching-resources" -> Map.of("items", courseService.listResourcesForActor(a));
            default -> {
                if (structuredService.supports(resourceType))
                    yield Map.of("items", structuredService.list(resourceType, a));
                throw new IllegalArgumentException("unsupported: " + resourceType);
            }
        };
    }

    // ==================== CREATE ====================

    @PostMapping("/courses")
    public Map<String, Object> createCourse(@Valid @RequestBody CourseDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("courses", a);
        return courseService.createCourse(toMap(dto), a);
    }

    @PostMapping("/teaching-resources")
    public Map<String, Object> createTeachingResource(@Valid @RequestBody TeachingResourceDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("teaching-resources", a);
        return courseService.createResource(toMap(dto), a);
    }

    @PostMapping("/spectrum-comparisons")
    public Map<String, Object> createSpectrum(@Valid @RequestBody SpectrumComparisonDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("spectrum-comparisons", a);
        return researchService.createSpectrum(toMap(dto), a);
    }

    @PostMapping("/growth-analysis")
    public Map<String, Object> createAnalysis(@Valid @RequestBody GrowthAnalysisDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("growth-analysis", a);
        return researchService.createAnalysis(toMap(dto), a);
    }

    @PostMapping("/growth-records")
    public Map<String, Object> createGrowthRecord(@Valid @RequestBody GrowthRecordDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("growth-records", a);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        return growthService.create(m);
    }

    @PostMapping("/trace-events")
    public Map<String, Object> createTraceEvent(@Valid @RequestBody TraceEventDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("trace-events", a);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        return traceService.create(m);
    }

    @PostMapping("/projects")
    public Map<String, Object> createProject(@Valid @RequestBody ResearchProjectDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("projects", a);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        return projectService.create(m);
    }

    @PostMapping("/{resourceType:^(?!files$|summary$|backup$|soap$|courses|teaching-resources|spectrum-comparisons|growth-analysis|growth-records|trace-events|projects$).+}")
    public Map<String, Object> createStructured(@PathVariable String resourceType,
            @Valid @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate(resourceType, a);
        attachActor(payload, a);
        return structuredService.create(resourceType, payload);
    }

    // ==================== UPDATE ====================

    @PutMapping("/courses")
    public Map<String, Object> updateCourse(@Valid @RequestBody CourseDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("courses", a);
        return courseService.updateCourse(m, a);
    }

    @PutMapping("/teaching-resources")
    public Map<String, Object> updateTeachingResource(@Valid @RequestBody TeachingResourceDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("teaching-resources", a);
        return courseService.updateResource(m, a);
    }

    @PutMapping("/spectrum-comparisons")
    public Map<String, Object> updateSpectrum(@Valid @RequestBody SpectrumComparisonDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("spectrum-comparisons", a);
        return researchService.updateSpectrum(m, a);
    }

    @PutMapping("/growth-analysis")
    public Map<String, Object> updateAnalysis(@Valid @RequestBody GrowthAnalysisDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("growth-analysis", a);
        return researchService.updateAnalysis(m, a);
    }

    @PutMapping("/growth-records")
    public Map<String, Object> updateGrowthRecord(@Valid @RequestBody GrowthRecordDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("growth-records", a);
        return growthService.update(m);
    }

    @PutMapping("/trace-events")
    public Map<String, Object> updateTraceEvent(@Valid @RequestBody TraceEventDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("trace-events", a);
        return traceService.update(m);
    }

    @PutMapping("/projects")
    public Map<String, Object> updateProject(@Valid @RequestBody ResearchProjectDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("projects", a);
        return projectService.update(m);
    }

    @PutMapping("/{resourceType:^(?!files$|summary$|backup$|soap$|courses|teaching-resources|spectrum-comparisons|growth-analysis|growth-records|trace-events|projects$).+}")
    public Map<String, Object> updateStructured(@PathVariable String resourceType,
            @Valid @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        attachActor(payload, a);
        permissionService.assertCanUpdate(resourceType, a);
        return structuredService.update(resourceType, payload);
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}/{id}")
    public Map<String, Object> delete(@PathVariable String resourceType, @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanDelete(resourceType, a);
        return switch (resourceType) {
            case "projects" -> { projectService.delete(id, a); yield Map.of("message", "deleted", "id", id); }
            case "growth-records" -> { growthService.delete(id, a.name(), a.role()); yield Map.of("message", "deleted", "id", id); }
            case "trace-events" -> { traceService.delete(id, a); yield Map.of("message", "deleted", "id", id); }
            case "spectrum-comparisons" -> { researchService.deleteSpectrum(id, a); yield Map.of("message", "deleted", "id", id); }
            case "growth-analysis" -> { researchService.deleteAnalysis(id, a); yield Map.of("message", "deleted", "id", id); }
            case "courses" -> { courseService.deleteCourse(id, a); yield Map.of("message", "deleted", "id", id); }
            case "teaching-resources" -> { courseService.deleteResource(id, a); yield Map.of("message", "deleted", "id", id); }
            default -> {
                if (structuredService.supports(resourceType)) {
                    structuredService.delete(resourceType, id, a);
                    yield Map.of("message", "deleted", "id", id);
                }
                throw new IllegalArgumentException("unsupported: " + resourceType);
            }
        };
    }

    // ==================== SUBMIT / REVIEW ====================

    @PutMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}/{id}/submit")
    public Map<String, Object> submit(@PathVariable String resourceType, @PathVariable String id,
                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        return switch (resourceType) {
            case "projects" -> projectService.submit(id, a);
            case "courses" -> courseService.submitCourse(id, a);
            case "teaching-resources" -> courseService.submitResource(id, a);
            case "spectrum-comparisons" -> researchService.submitSpectrum(id, a);
            case "growth-analysis" -> researchService.submitAnalysis(id, a);
            case "trainings", "evaluations", "achievements" -> structuredService.submit(resourceType, id, a);
            default -> throw new IllegalArgumentException("unsupported submission: " + resourceType);
        };
    }

    @PutMapping("/courses/{id}/review")
    public Map<String, Object> reviewCourse(@PathVariable String id,
            @Valid @RequestBody CourseDTOs.Review dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return courseService.reviewCourse(id, authService.requireActor(authorization), dto.status,
                dto.reviewComment != null ? dto.reviewComment : "");
    }

    @PutMapping("/teaching-resources/{id}/review")
    public Map<String, Object> reviewTeachingResource(@PathVariable String id,
            @Valid @RequestBody TeachingResourceDTOs.Review dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return courseService.reviewResource(id, authService.requireActor(authorization), dto.status,
                dto.reviewComment != null ? dto.reviewComment : "");
    }

    @PutMapping("/spectrum-comparisons/{id}/review")
    public Map<String, Object> reviewSpectrum(@PathVariable String id,
            @Valid @RequestBody SpectrumComparisonDTOs.Review dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return researchService.reviewSpectrum(id, authService.requireActor(authorization), dto.status,
                dto.reviewComment != null ? dto.reviewComment : "");
    }

    @PutMapping("/growth-analysis/{id}/review")
    public Map<String, Object> reviewAnalysis(@PathVariable String id,
            @Valid @RequestBody GrowthAnalysisDTOs.Review dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return researchService.reviewAnalysis(id, authService.requireActor(authorization), dto.status,
                dto.reviewComment != null ? dto.reviewComment : "");
    }

    @PutMapping("/projects/{id}/review")
    public Map<String, Object> reviewProject(@PathVariable String id,
            @Valid @RequestBody ResearchProjectDTOs.Review dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return projectService.review(id, authService.requireActor(authorization), dto.status,
                dto.reviewComment != null ? dto.reviewComment : "");
    }

    @PutMapping("/{resourceType:^(trainings|evaluations|achievements)$}/{id}/review")
    public Map<String, Object> reviewStructured(@PathVariable String resourceType, @PathVariable String id,
            @Valid @RequestBody Map<String, Object> dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        String s = String.valueOf(dto.getOrDefault("status", StatusMachine.APPROVED));
        String c = String.valueOf(dto.getOrDefault("reviewComment", ""));
        return structuredService.review(resourceType, id, a, s, c, dto);
    }

    // ---- 课题申请 ----

    @PostMapping("/projects/{projectId}/applications")
    public Map<String, Object> applyToProject(@PathVariable String projectId,
            @RequestBody(required = false) Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return projectService.apply(projectId, authService.requireActor(authorization),
                payload == null ? Map.of() : payload);
    }

    @PutMapping("/projects/{projectId}/applications/{applicationId}")
    public Map<String, Object> reviewProjectApplication(@PathVariable String projectId,
            @PathVariable String applicationId,
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return projectService.reviewApplication(projectId, applicationId,
                authService.requireActor(authorization), payload);
    }

    // ---- 辅助 ----

    private void attachActor(Map<String, Object> payload, PermissionService.Actor actor) {
        payload.put("_actorName", actor.name());
        payload.put("_actorRole", actor.role());
    }

    private Map<String, Object> toMap(Object dto) {
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            for (java.lang.reflect.Field f : dto.getClass().getFields()) {
                Object v = f.get(dto);
                if (v != null) map.put(f.getName(), v);
            }
        } catch (IllegalAccessException ignored) {}
        return map;
    }
}
