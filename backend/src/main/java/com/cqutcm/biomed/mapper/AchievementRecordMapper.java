package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.AchievementRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AchievementRecordMapper {
    @Select("SELECT * FROM achievement_record ORDER BY created_at DESC")
    List<AchievementRecord> findAll();

    @Select("SELECT id, title, owner_name AS owner, category, level_name AS level, status, " +
            "created_at AS createdAt, updated_at AS updatedAt FROM achievement_record ORDER BY created_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT * FROM achievement_record WHERE id = #{id}")
    AchievementRecord findById(String id);

    @Insert("INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at) " +
            "VALUES (#{id}, #{title}, #{ownerName}, #{category}, #{levelName}, #{status}, NOW())")
    int insert(AchievementRecord record);

    @Update("UPDATE achievement_record SET title = #{title}, owner_name = #{ownerName}, category = #{category}, " +
            "level_name = #{levelName}, status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int update(AchievementRecord record);

    @Delete("DELETE FROM achievement_record WHERE id = #{id}")
    int deleteById(String id);

    @Select("SELECT COUNT(*) FROM achievement_record")
    int count();
}
