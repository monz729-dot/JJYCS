<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div class="text-center">
        <div class="mx-auto h-16 w-16 bg-blue-600 rounded-lg flex items-center justify-center mb-6">
          <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
          </svg>
        </div>
        <h1 class="text-3xl font-extrabold text-gray-900 mb-2">
          이메일 인증
        </h1>
        <p class="text-gray-600">
          가입하신 이메일 주소로 인증 메일을 발송했습니다.
        </p>
      </div>

      <div class="bg-white shadow-xl rounded-lg p-8">
        <div class="text-center space-y-6">
          <!-- 이메일 아이콘 -->
          <div class="mx-auto w-24 h-24 bg-blue-100 rounded-full flex items-center justify-center">
            <svg class="w-12 h-12 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
          </div>

          <!-- 안내 메시지 -->
          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-gray-900">
              이메일을 확인해주세요
            </h2>
            <p class="text-gray-600 text-sm">
              <strong>{{ userEmail }}</strong>로 인증 메일을 발송했습니다.<br>
              이메일의 링크를 클릭하여 인증을 완료해주세요.
            </p>
          </div>

          <!-- 스팸함 안내 -->
          <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div class="flex">
              <svg class="w-5 h-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
              </svg>
              <div class="ml-3">
                <p class="text-sm text-yellow-800">
                  이메일이 보이지 않는다면 <strong>스팸함</strong>을 확인해주세요.
                </p>
              </div>
            </div>
          </div>

          <!-- 버튼들 -->
          <div class="space-y-4">
            <button
              @click="resendEmail"
              :disabled="resendLoading || resendCooldown > 0"
              class="w-full py-3 px-4 border border-blue-300 text-blue-700 bg-blue-50 hover:bg-blue-100 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="resendLoading">재발송 중...</span>
              <span v-else-if="resendCooldown > 0">{{ resendCooldown }}초 후 재발송 가능</span>
              <span v-else>인증 메일 재발송</span>
            </button>

            <button
              @click="checkVerification"
              :disabled="checking"
              class="w-full py-3 px-4 border border-transparent text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="checking">확인 중...</span>
              <span v-else>인증 완료 확인</span>
            </button>

            <button
              @click="goToLogin"
              class="w-full py-3 px-4 border border-gray-300 text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 rounded-lg transition-colors"
            >
              로그인으로 돌아가기
            </button>
          </div>

          <!-- 성공/에러 메시지 -->
          <div v-if="message" class="p-4 rounded-lg" :class="messageType === 'success' ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'">
            <div class="flex">
              <svg v-if="messageType === 'success'" class="w-5 h-5 text-green-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
              <svg v-else class="w-5 h-5 text-red-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
              <div class="ml-3">
                <p class="text-sm" :class="messageType === 'success' ? 'text-green-800' : 'text-red-800'">
                  {{ message }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { AuthService } from '@/services/authService'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

// 상태
const userEmail = ref('')
const resendLoading = ref(false)
const checking = ref(false)
const resendCooldown = ref(0)
const message = ref('')
const messageType = ref<'success' | 'error'>('success')
let cooldownTimer: NodeJS.Timeout | null = null

// 이메일 재발송
const resendEmail = async () => {
  if (resendCooldown.value > 0) return

  resendLoading.value = true
  message.value = ''

  try {
    const result = await AuthService.resendEmailVerification()
    
    if (result.success) {
      message.value = '인증 메일을 재발송했습니다.'
      messageType.value = 'success'
      resendCooldown.value = 60 // 60초 쿨다운
      startCooldown()
    } else {
      message.value = result.error || '인증 메일 재발송에 실패했습니다.'
      messageType.value = 'error'
    }
  } catch (error) {
    message.value = '인증 메일 재발송 중 오류가 발생했습니다.'
    messageType.value = 'error'
  } finally {
    resendLoading.value = false
  }
}

// 인증 완료 확인
const checkVerification = async () => {
  checking.value = true
  message.value = ''

  try {
    // 현재 사용자 정보 가져오기
    const result = await authStore.fetchUserProfile()
    
    if (result.success && authStore.user?.email_verified) {
      // 이메일 인증 완료 후 승인 상태 업데이트
      const verificationResult = await authStore.handleEmailVerification()
      
      if (verificationResult.success) {
        message.value = '이메일 인증이 완료되었습니다!'
        messageType.value = 'success'
        
        // 사용자 유형에 따라 다른 페이지로 이동
        setTimeout(() => {
          if (authStore.user?.user_type === 'general') {
            // 일반회원은 즉시 대시보드로
            router.push('/app/dashboard')
          } else {
            // 기업/파트너 회원은 승인 대기 페이지로
            router.push('/approval')
          }
        }, 2000)
      } else {
        message.value = verificationResult.error || '이메일 인증 처리 중 오류가 발생했습니다.'
        messageType.value = 'error'
      }
    } else {
      message.value = '아직 이메일 인증이 완료되지 않았습니다. 이메일을 확인해주세요.'
      messageType.value = 'error'
    }
  } catch (error) {
    message.value = '인증 상태 확인 중 오류가 발생했습니다.'
    messageType.value = 'error'
  } finally {
    checking.value = false
  }
}

// 로그인 페이지로 이동
const goToLogin = () => {
  router.push('/login')
}

// 쿨다운 타이머 시작
const startCooldown = () => {
  cooldownTimer = setInterval(() => {
    resendCooldown.value--
    if (resendCooldown.value <= 0) {
      if (cooldownTimer) {
        clearInterval(cooldownTimer)
        cooldownTimer = null
      }
    }
  }, 1000)
}

// 컴포넌트 마운트 시
onMounted(async () => {
  // 현재 사용자 이메일 가져오기
  const result = await AuthService.getCurrentUserProfile()
  if (result.success && authStore.user) {
    userEmail.value = authStore.user.email || ''
  } else {
    // 사용자 정보가 없으면 로그인 페이지로 이동
    router.push('/login')
  }
})

// 컴포넌트 언마운트 시 타이머 정리
onUnmounted(() => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
  }
})
</script>

<style scoped>
/* Custom styles */
</style>
