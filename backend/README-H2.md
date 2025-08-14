# YCS LMS Backend - H2 Database Version

## 🎯 개요

YCS 물류관리 시스템의 백엔드가 **H2 인메모리 데이터베이스**로 구성되었습니다. 
별도의 데이터베이스 설치 없이 바로 실행할 수 있습니다!

## 🚀 빠른 시작

### 1. Java 17+ 설치 확인
```bash
java -version
```

### 2. 백엔드 서버 실행
```bash
cd C:\YCS VER.2\backend
.\run-backend-dev-real.bat
```

### 3. 실행 확인
- **API 서버**: http://localhost:8080/api
- **H2 Console**: http://localhost:8080/api/h2-console
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **Health Check**: http://localhost:8080/api/actuator/health

## 🗄️ 데이터베이스 접근

### H2 Console 접속 정보
- **URL**: http://localhost:8080/api/h2-console
- **JDBC URL**: `jdbc:h2:mem:ycs_lms`
- **Username**: `sa`
- **Password**: (비어있음)

### 초기 데이터
시스템 시작시 자동으로 테스트 데이터가 생성됩니다:

**테스트 사용자:**
- `admin@ycs.com` (관리자)
- `warehouse@ycs.com` (창고)
- `user1@test.com` (일반 사용자)
- `corp@company.com` (기업 사용자)
- `partner@partner.com` (파트너)

**비밀번호:** 모든 사용자 `password123`

## 🔧 주요 기능 테스트

### 1. 비즈니스 룰 테스트 데이터

**CBM > 29m³ 규칙:**
- 주문 `YCS-2024-00006` - 총 CBM 약 30.5m³로 항공배송 전환

**THB 1,500 초과 규칙:**
- 주문 `YCS-2024-00006` - 롤렉스 시계 1,800 THB

**회원코드 없음 규칙:**
- 사용자 `nocode@test.com` - 주문 `YCS-2024-00007`

### 2. API 엔드포인트 테스트

**주문 관리:**
```bash
# 주문 목록 조회
GET http://localhost:8080/api/orders

# 주문 생성
POST http://localhost:8080/api/orders

# 견적 생성
POST http://localhost:8080/api/orders/1/estimates
```

**창고 관리:**
```bash
# 재고 조회
GET http://localhost:8080/api/warehouse/inventory

# QR 스캔
POST http://localhost:8080/api/warehouse/scan
```

## 📊 데이터베이스 스키마

### 주요 테이블
- `users` - 사용자 정보
- `orders` - 주문 정보
- `order_items` - 주문 상품
- `order_boxes` - 주문 박스 (CBM 자동계산)

### 비즈니스 룰 컬럼
- `orders.requires_extra_recipient` - THB 1,500 초과 플래그
- `orders.total_cbm_m3` - 자동 계산된 총 CBM
- `orders.order_type` - 'SEA'/'AIR' (CBM 29 초과시 자동 변경)
- `users.member_code` - NULL이면 지연 처리

## 🎛️ 설정

### application.yml 주요 설정
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:ycs_lms
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

## 🔍 디버깅

### 로그 확인
애플리케이션 실행 시 다음을 확인하세요:
- Hibernate SQL 로그 (show-sql: true)
- 비즈니스 룰 검증 로그
- API 요청/응답 로그

### 일반적인 문제 해결
1. **포트 충돌**: 8080 포트 사용중이면 다른 포트로 변경
2. **Java 버전**: JDK 17+ 필요
3. **메모리 부족**: JVM 힙 메모리 증가 (`-Xmx1024m`)

## 📈 성능 최적화

H2 인메모리 DB는 매우 빠르지만 다음 사항을 고려하세요:
- 애플리케이션 재시작시 데이터 초기화
- 대용량 데이터 처리시 메모리 사용량 증가
- 프로덕션 환경에서는 MySQL/PostgreSQL 권장

## 🔄 다음 단계

1. **프론트엔드 연동**: Vue.js 앱과 API 연결
2. **실제 외부 API 연동**: EMS, HS 코드 검증
3. **파일 업로드**: 리패킹 사진 업로드 기능
4. **실시간 알림**: WebSocket 또는 Server-Sent Events
5. **프로덕션 DB**: MySQL/PostgreSQL 마이그레이션

---

## 💡 팁

- H2 Console에서 SQL 쿼리 직접 실행 가능
- `data-h2.sql` 파일 수정해서 초기 데이터 변경
- Swagger UI에서 API 바로 테스트 가능
- 개발 중에는 `spring.jpa.show-sql=true`로 SQL 로그 확인