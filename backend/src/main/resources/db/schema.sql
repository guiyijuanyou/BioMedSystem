CREATE DATABASE IF NOT EXISTS biomed DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biomed;

CREATE TABLE IF NOT EXISTS file_asset (
  id VARCHAR(64) PRIMARY KEY,
  file_name VARCHAR(255) NOT NULL,
  category VARCHAR(100),
  size_bytes BIGINT NOT NULL DEFAULT 0,
  storage_path VARCHAR(500) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_file_category (category)
);

CREATE TABLE IF NOT EXISTS sys_role (
  id VARCHAR(64) PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user (
  id VARCHAR(64) PRIMARY KEY,
  username VARCHAR(80) NOT NULL UNIQUE,
  display_name VARCHAR(100) NOT NULL,
  password_hash VARCHAR(255),
  department VARCHAR(120),
  status VARCHAR(30) NOT NULL DEFAULT 'enabled',
  phone VARCHAR(30),
  email VARCHAR(150),
  avatar_url VARCHAR(500),
  title VARCHAR(100),
  research_area VARCHAR(300),
  bio TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_user_status (status)
);

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id VARCHAR(64) NOT NULL,
  role_id VARCHAR(64) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, role_id),
  INDEX idx_ur_role (role_id)
);

CREATE TABLE IF NOT EXISTS herb (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  district VARCHAR(100),
  longitude DECIMAL(10,6),
  latitude DECIMAL(10,6),
  scale_desc VARCHAR(100),
  environment TEXT,
  trace_code VARCHAR(100),
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_herb_name (name),
  INDEX idx_herb_district (district)
);

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

CREATE TABLE IF NOT EXISTS growth_record (
  id VARCHAR(64) PRIMARY KEY,
  batch_id VARCHAR(64),
  herb_name VARCHAR(100) NOT NULL,
  district VARCHAR(100),
  temperature DECIMAL(6,2),
  humidity DECIMAL(6,2),
  soil_ph DECIMAL(5,2),
  growth_stage VARCHAR(100),
  collect_source VARCHAR(100),
  recorder_name VARCHAR(100),
  recorder_role VARCHAR(50),
  recorded_at TIMESTAMP NULL,
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_gr_herb (herb_name),
  INDEX idx_gr_batch (batch_id),
  INDEX idx_gr_district (district),
  INDEX idx_gr_recorded (recorded_at)
);

CREATE TABLE IF NOT EXISTS trace_event (
  id VARCHAR(64) PRIMARY KEY,
  batch_id VARCHAR(64),
  herb_name VARCHAR(100) NOT NULL,
  trace_code VARCHAR(100) NOT NULL,
  event_type VARCHAR(80) NOT NULL,
  event_content TEXT,
  operator_name VARCHAR(100),
  event_time TIMESTAMP NULL,
  location VARCHAR(200),
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_te_trace (trace_code),
  INDEX idx_te_batch (batch_id),
  INDEX idx_te_herb (herb_name),
  INDEX idx_te_time (event_time)
);

CREATE TABLE IF NOT EXISTS spectrum_comparison (
  id VARCHAR(64) PRIMARY KEY,
  batch_id VARCHAR(64),
  sample_id VARCHAR(64),
  herb_name VARCHAR(100) NOT NULL,
  sample_code VARCHAR(100),
  district VARCHAR(100),
  spectrum_type VARCHAR(100),
  reference_name VARCHAR(200),
  similarity DECIMAL(6,2),
  result VARCHAR(100),
  operator_name VARCHAR(100),
  compared_at TIMESTAMP NULL,
  remark TEXT,
  status VARCHAR(50) NOT NULL DEFAULT '草稿',
  reviewer_name VARCHAR(100),
  review_comment TEXT,
  reviewed_at TIMESTAMP NULL,
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_sc_herb (herb_name),
  INDEX idx_sc_batch (batch_id),
  INDEX idx_sc_sample_id (sample_id),
  INDEX idx_sc_sample (sample_code),
  INDEX idx_sc_status (status)
);

CREATE TABLE IF NOT EXISTS growth_analysis (
  id VARCHAR(64) PRIMARY KEY,
  batch_id VARCHAR(64),
  analysis_name VARCHAR(200) NOT NULL,
  herb_name VARCHAR(100),
  district VARCHAR(100),
  indicator VARCHAR(200),
  baseline VARCHAR(200),
  current_value VARCHAR(200),
  difference_desc VARCHAR(200),
  trend VARCHAR(100),
  conclusion TEXT,
  analyst_name VARCHAR(100),
  analyzed_at TIMESTAMP NULL,
  status VARCHAR(50) NOT NULL DEFAULT '草稿',
  reviewer_name VARCHAR(100),
  review_comment TEXT,
  reviewed_at TIMESTAMP NULL,
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_ga_herb (herb_name),
  INDEX idx_ga_batch (batch_id),
  INDEX idx_ga_analyzed (analyzed_at),
  INDEX idx_ga_status (status)
);

CREATE TABLE IF NOT EXISTS research_project (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  leader_name VARCHAR(100),
  requirements TEXT,
  status VARCHAR(50) NOT NULL DEFAULT '草稿',
  reviewer_name VARCHAR(100),
  review_comment TEXT,
  reviewed_at TIMESTAMP NULL,
  stage VARCHAR(100),
  transformation TEXT,
  applicant_requests TEXT,
  approved_members TEXT,
  rejected_applicants TEXT,
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_rp_status (status),
  INDEX idx_rp_leader (leader_name)
);

CREATE TABLE IF NOT EXISTS project_application (
  id VARCHAR(64) PRIMARY KEY,
  project_id VARCHAR(64) NOT NULL,
  student_name VARCHAR(100) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'pending_approval',
  apply_reason TEXT,
  review_comment TEXT,
  applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  INDEX idx_pa_project (project_id)
);

CREATE TABLE IF NOT EXISTS project_member (
  id VARCHAR(64) PRIMARY KEY,
  project_id VARCHAR(64) NOT NULL,
  member_name VARCHAR(100) NOT NULL,
  member_role VARCHAR(50) NOT NULL DEFAULT 'student',
  joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_pm_project (project_id)
);

CREATE TABLE IF NOT EXISTS course (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  teacher_name VARCHAR(100),
  hours DECIMAL(5,1),
  material_type VARCHAR(100),
  status VARCHAR(50) NOT NULL DEFAULT '草稿',
  reviewer_name VARCHAR(100),
  review_comment TEXT,
  reviewed_at TIMESTAMP NULL,
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_course_status (status)
);

CREATE TABLE IF NOT EXISTS teaching_resource (
  id VARCHAR(64) PRIMARY KEY,
  course_id VARCHAR(64),
  course_title VARCHAR(200),
  title VARCHAR(200) NOT NULL,
  resource_type VARCHAR(80) NOT NULL,
  file_id VARCHAR(64),
  video_url VARCHAR(500),
  file_url VARCHAR(500),
  uploader_name VARCHAR(100),
  uploader_role VARCHAR(50),
  status VARCHAR(50) NOT NULL DEFAULT '草稿',
  reviewer_name VARCHAR(100),
  review_comment TEXT,
  reviewed_at TIMESTAMP NULL,
  published_at TIMESTAMP NULL,
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_tr_course (course_id),
  INDEX idx_tr_status (status)
);

CREATE TABLE IF NOT EXISTS training_material (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  trainer_name VARCHAR(100),
  audience VARCHAR(200),
  tracking TEXT,
  status VARCHAR(50) NOT NULL DEFAULT '草稿',
  reviewer_name VARCHAR(100),
  review_comment TEXT,
  reviewed_at TIMESTAMP NULL,
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS evaluation_record (
  id VARCHAR(64) PRIMARY KEY,
  batch_id VARCHAR(64),
  herb_name VARCHAR(100) NOT NULL,
  indicator VARCHAR(200),
  score DECIMAL(8,2),
  result VARCHAR(200),
  application_material TEXT,
  subject_owner_name VARCHAR(100),
  evaluator_name VARCHAR(100),
  evaluator_role VARCHAR(50),
  status VARCHAR(50) NOT NULL DEFAULT '草稿',
  reviewer_name VARCHAR(100),
  review_comment TEXT,
  reviewed_at TIMESTAMP NULL,
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_er_herb (herb_name),
  INDEX idx_er_batch (batch_id),
  INDEX idx_er_status (status)
);

CREATE TABLE IF NOT EXISTS achievement_record (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  owner_name VARCHAR(100),
  category VARCHAR(100),
  level_name VARCHAR(100),
  status VARCHAR(50) NOT NULL DEFAULT '草稿',
  reviewer_name VARCHAR(100),
  review_comment TEXT,
  reviewed_at TIMESTAMP NULL,
  version INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_ar_status (status),
  INDEX idx_ar_category (category)
);

CREATE TABLE IF NOT EXISTS achievement_standard (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  category VARCHAR(100),
  level_rule TEXT,
  effective_date DATE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

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
  INDEX idx_ei_evaluation (evaluation_id), INDEX idx_ei_analysis (source_analysis_id),
  INDEX idx_ei_batch (batch_id), INDEX idx_ei_status (status)
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
  INDEX idx_itt_issue (issue_id), INDEX idx_itt_assignee (assignee_name), INDEX idx_itt_status (status)
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
  INDEX idx_ae_achievement (achievement_id), INDEX idx_ae_issue (issue_id)
);


