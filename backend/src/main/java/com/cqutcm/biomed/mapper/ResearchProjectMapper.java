package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.ResearchProject;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ResearchProjectMapper extends BiomedBaseMapper<ResearchProject> {
    @Select("SELECT id, title, leader_name AS leader, requirements, status, stage, " +
            "applicant_requests, approved_members, " +
            "rejected_applicants, transformation, " +
            "created_at, updated_at " +
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

}
