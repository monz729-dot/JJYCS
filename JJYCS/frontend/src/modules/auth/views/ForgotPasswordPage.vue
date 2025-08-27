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
        </div>

        <div class="card-content">
          <!-- Step 1: 입력 -->
          <div v-if="currentStep === 'input'" class="step-content">
            <div class="auth-method-info">
              <div class="auth-method-header">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                </svg>
                <span class="auth-method-title">이메일 인증</span>
              </div>
              <p class="auth-method-description">
                등록된 이메일 주소로 인증번호를 전송합니다.
              </p>
            </div>

            <form @submit.prevent="handleInputSubmit">
              <div class="form-group">
                <label for="userId" class="form-label">아이디</label>
                <input
                  id="userId"
                  v-model="formData.userId"
                  type="text"
                  class="form-input"
                  placeholder="아이디를 입력하세요"
                  required
                />
              </div>

              <div class="form-group">
                <label for="name" class="form-label">이름</label>
                <input
                  id="name"
                  v-model="formData.name"
                  type="text"
                  class="form-input"
                  placeholder="이름을 입력하세요"
                  required
                />
              </div>

              <div class="form-group">
                <label for="email" class="form-label">이메일 주소</label>
                <input
                  id="email"
                  v-model="formData.email"
                  type="email"
                  class="form-input"
                  placeholder="이메일을 입력해주세요"
                  required
                />
              </div>

              <div v-if="errorMessage" class="alert alert-error">
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

          <!-- Step 2: 인증 -->
          <div v-if="currentStep === 'verify'" class="step-content">
            <div class="verify-info">
              <div class="verify-icon">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                </svg>
              </div>
              <p class="verify-description">
                {{ formData.email }}로<br>
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
                    class="form-input"
                    placeholder="6자리 인증번호"
                    maxlength="6"
                    required
                  />
                  <button type="button" class="btn btn-outline btn-small" @click="resendCode" :disabled="isLoading">
                    재전송
                  </button>
                </div>
              </div>

              <div v-if="errorMessage" class="alert alert-error">
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
                    placeholder="새 비밀번호를 입력하세요 (영문+숫자, 8자 이상)"
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
                      <span>8자 이상</span>
                    </div>
                    <div class="validation-item" :class="passwordValidation.hasLetter ? 'validation-valid' : 'validation-invalid'">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path v-if="passwordValidation.hasLetter" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"/>
                      </svg>
                      <span>영문 포함</span>
                    </div>
                    <div class="validation-item" :class="passwordValidation.hasNumber ? 'validation-valid' : 'validation-invalid'">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path v-if="passwordValidation.hasNumber" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"/>
                      </svg>
                      <span>숫자 포함</span>
                    </div>
                  </div>
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
                    placeholder="비밀번호를 다시 입력하세요"
                    required
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
              </div>

              <div v-if="errorMessage" class="alert alert-error">
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const currentStep = ref<'input' | 'verify' | 'reset' | 'complete'>('input')
const isLoading = ref(false)
const errorMessage = ref('')

const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

const formData = reactive({
  userId: '',
  name: '',
  email: '',
  verificationCode: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordValidation = reactive({
  minLength: false,
  hasLetter: false,
  hasNumber: false
})

// Mock 사용자 데이터
const mockUsers: Record<string, { name: string; email: string }> = {
  'hong123': {
    name: '홍길동',
    email: 'hong@example.com'
  },
  'kim123': {
    name: '김철수',
    email: 'kim@example.com'
  },
  'lee_younghee': {
    name: '이영희',
    email: 'lee@example.com'
  }
}

const validatePassword = (password: string) => {
  passwordValidation.minLength = password.length >= 8
  passwordValidation.hasLetter = /[a-zA-Z]/.test(password)
  passwordValidation.hasNumber = /[0-9]/.test(password)
  
  return passwordValidation.minLength && passwordValidation.hasLetter && passwordValidation.hasNumber
}

const handlePasswordChange = () => {
  validatePassword(formData.newPassword)
}

const togglePassword = (type: 'new' | 'confirm') => {
  if (type === 'new') {
    showNewPassword.value = !showNewPassword.value
  } else {
    showConfirmPassword.value = !showConfirmPassword.value
  }
}

const handleInputSubmit = async () => {
  if (isLoading.value) return
  
  errorMessage.value = ''
  
  // 입력 검증
  if (!formData.userId.trim()) {
    errorMessage.value = '아이디를 입력해주세요.'
    return
  }
  
  if (!formData.name.trim()) {
    errorMessage.value = '이름을 입력해주세요.'
    return
  }

  if (!formData.email.trim()) {
    errorMessage.value = '이메일을 입력해주세요.'
    return
  }

  // 이메일 형식 검증
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(formData.email)) {
    errorMessage.value = '올바른 이메일 형식을 입력해주세요.'
    return
  }

  isLoading.value = true

  // Mock API 호출
  setTimeout(() => {
    const mockUser = mockUsers[formData.userId]
    if (!mockUser || mockUser.name !== formData.name || mockUser.email !== formData.email) {
      errorMessage.value = '입력하신 정보와 일치하는 계정을 찾을 수 없습니다.'
      isLoading.value = false
      return
    }

    currentStep.value = 'verify'
    isLoading.value = false
  }, 1500)
}

const handleVerifySubmit = async () => {
  if (isLoading.value) return
  
  errorMessage.value = ''
  
  if (!formData.verificationCode.trim()) {
    errorMessage.value = '인증번호를 입력해주세요.'
    return
  }

  isLoading.value = true

  setTimeout(() => {
    if (formData.verificationCode === '123456') {
      currentStep.value = 'reset'
    } else {
      errorMessage.value = '인증번호가 일치하지 않습니다.'
    }
    isLoading.value = false
  }, 1500)
}

const handleResetSubmit = async () => {
  if (isLoading.value) return
  
  errorMessage.value = ''
  
  if (!formData.newPassword) {
    errorMessage.value = '새 비밀번호를 입력해주세요.'
    return
  }

  if (!validatePassword(formData.newPassword)) {
    errorMessage.value = '비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다.'
    return
  }

  if (formData.newPassword !== formData.confirmPassword) {
    errorMessage.value = '비밀번호가 일치하지 않습니다.'
    return
  }

  isLoading.value = true

  setTimeout(() => {
    currentStep.value = 'complete'
    isLoading.value = false
  }, 1500)
}

const resendCode = () => {
  if (isLoading.value) return
  
  isLoading.value = true
  setTimeout(() => {
    alert('인증번호가 이메일로 전송되었습니다. (데모: 123456)')
    isLoading.value = false
  }, 1000)
}

const goToPrevious = () => {
  errorMessage.value = ''
  formData.verificationCode = ''
  currentStep.value = 'input'
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
    userId: '',
    name: '',
    email: '',
    verificationCode: '',
    newPassword: '',
    confirmPassword: ''
  })
  
  Object.assign(passwordValidation, {
    minLength: false,
    hasLetter: false,
    hasNumber: false
  })
  
  errorMessage.value = ''
  currentStep.value = 'input'
}

const goToLogin = () => {
  router.push('/login')
}
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
  padding: 2rem 2rem 0;
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
  margin-bottom: 0.75rem;
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
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: rgb(30 64 175);
}

.form-input {
  height: 3.5rem;
  padding: 0 1rem;
  border: 1px solid rgb(191 219 254);
  border-radius: 0.375rem;
  font-size: 0.875rem;
  background: rgba(239, 246, 255, 0.3);
  transition: all 0.3s;
}

.form-input:hover {
  background: rgba(239, 246, 255, 0.5);
}

.form-input:focus {
  outline: none;
  border-color: rgb(37 99 235);
  background: white;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.form-input::placeholder {
  color: rgb(156 163 175);
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
  transition: all 0.5s;
  border: none;
  text-decoration: none;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  width: 100%;
  background: rgba(37, 99, 235, 0.2);
  border: 1px solid rgba(37, 99, 235, 0.3);
  color: rgb(29 78 216);
  opacity: 0.7;
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, rgb(37 99 235) 0%, rgb(59 130 246) 100%);
  border-color: rgb(37 99 235);
  color: white;
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  opacity: 1;
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
  padding: 0 1.5rem;
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
  padding: 0.75rem 1rem;
  border-radius: 0.375rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border: 1px solid;
  animation: scaleIn 0.3s ease-out;
}

.alert-error {
  background: rgba(239, 68, 68, 0.05);
  border-color: #fecaca;
  color: #991b1b;
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