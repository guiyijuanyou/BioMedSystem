package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.GrowthAnalysis;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface GrowthAnalysisMapper extends BiomedBaseMapper<GrowthAnalysis> {

    @Select("SELECT id, batch_id AS batchId, analysis_name AS analysisName, herb_name AS herbName, district, indicator, " +
            "baseline, current_value AS currentValue, difference_desc AS differenceDesc, trend, conclusion, " +
            "analyst_name AS analystName, analyzed_at AS analyzedAt, " +
            "status, reviewer_name AS reviewerName, review_comment AS reviewComment, reviewed_at AS reviewedAt, " +
            "version, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM growth_analysis ORDER BY analyzed_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT id, batch_id AS batchId, analysis_name AS analysisName, herb_name AS herbName, district, indicator, " +
            "baseline, current_value AS currentValue, difference_desc AS differenceDesc, trend, conclusion, " +
            "analyst_name AS analystName, analyzed_at AS analyzedAt, " +
            "status, reviewer_name AS reviewerName, review_comment AS reviewComment, reviewed_at AS reviewedAt, " +
            "version, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM growth_analysis WHERE id = #{id}")
    Map<String, Object> findByIdAsMap(String id);

    @Insert("INSERT INTO growth_analysis (id, batch_id, analysis_name, herb_name, district, indicator, baseline, current_value, " +
            "difference_desc, trend, conclusion, analyst_name, analyzed_at, status, created_at) " +
            "VALUES (#{id}, #{batchId}, #{analysisName}, #{herbName}, #{district}, #{indicator}, #{baseline}, #{currentValue}, " +
            "#{differenceDesc}, #{trend}, #{conclusion}, #{analystName}, #{analyzedAt}, #{status}, #{createdAt})")
    int insertMap(Map<String, Object> record);

    @Update("UPDATE growth_analysis SET batch_id = #{batchId}, analysis_name = #{analysisName}, herb_name = #{herbName}, district = #{district}, " +
            "indicator = #{indicator}, baseline = #{baseline}, current_value = #{currentValue}, difference_desc = #{differenceDesc}, " +
            "trend = #{trend}, conclusion = #{conclusion}, analyst_name = #{analystName}, analyzed_at = #{analyzedAt}, " +
            "status = #{status}, reviewer_name = #{reviewerName}, review_comment = #{reviewComment}, " +
            "reviewed_at = #{reviewedAt}, version = version + 1, updated_at = NOW() " +
            "WHERE id = #{id} AND version = #{version}")
    int updateMap(Map<String, Object> record);

}
