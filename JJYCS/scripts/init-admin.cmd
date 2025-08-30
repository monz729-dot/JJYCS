@echo off
setlocal enabledelayedexpansion

:: YSC 물류관리시스템 관리자 계정 초기화 스크립트 (Windows)
:: Usage: init-admin.cmd [environment]

set ENVIRONMENT=%1
if "%ENVIRONMENT%"=="" set ENVIRONMENT=dev

set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR:~0,-9%

echo === YSC LMS 관리자 계정 초기화 ===
echo 환경: %ENVIRONMENT%
echo 프로젝트 루트: %PROJECT_ROOT%
echo.

:: 환경별 설정
if "%ENVIRONMENT%"=="dev" goto :set_dev
if "%ENVIRONMENT%"=="development" goto :set_dev
if "%ENVIRONMENT%"=="prod" goto :set_prod
if "%ENVIRONMENT%"=="production" goto :set_prod

echo ❌ 지원하지 않는 환경입니다: %ENVIRONMENT%
echo 사용 가능한 환경: dev, development, prod, production
exit /b 1

:set_dev
if "%ADMIN_EMAIL%"=="" set ADMIN_EMAIL=admin@ysc.com
if "%ADMIN_PASSWORD%"=="" set ADMIN_PASSWORD=password
if "%ADMIN_NAME%"=="" set ADMIN_NAME=Administrator
if "%ADMIN_PHONE%"=="" set ADMIN_PHONE=010-0000-0000
if "%ADMIN_MEMBER_CODE%"=="" set ADMIN_MEMBER_CODE=ADM001
goto :config_done

:set_prod
if "%ADMIN_EMAIL%"=="" set ADMIN_EMAIL=admin@ysc.com
if "%ADMIN_PASSWORD%"=="" set ADMIN_PASSWORD=YSC-Admin-2024!
if "%ADMIN_NAME%"=="" set ADMIN_NAME=System Administrator
if "%ADMIN_PHONE%"=="" set ADMIN_PHONE=010-0000-0000
if "%ADMIN_MEMBER_CODE%"=="" set ADMIN_MEMBER_CODE=ADM001
goto :config_done

:config_done
echo 관리자 계정 설정:
echo   - 이메일: %ADMIN_EMAIL%
echo   - 이름: %ADMIN_NAME%
echo   - 전화번호: %ADMIN_PHONE%
echo   - 멤버코드: %ADMIN_MEMBER_CODE%
echo.

:: 비밀번호 보안 경고
if "%ENVIRONMENT%"=="prod" (
  if "%ADMIN_PASSWORD%"=="YSC-Admin-2024!" (
    echo ⚠️  경고: 프로덕션 환경에서 기본 비밀번호를 사용하고 있습니다!
    echo    ADMIN_PASSWORD 환경변수를 설정하여 보안이 강화된 비밀번호를 사용하세요.
    echo.
    set /p CONFIRM="계속하시겠습니까? (y/N): "
    if not "!CONFIRM!"=="y" if not "!CONFIRM!"=="Y" (
      echo 초기화를 취소했습니다.
      exit /b 1
    )
  )
)

:: 백엔드 서버 실행 확인
echo 백엔드 서버 연결 확인...
set BACKEND_URL=http://localhost:8081/api

curl -s -f "%BACKEND_URL%/actuator/health" >nul 2>&1
if errorlevel 1 (
  echo ❌ 백엔드 서버가 실행되고 있지 않습니다.
  echo    다음 명령어로 백엔드를 실행하세요:
  echo    cd backend ^&^& ./mvnw.cmd spring-boot:run
  exit /b 1
)

echo ✅ 백엔드 서버가 실행 중입니다.

:: 기존 관리자 계정 확인
echo.
echo 기존 관리자 계정 확인 중...

:: JSON 데이터를 임시 파일로 생성
echo {"email":"%ADMIN_EMAIL%","password":"%ADMIN_PASSWORD%"} > temp_login.json

curl -s -X POST "%BACKEND_URL%/auth/login" -H "Content-Type: application/json" -d @temp_login.json > login_response.json 2>nul

findstr /C:"\"success\":true" login_response.json >nul
if not errorlevel 1 (
  echo ✅ 관리자 계정이 이미 존재하고 로그인 가능합니다.
  
  :: 사용자 정보 표시
  findstr /C:"\"user\":" login_response.json
  echo 계정 정보를 확인했습니다.
  
) else (
  echo ⚠️  관리자 계정이 존재하지 않거나 로그인할 수 없습니다.
  
  :: 회원가입 시도
  echo 새로운 관리자 계정을 생성합니다...
  
  :: 회원가입 JSON 데이터 생성
  echo {"email":"%ADMIN_EMAIL%","password":"%ADMIN_PASSWORD%","name":"%ADMIN_NAME%","phone":"%ADMIN_PHONE%","userType":"ADMIN"} > temp_signup.json
  
  curl -s -X POST "%BACKEND_URL%/auth/signup" -H "Content-Type: application/json" -d @temp_signup.json > signup_response.json 2>nul
  
  findstr /C:"\"success\":true" signup_response.json >nul
  if not errorlevel 1 (
    echo ✅ 새로운 관리자 계정이 생성되었습니다.
    
    :: 다시 로그인 시도
    echo 생성된 계정으로 로그인을 시도합니다...
    curl -s -X POST "%BACKEND_URL%/auth/login" -H "Content-Type: application/json" -d @temp_login.json > login_response2.json 2>nul
    
    findstr /C:"\"success\":true" login_response2.json >nul
    if not errorlevel 1 (
      echo ✅ 관리자 계정 로그인 성공!
    ) else (
      echo ❌ 관리자 계정은 생성되었지만 로그인에 실패했습니다.
      echo    이메일 인증이 필요할 수 있습니다.
    )
    
    del login_response2.json >nul 2>&1
    
  ) else (
    echo ❌ 관리자 계정 생성에 실패했습니다.
    echo 응답:
    type signup_response.json
    del temp_login.json temp_signup.json login_response.json signup_response.json >nul 2>&1
    exit /b 1
  )
  
  del temp_signup.json signup_response.json >nul 2>&1
)

:: 임시 파일 정리
del temp_login.json login_response.json >nul 2>&1

echo.
echo === 관리자 계정 초기화 완료 ===
echo 로그인 정보:
echo   - URL: http://localhost:3001/login
echo   - 이메일: %ADMIN_EMAIL%
echo   - 비밀번호: %ADMIN_PASSWORD%
echo.

if "%ENVIRONMENT%"=="prod" (
  echo 🔒 프로덕션 환경 보안 체크리스트:
  echo   [ ] 관리자 비밀번호를 강력한 비밀번호로 변경
  echo   [ ] 2단계 인증^(2FA^) 활성화
  echo   [ ] 관리자 계정의 IP 접근 제한 설정
  echo   [ ] 정기적인 비밀번호 변경 정책 적용
  echo.
)

echo 관리자 대시보드: http://localhost:3001/admin
echo H2 데이터베이스 콘솔: http://localhost:8081/api/h2-console

endlocal