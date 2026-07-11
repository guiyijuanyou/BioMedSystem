package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.dto.*;
import com.cqutcm.biomed.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api")
public class ResourceController {
    // 缓存 TTL 配置
    private static final Duration SUMMARY_TTL = Duration.ofMinutes(2);
    private static final Duration LIST_TTL = Duration.ofMinutes(5);
    private static final Duration SLOW_CHANGE_TTL = Duration.ofMinutes(10);

    private final AuthService authService;
    private final BatchCatalogService batchCatalogService;
    private final BackupService backupService;
    private final CacheService cacheService;
    private final CourseRecordService courseService;
    private final GrowthRecordService growthService;
    private final PermissionService permissionService;
    private final ProjectRecordService projectService;
    private final ResearchDataService researchService;
    private final StructuredRecordService structuredService;
    private final TraceEventService traceService;
    private final ObjectMapper objectMapper;

    private static final TypeReference<List<Map<String, Object>>> LIST_OF_MAPS =
            new TypeReference<>() {};

    public ResourceController(AuthService authService, BatchCatalogService batchCatalogService,
                              BackupService backupService, CacheService cacheService,
                              CourseRecordService courseService, GrowthRecordService growthService,
                              PermissionService permissionService, ProjectRecordService projectService,
                              ResearchDataService researchService, StructuredRecordService structuredService,
                              TraceEventService traceService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.batchCatalogService = batchCatalogService;
        this.backupService = backupService;
        this.cacheService = cacheService;
        this.courseService = courseService;
        this.growthService = growthService;
        this.permissionService = permissionService;
        this.projectService = projectService;
        this.researchService = researchService;
        this.structuredService = structuredService;
        this.traceService = traceService;
        this.objectMapper = objectMapper;
    }

    // ==================== SUMMARY ====================

    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireActor(authorization);
        return cacheService.get("summary", SUMMARY_TTL, () -> {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("courseCount", courseService.courseCount());
            s.put("teachingResourceCount", courseService.resourceCount());
            s.put("growthRecordCount", growthService.count());
            s.put("traceEventCount", traceService.count());
            s.put("spectrumComparisonCount", researchService.spectrumCount());
            s.put("growthAnalysisCount", researchService.analysisCount());
            s.put("projectCount", projectService.count());
            s.put("herbCount", structuredService.count("herbs"));
            s.put("herbBatchCount", batchCatalogService.listBatches().size());
            s.put("labSampleCount", batchCatalogService.listSamples().size());
            s.put("evaluationCount", structuredService.count("evaluations"));
            s.put("achievementCount", structuredService.count("achievements"));
            s.put("latestBackup", LocalDateTime.now().toString());
            return s;
        }, new TypeReference<Map<String, Object>>() {});
    }

    @PostMapping("/backup")
    public Map<String, String> backup(@RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        if (!"admin".equals(a.role())) throw new AuthorizationDeniedException("only admin can backup");
        return Map.of("message", "backup completed", "file", backupService.manualBackup().toString());
    }

    // ==================== LIST (with cache) ====================

    @GetMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}")
    public Map<String, Object> list(@PathVariable String resourceType,
                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanRead(resourceType, a);
        return switch (resourceType) {
            case "herb-batches" ->
                Map.of("items", cachedRawList("list:herb-batches", LIST_TTL, batchCatalogService::listBatches));
            case "lab-samples" ->
                Map.of("items", cachedRawList("list:lab-samples", LIST_TTL, batchCatalogService::listSamples));
            case "projects" ->
                // 课题列表涉及复杂 actor 过滤，不缓存
                Map.of("items", projectService.list(a));
            case "growth-records" ->
                Map.of("items", cachedRawList("list:growth-records", LIST_TTL, growthService::list));
            case "trace-events" ->
                Map.of("items", cachedRawList("list:trace-events", LIST_TTL, traceService::list));
            case "spectrum-comparisons" ->
                Map.of("items", cachedWithFilter("list:spectrum-comparisons", LIST_TTL,
                        researchService::listSpectrum,
                        all -> filterForStudent(all, "status", a)));
            case "growth-analysis" ->
                Map.of("items", cachedWithFilter("list:growth-analysis", LIST_TTL,
                        researchService::listAnalysis,
                        all -> filterForStudent(all, "status", a)));
            case "courses" ->
                Map.of("items", cachedWithFilter("list:courses", LIST_TTL,
                        courseService::listCourses,
                        all -> filterForStudent(all, "status", a)));
            case "teaching-resources" ->
                Map.of("items", cachedWithFilter("list:teaching-resources", LIST_TTL,
                        courseService::listResources,
                        all -> filterForStudent(all, "status", a)));
            default -> {
                if (structuredService.supports(resourceType)) {
                    Duration ttl = isSlowChange(resourceType) ? SLOW_CHANGE_TTL : LIST_TTL;
                    yield Map.of("items", cachedWithFilter("list:" + resourceType, ttl,
                            () -> structuredService.list(resourceType),
                            all -> filterStructuredForStudent(resourceType, all, a)));
                }
                throw new IllegalArgumentException("unsupported: " + resourceType);
            }
        };
    }

    // ==================== GET SINGLE ====================

    @GetMapping("/herb-batches/{id}")
    public Map<String, Object> getBatch(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanRead("herb-batches", a);
        return batchCatalogService.getBatch(id);
    }

    @GetMapping("/lab-samples/{id}")
    public Map<String, Object> getSample(@PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanRead("lab-samples", a);
        return batchCatalogService.getSample(id);
    }

    // ==================== CREATE (with cache eviction) ====================

    @PostMapping("/herb-batches")
    public Map<String, Object> createBatch(@Valid @RequestBody BatchCatalogDTOs.BatchCreate dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("herb-batches", a);
        Map<String, Object> result = batchCatalogService.createBatch(dto, a);
        evictAfterWrite("herb-batches");
        return result;
    }

    @PostMapping("/lab-samples")
    public Map<String, Object> createSample(@Valid @RequestBody BatchCatalogDTOs.SampleCreate dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("lab-samples", a);
        Map<String, Object> result = batchCatalogService.createSample(dto, a);
        evictAfterWrite("lab-samples");
        return result;
    }

    @PostMapping("/courses")
    public Map<String, Object> createCourse(@Valid @RequestBody CourseDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("courses", a);
        Map<String, Object> result = courseService.createCourse(toMap(dto), a);
        evictAfterWrite("courses");
        return result;
    }

    @PostMapping("/teaching-resources")
    public Map<String, Object> createTeachingResource(@Valid @RequestBody TeachingResourceDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("teaching-resources", a);
        Map<String, Object> result = courseService.createResource(toMap(dto), a);
        evictAfterWrite("teaching-resources");
        return result;
    }

    @PostMapping("/spectrum-comparisons")
    public Map<String, Object> createSpectrum(@Valid @RequestBody SpectrumComparisonDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("spectrum-comparisons", a);
        Map<String, Object> result = researchService.createSpectrum(toMap(dto), a);
        evictAfterWrite("spectrum-comparisons");
        return result;
    }

    @PostMapping("/growth-analysis")
    public Map<String, Object> createAnalysis(@Valid @RequestBody GrowthAnalysisDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("growth-analysis", a);
        Map<String, Object> result = researchService.createAnalysis(toMap(dto), a);
        evictAfterWrite("growth-analysis");
        return result;
    }

    @PostMapping("/growth-records")
    public Map<String, Object> createGrowthRecord(@Valid @RequestBody GrowthRecordDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("growth-records", a);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        Map<String, Object> result = growthService.create(m);
        evictAfterWrite("growth-records");
        return result;
    }

    @PostMapping("/trace-events")
    public Map<String, Object> createTraceEvent(@Valid @RequestBody TraceEventDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("trace-events", a);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        Map<String, Object> result = traceService.create(m);
        evictAfterWrite("trace-events");
        return result;
    }

    @PostMapping("/projects")
    public Map<String, Object> createProject(@Valid @RequestBody ResearchProjectDTOs.Create dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate("projects", a);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        Map<String, Object> result = projectService.create(m);
        evictAfterWrite("projects");
        return result;
    }

    @PostMapping("/{resourceType:^(?!files$|summary$|backup$|soap$|courses$|teaching-resources$|spectrum-comparisons$|growth-analysis$|growth-records$|trace-events$|projects$).+}")
    public Map<String, Object> createStructured(@PathVariable String resourceType,
            @Valid @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanCreate(resourceType, a);
        attachActor(payload, a);
        Map<String, Object> result = structuredService.create(resourceType, payload);
        evictAfterWrite(resourceType);
        return result;
    }

    // ==================== UPDATE (with cache eviction) ====================

    @PutMapping("/herb-batches")
    public Map<String, Object> updateBatch(@Valid @RequestBody BatchCatalogDTOs.BatchUpdate dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanUpdate("herb-batches", a);
        Map<String, Object> result = batchCatalogService.updateBatch(dto, a);
        evictAfterWrite("herb-batches");
        return result;
    }

    @PutMapping("/lab-samples")
    public Map<String, Object> updateSample(@Valid @RequestBody BatchCatalogDTOs.SampleUpdate dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanUpdate("lab-samples", a);
        Map<String, Object> result = batchCatalogService.updateSample(dto, a);
        evictAfterWrite("lab-samples");
        return result;
    }

    @PutMapping("/courses")
    public Map<String, Object> updateCourse(@Valid @RequestBody CourseDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("courses", a);
        Map<String, Object> result = courseService.updateCourse(m, a);
        evictAfterWrite("courses");
        return result;
    }

    @PutMapping("/teaching-resources")
    public Map<String, Object> updateTeachingResource(@Valid @RequestBody TeachingResourceDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("teaching-resources", a);
        Map<String, Object> result = courseService.updateResource(m, a);
        evictAfterWrite("teaching-resources");
        return result;
    }

    @PutMapping("/spectrum-comparisons")
    public Map<String, Object> updateSpectrum(@Valid @RequestBody SpectrumComparisonDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("spectrum-comparisons", a);
        Map<String, Object> result = researchService.updateSpectrum(m, a);
        evictAfterWrite("spectrum-comparisons");
        return result;
    }

    @PutMapping("/growth-analysis")
    public Map<String, Object> updateAnalysis(@Valid @RequestBody GrowthAnalysisDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("growth-analysis", a);
        Map<String, Object> result = researchService.updateAnalysis(m, a);
        evictAfterWrite("growth-analysis");
        return result;
    }

    @PutMapping("/growth-records")
    public Map<String, Object> updateGrowthRecord(@Valid @RequestBody GrowthRecordDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("growth-records", a);
        Map<String, Object> result = growthService.update(m);
        evictAfterWrite("growth-records");
        return result;
    }

    @PutMapping("/trace-events")
    public Map<String, Object> updateTraceEvent(@Valid @RequestBody TraceEventDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("trace-events", a);
        Map<String, Object> result = traceService.update(m);
        evictAfterWrite("trace-events");
        return result;
    }

    @PutMapping("/projects")
    public Map<String, Object> updateProject(@Valid @RequestBody ResearchProjectDTOs.Update dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> m = toMap(dto); attachActor(m, a);
        permissionService.assertCanUpdate("projects", a);
        Map<String, Object> result = projectService.update(m);
        evictAfterWrite("projects");
        return result;
    }

    @PutMapping("/{resourceType:^(?!files$|summary$|backup$|soap$|courses$|teaching-resources$|spectrum-comparisons$|growth-analysis$|growth-records$|trace-events$|projects$).+}")
    public Map<String, Object> updateStructured(@PathVariable String resourceType,
            @Valid @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        attachActor(payload, a);
        permissionService.assertCanUpdate(resourceType, a);
        Map<String, Object> result = structuredService.update(resourceType, payload);
        evictAfterWrite(resourceType);
        return result;
    }

    // ==================== DELETE (with cache eviction) ====================

    @DeleteMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}/{id}")
    public Map<String, Object> delete(@PathVariable String resourceType, @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        permissionService.assertCanDelete(resourceType, a);
        Map<String, Object> result = switch (resourceType) {
            case "herb-batches" -> { batchCatalogService.deleteBatch(id, a); yield Map.of("message", "deleted", "id", id); }
            case "lab-samples" -> { batchCatalogService.deleteSample(id, a); yield Map.of("message", "deleted", "id", id); }
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
        evictAfterWrite(resourceType);
        return result;
    }

    // ==================== SUBMIT / REVIEW (with cache eviction) ====================

    @PutMapping("/{resourceType:^(?!files$|summary$|backup$|soap$).+}/{id}/submit")
    public Map<String, Object> submit(@PathVariable String resourceType, @PathVariable String id,
                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        Map<String, Object> result = switch (resourceType) {
            case "projects" -> projectService.submit(id, a);
            case "courses" -> courseService.submitCourse(id, a);
            case "teaching-resources" -> courseService.submitResource(id, a);
            case "spectrum-comparisons" -> researchService.submitSpectrum(id, a);
            case "growth-analysis" -> researchService.submitAnalysis(id, a);
            case "trainings", "evaluations", "achievements" -> structuredService.submit(resourceType, id, a);
            default -> throw new IllegalArgumentException("unsupported submission: " + resourceType);
        };
        evictAfterWrite(resourceType);
        return result;
    }

    @PutMapping("/courses/{id}/review")
    public Map<String, Object> reviewCourse(@PathVariable String id,
            @Valid @RequestBody CourseDTOs.Review dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> result = courseService.reviewCourse(id, authService.requireActor(authorization), dto.status,
                dto.reviewComment != null ? dto.reviewComment : "");
        evictAfterWrite("courses");
        return result;
    }

    @PutMapping("/teaching-resources/{id}/review")
    public Map<String, Object> reviewTeachingResource(@PathVariable String id,
            @Valid @RequestBody TeachingResourceDTOs.Review dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> result = courseService.reviewResource(id, authService.requireActor(authorization), dto.status,
                dto.reviewComment != null ? dto.reviewComment : "");
        evictAfterWrite("teaching-resources");
        return result;
    }

    @PutMapping("/spectrum-comparisons/{id}/review")
    public Map<String, Object> reviewSpectrum(@PathVariable String id,
            @Valid @RequestBody SpectrumComparisonDTOs.Review dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> result = researchService.reviewSpectrum(id, authService.requireActor(authorization), dto.status,
                dto.reviewComment != null ? dto.reviewComment : "");
        evictAfterWrite("spectrum-comparisons");
        return result;
    }

    @PutMapping("/growth-analysis/{id}/review")
    public Map<String, Object> reviewAnalysis(@PathVariable String id,
            @Valid @RequestBody GrowthAnalysisDTOs.Review dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> result = researchService.reviewAnalysis(id, authService.requireActor(authorization), dto.status,
                dto.reviewComment != null ? dto.reviewComment : "");
        evictAfterWrite("growth-analysis");
        return result;
    }

    @PutMapping("/projects/{id}/review")
    public Map<String, Object> reviewProject(@PathVariable String id,
            @Valid @RequestBody ResearchProjectDTOs.Review dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> result = projectService.review(id, authService.requireActor(authorization), dto.status,
                dto.reviewComment != null ? dto.reviewComment : "");
        evictAfterWrite("projects");
        return result;
    }

    @PutMapping("/{resourceType:^(trainings|evaluations|achievements)$}/{id}/review")
    public Map<String, Object> reviewStructured(@PathVariable String resourceType, @PathVariable String id,
            @Valid @RequestBody Map<String, Object> dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        PermissionService.Actor a = authService.requireActor(authorization);
        String s = String.valueOf(dto.getOrDefault("status", StatusMachine.APPROVED));
        String c = String.valueOf(dto.getOrDefault("reviewComment", ""));
        Map<String, Object> result = structuredService.review(resourceType, id, a, s, c, dto);
        evictAfterWrite(resourceType);
        return result;
    }

    // ---- 课题申请 ----

    @PostMapping("/projects/{projectId}/applications")
    public Map<String, Object> applyToProject(@PathVariable String projectId,
            @RequestBody(required = false) Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> result = projectService.apply(projectId, authService.requireActor(authorization),
                payload == null ? Map.of() : payload);
        evictAfterWrite("projects");
        return result;
    }

    @PutMapping("/projects/{projectId}/applications/{applicationId}")
    public Map<String, Object> reviewProjectApplication(@PathVariable String projectId,
            @PathVariable String applicationId,
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> result = projectService.reviewApplication(projectId, applicationId,
                authService.requireActor(authorization), payload);
        evictAfterWrite("projects");
        return result;
    }

    // ==================== CACHE HELPERS ====================

    /** 缓存原始列表（无 actor 过滤） */
    private List<Map<String, Object>> cachedRawList(String cacheKey, Duration ttl,
                                                     Supplier<List<Map<String, Object>>> loader) {
        return cacheService.get(cacheKey, ttl, loader, LIST_OF_MAPS);
    }

    /** 缓存原始列表 + 再应用 actor 过滤 */
    private List<Map<String, Object>> cachedWithFilter(String cacheKey, Duration ttl,
                                                        Supplier<List<Map<String, Object>>> rawLoader,
                                                        Function<List<Map<String, Object>>, List<Map<String, Object>>> filter) {
        List<Map<String, Object>> raw = cacheService.get(cacheKey, ttl, rawLoader, LIST_OF_MAPS);
        return filter.apply(raw);
    }

    /** 写操作后清除列表缓存和 summary 缓存 */
    private void evictAfterWrite(String resourceType) {
        cacheService.evict("summary");
        cacheService.evict("list:" + resourceType);
    }

    /** 学生过滤：仅显示已发布/已批准等状态 */
    private List<Map<String, Object>> filterForStudent(List<Map<String, Object>> all, String statusField,
                                                        PermissionService.Actor actor) {
        if (!permissionService.isStudent(actor)) return all;
        return all.stream()
                .filter(item -> StatusMachine.isStudentVisible(str(item, statusField)))
                .toList();
    }

    /** 结构化资源的学生过滤 */
    private List<Map<String, Object>> filterStructuredForStudent(String resourceType,
                                                                  List<Map<String, Object>> all,
                                                                  PermissionService.Actor actor) {
        if (!resourceType.equals("trainings") && !resourceType.equals("evaluations")
                && !resourceType.equals("achievements")) {
            return all;
        }
        if (permissionService.isAdmin(actor)) return all;
        String ownerField = switch (resourceType) {
            case "trainings" -> "trainer";
            case "evaluations" -> "evaluator";
            default -> "owner";
        };
        return all.stream()
                .filter(item -> StatusMachine.isStudentVisible(str(item, "status"))
                        || actor.name().equals(str(item, ownerField)))
                .toList();
    }

    private boolean isSlowChange(String resourceType) {
        return "herbs".equals(resourceType) || "standards".equals(resourceType);
    }

    // ==================== UTILS ====================

    private void attachActor(Map<String, Object> payload, PermissionService.Actor actor) {
        payload.put("_actorName", actor.name());
        payload.put("_actorRole", actor.role());
    }

    private Map<String, Object> toMap(Object dto) {
        return objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {});
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : String.valueOf(v);
    }
}
