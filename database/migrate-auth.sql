USE biomed;
SET NAMES utf8mb4;

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
  PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO sys_role (id, code, name, description, created_at)
SELECT 'role-admin', 'admin', '管理员', '系统管理与审核', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 'role-admin');

INSERT INTO sys_role (id, code, name, description, created_at)
SELECT 'role-teacher', 'teacher', '教师', '课程教学与课题管理', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 'role-teacher');

INSERT INTO sys_role (id, code, name, description, created_at)
SELECT 'role-researcher', 'researcher', '科研人员', '科研数据与课题研究', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 'role-researcher');

INSERT INTO sys_role (id, code, name, description, created_at)
SELECT 'role-student', 'student', '学生', '课程学习与课题申请', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 'role-student');

INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-admin', 'admin', '系统管理员', '123456', '系统管理部', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-admin');

INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-teacher', 'teacher', '李老师', '123456', '教学科研部', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-teacher');

INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-researcher', 'researcher', '王老师', '123456', '中药材科研中心', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-researcher');

INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-student', 'student', '当前学生', '123456', '生物医药学院', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-student');

INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-admin', 'role-admin', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-admin' AND role_id = 'role-admin');

INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-teacher', 'role-teacher', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-teacher' AND role_id = 'role-teacher');

INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-researcher', 'role-researcher', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-researcher' AND role_id = 'role-researcher');

INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-student', 'role-student', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-student' AND role_id = 'role-student');
