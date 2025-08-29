# YCS 물류관리시스템 관리자 계정 설정 가이드

## 개요

YCS LMS는 시스템 시작 시 자동으로 관리자 계정을 초기화합니다. 이 문서는 관리자 계정 설정 및 관리 방법에 대해 설명합니다.

## 자동 초기화 시스템

### 개발 환경 (dev profile)

시스템이 `dev` 프로필로 실행될 때, `DevSeedRunner`가 다음 기본 계정들을 자동 생성합니다:

**관리자 계정**
- 이메일: `admin@ycs.com`
- 비밀번호: `password`
- 이름: `Administrator`
- 멤버코드: `ADM001`
- 권한: `ADMIN`

**일반 사용자 계정**
- 이메일: `user@ycs.com`
- 비밀번호: `password`
- 이름: `General User`
- 멤버코드: `GEN001`
- 권한: `GENERAL`

### 프로덕션 환경 (prod profile)

프로덕션 환경에서는 `AdminInitializationRunner`가 환경변수를 통해 관리자 계정을 설정합니다:

```bash
# 환경변수 설정 예시
export ADMIN_EMAIL=admin@yourdomain.com
export ADMIN_PASSWORD=YourSecurePassword123!
export ADMIN_NAME="System Administrator"
export ADMIN_PHONE=010-1234-5678
export ADMIN_MEMBER_CODE=ADM001
```

## 수동 초기화 스크립트

자동 초기화가 실패하거나 수동으로 관리자 계정을 설정해야 하는 경우, 다음 스크립트를 사용할 수 있습니다:

### Windows 환경

```cmd
# 개발 환경
scripts\init-admin.cmd dev

# 프로덕션 환경
scripts\init-admin.cmd prod
```

### Linux/Mac 환경

```bash
# 개발 환경
./scripts/init-admin.sh dev

# 프로덕션 환경
./scripts/init-admin.sh prod
```

## 관리자 계정 로그인 테스트

### API를 통한 테스트

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@ycs.com","password":"password"}'
```

성공적인 응답:
```json
{
  "success": true,
  "message": "로그인 성공",
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "user": {
    "id": 1,
    "email": "admin@ycs.com",
    "name": "Administrator",
    "userType": "ADMIN",
    "status": "ACTIVE",
    "memberCode": "ADM001",
    "emailVerified": true
  }
}
```

### 웹 브라우저를 통한 테스트

1. 프론트엔드 실행: `http://localhost:3001`
2. 로그인 페이지로 이동
3. 관리자 계정으로 로그인
4. 관리자 대시보드 접근 확인: `http://localhost:3001/admin`

## 보안 설정

### 프로덕션 환경 보안 체크리스트

- [ ] **강력한 비밀번호 설정**: 기본 비밀번호를 복잡한 비밀번호로 변경
- [ ] **2단계 인증(2FA) 활성화**: 추가 보안 계층 적용
- [ ] **IP 접근 제한**: 관리자 계정의 접근 IP 제한
- [ ] **정기적인 비밀번호 변경**: 비밀번호 정기 변경 정책 수립
- [ ] **감사 로그 모니터링**: 관리자 계정 활동 로그 추적
- [ ] **세션 타임아웃 설정**: 비활성 시 자동 로그아웃

### 권장 비밀번호 정책

- 최소 12자 이상
- 대소문자, 숫자, 특수문자 조합
- 사전 단어나 개인정보 포함 금지
- 정기적 변경 (90일 주기 권장)

## 문제 해결

### 관리자 계정 로그인 실패

1. **이메일 인증 확인**
   ```sql
   SELECT email, email_verified FROM users WHERE user_type = 'ADMIN';
   ```

2. **계정 상태 확인**
   ```sql
   SELECT email, status FROM users WHERE user_type = 'ADMIN';
   ```

3. **비밀번호 재설정** (H2 콘솔에서)
   ```sql
   UPDATE users SET password = '$2a$10$encrypted_password_here' 
   WHERE email = 'admin@ycs.com';
   ```

### 관리자 권한 승격

기존 사용자를 관리자로 승격:

```sql
UPDATE users SET 
  user_type = 'ADMIN',
  status = 'ACTIVE',
  email_verified = true,
  updated_at = NOW()
WHERE email = 'user@example.com';
```

### 초기화 스크립트 실행 오류

1. **백엔드 서버 실행 확인**
   ```bash
   curl -f http://localhost:8080/api/actuator/health
   ```

2. **권한 확인** (Linux/Mac)
   ```bash
   chmod +x scripts/init-admin.sh
   ```

3. **환경변수 설정 확인**
   ```bash
   echo $ADMIN_EMAIL
   echo $ADMIN_PASSWORD
   ```

## 관리자 기능

관리자 계정으로 로그인하면 다음 기능에 접근할 수 있습니다:

- **사용자 관리**: 회원 승인/거부, 권한 변경
- **주문 관리**: 모든 주문 조회/수정/삭제
- **창고 관리**: 재고 현황, 입출고 내역
- **시스템 설정**: 비즈니스 룰, 요율 관리
- **리포트**: 통계 및 분석 데이터
- **감사 로그**: 시스템 활동 내역

## API 엔드포인트

관리자 전용 API 엔드포인트:

```
GET    /api/admin/users                 # 사용자 목록
POST   /api/admin/users/{id}/approve    # 사용자 승인
POST   /api/admin/users/{id}/reject     # 사용자 거부
GET    /api/admin/orders                # 모든 주문 조회
GET    /api/admin/statistics            # 통계 데이터
GET    /api/admin/audit-logs            # 감사 로그
POST   /api/admin/settings              # 시스템 설정 변경
```

모든 관리자 API는 `ADMIN` 권한이 필요하며, JWT 토큰으로 인증됩니다.

## 지원

관리자 계정 설정 관련 문제가 발생하면 다음을 확인하세요:

1. 백엔드 로그 확인: `logs/application.log`
2. H2 데이터베이스 콘솔: `http://localhost:8080/api/h2-console`
3. 개발팀 문의: `dev@ycs.com`