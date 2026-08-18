ALTER TABLE donations ADD COLUMN target_name VARCHAR(160) NULL AFTER item_description;
ALTER TABLE donations ADD COLUMN target_type VARCHAR(24) NULL AFTER target_name;
