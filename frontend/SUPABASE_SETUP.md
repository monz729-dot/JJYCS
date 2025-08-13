# YCS 물류관리 시스템 - Supabase 설정 가이드

## 1. Supabase 프로젝트 생성

1. [Supabase](https://supabase.com)에 로그인
2. 새 프로젝트 생성
3. 프로젝트 이름: `ycs-lms`
4. 데이터베이스 비밀번호 설정
5. 지역 선택 (가까운 지역)

## 2. 환경 변수 설정

프로젝트 생성 후 다음 정보를 복사하여 `.env` 파일에 설정:

```bash
# .env 파일 생성
VITE_SUPABASE_URL=your_supabase_project_url
VITE_SUPABASE_ANON_KEY=your_supabase_anon_key
```

## 3. 데이터베이스 스키마 설정

Supabase SQL Editor에서 다음 SQL을 실행:

```sql
-- 사용자 프로필 테이블 생성
CREATE TABLE IF NOT EXISTS user_profiles (
  id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  address TEXT,
  user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('general', 'corporate', 'partner')),
  business_license_url TEXT,
  terms_agreed BOOLEAN DEFAULT FALSE,
  privacy_agreed BOOLEAN DEFAULT FALSE,
  approval_status VARCHAR(20) DEFAULT 'pending' CHECK (approval_status IN ('pending', 'approved', 'rejected')),
  email_verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- RLS (Row Level Security) 활성화
ALTER TABLE user_profiles ENABLE ROW LEVEL SECURITY;

-- 사용자 자신의 프로필만 읽기/수정 가능
CREATE POLICY "Users can view own profile" ON user_profiles
  FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" ON user_profiles
  FOR UPDATE USING (auth.uid() = id);

-- 관리자는 모든 프로필 조회 가능
CREATE POLICY "Admins can view all profiles" ON user_profiles
  FOR SELECT USING (
    EXISTS (
      SELECT 1 FROM user_profiles 
      WHERE id = auth.uid() AND user_type = 'admin'
    )
  );

-- 관리자는 승인 상태 변경 가능
CREATE POLICY "Admins can update approval status" ON user_profiles
  FOR UPDATE USING (
    EXISTS (
      SELECT 1 FROM user_profiles 
      WHERE id = auth.uid() AND user_type = 'admin'
    )
  );

-- 사용자 생성 시 프로필 자동 생성 트리거
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.user_profiles (id, username, name, user_type)
  VALUES (NEW.id, NEW.raw_user_meta_data->>'username', NEW.raw_user_meta_data->>'name', NEW.raw_user_meta_data->>'user_type');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 업데이트 시간 자동 갱신 트리거
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_user_profiles_updated_at
  BEFORE UPDATE ON user_profiles
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

## 4. Storage 버킷 설정

1. Storage > Buckets에서 새 버킷 생성
2. 버킷 이름: `documents`
3. Public bucket으로 설정
4. RLS 정책 설정:

```sql
-- 문서 업로드 정책
CREATE POLICY "Users can upload documents" ON storage.objects
  FOR INSERT WITH CHECK (
    bucket_id = 'documents' AND 
    auth.uid()::text = (storage.foldername(name))[1]
  );

-- 문서 조회 정책
CREATE POLICY "Users can view documents" ON storage.objects
  FOR SELECT USING (
    bucket_id = 'documents' AND 
    (auth.uid()::text = (storage.foldername(name))[1] OR 
     EXISTS (
       SELECT 1 FROM user_profiles 
       WHERE id = auth.uid() AND user_type = 'admin'
     ))
  );
```

## 5. 인증 설정

### 이메일 템플릿 설정

1. Authentication > Email Templates에서 템플릿 수정
2. Confirm signup, Reset password 템플릿 커스터마이징

### 사이트 URL 설정

1. Authentication > URL Configuration
2. Site URL: `http://localhost:5173` (개발용)
3. Redirect URLs: 
   - `http://localhost:5173/login`
   - `http://localhost:5173/signup`
   - `http://localhost:5173/reset-password`

## 6. Edge Functions 배포

### 승인 관리 함수

```bash
# Supabase CLI 설치 (필요시)
npm install -g supabase

# 로그인
supabase login

# 프로젝트 링크
supabase link --project-ref your-project-ref

# 함수 배포
supabase functions deploy approve-user
```

## 7. 관리자 계정 생성

첫 번째 관리자 계정을 생성하려면:

1. 일반 회원으로 가입
2. Supabase SQL Editor에서 다음 실행:

```sql
-- 관리자 권한 부여
UPDATE user_profiles 
SET user_type = 'admin', approval_status = 'approved' 
WHERE email = 'admin@example.com';
```

## 8. 개발 서버 실행

```bash
# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

## 9. 테스트

### 회원가입 테스트

1. `/signup` 페이지 접속
2. 일반회원 가입 테스트
3. 기업회원 가입 테스트 (사업자등록증 업로드)
4. 파트너회원 가입 테스트

### 로그인 테스트

1. `/login` 페이지 접속
2. 이메일/비밀번호로 로그인
3. 승인 대기 상태 확인
4. 비밀번호 찾기 테스트

### 관리자 기능 테스트

1. 관리자 계정으로 로그인
2. 사용자 승인/거절 기능
3. 사용자 목록 조회

## 10. 프로덕션 배포

### 환경 변수 업데이트

```bash
# 프로덕션 URL로 변경
VITE_SUPABASE_URL=https://your-project.supabase.co
VITE_SUPABASE_ANON_KEY=your_anon_key
```

### 사이트 URL 업데이트

Supabase Dashboard에서:
1. Authentication > URL Configuration
2. Site URL을 프로덕션 URL로 변경
3. Redirect URLs 업데이트

## 11. 보안 고려사항

1. **RLS 정책 검토**: 모든 테이블에 적절한 RLS 정책 설정
2. **API 키 보안**: Service Role Key는 서버에서만 사용
3. **파일 업로드 제한**: 파일 크기 및 형식 제한 설정
4. **이메일 인증**: 모든 사용자에 대해 이메일 인증 활성화

## 12. 모니터링

1. **로그 확인**: Supabase Dashboard > Logs에서 에러 확인
2. **성능 모니터링**: Database > Performance에서 쿼리 성능 확인
3. **사용량 모니터링**: Usage에서 API 호출 및 저장소 사용량 확인

## 문제 해결

### 일반적인 문제들

1. **CORS 에러**: Supabase 설정에서 도메인 허용 확인
2. **RLS 정책 오류**: 정책이 올바르게 설정되었는지 확인
3. **파일 업로드 실패**: Storage 버킷 설정 및 정책 확인
4. **이메일 발송 실패**: 이메일 템플릿 및 SMTP 설정 확인

### 디버깅

```javascript
// 브라우저 콘솔에서 Supabase 클라이언트 확인
console.log(supabase.auth.getUser())
console.log(supabase.from('user_profiles').select('*'))
```

## 추가 리소스

- [Supabase 문서](https://supabase.com/docs)
- [Vue.js + Supabase 가이드](https://supabase.com/docs/guides/getting-started/tutorials/with-vue-3)
- [RLS 정책 가이드](https://supabase.com/docs/guides/auth/row-level-security)
