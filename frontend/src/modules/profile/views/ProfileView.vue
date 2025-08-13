<template>
  <div class="max-w-4xl mx-auto p-6">
    <div class="bg-white rounded-lg shadow-lg p-8">
      <h2 class="text-2xl font-bold text-gray-900 mb-6">마이페이지</h2>
      
      <!-- 프로필 정보 -->
      <div class="mb-8 p-6 bg-gray-50 rounded-lg">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">프로필 정보</h3>
        
        <div v-if="loading" class="text-center py-4">
          <div class="text-gray-600">정보를 불러오는 중...</div>
        </div>
        
        <div v-else-if="profile" class="space-y-4">
          <!-- 회원 상태 표시 -->
          <div class="flex items-center justify-between p-4 rounded-md" 
               :class="{
                 'bg-green-50 border border-green-200': profile.approval_status === 'approved',
                 'bg-yellow-50 border border-yellow-200': profile.approval_status === 'pending',
                 'bg-red-50 border border-red-200': profile.approval_status === 'rejected'
               }">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <svg v-if="profile.approval_status === 'approved'" class="h-5 w-5 text-green-400" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                </svg>
                <svg v-else-if="profile.approval_status === 'pending'" class="h-5 w-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                </svg>
                <svg v-else class="h-5 w-5 text-red-400" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3">
                <p class="text-sm font-medium" 
                   :class="{
                     'text-green-800': profile.approval_status === 'approved',
                     'text-yellow-800': profile.approval_status === 'pending',
                     'text-red-800': profile.approval_status === 'rejected'
                   }">
                  {{ getStatusText(profile.approval_status) }}
                </p>
                <p v-if="profile.user_type !== 'general' && profile.approval_status === 'pending'" 
                   class="text-xs mt-1"
                   :class="{
                     'text-yellow-600': profile.approval_status === 'pending'
                   }">
                  관리자 승인까지 평일 1~2일이 소요됩니다.
                </p>
              </div>
            </div>
            <div class="text-right">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                    :class="{
                      'bg-green-100 text-green-800': profile.approval_status === 'approved',
                      'bg-yellow-100 text-yellow-800': profile.approval_status === 'pending',
                      'bg-red-100 text-red-800': profile.approval_status === 'rejected'
                    }">
                {{ getUserTypeText(profile.user_type) }}
              </span>
            </div>
          </div>

          <!-- 기본 정보 표시 -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">아이디</label>
              <div class="mt-1 px-3 py-2 bg-gray-100 border border-gray-300 rounded-md text-gray-900">
                {{ profile.username }}
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">이메일</label>
              <div class="mt-1 px-3 py-2 bg-gray-100 border border-gray-300 rounded-md text-gray-900">
                {{ profile.email || '-' }}
                <span v-if="!profile.email_verified" class="ml-2 text-xs text-red-600">미인증</span>
                <span v-else class="ml-2 text-xs text-green-600">인증완료</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 정보 수정 폼 -->
      <form @submit.prevent="handleSubmit" class="space-y-6">
        <h3 class="text-lg font-semibold text-gray-900">정보 수정</h3>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">이름 *</label>
            <input
              v-model="form.name"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="이름을 입력하세요"
              required
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

        <!-- 이메일 변경 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            이메일 변경
            <span class="text-xs text-gray-500">(변경 시 이메일 인증 재발송)</span>
          </label>
          <input
            v-model="form.email"
            type="email"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="새 이메일을 입력하세요"
          />
        </div>

        <!-- 사업자등록증 업데이트 (기업/파트너 회원) -->
        <div v-if="profile && (profile.user_type === 'corporate' || profile.user_type === 'partner')">
          <label class="block text-sm font-medium text-gray-700 mb-2">사업자등록증 업데이트</label>
          
          <!-- 현재 파일 표시 -->
          <div v-if="profile.business_license_url" class="mb-2 p-3 bg-blue-50 border border-blue-200 rounded-md">
            <div class="flex items-center justify-between">
              <span class="text-sm text-blue-800">현재 업로드된 파일</span>
              <a 
                :href="profile.business_license_url" 
                target="_blank"
                class="text-blue-600 hover:text-blue-800 text-sm"
              >
                파일 보기
              </a>
            </div>
          </div>
          
          <!-- 새 파일 업로드 -->
          <div class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
            <input
              ref="fileInput"
              type="file"
              accept=".pdf,.jpg,.jpeg,.png"
              @change="handleFileChange"
              class="hidden"
            />
            <div v-if="!form.business_license_file" @click="$refs.fileInput?.click()" class="cursor-pointer">
              <div class="text-gray-600 mb-2">새 파일을 선택하거나 여기에 드래그하세요</div>
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

        <!-- 에러 메시지 -->
        <div v-if="error" class="text-red-600 text-sm bg-red-50 p-3 rounded-md">
          {{ error }}
        </div>

        <!-- 성공 메시지 -->
        <div v-if="success" class="text-green-600 text-sm bg-green-50 p-3 rounded-md">
          {{ success }}
        </div>

        <!-- 제출 버튼 -->
        <div class="flex gap-4">
          <button
            type="submit"
            :disabled="updateLoading"
            class="bg-blue-600 text-white py-2 px-6 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ updateLoading ? '업데이트 중...' : '정보 업데이트' }}
          </button>
          
          <button
            v-if="profile && !profile.email_verified"
            type="button"
            @click="resendEmailVerification"
            :disabled="emailLoading"
            class="bg-green-600 text-white py-2 px-6 rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ emailLoading ? '발송 중...' : '이메일 인증 재발송' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import { AuthService } from '@/services/authService'
import type { UserProfile } from '@/lib/supabase'

const router = useRouter()
const toast = useToast()

// 상태
const loading = ref(true)
const updateLoading = ref(false)
const emailLoading = ref(false)
const error = ref('')
const success = ref('')
const profile = ref<UserProfile | null>(null)

// 폼 데이터
const form = reactive({
  name: '',
  phone: '',
  address: '',
  email: '',
  business_license_file: null as File | null
})

// 사용자 상태 텍스트
const getStatusText = (status: string) => {
  switch (status) {
    case 'approved': return '승인 완료'
    case 'pending': return '승인 대기 중'
    case 'rejected': return '승인 거절'
    default: return '알 수 없음'
  }
}

// 회원 유형 텍스트
const getUserTypeText = (type: string) => {
  switch (type) {
    case 'general': return '일반회원'
    case 'corporate': return '기업회원'
    case 'partner': return '파트너회원'
    default: return '알 수 없음'
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
  const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
  if (fileInput) {
    fileInput.value = ''
  }
}

// 프로필 정보 로드
const loadProfile = async () => {
  loading.value = true
  error.value = ''

  try {
    const result = await AuthService.getCurrentUserProfile()
    
    if (result.data) {
      profile.value = result.data
      
      // 폼에 현재 정보 입력
      form.name = result.data.name || ''
      form.phone = result.data.phone || ''
      form.address = result.data.address || ''
      form.email = result.data.email || ''
    } else {
      error.value = result.error || '프로필 정보를 불러오지 못했습니다.'
    }
  } catch (err) {
    error.value = '프로필 정보 로드 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

// 정보 업데이트
const handleSubmit = async () => {
  updateLoading.value = true
  error.value = ''
  success.value = ''

  try {
    // 사업자등록증 파일 업로드 처리는 AuthService에서 처리
    const updates: Partial<UserProfile> = {
      name: form.name,
      phone: form.phone || undefined,
      address: form.address || undefined
    }

    // 이메일 변경이 있는 경우
    if (form.email !== profile.value?.email) {
      updates.email = form.email
      updates.email_verified = false // 이메일 변경 시 인증 상태 초기화
    }

    const result = await AuthService.updateUserProfile(updates)
    
    if (result.success) {
      success.value = '정보가 성공적으로 업데이트되었습니다.'
      
      // 이메일 변경이 있었다면 인증 이메일 자동 발송
      if (form.email !== profile.value?.email) {
        success.value += ' 이메일 인증 메일이 발송되었습니다.'
      }
      
      // 프로필 정보 재로드
      await loadProfile()
      
      toast.success(success.value)
    } else {
      error.value = result.error || '정보 업데이트에 실패했습니다.'
    }
  } catch (err) {
    error.value = '정보 업데이트 중 오류가 발생했습니다.'
  } finally {
    updateLoading.value = false
  }
}

// 이메일 인증 재발송
const resendEmailVerification = async () => {
  emailLoading.value = true
  error.value = ''

  try {
    const result = await AuthService.resendEmailVerification()
    
    if (result.success) {
      toast.success('이메일 인증 메일이 재발송되었습니다.')
    } else {
      error.value = result.error || '이메일 인증 재발송에 실패했습니다.'
    }
  } catch (err) {
    error.value = '이메일 인증 재발송 중 오류가 발생했습니다.'
  } finally {
    emailLoading.value = false
  }
}


// 컴포넌트 마운트 시 프로필 로드
onMounted(() => {
  loadProfile()
})
</script>