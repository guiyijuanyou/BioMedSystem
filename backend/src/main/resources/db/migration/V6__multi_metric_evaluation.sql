CREATE TABLE IF NOT EXISTS evaluation_scheme (
  id VARCHAR(64) PRIMARY KEY, scheme_code VARCHAR(80) NOT NULL, scheme_name VARCHAR(200) NOT NULL,
  evaluation_type VARCHAR(50) NOT NULL, herb_id VARCHAR(64), passing_score DECIMAL(8,2) NOT NULL DEFAULT 60,
  version_no INT NOT NULL DEFAULT 1, effective_date DATE, status VARCHAR(20) NOT NULL DEFAULT 'active',
  created_by VARCHAR(100), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NULL,
  UNIQUE KEY uk_es_code_version (scheme_code,version_no), INDEX idx_es_herb (herb_id), INDEX idx_es_status (status)
);
CREATE TABLE IF NOT EXISTS evaluation_scheme_item (
  id VARCHAR(64) PRIMARY KEY, scheme_id VARCHAR(64) NOT NULL, metric_definition_id VARCHAR(64) NOT NULL,
  weight_value DECIMAL(6,2) NOT NULL, minimum_item_score DECIMAL(8,2), veto_flag TINYINT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_esi_scheme_metric (scheme_id,metric_definition_id), INDEX idx_esi_scheme (scheme_id)
);
CREATE TABLE IF NOT EXISTS multi_metric_evaluation (
  id VARCHAR(64) PRIMARY KEY, scheme_id VARCHAR(64) NOT NULL, scheme_version INT NOT NULL,
  batch_id VARCHAR(64) NOT NULL, evaluation_record_id VARCHAR(64), total_score DECIMAL(8,2) NOT NULL,
  grade_name VARCHAR(30) NOT NULL, result VARCHAR(30) NOT NULL, conclusion TEXT,
  evaluator_name VARCHAR(100) NOT NULL, status VARCHAR(30) NOT NULL DEFAULT 'draft',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_mme_batch (batch_id), INDEX idx_mme_scheme (scheme_id), INDEX idx_mme_result (result)
);
CREATE TABLE IF NOT EXISTS multi_metric_evaluation_detail (
  id VARCHAR(64) PRIMARY KEY, evaluation_id VARCHAR(64) NOT NULL, metric_definition_id VARCHAR(64) NOT NULL,
  metric_code VARCHAR(80) NOT NULL, metric_name VARCHAR(200) NOT NULL, metric_version INT NOT NULL,
  measured_value DECIMAL(12,4), unit_name VARCHAR(40), judgement VARCHAR(30) NOT NULL,
  raw_score DECIMAL(8,2) NOT NULL, weight_value DECIMAL(6,2) NOT NULL, weighted_score DECIMAL(8,2) NOT NULL,
  source_result_id VARCHAR(64) NOT NULL, source_count INT NOT NULL DEFAULT 0, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_mmed_evaluation (evaluation_id), INDEX idx_mmed_metric (metric_definition_id)
);

INSERT INTO evaluation_scheme (id,scheme_code,scheme_name,evaluation_type,passing_score,version_no,status,created_by)
SELECT 'scheme-batch-quality-v1','BATCH_QUALITY','药材批次综合质量评价','quality',75,1,'active','system'
WHERE NOT EXISTS (SELECT 1 FROM evaluation_scheme WHERE scheme_code='BATCH_QUALITY' AND version_no=1);
INSERT INTO evaluation_scheme_item (id,scheme_id,metric_definition_id,weight_value,minimum_item_score,veto_flag)
SELECT CONCAT('scheme-item-',q.id),'scheme-batch-quality-v1',q.id,q.weight_value,60,0
FROM quality_metric_definition q WHERE q.id IN ('metric-temperature-avg','metric-humidity-avg','metric-soil-ph-avg','metric-spectrum-max')
AND NOT EXISTS (SELECT 1 FROM evaluation_scheme_item i WHERE i.scheme_id='scheme-batch-quality-v1' AND i.metric_definition_id=q.id);

INSERT INTO evaluation_scheme (id,scheme_code,scheme_name,evaluation_type,passing_score,version_no,status,created_by)
SELECT 'scheme-growth-env-v1','GROWTH_ENVIRONMENT','药材生长环境评价','growth',75,1,'active','system'
WHERE NOT EXISTS (SELECT 1 FROM evaluation_scheme WHERE scheme_code='GROWTH_ENVIRONMENT' AND version_no=1);
INSERT INTO evaluation_scheme_item (id,scheme_id,metric_definition_id,weight_value,minimum_item_score,veto_flag)
SELECT CONCAT('growth-item-',q.id),'scheme-growth-env-v1',q.id,
       CASE q.metric_code WHEN 'GROWTH_TEMP_AVG' THEN 34 ELSE 33 END,60,0
FROM quality_metric_definition q WHERE q.metric_code IN ('GROWTH_TEMP_AVG','GROWTH_HUMIDITY_AVG','GROWTH_SOIL_PH_AVG') AND q.version_no=1
AND NOT EXISTS (SELECT 1 FROM evaluation_scheme_item i WHERE i.scheme_id='scheme-growth-env-v1' AND i.metric_definition_id=q.id);

INSERT INTO evaluation_scheme (id,scheme_code,scheme_name,evaluation_type,passing_score,version_no,status,created_by)
SELECT 'scheme-spectrum-v1','SPECTRUM_QUALITY','药材图谱质量评价','spectrum',75,1,'active','system'
WHERE NOT EXISTS (SELECT 1 FROM evaluation_scheme WHERE scheme_code='SPECTRUM_QUALITY' AND version_no=1);
INSERT INTO evaluation_scheme_item (id,scheme_id,metric_definition_id,weight_value,minimum_item_score,veto_flag)
SELECT CONCAT('spectrum-item-',q.id),'scheme-spectrum-v1',q.id,100,60,0
FROM quality_metric_definition q WHERE q.metric_code='SPECTRUM_SIMILARITY_MAX' AND q.version_no=1
AND NOT EXISTS (SELECT 1 FROM evaluation_scheme_item i WHERE i.scheme_id='scheme-spectrum-v1' AND i.metric_definition_id=q.id);
