<template>
  <div class="container mx-auto px-4 py-6 max-w-4xl">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">공지사항</h1>
      <p class="mt-2 text-gray-600">YSC 물류 시스템의 중요한 소식과 업데이트를 확인하세요.</p>
    </div>

    <div class="bg-white rounded-lg shadow">
      <!-- 공지사항 목록 -->
      <div class="divide-y divide-gray-200">
        <div v-for="notice in notices" :key="notice.id" class="p-4 hover:bg-gray-50 cursor-pointer">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <h3 class="text-lg font-medium text-gray-900">{{ notice.title }}</h3>
              <p class="mt-1 text-sm text-gray-600">{{ notice.preview }}</p>
              <div class="mt-2 flex items-center gap-4 text-sm text-gray-500">
                <span>{{ notice.date }}</span>
                <span v-if="notice.isImportant" class="px-2 py-1 bg-red-100 text-red-700 rounded text-xs">중요</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 빈 상태 -->
      <div v-if="notices.length === 0" class="p-8 text-center">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <p class="mt-2 text-gray-500">등록된 공지사항이 없습니다.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 샘플 데이터
const notices = ref([
  {
    id: 1,
    title: '2025년 설 연휴 배송 안내',
    preview: '설 연휴 기간 동안 배송 일정이 변경됩니다. 자세한 내용을 확인해주세요.',
    date: '2025-01-15',
    isImportant: true
  },
  {
    id: 2,
    title: '시스템 정기 점검 안내',
    preview: '시스템 안정성 향상을 위한 정기 점검이 예정되어 있습니다.',
    date: '2025-01-10',
    isImportant: false
  },
  {
    id: 3,
    title: '신규 서비스 오픈 안내',
    preview: '더욱 빠르고 편리한 배송 서비스를 제공합니다.',
    date: '2025-01-05',
    isImportant: false
  }
])
</script>

<style scoped>
</style>