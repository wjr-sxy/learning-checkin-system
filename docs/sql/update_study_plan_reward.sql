-- Add reward_points column to sys_study_plan table
ALTER TABLE `sys_study_plan` ADD COLUMN `reward_points` INT DEFAULT 0 COMMENT 'Reward Points' AFTER `is_point_eligible`;
