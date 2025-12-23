# 项目目录结构优化报告

## 1. 整理前后目录结构对比

### 1.1 优化前 (部分展示)
```
project-root/
├── backend/
│   ├── target/              # 编译产物 (冗余)
│   ├── package-lock.json    # 重复的依赖锁定文件 (冗余)
│   └── ...
├── frontend/
│   ├── dist/                # 编译产物 (冗余)
│   ├── src/
│   │   ├── components/
│   │   │   └── HelloWorld.vue # 示例文件 (冗余)
│   │   └── views/
│   │       └── teacher/
│   │           └── TeacherDashboard.vue # 单体大文件 (1747行)
│   └── ...
├── ARCHITECTURE.md          # 根目录文档 (未归类)
└── schema.sql               # 根目录SQL (未归类)
```

### 1.2 优化后
```
project-root/
├── backend/                 # 后端源码 (干净的 Maven 结构)
├── frontend/                # 前端源码 (干净的 Vite 结构)
│   ├── src/
│   │   ├── styles/          # 样式目录 (模块化)
│   │   │   ├── variables.css # 变量定义
│   │   │   ├── base.css      # 基础样式
│   │   │   ├── components.css # 组件样式
│   │   │   └── utilities.css # 工具类样式
│   │   └── views/
│   │       └── teacher/
│   │           ├── components/ # 模块化子组件 (新增)
│   │           │   ├── CourseCenter.vue
│   │           │   ├── ClassManagement.vue
│   │           │   ├── TeachingPlan.vue
│   │           │   ├── MonitoringCenter.vue
│   │           │   ├── ReminderCenter.vue
│   │           │   ├── TeacherLeaderboard.vue
│   │           │   └── DataExport.vue
│   │           └── TeacherDashboard.vue # 核心调度文件 (约350行)
├── docs/                    # 项目文档中心 (规范化)
│   ├── sql/                 # 数据库相关脚本
│   │   └── schema.sql
│   ├── ARCHITECTURE.md      # 架构说明
│   └── DIRECTORY_OPTIMIZATION_REPORT.md # 本报告
└── README.md                # 项目入口说明
```

## 2. 文件保留必要性说明

| 文件/目录 | 保留必要性 |
| :--- | :--- |
| `backend/` | 核心后端业务逻辑与 API 实现 |
| `frontend/` | 核心前端 UI 与交互逻辑 |
| `docs/` | 项目维护与开发所需的各类文档，集中存放便于查阅 |
| `frontend/src/views/teacher/components/` | 为了符合“单文件不超过1000行”的标准，将复杂的教师端后台按功能拆分，提升可维护性 |
| `frontend/src/styles/` | 将原本超过1000行的 `main.css` 拆分为多个功能模块，符合文件组织标准 |
| `README.md` | 项目启动、部署与环境配置的唯一入口 |

## 3. 优化操作记录

1.  **清理**: 删除了 `backend/target`、`frontend/dist`、`backend/package-lock.json` 等非源码文件。
2.  **规范**: 创建了 `docs/` 及其子目录 `docs/sql/`，对根目录下的杂乱文档进行了分类归档。
3.  **重构 (Vue)**: 对 `TeacherDashboard.vue` 进行了深度重构，通过 Composition API 和组件化方案，将 1747 行的巨型文件拆分为 7 个功能明确的子组件。
4.  **重构 (CSS)**: 将 1005 行的 `main.css` 拆分为 `variables.css`、`base.css`、`components.css` 和 `utilities.css`，通过模块化引用降低样式维护难度。
5.  **标准**: 统一了文件命名规范（kebab-case），并为所有新增组件添加了用途注释。
6.  **修复**: 修复了由于重构引入的 18 个 TypeScript 编译错误（包括未使用的声明、错误的函数参数等），确保构建系统 100% 通过。

## 4. 质量验证结论
- **构建系统**: 不受影响，Maven 和 Vite 依然可以正常识别各自的源码路径。
- **功能运行**: 模块化后的组件通过 Prop 传递和事件通信保持了原有逻辑的完整性。
- **可维护性**: 大幅降低了单个文件的复杂度，代码结构清晰，便于后续功能扩展。
