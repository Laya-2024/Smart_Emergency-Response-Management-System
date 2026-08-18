-- Device registrations are used for authenticated web/mobile push notifications.
CREATE TABLE device_registrations (
  id BINARY(16) NOT NULL, user_id BINARY(16) NOT NULL, device_token_hash CHAR(64) NOT NULL,
  platform VARCHAR(16) NOT NULL, active BIT NOT NULL DEFAULT b'1', last_seen_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_device_token(device_token_hash), KEY idx_device_user(user_id,active),
  CONSTRAINT fk_device_user FOREIGN KEY(user_id) REFERENCES app_users(id)
);

-- A permanent record of whom an alert was offered to and whether it was seen/accepted.
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

-- Feedback is allowed only after the emergency is resolved and only from its reporter.
CREATE TABLE emergency_feedback (
  id BINARY(16) NOT NULL, emergency_id BINARY(16) NOT NULL, reporter_id BINARY(16) NOT NULL,
  rating TINYINT NOT NULL, comments VARCHAR(1000), created_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY(id), UNIQUE KEY uk_feedback_emergency(emergency_id), KEY idx_feedback_rating(rating),
  CONSTRAINT chk_feedback_rating CHECK(rating BETWEEN 1 AND 5),
  CONSTRAINT fk_feedback_emergency FOREIGN KEY(emergency_id) REFERENCES emergencies(id),
  CONSTRAINT fk_feedback_reporter FOREIGN KEY(reporter_id) REFERENCES app_users(id)
);
