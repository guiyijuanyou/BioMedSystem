package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.EvaluationRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface EvaluationRecordMapper extends BiomedBaseMapper<EvaluationRecord> {

    @Select("SELECT id, herb_name, indicator, score, result, application_material, subject_owner_name, " +
            "evaluator_name, evaluator_role, " +
            "status, reviewer_name, review_comment, reviewed_at, " +
            "version, created_at, updated_at FROM evaluation_record ORDER BY created_at DESC")
    List<Map<String, Object>> findAllAsMap();
}
