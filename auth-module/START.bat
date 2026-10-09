@echo off
echo ========================================
echo Starting Auth Module - Gym Management
echo ========================================
echo.

cd backend

echo [1/3] Building application...
call mvn clean install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo [2/3] Build successful!
echo.
echo [3/3] Starting application on port 8081...
echo.

call mvn spring-boot:run

pause
