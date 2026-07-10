package com.cqutcm.biomed.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class MyBatisPlusPersistenceStructureTest {

    private static final String TABLE_NAME = "com.baomidou.mybatisplus.annotation.TableName";
    private static final String TABLE_ID = "com.baomidou.mybatisplus.annotation.TableId";
    private static final Pattern SIMPLE_CAMEL_CASE_ALIAS = Pattern.compile(
            "\b([a-z][a-z0-9_]*_[a-z0-9_]+)\s+AS\s+([a-z][A-Za-z0-9]*)\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final List<String> ENTITIES = List.of(
            "AchievementRecord", "AchievementStandard", "Course", "EvaluationRecord",
            "FileAsset", "GrowthAnalysis", "GrowthRecord", "Herb", "ProjectApplication",
            "ProjectMember", "ResearchProject", "SpectrumComparison", "SysRole", "SysUser",
            "SysUserRole", "TeachingResource", "TraceEvent", "TrainingMaterial"
    );

    private static final List<String> COMMON_MAPPERS = List.of(
            "AchievementRecordMapper", "AchievementStandardMapper", "CourseMapper",
            "EvaluationRecordMapper", "FileAssetMapper", "GrowthAnalysisMapper",
            "GrowthRecordMapper", "HerbMapper", "ProjectApplicationMapper",
            "ProjectMemberMapper", "ResearchProjectMapper", "SpectrumComparisonMapper",
            "SysRoleMapper", "SysUserMapper", "TeachingResourceMapper", "TraceEventMapper",
            "TrainingMaterialMapper"
    );

    @Test
    void entitiesDeclareMyBatisPlusTableMetadata() throws Exception {
        Class<?> tableName = Class.forName(TABLE_NAME);

        for (String entityName : ENTITIES) {
            Class<?> entity = entityClass(entityName);
            assertNotNull(entity.getAnnotation(tableName.asSubclass(java.lang.annotation.Annotation.class)),
                    entityName + " should declare @TableName");
            assertTrue(hasAnnotatedField(entity, TABLE_ID), entityName + " should declare @TableId");
        }
    }

    @Test
    void commonMappersUseBaseMapperWithoutRedeclaringStandardCrud() throws Exception {
        for (String mapperName : COMMON_MAPPERS) {
            Class<?> mapper = mapperClass(mapperName);
            assertTrue(BaseMapper.class.isAssignableFrom(mapper), mapperName + " should extend BaseMapper");
            assertFalse(declaresStandardCrud(mapper), mapperName + " should not redeclare standard CRUD");
        }
    }

    @Test
    void compositeKeyMapperDoesNotExposeSingleIdBaseMapperApi() throws Exception {
        Class<?> mapper = mapperClass("SysUserRoleMapper");
        assertFalse(BaseMapper.class.isAssignableFrom(mapper));
        assertThrows(NoSuchMethodException.class, () -> mapper.getMethod("findById", String.class));
        assertThrows(NoSuchMethodException.class, () -> mapper.getMethod("deleteById", String.class));
        assertThrows(NoSuchMethodException.class, () -> mapper.getMethod("exists", String.class));
    }

    @Test
    void mapQueriesDoNotRepeatSnakeCaseToCamelCaseAliases() throws Exception {
        for (String mapperName : COMMON_MAPPERS) {
            for (Method method : mapperClass(mapperName).getDeclaredMethods()) {
                Select select = method.getAnnotation(Select.class);
                if (select == null) continue;
                String sql = String.join(" ", select.value());
                Matcher matcher = SIMPLE_CAMEL_CASE_ALIAS.matcher(sql);
                while (matcher.find()) {
                    String column = matcher.group(1);
                    String alias = matcher.group(2);
                    assertNotEquals(toCamelCase(column), alias,
                            mapperName + "." + method.getName() + " repeats automatic camel-case alias");
                }
            }
        }
    }

    private boolean declaresStandardCrud(Class<?> mapper) {
        for (Method method : mapper.getDeclaredMethods()) {
            String name = method.getName();
            int parameters = method.getParameterCount();
            if ((name.equals("findAll") || name.equals("count")) && parameters == 0) return true;
            if ((name.equals("findById") || name.equals("deleteById") || name.equals("exists"))
                    && parameters == 1 && method.getParameterTypes()[0] == String.class) return true;
            if ((name.equals("insert") || name.equals("update")) && parameters == 1
                    && !java.util.Map.class.isAssignableFrom(method.getParameterTypes()[0])) return true;
        }
        return false;
    }

    private Class<?> entityClass(String name) throws ClassNotFoundException {
        return Class.forName("com.cqutcm.biomed.entity." + name);
    }

    private Class<?> mapperClass(String name) throws ClassNotFoundException {
        return Class.forName("com.cqutcm.biomed.mapper." + name);
    }

    private boolean hasAnnotatedField(Class<?> type, String annotationClassName) throws Exception {
        Class<?> annotationType = Class.forName(annotationClassName);
        return java.util.Arrays.stream(type.getDeclaredFields())
                .anyMatch(field -> field.getAnnotation(
                        annotationType.asSubclass(java.lang.annotation.Annotation.class)) != null);
    }

    private String toCamelCase(String column) {
        StringBuilder result = new StringBuilder();
        boolean uppercaseNext = false;
        for (char character : column.toCharArray()) {
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
