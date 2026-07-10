package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.GrowthAnalysis;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface GrowthAnalysisMapper {
    @Select("SELECT * FROM growth_analysis ORDER BY analyzed_at DESC")
    List<GrowthAnalysis> findAll();

    @Select("SELECT * FROM growth_analysis WHERE id = #{id}")
    GrowthAnalysis findById(String id);

    @Insert("INSERT INTO growth_analysis (id, analysis_name, herb_name, district, indicator, baseline, current_value, " +
            "difference_desc, trend, conclusion, analyst_name, analyzed_at, created_at) " +
            "VALUES (#{id}, #{analysisName}, #{herbName}, #{district}, #{indicator}, #{baseline}, #{currentValue}, " +
            "#{differenceDesc}, #{trend}, #{conclusion}, #{analystName}, #{analyzedAt}, NOW())")
    int insert(GrowthAnalysis record);

    @Update("UPDATE growth_analysis SET analysis_name = #{analysisName}, herb_name = #{herbName}, district = #{district}, " +
            "indicator = #{indicator}, baseline = #{baseline}, current_value = #{currentValue}, difference_desc = #{differenceDesc}, " +
            "trend = #{trend}, conclusion = #{conclusion}, analyst_name = #{analystName}, analyzed_at = #{analyzedAt}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int update(GrowthAnalysis record);

    @Delete("DELETE FROM growth_analysis WHERE id = #{id}")
    int deleteById(String id);

    @Select("SELECT id, analysis_name AS analysisName, herb_name AS herbName, district, indicator, " +
            "baseline, current_value AS currentValue, difference_desc AS difference, trend, conclusion, " +
            "analyst_name AS analyst, analyzed_at AS analyzedAt, created_at AS createdAt, updated_at AS updatedAt " +
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

    @Select("SELECT COUNT(*) FROM growth_analysis")
    int count();

    @Select("SELECT COUNT(*) > 0 FROM growth_analysis WHERE id = #{id}")
    boolean exists(String id);
}
