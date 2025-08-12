@echo off
echo ========================================
echo YCS LMS 전체 개발 환경 시작
echo ========================================

REM Docker 환경 확인
where docker >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️ Docker가 설치되어 있지 않습니다.
    echo 수동으로 MySQL과 Redis를 설정하거나 Docker를 설치하세요.
    echo.
    echo 계속하려면 아무 키나 누르세요...
    pause > nul
) else (
    echo [1/3] Docker 인프라 시작 중...
    docker compose up -d mysql redis zookeeper kafka
    echo ✅ 인프라 서비스 시작 완료
)

echo.
echo [2/3] 백엔드 서버 시작 중...
start "YCS LMS Backend" cmd /k "cd backend && if exist mvnw.cmd (call mvnw.cmd spring-boot:run) else (echo Maven wrapper 없음. JDK 17과 Maven 설치 필요 && pause)"

REM 백엔드 시작 대기
echo 백엔드 서버 시작 대기 중...
timeout /t 5 > nul

echo.
echo [3/3] 프론트엔드 서버 시작 중...
start "YCS LMS Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo ========================================
echo 🚀 개발 환경 시작 완료!
echo ========================================
echo.
echo 서비스 URL:
echo - 프론트엔드: http://localhost:3000
echo - 백엔드 API: http://localhost:8080
echo - API 문서: http://localhost:8080/swagger-ui.html
echo - MySQL: localhost:3306 (root/root123)
echo - Redis: localhost:6379
echo.
echo 개발 서버들이 별도 창에서 실행 중입니다.
echo 종료하려면 각 창을 닫으세요.
echo ========================================
pause