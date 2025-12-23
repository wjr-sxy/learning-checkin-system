USE learning_checkin;

-- Add semester to sys_course
ALTER TABLE `sys_course` ADD COLUMN `semester` VARCHAR(50) DEFAULT NULL COMMENT 'Semester';

-- Add status to sys_course_student
ALTER TABLE `sys_course_student` ADD COLUMN `status` TINYINT DEFAULT 0 COMMENT '0: Normal, 1: Banned';
ALTER TABLE `sys_course_student` ADD COLUMN `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Join Time';

-- Ensure sys_study_plan has course_id if it doesn't (checking schema logic, assuming it might need it for class distribution)
-- If sys_study_plan is already linked to user, we might need a way to link it to a course for "Class Plan" distribution.
-- Let's add course_id to sys_study_plan to support "Plan belonging to a Course"
ALTER TABLE `sys_study_plan` ADD COLUMN `course_id` BIGINT DEFAULT NULL COMMENT 'Course ID';
