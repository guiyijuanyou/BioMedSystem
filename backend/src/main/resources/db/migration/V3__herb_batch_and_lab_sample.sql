-- Introduce stable batch/sample identifiers without breaking legacy name fields.
-- Logical references are used during the compatibility period; services validate IDs.

SET @schema_name = DATABASE();

CREATE TABLE IF NOT EXISTS herb_batch (
  id VARCHAR(64) PRIMARY KEY,
  herb_id VARCHAR(64) NOT NULL,
  batch_code VARCHAR(100) NOT NULL UNIQUE,
  batch_name VARCHAR(200) NOT NULL,
  trace_code VARCHAR(100),
  plot_name VARCHAR(200),
  district VARCHAR(100),
  longitude DECIMAL(10,6),
  latitude DECIMAL(10,6),
  scale_desc VARCHAR(100),
  environment TEXT,
  planting_date DATE,
  expected_harvest_date DATE,
  responsible_person VARCHAR(100),
  current_stage VARCHAR(50) NOT NULL DEFAULT '未开始',
  status VARCHAR(30) NOT NULL DEFAULT 'active',
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_hb_herb (herb_id),
  INDEX idx_hb_district (district),
  INDEX idx_hb_trace (trace_code),
  INDEX idx_hb_status (status)
);

CREATE TABLE IF NOT EXISTS lab_sample (
  id VARCHAR(64) PRIMARY KEY,
  batch_id VARCHAR(64) NOT NULL,
  sample_code VARCHAR(100) NOT NULL UNIQUE,
  sample_type VARCHAR(100),
  collected_at TIMESTAMP NULL,
  collector_name VARCHAR(100),
  sample_location VARCHAR(200),
  storage_condition VARCHAR(300),
  status VARCHAR(30) NOT NULL DEFAULT 'collected',
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_ls_batch (batch_id),
  INDEX idx_ls_status (status),
  INDEX idx_ls_collected (collected_at)
);

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_record' AND column_name = 'batch_id') = 0,
  'ALTER TABLE growth_record ADD COLUMN batch_id VARCHAR(64) NULL AFTER id, ADD INDEX idx_gr_batch (batch_id)', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'trace_event' AND column_name = 'batch_id') = 0,
  'ALTER TABLE trace_event ADD COLUMN batch_id VARCHAR(64) NULL AFTER id, ADD INDEX idx_te_batch (batch_id)', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'spectrum_comparison' AND column_name = 'batch_id') = 0,
  'ALTER TABLE spectrum_comparison ADD COLUMN batch_id VARCHAR(64) NULL AFTER id, ADD INDEX idx_sc_batch (batch_id)', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'spectrum_comparison' AND column_name = 'sample_id') = 0,
  'ALTER TABLE spectrum_comparison ADD COLUMN sample_id VARCHAR(64) NULL AFTER batch_id, ADD INDEX idx_sc_sample_id (sample_id)', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'batch_id') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN batch_id VARCHAR(64) NULL AFTER id, ADD INDEX idx_ga_batch (batch_id)', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'evaluation_record' AND column_name = 'batch_id') = 0,
  'ALTER TABLE evaluation_record ADD COLUMN batch_id VARCHAR(64) NULL AFTER id, ADD INDEX idx_er_batch (batch_id)', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

-- Each legacy herb distribution record becomes one compatibility batch.
INSERT INTO herb_batch (
  id, herb_id, batch_code, batch_name, trace_code, plot_name, district,
  longitude, latitude, scale_desc, environment, current_stage, status, created_at
)
SELECT h.id, h.id, CONCAT('LEGACY-', h.id),
       CONCAT(h.name, ' / ', COALESCE(NULLIF(h.district, ''), '未填写地区'), ' 历史档案'),
       h.trace_code, COALESCE(NULLIF(h.district, ''), '历史种植点'), h.district,
       h.longitude, h.latitude, h.scale_desc, h.environment, '历史数据', 'active', h.created_at
FROM herb h
WHERE NOT EXISTS (SELECT 1 FROM herb_batch b WHERE b.id = h.id);

-- Backfill only when name/district or trace code identifies one batch unambiguously.
UPDATE growth_record g
JOIN (
  SELECT h.name, COALESCE(h.district, '') AS district_key, MIN(b.id) AS batch_id
  FROM herb h JOIN herb_batch b ON b.herb_id = h.id
  GROUP BY h.name, COALESCE(h.district, '') HAVING COUNT(*) = 1
) x ON x.name = g.herb_name AND x.district_key = COALESCE(g.district, '')
SET g.batch_id = x.batch_id
WHERE g.batch_id IS NULL;

UPDATE trace_event t
JOIN (
  SELECT trace_code, MIN(id) AS batch_id FROM herb_batch
  WHERE trace_code IS NOT NULL AND trace_code <> ''
  GROUP BY trace_code HAVING COUNT(*) = 1
) x ON x.trace_code = t.trace_code
SET t.batch_id = x.batch_id
WHERE t.batch_id IS NULL;

UPDATE spectrum_comparison s
JOIN (
  SELECT h.name, COALESCE(h.district, '') AS district_key, MIN(b.id) AS batch_id
  FROM herb h JOIN herb_batch b ON b.herb_id = h.id
  GROUP BY h.name, COALESCE(h.district, '') HAVING COUNT(*) = 1
) x ON x.name = s.herb_name AND x.district_key = COALESCE(s.district, '')
SET s.batch_id = x.batch_id
WHERE s.batch_id IS NULL;

UPDATE growth_analysis a
JOIN (
  SELECT h.name, COALESCE(h.district, '') AS district_key, MIN(b.id) AS batch_id
  FROM herb h JOIN herb_batch b ON b.herb_id = h.id
  GROUP BY h.name, COALESCE(h.district, '') HAVING COUNT(*) = 1
) x ON x.name = a.herb_name AND x.district_key = COALESCE(a.district, '')
SET a.batch_id = x.batch_id
WHERE a.batch_id IS NULL;

UPDATE evaluation_record e
JOIN (
  SELECT h.name, MIN(b.id) AS batch_id
  FROM herb h JOIN herb_batch b ON b.herb_id = h.id
  GROUP BY h.name HAVING COUNT(*) = 1
) x ON x.name = e.herb_name
SET e.batch_id = x.batch_id
WHERE e.batch_id IS NULL;

-- Existing spectrum rows become compatibility samples; shared sample codes remain shared.
INSERT INTO lab_sample (
  id, batch_id, sample_code, sample_type, collected_at, collector_name,
  sample_location, status, created_at
)
SELECT MIN(s.id), MAX(s.batch_id), s.sample_code, '历史图谱样本',
       MAX(s.compared_at), MAX(s.operator_name), MAX(s.district), 'tested', MIN(s.created_at)
FROM spectrum_comparison s
WHERE s.sample_code IS NOT NULL AND s.sample_code <> '' AND s.batch_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM lab_sample ls WHERE ls.sample_code = s.sample_code)
GROUP BY s.sample_code;

UPDATE spectrum_comparison s
JOIN lab_sample ls ON ls.sample_code = s.sample_code
SET s.sample_id = ls.id, s.batch_id = ls.batch_id
WHERE s.sample_id IS NULL;
