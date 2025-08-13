<template>
  <div class="max-w-md mx-auto p-6">
    <div class="bg-white rounded-lg shadow-lg p-8">
      <h2 class="text-2xl font-bold text-gray-900 mb-6 text-center">로그인</h2>
      
      <form @submit.prevent="handleSubmit" class="space-y-6">
        <!-- 이메일/아이디 입력 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">이메일 또는 아이디</label>
          <input
            v-model="form.email"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="이메일 또는 아이디를 입력하세요"
            required
          />
        </div>

        <!-- 비밀번호 입력 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">비밀번호</label>
          <div class="relative">
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="w-full px-3 py-2 pr-10 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="비밀번호를 입력하세요"
              required
            />
            <button
              type="button"
              @click="showPassword = !showPassword"
              class="absolute inset-y-0 right-0 pr-3 flex items-center"
            >
              <svg
                v-if="showPassword"
                class="h-5 w-5 text-gray-400"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"
                />
              </svg>
              <svg
                v-else
                class="h-5 w-5 text-gray-400"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                />
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                />
              </svg>
            </button>
          </div>
        </div>

        <!-- 로그인 상태 표시 -->
        <div v-if="loginStatus" class="p-3 rounded-md" :class="{
          'bg-yellow-50 text-yellow-800 border border-yellow-200': loginStatus === 'pending',
          'bg-red-50 text-red-800 border border-red-200': loginStatus === 'rejected'
        }">
          <div class="flex items-center">
            <svg v-if="loginStatus === 'pending'" class="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
            </svg>
            <svg v-if="loginStatus === 'rejected'" class="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
            <span class="text-sm font-medium">
              {{ loginStatus === 'pending' ? '승인 대기 중인 계정입니다. 관리자 승인 후 로그인 가능합니다.' : '승인이 거절된 계정입니다.' }}
            </span>
          </div>
        </div>

        <!-- 에러 메시지 -->
        <div v-if="error" class="text-red-600 text-sm bg-red-50 p-3 rounded-md">
          {{ error }}
        </div>

        <!-- 로그인 버튼 -->
        <button
          type="submit"
          :disabled="loading"
          class="w-full bg-blue-600 text-white py-3 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <!-- 추가 옵션 -->
      <div class="mt-6 space-y-4">
        <!-- 이메일 찾기 / 비밀번호 찾기 -->
        <div class="flex justify-between text-sm">
          <button
            type="button"
            @click="showEmailFind = true"
            class="text-blue-600 hover:text-blue-800"
          >
            이메일 찾기
          </button>
          <button
            type="button"
            @click="showPasswordReset = true"
            class="text-blue-600 hover:text-blue-800"
          >
            비밀번호 찾기
          </button>
        </div>

        <!-- 회원가입 링크 -->
        <div class="text-center">
          <span class="text-gray-600">계정이 없으신가요?</span>
          <router-link to="/signup" class="text-blue-600 hover:text-blue-800 ml-1">
            회원가입하기
          </router-link>
        </div>
      </div>
    </div>

    <!-- 이메일 찾기 모달 -->
    <div v-if="showEmailFind" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4">
        <h3 class="text-lg font-semibold mb-4">이메일 찾기</h3>
        <form @submit.prevent="handleEmailFind" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">이름</label>
            <input
              v-model="emailFindForm.name"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="가입 시 입력한 이름을 입력하세요"
              required
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">연락처</label>
            <input
              v-model="emailFindForm.phone"
              type="tel"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="가입 시 입력한 연락처를 입력하세요"
              required
            />
          </div>
          <div v-if="emailFindError" class="text-red-600 text-sm">
            {{ emailFindError }}
          </div>
          <div v-if="emailFindSuccess" class="text-green-600 text-sm bg-green-50 p-3 rounded-md">
            {{ emailFindSuccess }}
          </div>
          <div class="flex gap-2">
            <button
              type="submit"
              :disabled="emailFindLoading"
              class="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 disabled:opacity-50"
            >
              {{ emailFindLoading ? '확인 중...' : '확인' }}
            </button>
            <button
              type="button"
              @click="showEmailFind = false"
              class="flex-1 bg-gray-500 text-white py-2 px-4 rounded-md hover:bg-gray-600"
            >
              취소
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 비밀번호 재설정 모달 -->
    <div v-if="showPasswordReset" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4">
        <h3 class="text-lg font-semibold mb-4">비밀번호 재설정</h3>
        <form @submit.prevent="handlePasswordReset" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">이메일</label>
            <input
              v-model="passwordResetForm.email"
              type="email"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="가입 시 입력한 이메일을 입력하세요"
              required
            />
          </div>
          <div v-if="passwordResetError" class="text-red-600 text-sm">
            {{ passwordResetError }}
          </div>
          <div v-if="passwordResetSuccess" class="text-green-600 text-sm bg-green-50 p-3 rounded-md">
            {{ passwordResetSuccess }}
          </div>
          <div class="flex gap-2">
            <button
              type="submit"
              :disabled="passwordResetLoading"
              class="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 disabled:opacity-50"
            >
              {{ passwordResetLoading ? '발송 중...' : '재설정 이메일 발송' }}
            </button>
            <button
              type="button"
              @click="showPasswordReset = false"
              class="flex-1 bg-gray-500 text-white py-2 px-4 rounded-md hover:bg-gray-600"
            >
              취소
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'
import { AuthService } from '@/services/authService'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

// 상태
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)
const loginStatus = ref<'pending' | 'rejected' | null>(null)
const showEmailFind = ref(false)
const showPasswordReset = ref(false)

// 폼 데이터
const form = reactive({
  email: '',
  password: ''
})

// 이메일 찾기 폼
const emailFindForm = reactive({
  name: '',
  phone: ''
})
const emailFindLoading = ref(false)
const emailFindError = ref('')
const emailFindSuccess = ref('')

// 비밀번호 재설정 폼
const passwordResetForm = reactive({
  email: ''
})
const passwordResetLoading = ref(false)
const passwordResetError = ref('')
const passwordResetSuccess = ref('')

// 로그인 처리
const handleSubmit = async () => {
  loading.value = true
  error.value = ''
  loginStatus.value = null

  try {
    const result = await AuthService.signIn({
      email: form.email,
      password: form.password
    })

    if (result.success) {
      toast.success('로그인되었습니다.')
      router.push('/dashboard')
    } else {
      error.value = result.error || '로그인에 실패했습니다.'
      
      // 승인 상태에 따른 메시지 처리
      if (result.error?.includes('승인 대기')) {
        loginStatus.value = 'pending'
      } else if (result.error?.includes('승인이 거절')) {
        loginStatus.value = 'rejected'
      }
    }
  } catch (err) {
    error.value = '로그인 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

// 이메일 찾기 처리
const handleEmailFind = async () => {
  emailFindLoading.value = true
  emailFindError.value = ''
  emailFindSuccess.value = ''

  try {
    // 실제 구현에서는 API 호출
    // const result = await authStore.findEmail(emailFindForm)
    
    // Mock 응답
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

// 비밀번호 재설정 처리
const handlePasswordReset = async () => {
  passwordResetLoading.value = true
  passwordResetError.value = ''
  passwordResetSuccess.value = ''

  try {
    const result = await AuthService.sendPasswordResetEmail(passwordResetForm.email)
    
    if (result.success) {
      passwordResetSuccess.value = '비밀번호 재설정 이메일을 발송했습니다.'
    } else {
      passwordResetError.value = result.error || '비밀번호 재설정 이메일 발송에 실패했습니다.'
    }
  } catch (err) {
    passwordResetError.value = '비밀번호 재설정 중 오류가 발생했습니다.'
  } finally {
    passwordResetLoading.value = false
  }
}

// 컴포넌트 마운트 시 에러 초기화
onMounted(() => {
  authStore.clearError()
})
</script>
