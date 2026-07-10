package com.cqutcm.biomed.mapper;

import com.cqutcm.biomed.persistence.BiomedBaseMapper;

import com.cqutcm.biomed.entity.EvaluationRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EvaluationRecordMapper extends BiomedBaseMapper<EvaluationRecord> {
}
