@echo off
title YSC LMS - 전체 시스템 시작
color 0A

echo ==========================================
echo        YSC 물류관리 시스템 (LMS)
echo        Full System Startup Script
echo ==========================================
echo.

echo [INFO] Docker 환경 확인 중...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker가 설치되지 않았거나 실행되지 않습니다.
    echo 먼저 Docker Desktop을 설치하고 실행해주세요.
    pause
    exit /b 1
)

echo [INFO] Docker Compose 실행 중 (MySQL, Redis, Kafka)...
docker-compose up -d --build

if %errorlevel% neq 0 (
    echo [ERROR] Docker Compose 실행에 실패했습니다.
    pause
    exit /b 1
)

echo [INFO] 데이터베이스 초기화 대기 중... (30초)
timeout /t 30 /nobreak

echo.
echo [INFO] 백엔드 서버 시작 중...
cd backend
start "YSC LMS Backend" cmd /k "mvn spring-boot:run -Dspring-boot.run.profiles=dev"
cd ..

echo [INFO] 백엔드 초기화 대기 중... (20초)
timeout /t 20 /nobreak

echo.
echo [INFO] 프론트엔드 개발 서버 시작 중...
cd frontend
start "YSC LMS Frontend" cmd /k "npm run dev"
cd ..

echo.
echo ==========================================
echo           시스템 시작 완료!
echo ==========================================
echo.
echo 🌐 프론트엔드: http://localhost:5173
echo 🚀 백엔드 API: http://localhost:8080/api
echo 📚 API 문서: http://localhost:8080/api/swagger-ui.html
echo 📊 관리자 패널: http://localhost:5173/admin
echo.
echo 🔑 기본 관리자 계정:
echo    이메일: admin@ysc-lms.com  
echo    비밀번호: admin123!
echo.
echo [INFO] 모든 서비스가 시작되었습니다.
echo 브라우저에서 http://localhost:5173 으로 접속하세요.
echo.
pause

echo [INFO] 시스템 종료 중...
echo Docker 컨테이너 중지 중...
docker-compose down

echo [INFO] 모든 서비스가 종료되었습니다.
pause