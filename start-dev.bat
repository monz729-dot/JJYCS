@echo off
echo ===============================================
echo        YCS LMS Development Server Startup
echo ===============================================
echo.

echo [1/4] Starting MySQL (if available)...
net start mysql > nul 2>&1
if %errorlevel% == 0 (
    echo MySQL service started successfully
) else (
    echo MySQL service not available or already running
)

echo.
echo [2/4] Building Backend...
cd backend
call mvn clean compile > nul 2>&1
if %errorlevel% == 0 (
    echo Backend compiled successfully
) else (
    echo Backend compilation may have issues - continuing...
)

echo.
echo [3/4] Building Frontend...
cd ..\frontend
call npm install > nul 2>&1
call npm run build > nul 2>&1
if %errorlevel% == 0 (
    echo Frontend built successfully
) else (
    echo Frontend build may have issues - continuing...
)

echo.
echo [4/4] Starting Development Servers...
echo.
echo ===========================================
echo          YCS LMS 개발용 계정 정보
echo ===========================================
echo.
echo 1. 일반 사용자 (Individual):
echo    Email: user@ycs.com ^| Password: password123
echo    Email: user2@ycs.com ^| Password: password123
echo.
echo 2. 기업 사용자 (Enterprise):
echo    Email: enterprise@ycs.com ^| Password: password123 [승인완료]
echo    Email: company2@ycs.com ^| Password: password123 [승인대기]
echo.
echo 3. 파트너 사용자 (Partner):
echo    Email: partner@ycs.com ^| Password: password123 [승인완료]
echo    Email: affiliate@ycs.com ^| Password: password123 [승인대기]
echo.
echo 4. 창고 관리자 (Warehouse - 중간관리자):
echo    Email: warehouse@ycs.com ^| Password: password123
echo    Email: warehouse2@ycs.com ^| Password: password123
echo.
echo 5. 시스템 관리자 (Admin - 최고관리자):
echo    Email: admin@ycs.com ^| Password: password123
echo    Email: superadmin@ycs.com ^| Password: password123
echo.
echo ===========================================
echo.
echo Backend will run on: http://localhost:8080
echo Frontend will run on: http://localhost:5173
echo.
echo Press Ctrl+C to stop servers
echo.

start "YCS LMS Backend" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"
timeout /t 5 > nul
start "YCS LMS Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

echo Development servers are starting...
echo Check the opened terminal windows for detailed logs.
pause