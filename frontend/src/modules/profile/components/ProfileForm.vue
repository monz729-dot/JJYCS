<template>
  <div class="max-w-4xl mx-auto p-6">
    <div class="bg-white rounded-lg shadow-lg p-8">
      <h2 class="text-2xl font-bold text-gray-900 mb-6">프로필 정보</h2>
      
      <!-- 승인 상태 표시 -->
      <div v-if="user && user.user_type !== 'general'" class="mb-6">
        <div class="p-4 rounded-md" :class="{
          'bg-yellow-50 text-yellow-800 border border-yellow-200': user.approval_status === 'pending',
          'bg-green-50 text-green-800 border border-green-200': user.approval_status === 'approved',
          'bg-red-50 text-red-800 border border-red-200': user.approval_status === 'rejected'
        }">
          <div class="flex items-center">
            <svg v-if="user.approval_status === 'pending'" class="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd" />
            </svg>
            <svg v-if="user.approval_status === 'approved'" class="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
            <svg v-if="user.approval_status === 'rejected'" class="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
            <span class="text-sm font-medium">
              {{ 
                user.approval_status === 'pending' ? '승인 대기 중입니다. 관리자 승인 후 서비스 이용이 가능합니다.' :
                user.approval_status === 'approved' ? '승인이 완료되었습니다.' :
                '승인이 거절되었습니다. 관리자에게 문의하세요.'
              }}
            </span>
          </div>
        </div>
      </div>

      <form @submit.prevent="handleSubmit" class="space-y-6">
        <!-- 기본 정보 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">아이디</label>
            <input
              :value="user?.username"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50"
              readonly
            />
            <p class="text-xs text-gray-500 mt-1">아이디는 변경할 수 없습니다.</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">회원 종류</label>
            <input
              :value="getUserTypeLabel(user?.user_type)"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50"
              readonly
            />
          </div>

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

          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 mb-2">주소</label>
            <input
              v-model="form.address"
              type="text"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="주소를 입력하세요"
            />
          </div>
        </div>

        <!-- 이메일 변경 섹션 -->
        <div class="border-t pt-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">이메일 변경</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">현재 이메일</label>
              <input
                :value="user?.email"
                type="email"
                class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50"
                readonly
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">새 이메일</label>
              <div class="flex gap-2">
                <input
                  v-model="form.newEmail"
                  type="email"
                  class="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="새 이메일을 입력하세요"
                />
                <button
                  type="button"
                  @click="sendEmailVerification"
                  :disabled="!form.newEmail || emailVerificationLoading"
                  class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
                >
                  {{ emailVerificationLoading ? '발송중...' : '인증메일 발송' }}
                </button>
              </div>
            </div>
          </div>
          <div v-if="emailVerificationMessage" class="mt-2 text-sm" :class="{
            'text-green-600': emailVerificationSuccess,
            'text-red-600': !emailVerificationSuccess
          }">
            {{ emailVerificationMessage }}
          </div>
        </div>

        <!-- 사업자등록증 업로드 (기업/파트너 회원) -->
        <div v-if="user && (user.user_type === 'corporate' || user.user_type === 'partner')" class="border-t pt-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">사업자등록증</h3>
          
          <!-- 기존 파일 표시 -->
          <div v-if="user.business_license_url" class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">현재 등록된 파일</label>
            <div class="flex items-center gap-2 p-3 bg-gray-50 rounded-md">
              <svg class="h-5 w-5 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clip-rule="evenodd" />
              </svg>
              <a :href="user.business_license_url" target="_blank" class="text-blue-600 hover:text-blue-800">
                사업자등록증 보기
              </a>
            </div>
          </div>

          <!-- 새 파일 업로드 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">새 사업자등록증 업로드</label>
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
            :disabled="loading"
            class="bg-blue-600 text-white py-3 px-6 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ loading ? '저장 중...' : '저장' }}
          </button>
          <button
            type="button"
            @click="resetForm"
            class="bg-gray-500 text-white py-3 px-6 rounded-md hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-500"
          >
            초기화
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'
import type { UserProfile } from '@/lib/supabase'

const authStore = useAuthStore()
const toast = useToast()

// 상태
const loading = ref(false)
const error = ref('')
const success = ref('')
const emailVerificationLoading = ref(false)
const emailVerificationMessage = ref('')
const emailVerificationSuccess = ref(false)

// 사용자 정보
const user = computed(() => authStore.user)

// 폼 데이터
const form = reactive({
  name: '',
  phone: '',
  address: '',
  newEmail: '',
  business_license_file: null as File | null
})

// 파일 입력 참조
const fileInput = ref<HTMLInputElement>()

// 회원 종류 라벨
const getUserTypeLabel = (userType?: string) => {
  switch (userType) {
    case 'general': return '일반회원'
    case 'corporate': return '기업회원'
    case 'partner': return '파트너회원'
    default: return ''
  }
}

// 폼 초기화
const initializeForm = () => {
  if (user.value) {
    form.name = user.value.name || ''
    form.phone = user.value.phone || ''
    form.address = user.value.address || ''
    form.newEmail = ''
    form.business_license_file = null
  }
}

// 폼 리셋
const resetForm = () => {
  initializeForm()
  error.value = ''
  success.value = ''
  emailVerificationMessage.value = ''
  emailVerificationSuccess.value = false
}

// 이메일 인증 메일 발송
const sendEmailVerification = async () => {
  if (!form.newEmail) {
    emailVerificationMessage.value = '새 이메일을 입력해주세요.'
    emailVerificationSuccess.value = false
    return
  }

  emailVerificationLoading.value = true
  emailVerificationMessage.value = ''
  emailVerificationSuccess.value = false

  try {
    // 실제 구현에서는 새 이메일로 인증 메일 발송
    // const result = await authStore.sendEmailChangeVerification(form.newEmail)
    
    // Mock 응답
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    emailVerificationMessage.value = '인증 메일을 발송했습니다. 이메일을 확인하여 인증을 완료해주세요.'
    emailVerificationSuccess.value = true
  } catch (err) {
    emailVerificationMessage.value = '인증 메일 발송에 실패했습니다.'
    emailVerificationSuccess.value = false
  } finally {
    emailVerificationLoading.value = false
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

// 폼 제출
const handleSubmit = async () => {
  loading.value = true
  error.value = ''
  success.value = ''

  try {
    const updates: Partial<UserProfile> = {
      name: form.name,
      phone: form.phone,
      address: form.address
    }

    // 새 이메일이 입력된 경우
    if (form.newEmail && form.newEmail !== user.value?.email) {
      // 실제 구현에서는 이메일 변경 로직
      // updates.email = form.newEmail
    }

    const result = await authStore.updateProfile(updates)

    if (result.success) {
      success.value = '프로필이 성공적으로 업데이트되었습니다.'
      toast.success('프로필이 업데이트되었습니다.')
      
      // 성공 후 폼 초기화
      setTimeout(() => {
        initializeForm()
        success.value = ''
      }, 3000)
    } else {
      error.value = result.error || '프로필 업데이트에 실패했습니다.'
    }
  } catch (err) {
    error.value = '프로필 업데이트 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

// 사용자 정보 변경 시 폼 초기화
watch(user, () => {
  initializeForm()
}, { immediate: true })

// 컴포넌트 마운트 시 초기화
onMounted(() => {
  authStore.clearError()
  initializeForm()
})
</script>

