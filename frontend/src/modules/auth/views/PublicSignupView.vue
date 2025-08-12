<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-2xl w-full space-y-8">
      <!-- Header -->
      <div class="text-center">
        <div class="mx-auto h-16 w-16 bg-blue-600 rounded-lg flex items-center justify-center mb-6">
          <svg class="w-10 h-10 text-white" fill="currentColor" viewBox="0 0 20 20">
            <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3z" />
          </svg>
        </div>
        <h1 class="text-4xl font-extrabold text-gray-900 mb-2">
          YCS LMS에 오신 것을 환영합니다
        </h1>
        <p class="text-xl text-gray-600 mb-8">
          글로벌 물류관리 시스템으로 비즈니스를 성장시키세요
        </p>
        
        <!-- Referral Info -->
        <div v-if="referralInfo" class="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-8">
          <div class="flex items-center justify-center mb-4">
            <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mr-4">
              <UserIcon class="w-6 h-6 text-blue-600" />
            </div>
            <div>
              <h3 class="font-semibold text-blue-900">{{ referralInfo.partnerName }}님이 초대했습니다</h3>
              <p class="text-sm text-blue-600">파트너 추천을 통한 특별 혜택</p>
            </div>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="text-center p-4 bg-white rounded-lg">
              <div class="text-2xl font-bold text-blue-600">10%</div>
              <div class="text-sm text-gray-600">수수료 할인</div>
            </div>
            <div class="text-center p-4 bg-white rounded-lg">
              <div class="text-2xl font-bold text-green-600">무료</div>
              <div class="text-sm text-gray-600">첫 3개월 기본료</div>
            </div>
            <div class="text-center p-4 bg-white rounded-lg">
              <div class="text-2xl font-bold text-purple-600">전담</div>
              <div class="text-sm text-gray-600">고객 지원</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Registration Form -->
      <div class="bg-white shadow-xl rounded-lg p-8">
        <h2 class="text-2xl font-bold text-center text-gray-900 mb-6">무료 계정 만들기</h2>
        
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Basic Info -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">이름</label>
              <input
                v-model="form.name"
                type="text"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="홍길동"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">이메일</label>
              <input
                v-model="form.email"
                type="email"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="hong@example.com"
              />
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">전화번호</label>
              <input
                v-model="form.phone"
                type="tel"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="010-1234-5678"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">비밀번호</label>
              <input
                v-model="form.password"
                type="password"
                required
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="8자 이상"
              />
            </div>
          </div>

          <!-- Account Type -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-4">계정 유형</label>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div
                class="border rounded-lg p-4 cursor-pointer transition-all"
                :class="{
                  'border-blue-500 bg-blue-50': form.role === 'individual',
                  'border-gray-300 hover:border-gray-400': form.role !== 'individual'
                }"
                @click="form.role = 'individual'"
              >
                <input
                  v-model="form.role"
                  value="individual"
                  type="radio"
                  class="sr-only"
                />
                <div class="flex items-center">
                  <UserIcon class="w-6 h-6 text-blue-600 mr-3" />
                  <div>
                    <h3 class="font-medium">개인</h3>
                    <p class="text-sm text-gray-600">개인 수입/수출</p>
                  </div>
                </div>
              </div>

              <div
                class="border rounded-lg p-4 cursor-pointer transition-all"
                :class="{
                  'border-blue-500 bg-blue-50': form.role === 'enterprise',
                  'border-gray-300 hover:border-gray-400': form.role !== 'enterprise'
                }"
                @click="form.role = 'enterprise'"
              >
                <input
                  v-model="form.role"
                  value="enterprise"
                  type="radio"
                  class="sr-only"
                />
                <div class="flex items-center">
                  <BuildingOfficeIcon class="w-6 h-6 text-green-600 mr-3" />
                  <div>
                    <h3 class="font-medium">기업</h3>
                    <p class="text-sm text-gray-600">법인사업자</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Enterprise Info -->
          <div v-if="form.role === 'enterprise'" class="space-y-4 bg-gray-50 p-6 rounded-lg">
            <h3 class="font-medium text-gray-900">기업 정보</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">회사명</label>
                <input
                  v-model="form.companyName"
                  type="text"
                  :required="form.role === 'enterprise'"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="(주)글로벌무역"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">사업자번호</label>
                <input
                  v-model="form.businessNumber"
                  type="text"
                  :required="form.role === 'enterprise'"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="123-45-67890"
                />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">사업장 주소</label>
              <input
                v-model="form.businessAddress"
                type="text"
                :required="form.role === 'enterprise'"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                placeholder="서울시 강남구 테헤란로 123"
              />
            </div>
          </div>

          <!-- Terms -->
          <div class="space-y-4">
            <label class="flex items-start">
              <input
                v-model="form.agreeTerms"
                type="checkbox"
                required
                class="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <span class="ml-2 text-sm text-gray-600">
                <router-link to="/terms" class="text-blue-600 hover:text-blue-500">이용약관</router-link> 및 
                <router-link to="/privacy" class="text-blue-600 hover:text-blue-500">개인정보처리방침</router-link>에 동의합니다.
              </span>
            </label>
            
            <label class="flex items-start">
              <input
                v-model="form.agreeMarketing"
                type="checkbox"
                class="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <span class="ml-2 text-sm text-gray-600">
                마케팅 정보 수신에 동의합니다. (선택)
              </span>
            </label>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="loading || !form.agreeTerms"
            class="w-full py-3 px-4 border border-transparent text-sm font-medium rounded-lg text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="loading">가입 처리 중...</span>
            <span v-else>무료로 시작하기</span>
          </button>

          <!-- Login Link -->
          <div class="text-center">
            <p class="text-sm text-gray-600">
              이미 계정이 있으신가요?
              <router-link 
                :to="{ name: 'Login' }" 
                class="font-medium text-blue-600 hover:text-blue-500 ml-1"
              >
                로그인하기
              </router-link>
            </p>
          </div>
        </form>
      </div>

      <!-- Features Section -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-12">
        <div class="text-center">
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mx-auto mb-4">
            <TruckIcon class="w-6 h-6 text-blue-600" />
          </div>
          <h3 class="font-semibold text-gray-900 mb-2">빠른 배송</h3>
          <p class="text-sm text-gray-600">항공/해상 운송으로 최적의 배송 솔루션</p>
        </div>
        
        <div class="text-center">
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center mx-auto mb-4">
            <CurrencyDollarIcon class="w-6 h-6 text-green-600" />
          </div>
          <h3 class="font-semibold text-gray-900 mb-2">투명한 요금</h3>
          <p class="text-sm text-gray-600">숨겨진 비용 없는 명확한 가격 체계</p>
        </div>
        
        <div class="text-center">
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mx-auto mb-4">
            <ChatBubbleLeftRightIcon class="w-6 h-6 text-purple-600" />
          </div>
          <h3 class="font-semibold text-gray-900 mb-2">24/7 지원</h3>
          <p class="text-sm text-gray-600">언제든지 도움받을 수 있는 고객 지원</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'
import {
  UserIcon,
  BuildingOfficeIcon,
  TruckIcon,
  CurrencyDollarIcon,
  ChatBubbleLeftRightIcon
} from '@heroicons/vue/24/outline'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

// State
const loading = ref(false)
const referralInfo = ref<{
  partnerName: string
  partnerCode: string
} | null>(null)

const form = reactive({
  name: '',
  email: '',
  phone: '',
  password: '',
  role: 'individual' as 'individual' | 'enterprise',
  companyName: '',
  businessNumber: '',
  businessAddress: '',
  referralCode: '',
  agreeTerms: false,
  agreeMarketing: false
})

// Methods
const loadReferralInfo = async () => {
  const code = route.query.ref as string
  if (!code) return

  try {
    // Mock referral info lookup
    referralInfo.value = {
      partnerName: '김파트너',
      partnerCode: code
    }
    form.referralCode = code
    
  } catch (error) {
    console.error('Failed to load referral info:', error)
  }
}

const handleSubmit = async () => {
  try {
    loading.value = true

    const registrationData = {
      ...form,
      partnerType: undefined // Not needed for public signup
    }

    const response = await authStore.register(registrationData)
    
    if (response.success) {
      if (form.role === 'enterprise') {
        toast.success('기업 계정이 생성되었습니다. 승인을 위해 평일 1-2일이 소요됩니다.')
        router.push({ name: 'Approval' })
      } else {
        toast.success('계정이 성공적으로 생성되었습니다!')
        router.push({ name: 'Dashboard' })
      }
    }
  } catch (error: any) {
    toast.error('회원가입 중 오류가 발생했습니다.')
    console.error('Registration error:', error)
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  loadReferralInfo()
})
</script>

<style scoped>
/* Custom styles */
</style>