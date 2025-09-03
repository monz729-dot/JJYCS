-- V100__sync_user_profiles.sql
-- Supabase 스키마 정합화: user_profiles 테이블 생성/동기화

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- user_profiles 테이블 생성 (이미 있으면 스킵)
CREATE TABLE IF NOT EXISTS public.user_profiles (
  id                uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  username          varchar,
  name              varchar,
  phone             varchar,
  address           text,
  user_type         varchar,         -- GENERAL | CORPORATE | PARTNER ...
  business_license_url text,
  terms_agreed      bool DEFAULT false,
  privacy_agreed    bool DEFAULT false,
  approval_status   varchar DEFAULT 'PENDING', -- PENDING|APPROVED|REJECTED
  email_verified    bool DEFAULT false,
  created_at        timestamptz DEFAULT now(),
  updated_at        timestamptz DEFAULT now(),
  email             varchar,
  manager_name      varchar,
  manager_contact   varchar,
  company_name      varchar,
  business_number   varchar
);

-- 기존 데이터가 있는 경우를 위한 ALTER 문들 (컬럼이 없으면 추가)
DO $$ 
BEGIN
    -- username 컬럼 추가 (없으면)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'public' AND table_name = 'user_profiles' AND column_name = 'username') THEN
        ALTER TABLE public.user_profiles ADD COLUMN username varchar;
    END IF;
    
    -- manager_name 컬럼 추가 (없으면)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'public' AND table_name = 'user_profiles' AND column_name = 'manager_name') THEN
        ALTER TABLE public.user_profiles ADD COLUMN manager_name varchar;
    END IF;
    
    -- manager_contact 컬럼 추가 (없으면)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'public' AND table_name = 'user_profiles' AND column_name = 'manager_contact') THEN
        ALTER TABLE public.user_profiles ADD COLUMN manager_contact varchar;
    END IF;
    
    -- company_name 컬럼 추가 (없으면)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'public' AND table_name = 'user_profiles' AND column_name = 'company_name') THEN
        ALTER TABLE public.user_profiles ADD COLUMN company_name varchar;
    END IF;
    
    -- business_number 컬럼 추가 (없으면)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'public' AND table_name = 'user_profiles' AND column_name = 'business_number') THEN
        ALTER TABLE public.user_profiles ADD COLUMN business_number varchar;
    END IF;
    
    -- business_license_url 컬럼 추가 (없으면)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'public' AND table_name = 'user_profiles' AND column_name = 'business_license_url') THEN
        ALTER TABLE public.user_profiles ADD COLUMN business_license_url text;
    END IF;
    
    -- terms_agreed 컬럼 추가 (없으면)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'public' AND table_name = 'user_profiles' AND column_name = 'terms_agreed') THEN
        ALTER TABLE public.user_profiles ADD COLUMN terms_agreed bool DEFAULT false;
    END IF;
    
    -- privacy_agreed 컬럼 추가 (없으면)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'public' AND table_name = 'user_profiles' AND column_name = 'privacy_agreed') THEN
        ALTER TABLE public.user_profiles ADD COLUMN privacy_agreed bool DEFAULT false;
    END IF;
    
    -- approval_status 컬럼 추가 (없으면)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'public' AND table_name = 'user_profiles' AND column_name = 'approval_status') THEN
        ALTER TABLE public.user_profiles ADD COLUMN approval_status varchar DEFAULT 'PENDING';
    END IF;
    
    -- email_verified 컬럼 추가 (없으면)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'public' AND table_name = 'user_profiles' AND column_name = 'email_verified') THEN
        ALTER TABLE public.user_profiles ADD COLUMN email_verified bool DEFAULT false;
    END IF;
END $$;

-- UX 검색용 인덱스 생성 (존재하지 않으면)
CREATE INDEX IF NOT EXISTS idx_user_profiles_email ON public.user_profiles(email);
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_type ON public.user_profiles(user_type);
CREATE INDEX IF NOT EXISTS idx_user_profiles_approval_status ON public.user_profiles(approval_status);

-- updated_at 자동 업데이트 함수 생성
CREATE OR REPLACE FUNCTION public.tg_set_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END; 
$$ LANGUAGE plpgsql;

-- updated_at 트리거 생성 (기존 트리거 삭제 후 재생성)
DROP TRIGGER IF EXISTS trg_user_profiles_updated_at ON public.user_profiles;
CREATE TRIGGER trg_user_profiles_updated_at
BEFORE UPDATE ON public.user_profiles
FOR EACH ROW EXECUTE FUNCTION public.tg_set_updated_at();

-- RLS(행수준보안) 활성화 (운영 환경에서 필요시)
-- ALTER TABLE public.user_profiles ENABLE ROW LEVEL SECURITY;

-- 본인 읽기/쓰기 정책 (RLS 활성화 시)
-- DROP POLICY IF EXISTS user_profiles_self_select ON public.user_profiles;
-- CREATE POLICY user_profiles_self_select ON public.user_profiles
--   FOR SELECT USING (auth.uid() = id);

-- DROP POLICY IF EXISTS user_profiles_self_upsert ON public.user_profiles;
-- CREATE POLICY user_profiles_self_upsert ON public.user_profiles
--   FOR INSERT WITH CHECK (auth.uid() = id);

-- DROP POLICY IF EXISTS user_profiles_self_update ON public.user_profiles;
-- CREATE POLICY user_profiles_self_update ON public.user_profiles
--   FOR UPDATE USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

-- 관리자(service_role)는 모든 데이터에 접근 가능 (Supabase 기본 설정)