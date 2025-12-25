-- Add ip_address column to sys_checkin table
ALTER TABLE `sys_checkin` ADD COLUMN `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'Check-in IP Address' AFTER `is_supplementary`;
