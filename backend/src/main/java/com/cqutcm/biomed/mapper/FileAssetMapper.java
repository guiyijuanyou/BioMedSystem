package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.FileAsset;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FileAssetMapper extends BiomedBaseMapper<FileAsset> {
    @Select("SELECT * FROM file_asset WHERE category = #{category} ORDER BY created_at DESC")
    List<FileAsset> findByCategory(String category);

    @Select("SELECT * FROM file_asset WHERE sha256 = #{sha256} AND category = #{category} ORDER BY created_at ASC LIMIT 1")
    FileAsset findBySha256AndCategory(@Param("sha256") String sha256, @Param("category") String category);

    @Select("SELECT * FROM file_asset WHERE file_name = #{fileName} AND size_bytes = #{size} AND category = #{category} ORDER BY created_at ASC LIMIT 1")
    FileAsset findByNameSizeAndCategory(@Param("fileName") String fileName, @Param("size") long size,
                                        @Param("category") String category);

}
