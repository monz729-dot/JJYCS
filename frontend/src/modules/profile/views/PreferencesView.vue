<template>
  <div class="preferences-view">
    <div class="container mx-auto p-6">
      <div class="max-w-2xl mx-auto">
        <!-- Header -->
        <div class="bg-white rounded-lg shadow p-6 mb-6">
          <h1 class="text-2xl font-bold text-gray-900 mb-2">환경 설정</h1>
          <p class="text-gray-600">언어, 시간대 및 기타 환경설정을 변경하세요.</p>
        </div>

        <!-- Language Settings -->
        <div class="bg-white rounded-lg shadow p-6 mb-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">언어 설정</h2>
          
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">표시 언어</label>
              <select
                v-model="preferences.language"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="ko">한국어</option>
                <option value="en">English</option>
                <option value="th">ไทย</option>
              </select>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">시간대</label>
              <select
                v-model="preferences.timezone"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="Asia/Seoul">서울 (UTC+9)</option>
                <option value="Asia/Bangkok">방콕 (UTC+7)</option>
                <option value="UTC">UTC (UTC+0)</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Display Settings -->
        <div class="bg-white rounded-lg shadow p-6 mb-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">화면 설정</h2>
          
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-medium text-gray-900">다크 모드</h3>
                <p class="text-sm text-gray-600">어두운 테마 사용</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  v-model="preferences.darkMode"
                  type="checkbox"
                  class="sr-only peer"
                />
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
              </label>
            </div>
            
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-medium text-gray-900">컴팩트 모드</h3>
                <p class="text-sm text-gray-600">좁은 간격으로 더 많은 정보 표시</p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  v-model="preferences.compactMode"
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
            @click="savePreferences"
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
const preferences = reactive({
  language: 'ko',
  timezone: 'Asia/Seoul',
  darkMode: false,
  compactMode: false
})

// Methods
const savePreferences = async () => {
  try {
    // TODO: Implement actual preferences save
    toast.success('환경 설정이 저장되었습니다.')
  } catch (error) {
    toast.error('설정 저장 중 오류가 발생했습니다.')
  }
}
</script>

<style scoped>
.preferences-view {
  min-height: 100vh;
  background-color: #f8fafc;
}
</style>