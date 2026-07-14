SET @schema_name = DATABASE();

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'course_id') = 0,
  'ALTER TABLE training_material ADD COLUMN course_id VARCHAR(64) NULL AFTER trainer_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'course_title') = 0,
  'ALTER TABLE training_material ADD COLUMN course_title VARCHAR(200) NULL AFTER course_id', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'batch_id') = 0,
  'ALTER TABLE training_material ADD COLUMN batch_id VARCHAR(64) NULL AFTER course_title', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'herb_name') = 0,
  'ALTER TABLE training_material ADD COLUMN herb_name VARCHAR(100) NULL AFTER batch_id', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'district') = 0,
  'ALTER TABLE training_material ADD COLUMN district VARCHAR(100) NULL AFTER herb_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'training_type') = 0,
  'ALTER TABLE training_material ADD COLUMN training_type VARCHAR(80) NULL AFTER district', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'completion_rate') = 0,
  'ALTER TABLE training_material ADD COLUMN completion_rate VARCHAR(30) NULL AFTER training_type', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'source_issue') = 0,
  'ALTER TABLE training_material ADD COLUMN source_issue VARCHAR(255) NULL AFTER completion_rate', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'source_module') = 0,
  'ALTER TABLE achievement_record ADD COLUMN source_module VARCHAR(80) NULL AFTER owner_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'batch_id') = 0,
  'ALTER TABLE achievement_record ADD COLUMN batch_id VARCHAR(64) NULL AFTER source_module', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'herb_name') = 0,
  'ALTER TABLE achievement_record ADD COLUMN herb_name VARCHAR(100) NULL AFTER batch_id', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'project_title') = 0,
  'ALTER TABLE achievement_record ADD COLUMN project_title VARCHAR(200) NULL AFTER herb_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'standard_id') = 0,
  'ALTER TABLE achievement_record ADD COLUMN standard_id VARCHAR(64) NULL AFTER project_title', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'score') = 0,
  'ALTER TABLE achievement_record ADD COLUMN score VARCHAR(30) NULL AFTER level_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'evidence') = 0,
  'ALTER TABLE achievement_record ADD COLUMN evidence TEXT NULL AFTER score', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

UPDATE training_material SET course_title = '试验课程', training_type = '课程培训', completion_rate = '100%' WHERE course_title IS NULL;
UPDATE achievement_record SET source_module = '成果管理', score = '0' WHERE source_module IS NULL;
