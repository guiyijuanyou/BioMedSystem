SET @schema_name = DATABASE();
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = @schema_name
       AND table_name = 'achievement_record'
       AND column_name = 'evidence_file_ids') = 0,
    'ALTER TABLE achievement_record ADD COLUMN evidence_file_ids TEXT NULL AFTER evidence',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
