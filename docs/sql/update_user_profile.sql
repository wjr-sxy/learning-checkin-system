USE `learning_checkin`;

ALTER TABLE `sys_user`
ADD COLUMN `full_name` VARCHAR(50) DEFAULT NULL COMMENT 'Full Name',
ADD COLUMN `college` VARCHAR(100) DEFAULT NULL COMMENT 'College',
ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT 'Phone Number',
ADD COLUMN `current_avatar_frame` VARCHAR(255) DEFAULT NULL COMMENT 'Current Avatar Frame',
ADD COLUMN `current_skin` VARCHAR(255) DEFAULT NULL COMMENT 'Current Skin',
ADD COLUMN `current_badge` VARCHAR(255) DEFAULT NULL COMMENT 'Current Badge';
