USE learning_checkin;

DROP TABLE IF EXISTS `sys_operation_log`;

CREATE TABLE `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL COMMENT 'User ID',
    `module` VARCHAR(50) DEFAULT NULL COMMENT 'Module',
    `action` VARCHAR(50) DEFAULT NULL COMMENT 'Action',
    `description` TEXT DEFAULT NULL COMMENT 'Description (might be long)',
    `method` TEXT DEFAULT NULL COMMENT 'Method Name (might be long)',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP Address',
    `status` TINYINT DEFAULT 0 COMMENT '0: Success, 1: Fail',
    `execution_time` BIGINT DEFAULT 0 COMMENT 'Execution Time (ms)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Operation Log';

CREATE TABLE IF NOT EXISTS `sys_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL COMMENT 'User ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT 'Username',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP Address',
    `status` TINYINT DEFAULT 0 COMMENT '0: Success, 1: Fail',
    `message` VARCHAR(255) DEFAULT NULL COMMENT 'Message',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Login Log';
