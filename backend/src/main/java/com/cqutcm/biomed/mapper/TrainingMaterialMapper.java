package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.TrainingMaterial;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface TrainingMaterialMapper extends BiomedBaseMapper<TrainingMaterial> {
    @Select("SELECT id, title, trainer_name AS trainer, audience, tracking, " +
            "created_at, updated_at FROM training_material ORDER BY created_at DESC")
    List<Map<String, Object>> findAllAsMap();

}
