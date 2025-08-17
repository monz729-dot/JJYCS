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

        <!-- Referral Info (자식 컴포넌트로 분리, 마크업 동일) -->
        <ReferralInfo :referral-info="referralInfo" />
      </div>

      <!-- Registration Form -->
      <div class="bg-white shadow-xl rounded-lg p-8">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Account Type -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-4">계정 유형 *</label>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div
                  class="border rounded-lg p-4 cursor-pointer transition-all"
                  :class="{
                  'border-blue-500 bg-blue-50': form.user_type === 'general',
                  'border-gray-300 hover:border-gray-400': form.user_type !== 'general'
                }"
                  @click="selectUserType('general')"
              >
                <input
                    v-model="form.user_type"
                    value="general"
                    type="radio"
                    class="sr-only"
                />
                <div class="flex items-center">
                  <UserIcon class="w-6 h-6 text-blue-600 mr-3" />
                  <div>
                    <h3 class="font-medium">일반회원</h3>
                    <p class="text-sm text-gray-600">개인 수입/수출</p>
                  </div>
                </div>
              </div>

              <div
                  class="border rounded-lg p-4 cursor-pointer transition-all"
                  :class="{
                  'border-blue-500 bg-blue-50': form.user_type === 'corporate',
                  'border-gray-300 hover:border-gray-400': form.user_type !== 'corporate'
                }"
                  @click="selectUserType('corporate')"
              >
                <input
                    v-model="form.user_type"
                    value="corporate"
                    type="radio"
                    class="sr-only"
                />
                <div class="flex items-center">
                  <BuildingOfficeIcon class="w-6 h-6 text-green-600 mr-3" />
                  <div>
                    <h3 class="font-medium">기업회원</h3>
                    <p class="text-sm text-gray-600">법인사업자</p>
                  </div>
                </div>
              </div>

              <div
                  class="border rounded-lg p-4 cursor-pointer transition-all"
                  :class="{
                  'border-blue-500 bg-blue-50': form.user_type === 'partner',
                  'border-gray-300 hover:border-gray-400': form.user_type !== 'partner'
                }"
                  @click="selectUserType('partner')"
              >
                <input
                    v-model="form.user_type"
                    value="partner"
                    type="radio"
                    class="sr-only"
                />
                <div class="flex items-center">
                  <UserGroupIcon class="w-6 h-6 text-purple-600 mr-3" />
                  <div>
                    <h3 class="font-medium">파트너회원</h3>
                    <p class="text-sm text-gray-600">파트너사</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 아이디 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">아이디 *</label>
            <div class="space-y-2">
              <input
                  v-model="form.username"
                  type="text"
                  required
                  :disabled="checkingUsername"
                  class="form-input form-input-md"
                  placeholder="사용할 아이디"
                  @blur="checkUsername"
              />
              <button
                  type="button"
                  @click="checkUsername"
                  :disabled="checkingUsername || !form.username"
                  class="btn btn-md btn-primary"
              >
                {{ checkingUsername ? '확인중...' : '중복확인' }}
              </button>
            </div>
            <div v-if="usernameMessage" class="mt-1 text-sm" :class="usernameAvailable ? 'text-green-600' : 'text-red-600'">
              {{ usernameMessage }}
            </div>
          </div>

          <!-- 비밀번호 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">비밀번호 *</label>
            <div class="relative">
              <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  required
                  class="form-input form-input-md pr-10"
                  placeholder="8자 이상, 영문/숫자/특수문자 포함"
                  @input="checkPasswordStrength"
              />
              <button
                  type="button"
                  @click="togglePassword"
                  class="absolute inset-y-0 right-0 pr-3 flex items-center"
              >
                <span :class="showPassword ? 'mdi mdi-eye-off' : 'mdi mdi-eye'" class="text-gray-400"></span>
              </button>
            </div>
            <div v-if="form.password" class="mt-2">
              <div class="flex items-center gap-2">
                <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div
                      class="h-full transition-all duration-300"
                      :class="passwordStrengthClass"
                      :style="{ width: passwordStrengthPercent + '%' }"
                  ></div>
                </div>
                <span class="text-xs" :class="passwordStrengthTextClass">{{ passwordStrengthText }}</span>
              </div>
              <div v-if="passwordErrors.length > 0" class="mt-1 text-xs text-gray-600">
                <div v-for="error in passwordErrors" :key="error" class="flex items-center gap-1">
                  <span class="mdi mdi-close-circle text-red-500"></span>
                  {{ error }}
                </div>
              </div>
            </div>
          </div>

          <!-- 비밀번호 확인 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">비밀번호 확인 *</label>
            <input
                v-model="form.passwordConfirm"
                type="password"
                required
                :class="[
                'w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500',
                passwordMismatch ? 'border-red-500' : 'border-gray-300'
              ]"
                placeholder="비밀번호를 다시 입력하세요"
                @blur="checkPasswordMatch"
            />
            <div v-if="passwordMismatch" class="mt-1 text-sm text-red-600">
              비밀번호가 일치하지 않습니다.
            </div>
          </div>

          <!-- 이름 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">이름 *</label>
            <input
                v-model="form.name"
                type="text"
                required
                class="form-input form-input-md"
                placeholder="홍길동"
            />
          </div>

          <!-- 전화번호 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">전화번호 *</label>
            <input
                v-model="form.phone"
                type="tel"
                required
                class="form-input form-input-md"
                placeholder="010-1234-5678"
                @input="formatPhoneNumber"
                maxlength="13"
            />
            <div v-if="phoneError" class="mt-1 text-sm text-red-600">
              {{ phoneError }}
            </div>
          </div>

          <!-- 이메일 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">이메일 *</label>
            <input
                v-model="form.email"
                type="email"
                required
                class="form-input form-input-md"
                placeholder="hong@example.com"
            />
            <div class="mt-1 text-sm text-gray-500">
              회원가입 시 인증 메일이 자동으로 발송됩니다.
            </div>
          </div>

          <!-- 이메일 인증 토큰 입력 -->
          <div v-if="registrationCompleted" class="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div class="flex items-start">
              <div class="flex-shrink-0">
               <svg class="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                 <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
               </svg>
              </div>
              <div class="ml-3 w-full">
                <h3 class="text-sm font-medium text-blue-800 mb-3">
                  이메일 인증 토큰을 입력해주세요
                </h3>
                <div class="space-y-3">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">인증 토큰 *</label>
                    <input
                        v-model="verificationToken"
                        type="text"
                        required
                        maxlength="6"
                        class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-center text-lg font-mono tracking-widest"
                        placeholder="000000"
                        @input="formatToken"
                    />
                    <button
                        type="button"
                        @click="verifyToken"
                        :disabled="verifyingToken || !verificationToken || verificationToken.length !== 6"
                        class="btn btn-md btn-primary w-full mt-2"
                    >
                      {{ verifyingToken ? '확인중...' : '인증하기' }}
                    </button>
                  </div>
                  <div class="text-sm text-blue-700">
                    <p>{{ form.email }}로 인증 토큰을 발송했습니다.</p>
                    <p class="mt-1">이메일을 확인하여 6자리 토큰을 입력해주세요.</p>
                  </div>
                  <div class="flex justify-between items-center">
                    <button
                        type="button"
                        @click="resendVerificationToken"
                        :disabled="resendCooldown > 0"
                        class="text-sm font-medium text-blue-800 hover:text-blue-600 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <span v-if="resendCooldown > 0">{{ resendCooldown }}초 후 재발송</span>
                      <span v-else>토큰 재발송</span>
                    </button>
                    <div v-if="tokenVerified" class="flex items-center text-green-600">
                      <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                      </svg>
                      <span class="text-sm font-medium">인증 완료</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 주소 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">주소</label>
            <input
                v-model="form.address"
                type="text"
                class="form-input form-input-md"
                placeholder="서울시 강남구 테헤란로 123"
            />
          </div>

          <!-- Enterprise/Partner Info -->
          <div v-if="form.user_type === 'corporate' || form.user_type === 'partner'" class="space-y-4 bg-gray-50 p-6 rounded-lg">
            <h3 class="font-medium text-gray-900">{{ form.user_type === 'corporate' ? '기업' : '파트너' }} 정보</h3>
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">회사명 *</label>
                <input
                    v-model="form.company_name"
                    type="text"
                    :required="form.user_type === 'corporate' || form.user_type === 'partner'"
                    class="form-input form-input-md"
                    placeholder="(주)글로벌무역"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">사업자번호</label>
                <input
                    v-model="form.business_number"
                    type="text"
                    class="form-input form-input-md"
                    placeholder="123-45-67890"
                />
              </div>
            </div>
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">담당자명</label>
                <input
                    v-model="form.manager_name"
                    type="text"
                    class="form-input form-input-md"
                    placeholder="담당자 이름"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">담당자 연락처</label>
                <input
                    v-model="form.manager_contact"
                    type="tel"
                    class="form-input form-input-md"
                    placeholder="010-1234-5678"
                />
              </div>
            </div>
            <div v-if="form.user_type === 'corporate'">
              <label class="block text-sm font-medium text-gray-700 mb-2">사업자등록증 *</label>
              <div class="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-lg">
                <div class="space-y-1 text-center">
                  <svg class="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48">
                    <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                  <div class="flex text-sm text-gray-600">
                    <label for="file-upload" class="relative cursor-pointer bg-white rounded-md font-medium text-blue-600 hover:text-blue-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-blue-500">
                      <span>파일 업로드</span>
                      <input
                          id="file-upload"
                          ref="fileInput"
                          type="file"
                          accept=".pdf,.jpg,.jpeg,.png"
                          class="sr-only"
                          @change="handleFileUpload"
                          required
                      />
                    </label>
                    <p class="pl-1">또는 드래그 앤 드롭</p>
                  </div>
                  <p class="text-xs text-gray-500">PDF, JPG, PNG 파일 (최대 10MB)</p>
                </div>
              </div>
              <div v-if="selectedFile" class="mt-2 text-sm text-gray-600">
                선택된 파일: {{ selectedFile.name }}
              </div>
            </div>
          </div>

          <!-- Terms -->
          <div class="space-y-4">
            <label class="flex items-start">
              <input
                  v-model="form.terms_agreed"
                  type="checkbox"
                  required
                  class="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <span class="ml-2 text-sm text-gray-600">
                <button type="button" @click="openTermsModal('terms')" class="text-blue-600 hover:text-blue-500 underline">이용약관</button>에 동의합니다. *
              </span>
            </label>

            <label class="flex items-start">
              <input
                  v-model="form.privacy_agreed"
                  type="checkbox"
                  required
                  class="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <span class="ml-2 text-sm text-gray-600">
                <button type="button" @click="openTermsModal('privacy')" class="text-blue-600 hover:text-blue-500 underline">개인정보 수집 및 이용</button>에 동의합니다. *
              </span>
            </label>
          </div>

          <!-- Submit Button -->
          <button
              type="submit"
              :disabled="loading || !form.terms_agreed || !form.privacy_agreed || !usernameAvailable || passwordMismatch || form.password.length < 8 || form.password !== form.passwordConfirm || (registrationCompleted && !tokenVerified)"
              class="btn btn-lg btn-primary w-full"
          >
            <span v-if="loading">가입 처리 중...</span>
            <span v-else-if="registrationCompleted && !tokenVerified">토큰 인증 후 회원가입</span>
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
    </div>

    <!-- 약관 모달 -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-lg max-w-4xl max-h-[80vh] w-full flex flex-col">
        <div class="flex items-center justify-between p-6 border-b">
          <h3 class="text-lg font-semibold">{{ modalTitle }}</h3>
          <button @click="closeModal" class="text-gray-400 hover:text-gray-600">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
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
          <button v-if="!getConsentValue()" @click="agreeAndClose" class="btn btn-md btn-primary">
            동의하고 닫기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './store.ts'
import { useToast } from '@/composables/useToast'
import ReferralInfo from './ReferralInfo.vue'
import {
  UserIcon,
  BuildingOfficeIcon,
  UserGroupIcon
} from '@heroicons/vue/24/outline'
import { AuthService } from './api'
import { TERMS_OF_SERVICE, PRIVACY_POLICY } from '@/constants/terms'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

// State
const loading = ref(false)
const checkingUsername = ref(false)
const usernameAvailable = ref(false)
const usernameMessage = ref('')
const selectedFile = ref<File | null>(null)
const referralInfo = ref<{ partnerName: string; partnerCode: string } | null>(null)
const showPassword = ref(false)
const phoneError = ref('')
const passwordStrength = ref(0)
const passwordErrors = ref<string[]>([])

// 이메일 인증 관련 상태
const registrationCompleted = ref(false)
const resendCooldown = ref(0)
const verificationToken = ref('')
const verifyingToken = ref(false)
const tokenVerified = ref(false)
let cooldownTimer: ReturnType<typeof setInterval> | null = null

const passwordMismatch = ref(false)

// 모달 상태
const showModal = ref(false)
const currentModalType = ref<'terms' | 'privacy'>('terms')
const modalTitle = ref('')
const modalContent = ref('')

const form = reactive({
  username: '',
  name: '',
  email: '',
  phone: '',
  password: '',
  passwordConfirm: '',
  address: '',
  user_type: 'general' as 'general' | 'corporate' | 'partner',
  company_name: '',
  business_number: '',
  manager_name: '',
  manager_contact: '',
  business_license_file: null as File | null,
  terms_agreed: false,
  privacy_agreed: false,
  referralCode: ''
})

// ===== functions (모두 function 선언식) =====
function selectUserType(type: 'general'|'corporate'|'partner') {
  form.user_type = type
}

function checkPasswordMatch() {
  if (form.passwordConfirm && form.password !== form.passwordConfirm) {
    passwordMismatch.value = true
  } else {
    passwordMismatch.value = false
  }
}

function openTermsModal(type: 'terms' | 'privacy') {
  currentModalType.value = type
  switch (type) {
    case 'terms':
      modalTitle.value = '이용약관'
      modalContent.value = TERMS_OF_SERVICE
      break
    case 'privacy':
      modalTitle.value = '개인정보 수집 및 이용 동의'
      modalContent.value = PRIVACY_POLICY
      break
  }
  showModal.value = true
}
function closeModal() { showModal.value = false }
function agreeAndClose() {
  if (currentModalType.value === 'terms') { form.terms_agreed = true }
  else if (currentModalType.value === 'privacy') { form.privacy_agreed = true }
  closeModal()
}
function getConsentValue() {
  if (currentModalType.value === 'terms') return form.terms_agreed
  if (currentModalType.value === 'privacy') return form.privacy_agreed
  return false
}

function renderMarkdown(content: string) {
  return content
      .replace(/^# (.*$)/gim, '<h1 class="text-2xl font-bold mb-4">$1</h1>')
      .replace(/^## (.*$)/gim, '<h2 class="text-xl font-semibold mb-3">$1</h2>')
      .replace(/^### (.*$)/gim, '<h3 class="text-lg font-medium mb-2">$1</h3>')
      .replace(/\*\*(.*)\*\*/gim, '<strong class="font-semibold">$1</strong>')
      .replace(/^\* (.*$)/gim, '<li class="ml-4">• $1</li>')
      .replace(/^(\d+\. .*$)/gim, '<li class="ml-4">$1</li>')
      .replace(/\n/g, '<br>')
}

async function loadReferralInfo() {
  const code = route.query.ref as string
  if (!code) return
  referralInfo.value = { partnerName: '김파트너', partnerCode: code }
  form.referralCode = code
}

async function checkUsername() {
  if (!form.username) {
    usernameMessage.value = ''
    usernameAvailable.value = false
    return
  }
  checkingUsername.value = true
  usernameMessage.value = ''
  try {
    const result = await authStore.checkUsernameAvailability(form.username)
    usernameAvailable.value = result.available
    usernameMessage.value = result.available
        ? '사용 가능한 아이디입니다.'
        : result.error || '이미 사용 중인 아이디입니다.'
  } catch (error) {
    usernameAvailable.value = false
    usernameMessage.value = '아이디 확인 중 오류가 발생했습니다.'
  } finally {
    checkingUsername.value = false
  }
}

function handleFileUpload(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    if (file.size > 10 * 1024 * 1024) {
      toast.error('파일 크기는 10MB 이하여야 합니다.')
      return
    }
    selectedFile.value = file
    form.business_license_file = file
  }
}

function formatToken(event: Event) {
  const target = event.target as HTMLInputElement
  let value = target.value.replace(/\D/g, '')
  value = value.substring(0, 6)
  verificationToken.value = value
}

async function verifyToken() {
  if (!verificationToken.value || verificationToken.value.length !== 6) {
    toast.error('6자리 토큰을 입력해주세요.')
    return
  }
  verifyingToken.value = true
  try {
    const result = await AuthService.verifyEmail(form.email, verificationToken.value)
    if (result.success) {
      tokenVerified.value = true
      toast.success('이메일 인증이 완료되었습니다!')
      router.push({ name: 'Login', query: { emailVerified: 'true' } })
    } else {
      toast.error(result.error || '토큰 인증에 실패했습니다.')
    }
  } catch (error) {
    toast.error('토큰 인증에 실패했습니다.')
  } finally {
    verifyingToken.value = false
  }
}

async function resendVerificationToken() {
  if (!form.email) {
    toast.error('이메일 주소를 입력해주세요.')
    return
  }
  try {
    const result = await AuthService.resendVerificationToken(form.email)
    if (result.success) {
      resendCooldown.value = 60
      startCooldown()
      toast.success('인증 토큰을 재발송했습니다.')
    } else {
      toast.error(result.error || '인증 토큰 재발송에 실패했습니다.')
    }
  } catch (error) {
    toast.error('인증 토큰 재발송에 실패했습니다.')
  }
}

function startCooldown() {
  if (cooldownTimer) clearInterval(cooldownTimer)
  cooldownTimer = setInterval(function () {
    resendCooldown.value--
    if (resendCooldown.value <= 0 && cooldownTimer) {
      clearInterval(cooldownTimer)
      cooldownTimer = null
    }
  }, 1000)
}

function formatPhoneNumber(event: Event) {
  const input = event.target as HTMLInputElement
  let value = input.value.replace(/[^0-9]/g, '')
  if (value.startsWith('010') || value.startsWith('011') || value.startsWith('016') || value.startsWith('017') || value.startsWith('018') || value.startsWith('019')) {
    if (value.length > 3 && value.length <= 7) {
      value = value.slice(0, 3) + '-' + value.slice(3)
    } else if (value.length > 7) {
      value = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11)
    }
  } else if (value.startsWith('02')) {
    if (value.length > 2 && value.length <= 5) {
      value = value.slice(0, 2) + '-' + value.slice(2)
    } else if (value.length > 5 && value.length <= 9) {
      value = value.slice(0, 2) + '-' + value.slice(2, 5) + '-' + value.slice(5)
    } else if (value.length > 9) {
      value = value.slice(0, 2) + '-' + value.slice(2, 6) + '-' + value.slice(6, 10)
    }
  } else if (value.startsWith('0')) {
    if (value.length > 3 && value.length <= 6) {
      value = value.slice(0, 3) + '-' + value.slice(3)
    } else if (value.length > 6 && value.length <= 10) {
      value = value.slice(0, 3) + '-' + value.slice(3, 6) + '-' + value.slice(6)
    } else if (value.length > 10) {
      value = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11)
    }
  }
  form.phone = value
  const phoneRegex = /^(010|011|016|017|018|019)-\d{3,4}-\d{4}$|^(02|0[3-9]{1}[0-9]{1})-\d{3,4}-\d{4}$/
  if (value.length > 0 && !phoneRegex.test(value) && value.length >= 12) {
    phoneError.value = '올바른 전화번호 형식이 아닙니다'
  } else {
    phoneError.value = ''
  }
}

function checkPasswordStrength() {
  const password = form.password
  passwordErrors.value = []
  let strength = 0
  if (password.length >= 8) { strength += 25 } else { passwordErrors.value.push('최소 8자 이상') }
  if (/[a-zA-Z]/.test(password)) { strength += 25 } else { passwordErrors.value.push('영문 포함') }
  if (/[0-9]/.test(password)) { strength += 25 } else { passwordErrors.value.push('숫자 포함') }
  if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) { strength += 25 } else { passwordErrors.value.push('특수문자 포함') }
  passwordStrength.value = strength
}

const passwordStrengthPercent = computed(function () { return passwordStrength.value })
const passwordStrengthClass = computed(function () {
  if (passwordStrength.value <= 25) return 'bg-red-500'
  if (passwordStrength.value <= 50) return 'bg-orange-500'
  if (passwordStrength.value <= 75) return 'bg-yellow-500'
  return 'bg-green-500'
})
const passwordStrengthText = computed(function () {
  if (passwordStrength.value <= 25) return '약함'
  if (passwordStrength.value <= 50) return '보통'
  if (passwordStrength.value <= 75) return '강함'
  return '매우 강함'
})
const passwordStrengthTextClass = computed(function () {
  if (passwordStrength.value <= 25) return 'text-red-600'
  if (passwordStrength.value <= 50) return 'text-orange-600'
  if (passwordStrength.value <= 75) return 'text-yellow-600'
  return 'text-green-600'
})

async function handleSubmit() {
  try {
    loading.value = true

    let business_license_url = ''
    if (form.business_license_file) {
      business_license_url = 'mock_url_' + Date.now()
    }

    const registrationData = {
      username: form.username,
      name: form.name,
      email: form.email,
      phone: form.phone,
      password: form.password,
      address: form.address,
      user_type: form.user_type,
      company_name: form.company_name,
      business_number: form.business_number,
      manager_name: form.manager_name,
      manager_contact: form.manager_contact,
      business_license_url,
      terms_agreed: form.terms_agreed,
      privacy_agreed: form.privacy_agreed,
      referralCode: form.referralCode || undefined
    }

    if (tokenVerified.value) {
      // 원본 UX: 인증 완료 시 로그인으로
      toast.success('회원가입이 완료되었습니다! 로그인해주세요.')
      router.push({ name: 'Login' })
      return
    }

    const response = await authStore.signUp(registrationData)
    if (response.success) {
      if (response.requiresEmailVerification) {
        registrationCompleted.value = true
        toast.success('이메일 인증 토큰을 발송했습니다. 이메일을 확인하여 6자리 토큰을 입력해주세요.')
      } else {
        // 원본 흐름: 이메일 인증 없이 완료면 서버측 완료 처리
        const completeResponse = await AuthService.completeSignUp(registrationData as any)
        if (completeResponse.success) {
          toast.success('회원가입이 완료되었습니다!')
          router.push({ name: 'Login' })
        } else {
          toast.error(completeResponse.error || '회원가입 완료에 실패했습니다.')
        }
      }
    } else {
      toast.error(response.error || '회원가입 중 오류가 발생했습니다.')
    }
  } catch (error) {
    toast.error('회원가입 중 오류가 발생했습니다.')
    console.error('Registration error:', error)
  } finally {
    loading.value = false
  }
}

async function handleMounted() {
  await authStore.signOut()
  loadReferralInfo()
  const urlParams = new URLSearchParams(window.location.search)
  const emailVerified = urlParams.get('emailVerified')
  const message = urlParams.get('message')
  if (emailVerified === 'true') {
    registrationCompleted.value = true
    if (message) {
      toast.success(message)
    }
  }
}
function handleUnmounted() {
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
  }
}

onMounted(handleMounted)
onUnmounted(handleUnmounted)
</script>

<style scoped>
/* Custom styles */
</style>
