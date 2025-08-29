<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">YCS</div>
        <h1 class="auth-title">회원가입</h1>
        <p class="auth-subtitle">YCS 물류 시스템에 가입하고 전문 배송 서비스를 이용하세요</p>
      </div>
      
      <!-- 회원 유형 선택 -->
      <div class="user-type-selector">
        <div 
          class="user-type-option general"
          :class="{ selected: form.userType === 'GENERAL' }"
          @click="selectUserType('GENERAL')"
        >
          <div class="user-type-title">개인 고객</div>
          <div class="user-type-desc">개인 고객용 일반 배송 서비스</div>
        </div>
        <div 
          class="user-type-option corporate"
          :class="{ selected: form.userType === 'CORPORATE' }"
          @click="selectUserType('CORPORATE')"
        >
          <div class="user-type-title">기업 고객</div>
          <div class="user-type-desc">기업용 전문 물류 서비스</div>
        </div>
        <div 
          class="user-type-option partner"
          :class="{ selected: form.userType === 'PARTNER' }"
          @click="selectUserType('PARTNER')"
        >
          <div class="user-type-title">파트너</div>
          <div class="user-type-desc">제휴 마케팅 파트너</div>
        </div>
      </div>
      
      <form class="auth-form" @submit.prevent="handleRegister">
        <!-- 기본 정보 -->
        <div class="form-section">
          <div class="form-section-title">기본 정보</div>
          
          <div class="form-group">
            <label class="form-label required" for="name">이름</label>
            <input 
              id="name"
              v-model="form.name" 
              type="text" 
              class="form-input" 
              placeholder="홍길동" 
              required
            />
          </div>
          
          <div class="form-group">
            <label class="form-label required" for="phone">연락처</label>
            <input 
              id="phone"
              v-model="form.phone" 
              type="tel" 
              class="form-input" 
              placeholder="010-1234-5678" 
              required
              @input="formatPhone"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label required" for="email">이메일</label>
            <div style="display: flex; gap: 0.5rem;">
              <input 
                id="email"
                v-model="form.email" 
                type="email" 
                class="form-input" 
                placeholder="example@email.com" 
                required 
                style="flex: 1;"
                :disabled="emailVerified"
              />
              <button 
                type="button" 
                class="verify-btn" 
                :disabled="!form.email || emailVerified"
                @click="sendVerificationCode"
              >
                {{ emailVerified ? '인증완료' : (verificationSent ? '재전송' : '인증요청') }}
              </button>
            </div>
            <div class="form-hint">로그인 ID로 사용됩니다</div>
          </div>
          
          <div v-if="verificationSent" class="form-group">
            <label class="form-label required" for="verifyCode">인증코드</label>
            <div style="display: flex; gap: 0.5rem;">
              <input 
                id="verifyCode"
                v-model="verificationCode" 
                type="text" 
                class="form-input" 
                placeholder="인증코드 6자리 입력" 
                maxlength="6" 
                style="flex: 1;"
                :disabled="emailVerified"
              />
              <button 
                type="button" 
                class="verify-btn" 
                :disabled="emailVerified"
                @click="confirmVerificationCode"
              >
                인증확인
              </button>
            </div>
            <div class="form-hint">
              <span v-if="verificationTimeLeft > 0" style="color: #ef4444;">
                {{ Math.floor(verificationTimeLeft / 60) }}:{{ (verificationTimeLeft % 60).toString().padStart(2, '0') }}
              </span>
              <span v-if="verificationStatus" :style="{ color: verificationStatus.includes('완료') ? '#059669' : '#ef4444' }">
                {{ verificationStatus }}
              </span>
            </div>
          </div>
          
          <div class="form-group">
            <label class="form-label required" for="password">비밀번호</label>
            <input 
              id="password"
              v-model="form.password" 
              type="password" 
              class="form-input" 
              placeholder="8자 이상 입력" 
              required
            />
          </div>
          
          <div class="form-group">
            <label class="form-label required" for="passwordConfirm">비밀번호 확인</label>
            <input 
              id="passwordConfirm"
              v-model="form.passwordConfirm" 
              type="password" 
              class="form-input" 
              placeholder="비밀번호 재입력" 
              required
              @blur="checkPasswordMatch"
            />
            <div v-if="passwordError" class="form-hint" style="color: #ef4444;">
              {{ passwordError }}
            </div>
          </div>
        </div>
        
        <!-- 기업/파트너 정보 -->
        <div v-if="showBusinessInfo" class="form-section business-info">
          <div class="form-section-title">사업자 정보</div>
          
          <div class="form-group">
            <label class="form-label required" for="companyName">회사명</label>
            <input 
              id="companyName"
              v-model="form.companyName" 
              type="text" 
              class="form-input" 
              placeholder="(주)회사명"
              :required="showBusinessInfo"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label required" for="businessNumber">사업자등록번호</label>
            <input 
              id="businessNumber"
              v-model="form.businessNumber" 
              type="text" 
              class="form-input" 
              placeholder="123-45-67890"
              :required="showBusinessInfo"
              @input="formatBusinessNumber"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label" for="contactPerson">담당자명</label>
            <input 
              id="contactPerson"
              v-model="form.contactPerson" 
              type="text" 
              class="form-input" 
              placeholder="담당자 이름"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label" for="contactPhone">담당자 연락처</label>
            <input 
              id="contactPhone"
              v-model="form.contactPhone" 
              type="tel" 
              class="form-input" 
              placeholder="010-0000-0000"
              @input="formatContactPhone"
            />
          </div>
          
          <div class="form-group">
            <label class="form-label" for="address">회사 주소</label>
            <input 
              id="address"
              v-model="form.address" 
              type="text" 
              class="form-input" 
              placeholder="서울시 강남구..."
            />
          </div>
          
          <div class="form-group">
            <label class="form-label required" for="businessLicense">사업자등록증</label>
            <div class="file-upload-container">
              <input 
                id="businessLicense"
                type="file" 
                class="form-input file-input" 
                accept="image/*,.pdf" 
                :required="showBusinessInfo"
                @change="handleFileUpload"
              />
              <div class="file-upload-hint">
                <span>사업자등록증을 업로드해주세요 (이미지 또는 PDF)</span>
              </div>
            </div>
            <div v-if="uploadedFile" class="file-preview">
              <div class="file-info">
                <span class="file-name">{{ uploadedFile.name }}</span>
                <button type="button" class="file-remove" @click="removeFile">×</button>
              </div>
            </div>
          </div>
          
          <div v-if="showBusinessInfo" class="approval-notice show">
            <div class="approval-notice-title">승인 안내</div>
            <div class="approval-notice-text">
              기업 고객 및 파트너 가입은 관리자 승인이 필요합니다.<br>
              • 승인 기간: 평일 기준 1~2일<br>
              • 승인 완료 시 이메일로 안내드립니다.<br>
              • 추가 서류가 필요할 경우 별도 연락드립니다.
            </div>
          </div>
        </div>
        
        <!-- 약관 동의 -->
        <div class="agreement-section">
          <div class="agreement-item">
            <input 
              id="agreeAll"
              v-model="agreeAll" 
              type="checkbox" 
              class="agreement-checkbox"
              @change="toggleAllAgreements"
            />
            <label class="agreement-text" for="agreeAll">
              <strong>전체 동의</strong>
            </label>
          </div>
          
          <div class="agreement-item">
            <input 
              id="agreeTerms"
              v-model="form.agreeTerms" 
              type="checkbox" 
              class="agreement-checkbox" 
              required
              @change="updateAgreeAll"
            />
            <label class="agreement-text" for="agreeTerms">
              <span class="agreement-required">[필수]</span> 서비스 이용약관에 동의합니다
            </label>
            <a href="#" class="agreement-link">보기</a>
          </div>
          
          <div class="agreement-item">
            <input 
              id="agreePrivacy"
              v-model="form.agreePrivacy" 
              type="checkbox" 
              class="agreement-checkbox" 
              required
              @change="updateAgreeAll"
            />
            <label class="agreement-text" for="agreePrivacy">
              <span class="agreement-required">[필수]</span> 개인정보 처리방침에 동의합니다
            </label>
            <a href="#" class="agreement-link">보기</a>
          </div>
          
          <div class="agreement-item">
            <input 
              id="agreeMarketing"
              v-model="form.agreeMarketing" 
              type="checkbox" 
              class="agreement-checkbox"
              @change="updateAgreeAll"
            />
            <label class="agreement-text" for="agreeMarketing">
              [선택] 마케팅 정보 수신에 동의합니다
            </label>
          </div>
        </div>
        
        <button 
          type="submit" 
          class="register-btn" 
          :disabled="!canRegister"
        >
          가입하기
        </button>
      </form>
      
      <div class="auth-links">
        <span style="color: #6b7280;">이미 계정이 있으신가요?</span>
        <router-link to="/login" class="auth-link">로그인</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { USER_TYPE } from '@/types'
import type { UserType } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  userType: '' as UserType | '',
  name: '',
  phone: '',
  email: '',
  password: '',
  passwordConfirm: '',
  companyName: '',
  businessNumber: '',
  contactPerson: '',
  contactPhone: '',
  address: '',
  agreeTerms: false,
  agreePrivacy: false,
  agreeMarketing: false
})

const emailVerified = ref(false)
const verificationSent = ref(false)
const verificationCode = ref('')
const verificationStatus = ref('')
const verificationTimeLeft = ref(0)
const passwordError = ref('')
const uploadedFile = ref<File | null>(null)
const agreeAll = ref(false)

let verificationTimer: NodeJS.Timeout | null = null
let actualVerificationCode = ''

const showBusinessInfo = computed(() => 
  form.userType === USER_TYPE.CORPORATE || form.userType === USER_TYPE.PARTNER
)

const canRegister = computed(() => {
  const basicValid = form.name && form.phone && form.email && form.password && 
                    form.passwordConfirm && emailVerified.value && 
                    form.agreeTerms && form.agreePrivacy

  const businessValid = !showBusinessInfo.value || 
    (form.companyName && form.businessNumber && uploadedFile.value)
  
  return basicValid && businessValid && !passwordError.value
})

const selectUserType = (type: UserType) => {
  form.userType = type
}

const formatPhone = (event: Event) => {
  const input = event.target as HTMLInputElement
  let value = input.value.replace(/[^\d]/g, '')
  
  if (value.length > 3 && value.length <= 7) {
    value = value.slice(0, 3) + '-' + value.slice(3)
  } else if (value.length > 7) {
    value = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11)
  }
  
  form.phone = value
}

const formatContactPhone = (event: Event) => {
  const input = event.target as HTMLInputElement
  let value = input.value.replace(/[^\d]/g, '')
  
  if (value.length > 3 && value.length <= 7) {
    value = value.slice(0, 3) + '-' + value.slice(3)
  } else if (value.length > 7) {
    value = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11)
  }
  
  form.contactPhone = value
}

const formatBusinessNumber = (event: Event) => {
  const input = event.target as HTMLInputElement
  let value = input.value.replace(/[^\d]/g, '')
  
  if (value.length > 3 && value.length <= 5) {
    value = value.slice(0, 3) + '-' + value.slice(3)
  } else if (value.length > 5) {
    value = value.slice(0, 3) + '-' + value.slice(3, 5) + '-' + value.slice(5, 10)
  }
  
  form.businessNumber = value
}

const sendVerificationCode = () => {
  if (!form.email) {
    alert('이메일을 입력해주세요.')
    return
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.email)) {
    alert('올바른 이메일 형식을 입력해주세요.')
    return
  }
  
  actualVerificationCode = Math.floor(100000 + Math.random() * 900000).toString()
  verificationSent.value = true
  verificationTimeLeft.value = 300
  
  console.log(`인증코드: ${actualVerificationCode} (실제 서비스에서는 ${form.email}로 전송됩니다)`)
  verificationStatus.value = '인증코드가 이메일로 전송되었습니다.'
  
  startVerificationTimer()
}

const startVerificationTimer = () => {
  if (verificationTimer) {
    clearInterval(verificationTimer)
  }
  
  verificationTimer = setInterval(() => {
    verificationTimeLeft.value--
    
    if (verificationTimeLeft.value <= 0) {
      clearInterval(verificationTimer!)
      verificationStatus.value = '인증시간이 만료되었습니다. 다시 요청해주세요.'
      actualVerificationCode = ''
    }
  }, 1000)
}

const confirmVerificationCode = () => {
  if (!verificationCode.value) {
    alert('인증코드를 입력해주세요.')
    return
  }
  
  if (verificationCode.value === actualVerificationCode) {
    emailVerified.value = true
    verificationStatus.value = '이메일 인증이 완료되었습니다.'
    
    if (verificationTimer) {
      clearInterval(verificationTimer)
      verificationTimer = null
    }
  } else {
    alert('인증코드가 일치하지 않습니다.')
  }
}

const checkPasswordMatch = () => {
  if (form.password && form.passwordConfirm && form.password !== form.passwordConfirm) {
    passwordError.value = '비밀번호가 일치하지 않습니다.'
  } else {
    passwordError.value = ''
  }
}

const handleFileUpload = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  
  if (file) {
    if (file.size > 5 * 1024 * 1024) {
      alert('파일 크기는 5MB 이하여야 합니다.')
      input.value = ''
      return
    }
    
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'application/pdf']
    if (!allowedTypes.includes(file.type)) {
      alert('JPG, PNG, PDF 파일만 업로드 가능합니다.')
      input.value = ''
      return
    }
    
    uploadedFile.value = file
  } else {
    uploadedFile.value = null
  }
}

const removeFile = () => {
  uploadedFile.value = null
  const input = document.getElementById('businessLicense') as HTMLInputElement
  if (input) input.value = ''
}

const toggleAllAgreements = () => {
  form.agreeTerms = agreeAll.value
  form.agreePrivacy = agreeAll.value
  form.agreeMarketing = agreeAll.value
}

const updateAgreeAll = () => {
  agreeAll.value = form.agreeTerms && form.agreePrivacy && form.agreeMarketing
}

const handleRegister = async () => {
  if (!emailVerified.value) {
    alert('이메일 인증을 완료해주세요.')
    return
  }
  
  if (showBusinessInfo.value && !uploadedFile.value) {
    alert('사업자등록증을 업로드해주세요.')
    return
  }
  
  try {
    const result = await authStore.register({
      userType: form.userType as UserType,
      name: form.name,
      email: form.email,
      phone: form.phone,
      password: form.password,
      companyName: form.companyName || undefined,
      businessNumber: form.businessNumber || undefined,
      contactPerson: form.contactPerson || undefined,
      contactPhone: form.contactPhone || undefined,
      address: form.address || undefined,
      agreeMarketing: form.agreeMarketing
    })
    
    if (result.success) {
      if (showBusinessInfo.value) {
        alert(`가입 신청이 완료되었습니다.\n\n관리자 승인 후 이용 가능합니다.\n승인 기간: 평일 기준 1~2일\n\n승인 완료 시 ${form.email}로 안내드립니다.`)
      } else {
        alert('가입이 완료되었습니다!\n로그인 페이지로 이동합니다.')
      }
      
      router.push('/login')
    }
  } catch (error: any) {
    console.error('회원가입 오류:', error)
    const errorMessage = error.error || error.message || '회원가입 중 오류가 발생했습니다.'
    alert(`회원가입 실패: ${errorMessage}\n\n다시 시도해주세요.`)
  }
}

onUnmounted(() => {
  if (verificationTimer) {
    clearInterval(verificationTimer)
  }
})
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  padding: 2rem 1rem;
}

.auth-card {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  max-width: 500px;
  margin: 0 auto;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  border: 1px solid #dbeafe;
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
}

.auth-logo {
  width: 60px;
  height: 60px;
  background: #3b82f6;
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem auto;
  color: white;
  font-size: 1.25rem;
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

.user-type-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
  margin-bottom: 2rem;
}

.user-type-option {
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 0.75rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.user-type-option:hover {
  border-color: #93c5fd;
  background: #f0f9ff;
}

.user-type-option.selected {
  border-color: #3b82f6;
  background: #f0f9ff;
}

.user-type-option.corporate.selected {
  border-color: #f97316;
  background: #fff7ed;
}

.user-type-option.partner.selected {
  border-color: #8b5cf6;
  background: #f5f3ff;
}

.user-type-title {
  font-weight: 600;
  margin-bottom: 0.25rem;
  font-size: 0.875rem;
}

.user-type-desc {
  font-size: 0.75rem;
  color: #6b7280;
  line-height: 1.3;
}

.form-section {
  background: #f8fafc;
  border-radius: 0.75rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  border: 1px solid #dbeafe;
}

.form-section-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1e3a8a;
  margin-bottom: 1rem;
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

.form-label.required::after {
  content: ' *';
  color: #dc2626;
}

.form-input {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  font-size: 0.875rem;
  transition: all 0.2s ease;
  background: white;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input:disabled {
  background: #f3f4f6;
  color: #6b7280;
}

.form-hint {
  font-size: 0.75rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

.verify-btn {
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 0.5rem;
  padding: 0.875rem 1rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  min-width: 80px;
}

.verify-btn:hover:not(:disabled) {
  background: #2563eb;
}

.verify-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.file-upload-container {
  position: relative;
  border: 2px dashed #d1d5db;
  border-radius: 0.75rem;
  padding: 1.5rem;
  text-align: center;
  transition: all 0.2s ease;
  background: #f9fafb;
}

.file-upload-container:hover {
  border-color: #93c5fd;
  background: #f0f9ff;
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  padding: 0 !important;
  border: none !important;
}

.file-upload-hint {
  pointer-events: none;
}

.file-upload-hint span {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}

.file-preview {
  margin-top: 0.75rem;
  padding: 0.75rem;
  background: #f0f9ff;
  border-radius: 0.5rem;
  border: 1px solid #bae6fd;
}

.file-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.file-name {
  font-size: 0.875rem;
  color: #0369a1;
  font-weight: 500;
}

.file-remove {
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-remove:hover {
  background: #b91c1c;
}

.approval-notice {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 0.75rem;
  padding: 1rem;
  margin-top: 1rem;
}

.approval-notice.show {
  display: block;
}

.approval-notice-title {
  font-weight: 600;
  color: #ea580c;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.approval-notice-text {
  font-size: 0.8rem;
  color: #9a3412;
  line-height: 1.4;
}

.agreement-section {
  background: white;
  border: 1px solid #bae6fd;
  border-radius: 0.75rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.agreement-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
  padding: 0.75rem;
  border-radius: 0.5rem;
  transition: all 0.2s ease;
}

.agreement-item:hover {
  background: #f0f9ff;
}

.agreement-checkbox {
  width: 1.125rem;
  height: 1.125rem;
  accent-color: #3b82f6;
}

.agreement-text {
  flex: 1;
  font-size: 0.875rem;
  color: #374151;
}

.agreement-required {
  color: #dc2626;
  font-weight: 500;
}

.agreement-link {
  color: #2563eb;
  text-decoration: none;
  font-size: 0.8rem;
}

.agreement-link:hover {
  text-decoration: underline;
}

.register-btn {
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
  margin-bottom: 1rem;
}

.register-btn:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-1px);
}

.register-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  transform: none;
}

.auth-links {
  text-align: center;
}

.auth-link {
  color: #2563eb;
  text-decoration: none;
  font-size: 0.875rem;
}

.auth-link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .auth-container {
    padding: 1rem 0.5rem;
  }
  
  .auth-card {
    padding: 1.5rem;
  }
  
  .user-type-selector {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }
  
  .form-section {
    padding: 1rem;
  }
}
</style>