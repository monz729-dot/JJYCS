<template>
  <div class="find-password-container">
    <div class="find-password-card">
      <div class="card">
        <div class="card-header">
          <button class="back-button" @click="handleBackButton">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
            </svg>
          </button>
          <div class="header-icon">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
            </svg>
          </div>
          <h1 class="card-title">비밀번호 찾기</h1>
          <p class="card-subtitle">본인 확인 후 새 비밀번호로 변경하실 수 있습니다</p>
        </div>

        <div class="card-content">
          <!-- Step 1: 회원 정보 입력 -->
          <div v-if="currentStep === 'input'" class="step-content">
            <div class="auth-method-info">
              <div class="auth-method-header">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                </svg>
                <span class="auth-method-title">본인 확인</span>
              </div>
              <p class="auth-method-description">
                회원가입 시 입력하신 이름, 연락처, 이메일을 정확히 입력해주세요.
              </p>
            </div>

            <form @submit.prevent="handleInputSubmit">
              <div class="form-group">
                <label for="name" class="form-label">이름</label>
                <input
                  id="name"
                  v-model="formData.name"
                  type="text"
                  class="form-input"
                  :class="{ 'error-input': fieldErrors.name }"
                  placeholder="이름을 입력하세요"
                  required
                  @input="clearFieldError('name')"
                />
                <div v-if="fieldErrors.name" class="field-error">
                  {{ fieldErrors.name }}
                </div>
              </div>

              <div class="form-group">
                <label for="phone" class="form-label">연락처</label>
                <input
                  id="phone"
                  v-model="formData.phone"
                  type="tel"
                  class="form-input"
                  :class="{ 'error-input': fieldErrors.phone }"
                  placeholder="연락처를 입력하세요 (예: 010-1234-5678)"
                  maxlength="13"
                  required
                  @input="formatPhoneNumber"
                />
                <div v-if="fieldErrors.phone" class="field-error">
                  {{ fieldErrors.phone }}
                </div>
              </div>

              <div class="form-group">
                <label for="email" class="form-label">이메일 주소</label>
                <input
                  id="email"
                  v-model="formData.email"
                  type="email"
                  class="form-input"
                  :class="{ 'error-input': fieldErrors.email }"
                  placeholder="이메일을 입력해주세요"
                  required
                  @input="clearFieldError('email')"
                />
                <div v-if="fieldErrors.email" class="field-error">
                  {{ fieldErrors.email }}
                </div>
              </div>

              <div v-if="errorMessage" class="alert alert-error">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 15.5c-.77.833.192 2.5 1.732 2.5z"/>
                </svg>
                <span>{{ errorMessage }}</span>
              </div>

              <button type="submit" class="btn btn-primary" :disabled="isLoading">
                <span v-if="isLoading">
                  <div class="spinner"></div> 인증번호 전송 중...
                </span>
                <span v-else>인증번호 받기</span>
              </button>
            </form>
          </div>

          <!-- Step 2: 인증번호 입력 -->
          <div v-if="currentStep === 'verify'" class="step-content">
            <div class="verify-info">
              <div class="verify-icon">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                </svg>
              </div>
              <p class="verify-description">
                <strong>{{ formData.email }}</strong>로<br>
                인증번호를 전송했습니다.
              </p>
            </div>

            <form @submit.prevent="handleVerifySubmit">
              <div class="form-group">
                <label for="verificationCode" class="form-label">인증번호</label>
                <div class="input-group">
                  <input
                    id="verificationCode"
                    v-model="formData.verificationCode"
                    type="text"
                    class="form-input verification-input"
                    :class="{ 'error-input': fieldErrors.verificationCode }"
                    placeholder="6자리 인증번호"
                    maxlength="6"
                    required
                    @input="formatVerificationCode"
                  />
                  <button type="button" class="btn btn-outline btn-small" @click="resendCode" :disabled="isLoading || !canResend">
                    <span v-if="canResend">재전송</span>
                    <span v-else>{{ resendCountdown }}초 후 재전송</span>
                  </button>
                </div>
                <div v-if="fieldErrors.verificationCode" class="field-error">
                  {{ fieldErrors.verificationCode }}
                </div>
                <div class="verification-timer" v-if="verificationTimer > 0">
                  ⏰ 남은 시간: {{ Math.floor(verificationTimer / 60) }}분 {{ verificationTimer % 60 }}초
                </div>
              </div>

              <div v-if="errorMessage" class="alert alert-error">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 15.5c-.77.833.192 2.5 1.732 2.5z"/>
                </svg>
                <span>{{ errorMessage }}</span>
              </div>

              <div class="button-group">
                <button type="button" class="btn btn-outline" @click="goToPrevious">
                  이전
                </button>
                <button type="submit" class="btn btn-primary" :disabled="isLoading">
                  <span v-if="isLoading">
                    <div class="spinner"></div> 확인 중...
                  </span>
                  <span v-else>확인</span>
                </button>
              </div>
            </form>
          </div>

          <!-- Step 3: 비밀번호 재설정 -->
          <div v-if="currentStep === 'reset'" class="step-content">
            <div class="verify-info">
              <div class="verify-icon">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
                </svg>
              </div>
              <p class="verify-description">
                새로운 비밀번호를 설정해주세요.
              </p>
            </div>

            <form @submit.prevent="handleResetSubmit">
              <div class="form-group">
                <label for="newPassword" class="form-label">새 비밀번호</label>
                <div class="password-input-group">
                  <input
                    id="newPassword"
                    v-model="formData.newPassword"
                    :type="showNewPassword ? 'text' : 'password'"
                    class="form-input password-input"
                    :class="{ 'error-input': fieldErrors.newPassword }"
                    placeholder="새 비밀번호를 입력하세요 (6자 이상)"
                    @input="handlePasswordChange"
                    required
                  />
                  <button type="button" class="password-toggle" @click="togglePassword('new')">
                    <svg v-if="showNewPassword" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L12 12m-3.122-3.122L9.879 9.879m0 0L12 12m3.121-3.121L12 12"/>
                    </svg>
                    <svg v-else fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                    </svg>
                  </button>
                </div>
                
                <div v-if="formData.newPassword" class="password-validation">
                  <p class="validation-title">비밀번호 조건</p>
                  <div class="validation-list">
                    <div class="validation-item" :class="passwordValidation.minLength ? 'validation-valid' : 'validation-invalid'">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path v-if="passwordValidation.minLength" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"/>
                      </svg>
                      <span>6자 이상</span>
                    </div>
                  </div>
                </div>
                <div v-if="fieldErrors.newPassword" class="field-error">
                  {{ fieldErrors.newPassword }}
                </div>
              </div>

              <div class="form-group">
                <label for="confirmPassword" class="form-label">비밀번호 확인</label>
                <div class="password-input-group">
                  <input
                    id="confirmPassword"
                    v-model="formData.confirmPassword"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    class="form-input password-input"
                    :class="{ 'error-input': fieldErrors.confirmPassword }"
                    placeholder="비밀번호를 다시 입력하세요"
                    required
                    @input="clearFieldError('confirmPassword')"
                  />
                  <button type="button" class="password-toggle" @click="togglePassword('confirm')">
                    <svg v-if="showConfirmPassword" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L12 12m-3.122-3.122L9.879 9.879m0 0L12 12m3.121-3.121L12 12"/>
                    </svg>
                    <svg v-else fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                    </svg>
                  </button>
                </div>
                <div v-if="fieldErrors.confirmPassword" class="field-error">
                  {{ fieldErrors.confirmPassword }}
                </div>
              </div>

              <div v-if="errorMessage" class="alert alert-error">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 15.5c-.77.833.192 2.5 1.732 2.5z"/>
                </svg>
                <span>{{ errorMessage }}</span>
              </div>

              <button type="submit" class="btn btn-primary" :disabled="isLoading">
                <span v-if="isLoading">
                  <div class="spinner"></div> 비밀번호 변경 중...
                </span>
                <span v-else>비밀번호 변경</span>
              </button>
            </form>
          </div>

          <!-- Step 4: 완료 -->
          <div v-if="currentStep === 'complete'" class="step-content">
            <div class="verify-info">
              <div class="complete-icon">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
              </div>
              <h3 class="complete-title">비밀번호 변경 완료</h3>
              <p class="complete-description">
                새로운 비밀번호로 로그인해주세요.
              </p>
            </div>

            <button type="button" class="btn btn-primary" @click="goToLogin">
              로그인하기
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../../../utils/api'
import type { ApiResponse } from '../../../types'

const router = useRouter()

// 상태 관리
const currentStep = ref<'input' | 'verify' | 'reset' | 'complete'>('input')
const isLoading = ref(false)
const errorMessage = ref('')

const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

const verificationTimer = ref(0)
const resendCountdown = ref(0)
const canResend = ref(true)

// 타이머 참조
let verificationInterval: NodeJS.Timeout | null = null
let resendInterval: NodeJS.Timeout | null = null

// 폼 데이터
const formData = reactive({
  name: '',
  phone: '',
  email: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

// 필드별 에러 메시지
const fieldErrors = reactive({
  name: '',
  phone: '',
  email: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

// 비밀번호 검증
const passwordValidation = reactive({
  minLength: false
})

// 유틸리티 함수들
const clearFieldError = (field: keyof typeof fieldErrors) => {
  fieldErrors[field] = ''
  if (errorMessage.value) {
    errorMessage.value = ''
  }
}

const clearAllErrors = () => {
  Object.keys(fieldErrors).forEach(key => {
    fieldErrors[key as keyof typeof fieldErrors] = ''
  })
  errorMessage.value = ''
}

const validatePassword = (password: string) => {
  passwordValidation.minLength = password.length >= 6
  return passwordValidation.minLength
}

const handlePasswordChange = () => {
  validatePassword(formData.newPassword)
  clearFieldError('newPassword')
}

const formatPhoneNumber = () => {
  // 숫자만 입력되도록 필터링
  let phone = formData.phone.replace(/\D/g, '')
  
  // 11자리 초과 시 자르기
  if (phone.length > 11) {
    phone = phone.substring(0, 11)
  }
  
  // 010-1234-5678 형식으로 포맷팅
  if (phone.length >= 3) {
    if (phone.length <= 7) {
      phone = phone.replace(/(\d{3})(\d+)/, '$1-$2')
    } else {
      phone = phone.replace(/(\d{3})(\d{4})(\d+)/, '$1-$2-$3')
    }
  }
  
  formData.phone = phone
  clearFieldError('phone')
}

const formatVerificationCode = () => {
  // 숫자만 입력되도록 필터링
  formData.verificationCode = formData.verificationCode.replace(/\D/g, '')
  clearFieldError('verificationCode')
}

const togglePassword = (type: 'new' | 'confirm') => {
  if (type === 'new') {
    showNewPassword.value = !showNewPassword.value
  } else {
    showConfirmPassword.value = !showConfirmPassword.value
  }
}

// 타이머 관리
const startVerificationTimer = () => {
  verificationTimer.value = 600 // 10분
  verificationInterval = setInterval(() => {
    verificationTimer.value--
    if (verificationTimer.value <= 0) {
      clearInterval(verificationInterval!)
      verificationInterval = null
    }
  }, 1000)
}

const startResendTimer = () => {
  canResend.value = false
  resendCountdown.value = 60 // 60초
  resendInterval = setInterval(() => {
    resendCountdown.value--
    if (resendCountdown.value <= 0) {
      canResend.value = true
      clearInterval(resendInterval!)
      resendInterval = null
    }
  }, 1000)
}

// API 호출 함수들
const handleInputSubmit = async () => {
  if (isLoading.value) return
  
  clearAllErrors()
  
  // 입력 검증
  let hasError = false
  
  if (!formData.name.trim()) {
    fieldErrors.name = '이름을 입력해주세요.'
    hasError = true
  }
  
  if (!formData.phone.trim()) {
    fieldErrors.phone = '연락처를 입력해주세요.'
    hasError = true
  } else if (!/^01[0-9]{8,9}$/.test(formData.phone.replace(/-/g, ''))) {
    fieldErrors.phone = '올바른 연락처 형식을 입력해주세요. (예: 01012345678)'
    hasError = true
  }

  if (!formData.email.trim()) {
    fieldErrors.email = '이메일을 입력해주세요.'
    hasError = true
  } else {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(formData.email)) {
      fieldErrors.email = '올바른 이메일 형식을 입력해주세요.'
      hasError = true
    }
  }

  if (hasError) return

  isLoading.value = true

  try {
    const response = await authApi.findPassword({
      name: formData.name,
      phone: formData.phone,
      email: formData.email
    })

    if (response.success) {
      console.log('✅ 비밀번호 찾기 성공:', response.data)
      currentStep.value = 'verify'
      startVerificationTimer()
      startResendTimer()
    } else {
      const field = response.data?.field || 'general'
      if (field === 'general') {
        errorMessage.value = response.error || '계정 정보 확인 중 오류가 발생했습니다.'
      } else {
        fieldErrors[field as keyof typeof fieldErrors] = response.error || '입력 정보를 확인해주세요.'
      }
    }
  } catch (error: any) {
    console.error('❌ 비밀번호 찾기 오류:', error)
    errorMessage.value = error.response?.data?.error || '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
  } finally {
    isLoading.value = false
  }
}

const handleVerifySubmit = async () => {
  if (isLoading.value) return
  
  clearAllErrors()
  
  if (!formData.verificationCode.trim()) {
    fieldErrors.verificationCode = '인증번호를 입력해주세요.'
    return
  }

  if (formData.verificationCode.length !== 6) {
    fieldErrors.verificationCode = '6자리 인증번호를 입력해주세요.'
    return
  }

  isLoading.value = true

  try {
    const response = await authApi.verifyPasswordReset({
      email: formData.email,
      verificationCode: formData.verificationCode
    })

    if (response.success) {
      console.log('✅ 인증번호 확인 성공:', response.data)
      currentStep.value = 'reset'
    } else {
      const field = response.data?.field || 'verificationCode'
      if (field === 'verificationCode') {
        fieldErrors.verificationCode = response.error || '올바르지 않은 인증번호입니다.'
      } else {
        errorMessage.value = response.error || '인증번호 확인 중 오류가 발생했습니다.'
      }
    }
  } catch (error: any) {
    console.error('❌ 인증번호 확인 오류:', error)
    fieldErrors.verificationCode = error.response?.data?.error || '인증번호 확인 중 오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}

const handleResetSubmit = async () => {
  if (isLoading.value) return
  
  clearAllErrors()
  
  let hasError = false
  
  if (!formData.newPassword) {
    fieldErrors.newPassword = '새 비밀번호를 입력해주세요.'
    hasError = true
  } else if (!validatePassword(formData.newPassword)) {
    fieldErrors.newPassword = '비밀번호는 6자 이상이어야 합니다.'
    hasError = true
  }

  if (!formData.confirmPassword) {
    fieldErrors.confirmPassword = '비밀번호 확인을 입력해주세요.'
    hasError = true
  } else if (formData.newPassword !== formData.confirmPassword) {
    fieldErrors.confirmPassword = '비밀번호가 일치하지 않습니다.'
    hasError = true
  }

  if (hasError) return

  isLoading.value = true

  try {
    const response = await authApi.resetPasswordDirect({
      email: formData.email,
      verificationCode: formData.verificationCode,
      newPassword: formData.newPassword,
      confirmPassword: formData.confirmPassword
    })

    if (response.success) {
      console.log('✅ 비밀번호 재설정 성공:', response.data)
      currentStep.value = 'complete'
    } else {
      const field = response.data?.field || 'general'
      if (field === 'general') {
        errorMessage.value = response.error || '비밀번호 변경 중 오류가 발생했습니다.'
      } else {
        fieldErrors[field as keyof typeof fieldErrors] = response.error || '입력 정보를 확인해주세요.'
      }
    }
  } catch (error: any) {
    console.error('❌ 비밀번호 재설정 오류:', error)
    errorMessage.value = error.response?.data?.error || '비밀번호 변경 중 오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}

const resendCode = async () => {
  if (isLoading.value || !canResend.value) return
  
  isLoading.value = true
  
  try {
    const response = await authApi.findPassword({
      name: formData.name,
      phone: formData.phone,
      email: formData.email
    })

    if (response.success) {
      console.log('✅ 인증번호 재전송 성공')
      startResendTimer()
      // 인증번호 초기화
      formData.verificationCode = ''
      clearFieldError('verificationCode')
    } else {
      errorMessage.value = response.error || '인증번호 재전송에 실패했습니다.'
    }
  } catch (error: any) {
    console.error('❌ 인증번호 재전송 오류:', error)
    errorMessage.value = '인증번호 재전송 중 오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 네비게이션
const goToPrevious = () => {
  clearAllErrors()
  formData.verificationCode = ''
  currentStep.value = 'input'
  
  // 타이머 정리
  if (verificationInterval) {
    clearInterval(verificationInterval)
    verificationInterval = null
    verificationTimer.value = 0
  }
  if (resendInterval) {
    clearInterval(resendInterval)
    resendInterval = null
    resendCountdown.value = 0
    canResend.value = true
  }
}

const handleBackButton = () => {
  if (currentStep.value === 'input') {
    goToLogin()
  } else {
    resetForm()
  }
}

const resetForm = () => {
  Object.assign(formData, {
    name: '',
    phone: '',
    email: '',
    verificationCode: '',
    newPassword: '',
    confirmPassword: ''
  })
  
  Object.assign(passwordValidation, {
    minLength: false
  })
  
  clearAllErrors()
  currentStep.value = 'input'
  
  // 타이머 정리
  if (verificationInterval) {
    clearInterval(verificationInterval)
    verificationInterval = null
    verificationTimer.value = 0
  }
  if (resendInterval) {
    clearInterval(resendInterval)
    resendInterval = null
    resendCountdown.value = 0
    canResend.value = true
  }
}

const goToLogin = () => {
  router.push('/login')
}

// 컴포넌트 언마운트 시 타이머 정리
onUnmounted(() => {
  if (verificationInterval) {
    clearInterval(verificationInterval)
  }
  if (resendInterval) {
    clearInterval(resendInterval)
  }
})
</script>

<style scoped>
.find-password-container {
  min-height: 100vh;
  background: linear-gradient(135deg, rgb(239 246 255) 0%, rgb(255 255 255) 50%, rgb(239 246 255) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.find-password-card {
  width: 100%;
  max-width: 28rem;
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.card {
  background: white;
  border-radius: 0.5rem;
  box-shadow: 0 10px 40px rgba(37, 99, 235, 0.15);
  border: 1px solid rgb(219 234 254);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.card-header {
  text-align: center;
  padding: 2rem 2rem 1rem;
  position: relative;
}

.back-button {
  position: absolute;
  left: 1.5rem;
  top: 1.5rem;
  padding: 0.5rem;
  color: rgba(37, 99, 235, 0.6);
  background: transparent;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
  opacity: 0.6;
}

.back-button:hover {
  color: rgb(29 78 216);
  background: rgb(239 246 255);
  opacity: 1;
}

.back-button svg {
  width: 1.25rem;
  height: 1.25rem;
}

.header-icon {
  width: 4rem;
  height: 4rem;
  background: linear-gradient(135deg, rgb(37 99 235) 0%, rgb(59 130 246) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
}

.header-icon svg {
  width: 2rem;
  height: 2rem;
  color: white;
}

.card-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: rgb(30 64 175);
  margin-bottom: 0.5rem;
}

.card-subtitle {
  font-size: 0.875rem;
  color: rgb(107 114 128);
  margin-bottom: 0;
}

.card-content {
  padding: 0 2rem 2rem;
}

.step-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.auth-method-info {
  padding: 1rem;
  background: rgba(239, 246, 255, 0.5);
  border: 1px solid rgba(191, 219, 254, 0.5);
  border-radius: 0.5rem;
}

.auth-method-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.auth-method-header svg {
  width: 1.25rem;
  height: 1.25rem;
  color: rgb(37 99 235);
}

.auth-method-title {
  font-weight: 500;
  color: rgb(30 64 175);
}

.auth-method-description {
  font-size: 0.875rem;
  color: rgb(37 99 235);
  line-height: 1.5;
}

.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.form-input {
  width: 100%;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  font-size: 1rem;
  transition: all 0.2s ease;
  background: white;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px #dbeafe;
}

.form-input::placeholder {
  color: #9ca3af;
}

.error-input {
  border-color: #dc2626 !important;
  background-color: #fef2f2 !important;
}

.error-input:focus {
  border-color: #dc2626 !important;
  box-shadow: 0 0 0 3px #fee2e2 !important;
}

.field-error {
  font-size: 0.75rem;
  color: #dc2626;
  margin-top: 0.25rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.password-input-group {
  position: relative;
}

.password-input {
  padding-right: 3rem;
}

.password-toggle {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  color: rgba(37, 99, 235, 0.6);
  cursor: pointer;
  transition: all 0.3s;
  opacity: 0;
}

.password-input-group:hover .password-toggle,
.password-toggle:focus {
  opacity: 1;
}

.password-toggle:hover {
  color: rgb(29 78 216);
}

.password-toggle svg {
  width: 1.25rem;
  height: 1.25rem;
}

.input-group {
  display: flex;
  gap: 0.75rem;
}

.input-group .form-input {
  flex: 1;
}

.verification-input {
  font-size: 1.125rem;
  font-weight: 500;
  letter-spacing: 0.25rem;
  text-align: center;
  font-family: 'Courier New', monospace;
}

.verification-timer {
  font-size: 0.75rem;
  color: rgb(239 68 68);
  text-align: center;
  margin-top: 0.5rem;
  font-weight: 500;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0 1.5rem;
  height: 3.5rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  text-decoration: none;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  width: 100%;
  background: #3b82f6;
  color: white;
  border: none;
  padding: 1rem;
  border-radius: 0.75rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 1rem;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-1px);
}

.btn-primary:active {
  transform: translateY(0);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-outline {
  background: transparent;
  border: 1px solid rgba(191, 219, 254, 0.5);
  color: rgba(37, 99, 235, 0.7);
  opacity: 0.7;
}

.btn-outline:hover:not(:disabled) {
  background: rgb(239 246 255);
  color: rgb(29 78 216);
  border-color: rgba(37, 99, 235, 0.3);
  opacity: 1;
}

.btn-small {
  height: 3.5rem;
  padding: 0 1rem;
  font-size: 0.75rem;
  white-space: nowrap;
}

.button-group {
  display: flex;
  gap: 1rem;
}

.button-group .btn {
  flex: 1;
}

.spinner {
  width: 1rem;
  height: 1rem;
  border: 2px solid currentColor;
  border-top: 2px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.alert {
  padding: 1rem;
  border-radius: 0.375rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  border: 1px solid;
  animation: scaleIn 0.3s ease-out;
}

.alert svg {
  width: 1.25rem;
  height: 1.25rem;
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.alert-error {
  background: rgba(254, 226, 226, 0.8);
  border-color: rgb(252 165 165);
  color: rgb(153 27 27);
}

.verify-info {
  text-align: center;
  margin-bottom: 1.5rem;
}

.verify-icon {
  width: 4rem;
  height: 4rem;
  background: rgb(219 234 254);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
}

.verify-icon svg {
  width: 2rem;
  height: 2rem;
  color: rgb(37 99 235);
}

.verify-description {
  font-size: 0.875rem;
  color: rgb(37 99 235);
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.complete-icon {
  width: 4rem;
  height: 4rem;
  background: rgb(220 252 231);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  animation: bounceIn 0.6s ease-out;
}

.complete-icon svg {
  width: 2rem;
  height: 2rem;
  color: rgb(22 163 74);
}

.complete-title {
  font-size: 1.125rem;
  font-weight: 500;
  color: rgb(30 64 175);
  margin-bottom: 0.75rem;
}

.complete-description {
  color: rgb(37 99 235);
  margin-bottom: 2rem;
}

.password-validation {
  padding: 0.75rem;
  background: rgba(249, 250, 251, 0.5);
  border-radius: 0.5rem;
  border: 1px solid rgba(229, 231, 235, 0.5);
  margin-top: 0.5rem;
}

.validation-title {
  font-size: 0.75rem;
  color: rgb(107 114 128);
  margin-bottom: 0.5rem;
}

.validation-list {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.validation-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
}

.validation-item svg {
  width: 0.75rem;
  height: 0.75rem;
}

.validation-valid {
  color: rgb(22 163 74);
}

.validation-invalid {
  color: rgb(156 163 175);
}
</style>