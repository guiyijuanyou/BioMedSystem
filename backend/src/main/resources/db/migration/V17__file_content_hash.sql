SET @schema_name = DATABASE();

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.columns
     WHERE table_schema = @schema_name AND table_name = 'file_asset' AND column_name = 'sha256') = 0,
    'ALTER TABLE file_asset ADD COLUMN sha256 CHAR(64) NULL AFTER storage_path',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.statistics
     WHERE table_schema = @schema_name AND table_name = 'file_asset' AND index_name = 'idx_file_hash_category') = 0,
    'ALTER TABLE file_asset ADD INDEX idx_file_hash_category (sha256, category)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
