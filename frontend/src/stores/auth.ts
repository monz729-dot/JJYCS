import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { AuthService } from '@/services/authService'
import type { UserProfile, SignUpData, LoginData } from '@/lib/supabase'

export const useAuthStore = defineStore('auth', () => {
  // 상태
  const user = ref<UserProfile | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 계산된 속성
  const isAuthenticated = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.user_type === 'admin')
  const isApproved = computed(() => user.value?.approval_status === 'approved')
  const isPending = computed(() => user.value?.approval_status === 'pending')
  const isRejected = computed(() => user.value?.approval_status === 'rejected')
  const userType = computed(() => user.value?.user_type)

  // 액션
  const signUp = async (data: SignUpData) => {
    loading.value = true
    error.value = null
    
    try {
      const result = await AuthService.signUp(data)
      
      if (result.success) {
        // 회원가입 성공 시 이메일 인증 안내
        return { success: true, message: '회원가입이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요.' }
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
      const result = await AuthService.signIn(data)
      
      if (result.success) {
        await fetchUserProfile()
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
      const result = await AuthService.signOut()
      
      if (result.success) {
        user.value = null
        return { success: true }
      } else {
        error.value = result.error || '로그아웃에 실패했습니다.'
        return { success: false, error: error.value }
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : '로그아웃 중 오류가 발생했습니다.'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const fetchUserProfile = async () => {
    try {
      const result = await AuthService.getCurrentUserProfile()
      
      if (result.data) {
        user.value = result.data
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
      const result = await AuthService.updateUserProfile(updates)
      
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
      const result = await AuthService.checkUsernameAvailability(username)
      return result
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
      const result = await AuthService.resendEmailVerification()
      
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
      const result = await AuthService.sendPasswordResetEmail(email)
      
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

  const resetPassword = async (newPassword: string) => {
    loading.value = true
    error.value = null
    
    try {
      const result = await AuthService.resetPassword(newPassword)
      
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

  const clearError = () => {
    error.value = null
  }

  const initializeAuth = () => {
    // 인증 상태 변경 구독
    AuthService.onAuthStateChange((authUser) => {
      if (authUser) {
        fetchUserProfile()
      } else {
        user.value = null
      }
    })
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
    
    // 액션
    signUp,
    signIn,
    signOut,
    fetchUserProfile,
    updateProfile,
    checkUsernameAvailability,
    resendEmailVerification,
    sendPasswordResetEmail,
    resetPassword,
    clearError,
    initializeAuth
  }
})