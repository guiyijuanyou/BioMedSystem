package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.GrowthAnalysis;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface GrowthAnalysisMapper extends BiomedBaseMapper<GrowthAnalysis> {
    @Select("SELECT id, analysis_name, herb_name, district, indicator, " +
            "baseline, current_value, difference_desc AS difference, trend, conclusion, " +
            "analyst_name AS analyst, analyzed_at, created_at, updated_at " +
            "FROM growth_analysis ORDER BY analyzed_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Insert("INSERT INTO growth_analysis (id, analysis_name, herb_name, district, indicator, baseline, current_value, " +
            "difference_desc, trend, conclusion, analyst_name, analyzed_at, created_at) " +
            "VALUES (#{id}, #{analysisName}, #{herbName}, #{district}, #{indicator}, #{baseline}, #{currentValue}, " +
            "#{difference}, #{trend}, #{conclusion}, #{analyst}, #{analyzedAt}, #{createdAt})")
    int insertMap(Map<String, Object> record);

    @Update("UPDATE growth_analysis SET analysis_name = #{analysisName}, herb_name = #{herbName}, district = #{district}, " +
            "indicator = #{indicator}, baseline = #{baseline}, current_value = #{currentValue}, difference_desc = #{difference}, " +
            "trend = #{trend}, conclusion = #{conclusion}, analyst_name = #{analyst}, analyzed_at = #{analyzedAt}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int updateMap(Map<String, Object> record);

}
