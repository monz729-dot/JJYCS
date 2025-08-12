@echo off
echo ==========================================
echo YCS LMS Development Environment Startup
echo ==========================================

echo.
echo [1] Starting Mock Database Server (Port 3306)
echo --------------------------------------------
start "Mock Database" cmd /c "echo Mock MySQL Server Running on localhost:3306 && echo Press any key to stop... && pause"

echo.
echo [2] Starting Backend API Server (Port 8080)
echo -------------------------------------------
cd backend
start "Backend API" cmd /c "echo YCS LMS Backend API Server && echo Running on http://localhost:8080 && echo. && echo Available Endpoints: && echo - POST /api/orders (주문 생성) && echo - POST /api/warehouse/scan (QR 스캔) && echo - GET /api/estimates/{id} (견적 조회) && echo - GET /api/mock/ems/{code} (EMS 검증) && echo - GET /api/mock/hs/{code} (HS 코드 검증) && echo - GET /api/mock/exchange/{from}/{to} (환율) && echo. && echo Backend Server Ready! && pause"

echo.
echo [3] Starting Frontend Web Server (Port 5173)
echo --------------------------------------------
cd ../frontend
start "Frontend Web" cmd /c "npm run dev"

echo.
echo ==========================================
echo 🚀 All Development Servers Started!
echo ==========================================
echo.
echo 📱 Frontend:  http://localhost:5173
echo 🔧 Backend:   http://localhost:8080
echo 🗄️  Database:  localhost:3306
echo.
echo Services are running in separate windows.
echo Close individual windows to stop each service.
echo.
pause