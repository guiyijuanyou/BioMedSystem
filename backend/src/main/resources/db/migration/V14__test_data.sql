-- ======================================================================
-- V14__test_data.sql — 综合测试数据（Flyway 迁移）
-- 在所有 V2–V13 表结构就绪后执行，覆盖全部 20+ 张迁移建表
-- 使用 WHERE NOT EXISTS 保证幂等，新增 ID 不与 data.sql 冲突
-- ======================================================================

-- ==================== 1. 扩展用户（8 人） ====================
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, phone, email, title, research_area, created_at)
SELECT 'user-zhang', 'zhang@cqutcm', '张教授', NULL, '中药学院', 'enabled', '13800001001', 'zhang@cqutcm.edu.cn', '教授', '道地药材生态适应性', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-zhang');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, phone, email, title, created_at)
SELECT 'user-liu', 'liu@cqutcm', '刘副教授', NULL, '教学科研部', 'enabled', '13800001002', 'liu@cqutcm.edu.cn', '副教授', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-liu');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, phone, email, title, research_area, created_at)
SELECT 'user-chen', 'chen@cqutcm', '陈研究员', NULL, '中药材科研中心', 'enabled', '13800001003', 'chen@cqutcm.edu.cn', '副研究员', '中药化学与指纹图谱', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-chen');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-stu-a', 'stu-a@cqutcm', '学生A（小张）', NULL, '生物医药学院', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-stu-a');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-stu-b', 'stu-b@cqutcm', '学生B（小李）', NULL, '生物医药学院', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-stu-b');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-stu-c', 'stu-c@cqutcm', '学生C（小王）', NULL, '生物医药学院', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-stu-c');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-stu-d', 'stu-d@cqutcm', '学生D（小赵）', NULL, '药学院', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-stu-d');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at)
SELECT 'user-stu-e', 'stu-e@cqutcm', '学生E（小刘）', NULL, '药学院', 'enabled', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-stu-e');

INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-zhang', 'role-teacher', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-zhang' AND role_id = 'role-teacher');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-liu', 'role-teacher', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-liu' AND role_id = 'role-teacher');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-chen', 'role-researcher', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-chen' AND role_id = 'role-researcher');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-stu-a', 'role-student', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-stu-a' AND role_id = 'role-student');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-stu-b', 'role-student', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-stu-b' AND role_id = 'role-student');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-stu-c', 'role-student', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-stu-c' AND role_id = 'role-student');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-stu-d', 'role-student', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-stu-d' AND role_id = 'role-student');
INSERT INTO sys_user_role (user_id, role_id, created_at)
SELECT 'user-stu-e', 'role-student', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-stu-e' AND role_id = 'role-student');

-- ==================== 2. 扩展药材（24 种） ====================
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-007', '川党参', '巫山县', 109.880000, 31.080000, '2800亩', '海拔800-1200m、湿润半阴', 'CQ-DS-007', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-007');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-008', '青蒿', '酉阳县', 108.770000, 28.840000, '4200亩', '丘陵阳坡、排水良好', 'CQ-QH-008', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-008');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-009', '枳壳', '江津区', 106.260000, 29.290000, '3600亩', '温暖向阳、土层深厚', 'CQ-ZQ-009', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-009');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-010', '牡丹皮', '垫江县', 107.350000, 30.330000, '1900亩', '向阳缓坡、富含有机质', 'CQ-MDP-010', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-010');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-011', '白芍', '丰都县', 107.730000, 29.860000, '1500亩', '低山丘陵、湿润肥沃', 'CQ-BS-011', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-011');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-012', '丹参', '开州区', 108.390000, 31.180000, '2200亩', '向阳坡地、砂质壤土', 'CQ-DS-012', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-012');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-013', '桔梗', '巫山县', 109.880000, 31.080000, '1300亩', '排水良好的砂质土', 'CQ-JG-013', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-013');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-014', '黄精', '南川区', 107.090000, 29.160000, '1700亩', '林下阴湿环境', 'CQ-HJ-014', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-014');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-015', '栀子', '江津区', 106.260000, 29.290000, '2600亩', '温暖湿润、日照充足', 'CQ-ZZ-015', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-015');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-016', '车前草', '铜梁区', 106.050000, 29.840000, '800亩', '田野路边、适应性强', 'CQ-CQC-016', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-016');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-017', '玉竹', '綦江区', 106.650000, 29.030000, '1100亩', '林下或山沟阴湿处', 'CQ-YZ-017', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-017');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-018', '厚朴', '武隆区', 107.760000, 29.330000, '3100亩', '中山地带、湿润凉爽', 'CQ-HP-018', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-018');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-019', '何首乌', '彭水县', 108.170000, 29.290000, '1400亩', '沟谷林缘、攀援生长', 'CQ-HSW-019', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-019');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-020', '玄参', '秀山县', 109.000000, 28.450000, '2000亩', '肥沃湿润、半阴环境', 'CQ-XS-020', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-020');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-021', '续断', '城口县', 108.670000, 31.950000, '900亩', '山坡草丛、适应性广', 'CQ-XD-021', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-021');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-022', '百部', '万州区', 108.410000, 30.810000, '1200亩', '林下湿润、富含有机质', 'CQ-BB-022', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-022');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-023', '南五味子', '石柱县', 108.120000, 30.000000, '1600亩', '山地灌木丛、攀援于树上', 'CQ-NWWZ-023', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-023');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-024', '钩藤', '彭水县', 108.170000, 29.290000, '1000亩', '溪边林缘、温暖湿润', 'CQ-GT-024', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-024');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-025', '白芨', '奉节县', 109.460000, 31.020000, '600亩', '阴湿山坡、排水良好', 'CQ-BJ-025', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-025');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-026', '山茱萸', '城口县', 108.670000, 31.950000, '850亩', '温暖向阳、土层深厚', 'CQ-SZY-026', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-026');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-027', '独活', '巫溪县', 109.630000, 31.400000, '1100亩', '中山地带、凉爽湿润', 'CQ-DH-027', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-027');
INSERT INTO herb (id, name, district, longitude, latitude, scale_desc, environment, trace_code, version, created_at)
SELECT 'herb-028', '云木香', '南川区', 107.090000, 29.160000, '750亩', '高海拔地区、凉爽气候', 'CQ-YMX-028', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM herb WHERE id = 'herb-028');

-- ==================== 3. herb_batch（每个药材对应 1-2 批，24 批） ====================
INSERT INTO herb_batch (id, herb_id, batch_code, batch_name, trace_code, plot_name, district, longitude, latitude, scale_desc, environment, planting_date, expected_harvest_date, responsible_person, current_stage, status, version, created_at) VALUES
('batch-herb001-1', 'herb-001', 'HL-2026-SZ-A', '石柱黄连A基地', 'CQ-HL-001', '黄水镇1号地', '石柱县', 108.120000, 30.000000, '3200亩', '海拔高、湿润阴凉', '2026-03-01', '2026-11-15', '张老师', '旺长期', 'active', 0, CURRENT_TIMESTAMP),
('batch-herb001-2', 'herb-001', 'HL-2026-SZ-B', '石柱黄连B基地', 'CQ-HL-001', '黄水镇2号地', '石柱县', 108.130000, 30.010000, '1800亩', '林下遮阴栽培', '2026-03-15', '2026-11-30', '张教授', '生长期', 'active', 0, CURRENT_TIMESTAMP),
('batch-herb002-1', 'herb-002', 'JYH-2026-XS-A', '秀山金银花基地', 'CQ-JYH-002', '平凯镇1号坡', '秀山县', 109.000000, 28.450000, '1800亩', '丘陵坡地、日照充足', '2025-10-01', '2026-07-20', '王老师', '旺长期', 'active', 0, CURRENT_TIMESTAMP),
('batch-herb003-1', 'herb-003', 'TM-2026-WX-A', '巫溪天麻基地', 'CQ-TM-003', '红池坝林下', '巫溪县', 109.630000, 31.400000, '950亩', '林下仿野生种植', '2026-01-10', '2026-10-20', '张教授', '萌芽期', 'active', 0, CURRENT_TIMESTAMP),
('batch-herb004-1', 'herb-004', 'DZ-2026-DJ-A', '垫江杜仲基地', 'CQ-DZ-004', '太平镇1号林', '垫江县', 107.350000, 30.330000, '2100亩', '低山丘陵、土层深厚', '2020-03-01', '2027-06-01', '刘副教授', '生长期', 'active', 0, CURRENT_TIMESTAMP),
('batch-herb005-1', 'herb-005', 'BZ-2026-YY-A', '酉阳白术基地', 'CQ-BZ-005', '板溪镇1号地', '酉阳县', 108.770000, 28.840000, '1650亩', '温润凉爽、富含腐殖质', '2026-04-01', '2026-10-15', '王老师', '展叶期', 'active', 0, CURRENT_TIMESTAMP),
('batch-herb006-1', 'herb-006', 'FS-2026-JJ-A', '江津佛手基地', 'CQ-FS-006', '石门镇果园', '江津区', 106.260000, 29.290000, '2400亩', '气候温暖、光照充足', '2020-02-01', '2026-08-30', '刘副教授', '旺长期', 'active', 0, CURRENT_TIMESTAMP),
('batch-herb007-1', 'herb-007', 'DS-2026-WS-A', '巫山川党参基地', 'CQ-DS-007', '当阳乡1号地', '巫山县', 109.880000, 31.080000, '2800亩', '海拔800-1200m湿润半阴', '2026-04-01', '2026-12-01', '张教授', '萌芽期', 'active', 0, CURRENT_TIMESTAMP),
('batch-herb008-1', 'herb-008', 'QH-2026-YY-A', '酉阳青蒿基地', 'CQ-QH-008', '板溪镇2号坡', '酉阳县', 108.770000, 28.840000, '4200亩', '丘陵阳坡、排水良好', '2026-04-15', '2026-08-15', '王老师', '生长期', 'active', 0, CURRENT_TIMESTAMP),
('batch-herb009-1', 'herb-009', 'ZQ-2026-JJ-A', '江津枳壳基地', 'CQ-ZQ-009', '先锋镇果园', '江津区', 106.260000, 29.290000, '3600亩', '温暖向阳、土层深厚', '2019-03-01', '2026-08-01', '刘副教授', '生长期', 'active', 0, CURRENT_TIMESTAMP),
('batch-herb010-1', 'herb-010', 'MDP-2026-DJ-A', '垫江牡丹皮基地', 'CQ-MDP-010', '太平镇2号地', '垫江县', 107.350000, 30.330000, '1900亩', '向阳缓坡、富含有机质', '2024-09-01', '2028-06-01', '学生D', '萌芽期', 'active', 0, CURRENT_TIMESTAMP),
('batch-herb011-1', 'herb-011', 'BS-2026-FD-A', '丰都白芍基地', 'CQ-BS-011', '高家镇1号地', '丰都县', 107.730000, 29.860000, '1500亩', '低山丘陵、湿润肥沃', '2025-10-01', '2027-09-01', '张教授', '生长期', 'active', 0, CURRENT_TIMESTAMP);

-- ==================== 4. lab_sample（12 个样本） ====================
INSERT IGNORE INTO lab_sample (id, batch_id, sample_code, sample_type, collected_at, collector_name, sample_location, storage_condition, status, version, created_at) VALUES
('sample-001', 'batch-herb001-1', 'HL-SZ-20260601', '叶片', '2026-06-01 09:00:00', '当前学生', '石柱县黄水镇', '硅胶干燥、4°C冷藏', 'analysed', 0, CURRENT_TIMESTAMP),
('sample-002', 'batch-herb001-1', 'HL-SZ-20260615', '根茎', '2026-06-15 09:30:00', '当前学生', '石柱县黄水镇', '冷藏转运、-20°C冻存', 'analysed', 0, CURRENT_TIMESTAMP),
('sample-003', 'batch-herb002-1', 'JYH-XS-20260602', '花蕾', '2026-06-02 08:00:00', '学生B', '秀山县平凯镇', '硅胶干燥、密封保存', 'analysed', 0, CURRENT_TIMESTAMP),
('sample-004', 'batch-herb003-1', 'TM-WX-20260603', '块茎', '2026-06-03 10:00:00', '学生A', '巫溪县红池坝', '冷藏转运', 'analysed', 0, CURRENT_TIMESTAMP),
('sample-005', 'batch-herb004-1', 'DZ-DJ-20260604', '树皮', '2026-06-04 11:00:00', '学生C', '垫江县太平镇', '阴干保存', 'analysed', 0, CURRENT_TIMESTAMP),
('sample-006', 'batch-herb007-1', 'DS-WS-20260605', '根', '2026-06-05 08:30:00', '学生A', '巫山县当阳乡', '硅胶干燥、密封保存', 'analysed', 0, CURRENT_TIMESTAMP),
('sample-007', 'batch-herb008-1', 'QH-YY-20260606', '全草', '2026-06-06 09:00:00', '学生B', '酉阳县板溪镇', '阴干、避光保存', 'stored', 0, CURRENT_TIMESTAMP),
('sample-008', 'batch-herb009-1', 'ZQ-JJ-20260607', '未成熟果实', '2026-06-07 10:00:00', '学生C', '江津区先锋镇', '冷藏转运、-20°C冻存', 'analysed', 0, CURRENT_TIMESTAMP),
('sample-009', 'batch-herb011-1', 'BS-FD-20260608', '根', '2026-06-08 09:30:00', '学生A', '丰都县高家镇', '硅胶干燥', 'stored', 0, CURRENT_TIMESTAMP),
('sample-010', 'batch-herb005-1', 'BZ-YY-20260609', '根茎', '2026-06-09 10:30:00', '学生E', '酉阳县板溪镇', '阴干保存', 'stored', 0, CURRENT_TIMESTAMP),
('sample-011', 'batch-herb006-1', 'FS-JJ-20260610', '果实', '2026-06-10 11:00:00', '学生D', '江津区石门镇', '冷藏转运', 'analysed', 0, CURRENT_TIMESTAMP),
('sample-012', 'batch-herb001-2', 'HL-SZ-20260620', '根茎', '2026-06-20 08:00:00', '张教授', '石柱县黄水镇B区', '冷藏转运、-20°C冻存', 'collected', 0, CURRENT_TIMESTAMP);

-- ==================== 5. 扩展生长记录（30 条，含 batch_id、GPS） ====================
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-010', 'batch-herb007-1', '川党参', '巫山县', 17.5, 79, 6.6, '萌芽期', '传感器网关', '张教授', 'teacher', 109.880000, 31.080000, '2026-05-10 08:30:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-010');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-011', 'batch-herb007-1', '川党参', '巫山县', 18.2, 76, 6.5, '展叶期', '手机APP采集', '学生A', 'student', 109.880000, 31.080000, '2026-05-20 09:10:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-011');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-012', 'batch-herb007-1', '川党参', '巫山县', 19.8, 74, 6.5, '生长期', '传感器网关', '张教授', 'teacher', 109.880000, 31.080000, '2026-06-05 10:00:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-012');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-013', 'batch-herb008-1', '青蒿', '酉阳县', 23.5, 72, 6.8, '生长期', '手机APP采集', '学生B', 'student', 108.770000, 28.840000, '2026-05-15 11:30:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-013');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-014', 'batch-herb008-1', '青蒿', '酉阳县', 25.1, 68, 6.9, '旺长期', '传感器网关', '王老师', 'researcher', 108.770000, 28.840000, '2026-06-01 14:20:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-014');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-015', 'batch-herb008-1', '青蒿', '酉阳县', 26.3, 65, 7.0, '旺长期', '手机APP采集', '学生B', 'student', 108.770000, 28.840000, '2026-06-15 10:45:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-015');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-016', 'batch-herb009-1', '枳壳', '江津区', 24.8, 70, 6.4, '萌芽期', '电脑终端录入', '刘副教授', 'teacher', 106.260000, 29.290000, '2026-04-05 09:00:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-016');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-017', 'batch-herb009-1', '枳壳', '江津区', 25.6, 68, 6.3, '展叶期', '传感器网关', '陈研究员', 'researcher', 106.260000, 29.290000, '2026-04-25 10:15:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-017');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-018', 'batch-herb009-1', '枳壳', '江津区', 27.2, 64, 6.5, '生长期', '手机APP采集', '学生C', 'student', 106.260000, 29.290000, '2026-05-20 14:30:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-018');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-019', 'batch-herb010-1', '牡丹皮', '垫江县', 20.1, 77, 6.1, '萌芽期', '手机APP采集', '学生D', 'student', 107.350000, 30.330000, '2026-03-20 09:30:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-019');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-020', 'batch-herb010-1', '牡丹皮', '垫江县', 21.5, 75, 6.2, '展叶期', '传感器网关', '刘副教授', 'teacher', 107.350000, 30.330000, '2026-04-10 11:00:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-020');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-021', 'batch-herb011-1', '白芍', '丰都县', 19.3, 81, 6.7, '萌芽期', '电脑终端录入', '张教授', 'teacher', 107.730000, 29.860000, '2026-04-08 08:45:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-021');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-022', 'batch-herb011-1', '白芍', '丰都县', 21.8, 78, 6.6, '生长期', '手机APP采集', '学生A', 'student', 107.730000, 29.860000, '2026-05-12 10:20:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-022');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-023', 'batch-herb001-1', '黄连', '石柱县', 21.5, 78, 6.3, '旺长期', '传感器网关', '张教授', 'teacher', 108.120000, 30.000000, '2026-07-01 09:00:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-023');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-024', 'batch-herb008-1', '青蒿', '酉阳县', 27.0, 62, 7.0, '旺长期', '电脑终端录入', '陈研究员', 'researcher', 108.770000, 28.840000, '2026-07-05 11:00:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-024');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-025', 'batch-herb001-2', '黄连', '石柱县', 19.2, 85, 6.1, '萌芽期', '手机APP采集', '学生A', 'student', 108.130000, 30.010000, '2026-06-15 08:30:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-025');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-026', 'batch-herb001-2', '黄连', '石柱县', 20.0, 82, 6.2, '展叶期', '传感器网关', '张教授', 'teacher', 108.130000, 30.010000, '2026-07-01 08:45:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-026');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-027', 'batch-herb004-1', '杜仲', '垫江县', 24.1, 68, 6.8, '生长期', '电脑终端录入', '刘副教授', 'teacher', 107.350000, 30.330000, '2026-06-20 10:00:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-027');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-028', 'batch-herb006-1', '佛手', '江津区', 27.0, 64, 6.9, '旺长期', '手机APP采集', '学生D', 'student', 106.260000, 29.290000, '2026-07-08 14:00:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-028');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-029', 'batch-herb005-1', '白术', '酉阳县', 19.5, 80, 6.4, '生长期', '传感器网关', '王老师', 'researcher', 108.770000, 28.840000, '2026-06-25 09:30:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-029');
INSERT INTO growth_record (id, batch_id, herb_name, district, temperature, humidity, soil_ph, growth_stage, collect_source, recorder_name, recorder_role, longitude, latitude, recorded_at, version, created_at)
SELECT 'growth-030', 'batch-herb002-1', '金银花', '秀山县', 26.8, 66, 6.9, '旺长期', '手机APP采集', '学生B', 'student', 109.000000, 28.450000, '2026-06-28 10:15:00', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_record WHERE id = 'growth-030');

-- ==================== 6. 扩展课程（12 门） ====================
INSERT INTO course (id, title, teacher_name, hours, material_type, status, reviewer_name, review_comment, version, created_at)
SELECT 'course-004', '中药材道地性评价方法', '张教授', 6, '视频+讲义', '已发布', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-004');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, reviewer_name, review_comment, version, created_at)
SELECT 'course-005', '中药炮制学基础实验', '李老师', 8, '视频+讲义', '已发布', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-005');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, reviewer_name, review_comment, version, created_at)
SELECT 'course-006', '中药材分子鉴定技术', '陈研究员', 4, '课件+案例', '已发布', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-006');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, reviewer_name, review_comment, version, created_at)
SELECT 'course-007', '药材种植与生态适应性分析', '刘副教授', 6, '视频+数据模板', '已发布', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-007');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, reviewer_name, review_comment, version, created_at)
SELECT 'course-008', '中药材质量评价标准实训', '王老师', 5, '图谱文件+讲义', '已发布', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-008');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, reviewer_name, review_comment, version, created_at)
SELECT 'course-009', '中药材溯源体系构建', '张教授', 4, '视频+案例', '待审核', '系统管理员', '课件内容基本完整，请补充实际溯源系统截图。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-009');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, reviewer_name, review_comment, version, created_at)
SELECT 'course-010', '中药化学图谱解析', '陈研究员', 6, '课件+图谱文件', '已发布', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-010');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, reviewer_name, review_comment, version, created_at)
SELECT 'course-011', '中药材市场流通与监管', '刘副教授', 4, '视频+课件', '已发布', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-011');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, reviewer_name, review_comment, version, created_at)
SELECT 'course-012', '药用植物组织培养技术', '李老师', 8, '视频+实验指导', '已发布', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-012');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, reviewer_name, review_comment, version, created_at)
SELECT 'course-013', '中药资源调查与保护', '张教授', 6, '视频+案例', '已发布', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-013');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, reviewer_name, review_comment, version, created_at)
SELECT 'course-014', '数据分析在中药研究中的应用', '陈研究员', 5, '课件+数据模板', '待审核', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-014');
INSERT INTO course (id, title, teacher_name, hours, material_type, status, reviewer_name, review_comment, version, created_at)
SELECT 'course-015', '中药安全性评价概论', '王老师', 4, '视频+讲义', '已发布', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-015');

-- ==================== 7. 扩展教学资源（12 条） ====================
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, reviewer_name, version, created_at)
SELECT 'resource-005', 'course-004', '中药材道地性评价方法', '道地药材评价指标体系', '课件文档', NULL, '', '', '张教授', '教师', '已发布', '指标分类清晰，案例充分。', '2026-06-15 09:00:00', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'resource-005');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, reviewer_name, version, created_at)
SELECT 'resource-006', 'course-005', '中药炮制学基础实验', '炒制与炙制操作演示', '教学视频', NULL, 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '', '李老师', '教师', '已发布', '操作步骤规范、安全提示到位。', '2026-06-12 10:00:00', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'resource-006');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, reviewer_name, version, created_at)
SELECT 'resource-007', NULL, '中药材采收加工培训', '产地初加工技术规范', '课件文档', NULL, '', '', '刘副教授', '教师', '已发布', '涵盖干燥、切制、包装等要点。', '2026-06-08 14:30:00', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'resource-007');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, reviewer_name, version, created_at)
SELECT 'resource-008', 'course-006', '中药材分子鉴定技术', 'DNA条形码鉴定实验流程', '教学视频', NULL, 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '', '陈研究员', '科研人员', '已发布', '实验设计严谨，适合研究生教学。', '2026-06-18 16:00:00', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'resource-008');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, reviewer_name, version, created_at)
SELECT 'resource-009', 'course-007', '药材种植与生态适应性分析', '黄连生态适宜性分析案例', '数据模板', NULL, '', '', '刘副教授', '教师', '待审核', '需补充数据分析方法和参考文献。', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'resource-009');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, reviewer_name, version, created_at)
SELECT 'resource-010', 'course-008', '中药材质量评价标准实训', '金银花木犀草苷含量测定', '图谱文件', NULL, '', '', '王老师', '科研人员', '已发布', '含量测定图谱完整，结论可靠。', '2026-06-20 09:30:00', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'resource-010');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, reviewer_name, version, created_at)
SELECT 'resource-011', 'course-010', '中药化学图谱解析', 'HPLC指纹图谱解析示例', '图谱文件', NULL, '', '', '陈研究员', '科研人员', '已发布', '图谱标注清晰，可作为教学范本。', '2026-06-22 11:00:00', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'resource-011');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, reviewer_name, version, created_at)
SELECT 'resource-012', 'course-012', '药用植物组织培养技术', '组培实验室操作规范', '教学视频', NULL, 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', '', '李老师', '教师', '已发布', '无菌操作技术示范到位。', '2026-06-16 15:00:00', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'resource-012');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, reviewer_name, version, created_at)
SELECT 'resource-013', 'course-009', '中药材溯源体系构建', '溯源码生成与管理规范', '课件文档', NULL, '', '', '张教授', '教师', '待审核', '需补充实际系统操作截图。', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'resource-013');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, reviewer_name, version, created_at)
SELECT 'resource-014', 'course-013', '中药资源调查与保护', '重庆地区中药资源分布调查报告', '课件文档', NULL, '', '', '张教授', '教师', '已发布', '调查方法科学，数据翔实。', '2026-06-25 10:00:00', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'resource-014');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, reviewer_name, version, created_at)
SELECT 'resource-015', NULL, '全国中药资源普查培训', '样地调查数据填报规范', '课件文档', NULL, '', '', '陈研究员', '科研人员', '已驳回', '部分数据指标定义不清晰，需修订。', NULL, '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'resource-015');
INSERT INTO teaching_resource (id, course_id, course_title, title, resource_type, file_id, video_url, file_url, uploader_name, uploader_role, status, review_comment, published_at, reviewer_name, version, created_at)
SELECT 'resource-016', 'course-015', '中药安全性评价概论', '中药肝毒性案例集', '案例文档', NULL, '', '', '王老师', '科研人员', '已发布', '案例典型，分析透彻。', '2026-06-28 14:00:00', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM teaching_resource WHERE id = 'resource-016');

-- ==================== 8. 扩展溯源事件（15 条，含 batch_id） ====================
INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, version, created_at)
SELECT 'trace-006', 'batch-herb007-1', '川党参', 'CQ-DS-007', '种植', '川党参种苗定植，记录种苗来源、基源鉴定和栽植密度。', '张教授', '2026-04-02 09:00:00', '巫山县当阳乡', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-006');
INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, version, created_at)
SELECT 'trace-007', 'batch-herb008-1', '青蒿', 'CQ-QH-008', '种植', '酉阳青蒿基地完成种苗移栽，登记地块编号。', '王老师', '2026-04-10 10:30:00', '酉阳县板溪镇', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-007');
INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, version, created_at)
SELECT 'trace-008', 'batch-herb008-1', '青蒿', 'CQ-QH-008', '检测', '青蒿素含量快速检测，样本记录进入溯源链。', '王老师', '2026-06-10 15:00:00', '酉阳县检测中心', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-008');
INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, version, created_at)
SELECT 'trace-009', 'batch-herb009-1', '枳壳', 'CQ-ZQ-009', '种植', '江津枳壳基地果园管理记录，施肥和修剪操作登记。', '刘副教授', '2026-03-25 08:30:00', '江津区先锋镇', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-009');
INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, version, created_at)
SELECT 'trace-010', 'batch-herb009-1', '枳壳', 'CQ-ZQ-009', '加工', '枳壳采收后切片、干燥加工记录，批次编号关联。', '刘副教授', '2026-06-15 16:30:00', '江津区中药材加工厂', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-010');
INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, version, created_at)
SELECT 'trace-011', 'batch-herb010-1', '牡丹皮', 'CQ-MDP-010', '种植', '牡丹皮种苗定植，记录土壤改良和基肥施用。', '学生D', '2026-03-20 09:00:00', '垫江县太平镇', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-011');
INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, version, created_at)
SELECT 'trace-012', 'batch-herb011-1', '白芍', 'CQ-BS-011', '采集', '白芍生长期数据采集，记录株高、茎粗和叶片数。', '学生A', '2026-05-12 10:20:00', '丰都县高家镇', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-012');
INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, version, created_at)
SELECT 'trace-013', 'batch-herb004-1', '杜仲', 'CQ-DZ-004', '检测', '杜仲胶丝特征和含水率检测，结果用于质量证明。', '刘副教授', '2026-06-20 14:30:00', '垫江县沙坪镇', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-013');
INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, version, created_at)
SELECT 'trace-014', 'batch-herb006-1', '佛手', 'CQ-FS-006', '加工', '佛手切片、干燥和包装过程记录，形成质量追溯节点。', '王老师', '2026-06-25 16:00:00', '江津区石门镇', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-014');
INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, version, created_at)
SELECT 'trace-015', 'batch-herb001-1', '黄连', 'CQ-HL-001', '检测', '黄连样本HPLC指纹图谱比对完成，相似度符合要求。', '李老师', '2026-06-22 16:00:00', '中药材数字信息实验室', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-015');
INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, version, created_at)
SELECT 'trace-016', 'batch-herb001-2', '黄连', 'CQ-HL-001', '采集', 'B基地黄连展叶期数据采集，记录生态因子。', '张教授', '2026-07-01 08:45:00', '石柱县黄水镇B区', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-016');
INSERT INTO trace_event (id, batch_id, herb_name, trace_code, event_type, event_content, operator_name, event_time, location, version, created_at)
SELECT 'trace-017', 'batch-herb005-1', '白术', 'CQ-BZ-005', '采集', '白术展叶期环境因子记录，气温和土壤湿度采集。', '学生E', '2026-06-25 09:30:00', '酉阳县板溪镇', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM trace_event WHERE id = 'trace-017');

-- ==================== 9. 扩展图谱比对（9 条，含 batch_id + sample_id + algorithm） ====================
INSERT INTO spectrum_comparison (id, batch_id, sample_id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, status, reviewer_name, version, sample_data_json, reference_data_json, compare_algorithm, created_at)
SELECT 'spec-004', 'batch-herb007-1', 'sample-006', '川党参', 'DS-WS-20260605', '巫山县', 'HPLC 指纹图谱', '川党参标准图谱 V1', 92.8, '通过', '张教授', '2026-06-10 15:00:00', '特征峰与标准图谱匹配良好', '已发布', '系统管理员', 0, NULL, NULL, 'COSINE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spec-004');
INSERT INTO spectrum_comparison (id, batch_id, sample_id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, status, reviewer_name, version, compare_algorithm, created_at)
SELECT 'spec-005', 'batch-herb008-1', 'sample-007', '青蒿', 'QH-YY-20260606', '酉阳县', '紫外光谱', '青蒿素提取液标准光谱', 90.5, '通过', '王老师', '2026-06-12 10:30:00', '吸收峰位置与标准一致', '已发布', '系统管理员', 0, 'COSINE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spec-005');
INSERT INTO spectrum_comparison (id, batch_id, sample_id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, status, reviewer_name, version, compare_algorithm, created_at)
SELECT 'spec-006', 'batch-herb009-1', 'sample-008', '枳壳', 'ZQ-JJ-20260607', '江津区', '薄层色谱图谱', '枳壳薄层鉴别标准', 87.6, '建议复核', '刘副教授', '2026-06-15 09:00:00', '个别斑点Rf值有偏移', '待审核', NULL, 0, 'PEARSON', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spec-006');
INSERT INTO spectrum_comparison (id, batch_id, sample_id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, status, reviewer_name, version, compare_algorithm, created_at)
SELECT 'spec-007', 'batch-herb001-1', 'sample-001', '黄连', 'HL-SZ-20260601', '石柱县', 'HPLC 指纹图谱', '重庆黄连标准图谱 V2', 95.8, '通过', '张教授', '2026-06-22 16:30:00', '图谱质量优良，可作为教学样本', '已发布', '系统管理员', 0, 'COMPOSITE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spec-007');
INSERT INTO spectrum_comparison (id, batch_id, sample_id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, status, reviewer_name, version, compare_algorithm, created_at)
SELECT 'spec-008', 'batch-herb003-1', 'sample-004', '天麻', 'TM-WX-20260603', '巫溪县', '红外图谱', '天麻红外标准图谱', 91.3, '通过', '王老师', '2026-06-08 14:20:00', '样本图谱与参考图谱一致', '已发布', '系统管理员', 0, 'COSINE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spec-008');
INSERT INTO spectrum_comparison (id, batch_id, sample_id, herb_name, sample_code, district, spectrum_type, reference_name, similarity, result, operator_name, compared_at, remark, status, reviewer_name, version, compare_algorithm, created_at)
SELECT 'spec-009', 'batch-herb002-1', 'sample-003', '金银花', 'JYH-XS-20260602', '秀山县', '薄层色谱图谱', '金银花薄层鉴别标准图谱', 88.2, '建议复核', '李老师', '2026-06-07 10:30:00', '局部斑点颜色偏浅，建议复测', '已发布', '系统管理员', 0, 'COSINE', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM spectrum_comparison WHERE id = 'spec-009');

-- ==================== 10. 扩展生长数据分析（6 条，含 batch_id） ====================
INSERT INTO growth_analysis (id, batch_id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, status, reviewer_name, version, created_at)
SELECT 'ga-003', 'batch-herb007-1', '巫山川党参展叶期生长趋势', '川党参', '巫山县', '温度、湿度', '2026-05-10 萌芽期', '2026-06-05 生长期', '温度 +2.3，湿度 -5', '温度稳步上升、湿度适中', '川党参生长正常，温湿度条件适宜。', '张教授', '2026-06-05 11:00:00', '已发布', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'ga-003');
INSERT INTO growth_analysis (id, batch_id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, status, reviewer_name, version, created_at)
SELECT 'ga-004', 'batch-herb008-1', '酉阳青蒿旺长期环境分析', '青蒿', '酉阳县', '温度、湿度、土壤PH', '2026-05-15 生长期', '2026-06-15 旺长期', '温度 +2.8，湿度 -7，PH +0.2', '温度升高明显、湿度下降', '青蒿进入旺长期，建议灌溉保湿。', '王老师', '2026-06-15 15:00:00', '已发布', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'ga-004');
INSERT INTO growth_analysis (id, batch_id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, status, reviewer_name, version, created_at)
SELECT 'ga-005', 'batch-herb001-1', '石柱黄连六月综合生长趋势', '黄连', '石柱县', '温度、湿度、土壤PH', '2026-06-01 萌芽期', '2026-06-22 旺长期', '温度 +1.8，湿度 -6，PH +0.3', '温度上升、湿度下降、PH稳定', '黄连六月生长良好，各阶段数据完整。', '李老师', '2026-06-22 17:30:00', '已发布', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'ga-005');
INSERT INTO growth_analysis (id, batch_id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, status, reviewer_name, version, created_at)
SELECT 'ga-006', 'batch-herb002-1', '秀山金银花花蕾期环境对比', '金银花', '秀山县', '温度、湿度', '2026-06-02 抽枝期', '2026-06-28 旺长期', '温度 +2.1，湿度 -5', '温度上升、湿度略降', '金银花旺长期环境条件适宜。', '王老师', '2026-06-28 16:30:00', '已发布', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'ga-006');
INSERT INTO growth_analysis (id, batch_id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, status, reviewer_name, version, created_at)
SELECT 'ga-007', 'batch-herb001-2', '黄连B基地展叶期评估', '黄连', '石柱县', '温度、湿度、土壤PH', '2026-06-15 萌芽期', '2026-07-01 展叶期', '温度 +0.8，湿度 -3，PH +0.1', '温湿度稳定、PH微升', 'B基地黄连展叶期进展顺利。', '张教授', '2026-07-01 10:00:00', '待审核', NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'ga-007');
INSERT INTO growth_analysis (id, batch_id, analysis_name, herb_name, district, indicator, baseline, current_value, difference_desc, trend, conclusion, analyst_name, analyzed_at, status, reviewer_name, version, created_at)
SELECT 'ga-008', 'batch-herb009-1', '江津枳壳生长期光合条件', '枳壳', '江津区', '温度、日照', '2026-04-05 萌芽期', '2026-05-20 生长期', '温度 +2.4，日照延长', '温度适宜、光照充足', '枳壳光合条件良好，利于果实发育。', '刘副教授', '2026-05-20 16:00:00', '已发布', '系统管理员', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM growth_analysis WHERE id = 'ga-008');

-- ==================== 11. research_project（9 个，含 reviewer） ====================
INSERT INTO research_project (id, title, leader_name, requirements, status, reviewer_name, review_comment, stage, transformation, applicant_requests, approved_members, rejected_applicants, version, created_at)
SELECT 'proj-004', '川党参生态适宜性区划研究', '张教授', '招募2名研究生，需具备GIS基础，参与野外调查和数据建模。', '已发布', '系统管理员', '选题符合平台研究方向，予以发布。', '数据采集中', '建立川党参适宜性区划图', '学生A', '学生D', '', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'proj-004');
INSERT INTO research_project (id, title, leader_name, requirements, status, reviewer_name, review_comment, stage, transformation, applicant_requests, approved_members, rejected_applicants, version, created_at)
SELECT 'proj-005', '青蒿素含量与环境因子关联分析', '王老师', '招募具备化学分析基础的学生，参与样本采集和含量测定。', '已发布', '系统管理员', '内容完整，予以发布。', '样本采集', '优化青蒿素提取工艺', '学生B', '学生C', '', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'proj-005');
INSERT INTO research_project (id, title, leader_name, requirements, status, reviewer_name, review_comment, stage, transformation, applicant_requests, approved_members, rejected_applicants, version, created_at)
SELECT 'proj-006', '重庆枳壳产地品质差异比较', '刘副教授', '招募1名学生参与枳壳样本收集和品质数据分析。', '已发布', '系统管理员', '已审核通过。', '数据对比分析', '建立枳壳品质分级标准', '学生C', '', '', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'proj-006');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, version, created_at)
SELECT 'proj-007', '黄精林下仿野生栽培技术优化', '陈研究员', '招募2名有栽培经验的学生，参与试验地管理。', '待审核', '方案设计', '推广林下栽培标准', '学生D', '', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'proj-007');
INSERT INTO research_project (id, title, leader_name, requirements, status, reviewer_name, review_comment, stage, transformation, applicant_requests, approved_members, rejected_applicants, version, created_at)
SELECT 'proj-008', '中药材非遗炮制技艺数字化保护', '张教授', '招募文献整理和影像采集的学生，需参与炮制过程记录。', '已发布', '系统管理员', '内容详实，具有文化传承价值，予以发布。', '评价资料整理', '形成非遗数字化档案', '学生A,学生E', '', '', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'proj-008');
INSERT INTO research_project (id, title, leader_name, requirements, status, reviewer_name, review_comment, stage, transformation, applicant_requests, approved_members, rejected_applicants, version, created_at)
SELECT 'proj-009', '重庆道地药材HPLC指纹图谱库建设', '陈研究员', '招募熟悉色谱分析的研究生参与图谱采集和建库。', '已发布', '系统管理员', '研究方案可行，予以发布。', '数据采集中', '建成30种药材指纹图谱库', '学生D', '学生E', '', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'proj-009');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, version, created_at)
SELECT 'proj-010', '中药加工炮制对有效成分影响研究', '李老师', '招募2名炮制方向的学生，参与炮制实验和成分分析。', '已发布', '实验进行中', '优化炮制工艺', '学生A', '学生B', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'proj-010');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, version, created_at)
SELECT 'proj-011', '中药材市场质量评价与溯源体系', '刘副教授', '招募对中药材流通感兴趣的学生，参与市场调研。', '待审核', '调研阶段', '构建市场质量溯源框架', '', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'proj-011');
INSERT INTO research_project (id, title, leader_name, requirements, status, stage, transformation, applicant_requests, approved_members, version, created_at)
SELECT 'proj-012', '基于光谱技术的药材真伪快速鉴别', '王老师', '招募熟悉光谱分析的学生，参与标准光谱采集。', '已发布', '方案设计', '开发快速鉴别方法', '学生C,学生D', '学生E', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM research_project WHERE id = 'proj-012');

-- ==================== 12. project_application + project_member ====================
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'proj-004-apply-a', 'proj-004', '学生A', '已批准', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'proj-004-apply-a');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'proj-004-apply-d', 'proj-004', '学生D', '已拒绝', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'proj-004-apply-d');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'proj-005-apply-b', 'proj-005', '学生B', '已批准', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'proj-005-apply-b');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'proj-005-apply-c', 'proj-005', '学生C', '已拒绝', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'proj-005-apply-c');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'proj-008-apply-a', 'proj-008', '学生A', '待审批', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'proj-008-apply-a');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'proj-009-apply-d', 'proj-009', '学生D', '已批准', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'proj-009-apply-d');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'proj-012-apply-e', 'proj-012', '学生E', '已批准', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'proj-012-apply-e');
INSERT INTO project_application (id, project_id, student_name, status, applied_at)
SELECT 'proj-006-apply-c', 'proj-006', '学生C', '已批准', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_application WHERE id = 'proj-006-apply-c');

INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'proj-004-mem-a', 'proj-004', '学生A', '学生', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'proj-004-mem-a');
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'proj-005-mem-b', 'proj-005', '学生B', '学生', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'proj-005-mem-b');
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'proj-006-mem-c', 'proj-006', '学生C', '学生', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'proj-006-mem-c');
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'proj-008-mem-a', 'proj-008', '学生A', '学生', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'proj-008-mem-a');
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'proj-009-mem-d', 'proj-009', '学生D', '学生', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'proj-009-mem-d');
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'proj-009-mem-e', 'proj-009', '学生E', '学生', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'proj-009-mem-e');
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'proj-012-mem-e', 'proj-012', '学生E', '学生', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'proj-012-mem-e');
INSERT INTO project_member (id, project_id, member_name, member_role, joined_at)
SELECT 'proj-001-mem-s', 'project-001', '当前学生', '学生', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM project_member WHERE id = 'proj-001-mem-s');

-- ==================== 13. 扩展培训素材（9 条） ====================
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, reviewer_name, review_comment, version, created_at)
SELECT 'train-004', '中药化学图谱解析专题培训', '陈研究员', '研究生和科研助理', '签到、考核、实操报告', '已发布', '系统管理员', '培训内容丰富，图谱解析案例详实。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'train-004');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, reviewer_name, review_comment, version, created_at)
SELECT 'train-005', '中药材种植规范（GAP）培训', '张教授', '种植基地技术人员', '签到、考核记录', '已发布', '系统管理员', 'GAP要点讲解清晰，实用性强。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'train-005');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, reviewer_name, review_comment, version, created_at)
SELECT 'train-006', '溯源码操作培训', '刘副教授', '生产与品管人员', '完成签收、实操考核', '已发布', '系统管理员', '操作流程演示完整。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'train-006');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, reviewer_name, review_comment, version, created_at)
SELECT 'train-007', '药材真伪鉴别技术培训', '王老师', '药材经营人员', '签到、实物鉴别考核', '待审核', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'train-007');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, reviewer_name, review_comment, version, created_at)
SELECT 'train-008', '科研论文写作与数据管理', '张教授', '研究生', '签到、案例作业、论文提纲', '已发布', '系统管理员', '对研究生科研能力提升有较大帮助。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'train-008');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, reviewer_name, review_comment, version, created_at)
SELECT 'train-009', '实验室安全规范培训', '李老师', '全体实验人员', '签到、考核、安全承诺书', '已发布', '系统管理员', '安全规范讲解到位。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'train-009');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, reviewer_name, review_comment, version, created_at)
SELECT 'train-010', '中药质量评价指标解读', '王老师', '教师与培训人员', '课件存储、考核归档', '已发布', '系统管理员', '指标解读专业，评价标准清晰。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'train-010');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, reviewer_name, review_comment, version, created_at)
SELECT 'train-011', '野外药材资源调查方法', '陈研究员', '调查队员和学生', '签到、调查表提交', '已发布', '系统管理员', '调查方法规范，具有实操指导意义。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'train-011');
INSERT INTO training_material (id, title, trainer_name, audience, tracking, status, reviewer_name, review_comment, version, created_at)
SELECT 'train-012', '生物医药大数据平台使用培训', '刘副教授', '全体师生', '签到、操作考核', '已发布', '系统管理员', '平台功能讲解全面。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM training_material WHERE id = 'train-012');

-- ==================== 14. 扩展评价记录（9 条） ====================
INSERT INTO evaluation_record (id, batch_id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, reviewer_name, review_comment, version, created_at)
SELECT 'eval-004', 'batch-herb007-1', '川党参', '外观性状、有效成分含量、生态适应性', 89, '良好', '可用于道地药材认证', '巫山党参种植基地', '张教授', '教师', '已发布', '系统管理员', '综合评价客观合理。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'eval-004');
INSERT INTO evaluation_record (id, batch_id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, reviewer_name, review_comment, version, created_at)
SELECT 'eval-005', 'batch-herb008-1', '青蒿', '青蒿素含量、种植环境、加工工艺', 92, '优秀', '可用于工艺优化和品牌建设', '酉阳青蒿合作社', '王老师', '科研人员', '已发布', '系统管理员', '青蒿素含量达到优质标准。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'eval-005');
INSERT INTO evaluation_record (id, batch_id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, reviewer_name, review_comment, version, created_at)
SELECT 'eval-006', 'batch-herb009-1', '枳壳', '果形、香气、柚皮苷含量', 86, '良好', '可用于产地品质证明', '江津枳壳基地', '刘副教授', '教师', '已发布', '系统管理员', '柚皮苷含量符合药典标准。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'eval-006');
INSERT INTO evaluation_record (id, batch_id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, version, created_at)
SELECT 'eval-007', 'batch-herb001-1', '黄连', '性状、含量、产地生态、传承工艺', 91, '优秀', '可用于非遗及品牌申报材料', '黄连课题组', '张老师', '教师', '已发布', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'eval-007');
INSERT INTO evaluation_record (id, batch_id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, version, created_at)
SELECT 'eval-008', 'batch-herb002-1', '金银花', '绿原酸含量、花蕾完整度、干燥工艺', 88, '良好', '可用于教学案例和品质分级', '秀山金银花基地', '李老师', '教师', '已发布', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'eval-008');
INSERT INTO evaluation_record (id, batch_id, herb_name, indicator, score, result, application_material, subject_owner_name, evaluator_name, evaluator_role, status, version, created_at)
SELECT 'eval-009', 'batch-herb006-1', '佛手', '果形、香气、有效成分、加工工艺', 93, '优秀', '可用于品牌建设和非遗申报', '佛手课题组', '王老师', '科研人员', '已发布', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM evaluation_record WHERE id = 'eval-009');

-- ==================== 15. 扩展业绩记录（9 条） ====================
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, reviewer_name, review_comment, version, created_at)
SELECT 'achv-004', '川党参生态适宜性区划研究', '巫山县农业技术推广中心', '科研成果', '省部级', '待审核', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achv-004');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, reviewer_name, review_comment, version, created_at)
SELECT 'achv-005', '酉阳青蒿素提取工艺优化', '中药材科研中心', '科研成果', '校级重点', '已通过', '系统管理员', '工艺优化方案可行，通过认定。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achv-005');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, reviewer_name, review_comment, version, created_at)
SELECT 'achv-006', '中药化学图谱解析虚拟仿真实验', '药学院', '教学建设', '国家级', '已通过', '系统管理员', '获批国家级虚拟仿真实验项目。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achv-006');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, reviewer_name, review_comment, version, created_at)
SELECT 'achv-007', '中药材溯源体系推广示范', '中药学院', '社会服务', '省部级', '待审核', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achv-007');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, reviewer_name, review_comment, version, created_at)
SELECT 'achv-008', '中药质量评价标准体系研究', '中药材数字信息实验室', '科研成果', '国家级', '已通过', '系统管理员', '完成质量标准体系构建，成果突出。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achv-008');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, reviewer_name, review_comment, version, created_at)
SELECT 'achv-009', '中药材加工炮制教学改革', '药学院', '教学建设', '院级', '已驳回', '系统管理员', '需补充改革效果对比数据。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achv-009');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, reviewer_name, review_comment, version, created_at)
SELECT 'achv-010', '科研数据管理规范制定', '科研处', '科研成果', '校级', '已通过', '系统管理员', '规范已发布实施。', 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achv-010');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, reviewer_name, review_comment, version, created_at)
SELECT 'achv-011', '生物医药数字信息系统推广应用', '中药学院', '社会服务', '校级重点', '待审核', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achv-011');
INSERT INTO achievement_record (id, title, owner_name, category, level_name, status, reviewer_name, review_comment, version, created_at)
SELECT 'achv-012', '林下黄精栽培技术培训与推广', '南川区林业科技中心', '社会服务', '省部级', '待审核', NULL, NULL, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_record WHERE id = 'achv-012');

-- ==================== 16. V4 表：evaluation_issue ====================
INSERT INTO evaluation_issue (id, evaluation_id, source_analysis_id, batch_id, title, description, severity, improvement_target, status, creator_name, version, created_at) VALUES
('ei-001', 'eval-004', NULL, 'batch-herb007-1', '川党参种质纯度待提升', '部分样本出现种质混杂现象，需加强种源鉴定。', 'medium', '建立种源鉴定SOP，纯度提升至95%以上', 'open', '张教授', 0, CURRENT_TIMESTAMP),
('ei-002', 'eval-005', 'ga-004', 'batch-herb008-1', '青蒿素含量波动较大', '不同批次青蒿素含量变异系数达12%，需优化采收窗口。', 'high', '确定最佳采收期，控制含量变异系数<8%', 'in_progress', '王老师', 0, CURRENT_TIMESTAMP),
('ei-003', 'eval-006', NULL, 'batch-herb009-1', '枳壳果形不匀问题', '部分果实果形偏小，可能与修剪管理有关。', 'low', '改进修剪方案，提高一级果率', 'open', '刘副教授', 0, CURRENT_TIMESTAMP),
('ei-004', 'eval-001', NULL, 'batch-herb001-1', '黄连采收期记录不规范', '部分批次采收日期未及时录入系统。', 'medium', '规范采收记录流程，实现100%当日录入', 'closed', '张老师', 0, CURRENT_TIMESTAMP);

-- ==================== 17. V4 表：improvement_training_task ====================
INSERT INTO improvement_training_task (id, issue_id, training_material_id, title, assignee_name, due_date, completion_note, pre_score, post_score, status, creator_name, completed_at, version, created_at) VALUES
('itt-001', 'ei-001', NULL, '种质鉴定技术培训', '学生A', '2026-08-15', NULL, 65.00, NULL, 'pending', '张教授', NULL, 0, CURRENT_TIMESTAMP),
('itt-002', 'ei-002', 'train-004', '青蒿素含量检测方法培训', '学生B', '2026-07-30', '已完成培训，掌握HPLC检测方法。', 60.00, 88.00, 'completed', '王老师', '2026-07-10 16:00:00', 0, CURRENT_TIMESTAMP),
('itt-003', 'ei-003', 'train-006', '果树修剪技术培训', '学生C', '2026-08-01', NULL, NULL, NULL, 'pending', '刘副教授', NULL, 0, CURRENT_TIMESTAMP);

-- ==================== 18. V4 表：achievement_evidence ====================
INSERT INTO achievement_evidence (id, achievement_id, issue_id, source_type, source_id, evidence_title, evidence_snapshot, creator_name, created_at) VALUES
('ae-001', 'achv-005', 'ei-002', 'evaluation_record', 'eval-005', '青蒿评价记录作为工艺优化依据', '青蒿工艺优化基于eval-005评价结果和ei-002改进项', '王老师', CURRENT_TIMESTAMP),
('ae-002', 'achv-006', NULL, 'teaching_resource', 'resource-011', '化学图谱解析课件作为教学成果支撑', '国家级虚拟仿真实验项目依托resource-011建设', '陈研究员', CURRENT_TIMESTAMP);

-- ==================== 19. V5 表：quality_metric_definition ====================
INSERT INTO quality_metric_definition (id, metric_code, metric_name, herb_id, source_type, source_field, unit_name, minimum_value, maximum_value, target_value, weight_value, version_no, status, created_by, created_at) VALUES
('qmd-001', 'TEMP_MIN', '最低温度耐受', 'herb-001', 'growth_record', 'temperature', '°C', 12.0000, 28.0000, 18.0000, 1.00, 1, 'active', '系统管理员', CURRENT_TIMESTAMP),
('qmd-002', 'TEMP_OPT', '最适温度', 'herb-001', 'growth_record', 'temperature', '°C', 15.0000, 25.0000, 20.0000, 1.50, 1, 'active', '系统管理员', CURRENT_TIMESTAMP),
('qmd-003', 'HUMID_OPT', '最适湿度', 'herb-001', 'growth_record', 'humidity', '%', 70.0000, 90.0000, 82.0000, 1.00, 1, 'active', '系统管理员', CURRENT_TIMESTAMP),
('qmd-004', 'SOIL_PH', '土壤酸碱度', 'herb-001', 'growth_record', 'soil_ph', 'pH', 5.5000, 7.0000, 6.2000, 1.00, 1, 'active', '系统管理员', CURRENT_TIMESTAMP),
('qmd-005', 'TEMP_MIN_QH', '青蒿最低温度', 'herb-008', 'growth_record', 'temperature', '°C', 15.0000, 32.0000, 24.0000, 1.00, 1, 'active', '王老师', CURRENT_TIMESTAMP);

-- ==================== 20. V5 表：batch_metric_result ====================
INSERT INTO batch_metric_result (id, batch_id, metric_definition_id, metric_code, metric_name, measured_value, unit_name, judgement, score, source_count, calculation_note, calculated_by, calculated_at) VALUES
('bmr-001', 'batch-herb001-1', 'qmd-001', 'TEMP_MIN', '最低温度耐受', 19.6000, '°C', 'PASS', 90.00, 9, '基于9条生长记录计算', '李老师', CURRENT_TIMESTAMP),
('bmr-002', 'batch-herb001-1', 'qmd-002', 'TEMP_OPT', '最适温度', 19.6000, '°C', 'PASS', 85.00, 9, '温度均值在适宜范围内', '李老师', CURRENT_TIMESTAMP),
('bmr-003', 'batch-herb001-1', 'qmd-003', 'HUMID_OPT', '最适湿度', 83.5000, '%', 'PASS', 88.00, 9, '湿度均值在适宜范围', '李老师', CURRENT_TIMESTAMP),
('bmr-004', 'batch-herb001-1', 'qmd-004', 'SOIL_PH', '土壤酸碱度', 6.2000, 'pH', 'PASS', 95.00, 9, '土壤PH稳定', '李老师', CURRENT_TIMESTAMP),
('bmr-005', 'batch-herb008-1', 'qmd-005', 'TEMP_MIN_QH', '青蒿最低温度', 25.3000, '°C', 'PASS', 82.00, 3, '基于3条记录均值', '王老师', CURRENT_TIMESTAMP);

-- ==================== 21. V6 表：evaluation_scheme ====================
INSERT INTO evaluation_scheme (id, scheme_code, scheme_name, evaluation_type, herb_id, passing_score, version_no, effective_date, status, created_by, created_at) VALUES
('es-001', 'SCHM-HL-001', '黄连质量综合评价方案', 'multi_metric', 'herb-001', 60.00, 1, '2026-01-01', 'active', '系统管理员', CURRENT_TIMESTAMP),
('es-002', 'SCHM-QH-001', '青蒿质量评价方案', 'multi_metric', 'herb-008', 60.00, 1, '2026-01-01', 'active', '王老师', CURRENT_TIMESTAMP);

INSERT INTO evaluation_scheme_item (id, scheme_id, metric_definition_id, weight_value, minimum_item_score, veto_flag, created_at) VALUES
('esi-001', 'es-001', 'qmd-001', 1.00, 30.00, 0, CURRENT_TIMESTAMP),
('esi-002', 'es-001', 'qmd-002', 1.50, 30.00, 0, CURRENT_TIMESTAMP),
('esi-003', 'es-001', 'qmd-003', 1.00, 30.00, 0, CURRENT_TIMESTAMP),
('esi-004', 'es-001', 'qmd-004', 1.00, 30.00, 0, CURRENT_TIMESTAMP),
('esi-005', 'es-002', 'qmd-005', 1.00, 30.00, 0, CURRENT_TIMESTAMP);

-- ==================== 22. V6 表：multi_metric_evaluation + detail ====================
INSERT INTO multi_metric_evaluation (id, scheme_id, scheme_version, batch_id, evaluation_record_id, total_score, grade_name, result, conclusion, evaluator_name, status, created_at) VALUES
('mme-001', 'es-001', 1, 'batch-herb001-1', 'eval-001', 88.50, 'A', 'PASS', '黄连生长综合质量优良，各指标均达标。', '李老师', 'published', CURRENT_TIMESTAMP),
('mme-002', 'es-002', 1, 'batch-herb008-1', 'eval-005', 82.00, 'B', 'PASS', '青蒿质量良好，建议优化采收时间。', '王老师', 'published', CURRENT_TIMESTAMP);

INSERT INTO multi_metric_evaluation_detail (id, evaluation_id, metric_definition_id, metric_code, metric_name, metric_version, measured_value, unit_name, judgement, raw_score, weight_value, weighted_score, source_result_id, source_count, created_at) VALUES
('mmed-001', 'mme-001', 'qmd-001', 'TEMP_MIN', '最低温度耐受', 1, 19.6000, '°C', 'PASS', 90.00, 1.00, 90.00, 'bmr-001', 9, CURRENT_TIMESTAMP),
('mmed-002', 'mme-001', 'qmd-002', 'TEMP_OPT', '最适温度', 1, 19.6000, '°C', 'PASS', 85.00, 1.50, 127.50, 'bmr-002', 9, CURRENT_TIMESTAMP),
('mmed-003', 'mme-001', 'qmd-003', 'HUMID_OPT', '最适湿度', 1, 83.5000, '%', 'PASS', 88.00, 1.00, 88.00, 'bmr-003', 9, CURRENT_TIMESTAMP),
('mmed-004', 'mme-001', 'qmd-004', 'SOIL_PH', '土壤酸碱度', 1, 6.2000, 'pH', 'PASS', 95.00, 1.00, 95.00, 'bmr-004', 9, CURRENT_TIMESTAMP),
('mmed-005', 'mme-002', 'qmd-005', 'TEMP_MIN_QH', '青蒿最低温度', 1, 25.3000, '°C', 'PASS', 82.00, 1.00, 82.00, 'bmr-005', 3, CURRENT_TIMESTAMP);

-- ==================== 23. V7 表：training_material_tag ====================
INSERT INTO training_material_tag (id, training_material_id, metric_code, issue_type, herb_id, role_code, priority_value, created_at) VALUES
('tmt-001', 'train-004', 'TEMP_MIN', '技术培训', 'herb-001', 'student', 80, CURRENT_TIMESTAMP),
('tmt-002', 'train-004', 'TEMP_OPT', '技术培训', 'herb-001', 'student', 80, CURRENT_TIMESTAMP),
('tmt-003', 'train-006', NULL, '操作规范', 'herb-009', 'teacher', 60, CURRENT_TIMESTAMP),
('tmt-004', 'train-010', NULL, '评价标准', NULL, 'teacher', 70, CURRENT_TIMESTAMP);

-- ==================== 24. V7 表：improvement_recommendation ====================
INSERT INTO improvement_recommendation (id, multi_evaluation_id, evaluation_detail_id, metric_code, issue_title, issue_description, severity, improvement_target, recommended_material_id, recommendation_reason, status, generated_at, handled_by, handled_at) VALUES
('ir-001', 'mme-002', 'mmed-005', 'TEMP_MIN_QH', '青蒿温度管理建议', '青蒿生长温度波动较大，建议加强环境监测。', 'medium', '稳定温度在24-28°C之间', 'train-004', '推荐参与图谱解析培训以提升数据解读能力', 'pending', CURRENT_TIMESTAMP, NULL, NULL),
('ir-002', 'mme-001', 'mmed-002', 'TEMP_OPT', '黄连温度管理建议', '黄连温度指标评分良好，建议持续监测。', 'low', '维持当前温控管理', NULL, '当前管理方案有效，无需额外培训', 'closed', CURRENT_TIMESTAMP, '系统管理员', CURRENT_TIMESTAMP);

-- ==================== 25. V8 表：achievement_scoring_rule ====================
INSERT INTO achievement_scoring_rule (id, rule_code, rule_name, base_points, improvement_factor, max_points, version_no, status, effective_date, created_at) VALUES
('asr-001', 'RULE-NATL', '国家级成果', 100.00, 1.50, 200.00, 1, 'active', '2026-01-01', CURRENT_TIMESTAMP),
('asr-002', 'RULE-PROV', '省部级成果', 60.00, 1.30, 120.00, 1, 'active', '2026-01-01', CURRENT_TIMESTAMP),
('asr-003', 'RULE-SCH-KEY', '校级重点', 30.00, 1.20, 60.00, 1, 'active', '2026-01-01', CURRENT_TIMESTAMP),
('asr-004', 'RULE-SCH', '校级/院级', 15.00, 1.10, 30.00, 1, 'active', '2026-01-01', CURRENT_TIMESTAMP);

-- ==================== 26. V8 表：achievement_quantification ====================
INSERT INTO achievement_quantification (id, achievement_evidence_id, issue_id, rule_id, rule_version, initial_score, recheck_score, improvement_value, base_points, suggested_points, confirmed_points, status, confirmed_by, confirmed_at, created_at) VALUES
('aq-001', 'ae-001', 'ei-002', 'asr-002', 1, 75.00, 88.00, 13.00, 60.00, 73.00, 70.00, 'confirmed', '系统管理员', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('aq-002', 'ae-002', 'ei-003', 'asr-001', 1, 90.00, 95.00, 5.00, 100.00, 105.00, 100.00, 'confirmed', '系统管理员', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==================== 27. V9 表：mobile_collection_device ====================
INSERT INTO mobile_collection_device (id, device_code, device_name, owner_name, platform_name, token_hash, status, last_seen_at, created_by, created_at) VALUES
('mcd-001', 'DEV-APP-001', '学生A手机采集端', '学生A', 'Android 14', SHA2('mobile-token-device-001', 256), 'active', '2026-07-10 08:30:00', '系统管理员', CURRENT_TIMESTAMP),
('mcd-002', 'DEV-APP-002', '学生B手机采集端', '学生B', 'iOS 17.5', SHA2('mobile-token-device-002', 256), 'active', '2026-07-09 16:20:00', '系统管理员', CURRENT_TIMESTAMP),
('mcd-003', 'DEV-GW-001', '石柱黄连网关', '张老师', 'Linux IoT', SHA2('gateway-token-001', 256), 'active', '2026-07-11 06:00:00', '系统管理员', CURRENT_TIMESTAMP);

-- ==================== 28. V9 表：mobile_sync_record ====================
INSERT INTO mobile_sync_record (id, device_id, client_record_id, growth_record_id, sync_status, error_message, received_at) VALUES
('msr-001', 'mcd-001', 'CLI-20260701-001', 'growth-011', 'success', NULL, '2026-05-20 09:15:00'),
('msr-002', 'mcd-002', 'CLI-20260702-001', 'growth-013', 'success', NULL, '2026-05-15 11:35:00'),
('msr-003', 'mcd-002', 'CLI-20260702-002', NULL, 'failed', 'GPS信号弱，定位超时', '2026-06-15 10:50:00');

-- ==================== 29. V11 表：integration_client ====================
INSERT INTO integration_client (id, client_code, client_name, secret_ciphertext, secret_iv, status, allowed_ips, created_by, created_at) VALUES
('icl-001', 'SCHOOL-EDU', '校内教务系统', 'RHVtbXlFbmNyeXB0ZWRWYWx1ZUZvclRlc3Rpbmc=', 'initvec12345678', 'active', '10.0.0.0/8,172.16.0.0/12', '系统管理员', CURRENT_TIMESTAMP);

-- ==================== 30. V11 表：integration_nonce ====================
INSERT INTO integration_nonce (id, client_id, nonce_value, request_timestamp, expires_at, created_at) VALUES
('in-001', 'icl-001', 'a1b2c3d4e5f6', 1720800000000, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 HOUR), CURRENT_TIMESTAMP);

-- ==================== 31. V13 表：integration_audit_log ====================
INSERT INTO integration_audit_log (id, request_id, protocol_type, operation_name, caller_type, caller_identifier, http_method, request_path, source_ip, request_digest, request_bytes, http_status, success_flag, result_code, duration_ms, occurred_at) VALUES
('ial-001', UUID(), 'SOAP', 'getHerbData', 'APP', 'student@cqutcm', 'POST', '/api/soap/school', '192.168.1.100', SHA2('req-body-001', 256), 2048, 200, 1, 'SUCCESS', 156, '2026-07-10 09:00:00'),
('ial-002', UUID(), 'SOAP', 'getHerbData', 'APP', 'teacher@cqutcm', 'POST', '/api/soap/school', '192.168.1.101', SHA2('req-body-002', 256), 1536, 200, 1, 'SUCCESS', 98, '2026-07-10 10:30:00'),
('ial-003', UUID(), 'REST', 'syncBatch', 'APP', 'student-a', 'POST', '/api/mobile/sync', '10.0.0.55', SHA2('sync-data-003', 256), 4096, 401, 0, 'AUTH_FAILED', 12, '2026-07-11 07:15:00');

-- ==================== 32. achievement_standard 补充 ====================
INSERT INTO achievement_standard (id, name, category, level_rule, effective_date, created_at)
SELECT 'standard-002', '科研成果认定标准', '科研成果', '国家级/省部级/市厅级/校级', '2026-01-01', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_standard WHERE id = 'standard-002');
INSERT INTO achievement_standard (id, name, category, level_rule, effective_date, created_at)
SELECT 'standard-003', '社会服务认定标准', '社会服务', '省部级/校级重点/校级/院级', '2026-01-01', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM achievement_standard WHERE id = 'standard-003');
