INSERT INTO sys_role (id, code, name, description, created_at) SELECT 'role-admin', 'admin', '管理员', '系统管理与审核', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 'role-admin');
INSERT INTO sys_role (id, code, name, description, created_at) SELECT 'role-teacher', 'teacher', '教师', '课程教学与课题管理', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 'role-teacher');
INSERT INTO sys_role (id, code, name, description, created_at) SELECT 'role-researcher', 'researcher', '科研人员', '科研数据与课题研究', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 'role-researcher');
INSERT INTO sys_role (id, code, name, description, created_at) SELECT 'role-student', 'student', '学生', '课程学习与课题申请', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 'role-student');

-- Passwords are initialized by DataInitializer (set to NULL here)
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at) SELECT 'user-admin', 'admin@cqutcm', '系统管理员', NULL, '系统管理部', 'enabled', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-admin');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at) SELECT 'user-teacher', 'teacher@cqutcm', '李老师', NULL, '教学科研部', 'enabled', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-teacher');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at) SELECT 'user-researcher', 'researcher@cqutcm', '王老师', NULL, '中药材科研中心', 'enabled', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-researcher');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at) SELECT 'user-student', 'student@cqutcm', '当前学生', NULL, '生物医药学院', 'enabled', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-student');

INSERT INTO sys_user_role (user_id, role_id, created_at) SELECT 'user-admin', 'role-admin', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-admin' AND role_id = 'role-admin');
INSERT INTO sys_user_role (user_id, role_id, created_at) SELECT 'user-teacher', 'role-teacher', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-teacher' AND role_id = 'role-teacher');
INSERT INTO sys_user_role (user_id, role_id, created_at) SELECT 'user-researcher', 'role-researcher', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-researcher' AND role_id = 'role-researcher');
INSERT INTO sys_user_role (user_id, role_id, created_at) SELECT 'user-student', 'role-student', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-student' AND role_id = 'role-student');

-- ==================== herb ====================
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-001', '黄连', '石柱县', 108.120000, 30.000000, '3200亩', '海拔高、湿润阴凉', 'CQ-HL-001', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-001');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-002', '金银花', '秀山县', 109.000000, 28.450000, '1800亩', '丘陵坡地、日照充足', 'CQ-JYH-002', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-002');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-003', '天麻', '巫溪县', 109.630000, 31.400000, '950亩', '林下仿野生种植', 'CQ-TM-003', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-003');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-004', '杜仲', '垫江县', 107.350000, 30.330000, '2100亩', '低山丘陵、土层深厚、排水良好', 'CQ-DZ-004', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-004');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-005', '白术', '酉阳县', 108.770000, 28.840000, '1650亩', '温润凉爽、富含腐殖质土壤', 'CQ-BZ-005', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-005');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-006', '佛手', '江津区', 106.260000, 29.290000, '2400亩', '气候温暖、光照充足、适合柑橘类药材', 'CQ-FS-006', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-006');

-- ==================== growth_record ====================
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-001', '黄连', '石柱县', 19.6, 83, 6.2, '展叶期', '手机APP采集', '当前学生', 'student', '2026-06-06 11:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-001');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-002', '天麻', '巫溪县', 17.1, 78, 6.8, '萌芽期', '传感器网关', '李老师', 'teacher', '2026-06-06 12:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-002');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-003', '杜仲', '垫江县', 22.4, 71, 6.7, '生长期', '手机APP采集', '当前学生', 'student', '2026-06-06 13:20:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-003');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-004', '白术', '酉阳县', 18.8, 82, 6.3, '展叶期', '传感器网关', '王老师', 'researcher', '2026-06-06 14:10:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-004');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-005', '佛手', '江津区', 25.6, 68, 6.9, '旺长期', '电脑终端录入', '李老师', 'teacher', '2026-06-06 15:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-005');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-006', '黄连', '石柱县', 18.9, 86, 6.1, '萌芽期', '传感器网关', '李老师', 'teacher', '2026-06-01 09:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-006');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-007', '黄连', '石柱县', 19.4, 84, 6.2, '展叶期', '手机APP采集', '当前学生', 'student', '2026-06-08 09:20:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-007');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-008', '黄连', '石柱县', 20.2, 82, 6.3, '生长期', '传感器网关', '李老师', 'teacher', '2026-06-15 09:15:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-008');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-009', '黄连', '石柱县', 20.7, 80, 6.4, '旺长期', '电脑终端录入', '张老师', 'teacher', '2026-06-22 10:05:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-009');

-- ==================== course ====================
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-001', '中药材显微鉴定实验', '张老师', 4, '视频+讲义', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-001');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-002', '中药材生长数据采集实验', '李老师', 6, '视频+数据模板', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-002');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-003', '药材溯源码与图谱比对实训', '王老师', 3, '图谱文件+案例', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-003');

-- ==================== teaching_resource ====================
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-001', 'course-002', '中药材生长数据采集实验', '黄连生长数据采集实验视频', '教学视频', NULL, 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '', '李老师', '教师', '已发布', '内容完整，已发布至学生课程学习。', '2026-06-08 09:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-001');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-002', 'course-003', '药材溯源码与图谱比对实训', '佛手样本图谱比对资料包', '图谱文件', NULL, '', '', '王老师', '科研人员', '待审核', '等待管理员审核图谱文件与说明文档。', NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-002');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-003', NULL, '中药材规范化采收培训', '中药材规范化采收培训课件', '课件文档', NULL, '', '', '张老师', '教师', '已驳回', '缺少封面和实验安全说明，补充后重新提交。', NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-003');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-004', 'course-001', '中药材显微鉴定实验', '显微鉴定实验操作演示', '教学视频', NULL, 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '', '张老师', '教师', '已发布', '显微制片和观察步骤清晰，已发布给学生。', '2026-06-10 10:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-004');

-- ==================== trace_event ====================
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-001', '黄连', 'CQ-HL-001', '种植', '完成黄连种植地块登记，记录海拔、遮阴、土壤湿度等基础生态信息。', '张老师', '2026-03-10 09:30:00', '石柱县黄水镇', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-001');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-002', '黄连', 'CQ-HL-001', '采集', '通过手机 APP 采集温湿度、土壤 PH 和生长阶段数据，并绑定同一溯源码。', '学生用户', '2026-06-06 11:00:00', '石柱县黄水镇', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-002');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-003', '黄连', 'CQ-HL-001', '检测', '完成黄连样本 HPLC 指纹图谱比对，相似度较高，可作为教学样本归档。', '李老师', '2026-06-06 16:00:00', '中药材数字信息实验室', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-003');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-004', '杜仲', 'CQ-DZ-004', '检测', '完成杜仲样品胶丝特征、含水率和外观性状检测，结果用于产地质量证明。', '王老师', '2026-06-06 14:30:00', '垫江县沙坪镇', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-004');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-005', '佛手', 'CQ-FS-006', '加工', '记录佛手切片、干燥和包装过程，形成后续质量追溯节点。', '王老师', '2026-06-06 16:20:00', '江津区石门镇', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-005');

-- ==================== spectrum_comparison (图谱比对) ====================
INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at)
SELECT 'spectrum-compare-001', '黄连', 'HL-SZ-20260601', '石柱县', 'HPLC 指纹图谱', '重庆黄连标准图谱 V1', 94.6, '通过', '张老师', '2026-06-06 16:00:00', '主峰保留时间稳定，特征峰匹配度高', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spectrum-compare-001');
INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at)
SELECT 'spectrum-compare-002', '金银花', 'JYH-XS-20260602', '秀山县', '薄层色谱图谱', '金银花薄层鉴别标准图谱', 88.2, '建议复核', '李老师', '2026-06-07 10:30:00', '局部斑点颜色偏浅，建议补充复测', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spectrum-compare-002');
INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at)
SELECT 'spectrum-compare-003', '天麻', 'TM-WX-20260603', '巫溪县', '红外图谱', '天麻红外标准图谱', 91.3, '通过', '王老师', '2026-06-08 14:20:00', '样本图谱与参考图谱整体一致', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spectrum-compare-003');

-- ==================== growth_analysis (生长数据分析) ====================
INSERT INTO growth_analysis (id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, created_at)
SELECT 'growth-analysis-001', '石柱黄连六月生长趋势分析', '黄连', '石柱县', '温度、湿度、土壤PH', '2026-06-01 首次记录', '2026-06-22 最新记录', '温度 +1.8，湿度 -6，PH +0.3', '温度上升、湿度下降、PH稳定', '黄连处于旺长期，温湿度变化仍在适宜范围内，建议保持遮阴与土壤保湿。', '李老师', '2026-06-22 17:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'growth-analysis-001');
INSERT INTO growth_analysis (id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, created_at)
SELECT 'growth-analysis-002', '秀山金银花花蕾期环境对比', '金银花', '秀山县', '温度、湿度', '2026-06-02 抽枝期', '2026-06-16 花蕾期', '温度 +2.1，湿度 -5', '温度上升、湿度下降', '花蕾期温度升高较明显，应关注连续高温对花蕾质量的影响。', '王老师', '2026-06-16 16:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'growth-analysis-002');

-- ==================== research_project (研究课题) ====================
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, rejected_applicants, created_at)
SELECT 'project-001', '重庆道地药材生态适应性研究', '李老师', '招募2名学生，要求能参与野外采样、掌握基础数据录入，按周提交采集记录。', '已发布', '数据采集中', '种植规范转化', '当前学生', '', '', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'project-001');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, rejected_applicants, created_at)
SELECT 'project-002', '中药材非遗申报评价素材建设', '张老师', '招募熟悉文献整理和影像资料归档的学生，需完成评价素材标注与过程记录。', '已发布', '评价资料整理', '形成非遗申报资料库', '', '学生A', '学生B', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'project-002');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, rejected_applicants, created_at)
SELECT 'project-003', '佛手产地生态因子与品质关联分析', '王老师', '招募具备图谱比对或统计分析基础的学生，参与样本数据清洗和品质模型验证。', '待审核', '数据对比分析', '建立产地品质评价模型', '', '', '', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'project-003');

-- ==================== project_application (课题申请) ====================
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'project-001-apply-current', 'project-001', '当前学生', '待审批', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'project-001-apply-current');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'project-002-reject-b', 'project-002', '学生B', '已拒绝', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'project-002-reject-b');

-- ==================== project_member (课题成员) ====================
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'project-002-member-a', 'project-002', '学生A', '学生', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'project-002-member-a');

-- ==================== training_material (培训素材) ====================
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, created_at)
SELECT 'training-001', '中药材规范化采收培训', '王老师', '基层技术人员', '签到、视频、考核记录完整', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'training-001');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, created_at)
SELECT 'training-002', '手机APP采集填报培训', '李老师', '学生与科研助理', '完成线上签到、实地采集和数据回传', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'training-002');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, created_at)
SELECT 'training-003', '中药材评价指标解读', '张老师', '教师与培训人员', '已完成课件存储、试题考核和结果归档', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'training-003');

-- ==================== evaluation_record (评价记录) ====================
INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, created_at)
SELECT 'evaluation-001', '黄连', '性状、含量、产地生态、传承工艺', 91, '优秀', '可用于非遗及品牌申报材料', '黄连课题组', '张老师', '教师', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'evaluation-001');
INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, created_at)
SELECT 'evaluation-002', '杜仲', '树皮性状、胶丝特征、采收年限、产地环境', 87, '良好', '可用于产地证明和培训素材', '杜仲课题组', '李老师', '教师', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'evaluation-002');
INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, created_at)
SELECT 'evaluation-003', '佛手', '果形、香气、有效成分、加工工艺', 93, '优秀', '可用于品牌建设和非遗申报', '佛手课题组', '王老师', '科研人员', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'evaluation-003');

-- ==================== achievement_record (业绩记录) ====================
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at)
SELECT 'achievement-001', '黄连种植技术推广', '中药学院', '社会服务', '校级重点', '待审核', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achievement-001');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at)
SELECT 'achievement-002', '中药材数字采集课程建设', '中药学院', '教学建设', '院级', '已通过', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achievement-002');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at)
SELECT 'achievement-003', '重庆道地药材评价指标库', '中药材数字信息实验室', '科研成果', '校级重点', '待审核', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achievement-003');

-- ==================== achievement_standard (业绩认定标准) ====================
INSERT INTO achievement_standard (id, name, category, level_rule, effective_date, created_at)
SELECT 'standard-001', '学校业绩分类认定办法', '教学科研业绩', '国家级/省部级/校级/院级', '2026-06-06', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_standard WHERE id = 'standard-001');
