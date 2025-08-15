import { supabase, type UserProfile, type SignUpData, type LoginData } from '@/lib/supabase'

export class AuthService {
  // 회원가입 (이메일 인증 후 완료)
  static async signUp(data: SignUpData): Promise<{ success: boolean; error?: string; requiresEmailVerification?: boolean; userType?: string }> {
    try {
      
      
      
      
      
      
      // Supabase 연결 테스트
      try {
        const { data, error } = await supabase.auth.getSession()
        
        
      } catch (testError) {
        console.error('Supabase 연결 테스트 실패:', testError)
      }
      
      // 이메일 발송 테스트
      
      
      // 기존 세션 완전 클리어
      await supabase.auth.signOut()
      
      
      // 새로운 회원가입 시도 (Supabase가 내부적으로 중복 이메일 처리)
      const signUpOptions = {
        email: data.email,
        password: data.password,
        options: {
          data: {
            username: data.username,
            name: data.name,
            user_type: data.user_type,
            terms_agreed: data.terms_agreed,
            privacy_agreed: data.privacy_agreed
          },
          emailRedirectTo: `${window.location.origin}/auth/callback`
        }
      }
      
      
      
      const { data: authData, error: authError } = await supabase.auth.signUp(signUpOptions)

      
      

      if (authError) {
        console.error('Supabase Auth 에러:', authError)
        // 이메일 중복 에러인 경우에도 계속 진행
        if (authError.message.includes('already registered') || authError.message.includes('already exists')) {
          
          return { 
            success: true,
            requiresEmailVerification: true,
            userType: data.user_type 
          }
        }
        throw new Error(authError.message)
      }

      if (authData.user) {
        
        
        
        return { 
          success: true,
          requiresEmailVerification: true,
          userType: data.user_type 
        }
      }

      
      return { success: false, error: '회원가입에 실패했습니다.' }
    } catch (error) {
      console.error('회원가입 실패:', error)
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '회원가입 중 오류가 발생했습니다.' 
      }
    }
  }

  // 이메일 인증 완료 후 프로필 생성
  static async completeSignUp(data: SignUpData): Promise<{ success: boolean; error?: string }> {
    try {
      const { data: { user } } = await supabase.auth.getUser()
      
      if (!user || !user.email_confirmed_at) {
        throw new Error('이메일 인증이 완료되지 않았습니다.')
      }

      // 프로필 업데이트 (트리거가 이미 생성했으므로)
      
      
      console.log('프로필 데이터:', {
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
        email_verified: true
      })
      
      const { data: profileData, error: profileError } = await supabase
        .from('user_profiles')
        .update({
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
          email_verified: true
        })
        .eq('id', user.id)
        .select()

      
      

      if (profileError) {
        console.error('프로필 업데이트 실패 상세:', {
          message: profileError.message,
          details: profileError.details,
          hint: profileError.hint,
          code: profileError.code
        })
        throw new Error(`프로필 업데이트에 실패했습니다: ${profileError.message}`)
      }

      
      return { success: true }
    } catch (error) {
      console.error('회원가입 완료 실패:', error)
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '회원가입 완료에 실패했습니다.' 
      }
    }
  }

  // 이메일 인증 메일 재발송 (기존 사용자용)
  static async sendVerificationEmail(email: string): Promise<{ success: boolean; error?: string }> {
    try {
      // 이메일 인증 재발송
      const { error } = await supabase.auth.resend({
        type: 'signup',
        email: email
      })

      if (error) {
        throw new Error(error.message)
      }

      return { success: true }
    } catch (error) {
      console.error('이메일 인증 발송 실패:', error)
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '이메일 인증 발송에 실패했습니다.' 
      }
    }
  }

  // 이메일 인증 확인 (토큰 방식)
  static async verifyEmail(email: string, token: string): Promise<{ success: boolean; error?: string }> {
    try {
      const { error } = await supabase.auth.verifyOtp({
        email: email,
        token: token,
        type: 'signup'
      })

      if (error) {
        throw new Error(error.message)
      }

      return { success: true }
    } catch (error) {
      console.error('이메일 인증 확인 실패:', error)
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '이메일 인증 확인에 실패했습니다.' 
      }
    }
  }

  // 이메일 인증 토큰 재발송
  static async resendVerificationToken(email: string): Promise<{ success: boolean; error?: string }> {
    try {
      
      
      
      const { error } = await supabase.auth.resend({
        type: 'signup',
        email: email
      })

      

      if (error) {
        console.error('재발송 에러:', error)
        throw new Error(error.message)
      }

      
      return { success: true }
    } catch (error) {
      console.error('인증 토큰 재발송 실패:', error)
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '인증 토큰 재발송에 실패했습니다.' 
      }
    }
  }

  // 이메일 인증 상태 확인 및 처리
  static async handleEmailVerification(): Promise<{ success: boolean; error?: string }> {
    try {
      const { data: { user } } = await supabase.auth.getUser()
      
      if (!user) {
        throw new Error('로그인된 사용자가 없습니다.')
      }

      // 이메일 인증 상태 확인
      if (user.email_confirmed_at) {
        // 이메일 인증이 완료된 경우, 프로필 업데이트
        const { data: profile } = await supabase
          .from('user_profiles')
          .select('user_type')
          .eq('id', user.id)
          .single()

        if (profile) {
          // 사용자 유형에 따라 승인 상태 설정
          const approvalStatus = profile.user_type === 'general' ? 'approved' : 'pending'
          
          const { error: updateError } = await supabase
            .from('user_profiles')
            .update({
              email_verified: true,
              approval_status: approvalStatus
            })
            .eq('id', user.id)

          if (updateError) {
            throw new Error(updateError.message)
          }
        }

        return { success: true }
      } else {
        throw new Error('이메일 인증이 완료되지 않았습니다.')
      }
    } catch (error) {
      console.error('이메일 인증 처리 실패:', error)
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '이메일 인증 처리에 실패했습니다.' 
      }
    }
  }

  // 어드민에서 사용할 사용자 목록 조회
  static async getAllUsers(): Promise<any[]> {
    try {
      const { data: users, error } = await supabase
        .from('user_profiles')
        .select('*')
        .order('created_at', { ascending: false })

      if (error) {
        throw new Error(error.message)
      }

      return users || []
    } catch (error) {
      console.error('사용자 목록 조회 실패:', error)
      return []
    }
  }

  // 로그인
  static async signIn(data: LoginData): Promise<{ success: boolean; error?: string }> {
    try {
      
      
      
      // 이메일 또는 아이디로 로그인 처리
      let email = data.email
      
      // 아이디로 로그인 시도하는 경우, 아이디로 이메일 찾기
      if (!data.email.includes('@')) {
        
        
        try {
          const { data: userProfile, error: profileError } = await supabase
            .from('user_profiles')
            .select('email')
            .eq('username', data.email)
            .single()
          
          
          
          if (profileError) {
            console.error('프로필 조회 에러:', profileError)
            throw new Error('사용자 정보를 찾을 수 없습니다.')
          }
          
          if (userProfile) {
            email = userProfile.email
            
          } else {
            throw new Error('존재하지 않는 아이디입니다.')
          }
        } catch (profileError) {
          console.error('프로필 조회 실패:', profileError)
          throw new Error('사용자 정보를 찾을 수 없습니다.')
        }
      }

      
      
      const { data: authData, error } = await supabase.auth.signInWithPassword({
        email: email,
        password: data.password
      })

      

      if (error) {
        console.error('로그인 에러:', error)
        throw new Error(error.message)
      }

      // 승인 상태 확인
      if (authData.user) {
        
        
        try {
          const { data: profile, error: profileError } = await supabase
            .from('user_profiles')
            .select('approval_status, user_type, email_verified')
            .eq('id', authData.user.id)
            .single()

          

          if (profileError) {
            console.error('프로필 상태 확인 에러:', profileError)
            // 프로필이 없어도 로그인은 허용 (나중에 생성될 수 있음)
            
          } else if (profile) {
            // 모든 사용자 유형에 대해 이메일 인증 확인
            if (!profile.email_verified) {
              await supabase.auth.signOut()
              throw new Error('이메일 인증이 필요합니다. 이메일을 확인해주세요.')
            }

            // 승인 상태 확인 (모든 사용자 유형)
            if (profile.approval_status === 'rejected') {
              await supabase.auth.signOut()
              throw new Error('승인이 거절된 계정입니다. 고객센터에 문의해주세요.')
            }

            if (profile.approval_status === 'pending') {
              await supabase.auth.signOut()
              throw new Error('승인 대기 중인 계정입니다. 관리자 승인 후 로그인 가능합니다.')
            }
          }
        } catch (profileError) {
          console.error('프로필 상태 확인 실패:', profileError)
          // 프로필 확인 실패해도 로그인은 허용
        }
      }

      
      return { success: true }
    } catch (error) {
      console.error('로그인 실패:', error)
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
        error: error instanceof Error ? error.message : '프로필 조회 중 오류가 발생했습니다.' 
      }
    }
  }

  // 사용자명 중복 확인
  static async checkUsernameAvailability(username: string): Promise<{ available: boolean; error?: string }> {
    try {
      const { data, error } = await supabase
        .from('user_profiles')
        .select('username')
        .eq('username', username)
        .single()

      if (error && error.code !== 'PGRST116') { // PGRST116는 결과가 없는 경우
        throw new Error(error.message)
      }

      return { available: !data }
    } catch (error) {
      return { 
        available: false, 
        error: error instanceof Error ? error.message : '사용자명 확인 중 오류가 발생했습니다.' 
      }
    }
  }

  // 프로필 업데이트
  static async updateProfile(profileData: Partial<UserProfile>): Promise<{ success: boolean; error?: string }> {
    try {
      const { data: { user } } = await supabase.auth.getUser()
      
      if (!user) {
        throw new Error('로그인된 사용자가 없습니다.')
      }

      const { error } = await supabase
        .from('user_profiles')
        .update(profileData)
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
        error: error instanceof Error ? error.message : '비밀번호 재설정 이메일 발송에 실패했습니다.' 
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
        error: error instanceof Error ? error.message : '비밀번호 재설정에 실패했습니다.' 
      }
    }
  }

  // 사용자 프로필 업데이트 (Auth Store에서 사용)
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

  // 이메일 인증 재발송 (Auth Store에서 사용)
  static async resendEmailVerification(): Promise<{ success: boolean; error?: string }> {
    try {
      const { data: { user } } = await supabase.auth.getUser()
      
      if (!user || !user.email) {
        throw new Error('로그인된 사용자 정보를 찾을 수 없습니다.')
      }

      const { error } = await supabase.auth.resend({
        type: 'signup',
        email: user.email
      })

      if (error) {
        throw new Error(error.message)
      }

      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '이메일 인증 재발송에 실패했습니다.' 
      }
    }
  }

  // 인증 상태 변경 구독
  static onAuthStateChange(callback: (user: any) => void) {
    return supabase.auth.onAuthStateChange((event, session) => {
      
      callback(session?.user || null)
    })
  }

  // 파일 업로드 (Supabase Storage)
  static async uploadFile(file: File, bucket: string = 'business-licenses'): Promise<{ success: boolean; url?: string; error?: string }> {
    try {
      const { data: { user } } = await supabase.auth.getUser()
      
      if (!user) {
        throw new Error('로그인된 사용자가 없습니다.')
      }

      const fileName = `${user.id}/${Date.now()}_${file.name}`
      
      const { data, error } = await supabase.storage
        .from(bucket)
        .upload(fileName, file)

      if (error) {
        throw new Error(error.message)
      }

      // 파일 URL 생성
      const { data: urlData } = supabase.storage
        .from(bucket)
        .getPublicUrl(fileName)

      return { success: true, url: urlData.publicUrl }
    } catch (error) {
      return { 
        success: false, 
        error: error instanceof Error ? error.message : '파일 업로드에 실패했습니다.' 
      }
    }
  }
}

