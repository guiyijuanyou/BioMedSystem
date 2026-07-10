package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.ResearchProject;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ResearchProjectMapper {
    @Select("SELECT * FROM research_project ORDER BY created_at DESC")
    List<ResearchProject> findAll();

    @Select("SELECT * FROM research_project WHERE id = #{id}")
    ResearchProject findById(String id);

    @Insert("INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, created_at) " +
            "VALUES (#{id}, #{title}, #{leaderName}, #{requirements}, #{status}, #{stage}, #{transformation}, NOW())")
    int insert(ResearchProject project);

    @Update("UPDATE research_project SET title = #{title}, leader_name = #{leaderName}, requirements = #{requirements}, " +
            "status = #{status}, stage = #{stage}, transformation = #{transformation}, updated_at = NOW() WHERE id = #{id}")
    int update(ResearchProject project);

    @Select("SELECT id, title, leader_name AS leader, requirements, status, stage, " +
            "applicant_requests AS applicantRequests, approved_members AS approvedMembers, " +
            "rejected_applicants AS rejectedApplicants, transformation, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM research_project ORDER BY created_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Insert("INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, " +
            "applicant_requests, approved_members, rejected_applicants, created_at) " +
            "VALUES (#{id}, #{title}, #{leader}, #{requirements}, #{status}, #{stage}, #{transformation}, " +
            "#{applicantRequests}, #{approvedMembers}, #{rejectedApplicants}, #{createdAt})")
    int insertMap(Map<String, Object> record);

    @Update("UPDATE research_project SET title = #{title}, leader_name = #{leader}, requirements = #{requirements}, " +
            "status = #{status}, stage = #{stage}, transformation = #{transformation}, " +
            "applicant_requests = #{applicantRequests}, approved_members = #{approvedMembers}, " +
            "rejected_applicants = #{rejectedApplicants}, updated_at = NOW() WHERE id = #{id}")
    int updateMap(Map<String, Object> record);

    @Delete("DELETE FROM research_project WHERE id = #{id}")
    int deleteById(String id);

    @Select("SELECT COUNT(*) FROM research_project")
    int count();

    @Select("SELECT COUNT(*) > 0 FROM research_project WHERE id = #{id}")
    boolean exists(String id);
}
