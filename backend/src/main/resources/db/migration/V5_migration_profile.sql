-- Idempotent profile migration for existing MySQL databases.
SET @schema_name = DATABASE();
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='sys_user' AND column_name='phone')=0,'ALTER TABLE sys_user ADD COLUMN phone VARCHAR(30) AFTER status','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='sys_user' AND column_name='email')=0,'ALTER TABLE sys_user ADD COLUMN email VARCHAR(150) AFTER phone','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='sys_user' AND column_name='avatar_url')=0,'ALTER TABLE sys_user ADD COLUMN avatar_url VARCHAR(500) AFTER email','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='sys_user' AND column_name='title')=0,'ALTER TABLE sys_user ADD COLUMN title VARCHAR(100) AFTER avatar_url','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='sys_user' AND column_name='research_area')=0,'ALTER TABLE sys_user ADD COLUMN research_area VARCHAR(300) AFTER title','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='sys_user' AND column_name='bio')=0,'ALTER TABLE sys_user ADD COLUMN bio TEXT AFTER research_area','SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE sys_user SET title='系统管理员',email='admin@cqutcm.edu.cn',research_area='系统管理与数据安全' WHERE username='admin' AND title IS NULL;
UPDATE sys_user SET title='副教授',email='teacher@cqutcm.edu.cn',phone='13800001111',research_area='中药材栽培与鉴定',bio='从事中药材规范化种植与质量评价研究多年，主讲中药材显微鉴定实验课程。' WHERE username='teacher' AND title IS NULL;
UPDATE sys_user SET title='研究员',email='researcher@cqutcm.edu.cn',phone='13800002222',research_area='中药材活性成分分析与图谱比对',bio='长期从事重庆道地药材化学成分研究，主持多项市级科研课题。' WHERE username='researcher' AND title IS NULL;
UPDATE sys_user SET title='本科生',email='student@cqutcm.edu.cn',research_area='中药学',bio='生物医药学院在读，对中药材生长数据采集和课题研究有浓厚兴趣。' WHERE username='student' AND title IS NULL;
