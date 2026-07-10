package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.SysUserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {
    @Select("SELECT * FROM sys_user_role ORDER BY created_at DESC")
    List<SysUserRole> findAll();

    @Select("SELECT * FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    SysUserRole findByUserAndRole(@Param("userId") String userId, @Param("roleId") String roleId);

    @Insert("INSERT INTO sys_user_role (user_id, role_id, created_at) VALUES (#{userId}, #{roleId}, NOW())")
    int insert(@Param("userId") String userId, @Param("roleId") String roleId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(String userId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteByUserAndRole(@Param("userId") String userId, @Param("roleId") String roleId);
}
