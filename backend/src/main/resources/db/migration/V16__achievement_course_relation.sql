SET @schema_name = DATABASE();

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'course_id') = 0,
    'ALTER TABLE achievement_record ADD COLUMN course_id VARCHAR(64) NULL AFTER source_module',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'course_title') = 0,
    'ALTER TABLE achievement_record ADD COLUMN course_title VARCHAR(200) NULL AFTER course_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
