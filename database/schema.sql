-- BioMedSystem normalized MySQL schema
-- MySQL 5.7+ / 8.0+

CREATE DATABASE IF NOT EXISTS biomed
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE biomed;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS generic_record (
  id VARCHAR(64) PRIMARY KEY,
  resource_type VARCHAR(64) NOT NULL,
  payload JSON NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_generic_record_type_created (resource_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS sys_role (
  id VARCHAR(64) PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS sys_user (
  id VARCHAR(64) PRIMARY KEY,
  username VARCHAR(80) NOT NULL UNIQUE,
  display_name VARCHAR(100) NOT NULL,
  password_hash VARCHAR(255),
  department VARCHAR(120),
  status VARCHAR(30) NOT NULL DEFAULT 'enabled',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_sys_user_display_name (display_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id VARCHAR(64) NOT NULL,
  role_id VARCHAR(64) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE IF NOT EXISTS herb (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  district VARCHAR(100),
  longitude DECIMAL(10,6),
  latitude DECIMAL(10,6),
  scale_desc VARCHAR(100),
  environment TEXT,
  trace_code VARCHAR(100),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_herb_name (name),
  INDEX idx_herb_district (district),
  INDEX idx_herb_trace_code (trace_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS file_asset (
  id VARCHAR(64) PRIMARY KEY,
  file_name VARCHAR(255) NOT NULL,
  category VARCHAR(100),
  size_bytes BIGINT NOT NULL DEFAULT 0,
  storage_path VARCHAR(500) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_file_asset_category_created (category, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS growth_record (
  id VARCHAR(64) PRIMARY KEY,
  herb_id VARCHAR(64),
  herb_name VARCHAR(100) NOT NULL,
  district VARCHAR(100),
  temperature DECIMAL(6,2),
  humidity DECIMAL(6,2),
  soil_ph DECIMAL(5,2),
  growth_stage VARCHAR(100),
  collect_source VARCHAR(100),
  recorder_id VARCHAR(64),
  recorder_name VARCHAR(100),
  recorder_role VARCHAR(50),
  recorded_at DATETIME,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_growth_herb_time (herb_name, recorded_at),
  INDEX idx_growth_district_time (district, recorded_at),
  INDEX idx_growth_recorder (recorder_id),
  CONSTRAINT fk_growth_herb FOREIGN KEY (herb_id) REFERENCES herb(id),
  CONSTRAINT fk_growth_recorder FOREIGN KEY (recorder_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS trace_event (
  id VARCHAR(64) PRIMARY KEY,
  herb_id VARCHAR(64),
  herb_name VARCHAR(100) NOT NULL,
  trace_code VARCHAR(100) NOT NULL,
  event_type VARCHAR(80) NOT NULL,
  event_content TEXT,
  operator_id VARCHAR(64),
  operator_name VARCHAR(100),
  event_time DATETIME,
  location VARCHAR(200),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_trace_code_time (trace_code, event_time),
  INDEX idx_trace_herb (herb_name),
  CONSTRAINT fk_trace_herb FOREIGN KEY (herb_id) REFERENCES herb(id),
  CONSTRAINT fk_trace_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS course (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  teacher_id VARCHAR(64),
  teacher_name VARCHAR(100),
  hours DECIMAL(5,1),
  material_type VARCHAR(100),
  status VARCHAR(50) NOT NULL DEFAULT 'pending_review',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_course_status (status),
  INDEX idx_course_teacher (teacher_id),
  CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS teaching_resource (
  id VARCHAR(64) PRIMARY KEY,
  course_id VARCHAR(64),
  course_title VARCHAR(200),
  title VARCHAR(200) NOT NULL,
  resource_type VARCHAR(80) NOT NULL,
  file_id VARCHAR(64),
  video_url VARCHAR(500),
  file_url VARCHAR(500),
  uploader_id VARCHAR(64),
  uploader_name VARCHAR(100),
  uploader_role VARCHAR(50),
  status VARCHAR(50) NOT NULL DEFAULT 'pending_review',
  review_comment TEXT,
  published_at DATETIME,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_resource_course_status (course_id, status),
  INDEX idx_resource_uploader (uploader_id),
  CONSTRAINT fk_teaching_resource_course FOREIGN KEY (course_id) REFERENCES course(id),
  CONSTRAINT fk_teaching_resource_file FOREIGN KEY (file_id) REFERENCES file_asset(id),
  CONSTRAINT fk_teaching_resource_uploader FOREIGN KEY (uploader_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS research_project (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  leader_id VARCHAR(64),
  leader_name VARCHAR(100),
  requirements TEXT,
  status VARCHAR(50) NOT NULL DEFAULT 'pending_review',
  stage VARCHAR(100),
  transformation TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_project_status (status),
  INDEX idx_project_leader (leader_id),
  CONSTRAINT fk_project_leader FOREIGN KEY (leader_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS project_application (
  id VARCHAR(64) PRIMARY KEY,
  project_id VARCHAR(64) NOT NULL,
  student_id VARCHAR(64),
  student_name VARCHAR(100) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'pending_approval',
  apply_reason TEXT,
  review_comment TEXT,
  reviewed_by VARCHAR(64),
  applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_at DATETIME,
  INDEX idx_project_application_project_status (project_id, status),
  INDEX idx_project_application_student (student_id),
  CONSTRAINT fk_project_application_project FOREIGN KEY (project_id) REFERENCES research_project(id),
  CONSTRAINT fk_project_application_student FOREIGN KEY (student_id) REFERENCES sys_user(id),
  CONSTRAINT fk_project_application_reviewer FOREIGN KEY (reviewed_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS project_member (
  id VARCHAR(64) PRIMARY KEY,
  project_id VARCHAR(64) NOT NULL,
  user_id VARCHAR(64),
  member_name VARCHAR(100) NOT NULL,
  member_role VARCHAR(50) NOT NULL DEFAULT 'student',
  joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_project_member (project_id, member_name),
  CONSTRAINT fk_project_member_project FOREIGN KEY (project_id) REFERENCES research_project(id),
  CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS spectrum_comparison (
  id VARCHAR(64) PRIMARY KEY,
  herb_id VARCHAR(64),
  herb_name VARCHAR(100) NOT NULL,
  sample_code VARCHAR(100),
  district VARCHAR(100),
  spectrum_type VARCHAR(100),
  reference_name VARCHAR(200),
  similarity DECIMAL(6,2),
  result VARCHAR(100),
  operator_id VARCHAR(64),
  operator_name VARCHAR(100),
  compared_at DATETIME,
  remark TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_spectrum_sample (sample_code),
  INDEX idx_spectrum_herb (herb_name),
  CONSTRAINT fk_spectrum_herb FOREIGN KEY (herb_id) REFERENCES herb(id),
  CONSTRAINT fk_spectrum_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS growth_analysis (
  id VARCHAR(64) PRIMARY KEY,
  analysis_name VARCHAR(200) NOT NULL,
  herb_name VARCHAR(100),
  district VARCHAR(100),
  indicator VARCHAR(200),
  baseline VARCHAR(200),
  current_value VARCHAR(200),
  difference_desc VARCHAR(200),
  trend VARCHAR(100),
  conclusion TEXT,
  analyst_id VARCHAR(64),
  analyst_name VARCHAR(100),
  analyzed_at DATETIME,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_growth_analysis_herb (herb_name),
  INDEX idx_growth_analysis_time (analyzed_at),
  CONSTRAINT fk_growth_analysis_analyst FOREIGN KEY (analyst_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS training_material (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  trainer_id VARCHAR(64),
  trainer_name VARCHAR(100),
  audience VARCHAR(200),
  tracking TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_training_trainer (trainer_id),
  CONSTRAINT fk_training_trainer FOREIGN KEY (trainer_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS evaluation_record (
  id VARCHAR(64) PRIMARY KEY,
  herb_id VARCHAR(64),
  herb_name VARCHAR(100) NOT NULL,
  indicator VARCHAR(200),
  score DECIMAL(8,2),
  result VARCHAR(200),
  application_material TEXT,
  evaluator_id VARCHAR(64),
  evaluator_name VARCHAR(100),
  evaluated_at DATETIME,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_evaluation_herb (herb_name),
  CONSTRAINT fk_evaluation_herb FOREIGN KEY (herb_id) REFERENCES herb(id),
  CONSTRAINT fk_evaluation_evaluator FOREIGN KEY (evaluator_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS achievement_standard (
  id VARCHAR(64) PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  category VARCHAR(100),
  level_rule TEXT,
  effective_date DATE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_standard_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS achievement_record (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  owner_id VARCHAR(64),
  owner_name VARCHAR(100),
  category VARCHAR(100),
  level_name VARCHAR(100),
  status VARCHAR(50) NOT NULL DEFAULT 'pending_review',
  reviewed_by VARCHAR(64),
  reviewed_at DATETIME,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_achievement_owner (owner_id),
  INDEX idx_achievement_category_level (category, level_name),
  CONSTRAINT fk_achievement_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id),
  CONSTRAINT fk_achievement_reviewer FOREIGN KEY (reviewed_by) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS backup_job (
  id VARCHAR(64) PRIMARY KEY,
  backup_type VARCHAR(50) NOT NULL DEFAULT 'manual',
  file_path VARCHAR(500),
  status VARCHAR(50) NOT NULL DEFAULT 'success',
  message TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS soap_exchange_log (
  id VARCHAR(64) PRIMARY KEY,
  exchange_type VARCHAR(80),
  request_summary TEXT,
  response_summary TEXT,
  status VARCHAR(50),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


