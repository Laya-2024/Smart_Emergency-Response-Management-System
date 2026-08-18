CREATE TABLE blood_requests (
  id BINARY(16) NOT NULL,
  blood_group VARCHAR(4) NOT NULL,
  units_required INT NOT NULL,
  hospital_name VARCHAR(180) NOT NULL,
  contact_note VARCHAR(1000),
  latitude DOUBLE NOT NULL,
  longitude DOUBLE NOT NULL,
  request_status VARCHAR(24) NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_blood_request_status (request_status, created_at)
);
