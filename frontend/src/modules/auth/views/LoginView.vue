<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 py-12 px-4">
    <div class="max-w-md w-full">
      <!-- Header -->
      <div class="text-center mb-8">
        <div class="mx-auto w-20 h-20 bg-gradient-to-br from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center mb-6 shadow-lg">
          <span class="mdi mdi-package-variant text-white text-3xl" />
        </div>
        <h1 class="text-3xl font-bold text-gray-900 mb-2">
          YCS LMS
        </h1>
        <p class="text-gray-600">
          물류관리 시스템에 로그인하세요
        </p>
      </div>

      <!-- Login Form -->
      <Card class="shadow-2xl">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Email Field -->
          <Input
            v-model="form.email"
            label="이메일 또는 아이디"
            placeholder="이메일 또는 아이디를 입력하세요"
            icon="mdi-account"
            required
            :error="emailError"
          />

          <!-- Password Field -->
          <Input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            label="비밀번호"
            placeholder="비밀번호를 입력하세요"
            icon="mdi-lock"
            :right-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
            :clickable-right-icon="true"
            required
            :error="passwordError"
            @icon-click="showPassword = !showPassword"
          />

          <!-- Remember Me & Forgot Password -->
          <div class="flex items-center justify-between">
            <label class="flex items-center cursor-pointer">
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
          <div v-if="error" class="p-4 rounded-lg bg-red-50 border border-red-200 flex items-start gap-3">
            <span class="mdi mdi-alert-circle text-red-500 text-lg flex-shrink-0 mt-0.5" />
            <p class="text-sm text-red-800">{{ error }}</p>
          </div>

          <!-- Login Button -->
          <Button
            type="submit"
            variant="primary"
            size="lg"
            :loading="loading"
            :disabled="loading"
            :full-width="true"
            :text="loading ? '로그인 중...' : '로그인'"
            icon="mdi-login"
          />
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

        <!-- Test Login Button -->
        <div class="mt-4 text-center">
          <Button
            variant="ghost"
            size="sm"
            text="테스트 로그인"
            icon="mdi-test-tube"
            @click="router.push('/test-login')"
          />
        </div>
      </Card>
      
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
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

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

// 에러 상태
const emailError = computed(() => {
  if (!form.email && error.value) return '이메일 또는 아이디를 입력해주세요'
  return ''
})

const passwordError = computed(() => {
  if (!form.password && error.value) return '비밀번호를 입력해주세요'
  return ''
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
    const result = await authStore.signIn({
      email: form.email,
      password: form.password
    })

    if (result.success) {
      // 리다이렉트 쿼리 파라미터가 있으면 해당 페이지로, 없으면 대시보드로
      const redirect = route.query.redirect as string
      if (redirect) {
        router.push(redirect)
      } else {
        router.push('/app/dashboard')
      }
    } else {
      error.value = result.error || '로그인에 실패했습니다.'
    }
  } catch (err) {
    error.value = '로그인 중 오류가 발생했습니다.'
    console.error('Login error:', err)
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
    const result = await authStore.sendPasswordResetEmail(forgotPasswordEmail.value)
    
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