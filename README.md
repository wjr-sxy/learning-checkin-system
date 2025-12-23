# Learning Check-in System (学习打卡系统)

## Project Structure
- `backend`: Spring Boot 2.7 + MyBatis Plus + MySQL + Redis
- `frontend`: Vue 3 + TypeScript + Element Plus + Vite

## Prerequisites
- Java 8+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0
- Redis

## Setup & Run

### 1. Database Setup
1. Create a MySQL database named `learning_checkin`.
2. Run the SQL script located at `backend/sql/schema.sql` to initialize tables and data.
3. Update database credentials in `backend/src/main/resources/application.yml` if necessary.

### 2. Backend
```bash
cd backend
mvn clean spring-boot:run
```
Backend will start at `http://localhost:8080`.

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend will start at `http://localhost:5173` (or similar).

## Features Implemented (Skeleton)
- **User Authentication**: Login, Register (Backend API + Frontend View).
- **Database Schema**: Tables for Users, Check-ins, Study Plans, Points, Products, Orders.
- **Project Structure**: Ready for feature implementation.
