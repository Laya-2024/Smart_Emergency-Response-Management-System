ALTER TABLE app_users
  ADD COLUMN phone_encrypted VARBINARY(512) NULL,
  ADD COLUMN phone_hash CHAR(64) NULL,
  ADD COLUMN email_verified BIT NOT NULL DEFAULT b'0',
  ADD COLUMN phone_verified BIT NOT NULL DEFAULT b'0',
  ADD COLUMN last_login_at TIMESTAMP(6) NULL,
  ADD UNIQUE KEY uk_user_phone_hash (phone_hash);

-- The code itself is never stored: only its SHA-256 hash and expiry are retained.
CREATE TABLE verification_tokens (
  id BINARY(16) NOT NULL, user_id BINARY(16) NOT NULL, channel VARCHAR(12) NOT NULL,
  purpose VARCHAR(32) NOT NULL, token_hash CHAR(64) NOT NULL, expires_at TIMESTAMP(6) NOT NULL,
  consumed_at TIMESTAMP(6) NULL, attempts INT NOT NULL DEFAULT 0, created_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY(id), KEY idx_verification_lookup(user_id, channel, purpose, expires_at),
  CONSTRAINT fk_verification_user FOREIGN KEY(user_id) REFERENCES app_users(id)
);

-- Required for every sensitive-role request. Aadhaar values are never stored here.
CREATE TABLE role_applications (
  id BINARY(16) NOT NULL, user_id BINARY(16) NOT NULL, requested_role VARCHAR(32) NOT NULL,
  organisation_name VARCHAR(150), professional_id_masked VARCHAR(32), details_json JSON NOT NULL,
  review_status VARCHAR(24) NOT NULL DEFAULT 'PENDING', submitted_at TIMESTAMP(6) NOT NULL,
  reviewed_at TIMESTAMP(6) NULL, reviewed_by BINARY(16) NULL, review_notes VARCHAR(1000),
  PRIMARY KEY(id), KEY idx_application_review(review_status, requested_role, submitted_at),
  CONSTRAINT fk_application_user FOREIGN KEY(user_id) REFERENCES app_users(id),
  CONSTRAINT fk_application_reviewer FOREIGN KEY(reviewed_by) REFERENCES app_users(id)
);

-- Store only object-storage keys plus checksum. Encrypt the uploaded object in private storage.
CREATE TABLE verification_documents (
  id BINARY(16) NOT NULL, application_id BINARY(16) NOT NULL, document_type VARCHAR(32) NOT NULL,
  object_key VARCHAR(500) NOT NULL, sha256_checksum CHAR(64) NOT NULL, content_type VARCHAR(100) NOT NULL,
  file_size_bytes BIGINT NOT NULL, uploaded_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY(id), KEY idx_document_application(application_id),
  CONSTRAINT fk_document_application FOREIGN KEY(application_id) REFERENCES role_applications(id)
);
