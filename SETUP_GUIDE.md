# YCS 물류관리 시스템 Supabase 설정 가이드

## 1. Supabase 프로젝트 설정

### 1.1 이메일 인증 활성화
1. Supabase 대시보드 → Authentication → Settings
2. "Enable email confirmations" 체크박스 활성화
3. "Confirm email template" 설정 확인

### 1.2 데이터베이스 스키마 생성
1. Supabase 대시보드 → SQL Editor
2. `supabase-schema.sql` 파일의 내용을 복사하여 실행
3. 실행 결과 확인

### 1.3 Storage 버킷 생성
1. Supabase 대시보드 → SQL Editor
2. `supabase-storage.sql` 파일의 내용을 복사하여 실행
3. Storage → Buckets에서 `business-licenses` 버킷이 생성되었는지 확인

## 2. 회원가입 플로우

### 2.1 일반회원
1. 회원가입 폼 작성
2. 이메일 인증 메일 발송
3. 이메일 인증 완료
4. **자동 승인** → 대시보드 접근 가능

### 2.2 기업회원
1. 회원가입 폼 작성 (사업자등록증 첨부)
2. 이메일 인증 메일 발송
3. 이메일 인증 완료
4. **관리자 승인 대기** → 승인 후 대시보드 접근 가능

### 2.3 파트너회원
1. 회원가입 폼 작성
2. 이메일 인증 메일 발송
3. 이메일 인증 완료
4. **관리자 승인 대기** → 승인 후 대시보드 접근 가능

## 3. 로그인 플로우

### 3.1 이메일 인증 확인
- 모든 사용자 유형에 대해 이메일 인증 완료 필수
- 이메일 인증 미완료 시 로그인 불가

### 3.2 승인 상태 확인
- 일반회원: 이메일 인증 완료 시 즉시 승인
- 기업/파트너 회원: 관리자 승인 필요

## 4. 관리자 기능

### 4.1 사용자 승인
- Supabase 대시보드 → Table Editor → user_profiles
- approval_status를 'pending'에서 'approved'로 변경

### 4.2 사용자 거절
- approval_status를 'rejected'로 변경

## 5. 테스트 방법

### 5.1 회원가입 테스트
1. `/signup` 페이지에서 회원가입
2. 이메일 확인
3. `/email-verification` 페이지에서 인증 완료
4. 사용자 유형에 따라 적절한 페이지로 이동

### 5.2 로그인 테스트
1. `/login` 페이지에서 로그인
2. 이메일 인증 및 승인 상태 확인
3. 대시보드 접근

## 6. 환경 변수 설정

프로젝트 루트의 `.env` 파일이 올바르게 설정되었는지 확인:

```env
VITE_SUPABASE_URL=https://suexposyicisufsnqglp.supabase.co
VITE_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 7. 개발 서버 실행

```bash
cd YCS-ver2/frontend
npm run dev
```

## 8. 주의사항

1. **이메일 인증 필수**: 모든 사용자 유형에 대해 이메일 인증 완료 필수
2. **승인 프로세스**: 일반회원은 자동 승인, 기업/파트너는 관리자 승인 필요
3. **파일 업로드**: 사업자등록증은 10MB 이하, PDF/JPG/PNG 형식만 허용
4. **보안**: RLS 정책으로 사용자별 데이터 접근 제한
