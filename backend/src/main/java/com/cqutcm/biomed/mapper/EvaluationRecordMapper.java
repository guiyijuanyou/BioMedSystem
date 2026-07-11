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

    @Select("SELECT id, batch_id AS batchId, herb_name AS herbName, indicator, score, result, " +
            "application_material AS applicationMaterial, subject_owner_name AS subjectOwner, " +
            "evaluator_name AS evaluator, evaluator_role AS evaluatorRole, status, reviewer_name AS reviewerName, " +
            "review_comment AS reviewComment, reviewed_at AS reviewedAt, version, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM evaluation_record WHERE batch_id = #{batchId} ORDER BY created_at DESC")
    List<Map<String, Object>> findByBatchIdAsMap(String batchId);
}
