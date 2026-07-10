package com.cqutcm.biomed.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.cqutcm.biomed.persistence.CamelCaseMapWrapperFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class MyBatisPlusConfig implements MetaObjectHandler {

    @Bean
    public ConfigurationCustomizer mapResultCustomizer() {
        return configuration -> configuration.setObjectWrapperFactory(new CamelCaseMapWrapperFactory());
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
