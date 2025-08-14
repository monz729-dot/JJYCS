<template>
  <div class="main-layout min-h-screen bg-gray-50">
    <!-- Mobile Header -->
    <header class="lg:hidden bg-white shadow-sm border-b border-gray-200">
      <div class="flex items-center justify-between px-4 py-3">
        <button @click="sidebarOpen = !sidebarOpen" class="text-gray-600 hover:text-gray-900">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
          </svg>
        </button>
        <h1 class="text-lg font-semibold text-gray-900">YCS LMS</h1>
        <button @click="showProfileMenu = !showProfileMenu" class="w-8 h-8 rounded-full bg-blue-500 text-white flex items-center justify-center text-sm font-medium">
          {{ userInitials }}
        </button>
      </div>
    </header>

    <!-- Sidebar -->
    <aside :class="[
      'fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-lg transform transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:inset-0',
      sidebarOpen ? 'translate-x-0' : '-translate-x-full'
    ]">
      <div class="flex items-center justify-center h-16 px-4 bg-blue-600">
        <h1 class="text-xl font-bold text-white">YCS LMS</h1>
      </div>
      
      <nav class="mt-4 px-4">
        <ul class="space-y-2">
          <li v-for="item in navigationItems" :key="item.name">
            <router-link
              :to="{ name: item.name }"
              :class="[
                'flex items-center px-4 py-2 text-sm font-medium rounded-lg transition-colors',
                $route.name === item.name
                  ? 'bg-blue-100 text-blue-700 border-r-2 border-blue-700'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
              ]"
            >
              <i :class="['mr-3', item.icon]"></i>
              {{ $t(item.title) }}
            </router-link>
          </li>
        </ul>
      </nav>

      <!-- User Profile Section -->
      <div class="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-200">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 rounded-full bg-blue-500 text-white flex items-center justify-center text-sm font-medium">
            {{ userInitials }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-gray-900 truncate">{{ authStore.user?.name }}</p>
            <p class="text-xs text-gray-500 truncate">{{ authStore.user?.email }}</p>
          </div>
          <button @click="logout" class="text-gray-400 hover:text-gray-600">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
            </svg>
          </button>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="lg:ml-64 min-h-screen">
      <!-- Desktop Header -->
      <header class="hidden lg:block bg-white shadow-sm border-b border-gray-200">
        <div class="flex items-center justify-between px-6 py-4">
          <div>
            <h2 v-if="$route.meta.title" class="text-lg font-semibold text-gray-900">
              {{ $t($route.meta.title) }}
            </h2>
          </div>
          <div class="flex items-center space-x-4">
            <!-- Notifications -->
            <button class="text-gray-400 hover:text-gray-600 relative">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-5-5h5m-5 0V3"></path>
              </svg>
              <span v-if="notificationCount > 0" class="absolute -top-1 -right-1 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                {{ notificationCount }}
              </span>
            </button>
            
            <!-- Profile Menu -->
            <div class="relative">
              <button @click="showProfileMenu = !showProfileMenu" class="flex items-center space-x-2 text-gray-700 hover:text-gray-900">
                <div class="w-8 h-8 rounded-full bg-blue-500 text-white flex items-center justify-center text-sm font-medium">
                  {{ userInitials }}
                </div>
                <span class="text-sm font-medium">{{ authStore.user?.name }}</span>
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                </svg>
              </button>
              
              <!-- Profile Dropdown -->
              <div v-if="showProfileMenu" class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg py-1 z-50">
                <router-link to="/profile" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                  {{ $t('navigation.profile') }}
                </router-link>
                <router-link to="/profile/security" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                  {{ $t('profile.security') }}
                </router-link>
                <div class="border-t border-gray-100"></div>
                <button @click="logout" class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                  {{ $t('auth.logout') }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <div class="p-4 lg:p-6">
        <router-view />
      </div>
    </main>

    <!-- Mobile Sidebar Overlay -->
    <div 
      v-if="sidebarOpen" 
      @click="sidebarOpen = false"
      class="fixed inset-0 z-40 bg-black bg-opacity-50 lg:hidden"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getNavigationItems } from '@/router'

const router = useRouter()
const authStore = useAuthStore()

// State
const sidebarOpen = ref(false)
const showProfileMenu = ref(false)
const notificationCount = ref(3) // Mock notification count

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

const navigationItems = computed(() => {
  return getNavigationItems(authStore.userRole || 'individual')
})

// Methods
const logout = async () => {
  try {
    await authStore.signOut()
    router.push({ name: 'Login' })
  } catch (error) {
    console.error('Logout error:', error)
  }
}

// Close dropdowns when clicking outside
const handleClickOutside = (event: Event) => {
  showProfileMenu.value = false
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.router-link-active {
  @apply bg-blue-100 text-blue-700;
}
</style>