<template>
  <div class="container mx-auto px-4 py-6 max-w-4xl">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">공지사항</h1>
      <p class="mt-2 text-gray-600">YSC 물류 시스템의 중요한 소식과 업데이트를 확인하세요.</p>
    </div>

    <div v-if="loading" class="bg-white rounded-lg shadow p-8 text-center">
      <span class="text-gray-500">공지사항을 불러오는 중...</span>
    </div>

    <div v-else class="bg-white rounded-lg shadow">
      <!-- 공지사항 목록 -->
      <div class="divide-y divide-gray-200">
        <div v-for="notice in notices" :key="notice.id" class="p-4 hover:bg-gray-50 cursor-pointer" @click="viewNotice(notice.id)">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-2">
                <span v-if="notice.isPinned" class="px-2 py-1 bg-yellow-100 text-yellow-700 rounded text-xs">고정</span>
                <h3 class="text-lg font-medium text-gray-900">{{ notice.title }}</h3>
              </div>
              <p class="mt-1 text-sm text-gray-600">{{ notice.content.substring(0, 100) }}...</p>
              <div class="mt-2 flex items-center gap-4 text-sm text-gray-500">
                <span>{{ formatDate(notice.createdAt) }}</span>
                <span>조회수: {{ notice.viewCount }}</span>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { showToast } = useToast()
const notices = ref<any[]>([])
const loading = ref(true)
const currentPage = ref(0)
const totalPages = ref(0)

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

const fetchNotices = async (page = 0) => {
  try {
    loading.value = true
    const response = await axios.get('/api/notices', {
      params: { page, size: 10 }
    })
    if (response.data.success) {
      notices.value = response.data.data
      totalPages.value = response.data.totalPages
      currentPage.value = response.data.currentPage
    } else {
      showToast('공지사항을 불러오는데 실패했습니다.', 'error')
    }
  } catch (error) {
    console.error('공지사항 로딩 오류:', error)
    showToast('공지사항을 불러오는데 실패했습니다.', 'error')
  } finally {
    loading.value = false
  }
}

const viewNotice = (id: number) => {
  router.push(`/notices/${id}`)
}

onMounted(() => {
  fetchNotices()
})
</script>

<style scoped>
</style>