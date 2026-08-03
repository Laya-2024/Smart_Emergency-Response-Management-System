-- Smart Emergency Response Management System
-- Paste this complete file into MySQL Workbench and execute it once.
CREATE DATABASE IF NOT EXISTS emergency_response CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'emergency_app'@'localhost' IDENTIFIED BY 'change-this-in-production';
ALTER USER 'emergency_app'@'localhost' IDENTIFIED BY 'change-this-in-production';
GRANT ALL PRIVILEGES ON emergency_response.* TO 'emergency_app'@'localhost';
FLUSH PRIVILEGES;
USE emergency_response;

CREATE TABLE app_users (
 id BINARY(16) NOT NULL, email VARCHAR(254) NOT NULL, full_name VARCHAR(100) NOT NULL,
 password_hash VARCHAR(100) NOT NULL, enabled BIT NOT NULL DEFAULT b'1',
 phone_encrypted VARBINARY(512) NULL, phone_hash CHAR(64) NULL,
 email_verified BIT NOT NULL DEFAULT b'0', phone_verified BIT NOT NULL DEFAULT b'0',
 last_login_at TIMESTAMP(6) NULL, created_at TIMESTAMP(6) NOT NULL,
 PRIMARY KEY(id), UNIQUE KEY uk_user_email(email), UNIQUE KEY uk_user_phone_hash(phone_hash)
);

CREATE TABLE user_roles (
 user_id BINARY(16) NOT NULL, role VARCHAR(32) NOT NULL, PRIMARY KEY(user_id,role),
 CONSTRAINT fk_user_roles_user FOREIGN KEY(user_id) REFERENCES app_users(id)
);

CREATE TABLE password_reset_tokens (
 id BINARY(16) NOT NULL, user_id BINARY(16) NOT NULL, token_hash VARCHAR(64) NOT NULL,
 expires_at TIMESTAMP(6) NOT NULL, used_at TIMESTAMP(6) NULL,
 PRIMARY KEY(id), UNIQUE KEY uk_reset_token(token_hash),
 CONSTRAINT fk_reset_user FOREIGN KEY(user_id) REFERENCES app_users(id)
);

CREATE TABLE verification_tokens (
 id BINARY(16) NOT NULL, user_id BINARY(16) NOT NULL, channel VARCHAR(12) NOT NULL,
 purpose VARCHAR(32) NOT NULL, token_hash CHAR(64) NOT NULL, expires_at TIMESTAMP(6) NOT NULL,
 consumed_at TIMESTAMP(6) NULL, attempts INT NOT NULL DEFAULT 0, created_at TIMESTAMP(6) NOT NULL,
 PRIMARY KEY(id), KEY idx_verification_lookup(user_id,channel,purpose,expires_at),
 CONSTRAINT fk_verification_user FOREIGN KEY(user_id) REFERENCES app_users(id)
);

CREATE TABLE role_applications (
 id BINARY(16) NOT NULL, user_id BINARY(16) NOT NULL, requested_role VARCHAR(32) NOT NULL,
 organisation_name VARCHAR(150), professional_id_masked VARCHAR(32), details_json JSON NOT NULL,
 review_status VARCHAR(24) NOT NULL DEFAULT 'PENDING', submitted_at TIMESTAMP(6) NOT NULL,
 reviewed_at TIMESTAMP(6) NULL, reviewed_by BINARY(16) NULL, review_notes VARCHAR(1000),
 PRIMARY KEY(id), KEY idx_application_review(review_status,requested_role,submitted_at),
 CONSTRAINT fk_application_user FOREIGN KEY(user_id) REFERENCES app_users(id),
 CONSTRAINT fk_application_reviewer FOREIGN KEY(reviewed_by) REFERENCES app_users(id)
);

CREATE TABLE verification_documents (
 id BINARY(16) NOT NULL, application_id BINARY(16) NOT NULL, document_type VARCHAR(32) NOT NULL,
 object_key VARCHAR(500) NOT NULL, sha256_checksum CHAR(64) NOT NULL, content_type VARCHAR(100) NOT NULL,
 file_size_bytes BIGINT NOT NULL, uploaded_at TIMESTAMP(6) NOT NULL,
 PRIMARY KEY(id), KEY idx_document_application(application_id),
 CONSTRAINT fk_document_application FOREIGN KEY(application_id) REFERENCES role_applications(id)
);

CREATE TABLE emergencies (
 id BINARY(16) NOT NULL, type VARCHAR(32) NOT NULL, status VARCHAR(32) NOT NULL,
 reporter_id BINARY(16) NOT NULL, idempotency_key VARCHAR(80) NOT NULL,
 latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL, description VARCHAR(1000),
 created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NULL, version BIGINT NOT NULL DEFAULT 0,
 PRIMARY KEY(id), UNIQUE KEY uk_emergency_idempotency(idempotency_key),
 KEY idx_emergency_status_created(status,created_at), KEY idx_emergency_type(type)
);

CREATE TABLE responder_profiles (
 id BINARY(16) NOT NULL, user_id BINARY(16) NOT NULL, organisation_name VARCHAR(150),
 verification_status VARCHAR(24) NOT NULL DEFAULT 'PENDING', availability_status VARCHAR(24) NOT NULL DEFAULT 'OFFLINE',
 service_type VARCHAR(32), latitude DOUBLE, longitude DOUBLE, updated_at TIMESTAMP(6) NOT NULL,
 PRIMARY KEY(id), UNIQUE KEY uk_responder_user(user_id),
 KEY idx_responder_available(verification_status,availability_status,service_type),
 CONSTRAINT fk_responder_user FOREIGN KEY(user_id) REFERENCES app_users(id)
);

CREATE TABLE emergency_assignments (
 id BINARY(16) NOT NULL, emergency_id BINARY(16) NOT NULL, responder_id BINARY(16) NOT NULL,
 assignment_status VARCHAR(24) NOT NULL DEFAULT 'OFFERED', assigned_at TIMESTAMP(6) NOT NULL,
 accepted_at TIMESTAMP(6) NULL, completed_at TIMESTAMP(6) NULL, notes VARCHAR(1000), version BIGINT NOT NULL DEFAULT 0,
 PRIMARY KEY(id), KEY idx_assignment_responder(responder_id,assignment_status), KEY idx_assignment_emergency(emergency_id),
 CONSTRAINT fk_assignment_emergency FOREIGN KEY(emergency_id) REFERENCES emergencies(id),
 CONSTRAINT fk_assignment_responder FOREIGN KEY(responder_id) REFERENCES responder_profiles(id)
);

CREATE TABLE emergency_updates (
 id BINARY(16) NOT NULL, emergency_id BINARY(16) NOT NULL, author_id BINARY(16) NULL,
 update_type VARCHAR(32) NOT NULL, message VARCHAR(2000) NOT NULL, visible_to_reporter BIT NOT NULL DEFAULT b'1',
 created_at TIMESTAMP(6) NOT NULL, PRIMARY KEY(id), KEY idx_update_emergency_created(emergency_id,created_at),
 CONSTRAINT fk_update_emergency FOREIGN KEY(emergency_id) REFERENCES emergencies(id),
 CONSTRAINT fk_update_author FOREIGN KEY(author_id) REFERENCES app_users(id)
);

CREATE TABLE trusted_contacts (
 id BINARY(16) NOT NULL, owner_id BINARY(16) NOT NULL, contact_name VARCHAR(100) NOT NULL,
 phone_encrypted VARBINARY(512) NOT NULL, relationship_name VARCHAR(60), active BIT NOT NULL DEFAULT b'1',
 created_at TIMESTAMP(6) NOT NULL, PRIMARY KEY(id), KEY idx_contact_owner(owner_id,active),
 CONSTRAINT fk_contact_owner FOREIGN KEY(owner_id) REFERENCES app_users(id)
);

CREATE TABLE shelters (
 id BINARY(16) NOT NULL, name VARCHAR(150) NOT NULL, address_line VARCHAR(500) NOT NULL,
 latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL, contact_phone_encrypted VARBINARY(512),
 capacity_total INT NOT NULL, capacity_available INT NOT NULL, status VARCHAR(24) NOT NULL DEFAULT 'OPEN',
 created_at TIMESTAMP(6) NOT NULL, updated_at TIMESTAMP(6) NOT NULL, version BIGINT NOT NULL DEFAULT 0,
 PRIMARY KEY(id), KEY idx_shelter_status(status,capacity_available)
);

CREATE TABLE disaster_events (
 id BINARY(16) NOT NULL, event_type VARCHAR(32) NOT NULL, title VARCHAR(180) NOT NULL,
 severity VARCHAR(16) NOT NULL, status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE', affected_area VARCHAR(500),
 started_at TIMESTAMP(6) NOT NULL, ended_at TIMESTAMP(6) NULL, created_by BINARY(16) NULL,
 PRIMARY KEY(id), KEY idx_disaster_active(status,event_type),
 CONSTRAINT fk_disaster_creator FOREIGN KEY(created_by) REFERENCES app_users(id)
);

CREATE TABLE relief_requests (
 id BINARY(16) NOT NULL, disaster_event_id BINARY(16) NULL, requester_id BINARY(16) NULL,
 request_type VARCHAR(32) NOT NULL, people_count INT NOT NULL DEFAULT 1, description VARCHAR(1000),
 latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL, request_status VARCHAR(24) NOT NULL DEFAULT 'OPEN',
 created_at TIMESTAMP(6) NOT NULL, fulfilled_at TIMESTAMP(6) NULL, PRIMARY KEY(id),
 KEY idx_relief_dispatch(request_status,request_type,created_at),
 CONSTRAINT fk_relief_disaster FOREIGN KEY(disaster_event_id) REFERENCES disaster_events(id),
 CONSTRAINT fk_relief_requester FOREIGN KEY(requester_id) REFERENCES app_users(id)
);

CREATE TABLE donations (
 id BINARY(16) NOT NULL, donor_id BINARY(16) NULL, donation_type VARCHAR(24) NOT NULL,
 amount DECIMAL(12,2) NULL, currency CHAR(3) NULL, item_description VARCHAR(1000) NULL,
 donation_status VARCHAR(24) NOT NULL DEFAULT 'PLEDGED', payment_reference VARCHAR(120) NULL,
 created_at TIMESTAMP(6) NOT NULL, PRIMARY KEY(id), KEY idx_donation_status(donation_status,created_at),
 CONSTRAINT fk_donation_donor FOREIGN KEY(donor_id) REFERENCES app_users(id)
);

CREATE TABLE notification_outbox (
 id BINARY(16) NOT NULL, user_id BINARY(16) NULL, channel VARCHAR(16) NOT NULL,
 template_code VARCHAR(80) NOT NULL, payload_json JSON NOT NULL, delivery_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
 attempts INT NOT NULL DEFAULT 0, next_attempt_at TIMESTAMP(6) NOT NULL, sent_at TIMESTAMP(6) NULL,
 created_at TIMESTAMP(6) NOT NULL, PRIMARY KEY(id), KEY idx_outbox_delivery(delivery_status,next_attempt_at),
 CONSTRAINT fk_outbox_user FOREIGN KEY(user_id) REFERENCES app_users(id)
);

CREATE TABLE audit_logs (
 id BINARY(16) NOT NULL, actor_id BINARY(16) NULL, action VARCHAR(100) NOT NULL,
 entity_type VARCHAR(80) NOT NULL, entity_id BINARY(16) NULL, ip_hash CHAR(64) NULL,
 metadata_json JSON NULL, created_at TIMESTAMP(6) NOT NULL, PRIMARY KEY(id),
 KEY idx_audit_entity(entity_type,entity_id,created_at), KEY idx_audit_actor(actor_id,created_at),
 CONSTRAINT fk_audit_actor FOREIGN KEY(actor_id) REFERENCES app_users(id)
);

CREATE TABLE device_registrations (
 id BINARY(16) NOT NULL, user_id BINARY(16) NOT NULL, device_token_hash CHAR(64) NOT NULL,
 platform VARCHAR(16) NOT NULL, active BIT NOT NULL DEFAULT b'1', last_seen_at TIMESTAMP(6) NOT NULL,
 PRIMARY KEY(id), UNIQUE KEY uk_device_token(device_token_hash), KEY idx_device_user(user_id,active),
 CONSTRAINT fk_device_user FOREIGN KEY(user_id) REFERENCES app_users(id)
);

CREATE TABLE alert_deliveries (
 id BINARY(16) NOT NULL, emergency_id BINARY(16) NOT NULL, recipient_id BINARY(16) NOT NULL,
 delivery_channel VARCHAR(16) NOT NULL, delivery_status VARCHAR(24) NOT NULL DEFAULT 'QUEUED',
 sent_at TIMESTAMP(6) NULL, seen_at TIMESTAMP(6) NULL, accepted_at TIMESTAMP(6) NULL,
 failure_reason VARCHAR(255) NULL, created_at TIMESTAMP(6) NOT NULL,
 PRIMARY KEY(id), UNIQUE KEY uk_alert_recipient(emergency_id,recipient_id),
 KEY idx_alert_recipient(recipient_id,delivery_status,created_at),
 CONSTRAINT fk_delivery_emergency FOREIGN KEY(emergency_id) REFERENCES emergencies(id),
 CONSTRAINT fk_delivery_recipient FOREIGN KEY(recipient_id) REFERENCES app_users(id)
);

CREATE TABLE emergency_feedback (
 id BINARY(16) NOT NULL, emergency_id BINARY(16) NOT NULL, reporter_id BINARY(16) NOT NULL,
 rating TINYINT NOT NULL, comments VARCHAR(1000), created_at TIMESTAMP(6) NOT NULL,
 PRIMARY KEY(id), UNIQUE KEY uk_feedback_emergency(emergency_id), KEY idx_feedback_rating(rating),
 CONSTRAINT fk_feedback_emergency FOREIGN KEY(emergency_id) REFERENCES emergencies(id),
 CONSTRAINT fk_feedback_reporter FOREIGN KEY(reporter_id) REFERENCES app_users(id)
);
