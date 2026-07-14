-- V14: Redesign growth_analysis for automated batch analysis
-- Idempotent: safe to run repeatedly

SET @schema_name = DATABASE();

-- Add batch_id
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'batch_id') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN batch_id VARCHAR(64) NULL COMMENT ''关联药材批次ID'' AFTER id', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

-- Add batch_name
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'batch_name') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN batch_name VARCHAR(200) NULL COMMENT ''批次名称快照'' AFTER batch_id', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

-- Add analysis_config_json
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'analysis_config_json') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN analysis_config_json JSON NULL COMMENT ''分析配置快照'' AFTER batch_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

-- Add trend_data_json
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'trend_data_json') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN trend_data_json JSON NULL COMMENT ''趋势分析结果'' AFTER analysis_config_json', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

-- Add suitability_json
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'suitability_json') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN suitability_json JSON NULL COMMENT ''适宜性偏差分析'' AFTER trend_data_json', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

-- Add record_count
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'record_count') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN record_count INT NULL COMMENT ''参与分析的数据记录数'' AFTER conclusion', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

-- Add stage_count
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'stage_count') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN stage_count INT NULL COMMENT ''覆盖的生长阶段数'' AFTER record_count', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

-- Add index for batch lookup
SET @sql = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND index_name = 'idx_ga_batch') = 0,
  'CREATE INDEX idx_ga_batch ON growth_analysis (batch_id)', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
