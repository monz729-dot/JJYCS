<template>
  <div class="notifications-view">
    <div class="container mx-auto p-6">
      <div class="max-w-2xl mx-auto">
        <!-- Header -->
        <div class="bg-white rounded-lg shadow p-6 mb-6">
          <h1 class="text-2xl font-bold text-gray-900 mb-2">알림 설정</h1>
          <p class="text-gray-600">받고 싶은 알림의 종류와 수신 방법을 선택하세요.</p>
        </div>

        <!-- Email Notifications -->
        <div class="bg-white rounded-lg shadow p-6 mb-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">이메일 알림</h2>
          
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-medium text-gray-900">주문 상태 업데이트</h3>
                <p class="text-sm text-gray-600">주문 진행 상황에 대한 알림</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  v-model="notifications.email.orderUpdates"
                  type="checkbox"
                  class="sr-only peer"
                />
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
              </label>
            </div>
            
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-medium text-gray-900">배송 알림</h3>
                <p class="text-sm text-gray-600">배송 시작, 완료 등의 알림</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  v-model="notifications.email.shipping"
                  type="checkbox"
                  class="sr-only peer"
                />
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
              </label>
            </div>
            
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-medium text-gray-900">계정 보안</h3>
                <p class="text-sm text-gray-600">로그인, 비밀번호 변경 등의 알림</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  v-model="notifications.email.security"
                  type="checkbox"
                  class="sr-only peer"
                />
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
              </label>
            </div>
          </div>
        </div>

        <!-- Push Notifications -->
        <div class="bg-white rounded-lg shadow p-6 mb-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">푸시 알림</h2>
          
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-medium text-gray-900">실시간 알림</h3>
                <p class="text-sm text-gray-600">중요한 업데이트를 즉시 받기</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  v-model="notifications.push.enabled"
                  type="checkbox"
                  class="sr-only peer"
                />
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
              </label>
            </div>
          </div>
        </div>

        <!-- Save Button -->
        <div class="flex justify-end">
          <button
            @click="saveNotificationSettings"
            class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            설정 저장
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useToast } from 'vue-toastification'

const toast = useToast()

// State
const notifications = reactive({
  email: {
    orderUpdates: true,
    shipping: true,
    security: true
  },
  push: {
    enabled: false
  }
})

// Methods
const saveNotificationSettings = async () => {
  try {
    // TODO: Implement actual notification settings save
    toast.success('알림 설정이 저장되었습니다.')
  } catch (error) {
    toast.error('설정 저장 중 오류가 발생했습니다.')
  }
}
</script>

<style scoped>
.notifications-view {
  min-height: 100vh;
  background-color: #f8fafc;
}
</style>