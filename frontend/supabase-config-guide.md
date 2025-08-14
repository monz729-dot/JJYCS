# Supabase 설정 가이드

## 🚨 즉시 해결 방법 (현재 오류 수정)

### 1단계: 데이터베이스 스키마 생성
Supabase 대시보드 → **SQL Editor**에서 다음 스크립트를 **즉시 실행**하세요:

```sql
-- 1. 사용자 프로필 테이블 생성 (기존 테이블이 있다면 삭제 후 재생성)
DROP TABLE IF EXISTS public.user_profiles CASCADE;

CREATE TABLE public.user_profiles (
    id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    email TEXT,
    phone TEXT,
    address TEXT,
    user_type TEXT NOT NULL CHECK (user_type IN ('general', 'corporate', 'partner', 'admin', 'warehouse')),
    manager_name TEXT,
    manager_contact TEXT,
    company_name TEXT,
    business_number TEXT,
    business_license_url TEXT,
    terms_agreed BOOLEAN DEFAULT false,
    privacy_agreed BOOLEAN DEFAULT false,
    approval_status TEXT DEFAULT 'pending' CHECK (approval_status IN ('pending', 'approved', 'rejected')),
    email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. RLS 활성화
ALTER TABLE public.user_profiles ENABLE ROW LEVEL SECURITY;

-- 3. 기본 RLS 정책 설정
CREATE POLICY "Enable insert for authenticated users only" ON public.user_profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

CREATE POLICY "Enable select for users based on user_id" ON public.user_profiles
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Enable update for users based on user_id" ON public.user_profiles
    FOR UPDATE USING (auth.uid() = id);

-- 4. 관리자 정책 (나중에 추가)
CREATE POLICY "Enable select for admins" ON public.user_profiles
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.user_profiles 
            WHERE id = auth.uid() AND user_type = 'admin'
        )
    );

-- 5. 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_user_profiles_username ON public.user_profiles(username);
CREATE INDEX IF NOT EXISTS idx_user_profiles_email ON public.user_profiles(email);
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_type ON public.user_profiles(user_type);
CREATE INDEX IF NOT EXISTS idx_user_profiles_approval_status ON public.user_profiles(approval_status);
```

### 2단계: 이메일 인증 설정
1. **Authentication** → **Settings** → **Auth**
2. **Enable email confirmations** 체크박스 활성화
3. **Save** 클릭

### 3단계: Gmail SMTP 설정 (이미 완료했다면 건너뛰기)
1. **Authentication** → **Settings** → **SMTP Settings**
2. **Custom** 선택
3. 다음 정보 입력:
   - Host: `smtp.gmail.com`
   - Port: `587`
   - Username: `your-gmail@gmail.com`
   - Password: `앱 비밀번호`
   - Sender Name: `YCS LMS`
   - Sender Email: `your-gmail@gmail.com`
4. **Test Connection** → **Save**

### 4단계: 테스트
위 설정 완료 후 회원가입을 다시 시도해보세요.

---

## 1. Supabase 프로젝트 설정

### 1.1 이메일 인증 활성화
1. Supabase 대시보드에서 프로젝트 선택
2. **Authentication** → **Settings** → **Auth** 메뉴로 이동
3. **Enable email confirmations** 체크박스를 활성화
4. **Confirm email template** 설정:
   - **Subject**: `YCS LMS 이메일 인증`
   - **Template**: 기본 템플릿 사용 또는 커스터마이징
5. **Save** 버튼 클릭

### 1.2 Gmail SMTP 설정 (권장)
Gmail을 사용하여 이메일을 발송하려면 다음 단계를 따르세요:

#### 1.2.1 Gmail 앱 비밀번호 생성
1. Google 계정 설정으로 이동: https://myaccount.google.com/
2. **보안** → **2단계 인증** 활성화
3. **보안** → **앱 비밀번호** 클릭
4. **앱 선택** → **기타 (맞춤 이름)** 선택
5. 이름 입력 (예: "Supabase YCS LMS")
6. **생성** 버튼 클릭하여 16자리 앱 비밀번호 생성

#### 1.2.2 Supabase SMTP 설정
1. Supabase 대시보드에서 **Authentication** → **Settings** → **SMTP Settings**
2. **SMTP Provider** 선택에서 **Custom** 선택
3. 다음 정보 입력:
   - **Host**: `smtp.gmail.com`
   - **Port**: `587`
   - **Username**: `your-email@gmail.com`
   - **Password**: `생성한 16자리 앱 비밀번호`
   - **Sender Name**: `YCS LMS`
   - **Sender Email**: `your-email@gmail.com`
4. **Test Connection** 버튼으로 연결 테스트
5. **Save** 버튼 클릭

### 1.3 이메일 템플릿 커스터마이징
1. **Authentication** → **Settings** → **Email Templates**
2. **Confirm signup** 템플릿 선택
3. 다음과 같이 수정:

```html
<h2>YCS LMS 이메일 인증</h2>
<p>안녕하세요!</p>
<p>YCS LMS 회원가입을 완료하려면 아래 버튼을 클릭해주세요.</p>
<p><a href="{{ .ConfirmationURL }}">이메일 인증하기</a></p>
<p>버튼이 작동하지 않는 경우, 아래 링크를 브라우저에 복사하여 붙여넣기 해주세요:</p>
<p>{{ .ConfirmationURL }}</p>
<p>감사합니다.</p>
<p>YCS LMS 팀</p>
```

## 2. 데이터베이스 스키마 설정

### 2.1 사용자 프로필 테이블 생성 (완전한 버전)
Supabase SQL Editor에서 다음 스크립트를 실행:

```sql
-- 사용자 프로필 테이블 생성
CREATE TABLE IF NOT EXISTS public.user_profiles (
    id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    email TEXT,
    phone TEXT,
    address TEXT,
    user_type TEXT NOT NULL CHECK (user_type IN ('general', 'corporate', 'partner', 'admin', 'warehouse')),
    manager_name TEXT,
    manager_contact TEXT,
    company_name TEXT,
    business_number TEXT,
    business_license_url TEXT,
    terms_agreed BOOLEAN DEFAULT false,
    privacy_agreed BOOLEAN DEFAULT false,
    approval_status TEXT DEFAULT 'pending' CHECK (approval_status IN ('pending', 'approved', 'rejected')),
    email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- RLS 활성화
ALTER TABLE public.user_profiles ENABLE ROW LEVEL SECURITY;

-- 사용자 자신의 프로필만 읽기/수정 가능
CREATE POLICY "Users can view own profile" ON public.user_profiles
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" ON public.user_profiles
    FOR UPDATE USING (auth.uid() = id);

-- 새 사용자 생성 시 프로필 생성 허용
CREATE POLICY "Enable insert for authenticated users only" ON public.user_profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

-- 관리자는 모든 프로필 조회 가능
CREATE POLICY "Admins can view all profiles" ON public.user_profiles
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.user_profiles 
            WHERE id = auth.uid() AND user_type = 'admin'
        )
    );

-- 새 사용자 생성 시 자동으로 프로필 생성 (선택사항)
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.user_profiles (id, username, name, email, user_type, terms_agreed, privacy_agreed)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'username', 'user_' || NEW.id),
        COALESCE(NEW.raw_user_meta_data->>'name', '사용자'),
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'user_type', 'general'),
        COALESCE((NEW.raw_user_meta_data->>'terms_agreed')::boolean, false),
        COALESCE((NEW.raw_user_meta_data->>'privacy_agreed')::boolean, false)
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 기존 트리거가 있다면 삭제
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 이메일 인증 완료 시 자동으로 상태 업데이트
CREATE OR REPLACE FUNCTION public.handle_email_verification()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.email_confirmed_at IS NOT NULL AND OLD.email_confirmed_at IS NULL THEN
        UPDATE public.user_profiles 
        SET email_verified = true
        WHERE id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 기존 트리거가 있다면 삭제
DROP TRIGGER IF EXISTS on_auth_user_email_verified ON auth.users;

CREATE TRIGGER on_auth_user_email_verified
    AFTER UPDATE ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_email_verification();

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_user_profiles_username ON public.user_profiles(username);
CREATE INDEX IF NOT EXISTS idx_user_profiles_email ON public.user_profiles(email);
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_type ON public.user_profiles(user_type);
CREATE INDEX IF NOT EXISTS idx_user_profiles_approval_status ON public.user_profiles(approval_status);
```

### 2.2 파일 저장소 설정
Supabase SQL Editor에서 다음 스크립트를 실행:

```sql
-- 사업자등록증 저장소 버킷 생성
INSERT INTO storage.buckets (id, name, public) 
VALUES ('business-licenses', 'business-licenses', true)
ON CONFLICT (id) DO NOTHING;

-- RLS 정책 설정
CREATE POLICY "Users can upload own business license" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'business-licenses' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can view own business license" ON storage.objects
    FOR SELECT USING (
        bucket_id = 'business-licenses' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can update own business license" ON storage.objects
    FOR UPDATE USING (
        bucket_id = 'business-licenses' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can delete own business license" ON storage.objects
    FOR DELETE USING (
        bucket_id = 'business-licenses' AND 
        auth.uid()::text = (storage.foldername(name))[1]
    );

-- 관리자는 모든 파일 조회 가능
CREATE POLICY "Admins can view all business licenses" ON storage.objects
    FOR SELECT USING (
        bucket_id = 'business-licenses' AND
        EXISTS (
            SELECT 1 FROM public.user_profiles 
            WHERE id = auth.uid() AND user_type = 'admin'
        )
    );

-- 공개 읽기 정책 (파일 URL 접근용)
CREATE POLICY "Public read access for business licenses" ON storage.objects
    FOR SELECT USING (bucket_id = 'business-licenses');
```

## 3. 환경 변수 설정

프로젝트 루트의 `.env` 파일에 다음 내용을 추가:

```env
# Supabase Configuration
VITE_SUPABASE_URL=your_supabase_project_url
VITE_SUPABASE_ANON_KEY=your_supabase_anon_key

# App Configuration
VITE_APP_TITLE=YCS 물류관리 시스템
VITE_APP_VERSION=2.0.0
```

## 4. 회원가입/로그인 플로우

### 4.1 새로운 회원가입 플로우
1. **사용자 정보 입력** → **회원가입 완료** → **이메일 인증**
2. **일반회원**: 이메일 인증 완료 시 즉시 승인
3. **기업/파트너 회원**: 이메일 인증 완료 후 관리자 승인 대기

### 4.2 이메일 인증 프로세스
1. 사용자가 회원가입 폼 작성 후 제출
2. Supabase Auth에서 자동으로 이메일 인증 발송
3. 사용자가 이메일의 인증 링크 클릭
4. 인증 완료 후 자동으로 `email_verified` 상태 업데이트
5. 사용자 유형에 따라 승인 상태 설정

### 4.3 로그인 플로우
1. 이메일/아이디 + 비밀번호 입력
2. 이메일 인증 상태 확인
3. 승인 상태 확인
4. 로그인 성공 시 사용자 유형에 따른 대시보드로 이동

## 5. 테스트 방법

### 5.1 이메일 인증 테스트
1. 회원가입 페이지에서 정보 입력 후 제출
2. 실제 이메일 수신 확인
3. 이메일의 인증 링크 클릭
4. 인증 완료 확인

### 5.2 개발 환경에서 이메일 확인
- Supabase 대시보드의 **Authentication** → **Users**에서 사용자 목록 확인
- 이메일 인증 상태 확인 가능

## 6. 문제 해결

### 6.1 이메일이 발송되지 않는 경우

#### 6.1.1 Gmail SMTP 설정 확인
1. **Gmail 앱 비밀번호가 올바른지 확인**
   - 16자리 앱 비밀번호 사용
   - 일반 Gmail 비밀번호가 아닌 앱 비밀번호 사용

2. **2단계 인증이 활성화되어 있는지 확인**
   - Gmail 계정에서 2단계 인증 필수

3. **Supabase SMTP 설정 확인**
   - Host: `smtp.gmail.com`
   - Port: `587`
   - Username: Gmail 주소
   - Password: 앱 비밀번호

#### 6.1.2 Supabase 설정 확인
1. **이메일 인증이 활성화되어 있는지 확인**
   - Authentication → Settings → Auth → Enable email confirmations

2. **SMTP 연결 테스트**
   - Authentication → Settings → SMTP Settings → Test Connection

3. **이메일 템플릿 확인**
   - Authentication → Settings → Email Templates → Confirm signup

#### 6.1.3 로그 확인
1. **Supabase 대시보드에서 로그 확인**
   - Authentication → Logs에서 이메일 발송 로그 확인

2. **브라우저 개발자 도구 확인**
   - Network 탭에서 API 호출 확인
   - Console 탭에서 오류 메시지 확인

### 6.2 인증 링크가 작동하지 않는 경우
1. 인증 링크의 유효기간 확인 (기본 24시간)
2. 브라우저에서 링크 직접 열기
3. 개발 환경에서는 `localhost` URL 확인

### 6.3 데이터베이스 오류
1. SQL 스크립트가 올바르게 실행되었는지 확인
2. RLS 정책이 올바르게 설정되었는지 확인
3. Supabase 대시보드의 로그 확인

### 6.4 Gmail 관련 추가 문제 해결

#### 6.4.1 Gmail 보안 설정
1. **보안 수준이 낮은 앱의 액세스** 허용 (필요시)
2. **캡차 확인** 완료
3. **계정 잠금** 해제

#### 6.4.2 대안 이메일 제공자
Gmail이 계속 문제가 되는 경우:
- **SendGrid**: 무료 100건/일
- **Mailgun**: 무료 5,000건/월
- **Amazon SES**: 매우 저렴한 비용

### 6.5 디버깅 체크리스트
- [ ] Gmail 2단계 인증 활성화
- [ ] Gmail 앱 비밀번호 생성
- [ ] Supabase SMTP 설정 완료
- [ ] SMTP 연결 테스트 성공
- [ ] 이메일 인증 활성화
- [ ] 데이터베이스 스키마 생성
- [ ] 환경 변수 설정
- [ ] 브라우저 캐시 클리어
- [ ] 스팸 메일함 확인

### 6.6 현재 오류 해결 방법

#### 406 오류 해결
- `user_profiles` 테이블이 존재하지 않음
- 위의 "즉시 해결 방법" 1단계 실행

#### 400 오류 해결
- RLS 정책 문제
- 위의 SQL 스크립트에서 RLS 정책 재설정

#### 403 오류 해결
- 관리자 권한 문제
- 코드에서 사용자 삭제 로직 제거 (이미 수정됨)
