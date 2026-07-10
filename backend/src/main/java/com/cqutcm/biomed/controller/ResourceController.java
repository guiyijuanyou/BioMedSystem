package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.*;
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
    public Map<String, Object> summary(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        authService.requireActor(authorization);
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
        java.nio.file.Path target = backupService.manualBackup();
        return Map.of("message", "backup completed", "file", target.toString());
    }

    @GetMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}")
    public Map<String, Object> list(
            @PathVariable String resourceType,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        permissionService.assertCanRead(resourceType, actor);
        return switch (resourceType) {
            case "projects" -> Map.of("items", projectService.list());
            case "growth-records" -> Map.of("items", growthService.list());
            case "trace-events" -> Map.of("items", traceService.list());
            case "spectrum-comparisons" -> Map.of("items", researchService.listSpectrum());
            case "growth-analysis" -> Map.of("items", researchService.listAnalysis());
            case "courses" -> Map.of("items", courseService.listCourses());
            case "teaching-resources" -> Map.of("items", courseService.listResources());
            default -> {
                if (structuredService.supports(resourceType)) {
                    yield Map.of("items", structuredService.list(resourceType));
                }
                throw new IllegalArgumentException("unsupported resource type: " + resourceType);
            }
        };
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
        return switch (resourceType) {
            case "projects" -> projectService.create(payload);
            case "growth-records" -> growthService.create(payload);
            case "trace-events" -> traceService.create(payload);
            case "spectrum-comparisons" -> researchService.createSpectrum(payload);
            case "growth-analysis" -> researchService.createAnalysis(payload);
            case "courses" -> courseService.createCourse(payload);
            case "teaching-resources" -> courseService.createResource(payload);
            default -> {
                if (structuredService.supports(resourceType)) {
                    yield structuredService.create(resourceType, payload);
                }
                throw new IllegalArgumentException("unsupported resource type: " + resourceType);
            }
        };
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
        return switch (resourceType) {
            case "projects" -> projectService.update(payload);
            case "growth-records" -> growthService.update(payload);
            case "trace-events" -> traceService.update(payload);
            case "spectrum-comparisons" -> researchService.updateSpectrum(payload);
            case "growth-analysis" -> researchService.updateAnalysis(payload);
            case "courses" -> courseService.updateCourse(payload);
            case "teaching-resources" -> courseService.updateResource(payload);
            default -> {
                if (structuredService.supports(resourceType)) {
                    yield structuredService.update(resourceType, payload);
                }
                throw new IllegalArgumentException("unsupported resource type: " + resourceType);
            }
        };
    }

    @DeleteMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}/{id}")
    public Map<String, Object> delete(
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        PermissionService.Actor actor = authService.requireActor(authorization);
        permissionService.assertCanDelete(resourceType, actor);
        return switch (resourceType) {
            case "projects" -> { projectService.delete(id); yield Map.of("message", "project deleted", "id", id); }
            case "growth-records" -> { growthService.delete(id, actor.name(), actor.role()); yield Map.of("message", "growth record deleted", "id", id); }
            case "trace-events" -> { traceService.delete(id); yield Map.of("message", "trace event deleted", "id", id); }
            case "spectrum-comparisons" -> { researchService.deleteSpectrum(id); yield Map.of("message", "spectrum comparison deleted", "id", id); }
            case "growth-analysis" -> { researchService.deleteAnalysis(id); yield Map.of("message", "growth analysis deleted", "id", id); }
            case "courses" -> { courseService.deleteCourse(id); yield Map.of("message", "course deleted", "id", id); }
            case "teaching-resources" -> { courseService.deleteResource(id); yield Map.of("message", "teaching resource deleted", "id", id); }
            default -> {
                if (structuredService.supports(resourceType)) {
                    structuredService.delete(resourceType, id);
                    yield Map.of("message", "structured record deleted", "id", id);
                }
                throw new IllegalArgumentException("unsupported resource type: " + resourceType);
            }
        };
    }

    private void attachActor(Map<String, Object> payload, PermissionService.Actor actor) {
        payload.put("_actorName", actor.name());
        payload.put("_actorRole", actor.role());
    }
}
