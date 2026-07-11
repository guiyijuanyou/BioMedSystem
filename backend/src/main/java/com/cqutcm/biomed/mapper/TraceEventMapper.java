package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;
import com.cqutcm.biomed.entity.TraceEvent;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface TraceEventMapper extends BiomedBaseMapper<TraceEvent> {

    @Select("SELECT * FROM trace_event WHERE trace_code = #{traceCode} ORDER BY event_time DESC")
    List<TraceEvent> findByTraceCode(String traceCode);

    @Select("SELECT id, batch_id AS batchId, herb_name AS herbName, trace_code AS traceCode, event_type AS eventType, " +
            "event_content AS eventContent, operator_name AS operatorName, event_time AS eventTime, " +
            "location, version, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM trace_event ORDER BY event_time DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT id, batch_id AS batchId, herb_name AS herbName, trace_code AS traceCode, event_type AS eventType, " +
            "event_content AS eventContent, operator_name AS operatorName, event_time AS eventTime, location, " +
            "version, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM trace_event WHERE batch_id = #{batchId} ORDER BY event_time ASC")
    List<Map<String, Object>> findByBatchIdAsMap(String batchId);

    @Select("SELECT id, batch_id AS batchId, herb_name AS herbName, trace_code AS traceCode, event_type AS eventType, " +
            "event_content AS eventContent, operator_name AS operatorName, event_time AS eventTime, " +
            "location, version, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM trace_event WHERE id = #{id}")
    Map<String, Object> findByIdAsMap(String id);

    @Insert("INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at) " +
            "VALUES (#{id}, #{batchId}, #{herbName}, #{traceCode}, #{eventType}, #{eventContent}, #{operatorName}, #{eventTime}, #{location}, #{createdAt})")
    int insertMap(Map<String, Object> record);

    @Update("UPDATE trace_event SET batch_id = #{batchId}, herb_name = #{herbName}, trace_code = #{traceCode}, event_type = #{eventType}, " +
            "event_content = #{eventContent}, operator_name = #{operatorName}, event_time = #{eventTime}, " +
            "location = #{location}, version = version + 1, updated_at = NOW() " +
            "WHERE id = #{id} AND version = #{version}")
    int updateMap(Map<String, Object> record);

}
