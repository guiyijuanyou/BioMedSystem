package com.cqutcm.biomed.persistence;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

import java.util.Map;

public class CamelCaseMapWrapperFactory implements ObjectWrapperFactory {

    @Override
    public boolean hasWrapperFor(Object object) {
        return object instanceof Map<?, ?>;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        if (!(object instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("CamelCaseMapWrapperFactory only supports Map results");
        }
        return new MapWrapper(metaObject, (Map<String, Object>) object) {
            @Override
            public String findProperty(String name, boolean useCamelCaseMapping) {
                return useCamelCaseMapping ? toCamelCase(name) : name;
            }
        };
    }

    private String toCamelCase(String name) {
        StringBuilder result = new StringBuilder(name.length());
        boolean uppercaseNext = false;
        for (char character : name.toCharArray()) {
            if (character == '_') {
                uppercaseNext = true;
            } else if (uppercaseNext) {
                result.append(Character.toUpperCase(character));
                uppercaseNext = false;
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }
}
