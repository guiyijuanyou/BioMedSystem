package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.entity.SysUser;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserMapper {
    @Select("SELECT * FROM sys_user ORDER BY created_at DESC")
    List<SysUser> findAll();

    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    SysUser findById(String id);

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser findByUsername(String username);

    @Select("SELECT u.id, u.username, u.password_hash AS password, u.display_name AS name, " +
            "u.department, u.status, COALESCE(r.code, 'student') AS role, " +
            "CASE COALESCE(r.code, 'student') WHEN 'admin' THEN '一级' WHEN 'teacher' THEN '二级' WHEN 'researcher' THEN '二级' ELSE '三级' END AS level, " +
            "u.created_at AS createdAt, u.updated_at AS updatedAt " +
            "FROM sys_user u LEFT JOIN sys_user_role ur ON ur.user_id = u.id " +
            "LEFT JOIN sys_role r ON r.id = ur.role_id ORDER BY u.created_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT r.* FROM sys_role r JOIN sys_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<Map<String, Object>> findRolesByUserId(String userId);

    @Select("SELECT COUNT(*) > 0 FROM sys_user WHERE username = #{username}")
    boolean existsByUsername(String username);

    @Insert("INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at) " +
            "VALUES (#{id}, #{username}, #{displayName}, #{passwordHash}, #{department}, #{status}, NOW())")
    int insert(SysUser user);

    @Update("UPDATE sys_user SET username = #{username}, display_name = #{displayName}, " +
            "password_hash = #{passwordHash}, department = #{department}, status = #{status}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int update(SysUser user);

    @Update("UPDATE sys_user SET password_hash = #{passwordHash}, updated_at = NOW() WHERE id = #{id}")
    int updatePasswordHash(@Param("id") String id, @Param("passwordHash") String passwordHash);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(String id);
}
