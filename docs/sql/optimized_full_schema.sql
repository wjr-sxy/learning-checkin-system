-- Optimized Comprehensive Database Schema for Learning Check-in System
-- Generated: 2025-12-23

CREATE DATABASE IF NOT EXISTS `learning_checkin_optimized` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `learning_checkin_optimized`;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. User Table
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    `username` VARCHAR(50) NOT NULL COMMENT 'Username',
    `password` VARCHAR(100) NOT NULL COMMENT 'Password (Encrypted)',
    `email` VARCHAR(100) DEFAULT NULL COMMENT 'Email',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT 'Avatar URL',
    `points` INT DEFAULT 0 COMMENT 'Current Points',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT 'Role: USER, TEACHER, ADMIN',
    `status` TINYINT DEFAULT 0 COMMENT '0: Normal, 1: Banned',
    `full_name` VARCHAR(50) DEFAULT NULL COMMENT 'Full Name',
    `college` VARCHAR(100) DEFAULT NULL COMMENT 'College',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT 'Phone Number',
    `continuous_checkin_days` INT DEFAULT 0 COMMENT 'Continuous Checkin Days',
    `last_checkin_date` DATE DEFAULT NULL COMMENT 'Last Checkin Date',
    `current_avatar_frame` VARCHAR(255) DEFAULT NULL COMMENT 'Current Avatar Frame',
    `current_skin` VARCHAR(255) DEFAULT NULL COMMENT 'Current Skin',
    `current_badge` VARCHAR(255) DEFAULT NULL COMMENT 'Current Badge',
    `profile_background` VARCHAR(255) DEFAULT NULL COMMENT 'Profile Background',
    `total_online_seconds` BIGINT DEFAULT 0 COMMENT 'Total Online Seconds',
    `last_active_time` DATETIME DEFAULT NULL COMMENT 'Last Active Time',
    `allow_friend_add` TINYINT(1) DEFAULT 1 COMMENT 'Allow Friend Add',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Table';

-- 2. Course Table
DROP TABLE IF EXISTS `sys_course`;
CREATE TABLE `sys_course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `teacher_id` BIGINT NOT NULL COMMENT 'Teacher ID',
    `name` VARCHAR(255) NOT NULL COMMENT 'Course Name',
    `description` TEXT DEFAULT NULL COMMENT 'Description',
    `semester` VARCHAR(50) DEFAULT NULL COMMENT 'Semester',
    `code` VARCHAR(50) NOT NULL COMMENT 'Join Code',
    `code_expire_time` DATETIME DEFAULT NULL COMMENT 'Code Expiration Time',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_teacher_id` (`teacher_id`),
    INDEX `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Courses';

-- 3. Course Student Relation Table
DROP TABLE IF EXISTS `sys_course_student`;
CREATE TABLE `sys_course_student` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `course_id` BIGINT NOT NULL COMMENT 'Course ID',
    `student_id` BIGINT NOT NULL COMMENT 'Student ID',
    `status` TINYINT DEFAULT 0 COMMENT '0: Normal, 1: Banned',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Join Time',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_student` (`course_id`, `student_id`),
    INDEX `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Course Students';

-- 4. Check-in Record Table
DROP TABLE IF EXISTS `sys_checkin`;
CREATE TABLE `sys_checkin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `checkin_date` DATE NOT NULL COMMENT 'Check-in Date',
    `checkin_time` DATETIME NOT NULL COMMENT 'Check-in Time',
    `study_duration` INT DEFAULT 0 COMMENT 'Study Duration (minutes)',
    `is_supplementary` TINYINT(1) DEFAULT 0 COMMENT 'Is Supplementary Check-in',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Check-in Record';

-- 5. Task Table
DROP TABLE IF EXISTS `sys_task`;
CREATE TABLE `sys_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    `teacher_id` BIGINT NOT NULL COMMENT 'Teacher ID',
    `course_id` BIGINT NOT NULL COMMENT 'Course ID',
    `title` VARCHAR(255) NOT NULL COMMENT 'Task Title',
    `content` TEXT COMMENT 'Task Content',
    `attachment_url` VARCHAR(255) DEFAULT NULL COMMENT 'Attachment URL',
    `deadline` DATETIME DEFAULT NULL COMMENT 'Deadline',
    `reward_points` INT DEFAULT 0 COMMENT 'Reward Points',
    `submit_type` VARCHAR(20) DEFAULT 'TEXT' COMMENT 'TEXT, IMAGE, FILE',
    `status` TINYINT DEFAULT 1 COMMENT '0: Draft, 1: Published',
    `is_recurring` TINYINT(1) DEFAULT 0 COMMENT 'Is Recurring Task',
    `frequency` VARCHAR(20) DEFAULT NULL COMMENT 'DAILY, WEEKLY',
    `start_date` DATE DEFAULT NULL COMMENT 'Recurring Start Date',
    `end_date` DATE DEFAULT NULL COMMENT 'Recurring End Date',
    `makeup_count` INT DEFAULT 0 COMMENT 'Allowed Makeup Count',
    `makeup_cost_percent` INT DEFAULT 50 COMMENT 'Points deduction % for makeup',
    `content_template` TEXT DEFAULT NULL COMMENT 'Submission Template',
    `reminder_config` VARCHAR(255) DEFAULT NULL COMMENT 'JSON Reminder Config',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`),
    INDEX `idx_course_id` (`course_id`),
    INDEX `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Task Table';

-- 6. Task Submission Table
DROP TABLE IF EXISTS `sys_task_submission`;
CREATE TABLE `sys_task_submission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `task_id` BIGINT NOT NULL COMMENT 'Task ID',
    `student_id` BIGINT NOT NULL COMMENT 'Student User ID',
    `content` LONGTEXT COMMENT 'Submission Content',
    `file_urls` TEXT COMMENT 'File URLs (JSON)',
    `status` TINYINT DEFAULT 0 COMMENT '0: Pending, 1: Graded, 2: Returned',
    `score` INT DEFAULT 0 COMMENT 'Score/Points Awarded',
    `rating` INT DEFAULT 0 COMMENT '1-5 Star Rating',
    `comment` VARCHAR(1024) DEFAULT NULL COMMENT 'Teacher Comment',
    `similarity_score` DECIMAL(5,2) DEFAULT 0.00 COMMENT 'Plagiarism Similarity Score',
    `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `grade_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_task_student` (`task_id`, `student_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Task Submissions';

-- 7. Task Submission History Table
DROP TABLE IF EXISTS `sys_task_submission_history`;
CREATE TABLE `sys_task_submission_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `submission_id` BIGINT NOT NULL COMMENT 'Submission ID',
    `score` INT COMMENT 'Score',
    `comment` TEXT COMMENT 'Comment',
    `grader_id` BIGINT COMMENT 'Grader User ID',
    `previous_score` INT COMMENT 'Previous Score',
    `previous_comment` TEXT COMMENT 'Previous Comment',
    `operate_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_submission_id` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Task Submission History';

-- 8. Study Plan Table
DROP TABLE IF EXISTS `sys_study_plan`;
CREATE TABLE `sys_study_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `course_id` BIGINT DEFAULT NULL COMMENT 'Course ID (if linked to course)',
    `title` VARCHAR(100) NOT NULL COMMENT 'Plan Title',
    `description` VARCHAR(255) DEFAULT NULL,
    `target_hours` INT NOT NULL COMMENT 'Target Study Hours',
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0: Ongoing, 1: Completed, 2: Expired',
    `total_tasks` INT DEFAULT 0 COMMENT 'Total Tasks',
    `completed_tasks` INT DEFAULT 0 COMMENT 'Completed Tasks',
    `progress_percentage` DECIMAL(5,1) DEFAULT 0.0 COMMENT 'Progress Percentage',
    `is_point_eligible` TINYINT(1) DEFAULT 1 COMMENT 'Is Point Eligible',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Study Plan';

-- 9. Study Plan Progress History Table
DROP TABLE IF EXISTS `sys_study_plan_progress_history`;
CREATE TABLE `sys_study_plan_progress_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `plan_id` BIGINT NOT NULL COMMENT 'Study Plan ID',
    `previous_progress` DECIMAL(5,1) DEFAULT 0.0 COMMENT 'Previous Progress',
    `new_progress` DECIMAL(5,1) NOT NULL COMMENT 'New Progress',
    `completed_tasks` INT DEFAULT 0 COMMENT 'Completed Tasks at that time',
    `total_tasks` INT DEFAULT 0 COMMENT 'Total Tasks at that time',
    `note` VARCHAR(255) DEFAULT NULL COMMENT 'Progress Note/Hint',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_plan_id` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Study Plan Progress History';

-- 10. Study Plan Task Table
DROP TABLE IF EXISTS `sys_study_plan_task`;
CREATE TABLE `sys_study_plan_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `plan_id` BIGINT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `standard` TEXT,
    `priority` INT DEFAULT 0,
    `deadline` DATE DEFAULT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0: Pending, 1: Completed',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_plan_id` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Study Plan Tasks';

-- 11. Task Checkin Table (for recurring tasks)
DROP TABLE IF EXISTS `sys_task_checkin`;
CREATE TABLE `sys_task_checkin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `task_id` BIGINT NOT NULL,
    `student_id` BIGINT NOT NULL,
    `date` DATE NOT NULL,
    `checkin_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `content` TEXT,
    `file_url` VARCHAR(255),
    `is_makeup` TINYINT(1) DEFAULT 0,
    `points_awarded` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_task_student` (`task_id`, `student_id`),
    INDEX `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Task Checkins';

-- 12. Points Record Table
DROP TABLE IF EXISTS `sys_points_record`;
CREATE TABLE `sys_points_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `type` TINYINT NOT NULL COMMENT '1: Gain, 2: Consume',
    `amount` INT NOT NULL COMMENT 'Points Amount',
    `description` VARCHAR(255) DEFAULT NULL COMMENT 'Reason',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Points Record';

-- 13. Points Rule Table
DROP TABLE IF EXISTS `sys_points_rule`;
CREATE TABLE `sys_points_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `rule_key` VARCHAR(50) NOT NULL,
    `rule_value` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_key` (`rule_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Points Rules';

-- 14. Product Table (Points Shop)
DROP TABLE IF EXISTS `sys_product`;
CREATE TABLE `sys_product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    `price` INT NOT NULL COMMENT 'Price in Points',
    `image_url` VARCHAR(255) DEFAULT NULL,
    `stock` INT DEFAULT 9999,
    `type` VARCHAR(50) DEFAULT 'VIRTUAL' COMMENT 'Type: AVATAR_FRAME, SKIN, etc.',
    `category` VARCHAR(50) DEFAULT NULL,
    `sub_category` VARCHAR(50) DEFAULT NULL,
    `valid_until` DATETIME DEFAULT NULL,
    `status` TINYINT DEFAULT 1 COMMENT '0: Off-shelf, 1: On-shelf',
    `days` INT DEFAULT 0 COMMENT 'Valid duration in days',
    `video_url` VARCHAR(255) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product';

-- 15. Order Table
DROP TABLE IF EXISTS `sys_order`;
CREATE TABLE `sys_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `price` INT NOT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0: Success, 1: Refunded',
    `is_abnormal` TINYINT(1) DEFAULT 0 COMMENT 'Abnormal Order',
    `shipping_address` VARCHAR(255) DEFAULT NULL COMMENT 'Shipping Address',
    `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT 'Receiver Name',
    `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT 'Receiver Phone',
    `tracking_number` VARCHAR(100) DEFAULT NULL COMMENT 'Tracking Number',
    `shipping_status` TINYINT DEFAULT 0 COMMENT '0: Pending, 1: Shipped, 2: Delivered',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Exchange Order';

-- 16. Friendship Table
DROP TABLE IF EXISTS `sys_friendship`;
CREATE TABLE `sys_friendship` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `friend_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Friendships';

-- 17. Friend Request Table
DROP TABLE IF EXISTS `sys_friend_request`;
CREATE TABLE `sys_friend_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sender_id` BIGINT NOT NULL,
    `receiver_id` BIGINT NOT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0: Pending, 1: Accepted, 2: Rejected',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_sender_receiver` (`sender_id`, `receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Friend Requests';

-- 18. Message Table
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `sender_id` BIGINT NOT NULL COMMENT '0 for System',
    `receiver_id` BIGINT NOT NULL,
    `title` VARCHAR(255) DEFAULT NULL,
    `content` TEXT,
    `type` TINYINT DEFAULT 0 COMMENT '0: System, 1: Private, 2: Remind',
    `is_read` TINYINT(1) DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_receiver_read` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Messages';

-- 19. Announcement Table
DROP TABLE IF EXISTS `sys_announcement`;
CREATE TABLE `sys_announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT,
    `status` TINYINT DEFAULT 0 COMMENT '0: Draft, 1: Published',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Announcements';

-- 20. Notification Table
DROP TABLE IF EXISTS `sys_notification`;
CREATE TABLE `sys_notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(255) DEFAULT NULL,
    `content` TEXT,
    `type` VARCHAR(50) DEFAULT NULL,
    `is_read` TINYINT(1) DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Notifications';

-- 21. Comment Template Table
DROP TABLE IF EXISTS `sys_comment_template`;
CREATE TABLE `sys_comment_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `teacher_id` BIGINT NOT NULL COMMENT 'Teacher ID',
    `content` TEXT NOT NULL COMMENT 'Template Content',
    `category` VARCHAR(50) DEFAULT 'General' COMMENT 'Category',
    `usage_count` INT DEFAULT 0 COMMENT 'Usage Count',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Comment Templates';

-- 22. Daily Online Stats Table
DROP TABLE IF EXISTS `sys_daily_online_stats`;
CREATE TABLE `sys_daily_online_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `stats_date` DATE NOT NULL,
    `duration_seconds` BIGINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `stats_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Daily Online Stats';

-- 23. Sys Config Table
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL,
    `config_value` TEXT,
    `description` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Configurations';

-- 24. Operation Log Table
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL COMMENT 'User ID',
    `module` VARCHAR(50) DEFAULT NULL COMMENT 'Module',
    `action` VARCHAR(50) DEFAULT NULL COMMENT 'Action',
    `description` TEXT DEFAULT NULL COMMENT 'Description',
    `method` TEXT DEFAULT NULL COMMENT 'Method Name',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP Address',
    `status` TINYINT DEFAULT 0 COMMENT '0: Success, 1: Fail',
    `execution_time` BIGINT DEFAULT 0 COMMENT 'Execution Time (ms)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Operation Log';

-- 25. Login Log Table
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL COMMENT 'User ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT 'Username',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP Address',
    `device` VARCHAR(255) DEFAULT NULL COMMENT 'Device Info',
    `status` TINYINT DEFAULT 0 COMMENT '0: Success, 1: Fail',
    `message` VARCHAR(255) DEFAULT NULL COMMENT 'Message',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Login Log';

-- 26. Sensitive Log Table
DROP TABLE IF EXISTS `sys_sensitive_log`;
CREATE TABLE `sys_sensitive_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL COMMENT 'User ID',
    `content_snippet` VARCHAR(255) DEFAULT NULL COMMENT 'Content Snippet',
    `detected_words` VARCHAR(255) DEFAULT NULL COMMENT 'Detected Words',
    `source_type` VARCHAR(50) DEFAULT NULL COMMENT 'Source Type',
    `source_id` BIGINT DEFAULT NULL COMMENT 'Source ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Sensitive Content Logs';

-- 27. Blacklist Table
DROP TABLE IF EXISTS `sys_blacklist`;
CREATE TABLE `sys_blacklist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `type` VARCHAR(50) NOT NULL COMMENT 'Type: JWT, IP, SENSITIVE_WORD',
    `value` VARCHAR(255) NOT NULL COMMENT 'Blacklisted Value',
    `reason` VARCHAR(255) DEFAULT NULL COMMENT 'Reason',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_type_value` (`type`, `value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Blacklist';

-- 28. Expiration Log Table
DROP TABLE IF EXISTS `sys_expiration_log`;
CREATE TABLE `sys_expiration_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `item_id` BIGINT NOT NULL,
    `operate_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_item` (`user_id`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Expiration Logs';

SET FOREIGN_KEY_CHECKS = 1;

-- Default Data
INSERT INTO `sys_user` (`username`, `password`, `role`, `points`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuloOul.QD.G6be/7k/1.32', 'ADMIN', 99999),
('student', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuloOul.QD.G6be/7k/1.32', 'USER', 100),
('teacher', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt8AEuloOul.QD.G6be/7k/1.32', 'TEACHER', 0);

INSERT INTO `sys_product` (`name`, `description`, `price`, `image_url`, `type`) VALUES 
('Gold Frame', 'Shiny gold avatar frame', 500, 'https://placehold.co/200x200/FFD700/FFFFFF.png?text=Gold+Frame', 'AVATAR_FRAME'),
('Cool Skin', 'Cool dark theme skin', 1000, 'https://placehold.co/200x200/000000/FFFFFF.png?text=Cool+Skin', 'SKIN');

INSERT INTO `sys_points_rule` (`rule_key`, `rule_value`, `description`) VALUES 
('checkin_base', '10', 'Base points for daily check-in'),
('task_base', '50', 'Base points for completing a task');

INSERT INTO `sys_config` (`config_key`, `config_value`, `description`) VALUES 
('system_name', 'Learning Check-in System', 'Name of the application'),
('max_upload_size', '10485760', 'Max upload size in bytes (10MB)');
