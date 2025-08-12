<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div>
        <div class="mx-auto h-12 w-12 text-center">
          <svg class="w-12 h-12 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M18 8a6 6 0 01-7.743 5.743L10 14l-1 1-1 1H6v2H2v-4l4.257-4.257A6 6 0 1118 8zm-6-4a1 1 0 100 2 2 2 0 012 2 1 1 0 102 0 4 4 0 00-4-4z" clip-rule="evenodd" />
          </svg>
        </div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          비밀번호 찾기
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          등록하신 이메일 주소를 입력하시면 비밀번호 재설정 링크를 보내드립니다.
        </p>
      </div>

      <!-- Form -->
      <form class="mt-8 space-y-6" @submit.prevent="handleSubmit">
        <div v-if="!emailSent">
          <!-- Email Input -->
          <div>
            <label for="email" class="sr-only">이메일 주소</label>
            <input
              id="email"
              v-model="email"
              name="email"
              type="email"
              autocomplete="email"
              required
              class="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
              placeholder="이메일 주소"
            />
          </div>

          <!-- Submit Button -->
          <div>
            <button
              type="submit"
              :disabled="loading || !email"
              class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="loading" class="absolute left-0 inset-y-0 flex items-center pl-3">
                <svg class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              </span>
              {{ loading ? '전송 중...' : '비밀번호 재설정 링크 전송' }}
            </button>
          </div>
        </div>

        <!-- Success Message -->
        <div v-else class="text-center">
          <div class="mx-auto h-16 w-16 text-center mb-4">
            <svg class="w-16 h-16 text-green-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
          </div>
          <h3 class="text-lg font-medium text-gray-900 mb-2">이메일을 확인하세요!</h3>
          <p class="text-gray-600 mb-4">
            {{ email }}로 비밀번호 재설정 링크를 보내드렸습니다.
          </p>
          <p class="text-sm text-gray-500 mb-6">
            이메일이 도착하지 않았다면 스팸 폴더를 확인해 주세요.
          </p>
          <button
            @click="resendEmail"
            :disabled="resendCooldown > 0"
            class="text-blue-600 hover:text-blue-500 text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ resendCooldown > 0 ? `${resendCooldown}초 후 재전송 가능` : '이메일 다시 보내기' }}
          </button>
        </div>

        <!-- Back to Login -->
        <div class="text-center">
          <router-link
            to="/auth/login"
            class="font-medium text-blue-600 hover:text-blue-500"
          >
            ← 로그인 페이지로 돌아가기
          </router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useToast } from 'vue-toastification'

const toast = useToast()

// State
const email = ref('')
const loading = ref(false)
const emailSent = ref(false)
const resendCooldown = ref(0)

let cooldownTimer: NodeJS.Timeout | null = null

// Methods
const handleSubmit = async () => {
  if (!email.value) return

  try {
    loading.value = true
    
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // TODO: Implement actual forgot password API call
    console.log('Sending password reset email to:', email.value)
    
    emailSent.value = true
    toast.success('비밀번호 재설정 이메일을 발송했습니다.')
    
    // Start cooldown for resend button
    startResendCooldown()
    
  } catch (error) {
    toast.error('이메일 발송 중 오류가 발생했습니다.')
    console.error('Forgot password error:', error)
  } finally {
    loading.value = false
  }
}

const resendEmail = async () => {
  if (resendCooldown.value > 0) return
  
  try {
    loading.value = true
    
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // TODO: Implement actual resend API call
    console.log('Resending password reset email to:', email.value)
    
    toast.success('이메일을 다시 발송했습니다.')
    startResendCooldown()
    
  } catch (error) {
    toast.error('이메일 재발송 중 오류가 발생했습니다.')
  } finally {
    loading.value = false
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

// Cleanup
onUnmounted(() => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
  }
})
</script>

<style scoped>
/* Custom styles if needed */
</style>