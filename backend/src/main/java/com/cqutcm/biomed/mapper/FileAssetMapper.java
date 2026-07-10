package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.FileAsset;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FileAssetMapper {
    @Select("SELECT * FROM file_asset ORDER BY created_at DESC")
    List<FileAsset> findAll();

    @Select("SELECT * FROM file_asset WHERE id = #{id}")
    FileAsset findById(String id);

    @Select("SELECT * FROM file_asset WHERE category = #{category} ORDER BY created_at DESC")
    List<FileAsset> findByCategory(String category);

    @Insert("INSERT INTO file_asset (id, file_name, category, size_bytes, storage_path, created_at) " +
            "VALUES (#{id}, #{fileName}, #{category}, #{sizeBytes}, #{storagePath}, NOW())")
    int insert(FileAsset fileAsset);

    @Delete("DELETE FROM file_asset WHERE id = #{id}")
    int deleteById(String id);
}
