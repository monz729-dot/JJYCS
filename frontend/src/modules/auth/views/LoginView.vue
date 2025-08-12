<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div>
        <div class="mx-auto h-12 w-12 text-center">
          <svg class="w-12 h-12 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 0C4.477 0 0 4.477 0 10s4.477 10 10 10 10-4.477 10-10S15.523 0 10 0zM8 5a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1zm1 3a1 1 0 100 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
          </svg>
        </div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          {{ $t('auth.login.title') }}
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          {{ $t('auth.login.subtitle') }}
          <router-link 
            to="/auth/register" 
            class="font-medium text-blue-600 hover:text-blue-500 ml-1"
          >
            {{ $t('auth.login.signup_link') }}
          </router-link>
        </p>
      </div>

      <!-- Login Form -->
      <form class="mt-8 space-y-6" @submit.prevent="handleSubmit">
        <div class="rounded-md shadow-sm -space-y-px">
          <!-- Email Field -->
          <div>
            <label for="email" class="sr-only">{{ $t('auth.login.email') }}</label>
            <input
              id="email"
              v-model="form.email"
              name="email"
              type="email"
              autocomplete="email"
              required
              class="appearance-none rounded-none relative block w-full px-3 py-2 border placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
              :class="{
                'border-red-300': errors.email,
                'border-gray-300': !errors.email
              }"
              :placeholder="$t('auth.login.email_placeholder')"
              @blur="validateField('email')"
            />
            <p v-if="errors.email" class="mt-1 text-sm text-red-600">
              {{ errors.email }}
            </p>
          </div>

          <!-- Password Field -->
          <div>
            <label for="password" class="sr-only">{{ $t('auth.login.password') }}</label>
            <div class="relative">
              <input
                id="password"
                v-model="form.password"
                name="password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                required
                class="appearance-none rounded-none relative block w-full px-3 py-2 pr-10 border placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                :class="{
                  'border-red-300': errors.password,
                  'border-gray-300': !errors.password
                }"
                :placeholder="$t('auth.login.password_placeholder')"
                @blur="validateField('password')"
              />
              <button
                type="button"
                class="absolute inset-y-0 right-0 pr-3 flex items-center"
                @click="showPassword = !showPassword"
              >
                <svg 
                  class="h-5 w-5 text-gray-400" 
                  :class="{ 'text-blue-500': showPassword }"
                  fill="none" 
                  viewBox="0 0 24 24" 
                  stroke="currentColor"
                >
                  <path 
                    v-if="!showPassword"
                    stroke-linecap="round" 
                    stroke-linejoin="round" 
                    stroke-width="2" 
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" 
                  />
                  <path 
                    v-if="!showPassword"
                    stroke-linecap="round" 
                    stroke-linejoin="round" 
                    stroke-width="2" 
                    d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" 
                  />
                  <path 
                    v-else
                    stroke-linecap="round" 
                    stroke-linejoin="round" 
                    stroke-width="2" 
                    d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L8.464 8.464m1.414 1.414l-1.414-1.414m4.242 4.242l1.414 1.414M14.12 14.12l1.415 1.415" 
                  />
                </svg>
              </button>
            </div>
            <p v-if="errors.password" class="mt-1 text-sm text-red-600">
              {{ errors.password }}
            </p>
          </div>
        </div>

        <!-- Remember & Forgot Password -->
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <input
              id="remember-me"
              v-model="form.rememberMe"
              name="remember-me"
              type="checkbox"
              class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label for="remember-me" class="ml-2 block text-sm text-gray-900">
              {{ $t('auth.login.remember_me') }}
            </label>
          </div>

          <div class="text-sm">
            <router-link 
              :to="{ name: 'ForgotPassword' }"
              class="font-medium text-blue-600 hover:text-blue-500"
            >
              {{ $t('auth.login.forgot_password') }}
            </router-link>
          </div>
        </div>

        <!-- Submit Button -->
        <div>
          <button
            type="submit"
            :disabled="isLoading || !isFormValid"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span class="absolute left-0 inset-y-0 flex items-center pl-3">
              <svg 
                v-if="isLoading"
                class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" 
                fill="none" 
                viewBox="0 0 24 24"
              >
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <svg 
                v-else
                class="h-5 w-5 text-blue-500 group-hover:text-blue-400" 
                fill="currentColor" 
                viewBox="0 0 20 20"
              >
                <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
              </svg>
            </span>
            {{ isLoading ? $t('auth.login.signing_in') : $t('auth.login.sign_in') }}
          </button>
        </div>

        <!-- Divider -->
        <div class="relative">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-gray-300" />
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="px-2 bg-gray-50 text-gray-500">{{ $t('auth.login.or') }}</span>
          </div>
        </div>

        <!-- Social Login Buttons -->
        <div class="space-y-3">
          <button
            type="button"
            class="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
            @click="loginWithGoogle"
          >
            <svg class="w-5 h-5 mr-2" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            {{ $t('auth.login.google_login') }}
          </button>

          <button
            type="button"
            class="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
            @click="loginWithNaver"
          >
            <svg class="w-5 h-5 mr-2" viewBox="0 0 24 24" fill="#03C75A">
              <path d="M16.273 12.845 7.376 0H0v24h7.726V11.156L16.624 24H24V0h-7.727v12.845Z"/>
            </svg>
            {{ $t('auth.login.naver_login') }}
          </button>
        </div>
      </form>

      <!-- Error Display -->
      <div v-if="authStore.error" class="rounded-md bg-red-50 p-4 mt-4">
        <div class="flex">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">
              {{ $t('auth.login.error_title') }}
            </h3>
            <div class="mt-2 text-sm text-red-700">
              {{ authStore.error }}
            </div>
          </div>
          <div class="ml-auto pl-3">
            <button
              type="button"
              class="inline-flex rounded-md text-red-400 hover:text-red-600 focus:outline-none focus:ring-2 focus:ring-red-500"
              @click="authStore.clearError"
            >
              <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 2FA Modal -->
      <TwoFactorModal 
        v-if="show2FAModal" 
        @verify="handle2FAVerification"
        @cancel="show2FAModal = false"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import TwoFactorModal from '../components/TwoFactorModal.vue'
import type { LoginRequest } from '@/types/auth'

// Composables
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

// Form state
const form = ref<LoginRequest>({
  email: '',
  password: '',
  rememberMe: false
})

const showPassword = ref(false)
const show2FAModal = ref(false)
const errors = ref<Record<string, string>>({})

// Computed
const isLoading = computed(() => authStore.isLoading)
const isFormValid = computed(() => {
  return form.value.email && 
         form.value.password && 
         !Object.keys(errors.value).length
})

// Validation
const validateField = (field: string) => {
  switch (field) {
    case 'email':
      if (!form.value.email) {
        errors.value.email = 'Email is required'
      } else if (!/\S+@\S+\.\S+/.test(form.value.email)) {
        errors.value.email = 'Please enter a valid email'
      } else {
        delete errors.value.email
      }
      break
    
    case 'password':
      if (!form.value.password) {
        errors.value.password = 'Password is required'
      } else if (form.value.password.length < 6) {
        errors.value.password = 'Password must be at least 6 characters'
      } else {
        delete errors.value.password
      }
      break
  }
}

const validateForm = () => {
  validateField('email')
  validateField('password')
  return Object.keys(errors.value).length === 0
}

// Form submission
const handleSubmit = async () => {
  if (!validateForm()) return

  try {
    const response = await authStore.login(form.value)
    
    if (response.success) {
      // Check if 2FA is required
      if (response.data?.requires2FA) {
        show2FAModal.value = true
        return
      }
      
      // Show success message
      notificationStore.success(
        'Login Successful',
        'Welcome back to YCS LMS!'
      )
      
      // Redirect to intended page or dashboard
      const redirectTo = (route.query.redirect as string) || '/dashboard'
      await router.push(redirectTo)
    }
  } catch (error: any) {
    // Error is already handled by the store
    console.error('Login error:', error)
  }
}

// 2FA handling
const handle2FAVerification = async (code: string) => {
  try {
    const response = await authStore.verify2FA(code)
    
    if (response.success) {
      show2FAModal.value = false
      
      notificationStore.success(
        'Login Successful',
        'Welcome back to YCS LMS!'
      )
      
      const redirectTo = (route.query.redirect as string) || '/dashboard'
      await router.push(redirectTo)
    }
  } catch (error) {
    // Error handling is done in the modal component
    console.error('2FA verification error:', error)
  }
}

// Social login methods
const loginWithGoogle = () => {
  // Redirect to Google OAuth endpoint
  window.location.href = '/api/auth/google'
}

const loginWithNaver = () => {
  // Redirect to Naver OAuth endpoint
  window.location.href = '/api/auth/naver'
}

// Lifecycle
onMounted(() => {
  // Clear any existing errors when component mounts
  authStore.clearError()
  
  // Check for OAuth callback parameters
  const { code, state, error } = route.query
  if (code && state) {
    // Handle OAuth callback
    handleOAuthCallback(code as string, state as string, error as string)
  }
})

const handleOAuthCallback = async (code: string, state: string, error?: string) => {
  if (error) {
    notificationStore.error('Login Failed', `OAuth error: ${error}`)
    return
  }
  
  try {
    // Make API call to exchange code for tokens
    const response = await fetch('/api/auth/oauth/callback', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ code, state })
    })
    
    const data = await response.json()
    
    if (data.success) {
      // Set user and token in store
      authStore.user = data.data.user
      authStore.token = data.data.token
      
      notificationStore.success('Login Successful', 'Welcome to YCS LMS!')
      
      const redirectTo = (route.query.redirect as string) || '/dashboard'
      await router.push(redirectTo)
    } else {
      notificationStore.error('Login Failed', data.message || 'OAuth login failed')
    }
  } catch (err) {
    console.error('OAuth callback error:', err)
    notificationStore.error('Login Failed', 'Failed to process OAuth login')
  }
}
</script>

<style scoped>
/* Custom styles for login form */
.login-form {
  /* Add any custom styling here */
}
</style>