import { supabase, type UserProfile, type SignUpData, type LoginData } from '@/lib/supabase'

export class AuthService {
  // 회원가입
  static async signUp(data: SignUpData): Promise<{ success: boolean; error?: string }> {
    try {
      console.log('회원가입 시도:', data)
      
      // 현재는 임시로 로컬 저장소에 사용자 데이터 저장 (실제 구현에서는 Supabase 사용)
      const existingUsers = JSON.parse(localStorage.getItem('users') || '[]')
      
      // 새 사용자 데이터 생성
      const newUser = {
        id: Date.now().toString(),
        username: data.username,
        name: data.name,
        email: data.email,
        phone: data.phone,
        address: data.address,
        user_type: data.user_type,
        manager_name: data.manager_name,
        manager_contact: data.manager_contact,
        company_name: data.company_name,
        business_number: data.business_number,
        business_license_url: data.business_license_url,
        terms_agreed: data.terms_agreed,
        privacy_agreed: data.privacy_agreed,
        approval_status: data.user_type === 'general' ? 'approved' : 'pending',
        email_verified: false,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString(),
        role: data.user_type === 'general' ? 'individual' : data.user_type, // 어드민 호환성
        status: data.user_type === 'general' ? 'active' : 'pending_approval' // 어드민 호환성
      }
      
      // 사용자 목록에 추가
      existingUsers.push(newUser)
      localStorage.setItem('users', JSON.stringify(existingUsers))
      
      console.log('회원가입 성공:', newUser)
      
      return { success: true }
    } catch (error) {
      console.error('회원가입 실패:', error)
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '회원가입 중 오류가 발생했습니다.' 
      }
    }
  }

  // 어드민에서 사용할 사용자 목록 조회
  static async getAllUsers(): Promise<any[]> {
    try {
      const users = JSON.parse(localStorage.getItem('users') || '[]')
      return users
    } catch (error) {
      console.error('사용자 목록 조회 실패:', error)
      return []
    }
  }

  // 로그인
  static async signIn(data: LoginData): Promise<{ success: boolean; error?: string }> {
    try {
      const { data: authData, error } = await supabase.auth.signInWithPassword({
        email: data.email,
        password: data.password
      })

      if (error) {
        throw new Error(error.message)
      }

      // 승인 상태 확인 (기업/파트너 회원)
      if (authData.user) {
        const { data: profile } = await supabase
          .from('user_profiles')
          .select('approval_status, user_type')
          .eq('id', authData.user.id)
          .single()

        if (profile && profile.user_type !== 'general' && profile.approval_status === 'rejected') {
          await supabase.auth.signOut()
          throw new Error('승인이 거절된 계정입니다.')
        }

        if (profile && profile.user_type !== 'general' && profile.approval_status === 'pending') {
          await supabase.auth.signOut()
          throw new Error('승인 대기 중인 계정입니다. 관리자 승인 후 로그인 가능합니다.')
        }
      }

      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '로그인 중 오류가 발생했습니다.' 
      }
    }
  }

  // 로그아웃
  static async signOut(): Promise<{ success: boolean; error?: string }> {
    try {
      const { error } = await supabase.auth.signOut()
      if (error) {
        throw new Error(error.message)
      }
      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '로그아웃 중 오류가 발생했습니다.' 
      }
    }
  }

  // 현재 사용자 프로필 조회
  static async getCurrentUserProfile(): Promise<{ data?: UserProfile; error?: string }> {
    try {
      const { data: { user } } = await supabase.auth.getUser()
      
      if (!user) {
        throw new Error('로그인된 사용자가 없습니다.')
      }

      const { data: profile, error } = await supabase
        .from('user_profiles')
        .select('*')
        .eq('id', user.id)
        .single()

      if (error) {
        throw new Error(error.message)
      }

      return { data: profile }
    } catch (error) {
      return { 
        error: error instanceof Error ? error.message : '사용자 정보 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // 사용자 프로필 업데이트
  static async updateUserProfile(updates: Partial<UserProfile>): Promise<{ success: boolean; error?: string }> {
    try {
      const { data: { user } } = await supabase.auth.getUser()
      
      if (!user) {
        throw new Error('로그인된 사용자가 없습니다.')
      }

      const { error } = await supabase
        .from('user_profiles')
        .update(updates)
        .eq('id', user.id)

      if (error) {
        throw new Error(error.message)
      }

      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '프로필 업데이트 중 오류가 발생했습니다.' 
      }
    }
  }

  // 아이디 중복 체크
  static async checkUsername(username: string): Promise<{ available: boolean; error?: string }> {
    try {
      // 네트워크 지연 시뮬레이션
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 로컬스토리지에서 기존 사용자들 확인
      const existingUsers = JSON.parse(localStorage.getItem('users') || '[]')
      const usedUsernames = existingUsers.map((user: any) => user.username ? user.username.toLowerCase() : '')
      
      // 기본적으로 사용 불가능한 아이디들
      const reservedUsernames = ['admin', 'test', 'user', 'demo', 'root', 'administrator']
      const allUsedUsernames = [...usedUsernames, ...reservedUsernames]
      
      const available = !allUsedUsernames.includes(username.toLowerCase())
      
      return { available }
    } catch (error) {
      return { 
        available: false, 
        error: error instanceof Error ? error.message : '사용자명 확인 중 오류가 발생했습니다.' 
      }
    }
  }

  // 실제 DB에서 아이디 중복 체크 (향후 Supabase 연동시 활성화)
  static async checkUsernameAvailability(username: string): Promise<{ available: boolean; error?: string }> {
    // 현재는 mock 구현 사용
    return this.checkUsername(username)
  }

  // 이메일 인증 재발송
  static async resendEmailVerification(): Promise<{ success: boolean; error?: string }> {
    try {
      const { error } = await supabase.auth.resend({
        type: 'signup',
        email: (await supabase.auth.getUser()).data.user?.email || ''
      })

      if (error) {
        throw new Error(error.message)
      }

      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '이메일 인증 재발송 중 오류가 발생했습니다.' 
      }
    }
  }

  // 비밀번호 재설정 이메일 발송
  static async sendPasswordResetEmail(email: string): Promise<{ success: boolean; error?: string }> {
    try {
      const { error } = await supabase.auth.resetPasswordForEmail(email, {
        redirectTo: `${window.location.origin}/reset-password`
      })

      if (error) {
        throw new Error(error.message)
      }

      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '비밀번호 재설정 이메일 발송 중 오류가 발생했습니다.' 
      }
    }
  }

  // 비밀번호 재설정
  static async resetPassword(newPassword: string): Promise<{ success: boolean; error?: string }> {
    try {
      const { error } = await supabase.auth.updateUser({
        password: newPassword
      })

      if (error) {
        throw new Error(error.message)
      }

      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '비밀번호 재설정 중 오류가 발생했습니다.' 
      }
    }
  }

  // 인증 상태 구독
  static onAuthStateChange(callback: (user: any) => void) {
    return supabase.auth.onAuthStateChange((event, session) => {
      callback(session?.user || null)
    })
  }
}
