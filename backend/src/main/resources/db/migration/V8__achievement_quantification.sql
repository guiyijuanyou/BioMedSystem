CREATE TABLE IF NOT EXISTS achievement_scoring_rule (
 id VARCHAR(64) PRIMARY KEY,rule_code VARCHAR(80) NOT NULL,rule_name VARCHAR(200) NOT NULL,
 base_points DECIMAL(8,2) NOT NULL,improvement_factor DECIMAL(8,2) NOT NULL,max_points DECIMAL(8,2) NOT NULL,
 version_no INT NOT NULL DEFAULT 1,status VARCHAR(20) NOT NULL DEFAULT 'active',effective_date DATE,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE KEY uk_asr_version(rule_code,version_no)
);
CREATE TABLE IF NOT EXISTS achievement_quantification (
 id VARCHAR(64) PRIMARY KEY,achievement_evidence_id VARCHAR(64) NOT NULL,issue_id VARCHAR(64) NOT NULL,
 rule_id VARCHAR(64) NOT NULL,rule_version INT NOT NULL,initial_score DECIMAL(8,2),recheck_score DECIMAL(8,2),
 improvement_value DECIMAL(8,2),base_points DECIMAL(8,2) NOT NULL,suggested_points DECIMAL(8,2) NOT NULL,
 confirmed_points DECIMAL(8,2),status VARCHAR(20) NOT NULL DEFAULT 'suggested',confirmed_by VARCHAR(100),confirmed_at TIMESTAMP NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE KEY uk_aq_evidence(achievement_evidence_id),INDEX idx_aq_issue(issue_id)
);
INSERT INTO achievement_scoring_rule(id,rule_code,rule_name,base_points,improvement_factor,max_points,version_no,status)
SELECT 'rule-quality-improvement-v1','QUALITY_IMPROVEMENT','质量改进闭环业绩计分',5,0.5,20,1,'active'
WHERE NOT EXISTS(SELECT 1 FROM achievement_scoring_rule WHERE rule_code='QUALITY_IMPROVEMENT' AND version_no=1);
