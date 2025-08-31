<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">YSC</div>
        <h1 class="auth-title">로그인</h1>
        <p class="auth-subtitle">YSC 물류 시스템에 오신 것을 환영합니다</p>
      </div>
      
      <form class="auth-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <div class="input-wrapper">
            <input 
              id="email"
              v-model="form.email" 
              type="email" 
              class="form-input" 
              :class="{ 'error-input': errors.email }"
              placeholder="이메일 주소" 
              required
              @input="clearError('email')"
              @blur="checkEmailExists(form.email)"
            />
            <svg v-if="isCheckingEmail" class="loading-icon" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none" stroke-dasharray="32" stroke-dashoffset="32">
                <animate attributeName="stroke-dashoffset" dur="2s" values="32;0;32" repeatCount="indefinite"/>
              </circle>
            </svg>
            <svg v-else-if="errors.email" class="error-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <transition name="fade">
            <div v-if="errors.email" class="error-message">
              <svg class="warning-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              {{ errors.email }}
            </div>
          </transition>
        </div>
        
        <div class="form-group">
          <div class="input-wrapper">
            <input 
              id="password"
              v-model="form.password" 
              type="password" 
              class="form-input"
              :class="{ 'error-input': errors.password }"
              placeholder="비밀번호" 
              required
              @input="clearError('password')"
              @blur="() => { if(form.password && form.password.length < 6) errors.password = '비밀번호는 6자 이상이어야 합니다.' }"
            />
            <svg v-if="errors.password" class="error-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <transition name="fade">
            <div v-if="errors.password" class="error-message">
              <svg class="warning-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              {{ errors.password }}
            </div>
          </transition>
        </div>
        
        <div class="form-checkbox">
          <input 
            id="remember"
            v-model="form.rememberMe" 
            type="checkbox" 
            class="checkbox"
          />
          <label class="checkbox-label" for="remember">로그인 상태 유지</label>
        </div>
        
        <button 
          type="submit" 
          class="login-btn"
          :disabled="authStore.loading"
        >
          {{ authStore.loading ? '로그인 중...' : '로그인' }}
        </button>
      </form>
      
      <div class="auth-links">
        <router-link to="/register" class="auth-link">회원가입</router-link>
        <span style="color: #d1d5db;">|</span>
        <router-link to="/forgot-password" class="auth-link">비밀번호 찾기</router-link>
      </div>
      
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { USER_TYPE } from '@/types'
import type { UserType } from '@/types'
import { authApi } from '@/utils/api'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  email: '',
  password: '',
  rememberMe: false
})

const errors = reactive({
  email: '',
  password: ''
})

const isCheckingEmail = ref(false)

const clearError = (field: 'email' | 'password') => {
  errors[field] = ''
}

const validateEmail = (email: string) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

const checkEmailExists = async (email: string) => {
  console.log('🔍 checkEmailExists called with:', email)
  
  if (!email || !validateEmail(email)) {
    console.log('❌ Email validation failed or empty:', email)
    return
  }
  
  console.log('✅ Email validation passed, checking existence...')
  isCheckingEmail.value = true
  clearError('email')
  
  try {
    const response = await authApi.checkEmail(email)
    console.log('📡 API Response:', response)
    
    if (response.success && response.data) {
      const data = response.data
      console.log('📊 Response data:', data)
      
      if (!data.exists) {
        console.log('❌ Email does not exist, setting error message')
        errors.email = '등록되지 않은 이메일입니다. 회원가입을 먼저 진행해주세요.'
      } else {
        console.log('✅ Email exists, checking status...')
        // 계정 상태 확인
        if (data.status === 'PENDING') {
          errors.email = '승인 대기 중인 계정입니다. 승인 후 로그인이 가능합니다.'
        } else if (data.status === 'REJECTED') {
          errors.email = '거부된 계정입니다. 고객센터에 문의해주세요.'
        } else if (data.status === 'SUSPENDED') {
          errors.email = '정지된 계정입니다. 고객센터에 문의해주세요.'
        } else if (!data.emailVerified) {
          errors.email = '이메일 인증이 필요합니다. 이메일을 확인해주세요.'
        } else if (data.status !== 'ACTIVE') {
          errors.email = '비활성화된 계정입니다. 관리자에게 문의해주세요.'
        }
        // 계정이 정상이면 에러 메시지 없음
      }
    } else {
      console.log('❌ API response not successful or no data:', response)
    }
  } catch (error: any) {
    console.error('🚨 이메일 확인 중 오류:', error)
    // 네트워크 오류 등은 조용히 무시 (로그인 시 다시 확인됨)
  } finally {
    isCheckingEmail.value = false
    console.log('🏁 checkEmailExists completed, errors.email:', errors.email)
  }
}

const validateForm = () => {
  let isValid = true
  
  if (!form.email.trim()) {
    errors.email = '이메일을 입력해주세요.'
    isValid = false
  } else if (!validateEmail(form.email)) {
    errors.email = '올바른 이메일 형식을 입력해주세요.'
    isValid = false
  } else {
    errors.email = ''
  }
  
  if (!form.password.trim()) {
    errors.password = '비밀번호를 입력해주세요.'
    isValid = false
  } else if (form.password.length < 6) {
    errors.password = '비밀번호는 6자 이상이어야 합니다.'
    isValid = false
  } else {
    errors.password = ''
  }
  
  return isValid
}


const handleLogin = async () => {
  if (!validateForm()) {
    return
  }

  authStore.clearError()

  try {
    const result = await authStore.login({
      email: form.email,
      password: form.password,
      rememberMe: form.rememberMe
    })

    if (result.success) {
      if (form.rememberMe) {
        localStorage.setItem('remembered_email', form.email)
      } else {
        localStorage.removeItem('remembered_email')
      }

      const redirectPath = router.currentRoute.value.query.redirect as string
      if (redirectPath) {
        router.push(redirectPath)
      } else {
        switch(authStore.user?.userType) {
          case USER_TYPE.ADMIN:
            router.push('/admin')
            break
          case USER_TYPE.PARTNER:
            router.push('/partner')
            break
          case USER_TYPE.WAREHOUSE:
            router.push('/warehouse')
            break
          case USER_TYPE.CORPORATE:
            router.push('/dashboard')
            break
          default:
            router.push('/dashboard')
        }
      }
    } else {
      // 백엔드에서 제공하는 field 정보를 활용한 에러 처리
      const errorMessage = result.error?.toLowerCase() || ''
      const errorField = result.field || 'general'
      
      // field 정보가 있는 경우 해당 필드에 에러 표시
      if (errorField === 'email' || errorMessage.includes('user not found')) {
        errors.email = result.error || '등록되지 않은 이메일입니다.'
        errors.password = ''
      } else if (errorField === 'password' || errorMessage.includes('invalid password')) {
        errors.password = result.error || '비밀번호가 일치하지 않습니다.'
        errors.email = ''
      } else {
        // 상세한 에러 메시지 분류
        if (errorMessage.includes('user not found')) {
          errors.email = '등록되지 않은 이메일입니다. 회원가입을 먼저 진행해주세요.'
        } else if (errorMessage.includes('invalid password')) {
          errors.password = '비밀번호가 일치하지 않습니다. 다시 확인해주세요.'
        } else if (errorMessage.includes('pending')) {
          errors.email = '승인 대기 중인 계정입니다. 승인 후 로그인이 가능합니다.'
        } else if (errorMessage.includes('rejected')) {
          errors.email = '거부된 계정입니다. 고객센터에 문의해주세요.'
        } else if (errorMessage.includes('suspended')) {
          errors.email = '정지된 계정입니다. 고객센터에 문의해주세요.'
        } else if (errorMessage.includes('email not verified')) {
          errors.email = '이메일 인증이 필요합니다. 이메일을 확인해주세요.'
        } else if (errorMessage.includes('inactive')) {
          errors.email = '비활성화된 계정입니다. 관리자에게 문의해주세요.'
        } else {
          // 기타 에러는 일반적인 메시지로 표시
          errors.password = result.error || '로그인에 실패했습니다. 다시 시도해주세요.'
        }
      }
    }
  } catch (error: any) {
    console.error('로그인 오류:', error)
    
    // 네트워크 오류 처리
    if (!navigator.onLine) {
      errors.password = '네트워크 연결이 끊어졌습니다. 인터넷 연결을 확인해주세요.'
    } else if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      errors.password = '서버 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.'
    } else if (error.response?.status === 500) {
      errors.password = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
    } else if (error.response?.status === 429) {
      errors.password = '너무 많은 시도가 있었습니다. 잠시 후 다시 시도해주세요.'
    } else {
      errors.password = '로그인 중 오류가 발생했습니다. 다시 시도해주세요.'
    }
  }
}

onMounted(() => {
  authStore.clearError()
  
  const rememberedEmail = localStorage.getItem('remembered_email')
  if (rememberedEmail) {
    form.email = rememberedEmail
    form.rememberMe = true
  }
  
  if (authStore.user) {
    switch(authStore.user.userType) {
      case USER_TYPE.ADMIN:
        router.push('/admin')
        break
      case USER_TYPE.PARTNER:
        router.push('/partner')
        break
      case USER_TYPE.WAREHOUSE:
        router.push('/warehouse')
        break
      default:
        router.push('/dashboard')
    }
  }
})
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.auth-card {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  border: 1px solid #dbeafe;
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
}

.auth-logo {
  width: 80px;
  height: 80px;
  background: #3b82f6;
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem auto;
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
}

.auth-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #1e3a8a;
  margin-bottom: 0.5rem;
}

.auth-subtitle {
  color: #6b7280;
  font-size: 0.875rem;
}

.auth-form {
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
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

.input-wrapper {
  position: relative;
}

.form-input.error-input {
  border-color: #dc2626;
  background-color: #fef2f2;
}

.form-input.error-input:focus {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px #fee2e2;
}

.error-icon {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  width: 1.25rem;
  height: 1.25rem;
  color: #dc2626;
  pointer-events: none;
}

.loading-icon {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  width: 1.25rem;
  height: 1.25rem;
  color: #3b82f6;
  pointer-events: none;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #dc2626;
  font-size: 0.875rem;
  margin-top: 0.5rem;
  font-weight: 500;
  padding: 0.5rem;
  background-color: #fef2f2;
  border-radius: 0.375rem;
  border: 1px solid #fecaca;
}

.warning-icon {
  width: 1rem;
  height: 1rem;
  flex-shrink: 0;
}

/* 애니메이션 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.form-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 1rem 0;
}

.checkbox {
  width: 1rem;
  height: 1rem;
  accent-color: #3b82f6;
}

.checkbox-label {
  font-size: 0.875rem;
  color: #374151;
}

.login-btn {
  width: 100%;
  background: #3b82f6;
  color: white;
  padding: 1rem;
  border-radius: 0.75rem;
  border: none;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  margin: 1rem 0;
}

.login-btn:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-1px);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-links {
  text-align: center;
  margin-bottom: 1.5rem;
}

.auth-link {
  color: #2563eb;
  text-decoration: none;
  font-size: 0.875rem;
  margin: 0 0.5rem;
}

.auth-link:hover {
  text-decoration: underline;
}


@media (max-width: 768px) {
  .auth-card {
    padding: 1.5rem;
    margin: 0.5rem;
    border-radius: 0.75rem;
  }
  
  .auth-logo {
    width: 60px;
    height: 60px;
    font-size: 1.25rem;
  }
  
  .auth-title {
    font-size: 1.25rem;
  }
}
</style>