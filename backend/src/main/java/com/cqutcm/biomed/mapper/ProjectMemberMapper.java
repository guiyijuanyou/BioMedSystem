package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.ProjectMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectMemberMapper {
    @Select("SELECT * FROM project_member ORDER BY joined_at DESC")
    List<ProjectMember> findAll();

    @Select("SELECT * FROM project_member WHERE id = #{id}")
    ProjectMember findById(String id);

    @Select("SELECT * FROM project_member WHERE project_id = #{projectId} ORDER BY joined_at DESC")
    List<ProjectMember> findByProjectId(String projectId);

    @Insert("INSERT INTO project_member (id, project_id, member_name, member_role, joined_at) " +
            "VALUES (#{id}, #{projectId}, #{memberName}, #{memberRole}, NOW())")
    int insert(ProjectMember member);

    @Update("UPDATE project_member SET member_name = #{memberName}, member_role = #{memberRole} WHERE id = #{id}")
    int update(ProjectMember member);

    @Delete("DELETE FROM project_member WHERE id = #{id}")
    int deleteById(String id);

    @Delete("DELETE FROM project_member WHERE project_id = #{projectId}")
    int deleteByProjectId(String projectId);
}
