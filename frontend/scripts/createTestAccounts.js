import { createClient } from '@supabase/supabase-js'
import dotenv from 'dotenv'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

// .env 파일 로드
dotenv.config({ path: join(__dirname, '..', '.env') })

const supabaseUrl = process.env.VITE_SUPABASE_URL
const supabaseServiceKey = process.env.SUPABASE_SERVICE_KEY || process.env.VITE_SUPABASE_ANON_KEY

const supabase = createClient(supabaseUrl, supabaseServiceKey)

// 테스트 계정 정보
const testAccounts = [
  {
    email: 'general@test.com',
    password: 'Test123!@#',
    profile: {
      username: 'general_user',
      name: '일반사용자',
      phone: '010-1111-1111',
      user_type: 'general',
      approval_status: 'approved',
      email_verified: true,
      terms_agreed: true,
      privacy_agreed: true
    }
  },
  {
    email: 'corporate@test.com',
    password: 'Test123!@#',
    profile: {
      username: 'corporate_user',
      name: '기업사용자',
      phone: '010-2222-2222',
      user_type: 'corporate',
      company_name: '테스트기업(주)',
      business_number: '123-45-67890',
      manager_name: '김대리',
      manager_contact: '010-2222-3333',
      approval_status: 'approved',
      email_verified: true,
      terms_agreed: true,
      privacy_agreed: true
    }
  },
  {
    email: 'partner@test.com',
    password: 'Test123!@#',
    profile: {
      username: 'partner_user',
      name: '파트너사용자',
      phone: '010-3333-3333',
      user_type: 'partner',
      company_name: '파트너물류(주)',
      business_number: '234-56-78901',
      manager_name: '박과장',
      manager_contact: '010-3333-4444',
      approval_status: 'approved',
      email_verified: true,
      terms_agreed: true,
      privacy_agreed: true
    }
  },
  {
    email: 'warehouse@test.com',
    password: 'Test123!@#',
    profile: {
      username: 'warehouse_user',
      name: '창고관리자',
      phone: '010-4444-4444',
      user_type: 'warehouse',
      approval_status: 'approved',
      email_verified: true,
      terms_agreed: true,
      privacy_agreed: true
    }
  },
  {
    email: 'admin@test.com',
    password: 'Test123!@#',
    profile: {
      username: 'admin_user',
      name: '시스템관리자',
      phone: '010-5555-5555',
      user_type: 'admin',
      approval_status: 'approved',
      email_verified: true,
      terms_agreed: true,
      privacy_agreed: true
    }
  }
]

async function createTestAccounts() {
  console.log('🚀 테스트 계정 생성 시작...')
  
  for (const account of testAccounts) {
    try {
      console.log(`\n📝 ${account.profile.user_type} 계정 생성 중...`)
      
      // 1. Auth 사용자 생성
      const { data: authData, error: authError } = await supabase.auth.admin.createUser({
        email: account.email,
        password: account.password,
        email_confirm: true,
        user_metadata: {
          name: account.profile.name,
          user_type: account.profile.user_type
        }
      })

      if (authError) {
        if (authError.message.includes('already exists')) {
          console.log(`⚠️  ${account.email} 계정이 이미 존재합니다.`)
          
          // 기존 사용자 조회
          const { data: existingUsers } = await supabase.auth.admin.listUsers()
          const existingUser = existingUsers?.users?.find(u => u.email === account.email)
          
          if (existingUser) {
            // 프로필 업데이트
            const { error: profileError } = await supabase
              .from('user_profiles')
              .upsert({
                id: existingUser.id,
                email: account.email,
                ...account.profile
              })
              
            if (profileError) {
              console.error(`❌ ${account.email} 프로필 업데이트 실패:`, profileError.message)
            } else {
              console.log(`✅ ${account.email} 프로필 업데이트 완료`)
            }
          }
        } else {
          console.error(`❌ ${account.email} 생성 실패:`, authError.message)
        }
        continue
      }

      if (authData.user) {
        // 2. 프로필 생성
        const { error: profileError } = await supabase
          .from('user_profiles')
          .insert({
            id: authData.user.id,
            email: account.email,
            ...account.profile
          })

        if (profileError) {
          console.error(`❌ ${account.email} 프로필 생성 실패:`, profileError.message)
        } else {
          console.log(`✅ ${account.email} 계정 생성 완료`)
        }
      }
    } catch (error) {
      console.error(`❌ ${account.email} 처리 중 오류:`, error.message)
    }
  }

  console.log('\n✨ 테스트 계정 생성 작업 완료!')
  console.log('\n📋 테스트 계정 정보:')
  console.log('================================')
  testAccounts.forEach(acc => {
    console.log(`${acc.profile.user_type.padEnd(12)} | ${acc.email.padEnd(20)} | ${acc.password}`)
  })
  console.log('================================')
}

// 실행
createTestAccounts().catch(console.error)