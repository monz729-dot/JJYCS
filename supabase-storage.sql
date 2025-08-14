-- YCS 물류관리 시스템 Storage 설정
-- Supabase SQL 에디터에서 실행하세요

-- 사업자등록증 파일 저장을 위한 Storage 버킷 생성
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
  'business-licenses',
  'business-licenses',
  true,
  10485760, -- 10MB
  ARRAY['application/pdf', 'image/jpeg', 'image/jpg', 'image/png']
) ON CONFLICT (id) DO NOTHING;

-- Storage 버킷에 대한 RLS 정책 설정
-- 사용자는 자신의 파일만 업로드/조회 가능
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

-- 공개 읽기 권한 (파일 URL 접근용)
CREATE POLICY "Public can view business licenses" ON storage.objects
  FOR SELECT USING (bucket_id = 'business-licenses');
