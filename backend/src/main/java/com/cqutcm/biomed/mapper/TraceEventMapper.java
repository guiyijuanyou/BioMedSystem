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

    @Select("SELECT id, herb_name, trace_code, event_type, " +
            "event_content, operator_name AS operator, event_time, " +
            "location, created_at, updated_at " +
            "FROM trace_event ORDER BY event_time DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT id, herb_name, trace_code, event_type, " +
            "event_content, operator_name AS operator, event_time, " +
            "location, created_at, updated_at " +
            "FROM trace_event WHERE id = #{id}")
    Map<String, Object> findByIdAsMap(String id);

    @Insert("INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at) " +
            "VALUES (#{id}, #{herbName}, #{traceCode}, #{eventType}, #{eventContent}, #{operator}, #{eventTime}, #{location}, #{createdAt})")
    int insertMap(Map<String, Object> record);

    @Update("UPDATE trace_event SET herb_name = #{herbName}, trace_code = #{traceCode}, event_type = #{eventType}, " +
            "event_content = #{eventContent}, operator_name = #{operator}, event_time = #{eventTime}, " +
            "location = #{location}, updated_at = NOW() WHERE id = #{id}")
    int updateMap(Map<String, Object> record);

}
