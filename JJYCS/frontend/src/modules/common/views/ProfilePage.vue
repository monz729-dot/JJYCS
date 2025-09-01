<template>
  <div class="min-h-screen bg-gray-50">
    <AppLayout>
      <div class="max-w-4xl mx-auto py-6">
        <div class="bg-white shadow rounded-lg p-6">
          <div class="flex items-center justify-between mb-6">
            <h1 class="text-2xl font-bold text-gray-900">마이페이지</h1>
            <div v-if="profile.status === 'PENDING'" class="bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full text-sm font-medium">
              승인 대기중
            </div>
            <div v-else-if="profile.status === 'ACTIVE'" class="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium">
              활성화됨
            </div>
          </div>
          
          <div v-if="loading" class="text-center py-8">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
            <p class="mt-2 text-gray-600">사용자 정보를 불러오는 중...</p>
          </div>
          
          <div v-else-if="error" class="text-center py-8">
            <div class="text-red-600 mb-4">
              <svg class="w-12 h-12 mx-auto" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
              </svg>
            </div>
            <p class="text-gray-600 mb-4">{{ error }}</p>
            <button @click="loadUserProfile" class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
              다시 시도
            </button>
          </div>
          
          <div v-else class="space-y-6">
            <!-- 기본 정보 -->
            <div class="border-b pb-6">
              <h2 class="text-lg font-semibold text-gray-900 mb-4">기본 정보</h2>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">이름</label>
                  <input 
                    v-model="profile.name"
                    type="text" 
                    class="form-input"
                    :readonly="!isEditing"
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">이메일</label>
                  <input 
                    v-model="profile.email"
                    type="email" 
                    class="form-input"
                    readonly
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">전화번호</label>
                  <input 
                    v-model="profile.phone"
                    type="text" 
                    class="form-input"
                    :readonly="!isEditing"
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">사용자 역할</label>
                  <span class="inline-flex px-3 py-1 rounded-full text-sm font-medium" :class="getUserTypeClass(profile.userType)">
                    {{ getUserTypeText(profile.userType) }}
                  </span>
                </div>

                <div v-if="profile.memberCode">
                  <label class="block text-sm font-medium text-gray-700 mb-2">회원코드</label>
                  <input 
                    v-model="profile.memberCode"
                    type="text" 
                    class="form-input"
                    readonly
                  >
                </div>
                
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">가입일</label>
                  <input 
                    :value="formatDate(profile.createdAt)"
                    type="text" 
                    class="form-input"
                    readonly
                  >
                </div>
              </div>
            </div>

            <!-- 기업/파트너 추가 정보 -->
            <div v-if="profile.userType === 'CORPORATE' || profile.userType === 'PARTNER'" class="border-b pb-6">
              <h2 class="text-lg font-semibold text-gray-900 mb-4">
                {{ profile.userType === 'CORPORATE' ? '기업 정보' : '파트너 정보' }}
              </h2>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div v-if="profile.companyName">
                  <label class="block text-sm font-medium text-gray-700 mb-2">회사명</label>
                  <input 
                    v-model="profile.companyName"
                    type="text" 
                    class="form-input"
                    :readonly="!isEditing"
                  >
                </div>
                
                <div v-if="profile.businessNumber">
                  <label class="block text-sm font-medium text-gray-700 mb-2">사업자등록번호</label>
                  <input 
                    v-model="profile.businessNumber"
                    type="text" 
                    class="form-input"
                    readonly
                  >
                </div>

                <div v-if="profile.companyAddress">
                  <label class="block text-sm font-medium text-gray-700 mb-2">회사주소</label>
                  <input 
                    v-model="profile.companyAddress"
                    type="text" 
                    class="form-input"
                    :readonly="!isEditing"
                  >
                </div>
              </div>
            </div>

            <!-- 계정 상태 정보 -->
            <div v-if="profile.status === 'PENDING'" class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
              <div class="flex items-start">
                <svg class="w-5 h-5 text-yellow-600 mt-0.5 mr-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
                </svg>
                <div>
                  <h3 class="text-sm font-medium text-yellow-800">승인 대기중</h3>
                  <p class="mt-1 text-sm text-yellow-700">
                    {{ profile.userType === 'CORPORATE' ? '기업' : '파트너' }} 계정은 관리자 승인 후 이용 가능합니다. (평일 1-2일 소요)
                  </p>
                </div>
              </div>
            </div>
            
            <!-- 액션 버튼 -->
            <div class="flex gap-3 pt-4">
              <button 
                v-if="!isEditing"
                @click="isEditing = true"
                class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors"
              >
                정보 수정
              </button>
              
              <template v-if="isEditing">
                <button 
                  @click="updateProfile"
                  :disabled="updating"
                  class="bg-green-600 hover:bg-green-700 disabled:bg-gray-400 text-white font-medium py-2 px-4 rounded-lg transition-colors"
                >
                  {{ updating ? '저장 중...' : '저장' }}
                </button>
                <button 
                  @click="cancelEdit"
                  class="bg-gray-500 hover:bg-gray-600 text-white font-medium py-2 px-4 rounded-lg transition-colors"
                >
                  취소
                </button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </AppLayout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { authApi } from '@/utils/api'
import AppLayout from '@/components/ui/AppLayout.vue'

const authStore = useAuthStore()
const { success: showSuccess, error: showError } = useToast()

const profile = ref({
  id: null,
  name: '',
  email: '',
  phone: '',
  userType: '',
  memberCode: '',
  status: '',
  createdAt: '',
  companyName: '',
  businessNumber: '',
  companyAddress: ''
})

const originalProfile = ref({})
const loading = ref(false)
const error = ref('')
const isEditing = ref(false)
const updating = ref(false)

const loadUserProfile = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await authApi.getCurrentUser()
    
    if (response.success && response.data) {
      profile.value = {
        id: response.data.id,
        name: response.data.name || '',
        email: response.data.email || '',
        phone: response.data.phone || '',
        userType: response.data.userType || '',
        memberCode: response.data.memberCode || '',
        status: response.data.status || '',
        createdAt: response.data.createdAt || '',
        companyName: response.data.companyName || '',
        businessNumber: response.data.businessNumber || '',
        companyAddress: response.data.companyAddress || ''
      }
      
      // 원본 데이터 백업 (취소 시 복원용)
      originalProfile.value = { ...profile.value }
    } else {
      error.value = response.error || '사용자 정보를 불러올 수 없습니다.'
    }
  } catch (err) {
    console.error('Profile loading error:', err)
    error.value = '사용자 정보를 불러오는데 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const updateProfile = async () => {
  updating.value = true
  
  try {
    // 수정 가능한 필드만 전송
    const updateData = {
      name: profile.value.name,
      phone: profile.value.phone,
      companyName: profile.value.companyName,
      companyAddress: profile.value.companyAddress
    }
    
    const response = await authApi.updateProfile(updateData)
    
    if (response.success) {
      showSuccess('프로필이 성공적으로 업데이트되었습니다.')
      isEditing.value = false
      originalProfile.value = { ...profile.value }
      
      // AuthStore 업데이트
      if (authStore.user) {
        authStore.user.name = profile.value.name
        authStore.user.phone = profile.value.phone
      }
    } else {
      showError(response.error || '프로필 업데이트에 실패했습니다.')
    }
  } catch (err) {
    console.error('Profile update error:', err)
    showError('프로필 업데이트 중 오류가 발생했습니다.')
  } finally {
    updating.value = false
  }
}

const cancelEdit = () => {
  profile.value = { ...originalProfile.value }
  isEditing.value = false
}

const getUserTypeText = (userType: string) => {
  const types = {
    'GENERAL': '일반 회원',
    'CORPORATE': '기업 회원',
    'PARTNER': '파트너',
    'WAREHOUSE': '창고 관리자',
    'ADMIN': '시스템 관리자'
  }
  return types[userType as keyof typeof types] || userType
}

const getUserTypeClass = (userType: string) => {
  const classes = {
    'GENERAL': 'bg-blue-100 text-blue-800',
    'CORPORATE': 'bg-purple-100 text-purple-800',
    'PARTNER': 'bg-green-100 text-green-800',
    'WAREHOUSE': 'bg-orange-100 text-orange-800',
    'ADMIN': 'bg-red-100 text-red-800'
  }
  return classes[userType as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  try {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  } catch (err) {
    return dateString
  }
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
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
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input:read-only {
  background-color: #f9fafb;
  color: #6b7280;
}

.form-input.error {
  border-color: #dc2626;
  background-color: #fef2f2;
}
</style>