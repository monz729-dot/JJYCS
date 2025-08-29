<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 p-4">
    <!-- 헤더 -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <h1 class="text-2xl font-bold text-blue-900">마이페이지</h1>
      <p class="text-sm text-gray-600 mt-1">회원 정보를 확인하고 수정할 수 있습니다</p>
    </div>

    <!-- 기본 정보 섹션 -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <h2 class="text-lg font-semibold text-blue-900 mb-4">기본 정보</h2>
      
      <div class="space-y-4">
        <!-- 이름 (읽기 전용) -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">이름</label>
          <input 
            type="text" 
            :value="formData.name"
            disabled
            class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-500 cursor-not-allowed"
          />
        </div>

        <!-- 이메일 변경 섹션 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">이메일</label>
          <div class="flex gap-2">
            <input 
              type="email" 
              v-model="formData.email"
              :disabled="!isEmailEditing"
              class="flex-1 px-3 py-2 border border-gray-300 rounded-lg"
              :class="{ 'bg-gray-50 text-gray-500': !isEmailEditing }"
            />
            <button
              v-if="!isEmailEditing"
              @click="startEmailEdit"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              변경
            </button>
            <button
              v-else
              @click="cancelEmailEdit"
              class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            >
              취소
            </button>
          </div>
          
          <!-- 이메일 변경 폼 -->
          <div v-if="isEmailEditing" class="mt-3 p-4 bg-blue-50 rounded-lg border border-blue-200">
            <p class="text-sm text-blue-700 mb-3">
              이메일 변경 시 인증이 필요합니다. 새 이메일로 인증 메일이 발송됩니다.
            </p>
            <button
              @click="requestEmailChange"
              :disabled="!isEmailValid || formData.email === originalEmail"
              class="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              인증 메일 발송
            </button>
          </div>

          <!-- 이메일 인증 대기 상태 -->
          <div v-if="emailVerificationPending" class="mt-3 p-4 bg-yellow-50 rounded-lg border border-yellow-200">
            <p class="text-sm text-yellow-700">
              인증 메일이 발송되었습니다. 이메일을 확인해주세요.
            </p>
          </div>
        </div>

        <!-- 휴대폰 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">휴대폰</label>
          <input 
            type="tel" 
            v-model="formData.phone"
            placeholder="010-0000-0000"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <!-- 주소 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">주소</label>
          <div class="space-y-2">
            <div class="flex gap-2">
              <input 
                type="text" 
                v-model="formData.zipCode"
                placeholder="우편번호"
                readonly
                class="w-32 px-3 py-2 border border-gray-300 rounded-lg bg-gray-50"
              />
              <button
                @click="searchAddress"
                class="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700"
              >
                주소 검색
              </button>
            </div>
            <input 
              type="text" 
              v-model="formData.address"
              placeholder="기본 주소"
              readonly
              class="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50"
            />
            <input 
              type="text" 
              v-model="formData.addressDetail"
              placeholder="상세 주소"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 비밀번호 변경 섹션 -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <h2 class="text-lg font-semibold text-blue-900 mb-4">비밀번호 변경</h2>
      
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">현재 비밀번호</label>
          <input 
            type="password" 
            v-model="passwordData.current"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">새 비밀번호</label>
          <input 
            type="password" 
            v-model="passwordData.new"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <p class="text-xs text-gray-500 mt-1">8자 이상, 영문/숫자/특수문자 포함</p>
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">새 비밀번호 확인</label>
          <input 
            type="password" 
            v-model="passwordData.confirm"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        
        <button
          @click="changePassword"
          :disabled="!isPasswordValid"
          class="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          비밀번호 변경
        </button>
      </div>
    </div>

    <!-- 알림 설정 섹션 -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <h2 class="text-lg font-semibold text-blue-900 mb-4">알림 설정</h2>
      
      <div class="space-y-3">
        <label class="flex items-center gap-3">
          <input 
            type="checkbox" 
            v-model="formData.notifications.order"
            class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
          />
          <span class="text-sm text-gray-700">주문 상태 알림</span>
        </label>
        
        <label class="flex items-center gap-3">
          <input 
            type="checkbox" 
            v-model="formData.notifications.shipping"
            class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
          />
          <span class="text-sm text-gray-700">배송 알림</span>
        </label>
        
        <label class="flex items-center gap-3">
          <input 
            type="checkbox" 
            v-model="formData.notifications.marketing"
            class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
          />
          <span class="text-sm text-gray-700">마케팅 정보 수신 동의</span>
        </label>
      </div>
    </div>

    <!-- 저장 버튼 -->
    <div class="flex gap-3">
      <button
        @click="saveProfile"
        class="flex-1 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
      >
        저장하기
      </button>
      <button
        @click="cancel"
        class="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-medium"
      >
        취소
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()
const { showToast } = useToast()

// Axios 설정
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

// 요청 인터셉터 - 토큰 추가
api.interceptors.request.use(
  (config) => {
    const token = authStore.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 폼 데이터
const formData = ref({
  name: '',
  email: '',
  phone: '',
  zipCode: '',
  address: '',
  addressDetail: '',
  notifications: {
    order: true,
    shipping: true,
    marketing: false
  }
})

// 비밀번호 변경 데이터
const passwordData = ref({
  current: '',
  new: '',
  confirm: ''
})

// 이메일 변경 상태
const isEmailEditing = ref(false)
const originalEmail = ref('')
const emailVerificationPending = ref(false)

// 유효성 검사
const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(formData.value.email)
})

const isPasswordValid = computed(() => {
  const { current, new: newPwd, confirm } = passwordData.value
  if (!current || !newPwd || !confirm) return false
  if (newPwd !== confirm) return false
  if (newPwd.length < 8) return false
  
  // 영문, 숫자, 특수문자 포함 검사
  const hasLetter = /[a-zA-Z]/.test(newPwd)
  const hasNumber = /[0-9]/.test(newPwd)
  const hasSpecial = /[!@#$%^&*]/.test(newPwd)
  
  return hasLetter && hasNumber && hasSpecial
})

// 사용자 정보 로드
const loadUserInfo = async () => {
  try {
    const response = await api.get('/user/profile')
    const userData = response.data.data
    
    formData.value = {
      name: userData.name || '',
      email: userData.email || '',
      phone: userData.phone || '',
      zipCode: userData.zipCode || '',
      address: userData.address || '',
      addressDetail: userData.addressDetail || '',
      notifications: userData.notifications || {
        order: true,
        shipping: true,
        marketing: false
      }
    }
    
    originalEmail.value = userData.email
  } catch (error) {
    console.error('Failed to load user info:', error)
    showToast('사용자 정보를 불러오는데 실패했습니다.', 'error')
  }
}

// 이메일 변경 시작
const startEmailEdit = () => {
  isEmailEditing.value = true
}

// 이메일 변경 취소
const cancelEmailEdit = () => {
  isEmailEditing.value = false
  formData.value.email = originalEmail.value
  emailVerificationPending.value = false
}

// 이메일 변경 요청
const requestEmailChange = async () => {
  if (!isEmailValid.value || formData.value.email === originalEmail.value) {
    return
  }
  
  try {
    await api.post('/user/email/change-request', {
      newEmail: formData.value.email
    })
    
    emailVerificationPending.value = true
    isEmailEditing.value = false
    showToast('인증 메일이 발송되었습니다. 이메일을 확인해주세요.', 'success')
  } catch (error: any) {
    if (error.response?.data?.message) {
      showToast(error.response.data.message, 'error')
    } else {
      showToast('이메일 변경 요청에 실패했습니다.', 'error')
    }
  }
}

// 비밀번호 변경
const changePassword = async () => {
  if (!isPasswordValid.value) {
    showToast('비밀번호를 확인해주세요.', 'error')
    return
  }
  
  try {
    await api.post('/user/password/change', {
      currentPassword: passwordData.value.current,
      newPassword: passwordData.value.new
    })
    
    passwordData.value = {
      current: '',
      new: '',
      confirm: ''
    }
    
    showToast('비밀번호가 변경되었습니다.', 'success')
  } catch (error: any) {
    if (error.response?.data?.message) {
      showToast(error.response.data.message, 'error')
    } else {
      showToast('비밀번호 변경에 실패했습니다.', 'error')
    }
  }
}

// 주소 검색
const searchAddress = () => {
  // Daum 우편번호 API 연동
  if (window.daum && window.daum.Postcode) {
    new window.daum.Postcode({
      oncomplete: (data: any) => {
        formData.value.zipCode = data.zonecode
        formData.value.address = data.roadAddress || data.jibunAddress
      }
    }).open()
  } else {
    showToast('주소 검색 서비스를 사용할 수 없습니다.', 'error')
  }
}

// 프로필 저장
const saveProfile = async () => {
  try {
    const updateData = {
      phone: formData.value.phone,
      zipCode: formData.value.zipCode,
      address: formData.value.address,
      addressDetail: formData.value.addressDetail,
      notifications: formData.value.notifications
    }
    
    await api.put('/user/profile', updateData)
    showToast('프로필이 저장되었습니다.', 'success')
    
    // 사용자 정보 재로드
    await authStore.fetchUserProfile()
  } catch (error: any) {
    if (error.response?.data?.message) {
      showToast(error.response.data.message, 'error')
    } else {
      showToast('프로필 저장에 실패했습니다.', 'error')
    }
  }
}

// 취소
const cancel = () => {
  router.push('/dashboard')
}

// 초기화
onMounted(() => {
  // 현재 로그인된 사용자 정보로 초기화
  if (authStore.user) {
    formData.value.name = authStore.user.name || ''
    formData.value.email = authStore.user.email || ''
    originalEmail.value = authStore.user.email || ''
  }
  
  // 전체 사용자 정보 로드
  loadUserInfo()
})

// Window 타입 확장
declare global {
  interface Window {
    daum: any
  }
}
</script>