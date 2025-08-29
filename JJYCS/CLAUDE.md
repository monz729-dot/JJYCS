# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

**YCS 물류관리 시스템(LMS) v2.0**
- 모바일 중심의 물류관리 플랫폼
- Spring Boot 3.0.3 + Vue.js 3.4 + TypeScript
- 다중 사용자 역할 지원 (일반/기업/파트너/창고/관리자)
- JWT 기반 인증 및 이메일 알림 시스템

## 시스템 아키텍처

### 백엔드 기술 스택
- **Framework**: Spring Boot 3.0.3
- **Java Version**: JDK 17
- **Security**: Spring Security 6.x + JWT
- **Database**: H2 (개발환경), MySQL 8.0+ (운영환경)
- **Build Tool**: Maven 3.8+
- **Port**: 8081 (context-path: /api)

### 프론트엔드 기술 스택
- **Framework**: Vue.js 3.4 + Composition API
- **Language**: TypeScript 4.9+
- **State Management**: Pinia
- **Build Tool**: Vite 4.x
- **UI**: Tailwind CSS + 커스텀 컴포넌트
- **Port**: 3008 (개발서버)

## 빌드 및 실행 명령어

### 백엔드 서버 실행
```bash
cd backend
./mvnw.cmd spring-boot:run
```

### 프론트엔드 서버 실행
```bash
cd frontend
npm run dev
```

### 전체 시스템 실행 (Windows)
```cmd
start "YCS Backend" cmd /c "cd backend && ./mvnw.cmd spring-boot:run"
start "YCS Frontend" cmd /c "cd frontend && npm run dev"
start "H2 Console" "http://localhost:8081/api/h2-console"
```

## 핵심 기능 구현 상태

### ✅ 완료된 기능

1. **회원 관리 시스템**
   - 다중 사용자 역할 (GENERAL, CORPORATE, PARTNER, WAREHOUSE, ADMIN)
   - 이메일 인증 토큰 생성
   - 기업/파트너 승인 프로세스
   - 회원 코드 자동 생성 (GEN001, COR001, PAR001 등)

2. **인증/인가 시스템**
   - JWT 액세스 토큰 (24시간) + 리프레시 토큰 (7일)
   - Spring Security 필터 체인
   - 역할별 API 접근 제어 (@PreAuthorize)
   - BCrypt 패스워드 암호화

3. **이메일 알림 시스템**
   - 회원 승인/거절 시 자동 이메일 발송
   - HTML 템플릿 기반 이메일
   - Gmail SMTP 연동 (개발환경에서는 로그 출력)

4. **HS 코드 캐싱 시스템**
   - TTL 기반 메모리 캐시 (1시간)
   - 캐시 히트/미스 통계
   - 품목명/HS코드 검색 캐시
   - 관세율/환율 조회 캐시

5. **API 예외 처리 개선**
   - WebClient 타임아웃 설정 (10초)
   - HTTP 상태별 사용자 친화적 오류 메시지
   - 네트워크 연결 오류 처리

6. **관리자 대시보드**
   - 사용자 통계 조회
   - 승인 대기 사용자 관리
   - 시스템 설정 페이지 연결

### ⚠️ 미완성 기능

1. **주문 관리 API**
   - 주문 생성/조회/상태 변경
   - CBM 29m³ 초과 시 자동 항공 전환
   - THB 1,500 초과 수취인 추가 정보 경고

2. **HS 코드 실제 API 연동**
   - 관세청 Open API 연동
   - 실시간 환율 조회

3. **창고 관리 시스템**
   - QR/바코드 스캔
   - 입출고 관리
   - 재고 현황

## 환경 설정

### 필수 환경변수
```bash
# JWT 보안
JWT_SECRET=your-very-secure-jwt-secret-key-minimum-64-characters

# 데이터베이스 (운영환경)
DB_URL=jdbc:mysql://localhost:3306/ycs_lms
DB_USERNAME=ycs_user
DB_PASSWORD=your_secure_password

# SMTP 설정
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password

# HS 코드 API (관세청)
HSCODE_SEARCH_API_KEY=your-api-key
HSCODE_TARIFF_API_KEY=your-api-key
HSCODE_EXCHANGE_API_KEY=your-api-key
```

### 개발환경 기본 계정
```
관리자: admin@ycs.com / password
일반 사용자: user@example.com / password
H2 Console: sa / (비밀번호 없음)
```

## 보안 설정

### 🔴 즉시 수정 필요한 보안 이슈

1. **CORS 설정**: 현재 모든 Origin 허용 중 → 특정 도메인만 허용해야 함
2. **JWT 시크릿**: 하드코딩된 키 → 환경변수로 이전 필요
3. **SMTP 인증정보**: 평문 노출 → 환경변수로 이전 필요
4. **H2 Console**: 운영환경 노출 위험 → 개발환경에서만 활성화

### 권장 보안 설정
```yaml
# application-prod.yml
spring:
  h2:
    console:
      enabled: false  # 운영환경에서 비활성화

cors:
  allowed-origins:
    - https://yourdomain.com
    - https://admin.yourdomain.com
  # localhost는 개발환경에서만 허용

logging:
  level:
    com.ycs.lms: INFO  # 운영환경에서는 INFO 레벨
    org.springframework.security: WARN
```

## API 엔드포인트

### 인증 API
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인
- `POST /api/auth/refresh` - 토큰 갱신
- `POST /api/auth/logout` - 로그아웃

### 관리자 API
- `GET /api/admin/stats` - 시스템 통계
- `GET /api/admin/users/pending` - 승인 대기 사용자
- `POST /api/admin/users/{id}/approve` - 사용자 승인
- `POST /api/admin/users/{id}/reject` - 사용자 거절

### HS 코드 API (개발중)
- `GET /api/hscode/search` - 품목명으로 검색
- `GET /api/hscode/tariff/{code}` - 관세율 조회
- `GET /api/hscode/exchange` - 환율 조회

## 테스트 가이드

### API 테스트 시나리오
1. 회원가입 → 일반사용자는 즉시 활성화, 기업/파트너는 승인 대기
2. 로그인 → JWT 토큰 발급 확인
3. 관리자 승인 → 이메일 발송 확인
4. 권한별 API 접근 테스트

### 성능 테스트
- HS 코드 캐시 히트율 확인
- 동일 요청 반복 시 응답 시간 개선 확인
- JWT 토큰 검증 성능

## 개발 워크플로우

### 브랜치 전략
- `master`: 운영 브랜치
- `develop`: 개발 브랜치  
- `feature/*`: 기능별 브랜치

### 커밋 메시지 규칙
```
feat: 새로운 기능 추가
fix: 버그 수정
security: 보안 개선
perf: 성능 개선
docs: 문서 수정
test: 테스트 추가/수정
refactor: 코드 리팩토링
```

### 코드 리뷰 체크리스트
- [ ] 보안: @PreAuthorize 어노테이션 적용
- [ ] 예외처리: 사용자 친화적 에러 메시지
- [ ] 로깅: 민감정보 제외, 적절한 로그 레벨
- [ ] 테스트: 비즈니스 로직 단위 테스트
- [ ] 문서: API 스펙 업데이트

## 배포 가이드

### 운영환경 배포 전 체크리스트
- [ ] 환경변수 설정 확인
- [ ] H2 Console 비활성화
- [ ] CORS 설정 운영용으로 변경
- [ ] 로그 레벨 INFO로 설정
- [ ] SSL/HTTPS 설정
- [ ] 데이터베이스 연결 확인
- [ ] SMTP 설정 확인

### Docker 배포
```bash
# 백엔드 이미지 빌드
cd backend
docker build -t ycs-backend:latest .

# 프론트엔드 이미지 빌드  
cd frontend
docker build -t ycs-frontend:latest .

# Docker Compose로 전체 실행
docker-compose up -d
```

## 트러블슈팅

### 자주 발생하는 문제

1. **UTF-8 인코딩 에러**
   - 원인: 한글 데이터 JSON 파싱 시 발생
   - 해결: CharacterEncodingFilter 설정 확인

2. **JWT 토큰 만료**
   - 원인: 24시간 후 자동 만료
   - 해결: refresh token으로 갱신

3. **CORS 에러**
   - 원인: 허용되지 않은 Origin에서 요청
   - 해결: CorsConfig에서 Origin 추가

4. **H2 데이터베이스 락**
   - 원인: 다중 접속 시 발생
   - 해결: 애플리케이션 재시작

### 로그 확인 방법
```bash
# 백엔드 로그 실시간 모니터링
tail -f logs/application.log

# 특정 에러 검색
grep -i "error\|exception" logs/application.log

# 캐시 히트율 확인
grep -i "cache hit\|cache miss" logs/application.log
```

## 향후 개발 계획

### Phase 4: 비즈니스 로직 완성
- [ ] 주문 생성/관리 API 완성
- [ ] CBM/THB 비즈니스 룰 구현
- [ ] 창고 관리 시스템 구현

### Phase 5: 고도화
- [ ] Redis 캐시 서버 도입
- [ ] 실시간 알림 (WebSocket)
- [ ] 모바일 PWA 최적화
- [ ] 성능 모니터링 도구 연동

## 연락처 및 지원

- **개발팀**: YCS Development Team
- **문의사항**: 이슈 트래커 또는 개발팀 연락
- **문서 업데이트**: 매 Sprint 종료 시점

---

**마지막 업데이트**: 2024-08-29
**문서 버전**: 2.0
**시스템 버전**: Phase 3 Complete