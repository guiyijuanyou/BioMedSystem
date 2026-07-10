package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.SysRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysRoleMapper {
    @Select("SELECT * FROM sys_role ORDER BY created_at DESC")
    List<SysRole> findAll();

    @Select("SELECT * FROM sys_role WHERE id = #{id}")
    SysRole findById(String id);

    @Select("SELECT * FROM sys_role WHERE code = #{code}")
    SysRole findByCode(String code);

    @Insert("INSERT INTO sys_role (id, code, name, description, created_at) VALUES (#{id}, #{code}, #{name}, #{description}, NOW())")
    int insert(SysRole role);

    @Update("UPDATE sys_role SET code = #{code}, name = #{name}, description = #{description} WHERE id = #{id}")
    int update(SysRole role);

    @Delete("DELETE FROM sys_role WHERE id = #{id}")
    int deleteById(String id);
}
