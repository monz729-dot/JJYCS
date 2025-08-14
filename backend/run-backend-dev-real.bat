@echo off
chcp 65001 > nul
echo ==========================================
echo YCS LMS Backend Development Server
echo ==========================================
echo.

REM Check if Java is installed
java -version 2>nul
if errorlevel 1 (
    echo ❌ Java is not installed or not in PATH
    echo.
    echo Please install Java 17+ and try again:
    echo - Download: https://adoptium.net/
    echo - Or use: winget install EclipseAdoptium.Temurin.17.JDK
    echo.
    pause
    exit /b 1
)

echo ✅ Java found
echo.

REM Check if Maven wrapper exists
if not exist "mvnw.cmd" (
    echo ❌ Maven wrapper not found
    echo.
    echo Please run this from the backend directory
    pause
    exit /b 1
)

echo 🔧 Starting Spring Boot Application...
echo 📊 Profile: development  
echo 🌐 Port: 8080
echo 🗄️ Database: H2 In-Memory (자동 생성)
echo 🔍 H2 Console: http://localhost:8080/api/h2-console
echo 📮 Cache: Redis (localhost:6379) - 선택사항
echo 🔌 Message Queue: Kafka (localhost:9092) - 선택사항
echo.

echo Available API Endpoints:
echo -------------------------
echo POST   /api/auth/signup                - 회원가입
echo POST   /api/auth/login                 - 로그인  
echo POST   /api/orders                     - 주문 생성 (CBM/THB 검증)
echo GET    /api/orders                     - 주문 목록 조회
echo GET    /api/orders/{id}                - 주문 상세 조회
echo POST   /api/orders/{orderId}/estimates - 견적 생성
echo GET    /api/orders/{orderId}/estimates - 견적 목록 조회
echo POST   /api/warehouse/scan             - QR 스캔 처리
echo GET    /api/warehouse/inventory        - 재고 조회
echo POST   /api/warehouse/batch-process    - 일괄 처리
echo.

echo Mock API Endpoints:
echo -------------------
echo GET    /api/mock/ems/validate/{code}  - EMS 코드 검증
echo GET    /api/mock/hs/validate/{code}   - HS 코드 검증
echo GET    /api/mock/exchange/{from}/{to} - 환율 조회
echo.

echo Business Rules Active:
echo ----------------------
echo ✅ CBM > 29m³ → 항공배송 자동 전환
echo ✅ THB > 1,500 → 추가 수취인 정보 요구
echo ✅ 회원코드 누락 → 지연 처리
echo ✅ EMS/HS 코드 실시간 검증
echo.

echo Documentation:
echo ---------------
echo 📚 Swagger UI: http://localhost:8080/api/swagger-ui.html
echo 📊 Health Check: http://localhost:8080/api/actuator/health
echo 🗄️ H2 Console: http://localhost:8080/api/h2-console
echo    ├─ JDBC URL: jdbc:h2:mem:ycs_lms
echo    ├─ Username: sa
echo    └─ Password: (비어있음)
echo.

echo 🚀 Starting server...
echo.

REM Set environment variables for development
set SPRING_PROFILES_ACTIVE=dev
set JWT_SECRET=ycs-lms-development-secret-key-for-jwt-token-generation-2024
set CORS_ORIGINS=http://localhost:3000,http://localhost:5173,http://localhost:5175,http://localhost:5176,http://localhost:5177,http://localhost:5178
REM H2 Database is configured in application.yml - no external database needed!

REM Run Spring Boot application
.\mvnw.cmd spring-boot:run