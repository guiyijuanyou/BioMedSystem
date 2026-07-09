USE biomed;
SET NAMES utf8mb4;

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

INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-001', '中药材显微鉴定实验', '张老师', 4, '视频+讲义', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-001');

INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-002', '中药材生长数据采集实验', '李老师', 6, '视频+数据模板', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-002');

INSERT INTO course (id, title, teacher_name, hours, material_type, status, created_at)
SELECT 'course-003', '药材溯源码与图谱比对实训', '王老师', 3, '图谱文件+案例', '已发布', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = 'course-003');
