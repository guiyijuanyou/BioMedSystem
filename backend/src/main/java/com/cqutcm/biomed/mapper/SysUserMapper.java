package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.SysUser;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserMapper extends BiomedBaseMapper<SysUser> {
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser findByUsername(String username);

    @Select("SELECT u.id, u.username, u.display_name AS name, " +
            "u.department, u.status, u.phone, u.email, u.avatar_url, u.title, u.research_area, u.bio, " +
            "COALESCE(r.code, 'student') AS role, " +
            "CASE COALESCE(r.code, 'student') WHEN 'admin' THEN '一级' WHEN 'teacher' THEN '二级' WHEN 'researcher' THEN '二级' ELSE '三级' END AS level, " +
            "u.created_at, u.updated_at " +
            "FROM sys_user u LEFT JOIN sys_user_role ur ON ur.user_id = u.id " +
            "LEFT JOIN sys_role r ON r.id = ur.role_id ORDER BY u.created_at DESC")
    List<Map<String, Object>> findAllAsMap();

    @Select("SELECT r.* FROM sys_role r JOIN sys_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<Map<String, Object>> findRolesByUserId(String userId);

    @Select("SELECT COUNT(*) > 0 FROM sys_user WHERE username = #{username}")
    boolean existsByUsername(String username);

    @Update("UPDATE sys_user SET password_hash = #{passwordHash}, updated_at = NOW() WHERE id = #{id}")
    int updatePasswordHash(@Param("id") String id, @Param("passwordHash") String passwordHash);

    @Select("SELECT u.*, COALESCE(r.code, 'student') AS role_code, COALESCE(r.name, '学生') AS role_name " +
            "FROM sys_user u LEFT JOIN sys_user_role ur ON ur.user_id = u.id " +
            "LEFT JOIN sys_role r ON r.id = ur.role_id WHERE u.id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "displayName", column = "display_name"),
        @Result(property = "passwordHash", column = "password_hash"),
        @Result(property = "department", column = "department"),
        @Result(property = "status", column = "status"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "email", column = "email"),
        @Result(property = "avatarUrl", column = "avatar_url"),
        @Result(property = "title", column = "title"),
        @Result(property = "researchArea", column = "research_area"),
        @Result(property = "bio", column = "bio"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Map<String, Object> findByIdWithRoles(String id);

    @Update("UPDATE sys_user SET phone = #{phone}, email = #{email}, avatar_url = #{avatarUrl}, " +
            "title = #{title}, research_area = #{researchArea}, bio = #{bio}, updated_at = NOW() WHERE id = #{id}")
    int updateProfile(@Param("id") String id, @Param("phone") String phone, @Param("email") String email,
                      @Param("avatarUrl") String avatarUrl, @Param("title") String title,
                      @Param("researchArea") String researchArea, @Param("bio") String bio);
}
