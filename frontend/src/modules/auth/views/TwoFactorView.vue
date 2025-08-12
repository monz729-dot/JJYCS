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
          2단계 인증
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          인증 앱에서 6자리 코드를 입력해 주세요.
        </p>
      </div>

      <!-- Form -->
      <form class="mt-8 space-y-6" @submit.prevent="handleSubmit">
        <!-- 2FA Code Input -->
        <div>
          <label for="code" class="sr-only">인증 코드</label>
          <div class="flex justify-center space-x-2">
            <input
              v-for="(digit, index) in codeDigits"
              :key="index"
              :ref="el => codeInputs[index] = el"
              v-model="codeDigits[index]"
              type="text"
              maxlength="1"
              class="w-12 h-12 text-center text-2xl font-bold border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              @input="handleInput(index, $event)"
              @keydown="handleKeydown(index, $event)"
              @paste="handlePaste"
            />
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="error" class="bg-red-50 border border-red-200 rounded-md p-4">
          <p class="text-sm text-red-600">{{ error }}</p>
        </div>

        <!-- Submit Button -->
        <div>
          <button
            type="submit"
            :disabled="loading || !isCodeComplete"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="loading" class="absolute left-0 inset-y-0 flex items-center pl-3">
              <svg class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </span>
            {{ loading ? '인증 중...' : '인증하기' }}
          </button>
        </div>

        <!-- Help Links -->
        <div class="space-y-3">
          <div class="text-center">
            <button
              type="button"
              @click="resendCode"
              :disabled="resendCooldown > 0"
              class="text-sm text-blue-600 hover:text-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ resendCooldown > 0 ? `${resendCooldown}초 후 재전송 가능` : '코드 다시 받기' }}
            </button>
          </div>

          <div class="text-center">
            <button
              type="button"
              @click="showBackupCodes = !showBackupCodes"
              class="text-sm text-gray-600 hover:text-gray-500"
            >
              백업 코드 사용하기
            </button>
          </div>

          <!-- Backup Codes Section -->
          <div v-if="showBackupCodes" class="bg-gray-50 border border-gray-200 rounded-md p-4">
            <h3 class="text-sm font-medium text-gray-900 mb-2">백업 코드 입력</h3>
            <input
              v-model="backupCode"
              type="text"
              placeholder="8자리 백업 코드"
              class="w-full px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
            <button
              type="button"
              @click="verifyBackupCode"
              :disabled="!backupCode || loading"
              class="mt-2 w-full py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-gray-600 hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              백업 코드로 인증
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
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

// State
const codeDigits = ref(['', '', '', '', '', ''])
const codeInputs = ref<(HTMLInputElement | null)[]>([])
const loading = ref(false)
const error = ref('')
const showBackupCodes = ref(false)
const backupCode = ref('')
const resendCooldown = ref(0)

let cooldownTimer: NodeJS.Timeout | null = null

// Computed
const isCodeComplete = computed(() => {
  return codeDigits.value.every(digit => digit !== '')
})

const fullCode = computed(() => {
  return codeDigits.value.join('')
})

// Methods
const handleInput = (index: number, event: Event) => {
  const target = event.target as HTMLInputElement
  const value = target.value

  if (!/^\d$/.test(value)) {
    codeDigits.value[index] = ''
    return
  }

  if (value && index < 5) {
    // Auto-focus next input
    nextTick(() => {
      const nextInput = codeInputs.value[index + 1]
      if (nextInput) {
        nextInput.focus()
      }
    })
  }
}

const handleKeydown = (index: number, event: KeyboardEvent) => {
  if (event.key === 'Backspace' && !codeDigits.value[index] && index > 0) {
    // Focus previous input on backspace
    const prevInput = codeInputs.value[index - 1]
    if (prevInput) {
      prevInput.focus()
    }
  }
}

const handlePaste = (event: ClipboardEvent) => {
  event.preventDefault()
  const pastedText = event.clipboardData?.getData('text') || ''
  const digits = pastedText.replace(/\D/g, '').slice(0, 6).split('')
  
  digits.forEach((digit, index) => {
    if (index < 6) {
      codeDigits.value[index] = digit
    }
  })
  
  // Focus the last filled input or the first empty one
  const lastIndex = Math.min(digits.length - 1, 5)
  const targetInput = codeInputs.value[lastIndex]
  if (targetInput) {
    nextTick(() => {
      targetInput.focus()
    })
  }
}

const handleSubmit = async () => {
  if (!isCodeComplete.value) return

  try {
    loading.value = true
    error.value = ''

    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // Mock 2FA verification
    if (fullCode.value === '123456') {
      toast.success('2단계 인증이 완료되었습니다.')
      router.push({ name: 'Dashboard' })
    } else {
      throw new Error('Invalid 2FA code')
    }
    
  } catch (err: any) {
    error.value = '인증 코드가 올바르지 않습니다. 다시 확인해 주세요.'
    // Clear the code inputs
    codeDigits.value = ['', '', '', '', '', '']
    // Focus first input
    const firstInput = codeInputs.value[0]
    if (firstInput) {
      nextTick(() => {
        firstInput.focus()
      })
    }
  } finally {
    loading.value = false
  }
}

const verifyBackupCode = async () => {
  if (!backupCode.value) return

  try {
    loading.value = true
    error.value = ''

    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Mock backup code verification
    if (backupCode.value.length === 8) {
      toast.success('백업 코드로 인증이 완료되었습니다.')
      router.push({ name: 'Dashboard' })
    } else {
      throw new Error('Invalid backup code')
    }
    
  } catch (err: any) {
    error.value = '백업 코드가 올바르지 않습니다.'
  } finally {
    loading.value = false
  }
}

const resendCode = async () => {
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    toast.success('새 인증 코드를 발송했습니다.')
    startResendCooldown()
    
  } catch (err: any) {
    toast.error('코드 재전송 중 오류가 발생했습니다.')
  }
}

const startResendCooldown = () => {
  resendCooldown.value = 30 // 30 seconds
  
  cooldownTimer = setInterval(() => {
    resendCooldown.value--
    if (resendCooldown.value <= 0 && cooldownTimer) {
      clearInterval(cooldownTimer)
      cooldownTimer = null
    }
  }, 1000)
}

// Lifecycle
onMounted(() => {
  // Focus first input
  const firstInput = codeInputs.value[0]
  if (firstInput) {
    nextTick(() => {
      firstInput.focus()
    })
  }
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