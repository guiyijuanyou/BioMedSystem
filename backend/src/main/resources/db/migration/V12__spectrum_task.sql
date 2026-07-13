-- V12: Spectrum comparison task - Add chromatogram data columns
-- Guard: columns may already exist from schema.sql
DROP PROCEDURE IF EXISTS alter_spectrum_comparison;
DELIMITER $$
CREATE PROCEDURE alter_spectrum_comparison()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'spectrum_comparison'
          AND COLUMN_NAME = 'sample_data_json'
    ) THEN
        ALTER TABLE spectrum_comparison
            ADD COLUMN sample_data_json LONGTEXT
                COMMENT 'Sample chromatogram data points JSON: [{"x":1.2,"y":345},...]',
            ADD COLUMN reference_data_json LONGTEXT
                COMMENT 'Reference chromatogram data points JSON',
            ADD COLUMN compare_algorithm VARCHAR(50)
                DEFAULT 'COSINE'
                COMMENT 'Algorithm: COSINE/PEARSON/EUCLIDEAN/COMPOSITE';
    END IF;
END $$
DELIMITER ;
CALL alter_spectrum_comparison();
DROP PROCEDURE alter_spectrum_comparison;
