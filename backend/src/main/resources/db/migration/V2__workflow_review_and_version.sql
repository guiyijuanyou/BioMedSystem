-- Idempotent migration for existing databases created before the review workflow.
-- This file is safe to run repeatedly and is executed after schema.sql.

SET @schema_name = DATABASE();

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'herb' AND column_name = 'version') = 0,
  'ALTER TABLE herb ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER trace_code', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_record' AND column_name = 'version') = 0,
  'ALTER TABLE growth_record ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER recorded_at', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'trace_event' AND column_name = 'version') = 0,
  'ALTER TABLE trace_event ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER location', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'spectrum_comparison' AND column_name = 'status') = 0,
  'ALTER TABLE spectrum_comparison ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT ''草稿'' AFTER remark', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'spectrum_comparison' AND column_name = 'reviewer_name') = 0,
  'ALTER TABLE spectrum_comparison ADD COLUMN reviewer_name VARCHAR(100) NULL AFTER status', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'spectrum_comparison' AND column_name = 'review_comment') = 0,
  'ALTER TABLE spectrum_comparison ADD COLUMN review_comment TEXT NULL AFTER reviewer_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'spectrum_comparison' AND column_name = 'reviewed_at') = 0,
  'ALTER TABLE spectrum_comparison ADD COLUMN reviewed_at TIMESTAMP NULL AFTER review_comment', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'spectrum_comparison' AND column_name = 'version') = 0,
  'ALTER TABLE spectrum_comparison ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER reviewed_at', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'status') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT ''草稿'' AFTER analyzed_at', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'reviewer_name') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN reviewer_name VARCHAR(100) NULL AFTER status', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'review_comment') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN review_comment TEXT NULL AFTER reviewer_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'reviewed_at') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN reviewed_at TIMESTAMP NULL AFTER review_comment', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'growth_analysis' AND column_name = 'version') = 0,
  'ALTER TABLE growth_analysis ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER reviewed_at', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'course' AND column_name = 'reviewer_name') = 0,
  'ALTER TABLE course ADD COLUMN reviewer_name VARCHAR(100) NULL AFTER status', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'course' AND column_name = 'review_comment') = 0,
  'ALTER TABLE course ADD COLUMN review_comment TEXT NULL AFTER reviewer_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'course' AND column_name = 'reviewed_at') = 0,
  'ALTER TABLE course ADD COLUMN reviewed_at TIMESTAMP NULL AFTER review_comment', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'course' AND column_name = 'version') = 0,
  'ALTER TABLE course ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER reviewed_at', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'teaching_resource' AND column_name = 'reviewer_name') = 0,
  'ALTER TABLE teaching_resource ADD COLUMN reviewer_name VARCHAR(100) NULL AFTER status', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'teaching_resource' AND column_name = 'review_comment') = 0,
  'ALTER TABLE teaching_resource ADD COLUMN review_comment TEXT NULL AFTER reviewer_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'teaching_resource' AND column_name = 'reviewed_at') = 0,
  'ALTER TABLE teaching_resource ADD COLUMN reviewed_at TIMESTAMP NULL AFTER review_comment', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'teaching_resource' AND column_name = 'version') = 0,
  'ALTER TABLE teaching_resource ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER published_at', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'status') = 0,
  'ALTER TABLE training_material ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT ''草稿'' AFTER tracking', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'reviewer_name') = 0,
  'ALTER TABLE training_material ADD COLUMN reviewer_name VARCHAR(100) NULL AFTER status', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'review_comment') = 0,
  'ALTER TABLE training_material ADD COLUMN review_comment TEXT NULL AFTER reviewer_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'reviewed_at') = 0,
  'ALTER TABLE training_material ADD COLUMN reviewed_at TIMESTAMP NULL AFTER review_comment', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'training_material' AND column_name = 'version') = 0,
  'ALTER TABLE training_material ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER reviewed_at', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'evaluation_record' AND column_name = 'evaluator_name') = 0,
  'ALTER TABLE evaluation_record ADD COLUMN evaluator_name VARCHAR(100) NULL AFTER application_material', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'evaluation_record' AND column_name = 'subject_owner_name') = 0,
  'ALTER TABLE evaluation_record ADD COLUMN subject_owner_name VARCHAR(100) NULL AFTER application_material', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'evaluation_record' AND column_name = 'evaluator_role') = 0,
  'ALTER TABLE evaluation_record ADD COLUMN evaluator_role VARCHAR(50) NULL AFTER evaluator_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'evaluation_record' AND column_name = 'status') = 0,
  'ALTER TABLE evaluation_record ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT ''草稿'' AFTER evaluator_role', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'evaluation_record' AND column_name = 'reviewer_name') = 0,
  'ALTER TABLE evaluation_record ADD COLUMN reviewer_name VARCHAR(100) NULL AFTER status', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'evaluation_record' AND column_name = 'review_comment') = 0,
  'ALTER TABLE evaluation_record ADD COLUMN review_comment TEXT NULL AFTER reviewer_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'evaluation_record' AND column_name = 'reviewed_at') = 0,
  'ALTER TABLE evaluation_record ADD COLUMN reviewed_at TIMESTAMP NULL AFTER review_comment', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'evaluation_record' AND column_name = 'version') = 0,
  'ALTER TABLE evaluation_record ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER reviewed_at', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'status') = 0,
  'ALTER TABLE achievement_record ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT ''草稿'' AFTER level_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'reviewer_name') = 0,
  'ALTER TABLE achievement_record ADD COLUMN reviewer_name VARCHAR(100) NULL AFTER status', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'review_comment') = 0,
  'ALTER TABLE achievement_record ADD COLUMN review_comment TEXT NULL AFTER reviewer_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'reviewed_at') = 0,
  'ALTER TABLE achievement_record ADD COLUMN reviewed_at TIMESTAMP NULL AFTER review_comment', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'achievement_record' AND column_name = 'version') = 0,
  'ALTER TABLE achievement_record ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER reviewed_at', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'research_project' AND column_name = 'reviewer_name') = 0,
  'ALTER TABLE research_project ADD COLUMN reviewer_name VARCHAR(100) NULL AFTER status', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'research_project' AND column_name = 'review_comment') = 0,
  'ALTER TABLE research_project ADD COLUMN review_comment TEXT NULL AFTER reviewer_name', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'research_project' AND column_name = 'reviewed_at') = 0,
  'ALTER TABLE research_project ADD COLUMN reviewed_at TIMESTAMP NULL AFTER review_comment', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'research_project' AND column_name = 'version') = 0,
  'ALTER TABLE research_project ADD COLUMN version INT NOT NULL DEFAULT 0 AFTER rejected_applicants', 'SELECT 1');
PREPARE migration_stmt FROM @sql; EXECUTE migration_stmt; DEALLOCATE PREPARE migration_stmt;

UPDATE spectrum_comparison SET status = CASE status WHEN 'draft' THEN '草稿' WHEN 'pending_review' THEN '待审核' WHEN 'approved' THEN '已通过' WHEN 'rejected' THEN '已驳回' WHEN 'published' THEN '已发布' WHEN 'archived' THEN '已归档' ELSE status END;
UPDATE growth_analysis SET status = CASE status WHEN 'draft' THEN '草稿' WHEN 'pending_review' THEN '待审核' WHEN 'approved' THEN '已通过' WHEN 'rejected' THEN '已驳回' WHEN 'published' THEN '已发布' WHEN 'archived' THEN '已归档' ELSE status END;
UPDATE course SET status = CASE status WHEN 'draft' THEN '草稿' WHEN 'pending_review' THEN '待审核' WHEN 'approved' THEN '已通过' WHEN 'rejected' THEN '已驳回' WHEN 'published' THEN '已发布' WHEN 'archived' THEN '已归档' ELSE status END;
UPDATE teaching_resource SET status = CASE status WHEN 'draft' THEN '草稿' WHEN 'pending_review' THEN '待审核' WHEN 'approved' THEN '已通过' WHEN 'rejected' THEN '已驳回' WHEN 'published' THEN '已发布' WHEN 'archived' THEN '已归档' ELSE status END;
UPDATE training_material SET status = CASE status WHEN 'draft' THEN '草稿' WHEN 'pending_review' THEN '待审核' WHEN 'approved' THEN '已通过' WHEN 'rejected' THEN '已驳回' WHEN 'published' THEN '已发布' WHEN 'archived' THEN '已归档' ELSE status END;
UPDATE evaluation_record SET status = CASE status WHEN 'draft' THEN '草稿' WHEN 'pending_review' THEN '待审核' WHEN 'approved' THEN '已通过' WHEN 'rejected' THEN '已驳回' WHEN 'published' THEN '已发布' WHEN 'archived' THEN '已归档' ELSE status END;
UPDATE achievement_record SET status = CASE status WHEN 'draft' THEN '草稿' WHEN 'pending_review' THEN '待审核' WHEN 'approved' THEN '已通过' WHEN 'rejected' THEN '已驳回' WHEN 'published' THEN '已发布' WHEN 'archived' THEN '已归档' ELSE status END;
UPDATE research_project SET status = CASE status WHEN 'draft' THEN '草稿' WHEN 'pending_review' THEN '待审核' WHEN 'approved' THEN '已通过' WHEN 'rejected' THEN '已驳回' WHEN 'published' THEN '已发布' WHEN 'archived' THEN '已归档' ELSE status END;

UPDATE training_material SET status = '已发布' WHERE id IN ('training-001', 'training-002', 'training-003') AND status = '草稿';
UPDATE evaluation_record SET subject_owner_name = '黄连课题组', evaluator_name = '张老师', evaluator_role = '教师', status = '已发布' WHERE id = 'evaluation-001' AND evaluator_name IS NULL;
UPDATE evaluation_record SET subject_owner_name = '杜仲课题组', evaluator_name = '李老师', evaluator_role = '教师', status = '已发布' WHERE id = 'evaluation-002' AND evaluator_name IS NULL;
UPDATE evaluation_record SET subject_owner_name = '佛手课题组', evaluator_name = '王老师', evaluator_role = '科研人员', status = '已发布' WHERE id = 'evaluation-003' AND evaluator_name IS NULL;

ALTER TABLE spectrum_comparison ALTER COLUMN status SET DEFAULT '草稿';
ALTER TABLE growth_analysis ALTER COLUMN status SET DEFAULT '草稿';
ALTER TABLE course ALTER COLUMN status SET DEFAULT '草稿';
ALTER TABLE teaching_resource ALTER COLUMN status SET DEFAULT '草稿';
ALTER TABLE training_material ALTER COLUMN status SET DEFAULT '草稿';
ALTER TABLE evaluation_record ALTER COLUMN status SET DEFAULT '草稿';
ALTER TABLE achievement_record ALTER COLUMN status SET DEFAULT '草稿';
ALTER TABLE research_project ALTER COLUMN status SET DEFAULT '草稿';
