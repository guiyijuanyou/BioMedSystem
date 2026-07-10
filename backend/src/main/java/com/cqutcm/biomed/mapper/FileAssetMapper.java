package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.FileAsset;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FileAssetMapper extends BiomedBaseMapper<FileAsset> {
    @Select("SELECT * FROM file_asset WHERE category = #{category} ORDER BY created_at DESC")
    List<FileAsset> findByCategory(String category);

}
