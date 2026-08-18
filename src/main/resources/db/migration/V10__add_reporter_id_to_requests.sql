-- V10: add reporter_id to community request tables for per-user queries
ALTER TABLE blood_requests ADD COLUMN reporter_id BINARY(16) NULL;
ALTER TABLE donations ADD COLUMN reporter_id BINARY(16) NULL;
ALTER TABLE relief_requests ADD COLUMN reporter_id BINARY(16) NULL;
