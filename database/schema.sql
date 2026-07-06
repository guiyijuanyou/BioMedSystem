-- 生物医药数字信息系统数据库设计脚本
-- 适用数据库：MySQL 8.0

CREATE DATABASE IF NOT EXISTS biomed
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE biomed;

SET NAMES utf8mb4;

DROP TABLE IF EXISTS soap_exchange_log;
DROP TABLE IF EXISTS backup_log;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS file_asset;
DROP TABLE IF EXISTS achievement_review;
DROP TABLE IF EXISTS achievement;
DROP TABLE IF EXISTS classification_standard;
DROP TABLE IF EXISTS herb_evaluation_record;
DROP TABLE IF EXISTS herb_evaluation_indicator;
DROP TABLE IF EXISTS training_record;
DROP TABLE IF EXISTS training_material;
DROP TABLE IF EXISTS research_project_member;
DROP TABLE IF EXISTS research_project;
DROP TABLE IF EXISTS experiment_course_resource;
DROP TABLE IF EXISTS experiment_course;
DROP TABLE IF EXISTS spectrum_compare_record;
DROP TABLE IF EXISTS trace_event;
DROP TABLE IF EXISTS growth_record;
DROP TABLE IF EXISTS herb_distribution;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_department;
DROP TABLE IF EXISTS data_dictionary;

CREATE TABLE data_dictionary (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  dict_type VARCHAR(80) NOT NULL COMMENT '字典类型',
  dict_code VARCHAR(80) NOT NULL COMMENT '字典编码',
  dict_name VARCHAR(120) NOT NULL COMMENT '字典名称',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用，0停用',
  remark VARCHAR(255) COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_dict_type_code (dict_type, dict_code)
) COMMENT='数据字典表';

CREATE TABLE sys_department (
  id VARCHAR(64) PRIMARY KEY COMMENT '部门ID',
  parent_id VARCHAR(64) COMMENT '上级部门ID',
  name VARCHAR(120) NOT NULL COMMENT '部门名称',
  category VARCHAR(60) COMMENT '部门类型：学院、科研机构、管理部门等',
  contact_person VARCHAR(80) COMMENT '联系人',
  phone VARCHAR(40) COMMENT '联系电话',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_department_parent (parent_id)
) COMMENT='组织部门表';

CREATE TABLE sys_user (
  id VARCHAR(64) PRIMARY KEY COMMENT '用户ID',
  username VARCHAR(80) NOT NULL COMMENT '登录账号',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  real_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
  phone VARCHAR(40) COMMENT '手机号',
  email VARCHAR(120) COMMENT '邮箱',
  department_id VARCHAR(64) COMMENT '所属部门',
  level_name VARCHAR(50) NOT NULL DEFAULT '三级' COMMENT '权限层级：一级、二级、三级',
  status VARCHAR(30) NOT NULL DEFAULT '启用' COMMENT '账号状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME COMMENT '更新时间',
  UNIQUE KEY uk_user_username (username),
  KEY idx_user_department (department_id),
  CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES sys_department(id)
) COMMENT='系统用户表';

CREATE TABLE sys_role (
  id VARCHAR(64) PRIMARY KEY COMMENT '角色ID',
  role_code VARCHAR(80) NOT NULL COMMENT '角色编码',
  role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
  role_level INT NOT NULL DEFAULT 3 COMMENT '角色层级',
  remark VARCHAR(255) COMMENT '备注',
  UNIQUE KEY uk_role_code (role_code)
) COMMENT='角色表';

CREATE TABLE sys_permission (
  id VARCHAR(64) PRIMARY KEY COMMENT '权限ID',
  permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
  permission_name VARCHAR(120) NOT NULL COMMENT '权限名称',
  module_name VARCHAR(80) NOT NULL COMMENT '所属模块',
  UNIQUE KEY uk_permission_code (permission_code)
) COMMENT='权限表';

CREATE TABLE sys_user_role (
  user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
  role_id VARCHAR(64) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) COMMENT='用户角色关联表';

CREATE TABLE sys_role_permission (
  role_id VARCHAR(64) NOT NULL COMMENT '角色ID',
  permission_id VARCHAR(64) NOT NULL COMMENT '权限ID',
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
  CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id)
) COMMENT='角色权限关联表';

CREATE TABLE herb_distribution (
  id VARCHAR(64) PRIMARY KEY COMMENT '药材分布ID',
  herb_code VARCHAR(80) NOT NULL COMMENT '药材编码',
  name VARCHAR(100) NOT NULL COMMENT '药材名称',
  latin_name VARCHAR(160) COMMENT '拉丁名',
  district VARCHAR(100) NOT NULL COMMENT '重庆区县',
  town VARCHAR(100) COMMENT '乡镇',
  longitude DECIMAL(10,6) COMMENT '经度',
  latitude DECIMAL(10,6) COMMENT '纬度',
  altitude DECIMAL(10,2) COMMENT '海拔',
  planting_scale_mu DECIMAL(12,2) COMMENT '种植规模（亩）',
  environment TEXT COMMENT '生态环境',
  growth_cycle VARCHAR(100) COMMENT '生长周期',
  trace_code VARCHAR(100) COMMENT '溯源码',
  status VARCHAR(30) NOT NULL DEFAULT '正常' COMMENT '状态',
  created_by VARCHAR(64) COMMENT '创建人',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME COMMENT '更新时间',
  UNIQUE KEY uk_herb_code (herb_code),
  KEY idx_herb_name (name),
  KEY idx_herb_district (district),
  KEY idx_herb_location (longitude, latitude),
  CONSTRAINT fk_herb_created_by FOREIGN KEY (created_by) REFERENCES sys_user(id)
) COMMENT='中药材分布网络地图表';

CREATE TABLE growth_record (
  id VARCHAR(64) PRIMARY KEY COMMENT '生长数据ID',
  herb_id VARCHAR(64) NOT NULL COMMENT '药材ID',
  device_code VARCHAR(100) COMMENT '采集设备编码',
  collect_source VARCHAR(60) NOT NULL COMMENT '采集来源：手机APP、传感器网关、电脑终端',
  temperature DECIMAL(8,2) COMMENT '温度',
  humidity DECIMAL(8,2) COMMENT '空气湿度',
  soil_ph DECIMAL(8,2) COMMENT '土壤PH',
  soil_moisture DECIMAL(8,2) COMMENT '土壤湿度',
  illumination DECIMAL(10,2) COMMENT '光照强度',
  rainfall DECIMAL(10,2) COMMENT '降雨量',
  growth_stage VARCHAR(80) COMMENT '生长阶段',
  collector_id VARCHAR(64) COMMENT '采集人',
  recorded_at DATETIME NOT NULL COMMENT '采集时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  KEY idx_growth_herb_time (herb_id, recorded_at),
  KEY idx_growth_source (collect_source),
  CONSTRAINT fk_growth_herb FOREIGN KEY (herb_id) REFERENCES herb_distribution(id),
  CONSTRAINT fk_growth_collector FOREIGN KEY (collector_id) REFERENCES sys_user(id)
) COMMENT='中药材生长数据采集表';

CREATE TABLE trace_event (
  id VARCHAR(64) PRIMARY KEY COMMENT '溯源事件ID',
  herb_id VARCHAR(64) NOT NULL COMMENT '药材ID',
  trace_code VARCHAR(100) NOT NULL COMMENT '溯源码',
  event_type VARCHAR(80) NOT NULL COMMENT '事件类型：种植、采收、检测、加工、入库、教学使用',
  event_content TEXT NOT NULL COMMENT '事件内容',
  operator_id VARCHAR(64) COMMENT '操作人',
  event_time DATETIME NOT NULL COMMENT '事件时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_trace_code (trace_code),
  KEY idx_trace_herb (herb_id),
  CONSTRAINT fk_trace_herb FOREIGN KEY (herb_id) REFERENCES herb_distribution(id),
  CONSTRAINT fk_trace_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id)
) COMMENT='中药材溯源管理表';

CREATE TABLE spectrum_compare_record (
  id VARCHAR(64) PRIMARY KEY COMMENT '图谱比对ID',
  herb_id VARCHAR(64) NOT NULL COMMENT '药材ID',
  sample_no VARCHAR(100) NOT NULL COMMENT '样品编号',
  spectrum_file_id VARCHAR(64) COMMENT '图谱文件ID',
  reference_name VARCHAR(160) COMMENT '参考图谱名称',
  similarity DECIMAL(8,4) COMMENT '相似度',
  conclusion VARCHAR(255) COMMENT '比对结论',
  compared_by VARCHAR(64) COMMENT '比对人',
  compared_at DATETIME NOT NULL COMMENT '比对时间',
  KEY idx_spectrum_herb (herb_id),
  KEY idx_spectrum_sample (sample_no),
  CONSTRAINT fk_spectrum_herb FOREIGN KEY (herb_id) REFERENCES herb_distribution(id)
) COMMENT='图谱比对记录表';

CREATE TABLE experiment_course (
  id VARCHAR(64) PRIMARY KEY COMMENT '课程ID',
  title VARCHAR(200) NOT NULL COMMENT '课程名称',
  teacher_id VARCHAR(64) COMMENT '授课教师',
  course_type VARCHAR(80) COMMENT '课程类型',
  hours INT COMMENT '学时',
  summary TEXT COMMENT '课程简介',
  status VARCHAR(50) NOT NULL DEFAULT '草稿' COMMENT '状态',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME COMMENT '更新时间',
  KEY idx_course_teacher (teacher_id),
  CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
) COMMENT='中药材试验课程表';

CREATE TABLE experiment_course_resource (
  id VARCHAR(64) PRIMARY KEY COMMENT '课程资源ID',
  course_id VARCHAR(64) NOT NULL COMMENT '课程ID',
  resource_type VARCHAR(80) NOT NULL COMMENT '资源类型：视频、讲义、实验指导书',
  file_id VARCHAR(64) COMMENT '附件ID',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_course_resource_course (course_id),
  CONSTRAINT fk_course_resource_course FOREIGN KEY (course_id) REFERENCES experiment_course(id)
) COMMENT='课程资源表';

CREATE TABLE research_project (
  id VARCHAR(64) PRIMARY KEY COMMENT '课题ID',
  title VARCHAR(200) NOT NULL COMMENT '课题名称',
  leader_id VARCHAR(64) COMMENT '负责人',
  project_level VARCHAR(80) COMMENT '课题级别',
  stage VARCHAR(100) COMMENT '研究阶段',
  start_date DATE COMMENT '开始日期',
  end_date DATE COMMENT '结束日期',
  content TEXT COMMENT '课题内容',
  transformation TEXT COMMENT '成果转化',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_project_leader (leader_id),
  KEY idx_project_stage (stage),
  CONSTRAINT fk_project_leader FOREIGN KEY (leader_id) REFERENCES sys_user(id)
) COMMENT='中药材试验课题研究表';

CREATE TABLE research_project_member (
  project_id VARCHAR(64) NOT NULL COMMENT '课题ID',
  user_id VARCHAR(64) NOT NULL COMMENT '参与人ID',
  duty VARCHAR(120) COMMENT '职责',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (project_id, user_id),
  CONSTRAINT fk_project_member_project FOREIGN KEY (project_id) REFERENCES research_project(id),
  CONSTRAINT fk_project_member_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT='课题参与人员表';

CREATE TABLE training_material (
  id VARCHAR(64) PRIMARY KEY COMMENT '培训素材ID',
  title VARCHAR(200) NOT NULL COMMENT '培训主题',
  trainer_id VARCHAR(64) COMMENT '培训人',
  audience VARCHAR(200) COMMENT '培训对象',
  material_type VARCHAR(80) COMMENT '素材类型',
  content TEXT COMMENT '培训内容',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_training_trainer (trainer_id),
  CONSTRAINT fk_training_trainer FOREIGN KEY (trainer_id) REFERENCES sys_user(id)
) COMMENT='培训素材表';

CREATE TABLE training_record (
  id VARCHAR(64) PRIMARY KEY COMMENT '培训记录ID',
  training_id VARCHAR(64) NOT NULL COMMENT '培训素材ID',
  trainee_name VARCHAR(100) COMMENT '参训人员',
  sign_status VARCHAR(50) COMMENT '签到状态',
  exam_score DECIMAL(8,2) COMMENT '考核成绩',
  feedback TEXT COMMENT '反馈内容',
  tracked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '跟踪时间',
  KEY idx_training_record_training (training_id),
  CONSTRAINT fk_training_record_training FOREIGN KEY (training_id) REFERENCES training_material(id)
) COMMENT='培训过程跟踪记录表';

CREATE TABLE herb_evaluation_indicator (
  id VARCHAR(64) PRIMARY KEY COMMENT '评价指标ID',
  indicator_name VARCHAR(160) NOT NULL COMMENT '指标名称',
  indicator_type VARCHAR(80) COMMENT '指标类型',
  weight DECIMAL(8,4) NOT NULL DEFAULT 1 COMMENT '指标权重',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='中药材评价指标表';

CREATE TABLE herb_evaluation_record (
  id VARCHAR(64) PRIMARY KEY COMMENT '评价记录ID',
  herb_id VARCHAR(64) NOT NULL COMMENT '药材ID',
  evaluator_id VARCHAR(64) COMMENT '评价人',
  total_score DECIMAL(8,2) COMMENT '总评分',
  result VARCHAR(100) COMMENT '评价结果',
  process_record TEXT COMMENT '评价过程记录',
  application_material TEXT COMMENT '申报素材说明',
  evaluated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  KEY idx_evaluation_herb (herb_id),
  CONSTRAINT fk_evaluation_herb FOREIGN KEY (herb_id) REFERENCES herb_distribution(id),
  CONSTRAINT fk_evaluation_user FOREIGN KEY (evaluator_id) REFERENCES sys_user(id)
) COMMENT='中药材评价记录表';

CREATE TABLE classification_standard (
  id VARCHAR(64) PRIMARY KEY COMMENT '分类标准ID',
  name VARCHAR(200) NOT NULL COMMENT '标准名称',
  category VARCHAR(100) NOT NULL COMMENT '适用分类',
  level_name VARCHAR(100) NOT NULL COMMENT '级别名称',
  rule_content TEXT NOT NULL COMMENT '认定规则',
  effective_date DATE NOT NULL COMMENT '生效日期',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_standard_category (category, level_name)
) COMMENT='工作业绩分类分级标准表';

CREATE TABLE achievement (
  id VARCHAR(64) PRIMARY KEY COMMENT '业绩ID',
  title VARCHAR(200) NOT NULL COMMENT '业绩名称',
  owner_id VARCHAR(64) COMMENT '填报人',
  department_id VARCHAR(64) COMMENT '所属部门',
  category VARCHAR(100) COMMENT '业绩分类',
  level_name VARCHAR(100) COMMENT '认定级别',
  status VARCHAR(50) NOT NULL DEFAULT '待审核' COMMENT '审核状态',
  description TEXT COMMENT '业绩说明',
  submitted_at DATETIME COMMENT '提交时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME COMMENT '更新时间',
  KEY idx_achievement_status (status),
  KEY idx_achievement_category_level (category, level_name),
  CONSTRAINT fk_achievement_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id),
  CONSTRAINT fk_achievement_department FOREIGN KEY (department_id) REFERENCES sys_department(id)
) COMMENT='工作业绩表';

CREATE TABLE achievement_review (
  id VARCHAR(64) PRIMARY KEY COMMENT '审核记录ID',
  achievement_id VARCHAR(64) NOT NULL COMMENT '业绩ID',
  reviewer_id VARCHAR(64) COMMENT '审核人',
  review_result VARCHAR(50) NOT NULL COMMENT '审核结果',
  review_opinion TEXT COMMENT '审核意见',
  reviewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  KEY idx_review_achievement (achievement_id),
  CONSTRAINT fk_review_achievement FOREIGN KEY (achievement_id) REFERENCES achievement(id),
  CONSTRAINT fk_review_user FOREIGN KEY (reviewer_id) REFERENCES sys_user(id)
) COMMENT='业绩审核记录表';

CREATE TABLE file_asset (
  id VARCHAR(64) PRIMARY KEY COMMENT '文件ID',
  file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
  category VARCHAR(100) COMMENT '文件分类',
  biz_type VARCHAR(80) COMMENT '业务类型',
  biz_id VARCHAR(64) COMMENT '关联业务ID',
  content_type VARCHAR(120) COMMENT '文件类型',
  size_bytes BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小',
  storage_path VARCHAR(500) NOT NULL COMMENT '存储路径',
  uploader_id VARCHAR(64) COMMENT '上传人',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  KEY idx_file_biz (biz_type, biz_id),
  KEY idx_file_category (category),
  CONSTRAINT fk_file_uploader FOREIGN KEY (uploader_id) REFERENCES sys_user(id)
) COMMENT='资料文件表';

CREATE TABLE audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
  user_id VARCHAR(64) COMMENT '操作用户',
  module_name VARCHAR(100) COMMENT '模块名称',
  action_name VARCHAR(100) COMMENT '操作名称',
  target_id VARCHAR(64) COMMENT '目标数据ID',
  ip_address VARCHAR(80) COMMENT 'IP地址',
  detail TEXT COMMENT '操作详情',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  KEY idx_audit_user_time (user_id, created_at),
  KEY idx_audit_module (module_name)
) COMMENT='操作审计日志表';

CREATE TABLE backup_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '备份ID',
  backup_file VARCHAR(500) NOT NULL COMMENT '备份文件路径',
  backup_type VARCHAR(80) NOT NULL COMMENT '备份类型：自动、手动',
  status VARCHAR(50) NOT NULL COMMENT '备份状态',
  message VARCHAR(500) COMMENT '备份说明',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '备份时间'
) COMMENT='数据自动备份日志表';

CREATE TABLE soap_exchange_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '交换日志ID',
  system_name VARCHAR(120) NOT NULL COMMENT '对接系统名称',
  service_name VARCHAR(120) NOT NULL COMMENT '服务名称',
  request_xml MEDIUMTEXT COMMENT '请求报文',
  response_xml MEDIUMTEXT COMMENT '响应报文',
  status VARCHAR(50) NOT NULL COMMENT '处理状态',
  exchanged_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交换时间',
  KEY idx_soap_service_time (service_name, exchanged_at)
) COMMENT='校内SOAP系统数据交换日志表';

INSERT INTO data_dictionary (dict_type, dict_code, dict_name, sort_no) VALUES
('USER_LEVEL', 'LEVEL_1', '一级', 1),
('USER_LEVEL', 'LEVEL_2', '二级', 2),
('USER_LEVEL', 'LEVEL_3', '三级', 3),
('COLLECT_SOURCE', 'APP', '手机APP采集', 1),
('COLLECT_SOURCE', 'SENSOR', '传感器网关', 2),
('COLLECT_SOURCE', 'PC', '电脑终端录入', 3),
('ACHIEVEMENT_STATUS', 'PENDING', '待审核', 1),
('ACHIEVEMENT_STATUS', 'APPROVED', '已通过', 2),
('ACHIEVEMENT_STATUS', 'REJECTED', '已退回', 3);

INSERT INTO sys_department (id, parent_id, name, category, contact_person, phone) VALUES
('dept-001', NULL, '重庆中药学院', '学院', '张老师', '023-00000001'),
('dept-002', 'dept-001', '中药材数字信息实验室', '科研机构', '李老师', '023-00000002'),
('dept-003', NULL, '信息中心', '管理部门', '王老师', '023-00000003');

INSERT INTO sys_role (id, role_code, role_name, role_level, remark) VALUES
('role-admin', 'ADMIN', '系统管理员', 1, '系统配置、用户权限和数据维护'),
('role-teacher', 'TEACHER', '教师', 2, '课程、课题、培训、评价管理'),
('role-student', 'STUDENT', '学生', 3, '查询、学习和参与课题');

INSERT INTO sys_permission (id, permission_code, permission_name, module_name) VALUES
('perm-herb-query', 'HERB_QUERY', '中药材地图查询', '中药材地图'),
('perm-growth-create', 'GROWTH_CREATE', '生长数据采集', '生长数据'),
('perm-course-manage', 'COURSE_MANAGE', '试验课程管理', '线上教学'),
('perm-achievement-review', 'ACHIEVEMENT_REVIEW', '业绩审核', '业绩管理');

INSERT INTO sys_role_permission (role_id, permission_id) VALUES
('role-admin', 'perm-herb-query'),
('role-admin', 'perm-growth-create'),
('role-admin', 'perm-course-manage'),
('role-admin', 'perm-achievement-review'),
('role-teacher', 'perm-herb-query'),
('role-teacher', 'perm-growth-create'),
('role-teacher', 'perm-course-manage'),
('role-student', 'perm-herb-query');

INSERT INTO sys_user (id, username, password_hash, real_name, phone, email, department_id, level_name, status) VALUES
('user-admin', 'admin', '$2a$10$demo', '系统管理员', '13800000001', 'admin@example.com', 'dept-003', '一级', '启用'),
('user-teacher', 'teacher', '$2a$10$demo', '张老师', '13800000002', 'teacher@example.com', 'dept-001', '二级', '启用'),
('user-student', 'student', '$2a$10$demo', '学生用户', '13800000003', 'student@example.com', 'dept-001', '三级', '启用');

INSERT INTO sys_user_role (user_id, role_id) VALUES
('user-admin', 'role-admin'),
('user-teacher', 'role-teacher'),
('user-student', 'role-student');

INSERT INTO herb_distribution (id, herb_code, name, latin_name, district, town, longitude, latitude, altitude, planting_scale_mu, environment, growth_cycle, trace_code, created_by) VALUES
('herb-001', 'CQ-HL', '黄连', 'Coptis chinensis Franch.', '石柱县', '黄水镇', 108.120000, 30.000000, 1450.00, 3200.00, '海拔高、湿润阴凉，适合林下仿野生种植。', '多年生', 'CQ-HL-001', 'user-teacher'),
('herb-002', 'CQ-JYH', '金银花', 'Lonicera japonica Thunb.', '秀山县', '隘口镇', 109.000000, 28.450000, 620.00, 1800.00, '丘陵坡地、日照充足，排水条件较好。', '多年生藤本', 'CQ-JYH-002', 'user-teacher'),
('herb-003', 'CQ-TM', '天麻', 'Gastrodia elata Bl.', '巫溪县', '文峰镇', 109.630000, 31.400000, 1200.00, 950.00, '林下仿野生环境，腐殖质丰富。', '多年生', 'CQ-TM-003', 'user-teacher');

INSERT INTO growth_record (id, herb_id, device_code, collect_source, temperature, humidity, soil_ph, soil_moisture, illumination, rainfall, growth_stage, collector_id, recorded_at) VALUES
('growth-001', 'herb-001', 'APP-001', '手机APP采集', 19.60, 83.00, 6.20, 42.50, 1200.00, 3.20, '旺长期', 'user-student', '2026-06-06 11:00:00'),
('growth-002', 'herb-003', 'SENSOR-001', '传感器网关', 17.10, 78.00, 6.80, 39.80, 980.00, 2.10, '块茎膨大期', 'user-teacher', '2026-06-06 12:00:00');

INSERT INTO trace_event (id, herb_id, trace_code, event_type, event_content, operator_id, event_time) VALUES
('trace-001', 'herb-001', 'CQ-HL-001', '种植', '完成黄连种植地块登记与生态环境记录。', 'user-teacher', '2026-03-10 09:30:00'),
('trace-002', 'herb-001', 'CQ-HL-001', '采集', '手机APP采集温湿度、土壤PH等生长数据。', 'user-student', '2026-06-06 11:00:00');

INSERT INTO experiment_course (id, title, teacher_id, course_type, hours, summary, status) VALUES
('course-001', '中药材显微鉴定实验', 'user-teacher', '试验课程', 4, '学习中药材显微特征观察、记录和图谱比对。', '已发布');

INSERT INTO research_project (id, title, leader_id, project_level, stage, start_date, end_date, content, transformation) VALUES
('project-001', '重庆道地药材生态适应性研究', 'user-teacher', '校级重点', '数据采集中', '2026-03-01', '2026-12-31', '围绕重庆道地中药材开展生态因子、生长数据和品质评价研究。', '形成种植规范、课程素材和成果申报材料。');

INSERT INTO research_project_member (project_id, user_id, duty) VALUES
('project-001', 'user-teacher', '课题负责人'),
('project-001', 'user-student', '数据采集与整理');

INSERT INTO training_material (id, title, trainer_id, audience, material_type, content) VALUES
('training-001', '中药材规范化采收培训', 'user-teacher', '基层技术人员', '视频+讲义', '围绕采收时间、采收方法、初加工、过程留痕开展培训。');

INSERT INTO herb_evaluation_indicator (id, indicator_name, indicator_type, weight) VALUES
('indicator-001', '药材性状', '质量评价', 0.25),
('indicator-002', '有效成分含量', '质量评价', 0.35),
('indicator-003', '产地生态条件', '生态评价', 0.20),
('indicator-004', '传承工艺完整性', '申报支撑', 0.20);

INSERT INTO herb_evaluation_record (id, herb_id, evaluator_id, total_score, result, process_record, application_material) VALUES
('evaluation-001', 'herb-001', 'user-teacher', 91.00, '优秀', '完成性状、含量、产地生态和传承工艺综合评价。', '可用于非遗及品牌申报材料。');

INSERT INTO classification_standard (id, name, category, level_name, rule_content, effective_date) VALUES
('standard-001', '学校业绩分类认定办法', '教学科研业绩', '国家级', '国家级项目、奖励、成果转化按国家级认定。', '2026-06-06'),
('standard-002', '学校业绩分类认定办法', '教学科研业绩', '校级重点', '学校重点项目、重点课程、重点成果按校级重点认定。', '2026-06-06');

INSERT INTO achievement (id, title, owner_id, department_id, category, level_name, status, description, submitted_at) VALUES
('achievement-001', '黄连种植技术推广', 'user-teacher', 'dept-001', '社会服务', '校级重点', '待审核', '面向基层技术人员推广黄连规范化种植技术。', '2026-06-06 13:00:00');

INSERT INTO achievement_review (id, achievement_id, reviewer_id, review_result, review_opinion) VALUES
('review-001', 'achievement-001', 'user-admin', '待审核', '等待管理员审核认定。');

INSERT INTO herb_distribution (id, herb_code, name, latin_name, district, town, longitude, latitude, altitude, planting_scale_mu, environment, growth_cycle, trace_code, created_by) VALUES
('herb-004', 'CQ-DZ', '杜仲', 'Eucommia ulmoides Oliv.', '垫江县', '沙坪镇', 107.350000, 30.330000, 520.00, 2100.00, '低山丘陵、土层深厚、排水良好，适合杜仲规模化栽培。', '多年生乔木', 'CQ-DZ-004', 'user-teacher'),
('herb-005', 'CQ-BZ', '白术', 'Atractylodes macrocephala Koidz.', '酉阳县', '桃花源街道', 108.770000, 28.840000, 980.00, 1650.00, '温润凉爽、富含腐殖质土壤，适合根茎类药材生长。', '一年生或多年生', 'CQ-BZ-005', 'user-teacher'),
('herb-006', 'CQ-FS', '佛手', 'Citrus medica L. var. sarcodactylis Swingle', '江津区', '石门镇', 106.260000, 29.290000, 360.00, 2400.00, '气候温暖、光照充足，适合柑橘类药材种植。', '多年生灌木或小乔木', 'CQ-FS-006', 'user-teacher');

INSERT INTO growth_record (id, herb_id, device_code, collect_source, temperature, humidity, soil_ph, soil_moisture, illumination, rainfall, growth_stage, collector_id, recorded_at) VALUES
('growth-003', 'herb-004', 'APP-002', '手机APP采集', 22.40, 71.00, 6.70, 36.20, 1480.00, 0.80, '展叶期', 'user-student', '2026-06-06 13:20:00'),
('growth-004', 'herb-005', 'SENSOR-002', '传感器网关', 18.80, 82.00, 6.30, 45.10, 1020.00, 4.60, '根茎生长期', 'user-teacher', '2026-06-06 14:10:00'),
('growth-005', 'herb-006', 'PC-001', '电脑终端录入', 25.60, 68.00, 6.90, 32.80, 1850.00, 0.00, '开花坐果期', 'user-teacher', '2026-06-06 15:00:00');

INSERT INTO trace_event (id, herb_id, trace_code, event_type, event_content, operator_id, event_time) VALUES
('trace-003', 'herb-004', 'CQ-DZ-004', '检测', '完成杜仲样品胶丝特征、含水率和外观性状检测。', 'user-teacher', '2026-06-06 14:30:00'),
('trace-004', 'herb-006', 'CQ-FS-006', '加工', '记录佛手切片、干燥和包装过程，用于后续质量追溯。', 'user-teacher', '2026-06-06 16:20:00');

INSERT INTO spectrum_compare_record (id, herb_id, sample_no, spectrum_file_id, reference_name, similarity, conclusion, compared_by, compared_at) VALUES
('spectrum-001', 'herb-001', 'HL-SAMPLE-20260606', NULL, '黄连标准图谱', 0.9625, '与标准图谱匹配度高，可作为教学样本。', 'user-teacher', '2026-06-06 15:30:00'),
('spectrum-002', 'herb-006', 'FS-SAMPLE-20260606', NULL, '佛手挥发油参考图谱', 0.9342, '主要特征峰一致，建议补充批次复核。', 'user-teacher', '2026-06-06 16:40:00');

INSERT INTO experiment_course (id, title, teacher_id, course_type, hours, summary, status) VALUES
('course-002', '中药材生长数据采集实验', 'user-teacher', '试验课程', 3, '学习手机APP采集、传感器数据读取和采集数据入库流程。', '已发布'),
('course-003', '药材溯源码与图谱比对实训', 'user-teacher', '实训课程', 6, '围绕溯源码查询、图谱上传、图谱比对和结果归档开展实训。', '待审核');

INSERT INTO experiment_course_resource (id, course_id, resource_type, file_id, sort_no) VALUES
('course-resource-001', 'course-001', '实验指导书', NULL, 1),
('course-resource-002', 'course-002', '采集表模板', NULL, 1),
('course-resource-003', 'course-003', '图谱文件', NULL, 1);

INSERT INTO research_project (id, title, leader_id, project_level, stage, start_date, end_date, content, transformation) VALUES
('project-002', '中药材非遗申报评价素材建设', 'user-teacher', '院级', '评价资料整理', '2026-04-01', '2026-11-30', '围绕中药材传承工艺、评价指标和佐证材料进行资料建设。', '形成非遗申报资料库和培训案例。'),
('project-003', '佛手产地生态因子与品质关联分析', 'user-teacher', '校级重点', '数据对比分析', '2026-05-01', '2026-12-31', '采集不同地块佛手生态环境和品质检测数据，分析产地质量关联。', '建立产地品质评价模型。');

INSERT INTO research_project_member (project_id, user_id, duty) VALUES
('project-002', 'user-teacher', '评价体系设计'),
('project-002', 'user-student', '申报素材整理'),
('project-003', 'user-teacher', '数据分析'),
('project-003', 'user-student', '样本采集');

INSERT INTO training_material (id, title, trainer_id, audience, material_type, content) VALUES
('training-002', '手机APP采集填报培训', 'user-teacher', '学生与科研助理', '操作视频', '讲解手机端采集表单、定位信息、图片上传和数据审核流程。'),
('training-003', '中药材评价指标解读', 'user-teacher', '教师与培训人员', '课件+测试题', '讲解性状、含量、产地生态、传承工艺等评价指标。');

INSERT INTO training_record (id, training_id, trainee_name, sign_status, exam_score, feedback) VALUES
('training-record-001', 'training-002', '学生用户', '已签到', 88.00, '已掌握手机端采集和数据上传流程。'),
('training-record-002', 'training-003', '基层技术人员A', '已签到', 91.00, '评价指标说明清晰，建议增加更多案例。');

INSERT INTO herb_evaluation_record (id, herb_id, evaluator_id, total_score, result, process_record, application_material) VALUES
('evaluation-002', 'herb-004', 'user-teacher', 87.00, '良好', '完成树皮性状、胶丝特征、采收年限和产地环境评价。', '可用于产地证明和培训素材。'),
('evaluation-003', 'herb-006', 'user-teacher', 93.00, '优秀', '完成果形、香气、有效成分和加工工艺综合评价。', '可用于品牌建设和非遗申报。');

INSERT INTO classification_standard (id, name, category, level_name, rule_content, effective_date) VALUES
('standard-003', '学校业绩分类认定办法', '教学建设', '院级', '院级课程建设、实训平台建设和教学资源建设按院级认定。', '2026-06-06'),
('standard-004', '学校业绩分类认定办法', '社会服务', '校级重点', '服务地方产业、形成培训材料和推广成果的项目按校级重点认定。', '2026-06-06');

INSERT INTO achievement (id, title, owner_id, department_id, category, level_name, status, description, submitted_at) VALUES
('achievement-002', '中药材数字采集课程建设', 'user-teacher', 'dept-001', '教学建设', '院级', '已通过', '建设手机端采集、数据分析和图谱比对相关课程资源。', '2026-06-06 14:00:00'),
('achievement-003', '重庆道地药材评价指标库', 'user-teacher', 'dept-002', '科研成果', '校级重点', '待审核', '沉淀道地药材评价指标和非遗申报支撑材料。', '2026-06-06 15:20:00');

INSERT INTO achievement_review (id, achievement_id, reviewer_id, review_result, review_opinion) VALUES
('review-002', 'achievement-002', 'user-admin', '已通过', '材料完整，符合院级教学建设业绩认定要求。'),
('review-003', 'achievement-003', 'user-admin', '待审核', '等待补充评价指标附件和成果转化说明。');
