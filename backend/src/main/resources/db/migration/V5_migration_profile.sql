-- 用户资料字段迁移脚本（适用于已有数据库）
-- 如果表已存在但缺少新字段，执行此脚本添加列

ALTER TABLE sys_user ADD COLUMN phone VARCHAR(30) AFTER status;
ALTER TABLE sys_user ADD COLUMN email VARCHAR(150) AFTER phone;
ALTER TABLE sys_user ADD COLUMN avatar_url VARCHAR(500) AFTER email;
ALTER TABLE sys_user ADD COLUMN title VARCHAR(100) AFTER avatar_url;
ALTER TABLE sys_user ADD COLUMN research_area VARCHAR(300) AFTER title;
ALTER TABLE sys_user ADD COLUMN bio TEXT AFTER research_area;

-- 为种子用户补充示例资料
UPDATE sys_user SET title = '系统管理员', email = 'admin@cqutcm.edu.cn', research_area = '系统管理与数据安全' WHERE username = 'admin' AND title IS NULL;
UPDATE sys_user SET title = '副教授', email = 'teacher@cqutcm.edu.cn', phone = '13800001111', research_area = '中药材栽培与鉴定', bio = '从事中药材规范化种植与质量评价研究多年，主讲中药材显微鉴定实验课程。' WHERE username = 'teacher' AND title IS NULL;
UPDATE sys_user SET title = '研究员', email = 'researcher@cqutcm.edu.cn', phone = '13800002222', research_area = '中药材活性成分分析与图谱比对', bio = '长期从事重庆道地药材化学成分研究，主持多项市级科研课题。' WHERE username = 'researcher' AND title IS NULL;
UPDATE sys_user SET title = '本科生', email = 'student@cqutcm.edu.cn', research_area = '中药学', bio = '生物医药学院在读，对中药材生长数据采集和课题研究有浓厚兴趣。' WHERE username = 'student' AND title IS NULL;
