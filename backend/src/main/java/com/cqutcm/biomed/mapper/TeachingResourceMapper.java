package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.TeachingResource;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface TeachingResourceMapper extends BiomedBaseMapper<TeachingResource> {
    @Select("SELECT * FROM teaching_resource WHERE course_id = #{courseId} ORDER BY created_at DESC")
    List<TeachingResource> findByCourseId(String courseId);

    @Select("SELECT tr.id, tr.course_id, tr.course_title, tr.title, " +
            "tr.resource_type, tr.file_id, tr.video_url, tr.file_url, " +
            "tr.uploader_name AS uploader, tr.uploader_role, tr.status, " +
            "tr.review_comment, tr.published_at, " +
            "tr.created_at, tr.updated_at, " +
            "fa.file_name, fa.storage_path FROM teaching_resource tr " +
            "LEFT JOIN file_asset fa ON tr.file_id = fa.id")
    List<Map<String, Object>> findAllWithFile();

    @Select("SELECT tr.*, fa.file_name, fa.storage_path FROM teaching_resource tr " +
            "LEFT JOIN file_asset fa ON tr.file_id = fa.id WHERE tr.id = #{id}")
    Map<String, Object> findWithFileById(String id);

}
