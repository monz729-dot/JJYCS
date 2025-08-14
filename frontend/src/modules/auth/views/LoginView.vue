<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div class="text-center">
        <div class="mx-auto h-16 w-16 bg-blue-600 rounded-lg flex items-center justify-center mb-6">
          <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3z" />
          </svg>
        </div>
        <h1 class="text-3xl font-extrabold text-gray-900 mb-2">
          로그인
        </h1>
        <p class="text-gray-600">
          YCS 물류관리 시스템에 로그인하세요
        </p>
      </div>

      <!-- Login Form -->
      <div class="bg-white shadow-xl rounded-lg p-8">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Email Field -->
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700 mb-2">이메일 또는 아이디</label>
            <input
              id="email"
              v-model="form.email"
              type="text"
              required
              class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              placeholder="이메일 또는 아이디를 입력하세요"
            />
          </div>

          <!-- Password Field -->
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-2">비밀번호</label>
            <div class="relative">
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                required
                class="w-full px-4 py-2 pr-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="비밀번호를 입력하세요"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute inset-y-0 right-0 pr-3 flex items-center"
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
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <span class="ml-2 text-sm text-gray-600">로그인 상태 유지</span>
            </label>
            <button
              type="button"
              @click="showForgotPassword = true"
              class="text-sm text-blue-600 hover:text-blue-700 font-medium"
            >
              비밀번호 찾기
            </button>
          </div>

          <!-- Error Message -->
          <div v-if="error" class="p-4 rounded-lg bg-red-50 border border-red-200">
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
            class="w-full py-3 px-4 border border-transparent text-sm font-medium rounded-lg text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="loading">로그인 중...</span>
            <span v-else>로그인</span>
          </button>

          <!-- Quick Actions -->
          <div class="flex space-x-4 text-sm">
            <button
              type="button"
              @click="showEmailFind = true"
              class="flex-1 text-gray-600 hover:text-blue-600"
            >
              이메일 찾기
            </button>
            <button
              type="button"
              @click="showForgotPassword = true"
              class="flex-1 text-gray-600 hover:text-blue-600"
            >
              비밀번호 찾기
            </button>
          </div>
        </form>

        <!-- Sign Up Link -->
        <div class="mt-6 text-center">
          <p class="text-gray-600 text-sm">
            계정이 없으신가요?
            <router-link to="/signup" class="font-medium text-blue-600 hover:text-blue-700">
              회원가입하기
            </router-link>
          </p>
        </div>
      </div>
      
      <!-- Forgot Password Modal -->
      <div v-if="showForgotPassword" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4" @click.self="showForgotPassword = false">
        <div class="bg-white rounded-lg p-6 max-w-md w-full">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">비밀번호 재설정</h3>
          <form @submit.prevent="handleForgotPassword" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">이메일</label>
              <input
                v-model="forgotPasswordEmail"
                type="email"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="가입 시 사용한 이메일을 입력하세요"
              />
            </div>
            <div v-if="forgotPasswordError" class="text-red-600 text-sm">
              {{ forgotPasswordError }}
            </div>
            <div v-if="forgotPasswordSuccess" class="text-green-600 text-sm bg-green-50 p-3 rounded-lg">
              {{ forgotPasswordSuccess }}
            </div>
            <div class="flex gap-3">
              <button
                type="submit"
                :disabled="forgotPasswordLoading"
                class="flex-1 bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                {{ forgotPasswordLoading ? '발송 중...' : '재설정 이메일 발송' }}
              </button>
              <button
                type="button"
                @click="showForgotPassword = false"
                class="flex-1 bg-gray-100 text-gray-700 py-2 px-4 rounded-lg hover:bg-gray-200 transition-colors"
              >
                취소
              </button>
            </div>
          </form>
        </div>
      </div>
      
      <!-- Email Find Modal -->
      <div v-if="showEmailFind" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4" @click.self="showEmailFind = false">
        <div class="bg-white rounded-lg p-6 max-w-md w-full">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">이메일 찾기</h3>
          <form @submit.prevent="handleEmailFind" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">이름</label>
              <input
                v-model="emailFindForm.name"
                type="text"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="가입 시 입력한 이름을 입력하세요"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">연락처</label>
              <input
                v-model="emailFindForm.phone"
                type="tel"
                required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="가입 시 입력한 연락처를 입력하세요"
              />
            </div>
            <div v-if="emailFindError" class="text-red-600 text-sm">
              {{ emailFindError }}
            </div>
            <div v-if="emailFindSuccess" class="text-green-600 text-sm bg-green-50 p-3 rounded-lg">
              {{ emailFindSuccess }}
            </div>
            <div class="flex gap-3">
              <button
                type="submit"
                :disabled="emailFindLoading"
                class="flex-1 bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                {{ emailFindLoading ? '확인 중...' : '확인' }}
              </button>
              <button
                type="button"
                @click="showEmailFind = false"
                class="flex-1 bg-gray-100 text-gray-700 py-2 px-4 rounded-lg hover:bg-gray-200 transition-colors"
              >
                취소
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useToast } from '@/composables/useToast'
import { AuthService } from '@/services/authService'

const router = useRouter()
const route = useRoute()
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
      
      // 리다이렉트 쿼리 파라미터가 있으면 해당 페이지로, 없으면 대시보드로
      const redirect = route.query.redirect as string
      if (redirect) {
        router.push(redirect)
      } else {
                  // 사용자 유형에 따른 기본 리다이렉트
          try {
            const userProfile = await AuthService.getCurrentUserProfile()
            console.log('로그인 후 프로필 조회:', userProfile)
            
            if (userProfile.data) {
              switch(userProfile.data.user_type) {
                case 'general':
                  // 일반회원: 주문 관리 페이지
                  router.push('/app/orders')
                  break
                case 'corporate':
                  // 기업회원: 주문 관리 페이지 (대량 주문 기능 포함)
                  router.push('/app/orders')
                  break
                case 'partner':
                  // 파트너: 파트너 대시보드
                  router.push('/app/partner')
                  break
                case 'warehouse':
                  // 현장관리자: 창고 대시보드 (모바일 스캔/입고)
                  router.push('/app/warehouse')
                  break
                case 'admin':
                  // 관리자: 어드민 대시보드
                  router.push('/app/admin')
                  break
                default:
                  // 기본: 일반 대시보드
                  router.push('/app/dashboard')
              }
            } else {
              console.log('프로필이 없어서 기본 대시보드로 이동')
              router.push('/app/dashboard')
            }
          } catch (profileError) {
            console.error('프로필 조회 실패, 기본 대시보드로 이동:', profileError)
            router.push('/app/dashboard')
          }
      }
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
/* Custom styles */
</style>