@echo off
title YCS LMS Development Environment

echo 🚀 YCS LMS 개발 환경 시작
echo =============================

REM 현재 디렉토리 확인
if not exist "backend\pom.xml" if not exist "frontend\package.json" (
    echo ❌ 프로젝트 루트 디렉토리에서 실행해주세요
    pause
    exit /b 1
)

REM 명령어 파라미터 확인
set COMMAND=%1
if "%COMMAND%"=="" set COMMAND=start

if "%COMMAND%"=="start" goto START_ALL
if "%COMMAND%"=="backend" goto START_BACKEND
if "%COMMAND%"=="frontend" goto START_FRONTEND
if "%COMMAND%"=="stop" goto STOP_ALL
if "%COMMAND%"=="help" goto SHOW_HELP

echo ❌ 알 수 없는 명령어: %COMMAND%
goto SHOW_HELP

:START_ALL
echo 📦 백엔드 서버 시작 중...
cd backend
start "YCS Backend" cmd /k "mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local"
cd ..

timeout /t 10 /nobreak >nul

echo 🎨 프론트엔드 서버 시작 중...
cd frontend
if not exist "node_modules" (
    echo 📦 npm 패키지 설치 중...
    npm install
)
start "YCS Frontend" cmd /k "npm run dev"
cd ..

goto SHOW_INFO

:START_BACKEND
echo 📦 백엔드만 시작 중...
cd backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
cd ..
goto END

:START_FRONTEND
echo 🎨 프론트엔드만 시작 중...
cd frontend
if not exist "node_modules" npm install
npm run dev
cd ..
goto END

:STOP_ALL
echo 🛑 서비스 종료 중...
taskkill /f /im "java.exe" 2>nul
taskkill /f /im "node.exe" 2>nul
echo ✅ 모든 서비스가 종료되었습니다
goto END

:SHOW_INFO
timeout /t 5 /nobreak >nul
echo.
echo 🎉 YCS LMS 개발 환경이 시작되었습니다!
echo =====================================
echo 📱 프론트엔드: http://localhost:5173
echo 🔧 백엔드 API: http://localhost:8080
echo 📚 API 문서: http://localhost:8080/swagger-ui.html
echo.
echo 📋 테스트 계정:
echo    관리자: admin@ycs.com / password123
echo    창고: warehouse@ycs.com / password123
echo    개인: user1@example.com / password123
echo    기업: company@corp.com / password123
echo.
echo ⏹️  종료하려면 stop-dev.bat를 실행하세요
goto END

:SHOW_HELP
echo YCS LMS 개발 환경 스크립트
echo.
echo 사용법:
echo   start-dev.bat [명령어]
echo.
echo 명령어:
echo   start     - 백엔드와 프론트엔드 모두 시작 (기본값)
echo   backend   - 백엔드만 시작
echo   frontend  - 프론트엔드만 시작  
echo   stop      - 모든 서비스 종료
echo   help      - 도움말 표시
echo.
echo 예시:
echo   start-dev.bat
echo   start-dev.bat backend
echo   start-dev.bat stop

:END
pause