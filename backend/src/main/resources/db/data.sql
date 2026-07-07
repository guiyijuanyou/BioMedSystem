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
SELECT 'growth-006', 'growth-records', '{"herbName":"黄连","district":"石柱县","temperature":"18.9","humidity":"86","soilPh":"6.1","growthStage":"萌芽期","collector":"传感器网关","recordedAt":"2026-06-01T09:00:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-006');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-007', 'growth-records', '{"herbName":"黄连","district":"石柱县","temperature":"19.4","humidity":"84","soilPh":"6.2","growthStage":"展叶期","collector":"手机APP采集","recordedAt":"2026-06-08T09:20:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-007');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-008', 'growth-records', '{"herbName":"黄连","district":"石柱县","temperature":"20.2","humidity":"82","soilPh":"6.3","growthStage":"生长期","collector":"传感器网关","recordedAt":"2026-06-15T09:15:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-008');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-009', 'growth-records', '{"herbName":"黄连","district":"石柱县","temperature":"20.7","humidity":"80","soilPh":"6.4","growthStage":"旺长期","collector":"电脑终端录入","recordedAt":"2026-06-22T10:05:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-009');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-010', 'growth-records', '{"herbName":"金银花","district":"秀山县","temperature":"23.1","humidity":"69","soilPh":"6.8","growthStage":"抽枝期","collector":"手机APP采集","recordedAt":"2026-06-02T08:40:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-010');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-011', 'growth-records', '{"herbName":"金银花","district":"秀山县","temperature":"24.0","humidity":"66","soilPh":"6.9","growthStage":"现蕾期","collector":"传感器网关","recordedAt":"2026-06-09T08:50:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-011');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-012', 'growth-records', '{"herbName":"金银花","district":"秀山县","temperature":"25.2","humidity":"64","soilPh":"7.0","growthStage":"花蕾期","collector":"传感器网关","recordedAt":"2026-06-16T08:30:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-012');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-013', 'growth-records', '{"herbName":"天麻","district":"巫溪县","temperature":"16.8","humidity":"79","soilPh":"6.7","growthStage":"出芽期","collector":"传感器网关","recordedAt":"2026-06-03T11:00:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-013');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-014', 'growth-records', '{"herbName":"天麻","district":"巫溪县","temperature":"17.5","humidity":"77","soilPh":"6.8","growthStage":"块茎膨大期","collector":"手机APP采集","recordedAt":"2026-06-10T11:10:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-014');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-015', 'growth-records', '{"herbName":"天麻","district":"巫溪县","temperature":"18.0","humidity":"76","soilPh":"6.9","growthStage":"块茎膨大期","collector":"电脑终端录入","recordedAt":"2026-06-17T11:20:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-015');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'spectrum-compare-001', 'spectrum-comparisons', '{"herbName":"黄连","sampleCode":"HL-SZ-20260601","district":"石柱县","spectrumType":"HPLC 指纹图谱","referenceName":"重庆黄连标准图谱 V1","similarity":"94.6","result":"通过","operator":"张老师","comparedAt":"2026-06-06T16:00:00","remark":"主峰保留时间稳定，特征峰匹配度高"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'spectrum-compare-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'spectrum-compare-002', 'spectrum-comparisons', '{"herbName":"金银花","sampleCode":"JYH-XS-20260602","district":"秀山县","spectrumType":"薄层色谱图谱","referenceName":"金银花薄层鉴别标准图谱","similarity":"88.2","result":"建议复核","operator":"李老师","comparedAt":"2026-06-07T10:30:00","remark":"局部斑点颜色偏浅，建议补充复测"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'spectrum-compare-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'spectrum-compare-003', 'spectrum-comparisons', '{"herbName":"天麻","sampleCode":"TM-WX-20260603","district":"巫溪县","spectrumType":"红外图谱","referenceName":"天麻红外标准图谱","similarity":"91.3","result":"通过","operator":"王老师","comparedAt":"2026-06-08T14:20:00","remark":"样本图谱与参考图谱整体一致"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'spectrum-compare-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-analysis-001', 'growth-analysis', '{"analysisName":"石柱黄连六月生长趋势分析","herbName":"黄连","district":"石柱县","indicator":"温度、湿度、土壤PH","baseline":"2026-06-01 首次记录","currentValue":"2026-06-22 最新记录","difference":"温度 +1.8，湿度 -6，PH +0.3","trend":"温度上升、湿度下降、PH稳定","conclusion":"黄连处于旺长期，温湿度变化仍在适宜范围内，建议保持遮阴与土壤保湿。","analyst":"李老师","analyzedAt":"2026-06-22T17:00:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'growth-analysis-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'growth-analysis-002', 'growth-analysis', '{"analysisName":"秀山金银花花蕾期环境对比","herbName":"金银花","district":"秀山县","indicator":"温度、湿度","baseline":"2026-06-02 抽枝期","currentValue":"2026-06-16 花蕾期","difference":"温度 +2.1，湿度 -5","trend":"温度上升、湿度下降","conclusion":"花蕾期温度升高较明显，应关注连续高温对花蕾质量的影响。","analyst":"王老师","analyzedAt":"2026-06-16T16:30:00"}', CURRENT_TIMESTAMP
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
SET payload = '{"title":"重庆道地药材生态适应性研究","leader":"李老师","requirements":"招募2名学生，要求能参与野外采样、掌握基础数据录入，按周提交采集记录。","status":"已发布","stage":"数据采集中","applicantRequests":"当前学生","approvedMembers":"","rejectedApplicants":"","transformation":"种植规范转化"}'
WHERE id = 'project-001' AND resource_type = 'projects' AND payload NOT LIKE '%requirements%';

UPDATE generic_record
SET payload = '{"title":"中药材非遗申报评价素材建设","leader":"张老师","requirements":"招募熟悉文献整理和影像资料归档的学生，需完成评价素材标注与过程记录。","status":"已发布","stage":"评价资料整理","applicantRequests":"","approvedMembers":"学生A","rejectedApplicants":"学生B","transformation":"形成非遗申报资料库"}'
WHERE id = 'project-002' AND resource_type = 'projects' AND payload NOT LIKE '%requirements%';

UPDATE generic_record
SET payload = '{"title":"佛手产地生态因子与品质关联分析","leader":"王老师","requirements":"招募具备图谱比对或统计分析基础的学生，参与样本数据清洗和品质模型验证。","status":"待审核","stage":"数据对比分析","applicantRequests":"","approvedMembers":"","rejectedApplicants":"","transformation":"建立产地品质评价模型"}'
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
SELECT 'trace-event-001', 'trace-events', '{"herbName":"黄连","traceCode":"CQ-HL-001","eventType":"种植","eventContent":"完成黄连种植地块登记，记录海拔、遮阴、土壤湿度等基础生态信息。","operator":"张老师","eventTime":"2026-03-10T09:30:00","location":"石柱县黄水镇"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'trace-event-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'trace-event-002', 'trace-events', '{"herbName":"黄连","traceCode":"CQ-HL-001","eventType":"采集","eventContent":"通过手机 APP 采集温湿度、土壤 PH 和生长阶段数据，并绑定同一溯源码。","operator":"学生用户","eventTime":"2026-06-06T11:00:00","location":"石柱县黄水镇"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'trace-event-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'trace-event-003', 'trace-events', '{"herbName":"黄连","traceCode":"CQ-HL-001","eventType":"检测","eventContent":"完成黄连样本 HPLC 指纹图谱比对，相似度较高，可作为教学样本归档。","operator":"李老师","eventTime":"2026-06-06T16:00:00","location":"中药材数字信息实验室"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'trace-event-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'trace-event-004', 'trace-events', '{"herbName":"杜仲","traceCode":"CQ-DZ-004","eventType":"检测","eventContent":"完成杜仲样品胶丝特征、含水率和外观性状检测，结果用于产地质量证明。","operator":"王老师","eventTime":"2026-06-06T14:30:00","location":"垫江县沙坪镇"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'trace-event-004');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'trace-event-005', 'trace-events', '{"herbName":"佛手","traceCode":"CQ-FS-006","eventType":"加工","eventContent":"记录佛手切片、干燥和包装过程，形成后续质量追溯节点。","operator":"王老师","eventTime":"2026-06-06T16:20:00","location":"江津区石门镇"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'trace-event-005');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'teaching-resource-001', 'teaching-resources', '{"title":"黄连生长数据采集实验视频","resourceType":"教学视频","courseTitle":"中药材生长数据采集实验","uploader":"李老师","uploaderRole":"教师","status":"已发布","reviewComment":"内容完整，已发布至学生课程学习。","videoUrl":"https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4","fileUrl":"","publishedAt":"2026-06-08T09:00:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'teaching-resource-001');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'teaching-resource-002', 'teaching-resources', '{"title":"佛手样本图谱比对资料包","resourceType":"图谱文件","courseTitle":"药材溯源码与图谱比对实训","uploader":"王老师","uploaderRole":"科研人员","status":"待审核","reviewComment":"等待管理员审核图谱文件与说明文档。","videoUrl":"","fileUrl":"","publishedAt":""}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'teaching-resource-002');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'teaching-resource-003', 'teaching-resources', '{"title":"中药材规范化采收培训课件","resourceType":"课件文档","courseTitle":"中药材规范化采收培训","uploader":"张老师","uploaderRole":"教师","status":"已驳回","reviewComment":"缺少封面和实验安全说明，补充后重新提交。","videoUrl":"","fileUrl":"","publishedAt":""}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'teaching-resource-003');

INSERT INTO generic_record (id, resource_type, payload, created_at)
SELECT 'teaching-resource-004', 'teaching-resources', '{"title":"显微鉴定实验操作演示","resourceType":"教学视频","courseTitle":"中药材显微鉴定实验","uploader":"张老师","uploaderRole":"教师","status":"已发布","reviewComment":"显微制片和观察步骤清晰，已发布给学生。","videoUrl":"https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4","fileUrl":"","publishedAt":"2026-06-10T10:30:00"}', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM generic_record WHERE id = 'teaching-resource-004');

UPDATE generic_record
SET payload = '{"title":"黄连生长数据采集实验视频","resourceType":"教学视频","courseTitle":"中药材生长数据采集实验","uploader":"李老师","uploaderRole":"教师","status":"已发布","reviewComment":"内容完整，已发布至学生课程学习。","videoUrl":"https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4","fileUrl":"","publishedAt":"2026-06-08T09:00:00"}'
WHERE id = 'teaching-resource-001' AND resource_type = 'teaching-resources' AND payload NOT LIKE '%videoUrl%';
