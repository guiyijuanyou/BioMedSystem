package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.GrowthRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface GrowthRecordMapper extends BiomedBaseMapper<GrowthRecord> {
    @Select("SELECT id, batch_id AS batchId, herb_name, district, temperature, humidity, soil_ph, " +
            "growth_stage, collect_source, recorder_name AS recorder, " +
            "recorder_role, recorded_at, version, created_at, updated_at " +
            "FROM growth_record ORDER BY recorded_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT id, batch_id AS batchId, herb_name, district, temperature, humidity, soil_ph, " +
            "growth_stage, collect_source, recorder_name AS recorder, recorder_role, recorded_at, " +
            "version, created_at, updated_at FROM growth_record WHERE batch_id = #{batchId} ORDER BY recorded_at ASC")
    List<Map<String, Object>> findByBatchIdAsMap(String batchId);

    @Select("SELECT id, batch_id AS batchId, herb_name, district, temperature, humidity, soil_ph, " +
            "growth_stage, collect_source, recorder_name AS recorder, " +
            "recorder_role, recorded_at, version, created_at, updated_at " +
            "FROM growth_record WHERE id = #{id}")
    Map<String, Object> findByIdAsMap(String id);

    @Select("SELECT * FROM growth_record WHERE herb_name = #{herbName} ORDER BY recorded_at DESC")
    List<GrowthRecord> findByHerbName(String herbName);

    @Insert("INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, " +
            "collect_source, recorder_name, recorder_role, recorded_at, created_at) " +
            "VALUES (#{id}, #{batchId}, #{herbName}, #{district}, #{temperature}, #{humidity}, #{soilPh}, #{growthStage}, " +
            "#{collectSource}, #{recorder}, #{recorderRole}, #{recordedAt}, #{createdAt})")
    int insertMap(Map<String, Object> record);

    @Update("UPDATE growth_record SET batch_id = #{batchId}, herb_name = #{herbName}, district = #{district}, temperature = #{temperature}, " +
            "humidity = #{humidity}, soil_ph = #{soilPh}, growth_stage = #{growthStage}, collect_source = #{collectSource}, " +
            "recorder_name = #{recorder}, recorder_role = #{recorderRole}, recorded_at = #{recordedAt}, updated_at = NOW(), " +
            "version = version + 1 WHERE id = #{id} AND version = #{version}")
    int updateMap(Map<String, Object> record);

}
