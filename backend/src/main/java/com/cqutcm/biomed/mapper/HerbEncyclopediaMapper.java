package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.HerbEncyclopedia;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface HerbEncyclopediaMapper extends BiomedBaseMapper<HerbEncyclopedia> {
    @Select("SELECT id, name, pinyin, english_name, latin_name, category, source_desc, origin_desc, " +
            "macroscopic, quality_desc, nature_flavor, efficacy, " +
            "image_file_id, source_url, " +
            "version, created_at, updated_at FROM herb_encyclopedia ORDER BY name ASC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT * FROM herb_encyclopedia WHERE name = #{name} LIMIT 1")
    HerbEncyclopedia findByName(@Param("name") String name);
}
