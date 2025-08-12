# YSC 물류관리시스템 (LMS) 

> 모바일 웹 중심의 물류 관리 시스템 - **CBM 자동계산, EMS/HS 검증, THB 1,500 초과 경고, QR 스캔** 등 핵심 비즈니스 룰 완전 구현

## 🚀 빠른 시작

### 1. 시스템 요구사항
- **Java 17** 이상
- **Node.js 18** 이상  
- **Docker Desktop** (MySQL, Redis, Kafka 실행용)
- **Git**

### 2. 전체 시스템 실행
```bash
# Windows
start-full-system.bat

# 또는 수동 실행
docker-compose up -d
cd backend && mvn spring-boot:run &
cd frontend && npm run dev
```

### 3. 접속 정보
- 🌐 **프론트엔드**: http://localhost:5173
- 🚀 **백엔드 API**: http://localhost:8080/api  
- 📚 **API 문서**: http://localhost:8080/api/swagger-ui.html
- 📊 **어드민 패널**: http://localhost:5173/admin

### 4. 기본 계정
```
관리자: admin@ysc-lms.com / admin123!
개발용 계정들은 /docs/DEV_USERS.sql 참조
```

---

## 📋 주요 기능

### ✅ 비즈니스 룰 (완전 구현)
- **CBM 29 초과 시 자동 항공 전환** + 경고 알림
- **THB 1,500 초과 시 수취인 추가 정보 입력 유도**
- **회원코드 미기재 시 주문 지연 처리**
- **EMS/HS 코드 실시간 검증** (data.go.kr API)

### 📱 사용자 인터페이스
- **모바일 우선 PWA** 설계
- **다국어 지원** (한국어/영어)
- **실시간 상태 업데이트**
- **직관적 대시보드**

### 📦 주문 관리
- **4단계 주문 생성** (수취인 → 상품 → 박스 → 배송)
- **실시간 CBM 계산** 및 배송 방법 추천
- **주문 상태 추적** (12단계 상세 상태)
- **견적서 자동 생성** (1차/2차 버전 관리)

### 🏭 창고 관리  
- **QR/바코드 스캔** (카메라 + 수동 입력)
- **일괄 입출고 처리** (배치 스캔)
- **실시간 재고 현황**
- **믹스박스/보류 상태 관리**

### 👥 사용자 관리
- **5가지 역할** (개인/기업/파트너/창고/관리자)
- **기업/파트너 승인 프로세스** (평일 1-2일)
- **2단계 인증** (2FA)
- **이메일 인증** 필수

### 🤝 파트너 시스템
- **추천 코드 시스템**
- **커미션 자동 계산**
- **실시간 성과 대시보드**
- **정산 관리**

---

## 🏗️ 기술 스택

### Backend (Spring Boot 3)
- **Java 17**, **Spring Boot 3.0.3**
- **MyBatis 3.0.3** (MySQL 연동)
- **Redis** (세션/캐시), **Kafka** (이벤트)
- **JWT**, **bcrypt**, **2FA**
- **Flyway** (DB 마이그레이션)
- **Swagger/OpenAPI** (API 문서)

### Frontend (Vue 3)
- **Vue 3.4** + **TypeScript**
- **Pinia** (상태관리), **Vue Router 4**
- **Vite** + **PWA**
- **TailwindCSS** + **PrimeVue**
- **Vue-i18n** (다국어)
- **html5-qrcode** (QR 스캔)

### Infrastructure
- **Docker Compose** (개발환경)
- **MySQL 8.0**, **Redis 7**, **Kafka 3.2**
- **Nginx** (프로덕션 배포)
- **AWS/Azure** 배포 준비

---

## 📊 데이터베이스 스키마

### 핵심 테이블
```sql
users (사용자), enterprise_profiles (기업), partner_profiles (파트너)
orders (주문), order_items (상품), order_boxes (박스/CBM)
warehouses (창고), inventory (재고), scan_events (스캔로그)
shipment_tracking (배송추적), estimates (견적), payments (결제)
partner_referrals (추천), config (시스템설정), audit_logs (감사로그)
```

### 가상 컬럼 & 트리거
- **CBM 자동계산**: `(width×height×depth)/1,000,000`
- **29 초과 감지**: 트리거로 자동 `air` 전환
- **THB 1,500 체크**: 주문 생성시 플래그 설정
- **회원코드 검증**: NULL 시 `delayed` 상태

---

## 🎯 MVP 스프린트 계획

### Sprint 1 (백엔드 코어 + 회원/주문)
- ✅ DB 마이그레이션 (Flyway)
- ✅ 인증/인가 (JWT, 2FA, 이메일)
- ✅ 주문 생성 API (CBM/EMS/HS/THB 룰)
- ✅ 창고 스캔 API (입고 기초)
- ✅ 사용자/관리자 대시보드

### Sprint 2 (출고/라벨/정산/파트너)
- ✅ 라벨/QR 생성·스캔·출력
- ✅ 일괄 입출고, 보류/믹스박스
- ✅ 견적(1차/2차) + 운임 합계
- ✅ 파트너 대시보드/정산 기초

**Done 정의**: 위 플로우가 **모바일 실제 기기**에서 E2E 동작, 오류 메시지 현지화 포함

---

## 🔧 개발 명령어

### 백엔드
```bash
cd backend
mvn spring-boot:run                    # 개발 서버 실행
mvn test                              # 단위 테스트
mvn spring-boot:run -Dspring.profiles.active=prod  # 프로덕션 모드
```

### 프론트엔드
```bash
cd frontend  
npm run dev                           # 개발 서버 실행
npm run build                         # 프로덕션 빌드
npm run preview                       # 빌드 미리보기
npm run lint                          # ESLint 검사
```

### 전체 시스템
```bash
docker-compose up -d                  # 인프라 시작 (MySQL, Redis, Kafka)
docker-compose down                   # 인프라 종료
docker-compose logs -f                # 로그 모니터링
```

---

## 📈 성능 최적화

### 백엔드
- **HikariCP** 커넥션 풀 (최대 10개)
- **Redis 캐시** (세션, 자주 조회되는 데이터)
- **MyBatis** 페이징 (100개 단위)
- **인덱스 최적화** (주문/박스/CBM/파트너 성과)

### 프론트엔드
- **Vite** 빠른 번들링
- **코드 스플리팅** (모듈별 지연 로딩)
- **PWA 캐싱** (오프라인 지원)
- **이미지 최적화** (WebP, 압축)

---

## 🔐 보안

- **OAuth2** + **JWT** 토큰 (1시간 만료)
- **bcrypt** 비밀번호 해싱
- **2FA** (TOTP, Google Authenticator)
- **ReCAPTCHA v3** (봇 방지)
- **HTTPS** 강제 (프로덕션)
- **PII 마스킹** (개인정보 보호)
- **감사 로그** 전체 기록

---

## 🌍 국제화

### 지원 언어
- **한국어** (기본)
- **영어** (전체 번역 완료)
- **태국어** (비즈니스 룰 관련)

### 다국가 지원
- **통화**: THB, KRW, USD, CNY, VND
- **날짜/시간**: 현지 표준
- **주소 형식**: 국가별 대응
- **전화번호**: 국가 코드 자동 감지

---

## 🧪 테스트

### 테스트 전략
```bash
# 백엔드 테스트
mvn test                              # 단위 테스트
mvn integration-test                  # 통합 테스트

# 프론트엔드 테스트  
npm run test:unit                     # 유닛 테스트
npm run test:e2e                      # E2E 테스트 (Cypress)
```

### 테스트 범위
- **비즈니스 룰**: CBM/THB/회원코드/EMS-HS 검증
- **API 계약**: 요청/응답 스키마 검증
- **UI 플로우**: 주문생성→입고→출고→추적 시나리오
- **모바일**: BrowserStack 실제 디바이스

**목표 커버리지**: **80%+**

---

## 📝 문서

### API 문서
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **전체 API 스펙**: `/docs/API.md`
- **에러 코드**: `/docs/ERROR_CODES.md`

### 개발자 가이드  
- **코딩 컨벤션**: Google Java Style + ESLint/Prettier
- **커밋 규칙**: `feat(module): description` 형태
- **브랜치 전략**: Git Flow
- **배포 프로세스**: GitHub Actions

---

## 🚨 트러블슈팅

### 일반적인 문제

**Q: Docker 컨테이너가 시작되지 않음**
```bash
# Windows에서 Docker Desktop 실행 확인
docker --version
docker-compose down
docker system prune -f  # 캐시 정리
docker-compose up -d
```

**Q: 백엔드 8080 포트 충돌**
```bash
# 포트 사용 프로세스 확인 및 종료
netstat -ano | findstr :8080
taskkill /PID [PID번호] /F
```

**Q: CBM 자동 전환이 작동하지 않음**
- DB 트리거 확인: `SHOW TRIGGERS;`
- 설정값 확인: `SELECT * FROM config WHERE config_key='cbm_threshold';`
- 로그 확인: `tail -f logs/ycs-lms.log`

**Q: QR 스캔 카메라 권한 문제**
- HTTPS 환경에서만 카메라 접근 가능
- 브라우저 설정에서 카메라 권한 허용 필요
- 개발환경: `localhost`는 HTTP도 허용

### 성능 문제
```bash
# DB 쿼리 성능 확인
SHOW PROCESSLIST;
EXPLAIN SELECT * FROM orders WHERE ...;

# 캐시 상태 확인  
redis-cli INFO memory
redis-cli FLUSHDB  # 캐시 초기화
```

---

## 🤝 기여하기

### 브랜치 전략
```
main (프로덕션)
├── develop (개발)
├── feature/order-management (기능 브랜치)
├── hotfix/cbm-calculation-fix (핫픽스)
└── release/v1.0.0 (릴리스)
```

### 커밋 메시지 규칙
```bash
feat(orders): add CBM auto-conversion logic
fix(warehouse): resolve QR scan camera permission
docs(api): update swagger documentation  
style(frontend): apply consistent spacing
refactor(backend): optimize order query performance
test(integration): add warehouse scan scenarios
```

### PR 체크리스트
- [ ] 비즈니스 룰 테스트 통과
- [ ] API 문서 업데이트
- [ ] 모바일 반응형 확인
- [ ] 다국어 번역 추가
- [ ] 보안 체크 완료

---

## 📞 연락처

- **프로젝트 관리자**: Claude Code Assistant  
- **기술 지원**: claude.ai/code
- **이슈 제보**: GitHub Issues
- **문서 기여**: Pull Request 환영

---

## ⚖️ 라이선스

이 프로젝트는 **MIT 라이선스** 하에 배포됩니다.

---

## 🎉 마무리

> **YSC 물류관리시스템**은 모바일 중심의 현대적인 물류 솔루션입니다. 
> CBM 자동 계산, EMS/HS 검증, QR 스캔 등 **실무에서 바로 사용 가능한** 핵심 기능들을 완전히 구현했습니다.

**🚀 지금 바로 시작해보세요!**

```bash
git clone [repository-url]
cd ycs-lms  
start-full-system.bat  # Windows
# 또는 ./start-full-system.sh  # Linux/Mac
```

**Happy Coding! 📦✨**