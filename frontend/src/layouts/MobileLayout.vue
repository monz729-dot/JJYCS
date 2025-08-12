<template>
  <div class="mobile-layout h-screen bg-gray-50 flex flex-col">
    <!-- Mobile Header -->
    <header class="bg-white shadow-sm border-b border-gray-200 flex-shrink-0">
      <div class="flex items-center justify-between px-4 py-3">
        <div class="flex items-center space-x-3">
          <button @click="goBack" v-if="showBackButton" class="text-gray-600 hover:text-gray-900">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
          </button>
          <h1 class="text-lg font-semibold text-gray-900">
            {{ currentPageTitle }}
          </h1>
        </div>
        
        <div class="flex items-center space-x-2">
          <!-- Notifications -->
          <button class="text-gray-600 hover:text-gray-900 relative">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-5-5h5m-5 0V3"></path>
            </svg>
            <span v-if="notificationCount > 0" class="absolute -top-1 -right-1 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
              {{ notificationCount }}
            </span>
          </button>
          
          <!-- Profile -->
          <button @click="showProfileMenu = !showProfileMenu" class="w-8 h-8 rounded-full bg-blue-500 text-white flex items-center justify-center text-sm font-medium">
            {{ userInitials }}
          </button>
        </div>
      </div>
      
      <!-- Profile Dropdown -->
      <div v-if="showProfileMenu" class="absolute right-4 top-14 w-48 bg-white rounded-lg shadow-lg py-1 z-50">
        <router-link to="/profile" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
          {{ $t('navigation.profile') }}
        </router-link>
        <div class="border-t border-gray-100"></div>
        <button @click="logout" class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
          {{ $t('auth.logout') }}
        </button>
      </div>
    </header>

    <!-- Main Content -->
    <main class="flex-1 overflow-y-auto">
      <div class="p-4">
        <router-view />
      </div>
    </main>

    <!-- Bottom Navigation -->
    <nav class="bg-white border-t border-gray-200 flex-shrink-0">
      <div class="flex">
        <router-link
          v-for="item in bottomNavItems"
          :key="item.name"
          :to="{ name: item.name }"
          :class="[
            'flex-1 flex flex-col items-center py-2 px-1 text-xs font-medium transition-colors',
            $route.name === item.name
              ? 'text-blue-600 bg-blue-50'
              : 'text-gray-600 hover:text-gray-900'
          ]"
        >
          <i :class="['mb-1', item.icon]"></i>
          <span>{{ $t(item.title) }}</span>
        </router-link>
      </div>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// State
const showProfileMenu = ref(false)
const notificationCount = ref(2)

// Computed
const userInitials = computed(() => {
  if (!authStore.user?.name) return 'U'
  return authStore.user.name
    .split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
})

const currentPageTitle = computed(() => {
  if (route.meta.title) {
    return route.meta.title.toString()
  }
  return 'YCS LMS'
})

const showBackButton = computed(() => {
  return route.name !== 'MobileDashboard'
})

const bottomNavItems = computed(() => [
  { name: 'MobileDashboard', title: 'dashboard.title', icon: 'pi pi-home' },
  { name: 'MobileOrders', title: 'orders.title', icon: 'pi pi-list' },
  { name: 'MobileScan', title: 'warehouse.scan.title', icon: 'pi pi-qrcode' },
])

// Methods
const goBack = () => {
  router.go(-1)
}

const logout = async () => {
  try {
    await authStore.logout()
    router.push({ name: 'Login' })
  } catch (error) {
    console.error('Logout error:', error)
  }
}
</script>

<style scoped>
.router-link-active {
  @apply text-blue-600 bg-blue-50;
}
</style>