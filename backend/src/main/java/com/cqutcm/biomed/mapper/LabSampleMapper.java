package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.LabSample;
import com.cqutcm.biomed.persistence.BiomedBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface LabSampleMapper extends BiomedBaseMapper<LabSample> {
    @Select("SELECT s.id, s.batch_id AS batchId, b.batch_code AS batchCode, b.batch_name AS batchName, " +
            "h.name AS herbName, b.district, s.sample_code AS sampleCode, s.sample_type AS sampleType, " +
            "s.collected_at AS collectedAt, s.collector_name AS collector, s.sample_location AS sampleLocation, " +
            "s.storage_condition AS storageCondition, s.status, s.version, s.created_at AS createdAt, s.updated_at AS updatedAt " +
            "FROM lab_sample s JOIN herb_batch b ON b.id = s.batch_id JOIN herb h ON h.id = b.herb_id " +
            "ORDER BY s.collected_at DESC, s.created_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT s.id, s.batch_id AS batchId, b.batch_code AS batchCode, b.batch_name AS batchName, " +
            "h.name AS herbName, b.district, s.sample_code AS sampleCode, s.sample_type AS sampleType, " +
            "s.collected_at AS collectedAt, s.collector_name AS collector, s.sample_location AS sampleLocation, " +
            "s.storage_condition AS storageCondition, s.status, s.version, s.created_at AS createdAt, s.updated_at AS updatedAt " +
            "FROM lab_sample s JOIN herb_batch b ON b.id = s.batch_id JOIN herb h ON h.id = b.herb_id " +
            "WHERE s.batch_id = #{batchId} ORDER BY s.collected_at DESC, s.created_at DESC")
    List<Map<String, Object>> findByBatchIdAsMap(String batchId);

    @Select("SELECT s.id, s.batch_id AS batchId, b.batch_code AS batchCode, b.batch_name AS batchName, " +
            "h.name AS herbName, b.district, s.sample_code AS sampleCode, s.sample_type AS sampleType, " +
            "s.collected_at AS collectedAt, s.collector_name AS collector, s.sample_location AS sampleLocation, " +
            "s.storage_condition AS storageCondition, s.status, s.version, s.created_at AS createdAt, s.updated_at AS updatedAt " +
            "FROM lab_sample s JOIN herb_batch b ON b.id = s.batch_id JOIN herb h ON h.id = b.herb_id " +
            "WHERE s.id = #{id}")
    Map<String, Object> findByIdAsMap(String id);

    @Select("SELECT * FROM lab_sample WHERE sample_code = #{sampleCode} LIMIT 1")
    LabSample findBySampleCode(String sampleCode);

    @Select("SELECT COUNT(*) FROM spectrum_comparison WHERE sample_id = #{id}")
    int countReferences(String id);
}
