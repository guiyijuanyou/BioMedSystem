CREATE TABLE IF NOT EXISTS training_material_tag (
  id VARCHAR(64) PRIMARY KEY, training_material_id VARCHAR(64) NOT NULL,
  metric_code VARCHAR(80), issue_type VARCHAR(50), herb_id VARCHAR(64), role_code VARCHAR(30),
  priority_value INT NOT NULL DEFAULT 50, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tmt_material (training_material_id), INDEX idx_tmt_metric (metric_code)
);
CREATE TABLE IF NOT EXISTS improvement_recommendation (
  id VARCHAR(64) PRIMARY KEY, multi_evaluation_id VARCHAR(64) NOT NULL,
  evaluation_detail_id VARCHAR(64) NOT NULL, metric_code VARCHAR(80) NOT NULL,
  issue_title VARCHAR(200) NOT NULL, issue_description TEXT, severity VARCHAR(20) NOT NULL,
  improvement_target TEXT, recommended_material_id VARCHAR(64), recommendation_reason VARCHAR(500),
  status VARCHAR(20) NOT NULL DEFAULT 'pending', created_issue_id VARCHAR(64),
  generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, handled_by VARCHAR(100), handled_at TIMESTAMP NULL,
  UNIQUE KEY uk_ir_detail (evaluation_detail_id), INDEX idx_ir_evaluation (multi_evaluation_id), INDEX idx_ir_status (status)
);
INSERT INTO training_material_tag (id,training_material_id,metric_code,issue_type,priority_value)
SELECT 'tag-collect-temp','training-002','GROWTH_TEMP_AVG','data_collection',90
WHERE NOT EXISTS(SELECT 1 FROM training_material_tag WHERE id='tag-collect-temp');
INSERT INTO training_material_tag (id,training_material_id,metric_code,issue_type,priority_value)
SELECT 'tag-harvest-humidity','training-001','GROWTH_HUMIDITY_AVG','growth_control',80
WHERE NOT EXISTS(SELECT 1 FROM training_material_tag WHERE id='tag-harvest-humidity');
INSERT INTO training_material_tag (id,training_material_id,metric_code,issue_type,priority_value)
SELECT 'tag-harvest-ph','training-001','GROWTH_SOIL_PH_AVG','growth_control',80
WHERE NOT EXISTS(SELECT 1 FROM training_material_tag WHERE id='tag-harvest-ph');
INSERT INTO training_material_tag (id,training_material_id,metric_code,issue_type,priority_value)
SELECT 'tag-eval-spectrum','training-003','SPECTRUM_SIMILARITY_MAX','quality_control',95
WHERE NOT EXISTS(SELECT 1 FROM training_material_tag WHERE id='tag-eval-spectrum');
