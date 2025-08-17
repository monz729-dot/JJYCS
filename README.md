# YCS LMS (Logistics Management System) v2.0

물류 관리 시스템 - JWT 기반 인증, 역할별 접근 제어, 실시간 창고 관리

## 🚀 빠른 시작

### 사전 요구사항
- Java 17+
- Node.js 18+
- Maven 3.9+

### 개발 환경 실행
```bash
# 모든 서비스 시작
./start-dev.sh

# 백엔드만 시작
./start-dev.sh backend

# 프론트엔드만 시작  
./start-dev.sh frontend

# 서비스 종료
./start-dev.sh stop
```

### Windows
```cmd
REM 모든 서비스 시작
start-dev.bat

REM 서비스 종료
start-dev.bat stop
```

## 📱 접속 주소
- **프론트엔드**: http://localhost:5173
- **백엔드 API**: http://localhost:8080
- **API 문서**: http://localhost:8080/swagger-ui.html

## 👥 테스트 계정
| 역할 | 이메일 | 비밀번호 | 설명 |
|------|--------|----------|------|
| 관리자 | admin@ycs.com | password123 | 전체 시스템 관리 |
| 창고 | warehouse@ycs.com | password123 | 창고 관리, QR 스캔 |
| 개인 | user1@example.com | password123 | 개인 사용자 |
| 기업 | company@corp.com | password123 | 기업 사용자 |
| 파트너 | partner@affiliate.com | password123 | 파트너 |

## 🛠️ 개발 가이드
### 백엔드 (Spring Boot)
```bash
cd backend
./mvnw spring-boot:run
```

### 프론트엔드 (Vue.js + Vite)
```bash
cd frontend
npm install
npm run dev
```

## 📝 주요 기능
- ✅ JWT 기반 인증/인가
- ✅ 역할별 접근 제어 (관리자/창고/개인/기업/파트너)
- ✅ 주문 관리 (생성/수정/조회/취소)
- ✅ 창고 관리 (QR 스캔, 재고 관리)
- ✅ 견적 관리 (생성/승인/거절)
- ✅ 결제 관리 (결제 처리/취소/환불)
- ✅ 배송 추적 (실시간 상태 업데이트)
- ✅ 알림 시스템
- ✅ 파일 업로드 (라벨, 사진)
- ✅ PDF 생성 (견적서, 라벨)
- ✅ QR 코드 생성/스캔

## 🏗️ 시스템 아키텍처

### 백엔드 (Spring Boot)
```
├── controller/          # REST API 컨트롤러
│   ├── AuthController      # 인증 관련 API
│   ├── UserController      # 사용자 프로필 API
│   ├── OrdersController    # 주문 관리 API
│   ├── WarehouseController # 창고 관리 API
│   ├── PaymentsController  # 결제 관리 API
│   ├── TrackingController  # 배송 추적 API
│   ├── EstimatesController # 견적 관리 API
│   └── AdminController     # 관리자 API
├── service/             # 비즈니스 로직
├── security/            # JWT 보안 설정
├── dto/                 # 데이터 전송 객체
└── util/                # 유틸리티 클래스
```

### 프론트엔드 (Vue.js)
```
├── modules/             # 기능별 모듈
│   ├── auth/               # 인증 모듈
│   ├── orders/             # 주문 관리 모듈
│   ├── warehouse/          # 창고 관리 모듈
│   ├── payments/           # 결제 관리 모듈
│   ├── tracking/           # 배송 추적 모듈
│   ├── estimates/          # 견적 관리 모듈
│   ├── admin/              # 관리자 모듈
│   ├── partner/            # 파트너 모듈
│   └── profile/            # 프로필 모듈
├── services/            # API 서비스
├── stores/              # Pinia 상태 관리
├── components/          # 재사용 컴포넌트
└── layouts/             # 레이아웃 컴포넌트
```

## 🔧 API 엔드포인트

### 인증 API
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인  
- `POST /api/auth/refresh` - 토큰 새로고침
- `POST /api/auth/verify-email` - 이메일 인증
- `POST /api/auth/forgot-password` - 비밀번호 찾기
- `POST /api/auth/reset-password` - 비밀번호 재설정

### 사용자 API
- `GET /api/users/me` - 현재 사용자 프로필 조회
- `PUT /api/users/me` - 프로필 업데이트
- `POST /api/users/me/change-password` - 비밀번호 변경

### 주문 API
- `GET /api/orders` - 주문 목록 조회
- `POST /api/orders` - 주문 생성
- `GET /api/orders/{id}` - 주문 상세 조회
- `PUT /api/orders/{id}` - 주문 수정
- `POST /api/orders/{id}/cancel` - 주문 취소

### 창고 API
- `POST /api/warehouse/scan` - QR 스캔 처리
- `POST /api/warehouse/batch-process` - 일괄 처리
- `GET /api/warehouse/{id}/inventory` - 재고 조회
- `POST /api/warehouse/generate-label` - 라벨 생성

### 견적 API
- `GET /api/estimates` - 견적 목록 조회
- `POST /api/estimates` - 견적 생성
- `GET /api/estimates/{id}` - 견적 상세 조회
- `POST /api/estimates/{id}/respond` - 견적 응답

### 결제 API
- `GET /api/payments` - 결제 목록 조회
- `POST /api/payments/process` - 결제 처리
- `GET /api/payments/{id}` - 결제 상세 조회

### 배송 추적 API
- `GET /api/tracking` - 배송 추적 목록
- `GET /api/tracking/{trackingNumber}` - 배송 상세 조회
- `POST /api/tracking/register` - 운송장 번호 등록

## 🔐 보안 기능

### JWT 토큰 인증
- 액세스 토큰 (24시간 유효)
- 리프레시 토큰 (7일 유효)
- 자동 토큰 갱신

### 역할별 접근 제어
- **ADMIN**: 모든 기능 접근
- **WAREHOUSE**: 창고 관리, 견적 생성
- **ENTERPRISE/INDIVIDUAL**: 주문 관리, 결제
- **PARTNER**: 파트너 센터 기능

### 보안 미들웨어
- CORS 설정
- 요청 검증
- 에러 핸들링

## 🎯 비즈니스 룰

### CBM 계산
- 29m³ 초과시 자동으로 해상 → 항공 전환
- 실시간 CBM 계산 및 경고

### 금액 제한
- THB 1,500 초과시 추가 수취인 정보 필요
- 자동 비즈니스 룰 검증

### 회원 관리
- member_code 검증
- 기업/파트너 계정 승인 프로세스

## 🚀 배포

### 개발 환경
```bash
./start-dev.sh
```

### 프로덕션 빌드
```bash
# 백엔드
cd backend
./mvnw clean package -Pproduction

# 프론트엔드  
cd frontend
npm run build
```

## 📞 지원

문제가 발생하거나 질문이 있으시면:
1. GitHub Issues에 등록
2. 개발팀에 직접 문의

---

**YCS LMS v2.0** - 완전한 물류 관리 솔루션 🚛