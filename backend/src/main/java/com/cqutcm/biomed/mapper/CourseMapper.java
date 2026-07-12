package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseMapper extends BiomedBaseMapper<Course> {
    @Select("SELECT id, title, teacher_name AS teacher, hours, material_type AS materialType, " +
            "status, reviewer_name AS reviewerName, review_comment AS reviewComment, reviewed_at AS reviewedAt, " +
            "version, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM course ORDER BY created_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT * FROM course WHERE title LIKE CONCAT('%', #{title}, '%') ORDER BY created_at DESC")
    List<Course> findByTitle(String title);

}
