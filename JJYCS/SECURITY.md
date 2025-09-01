# YSC 물류관리시스템 보안 가이드

## 보안 설정 개요

YSC 물류관리시스템은 다층적 보안 접근 방식을 사용합니다.

### 1. 인증 및 권한 관리

#### JWT 토큰 기반 인증
- **액세스 토큰**: 24시간 유효
- **리프레시 토큰**: 7일 유효
- **알고리즘**: HMAC SHA256
- **시크릿 키**: 환경변수로 관리 (최소 64자)

#### 사용자 역할
- `GENERAL`: 일반 개인 사용자
- `CORPORATE`: 기업 사용자  
- `PARTNER`: 파트너 업체
- `WAREHOUSE`: 창고 직원
- `ADMIN`: 시스템 관리자
- `PENDING`: 승인 대기 중인 사용자 (제한된 기능만 이용 가능)

### 2. 패스워드 보안

- **암호화**: BCrypt (강도 12)
- **최소 요구사항**: 8자 이상, 영문/숫자/특수문자 조합
- **저장**: 해시된 형태로만 데이터베이스 저장

### 3. 네트워크 보안

#### HTTPS 강제 (운영환경)
```yaml
# 운영환경에서 HTTPS 강제 설정
server:
  port: 443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

#### CORS 설정
```yaml
# 개발환경: localhost만 허용
cors:
  allowed-origins:
    - http://localhost:3008
    - http://127.0.0.1:3008

# 운영환경: 특정 도메인만 허용  
cors:
  allowed-origins:
    - https://yourdomain.com
    - https://admin.yourdomain.com
```

### 4. 보안 헤더

운영환경에서 자동 적용되는 보안 헤더들:

```http
# XSS 보호
X-XSS-Protection: 1; mode=block

# Content Type 스니핑 방지
X-Content-Type-Options: nosniff

# Clickjacking 방지
X-Frame-Options: DENY

# HTTPS 강제 (HSTS)
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload

# Referrer 정책
Referrer-Policy: strict-origin-when-cross-origin

# Content Security Policy
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:;
```

### 5. API 엔드포인트 보안

#### 공개 엔드포인트 (인증 불요)
- `POST /api/auth/login` - 로그인
- `POST /api/auth/signup` - 회원가입  
- `POST /api/auth/refresh` - 토큰 갱신
- `GET /actuator/health` - 헬스체크

#### 권한별 접근 제어
```java
// 관리자 전용
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/api/admin/users")

// 창고직원 + 관리자
@PreAuthorize("hasRole('WAREHOUSE') or hasRole('ADMIN')")  
@PostMapping("/api/warehouse/scan")

// 승인된 사용자만 (PENDING 제외)
@PreAuthorize("hasRole('GENERAL') or hasRole('CORPORATE') or hasRole('ADMIN')")
@PostMapping("/api/orders")
```

### 6. 환경변수 보안

#### 필수 보안 환경변수
```bash
# JWT 보안 (64자 이상 필수)
JWT_SECRET_KEY=your-very-secure-64-character-minimum-secret-key-for-production

# 데이터베이스 보안  
DB_PASSWORD=your-secure-database-password

# SMTP 보안
SMTP_PASSWORD=your-gmail-app-password

# API 키 보안
HSCODE_SEARCH_API_KEY=your-customs-api-key
HSCODE_TARIFF_API_KEY=your-tariff-api-key
```

### 7. 데이터베이스 보안

#### 연결 보안
- SSL/TLS 연결 강제
- 전용 데이터베이스 사용자 계정
- 최소 권한 원칙 적용

```yaml
# 운영 데이터베이스 설정
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ycs_lms?useSSL=true&requireSSL=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

#### 민감 정보 처리
- 개인정보: 필요시에만 수집, 암호화 저장
- 결제정보: PCI DSS 준수 (외부 결제 모듈 사용)
- 로그: 민감정보 마스킹 처리

### 8. 세션 및 쿠키 보안

#### JWT 토큰 저장
- **저장 위치**: HTTP-only 쿠키 (XSS 방지)
- **전송**: HTTPS에서만 전송 (Secure 플래그)
- **도메인**: Same-Site 정책 적용

#### 세션 관리
```java
// JWT 사용으로 stateless 세션
.sessionManagement(session -> 
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

### 9. 로깅 및 모니터링

#### 보안 이벤트 로깅
```java
// 인증 실패 로깅
log.warn("Failed login attempt for user: {} from IP: {}", username, clientIP);

// 권한 없는 접근 로깅  
log.warn("Unauthorized access attempt to {} from {}", requestURI, remoteAddr);

// 민감한 작업 로깅
log.info("Admin action: {} performed by user: {}", action, username);
```

#### 모니터링 대상
- 로그인 실패 횟수 모니터링
- 비정상적인 API 호출 패턴 감지
- 권한 없는 접근 시도 감지

### 10. 개발 vs 운영 환경 분리

#### 개발환경 (`application-dev.yml`)
- H2 콘솔 활성화
- 상세한 로깅
- 느슨한 CORS 정책
- 하드코딩된 기본값 허용

#### 운영환경 (`application-prod.yml`)  
- H2 콘솔 비활성화
- 최소한의 로깅
- 엄격한 CORS 정책
- 환경변수 강제 사용

### 11. 보안 검토 체크리스트

#### 배포 전 필수 확인사항
- [ ] JWT Secret Key 환경변수 설정
- [ ] 데이터베이스 패스워드 환경변수 설정  
- [ ] SMTP 인증정보 환경변수 설정
- [ ] HTTPS 인증서 설정
- [ ] H2 콘솔 비활성화 확인
- [ ] CORS Origin 운영 도메인으로 제한
- [ ] API 키 환경변수 설정
- [ ] 로그 레벨 INFO로 변경
- [ ] 보안 헤더 설정 확인
- [ ] 방화벽 설정 확인

#### 정기 보안 점검
- [ ] JWT 토큰 만료 시간 검토
- [ ] 사용자 권한 정기 감사
- [ ] 로그 파일 정기 점검
- [ ] 의존성 보안 업데이트
- [ ] SSL/TLS 인증서 갱신

### 12. 보안 사고 대응

#### 사고 대응 절차
1. **사고 감지**: 모니터링 시스템 알림
2. **초기 대응**: 해당 계정 임시 차단
3. **피해 범위 확인**: 로그 분석 및 영향 범위 파악  
4. **복구 작업**: 보안 패치 및 시스템 복구
5. **사후 검토**: 재발 방지 대책 수립

#### 긴급 연락처
- 시스템 관리자: admin@ysc.com
- 보안 담당자: security@ysc.com
- 긴급 대응팀: emergency@ysc.com

---

**마지막 업데이트**: 2024-08-29  
**문서 버전**: 1.0  
**보안 등급**: 기밀