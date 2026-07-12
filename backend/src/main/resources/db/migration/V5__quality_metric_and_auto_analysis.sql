CREATE TABLE IF NOT EXISTS quality_metric_definition (
  id VARCHAR(64) PRIMARY KEY,
  metric_code VARCHAR(80) NOT NULL,
  metric_name VARCHAR(200) NOT NULL,
  herb_id VARCHAR(64),
  source_type VARCHAR(40) NOT NULL,
  source_field VARCHAR(80) NOT NULL,
  unit_name VARCHAR(40),
  minimum_value DECIMAL(12,4),
  maximum_value DECIMAL(12,4),
  target_value DECIMAL(12,4),
  weight_value DECIMAL(6,2) NOT NULL DEFAULT 1,
  version_no INT NOT NULL DEFAULT 1,
  status VARCHAR(20) NOT NULL DEFAULT 'active',
  created_by VARCHAR(100),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  UNIQUE KEY uk_qmd_code_version (metric_code, version_no),
  INDEX idx_qmd_herb (herb_id), INDEX idx_qmd_status (status)
);

CREATE TABLE IF NOT EXISTS batch_metric_result (
  id VARCHAR(64) PRIMARY KEY,
  batch_id VARCHAR(64) NOT NULL,
  metric_definition_id VARCHAR(64) NOT NULL,
  metric_code VARCHAR(80) NOT NULL,
  metric_name VARCHAR(200) NOT NULL,
  measured_value DECIMAL(12,4),
  unit_name VARCHAR(40),
  judgement VARCHAR(30) NOT NULL,
  score DECIMAL(8,2),
  source_count INT NOT NULL DEFAULT 0,
  calculation_note VARCHAR(500),
  calculated_by VARCHAR(100),
  calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_bmr_batch (batch_id), INDEX idx_bmr_metric (metric_definition_id),
  INDEX idx_bmr_judgement (judgement)
);

INSERT INTO quality_metric_definition
  (id,metric_code,metric_name,source_type,source_field,unit_name,minimum_value,maximum_value,target_value,weight_value,version_no,status,created_by)
SELECT 'metric-temperature-avg','GROWTH_TEMP_AVG','平均生长温度','growth','temperature_avg','℃',15,30,22,25,1,'active','system'
WHERE NOT EXISTS (SELECT 1 FROM quality_metric_definition WHERE metric_code='GROWTH_TEMP_AVG' AND version_no=1);
INSERT INTO quality_metric_definition
  (id,metric_code,metric_name,source_type,source_field,unit_name,minimum_value,maximum_value,target_value,weight_value,version_no,status,created_by)
SELECT 'metric-humidity-avg','GROWTH_HUMIDITY_AVG','平均环境湿度','growth','humidity_avg','%',50,90,70,20,1,'active','system'
WHERE NOT EXISTS (SELECT 1 FROM quality_metric_definition WHERE metric_code='GROWTH_HUMIDITY_AVG' AND version_no=1);
INSERT INTO quality_metric_definition
  (id,metric_code,metric_name,source_type,source_field,unit_name,minimum_value,maximum_value,target_value,weight_value,version_no,status,created_by)
SELECT 'metric-soil-ph-avg','GROWTH_SOIL_PH_AVG','平均土壤pH','growth','soil_ph_avg','pH',5.5,7.5,6.5,20,1,'active','system'
WHERE NOT EXISTS (SELECT 1 FROM quality_metric_definition WHERE metric_code='GROWTH_SOIL_PH_AVG' AND version_no=1);
INSERT INTO quality_metric_definition
  (id,metric_code,metric_name,source_type,source_field,unit_name,minimum_value,maximum_value,target_value,weight_value,version_no,status,created_by)
SELECT 'metric-spectrum-max','SPECTRUM_SIMILARITY_MAX','图谱最高相似度','spectrum','similarity_max','%',90,100,98,35,1,'active','system'
WHERE NOT EXISTS (SELECT 1 FROM quality_metric_definition WHERE metric_code='SPECTRUM_SIMILARITY_MAX' AND version_no=1);
