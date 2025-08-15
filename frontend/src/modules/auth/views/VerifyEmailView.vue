<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div>
        <div class="mx-auto h-12 w-12 text-center">
          <svg v-if="!verified && !error" class="w-12 h-12 text-yellow-600 animate-pulse" fill="currentColor" viewBox="0 0 20 20">
            <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
            <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
          </svg>
          <svg v-else-if="verified" class="w-12 h-12 text-green-600" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
          </svg>
          <svg v-else class="w-12 h-12 text-red-600" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
          </svg>
        </div>
        
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          <span v-if="!verified && !error">이메일 인증 중...</span>
          <span v-else-if="verified">이메일 인증 완료!</span>
          <span v-else>인증 실패</span>
        </h2>
        
        <div v-if="!verified && !error" class="mt-2 text-center">
          <p class="text-sm text-gray-600">
            이메일 인증을 처리하고 있습니다. 잠시만 기다려 주세요.
          </p>
        </div>
        
        <div v-else-if="verified" class="mt-2 text-center">
          <p class="text-sm text-gray-600 mb-4">
            이메일 인증이 완료되었습니다. 이제 YCS LMS의 모든 기능을 사용하실 수 있습니다.
          </p>
          <div class="bg-green-50 border border-green-200 rounded-md p-4">
            <p class="text-sm text-green-600">
              인증이 완료되어 자동으로 로그인됩니다.
            </p>
          </div>
        </div>
        
        <div v-else class="mt-2 text-center">
          <p class="text-sm text-gray-600 mb-4">
            이메일 인증에 실패했습니다.
          </p>
          <div class="bg-red-50 border border-red-200 rounded-md p-4">
            <p class="text-sm text-red-600">{{ errorMessage }}</p>
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="space-y-4">
        <div v-if="verified">
          <button
            @click="goToDashboard"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
          >
            대시보드로 이동
          </button>
        </div>
        
        <div v-else-if="error">
          <button
            @click="resendVerification"
            :disabled="resendCooldown > 0"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ resendCooldown > 0 ? `${resendCooldown}초 후 재전송 가능` : '인증 이메일 재전송' }}
          </button>
        </div>

        <!-- Back to Login -->
        <div class="text-center">
          <router-link
            :to="{ name: 'Login' }"
            class="font-medium text-blue-600 hover:text-blue-500"
          >
            ← 로그인 페이지로 돌아가기
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'
import { AuthService } from '@/services/authService'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

// State
const verified = ref(false)
const error = ref(false)
const errorMessage = ref('')
const resendCooldown = ref(0)

let cooldownTimer: NodeJS.Timeout | null = null

// Methods
const verifyEmail = async () => {
  const token = route.query.token as string
  
  if (!token) {
    error.value = true
    errorMessage.value = '유효하지 않은 인증 링크입니다.'
    return
  }

  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // Mock verification - in real implementation, this would call the API
    if (token === 'invalid') {
      throw new Error('Invalid verification token')
    }
    
    // TODO: Implement actual email verification API call
    
    
    verified.value = true
    toast.success('이메일 인증이 완료되었습니다.')
    
    // Auto redirect to dashboard after 3 seconds
    setTimeout(() => {
      goToDashboard()
    }, 3000)
    
  } catch (err: any) {
    error.value = true
    errorMessage.value = err.message || '인증 토큰이 만료되었거나 유효하지 않습니다.'
  }
}

const resendVerification = async () => {
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // AuthService를 사용하여 이메일 인증 재발송
    const result = await AuthService.resendEmailVerification()
    
    if (!result.success) {
      throw new Error(result.error || '이메일 재발송에 실패했습니다.')
    }
    
    toast.success('인증 이메일을 다시 발송했습니다.')
    startResendCooldown()
    
  } catch (err: any) {
    toast.error('이메일 재발송 중 오류가 발생했습니다.')
  }
}

const startResendCooldown = () => {
  resendCooldown.value = 60 // 60 seconds
  
  cooldownTimer = setInterval(() => {
    resendCooldown.value--
    if (resendCooldown.value <= 0 && cooldownTimer) {
      clearInterval(cooldownTimer)
      cooldownTimer = null
    }
  }, 1000)
}

const goToDashboard = () => {
  router.push({ name: 'Dashboard' })
}

// Lifecycle
onMounted(() => {
  verifyEmail()
})

onUnmounted(() => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
  }
})
</script>

<style scoped>
/* Custom styles if needed */
</style>