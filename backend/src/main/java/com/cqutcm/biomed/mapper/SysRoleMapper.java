package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.SysRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BiomedBaseMapper<SysRole> {
    @Select("SELECT * FROM sys_role WHERE code = #{code}")
    SysRole findByCode(String code);

}
