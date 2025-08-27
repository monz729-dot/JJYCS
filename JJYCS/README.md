# YCS Logistics Management System (LMS)

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-1.0.0-green.svg)](package.json)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()

**YCS LMS**는 현대적인 웹 기술을 활용하여 구축된 포괄적인 물류 관리 시스템입니다. 주문 처리, 창고 관리, 파트너 협업을 위한 통합 플랫폼을 제공합니다.

## 🚀 주요 기능

### 📦 스마트 주문 관리
- **자동 CBM 계산**: 실시간 부피 계산 및 29m³ 초과 시 자동 해상→항공 전환
- **비즈니스 규칙 적용**: THB 1,500 초과 경고, 회원코드 누락 지연 처리
- **EMS/HS 코드 검증**: Korea Trade-Investment Promotion Agency 연동
- **다중 사용자 지원**: 일반/기업/파트너 역할별 주문 생성

### 🏭 지능형 창고 시스템
- **QR 코드 스캔**: PWA 카메라 지원 + 수동 입력 옵션
- **실시간 재고 추적**: 입고/출고/보류/믹스박스 상태 관리
- **위치 기반 관리**: 자동 위치 할당 및 추적
- **배치 처리**: 일괄 입출고 작업 지원

### 👥 파트너 네트워크
- **리퍼럴 추적**: 파트너 소개 고객 관리 및 커미션 정산
- **지역별 파트너**: 권역별 파트너 관리 시스템
- **성과 대시보드**: 실시간 수익 및 활동 모니터링

### ⚡ 관리자 도구
- **사용자 승인**: 기업/파트너 가입 승인 워크플로우 (평일 1-2일)
- **시스템 모니터링**: 실시간 통계 및 활동 로그
- **권한 관리**: 역할 기반 접근 제어

## 🏗️ 시스템 아키텍처

### Backend
- **Framework**: Spring Boot 3.0.3 with JDK 17
- **Database**: MySQL 8.0+ (Production), H2 (Development)
- **Security**: JWT Authentication, bcrypt Password Hashing
- **API**: RESTful with comprehensive error handling
- **ORM**: JPA/Hibernate with MyBatis integration

### Frontend
- **Framework**: Vue 3.4 with TypeScript
- **State Management**: Pinia
- **Build Tool**: Vite with HMR
- **UI**: Tailwind CSS with responsive design
- **PWA**: Offline support and camera access

### Infrastructure
- **Deployment**: Docker containers with multi-stage builds
- **Database**: MySQL with Redis caching
- **Monitoring**: Prometheus + Grafana (planned)
- **CI/CD**: GitHub Actions (planned)

## 📋 필수 조건

### 개발 환경
- **Java**: JDK 17 또는 이상
- **Node.js**: 18.0 또는 이상
- **Database**: MySQL 8.0+ (또는 H2 for development)
- **Git**: 최신 버전

### 운영 환경
- **Server**: Linux/Windows Server with 4GB+ RAM
- **Database**: MySQL 8.0+ with 10GB+ storage
- **SSL Certificate**: HTTPS 통신 (Let's Encrypt 권장)
- **Domain**: Custom domain for production deployment

## ⚡ 빠른 시작

### 1. 프로젝트 클론
```bash
git clone https://github.com/your-org/ycs-lms.git
cd ycs-lms
```

### 2. 백엔드 설정 및 실행
```bash
cd backend

# Maven wrapper를 사용하여 의존성 설치 및 빌드
./mvnw clean install

# 개발용 H2 데이터베이스로 실행
./mvnw spring-boot:run

# 또는 MySQL 사용 시
export DB_URL="mysql://username:password@localhost:3306/ycs_lms"
./mvnw spring-boot:run -Dspring.profiles.active=mysql
```

백엔드는 `http://localhost:8081`에서 실행됩니다.

### 3. 프론트엔드 설정 및 실행
```bash
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

프론트엔드는 `http://localhost:3003`에서 실행됩니다.

### 4. 초기 관리자 계정 생성
```bash
# 백엔드가 실행 중인 상태에서 실행
node create-admin.js
```

기본 관리자 계정:
- Email: `yadmin@ycs.com`
- Password: `YCSAdmin2024!`

## 🔧 환경 설정

### Backend 환경변수 (.env 또는 application.yml)
```yaml
# Database Configuration
spring:
  datasource:
    url: ${DB_URL:jdbc:h2:mem:testdb}
    username: ${DB_USERNAME:sa}
    password: ${DB_PASSWORD:password}
  
# Security
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 86400000  # 24 hours

# Email Configuration
spring:
  mail:
    host: ${SMTP_HOST:smtp.gmail.com}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USER:}
    password: ${SMTP_PASS:}

# External APIs
ems:
  api:
    key: ${EMS_API_KEY:}
    url: "https://epost.go.kr/api"

hs:
  api:
    key: ${HS_API_KEY:}
    url: "https://openapi.customs.go.kr"
```

### Frontend 환경변수 (.env)
```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8081/api

# Feature Flags
VITE_ENABLE_PWA=true
VITE_ENABLE_CAMERA=true

# External Services
VITE_GOOGLE_MAPS_KEY=your_google_maps_api_key
VITE_RECAPTCHA_SITE_KEY=your_recaptcha_site_key
```

## 📚 API 문서

### 주요 엔드포인트

#### 인증
- `POST /auth/signup` - 사용자 등록 (역할별 승인 필요)
- `POST /auth/login` - 로그인
- `POST /auth/logout` - 로그아웃

#### 주문 관리
- `POST /orders` - 주문 생성 (CBM 자동계산, 비즈니스 규칙 적용)
- `GET /orders/{id}` - 주문 상세 조회
- `GET /orders/number/{orderNumber}` - 주문번호로 조회

#### 창고 관리
- `POST /warehouse/scan` - QR 스캔 처리 (INBOUND/OUTBOUND/HOLD/MIXBOX)
- `GET /warehouse/inventory` - 재고 현황 조회
- `POST /warehouse/batch` - 일괄 처리

#### 관리자
- `GET /admin/users/pending` - 승인 대기 사용자 목록
- `POST /admin/users/{id}/approve` - 사용자 승인
- `POST /admin/users/{id}/reject` - 사용자 거부

전체 API 문서는 [API.md](docs/API.md)를 참조하세요.

## 🎯 비즈니스 규칙

### 1. CBM (Cubic Meter) 자동 계산
```
CBM = (Width × Height × Depth) ÷ 1,000,000
```
- **29m³ 초과 시**: 자동으로 해상운송 → 항공운송 전환
- **알림**: 사용자에게 자동 전환 알림 표시

### 2. THB (태국 바트) 임계값 확인
- **1,500 THB 초과 시**: 수취인 추가 정보 입력 요청
- **세관 규정**: 태국 세관 규정에 따른 자동 체크

### 3. 회원코드 검증
- **누락 시**: 배송 지연 경고 표시
- **처리**: No Member Code 플래그로 별도 처리 프로세스

### 4. 사용자 승인 프로세스
- **기업 사용자**: 사업자등록번호 확인 후 승인
- **파트너**: 지역별 파트너 자격 확인
- **처리 시간**: 평일 기준 1-2일

## 🧪 테스트

### Backend 테스트
```bash
cd backend
./mvnw test
```

### Frontend 테스트
```bash
cd frontend
npm run test:unit        # 단위 테스트
npm run test:e2e         # E2E 테스트
npm run test:coverage    # 커버리지 리포트
```

### API 테스트
```bash
# 전체 시스템 기능 테스트
node test-enhanced-features.js

# 창고 스캔 기능 테스트
node test-warehouse-scan.js

# 관리자 승인 기능 테스트
node test-admin-approval.js
```

## 🚀 배포

### Docker를 사용한 배포 (권장)

1. **Docker Compose 설정**
```yaml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8081:8081"
    environment:
      - DB_URL=jdbc:mysql://db:3306/ycs_lms
      - DB_USERNAME=ycs_user
      - DB_PASSWORD=your_password
    depends_on:
      - db
  
  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
  
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: ycs_lms
      MYSQL_USER: ycs_user
      MYSQL_PASSWORD: your_password
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

2. **빌드 및 실행**
```bash
docker-compose up -d
```

### 수동 배포

1. **Backend 배포**
```bash
cd backend
./mvnw clean package -DskipTests
java -jar target/ycs-lms-backend-1.0.0.jar --server.port=8081
```

2. **Frontend 빌드 및 배포**
```bash
cd frontend
npm run build
# dist/ 폴더를 웹 서버에 배포
```

## 🔐 보안 고려사항

### 인증 및 권한
- JWT 토큰 기반 인증 (24시간 만료)
- 역할 기반 접근 제어 (RBAC)
- 비밀번호 bcrypt 해싱 (saltRounds: 12)

### 데이터 보호
- HTTPS 강제 (Production)
- SQL Injection 방지 (Prepared Statements)
- XSS 방지 (Content Security Policy)
- 개인정보 마스킹 옵션 제공

### API 보안
- Rate Limiting (계획 중)
- Request Validation
- Error Message 정제 (민감한 정보 노출 방지)

## 📊 모니터링 및 로깅

### 로그 레벨
- **PRODUCTION**: INFO 이상
- **DEVELOPMENT**: DEBUG 이상
- **Audit Log**: 모든 중요 작업 기록

### 메트릭 수집 (계획 중)
- API 응답 시간
- 데이터베이스 쿼리 성능
- 사용자 활동 통계
- 오류 발생률

## 🤝 기여하기

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### 코딩 컨벤션
- **Backend**: Google Java Style Guide
- **Frontend**: ESLint + Prettier
- **Commit**: Conventional Commits 스타일

## 🐛 문제 해결

### 자주 발생하는 문제

**1. 포트 충돌**
```bash
# 사용 중인 포트 확인
netstat -ano | findstr :8081  # Windows
lsof -i :8081                # macOS/Linux

# 프로세스 종료
taskkill /PID <PID> /F       # Windows
kill -9 <PID>               # macOS/Linux
```

**2. 데이터베이스 연결 오류**
```bash
# MySQL 서비스 상태 확인
systemctl status mysql       # Linux
brew services list           # macOS

# 연결 테스트
mysql -u username -p -h localhost -P 3306
```

**3. 빌드 오류**
```bash
# Maven 캐시 정리
./mvnw clean

# Node.js 캐시 정리
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

## 📞 지원 및 연락처

- **이슈 제보**: [GitHub Issues](https://github.com/your-org/ycs-lms/issues)
- **문서**: [Wiki](https://github.com/your-org/ycs-lms/wiki)
- **이메일**: support@ycs-lms.com

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 있습니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

## 📋 버전 히스토리

### v1.0.0 (2024-08-24)
- ✨ 초기 MVP 릴리즈
- 🎯 CBM 자동 계산 및 해상→항공 전환
- 🏭 창고 QR 스캔 시스템
- 👥 관리자 승인 워크플로우
- 🔐 JWT 기반 인증 시스템
- 📱 PWA 모바일 지원
- 🎨 반응형 UI/UX
- 🚨 포괄적인 오류 처리 및 알림 시스템

---

**Made with ❤️ by the YCS Team**