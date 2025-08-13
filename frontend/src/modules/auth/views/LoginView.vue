<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
    <!-- Background Pattern -->
    <div class="absolute inset-0 bg-white/50 backdrop-blur-[2px]"></div>
    <div class="absolute top-0 left-0 w-full h-full">
      <div class="absolute top-10 left-10 w-72 h-72 bg-blue-400 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob"></div>
      <div class="absolute top-10 right-10 w-72 h-72 bg-purple-400 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-2000"></div>
      <div class="absolute bottom-10 left-20 w-72 h-72 bg-pink-400 rounded-full mix-blend-multiply filter blur-xl opacity-20 animate-blob animation-delay-4000"></div>
    </div>
    
    <!-- Login Card -->
    <div class="relative max-w-md w-full">
      <div class="bg-white/80 backdrop-blur-md rounded-2xl shadow-2xl border border-white/50 p-8">
        <!-- Logo & Title -->
        <div class="text-center mb-8">
          <div class="mx-auto w-16 h-16 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl flex items-center justify-center mb-4 shadow-lg">
            <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
            </svg>
          </div>
          <h1 class="text-2xl font-bold text-gray-900 mb-2">YCS 물류관리</h1>
          <p class="text-gray-600 text-sm">안전하고 빠른 글로벌 배송 서비스</p>
        </div>

        <!-- Login Form -->
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Email Field -->
          <div class="space-y-2">
            <label for="email" class="text-sm font-medium text-gray-700 block">이메일 또는 아이디</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                </svg>
              </div>
              <input
                id="email"
                v-model="form.email"
                type="text"
                required
                class="block w-full pl-10 pr-3 py-3 border border-gray-200 rounded-xl text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                placeholder="이메일 또는 아이디를 입력하세요"
              />
            </div>
          </div>

          <!-- Password Field -->
          <div class="space-y-2">
            <label for="password" class="text-sm font-medium text-gray-700 block">비밀번호</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                required
                class="block w-full pl-10 pr-12 py-3 border border-gray-200 rounded-xl text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
                placeholder="비밀번호를 입력하세요"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute inset-y-0 right-0 pr-3 flex items-center hover:text-blue-600 transition-colors"
              >
                <svg class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path v-if="!showPassword" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path v-if="!showPassword" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
                </svg>
              </button>
            </div>
          </div>

          <!-- Remember Me & Forgot Password -->
          <div class="flex items-center justify-between">
            <label class="flex items-center">
              <input
                v-model="rememberMe"
                type="checkbox"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded transition-colors"
              />
              <span class="ml-2 text-sm text-gray-600">로그인 상태 유지</span>
            </label>
            <button
              type="button"
              @click="showForgotPassword = true"
              class="text-sm text-blue-600 hover:text-blue-700 font-medium transition-colors"
            >
              비밀번호 찾기
            </button>
          </div>

          <!-- Error Message -->
          <div v-if="error" class="p-4 rounded-xl bg-red-50 border border-red-200">
            <div class="flex">
              <svg class="h-5 w-5 text-red-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
              <div class="ml-3">
                <p class="text-sm text-red-800">{{ error }}</p>
              </div>
            </div>
          </div>

          <!-- Login Button -->
          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-medium py-3 px-4 rounded-xl transition-all duration-200 transform hover:scale-[1.02] focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none shadow-lg"
          >
            <div class="flex items-center justify-center">
              <svg v-if="loading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ loading ? '로그인 중...' : '로그인' }}
            </div>
          </button>

          <!-- Divider -->
          <div class="relative">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-200"></div>
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-3 bg-white text-gray-500">또는</span>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="flex space-x-3 text-sm">
            <button
              type="button"
              @click="showEmailFind = true"
              class="flex-1 text-gray-600 hover:text-blue-600 transition-colors"
            >
              이메일 찾기
            </button>
            <button
              type="button"
              @click="showForgotPassword = true"
              class="flex-1 text-gray-600 hover:text-blue-600 transition-colors"
            >
              비밀번호 찾기
            </button>
          </div>
        </form>

        <!-- Sign Up Link -->
        <div class="mt-6 text-center">
          <p class="text-gray-600 text-sm">
            계정이 없으신가요?
            <router-link to="/signup" class="font-medium text-blue-600 hover:text-blue-700 transition-colors">
              회원가입하기
            </router-link>
          </p>
        </div>
      </div>
      
      <!-- Forgot Password Modal -->
      <div v-if="showForgotPassword" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4" @click.self="showForgotPassword = false">
        <div class="bg-white rounded-2xl p-6 max-w-md w-full">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">비밀번호 재설정</h3>
          <form @submit.prevent="handleForgotPassword" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">이메일</label>
              <input
                v-model="forgotPasswordEmail"
                type="email"
                required
                class="w-full px-3 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="가입 시 사용한 이메일을 입력하세요"
              />
            </div>
            <div v-if="forgotPasswordError" class="text-red-600 text-sm">
              {{ forgotPasswordError }}
            </div>
            <div v-if="forgotPasswordSuccess" class="text-green-600 text-sm bg-green-50 p-3 rounded-xl">
              {{ forgotPasswordSuccess }}
            </div>
            <div class="flex gap-3">
              <button
                type="submit"
                :disabled="forgotPasswordLoading"
                class="flex-1 bg-blue-600 text-white py-2 px-4 rounded-xl hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                {{ forgotPasswordLoading ? '발송 중...' : '재설정 이메일 발송' }}
              </button>
              <button
                type="button"
                @click="showForgotPassword = false"
                class="flex-1 bg-gray-100 text-gray-700 py-2 px-4 rounded-xl hover:bg-gray-200 transition-colors"
              >
                취소
              </button>
            </div>
          </form>
        </div>
      </div>
      
      <!-- Email Find Modal -->
      <div v-if="showEmailFind" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4" @click.self="showEmailFind = false">
        <div class="bg-white rounded-2xl p-6 max-w-md w-full">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">이메일 찾기</h3>
          <form @submit.prevent="handleEmailFind" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">이름</label>
              <input
                v-model="emailFindForm.name"
                type="text"
                required
                class="w-full px-3 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="가입 시 입력한 이름을 입력하세요"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">연락처</label>
              <input
                v-model="emailFindForm.phone"
                type="tel"
                required
                class="w-full px-3 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="가입 시 입력한 연락처를 입력하세요"
              />
            </div>
            <div v-if="emailFindError" class="text-red-600 text-sm">
              {{ emailFindError }}
            </div>
            <div v-if="emailFindSuccess" class="text-green-600 text-sm bg-green-50 p-3 rounded-xl">
              {{ emailFindSuccess }}
            </div>
            <div class="flex gap-3">
              <button
                type="submit"
                :disabled="emailFindLoading"
                class="flex-1 bg-blue-600 text-white py-2 px-4 rounded-xl hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                {{ emailFindLoading ? '확인 중...' : '확인' }}
              </button>
              <button
                type="button"
                @click="showEmailFind = false"
                class="flex-1 bg-gray-100 text-gray-700 py-2 px-4 rounded-xl hover:bg-gray-200 transition-colors"
              >
                취소
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { AuthService } from '@/services/authService'

const router = useRouter()
const toast = useToast()

// 상태
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)
const rememberMe = ref(false)

// 폼 데이터
const form = reactive({
  email: '',
  password: ''
})

// 비밀번호 찾기 모달
const showForgotPassword = ref(false)
const forgotPasswordEmail = ref('')
const forgotPasswordLoading = ref(false)
const forgotPasswordError = ref('')
const forgotPasswordSuccess = ref('')

// 이메일 찾기 모달
const showEmailFind = ref(false)
const emailFindForm = reactive({
  name: '',
  phone: ''
})
const emailFindLoading = ref(false)
const emailFindError = ref('')
const emailFindSuccess = ref('')

// 로그인 처리
const handleSubmit = async () => {
  if (!form.email || !form.password) {
    error.value = '이메일과 비밀번호를 모두 입력해주세요.'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const result = await AuthService.signIn({
      email: form.email,
      password: form.password
    })

    if (result.success) {
      toast.success('로그인되었습니다.')
      router.push('/app/dashboard')
    } else {
      error.value = result.error || '로그인에 실패했습니다.'
    }
  } catch (err) {
    error.value = '로그인 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

// 비밀번호 찾기 처리
const handleForgotPassword = async () => {
  if (!forgotPasswordEmail.value) {
    forgotPasswordError.value = '이메일을 입력해주세요.'
    return
  }

  forgotPasswordLoading.value = true
  forgotPasswordError.value = ''
  forgotPasswordSuccess.value = ''

  try {
    const result = await AuthService.sendPasswordResetEmail(forgotPasswordEmail.value)
    
    if (result.success) {
      forgotPasswordSuccess.value = '비밀번호 재설정 이메일을 발송했습니다.'
    } else {
      forgotPasswordError.value = result.error || '비밀번호 재설정 이메일 발송에 실패했습니다.'
    }
  } catch (err) {
    forgotPasswordError.value = '비밀번호 재설정 중 오류가 발생했습니다.'
  } finally {
    forgotPasswordLoading.value = false
  }
}

// 이메일 찾기 처리
const handleEmailFind = async () => {
  if (!emailFindForm.name || !emailFindForm.phone) {
    emailFindError.value = '이름과 연락처를 모두 입력해주세요.'
    return
  }

  emailFindLoading.value = true
  emailFindError.value = ''
  emailFindSuccess.value = ''

  try {
    // Mock 응답 (실제 구현에서는 API 호출)
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    if (emailFindForm.name && emailFindForm.phone) {
      emailFindSuccess.value = '입력하신 정보로 등록된 이메일이 있습니다. 관리자에게 문의하세요.'
    } else {
      emailFindError.value = '입력하신 정보와 일치하는 계정을 찾을 수 없습니다.'
    }
  } catch (err) {
    emailFindError.value = '이메일 찾기 중 오류가 발생했습니다.'
  } finally {
    emailFindLoading.value = false
  }
}

</script>

<style scoped>
@keyframes blob {
  0% {
    transform: translate(0px, 0px) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
  100% {
    transform: translate(0px, 0px) scale(1);
  }
}

.animate-blob {
  animation: blob 7s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

.animation-delay-4000 {
  animation-delay: 4s;
}
</style>