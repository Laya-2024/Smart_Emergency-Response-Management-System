CREATE TABLE emergencies (
 id BINARY(16) NOT NULL, type VARCHAR(32) NOT NULL, status VARCHAR(32) NOT NULL,
 reporter_id BINARY(16) NOT NULL, idempotency_key VARCHAR(80) NOT NULL,
 latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL, description VARCHAR(1000),
 created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6), version BIGINT NOT NULL,
 PRIMARY KEY (id), UNIQUE KEY uk_emergency_idempotency (idempotency_key),
 KEY idx_emergency_status_created (status, created_at), KEY idx_emergency_type (type)
);
