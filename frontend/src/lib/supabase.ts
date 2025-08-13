import { createClient } from '@supabase/supabase-js'

// 환경 변수 확인 및 기본값 설정
const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'https://demo.supabase.co'
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZS1kZW1vIiwicm9sZSI6ImFub24iLCJleHAiOjE5ODM4MTI5OTZ9.CRXP1A7WOeoJeXxjNni43kdQwgnWNReilDMblYTn_I0'

// 실제 Supabase 프로젝트가 없을 때 알림
if (!import.meta.env.VITE_SUPABASE_URL || !import.meta.env.VITE_SUPABASE_ANON_KEY) {
  console.warn('⚠️ Supabase 환경 변수가 설정되지 않았습니다. 데모 모드로 실행됩니다.')
  console.info('실제 Supabase 연동을 위해 .env 파일에 다음을 추가하세요:')
  console.info('VITE_SUPABASE_URL=your-project-url')
  console.info('VITE_SUPABASE_ANON_KEY=your-anon-key')
}

export const supabase = createClient(supabaseUrl, supabaseAnonKey)

// 타입 정의
export interface UserProfile {
  id: string
  username: string
  name: string
  email?: string
  phone?: string
  address?: string
  user_type: 'general' | 'corporate' | 'partner'
  manager_name?: string
  manager_contact?: string
  company_name?: string
  business_number?: string
  business_license_url?: string
  terms_agreed: boolean
  privacy_agreed: boolean
  approval_status: 'pending' | 'approved' | 'rejected'
  email_verified: boolean
  created_at: string
  updated_at: string
}

export interface SignUpData {
  username: string
  email: string
  password: string
  name: string
  phone?: string
  address?: string
  user_type: 'general' | 'corporate' | 'partner'
  manager_name?: string
  manager_contact?: string
  company_name?: string
  business_number?: string
  business_license_file?: File
  business_license_url?: string
  terms_agreed: boolean
  privacy_agreed: boolean
}

export interface LoginData {
  email: string
  password: string
}
