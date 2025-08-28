import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { USER_TYPE } from '../types'
import type { User, UserType, LoginForm, RegisterForm } from '../types'
import { authApi } from '../utils/api'
import { tokenStorage } from '../utils/tokenStorage'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const refreshTokenValue = ref<string | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.userType === USER_TYPE.ADMIN)
  const isPartner = computed(() => user.value?.userType === USER_TYPE.PARTNER)
  const isWarehouse = computed(() => user.value?.userType === USER_TYPE.WAREHOUSE)
  const isCorporate = computed(() => user.value?.userType === USER_TYPE.CORPORATE)

  // Actions
  const initAuth = async () => {
    const savedToken = tokenStorage.getToken()
    const savedRefreshToken = tokenStorage.getRefreshToken()
    const savedUser = localStorage.getItem('user') // Keep user data in localStorage for all environments
    
    if (savedToken) {
      token.value = savedToken
      refreshTokenValue.value = savedRefreshToken
      
      // Try to hydrate user info from server
      const userInitialized = await initializeUser()
      
      // If server call failed, fall back to cached user data
      if (!userInitialized && savedUser) {
        try {
          user.value = JSON.parse(savedUser)
        } catch (e) {
          clearAuth()
        }
      }
    }
  }

  const login = async (credentials: LoginForm) => {
    loading.value = true
    error.value = null

    try {
      const response = await authApi.login(credentials.email, credentials.password)
      
      if (response.success && response.data) {
        // response.data가 백엔드의 실제 응답을 포함함
        const loginData = response.data
        
        if (loginData.success) {
          token.value = loginData.token || loginData.accessToken || 'mock-jwt-token'
          refreshTokenValue.value = loginData.refreshToken
          user.value = loginData.user
          
          tokenStorage.setToken(token.value)
          if (refreshTokenValue.value) {
            tokenStorage.setRefreshToken(refreshTokenValue.value)
          }
          localStorage.setItem('user', JSON.stringify(user.value))
          
          if (credentials.rememberMe) {
            localStorage.setItem('remember_me', 'true')
          }
          
          return { success: true }
        } else {
          error.value = loginData.error || 'Login failed'
          return { success: false, error: error.value }
        }
      } else {
        error.value = response.error || 'Login failed'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Login failed'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const register = async (data: RegisterForm) => {
    loading.value = true
    error.value = null

    try {
      const response = await authApi.register(data)
      
      if (response.success) {
        return { 
          success: true, 
          message: response.message || 'Registration successful. Please check your email for verification.' 
        }
      } else {
        error.value = response.error || 'Registration failed'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Registration failed'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const logout = async () => {
    try {
      await authApi.logout()
    } catch (err) {
      console.warn('Logout API call failed:', err)
    } finally {
      clearAuth()
    }
  }

  const clearAuth = () => {
    user.value = null
    token.value = null
    refreshTokenValue.value = null
    error.value = null
    tokenStorage.removeTokens()
    localStorage.removeItem('user')
    localStorage.removeItem('remember_me')
  }

  const verifyEmail = async (verificationToken: string) => {
    loading.value = true
    error.value = null

    try {
      const response = await authApi.verifyEmail(verificationToken)
      
      if (response.success) {
        return { success: true, message: 'Email verified successfully' }
      } else {
        error.value = response.error || 'Email verification failed'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Email verification failed'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const forgotPassword = async (email: string) => {
    loading.value = true
    error.value = null

    try {
      const response = await authApi.forgotPassword(email)
      
      if (response.success) {
        return { 
          success: true, 
          message: 'Password reset link sent to your email' 
        }
      } else {
        error.value = response.error || 'Password reset request failed'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Password reset request failed'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const resetPassword = async (resetToken: string, password: string) => {
    loading.value = true
    error.value = null

    try {
      const response = await authApi.resetPassword(resetToken, password)
      
      if (response.success) {
        return { success: true, message: 'Password reset successful' }
      } else {
        error.value = response.error || 'Password reset failed'
        return { success: false, error: error.value }
      }
    } catch (err: any) {
      error.value = err.message || 'Password reset failed'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const refreshToken = async () => {
    if (!refreshTokenValue.value) {
      return false
    }

    try {
      const response = await authApi.refreshToken(refreshTokenValue.value)
      
      if (response.success && response.data) {
        token.value = response.data.accessToken || response.data.token
        
        tokenStorage.setToken(response.data.accessToken || response.data.token)
        
        return true
      }
    } catch (err) {
      console.warn('Token refresh failed:', err)
      clearAuth()
    }
    
    return false
  }

  const initializeUser = async () => {
    if (!token.value) {
      return false
    }

    try {
      const response = await authApi.getCurrentUser()
      
      if (response.success && response.data) {
        const userData = response.data
        if (userData.success) {
          user.value = userData.user
          localStorage.setItem('user', JSON.stringify(userData.user))
          return true
        }
      }
    } catch (err) {
      console.warn('Failed to initialize user:', err)
      clearAuth()
    }
    
    return false
  }

  const updateUserInfo = (updatedUser: User) => {
    user.value = updatedUser
    localStorage.setItem('user', JSON.stringify(updatedUser))
  }

  const clearError = () => {
    error.value = null
  }

  // Initialize auth on store creation
  initAuth()

  return {
    // State
    user,
    token,
    loading,
    error,
    
    // Getters
    isAuthenticated,
    isAdmin,
    isPartner,
    isWarehouse,
    isCorporate,
    
    // Actions
    login,
    register,
    logout,
    verifyEmail,
    forgotPassword,
    resetPassword,
    refreshToken,
    initializeUser,
    updateUserInfo,
    clearError,
    clearAuth
  }
})