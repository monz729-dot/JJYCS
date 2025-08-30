<script setup lang="ts">
import { useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const auth = useAuthStore()
const { showToast } = useToast()

const onLogout = async () => {
  try {
    await auth.logout()
    showToast('로그아웃되었습니다.', 'success')
    router.replace('/login')
  } catch (error) {
    showToast('로그아웃 중 오류가 발생했습니다.', 'error')
  }
}
</script>

<template>
  <aside class="w-64 bg-white border-r min-h-screen flex flex-col">
    <div class="p-4 text-sm font-semibold text-gray-700">관리자 메뉴</div>
    <nav class="px-2 space-y-1">
      <RouterLink class="nav-item" to="/admin">대시보드</RouterLink>
      <RouterLink class="nav-item" to="/admin/users">사용자 관리</RouterLink>
      <RouterLink class="nav-item" to="/admin/orders">주문 관리</RouterLink>
      <!-- ★ '승인 대기/시뮬레이터/전체페이지/설정' 등은 노출하지 않음 -->
    </nav>

    <div class="mt-auto p-3 border-t">
      <button class="w-full btn-outline" @click="onLogout">로그아웃</button>
    </div>
  </aside>
</template>

<style scoped>
.nav-item { @apply block px-3 py-2 rounded text-gray-700 hover:bg-gray-100; }
.nav-item.router-link-active { @apply bg-blue-100 text-blue-700; }
.btn-outline { @apply px-3 py-2 rounded border hover:bg-gray-50; }
</style>