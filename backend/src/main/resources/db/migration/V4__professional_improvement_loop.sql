-- Connect analysis, evaluation, corrective training, re-evaluation and achievements.
CREATE TABLE IF NOT EXISTS evaluation_issue (
  id VARCHAR(64) PRIMARY KEY,
  evaluation_id VARCHAR(64) NOT NULL,
  source_analysis_id VARCHAR(64),
  batch_id VARCHAR(64),
  title VARCHAR(200) NOT NULL,
  description TEXT,
  severity VARCHAR(20) NOT NULL DEFAULT 'medium',
  improvement_target TEXT,
  status VARCHAR(30) NOT NULL DEFAULT 'open',
  recheck_evaluation_id VARCHAR(64),
  creator_name VARCHAR(100) NOT NULL,
  closed_by VARCHAR(100),
  closed_at TIMESTAMP NULL,
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_ei_evaluation (evaluation_id),
  INDEX idx_ei_analysis (source_analysis_id),
  INDEX idx_ei_batch (batch_id),
  INDEX idx_ei_status (status)
);

CREATE TABLE IF NOT EXISTS improvement_training_task (
  id VARCHAR(64) PRIMARY KEY,
  issue_id VARCHAR(64) NOT NULL,
  training_material_id VARCHAR(64),
  title VARCHAR(200) NOT NULL,
  assignee_name VARCHAR(100) NOT NULL,
  due_date DATE,
  completion_note TEXT,
  pre_score DECIMAL(8,2),
  post_score DECIMAL(8,2),
  status VARCHAR(30) NOT NULL DEFAULT 'pending',
  creator_name VARCHAR(100) NOT NULL,
  completed_at TIMESTAMP NULL,
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_itt_issue (issue_id),
  INDEX idx_itt_assignee (assignee_name),
  INDEX idx_itt_status (status)
);

CREATE TABLE IF NOT EXISTS achievement_evidence (
  id VARCHAR(64) PRIMARY KEY,
  achievement_id VARCHAR(64) NOT NULL,
  issue_id VARCHAR(64),
  source_type VARCHAR(30) NOT NULL,
  source_id VARCHAR(64) NOT NULL,
  evidence_title VARCHAR(200) NOT NULL,
  evidence_snapshot TEXT,
  creator_name VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ae_source (achievement_id, source_type, source_id),
  INDEX idx_ae_achievement (achievement_id),
  INDEX idx_ae_issue (issue_id)
);
