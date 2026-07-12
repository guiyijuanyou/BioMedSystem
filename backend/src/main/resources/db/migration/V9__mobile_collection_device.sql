CREATE TABLE IF NOT EXISTS mobile_collection_device (
 id VARCHAR(64) PRIMARY KEY,device_code VARCHAR(80) NOT NULL UNIQUE,device_name VARCHAR(200) NOT NULL,
 owner_name VARCHAR(100),platform_name VARCHAR(50),token_hash VARCHAR(64) NOT NULL UNIQUE,
 status VARCHAR(20) NOT NULL DEFAULT 'active',last_seen_at TIMESTAMP NULL,token_rotated_at TIMESTAMP NULL,
 created_by VARCHAR(100),created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMP NULL,
 INDEX idx_mcd_status(status),INDEX idx_mcd_owner(owner_name)
);
CREATE TABLE IF NOT EXISTS mobile_sync_record (
 id VARCHAR(64) PRIMARY KEY,device_id VARCHAR(64) NOT NULL,client_record_id VARCHAR(100) NOT NULL,
 growth_record_id VARCHAR(64),sync_status VARCHAR(20) NOT NULL,error_message VARCHAR(500),
 received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_msr_device_client(device_id,client_record_id),INDEX idx_msr_device(device_id),INDEX idx_msr_status(sync_status)
);
SET @schema_name=DATABASE();
SET @sql=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=@schema_name AND table_name='growth_record' AND column_name='longitude')=0,'ALTER TABLE growth_record ADD COLUMN longitude DECIMAL(10,6) NULL,ADD COLUMN latitude DECIMAL(10,6) NULL,ADD COLUMN location_accuracy DECIMAL(10,2) NULL,ADD COLUMN device_id VARCHAR(64) NULL,ADD COLUMN client_record_id VARCHAR(100) NULL,ADD INDEX idx_gr_device(device_id)','SELECT 1');PREPARE stmt FROM @sql;EXECUTE stmt;DEALLOCATE PREPARE stmt;
