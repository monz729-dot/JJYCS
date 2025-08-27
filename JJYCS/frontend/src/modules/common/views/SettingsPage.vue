<template>
  <div class="min-h-screen bg-gray-50">
    <AppLayout>
      <div class="max-w-4xl mx-auto py-6">
        <div class="bg-white shadow rounded-lg p-6">
          <h1 class="text-2xl font-bold text-gray-900 mb-6">설정</h1>
          
          <div class="space-y-6">
            <div>
              <h2 class="text-lg font-semibold text-gray-900 mb-4">알림 설정</h2>
              <div class="space-y-3">
                <label class="flex items-center">
                  <input type="checkbox" v-model="settings.emailNotifications" class="rounded border-gray-300 text-blue-600 focus:ring-blue-500">
                  <span class="ml-2 text-sm text-gray-700">이메일 알림</span>
                </label>
                <label class="flex items-center">
                  <input type="checkbox" v-model="settings.smsNotifications" class="rounded border-gray-300 text-blue-600 focus:ring-blue-500">
                  <span class="ml-2 text-sm text-gray-700">SMS 알림</span>
                </label>
              </div>
            </div>
            
            <div>
              <h2 class="text-lg font-semibold text-gray-900 mb-4">언어 설정</h2>
              <select v-model="settings.language" class="w-48 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
                <option value="ko">한국어</option>
                <option value="en">English</option>
              </select>
            </div>
            
            <div>
              <h2 class="text-lg font-semibold text-gray-900 mb-4">테마 설정</h2>
              <select v-model="settings.theme" class="w-48 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
                <option value="light">라이트 테마</option>
                <option value="dark">다크 테마</option>
                <option value="auto">시스템 설정</option>
              </select>
            </div>
          </div>
          
          <div class="mt-6">
            <button 
              @click="saveSettings"
              class="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors"
            >
              설정 저장
            </button>
          </div>
        </div>
      </div>
    </AppLayout>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useToast } from '@/composables/useToast'
import AppLayout from '@/components/ui/AppLayout.vue'

const { addToast } = useToast()

const settings = ref({
  emailNotifications: true,
  smsNotifications: false,
  language: 'ko',
  theme: 'light'
})

const saveSettings = () => {
  addToast({
    type: 'success',
    title: '설정 저장',
    message: '설정이 성공적으로 저장되었습니다.'
  })
}
</script>