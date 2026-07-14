-- ======================================================================
-- test-data-extra.sql — 补充测试数据（约为 data.sql 的 3 倍）
-- 使用 WHERE NOT EXISTS 保证幂等，可重复执行
-- 依赖 data.sql 已执行（角色、基础用户、基础药材等已存在）
-- ======================================================================

-- ==================== 补充用户 ====================
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-teacher-zhang', 'zhang@cqutcm', '张教授', NULL, '中药学院', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-teacher-zhang');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-teacher-liu', 'liu@cqutcm', '刘副教授', NULL, '教学科研部', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-teacher-liu');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-teacher-chen', 'chen@cqutcm', '陈研究员', NULL, '中药材科研中心', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-teacher-chen');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-student-a', 'stu-a@cqutcm', '学生A', NULL, '生物医药学院', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-student-a');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-student-b', 'stu-b@cqutcm', '学生B', NULL, '生物医药学院', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-student-b');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-student-c', 'stu-c@cqutcm', '学生C', NULL, '生物医药学院', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-student-c');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-student-d', 'stu-d@cqutcm', '学生D', NULL, '药学院', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-student-d');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-student-e', 'stu-e@cqutcm', '学生E', NULL, '药学院', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-student-e');

-- 关联角色
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-teacher-zhang', 'role-teacher', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-teacher-zhang' AND role_id = 'role-teacher');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-teacher-liu', 'role-teacher', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-teacher-liu' AND role_id = 'role-teacher');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-teacher-chen', 'role-researcher', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-teacher-chen' AND role_id = 'role-researcher');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-student-a', 'role-student', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-student-a' AND role_id = 'role-student');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-student-b', 'role-student', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-student-b' AND role_id = 'role-student');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-student-c', 'role-student', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-student-c' AND role_id = 'role-student');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-student-d', 'role-student', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-student-d' AND role_id = 'role-student');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-student-e', 'role-student', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-student-e' AND role_id = 'role-student');

-- ==================== herb — 新增 18 种重庆道地药材 ====================
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-007', '川党参', '巫山县', 109.880000, 31.080000, '2800亩', '海拔800-1200m、湿润半阴', 'CQ-DS-007', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-007');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-008', '青蒿', '酉阳县', 108.770000, 28.840000, '4200亩', '丘陵阳坡、排水良好', 'CQ-QH-008', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-008');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-009', '枳壳', '江津区', 106.260000, 29.290000, '3600亩', '温暖向阳、土层深厚', 'CQ-ZQ-009', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-009');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-010', '牡丹皮', '垫江县', 107.350000, 30.330000, '1900亩', '向阳缓坡、富含有机质', 'CQ-MDP-010', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-010');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-011', '白芍', '丰都县', 107.730000, 29.860000, '1500亩', '低山丘陵、湿润肥沃', 'CQ-BS-011', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-011');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-012', '丹参', '开州区', 108.390000, 31.180000, '2200亩', '向阳坡地、砂质壤土', 'CQ-DS-012', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-012');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-013', '桔梗', '巫山县', 109.880000, 31.080000, '1300亩', '排水良好的砂质土', 'CQ-JG-013', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-013');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-014', '黄精', '南川区', 107.090000, 29.160000, '1700亩', '林下阴湿环境', 'CQ-HJ-014', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-014');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-015', '栀子', '江津区', 106.260000, 29.290000, '2600亩', '温暖湿润、日照充足', 'CQ-ZZ-015', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-015');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-016', '车前草', '铜梁区', 106.050000, 29.840000, '800亩', '田野路边、适应性强', 'CQ-CQC-016', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-016');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-017', '玉竹', '綦江区', 106.650000, 29.030000, '1100亩', '林下或山沟阴湿处', 'CQ-YZ-017', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-017');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-018', '厚朴', '武隆区', 107.760000, 29.330000, '3100亩', '中山地带、湿润凉爽', 'CQ-HP-018', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-018');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-019', '何首乌', '彭水县', 108.170000, 29.290000, '1400亩', '沟谷林缘、攀援生长', 'CQ-HSW-019', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-019');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-020', '玄参', '秀山县', 109.000000, 28.450000, '2000亩', '肥沃湿润、半阴环境', 'CQ-XS-020', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-020');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-021', '续断', '城口县', 108.670000, 31.950000, '900亩', '山坡草丛、适应性广', 'CQ-XD-021', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-021');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-022', '百部', '万州区', 108.410000, 30.810000, '1200亩', '林下湿润、富含有机质', 'CQ-BB-022', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-022');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-023', '南五味子', '石柱县', 108.120000, 30.000000, '1600亩', '山地灌木丛、攀援于树上', 'CQ-NWWZ-023', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-023');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, created_at)
SELECT 'herb-024', '钩藤', '彭水县', 108.170000, 29.290000, '1000亩', '溪边林缘、温暖湿润', 'CQ-GT-024', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-024');

-- ==================== growth_record — 新增 30 条 ====================
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-010', '川党参', '巫山县', 17.5, 79, 6.6, '萌芽期', '传感器网关', '张教授', 'teacher', '2026-05-10 08:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-010');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-011', '川党参', '巫山县', 18.2, 76, 6.5, '展叶期', '手机APP采集', '学生A', 'student', '2026-05-20 09:10:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-011');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-012', '川党参', '巫山县', 19.8, 74, 6.5, '生长期', '传感器网关', '张教授', 'teacher', '2026-06-05 10:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-012');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-013', '青蒿', '酉阳县', 23.5, 72, 6.8, '生长期', '手机APP采集', '学生B', 'student', '2026-05-15 11:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-013');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-014', '青蒿', '酉阳县', 25.1, 68, 6.9, '旺长期', '传感器网关', '王老师', 'researcher', '2026-06-01 14:20:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-014');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-015', '青蒿', '酉阳县', 26.3, 65, 7.0, '旺长期', '手机APP采集', '学生B', 'student', '2026-06-15 10:45:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-015');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-016', '枳壳', '江津区', 24.8, 70, 6.4, '萌芽期', '电脑终端录入', '刘副教授', 'teacher', '2026-04-05 09:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-016');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-017', '枳壳', '江津区', 25.6, 68, 6.3, '展叶期', '传感器网关', '陈研究员', 'researcher', '2026-04-25 10:15:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-017');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-018', '枳壳', '江津区', 27.2, 64, 6.5, '生长期', '手机APP采集', '学生C', 'student', '2026-05-20 14:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-018');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-019', '牡丹皮', '垫江县', 20.1, 77, 6.1, '萌芽期', '手机APP采集', '学生D', 'student', '2026-03-20 09:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-019');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-020', '牡丹皮', '垫江县', 21.5, 75, 6.2, '展叶期', '传感器网关', '刘副教授', 'teacher', '2026-04-10 11:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-020');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-021', '白芍', '丰都县', 19.3, 81, 6.7, '萌芽期', '电脑终端录入', '张教授', 'teacher', '2026-04-08 08:45:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-021');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-022', '白芍', '丰都县', 21.8, 78, 6.6, '生长期', '手机APP采集', '学生A', 'student', '2026-05-12 10:20:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-022');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-023', '丹参', '开州区', 21.2, 73, 6.8, '生长期', '传感器网关', '陈研究员', 'researcher', '2026-05-05 09:50:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-023');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-024', '丹参', '开州区', 23.6, 69, 6.9, '旺长期', '手机APP采集', '学生E', 'student', '2026-06-10 11:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-024');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-025', '桔梗', '巫山县', 18.9, 76, 6.3, '展叶期', '传感器网关', '张教授', 'teacher', '2026-05-18 10:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-025');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-026', '黄精', '南川区', 17.8, 82, 5.8, '萌芽期', '手机APP采集', '学生C', 'student', '2026-04-15 08:20:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-026');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-027', '黄精', '南川区', 19.4, 79, 5.9, '展叶期', '传感器网关', '陈研究员', 'researcher', '2026-05-20 09:40:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-027');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-028', '栀子', '江津区', 24.5, 71, 6.5, '生长期', '手机APP采集', '学生D', 'student', '2026-05-25 14:15:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-028');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-029', '栀子', '江津区', 26.1, 67, 6.6, '旺长期', '传感器网关', '刘副教授', 'teacher', '2026-06-20 15:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-029');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-030', '厚朴', '武隆区', 18.5, 80, 6.2, '萌芽期', '电脑终端录入', '张教授', 'teacher', '2026-04-02 08:10:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-030');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-031', '厚朴', '武隆区', 20.3, 76, 6.3, '展叶期', '传感器网关', '学生A', 'student', '2026-05-08 09:55:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-031');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-032', '何首乌', '彭水县', 22.1, 78, 6.4, '生长期', '手机APP采集', '学生E', 'student', '2026-05-30 10:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-032');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-033', '玄参', '秀山县', 19.6, 80, 6.7, '展叶期', '传感器网关', '王老师', 'researcher', '2026-05-12 11:20:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-033');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-034', '玄参', '秀山县', 21.4, 76, 6.8, '生长期', '手机APP采集', '学生B', 'student', '2026-06-05 14:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-034');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-035', '续断', '城口县', 16.8, 84, 6.1, '萌芽期', '传感器网关', '陈研究员', 'researcher', '2026-05-05 08:40:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-035');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-036', '南五味子', '石柱县', 20.5, 79, 5.7, '展叶期', '手机APP采集', '学生C', 'student', '2026-05-22 09:15:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-036');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-037', '钩藤', '彭水县', 23.2, 75, 6.0, '生长期', '传感器网关', '刘副教授', 'teacher', '2026-06-08 10:50:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-037');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-038', '车前草', '铜梁区', 25.4, 72, 7.1, '旺长期', '手机APP采集', '学生D', 'student', '2026-06-18 09:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-038');
INSERT INTO growth_record (id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, recorded_at, created_at)
SELECT 'growth-039', '玉竹', '綦江区', 18.7, 81, 6.0, '展叶期', '电脑终端录入', '张教授', 'teacher', '2026-05-28 10:10:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-039');

-- ==================== course — 新增 12 门 ====================
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-004', '中药材道地性评价方法', '张教授', 6, '视频+讲义', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-004');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-005', '中药炮制学基础实验', '李老师', 8, '视频+讲义', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-005');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-006', '中药材分子鉴定技术', '陈研究员', 4, '课件+案例', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-006');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-007', '药材种植与生态适应性分析', '刘副教授', 6, '视频+数据模板', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-007');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-008', '中药材质量评价标准实训', '王老师', 5, '图谱文件+讲义', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-008');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-009', '中药材溯源体系构建', '张教授', 4, '视频+案例', '待审核', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-009');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-010', '中药化学图谱解析', '陈研究员', 6, '课件+图谱文件', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-010');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-011', '中药材市场流通与监管', '刘副教授', 4, '视频+课件', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-011');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-012', '药用植物组织培养技术', '李老师', 8, '视频+实验指导', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-012');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-013', '中药资源调查与保护', '张教授', 6, '视频+案例', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-013');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-014', '数据分析在中药研究中的应用', '陈研究员', 5, '课件+数据模板', '待审核', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-014');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-015', '中药安全性评价概论', '王老师', 4, '视频+讲义', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-015');

-- ==================== teaching_resource — 新增 12 条 ====================
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-005', 'course-004', '中药材道地性评价方法', '道地药材评价指标体系', '课件文档', NULL, '', '', '张教授', '教师', '已发布', '指标分类清晰，案例充分。', '2026-06-15 09:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-005');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-006', 'course-005', '中药炮制学基础实验', '炒制与炙制操作演示', '教学视频', NULL, 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '', '李老师', '教师', '已发布', '操作步骤规范、安全提示到位。', '2026-06-12 10:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-006');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-007', NULL, '中药材采收加工培训', '产地初加工技术规范', '课件文档', NULL, '', '', '刘副教授', '教师', '已发布', '涵盖干燥、切制、包装等要点。', '2026-06-08 14:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-007');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-008', 'course-006', '中药材分子鉴定技术', 'DNA条形码鉴定实验流程', '教学视频', NULL, 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '', '陈研究员', '科研人员', '已发布', '实验设计严谨，适合研究生教学。', '2026-06-18 16:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-008');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-009', 'course-007', '药材种植与生态适应性分析', '黄连生态适宜性分析案例', '数据模板', NULL, '', '', '刘副教授', '教师', '待审核', '需补充数据分析方法和参考文献。', NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-009');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-010', 'course-008', '中药材质量评价标准实训', '金银花木犀草苷含量测定', '图谱文件', NULL, '', '', '王老师', '科研人员', '已发布', '含量测定图谱完整，结论可靠。', '2026-06-20 09:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-010');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-011', 'course-010', '中药化学图谱解析', 'HPLC指纹图谱解析示例', '图谱文件', NULL, '', '', '陈研究员', '科研人员', '已发布', '图谱标注清晰，可作为教学范本。', '2026-06-22 11:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-011');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-012', 'course-012', '药用植物组织培养技术', '组培实验室操作规范', '教学视频', NULL, 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '', '李老师', '教师', '已发布', '无菌操作技术示范到位。', '2026-06-16 15:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-012');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-013', 'course-009', '中药材溯源体系构建', '溯源码生成与管理规范', '课件文档', NULL, '', '', '张教授', '教师', '待审核', '需补充实际系统操作截图。', NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-013');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-014', 'course-013', '中药资源调查与保护', '重庆地区中药资源分布调查报告', '课件文档', NULL, '', '', '张教授', '教师', '已发布', '调查方法科学，数据翔实。', '2026-06-25 10:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-014');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-015', NULL, '全国中药资源普查培训', '样地调查数据填报规范', '课件文档', NULL, '', '', '陈研究员', '科研人员', '已驳回', '部分数据指标定义不清晰，需修订。', NULL, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-015');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, created_at)
SELECT 'teaching-resource-016', 'course-015', '中药安全性评价概论', '中药肝毒性案例集', '案例文档', NULL, '', '', '王老师', '科研人员', '已发布', '案例典型，分析透彻。', '2026-06-28 14:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'teaching-resource-016');

-- ==================== trace_event — 新增 15 条 ====================
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-006', '川党参', 'CQ-DS-007', '种植', '川党参种苗定植，记录种苗来源、基源鉴定和栽植密度。', '张教授', '2026-04-02 09:00:00', '巫山县当阳乡', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-006');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-007', '川党参', 'CQ-DS-007', '采集', '手机APP采集川党参展叶期温湿度与土壤数据，记录长势。', '学生A', '2026-05-20 09:10:00', '巫山县当阳乡', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-007');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-008', '青蒿', 'CQ-QH-008', '种植', '酉阳青蒿基地完成种苗移栽，登记地块编号和基源信息。', '王老师', '2026-04-10 10:30:00', '酉阳县板溪镇', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-008');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-009', '青蒿', 'CQ-QH-008', '检测', '青蒿素含量快速检测，样本记录进入溯源链。', '王老师', '2026-06-10 15:00:00', '酉阳县检测中心', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-009');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-010', '枳壳', 'CQ-ZQ-009', '种植', '江津枳壳基地果园管理记录，施肥和修剪操作登记。', '刘副教授', '2026-03-25 08:30:00', '江津区先锋镇', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-010');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-011', '枳壳', 'CQ-ZQ-009', '加工', '枳壳采收后切片、干燥加工记录，批次编号与溯源关联。', '刘副教授', '2026-06-15 16:30:00', '江津区中药材加工厂', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-011');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-012', '牡丹皮', 'CQ-MDP-010', '种植', '牡丹皮种苗定植，记录土壤改良和基肥施用情况。', '学生D', '2026-03-20 09:00:00', '垫江县太平镇', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-012');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-013', '白芍', 'CQ-BS-011', '采集', '白芍生长期数据采集，记录株高、茎粗和叶片数。', '学生A', '2026-05-12 10:20:00', '丰都县高家镇', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-013');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-014', '丹参', 'CQ-DS-012', '检测', '丹参酮IIA含量测定，记录检测方法和结果。', '陈研究员', '2026-06-12 14:00:00', '开州区中药材检测站', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-014');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-015', '厚朴', 'CQ-HP-018', '种植', '厚朴幼苗定植于武隆中山地带，记录海拔、坡向和间距。', '张教授', '2026-04-02 08:00:00', '武隆区仙女山镇', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-015');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-016', '何首乌', 'CQ-HSW-019', '采集', '何首乌藤茎生长数据采集，记录攀援高度和叶片状态。', '学生E', '2026-05-30 10:30:00', '彭水县保家镇', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-016');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-017', '玄参', 'CQ-XS-020', '加工', '玄参加工干燥记录，切片厚度和温度参数登记。', '王老师', '2026-06-10 09:00:00', '秀山县龙池镇加工车间', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-017');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-018', '南五味子', 'CQ-NWWZ-023', '采集', '南五味子果实采集记录，登记成熟度和果实品质。', '学生C', '2026-06-20 08:45:00', '石柱县黄水镇', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-018');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-019', '钩藤', 'CQ-GT-024', '加工', '钩藤采收后剪枝、干燥和包装记录。', '刘副教授', '2026-06-22 14:30:00', '彭水县加工基地', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-019');
INSERT INTO trace_event (id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, created_at)
SELECT 'trace-event-020', '金银花', 'CQ-JYH-002', '检测', '金银花绿原酸含量测定，与前期样本对比分析。', '李老师', '2026-06-18 11:20:00', '秀山县中药检测中心', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-event-020');

-- ==================== spectrum_comparison — 新增 9 条 ====================
INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at)
SELECT 'spectrum-compare-004', '川党参', 'DS-WS-20260601', '巫山县', 'HPLC 指纹图谱', '川党参标准图谱 V1', 92.8, '通过', '张教授', '2026-06-10 15:00:00', '特征峰与标准图谱匹配良好', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spectrum-compare-004');
INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at)
SELECT 'spectrum-compare-005', '青蒿', 'QH-YX-20260601', '酉阳县', '紫外光谱', '青蒿素提取液标准光谱', 90.5, '通过', '王老师', '2026-06-12 10:30:00', '吸收峰位置与标准一致', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spectrum-compare-005');
INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at)
SELECT 'spectrum-compare-006', '枳壳', 'ZQ-JJ-20260601', '江津区', '薄层色谱图谱', '枳壳薄层鉴别标准', 87.6, '建议复核', '刘副教授', '2026-06-15 09:00:00', '个别斑点Rf值有偏移', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spectrum-compare-006');
INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at)
SELECT 'spectrum-compare-007', '牡丹皮', 'MDP-DJ-20260601', '垫江县', '红外图谱', '牡丹皮红外标准图谱', 93.4, '通过', '陈研究员', '2026-06-16 14:20:00', '丹皮酚特征峰明显', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spectrum-compare-007');
INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at)
SELECT 'spectrum-compare-008', '丹参', 'DS-KZ-20260601', '开州区', 'HPLC 指纹图谱', '丹参标准指纹图谱', 91.2, '通过', '陈研究员', '2026-06-18 11:00:00', '丹参酮IIA峰面积达标', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spectrum-compare-008');
INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at)
SELECT 'spectrum-compare-009', '厚朴', 'HP-WL-20260601', '武隆区', '薄层色谱图谱', '厚朴酚薄层鉴别标准', 89.8, '通过', '张教授', '2026-06-20 16:00:00', '厚朴酚与和厚朴酚斑点清晰', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spectrum-compare-009');
INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at)
SELECT 'spectrum-compare-010', '玄参', 'XS-XS-20260601', '秀山县', '红外图谱', '玄参红外标准图谱', 88.5, '建议复核', '王老师', '2026-06-22 09:30:00', '个别吸收峰强度偏低', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spectrum-compare-010');
INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at)
SELECT 'spectrum-compare-011', '黄连', 'HL-SZ-20260615', '石柱县', 'HPLC 指纹图谱', '重庆黄连标准图谱 V2', 95.8, '通过', '张教授', '2026-06-22 16:30:00', '图谱质量优良，可作为教学样本', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spectrum-compare-011');
INSERT INTO spectrum_comparison (id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, created_at)
SELECT 'spectrum-compare-012', '黄精', 'HJ-NC-20260601', '南川区', 'HPLC 指纹图谱', '黄精多糖标准图谱', 86.3, '建议复核', '陈研究员', '2026-06-25 10:00:00', '多糖峰形变化较大', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spectrum-compare-012');

-- ==================== growth_analysis — 新增 6 条 ====================
INSERT INTO growth_analysis (id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, created_at)
SELECT 'growth-analysis-003', '巫山川党参展叶期生长趋势', '川党参', '巫山县', '温度、湿度', '2026-05-10 萌芽期', '2026-06-05 生长期', '温度 +2.3，湿度 -5', '温度稳步上升、湿度适中', '川党参生长正常，温湿度条件适宜，建议持续监测土壤水分。', '张教授', '2026-06-05 11:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'growth-analysis-003');
INSERT INTO growth_analysis (id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, created_at)
SELECT 'growth-analysis-004', '酉阳青蒿旺长期环境分析', '青蒿', '酉阳县', '温度、湿度、土壤PH', '2026-05-15 生长期', '2026-06-15 旺长期', '温度 +2.8，湿度 -7，PH +0.2', '温度升高明显、湿度下降', '青蒿进入旺长期，温度适宜但湿度下降较快，建议灌溉保持土壤湿润。', '王老师', '2026-06-15 15:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'growth-analysis-004');
INSERT INTO growth_analysis (id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, created_at)
SELECT 'growth-analysis-005', '垫江牡丹皮萌发至展叶期分析', '牡丹皮', '垫江县', '温度、湿度', '2026-03-20 萌芽期', '2026-04-10 展叶期', '温度 +1.4，湿度 -2', '温湿度相对稳定', '牡丹皮萌芽至展叶期进展顺利，温湿条件稳定。', '刘副教授', '2026-04-10 15:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'growth-analysis-005');
INSERT INTO growth_analysis (id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, created_at)
SELECT 'growth-analysis-006', '江津栀子生长期光合条件评估', '栀子', '江津区', '温度、日照', '2026-05-25 生长期', '2026-06-20 旺长期', '温度 +1.6，日照延长', '温度适宜、光照充足', '栀子光合条件良好，有利于花芽分化和果实发育。', '刘副教授', '2026-06-20 16:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'growth-analysis-006');
INSERT INTO growth_analysis (id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, created_at)
SELECT 'growth-analysis-007', '武隆厚朴萌芽期环境评估', '厚朴', '武隆区', '温度、湿度', '2026-04-02 萌芽期', '2026-05-08 展叶期', '温度 +1.8，湿度 -4', '温度上升、湿度下降', '厚朴幼苗成活率良好，中山环境温度适宜生长。', '张教授', '2026-05-08 14:00:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'growth-analysis-007');
INSERT INTO growth_analysis (id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, created_at)
SELECT 'growth-analysis-008', '石柱黄连六月综合生长趋势', '黄连', '石柱县', '温度、湿度、土壤PH', '2026-06-01 萌芽期', '2026-06-22 旺长期', '温度 +1.8，湿度 -6，PH +0.3', '温度上升、湿度下降、PH稳定', '黄连六月整体生长评估良好，多阶段数据完整，可用于年度对比分析。', '李老师', '2026-06-22 17:30:00', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'growth-analysis-008');

-- ==================== research_project — 新增 9 个 ====================
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, rejected_applicants, created_at)
SELECT 'project-004', '川党参生态适宜性区划研究', '张教授', '招募2名研究生，需具备GIS基础，参与野外调查和数据建模。', '已发布', '数据采集中', '建立川党参适宜性区划图', '学生A', '学生D', '', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'project-004');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, rejected_applicants, created_at)
SELECT 'project-005', '青蒿素含量与环境因子关联分析', '王老师', '招募具备化学分析基础的学生，参与样本采集和含量测定。', '已发布', '样本采集', '优化青蒿素提取工艺', '学生B', '学生C', '', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'project-005');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, rejected_applicants, created_at)
SELECT 'project-006', '重庆枳壳产地品质差异比较', '刘副教授', '招募1名学生参与枳壳样本收集和品质数据分析。', '已发布', '数据对比分析', '建立枳壳品质分级标准', '学生C', '', '', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'project-006');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, rejected_applicants, created_at)
SELECT 'project-007', '黄精林下仿野生栽培技术优化', '陈研究员', '招募2名有栽培经验的学生，参与试验地管理和数据记录。', '待审核', '方案设计', '推广林下栽培标准', '学生D', '', '', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'project-007');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, rejected_applicants, created_at)
SELECT 'project-008', '中药材非遗炮制技艺数字化保护', '张教授', '招募文献整理和影像采集的学生，需参与炮制过程记录。', '已发布', '评价资料整理', '形成非遗数字化档案', '学生A,学生E', '', '', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'project-008');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, rejected_applicants, created_at)
SELECT 'project-009', '重庆道地药材HPLC指纹图谱库建设', '陈研究员', '招募熟悉色谱分析的研究生参与图谱采集和建库工作。', '已发布', '数据采集中', '建成30种药材指纹图谱库', '学生D', '学生E', '', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'project-009');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, rejected_applicants, created_at)
SELECT 'project-010', '中药加工炮制对有效成分影响研究', '李老师', '招募2名炮制方向的学生，参与炮制实验和成分分析。', '已发布', '实验进行中', '优化炮制工艺', '学生A', '学生B', '', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'project-010');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, rejected_applicants, created_at)
SELECT 'project-011', '中药材市场质量评价与溯源体系研究', '刘副教授', '招募对中药材流通感兴趣的学生，参与市场调研和数据整理。', '待审核', '调研阶段', '构建市场质量溯源框架', '', '', '', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'project-011');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, rejected_applicants, created_at)
SELECT 'project-012', '基于光谱技术的药材真伪快速鉴别', '王老师', '招募熟悉光谱分析的学生，参与标准光谱采集和模型训练。', '已发布', '方案设计', '开发快速鉴别方法', '学生C,学生D', '学生E', '', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'project-012');

-- ==================== project_application — 新增 6 条 ====================
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'project-004-apply-a', 'project-004', '学生A', '已批准', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'project-004-apply-a');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'project-004-apply-d', 'project-004', '学生D', '已拒绝', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'project-004-apply-d');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'project-005-apply-b', 'project-005', '学生B', '已批准', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'project-005-apply-b');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'project-005-apply-c', 'project-005', '学生C', '已拒绝', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'project-005-apply-c');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'project-009-apply-d', 'project-009', '学生D', '已批准', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'project-009-apply-d');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'project-008-apply-a', 'project-008', '学生A', '待审批', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'project-008-apply-a');

-- ==================== project_member — 新增 6 条 ====================
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'project-004-member-a', 'project-004', '学生A', '学生', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'project-004-member-a');
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'project-005-member-b', 'project-005', '学生B', '学生', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'project-005-member-b');
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'project-006-member-c', 'project-006', '学生C', '学生', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'project-006-member-c');
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'project-009-member-d', 'project-009', '学生D', '学生', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'project-009-member-d');
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'project-009-member-e', 'project-009', '学生E', '学生', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'project-009-member-e');
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'project-010-member-a', 'project-010', '学生A', '学生', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'project-010-member-a');

-- ==================== training_material — 新增 9 条 ====================
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, created_at)
SELECT 'training-004', '中药化学图谱解析专题培训', '陈研究员', '研究生和科研助理', '签到、考核、实操报告', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'training-004');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, created_at)
SELECT 'training-005', '中药材种植规范（GAP）培训', '张教授', '种植基地技术人员', '签到、考核记录', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'training-005');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, created_at)
SELECT 'training-006', '中药材溯源码操作培训', '刘副教授', '生产与品管人员', '完成签收、实操考核', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'training-006');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, created_at)
SELECT 'training-007', '药材真伪鉴别技术培训', '王老师', '药材经营人员', '签到、实物鉴别考核', '待审核', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'training-007');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, created_at)
SELECT 'training-008', '科研论文写作与数据管理', '张教授', '研究生', '签到、案例作业、论文提纲', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'training-008');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, created_at)
SELECT 'training-009', '实验室安全规范培训', '李老师', '全体实验人员', '签到、考核、安全承诺书', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'training-009');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, created_at)
SELECT 'training-010', '中药材质量评价指标解读', '王老师', '教师与培训人员', '课件存储、考核归档', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'training-010');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, created_at)
SELECT 'training-011', '野外药材资源调查方法', '陈研究员', '调查队员和学生', '签到、调查表提交', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'training-011');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, created_at)
SELECT 'training-012', '生物医药大数据平台使用培训', '刘副教授', '全体师生', '签到、操作考核', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'training-012');

-- ==================== evaluation_record — 新增 9 条 ====================
INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, created_at)
SELECT 'evaluation-004', '川党参', '外观性状、有效成分含量、生态适应性', 89, '良好', '可用于道地药材认证', '巫山党参种植基地', '张教授', '教师', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'evaluation-004');
INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, created_at)
SELECT 'evaluation-005', '青蒿', '青蒿素含量、种植环境、加工工艺', 92, '优秀', '可用于工艺优化和品牌建设', '酉阳青蒿合作社', '王老师', '科研人员', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'evaluation-005');
INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, created_at)
SELECT 'evaluation-006', '枳壳', '果形、香气、柚皮苷含量、加工规范', 86, '良好', '可用于产地品质证明', '江津枳壳基地', '刘副教授', '教师', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'evaluation-006');
INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, created_at)
SELECT 'evaluation-007', '丹参', '丹参酮含量、产地环境、采收加工', 90, '优秀', '可用于药材等级认证', '开州丹参种植基地', '陈研究员', '科研人员', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'evaluation-007');
INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, created_at)
SELECT 'evaluation-008', '厚朴', '厚朴酚含量、树皮外观、采收年限', 84, '良好', '可用于产地品牌建设', '武隆厚朴种植基地', '张教授', '教师', '待审核', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'evaluation-008');
INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, created_at)
SELECT 'evaluation-009', '金银花', '绿原酸含量、花蕾完整度、干燥工艺', 88, '良好', '可用于教学案例和品质分级', '秀山金银花基地', '李老师', '教师', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'evaluation-009');
INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, created_at)
SELECT 'evaluation-010', '玄参', '哈巴苷含量、外观性状、加工工艺', 85, '良好', '用于产地加工评价改进', '秀山玄参加工厂', '王老师', '科研人员', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'evaluation-010');
INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, created_at)
SELECT 'evaluation-011', '白术', '挥发油含量、土壤适应性、种植管理', 83, '良好', '用于白术规范化种植参考', '酉阳白术课题组', '李老师', '教师', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'evaluation-011');
INSERT INTO evaluation_record (id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, created_at)
SELECT 'evaluation-012', '天麻', '天麻素含量、林下环境、采收工艺', 94, '优秀', '可用于非遗申报材料支撑', '巫溪天麻课题组', '张教授', '教师', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'evaluation-012');

-- ==================== achievement_record — 新增 9 条 ====================
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at)
SELECT 'achievement-004', '川党参生态适宜性区划研究', '巫山县农业技术推广中心', '科研成果', '省部级', '待审核', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achievement-004');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at)
SELECT 'achievement-005', '酉阳青蒿素提取工艺优化', '中药材科研中心', '科研成果', '校级重点', '已通过', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achievement-005');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at)
SELECT 'achievement-006', '中药化学图谱解析虚拟仿真实验', '药学院', '教学建设', '国家级', '已通过', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achievement-006');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at)
SELECT 'achievement-007', '中药材溯源体系推广示范', '中药学院', '社会服务', '省部级', '待审核', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achievement-007');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at)
SELECT 'achievement-008', '中药质量评价标准体系研究', '中药材数字信息实验室', '科研成果', '国家级', '已通过', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achievement-008');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at)
SELECT 'achievement-009', '中药材加工炮制教学改革', '药学院', '教学建设', '院级', '已驳回', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achievement-009');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at)
SELECT 'achievement-010', '科研数据管理规范制定', '科研处', '科研成果', '校级', '已通过', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achievement-010');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at)
SELECT 'achievement-011', '生物医药数字信息系统推广应用', '中药学院', '社会服务', '校级重点', '待审核', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achievement-011');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, created_at)
SELECT 'achievement-012', '林下黄精栽培技术培训与推广', '南川区林业科技中心', '社会服务', '省部级', '待审核', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achievement-012');
