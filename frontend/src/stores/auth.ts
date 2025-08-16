import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ApiService } from '@/services/apiService'
import type { UserProfile, SignUpData, LoginData } from '@/lib/supabase'

// Backend API 타입 정의
interface BackendUser {
  id: number
  email: string
  name: string
  phone: string
  role: string
  status: string
  memberCode: string
  emailVerified: boolean
  twoFactorEnabled: boolean
  createdAt: string
}

interface BackendAuthResponse {
  accessToken: string
  tokenType: string
  user: BackendUser
  requiresEmailVerification?: boolean
  requiresApproval?: boolean
  message?: string
}

export const useAuthStore = defineStore('auth', () => {
  // 상태
  const user = ref<UserProfile | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 계산된 속성
  const isAuthenticated = computed(() => !!user.value && !!localStorage.getItem('access_token'))
  const isAdmin = computed(() => user.value?.user_type === 'admin' || user.value?.role === 'ADMIN')
  const isApproved = computed(() => {
    if (user.value?.approval_status) {
      return user.value.approval_status === 'approved'
    }
    // Backend 응답의 경우 status 필드 사용
    return user.value?.status === 'ACTIVE'
  })
  const isPending = computed(() => {
    if (user.value?.approval_status) {
      return user.value.approval_status === 'pending'
    }
    return user.value?.status === 'PENDING_APPROVAL'
  })
  const isRejected = computed(() => {
    if (user.value?.approval_status) {
      return user.value.approval_status === 'rejected'
    }
    return user.value?.status === 'SUSPENDED' || user.value?.status === 'INACTIVE'
  })
  const userType = computed(() => {
    if (user.value?.user_type) {
      return user.value.user_type
    }
    // Backend role을 frontend user_type으로 변환
    const roleMap: { [key: string]: string } = {
      'ADMIN': 'admin',
      'INDIVIDUAL': 'general',
      'ENTERPRISE': 'corporate',
      'PARTNER': 'partner',
      'WAREHOUSE': 'warehouse'
    }
    return user.value?.role ? roleMap[user.value.role] || 'general' : null
  })
  
  // 역할 확인 함수
  const hasRole = (role: string) => {
    return userType.value === role
  }
  
  const hasAnyRole = (roles: string[]) => {
    return userType.value && roles.includes(userType.value)
  }

  // 액션
  const signUp = async (data: SignUpData) => {
    loading.value = true
    error.value = null
    
    try {
      // SignUpData를 백엔드 형식으로 변환
      const backendSignupData = {
        email: data.email,
        password: data.password,
        name: data.name,
        phone: data.phone || '',
        role: data.user_type === 'general' ? 'INDIVIDUAL' : 
              data.user_type === 'corporate' ? 'ENTERPRISE' : 
              data.user_type === 'partner' ? 'PARTNER' : 'INDIVIDUAL',
        memberCode: data.username, // 백엔드에서는 memberCode로 사용
        agreeTerms: data.terms_agreed || false,
        agreePrivacy: data.privacy_agreed || false,
        agreeMarketing: false
      }
      
      const result = await ApiService.post<BackendAuthResponse>('/auth/signup', backendSignupData)
      
      if (result.success && result.data) {
        // JWT 토큰 저장
        localStorage.setItem('access_token', result.data.accessToken)
        
        // 사용자 정보를 frontend 형식으로 변환해서 저장
        user.value = {
          id: result.data.user.id.toString(),
          email: result.data.user.email,
          name: result.data.user.name,
          phone: result.data.user.phone,
          user_type: data.user_type,
          role: result.data.user.role,
          status: result.data.user.status,
          memberCode: result.data.user.memberCode,
          email_verified: result.data.user.emailVerified,
          approval_status: result.data.user.status === 'ACTIVE' ? 'approved' : 
                          result.data.user.status === 'PENDING_APPROVAL' ? 'pending' : 'rejected',
          created_at: result.data.user.createdAt
        } as UserProfile
        
        const userTypeText = data.user_type === 'general' ? '일반회원' : 
                           data.user_type === 'corporate' ? '기업회원' : '파트너회원'
        
        return { 
          success: true, 
          requiresEmailVerification: result.data.requiresEmailVerification,
          userType: data.user_type,
          message: result.data.message || `${userTypeText} 회원가입이 완료되었습니다.`
        }
      } else {
        error.value = result.error || '회원가입에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '회원가입 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const signIn = async (data: LoginData) => {
    loading.value = true
    error.value = null
    
    try {
      const loginData = {
        email: data.email,
        password: data.password
      }
      
      const result = await ApiService.post<BackendAuthResponse>('/auth/login', loginData)
      
      if (result.success && result.data) {
        // JWT 토큰 저장
        localStorage.setItem('access_token', result.data.accessToken)
        
        // 사용자 정보를 frontend 형식으로 변환해서 저장
        user.value = {
          id: result.data.user.id.toString(),
          email: result.data.user.email,
          name: result.data.user.name,
          phone: result.data.user.phone,
          role: result.data.user.role,
          status: result.data.user.status,
          memberCode: result.data.user.memberCode,
          email_verified: result.data.user.emailVerified,
          approval_status: result.data.user.status === 'ACTIVE' ? 'approved' : 
                          result.data.user.status === 'PENDING_APPROVAL' ? 'pending' : 'rejected',
          created_at: result.data.user.createdAt,
          // user_type 매핑
          user_type: result.data.user.role === 'ADMIN' ? 'admin' :
                    result.data.user.role === 'INDIVIDUAL' ? 'general' :
                    result.data.user.role === 'ENTERPRISE' ? 'corporate' :
                    result.data.user.role === 'PARTNER' ? 'partner' : 'general'
        } as UserProfile
        
        return { success: true }
      } else {
        error.value = result.error || '로그인에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '로그인 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const signOut = async () => {
    loading.value = true
    error.value = null
    
    try {
      // 백엔드 로그아웃 호출 (선택사항)
      await ApiService.post('/auth/logout', {})
      
      // 로컬 토큰 제거
      localStorage.removeItem('access_token')
      user.value = null
      
      return { success: true }
    } catch (err) {
      // 로그아웃은 실패해도 로컬 상태는 정리
      localStorage.removeItem('access_token')
      user.value = null
      
      error.value = err instanceof Error ? err.message : '로그아웃 중 오류가 발생했습니다.'
      return { success: true } // 로컬 정리는 성공
    } finally {
      loading.value = false
    }
  }

  const fetchUserProfile = async () => {
    try {
      const token = localStorage.getItem('access_token')
      if (!token) {
        user.value = null
        return { success: false, error: '로그인이 필요합니다.' }
      }

      const result = await ApiService.get<BackendUser>('/auth/me')
      
      if (result.success && result.data) {
        user.value = {
          id: result.data.id.toString(),
          email: result.data.email,
          name: result.data.name,
          phone: result.data.phone,
          role: result.data.role,
          status: result.data.status,
          memberCode: result.data.memberCode,
          email_verified: result.data.emailVerified,
          approval_status: result.data.status === 'ACTIVE' ? 'approved' : 
                          result.data.status === 'PENDING_APPROVAL' ? 'pending' : 'rejected',
          created_at: result.data.createdAt,
          user_type: result.data.role === 'ADMIN' ? 'admin' :
                    result.data.role === 'INDIVIDUAL' ? 'general' :
                    result.data.role === 'ENTERPRISE' ? 'corporate' :
                    result.data.role === 'PARTNER' ? 'partner' : 'general'
        } as UserProfile
        
        return { success: true }
      } else {
        error.value = result.error || '사용자 정보를 가져올 수 없습니다.'
        return { success: false, error: error.value }
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '사용자 정보 조회 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    }
  }

  const updateProfile = async (updates: Partial<UserProfile>) => {
    loading.value = true
    error.value = null
    
    try {
      const result = await ApiService.put<BackendUser>('/auth/profile', updates)
      
      if (result.success) {
        await fetchUserProfile() // 업데이트된 정보 다시 가져오기
        return { success: true }
      } else {
        error.value = result.error || '프로필 업데이트에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '프로필 업데이트 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const checkUsernameAvailability = async (username: string) => {
    try {
      const result = await ApiService.get<{ available: boolean }>(`/auth/check-username?username=${encodeURIComponent(username)}`)
      
      if (result.success && result.data) {
        return { available: result.data.available }
      } else {
        return { 
          available: false, 
          error: result.error || '아이디 중복 확인에 실패했습니다.' 
        }
      }
    } catch (err) {
      return { 
        available: false, 
        error: err instanceof Error ? err.message : '아이디 중복 확인 중 오류가 발생했습니다.' 
      }
    }
  }

  const resendEmailVerification = async () => {
    loading.value = true
    error.value = null
    
    try {
      const result = await ApiService.post('/auth/resend-verification', {})
      
      if (result.success) {
        return { success: true, message: '이메일 인증 메일을 재발송했습니다.' }
      } else {
        error.value = result.error || '이메일 인증 재발송에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '이메일 인증 재발송 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const sendPasswordResetEmail = async (email: string) => {
    loading.value = true
    error.value = null
    
    try {
      const result = await ApiService.post('/auth/password-reset', { email })
      
      if (result.success) {
        return { success: true, message: '비밀번호 재설정 이메일을 발송했습니다.' }
      } else {
        error.value = result.error || '비밀번호 재설정 이메일 발송에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '비밀번호 재설정 이메일 발송 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const resetPassword = async (newPassword: string, token?: string) => {
    loading.value = true
    error.value = null
    
    try {
      const result = await ApiService.post('/auth/reset-password', { newPassword, token })
      
      if (result.success) {
        return { success: true, message: '비밀번호가 성공적으로 변경되었습니다.' }
      } else {
        error.value = result.error || '비밀번호 재설정에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '비밀번호 재설정 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const handleEmailVerification = async (token?: string) => {
    loading.value = true
    error.value = null
    
    try {
      const result = await ApiService.post('/auth/verify-email', { token })
      
      if (result.success) {
        await fetchUserProfile() // 사용자 정보 다시 가져오기
        return { success: true, message: '이메일 인증이 완료되었습니다.' }
      } else {
        error.value = result.error || '이메일 인증 처리에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '이메일 인증 처리 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const clearError = () => {
    error.value = null
  }

  const initializeAuth = () => {
    // 토큰 존재 여부 확인하여 사용자 정보 로드
    const token = localStorage.getItem('access_token')
    if (token) {
      fetchUserProfile().catch(() => {
        // 토큰이 유효하지 않으면 제거
        localStorage.removeItem('access_token')
        user.value = null
      })
    }
  }

  return {
    // 상태
    user,
    loading,
    error,
    
    // 계산된 속성
    isAuthenticated,
    isAdmin,
    isApproved,
    isPending,
    isRejected,
    userType,
    
    // 역할 확인 함수
    hasRole,
    hasAnyRole,
    
    // 액션
    signUp,
    signIn,
    signOut,
    logout: signOut, // alias for logout
    fetchUserProfile,
    updateProfile,
    checkUsernameAvailability,
    resendEmailVerification,
    sendPasswordResetEmail,
    resetPassword,
    handleEmailVerification,
    clearError,
    initializeAuth
  }
})