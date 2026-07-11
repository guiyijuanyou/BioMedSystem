package com.cqutcm.biomed.service;

import com.cqutcm.biomed.entity.Course;
import com.cqutcm.biomed.entity.TeachingResource;
import com.cqutcm.biomed.mapper.CourseMapper;
import com.cqutcm.biomed.mapper.TeachingResourceMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseRecordService {

    private final CourseMapper courseMapper;
    private final TeachingResourceMapper resourceMapper;
    private final PermissionService permissionService;

    public CourseRecordService(CourseMapper courseMapper, TeachingResourceMapper resourceMapper,
                               PermissionService permissionService) {
        this.courseMapper = courseMapper;
        this.resourceMapper = resourceMapper;
        this.permissionService = permissionService;
    }

    // ---- 列表（含学生过滤） ----

    public List<Map<String, Object>> listCourses() {
        return courseMapper.findAllAsMap();
    }

    public List<Map<String, Object>> listCoursesForActor(PermissionService.Actor actor) {
        List<Map<String, Object>> all = courseMapper.findAllAsMap();
        if (permissionService.isStudent(actor)) {
            return all.stream()
                    .filter(c -> StatusMachine.isStudentVisible(str(c, "status")))
                    .collect(Collectors.toList());
        }
        return all;
    }

    public List<Map<String, Object>> listResources() {
        return resourceMapper.findAllWithFile().stream().map(this::safeResourceView).toList();
    }

    public List<Map<String, Object>> listResourcesForActor(PermissionService.Actor actor) {
        List<Map<String, Object>> all = resourceMapper.findAllWithFile().stream()
                .map(this::safeResourceView).toList();
        if (permissionService.isStudent(actor)) {
            return all.stream()
                    .filter(r -> StatusMachine.isStudentVisible(str(r, "status")))
                    .collect(Collectors.toList());
        }
        return all;
    }

    // ---- 创建 ----

    public Map<String, Object> createCourse(Map<String, Object> payload, PermissionService.Actor actor) {
        String id = UUID.randomUUID().toString();
        Course course = mapToCourse(payload, actor);
        course.setId(id);
        course.setCreatedAt(LocalDateTime.now());
        courseMapper.insert(course);
        Map<String, Object> result = courseToMap(course);
        result.put("createdAt", course.getCreatedAt().toString());
        return result;
    }

    public Map<String, Object> createResource(Map<String, Object> payload, PermissionService.Actor actor) {
        // 管理员仅审核，不可伪装上传者创建
        permissionService.assertResourceUploader(actor, str(payload, "uploader"));
        String id = UUID.randomUUID().toString();
        TeachingResource resource = mapToResource(payload, actor);
        resource.setId(id);
        resource.setCreatedAt(LocalDateTime.now());
        // 通过 courseId 校验课程存在性，并填充课程标题（冗余展示）
        Course course = requireExistingCourse(resource.getCourseId());
        resource.setCourseTitle(course.getTitle());
        resourceMapper.insert(resource);
        Map<String, Object> result = resourceToMap(resource);
        result.put("createdAt", resource.getCreatedAt().toString());
        return result;
    }

    // ---- 更新 ----

    public Map<String, Object> updateCourse(Map<String, Object> payload, PermissionService.Actor actor) {
        String id = id(payload, "course");
        Course existing = courseMapper.findById(id);
        if (existing == null) throw new IllegalArgumentException("course not found");

        // 教师仅可编辑自身课程
        permissionService.assertCourseOwnership(actor, existing.getTeacherName(), "课程");
        if (!permissionService.isAdmin(actor)) StatusMachine.assertOwnerEditable(existing.getStatus(), "课程");

        Map<String, Object> cleaned = clean(payload);
        Course course = mapFromMap(cleaned);
        course.setId(id);
        course.setTeacherName(existing.getTeacherName());
        course.setStatus(existing.getStatus());
        course.setReviewerName(existing.getReviewerName());
        course.setReviewComment(existing.getReviewComment());
        course.setReviewedAt(existing.getReviewedAt());
        course.setVersion(existing.getVersion());
        if (courseMapper.update(course) == 0) throw conflict("课程");
        return courseToMap(course);
    }

    public Map<String, Object> updateResource(Map<String, Object> payload, PermissionService.Actor actor) {
        String id = id(payload, "teaching resource");
        TeachingResource existing = resourceMapper.findById(id);
        if (existing == null) throw new IllegalArgumentException("teaching resource not found");

        if (permissionService.isAdmin(actor)) {
            throw new AuthorizationDeniedException("管理员请使用审核接口处理教学资源");
        } else {
            permissionService.assertCourseOwnership(actor, existing.getUploaderName(), "教学资源");
            StatusMachine.assertOwnerEditable(existing.getStatus(), "教学资源");
        }

        TeachingResource resource = mapToResource(payload, actor);
        resource.setId(id);
        preserveResourceReviewFields(resource, existing);
        resource.setVersion(existing.getVersion());
        // 通过 courseId 校验课程存在性，并填充课程标题（冗余展示）
        Course course = requireExistingCourse(resource.getCourseId());
        resource.setCourseTitle(course.getTitle());
        if (resourceMapper.update(resource) == 0) throw conflict("教学资源");
        Map<String, Object> result = resourceToMap(resource);
        result.put("id", id);
        result.put("updatedAt", LocalDateTime.now().toString());
        return result;
    }

    // ---- 审核（管理员操作） ----

    public Map<String, Object> reviewCourse(String id, PermissionService.Actor actor, String targetStatus, String comment) {
        StatusMachine.assertRequireAdmin(actor.role());
        Course existing = courseMapper.findById(id);
        if (existing == null) throw new IllegalArgumentException("course not found");
        StatusMachine.assertTransition(existing.getStatus(), targetStatus, "课程");

        existing.setStatus(targetStatus);
        existing.setReviewerName(actor.name());
        existing.setReviewComment(comment);
        LocalDateTime now = LocalDateTime.now();
        existing.setReviewedAt(now);
        existing.setUpdatedAt(now);
        if (courseMapper.update(existing) == 0) throw conflict("课程");

        Map<String, Object> result = courseToMap(existing);
        return result;
    }

    public Map<String, Object> reviewResource(String id, PermissionService.Actor actor,
                                               String targetStatus, String comment) {
        StatusMachine.assertRequireAdmin(actor.role());
        TeachingResource existing = resourceMapper.findById(id);
        if (existing == null) throw new IllegalArgumentException("teaching resource not found");
        StatusMachine.assertTransition(existing.getStatus(), targetStatus, "教学资源");

        existing.setStatus(targetStatus);
        existing.setReviewerName(actor.name());
        existing.setReviewComment(comment);
        LocalDateTime now = LocalDateTime.now();
        existing.setReviewedAt(now);
        existing.setUpdatedAt(now);

        if (StatusMachine.PUBLISHED.equals(targetStatus)) {
            existing.setPublishedAt(now);
        }
        if (resourceMapper.update(existing) == 0) throw conflict("教学资源");

        return resourceToMap(existing);
    }

    public Map<String, Object> submitCourse(String id, PermissionService.Actor actor) {
        Course existing = courseMapper.findById(id);
        if (existing == null) throw new IllegalArgumentException("course not found");
        permissionService.assertCourseOwnership(actor, existing.getTeacherName(), "课程");
        StatusMachine.assertTransition(existing.getStatus(), StatusMachine.PENDING_REVIEW, "课程");
        existing.setStatus(StatusMachine.PENDING_REVIEW);
        if (courseMapper.update(existing) == 0) throw conflict("课程");
        return courseToMap(existing);
    }

    public Map<String, Object> submitResource(String id, PermissionService.Actor actor) {
        TeachingResource existing = resourceMapper.findById(id);
        if (existing == null) throw new IllegalArgumentException("teaching resource not found");
        permissionService.assertCourseOwnership(actor, existing.getUploaderName(), "教学资源");
        StatusMachine.assertTransition(existing.getStatus(), StatusMachine.PENDING_REVIEW, "教学资源");
        existing.setStatus(StatusMachine.PENDING_REVIEW);
        if (resourceMapper.update(existing) == 0) throw conflict("教学资源");
        return resourceToMap(existing);
    }

    // ---- 删除 ----

    public void deleteCourse(String id, PermissionService.Actor actor) {
        Course existing = courseMapper.findById(id);
        if (existing == null) throw new IllegalArgumentException("course not found");
        permissionService.assertCourseOwnership(actor, existing.getTeacherName(), "课程");
        if (!permissionService.isAdmin(actor)) StatusMachine.assertOwnerEditable(existing.getStatus(), "课程");
        if (courseMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("course not found");
        }
    }

    public void deleteResource(String id, PermissionService.Actor actor) {
        TeachingResource existing = resourceMapper.findById(id);
        if (existing == null) throw new IllegalArgumentException("teaching resource not found");
        permissionService.assertCourseOwnership(actor, existing.getUploaderName(), "教学资源");
        if (!permissionService.isAdmin(actor)) StatusMachine.assertOwnerEditable(existing.getStatus(), "教学资源");
        if (resourceMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("teaching resource not found");
        }
    }

    // ---- 计数 ----

    public long courseCount() {
        return courseMapper.findAll().size();
    }

    public long resourceCount() {
        return resourceMapper.findAll().size();
    }

    // ---- 私有方法 ----

    private String id(Map<String, Object> payload, String label) {
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) throw new IllegalArgumentException("missing id for " + label);
        return id;
    }

    private Map<String, Object> clean(Map<String, Object> payload) {
        Map<String, Object> cleaned = new LinkedHashMap<>(payload);
        cleaned.remove("id");
        cleaned.remove("createdAt");
        cleaned.remove("updatedAt");
        cleaned.putIfAbsent("status", StatusMachine.INITIAL_NON_ADMIN);
        return cleaned;
    }

    private void preserveResourceReviewFields(TeachingResource target, TeachingResource existing) {
        target.setUploaderName(existing.getUploaderName());
        target.setUploaderRole(existing.getUploaderRole());
        target.setStatus(existing.getStatus());
        target.setReviewerName(existing.getReviewerName());
        target.setReviewComment(existing.getReviewComment());
        target.setReviewedAt(existing.getReviewedAt());
        target.setPublishedAt(existing.getPublishedAt());
    }

    private Course requireExistingCourse(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("请选择要发布到的试验课程");
        }
        Course course = courseMapper.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("试验课程不存在，请先在试验课程中创建课程");
        }
        return course;
    }

    // ---- Map/Entity 转换 ----

    private Course mapToCourse(Map<String, Object> m, PermissionService.Actor actor) {
        Course c = new Course();
        c.setTitle(str(m, "title"));
        c.setMaterialType(str(m, "materialType"));
        try { c.setHours(new java.math.BigDecimal(String.valueOf(m.getOrDefault("hours", "0")))); } catch (Exception ignored) {}

        if (permissionService.isAdmin(actor)) {
            c.setTeacherName(str(m, "teacher"));
            String status = str(m, "status");
            c.setStatus(status == null || status.isBlank() ? StatusMachine.initialStatus(true) : status);
        } else {
            // 非管理员强制使用当前用户作为教师
            c.setTeacherName(actor.name());
            c.setStatus(StatusMachine.initialStatus(false));
        }
        return c;
    }

    private Course mapFromMap(Map<String, Object> m) {
        Course c = new Course();
        c.setTitle(str(m, "title"));
        c.setTeacherName(str(m, "teacher"));
        c.setMaterialType(str(m, "materialType"));
        c.setStatus(str(m, "status"));
        try { c.setHours(new java.math.BigDecimal(String.valueOf(m.getOrDefault("hours", "0")))); } catch (Exception ignored) {}
        return c;
    }

    private TeachingResource mapToResource(Map<String, Object> m, PermissionService.Actor actor) {
        TeachingResource r = new TeachingResource();
        r.setCourseId(str(m, "courseId"));
        r.setCourseTitle(str(m, "courseTitle"));
        r.setTitle(str(m, "title"));
        r.setResourceType(str(m, "resourceType"));
        r.setFileId(str(m, "fileId"));
        r.setVideoUrl(str(m, "videoUrl"));
        r.setFileUrl(str(m, "fileUrl"));

        if (permissionService.isAdmin(actor)) {
            // 管理员审核：保留上传者信息
            r.setUploaderName(str(m, "uploader"));
            r.setUploaderRole(str(m, "uploaderRole"));
            r.setStatus(str(m, "status"));
            r.setReviewComment(str(m, "reviewComment"));
            try { r.setPublishedAt(str(m, "publishedAt") != null && !str(m, "publishedAt").isBlank() ? LocalDateTime.parse(str(m, "publishedAt"), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null); } catch (Exception ignored) {}
        } else {
            // 非管理员：强制使用当前用户作为上传者
            r.setUploaderName(actor.name());
            r.setUploaderRole(permissionService.roleLabel(actor.role()));
            r.setStatus(StatusMachine.initialStatus(false));
            r.setReviewComment("");
            r.setPublishedAt(null);
        }
        return r;
    }

    private Map<String, Object> courseToMap(Course c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId()); m.put("title", c.getTitle()); m.put("teacher", c.getTeacherName());
        m.put("hours", c.getHours()); m.put("materialType", c.getMaterialType()); m.put("status", c.getStatus());
        m.put("reviewerName", c.getReviewerName()); m.put("reviewComment", c.getReviewComment());
        if (c.getReviewedAt() != null) m.put("reviewedAt", c.getReviewedAt().toString());
        m.put("version", c.getVersion());
        if (c.getCreatedAt() != null) m.put("createdAt", c.getCreatedAt().toString());
        if (c.getUpdatedAt() != null) m.put("updatedAt", c.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> resourceToMap(TeachingResource r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId()); m.put("courseId", r.getCourseId()); m.put("courseTitle", r.getCourseTitle());
        m.put("title", r.getTitle()); m.put("resourceType", r.getResourceType());
        m.put("fileId", r.getFileId()); m.put("videoUrl", r.getVideoUrl()); m.put("fileUrl", r.getFileUrl());
        m.put("uploader", r.getUploaderName()); m.put("uploaderRole", r.getUploaderRole());
        m.put("status", r.getStatus()); m.put("reviewerName", r.getReviewerName());
        m.put("reviewComment", r.getReviewComment());
        if (r.getReviewedAt() != null) m.put("reviewedAt", r.getReviewedAt().toString());
        m.put("version", r.getVersion());
        if (r.getPublishedAt() != null) m.put("publishedAt", r.getPublishedAt().toString());
        if (r.getCreatedAt() != null) m.put("createdAt", r.getCreatedAt().toString());
        if (r.getUpdatedAt() != null) m.put("updatedAt", r.getUpdatedAt().toString());
        return m;
    }

    private Map<String, Object> safeResourceView(Map<String, Object> source) {
        Map<String, Object> view = new LinkedHashMap<>(source);
        view.remove("storage_path");
        view.remove("storagePath");
        Object fileId = view.get("fileId");
        if (fileId != null && !String.valueOf(fileId).isBlank()) {
            view.put("previewUrl", "/api/files/" + fileId + "/preview");
            view.put("downloadUrl", "/api/files/" + fileId + "/download");
        }
        return view;
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private StateConflictException conflict(String label) {
        return new StateConflictException(label + "已被其他用户修改，请刷新后重试");
    }
}
