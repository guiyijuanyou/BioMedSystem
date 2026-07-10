package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.Herb;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HerbMapper {
    @Select("SELECT * FROM herb ORDER BY created_at DESC")
    List<Herb> findAll();

    @Select("SELECT * FROM herb WHERE id = #{id}")
    Herb findById(String id);

    @Insert("INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at) " +
            "VALUES (#{id}, #{name}, #{district}, #{longitude}, #{latitude}, #{scaleDesc}, #{environment}, #{traceCode}, NOW())")
    int insert(Herb herb);

    @Update("UPDATE herb SET name = #{name}, district = #{district}, longitude = #{longitude}, latitude = #{latitude}, " +
            "scale_desc = #{scaleDesc}, environment = #{environment}, trace_code = #{traceCode}, updated_at = NOW() WHERE id = #{id}")
    int update(Herb herb);

    @Delete("DELETE FROM herb WHERE id = #{id}")
    int deleteById(String id);

    @Select("SELECT COUNT(*) FROM ${tableName}")
    int countByTable(String tableName);
}
