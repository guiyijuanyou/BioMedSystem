ALTER TABLE spectrum_comparison
    ADD COLUMN sample_data_json LONGTEXT
    COMMENT 'Sample chromatogram data points JSON: [{"x":1.2,"y":345},...]',
    ADD COLUMN reference_data_json LONGTEXT
        COMMENT 'Reference chromatogram data points JSON',
    ADD COLUMN compare_algorithm VARCHAR(50)
        DEFAULT 'COSINE'
        COMMENT 'Algorithm: COSINE/PEARSON/EUCLIDEAN/COMPOSITE';