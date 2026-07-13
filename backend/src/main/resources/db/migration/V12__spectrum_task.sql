-- V6: Add data point storage and algorithm tracking to spectrum_comparison
ALTER TABLE spectrum_comparison
  ADD COLUMN IF NOT EXISTS sample_data_json LONGTEXT COMMENT 'Sample chromatogram data points JSON: [{"x":1.2,"y":345},...]',
  ADD COLUMN IF NOT EXISTS reference_data_json LONGTEXT COMMENT 'Reference chromatogram data points JSON',
  ADD COLUMN IF NOT EXISTS compare_algorithm VARCHAR(50) DEFAULT 'COSINE' COMMENT 'Algorithm: COSINE/PEARSON/EUCLIDEAN/COMPOSITE';
