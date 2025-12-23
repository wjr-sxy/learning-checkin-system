@echo off
echo Starting Learning Check-in System...

:: 1. Check and Start Redis
tasklist /FI "IMAGENAME eq redis-server.exe" 2>NUL | find /I /N "redis-server.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo Redis is already running.
) else (
    echo Starting Redis...
    :: Assuming redis-server is in PATH. If not, user needs to set path or install it.
    start "Redis Server" redis-server
)

:: 2. Start Backend
echo Starting Backend...
cd backend
start "Backend" mvn spring-boot:run
cd ..

:: 3. Start Frontend
echo Starting Frontend...
cd frontend
start "Frontend" npm run dev
cd ..

echo All services initiated. Please check the new windows for status.
pause
