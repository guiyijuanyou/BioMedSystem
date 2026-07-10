package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.AchievementStandard;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AchievementStandardMapper {
    @Select("SELECT * FROM achievement_standard ORDER BY created_at DESC")
    List<AchievementStandard> findAll();

    @Select("SELECT * FROM achievement_standard WHERE id = #{id}")
    AchievementStandard findById(String id);

    @Insert("INSERT INTO achievement_standard (id, name, category, level_rule, effective_date, created_at) " +
            "VALUES (#{id}, #{name}, #{category}, #{levelRule}, #{effectiveDate}, NOW())")
    int insert(AchievementStandard standard);

    @Update("UPDATE achievement_standard SET name = #{name}, category = #{category}, level_rule = #{levelRule}, " +
            "effective_date = #{effectiveDate}, updated_at = NOW() WHERE id = #{id}")
    int update(AchievementStandard standard);

    @Delete("DELETE FROM achievement_standard WHERE id = #{id}")
    int deleteById(String id);

    @Select("SELECT COUNT(*) FROM ${tableName}")
    int countByTable(String tableName);
}
