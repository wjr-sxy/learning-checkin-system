package com.example.learningcheckin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化与架构保护类 (Database Initializer & Schema Guard)
 * 确保核心表结构与字段与 Java 实体类保持同步，且不破坏现有数据。
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Checking database schema and performing optimizations...");
        
        // 1. 系统配置表 (System Config)
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_config` (\n" +
                    "    `id` BIGINT NOT NULL AUTO_INCREMENT,\n" +
                    "    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT 'Config Key',\n" +
                    "    `config_value` TEXT DEFAULT NULL COMMENT 'Config Value',\n" +
                    "    `description` VARCHAR(255) DEFAULT NULL COMMENT 'Description',\n" +
                    "    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                    "    PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Config'");
            
            // 插入默认配置
            try {
                jdbcTemplate.execute("INSERT IGNORE INTO `sys_config` (config_key, config_value, description) VALUES " +
                        "('points_multiplier', '1.5', 'Global Points Multiplier (0.1-5.0)')," +
                        "('points_rule_daily_checkin', '10', 'Daily Check-in Base Points')," +
                        "('points_rule_streak_bonus', '5', 'Continuous Check-in Bonus')," +
                        "('points_rule_makeup_cost', '50', 'Makeup Check-in Cost')");
            } catch (Exception e) {}
        } catch (Exception e) {
            System.err.println("Error ensuring sys_config: " + e.getMessage());
        }

        // 2. 统一日志与通知表 (Unified Log and Notice)
        String[] unifiedTables = {
            "CREATE TABLE IF NOT EXISTS `sys_log` (\n" +
            "    `id` BIGINT NOT NULL AUTO_INCREMENT,\n" +
            "    `log_type` VARCHAR(20) NOT NULL COMMENT 'OPERATION, LOGIN, SENSITIVE, EXPIRATION',\n" +
            "    `user_id` BIGINT DEFAULT NULL,\n" +
            "    `module` VARCHAR(50) DEFAULT NULL,\n" +
            "    `action` VARCHAR(100) DEFAULT NULL,\n" +
            "    `content` TEXT,\n" +
            "    `ip` VARCHAR(50) DEFAULT NULL,\n" +
            "    `status` TINYINT DEFAULT 0,\n" +
            "    `execution_time` BIGINT DEFAULT 0,\n" +
            "    `extra_info` JSON DEFAULT NULL,\n" +
            "    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
            "    PRIMARY KEY (`id`, `create_time`),\n" +
            "    INDEX `idx_user_type_time` (`user_id`, `log_type`, `create_time`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Unified Log Table'",

            "CREATE TABLE IF NOT EXISTS `sys_notice` (\n" +
            "    `id` BIGINT NOT NULL AUTO_INCREMENT,\n" +
            "    `type` VARCHAR(20) NOT NULL COMMENT 'NOTIFICATION, MESSAGE, ANNOUNCEMENT',\n" +
            "    `sender_id` BIGINT DEFAULT NULL,\n" +
            "    `receiver_id` BIGINT DEFAULT NULL,\n" +
            "    `title` VARCHAR(255) NOT NULL,\n" +
            "    `content` TEXT NOT NULL,\n" +
            "    `is_read` TINYINT DEFAULT 0,\n" +
            "    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
            "    PRIMARY KEY (`id`),\n" +
            "    INDEX `idx_receiver_read` (`receiver_id`, `is_read`),\n" +
            "    INDEX `idx_type_time` (`type`, `create_time`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Unified Communication Table'"
        };
        executeSafe(unifiedTables);

        // 2. 用户表字段扩展 (User Table Extensions)
        String[] userColumns = {
            "ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `full_name` VARCHAR(50) DEFAULT NULL COMMENT 'Full Name'",
            "ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `college` VARCHAR(100) DEFAULT NULL COMMENT 'College'",
            "ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `phone` VARCHAR(20) DEFAULT NULL COMMENT 'Phone Number'",
            "ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `continuous_checkin_days` INT DEFAULT 0 COMMENT 'Continuous Checkin Days'",
            "ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `last_checkin_date` DATE DEFAULT NULL COMMENT 'Last Checkin Date'",
            "ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `total_online_seconds` BIGINT DEFAULT 0 COMMENT 'Total Online Seconds'",
            "ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `last_active_time` DATETIME DEFAULT NULL COMMENT 'Last Active Time'",
            "ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `allow_friend_add` TINYINT(1) DEFAULT 1 COMMENT 'Allow Friend Add'",
            "ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `status` TINYINT DEFAULT 0 COMMENT '0: Normal, 1: Banned'",
            "ALTER TABLE `sys_user` MODIFY COLUMN `avatar` LONGTEXT DEFAULT NULL COMMENT 'Avatar URL/Base64'"
        };
        executeSafe(userColumns);

        // 3. 任务表字段扩展 (Task Table Extensions)
        String[] taskColumns = {
            "ALTER TABLE `sys_task` ADD COLUMN IF NOT EXISTS `teacher_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Teacher ID'",
            "ALTER TABLE `sys_task` ADD COLUMN IF NOT EXISTS `attachment_url` VARCHAR(255) DEFAULT NULL COMMENT 'Attachment URL'",
            "ALTER TABLE `sys_task` ADD COLUMN IF NOT EXISTS `submit_type` VARCHAR(20) DEFAULT 'TEXT' COMMENT 'TEXT, IMAGE, FILE'",
            "ALTER TABLE `sys_task` ADD COLUMN IF NOT EXISTS `status` TINYINT DEFAULT 1 COMMENT '0: Disabled, 1: Enabled'",
            "ALTER TABLE `sys_task` ADD COLUMN IF NOT EXISTS `frequency` VARCHAR(20) DEFAULT NULL COMMENT 'DAILY, WEEKLY'",
            "ALTER TABLE `sys_task` ADD COLUMN IF NOT EXISTS `reminder_config` VARCHAR(255) DEFAULT NULL COMMENT 'JSON Reminder Config'"
        };
        executeSafe(taskColumns);

        // 4. 学习计划表扩展 (Study Plan Extensions)
        String[] planColumns = {
            "ALTER TABLE `sys_study_plan` ADD COLUMN IF NOT EXISTS `total_tasks` INT DEFAULT 0 COMMENT 'Total Tasks'",
            "ALTER TABLE `sys_study_plan` ADD COLUMN IF NOT EXISTS `completed_tasks` INT DEFAULT 0 COMMENT 'Completed Tasks'",
            "ALTER TABLE `sys_study_plan` ADD COLUMN IF NOT EXISTS `progress_percentage` DECIMAL(5,1) DEFAULT 0.0 COMMENT 'Progress %'",
            "ALTER TABLE `sys_study_plan` ADD COLUMN IF NOT EXISTS `is_point_eligible` TINYINT(1) DEFAULT 1 COMMENT 'Is Point Eligible'"
        };
        executeSafe(planColumns);

        // 5. 商品表扩展 (Product Extensions)
        String[] productColumns = {
            "ALTER TABLE `sys_product` ADD COLUMN IF NOT EXISTS `category` VARCHAR(50) DEFAULT NULL COMMENT 'Main Category'",
            "ALTER TABLE `sys_product` ADD COLUMN IF NOT EXISTS `sub_category` VARCHAR(50) DEFAULT NULL COMMENT 'Sub Category'",
            "ALTER TABLE `sys_product` ADD COLUMN IF NOT EXISTS `valid_until` DATETIME DEFAULT NULL COMMENT 'Valid Until'",
            "ALTER TABLE `sys_product` ADD COLUMN IF NOT EXISTS `status` INT DEFAULT 1 COMMENT '0: Off, 1: On'",
            "ALTER TABLE `sys_product` ADD COLUMN IF NOT EXISTS `days` INT DEFAULT 0 COMMENT 'Valid Days'",
            "ALTER TABLE `sys_product` ADD COLUMN IF NOT EXISTS `video_url` VARCHAR(255) DEFAULT NULL COMMENT 'Video URL'"
        };
        executeSafe(productColumns);

        // 6. 核心业务表创建 (Core Business Tables)
        String[] tableDDLs = {
            "CREATE TABLE IF NOT EXISTS `sys_study_plan_progress_history` (\n" +
            "    `id` BIGINT NOT NULL AUTO_INCREMENT,\n" +
            "    `plan_id` BIGINT NOT NULL,\n" +
            "    `previous_progress` DECIMAL(5,1) DEFAULT 0.0,\n" +
            "    `new_progress` DECIMAL(5,1) NOT NULL,\n" +
            "    `completed_tasks` INT DEFAULT 0,\n" +
            "    `total_tasks` INT DEFAULT 0,\n" +
            "    `note` VARCHAR(255) DEFAULT NULL,\n" +
            "    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
            "    PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

            "CREATE TABLE IF NOT EXISTS `sys_study_plan_task` (\n" +
            "    `id` BIGINT NOT NULL AUTO_INCREMENT,\n" +
            "    `plan_id` BIGINT NOT NULL,\n" +
            "    `title` VARCHAR(255) NOT NULL,\n" +
            "    `description` TEXT,\n" +
            "    `standard` VARCHAR(255),\n" +
            "    `priority` INT DEFAULT 0,\n" +
            "    `deadline` DATE,\n" +
            "    `status` INT DEFAULT 0,\n" +
            "    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
            "    PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

            "CREATE TABLE IF NOT EXISTS `sys_comment_template` (\n" +
            "    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
            "    `teacher_id` BIGINT NOT NULL,\n" +
            "    `content` TEXT NOT NULL,\n" +
            "    `category` VARCHAR(50) DEFAULT 'General',\n" +
            "    `usage_count` INT DEFAULT 0,\n" +
            "    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
            "    INDEX `idx_teacher` (`teacher_id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

            "CREATE TABLE IF NOT EXISTS `sys_task_submission_history` (\n" +
            "    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
            "    `submission_id` BIGINT NOT NULL,\n" +
            "    `score` INT,\n" +
            "    `comment` TEXT,\n" +
            "    `grader_id` BIGINT,\n" +
            "    `operate_time` DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
            "    `previous_score` INT,\n" +
            "    `previous_comment` TEXT,\n" +
            "    INDEX `idx_submission` (`submission_id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

            "CREATE TABLE IF NOT EXISTS `sys_daily_online_stats` (\n" +
            "    `id` BIGINT NOT NULL AUTO_INCREMENT,\n" +
            "    `user_id` BIGINT NOT NULL,\n" +
            "    `stats_date` DATE NOT NULL,\n" +
            "    `duration_seconds` BIGINT DEFAULT 0,\n" +
            "    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
            "    PRIMARY KEY (`id`),\n" +
            "    UNIQUE KEY `uk_user_date` (`user_id`, `stats_date`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        };
        executeSafe(tableDDLs);

        // 7. 移除旧的积分规则表 (Cleanup old tables)
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS `sys_points_rule` ");
        } catch (Exception e) {}

        System.out.println("Database schema optimization completed.");
    }

    /**
     * 安全执行 SQL 语句，忽略常见错误（如字段已存在、表已存在等）
     */
    private void executeSafe(String[] sqls) {
        for (String sql : sqls) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                // 静默忽略常见错误，生产环境建议记录 DEBUG 日志
            }
        }
    }
}
