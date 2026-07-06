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
