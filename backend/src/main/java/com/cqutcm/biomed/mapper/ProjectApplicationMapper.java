package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.ProjectApplication;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectApplicationMapper extends BiomedBaseMapper<ProjectApplication> {
    @Select("SELECT * FROM project_application WHERE project_id = #{projectId} ORDER BY applied_at DESC")
    List<ProjectApplication> findByProjectId(String projectId);

    @Delete("DELETE FROM project_application WHERE project_id = #{projectId}")
    int deleteByProjectId(String projectId);
}
