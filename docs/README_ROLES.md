# 多角色系统更新说明

本更新实现了基于角色的自动跳转和权限控制系统。

## 1. 更新内容
- **前端**:
  - 新增 `StudentDashboard.vue`, `TeacherDashboard.vue`, `AdminDashboard.vue`。
  - 更新 `router/index.ts`，添加角色路由和全局权限守卫。
  - 更新 `Login.vue`，实现登录后根据角色自动跳转。
- **后端**:
  - 新增 `RoleDataInitializer.java`，系统启动时自动创建测试用教师账号。

## 2. 如何验证
请参考项目根目录下的 `TEST_CASES.md` 文档进行详细测试。

### 快速验证步骤:
1. **启动后端**: 运行 Spring Boot 应用。
   - 控制台应显示 `Checking role data...` 和 `Created test teacher user.` (如果是首次运行)。
2. **启动前端**: 运行 `npm run dev`。
3. **测试教师角色**:
   - 登录账号: `teacher`
   - 密码: `123456` (默认加密密码对应的值，假设 RoleDataInitializer 使用的是该密码的哈希)
   - **预期**: 自动跳转至 "教师管理后台"。
4. **测试学生角色**:
   - 使用现有学生账号或注册新账号。
   - **预期**: 自动跳转至 "学生门户"。
5. **测试权限隔离**:
   - 登录学生账号后，手动修改 URL 为 `/teacher-dashboard`。
   - **预期**: 提示无权访问，并自动跳回学生门户。

## 3. 文档
- `TEST_CASES.md`: 详细测试用例。
- `ARCHITECTURE.md`: 系统架构设计说明。
