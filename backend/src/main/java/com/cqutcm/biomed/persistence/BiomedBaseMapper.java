package com.cqutcm.biomed.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.io.Serializable;
import java.util.List;

public interface BiomedBaseMapper<T> extends BaseMapper<T> {

    default List<T> findAll() {
        return selectList(null);
    }

    default T findById(String id) {
        return selectById(id);
    }

    default int update(T entity) {
        return updateById(entity);
    }

    default int deleteById(String id) {
        return deleteById((Serializable) id);
    }

    default int count() {
        return Math.toIntExact(selectCount(null));
    }

    default boolean exists(String id) {
        return selectById(id) != null;
    }
}
