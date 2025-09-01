@echo off
echo ===============================================
echo YSC 물류관리시스템 개발환경 실행
echo ===============================================
echo.

REM 개발환경 환경변수 설정
set SPRING_PROFILES_ACTIVE=dev
set SERVER_PORT=8081

echo [INFO] 개발 프로파일로 실행 중...
echo [INFO] 프로파일: %SPRING_PROFILES_ACTIVE%
echo [INFO] 포트: %SERVER_PORT%
echo [INFO] H2 콘솔: http://localhost:%SERVER_PORT%/h2-console
echo [INFO] API 서버: http://localhost:%SERVER_PORT%/api
echo.

REM 백엔드 서버 실행
echo [INFO] Spring Boot 서버 시작...
cd backend
call ./mvnw.cmd clean spring-boot:run -Dspring-boot.run.profiles=dev

echo.
echo [INFO] 서버가 종료되었습니다.
pause