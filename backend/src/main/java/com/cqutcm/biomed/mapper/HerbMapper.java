package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.Herb;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HerbMapper extends BiomedBaseMapper<Herb> {
}
