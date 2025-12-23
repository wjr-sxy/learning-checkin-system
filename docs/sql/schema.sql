CREATE DATABASE IF NOT EXISTS `learning_checkin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `learning_checkin`;

-- User Table
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    `username` VARCHAR(50) NOT NULL COMMENT 'Username',
    `password` VARCHAR(100) NOT NULL COMMENT 'Password (Encrypted)',
    `email` VARCHAR(100) DEFAULT NULL COMMENT 'Email',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT 'Avatar URL',
    `points` INT DEFAULT 0 COMMENT 'Current Points',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT 'Role: USER, ADMIN',
    `current_avatar_frame` VARCHAR(255) DEFAULT NULL COMMENT 'Current Avatar Frame',
    `current_skin` VARCHAR(255) DEFAULT NULL COMMENT 'Current Skin',
    `current_badge` VARCHAR(255) DEFAULT NULL COMMENT 'Current Badge',
    `profile_background` VARCHAR(255) DEFAULT NULL COMMENT 'Profile Background',

    -- Online Stats
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Table';

-- Check-in Record Table
CREATE TABLE IF NOT EXISTS `sys_checkin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `checkin_date` DATE NOT NULL COMMENT 'Check-in Date',
    `checkin_time` DATETIME NOT NULL COMMENT 'Check-in Time',
    `study_duration` INT DEFAULT 0 COMMENT 'Study Duration (minutes)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Check-in Record';

-- Study Plan Table
CREATE TABLE IF NOT EXISTS `sys_study_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(100) NOT NULL COMMENT 'Plan Title',
    `description` VARCHAR(255) DEFAULT NULL,
    `target_hours` INT NOT NULL COMMENT 'Target Study Hours',
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0: Ongoing, 1: Completed, 2: Expired',
    `total_tasks` INT DEFAULT 0 COMMENT 'Total Tasks',
    `completed_tasks` INT DEFAULT 0 COMMENT 'Completed Tasks',
    `progress_percentage` DECIMAL(5,1) DEFAULT 0.0 COMMENT 'Progress Percentage',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Study Plan';

-- Points Record Table
CREATE TABLE IF NOT EXISTS `sys_points_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `type` TINYINT NOT NULL COMMENT '1: Gain, 2: Consume',
    `amount` INT NOT NULL COMMENT 'Points Amount',
    `description` VARCHAR(255) NOT NULL COMMENT 'Reason',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Points Record';

-- Product Table (Points Shop)
CREATE TABLE IF NOT EXISTS `sys_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    `price` INT NOT NULL COMMENT 'Price in Points',
    `image_url` VARCHAR(255) DEFAULT NULL,
    `stock` INT DEFAULT 9999,
    `type` VARCHAR(50) DEFAULT 'VIRTUAL' COMMENT 'Type: AVATAR_FRAME, SKIN, etc.',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product';

-- Order Table
CREATE TABLE IF NOT EXISTS `sys_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `price` INT NOT NULL,
    `status` INT DEFAULT 0 COMMENT '0: Success, 1: Refunded',
    `is_abnormal` BOOLEAN DEFAULT FALSE COMMENT 'Abnormal Order',
    `shipping_address` VARCHAR(255) DEFAULT NULL COMMENT 'Shipping Address',
    `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT 'Receiver Name',
    `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT 'Receiver Phone',
    `tracking_number` VARCHAR(100) DEFAULT NULL COMMENT 'Tracking Number',
    `shipping_status` INT DEFAULT 0 COMMENT '0: Pending, 1: Shipped, 2: Delivered',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Exchange Order';

-- Study Plan Progress History Table
CREATE TABLE IF NOT EXISTS `sys_study_plan_progress_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `plan_id` BIGINT NOT NULL COMMENT 'Study Plan ID',
    `previous_progress` DECIMAL(5,1) DEFAULT 0.0 COMMENT 'Previous Progress',
    `new_progress` DECIMAL(5,1) NOT NULL COMMENT 'New Progress',
    `completed_tasks` INT DEFAULT 0 COMMENT 'Completed Tasks at that time',
    `total_tasks` INT DEFAULT 0 COMMENT 'Total Tasks at that time',
    `note` VARCHAR(255) DEFAULT NULL COMMENT 'Progress Note/Hint',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Study Plan Progress History';

-- Insert Admin User (password: 123456)
INSERT INTO `sys_user` (`username`, `password`, `role`, `points`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuloOul.QD.G6be/7k/1.32', 'ADMIN', 99999);
-- Insert Test User (password: 123456)
INSERT INTO `sys_user` (`username`, `password`, `role`, `points`) VALUES 
('student', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuloOul.QD.G6be/7k/1.32', 'USER', 100);

-- Insert some products
INSERT INTO `sys_product` (`name`, `description`, `price`, `image_url`, `type`) VALUES 
('Gold Frame', 'Shiny gold avatar frame', 500, 'https://placehold.co/200x200/FFD700/FFFFFF.png?text=Gold+Frame', 'AVATAR_FRAME'),
('Cool Skin', 'Cool dark theme skin', 1000, 'https://placehold.co/200x200/000000/FFFFFF.png?text=Cool+Skin', 'SKIN');
