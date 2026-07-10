package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.SpectrumComparison;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SpectrumComparisonMapper extends BiomedBaseMapper<SpectrumComparison> {

    @Select("SELECT id, herb_name AS herbName, sample_code AS sampleCode, district, " +
            "spectrum_type AS spectrumType, reference_name AS referenceName, similarity, result, " +
            "operator_name AS operatorName, compared_at AS comparedAt, remark, " +
            "status, reviewer_name AS reviewerName, review_comment AS reviewComment, reviewed_at AS reviewedAt, " +
            "version, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM spectrum_comparison ORDER BY compared_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT id, herb_name AS herbName, sample_code AS sampleCode, district, " +
            "spectrum_type AS spectrumType, reference_name AS referenceName, similarity, result, " +
            "operator_name AS operatorName, compared_at AS comparedAt, remark, " +
            "status, reviewer_name AS reviewerName, review_comment AS reviewComment, reviewed_at AS reviewedAt, " +
            "version, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM spectrum_comparison WHERE id = #{id}")
    Map<String, Object> findByIdAsMap(String id);

    @Insert("INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, " +
            "similarity, result, operator_name, compared_at, remark, status, created_at) " +
            "VALUES (#{id}, #{herbName}, #{sampleCode}, #{district}, #{spectrumType}, #{referenceName}, " +
            "#{similarity}, #{result}, #{operatorName}, #{comparedAt}, #{remark}, #{status}, #{createdAt})")
    int insertMap(Map<String, Object> record);

    @Update("UPDATE spectrum_comparison SET herb_name = #{herbName}, sample_code = #{sampleCode}, district = #{district}, " +
            "spectrum_type = #{spectrumType}, reference_name = #{referenceName}, similarity = #{similarity}, " +
            "result = #{result}, operator_name = #{operatorName}, compared_at = #{comparedAt}, remark = #{remark}, " +
            "status = #{status}, reviewer_name = #{reviewerName}, review_comment = #{reviewComment}, " +
            "reviewed_at = #{reviewedAt}, version = version + 1, updated_at = NOW() " +
            "WHERE id = #{id} AND version = #{version}")
    int updateMap(Map<String, Object> record);

}
