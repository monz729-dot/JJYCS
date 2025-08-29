<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">YCS</div>
        <h1 class="auth-title">로그인</h1>
        <p class="auth-subtitle">YCS 물류 시스템에 오신 것을 환영합니다</p>
      </div>
      
      <form class="auth-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <input 
            id="email"
            v-model="form.email" 
            type="email" 
            class="form-input" 
            placeholder="이메일 주소" 
            required
            @input="clearError('email')"
          />
          <div v-if="errors.email" class="error-message show">{{ errors.email }}</div>
        </div>
        
        <div class="form-group">
          <input 
            id="password"
            v-model="form.password" 
            type="password" 
            class="form-input" 
            placeholder="비밀번호" 
            required
            @input="clearError('password')"
          />
          <div v-if="errors.password" class="error-message show">{{ errors.password }}</div>
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

const clearError = (field: 'email' | 'password') => {
  errors[field] = ''
}

const validateEmail = (email: string) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
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
    }
  } catch (error) {
    console.error('로그인 오류:', error)
    errors.password = '로그인에 실패했습니다. 다시 시도해주세요.'
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

.error-message {
  color: #dc2626;
  font-size: 0.875rem;
  margin-top: 0.5rem;
  font-weight: 500;
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