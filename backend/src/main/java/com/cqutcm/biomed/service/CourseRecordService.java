package com.cqutcm.biomed.service;

import com.cqutcm.biomed.repository.CourseRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CourseRecordService {
    private static final String PENDING_REVIEW = "\u5f85\u5ba1\u6838";

    private final CourseRecordRepository courseRepository;

    public CourseRecordService(CourseRecordRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Map<String, Object>> listCourses() {
        return courseRepository.findCourses();
    }

    public List<Map<String, Object>> listResources() {
        return courseRepository.findResources();
    }

    public Map<String, Object> createCourse(Map<String, Object> payload) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        forcePendingForNonAdmin(cleaned);
        LocalDateTime now = LocalDateTime.now();
        courseRepository.insertCourse(id, cleaned, now);
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        return cleaned;
    }

    public Map<String, Object> createResource(Map<String, Object> payload) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> cleaned = clean(payload);
        forcePendingForNonAdmin(cleaned);
        requireExistingCourse(cleaned);
        LocalDateTime now = LocalDateTime.now();
        courseRepository.insertResource(id, cleaned, now);
        cleaned.put("id", id);
        cleaned.put("createdAt", now.toString());
        return cleaned;
    }

    public Map<String, Object> updateCourse(Map<String, Object> payload) {
        String id = id(payload, "course");
        Map<String, Object> cleaned = clean(payload);
        keepCourseReviewForNonAdmin(id, cleaned);
        if (courseRepository.updateCourse(id, cleaned) == 0) {
            throw new IllegalArgumentException("course not found");
        }
        cleaned.put("id", id);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public Map<String, Object> updateResource(Map<String, Object> payload) {
        String id = id(payload, "teaching resource");
        Map<String, Object> cleaned = clean(payload);
        keepResourceReviewForNonAdmin(id, cleaned);
        requireExistingCourse(cleaned);
        if (courseRepository.updateResource(id, cleaned) == 0) {
            throw new IllegalArgumentException("teaching resource not found");
        }
        cleaned.put("id", id);
        cleaned.put("updatedAt", LocalDateTime.now().toString());
        return cleaned;
    }

    public void deleteCourse(String id) {
        if (courseRepository.deleteCourse(id) == 0) {
            throw new IllegalArgumentException("course not found");
        }
    }

    public void deleteResource(String id) {
        if (courseRepository.deleteResource(id) == 0) {
            throw new IllegalArgumentException("teaching resource not found");
        }
    }

    public long courseCount() {
        return courseRepository.countCourses();
    }

    public long resourceCount() {
        return courseRepository.countResources();
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

    private void forcePendingForNonAdmin(Map<String, Object> payload) {
        if (isAdmin(payload)) return;
        payload.put("status", PENDING_REVIEW);
        payload.put("reviewComment", "");
        payload.put("publishedAt", "");
    }

    private void keepCourseReviewForNonAdmin(String id, Map<String, Object> payload) {
        if (isAdmin(payload)) return;
        Map<String, Object> existing = courseRepository.findCourseById(id);
        payload.put("status", existing.getOrDefault("status", PENDING_REVIEW));
    }

    private void keepResourceReviewForNonAdmin(String id, Map<String, Object> payload) {
        if (isAdmin(payload)) return;
        Map<String, Object> existing = courseRepository.findResourceById(id);
        payload.put("status", existing.getOrDefault("status", PENDING_REVIEW));
        payload.put("reviewComment", existing.getOrDefault("reviewComment", ""));
        payload.put("publishedAt", existing.getOrDefault("publishedAt", ""));
    }

    private boolean isAdmin(Map<String, Object> payload) {
        return "admin".equals(String.valueOf(payload.getOrDefault("_actorRole", "")));
    }

    private void requireExistingCourse(Map<String, Object> payload) {
        String courseTitle = String.valueOf(payload.getOrDefault("courseTitle", "")).trim();
        if (courseTitle.isBlank()) {
            throw new IllegalArgumentException("请选择要发布到的试验课程");
        }
        if (!courseRepository.courseTitleExists(courseTitle)) {
            throw new IllegalArgumentException("试验课程不存在，请先在试验课程中创建课程");
        }
    }
}
