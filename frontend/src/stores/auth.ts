import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import SpringBootAuthService, { 
  type SignUpRequest, 
  type LoginRequest, 
  type AuthResponse, 
  type UserProfile 
} from '@/services/authApiService'

interface AuthState {
  user: UserProfile | null
  accessToken: string | null
  refreshToken: string | null
  loading: boolean
  error: string | null
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<UserProfile | null>(null)
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => {
    return !!accessToken.value && !!user.value
  })

  const userType = computed(() => {
    return user.value?.role || null
  })

  const userStatus = computed(() => {
    return user.value?.status || null
  })

  const isEmailVerified = computed(() => {
    return user.value?.emailVerified || false
  })

  const isPendingApproval = computed(() => {
    return user.value?.status === 'pending_approval'
  })

  const isActive = computed(() => {
    return user.value?.status === 'active'
  })

  const isTwoFactorEnabled = computed(() => {
    return user.value?.twoFactorEnabled || false
  })

  const hasRole = computed(() => (role: string) => {
    return user.value?.role === role
  })

  const canAccessRoute = computed(() => (requiredRoles?: string[]) => {
    if (!requiredRoles || requiredRoles.length === 0) return true
    if (!user.value?.role) return false
    return requiredRoles.includes(user.value.role)
  })

  const statusMessage = computed(() => {
    if (!user.value) return ''
    
    switch (user.value.status) {
      case 'pending_approval':
        return '계정 승인 대기 중입니다. 승인까지 평일 1-2일이 소요됩니다.'
      case 'active':
        return user.value.emailVerified ? '' : '이메일 인증을 완료해주세요.'
      case 'suspended':
        return '계정이 일시 정지되었습니다. 관리자에게 문의하세요.'
      case 'deactivated':
        return '비활성화된 계정입니다.'
      default:
        return ''
    }
  })

  const dashboardPath = computed(() => {
    if (!user.value) return '/auth/login'
    
    switch (user.value.role) {
      case 'admin':
        return '/app/admin'
      case 'warehouse':
        return '/app/warehouse'
      case 'partner':
        return '/app/partner'
      default:
        return '/app/dashboard'
    }
  })

  // Actions
  const initializeAuth = async () => {
    try {
      // 로컬 스토리지에서 토큰과 사용자 정보 복원
      const storedToken = localStorage.getItem('accessToken')
      const storedRefreshToken = localStorage.getItem('refreshToken')
      const storedUser = localStorage.getItem('user')

      if (storedToken && storedUser) {
        accessToken.value = storedToken
        refreshToken.value = storedRefreshToken
        user.value = JSON.parse(storedUser)

        // 서버에서 최신 사용자 정보 가져오기
        await fetchUserProfile()
      }
    } catch (error) {
      console.error('Auth initialization failed:', error)
      clearAuth()
    }
  }

  const signUp = async (signUpData: SignUpRequest) => {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootAuthService.signUp(signUpData)
      
      if (result.success && result.data) {
        setAuthData(result.data)
        return { success: true, data: result.data }
      } else {
        error.value = result.error || '회원가입에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || '회원가입 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const signIn = async (loginData: LoginRequest) => {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootAuthService.signIn(loginData)
      
      if (result.success && result.data) {
        setAuthData(result.data)
        return { success: true, data: result.data }
      } else {
        error.value = result.error || '로그인에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || '로그인 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const signOut = async () => {
    loading.value = true

    try {
      await SpringBootAuthService.signOut()
    } catch (err) {
      console.error('Sign out error:', err)
    } finally {
      clearAuth()
      loading.value = false
    }
  }

  const fetchUserProfile = async () => {
    try {
      const result = await SpringBootAuthService.getCurrentUserProfile()
      
      if (result.data) {
        user.value = result.data
        return { success: true, data: result.data }
      } else {
        throw new Error(result.error || 'Failed to fetch user profile')
      }
    } catch (err: any) {
      console.error('Fetch user profile error:', err)
      // 프로필 조회 실패시 로그아웃
      clearAuth()
      throw err
    }
  }

  const updateProfile = async (profileData: { name?: string; phone?: string }) => {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootAuthService.updateProfile(profileData)
      
      if (result.success) {
        // 업데이트된 프로필 정보 다시 가져오기
        await fetchUserProfile()
        return { success: true }
      } else {
        error.value = result.error || '프로필 업데이트에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || '프로필 업데이트 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const verifyEmail = async (token: string) => {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootAuthService.verifyEmail(token)
      
      if (result.success) {
        // 이메일 인증 후 사용자 정보 업데이트
        await fetchUserProfile()
        return { success: true }
      } else {
        error.value = result.error || '이메일 인증에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || '이메일 인증 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const resendEmailVerification = async () => {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootAuthService.resendEmailVerification()
      
      if (result.success) {
        return { success: true }
      } else {
        error.value = result.error || '이메일 재발송에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || '이메일 재발송 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const sendPasswordResetEmail = async (email: string) => {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootAuthService.requestPasswordReset(email)
      
      if (result.success) {
        return { success: true }
      } else {
        error.value = result.error || '비밀번호 재설정 이메일 발송에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || '비밀번호 재설정 이메일 발송 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const resetPassword = async (token: string, newPassword: string) => {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootAuthService.resetPassword(token, newPassword)
      
      if (result.success) {
        return { success: true }
      } else {
        error.value = result.error || '비밀번호 재설정에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || '비밀번호 재설정 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const changePassword = async (currentPassword: string, newPassword: string) => {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootAuthService.changePassword(currentPassword, newPassword)
      
      if (result.success) {
        return { success: true }
      } else {
        error.value = result.error || '비밀번호 변경에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || '비밀번호 변경 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const setup2FA = async () => {
    loading.value = true
    error.value = null

    try {
      const result = await SpringBootAuthService.setup2FA()
      
      if (result.success) {
        return { success: true, data: result.data }
      } else {
        error.value = result.error || '2FA 설정에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || '2FA 설정 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const refreshAccessToken = async () => {
    try {
      const result = await SpringBootAuthService.refreshToken()
      
      if (result.success && result.data) {
        accessToken.value = result.data.accessToken
        if (result.data.refreshToken) {
          refreshToken.value = result.data.refreshToken
        }
        return { success: true }
      } else {
        throw new Error(result.error || 'Token refresh failed')
      }
    } catch (err: any) {
      console.error('Token refresh error:', err)
      clearAuth()
      throw err
    }
  }

  // Helper functions
  const setAuthData = (authData: AuthResponse) => {
    user.value = authData.user
    accessToken.value = authData.accessToken
    refreshToken.value = authData.refreshToken

    // 로컬 스토리지에 저장
    localStorage.setItem('accessToken', authData.accessToken)
    if (authData.refreshToken) {
      localStorage.setItem('refreshToken', authData.refreshToken)
    }
    localStorage.setItem('user', JSON.stringify(authData.user))
  }

  const clearAuth = () => {
    user.value = null
    accessToken.value = null
    refreshToken.value = null
    error.value = null

    // 로컬 스토리지 클리어
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  const clearError = () => {
    error.value = null
  }

  // 초기화
  initializeAuth()

  return {
    // State
    user,
    accessToken,
    refreshToken,
    loading,
    error,

    // Getters
    isAuthenticated,
    userType,
    userStatus,
    isEmailVerified,
    isPendingApproval,
    isActive,
    isTwoFactorEnabled,
    hasRole,
    canAccessRoute,
    statusMessage,
    dashboardPath,

    // Actions
    signUp,
    signIn,
    signOut,
    fetchUserProfile,
    updateProfile,
    verifyEmail,
    resendEmailVerification,
    sendPasswordResetEmail,
    resetPassword,
    changePassword,
    setup2FA,
    refreshAccessToken,
    clearAuth,
    clearError
  }
})

export default useAuthStore