package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.SpectrumComparison;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SpectrumComparisonMapper {
    @Select("SELECT * FROM spectrum_comparison ORDER BY compared_at DESC")
    List<SpectrumComparison> findAll();

    @Select("SELECT * FROM spectrum_comparison WHERE id = #{id}")
    SpectrumComparison findById(String id);

    @Insert("INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, " +
            "similarity, result, operator_name, compared_at, remark, created_at) " +
            "VALUES (#{id}, #{herbName}, #{sampleCode}, #{district}, #{spectrumType}, #{referenceName}, " +
            "#{similarity}, #{result}, #{operatorName}, #{comparedAt}, #{remark}, NOW())")
    int insert(SpectrumComparison record);

    @Update("UPDATE spectrum_comparison SET herb_name = #{herbName}, sample_code = #{sampleCode}, district = #{district}, " +
            "spectrum_type = #{spectrumType}, reference_name = #{referenceName}, similarity = #{similarity}, " +
            "result = #{result}, operator_name = #{operatorName}, compared_at = #{comparedAt}, remark = #{remark}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int update(SpectrumComparison record);

    @Delete("DELETE FROM spectrum_comparison WHERE id = #{id}")
    int deleteById(String id);

    @Select("SELECT id, herb_name AS herbName, sample_code AS sampleCode, district, " +
            "spectrum_type AS spectrumType, reference_name AS referenceName, similarity, result, " +
            "operator_name AS operator, compared_at AS comparedAt, remark, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
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

    @Select("SELECT COUNT(*) FROM spectrum_comparison")
    int count();

    @Select("SELECT COUNT(*) > 0 FROM spectrum_comparison WHERE id = #{id}")
    boolean exists(String id);
}
