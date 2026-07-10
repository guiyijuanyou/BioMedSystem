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

@Service
public class CourseRecordService {
    private static final String PENDING_REVIEW = "待审核";

    private final CourseMapper courseMapper;
    private final TeachingResourceMapper resourceMapper;

    public CourseRecordService(CourseMapper courseMapper, TeachingResourceMapper resourceMapper) {
        this.courseMapper = courseMapper;
        this.resourceMapper = resourceMapper;
    }

    public List<Map<String, Object>> listCourses() {
        return courseMapper.findAllAsMap();
    }

    public List<Map<String, Object>> listResources() {
        return resourceMapper.findAllWithFile();
    }

    public Map<String, Object> createCourse(Map<String, Object> payload) {
        String id = UUID.randomUUID().toString();
        Course course = mapToCourse(payload);
        course.setId(id);
        course.setCreatedAt(LocalDateTime.now());
        courseMapper.insert(course);
        Map<String, Object> result = courseToMap(course);
        result.put("createdAt", course.getCreatedAt().toString());
        return result;
    }

    public Map<String, Object> createResource(Map<String, Object> payload) {
        String id = UUID.randomUUID().toString();
        TeachingResource resource = mapToResource(payload);
        resource.setId(id);
        resource.setCreatedAt(LocalDateTime.now());
        requireExistingCourse(resource.getCourseTitle());
        resourceMapper.insert(resource);
        Map<String, Object> result = resourceToMap(resource);
        result.put("createdAt", resource.getCreatedAt().toString());
        return result;
    }

    public Map<String, Object> updateCourse(Map<String, Object> payload) {
        String id = id(payload, "course");
        Map<String, Object> cleaned = clean(payload);
        keepCourseReviewForNonAdmin(id, payload, cleaned);
        cleaned.put("id", id);
        Course course = mapFromMap(cleaned);
        course.setId(id);
        courseMapper.update(course);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public Map<String, Object> updateResource(Map<String, Object> payload) {
        String id = id(payload, "teaching resource");
        TeachingResource resource = mapToResource(payload);
        resource.setId(id);
        keepResourceReviewForNonAdmin(id, payload, resource);
        requireExistingCourse(resource.getCourseTitle());
        resourceMapper.update(resource);
        Map<String, Object> result = resourceToMap(resource);
        result.put("id", id);
        result.put("updatedAt", LocalDateTime.now().toString());
        return result;
    }

    public void deleteCourse(String id) {
        if (courseMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("course not found");
        }
    }

    public void deleteResource(String id) {
        if (resourceMapper.deleteById(id) == 0) {
            throw new IllegalArgumentException("teaching resource not found");
        }
    }

    public long courseCount() {
        return courseMapper.findAll().size();
    }

    public long resourceCount() {
        return resourceMapper.findAll().size();
    }

    private String id(Map<String, Object> payload, String label) {
        String id = String.valueOf(payload.getOrDefault("id", ""));
        if (id.isBlank()) {
            throw new IllegalArgumentException("missing id for " + label);
        }
        return id;
    }

    private Map<String, Object> clean(Map<String, Object> payload) {
        Map<String, Object> cleaned = new LinkedHashMap<>(payload);
        cleaned.remove("id");
        cleaned.remove("createdAt");
        cleaned.remove("updatedAt");
        cleaned.putIfAbsent("status", PENDING_REVIEW);
        return cleaned;
    }

    private void keepCourseReviewForNonAdmin(String id, Map<String, Object> payload, Map<String, Object> cleaned) {
        if (isAdmin(payload)) return;
        Course existing = courseMapper.findById(id);
        if (existing != null) cleaned.put("status", existing.getStatus() != null ? existing.getStatus() : PENDING_REVIEW);
    }

    private void keepResourceReviewForNonAdmin(String id, Map<String, Object> payload, TeachingResource resource) {
        if (isAdmin(payload)) return;
        TeachingResource existing = resourceMapper.findById(id);
        if (existing != null) {
            resource.setStatus(existing.getStatus());
            resource.setReviewComment(existing.getReviewComment());
            resource.setPublishedAt(existing.getPublishedAt());
        }
    }

    private boolean isAdmin(Map<String, Object> payload) {
        return "admin".equals(String.valueOf(payload.getOrDefault("_actorRole", "")));
    }

    private void requireExistingCourse(String courseTitle) {
        if (courseTitle == null || courseTitle.isBlank()) {
            throw new IllegalArgumentException("请选择要发布到的试验课程");
        }
        List<Course> courses = courseMapper.findByTitle(courseTitle);
        if (courses.isEmpty()) {
            throw new IllegalArgumentException("试验课程不存在，请先在试验课程中创建课程");
        }
    }

    private Course mapToCourse(Map<String, Object> m) {
        Course c = new Course();
        c.setTitle(str(m, "title")); c.setTeacherName(str(m, "teacher"));
        c.setMaterialType(str(m, "materialType")); c.setStatus(str(m, "status"));
        try { c.setHours(new java.math.BigDecimal(String.valueOf(m.getOrDefault("hours", "0")))); } catch (Exception ignored) {}
        if (!isAdmin(m)) { c.setStatus(PENDING_REVIEW); }
        return c;
    }

    private Course mapFromMap(Map<String, Object> m) {
        Course c = new Course();
        c.setTitle(str(m, "title")); c.setTeacherName(str(m, "teacher"));
        c.setMaterialType(str(m, "materialType")); c.setStatus(str(m, "status"));
        try { c.setHours(new java.math.BigDecimal(String.valueOf(m.getOrDefault("hours", "0")))); } catch (Exception ignored) {}
        return c;
    }

    private TeachingResource mapToResource(Map<String, Object> m) {
        TeachingResource r = new TeachingResource();
        r.setCourseId(str(m, "courseId")); r.setCourseTitle(str(m, "courseTitle"));
        r.setTitle(str(m, "title")); r.setResourceType(str(m, "resourceType"));
        r.setFileId(str(m, "fileId")); r.setVideoUrl(str(m, "videoUrl"));
        r.setFileUrl(str(m, "fileUrl")); r.setUploaderName(str(m, "uploader"));
        r.setUploaderRole(str(m, "uploaderRole")); r.setStatus(str(m, "status"));
        r.setReviewComment(str(m, "reviewComment"));
        try { r.setPublishedAt(str(m, "publishedAt") != null && !str(m, "publishedAt").isBlank() ? LocalDateTime.parse(str(m, "publishedAt"), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null); } catch (Exception ignored) {}
        if (!isAdmin(m)) { r.setStatus(PENDING_REVIEW); r.setReviewComment(""); r.setPublishedAt(null); }
        return r;
    }

    private Map<String, Object> courseToMap(Course c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId()); m.put("title", c.getTitle()); m.put("teacher", c.getTeacherName());
        m.put("hours", c.getHours()); m.put("materialType", c.getMaterialType()); m.put("status", c.getStatus());
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
        m.put("status", r.getStatus()); m.put("reviewComment", r.getReviewComment());
        if (r.getPublishedAt() != null) m.put("publishedAt", r.getPublishedAt().toString());
        if (r.getCreatedAt() != null) m.put("createdAt", r.getCreatedAt().toString());
        if (r.getUpdatedAt() != null) m.put("updatedAt", r.getUpdatedAt().toString());
        return m;
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : String.valueOf(v);
    }
}
