-- Phase 1: Database Infrastructure Upgrade

-- 1. Modify sys_study_plan_task table
ALTER TABLE sys_study_plan_task 
ADD COLUMN type VARCHAR(20) DEFAULT 'CUSTOM' COMMENT 'Task Type: VIDEO, READING, HOMEWORK, QUIZ, CUSTOM',
ADD COLUMN target_value INT DEFAULT 1 COMMENT 'Target Value (e.g., minutes, pages)',
ADD COLUMN current_value INT DEFAULT 0 COMMENT 'Current Progress Value',
ADD COLUMN resource_url VARCHAR(500) COMMENT 'Resource URL';

-- 2. Create sys_learning_log table
CREATE TABLE IF NOT EXISTS sys_learning_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT,
    activity_type VARCHAR(50) COMMENT 'VIDEO_WATCH, QUIZ_PASS, etc.',
    duration INT DEFAULT 0 COMMENT 'Duration in seconds',
    data TEXT COMMENT 'Detailed JSON data',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Create sys_user_badge table
CREATE TABLE IF NOT EXISTS sys_user_badge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL COMMENT 'Product ID of the badge or specific badge code',
    reason VARCHAR(255) COMMENT 'Reason for earning',
    obtain_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_badge (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
