<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div class="text-center">
        <div class="mx-auto h-16 w-16 bg-yellow-600 rounded-lg flex items-center justify-center mb-6">
          <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <h1 class="text-3xl font-extrabold text-gray-900 mb-2">
          승인 대기 중
        </h1>
        <p class="text-gray-600">
          관리자 승인을 기다리고 있습니다.
        </p>
      </div>

      <div class="bg-white shadow-xl rounded-lg p-8">
        <div class="text-center space-y-6">
          <!-- 승인 대기 아이콘 -->
          <div class="mx-auto w-24 h-24 bg-yellow-100 rounded-full flex items-center justify-center">
            <svg class="w-12 h-12 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>

          <!-- 안내 메시지 -->
          <div class="space-y-4">
            <h2 class="text-xl font-semibold text-gray-900">
              승인 절차 안내
            </h2>
            <div class="text-left space-y-3 text-sm text-gray-600">
              <div class="flex items-start">
                <div class="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center mr-3 mt-0.5">
                  <span class="text-xs font-semibold text-blue-600">1</span>
                </div>
                <p>제출하신 정보를 검토합니다.</p>
              </div>
              <div class="flex items-start">
                <div class="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center mr-3 mt-0.5">
                  <span class="text-xs font-semibold text-blue-600">2</span>
                </div>
                <p>사업자등록증 및 기업 정보를 확인합니다.</p>
              </div>
              <div class="flex items-start">
                <div class="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center mr-3 mt-0.5">
                  <span class="text-xs font-semibold text-blue-600">3</span>
                </div>
                <p>승인 완료 시 이메일로 알려드립니다.</p>
              </div>
            </div>
          </div>

          <!-- 예상 소요 시간 -->
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div class="flex items-center">
              <svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
              </svg>
              <div class="ml-3">
                <p class="text-sm text-blue-800">
                  <strong>예상 소요 시간:</strong> 평일 1-2일
                </p>
              </div>
            </div>
          </div>

          <!-- 사용자 정보 요약 -->
          <div v-if="userInfo" class="bg-gray-50 rounded-lg p-4">
            <h3 class="font-medium text-gray-900 mb-3">제출 정보</h3>
            <div class="text-left space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-600">회사명:</span>
                <span class="font-medium">{{ userInfo.company_name }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">계정 유형:</span>
                <span class="font-medium">{{ getUserTypeText(userInfo.user_type) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">제출일:</span>
                <span class="font-medium">{{ formatDate(userInfo.created_at) }}</span>
              </div>
            </div>
          </div>

          <!-- 버튼들 -->
          <div class="space-y-4">
            <button
              @click="checkApprovalStatus"
              :disabled="checking"
              class="w-full py-3 px-4 border border-transparent text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="checking">확인 중...</span>
              <span v-else>승인 상태 확인</span>
            </button>

            <button
              @click="goToLogin"
              class="w-full py-3 px-4 border border-gray-300 text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 rounded-lg transition-colors"
            >
              로그인으로 돌아가기
            </button>
          </div>

          <!-- 성공/에러 메시지 -->
          <div v-if="message" class="p-4 rounded-lg" :class="messageType === 'success' ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'">
            <div class="flex">
              <svg v-if="messageType === 'success'" class="w-5 h-5 text-green-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
              <svg v-else class="w-5 h-5 text-red-400" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
              <div class="ml-3">
                <p class="text-sm" :class="messageType === 'success' ? 'text-green-800' : 'text-red-800'">
                  {{ message }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 문의 안내 -->
      <div class="bg-white shadow-lg rounded-lg p-6">
        <div class="text-center">
          <h3 class="font-medium text-gray-900 mb-2">문의사항이 있으신가요?</h3>
          <p class="text-sm text-gray-600 mb-4">
            승인 절차나 기타 문의사항은 고객센터로 연락해주세요.
          </p>
          <div class="space-y-2 text-sm">
            <div class="flex items-center justify-center">
              <svg class="w-4 h-4 text-gray-400 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
              <span class="text-gray-600">support@ycs-lms.com</span>
            </div>
            <div class="flex items-center justify-center">
              <svg class="w-4 h-4 text-gray-400 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
              </svg>
              <span class="text-gray-600">1588-1234</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'
import type { UserProfile } from '@/lib/supabase'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

// 상태
const userInfo = ref<UserProfile | null>(null)
const checking = ref(false)
const message = ref('')
const messageType = ref<'success' | 'error'>('success')

// 사용자 유형 텍스트 변환
const getUserTypeText = (userType: string) => {
  switch (userType) {
    case 'corporate':
      return '기업회원'
    case 'partner':
      return '파트너회원'
    default:
      return '일반회원'
  }
}

// 날짜 포맷팅
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 승인 상태 확인
const checkApprovalStatus = async () => {
  checking.value = true
  message.value = ''

  try {
    const result = await authStore.fetchUserProfile()
    
    if (result.success && authStore.user) {
      userInfo.value = authStore.user
      
      if (authStore.user.approval_status === 'approved') {
        message.value = '승인이 완료되었습니다! 잠시 후 대시보드로 이동합니다.'
        messageType.value = 'success'
        
        setTimeout(() => {
          router.push('/app/dashboard')
        }, 2000)
      } else if (authStore.user.approval_status === 'rejected') {
        message.value = '승인이 거절되었습니다. 고객센터로 문의해주세요.'
        messageType.value = 'error'
      } else {
        message.value = '아직 승인 대기 중입니다. 조금만 더 기다려주세요.'
        messageType.value = 'error'
      }
    } else {
      message.value = '사용자 정보를 가져올 수 없습니다.'
      messageType.value = 'error'
    }
  } catch (error) {
    message.value = '승인 상태 확인 중 오류가 발생했습니다.'
    messageType.value = 'error'
  } finally {
    checking.value = false
  }
}

// 로그인 페이지로 이동
const goToLogin = () => {
  router.push('/login')
}

// 컴포넌트 마운트 시
onMounted(async () => {
  // 현재 사용자 정보 가져오기
  const result = await authStore.fetchUserProfile()
  if (result.success && authStore.user) {
    userInfo.value = authStore.user
    
    // 이미 승인된 경우 대시보드로 이동
    if (authStore.user.approval_status === 'approved') {
      router.push('/app/dashboard')
    }
  } else {
    // 사용자 정보가 없으면 로그인 페이지로 이동
    router.push('/login')
  }
})
</script>

<style scoped>
/* Custom styles */
</style>