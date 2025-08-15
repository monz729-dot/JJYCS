<template>
  <div class="max-w-2xl mx-auto p-6">
    <div class="bg-white rounded-lg shadow-lg p-8">
      <h2 class="text-2xl font-bold text-gray-900 mb-6 text-center">회원가입</h2>
      
      <!-- 회원 종류 선택 -->
      <div class="mb-6">
        <label class="block text-sm font-medium text-gray-700 mb-3">회원 종류</label>
        <div class="grid grid-cols-3 gap-4">
          <div
            v-for="type in userTypes"
            :key="type.value"
            @click="form.user_type = type.value"
            :class="[
              'p-4 border-2 rounded-lg cursor-pointer transition-colors',
              form.user_type === type.value
                ? 'border-blue-500 bg-blue-50'
                : 'border-gray-200 hover:border-gray-300'
            ]"
          >
            <div class="text-center">
              <div class="text-lg font-semibold text-gray-900">{{ type.label }}</div>
              <div class="text-sm text-gray-600 mt-1">{{ type.description }}</div>
            </div>
          </div>
        </div>
      </div>

      <form @submit.prevent="handleSubmit" class="space-y-6">
        <!-- 기본 정보 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">아이디 *</label>
            <div class="flex gap-2">
              <input
                v-model="form.username"
                type="text"
                :class="[
                  'flex-1 px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500',
                  usernameError ? 'border-red-500' : 'border-gray-300'
                ]"
                placeholder="아이디를 입력하세요"
                @blur="checkUsername"
              />
              <button
                type="button"
                @click="checkUsername"
                :disabled="!form.username || usernameChecking"
                class="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600 disabled:opacity-50"
              >
                {{ usernameChecking ? '확인중...' : '중복확인' }}
              </button>
            </div>
            <div v-if="usernameMessage" :class="[
              'text-sm mt-1',
              usernameAvailable ? 'text-green-600' : 'text-red-600'
            ]">
              {{ usernameMessage }}
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">이메일 *</label>
            <input
              v-model="form.email"
              type="email"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="이메일을 입력하세요"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">비밀번호 *</label>
            <input
              v-model="form.password"
              type="password"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="8자 이상, 영문/숫자/특수문자 포함"
            />
            <div v-if="form.password && form.password.length < 8" class="text-sm mt-1 text-amber-600">
              비밀번호는 최소 8자 이상이어야 합니다.
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">비밀번호 확인 *</label>
            <input
              v-model="form.passwordConfirm"
              type="password"
              :class="[
                'w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500',
                passwordMismatch ? 'border-red-500' : 'border-gray-300'
              ]"
              placeholder="비밀번호를 다시 입력하세요"
              @blur="checkPasswordMatch"
            />
            <div v-if="passwordMismatch" class="text-sm mt-1 text-red-600">
              비밀번호가 일치하지 않습니다.
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">이름 *</label>
            <input
              v-model="form.name"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="이름을 입력하세요"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">연락처</label>
            <input
              v-model="form.phone"
              type="tel"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="연락처를 입력하세요"
            />
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">주소</label>
          <input
            v-model="form.address"
            type="text"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="주소를 입력하세요"
          />
        </div>

        <!-- 사업자등록증 업로드 (기업/파트너 회원) -->
        <div v-if="form.user_type === 'corporate' || form.user_type === 'partner'">
          <label class="block text-sm font-medium text-gray-700 mb-2">사업자등록증 *</label>
          <div class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
            <input
              ref="fileInput"
              type="file"
              accept=".pdf,.jpg,.jpeg,.png"
              @change="handleFileChange"
              class="hidden"
            />
            <div v-if="!form.business_license_file" @click="$refs.fileInput.click()" class="cursor-pointer">
              <div class="text-gray-600 mb-2">파일을 선택하거나 여기에 드래그하세요</div>
              <div class="text-sm text-gray-500">PDF, JPG, PNG 파일만 가능 (최대 10MB)</div>
            </div>
            <div v-else class="text-left">
              <div class="flex items-center justify-between">
                <span class="text-sm text-gray-700">{{ form.business_license_file.name }}</span>
                <button
                  type="button"
                  @click="removeFile"
                  class="text-red-500 hover:text-red-700 text-sm"
                >
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 약관 동의 -->
        <div class="space-y-4">
          <div class="bg-gray-50 p-4 rounded-lg">
            <h3 class="font-medium text-gray-900 mb-3">약관 동의</h3>
            
            <!-- 전체 동의 -->
            <div class="flex items-start mb-3 p-3 bg-white rounded border">
              <input
                v-model="allAgreed"
                @change="toggleAllAgreements"
                type="checkbox"
                id="allAgreed"
                class="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label for="allAgreed" class="ml-2 block text-sm font-medium text-gray-900">
                전체 약관에 동의합니다
              </label>
            </div>

            <!-- 필수 약관들 -->
            <div class="space-y-3">
              <div class="flex items-start">
                <input
                  v-model="consents.termsOfService"
                  type="checkbox"
                  id="terms"
                  class="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label for="terms" class="ml-2 block text-sm text-gray-900 flex-1">
                  <span class="font-medium text-red-600">[필수]</span> 이용약관에 동의합니다
                  <button 
                    type="button" 
                    @click="openTermsModal('terms')" 
                    class="text-blue-600 hover:text-blue-800 ml-2 underline"
                  >
                    보기
                  </button>
                </label>
              </div>

              <div class="flex items-start">
                <input
                  v-model="consents.privacyPolicy"
                  type="checkbox"
                  id="privacy"
                  class="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label for="privacy" class="ml-2 block text-sm text-gray-900 flex-1">
                  <span class="font-medium text-red-600">[필수]</span> 개인정보 수집 및 이용에 동의합니다
                  <button 
                    type="button" 
                    @click="openTermsModal('privacy')" 
                    class="text-blue-600 hover:text-blue-800 ml-2 underline"
                  >
                    보기
                  </button>
                </label>
              </div>

              <div class="flex items-start">
                <input
                  v-model="consents.marketingConsent"
                  type="checkbox"
                  id="marketing"
                  class="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label for="marketing" class="ml-2 block text-sm text-gray-900 flex-1">
                  <span class="font-medium text-gray-600">[선택]</span> 마케팅 정보 수신에 동의합니다
                  <button 
                    type="button" 
                    @click="openTermsModal('marketing')" 
                    class="text-blue-600 hover:text-blue-800 ml-2 underline"
                  >
                    보기
                  </button>
                </label>
              </div>
            </div>

            <!-- 필수 동의 안내 -->
            <div v-if="!isAllRequiredConsentsChecked" class="mt-3 text-sm text-red-600">
              필수 항목에 모두 동의해주세요.
            </div>
          </div>
        </div>

        <!-- 에러 메시지 -->
        <div v-if="error" class="text-red-600 text-sm bg-red-50 p-3 rounded-md">
          {{ error }}
        </div>

        <!-- 제출 버튼 -->
        <button
          type="submit"
          :disabled="!isFormValid || loading"
          class="w-full bg-blue-600 text-white py-3 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ loading ? '처리중...' : '회원가입' }}
        </button>
      </form>

      <!-- 로그인 링크 -->
      <div class="mt-6 text-center">
        <span class="text-gray-600">이미 계정이 있으신가요?</span>
        <router-link to="/login" class="text-blue-600 hover:text-blue-800 ml-1">
          로그인하기
        </router-link>
      </div>
    </div>

    <!-- 약관 모달 -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg max-w-4xl max-h-[80vh] w-full mx-4 flex flex-col">
        <div class="flex items-center justify-between p-6 border-b">
          <h3 class="text-lg font-semibold">{{ modalTitle }}</h3>
          <button @click="closeModal" class="text-gray-400 hover:text-gray-600">
            <span class="mdi mdi-close text-xl"></span>
          </button>
        </div>
        
        <div class="flex-1 overflow-y-auto p-6">
          <div class="prose prose-sm max-w-none">
            <div v-html="renderMarkdown(modalContent)"></div>
          </div>
        </div>
        
        <div class="flex justify-end gap-3 p-6 border-t">
          <button @click="closeModal" class="btn btn-md btn-secondary">
            확인
          </button>
          <button v-if="!consents[currentModalType]" @click="agreeAndClose" class="btn btn-md btn-primary">
            동의하고 닫기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'
import { AuthService } from '@/services/authService'
import { 
  TERMS_OF_SERVICE, 
  PRIVACY_POLICY, 
  MARKETING_CONSENT,
  type ConsentState,
  isAllRequiredConsentsChecked as checkRequiredConsents
} from '@/constants/terms'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

// 상태
const loading = ref(false)
const error = ref('')
const usernameChecking = ref(false)
const usernameAvailable = ref(false)
const usernameMessage = ref('')
const usernameError = ref(false)
const passwordMismatch = ref(false)

// 약관 동의 상태
const consents = reactive<ConsentState>({
  termsOfService: false,
  privacyPolicy: false,
  marketingConsent: false
})

// 모달 상태
const showModal = ref(false)
const currentModalType = ref<keyof ConsentState>('termsOfService')
const modalTitle = ref('')
const modalContent = ref('')

// 폼 데이터
const form = reactive({
  username: '',
  email: '',
  password: '',
  passwordConfirm: '',
  name: '',
  phone: '',
  address: '',
  user_type: 'general' as 'general' | 'corporate' | 'partner',
  business_license_file: null as File | null,
  terms_agreed: false,
  privacy_agreed: false,
  marketing_agreed: false
})

// 회원 종류 옵션
const userTypes = [
  {
    value: 'general',
    label: '일반회원',
    description: '개인 사용자'
  },
  {
    value: 'corporate',
    label: '기업회원',
    description: '기업 사용자'
  },
  {
    value: 'partner',
    label: '파트너회원',
    description: '파트너사'
  }
]

// 동의 관련 computed
const allAgreed = computed({
  get: () => consents.termsOfService && consents.privacyPolicy && consents.marketingConsent,
  set: (value: boolean) => {
    if (value) {
      toggleAllAgreements()
    }
  }
})

const isAllRequiredConsentsChecked = computed(() => {
  return checkRequiredConsents(consents)
})

// 폼 유효성 검사
const isFormValid = computed(() => {
  return (
    form.username &&
    form.email &&
    form.password &&
    form.password.length >= 8 &&
    form.password === form.passwordConfirm &&
    form.name &&
    isAllRequiredConsentsChecked.value &&
    usernameAvailable.value &&
    (form.user_type === 'general' || form.business_license_file)
  )
})

// 약관 동의 관련 함수
const toggleAllAgreements = () => {
  const allChecked = consents.termsOfService && consents.privacyPolicy && consents.marketingConsent
  consents.termsOfService = !allChecked
  consents.privacyPolicy = !allChecked  
  consents.marketingConsent = !allChecked
}

const openTermsModal = (type: string) => {
  switch (type) {
    case 'terms':
      currentModalType.value = 'termsOfService'
      modalTitle.value = '이용약관'
      modalContent.value = TERMS_OF_SERVICE
      break
    case 'privacy':
      currentModalType.value = 'privacyPolicy'
      modalTitle.value = '개인정보 수집 및 이용 동의'
      modalContent.value = PRIVACY_POLICY
      break
    case 'marketing':
      currentModalType.value = 'marketingConsent'
      modalTitle.value = '마케팅 정보 수신 동의'
      modalContent.value = MARKETING_CONSENT
      break
  }
  
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const agreeAndClose = () => {
  consents[currentModalType.value] = true
  closeModal()
}

const renderMarkdown = (content: string) => {
  // 간단한 마크다운 렌더링
  return content
    .replace(/^# (.*$)/gim, '<h1 class="text-2xl font-bold mb-4">$1</h1>')
    .replace(/^## (.*$)/gim, '<h2 class="text-xl font-semibold mb-3">$1</h2>')
    .replace(/^### (.*$)/gim, '<h3 class="text-lg font-medium mb-2">$1</h3>')
    .replace(/^\*\*(.*)\*\*/gim, '<strong class="font-semibold">$1</strong>')
    .replace(/^\* (.*$)/gim, '<li class="ml-4">• $1</li>')
    .replace(/^(\d+\. .*$)/gim, '<li class="ml-4">$1</li>')
    .replace(/\n/g, '<br>')
}

// 비밀번호 일치 확인
const checkPasswordMatch = () => {
  if (form.passwordConfirm && form.password !== form.passwordConfirm) {
    passwordMismatch.value = true
  } else {
    passwordMismatch.value = false
  }
}

// 아이디 중복 확인
const checkUsername = async () => {
  if (!form.username) {
    usernameMessage.value = '아이디를 입력해주세요.'
    usernameError.value = true
    return
  }

  usernameChecking.value = true
  usernameMessage.value = ''
  usernameError.value = false

  try {
    const result = await AuthService.checkUsernameAvailability(form.username)
    
    if (result.available) {
      usernameAvailable.value = true
      usernameMessage.value = '사용 가능한 아이디입니다.'
      usernameError.value = false
    } else {
      usernameAvailable.value = false
      usernameMessage.value = result.error || '이미 사용 중인 아이디입니다.'
      usernameError.value = true
    }
  } catch (err) {
    usernameAvailable.value = false
    usernameMessage.value = '아이디 중복 확인 중 오류가 발생했습니다.'
    usernameError.value = true
  } finally {
    usernameChecking.value = false
  }
}

// 파일 선택 처리
const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    
    // 파일 크기 검사 (10MB)
    if (file.size > 10 * 1024 * 1024) {
      toast.error('파일 크기는 10MB 이하여야 합니다.')
      return
    }
    
    // 파일 형식 검사
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png']
    if (!allowedTypes.includes(file.type)) {
      toast.error('PDF, JPG, PNG 파일만 업로드 가능합니다.')
      return
    }
    
    form.business_license_file = file
  }
}

// 파일 제거
const removeFile = () => {
  form.business_license_file = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const fileInput = ref<HTMLInputElement>()

// 폼 제출
const handleSubmit = async () => {
  if (!isFormValid.value) {
    error.value = '모든 필수 항목을 입력하고 약관에 동의해주세요.'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const result = await AuthService.signUp({
      username: form.username,
      email: form.email,
      password: form.password,
      name: form.name,
      phone: form.phone || undefined,
      address: form.address || undefined,
      user_type: form.user_type,
      business_license_file: form.business_license_file || undefined,
      terms_agreed: consents.termsOfService,
      privacy_agreed: consents.privacyPolicy,
      marketing_agreed: consents.marketingConsent
    })

    if (result.success) {
      let message = '회원가입이 완료되었습니다.'
      if (form.user_type === 'corporate' || form.user_type === 'partner') {
        message += ' 관리자 승인 후 로그인 가능합니다. (평일 1~2일 소요)'
      } else {
        message += ' 이메일 인증 후 로그인해 주세요.'
      }
      toast.success(message)
      router.push('/auth/verify-email')
    } else {
      error.value = result.error || '회원가입에 실패했습니다.'
    }
  } catch (err) {
    error.value = '회원가입 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

// 컴포넌트 마운트 시 에러 초기화
onMounted(() => {
  authStore.clearError()
})
</script>

