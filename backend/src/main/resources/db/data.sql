INSERT INTO sys_role (id, code, name, description, created_at) SELECT 'role-admin', 'admin', '管理员', '系统管理与审核', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 'role-admin');
INSERT INTO sys_role (id, code, name, description, created_at) SELECT 'role-teacher', 'teacher', '教师', '课程教学与课题管理', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 'role-teacher');
INSERT INTO sys_role (id, code, name, description, created_at) SELECT 'role-researcher', 'researcher', '科研人员', '科研数据与课题研究', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 'role-researcher');
INSERT INTO sys_role (id, code, name, description, created_at) SELECT 'role-student', 'student', '学生', '课程学习与课题申请', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 'role-student');

INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at) SELECT 'user-admin', 'admin', '系统管理员', '123456', '系统管理部', 'enabled', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-admin');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at) SELECT 'user-teacher', 'teacher', '李老师', '123456', '教学科研部', 'enabled', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-teacher');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at) SELECT 'user-researcher', 'researcher', '王老师', '123456', '中药材科研中心', 'enabled', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-researcher');
INSERT INTO sys_user (id, username, display_name, password_hash, department, status, created_at) SELECT 'user-student', 'student', '当前学生', '123456', '生物医药学院', 'enabled', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 'user-student');

INSERT INTO sys_user_role (user_id, role_id, created_at) SELECT 'user-admin', 'role-admin', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-admin' AND role_id = 'role-admin');
INSERT INTO sys_user_role (user_id, role_id, created_at) SELECT 'user-teacher', 'role-teacher', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-teacher' AND role_id = 'role-teacher');
INSERT INTO sys_user_role (user_id, role_id, created_at) SELECT 'user-researcher', 'role-researcher', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-researcher' AND role_id = 'role-researcher');
INSERT INTO sys_user_role (user_id, role_id, created_at) SELECT 'user-student', 'role-student', CURRENT_TIMESTAMP WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 'user-student' AND role_id = 'role-student');

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

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'herb-001', 'herbs', '{"name":"\u9ec4\u8fde","district":"\u77f3\u67f1\u53bf","longitude":"108.12","latitude":"30.00","scale":"3200\u4ea9","environment":"\u6d77\u62d4\u9ad8\u3001\u6e7f\u6da6\u9634\u51c9","traceCode":"CQ-HL-001"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'herb-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'herb-002', 'herbs', '{"name":"\u91d1\u94f6\u82b1","district":"\u79c0\u5c71\u53bf","longitude":"109.00","latitude":"28.45","scale":"1800\u4ea9","environment":"\u4e18\u9675\u5761\u5730\u3001\u65e5\u7167\u5145\u8db3","traceCode":"CQ-JYH-002"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'herb-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'herb-003', 'herbs', '{"name":"\u5929\u9ebb","district":"\u5deb\u6eaa\u53bf","longitude":"109.63","latitude":"31.40","scale":"950\u4ea9","environment":"\u6797\u4e0b\u4eff\u91ce\u751f\u79cd\u690d","traceCode":"CQ-TM-003"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'herb-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'herb-004', 'herbs', '{"name":"\u675c\u4ef2","district":"\u57ab\u6c5f\u53bf","longitude":"107.35","latitude":"30.33","scale":"2100\u4ea9","environment":"\u4f4e\u5c71\u4e18\u9675\u3001\u571f\u5c42\u6df1\u539a\u3001\u6392\u6c34\u826f\u597d","traceCode":"CQ-DZ-004"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'herb-004');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'herb-005', 'herbs', '{"name":"\u767d\u672f","district":"\u9149\u9633\u53bf","longitude":"108.77","latitude":"28.84","scale":"1650\u4ea9","environment":"\u6e29\u6da6\u51c9\u723d\u3001\u5bcc\u542b\u8150\u6b96\u8d28\u571f\u58e4","traceCode":"CQ-BZ-005"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'herb-005');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'herb-006', 'herbs', '{"name":"\u4f5b\u624b","district":"\u6c5f\u6d25\u533a","longitude":"106.26","latitude":"29.29","scale":"2400\u4ea9","environment":"\u6c14\u5019\u6e29\u6696\u3001\u5149\u7167\u5145\u8db3\u3001\u9002\u5408\u67d1\u6a58\u7c7b\u836f\u6750","traceCode":"CQ-FS-006"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'herb-006');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-001', 'growth-records', '{"herbName":"\u9ec4\u8fde","district":"\u77f3\u67f1\u53bf","temperature":"19.6","humidity":"83","soilPh":"6.2","collector":"\u624b\u673aAPP\u91c7\u96c6","recordedAt":"2026-06-06T11:00:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-002', 'growth-records', '{"herbName":"\u5929\u9ebb","district":"\u5deb\u6eaa\u53bf","temperature":"17.1","humidity":"78","soilPh":"6.8","collector":"\u4f20\u611f\u5668\u7f51\u5173","recordedAt":"2026-06-06T12:00:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-003', 'growth-records', '{"herbName":"\u675c\u4ef2","district":"\u57ab\u6c5f\u53bf","temperature":"22.4","humidity":"71","soilPh":"6.7","collector":"\u624b\u673aAPP\u91c7\u96c6","recordedAt":"2026-06-06T13:20:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-004', 'growth-records', '{"herbName":"\u767d\u672f","district":"\u9149\u9633\u53bf","temperature":"18.8","humidity":"82","soilPh":"6.3","collector":"\u4f20\u611f\u5668\u7f51\u5173","recordedAt":"2026-06-06T14:10:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-004');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-005', 'growth-records', '{"herbName":"\u4f5b\u624b","district":"\u6c5f\u6d25\u533a","temperature":"25.6","humidity":"68","soilPh":"6.9","collector":"\u7535\u8111\u7ec8\u7aef\u5f55\u5165","recordedAt":"2026-06-06T15:00:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-005');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-006', 'growth-records', '{"herbName":"榛勮繛","district":"鐭虫煴鍘?,"temperature":"18.9","humidity":"86","soilPh":"6.1","growthStage":"钀岃娊鏈?,"collector":"浼犳劅鍣ㄧ綉鍏?,"recordedAt":"2026-06-01T09:00:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-006');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-007', 'growth-records', '{"herbName":"榛勮繛","district":"鐭虫煴鍘?,"temperature":"19.4","humidity":"84","soilPh":"6.2","growthStage":"灞曞彾鏈?,"collector":"鎵嬫満APP閲囬泦","recordedAt":"2026-06-08T09:20:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-007');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-008', 'growth-records', '{"herbName":"榛勮繛","district":"鐭虫煴鍘?,"temperature":"20.2","humidity":"82","soilPh":"6.3","growthStage":"鐢熼暱鏈?,"collector":"浼犳劅鍣ㄧ綉鍏?,"recordedAt":"2026-06-15T09:15:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-008');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-009', 'growth-records', '{"herbName":"榛勮繛","district":"鐭虫煴鍘?,"temperature":"20.7","humidity":"80","soilPh":"6.4","growthStage":"鏃洪暱鏈?,"collector":"鐢佃剳缁堢褰曞叆","recordedAt":"2026-06-22T10:05:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-009');

UPDATE generic_record
SET payload = REPLACE(payload, '"recordedAt"', '"recorder":"褰撳墠瀛︾敓","recorderRole":"瀛︾敓","recordedAt"')
WHERE resource_type = 'growth-records' AND payload NOT LIKE '%"recorder"%';

UPDATE generic_record
SET payload = '{"herbName":"榛勮繛","district":"鐭虫煴鍘?,"temperature":"18.9","humidity":"86","soilPh":"6.1","growthStage":"钀岃娊鏈?,"collector":"浼犳劅鍣ㄧ綉鍏?,"recorder":"鏉庤€佸笀","recorderRole":"鏁欏笀","recordedAt":"2026-06-01T09:00:00"}'
WHERE id = 'growth-006' AND resource_type = 'growth-records';

UPDATE generic_record
SET payload = '{"herbName":"榛勮繛","district":"鐭虫煴鍘?,"temperature":"20.7","humidity":"80","soilPh":"6.4","growthStage":"鏃洪暱鏈?,"collector":"鐢佃剳缁堢褰曞叆","recorder":"寮犺€佸笀","recorderRole":"鏁欏笀","recordedAt":"2026-06-22T10:05:00"}'
WHERE id = 'growth-009' AND resource_type = 'growth-records';

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-010', 'growth-records', '{"herbName":"閲戦摱鑺?,"district":"绉€灞卞幙","temperature":"23.1","humidity":"69","soilPh":"6.8","growthStage":"鎶芥灊鏈?,"collector":"鎵嬫満APP閲囬泦","recordedAt":"2026-06-02T08:40:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-010');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-011', 'growth-records', '{"herbName":"閲戦摱鑺?,"district":"绉€灞卞幙","temperature":"24.0","humidity":"66","soilPh":"6.9","growthStage":"鐜拌暰鏈?,"collector":"浼犳劅鍣ㄧ綉鍏?,"recordedAt":"2026-06-09T08:50:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-011');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-012', 'growth-records', '{"herbName":"閲戦摱鑺?,"district":"绉€灞卞幙","temperature":"25.2","humidity":"64","soilPh":"7.0","growthStage":"鑺辫暰鏈?,"collector":"浼犳劅鍣ㄧ綉鍏?,"recordedAt":"2026-06-16T08:30:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-012');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-013', 'growth-records', '{"herbName":"澶╅夯","district":"宸邯鍘?,"temperature":"16.8","humidity":"79","soilPh":"6.7","growthStage":"鍑鸿娊鏈?,"collector":"浼犳劅鍣ㄧ綉鍏?,"recordedAt":"2026-06-03T11:00:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-013');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-014', 'growth-records', '{"herbName":"澶╅夯","district":"宸邯鍘?,"temperature":"17.5","humidity":"77","soilPh":"6.8","growthStage":"鍧楄寧鑶ㄥぇ鏈?,"collector":"鎵嬫満APP閲囬泦","recordedAt":"2026-06-10T11:10:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-014');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-015', 'growth-records', '{"herbName":"澶╅夯","district":"宸邯鍘?,"temperature":"18.0","humidity":"76","soilPh":"6.9","growthStage":"鍧楄寧鑶ㄥぇ鏈?,"collector":"鐢佃剳缁堢褰曞叆","recordedAt":"2026-06-17T11:20:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-015');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'spectrum-compare-001', 'spectrum-comparisons', '{"herbName":"榛勮繛","sampleCode":"HL-SZ-20260601","district":"鐭虫煴鍘?,"spectrumType":"HPLC 鎸囩汗鍥捐氨","referenceName":"閲嶅簡榛勮繛鏍囧噯鍥捐氨 V1","similarity":"94.6","result":"閫氳繃","operator":"寮犺€佸笀","comparedAt":"2026-06-06T16:00:00","remark":"涓诲嘲淇濈暀鏃堕棿绋冲畾锛岀壒寰佸嘲鍖归厤搴﹂珮"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'spectrum-compare-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'spectrum-compare-002', 'spectrum-comparisons', '{"herbName":"閲戦摱鑺?,"sampleCode":"JYH-XS-20260602","district":"绉€灞卞幙","spectrumType":"钖勫眰鑹茶氨鍥捐氨","referenceName":"閲戦摱鑺辫杽灞傞壌鍒爣鍑嗗浘璋?,"similarity":"88.2","result":"寤鸿澶嶆牳","operator":"鏉庤€佸笀","comparedAt":"2026-06-07T10:30:00","remark":"灞€閮ㄦ枒鐐归鑹插亸娴咃紝寤鸿琛ュ厖澶嶆祴"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'spectrum-compare-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'spectrum-compare-003', 'spectrum-comparisons', '{"herbName":"澶╅夯","sampleCode":"TM-WX-20260603","district":"宸邯鍘?,"spectrumType":"绾㈠鍥捐氨","referenceName":"澶╅夯绾㈠鏍囧噯鍥捐氨","similarity":"91.3","result":"閫氳繃","operator":"鐜嬭€佸笀","comparedAt":"2026-06-08T14:20:00","remark":"鏍锋湰鍥捐氨涓庡弬鑰冨浘璋辨暣浣撲竴鑷?}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'spectrum-compare-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-analysis-001', 'growth-analysis', '{"analysisName":"鐭虫煴榛勮繛鍏湀鐢熼暱瓒嬪娍鍒嗘瀽","herbName":"榛勮繛","district":"鐭虫煴鍘?,"indicator":"娓╁害銆佹箍搴︺€佸湡澹H","baseline":"2026-06-01 棣栨璁板綍","currentValue":"2026-06-22 鏈€鏂拌褰?,"difference":"娓╁害 +1.8锛屾箍搴?-6锛孭H +0.3","trend":"娓╁害涓婂崌銆佹箍搴︿笅闄嶃€丳H绋冲畾","conclusion":"榛勮繛澶勪簬鏃洪暱鏈燂紝娓╂箍搴﹀彉鍖栦粛鍦ㄩ€傚疁鑼冨洿鍐咃紝寤鸿淇濇寔閬槾涓庡湡澹や繚婀裤€?,"analyst":"鏉庤€佸笀","analyzedAt":"2026-06-22T17:00:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-analysis-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-analysis-002', 'growth-analysis', '{"analysisName":"绉€灞遍噾閾惰姳鑺辫暰鏈熺幆澧冨姣?,"herbName":"閲戦摱鑺?,"district":"绉€灞卞幙","indicator":"娓╁害銆佹箍搴?,"baseline":"2026-06-02 鎶芥灊鏈?,"currentValue":"2026-06-16 鑺辫暰鏈?,"difference":"娓╁害 +2.1锛屾箍搴?-5","trend":"娓╁害涓婂崌銆佹箍搴︿笅闄?,"conclusion":"鑺辫暰鏈熸俯搴﹀崌楂樿緝鏄庢樉锛屽簲鍏虫敞杩炵画楂樻俯瀵硅姳钑捐川閲忕殑褰卞搷銆?,"analyst":"鐜嬭€佸笀","analyzedAt":"2026-06-16T16:30:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-analysis-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'course-001', 'courses', '{"title":"\u4e2d\u836f\u6750\u663e\u5fae\u9274\u5b9a\u5b9e\u9a8c","teacher":"\u5f20\u8001\u5e08","hours":"4","materialType":"\u89c6\u9891+\u8bb2\u4e49","status":"\u5df2\u53d1\u5e03"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'course-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'course-002', 'courses', '{"title":"\u4e2d\u836f\u6750\u751f\u957f\u6570\u636e\u91c7\u96c6\u5b9e\u9a8c","teacher":"\u674e\u8001\u5e08","hours":"3","materialType":"\u89c6\u9891+\u91c7\u96c6\u8868","status":"\u5df2\u53d1\u5e03"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'course-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'course-003', 'courses', '{"title":"\u836f\u6750\u6eaf\u6e90\u7801\u4e0e\u56fe\u8c31\u6bd4\u5bf9\u5b9e\u8bad","teacher":"\u738b\u8001\u5e08","hours":"6","materialType":"\u5b9e\u64cd\u89c6\u9891+\u56fe\u8c31\u6587\u4ef6","status":"\u5f85\u5ba1\u6838"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'course-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'project-001', 'projects', '{"title":"\u91cd\u5e86\u9053\u5730\u836f\u6750\u751f\u6001\u9002\u5e94\u6027\u7814\u7a76","leader":"\u674e\u8001\u5e08","stage":"\u6570\u636e\u91c7\u96c6\u4e2d","transformation":"\u79cd\u690d\u89c4\u8303\u8f6c\u5316"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'project-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'project-002', 'projects', '{"title":"\u4e2d\u836f\u6750\u975e\u9057\u7533\u62a5\u8bc4\u4ef7\u7d20\u6750\u5efa\u8bbe","leader":"\u5f20\u8001\u5e08","stage":"\u8bc4\u4ef7\u8d44\u6599\u6574\u7406","transformation":"\u5f62\u6210\u975e\u9057\u7533\u62a5\u8d44\u6599\u5e93"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'project-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'project-003', 'projects', '{"title":"\u4f5b\u624b\u4ea7\u5730\u751f\u6001\u56e0\u5b50\u4e0e\u54c1\u8d28\u5173\u8054\u5206\u6790","leader":"\u738b\u8001\u5e08","stage":"\u6570\u636e\u5bf9\u6bd4\u5206\u6790","transformation":"\u5efa\u7acb\u4ea7\u5730\u54c1\u8d28\u8bc4\u4ef7\u6a21\u578b"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'project-003');

UPDATE generic_record
SET payload = '{"title":"閲嶅簡閬撳湴鑽潗鐢熸€侀€傚簲鎬х爺绌?,"leader":"鏉庤€佸笀","requirements":"鎷涘嫙2鍚嶅鐢燂紝瑕佹眰鑳藉弬涓庨噹澶栭噰鏍枫€佹帉鎻″熀纭€鏁版嵁褰曞叆锛屾寜鍛ㄦ彁浜ら噰闆嗚褰曘€?,"status":"宸插彂甯?,"stage":"鏁版嵁閲囬泦涓?,"applicantRequests":"褰撳墠瀛︾敓","approvedMembers":"","rejectedApplicants":"","transformation":"绉嶆瑙勮寖杞寲"}'
WHERE id = 'project-001' AND resource_type = 'projects' AND payload NOT LIKE '%requirements%';

UPDATE generic_record
SET payload = '{"title":"涓嵂鏉愰潪閬楃敵鎶ヨ瘎浠风礌鏉愬缓璁?,"leader":"寮犺€佸笀","requirements":"鎷涘嫙鐔熸倝鏂囩尞鏁寸悊鍜屽奖鍍忚祫鏂欏綊妗ｇ殑瀛︾敓锛岄渶瀹屾垚璇勪环绱犳潗鏍囨敞涓庤繃绋嬭褰曘€?,"status":"宸插彂甯?,"stage":"璇勪环璧勬枡鏁寸悊","applicantRequests":"","approvedMembers":"瀛︾敓A","rejectedApplicants":"瀛︾敓B","transformation":"褰㈡垚闈為仐鐢虫姤璧勬枡搴?}'
WHERE id = 'project-002' AND resource_type = 'projects' AND payload NOT LIKE '%requirements%';

UPDATE generic_record
SET payload = '{"title":"浣涙墜浜у湴鐢熸€佸洜瀛愪笌鍝佽川鍏宠仈鍒嗘瀽","leader":"鐜嬭€佸笀","requirements":"鎷涘嫙鍏峰鍥捐氨姣斿鎴栫粺璁″垎鏋愬熀纭€鐨勫鐢燂紝鍙備笌鏍锋湰鏁版嵁娓呮礂鍜屽搧璐ㄦā鍨嬮獙璇併€?,"status":"寰呭鏍?,"stage":"鏁版嵁瀵规瘮鍒嗘瀽","applicantRequests":"","approvedMembers":"","rejectedApplicants":"","transformation":"寤虹珛浜у湴鍝佽川璇勪环妯″瀷"}'
WHERE id = 'project-003' AND resource_type = 'projects' AND payload NOT LIKE '%requirements%';

UPDATE generic_record
SET payload = REPLACE(payload, '"transformation"', '"rejectedApplicants":"","transformation"')
WHERE resource_type = 'projects' AND payload LIKE '%"requirements"%' AND payload NOT LIKE '%rejectedApplicants%';

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'training-001', 'trainings', '{"title":"\u4e2d\u836f\u6750\u89c4\u8303\u5316\u91c7\u6536\u57f9\u8bad","trainer":"\u738b\u8001\u5e08","audience":"\u57fa\u5c42\u6280\u672f\u4eba\u5458","tracking":"\u7b7e\u5230\u3001\u89c6\u9891\u3001\u8003\u6838\u8bb0\u5f55\u5b8c\u6574"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'training-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'training-002', 'trainings', '{"title":"\u624b\u673aAPP\u91c7\u96c6\u586b\u62a5\u57f9\u8bad","trainer":"\u674e\u8001\u5e08","audience":"\u5b66\u751f\u4e0e\u79d1\u7814\u52a9\u7406","tracking":"\u5b8c\u6210\u7ebf\u4e0a\u7b7e\u5230\u3001\u5b9e\u5730\u91c7\u96c6\u548c\u6570\u636e\u56de\u4f20"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'training-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'training-003', 'trainings', '{"title":"\u4e2d\u836f\u6750\u8bc4\u4ef7\u6307\u6807\u89e3\u8bfb","trainer":"\u5f20\u8001\u5e08","audience":"\u6559\u5e08\u4e0e\u57f9\u8bad\u4eba\u5458","tracking":"\u5df2\u5b8c\u6210\u8bfe\u4ef6\u5b58\u50a8\u3001\u8bd5\u9898\u8003\u6838\u548c\u7ed3\u679c\u5f52\u6863"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'training-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'evaluation-001', 'evaluations', '{"herbName":"\u9ec4\u8fde","indicator":"\u6027\u72b6\u3001\u542b\u91cf\u3001\u4ea7\u5730\u751f\u6001\u3001\u4f20\u627f\u5de5\u827a","score":"91","result":"\u4f18\u79c0","applicationMaterial":"\u53ef\u7528\u4e8e\u975e\u9057\u53ca\u54c1\u724c\u7533\u62a5\u6750\u6599"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'evaluation-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'evaluation-002', 'evaluations', '{"herbName":"\u675c\u4ef2","indicator":"\u6811\u76ae\u6027\u72b6\u3001\u80f6\u4e1d\u7279\u5f81\u3001\u91c7\u6536\u5e74\u9650\u3001\u4ea7\u5730\u73af\u5883","score":"87","result":"\u826f\u597d","applicationMaterial":"\u53ef\u7528\u4e8e\u4ea7\u5730\u8bc1\u660e\u548c\u57f9\u8bad\u7d20\u6750"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'evaluation-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'evaluation-003', 'evaluations', '{"herbName":"\u4f5b\u624b","indicator":"\u679c\u5f62\u3001\u9999\u6c14\u3001\u6709\u6548\u6210\u5206\u3001\u52a0\u5de5\u5de5\u827a","score":"93","result":"\u4f18\u79c0","applicationMaterial":"\u53ef\u7528\u4e8e\u54c1\u724c\u5efa\u8bbe\u548c\u975e\u9057\u7533\u62a5"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'evaluation-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'achievement-001', 'achievements', '{"title":"\u9ec4\u8fde\u79cd\u690d\u6280\u672f\u63a8\u5e7f","owner":"\u4e2d\u836f\u5b66\u9662","category":"\u793e\u4f1a\u670d\u52a1","level":"\u6821\u7ea7\u91cd\u70b9","status":"\u5f85\u5ba1\u6838"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'achievement-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'achievement-002', 'achievements', '{"title":"\u4e2d\u836f\u6750\u6570\u5b57\u91c7\u96c6\u8bfe\u7a0b\u5efa\u8bbe","owner":"\u4e2d\u836f\u5b66\u9662","category":"\u6559\u5b66\u5efa\u8bbe","level":"\u9662\u7ea7","status":"\u5df2\u901a\u8fc7"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'achievement-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'achievement-003', 'achievements', '{"title":"\u91cd\u5e86\u9053\u5730\u836f\u6750\u8bc4\u4ef7\u6307\u6807\u5e93","owner":"\u4e2d\u836f\u6750\u6570\u5b57\u4fe1\u606f\u5b9e\u9a8c\u5ba4","category":"\u79d1\u7814\u6210\u679c","level":"\u6821\u7ea7\u91cd\u70b9","status":"\u5f85\u5ba1\u6838"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'achievement-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'user-001', 'users', '{"name":"\u7cfb\u7edf\u7ba1\u7406\u5458","role":"\u7ba1\u7406\u5458","department":"\u4fe1\u606f\u4e2d\u5fc3","level":"\u4e00\u7ea7"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'user-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'user-002', 'users', '{"name":"\u5f20\u8001\u5e08","role":"\u6559\u5e08","department":"\u4e2d\u836f\u5b66\u9662","level":"\u4e8c\u7ea7"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'user-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'user-003', 'users', '{"name":"\u5b66\u751f\u7528\u6237","role":"\u5b66\u751f","department":"\u4e2d\u836f\u5b66\u9662","level":"\u4e09\u7ea7"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'user-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'standard-001', 'standards', '{"name":"\u5b66\u6821\u4e1a\u7ee9\u5206\u7c7b\u8ba4\u5b9a\u529e\u6cd5","category":"\u6559\u5b66\u79d1\u7814\u4e1a\u7ee9","levelRule":"\u56fd\u5bb6\u7ea7/\u7701\u90e8\u7ea7/\u6821\u7ea7/\u9662\u7ea7","effectiveDate":"2026-06-06"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'standard-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'trace-event-001', 'trace-events', '{"herbName":"榛勮繛","traceCode":"CQ-HL-001","eventType":"绉嶆","eventContent":"瀹屾垚榛勮繛绉嶆鍦板潡鐧昏锛岃褰曟捣鎷斻€侀伄闃淬€佸湡澹ゆ箍搴︾瓑鍩虹鐢熸€佷俊鎭€?,"operator":"寮犺€佸笀","eventTime":"2026-03-10T09:30:00","location":"鐭虫煴鍘块粍姘撮晣"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'trace-event-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'trace-event-002', 'trace-events', '{"herbName":"榛勮繛","traceCode":"CQ-HL-001","eventType":"閲囬泦","eventContent":"閫氳繃鎵嬫満 APP 閲囬泦娓╂箍搴︺€佸湡澹?PH 鍜岀敓闀块樁娈垫暟鎹紝骞剁粦瀹氬悓涓€婧簮鐮併€?,"operator":"瀛︾敓鐢ㄦ埛","eventTime":"2026-06-06T11:00:00","location":"鐭虫煴鍘块粍姘撮晣"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'trace-event-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'trace-event-003', 'trace-events', '{"herbName":"榛勮繛","traceCode":"CQ-HL-001","eventType":"妫€娴?,"eventContent":"瀹屾垚榛勮繛鏍锋湰 HPLC 鎸囩汗鍥捐氨姣斿锛岀浉浼煎害杈冮珮锛屽彲浣滀负鏁欏鏍锋湰褰掓。銆?,"operator":"鏉庤€佸笀","eventTime":"2026-06-06T16:00:00","location":"涓嵂鏉愭暟瀛椾俊鎭疄楠屽"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'trace-event-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'trace-event-004', 'trace-events', '{"herbName":"鏉滀徊","traceCode":"CQ-DZ-004","eventType":"妫€娴?,"eventContent":"瀹屾垚鏉滀徊鏍峰搧鑳朵笣鐗瑰緛銆佸惈姘寸巼鍜屽瑙傛€х姸妫€娴嬶紝缁撴灉鐢ㄤ簬浜у湴璐ㄩ噺璇佹槑銆?,"operator":"鐜嬭€佸笀","eventTime":"2026-06-06T14:30:00","location":"鍨睙鍘挎矙鍧晣"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'trace-event-004');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'trace-event-005', 'trace-events', '{"herbName":"浣涙墜","traceCode":"CQ-FS-006","eventType":"鍔犲伐","eventContent":"璁板綍浣涙墜鍒囩墖銆佸共鐕ュ拰鍖呰杩囩▼锛屽舰鎴愬悗缁川閲忚拷婧妭鐐广€?,"operator":"鐜嬭€佸笀","eventTime":"2026-06-06T16:20:00","location":"姹熸触鍖虹煶闂ㄩ晣"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'trace-event-005');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'teaching-resource-001', 'teaching-resources', '{"title":"榛勮繛鐢熼暱鏁版嵁閲囬泦瀹為獙瑙嗛","resourceType":"鏁欏瑙嗛","courseTitle":"涓嵂鏉愮敓闀挎暟鎹噰闆嗗疄楠?,"uploader":"鏉庤€佸笀","uploaderRole":"鏁欏笀","status":"宸插彂甯?,"reviewComment":"鍐呭瀹屾暣锛屽凡鍙戝竷鑷冲鐢熻绋嬪涔犮€?,"videoUrl":"https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4","fileUrl":"","publishedAt":"2026-06-08T09:00:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'teaching-resource-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'teaching-resource-002', 'teaching-resources', '{"title":"浣涙墜鏍锋湰鍥捐氨姣斿璧勬枡鍖?,"resourceType":"鍥捐氨鏂囦欢","courseTitle":"鑽潗婧簮鐮佷笌鍥捐氨姣斿瀹炶","uploader":"鐜嬭€佸笀","uploaderRole":"绉戠爺浜哄憳","status":"寰呭鏍?,"reviewComment":"绛夊緟绠＄悊鍛樺鏍稿浘璋辨枃浠朵笌璇存槑鏂囨。銆?,"videoUrl":"","fileUrl":"","publishedAt":""}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'teaching-resource-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'teaching-resource-003', 'teaching-resources', '{"title":"涓嵂鏉愯鑼冨寲閲囨敹鍩硅璇句欢","resourceType":"璇句欢鏂囨。","courseTitle":"涓嵂鏉愯鑼冨寲閲囨敹鍩硅","uploader":"寮犺€佸笀","uploaderRole":"鏁欏笀","status":"宸查┏鍥?,"reviewComment":"缂哄皯灏侀潰鍜屽疄楠屽畨鍏ㄨ鏄庯紝琛ュ厖鍚庨噸鏂版彁浜ゃ€?,"videoUrl":"","fileUrl":"","publishedAt":""}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'teaching-resource-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'teaching-resource-004', 'teaching-resources', '{"title":"鏄惧井閴村畾瀹為獙鎿嶄綔婕旂ず","resourceType":"鏁欏瑙嗛","courseTitle":"涓嵂鏉愭樉寰壌瀹氬疄楠?,"uploader":"寮犺€佸笀","uploaderRole":"鏁欏笀","status":"宸插彂甯?,"reviewComment":"鏄惧井鍒剁墖鍜岃瀵熸楠ゆ竻鏅帮紝宸插彂甯冪粰瀛︾敓銆?,"videoUrl":"https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4","fileUrl":"","publishedAt":"2026-06-10T10:30:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'teaching-resource-004');

UPDATE generic_record
SET payload = '{"title":"榛勮繛鐢熼暱鏁版嵁閲囬泦瀹為獙瑙嗛","resourceType":"鏁欏瑙嗛","courseTitle":"涓嵂鏉愮敓闀挎暟鎹噰闆嗗疄楠?,"uploader":"鏉庤€佸笀","uploaderRole":"鏁欏笀","status":"宸插彂甯?,"reviewComment":"鍐呭瀹屾暣锛屽凡鍙戝竷鑷冲鐢熻绋嬪涔犮€?,"videoUrl":"https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4","fileUrl":"","publishedAt":"2026-06-08T09:00:00"}'
WHERE id = 'teaching-resource-001' AND resource_type = 'teaching-resources' AND payload NOT LIKE '%videoUrl%';
