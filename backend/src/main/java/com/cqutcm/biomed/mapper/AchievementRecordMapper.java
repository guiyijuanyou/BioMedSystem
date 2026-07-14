package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.AchievementRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AchievementRecordMapper extends BiomedBaseMapper<AchievementRecord> {
    @Select("SELECT id, title, owner_name, source_module, batch_id, herb_name, project_title, standard_id, " +
            "category, level_name, score, evidence, status, " +
            "reviewer_name, review_comment, reviewed_at, " +
            "version, created_at, updated_at FROM achievement_record ORDER BY created_at DESC")
    List<Map<String, Object>> findAllAsMap();
}
