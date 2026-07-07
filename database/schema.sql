-- BioMedSystem MySQL runtime schema
-- 适用数据库：MySQL 8.0+
-- 说明：当前后端采用 generic_record 通用业务表保存各模块数据，
-- 包括中药材、采集记录、溯源事件、图谱比对、数据分析、课程等。

CREATE DATABASE IF NOT EXISTS biomed
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE biomed;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS generic_record (
  id VARCHAR(64) PRIMARY KEY COMMENT '记录ID',
  resource_type VARCHAR(64) NOT NULL COMMENT '资源类型，如 herbs、growth-records、trace-events',
  payload JSON NOT NULL COMMENT '业务数据 JSON',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP NULL COMMENT '更新时间',
  INDEX idx_generic_record_type_created (resource_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通用业务记录表';

CREATE TABLE IF NOT EXISTS file_asset (
  id VARCHAR(64) PRIMARY KEY COMMENT '文件ID',
  file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
  category VARCHAR(100) COMMENT '文件分类',
  size_bytes BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小',
  storage_path VARCHAR(500) NOT NULL COMMENT '服务端存储路径',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  INDEX idx_file_asset_category_created (category, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='资料文件表';

-- 可选：如果要手动导入初始化示例数据，请在 MySQL 客户端中执行：
-- SOURCE backend/src/main/resources/db/data.sql;
--
-- 程序启动时也会根据 application.yml 自动执行：
-- backend/src/main/resources/db/schema.sql
-- backend/src/main/resources/db/data.sql
