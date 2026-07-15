-- V19: Create herb_encyclopedia table
-- Idempotent: safe to run repeatedly

CREATE TABLE IF NOT EXISTS herb_encyclopedia (
    id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'UUID',
    name VARCHAR(100) NOT NULL COMMENT '药材名称',
    pinyin VARCHAR(100) DEFAULT NULL COMMENT '拼音',
    english_name VARCHAR(200) DEFAULT NULL COMMENT '英文名',
    latin_name VARCHAR(200) DEFAULT NULL COMMENT '拉丁学名',
    category VARCHAR(50) DEFAULT NULL COMMENT '类别（根及根茎类/果实及种子类/全草类等）',
    source_desc TEXT DEFAULT NULL COMMENT '来源',
    origin_desc TEXT DEFAULT NULL COMMENT '产地',
    macroscopic TEXT DEFAULT NULL COMMENT '性状',
    quality_desc TEXT DEFAULT NULL COMMENT '品质',
    nature_flavor VARCHAR(200) DEFAULT NULL COMMENT '性味',
    efficacy TEXT DEFAULT NULL COMMENT '功效',
    image_file_id VARCHAR(64) DEFAULT NULL COMMENT '药材图片（关联file_asset.id）',
    source_url VARCHAR(500) DEFAULT NULL COMMENT '数据来源链接',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁',
    created_at DATETIME DEFAULT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='中药材百科';
