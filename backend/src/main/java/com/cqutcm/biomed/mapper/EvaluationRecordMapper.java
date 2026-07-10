package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.EvaluationRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EvaluationRecordMapper {
    @Select("SELECT * FROM evaluation_record ORDER BY created_at DESC")
    List<EvaluationRecord> findAll();

    @Select("SELECT * FROM evaluation_record WHERE id = #{id}")
    EvaluationRecord findById(String id);

    @Insert("INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, created_at) " +
            "VALUES (#{id}, #{herbName}, #{indicator}, #{score}, #{result}, #{applicationMaterial}, NOW())")
    int insert(EvaluationRecord record);

    @Update("UPDATE evaluation_record SET herb_name = #{herbName}, indicator = #{indicator}, score = #{score}, " +
            "result = #{result}, application_material = #{applicationMaterial}, updated_at = NOW() WHERE id = #{id}")
    int update(EvaluationRecord record);

    @Delete("DELETE FROM evaluation_record WHERE id = #{id}")
    int deleteById(String id);

    @Select("SELECT COUNT(*) FROM evaluation_record")
    int count();
}
