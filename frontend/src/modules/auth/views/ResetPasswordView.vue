<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div>
        <div class="mx-auto h-12 w-12 text-center">
          <svg class="w-12 h-12 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
          </svg>
        </div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          새 비밀번호 설정
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          새로운 비밀번호를 설정해 주세요.
        </p>
      </div>

      <!-- Form -->
      <form class="mt-8 space-y-6" @submit.prevent="handleSubmit">
        <div class="space-y-4">
          <!-- New Password -->
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
              새 비밀번호
            </label>
            <input
              id="password"
              v-model="form.password"
              name="password"
              type="password"
              autocomplete="new-password"
              required
              class="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
              placeholder="새 비밀번호"
            />
            <p class="mt-1 text-sm text-gray-500">
              8자 이상, 대소문자, 숫자, 특수문자 포함
            </p>
          </div>

          <!-- Confirm Password -->
          <div>
            <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-2">
              새 비밀번호 확인
            </label>
            <input
              id="confirmPassword"
              v-model="form.confirmPassword"
              name="confirmPassword"
              type="password"
              autocomplete="new-password"
              required
              class="appearance-none rounded-md relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
              placeholder="새 비밀번호 확인"
            />
          </div>
        </div>

        <!-- Error Messages -->
        <div v-if="error" class="bg-red-50 border border-red-200 rounded-md p-4">
          <p class="text-sm text-red-600">{{ error }}</p>
        </div>

        <!-- Success Message -->
        <div v-if="success" class="bg-green-50 border border-green-200 rounded-md p-4">
          <p class="text-sm text-green-600">
            비밀번호가 성공적으로 변경되었습니다. 잠시 후 로그인 페이지로 이동합니다.
          </p>
        </div>

        <!-- Submit Button -->
        <div>
          <button
            type="submit"
            :disabled="loading || success"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="loading" class="absolute left-0 inset-y-0 flex items-center pl-3">
              <svg class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </span>
            {{ loading ? '변경 중...' : '비밀번호 변경' }}
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
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'

const route = useRoute()
const router = useRouter()
const toast = useToast()

// State
const loading = ref(false)
const error = ref('')
const success = ref(false)

const form = reactive({
  password: '',
  confirmPassword: ''
})

// Methods
const validateForm = () => {
  if (form.password.length < 8) {
    return '비밀번호는 8자 이상이어야 합니다.'
  }
  
  if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/.test(form.password)) {
    return '비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다.'
  }
  
  if (form.password !== form.confirmPassword) {
    return '비밀번호가 일치하지 않습니다.'
  }
  
  return null
}

const handleSubmit = async () => {
  error.value = ''
  
  const validationError = validateForm()
  if (validationError) {
    error.value = validationError
    return
  }

  try {
    loading.value = true
    
    const token = route.query.token as string
    if (!token) {
      error.value = '유효하지 않은 재설정 링크입니다.'
      return
    }
    
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // TODO: Implement actual password reset API call
    console.log('Resetting password with token:', token, 'New password:', form.password)
    
    success.value = true
    toast.success('비밀번호가 성공적으로 변경되었습니다.')
    
    // Redirect to login after 3 seconds
    setTimeout(() => {
      router.push({ name: 'Login' })
    }, 3000)
    
  } catch (err: any) {
    error.value = err.message || '비밀번호 변경 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  // Check if token is present
  if (!route.query.token) {
    error.value = '유효하지 않은 재설정 링크입니다.'
  }
})
</script>

<style scoped>
/* Custom styles if needed */
</style>