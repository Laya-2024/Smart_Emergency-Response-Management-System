



-- Run this once in MySQL Workbench while connected as MySQL root/administrator.
CREATE DATABASE IF NOT EXISTS emergency_response CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'emergency_app'@'localhost'
  IDENTIFIED BY 'Laya@2024';
ALTER USER 'emergency_app'@'localhost'
  IDENTIFIED BY 'Laya@2024';
GRANT ALL PRIVILEGES ON emergency_response.* TO 'emergency_app'@'localhost';
FLUSH PRIVILEGES;
