@echo off
echo ========================================
echo  YCS LMS - H2 Database Setup Script
echo ========================================
echo.

:: 변수 설정
set H2_VERSION=2.2.224
set H2_DOWNLOAD_URL=https://github.com/h2database/h2database/releases/download/version-%H2_VERSION%/h2-2024-08-11.zip
set H2_DIR=%USERPROFILE%\h2
set H2_DATA_DIR=%H2_DIR%\data
set PROJECT_DIR=%CD%

:: 색상 설정
color 0A

echo [1/6] Checking prerequisites...
echo ----------------------------------------

:: Java 확인
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH!
    echo Please install Java 8 or higher and try again.
    pause
    exit /b 1
)
echo [OK] Java is installed

:: H2 디렉토리 생성
echo.
echo [2/6] Creating H2 directories...
echo ----------------------------------------
if not exist "%H2_DIR%" (
    mkdir "%H2_DIR%"
    echo Created: %H2_DIR%
)
if not exist "%H2_DATA_DIR%" (
    mkdir "%H2_DATA_DIR%"
    echo Created: %H2_DATA_DIR%
)

:: H2 다운로드 확인
echo.
echo [3/6] Checking H2 Database...
echo ----------------------------------------
if not exist "%H2_DIR%\bin\h2-%H2_VERSION%.jar" (
    echo H2 Database not found. Downloading...
    echo.
    echo Please download H2 manually from:
    echo %H2_DOWNLOAD_URL%
    echo.
    echo Extract to: %H2_DIR%
    echo.
    echo After download and extraction, run this script again.
    start %H2_DOWNLOAD_URL%
    pause
    exit /b 1
) else (
    echo [OK] H2 Database found at %H2_DIR%
)

:: H2 서버 시작 스크립트 생성
echo.
echo [4/6] Creating H2 server scripts...
echo ----------------------------------------

:: H2 서버 시작 스크립트
echo @echo off > "%H2_DIR%\start-h2-server.bat"
echo echo Starting H2 Database Server... >> "%H2_DIR%\start-h2-server.bat"
echo cd /d "%H2_DIR%\bin" >> "%H2_DIR%\start-h2-server.bat"
echo java -cp h2-%H2_VERSION%.jar org.h2.tools.Server -tcp -tcpAllowOthers -tcpPort 9092 -baseDir "%H2_DATA_DIR%" >> "%H2_DIR%\start-h2-server.bat"
echo pause >> "%H2_DIR%\start-h2-server.bat"
echo Created: start-h2-server.bat

:: H2 콘솔 시작 스크립트
echo @echo off > "%H2_DIR%\start-h2-console.bat"
echo echo Starting H2 Console... >> "%H2_DIR%\start-h2-console.bat"
echo cd /d "%H2_DIR%\bin" >> "%H2_DIR%\start-h2-console.bat"
echo start java -jar h2-%H2_VERSION%.jar >> "%H2_DIR%\start-h2-console.bat"
echo Created: start-h2-console.bat

:: H2 서버 중지 스크립트
echo @echo off > "%H2_DIR%\stop-h2-server.bat"
echo echo Stopping H2 Database Server... >> "%H2_DIR%\stop-h2-server.bat"
echo cd /d "%H2_DIR%\bin" >> "%H2_DIR%\stop-h2-server.bat"
echo java -cp h2-%H2_VERSION%.jar org.h2.tools.Server -tcpShutdown tcp://localhost:9092 >> "%H2_DIR%\stop-h2-server.bat"
echo echo H2 Server stopped. >> "%H2_DIR%\stop-h2-server.bat"
echo pause >> "%H2_DIR%\stop-h2-server.bat"
echo Created: stop-h2-server.bat

:: 데이터베이스 초기화 스크립트
echo.
echo [5/6] Creating database initialization script...
echo ----------------------------------------

echo @echo off > "%PROJECT_DIR%\init-h2-database.bat"
echo echo Initializing H2 Database... >> "%PROJECT_DIR%\init-h2-database.bat"
echo set H2_JAR=%H2_DIR%\bin\h2-%H2_VERSION%.jar >> "%PROJECT_DIR%\init-h2-database.bat"
echo set DB_URL=jdbc:h2:tcp://localhost:9092/~/ycs >> "%PROJECT_DIR%\init-h2-database.bat"
echo set DB_USER=sa >> "%PROJECT_DIR%\init-h2-database.bat"
echo set DB_PASS=ycs >> "%PROJECT_DIR%\init-h2-database.bat"
echo. >> "%PROJECT_DIR%\init-h2-database.bat"
echo echo Make sure H2 server is running... >> "%PROJECT_DIR%\init-h2-database.bat"
echo timeout /t 2 /nobreak ^>nul >> "%PROJECT_DIR%\init-h2-database.bat"
echo. >> "%PROJECT_DIR%\init-h2-database.bat"
echo echo Creating schema... >> "%PROJECT_DIR%\init-h2-database.bat"
echo java -cp "%%H2_JAR%%" org.h2.tools.RunScript -url "%%DB_URL%%" -user "%%DB_USER%%" -password "%%DB_PASS%%" -script "%PROJECT_DIR%\database\h2-schema.sql" -showResults >> "%PROJECT_DIR%\init-h2-database.bat"
echo if %%errorlevel%% neq 0 ( >> "%PROJECT_DIR%\init-h2-database.bat"
echo     echo [ERROR] Failed to create schema >> "%PROJECT_DIR%\init-h2-database.bat"
echo     pause >> "%PROJECT_DIR%\init-h2-database.bat"
echo     exit /b 1 >> "%PROJECT_DIR%\init-h2-database.bat"
echo ) >> "%PROJECT_DIR%\init-h2-database.bat"
echo. >> "%PROJECT_DIR%\init-h2-database.bat"
echo echo Loading initial data... >> "%PROJECT_DIR%\init-h2-database.bat"
echo java -cp "%%H2_JAR%%" org.h2.tools.RunScript -url "%%DB_URL%%" -user "%%DB_USER%%" -password "%%DB_PASS%%" -script "%PROJECT_DIR%\database\h2-init-data.sql" -showResults >> "%PROJECT_DIR%\init-h2-database.bat"
echo if %%errorlevel%% neq 0 ( >> "%PROJECT_DIR%\init-h2-database.bat"
echo     echo [ERROR] Failed to load initial data >> "%PROJECT_DIR%\init-h2-database.bat"
echo     pause >> "%PROJECT_DIR%\init-h2-database.bat"
echo     exit /b 1 >> "%PROJECT_DIR%\init-h2-database.bat"
echo ) >> "%PROJECT_DIR%\init-h2-database.bat"
echo. >> "%PROJECT_DIR%\init-h2-database.bat"
echo echo Database initialization complete! >> "%PROJECT_DIR%\init-h2-database.bat"
echo pause >> "%PROJECT_DIR%\init-h2-database.bat"
echo Created: init-h2-database.bat

:: 통합 시작 스크립트
echo.
echo [6/6] Creating all-in-one start script...
echo ----------------------------------------

echo @echo off > "%PROJECT_DIR%\start-h2-complete.bat"
echo echo ======================================== >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo  YCS LMS - H2 Database Start and Init >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo ======================================== >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo. >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo [1/3] Starting H2 Server... >> "%PROJECT_DIR%\start-h2-complete.bat"
echo start /B cmd /c "%H2_DIR%\start-h2-server.bat" >> "%PROJECT_DIR%\start-h2-complete.bat"
echo timeout /t 5 /nobreak ^>nul >> "%PROJECT_DIR%\start-h2-complete.bat"
echo. >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo [2/3] Initializing Database... >> "%PROJECT_DIR%\start-h2-complete.bat"
echo call "%PROJECT_DIR%\init-h2-database.bat" >> "%PROJECT_DIR%\start-h2-complete.bat"
echo. >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo [3/3] Starting H2 Console... >> "%PROJECT_DIR%\start-h2-complete.bat"
echo start cmd /c "%H2_DIR%\start-h2-console.bat" >> "%PROJECT_DIR%\start-h2-complete.bat"
echo. >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo ======================================== >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo  Setup Complete! >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo ======================================== >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo. >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo H2 Server: tcp://localhost:9092 >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo H2 Console: http://localhost:8082 >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo Spring H2 Console: http://localhost:8080/api/h2-console >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo. >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo Database: jdbc:h2:tcp://localhost:9092/~/ycs >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo Username: sa >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo Password: ycs >> "%PROJECT_DIR%\start-h2-complete.bat"
echo echo. >> "%PROJECT_DIR%\start-h2-complete.bat"
echo pause >> "%PROJECT_DIR%\start-h2-complete.bat"
echo Created: start-h2-complete.bat

echo.
echo ========================================
echo  Setup Complete!
echo ========================================
echo.
echo Created scripts:
echo   - %H2_DIR%\start-h2-server.bat
echo   - %H2_DIR%\start-h2-console.bat
echo   - %H2_DIR%\stop-h2-server.bat
echo   - %PROJECT_DIR%\init-h2-database.bat
echo   - %PROJECT_DIR%\start-h2-complete.bat
echo.
echo Next steps:
echo   1. Run 'start-h2-complete.bat' to start H2 and initialize DB
echo   2. Or manually:
echo      a. Run '%H2_DIR%\start-h2-server.bat'
echo      b. Run 'init-h2-database.bat'
echo      c. Run '%H2_DIR%\start-h2-console.bat'
echo.
echo H2 Connection Info:
echo   URL: jdbc:h2:tcp://localhost:9092/~/ycs
echo   User: sa
echo   Password: ycs
echo.
pause
