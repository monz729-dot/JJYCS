<template>
  <div class="profile-view">
    <div class="container mx-auto p-6">
      <div class="max-w-4xl mx-auto">
        <!-- Header -->
        <div class="bg-white rounded-lg shadow p-6 mb-6">
          <h1 class="text-2xl font-bold text-gray-900 mb-2">프로필</h1>
          <p class="text-gray-600">계정 정보를 관리하고 설정을 변경할 수 있습니다.</p>
        </div>

        <!-- Profile Info -->
        <div class="bg-white rounded-lg shadow p-6 mb-6">
          <div class="flex items-center space-x-6">
            <!-- Avatar -->
            <div class="w-20 h-20 bg-gray-200 rounded-full flex items-center justify-center">
              <UserIcon class="w-10 h-10 text-gray-500" />
            </div>
            
            <!-- User Info -->
            <div class="flex-1">
              <h2 class="text-xl font-semibold text-gray-900">{{ user?.name || '사용자' }}</h2>
              <p class="text-gray-600">{{ user?.email || 'email@example.com' }}</p>
              <div class="flex items-center space-x-2 mt-2">
                <span class="px-3 py-1 text-sm bg-blue-100 text-blue-800 rounded-full">
                  {{ getRoleText(user?.role || 'individual') }}
                </span>
                <span class="px-3 py-1 text-sm bg-green-100 text-green-800 rounded-full">
                  {{ getStatusText(user?.status || 'active') }}
                </span>
              </div>
            </div>
            
            <!-- Edit Button -->
            <div class="flex space-x-2">
              <button
                v-if="!editMode"
                @click="startEdit"
                :disabled="isLoading"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                편집
              </button>
              <button
                v-if="editMode"
                @click="cancelEdit"
                :disabled="isLoading"
                class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 disabled:opacity-50"
              >
                취소
              </button>
            </div>
          </div>
        </div>

        <!-- Profile Form -->
        <div class="bg-white rounded-lg shadow p-6">
          <form @submit.prevent="updateProfile">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Name -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">이름</label>
                <input
                  v-model="form.name"
                  :disabled="!editMode"
                  type="text"
                  class="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-50"
                  :class="{
                    'border-red-300': errors.name,
                    'border-gray-300': !errors.name
                  }"
                />
                <p v-if="errors.name" class="mt-1 text-sm text-red-600">{{ errors.name }}</p>
              </div>

              <!-- Email -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">이메일</label>
                <input
                  v-model="form.email"
                  disabled
                  type="email"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50"
                />
                <p class="text-sm text-gray-500 mt-1">이메일은 변경할 수 없습니다.</p>
              </div>

              <!-- Phone -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">전화번호</label>
                <input
                  v-model="form.phone"
                  :disabled="!editMode"
                  type="tel"
                  placeholder="010-1234-5678"
                  class="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:bg-gray-50"
                  :class="{
                    'border-red-300': errors.phone,
                    'border-gray-300': !errors.phone
                  }"
                />
                <p v-if="errors.phone" class="mt-1 text-sm text-red-600">{{ errors.phone }}</p>
              </div>

              <!-- Role (Read-only) -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">계정 유형</label>
                <input
                  :value="getRoleText(user?.role || 'individual')"
                  disabled
                  type="text"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50"
                />
              </div>

              <!-- Member Code -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">회원 코드</label>
                <div class="flex items-center space-x-2">
                  <input
                    :value="memberCode || '미설정'"
                    disabled
                    type="text"
                    class="flex-1 px-4 py-2 border border-gray-300 rounded-lg bg-gray-50"
                  />
                  <button
                    v-if="!hasMemberCode"
                    @click="generateMemberCode"
                    :disabled="isLoading"
                    type="button"
                    class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50"
                  >
                    생성
                  </button>
                </div>
                <p v-if="!hasMemberCode" class="text-sm text-orange-600 mt-1">
                  ⚠️ 회원 코드가 없으면 배송이 지연될 수 있습니다.
                </p>
              </div>

              <!-- Email Verification Status -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">이메일 인증</label>
                <div class="flex items-center space-x-2">
                  <span 
                    class="px-3 py-1 text-sm rounded-full"
                    :class="{
                      'bg-green-100 text-green-800': isEmailVerified,
                      'bg-yellow-100 text-yellow-800': !isEmailVerified
                    }"
                  >
                    {{ isEmailVerified ? '✓ 인증 완료' : '○ 미인증' }}
                  </span>
                  <button
                    v-if="!isEmailVerified"
                    type="button"
                    class="text-sm text-blue-600 hover:text-blue-800"
                    @click="resendVerificationEmail"
                  >
                    인증 메일 재발송
                  </button>
                </div>
              </div>

              <!-- 2FA Status -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">2단계 인증</label>
                <div class="flex items-center space-x-2">
                  <span 
                    class="px-3 py-1 text-sm rounded-full"
                    :class="{
                      'bg-green-100 text-green-800': is2FAEnabled,
                      'bg-gray-100 text-gray-800': !is2FAEnabled
                    }"
                  >
                    {{ is2FAEnabled ? '✓ 활성화' : '○ 비활성화' }}
                  </span>
                  <router-link
                    to="/profile/security"
                    class="text-sm text-blue-600 hover:text-blue-800"
                  >
                    {{ is2FAEnabled ? '설정 관리' : '활성화하기' }}
                  </router-link>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div v-if="editMode" class="mt-6 flex justify-end space-x-4">
              <button
                type="button"
                @click="cancelEdit"
                :disabled="isLoading"
                class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 disabled:opacity-50"
              >
                취소
              </button>
              <button
                type="submit"
                :disabled="isLoading"
                class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 relative"
              >
                <span v-if="isLoading" class="absolute inset-0 flex items-center justify-center">
                  <svg class="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                </span>
                <span :class="{ 'opacity-0': isLoading }">저장</span>
              </button>
            </div>
          </form>
        </div>

        <!-- Quick Actions -->
        <div class="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
          <router-link
            to="/profile/security"
            class="bg-white p-4 rounded-lg shadow hover:shadow-md transition-shadow"
          >
            <div class="flex items-center space-x-3">
              <LockClosedIcon class="w-8 h-8 text-blue-600" />
              <div>
                <h3 class="font-medium text-gray-900">보안 설정</h3>
                <p class="text-sm text-gray-500">비밀번호 및 2FA 관리</p>
              </div>
            </div>
          </router-link>

          <router-link
            to="/profile/preferences"
            class="bg-white p-4 rounded-lg shadow hover:shadow-md transition-shadow"
          >
            <div class="flex items-center space-x-3">
              <CogIcon class="w-8 h-8 text-green-600" />
              <div>
                <h3 class="font-medium text-gray-900">환경 설정</h3>
                <p class="text-sm text-gray-500">언어 및 알림 설정</p>
              </div>
            </div>
          </router-link>

          <router-link
            to="/profile/notifications"
            class="bg-white p-4 rounded-lg shadow hover:shadow-md transition-shadow"
          >
            <div class="flex items-center space-x-3">
              <BellIcon class="w-8 h-8 text-yellow-600" />
              <div>
                <h3 class="font-medium text-gray-900">알림 설정</h3>
                <p class="text-sm text-gray-500">알림 수신 방법 설정</p>
              </div>
            </div>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import {
  UserIcon,
  LockClosedIcon,
  CogIcon,
  BellIcon
} from '@heroicons/vue/24/outline'

const authStore = useAuthStore()
const notificationStore = useNotificationStore()

// State
const editMode = ref(false)
const isLoading = ref(false)
const errors = ref<Record<string, string>>({})

// Computed
const user = computed(() => authStore.user)

// Reactive form
const form = reactive({
  name: '',
  email: '',
  phone: ''
})

// Initialize form data when user changes
const initializeForm = () => {
  if (user.value) {
    form.name = user.value.name || ''
    form.email = user.value.email || ''
    form.phone = user.value.phone || ''
  }
}

// Watch for user changes
watch(user, initializeForm, { immediate: true })

// Methods
const getRoleText = (role: string) => {
  const roleTexts = {
    'individual': '개인',
    'enterprise': '기업',
    'partner': '파트너',
    'warehouse': '창고',
    'admin': '관리자'
  }
  return roleTexts[role as keyof typeof roleTexts] || role
}

const getStatusText = (status: string) => {
  const statusTexts = {
    'active': '활성',
    'pending_approval': '승인 대기',
    'suspended': '정지'
  }
  return statusTexts[status as keyof typeof statusTexts] || status
}

// Validation
const validateForm = (): boolean => {
  errors.value = {}
  
  if (!form.name || form.name.trim().length < 2) {
    errors.value.name = '이름은 2자 이상 입력해주세요.'
  }
  
  if (!form.email || !/\S+@\S+\.\S+/.test(form.email)) {
    errors.value.email = '올바른 이메일 주소를 입력해주세요.'
  }
  
  if (form.phone && !/^01[016789]-?\d{3,4}-?\d{4}$/.test(form.phone)) {
    errors.value.phone = '올바른 전화번호 형식을 입력해주세요.'
  }
  
  return Object.keys(errors.value).length === 0
}

const updateProfile = async () => {
  if (!validateForm()) {
    notificationStore.error('입력 정보를 확인해주세요.', '프로필 업데이트 실패')
    return
  }

  isLoading.value = true
  
  try {
    const updateData = {
      name: form.name.trim(),
      phone: form.phone.trim()
    }
    
    const response = await authStore.updateProfile(updateData)
    
    if (response.success) {
      notificationStore.success(
        '프로필이 성공적으로 업데이트되었습니다.',
        '프로필 업데이트 완료'
      )
      editMode.value = false
      errors.value = {}
    } else {
      throw new Error(response.message || '프로필 업데이트에 실패했습니다.')
    }
  } catch (error: any) {
    console.error('Profile update error:', error)
    notificationStore.error(
      error.message || '프로필 업데이트 중 오류가 발생했습니다.',
      '프로필 업데이트 실패'
    )
  } finally {
    isLoading.value = false
  }
}

const cancelEdit = () => {
  editMode.value = false
  errors.value = {}
  initializeForm() // Reset form to original values
}

const startEdit = () => {
  editMode.value = true
  errors.value = {}
}

// Member code management
const hasMemberCode = computed(() => user.value?.memberCode)
const memberCode = computed(() => user.value?.memberCode)

const generateMemberCode = async () => {
  if (hasMemberCode.value) return
  
  try {
    isLoading.value = true
    
    // For demo purposes, generate a simple member code
    const rolePrefix = {
      'individual': 'IND',
      'enterprise': 'ENT',
      'partner': 'PTR',
      'warehouse': 'WH',
      'admin': 'ADM'
    }[user.value?.role || 'individual']
    
    const randomNum = Math.floor(Math.random() * 999) + 1
    const newMemberCode = `${rolePrefix}${String(randomNum).padStart(3, '0')}`
    
    const response = await authStore.updateProfile({ memberCode: newMemberCode })
    
    if (response.success) {
      notificationStore.success(
        `회원 코드가 생성되었습니다: ${newMemberCode}`,
        '회원 코드 생성 완료'
      )
    } else {
      throw new Error('회원 코드 생성에 실패했습니다.')
    }
  } catch (error: any) {
    console.error('Member code generation error:', error)
    notificationStore.error(
      error.message || '회원 코드 생성 중 오류가 발생했습니다.',
      '회원 코드 생성 실패'
    )
  } finally {
    isLoading.value = false
  }
}

// Account verification status
const isEmailVerified = computed(() => user.value?.emailVerified || false)
const is2FAEnabled = computed(() => user.value?.twoFactorEnabled || false)

// Email verification
const resendVerificationEmail = async () => {
  if (isEmailVerified.value) return
  
  try {
    isLoading.value = true
    
    // For demo purposes, simulate verification email sending
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    notificationStore.success(
      '인증 메일이 발송되었습니다. 이메일을 확인해주세요.',
      '인증 메일 발송 완료'
    )
  } catch (error: any) {
    console.error('Email verification error:', error)
    notificationStore.error(
      error.message || '인증 메일 발송 중 오류가 발생했습니다.',
      '인증 메일 발송 실패'
    )
  } finally {
    isLoading.value = false
  }
}

// Lifecycle
onMounted(async () => {
  try {
    // Ensure we have fresh user data
    if (authStore.token) {
      await authStore.getCurrentUser()
    }
    initializeForm()
  } catch (error) {
    console.error('Failed to load user data:', error)
  }
})
</script>

<style scoped>
.profile-view {
  min-height: 100vh;
  background-color: #f8fafc;
}
</style>