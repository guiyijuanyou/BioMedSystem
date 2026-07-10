package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.TrainingMaterial;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface TrainingMaterialMapper {
    @Select("SELECT * FROM training_material ORDER BY created_at DESC")
    List<TrainingMaterial> findAll();

    @Select("SELECT id, title, trainer_name AS trainer, audience, tracking, " +
            "created_at AS createdAt, updated_at AS updatedAt FROM training_material ORDER BY created_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT * FROM training_material WHERE id = #{id}")
    TrainingMaterial findById(String id);

    @Insert("INSERT INTO training_material (id, title, trainer_name, audience, tracking, created_at) " +
            "VALUES (#{id}, #{title}, #{trainerName}, #{audience}, #{tracking}, NOW())")
    int insert(TrainingMaterial material);

    @Update("UPDATE training_material SET title = #{title}, trainer_name = #{trainerName}, audience = #{audience}, " +
            "tracking = #{tracking}, updated_at = NOW() WHERE id = #{id}")
    int update(TrainingMaterial material);

    @Delete("DELETE FROM training_material WHERE id = #{id}")
    int deleteById(String id);

    @Select("SELECT COUNT(*) FROM training_material")
    int count();
}
