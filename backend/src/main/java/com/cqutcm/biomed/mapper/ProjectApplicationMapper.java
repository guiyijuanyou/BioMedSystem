package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.ProjectApplication;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectApplicationMapper {
    @Select("SELECT * FROM project_application ORDER BY applied_at DESC")
    List<ProjectApplication> findAll();

    @Select("SELECT * FROM project_application WHERE id = #{id}")
    ProjectApplication findById(String id);

    @Select("SELECT * FROM project_application WHERE project_id = #{projectId} ORDER BY applied_at DESC")
    List<ProjectApplication> findByProjectId(String projectId);

    @Insert("INSERT INTO project_application (id, project_id, student_name, status, apply_reason, review_comment, applied_at) " +
            "VALUES (#{id}, #{projectId}, #{studentName}, #{status}, #{applyReason}, #{reviewComment}, NOW())")
    int insert(ProjectApplication application);

    @Update("UPDATE project_application SET student_name = #{studentName}, status = #{status}, " +
            "apply_reason = #{applyReason}, review_comment = #{reviewComment}, reviewed_at = #{reviewedAt} WHERE id = #{id}")
    int update(ProjectApplication application);

    @Delete("DELETE FROM project_application WHERE id = #{id}")
    int deleteById(String id);

    @Delete("DELETE FROM project_application WHERE project_id = #{projectId}")
    int deleteByProjectId(String projectId);
}
