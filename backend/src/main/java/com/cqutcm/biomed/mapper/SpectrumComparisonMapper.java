package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.SpectrumComparison;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SpectrumComparisonMapper extends BiomedBaseMapper<SpectrumComparison> {
    @Select("SELECT id, herb_name, sample_code, district, " +
            "spectrum_type, reference_name, similarity, result, " +
            "operator_name AS operator, compared_at, remark, " +
            "created_at, updated_at " +
            "FROM spectrum_comparison ORDER BY compared_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Insert("INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, " +
            "similarity, result, operator_name, compared_at, remark, created_at) " +
            "VALUES (#{id}, #{herbName}, #{sampleCode}, #{district}, #{spectrumType}, #{referenceName}, " +
            "#{similarity}, #{result}, #{operator}, #{comparedAt}, #{remark}, #{createdAt})")
    int insertMap(Map<String, Object> record);

    @Update("UPDATE spectrum_comparison SET herb_name = #{herbName}, sample_code = #{sampleCode}, district = #{district}, " +
            "spectrum_type = #{spectrumType}, reference_name = #{referenceName}, similarity = #{similarity}, " +
            "result = #{result}, operator_name = #{operator}, compared_at = #{comparedAt}, remark = #{remark}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int updateMap(Map<String, Object> record);

}
