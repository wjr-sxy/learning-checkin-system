# 数据库变更回滚计划 (Database Change Rollback Plan)

## 1. 变更概述 (Change Overview)
本次变更涉及对 `learning_checkin` 数据库进行全面优化，包括表结构调整、索引优化、字段类型规范化以及默认数据填充。

## 2. 备份验证 (Backup Verification)
在执行任何变更前，必须确认已成功执行以下备份：
- **备份文件**: `docs/sql/backups/learning_checkin_full_backup_20251223.sql` (示例)
- **验证方式**: 
  - 检查备份文件大小是否正常。
  - 在临时数据库环境执行 `mysql -u root -p temp_db < backup_file.sql` 验证恢复成功。

## 3. 回滚步骤 (Rollback Steps)

### 场景 A：变更过程中发生严重错误 (Scenario A: Error during deployment)
1. **立即停止变更**: 停止当前正在执行的 SQL 脚本。
2. **清理环境**: 
   ```sql
   DROP DATABASE IF EXISTS `learning_checkin`;
   CREATE DATABASE `learning_checkin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```
3. **恢复备份**:
   ```bash
   mysql -u root -p learning_checkin < d:/al-chat/备份/01/learning-checkin-system/docs/sql/backups/full_backup_20251223_*.sql
   ```
4. **验证恢复**: 启动后端服务，检查基本功能是否恢复正常。

### 场景 B：变更后系统运行异常 (Scenario B: System anomaly after deployment)
1. **停止应用服务**: 停止 Spring Boot 后端。
2. **执行备份恢复**: 重复场景 A 中的步骤 2 和 3。
3. **排查日志**: 检查应用日志 `logs/` 和 MySQL 错误日志，定位异常原因。

## 4. 验证清单 (Verification Checklist)
回滚完成后，需确认以下事项：
- [ ] 核心表（`sys_user`, `sys_course`, `sys_checkin`）数据完整。
- [ ] 用户登录功能正常。
- [ ] 签到及积分系统逻辑正常。
- [ ] 教师端课程管理功能正常。

## 5. 紧急联系人 (Emergency Contact)
- 数据库管理员: [Name]
- 后端开发负责人: [Name]
