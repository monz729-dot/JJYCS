<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { adminApi } from '@/utils/api'

const router = useRouter()
const auth = useAuthStore()
const { showToast } = useToast()

const pendingApprovalsCount = ref(0)

const onLogout = async () => {
  try {
    await auth.logout()
    showToast('로그아웃되었습니다.', 'success')
    router.replace('/login')
  } catch (error) {
    showToast('로그아웃 중 오류가 발생했습니다.', 'error')
  }
}

const loadPendingApprovals = async () => {
  try {
    const response = await adminApi.getPendingApprovals()
    if (response.success && response.data && Array.isArray(response.data)) {
      pendingApprovalsCount.value = response.data.length
    }
  } catch (error) {
    console.error('승인 대기 건수 조회 실패:', error)
  }
}

onMounted(() => {
  loadPendingApprovals()
})
</script>

<template>
  <aside class="w-64 bg-white border-r min-h-screen flex flex-col">
    <div class="p-4 text-sm font-semibold text-gray-700">관리자 메뉴</div>
    <nav class="px-2 space-y-1">
      <RouterLink class="nav-item" to="/admin">
        <div class="flex items-center gap-2">
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path d="M10.707 2.293a1 1 0 00-1.414 0l-9 9a1 1 0 001.414 1.414L9 5.414V17a1 1 0 102 0V5.414l7.293 7.293a1 1 0 001.414-1.414l-9-9z"/>
          </svg>
          대시보드
        </div>
      </RouterLink>
      <RouterLink class="nav-item" to="/admin/users">
        <div class="flex items-center gap-2">
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"/>
          </svg>
          사용자 관리
        </div>
      </RouterLink>
      <RouterLink class="nav-item" to="/admin/approvals">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            승인 관리
          </div>
          <span v-if="pendingApprovalsCount > 0" class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
            {{ pendingApprovalsCount }}
          </span>
        </div>
      </RouterLink>
      <RouterLink class="nav-item" to="/admin/orders">
        <div class="flex items-center gap-2">
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z"/>
          </svg>
          주문 관리
        </div>
      </RouterLink>
      <RouterLink class="nav-item" to="/admin/inbound">
        <div class="flex items-center gap-2">
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path d="M5 3a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2V5a2 2 0 00-2-2H5zM5 11a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2v-2a2 2 0 00-2-2H5zM11 5a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V5zM11 13a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"/>
          </svg>
          입고확인
        </div>
      </RouterLink>
    </nav>

    <div class="mt-auto p-3 border-t">
      <button class="w-full flex items-center justify-center gap-2 px-3 py-2 rounded-lg border border-red-200 text-red-600 hover:bg-red-50 hover:border-red-300 transition-colors" @click="onLogout">
        <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clip-rule="evenodd"/>
        </svg>
        로그아웃
      </button>
    </div>
  </aside>
</template>

<style scoped>
.nav-item { @apply block px-3 py-2 rounded text-gray-700 hover:bg-gray-100; }
.nav-item.router-link-active { @apply bg-blue-100 text-blue-700; }
</style>