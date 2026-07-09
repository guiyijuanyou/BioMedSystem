package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.service.CourseRecordService;
import com.cqutcm.biomed.service.AuthService;
import com.cqutcm.biomed.service.GenericRecordService;
import com.cqutcm.biomed.service.GrowthRecordService;
import com.cqutcm.biomed.service.PermissionService;
import com.cqutcm.biomed.service.ProjectRecordService;
import com.cqutcm.biomed.service.ResearchDataService;
import com.cqutcm.biomed.service.StructuredRecordService;
import com.cqutcm.biomed.service.TraceEventService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResourceController {
    private final AuthService authService;
    private final GenericRecordService service;
    private final CourseRecordService courseService;
    private final GrowthRecordService growthService;
    private final PermissionService permissionService;
    private final ProjectRecordService projectService;
    private final ResearchDataService researchService;
    private final StructuredRecordService structuredService;
    private final TraceEventService traceService;

    public ResourceController(AuthService authService, GenericRecordService service, CourseRecordService courseService, GrowthRecordService growthService, PermissionService permissionService, ProjectRecordService projectService, ResearchDataService researchService, StructuredRecordService structuredService, TraceEventService traceService) {
        this.authService = authService;
        this.service = service;
        this.courseService = courseService;
        this.growthService = growthService;
        this.permissionService = permissionService;
        this.projectService = projectService;
        this.researchService = researchService;
        this.structuredService = structuredService;
        this.traceService = traceService;
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
        Path backup = service.backup();
        return Map.of("message", "backup completed", "file", backup.toString());
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
        return Map.of("items", service.list(resourceType));
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
        return service.create(resourceType, payload);
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
        return service.update(resourceType, payload);
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
        service.delete(resourceType, id);
        return Map.of("message", "record deleted", "id", id);
    }

    private void attachActor(Map<String, Object> payload, PermissionService.Actor actor) {
        payload.put("_actorName", actor.name());
        payload.put("_actorRole", actor.role());
    }
}
