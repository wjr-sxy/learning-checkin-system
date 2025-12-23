# 好友功能模块 API 文档

## 1. 概述
本文档描述了学习打卡系统中好友功能模块的 RESTful API 接口。所有接口的基础路径为 `/api/friend`。

## 2. 接口列表

### 2.1 发送好友请求
- **URL**: `/api/friend/request`
- **Method**: `POST`
- **Content-Type**: `application/json`
- **Description**: 向指定用户发送好友请求。
- **Request Body**:
    ```json
    {
        "userId": 1,          // 当前用户ID
        "targetUserId": 2     // 目标用户ID
    }
    ```
- **Response**:
    - Success (200):
    ```json
    {
        "code": 200,
        "message": "Request sent",
        "data": null
    }
    ```
    - Error (500):
    ```json
    {
        "code": 500,
        "message": "Already friends / Request already sent / User not found",
        "data": null
    }
    ```

### 2.2 处理好友请求
- **URL**: `/api/friend/handle`
- **Method**: `POST`
- **Content-Type**: `application/json`
- **Description**: 接受或拒绝好友请求。
- **Request Body**:
    ```json
    {
        "userId": 1,      // 当前用户ID
        "requestId": 10,  // 请求ID
        "status": 1       // 1: 接受, 2: 拒绝
    }
    ```
- **Response**:
    - Success (200):
    ```json
    {
        "code": 200,
        "message": "Handled",
        "data": null
    }
    ```

### 2.3 删除好友
- **URL**: `/api/friend/delete`
- **Method**: `DELETE`
- **Content-Type**: `application/json`
- **Description**: 删除指定好友。
- **Request Body**:
    ```json
    {
        "userId": 1,      // 当前用户ID
        "friendId": 2     // 好友用户ID
    }
    ```
- **Response**:
    - Success (200):
    ```json
    {
        "code": 200,
        "message": "Deleted",
        "data": null
    }
    ```

### 2.4 获取好友列表
- **URL**: `/api/friend/list`
- **Method**: `GET`
- **Description**: 获取当前用户的好友列表。
- **Parameters**:
    - `userId`: 当前用户ID
- **Response**:
    ```json
    {
        "code": 200,
        "message": "Success",
        "data": [
            {
                "id": 2,
                "username": "user2",
                "fullName": "张三",
                "avatar": "http://...",
                "lastActiveTime": "2023-10-27T10:00:00"
            }
        ]
    }
    ```

### 2.5 获取好友请求列表
- **URL**: `/api/friend/requests`
- **Method**: `GET`
- **Description**: 获取当前用户收到的未处理好友请求。
- **Parameters**:
    - `userId`: 当前用户ID
- **Response**:
    ```json
    {
        "code": 200,
        "message": "Success",
        "data": [
            {
                "requestId": 10,
                "senderId": 3,
                "username": "user3",
                "fullName": "李四",
                "avatar": "http://...",
                "createTime": "2023-10-27T09:30:00"
            }
        ]
    }
    ```

### 2.6 搜索用户
- **URL**: `/api/friend/search`
- **Method**: `GET`
- **Description**: 根据用户名或姓名搜索用户（用于添加好友）。
- **Parameters**:
    - `keyword`: 搜索关键字
    - `currentUserId`: 当前用户ID（用于排除自己）
- **Response**:
    ```json
    {
        "code": 200,
        "message": "Success",
        "data": [
            {
                "id": 4,
                "username": "user4",
                "fullName": "王五",
                "avatar": "http://..."
            }
        ]
    }
    ```

## 3. 错误码说明
- `200`: 成功
- `500`: 业务逻辑错误（如重复添加、用户不存在等）
- `400`: 参数错误
- `401`: 未认证
