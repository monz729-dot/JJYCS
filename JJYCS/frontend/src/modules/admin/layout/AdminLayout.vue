<script setup lang="ts">
import AdminHeader from './AdminHeader.vue'
import AdminSidebar from '../components/AdminSidebar.vue'
</script>

<template>
  <!-- ★ 관리자 전용 레이아웃: 전역 헤더/사이드바를 아예 사용하지 않음 -->
  <div class="flex min-h-screen bg-gray-50">
    <AdminSidebar class="shrink-0" />
    <main class="flex-1">
      <!-- 관리자 헤더(우측 햄버거 제거된 헤더) -->
      <AdminHeader />
      <div class="p-4 md:p-6">
        <router-view />
      </div>
    </main>
  </div>
</template>