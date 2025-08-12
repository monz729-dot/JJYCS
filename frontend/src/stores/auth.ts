import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { authApi } from '@/services/authApi'
import type { User, LoginRequest, RegisterRequest, AuthResponse } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('auth_token'))
  const isInitialized = ref(false)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!user.value && !!token.value)
  const userRole = computed(() => user.value?.role || null)
  const userStatus = computed(() => user.value?.status || null)
  const isPending = computed(() => userStatus.value === 'pending_approval')
  const isActive = computed(() => userStatus.value === 'active')
  const isSuspended = computed(() => userStatus.value === 'suspended')
  const canCreateOrder = computed(() => 
    isActive.value && ['individual', 'enterprise'].includes(userRole.value || '')
  )

  // Actions
  const initialize = async () => {
    if (isInitialized.value) return

    try {
      if (token.value) {
        await getCurrentUser()
      }
    } catch (error) {
      console.error('Failed to initialize auth:', error)
      await logout()
    } finally {
      isInitialized.value = true
    }
  }

  const login = async (credentials: LoginRequest): Promise<AuthResponse> => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.login(credentials)
      
      if (response.success && response.data) {
        token.value = response.data.token
        user.value = response.data.user
        
        localStorage.setItem('auth_token', response.data.token)
        localStorage.setItem('current_user_id', response.data.user.id)
        
        // Setup axios default header
        setAuthHeader(response.data.token)
      }

      return response
    } catch (err: any) {
      error.value = err.message || 'Login failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const register = async (userData: RegisterRequest): Promise<AuthResponse> => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.register(userData)
      
      if (response.success && response.data) {
        token.value = response.data.token
        user.value = response.data.user
        
        localStorage.setItem('auth_token', response.data.token)
        localStorage.setItem('current_user_id', response.data.user.id)
        setAuthHeader(response.data.token)
      }

      return response
    } catch (err: any) {
      error.value = err.message || 'Registration failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const logout = async () => {
    isLoading.value = true
    
    try {
      if (token.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('Logout API error:', error)
    } finally {
      // Clear state regardless of API success
      user.value = null
      token.value = null
      error.value = null
      
      localStorage.removeItem('auth_token')
      localStorage.removeItem('current_user_id')
      removeAuthHeader()
      
      isLoading.value = false
    }
  }

  const getCurrentUser = async () => {
    if (!token.value) throw new Error('No token available')

    try {
      const response = await authApi.getCurrentUser()
      
      if (response.success && response.data) {
        user.value = response.data
      } else {
        throw new Error('Failed to get current user')
      }
    } catch (error) {
      console.error('Get current user error:', error)
      throw error
    }
  }

  const updateProfile = async (profileData: Partial<User>) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.updateProfile(profileData)
      
      if (response.success && response.data) {
        user.value = { ...user.value, ...response.data }
      }

      return response
    } catch (err: any) {
      error.value = err.message || 'Profile update failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const changePassword = async (currentPassword: string, newPassword: string) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.changePassword(currentPassword, newPassword)
      return response
    } catch (err: any) {
      error.value = err.message || 'Password change failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const requestPasswordReset = async (email: string) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.requestPasswordReset(email)
      return response
    } catch (err: any) {
      error.value = err.message || 'Password reset request failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const resetPassword = async (token: string, newPassword: string) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.resetPassword(token, newPassword)
      return response
    } catch (err: any) {
      error.value = err.message || 'Password reset failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const verifyEmail = async (token: string) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.verifyEmail(token)
      
      if (response.success && user.value) {
        user.value.emailVerified = true
      }

      return response
    } catch (err: any) {
      error.value = err.message || 'Email verification failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const enable2FA = async () => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.enable2FA()
      return response
    } catch (err: any) {
      error.value = err.message || '2FA setup failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const disable2FA = async (code: string) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.disable2FA(code)
      
      if (response.success && user.value) {
        user.value.twoFactorEnabled = false
      }

      return response
    } catch (err: any) {
      error.value = err.message || '2FA disable failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const verify2FA = async (code: string) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.verify2FA(code)
      
      if (response.success && response.data) {
        token.value = response.data.token
        user.value = response.data.user
        
        localStorage.setItem('auth_token', response.data.token)
        setAuthHeader(response.data.token)
      }

      return response
    } catch (err: any) {
      error.value = err.message || '2FA verification failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const checkAuth = async () => {
    if (!token.value) return false

    try {
      await getCurrentUser()
      return true
    } catch (error) {
      await logout()
      return false
    }
  }

  const clearError = () => {
    error.value = null
  }

  // Utility functions
  const setAuthHeader = (authToken: string) => {
    // This will be set up when we configure axios
    if (typeof window !== 'undefined' && (window as any).axios) {
      (window as any).axios.defaults.headers.common['Authorization'] = `Bearer ${authToken}`
    }
  }

  const removeAuthHeader = () => {
    if (typeof window !== 'undefined' && (window as any).axios) {
      delete (window as any).axios.defaults.headers.common['Authorization']
    }
  }

  const hasRole = (role: string | string[]) => {
    if (!userRole.value) return false
    
    if (Array.isArray(role)) {
      return role.includes(userRole.value)
    }
    
    return userRole.value === role
  }

  const hasAnyRole = (roles: string[]) => {
    return roles.some(role => hasRole(role))
  }

  const canAccess = (route: any) => {
    // Check authentication requirement
    if (route.meta?.requiresAuth && !isAuthenticated.value) {
      return false
    }

    // Check role requirement
    if (route.meta?.requiresRole && !hasAnyRole(
      Array.isArray(route.meta.requiresRole) 
        ? route.meta.requiresRole 
        : [route.meta.requiresRole]
    )) {
      return false
    }

    // Check status requirements
    if (isPending.value && route.name !== 'Approval') {
      const allowedRoutes = ['Approval', 'Profile', 'ProfileSecurity']
      return allowedRoutes.includes(route.name)
    }

    if (isSuspended.value) {
      return false
    }

    return true
  }

  // Admin functions
  const getAllUsers = async () => {
    try {
      const response = await authApi.getAllUsers()
      if (response.success && response.data) {
        return response.data
      }
      return []
    } catch (error) {
      console.error('Failed to get all users:', error)
      return []
    }
  }

  const approveUser = async (userId: string) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.approveUser(userId)
      return response
    } catch (err: any) {
      error.value = err.message || 'User approval failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const rejectUser = async (userId: string) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.rejectUser(userId)
      return response
    } catch (err: any) {
      error.value = err.message || 'User rejection failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  return {
    // State
    user: readonly(user),
    token: readonly(token),
    isInitialized: readonly(isInitialized),
    isLoading: readonly(isLoading),
    error: readonly(error),
    
    // Getters
    isAuthenticated,
    userRole,
    userStatus,
    isPending,
    isActive,
    isSuspended,
    canCreateOrder,
    
    // Actions
    initialize,
    login,
    register,
    logout,
    getCurrentUser,
    updateProfile,
    changePassword,
    requestPasswordReset,
    resetPassword,
    verifyEmail,
    enable2FA,
    disable2FA,
    verify2FA,
    checkAuth,
    clearError,
    
    // Utilities
    hasRole,
    hasAnyRole,
    canAccess,
    
    // Admin functions
    getAllUsers,
    approveUser,
    rejectUser
  }
})