package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.TeachingResource;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface TeachingResourceMapper {
    @Select("SELECT * FROM teaching_resource ORDER BY created_at DESC")
    List<TeachingResource> findAll();

    @Select("SELECT * FROM teaching_resource WHERE id = #{id}")
    TeachingResource findById(String id);

    @Select("SELECT * FROM teaching_resource WHERE course_id = #{courseId} ORDER BY created_at DESC")
    List<TeachingResource> findByCourseId(String courseId);

    @Select("SELECT tr.id, tr.course_id AS courseId, tr.course_title AS courseTitle, tr.title, " +
            "tr.resource_type AS resourceType, tr.file_id AS fileId, tr.video_url AS videoUrl, tr.file_url AS fileUrl, " +
            "tr.uploader_name AS uploader, tr.uploader_role AS uploaderRole, tr.status, " +
            "tr.review_comment AS reviewComment, tr.published_at AS publishedAt, " +
            "tr.created_at AS createdAt, tr.updated_at AS updatedAt, " +
            "fa.file_name, fa.storage_path FROM teaching_resource tr " +
            "LEFT JOIN file_asset fa ON tr.file_id = fa.id")
    List<Map<String, Object>> findAllWithFile();

    @Select("SELECT tr.*, fa.file_name, fa.storage_path FROM teaching_resource tr " +
            "LEFT JOIN file_asset fa ON tr.file_id = fa.id WHERE tr.id = #{id}")
    Map<String, Object> findWithFileById(String id);

    @Insert("INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, " +
            "uploader_name, uploader_role, status, review_comment, published_at, created_at) " +
            "VALUES (#{id}, #{courseId}, #{courseTitle}, #{title}, #{resourceType}, #{fileId}, #{videoUrl}, #{fileUrl}, " +
            "#{uploaderName}, #{uploaderRole}, #{status}, #{reviewComment}, #{publishedAt}, NOW())")
    int insert(TeachingResource resource);

    @Update("UPDATE teaching_resource SET course_id = #{courseId}, course_title = #{courseTitle}, title = #{title}, " +
            "resource_type = #{resourceType}, file_id = #{fileId}, video_url = #{videoUrl}, file_url = #{fileUrl}, " +
            "uploader_name = #{uploaderName}, uploader_role = #{uploaderRole}, status = #{status}, " +
            "review_comment = #{reviewComment}, published_at = #{publishedAt}, updated_at = NOW() WHERE id = #{id}")
    int update(TeachingResource resource);

    @Delete("DELETE FROM teaching_resource WHERE id = #{id}")
    int deleteById(String id);
}
