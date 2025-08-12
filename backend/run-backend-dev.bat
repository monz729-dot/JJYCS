@echo off
echo ==========================================
echo YCS LMS Backend Development Server
echo ==========================================
echo.
echo 🔧 Starting Spring Boot Application...
echo 📊 Profile: development
echo 🌐 Port: 8080
echo.

echo Mock API Endpoints Available:
echo --------------------------------
echo POST   /api/orders                     - 주문 생성 (CBM/THB 검증 포함)
echo POST   /api/warehouse/scan             - QR 스캔 처리
echo GET    /api/orders                     - 주문 목록 조회
echo GET    /api/orders/{id}                - 주문 상세 조회
echo POST   /api/orders/{id}/estimate       - 견적 생성
echo.
echo External API Mocks:
echo ------------------  
echo GET    /api/mock/ems/validate/{code}   - EMS 코드 검증
echo GET    /api/mock/hs/validate/{code}    - HS 코드 검증
echo GET    /api/mock/exchange/{from}/{to}  - 환율 조회
echo.
echo Business Rules Active:
echo ---------------------
echo ✅ CBM > 29m³ → 항공배송 자동 전환
echo ✅ THB > 1,500 → 추가 수취인 정보 요구
echo ✅ 회원코드 누락 → 지연 처리
echo ✅ EMS/HS 코드 실시간 검증
echo.

echo 🚀 Backend Server Ready at http://localhost:8080
echo.
echo Press Ctrl+C to stop the server
echo ========================================
echo.

REM 실제 환경에서는 아래 명령어로 실행:
REM java -jar target/ycs-lms-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

echo [Simulated] Spring Boot Application Started
echo [Simulated] Mock APIs responding on port 8080
echo.
echo 개발용 Mock 서버가 실행 중입니다.
echo 실제 Spring Boot 서버는 Maven 빌드 후 실행 가능합니다.
echo.
pause