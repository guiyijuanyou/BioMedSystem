CREATE TABLE IF NOT EXISTS generic_record (
  id VARCHAR(64) PRIMARY KEY,
  resource_type VARCHAR(64) NOT NULL,
  payload TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS file_asset (
  id VARCHAR(64) PRIMARY KEY,
  file_name VARCHAR(255) NOT NULL,
  category VARCHAR(100),
  size_bytes BIGINT NOT NULL DEFAULT 0,
  storage_path VARCHAR(500) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
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
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id VARCHAR(64) NOT NULL,
  role_id VARCHAR(64) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, role_id)
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
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS growth_record (
  id VARCHAR(64) PRIMARY KEY,
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
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS trace_event (
  id VARCHAR(64) PRIMARY KEY,
  herb_name VARCHAR(100) NOT NULL,
  trace_code VARCHAR(100) NOT NULL,
  event_type VARCHAR(80) NOT NULL,
  event_content TEXT,
  operator_name VARCHAR(100),
  event_time TIMESTAMP NULL,
  location VARCHAR(200),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS spectrum_comparison (
  id VARCHAR(64) PRIMARY KEY,
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
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

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
  analyst_name VARCHAR(100),
  analyzed_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS research_project (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  leader_name VARCHAR(100),
  requirements TEXT,
  status VARCHAR(50) NOT NULL DEFAULT 'pending_review',
  stage VARCHAR(100),
  transformation TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS project_application (
  id VARCHAR(64) PRIMARY KEY,
  project_id VARCHAR(64) NOT NULL,
  student_name VARCHAR(100) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'pending_approval',
  apply_reason TEXT,
  review_comment TEXT,
  applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS project_member (
  id VARCHAR(64) PRIMARY KEY,
  project_id VARCHAR(64) NOT NULL,
  member_name VARCHAR(100) NOT NULL,
  member_role VARCHAR(50) NOT NULL DEFAULT 'student',
  joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS course (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  teacher_name VARCHAR(100),
  hours DECIMAL(5,1),
  material_type VARCHAR(100),
  status VARCHAR(50) NOT NULL DEFAULT 'pending_review',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
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
  status VARCHAR(50) NOT NULL DEFAULT 'pending_review',
  review_comment TEXT,
  published_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS training_material (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  trainer_name VARCHAR(100),
  audience VARCHAR(200),
  tracking TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS evaluation_record (
  id VARCHAR(64) PRIMARY KEY,
  herb_name VARCHAR(100) NOT NULL,
  indicator VARCHAR(200),
  score DECIMAL(8,2),
  result VARCHAR(200),
  application_material TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS achievement_record (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  owner_name VARCHAR(100),
  category VARCHAR(100),
  level_name VARCHAR(100),
  status VARCHAR(50) NOT NULL DEFAULT 'pending_review',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL
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


