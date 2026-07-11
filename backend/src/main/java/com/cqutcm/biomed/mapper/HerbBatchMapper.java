package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.HerbBatch;
import com.cqutcm.biomed.persistence.BiomedBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface HerbBatchMapper extends BiomedBaseMapper<HerbBatch> {
    @Select("SELECT b.id, b.herb_id AS herbId, h.name AS herbName, b.batch_code AS batchCode, " +
            "b.batch_name AS batchName, b.trace_code AS traceCode, b.plot_name AS plotName, b.district, " +
            "b.longitude, b.latitude, b.scale_desc AS scale, b.environment, b.planting_date AS plantingDate, " +
            "b.expected_harvest_date AS expectedHarvestDate, b.responsible_person AS responsiblePerson, " +
            "b.current_stage AS currentStage, b.status, b.version, b.created_at AS createdAt, b.updated_at AS updatedAt " +
            "FROM herb_batch b JOIN herb h ON h.id = b.herb_id ORDER BY b.created_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT b.id, b.herb_id AS herbId, h.name AS herbName, b.batch_code AS batchCode, " +
            "b.batch_name AS batchName, b.trace_code AS traceCode, b.plot_name AS plotName, b.district, " +
            "b.longitude, b.latitude, b.scale_desc AS scale, b.environment, b.planting_date AS plantingDate, " +
            "b.expected_harvest_date AS expectedHarvestDate, b.responsible_person AS responsiblePerson, " +
            "b.current_stage AS currentStage, b.status, b.version, b.created_at AS createdAt, b.updated_at AS updatedAt " +
            "FROM herb_batch b JOIN herb h ON h.id = b.herb_id WHERE b.id = #{id}")
    Map<String, Object> findByIdAsMap(String id);

    @Select("SELECT * FROM herb_batch WHERE batch_code = #{batchCode} LIMIT 1")
    HerbBatch findByBatchCode(String batchCode);

    @Select("SELECT (SELECT COUNT(*) FROM growth_record WHERE batch_id = #{id}) + " +
            "(SELECT COUNT(*) FROM trace_event WHERE batch_id = #{id}) + " +
            "(SELECT COUNT(*) FROM spectrum_comparison WHERE batch_id = #{id}) + " +
            "(SELECT COUNT(*) FROM growth_analysis WHERE batch_id = #{id}) + " +
            "(SELECT COUNT(*) FROM evaluation_record WHERE batch_id = #{id}) + " +
            "(SELECT COUNT(*) FROM lab_sample WHERE batch_id = #{id})")
    int countReferences(String id);
}
