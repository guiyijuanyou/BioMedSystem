package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.GrowthRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface GrowthRecordMapper extends BiomedBaseMapper<GrowthRecord> {
    @Select("SELECT id, herb_name, district, temperature, humidity, soil_ph, " +
            "growth_stage, collect_source AS collector, recorder_name AS recorder, " +
            "recorder_role, recorded_at, created_at, updated_at " +
            "FROM growth_record ORDER BY recorded_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT id, herb_name, district, temperature, humidity, soil_ph, " +
            "growth_stage, collect_source AS collector, recorder_name AS recorder, " +
            "recorder_role, recorded_at, created_at, updated_at " +
            "FROM growth_record WHERE id = #{id}")
    Map<String, Object> findByIdAsMap(String id);

    @Select("SELECT * FROM growth_record WHERE herb_name = #{herbName} ORDER BY recorded_at DESC")
    List<GrowthRecord> findByHerbName(String herbName);

    @Insert("INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, " +
            "collect_source, recorder_name, recorder_role, recorded_at, created_at) " +
            "VALUES (#{id}, #{herbName}, #{district}, #{temperature}, #{humidity}, #{soilPh}, #{growthStage}, " +
            "#{collectSource}, #{recorder}, #{recorderRole}, #{recordedAt}, #{createdAt})")
    int insertMap(Map<String, Object> record);

    @Update("UPDATE growth_record SET herb_name = #{herbName}, district = #{district}, temperature = #{temperature}, " +
            "humidity = #{humidity}, soil_ph = #{soilPh}, growth_stage = #{growthStage}, collect_source = #{collectSource}, " +
            "recorder_name = #{recorder}, recorder_role = #{recorderRole}, recorded_at = #{recordedAt}, updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateMap(Map<String, Object> record);

}
