/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : learning_checkin_optimized

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 30/12/2025 00:10:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `sys_blacklist`;
CREATE TABLE `sys_blacklist`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Type: JWT, IP, SENSITIVE_WORD',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Blacklisted Value',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Reason',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type_value`(`type` ASC, `value` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Blacklist' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_blacklist
-- ----------------------------

-- ----------------------------
-- Table structure for sys_checkin
-- ----------------------------
DROP TABLE IF EXISTS `sys_checkin`;
CREATE TABLE `sys_checkin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `checkin_date` date NOT NULL COMMENT 'Check-in Date',
  `checkin_time` datetime NOT NULL COMMENT 'Check-in Time',
  `study_duration` int NULL DEFAULT 0 COMMENT 'Study Duration (minutes)',
  `is_supplementary` tinyint(1) NULL DEFAULT 0 COMMENT 'Is Supplementary Check-in',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Check-in IP Address',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id` ASC, `checkin_date` ASC) USING BTREE,
  INDEX `idx_date`(`checkin_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Check-in Record' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_checkin
-- ----------------------------
INSERT INTO `sys_checkin` VALUES (1, 28, '2025-12-03', '2025-12-03 00:50:08', 0, 0, NULL, '2025-12-03 00:50:08');
INSERT INTO `sys_checkin` VALUES (2, 29, '2025-12-03', '2025-12-03 00:56:25', 0, 0, NULL, '2025-12-03 00:56:24');
INSERT INTO `sys_checkin` VALUES (3, 1, '2025-12-03', '2025-12-03 09:49:14', 0, 0, NULL, '2025-12-03 09:49:14');
INSERT INTO `sys_checkin` VALUES (4, 29, '2025-12-04', '2025-12-04 00:46:11', 0, 0, NULL, '2025-12-04 00:46:10');
INSERT INTO `sys_checkin` VALUES (5, 29, '2025-12-08', '2025-12-08 15:26:05', 0, 0, NULL, '2025-12-08 15:26:05');
INSERT INTO `sys_checkin` VALUES (6, 29, '2025-12-09', '2025-12-09 10:43:23', 0, 0, NULL, '2025-12-09 10:43:23');
INSERT INTO `sys_checkin` VALUES (7, 29, '2025-12-12', '2025-12-12 14:58:47', 0, 0, NULL, '2025-12-12 14:58:46');
INSERT INTO `sys_checkin` VALUES (8, 83, '2025-12-12', '2025-12-12 15:13:58', 0, 0, NULL, '2025-12-12 15:13:58');
INSERT INTO `sys_checkin` VALUES (9, 29, '2025-12-15', '2025-12-15 10:16:18', 0, 0, NULL, '2025-12-15 10:16:18');
INSERT INTO `sys_checkin` VALUES (10, 29, '2025-12-24', '2025-12-24 01:11:58', 0, 0, NULL, '2025-12-24 01:11:57');
INSERT INTO `sys_checkin` VALUES (11, 29, '2025-12-25', '2025-12-25 17:49:48', 0, 0, '127.0.0.1', '2025-12-25 17:49:48');

-- ----------------------------
-- Table structure for sys_comment_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_comment_template`;
CREATE TABLE `sys_comment_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint NOT NULL COMMENT 'Teacher ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Template Content',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'General' COMMENT 'Category',
  `usage_count` int NULL DEFAULT 0 COMMENT 'Usage Count',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Comment Templates' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_comment_template
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 88 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'System Configurations' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'system_name', 'Learning Check-in System', 'Name of the application');
INSERT INTO `sys_config` VALUES (2, 'max_upload_size', '10485760', 'Max upload size in bytes (10MB)');
INSERT INTO `sys_config` VALUES (3, 'points_multiplier', '1.5', 'Global Points Multiplier (0.1-5.0)');
INSERT INTO `sys_config` VALUES (5, 'points.checkin_base', '10', 'Base points for daily check-in');
INSERT INTO `sys_config` VALUES (6, 'points.task_base', '50', 'Base points for completing a task');
INSERT INTO `sys_config` VALUES (7, 'points.daily_checkin', '10', 'Daily Check-in Base Points');
INSERT INTO `sys_config` VALUES (8, 'points.streak_bonus', '5', 'Continuous Check-in Bonus');
INSERT INTO `sys_config` VALUES (9, 'points.makeup_cost', '50', 'Makeup Check-in Cost');
INSERT INTO `sys_config` VALUES (12, 'points_rule_daily_checkin', '10', 'Daily Check-in Base Points');
INSERT INTO `sys_config` VALUES (13, 'points_rule_streak_bonus', '5', 'Continuous Check-in Bonus');
INSERT INTO `sys_config` VALUES (14, 'points_rule_makeup_cost', '50', 'Makeup Check-in Cost');

-- ----------------------------
-- Table structure for sys_course
-- ----------------------------
DROP TABLE IF EXISTS `sys_course`;
CREATE TABLE `sys_course`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint NOT NULL COMMENT 'Teacher ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Course Name',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Description',
  `semester` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Semester',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Join Code',
  `code_expire_time` datetime NULL DEFAULT NULL COMMENT 'Code Expiration Time',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Courses' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_course
-- ----------------------------
INSERT INTO `sys_course` VALUES (1, 30, '课程测试', '测试', NULL, '2F949A55', '2025-12-22 19:15:37', '2025-12-03 21:57:35', '2025-12-24 00:51:11');
INSERT INTO `sys_course` VALUES (19, 30, '测试001', '测试相关功能', '2025-2026-1', 'B07204CB', '2025-12-22 19:15:39', '2025-12-15 17:38:29', '2025-12-24 00:51:11');

-- ----------------------------
-- Table structure for sys_course_student
-- ----------------------------
DROP TABLE IF EXISTS `sys_course_student`;
CREATE TABLE `sys_course_student`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT 'Course ID',
  `student_id` bigint NOT NULL COMMENT 'Student ID',
  `status` tinyint NULL DEFAULT 0 COMMENT '0: Normal, 1: Banned',
  `join_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Join Time',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_student`(`course_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Course Students' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_course_student
-- ----------------------------
INSERT INTO `sys_course_student` VALUES (1, 1, 29, 0, '2025-12-04 01:08:28', '2025-12-03 21:58:59');
INSERT INTO `sys_course_student` VALUES (21, 19, 29, 0, '2025-12-15 19:17:57', '2025-12-15 19:17:57');

-- ----------------------------
-- Table structure for sys_daily_online_stats
-- ----------------------------
DROP TABLE IF EXISTS `sys_daily_online_stats`;
CREATE TABLE `sys_daily_online_stats`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `stats_date` date NOT NULL,
  `online_seconds` int NULL DEFAULT 0 COMMENT '当日在线秒数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_active_time` datetime NULL DEFAULT NULL COMMENT '最后活跃时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id` ASC, `stats_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3054 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Daily Online Stats' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_daily_online_stats
-- ----------------------------
INSERT INTO `sys_daily_online_stats` VALUES (1, 30, '2025-12-04', 7620, '2025-12-04 21:53:26', '2025-12-04 23:23:00', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (84, 1, '2025-12-04', 1500, '2025-12-04 22:42:40', '2025-12-04 23:23:09', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (131, 29, '2025-12-04', 600, '2025-12-04 23:13:55', '2025-12-04 23:22:55', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (163, 29, '2025-12-08', 60, '2025-12-08 15:26:01', '2025-12-08 15:26:01', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (164, 1, '2025-12-08', 28560, '2025-12-08 15:27:56', '2025-12-08 23:22:56', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (640, 1, '2025-12-09', 60, '2025-12-09 08:46:06', '2025-12-09 08:46:06', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (641, 29, '2025-12-09', 23280, '2025-12-09 08:47:16', '2025-12-09 16:13:11', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1029, 29, '2025-12-10', 60, '2025-12-10 23:53:24', '2025-12-10 23:53:24', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1030, 83, '2025-12-11', 1200, '2025-12-11 00:10:30', '2025-12-11 00:29:31', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1050, 30, '2025-12-11', 180, '2025-12-11 20:31:55', '2025-12-11 20:33:55', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1053, 29, '2025-12-11', 7920, '2025-12-11 20:35:30', '2025-12-11 22:47:41', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1055, 84, '2025-12-11', 7440, '2025-12-11 20:37:00', '2025-12-11 22:41:14', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1309, 29, '2025-12-12', 3660, '2025-12-12 11:42:45', '2025-12-12 20:32:15', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1324, 1, '2025-12-12', 900, '2025-12-12 12:13:32', '2025-12-12 20:29:37', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1326, 83, '2025-12-12', 360, '2025-12-12 15:14:35', '2025-12-12 20:05:58', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1347, 30, '2025-12-12', 240, '2025-12-12 20:10:07', '2025-12-12 20:14:02', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1395, 29, '2025-12-15', 12600, '2025-12-15 10:16:04', '2025-12-15 19:08:54', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1419, 1, '2025-12-15', 4380, '2025-12-15 10:52:19', '2025-12-15 11:29:46', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1597, 30, '2025-12-15', 10800, '2025-12-15 15:33:09', '2025-12-15 20:34:02', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1858, 29, '2025-12-18', 3780, '2025-12-18 12:47:02', '2025-12-18 13:49:59', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1859, 30, '2025-12-18', 3780, '2025-12-18 12:47:13', '2025-12-18 13:49:59', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1860, 1, '2025-12-18', 3780, '2025-12-18 12:47:29', '2025-12-18 13:49:59', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1861, 2, '2025-12-25', 60, '2025-12-25 14:50:42', '2025-12-25 14:50:42', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1862, 29, '2025-12-25', 8095, '2025-12-25 14:52:19', '2025-12-25 17:51:40', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1875, 3, '2025-12-25', 7740, '2025-12-25 15:37:46', '2025-12-25 17:51:15', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (1876, 1, '2025-12-25', 7500, '2025-12-25 15:38:38', '2025-12-25 17:51:15', NULL);
INSERT INTO `sys_daily_online_stats` VALUES (2255, 83, '2025-12-26', 10589, '2025-12-26 17:14:50', '2025-12-26 19:40:30', '2025-12-26 19:40:31');
INSERT INTO `sys_daily_online_stats` VALUES (2256, 3, '2025-12-26', 8280, '2025-12-26 17:14:50', '2025-12-26 19:39:50', '2025-12-26 19:39:51');
INSERT INTO `sys_daily_online_stats` VALUES (2257, 29, '2025-12-26', 17012, '2025-12-26 17:14:50', '2025-12-26 19:40:38', '2025-12-26 19:40:38');
INSERT INTO `sys_daily_online_stats` VALUES (2258, 1, '2025-12-26', 8160, '2025-12-26 17:14:50', '2025-12-26 19:39:50', '2025-12-26 19:39:51');

-- ----------------------------
-- Table structure for sys_friend_request
-- ----------------------------
DROP TABLE IF EXISTS `sys_friend_request`;
CREATE TABLE `sys_friend_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NOT NULL,
  `receiver_id` bigint NOT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '0: Pending, 1: Accepted, 2: Rejected',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender_receiver`(`sender_id` ASC, `receiver_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Friend Requests' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_friend_request
-- ----------------------------
INSERT INTO `sys_friend_request` VALUES (1, 2, 29, 1, '2025-12-25 14:50:49', '2025-12-25 14:50:49');
INSERT INTO `sys_friend_request` VALUES (2, 2, 84, 0, '2025-12-25 14:50:52', '2025-12-25 14:50:51');
INSERT INTO `sys_friend_request` VALUES (3, 83, 29, 1, '2025-12-26 17:15:48', '2025-12-26 17:15:47');

-- ----------------------------
-- Table structure for sys_friendship
-- ----------------------------
DROP TABLE IF EXISTS `sys_friendship`;
CREATE TABLE `sys_friendship`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `friend_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_friend`(`user_id` ASC, `friend_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Friendships' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_friendship
-- ----------------------------
INSERT INTO `sys_friendship` VALUES (1, 29, 30, '2025-12-10 23:53:22');
INSERT INTO `sys_friendship` VALUES (2, 29, 2, '2025-12-25 14:52:59');
INSERT INTO `sys_friendship` VALUES (3, 2, 29, '2025-12-25 14:52:59');
INSERT INTO `sys_friendship` VALUES (4, 29, 83, '2025-12-26 17:15:54');
INSERT INTO `sys_friendship` VALUES (5, 83, 29, '2025-12-26 17:15:54');

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `log_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OPERATION, LOGIN, SENSITIVE, EXPIRATION',
  `user_id` bigint NULL DEFAULT NULL,
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `action` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Description/Message',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '0: Success, 1: Fail',
  `execution_time` bigint NULL DEFAULT 0 COMMENT 'ms',
  `extra_info` json NULL COMMENT 'JSON for device, IDs, words, etc.',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`, `create_time`) USING BTREE,
  INDEX `idx_user_type_time`(`user_id` ASC, `log_type` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 377 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Unified Log Table' ROW_FORMAT = Dynamic PARTITION BY RANGE (to_days(`create_time`))
PARTITIONS 3
(PARTITION `p_2025_12` VALUES LESS THAN (739982) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p_2026_01` VALUES LESS THAN (740013) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 ,
PARTITION `p_future` VALUES LESS THAN (MAXVALUE) ENGINE = InnoDB MAX_ROWS = 0 MIN_ROWS = 0 )
;

-- ----------------------------
-- Records of sys_log
-- ----------------------------
INSERT INTO `sys_log` VALUES (4, 'LOGIN', 29, 'Auth', 'Login', 'Incorrect password', '127.0.0.1', 1, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-03 23:25:52');
INSERT INTO `sys_log` VALUES (5, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-03 23:25:57');
INSERT INTO `sys_log` VALUES (6, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-03 23:26:06');
INSERT INTO `sys_log` VALUES (7, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-03 23:26:25');
INSERT INTO `sys_log` VALUES (8, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-03 23:26:51');
INSERT INTO `sys_log` VALUES (9, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 00:46:07');
INSERT INTO `sys_log` VALUES (10, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 00:46:48');
INSERT INTO `sys_log` VALUES (11, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 08:47:58');
INSERT INTO `sys_log` VALUES (12, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 08:55:11');
INSERT INTO `sys_log` VALUES (13, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 08:56:12');
INSERT INTO `sys_log` VALUES (14, 'LOGIN', 30, 'Auth', 'Login', 'Incorrect password', '127.0.0.1', 1, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 08:56:41');
INSERT INTO `sys_log` VALUES (15, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 08:56:47');
INSERT INTO `sys_log` VALUES (16, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 09:29:58');
INSERT INTO `sys_log` VALUES (17, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 09:30:25');
INSERT INTO `sys_log` VALUES (18, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 10:27:31');
INSERT INTO `sys_log` VALUES (19, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 10:27:53');
INSERT INTO `sys_log` VALUES (20, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 10:28:14');
INSERT INTO `sys_log` VALUES (21, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 10:28:40');
INSERT INTO `sys_log` VALUES (22, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 11:47:41');
INSERT INTO `sys_log` VALUES (23, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 11:56:22');
INSERT INTO `sys_log` VALUES (24, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 11:58:02');
INSERT INTO `sys_log` VALUES (25, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 12:13:06');
INSERT INTO `sys_log` VALUES (26, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 12:13:17');
INSERT INTO `sys_log` VALUES (27, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 12:49:42');
INSERT INTO `sys_log` VALUES (28, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 12:50:43');
INSERT INTO `sys_log` VALUES (29, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 18:26:16');
INSERT INTO `sys_log` VALUES (30, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 18:35:19');
INSERT INTO `sys_log` VALUES (31, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 18:53:12');
INSERT INTO `sys_log` VALUES (32, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 21:46:24');
INSERT INTO `sys_log` VALUES (33, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 21:50:02');
INSERT INTO `sys_log` VALUES (34, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 21:57:37');
INSERT INTO `sys_log` VALUES (35, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 22:41:41');
INSERT INTO `sys_log` VALUES (36, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 23:01:46');
INSERT INTO `sys_log` VALUES (37, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 23:02:17');
INSERT INTO `sys_log` VALUES (38, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 23:12:34');
INSERT INTO `sys_log` VALUES (39, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 23:16:21');
INSERT INTO `sys_log` VALUES (40, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 23:17:10');
INSERT INTO `sys_log` VALUES (41, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-08 15:25:00');
INSERT INTO `sys_log` VALUES (42, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-08 15:26:32');
INSERT INTO `sys_log` VALUES (43, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-08 15:26:56');
INSERT INTO `sys_log` VALUES (44, 'LOGIN', 1, 'Auth', 'Login', 'Incorrect password', '127.0.0.1', 1, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-08 15:57:50');
INSERT INTO `sys_log` VALUES (45, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-09 08:43:59');
INSERT INTO `sys_log` VALUES (46, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-09 08:45:06');
INSERT INTO `sys_log` VALUES (47, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-09 08:46:16');
INSERT INTO `sys_log` VALUES (48, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-09 10:33:47');
INSERT INTO `sys_log` VALUES (49, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-10 23:52:24');
INSERT INTO `sys_log` VALUES (50, 'LOGIN', 83, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr2\"}', '2025-12-10 23:55:19');
INSERT INTO `sys_log` VALUES (51, 'LOGIN', 84, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr3\"}', '2025-12-11 20:30:13');
INSERT INTO `sys_log` VALUES (52, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-11 20:30:55');
INSERT INTO `sys_log` VALUES (53, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-11 20:34:25');
INSERT INTO `sys_log` VALUES (54, 'LOGIN', 84, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr3\"}', '2025-12-11 20:36:00');
INSERT INTO `sys_log` VALUES (55, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 11:40:44');
INSERT INTO `sys_log` VALUES (56, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-12 12:12:32');
INSERT INTO `sys_log` VALUES (57, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 14:58:12');
INSERT INTO `sys_log` VALUES (58, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-12 14:59:12');
INSERT INTO `sys_log` VALUES (59, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-12 14:59:51');
INSERT INTO `sys_log` VALUES (60, 'LOGIN', 83, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr2\"}', '2025-12-12 15:13:36');
INSERT INTO `sys_log` VALUES (61, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 16:54:08');
INSERT INTO `sys_log` VALUES (62, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:02:48');
INSERT INTO `sys_log` VALUES (63, 'LOGIN', 83, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr2\"}', '2025-12-12 20:04:33');
INSERT INTO `sys_log` VALUES (64, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:06:10');
INSERT INTO `sys_log` VALUES (65, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-12 20:06:53');
INSERT INTO `sys_log` VALUES (66, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:07:31');
INSERT INTO `sys_log` VALUES (67, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-12 20:09:07');
INSERT INTO `sys_log` VALUES (68, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:10:36');
INSERT INTO `sys_log` VALUES (69, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-12 20:11:02');
INSERT INTO `sys_log` VALUES (70, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:13:24');
INSERT INTO `sys_log` VALUES (71, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:14:41');
INSERT INTO `sys_log` VALUES (72, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-12 20:17:37');
INSERT INTO `sys_log` VALUES (73, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 10:15:04');
INSERT INTO `sys_log` VALUES (74, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 10:50:29');
INSERT INTO `sys_log` VALUES (75, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-15 10:51:19');
INSERT INTO `sys_log` VALUES (76, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-15 11:00:17');
INSERT INTO `sys_log` VALUES (77, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-15 11:02:31');
INSERT INTO `sys_log` VALUES (78, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 15:31:54');
INSERT INTO `sys_log` VALUES (79, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 15:57:09');
INSERT INTO `sys_log` VALUES (80, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 15:58:07');
INSERT INTO `sys_log` VALUES (81, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 15:59:06');
INSERT INTO `sys_log` VALUES (82, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 15:59:43');
INSERT INTO `sys_log` VALUES (83, 'LOGIN', 4, 'Auth', 'Login', 'Incorrect password', '127.0.0.1', 1, 0, '{\"device\": null, \"username\": \"\"}', '2025-12-15 16:00:12');
INSERT INTO `sys_log` VALUES (84, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-15 16:05:44');
INSERT INTO `sys_log` VALUES (85, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 16:06:45');
INSERT INTO `sys_log` VALUES (86, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 16:07:34');
INSERT INTO `sys_log` VALUES (87, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 16:27:53');
INSERT INTO `sys_log` VALUES (88, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 16:28:20');
INSERT INTO `sys_log` VALUES (89, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 16:29:12');
INSERT INTO `sys_log` VALUES (90, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 16:29:47');
INSERT INTO `sys_log` VALUES (91, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 17:38:54');
INSERT INTO `sys_log` VALUES (92, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 17:39:24');
INSERT INTO `sys_log` VALUES (93, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 17:47:52');
INSERT INTO `sys_log` VALUES (94, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 19:10:49');
INSERT INTO `sys_log` VALUES (95, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 19:17:54');
INSERT INTO `sys_log` VALUES (96, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 19:18:13');
INSERT INTO `sys_log` VALUES (97, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 19:19:21');
INSERT INTO `sys_log` VALUES (98, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 19:19:58');
INSERT INTO `sys_log` VALUES (99, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-18 12:46:00');
INSERT INTO `sys_log` VALUES (100, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-18 12:46:12');
INSERT INTO `sys_log` VALUES (101, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-18 12:46:28');
INSERT INTO `sys_log` VALUES (102, 'OPERATION', 1, '商城运营', '标记异常', '3', '127.0.0.1', 0, 16, '{\"method\": \"com.example.learningcheckin.controller.AdminShopController.markAbnormal()\"}', '2025-12-04 12:52:16');
INSERT INTO `sys_log` VALUES (103, 'OPERATION', 1, '商城运营', '退款', '3', '127.0.0.1', 0, 52, '{\"method\": \"com.example.learningcheckin.controller.AdminShopController.refundOrder()\"}', '2025-12-04 12:52:27');
INSERT INTO `sys_log` VALUES (104, 'OPERATION', 1, '商城运营', '取消异常', '3', '127.0.0.1', 0, 20, '{\"method\": \"com.example.learningcheckin.controller.AdminShopController.cancelAbnormal()\"}', '2025-12-04 18:05:32');
INSERT INTO `sys_log` VALUES (105, 'OPERATION', 1, '系统工具', '保存公告', 'Announcement(id=1, title=测试, content=来了老弟, status=1, createTime=2025-12-04T23:01:34.991)', '127.0.0.1', 0, 12, '{\"method\": \"com.example.learningcheckin.controller.AdminSystemController.saveAnnouncement()\"}', '2025-12-04 23:01:35');
INSERT INTO `sys_log` VALUES (106, 'OPERATION', 1, '用户管理', '调整积分', '29', '127.0.0.1', 0, 11, '{\"method\": \"com.example.learningcheckin.controller.AdminUserController.adjustPoints()\"}', '2025-12-12 15:00:15');
INSERT INTO `sys_log` VALUES (107, 'OPERATION', 30, 'Task', 'Create', '\r\n### Error updating database.  Cause: java.sql.SQLException: Field \'deadline\' doesn\'t have a default value\r\n### The error may exist in com/example/learningcheckin/mapper/TaskMapper.java (best guess)\r\n### The error may involve com.example.learningcheckin.mapper.TaskMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO sys_task  ( teacher_id,  title, content,   reward_points, submit_type, status, is_recurring, frequency, start_date, end_date, makeup_count, makeup_cost_percent, content_template,  create_time )  VALUES  ( ?,  ?, ?,   ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ? )\r\n### Cause: java.sql.SQLException: Field \'deadline\' doesn\'t have a default value\n; Field \'deadline\' doesn\'t have a default value; nested exception is java.sql.SQLException: Field \'deadline\' doesn\'t have a default value', '127.0.0.1', 1, 30, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 15:38:10');
INSERT INTO `sys_log` VALUES (108, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50048, teacherId=30, courseId=null, title=测试, content=, attachmentUrl=null, deadline=null, rewardPoints=15, submitType=TEXT, status=1, isRecurring=true, frequency=DAILY, startDate=2025-12-15, ...', '127.0.0.1', 0, 51, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 15:40:32');
INSERT INTO `sys_log` VALUES (109, 'OPERATION', 30, 'Task', 'Grade', '1', '127.0.0.1', 0, 24, '{\"method\": \"com.example.learningcheckin.controller.TaskController.gradeSubmission()\"}', '2025-12-15 15:59:57');
INSERT INTO `sys_log` VALUES (110, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50049, teacherId=30, courseId=null, title=检查发布任务, content=, attachmentUrl=null, deadline=null, rewardPoints=15, submitType=TEXT, status=1, isRecurring=true, frequency=DAILY, startDate=2025-12-...', '127.0.0.1', 0, 18, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 16:08:29');
INSERT INTO `sys_log` VALUES (111, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50050, teacherId=30, courseId=null, title=测试, content=去打卡, attachmentUrl=null, deadline=null, rewardPoints=15, submitType=FILE, status=1, isRecurring=true, frequency=DAILY, startDate=2025-12-1...', '127.0.0.1', 0, 24, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 16:28:59');
INSERT INTO `sys_log` VALUES (112, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50051, teacherId=30, courseId=null, title=每日任务, content=测试100, attachmentUrl=, deadline=null, rewardPoints=30, submitType=TEXT, status=1, isRecurring=true, frequency=DAILY, startDate=2025-12-1...', '127.0.0.1', 0, 50, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 19:19:06');
INSERT INTO `sys_log` VALUES (113, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50052, teacherId=30, courseId=null, title=ffff, content=wfewfwefe, attachmentUrl=, deadline=2025-12-15T03:04, rewardPoints=15, submitType=TEXT, status=1, isRecurring=false, frequency=DAILY, st...', '127.0.0.1', 0, 47, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 20:33:40');
INSERT INTO `sys_log` VALUES (114, 'LOGIN', 29, 'Auth', 'Login', 'Incorrect password', '127.0.0.1', 1, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-03 23:25:52');
INSERT INTO `sys_log` VALUES (115, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-03 23:25:57');
INSERT INTO `sys_log` VALUES (116, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-03 23:26:06');
INSERT INTO `sys_log` VALUES (117, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-03 23:26:25');
INSERT INTO `sys_log` VALUES (118, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-03 23:26:51');
INSERT INTO `sys_log` VALUES (119, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 00:46:07');
INSERT INTO `sys_log` VALUES (120, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 00:46:48');
INSERT INTO `sys_log` VALUES (121, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 08:47:58');
INSERT INTO `sys_log` VALUES (122, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 08:55:11');
INSERT INTO `sys_log` VALUES (123, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 08:56:12');
INSERT INTO `sys_log` VALUES (124, 'LOGIN', 30, 'Auth', 'Login', 'Incorrect password', '127.0.0.1', 1, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 08:56:41');
INSERT INTO `sys_log` VALUES (125, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 08:56:47');
INSERT INTO `sys_log` VALUES (126, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 09:29:58');
INSERT INTO `sys_log` VALUES (127, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 09:30:25');
INSERT INTO `sys_log` VALUES (128, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 10:27:31');
INSERT INTO `sys_log` VALUES (129, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 10:27:53');
INSERT INTO `sys_log` VALUES (130, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 10:28:14');
INSERT INTO `sys_log` VALUES (131, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 10:28:40');
INSERT INTO `sys_log` VALUES (132, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 11:47:41');
INSERT INTO `sys_log` VALUES (133, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 11:56:22');
INSERT INTO `sys_log` VALUES (134, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 11:58:02');
INSERT INTO `sys_log` VALUES (135, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 12:13:06');
INSERT INTO `sys_log` VALUES (136, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 12:13:17');
INSERT INTO `sys_log` VALUES (137, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 12:49:42');
INSERT INTO `sys_log` VALUES (138, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 12:50:43');
INSERT INTO `sys_log` VALUES (139, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 18:26:16');
INSERT INTO `sys_log` VALUES (140, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 18:35:19');
INSERT INTO `sys_log` VALUES (141, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 18:53:12');
INSERT INTO `sys_log` VALUES (142, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 21:46:24');
INSERT INTO `sys_log` VALUES (143, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 21:50:02');
INSERT INTO `sys_log` VALUES (144, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 21:57:37');
INSERT INTO `sys_log` VALUES (145, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 22:41:41');
INSERT INTO `sys_log` VALUES (146, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 23:01:46');
INSERT INTO `sys_log` VALUES (147, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 23:02:17');
INSERT INTO `sys_log` VALUES (148, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 23:12:34');
INSERT INTO `sys_log` VALUES (149, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 23:16:21');
INSERT INTO `sys_log` VALUES (150, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 23:17:10');
INSERT INTO `sys_log` VALUES (151, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-08 15:25:00');
INSERT INTO `sys_log` VALUES (152, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-08 15:26:32');
INSERT INTO `sys_log` VALUES (153, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-08 15:26:56');
INSERT INTO `sys_log` VALUES (154, 'LOGIN', 1, 'Auth', 'Login', 'Incorrect password', '127.0.0.1', 1, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-08 15:57:50');
INSERT INTO `sys_log` VALUES (155, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-09 08:43:59');
INSERT INTO `sys_log` VALUES (156, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-09 08:45:06');
INSERT INTO `sys_log` VALUES (157, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-09 08:46:16');
INSERT INTO `sys_log` VALUES (158, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-09 10:33:47');
INSERT INTO `sys_log` VALUES (159, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-10 23:52:24');
INSERT INTO `sys_log` VALUES (160, 'LOGIN', 83, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr2\"}', '2025-12-10 23:55:19');
INSERT INTO `sys_log` VALUES (161, 'LOGIN', 84, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr3\"}', '2025-12-11 20:30:13');
INSERT INTO `sys_log` VALUES (162, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-11 20:30:55');
INSERT INTO `sys_log` VALUES (163, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-11 20:34:25');
INSERT INTO `sys_log` VALUES (164, 'LOGIN', 84, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr3\"}', '2025-12-11 20:36:00');
INSERT INTO `sys_log` VALUES (165, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 11:40:44');
INSERT INTO `sys_log` VALUES (166, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-12 12:12:32');
INSERT INTO `sys_log` VALUES (167, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 14:58:12');
INSERT INTO `sys_log` VALUES (168, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-12 14:59:12');
INSERT INTO `sys_log` VALUES (169, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-12 14:59:51');
INSERT INTO `sys_log` VALUES (170, 'LOGIN', 83, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr2\"}', '2025-12-12 15:13:36');
INSERT INTO `sys_log` VALUES (171, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 16:54:08');
INSERT INTO `sys_log` VALUES (172, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:02:48');
INSERT INTO `sys_log` VALUES (173, 'LOGIN', 83, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr2\"}', '2025-12-12 20:04:33');
INSERT INTO `sys_log` VALUES (174, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:06:10');
INSERT INTO `sys_log` VALUES (175, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-12 20:06:53');
INSERT INTO `sys_log` VALUES (176, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:07:31');
INSERT INTO `sys_log` VALUES (177, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-12 20:09:07');
INSERT INTO `sys_log` VALUES (178, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:10:36');
INSERT INTO `sys_log` VALUES (179, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-12 20:11:02');
INSERT INTO `sys_log` VALUES (180, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:13:24');
INSERT INTO `sys_log` VALUES (181, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:14:41');
INSERT INTO `sys_log` VALUES (182, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-12 20:17:37');
INSERT INTO `sys_log` VALUES (183, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 10:15:04');
INSERT INTO `sys_log` VALUES (184, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 10:50:29');
INSERT INTO `sys_log` VALUES (185, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-15 10:51:19');
INSERT INTO `sys_log` VALUES (186, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-15 11:00:17');
INSERT INTO `sys_log` VALUES (187, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-15 11:02:31');
INSERT INTO `sys_log` VALUES (188, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 15:31:54');
INSERT INTO `sys_log` VALUES (189, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 15:57:09');
INSERT INTO `sys_log` VALUES (190, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 15:58:07');
INSERT INTO `sys_log` VALUES (191, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 15:59:06');
INSERT INTO `sys_log` VALUES (192, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 15:59:43');
INSERT INTO `sys_log` VALUES (193, 'LOGIN', 4, 'Auth', 'Login', 'Incorrect password', '127.0.0.1', 1, 0, '{\"device\": null, \"username\": \"\"}', '2025-12-15 16:00:12');
INSERT INTO `sys_log` VALUES (194, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-15 16:05:44');
INSERT INTO `sys_log` VALUES (195, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 16:06:45');
INSERT INTO `sys_log` VALUES (196, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 16:07:34');
INSERT INTO `sys_log` VALUES (197, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 16:27:53');
INSERT INTO `sys_log` VALUES (198, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 16:28:20');
INSERT INTO `sys_log` VALUES (199, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 16:29:12');
INSERT INTO `sys_log` VALUES (200, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 16:29:47');
INSERT INTO `sys_log` VALUES (201, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 17:38:54');
INSERT INTO `sys_log` VALUES (202, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 17:39:24');
INSERT INTO `sys_log` VALUES (203, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 17:47:52');
INSERT INTO `sys_log` VALUES (204, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 19:10:49');
INSERT INTO `sys_log` VALUES (205, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 19:17:54');
INSERT INTO `sys_log` VALUES (206, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 19:18:13');
INSERT INTO `sys_log` VALUES (207, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 19:19:21');
INSERT INTO `sys_log` VALUES (208, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 19:19:58');
INSERT INTO `sys_log` VALUES (209, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-18 12:46:00');
INSERT INTO `sys_log` VALUES (210, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-18 12:46:12');
INSERT INTO `sys_log` VALUES (211, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-18 12:46:28');
INSERT INTO `sys_log` VALUES (212, 'OPERATION', 1, '商城运营', '标记异常', '3', '127.0.0.1', 0, 16, '{\"method\": \"com.example.learningcheckin.controller.AdminShopController.markAbnormal()\"}', '2025-12-04 12:52:16');
INSERT INTO `sys_log` VALUES (213, 'OPERATION', 1, '商城运营', '退款', '3', '127.0.0.1', 0, 52, '{\"method\": \"com.example.learningcheckin.controller.AdminShopController.refundOrder()\"}', '2025-12-04 12:52:27');
INSERT INTO `sys_log` VALUES (214, 'OPERATION', 1, '商城运营', '取消异常', '3', '127.0.0.1', 0, 20, '{\"method\": \"com.example.learningcheckin.controller.AdminShopController.cancelAbnormal()\"}', '2025-12-04 18:05:32');
INSERT INTO `sys_log` VALUES (215, 'OPERATION', 1, '系统工具', '保存公告', 'Announcement(id=1, title=测试, content=来了老弟, status=1, createTime=2025-12-04T23:01:34.991)', '127.0.0.1', 0, 12, '{\"method\": \"com.example.learningcheckin.controller.AdminSystemController.saveAnnouncement()\"}', '2025-12-04 23:01:35');
INSERT INTO `sys_log` VALUES (216, 'OPERATION', 1, '用户管理', '调整积分', '29', '127.0.0.1', 0, 11, '{\"method\": \"com.example.learningcheckin.controller.AdminUserController.adjustPoints()\"}', '2025-12-12 15:00:15');
INSERT INTO `sys_log` VALUES (217, 'OPERATION', 30, 'Task', 'Create', '\r\n### Error updating database.  Cause: java.sql.SQLException: Field \'deadline\' doesn\'t have a default value\r\n### The error may exist in com/example/learningcheckin/mapper/TaskMapper.java (best guess)\r\n### The error may involve com.example.learningcheckin.mapper.TaskMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO sys_task  ( teacher_id,  title, content,   reward_points, submit_type, status, is_recurring, frequency, start_date, end_date, makeup_count, makeup_cost_percent, content_template,  create_time )  VALUES  ( ?,  ?, ?,   ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ? )\r\n### Cause: java.sql.SQLException: Field \'deadline\' doesn\'t have a default value\n; Field \'deadline\' doesn\'t have a default value; nested exception is java.sql.SQLException: Field \'deadline\' doesn\'t have a default value', '127.0.0.1', 1, 30, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 15:38:10');
INSERT INTO `sys_log` VALUES (218, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50048, teacherId=30, courseId=null, title=测试, content=, attachmentUrl=null, deadline=null, rewardPoints=15, submitType=TEXT, status=1, isRecurring=true, frequency=DAILY, startDate=2025-12-15, ...', '127.0.0.1', 0, 51, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 15:40:32');
INSERT INTO `sys_log` VALUES (219, 'OPERATION', 30, 'Task', 'Grade', '1', '127.0.0.1', 0, 24, '{\"method\": \"com.example.learningcheckin.controller.TaskController.gradeSubmission()\"}', '2025-12-15 15:59:57');
INSERT INTO `sys_log` VALUES (220, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50049, teacherId=30, courseId=null, title=检查发布任务, content=, attachmentUrl=null, deadline=null, rewardPoints=15, submitType=TEXT, status=1, isRecurring=true, frequency=DAILY, startDate=2025-12-...', '127.0.0.1', 0, 18, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 16:08:29');
INSERT INTO `sys_log` VALUES (221, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50050, teacherId=30, courseId=null, title=测试, content=去打卡, attachmentUrl=null, deadline=null, rewardPoints=15, submitType=FILE, status=1, isRecurring=true, frequency=DAILY, startDate=2025-12-1...', '127.0.0.1', 0, 24, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 16:28:59');
INSERT INTO `sys_log` VALUES (222, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50051, teacherId=30, courseId=null, title=每日任务, content=测试100, attachmentUrl=, deadline=null, rewardPoints=30, submitType=TEXT, status=1, isRecurring=true, frequency=DAILY, startDate=2025-12-1...', '127.0.0.1', 0, 50, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 19:19:06');
INSERT INTO `sys_log` VALUES (223, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50052, teacherId=30, courseId=null, title=ffff, content=wfewfwefe, attachmentUrl=, deadline=2025-12-15T03:04, rewardPoints=15, submitType=TEXT, status=1, isRecurring=false, frequency=DAILY, st...', '127.0.0.1', 0, 47, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 20:33:40');
INSERT INTO `sys_log` VALUES (224, 'LOGIN', 29, 'Auth', 'Login', 'Incorrect password', '127.0.0.1', 1, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-03 23:25:52');
INSERT INTO `sys_log` VALUES (225, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-03 23:25:57');
INSERT INTO `sys_log` VALUES (226, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-03 23:26:06');
INSERT INTO `sys_log` VALUES (227, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-03 23:26:25');
INSERT INTO `sys_log` VALUES (228, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-03 23:26:51');
INSERT INTO `sys_log` VALUES (229, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 00:46:07');
INSERT INTO `sys_log` VALUES (230, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 00:46:48');
INSERT INTO `sys_log` VALUES (231, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 08:47:58');
INSERT INTO `sys_log` VALUES (232, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 08:55:11');
INSERT INTO `sys_log` VALUES (233, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 08:56:12');
INSERT INTO `sys_log` VALUES (234, 'LOGIN', 30, 'Auth', 'Login', 'Incorrect password', '127.0.0.1', 1, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 08:56:41');
INSERT INTO `sys_log` VALUES (235, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 08:56:47');
INSERT INTO `sys_log` VALUES (236, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 09:29:58');
INSERT INTO `sys_log` VALUES (237, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 09:30:25');
INSERT INTO `sys_log` VALUES (238, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 10:27:31');
INSERT INTO `sys_log` VALUES (239, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 10:27:53');
INSERT INTO `sys_log` VALUES (240, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 10:28:14');
INSERT INTO `sys_log` VALUES (241, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 10:28:40');
INSERT INTO `sys_log` VALUES (242, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 11:47:41');
INSERT INTO `sys_log` VALUES (243, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 11:56:22');
INSERT INTO `sys_log` VALUES (244, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 11:58:02');
INSERT INTO `sys_log` VALUES (245, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 12:13:06');
INSERT INTO `sys_log` VALUES (246, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 12:13:17');
INSERT INTO `sys_log` VALUES (247, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 12:49:42');
INSERT INTO `sys_log` VALUES (248, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 12:50:43');
INSERT INTO `sys_log` VALUES (249, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 18:26:16');
INSERT INTO `sys_log` VALUES (250, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 18:35:19');
INSERT INTO `sys_log` VALUES (251, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 18:53:12');
INSERT INTO `sys_log` VALUES (252, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 21:46:24');
INSERT INTO `sys_log` VALUES (253, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 21:50:02');
INSERT INTO `sys_log` VALUES (254, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 21:57:37');
INSERT INTO `sys_log` VALUES (255, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 22:41:41');
INSERT INTO `sys_log` VALUES (256, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 23:01:46');
INSERT INTO `sys_log` VALUES (257, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 23:02:17');
INSERT INTO `sys_log` VALUES (258, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-04 23:12:34');
INSERT INTO `sys_log` VALUES (259, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-04 23:16:21');
INSERT INTO `sys_log` VALUES (260, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-04 23:17:10');
INSERT INTO `sys_log` VALUES (261, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-08 15:25:00');
INSERT INTO `sys_log` VALUES (262, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-08 15:26:32');
INSERT INTO `sys_log` VALUES (263, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-08 15:26:56');
INSERT INTO `sys_log` VALUES (264, 'LOGIN', 1, 'Auth', 'Login', 'Incorrect password', '127.0.0.1', 1, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-08 15:57:50');
INSERT INTO `sys_log` VALUES (265, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-09 08:43:59');
INSERT INTO `sys_log` VALUES (266, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-09 08:45:06');
INSERT INTO `sys_log` VALUES (267, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-09 08:46:16');
INSERT INTO `sys_log` VALUES (268, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-09 10:33:47');
INSERT INTO `sys_log` VALUES (269, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-10 23:52:24');
INSERT INTO `sys_log` VALUES (270, 'LOGIN', 83, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr2\"}', '2025-12-10 23:55:19');
INSERT INTO `sys_log` VALUES (271, 'LOGIN', 84, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr3\"}', '2025-12-11 20:30:13');
INSERT INTO `sys_log` VALUES (272, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-11 20:30:55');
INSERT INTO `sys_log` VALUES (273, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-11 20:34:25');
INSERT INTO `sys_log` VALUES (274, 'LOGIN', 84, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr3\"}', '2025-12-11 20:36:00');
INSERT INTO `sys_log` VALUES (275, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 11:40:44');
INSERT INTO `sys_log` VALUES (276, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-12 12:12:32');
INSERT INTO `sys_log` VALUES (277, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 14:58:12');
INSERT INTO `sys_log` VALUES (278, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-12 14:59:12');
INSERT INTO `sys_log` VALUES (279, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-12 14:59:51');
INSERT INTO `sys_log` VALUES (280, 'LOGIN', 83, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr2\"}', '2025-12-12 15:13:36');
INSERT INTO `sys_log` VALUES (281, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 16:54:08');
INSERT INTO `sys_log` VALUES (282, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:02:48');
INSERT INTO `sys_log` VALUES (283, 'LOGIN', 83, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr2\"}', '2025-12-12 20:04:33');
INSERT INTO `sys_log` VALUES (284, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:06:10');
INSERT INTO `sys_log` VALUES (285, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-12 20:06:53');
INSERT INTO `sys_log` VALUES (286, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:07:31');
INSERT INTO `sys_log` VALUES (287, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-12 20:09:07');
INSERT INTO `sys_log` VALUES (288, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:10:36');
INSERT INTO `sys_log` VALUES (289, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-12 20:11:02');
INSERT INTO `sys_log` VALUES (290, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:13:24');
INSERT INTO `sys_log` VALUES (291, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-12 20:14:41');
INSERT INTO `sys_log` VALUES (292, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-12 20:17:37');
INSERT INTO `sys_log` VALUES (293, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 10:15:04');
INSERT INTO `sys_log` VALUES (294, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 10:50:29');
INSERT INTO `sys_log` VALUES (295, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-15 10:51:19');
INSERT INTO `sys_log` VALUES (296, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-15 11:00:17');
INSERT INTO `sys_log` VALUES (297, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-15 11:02:31');
INSERT INTO `sys_log` VALUES (298, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 15:31:54');
INSERT INTO `sys_log` VALUES (299, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 15:57:09');
INSERT INTO `sys_log` VALUES (300, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 15:58:07');
INSERT INTO `sys_log` VALUES (301, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 15:59:06');
INSERT INTO `sys_log` VALUES (302, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 15:59:43');
INSERT INTO `sys_log` VALUES (303, 'LOGIN', 4, 'Auth', 'Login', 'Incorrect password', '127.0.0.1', 1, 0, '{\"device\": null, \"username\": \"\"}', '2025-12-15 16:00:12');
INSERT INTO `sys_log` VALUES (304, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-15 16:05:44');
INSERT INTO `sys_log` VALUES (305, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 16:06:45');
INSERT INTO `sys_log` VALUES (306, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 16:07:34');
INSERT INTO `sys_log` VALUES (307, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 16:27:53');
INSERT INTO `sys_log` VALUES (308, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 16:28:20');
INSERT INTO `sys_log` VALUES (309, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 16:29:12');
INSERT INTO `sys_log` VALUES (310, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 16:29:47');
INSERT INTO `sys_log` VALUES (311, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 17:38:54');
INSERT INTO `sys_log` VALUES (312, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 17:39:24');
INSERT INTO `sys_log` VALUES (313, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 17:47:52');
INSERT INTO `sys_log` VALUES (314, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 19:10:49');
INSERT INTO `sys_log` VALUES (315, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 19:17:54');
INSERT INTO `sys_log` VALUES (316, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 19:18:13');
INSERT INTO `sys_log` VALUES (317, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-15 19:19:21');
INSERT INTO `sys_log` VALUES (318, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-15 19:19:58');
INSERT INTO `sys_log` VALUES (319, 'LOGIN', 29, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"wjr\"}', '2025-12-18 12:46:00');
INSERT INTO `sys_log` VALUES (320, 'LOGIN', 30, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"teacher\"}', '2025-12-18 12:46:12');
INSERT INTO `sys_log` VALUES (321, 'LOGIN', 1, 'Auth', 'Login', 'Login success', '127.0.0.1', 0, 0, '{\"device\": null, \"username\": \"admin\"}', '2025-12-18 12:46:28');
INSERT INTO `sys_log` VALUES (322, 'OPERATION', 1, '商城运营', '标记异常', '3', '127.0.0.1', 0, 16, '{\"method\": \"com.example.learningcheckin.controller.AdminShopController.markAbnormal()\"}', '2025-12-04 12:52:16');
INSERT INTO `sys_log` VALUES (323, 'OPERATION', 1, '商城运营', '退款', '3', '127.0.0.1', 0, 52, '{\"method\": \"com.example.learningcheckin.controller.AdminShopController.refundOrder()\"}', '2025-12-04 12:52:27');
INSERT INTO `sys_log` VALUES (324, 'OPERATION', 1, '商城运营', '取消异常', '3', '127.0.0.1', 0, 20, '{\"method\": \"com.example.learningcheckin.controller.AdminShopController.cancelAbnormal()\"}', '2025-12-04 18:05:32');
INSERT INTO `sys_log` VALUES (325, 'OPERATION', 1, '系统工具', '保存公告', 'Announcement(id=1, title=测试, content=来了老弟, status=1, createTime=2025-12-04T23:01:34.991)', '127.0.0.1', 0, 12, '{\"method\": \"com.example.learningcheckin.controller.AdminSystemController.saveAnnouncement()\"}', '2025-12-04 23:01:35');
INSERT INTO `sys_log` VALUES (326, 'OPERATION', 1, '用户管理', '调整积分', '29', '127.0.0.1', 0, 11, '{\"method\": \"com.example.learningcheckin.controller.AdminUserController.adjustPoints()\"}', '2025-12-12 15:00:15');
INSERT INTO `sys_log` VALUES (327, 'OPERATION', 30, 'Task', 'Create', '\r\n### Error updating database.  Cause: java.sql.SQLException: Field \'deadline\' doesn\'t have a default value\r\n### The error may exist in com/example/learningcheckin/mapper/TaskMapper.java (best guess)\r\n### The error may involve com.example.learningcheckin.mapper.TaskMapper.insert-Inline\r\n### The error occurred while setting parameters\r\n### SQL: INSERT INTO sys_task  ( teacher_id,  title, content,   reward_points, submit_type, status, is_recurring, frequency, start_date, end_date, makeup_count, makeup_cost_percent, content_template,  create_time )  VALUES  ( ?,  ?, ?,   ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ? )\r\n### Cause: java.sql.SQLException: Field \'deadline\' doesn\'t have a default value\n; Field \'deadline\' doesn\'t have a default value; nested exception is java.sql.SQLException: Field \'deadline\' doesn\'t have a default value', '127.0.0.1', 1, 30, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 15:38:10');
INSERT INTO `sys_log` VALUES (328, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50048, teacherId=30, courseId=null, title=测试, content=, attachmentUrl=null, deadline=null, rewardPoints=15, submitType=TEXT, status=1, isRecurring=true, frequency=DAILY, startDate=2025-12-15, ...', '127.0.0.1', 0, 51, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 15:40:32');
INSERT INTO `sys_log` VALUES (329, 'OPERATION', 30, 'Task', 'Grade', '1', '127.0.0.1', 0, 24, '{\"method\": \"com.example.learningcheckin.controller.TaskController.gradeSubmission()\"}', '2025-12-15 15:59:57');
INSERT INTO `sys_log` VALUES (330, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50049, teacherId=30, courseId=null, title=检查发布任务, content=, attachmentUrl=null, deadline=null, rewardPoints=15, submitType=TEXT, status=1, isRecurring=true, frequency=DAILY, startDate=2025-12-...', '127.0.0.1', 0, 18, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 16:08:29');
INSERT INTO `sys_log` VALUES (331, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50050, teacherId=30, courseId=null, title=测试, content=去打卡, attachmentUrl=null, deadline=null, rewardPoints=15, submitType=FILE, status=1, isRecurring=true, frequency=DAILY, startDate=2025-12-1...', '127.0.0.1', 0, 24, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 16:28:59');
INSERT INTO `sys_log` VALUES (332, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50051, teacherId=30, courseId=null, title=每日任务, content=测试100, attachmentUrl=, deadline=null, rewardPoints=30, submitType=TEXT, status=1, isRecurring=true, frequency=DAILY, startDate=2025-12-1...', '127.0.0.1', 0, 50, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 19:19:06');
INSERT INTO `sys_log` VALUES (333, 'OPERATION', 30, 'Task', 'Create', 'Task(id=50052, teacherId=30, courseId=null, title=ffff, content=wfewfwefe, attachmentUrl=, deadline=2025-12-15T03:04, rewardPoints=15, submitType=TEXT, status=1, isRecurring=false, frequency=DAILY, st...', '127.0.0.1', 0, 47, '{\"method\": \"com.example.learningcheckin.controller.TaskController.createTask()\"}', '2025-12-15 20:33:40');
INSERT INTO `sys_log` VALUES (334, 'LOGIN', 1, '认证中心', '用户登录: admin', 'Incorrect password', '127.0.0.1', 1, 0, NULL, '2025-12-24 01:04:48');
INSERT INTO `sys_log` VALUES (335, 'LOGIN', 1, '认证中心', '用户登录: admin', 'Incorrect password', '127.0.0.1', 1, 0, NULL, '2025-12-24 01:06:03');
INSERT INTO `sys_log` VALUES (336, 'LOGIN', 1, '认证中心', '用户登录: admin', 'Incorrect password', '127.0.0.1', 1, 0, NULL, '2025-12-24 01:07:24');
INSERT INTO `sys_log` VALUES (337, 'LOGIN', 1, '认证中心', '用户登录: admin', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-24 01:08:26');
INSERT INTO `sys_log` VALUES (338, 'LOGIN', 2, '认证中心', '用户登录: student', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-24 01:08:28');
INSERT INTO `sys_log` VALUES (339, 'LOGIN', 1, '认证中心', '用户登录: admin', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-24 01:10:55');
INSERT INTO `sys_log` VALUES (340, 'LOGIN', 29, '认证中心', '用户登录: wjr', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-24 01:11:37');
INSERT INTO `sys_log` VALUES (359, 'LOGIN', 2, '认证中心', '用户登录: student', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-25 14:49:41');
INSERT INTO `sys_log` VALUES (360, 'LOGIN', 29, '认证中心', '用户登录: wjr', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-25 14:51:19');
INSERT INTO `sys_log` VALUES (361, 'LOGIN', 29, '认证中心', '用户登录: wjr', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-25 15:35:58');
INSERT INTO `sys_log` VALUES (362, 'LOGIN', 3, '认证中心', '用户登录: teacher', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-25 15:36:46');
INSERT INTO `sys_log` VALUES (363, 'LOGIN', 1, '认证中心', '用户登录: admin', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-25 15:37:39');
INSERT INTO `sys_log` VALUES (364, 'LOGIN', 3, '认证中心', '用户登录: teacher', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-25 15:39:45');
INSERT INTO `sys_log` VALUES (365, 'OPERATION', 1, '用户管理', '调整积分 [com.example.learningcheckin.controller.AdminUserController.adjustPoints]', '2', '127.0.0.1', 0, 13, NULL, '2025-12-25 15:40:26');
INSERT INTO `sys_log` VALUES (366, 'OPERATION', 1, '系统工具', '删除公告 [com.example.learningcheckin.controller.AdminSystemController.deleteAnnouncement]', '4', '127.0.0.1', 0, 19, NULL, '2025-12-25 15:40:37');
INSERT INTO `sys_log` VALUES (367, 'OPERATION', 1, '系统工具', '删除公告 [com.example.learningcheckin.controller.AdminSystemController.deleteAnnouncement]', '3', '127.0.0.1', 0, 14, NULL, '2025-12-25 15:40:38');
INSERT INTO `sys_log` VALUES (368, 'OPERATION', 1, '系统工具', '删除公告 [com.example.learningcheckin.controller.AdminSystemController.deleteAnnouncement]', '2', '127.0.0.1', 0, 11, NULL, '2025-12-25 15:40:40');
INSERT INTO `sys_log` VALUES (369, 'LOGIN', 29, '认证中心', '用户登录: wjr', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-25 15:45:07');
INSERT INTO `sys_log` VALUES (370, 'OPERATION', 1, '用户管理', '调整积分 [com.example.learningcheckin.controller.AdminUserController.adjustPoints]', '29', '127.0.0.1', 0, 27, NULL, '2025-12-25 16:42:10');
INSERT INTO `sys_log` VALUES (371, 'LOGIN', 2, '认证中心', '用户登录: student', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-25 17:50:39');
INSERT INTO `sys_log` VALUES (372, 'LOGIN', 2, '认证中心', '用户登录: student', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-25 18:08:59');
INSERT INTO `sys_log` VALUES (373, 'LOGIN', 29, '认证中心', '用户登录: wjr', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-26 17:04:50');
INSERT INTO `sys_log` VALUES (374, 'LOGIN', 3, '认证中心', '用户登录: teacher', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-26 17:05:01');
INSERT INTO `sys_log` VALUES (375, 'LOGIN', 1, '认证中心', '用户登录: admin', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-26 17:05:08');
INSERT INTO `sys_log` VALUES (376, 'LOGIN', 83, '认证中心', '用户登录: wjr2', 'Login success', '127.0.0.1', 0, 0, NULL, '2025-12-26 17:05:19');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'MESSAGE, NOTIFICATION, ANNOUNCEMENT',
  `sender_id` bigint NULL DEFAULT 0 COMMENT '0 for System',
  `receiver_id` bigint NULL DEFAULT NULL COMMENT 'NULL for Announcement/All',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `is_read` tinyint(1) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver_type_read`(`receiver_id` ASC, `type` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Unified Notice Table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (7, 'REMIND_CHECKIN', 0, 2, '好友提醒', '您的好友 [lch] 提醒您今天还没打卡，快去学习吧！', 0, '2025-12-26 19:17:37');

-- ----------------------------
-- Table structure for sys_order
-- ----------------------------
DROP TABLE IF EXISTS `sys_order`;
CREATE TABLE `sys_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `price` int NOT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '0: Success, 1: Refunded',
  `is_abnormal` tinyint(1) NULL DEFAULT 0 COMMENT 'Abnormal Order',
  `shipping_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Shipping Address',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Receiver Name',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Receiver Phone',
  `tracking_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Tracking Number',
  `shipping_status` tinyint NULL DEFAULT 0 COMMENT '0: Pending, 1: Shipped, 2: Delivered',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Exchange Order' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_order
-- ----------------------------
INSERT INTO `sys_order` VALUES (1, 1, 1, 500, 0, 0, NULL, NULL, NULL, NULL, 0, '2025-12-03 09:49:28', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (2, 1, 2, 1000, 0, 0, NULL, NULL, NULL, NULL, 0, '2025-12-03 09:49:31', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (3, 29, 3, 1, 1, 0, NULL, NULL, NULL, NULL, 0, '2025-12-04 08:55:48', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (4, 29, 3, 1, 0, 0, NULL, NULL, NULL, NULL, 0, '2025-12-04 23:12:50', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (11, 29, 1, 500, 0, 0, NULL, NULL, NULL, NULL, 0, '2025-12-15 14:18:15', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (12, 29, 3, 1, 0, 0, NULL, NULL, NULL, NULL, 0, '2025-12-15 14:18:32', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (13, 29, 2, 1000, 0, 0, NULL, NULL, NULL, NULL, 0, '2025-12-15 14:19:23', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (14, 29, 1, 500, 1, 0, NULL, NULL, NULL, NULL, 0, '2025-12-15 14:27:17', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (15, 29, 22, 10, 1, 0, NULL, NULL, NULL, NULL, 0, '2025-12-15 14:27:26', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (16, 29, 22, 10, 1, 0, NULL, NULL, NULL, NULL, 0, '2025-12-15 14:27:34', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (17, 29, 1, 500, 0, 0, NULL, NULL, NULL, NULL, 0, '2025-12-15 14:28:31', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (18, 29, 53, 100, 0, 0, NULL, NULL, NULL, NULL, 0, '2025-12-15 16:06:59', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (19, 29, 2, 1000, 0, 0, NULL, NULL, NULL, NULL, 0, '2025-12-18 12:49:10', '2025-12-24 00:51:11');
INSERT INTO `sys_order` VALUES (20, 29, 57, 5000, 0, 0, NULL, NULL, NULL, NULL, 0, '2025-12-25 16:42:48', '2025-12-25 16:42:48');

-- ----------------------------
-- Table structure for sys_points_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_points_record`;
CREATE TABLE `sys_points_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '1: Gain, 2: Consume',
  `amount` int NOT NULL COMMENT 'Points Amount',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Reason',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type_time`(`type` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Points Record' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_points_record
-- ----------------------------
INSERT INTO `sys_points_record` VALUES (1, 28, 1, 10, 'Daily Check-in', '2025-12-03 00:50:08');
INSERT INTO `sys_points_record` VALUES (2, 29, 1, 10, 'Daily Check-in', '2025-12-03 00:56:25');
INSERT INTO `sys_points_record` VALUES (3, 1, 1, 10, 'Daily Check-in', '2025-12-03 09:49:14');
INSERT INTO `sys_points_record` VALUES (4, 1, 2, 500, 'Exchange Product: Gold Frame', '2025-12-03 09:49:28');
INSERT INTO `sys_points_record` VALUES (5, 1, 2, 1000, 'Exchange Product: Cool Skin', '2025-12-03 09:49:31');
INSERT INTO `sys_points_record` VALUES (6, 28, 1, 50, 'Completed Study Plan: 测试', '2025-12-03 09:51:24');
INSERT INTO `sys_points_record` VALUES (7, 29, 1, 50, 'Completed Study Plan: 测试', '2025-12-03 10:58:28');
INSERT INTO `sys_points_record` VALUES (10, 28, 1, 30, 'Ranking Reward: Daily 2nd', '2025-12-04 00:00:00');
INSERT INTO `sys_points_record` VALUES (11, 29, 1, 10, 'Ranking Reward: Daily 3rd', '2025-12-04 00:00:00');
INSERT INTO `sys_points_record` VALUES (12, 29, 1, 10, '每日打卡', '2025-12-04 00:46:11');
INSERT INTO `sys_points_record` VALUES (13, 29, 1, 50, 'Completed Study Plan: 1', '2025-12-04 00:46:14');
INSERT INTO `sys_points_record` VALUES (14, 29, 2, 1, 'Exchange Product: 添加商品测试', '2025-12-04 08:55:48');
INSERT INTO `sys_points_record` VALUES (15, 29, 1, 50, 'Completed Study Plan: 测试001', '2025-12-04 09:30:11');
INSERT INTO `sys_points_record` VALUES (16, 29, 1, 1, 'Refund: 添加商品测试', '2025-12-04 12:52:27');
INSERT INTO `sys_points_record` VALUES (17, 29, 2, 1, 'Exchange Product: 添加商品测试', '2025-12-04 23:12:50');
INSERT INTO `sys_points_record` VALUES (18, 29, 1, 10, '每日打卡', '2025-12-08 15:26:05');
INSERT INTO `sys_points_record` VALUES (19, 29, 1, 10, '每日打卡', '2025-12-09 10:43:24');
INSERT INTO `sys_points_record` VALUES (26, 29, 1, 10, '每日打卡', '2025-12-12 14:58:47');
INSERT INTO `sys_points_record` VALUES (27, 29, 1, 4999, 'Admin Adjustment: 赠予', '2025-12-12 15:00:15');
INSERT INTO `sys_points_record` VALUES (28, 83, 1, 10, '每日打卡', '2025-12-12 15:13:58');
INSERT INTO `sys_points_record` VALUES (29, 29, 1, 10, '每日打卡', '2025-12-15 10:16:18');
INSERT INTO `sys_points_record` VALUES (30, 29, 2, 500, 'Exchange Product: Gold Frame', '2025-12-15 14:18:15');
INSERT INTO `sys_points_record` VALUES (31, 29, 2, 1, 'Exchange Product: 添加商品测试', '2025-12-15 14:18:32');
INSERT INTO `sys_points_record` VALUES (32, 29, 2, 1000, 'Exchange Product: Cool Skin', '2025-12-15 14:19:23');
INSERT INTO `sys_points_record` VALUES (33, 29, 2, 500, 'Exchange Product: Gold Frame', '2025-12-15 14:27:17');
INSERT INTO `sys_points_record` VALUES (34, 29, 2, 10, 'Exchange Product: 测试', '2025-12-15 14:27:26');
INSERT INTO `sys_points_record` VALUES (35, 29, 2, 10, 'Exchange Product: 测试', '2025-12-15 14:27:34');
INSERT INTO `sys_points_record` VALUES (36, 29, 1, 10, 'Refund: 测试', '2025-12-15 14:28:15');
INSERT INTO `sys_points_record` VALUES (37, 29, 1, 10, 'Refund: 测试', '2025-12-15 14:28:19');
INSERT INTO `sys_points_record` VALUES (38, 29, 1, 500, 'Refund: Gold Frame', '2025-12-15 14:28:21');
INSERT INTO `sys_points_record` VALUES (39, 29, 2, 500, 'Exchange Product: Gold Frame', '2025-12-15 14:28:31');
INSERT INTO `sys_points_record` VALUES (40, 29, 2, 100, 'Exchange Product: 检查功能', '2025-12-15 16:06:59');
INSERT INTO `sys_points_record` VALUES (41, 29, 2, 1000, 'Exchange Product: Cool Skin', '2025-12-18 12:49:10');
INSERT INTO `sys_points_record` VALUES (42, 29, 1, 10, '每日打卡', '2025-12-24 01:11:58');
INSERT INTO `sys_points_record` VALUES (43, 2, 1, 5000, 'Admin Adjustment: 激励', '2025-12-25 15:40:26');
INSERT INTO `sys_points_record` VALUES (44, 29, 1, 100000, 'Admin Adjustment: 奖励', '2025-12-25 16:42:10');
INSERT INTO `sys_points_record` VALUES (45, 29, 2, 5000, 'Exchange Product: 璀璨星辰框', '2025-12-25 16:42:48');
INSERT INTO `sys_points_record` VALUES (46, 29, 1, 10, '每日打卡', '2025-12-25 17:49:48');

-- ----------------------------
-- Table structure for sys_product
-- ----------------------------
DROP TABLE IF EXISTS `sys_product`;
CREATE TABLE `sys_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `price` int NOT NULL COMMENT 'Price in Points',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `stock` int NULL DEFAULT 9999,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'VIRTUAL' COMMENT 'Type: AVATAR_FRAME, SKIN, etc.',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sub_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `valid_until` datetime NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1 COMMENT '0: Off-shelf, 1: On-shelf',
  `days` int NULL DEFAULT 0 COMMENT 'Valid duration in days',
  `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Product' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_product
-- ----------------------------
INSERT INTO `sys_product` VALUES (2, 'Cool Skin', 'Cool dark theme skin', 1000, 'https://placehold.co/200x200/000000/FFFFFF.png?text=Cool+Skin', 0, 'SKIN', NULL, NULL, NULL, 1, 0, NULL, '2025-12-24 00:26:24', '2025-12-26 17:26:51');
INSERT INTO `sys_product` VALUES (54, '青铜学徒框', '初入学习殿堂的证明', 100, '/frames/bronze_frame.svg', 10, 'AVATAR_FRAME', NULL, NULL, NULL, 1, 0, NULL, '2025-12-25 16:27:41', '2025-12-26 17:26:25');
INSERT INTO `sys_product` VALUES (55, '白银学者框', '坚持不懈的进阶奖励', 500, '/frames/silver_frame.svg', 1, 'AVATAR_FRAME', NULL, NULL, NULL, 1, 0, NULL, '2025-12-25 16:27:41', '2025-12-26 17:26:30');
INSERT INTO `sys_product` VALUES (56, '黄金大师框', '卓越学习者的至高荣誉', 2000, '/frames/gold_frame.svg', -1, 'AVATAR_FRAME', NULL, NULL, NULL, 1, 0, NULL, '2025-12-25 16:27:41', '2025-12-25 16:27:41');
INSERT INTO `sys_product` VALUES (57, '璀璨星辰框', '全站限量款，星光熠熠', 5000, '/frames/star_frame.svg', 50, 'AVATAR_FRAME', NULL, NULL, NULL, 1, 0, NULL, '2025-12-25 16:27:41', '2025-12-26 17:26:43');

-- ----------------------------
-- Table structure for sys_study_plan
-- ----------------------------
DROP TABLE IF EXISTS `sys_study_plan`;
CREATE TABLE `sys_study_plan`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NULL DEFAULT NULL COMMENT 'Course ID (if linked to course)',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Plan Title',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `target_hours` int NOT NULL COMMENT 'Target Study Hours',
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '0: Ongoing, 1: Completed, 2: Expired',
  `total_tasks` int NULL DEFAULT 0 COMMENT 'Total Tasks',
  `completed_tasks` int NULL DEFAULT 0 COMMENT 'Completed Tasks',
  `progress_percentage` decimal(5, 1) NULL DEFAULT 0.0 COMMENT 'Progress Percentage',
  `is_point_eligible` tinyint(1) NULL DEFAULT 1 COMMENT 'Is Point Eligible',
  `reward_points` int NULL DEFAULT 0 COMMENT 'Reward Points',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Study Plan' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_study_plan
-- ----------------------------
INSERT INTO `sys_study_plan` VALUES (1, 28, NULL, '测试', '啦啦啦啦啦啦啦啦啦', 15, '2025-12-03', '2025-12-04', 1, 0, 0, 0.0, 1, 0, '2025-12-03 09:50:57', '2025-12-03 09:51:24');
INSERT INTO `sys_study_plan` VALUES (4, 28, NULL, '学生', '111', 10, '2025-12-04', '2025-12-04', 0, 0, 0, 0.0, 1, 0, '2025-12-03 10:37:21', '2025-12-03 10:37:21');
INSERT INTO `sys_study_plan` VALUES (7, 30, 1, '测试001', 'demo', 0, '2025-12-04', '2025-12-05', 0, 1, 0, 0.0, 1, 0, '2025-12-04 09:27:37', '2025-12-04 09:27:37');

-- ----------------------------
-- Table structure for sys_study_plan_progress_history
-- ----------------------------
DROP TABLE IF EXISTS `sys_study_plan_progress_history`;
CREATE TABLE `sys_study_plan_progress_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL COMMENT 'Study Plan ID',
  `previous_progress` decimal(5, 1) NULL DEFAULT 0.0 COMMENT 'Previous Progress',
  `new_progress` decimal(5, 1) NOT NULL COMMENT 'New Progress',
  `completed_tasks` int NULL DEFAULT 0 COMMENT 'Completed Tasks at that time',
  `total_tasks` int NULL DEFAULT 0 COMMENT 'Total Tasks at that time',
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Progress Note/Hint',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Study Plan Progress History' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_study_plan_progress_history
-- ----------------------------
INSERT INTO `sys_study_plan_progress_history` VALUES (1, 6, 0.0, 20.0, 20, 100, '', '2025-12-03 11:14:33');
INSERT INTO `sys_study_plan_progress_history` VALUES (2, 6, 20.0, 12.0, 12, 100, '', '2025-12-03 11:15:07');
INSERT INTO `sys_study_plan_progress_history` VALUES (3, 6, 12.0, 66.0, 66, 100, '', '2025-12-03 23:26:41');
INSERT INTO `sys_study_plan_progress_history` VALUES (4, 7, 0.0, 0.0, 0, 1, 'Auto update from tasks', '2025-12-04 09:27:37');
INSERT INTO `sys_study_plan_progress_history` VALUES (5, 9, 0.0, 20.0, 2, 10, '', '2025-12-09 10:52:36');
INSERT INTO `sys_study_plan_progress_history` VALUES (6, 10, 0.0, 10.0, 10, 100, '', '2025-12-12 20:05:41');

-- ----------------------------
-- Table structure for sys_study_plan_task
-- ----------------------------
DROP TABLE IF EXISTS `sys_study_plan_task`;
CREATE TABLE `sys_study_plan_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `standard` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `priority` int NULL DEFAULT 0,
  `deadline` date NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 0 COMMENT '0: Pending, 1: Completed',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Study Plan Tasks' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_study_plan_task
-- ----------------------------
INSERT INTO `sys_study_plan_task` VALUES (1, 7, '打卡', NULL, NULL, 0, NULL, 0, '2025-12-04 09:27:37');
INSERT INTO `sys_study_plan_task` VALUES (2, 8, '打卡', NULL, NULL, 0, NULL, 0, '2025-12-04 09:29:49');

-- ----------------------------
-- Table structure for sys_task
-- ----------------------------
DROP TABLE IF EXISTS `sys_task`;
CREATE TABLE `sys_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `teacher_id` bigint NOT NULL COMMENT 'Teacher ID',
  `course_id` bigint NOT NULL COMMENT 'Course ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Task Title',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Task Content',
  `attachment_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Attachment URL',
  `deadline` datetime NULL DEFAULT NULL COMMENT 'Deadline',
  `reward_points` int NULL DEFAULT 0 COMMENT 'Reward Points',
  `submit_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'TEXT' COMMENT 'TEXT, IMAGE, FILE',
  `status` tinyint NULL DEFAULT 1 COMMENT '0: Draft, 1: Published',
  `is_recurring` tinyint(1) NULL DEFAULT 0 COMMENT 'Is Recurring Task',
  `frequency` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'DAILY, WEEKLY',
  `start_date` date NULL DEFAULT NULL COMMENT 'Recurring Start Date',
  `end_date` date NULL DEFAULT NULL COMMENT 'Recurring End Date',
  `makeup_count` int NULL DEFAULT 0 COMMENT 'Allowed Makeup Count',
  `makeup_cost_percent` int NULL DEFAULT 50 COMMENT 'Points deduction % for makeup',
  `content_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Submission Template',
  `reminder_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JSON Reminder Config',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50055 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Task Table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_task
-- ----------------------------
INSERT INTO `sys_task` VALUES (50051, 30, 0, '每日任务', '测试100', '', NULL, 30, 'TEXT', 1, 1, 'DAILY', '2025-12-15', '2025-12-16', 1, 100, '', NULL, '2025-12-15 19:19:06', '2025-12-24 00:55:51');
INSERT INTO `sys_task` VALUES (50052, 30, 0, 'ffff', 'wfewfwefe', '', '2025-12-15 03:04:00', 15, 'TEXT', 1, 0, 'DAILY', NULL, NULL, 0, 100, '', NULL, '2025-12-15 20:33:39', '2025-12-24 00:55:51');

-- ----------------------------
-- Table structure for sys_task_checkin
-- ----------------------------
DROP TABLE IF EXISTS `sys_task_checkin`;
CREATE TABLE `sys_task_checkin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `date` date NOT NULL,
  `checkin_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_makeup` tinyint(1) NULL DEFAULT 0,
  `points_awarded` int NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_student`(`task_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_date`(`date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Task Checkins' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_task_checkin
-- ----------------------------
INSERT INTO `sys_task_checkin` VALUES (1, 50047, 29, '2025-12-12', '2025-12-12 20:10:47', 'ehe', '', 0, 15);
INSERT INTO `sys_task_checkin` VALUES (2, 50048, 29, '2025-12-15', '2025-12-15 15:57:39', '已完成', '[\"/uploads/310122d3-1066-4e17-aba1-231df02f6a03.webp\"]', 0, 15);
INSERT INTO `sys_task_checkin` VALUES (3, 50050, 29, '2025-12-15', '2025-12-15 16:29:25', '已完成', '[]', 0, 15);
INSERT INTO `sys_task_checkin` VALUES (4, 50051, 29, '2025-12-15', '2025-12-15 19:19:44', '提交100', '[\"/uploads/fba8aec9-6f18-4ca5-9e14-5815dc0d0dbd.webp\"]', 0, 30);

-- ----------------------------
-- Table structure for sys_task_submission
-- ----------------------------
DROP TABLE IF EXISTS `sys_task_submission`;
CREATE TABLE `sys_task_submission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL COMMENT 'Task ID',
  `student_id` bigint NOT NULL COMMENT 'Student User ID',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Submission Content',
  `file_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'File URLs (JSON)',
  `status` tinyint NULL DEFAULT 0 COMMENT '0: Pending, 1: Graded, 2: Returned',
  `score` int NULL DEFAULT 0 COMMENT 'Score/Points Awarded',
  `rating` int NULL DEFAULT 0 COMMENT '1-5 Star Rating',
  `comment` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Teacher Comment',
  `similarity_score` decimal(5, 2) NULL DEFAULT 0.00 COMMENT 'Plagiarism Similarity Score',
  `submit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `grade_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_student`(`task_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Task Submissions' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_task_submission
-- ----------------------------
INSERT INTO `sys_task_submission` VALUES (1, 1, 29, '通过', '[]', 1, 1, 5, '0', 0.00, '2025-12-15 15:59:27', '2025-12-15 15:59:57');
INSERT INTO `sys_task_submission` VALUES (2, 50047, 29, NULL, NULL, 1, 0, 0, NULL, 0.00, '2025-12-12 20:10:47', NULL);
INSERT INTO `sys_task_submission` VALUES (3, 50048, 29, NULL, NULL, 2, 0, 0, '0', 0.00, '2025-12-15 15:57:39', NULL);
INSERT INTO `sys_task_submission` VALUES (4, 50050, 29, NULL, NULL, 1, 0, 0, NULL, 0.00, '2025-12-15 16:29:25', NULL);
INSERT INTO `sys_task_submission` VALUES (5, 50051, 29, NULL, NULL, 1, 0, 0, NULL, 0.00, '2025-12-15 19:19:44', NULL);
INSERT INTO `sys_task_submission` VALUES (6, 50052, 29, '', '[\"/uploads/189a3c05-b088-497b-89fc-1a940394f467.webp\"]', 0, 0, 0, NULL, 0.00, '2025-12-18 12:47:37', NULL);
INSERT INTO `sys_task_submission` VALUES (9, 50052, 2, '2222', '[]', 0, 0, 0, NULL, 0.00, '2025-12-25 14:50:15', NULL);

-- ----------------------------
-- Table structure for sys_task_submission_history
-- ----------------------------
DROP TABLE IF EXISTS `sys_task_submission_history`;
CREATE TABLE `sys_task_submission_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submission_id` bigint NOT NULL COMMENT 'Submission ID',
  `score` int NULL DEFAULT NULL COMMENT 'Score',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Comment',
  `grader_id` bigint NULL DEFAULT NULL COMMENT 'Grader User ID',
  `previous_score` int NULL DEFAULT NULL COMMENT 'Previous Score',
  `previous_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Previous Comment',
  `operate_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_submission_id`(`submission_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Task Submission History' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_task_submission_history
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Username',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Password (Encrypted)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Email',
  `avatar` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Avatar URL/Base64',
  `points` int NULL DEFAULT 0 COMMENT 'Current Points',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'USER' COMMENT 'Role: USER, TEACHER, ADMIN',
  `status` tinyint NULL DEFAULT 0 COMMENT '0: Normal, 1: Banned',
  `full_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Full Name',
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'College',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Phone Number',
  `continuous_checkin_days` int NULL DEFAULT 0 COMMENT 'Continuous Checkin Days',
  `last_checkin_date` date NULL DEFAULT NULL COMMENT 'Last Checkin Date',
  `current_avatar_frame` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Current Avatar Frame',
  `current_skin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Current Skin',
  `current_badge` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Current Badge',
  `profile_background` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Profile Background',
  `total_online_seconds` bigint NULL DEFAULT 0 COMMENT 'Total Online Seconds',
  `last_active_time` datetime NULL DEFAULT NULL COMMENT 'Last Active Time',
  `allow_friend_add` tinyint(1) NULL DEFAULT 1 COMMENT 'Allow Friend Add',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `max_streak` int NULL DEFAULT 0 COMMENT 'Max Streak',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 317 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'User Table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$J1miDNpzCx8bMuuGrd4KO.QJAJebMaNMYPFaRYbYnkXfGLRgcnvpi', NULL, NULL, 99999, 'ADMIN', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 20340, '2025-12-26 19:39:51', 1, '2025-12-24 00:26:24', '2025-12-24 01:07:54', 0);
INSERT INTO `sys_user` VALUES (2, 'student', '$2a$10$J1miDNpzCx8bMuuGrd4KO.QJAJebMaNMYPFaRYbYnkXfGLRgcnvpi', NULL, NULL, 5100, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 4140, '2025-12-25 19:02:22', 1, '2025-12-24 00:26:24', '2025-12-24 01:07:54', 0);
INSERT INTO `sys_user` VALUES (3, 'teacher', '$2a$10$8AJ7y/fLcNkdw//rSTJ1re1uXOVZN69CJX0XCXl4XYOAY./GdrAqi', NULL, NULL, 0, 'TEACHER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 20700, '2025-12-26 19:39:51', 1, '2025-12-24 00:26:24', '2025-12-26 18:56:02', 0);
INSERT INTO `sys_user` VALUES (4, '', '$2a$10$0VinpjUrohJfUnt7mcaUWuwwYxqYn77PRq.76KCeJxQk.peza8Nru', 'valid@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-03 00:42:37', '2025-12-03 00:42:37', 0);
INSERT INTO `sys_user` VALUES (5, 'concurrentuser', '$2a$10$ECfoVaZwIZuFQr125kQOC.9Eb7L8TJI2ANM2WZhIfaA9z1TJ7048u', 'concurrent@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-03 00:42:38', '2025-12-03 00:42:38', 0);
INSERT INTO `sys_user` VALUES (14, 'concurrent_162c75', '$2a$10$NWjP2Eo9TAjIQKeRHP/R3uzvmYD0FAhxLvBsO8IaWXbsnfC8D84Ba', 'concurrent_162c75@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-24 00:27:17', '2025-12-24 00:27:17', 0);
INSERT INTO `sys_user` VALUES (28, 'user1', '$2a$10$wW28PKcaM7BizkOagVZUlutCiZKnbfYRucd4n0oBzsIinG6RrtVGu', 'user1@example.com', NULL, 90, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-03 00:48:01', '2025-12-03 00:48:01', 0);
INSERT INTO `sys_user` VALUES (29, 'wjr', '$2a$10$j2.hoerzKdL69YyBxYcCvuobXD5YR02MCQLZ5T/mWWlM1nlBqWcQG', '2877054429@qq.com', 'data:image/jpeg;base64,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', 97255, 'USER', 0, 'lch', '计算机学院', '13163438915', 2, '2025-12-25', '/frames/star_frame.svg', 'https://placehold.co/200x200/000000/FFFFFF.png?text=Cool+Skin', NULL, NULL, 78117, '2025-12-26 19:40:38', 1, '2025-12-03 00:56:06', '2025-12-25 16:42:48', 2);
INSERT INTO `sys_user` VALUES (30, 'concurrent_2a1c2e', '$2a$10$sG6T0nm6lmm.2hZwBTpoz.9yoHQQnw349LFNW1oD1bvqBN995JfWW', 'concurrent_2a1c2e@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-24 00:46:24', '2025-12-24 00:46:24', 0);
INSERT INTO `sys_user` VALUES (52, 'concurrent_6543da', '$2a$10$JrIzynbkv4A5WCxyBavzuO0e0y.gRfBfsUvbIdfRPQhr4tO18a51C', 'concurrent_6543da@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-10 23:33:19', '2025-12-10 23:33:19', 0);
INSERT INTO `sys_user` VALUES (69, 'concurrent_e59263', '$2a$10$W7D6q47XdcCIniIT3kYl2.Bzh7RA7gBurXlGhh5.WAX0InweupiHe', 'concurrent_e59263@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-10 23:34:13', '2025-12-10 23:34:13', 0);
INSERT INTO `sys_user` VALUES (83, 'wjr2', '$2a$10$y3k3gIvwh0X2EBHsnFGOAuA6ypzZQTh4budcc5nzq/FL7RIXnvCLS', '2776600429@qq.com', 'data:image/jpeg;base64,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', 10, 'USER', 0, 'ddd', '计算机学院', '13166644587', 1, '2025-12-12', NULL, NULL, NULL, NULL, 12086, '2025-12-26 19:40:31', 1, '2025-12-10 23:55:11', '2025-12-10 23:55:11', 0);
INSERT INTO `sys_user` VALUES (84, 'wjr3', '$2a$10$RuR/xiDJuyp.T8yYmFpBQOMefJQ4fRnLZ9LmY2xwrgOK6fiMsP65W', '2776655443@qq.com', '', 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 7440, '2025-12-11 22:41:15', 1, '2025-12-11 20:30:08', '2025-12-11 20:30:08', 0);
INSERT INTO `sys_user` VALUES (122, 'concurrent_e071d1', '$2a$10$nDF8EpI9eTzlF2a0qloB8eQE2KuoRVL2NrczGSyFg1SIW8frcuhIm', 'concurrent_e071d1@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-11 21:52:16', '2025-12-11 21:52:16', 0);
INSERT INTO `sys_user` VALUES (149, 'concurrent_ee94e0', '$2a$10$VQ9F.lrfT/MFSN6okxQeNeHojh/mKFLfgFNCvTjhxDimGFzqPAmeK', 'concurrent_ee94e0@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-11 22:29:33', '2025-12-11 22:29:33', 0);
INSERT INTO `sys_user` VALUES (180, 'concurrent_d99c2c', '$2a$10$NzvP4LuQZAZCA4Ar5p7cbumuTE9VLy7TFrbtJUahVRB/xkriRClt2', 'concurrent_d99c2c@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-11 23:13:30', '2025-12-11 23:13:30', 0);
INSERT INTO `sys_user` VALUES (202, 'concurrent_3a73f0', '$2a$10$mtnvptstcqHKP2UrTKSiou18Yb99y9t3aBUWT00W.OFWuj8rI8zb6', 'concurrent_3a73f0@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-11 23:34:43', '2025-12-11 23:34:43', 0);
INSERT INTO `sys_user` VALUES (224, 'concurrent_b80939', '$2a$10$toJQVtBbrHuzsVWKM25d1ujqg597Uaoj6enQwRnWejb8C4yzuPBKS', 'concurrent_b80939@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-23 23:53:50', '2025-12-23 23:53:50', 0);
INSERT INTO `sys_user` VALUES (237, 'concurrent_3f7316', '$2a$10$9cH.b5QhNwN2dbc4UTXxvuViySGWLUMWM5zuMa7nNqG.HnYN4DN.K', 'concurrent_3f7316@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-23 23:54:29', '2025-12-23 23:54:29', 0);
INSERT INTO `sys_user` VALUES (277, 'concurrent_f4d103', '$2a$10$pImk7lUuKycqtkYD.SmxIuEEp6uXvh/xN4Z4U3JR0JooOWZrCtuY2', 'concurrent_f4d103@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-24 00:15:16', '2025-12-24 00:15:16', 0);
INSERT INTO `sys_user` VALUES (300, 'concurrent_6d65d9', '$2a$10$.GRve4e7o.h.loPEUWuBZONiK2UBwa4TQz.czdbOFat4Pa.Pw30Ay', 'concurrent_6d65d9@example.com', NULL, 0, 'USER', 0, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-24 00:17:34', '2025-12-24 00:17:34', 0);

SET FOREIGN_KEY_CHECKS = 1;
