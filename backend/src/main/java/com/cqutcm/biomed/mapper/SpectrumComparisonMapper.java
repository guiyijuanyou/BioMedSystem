package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.SpectrumComparison;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SpectrumComparisonMapper extends BiomedBaseMapper<SpectrumComparison> {

    public static final String SELECT_COLS =
            "id, batch_id AS batchId, sample_id AS sampleId, herb_name AS herbName, sample_code AS sampleCode, district, " +
            "spectrum_type AS spectrumType, reference_name AS referenceName, similarity, result, " +
            "operator_name AS operatorName, compared_at AS comparedAt, remark, " +
            "status, reviewer_name AS reviewerName, review_comment AS reviewComment, reviewed_at AS reviewedAt, " +
            "sample_data_json AS sampleDataJson, reference_data_json AS referenceDataJson, " +
            "compare_algorithm AS compareAlgorithm, " +
            "version, created_at AS createdAt, updated_at AS updatedAt ";

    @Select("SELECT " + SELECT_COLS + "FROM spectrum_comparison ORDER BY compared_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT " + SELECT_COLS + "FROM spectrum_comparison WHERE batch_id = #{batchId} ORDER BY compared_at DESC")
    List<Map<String, Object>> findByBatchIdAsMap(String batchId);

    @Select("SELECT " + SELECT_COLS + "FROM spectrum_comparison WHERE id = #{id}")
    Map<String, Object> findByIdAsMap(String id);

    @Insert("INSERT INTO spectrum_comparison (id, batch_id, sample_id, herb_name, sample_code, district, " +
            "spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, " +
            "status, sample_data_json, reference_data_json, compare_algorithm, created_at) " +
            "VALUES (#{id}, #{batchId}, #{sampleId}, #{herbName}, #{sampleCode}, #{district}, " +
            "#{spectrumType}, #{referenceName}, #{similarity}, #{result}, #{operatorName}, #{comparedAt}, #{remark}, " +
            "#{status}, #{sampleDataJson}, #{referenceDataJson}, #{compareAlgorithm}, #{createdAt})")
    int insertMap(Map<String, Object> record);

    @Update("UPDATE spectrum_comparison SET batch_id = #{batchId}, sample_id = #{sampleId}, herb_name = #{herbName}, sample_code = #{sampleCode}, district = #{district}, " +
            "spectrum_type = #{spectrumType}, reference_name = #{referenceName}, similarity = #{similarity}, " +
            "result = #{result}, operator_name = #{operatorName}, compared_at = #{comparedAt}, remark = #{remark}, " +
            "status = #{status}, reviewer_name = #{reviewerName}, review_comment = #{reviewComment}, " +
            "reviewed_at = #{reviewedAt}, version = version + 1, updated_at = NOW() " +
            "WHERE id = #{id} AND version = #{version}")
    int updateMap(Map<String, Object> record);

}
