# 好友功能模块数据库设计文档

## 1. 概述
本模块涉及两张主要数据表：`sys_friendship`（好友关系表）和 `sys_friend_request`（好友请求表），并在 `sys_user` 表中增加了隐私设置字段。

## 2. 数据表设计

### 2.1 好友关系表 (sys_friendship)
用于存储用户之间的双向好友关系。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | NO | AUTO_INCREMENT | 主键ID |
| user_id | BIGINT | 20 | NO | | 用户ID |
| friend_id | BIGINT | 20 | NO | | 好友用户ID |
| create_time | DATETIME | | YES | CURRENT_TIMESTAMP | 建立时间 |

**索引:**
- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_user_friend (user_id, friend_id)`: 确保关系唯一

### 2.2 好友请求表 (sys_friend_request)
用于存储好友请求记录及其状态。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| id | BIGINT | 20 | NO | AUTO_INCREMENT | 主键ID |
| sender_id | BIGINT | 20 | NO | | 发送者ID |
| receiver_id | BIGINT | 20 | NO | | 接收者ID |
| status | INT | 11 | YES | 0 | 状态 (0:待处理, 1:已接受, 2:已拒绝) |
| create_time | DATETIME | | YES | CURRENT_TIMESTAMP | 创建时间 |

**索引:**
- `PRIMARY KEY (id)`

### 2.3 用户表变更 (sys_user)
增加隐私设置字段。

| 字段名 | 类型 | 长度 | 是否为空 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| allow_friend_add | TINYINT | 1 | YES | 1 | 是否允许被添加好友 (1:允许, 0:禁止) |

## 3. ER图关系
- `sys_user` (1) <---> (N) `sys_friendship`
- `sys_user` (1) <---> (N) `sys_friend_request` (Sender)
- `sys_user` (1) <---> (N) `sys_friend_request` (Receiver)
