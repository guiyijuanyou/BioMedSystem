-- ============================================================
-- 合并 V19(有用部分) + V21 + V22，跳过了无用的 V20
-- 所有语句幂等，可重复执行
-- ============================================================

SET @schema_name = DATABASE();

-- ── V22: spectrum_comparison 加 sha256 + file_name ──────────
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = @schema_name AND table_name = 'spectrum_comparison' AND column_name = 'sha256') = 0,
    'ALTER TABLE spectrum_comparison ADD COLUMN sha256 CHAR(64)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = @schema_name AND table_name = 'spectrum_comparison' AND column_name = 'file_name') = 0,
    'ALTER TABLE spectrum_comparison ADD COLUMN file_name VARCHAR(255)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = @schema_name AND table_name = 'spectrum_comparison' AND index_name = 'idx_sc_sha256') = 0,
    'ALTER TABLE spectrum_comparison ADD INDEX idx_sc_sha256 (sha256)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ── V19(有用): quality_metric_definition herb_id NOT NULL + 唯一索引修正 ──
SET @sql = IF(
    (SELECT IS_NULLABLE FROM information_schema.columns
     WHERE table_schema = @schema_name AND table_name = 'quality_metric_definition' AND column_name = 'herb_id') = 'YES',
    'ALTER TABLE quality_metric_definition MODIFY herb_id VARCHAR(64) NOT NULL',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删除旧唯一索引（如果存在）
SET @idx_exists = (SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = @schema_name AND table_name = 'quality_metric_definition' AND index_name = 'uk_qmd_code_version');
SET @sql_drop = IF(@idx_exists > 0, 'ALTER TABLE quality_metric_definition DROP INDEX uk_qmd_code_version', 'SELECT 1');
PREPARE stmt_drop FROM @sql_drop; EXECUTE stmt_drop; DEALLOCATE PREPARE stmt_drop;

-- 新建唯一索引 (metric_code, herb_id, version_no)
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = @schema_name AND table_name = 'quality_metric_definition' AND index_name = 'uk_qmd_code_herb_version') = 0,
    'ALTER TABLE quality_metric_definition ADD UNIQUE KEY uk_qmd_code_herb_version (metric_code, herb_id, version_no)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ── V21: 重构评价方案 ─────────────────────────────────────────
-- 清空旧方案数据（V20 插入的无用数据一并清除）
DELETE FROM evaluation_scheme_item;
DELETE FROM evaluation_scheme;

-- 重建 evaluation_scheme_item（简化为纯指标选择器）
DROP TABLE IF EXISTS evaluation_scheme_item;
CREATE TABLE evaluation_scheme_item (
  id VARCHAR(64) PRIMARY KEY,
  scheme_id VARCHAR(64) NOT NULL,
  metric_code VARCHAR(80) NOT NULL,
  INDEX idx_esi_scheme (scheme_id),
  UNIQUE KEY uk_esi_scheme_code (scheme_id, metric_code)
);

-- 插入 3 套评价方案（幂等：先删后插）
DELETE FROM evaluation_scheme WHERE id IN ('scheme-growth-v1', 'scheme-spectrum-v1', 'scheme-full-v1');
INSERT INTO evaluation_scheme (id, scheme_code, scheme_name, evaluation_type, passing_score, version_no, status, created_by) VALUES
('scheme-growth-v1',   'GROWTH_ENVIRONMENT', '药材生长环境评价',   'growth',   70, 1, 'active', 'system'),
('scheme-spectrum-v1', 'SPECTRUM_QUALITY',   '药材图谱质量评价',   'spectrum', 80, 1, 'active', 'system'),
('scheme-full-v1',     'FULL_QUALITY',       '药材综合质量评价',   'full',     70, 1, 'active', 'system');

-- 插入方案指标项
INSERT IGNORE INTO evaluation_scheme_item (id, scheme_id, metric_code) VALUES
('si-g-1', 'scheme-growth-v1',   'GROWTH_TEMP_AVG'),
('si-g-2', 'scheme-growth-v1',   'GROWTH_HUMIDITY_AVG'),
('si-g-3', 'scheme-growth-v1',   'GROWTH_SOIL_PH_AVG'),
('si-s-1', 'scheme-spectrum-v1', 'SPECTRUM_SIMILARITY_MAX'),
('si-f-1', 'scheme-full-v1',     'GROWTH_TEMP_AVG'),
('si-f-2', 'scheme-full-v1',     'GROWTH_HUMIDITY_AVG'),
('si-f-3', 'scheme-full-v1',     'GROWTH_SOIL_PH_AVG'),
('si-f-4', 'scheme-full-v1',     'SPECTRUM_SIMILARITY_MAX');
