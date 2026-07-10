package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.TraceEvent;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface TraceEventMapper {
    @Select("SELECT * FROM trace_event ORDER BY event_time DESC")
    List<TraceEvent> findAll();

    @Select("SELECT * FROM trace_event WHERE id = #{id}")
    TraceEvent findById(String id);

    @Select("SELECT * FROM trace_event WHERE trace_code = #{traceCode} ORDER BY event_time DESC")
    List<TraceEvent> findByTraceCode(String traceCode);

    @Insert("INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at) " +
            "VALUES (#{id}, #{herbName}, #{traceCode}, #{eventType}, #{eventContent}, #{operatorName}, #{eventTime}, #{location}, NOW())")
    int insert(TraceEvent event);

    @Update("UPDATE trace_event SET herb_name = #{herbName}, trace_code = #{traceCode}, event_type = #{eventType}, " +
            "event_content = #{eventContent}, operator_name = #{operatorName}, event_time = #{eventTime}, " +
            "location = #{location}, updated_at = NOW() WHERE id = #{id}")
    int update(TraceEvent event);

    @Select("SELECT id, herb_name AS herbName, trace_code AS traceCode, event_type AS eventType, " +
            "event_content AS eventContent, operator_name AS operator, event_time AS eventTime, " +
            "location, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM trace_event ORDER BY event_time DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT id, herb_name AS herbName, trace_code AS traceCode, event_type AS eventType, " +
            "event_content AS eventContent, operator_name AS operator, event_time AS eventTime, " +
            "location, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM trace_event WHERE id = #{id}")
    Map<String, Object> findByIdAsMap(String id);

    @Insert("INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at) " +
            "VALUES (#{id}, #{herbName}, #{traceCode}, #{eventType}, #{eventContent}, #{operator}, #{eventTime}, #{location}, #{createdAt})")
    int insertMap(Map<String, Object> record);

    @Update("UPDATE trace_event SET herb_name = #{herbName}, trace_code = #{traceCode}, event_type = #{eventType}, " +
            "event_content = #{eventContent}, operator_name = #{operator}, event_time = #{eventTime}, " +
            "location = #{location}, updated_at = NOW() WHERE id = #{id}")
    int updateMap(Map<String, Object> record);

    @Delete("DELETE FROM trace_event WHERE id = #{id}")
    int deleteById(String id);

    @Select("SELECT COUNT(*) FROM trace_event")
    int count();

    @Select("SELECT COUNT(*) > 0 FROM trace_event WHERE id = #{id}")
    boolean exists(String id);
}
