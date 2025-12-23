USE learning_checkin;

-- Add recurring task fields to sys_task
ALTER TABLE `sys_task` 
ADD COLUMN `is_recurring` TINYINT(1) DEFAULT 0 COMMENT 'Is Recurring Task',
ADD COLUMN `start_date` DATE DEFAULT NULL COMMENT 'Recurring Start Date',
ADD COLUMN `end_date` DATE DEFAULT NULL COMMENT 'Recurring End Date',
ADD COLUMN `makeup_count` INT DEFAULT 0 COMMENT 'Allowed Makeup Count',
ADD COLUMN `makeup_cost_percent` INT DEFAULT 100 COMMENT 'Makeup Cost Percentage',
ADD COLUMN `content_template` TEXT DEFAULT NULL COMMENT 'Content Template';

-- Add fields to sys_task_checkin
ALTER TABLE `sys_task_checkin` 
ADD COLUMN `is_makeup` TINYINT(1) DEFAULT 0 COMMENT 'Is Makeup Checkin',
ADD COLUMN `points_awarded` INT DEFAULT 0 COMMENT 'Points Awarded';
