<template>
  <div class="approval-view max-w-md mx-auto">
    <!-- Header -->
    <div class="text-center mb-8">
      <div class="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <svg class="w-8 h-8 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
      </div>
      <h1 class="text-2xl font-bold text-gray-900 mb-2">
        {{ $t('dashboard.account_pending_title') }}
      </h1>
      <p class="text-gray-600">
        {{ $t('dashboard.account_pending_message') }}
      </p>
    </div>

    <!-- Account Information -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">계정 정보</h2>
      <div class="space-y-3">
        <div class="flex justify-between">
          <span class="text-gray-600">이름</span>
          <span class="font-medium">{{ authStore.user?.name }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-600">이메일</span>
          <span class="font-medium">{{ authStore.user?.email }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-600">계정 유형</span>
          <span class="font-medium capitalize">{{ authStore.user?.role }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-600">상태</span>
          <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
            승인 대기
          </span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-600">신청일</span>
          <span class="font-medium">{{ formatDate(authStore.user?.createdAt) }}</span>
        </div>
      </div>
    </div>

    <!-- Timeline -->
    <div class="bg-white rounded-lg shadow p-6 mb-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">승인 진행 상황</h2>
      <div class="space-y-4">
        <div class="flex items-center">
          <div class="flex-shrink-0 w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
            <svg class="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path>
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-900">계정 신청 완료</p>
            <p class="text-sm text-gray-500">{{ formatDate(authStore.user?.createdAt) }}</p>
          </div>
        </div>
        
        <div class="flex items-center">
          <div class="flex-shrink-0 w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
            <svg class="w-5 h-5 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-900">서류 검토 중</p>
            <p class="text-sm text-gray-500">평일 기준 1-2일 소요</p>
          </div>
        </div>
        
        <div class="flex items-center">
          <div class="flex-shrink-0 w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center">
            <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-400">승인 완료</p>
            <p class="text-sm text-gray-400">이메일로 승인 결과 안내</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Contact Information -->
    <div class="bg-blue-50 rounded-lg p-4 mb-6">
      <div class="flex">
        <div class="flex-shrink-0">
          <svg class="w-5 h-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path>
          </svg>
        </div>
        <div class="ml-3">
          <h3 class="text-sm font-medium text-blue-800">문의사항이 있으신가요?</h3>
          <div class="mt-2 text-sm text-blue-700">
            <p>이메일: support@ycs.com</p>
            <p>전화: 1588-1234</p>
            <p>운영시간: 평일 09:00-18:00</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex space-x-3">
      <router-link 
        to="/profile" 
        class="flex-1 btn btn-secondary text-center"
      >
        프로필 수정
      </router-link>
      <button 
        @click="logout" 
        class="flex-1 btn btn-outline text-center"
        :disabled="loading"
      >
        <span v-if="loading" class="loading-spinner mr-2"></span>
        로그아웃
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
// Simple date formatter without external dependencies

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const formatDate = (dateString?: string) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR') + ' ' + date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
}

const logout = async () => {
  loading.value = true
  try {
    await authStore.logout()
    router.push({ name: 'Login' })
  } catch (error) {
    console.error('Logout error:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.btn-outline {
  @apply border border-gray-300 text-gray-700 hover:bg-gray-50;
}
</style>