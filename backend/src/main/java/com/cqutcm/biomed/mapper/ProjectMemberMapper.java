package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.ProjectMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectMemberMapper extends BiomedBaseMapper<ProjectMember> {
    @Select("SELECT * FROM project_member WHERE project_id = #{projectId} ORDER BY joined_at DESC")
    List<ProjectMember> findByProjectId(String projectId);

    @Delete("DELETE FROM project_member WHERE project_id = #{projectId}")
    int deleteByProjectId(String projectId);
}
