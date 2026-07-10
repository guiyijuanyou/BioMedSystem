package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseMapper {
    @Select("SELECT * FROM course ORDER BY created_at DESC")
    List<Course> findAll();

    @Select("SELECT id, title, teacher_name AS teacher, hours, material_type AS materialType, " +
            "status, created_at AS createdAt, updated_at AS updatedAt FROM course ORDER BY created_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT * FROM course WHERE id = #{id}")
    Course findById(String id);

    @Select("SELECT * FROM course WHERE title LIKE CONCAT('%', #{title}, '%') ORDER BY created_at DESC")
    List<Course> findByTitle(String title);

    @Insert("INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at) " +
            "VALUES (#{id}, #{title}, #{teacherName}, #{hours}, #{materialType}, #{status}, NOW())")
    int insert(Course course);

    @Update("UPDATE course SET title = #{title}, teacher_name = #{teacherName}, hours = #{hours}, " +
            "material_type = #{materialType}, status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int update(Course course);

    @Delete("DELETE FROM course WHERE id = #{id}")
    int deleteById(String id);
}
