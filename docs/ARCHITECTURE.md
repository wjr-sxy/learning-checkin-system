# 系统架构说明文档 - 多角色自动跳转系统

## 1. 概述
本系统实现了基于角色的访问控制 (RBAC) 和自动跳转机制。系统根据用户的角色（STUDENT, TEACHER, ADMIN）提供差异化的用户界面和功能访问权限，确保数据安全和良好的用户体验。

## 2. 技术栈
- **前端**: Vue 3, Vue Router 4, Pinia (状态管理), TypeScript
- **后端**: Spring Boot 2.7, Spring Security (密码加密), JWT (令牌认证)
- **数据库**: MySQL 8.0

## 3. 架构设计

### 3.1 用户角色模型
系统定义了三种核心角色，存储于 `sys_user` 表的 `role` 字段中：
- `USER` (学生): 普通学习者，访问学习计划、打卡、商城。
- `TEACHER` (教师): 教学管理者，访问教师管理后台。
- `ADMIN` (管理员): 系统维护者，访问系统管理控制台。

### 3.2 认证与授权流程

1.  **登录认证**:
    - 用户提交用户名/密码至 `/auth/login`。
    - 后端验证通过后，返回 JWT Token 和包含 `role` 的用户信息。
    - 前端 `UserStore` 存储 Token 和 User 对象。

2.  **自动跳转 (Login Redirect)**:
    - `Login.vue` 根据响应中的 `role` 字段进行路由跳转：
        - `ADMIN` -> `/admin-dashboard`
        - `TEACHER` -> `/teacher-dashboard`
        - 其他 -> `/student-dashboard`

3.  **路由守卫 (Router Guard)**:
    - 全局前置守卫 `router.beforeEach` 拦截所有路由导航。
    - **鉴权**: 检查 `to.meta.requiresAuth`，若未登录则重定向至 `/login`。
    - **角色检查**: 检查 `to.meta.role`。若当前用户角色与目标路由所需角色不匹配，触发“无权访问”拦截，并重定向回用户对应的 Dashboard。
    - **已登录重定向**: 若已登录用户访问 `/login` 或 `/`，自动跳转至其对应的 Dashboard，防止重复登录。

### 3.3 目录结构 (优化后)

```
project-root/
├── backend/              # 后端源码 (Spring Boot)
├── frontend/             # 前端源码 (Vue 3 + Vite)
│   ├── src/
│   │   ├── api/          # 接口请求定义
│   │   ├── components/   # 公共组件
│   │   ├── styles/       # 全局样式 (模块化)
│   │   ├── stores/       # Pinia 状态管理
│   │   ├── views/
│   │   │   ├── student/  # 学生端页面
│   │   │   ├── teacher/  # 教师端页面
│   │   │   │   └── components/ # 教师端模块化组件
│   │   │   └── admin/    # 管理员端页面
│   │   └── router/       # 路由配置
├── docs/                 # 项目文档
│   ├── sql/              # 数据库脚本
│   └── ARCHITECTURE.md   # 系统架构说明
└── README.md             # 项目启动说明
```

### 3.4 模块化设计

1. **逻辑拆分 (教师端)**:
   教师端 `TeacherDashboard.vue` 采用了组件化拆分模式，将核心功能拆分为以下子组件，以确保单个文件代码量保持在合理范围内（<1000行）：
   - `CourseCenter.vue`: 课程创建与管理
   - `ClassManagement.vue`: 学生名单与状态管理
   - `TeachingPlan.vue`: 教学计划下发
   - `MonitoringCenter.vue`: 数据统计与监控
   - `ReminderCenter.vue`: 催学提醒系统
   - `TeacherLeaderboard.vue`: 学生积分排行
   - `DataExport.vue`: 数据导出功能

2. **样式拆分 (Global Styles)**:
   原本庞大的 `main.css` 被拆分为四个核心样式文件，提升了样式的可读性和层级清晰度：
   - `variables.css`: 颜色、尺寸等全局变量
   - `base.css`: 重置样式与基础布局
   - `components.css`: 通用 UI 组件样式
   - `utilities.css`: 功能性辅助类 (Flex, Margin, Padding 等)

### 3.5 数据库设计 (变更)

- **表**: `sys_user`
- **字段**: `role` (VARCHAR) - 存储角色标识。
- **初始化**: `RoleDataInitializer.java` 会在系统启动时检查并自动创建默认的 `teacher` 账号（如果不存在）。

## 4. 安全性考量
- **前端拦截**: 通过路由守卫防止用户通过 URL 直接访问未授权页面。
- **后端验证**: (建议) 后端 API 也应添加相应的角色校验注解 (如 `@PreAuthorize`)，以防止绕过前端直接调用接口。
- **Token 安全**: JWT 包含用户信息，并在每次请求头中携带，确保无状态认证的安全性。

## 5. 扩展性
- 新增角色只需在数据库添加对应枚举，在前端 `router/index.ts` 配置新路由和 Meta 信息，并在 `Login.vue` 添加跳转分支即可。
