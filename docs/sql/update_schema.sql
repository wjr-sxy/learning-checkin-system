-- Add progress columns to sys_study_plan
ALTER TABLE `sys_study_plan` 
ADD COLUMN `total_tasks` INT DEFAULT 0 COMMENT 'Total Tasks',
ADD COLUMN `completed_tasks` INT DEFAULT 0 COMMENT 'Completed Tasks',
ADD COLUMN `progress_percentage` DECIMAL(5,1) DEFAULT 0.0 COMMENT 'Progress Percentage';

-- Create sys_study_plan_progress_history table
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
