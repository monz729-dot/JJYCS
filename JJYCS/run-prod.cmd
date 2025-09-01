@echo off
echo ===============================================
echo YSC 물류관리시스템 운영환경 실행
echo ===============================================
echo.

REM 운영환경 환경변수 체크
if "%JWT_SECRET_KEY%"=="" (
    echo [ERROR] JWT_SECRET_KEY 환경변수가 설정되지 않았습니다.
    echo [ERROR] 운영환경에서는 JWT_SECRET_KEY가 필수입니다.
    echo [ERROR] .env 파일을 확인하거나 환경변수를 설정하세요.
    echo.
    pause
    exit /b 1
)

if "%DB_PASSWORD%"=="" (
    echo [WARNING] DB_PASSWORD 환경변수가 설정되지 않았습니다.
    echo [WARNING] MySQL 사용 시 DB_PASSWORD가 필요합니다.
    echo.
)

REM 운영환경 설정
set SPRING_PROFILES_ACTIVE=prod
set SERVER_PORT=8080

echo [INFO] 운영 프로파일로 실행 중...
echo [INFO] 프로파일: %SPRING_PROFILES_ACTIVE%
echo [INFO] 포트: %SERVER_PORT%
echo [INFO] API 서버: http://localhost:%SERVER_PORT%/api
echo [WARNING] H2 콘솔은 운영환경에서 비활성화됩니다.
echo.

REM 애플리케이션 빌드
echo [INFO] 애플리케이션 빌드 중...
cd backend
call ./mvnw.cmd clean package -DskipTests

if errorlevel 1 (
    echo [ERROR] 빌드 실패!
    pause
    exit /b 1
)

echo [INFO] 빌드 완료.
echo.

REM 운영환경 서버 실행
echo [INFO] Spring Boot 서버 시작 (운영모드)...
java -jar target\ycs-lms-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

echo.
echo [INFO] 서버가 종료되었습니다.
pause