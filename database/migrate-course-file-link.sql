USE biomed;
SET NAMES utf8mb4;

SET @has_file_id := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'teaching_resource'
    AND column_name = 'file_id'
);

SET @sql := IF(
  @has_file_id = 0,
  'ALTER TABLE teaching_resource ADD COLUMN file_id VARCHAR(64) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE teaching_resource
SET video_url = NULL
WHERE video_url = 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4';
