package com.cqutcm.biomed.persistence;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CamelCaseMapWrapperFactoryTest {

    @Test
    void convertsUnderscoreColumnNamesToCamelCaseProperties() {
        CamelCaseMapWrapperFactory factory = new CamelCaseMapWrapperFactory();
        Map<String, Object> result = new LinkedHashMap<>();
        MetaObject metaObject = SystemMetaObject.forObject(result);

        assertTrue(factory.hasWrapperFor(result));
        ObjectWrapper wrapper = factory.getWrapperFor(metaObject, result);
        assertEquals("createdAt", wrapper.findProperty("created_at", true));
        assertEquals("herbName", wrapper.findProperty("herb_name", true));
        assertEquals("created_at", wrapper.findProperty("created_at", false));
    }

    @Test
    void rejectsNonMapObjects() {
        CamelCaseMapWrapperFactory factory = new CamelCaseMapWrapperFactory();
        assertFalse(factory.hasWrapperFor("not-a-map"));
        assertThrows(IllegalArgumentException.class,
                () -> factory.getWrapperFor(SystemMetaObject.forObject("not-a-map"), "not-a-map"));
    }
}
