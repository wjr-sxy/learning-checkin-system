-- Database Optimization Migration Script
-- Target Database: learning_checkin_optimized

USE `learning_checkin_optimized`;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Create Unified Log Table with Partitioning
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `log_type` VARCHAR(20) NOT NULL COMMENT 'OPERATION, LOGIN, SENSITIVE, EXPIRATION',
    `user_id` BIGINT DEFAULT NULL,
    `module` VARCHAR(50) DEFAULT NULL,
    `action` VARCHAR(100) DEFAULT NULL,
    `content` TEXT COMMENT 'Description/Message',
    `ip` VARCHAR(50) DEFAULT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0: Success, 1: Fail',
    `execution_time` BIGINT DEFAULT 0 COMMENT 'ms',
    `extra_info` JSON DEFAULT NULL COMMENT 'JSON for device, IDs, words, etc.',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`, `create_time`),
    INDEX `idx_user_type_time` (`user_id`, `log_type`, `create_time`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Unified Log Table'
PARTITION BY RANGE (TO_DAYS(create_time)) (
    PARTITION p_2025_12 VALUES LESS THAN (TO_DAYS('2026-01-01')),
    PARTITION p_2026_01 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- Migrate Log Data (if any exists, though currently mostly empty)
INSERT INTO `sys_log` (log_type, user_id, module, action, content, ip, status, execution_time, create_time)
SELECT 'OPERATION', user_id, module, action, description, ip, status, execution_time, create_time FROM `sys_operation_log`;

INSERT INTO `sys_log` (log_type, user_id, content, ip, status, extra_info, create_time)
SELECT 'LOGIN', user_id, message, ip, status, JSON_OBJECT('username', username, 'device', device), create_time FROM `sys_login_log`;

INSERT INTO `sys_log` (log_type, user_id, content, extra_info, create_time)
SELECT 'SENSITIVE', user_id, content_snippet, JSON_OBJECT('detected_words', detected_words, 'source_type', source_type, 'source_id', source_id), create_time FROM `sys_sensitive_log`;

INSERT INTO `sys_log` (log_type, user_id, extra_info, create_time)
SELECT 'EXPIRATION', user_id, JSON_OBJECT('item_id', item_id), operate_time FROM `sys_expiration_log`;

DROP TABLE IF EXISTS `sys_operation_log`;
DROP TABLE IF EXISTS `sys_login_log`;
DROP TABLE IF EXISTS `sys_sensitive_log`;
DROP TABLE IF EXISTS `sys_expiration_log`;


-- 2. Create Unified Notice Table
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `type` VARCHAR(20) NOT NULL COMMENT 'MESSAGE, NOTIFICATION, ANNOUNCEMENT',
    `sender_id` BIGINT DEFAULT 0 COMMENT '0 for System',
    `receiver_id` BIGINT DEFAULT NULL COMMENT 'NULL for Announcement/All',
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT,
    `is_read` TINYINT(1) DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_receiver_type_read` (`receiver_id`, `type`, `is_read`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Unified Notice Table';

-- Migrate Notice Data
INSERT INTO `sys_notice` (type, sender_id, receiver_id, title, content, is_read, create_time)
SELECT 'MESSAGE', sender_id, receiver_id, title, content, is_read, create_time FROM `sys_message`;

INSERT INTO `sys_notice` (type, sender_id, receiver_id, title, content, is_read, create_time)
SELECT 'NOTIFICATION', 0, user_id, title, content, is_read, create_time FROM `sys_notification`;

INSERT INTO `sys_notice` (type, sender_id, title, content, create_time)
SELECT 'ANNOUNCEMENT', 0, title, content, create_time FROM `sys_announcement`;

DROP TABLE IF EXISTS `sys_message`;
DROP TABLE IF EXISTS `sys_notification`;
DROP TABLE IF EXISTS `sys_announcement`;


-- 3. Unify Config
-- Merge points_rule into sys_config
INSERT INTO `sys_config` (config_key, config_value, description)
SELECT CONCAT('points.', rule_key), rule_value, description FROM `sys_points_rule`
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

DROP TABLE IF EXISTS `sys_points_rule`;


-- 4. Additional Indexing and Standardizing
ALTER TABLE `sys_checkin` ADD INDEX `idx_date` (`checkin_date`);
ALTER TABLE `sys_points_record` ADD INDEX `idx_type_time` (`type`, `create_time`);
ALTER TABLE `sys_order` ADD INDEX `idx_create_time` (`create_time`);

SET FOREIGN_KEY_CHECKS = 1;
