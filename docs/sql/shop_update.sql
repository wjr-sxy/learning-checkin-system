ALTER TABLE `sys_product` 
ADD COLUMN `category` VARCHAR(50) COMMENT 'Main Category',
ADD COLUMN `sub_category` VARCHAR(50) COMMENT 'Sub Category',
ADD COLUMN `valid_until` DATETIME COMMENT 'Listing Expiration Date',
ADD COLUMN `status` INT DEFAULT 1 COMMENT '0: Off-shelf, 1: On-shelf';
