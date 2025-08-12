@echo off
echo ========================================
echo YCS LMS 개발 환경 설정 스크립트
echo ========================================

REM 환경 체크
echo [1/5] 개발 환경 요구사항 체크...

where node >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js가 설치되어 있지 않습니다.
    echo https://nodejs.org에서 Node.js 18+ LTS를 설치하세요.
    pause
    exit /b 1
)

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java가 설치되어 있지 않습니다.
    echo JDK 17+를 설치하세요.
    pause
    exit /b 1
)

where docker >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️ Docker가 설치되어 있지 않습니다.
    echo Docker Desktop을 설치하면 더 나은 개발 환경을 구성할 수 있습니다.
)

echo ✅ 기본 요구사항 확인 완료

REM 백엔드 의존성 설치
echo.
echo [2/5] 백엔드 의존성 설치...
cd backend
if exist mvnw.cmd (
    echo Maven wrapper로 의존성 다운로드 중...
    call mvnw.cmd dependency:resolve
) else (
    echo Maven wrapper가 없습니다. 수동으로 Maven을 설치하세요.
)
cd ..

REM 프론트엔드 의존성 설치
echo.
echo [3/5] 프론트엔드 의존성 설치...
cd frontend
call npm install
if %errorlevel% neq 0 (
    echo ❌ 프론트엔드 의존성 설치 실패
    cd ..
    pause
    exit /b 1
)
echo ✅ 프론트엔드 의존성 설치 완료
cd ..

REM 환경 설정 파일 생성
echo.
echo [4/5] 환경 설정 파일 준비...
if not exist .env (
    echo # YCS LMS 개발 환경 설정 > .env
    echo NODE_ENV=development >> .env
    echo DB_HOST=localhost >> .env
    echo DB_PORT=3306 >> .env
    echo DB_NAME=ycs_lms >> .env
    echo DB_USER=lms_user >> .env
    echo DB_PASSWORD=lms_pass123 >> .env
    echo REDIS_HOST=localhost >> .env
    echo REDIS_PORT=6379 >> .env
    echo JWT_SECRET=dev_jwt_secret_change_in_production >> .env
    echo VITE_API_BASE_URL=http://localhost:8080/api >> .env
    echo ✅ .env 파일 생성 완료
) else (
    echo ✅ .env 파일이 이미 존재합니다
)

REM 개발 도구 스크립트 생성
echo.
echo [5/5] 개발 도구 스크립트 생성...

echo @echo off > start-frontend.bat
echo echo 프론트엔드 개발 서버 시작... >> start-frontend.bat
echo cd frontend >> start-frontend.bat
echo npm run dev >> start-frontend.bat
echo pause >> start-frontend.bat

echo @echo off > start-backend.bat
echo echo 백엔드 개발 서버 시작... >> start-backend.bat
echo cd backend >> start-backend.bat
echo if exist mvnw.cmd ( >> start-backend.bat
echo   call mvnw.cmd spring-boot:run >> start-backend.bat
echo ^) else ( >> start-backend.bat
echo   echo Maven wrapper를 찾을 수 없습니다. >> start-backend.bat
echo   echo JDK 17과 Maven을 설치한 후 다시 시도하세요. >> start-backend.bat
echo ^) >> start-backend.bat
echo pause >> start-backend.bat

echo ✅ 스크립트 생성 완료

echo.
echo ========================================
echo 🎉 개발 환경 설정 완료!
echo ========================================
echo.
echo 다음 단계:
echo 1. Docker Desktop 설치 (권장)
echo 2. start-backend.bat 실행 (백엔드 서버)
echo 3. start-frontend.bat 실행 (프론트엔드 서버)
echo.
echo 접속 URL:
echo - 프론트엔드: http://localhost:3000
echo - 백엔드 API: http://localhost:8080
echo - API 문서: http://localhost:8080/swagger-ui.html
echo.
echo 개발 가이드: docs/DEV_SETUP_PLAN.md 참고
echo ========================================
pause