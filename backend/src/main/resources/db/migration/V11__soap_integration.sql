CREATE TABLE IF NOT EXISTS integration_client (
  id VARCHAR(64) PRIMARY KEY,
  client_code VARCHAR(80) NOT NULL UNIQUE,
  client_name VARCHAR(200) NOT NULL,
  secret_ciphertext TEXT NOT NULL,
  secret_iv VARCHAR(64) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'active',
  allowed_ips VARCHAR(1000),
  last_used_at TIMESTAMP NULL,
  secret_rotated_at TIMESTAMP NULL,
  created_by VARCHAR(100),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  INDEX idx_integration_client_status(status)
);

CREATE TABLE IF NOT EXISTS integration_nonce (
  id VARCHAR(64) PRIMARY KEY,
  client_id VARCHAR(64) NOT NULL,
  nonce_value VARCHAR(128) NOT NULL,
  request_timestamp BIGINT NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_integration_client_nonce(client_id, nonce_value),
  INDEX idx_integration_nonce_expiry(expires_at)
);
