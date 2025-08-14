<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Mobile Header -->
    <header class="lg:hidden fixed top-0 left-0 right-0 z-40 bg-white border-b border-gray-100">
      <div class="flex items-center justify-between px-4 h-14">
        <button 
          @click="sidebarOpen = !sidebarOpen" 
          class="p-2 rounded-lg text-gray-600 hover:bg-gray-100 transition-colors"
        >
          <span class="mdi mdi-menu text-xl" />
        </button>
        
        <div class="flex items-center gap-2">
          <span class="mdi mdi-package-variant text-blue-600 text-xl" />
          <span class="font-semibold text-gray-900">YCS LMS</span>
        </div>
        
        <button 
          @click="showMobileProfile = !showMobileProfile"
          class="w-9 h-9 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 text-white flex items-center justify-center text-sm font-medium shadow-sm"
        >
          {{ userInitials }}
        </button>
      </div>
    </header>

    <!-- Sidebar -->
    <aside 
      :class="[
        'fixed inset-y-0 left-0 z-50 w-64 bg-white border-r border-gray-100 transform transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:inset-0',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full'
      ]"
    >
      <!-- Logo -->
      <div class="flex items-center gap-3 h-16 px-6 border-b border-gray-100">
        <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center shadow-sm">
          <span class="mdi mdi-package-variant text-white text-xl" />
        </div>
        <div>
          <h1 class="font-bold text-gray-900">YCS LMS</h1>
          <p class="text-xs text-gray-500">물류관리시스템</p>
        </div>
      </div>
      
      <!-- Navigation -->
      <nav class="flex-1 px-4 py-4 overflow-y-auto">
        <div v-for="section in navigationSections" :key="section.title" class="mb-6">
          <h3 v-if="section.title" class="px-3 mb-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
            {{ section.title }}
          </h3>
          <ul class="space-y-1">
            <li v-for="item in section.items" :key="item.name">
              <router-link
                :to="{ name: item.name }"
                :class="[
                  'flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-lg transition-all duration-200',
                  isActiveRoute(item.name)
                    ? 'bg-blue-50 text-blue-700 shadow-sm'
                    : 'text-gray-700 hover:bg-gray-50'
                ]"
              >
                <span 
                  :class="[
                    'mdi', item.icon, 'text-lg',
                    isActiveRoute(item.name) ? 'text-blue-600' : 'text-gray-400'
                  ]"
                />
                <span class="flex-1">{{ $t(item.title) }}</span>
                <span 
                  v-if="item.badge"
                  class="px-2 py-0.5 text-xs font-medium bg-red-100 text-red-600 rounded-full"
                >
                  {{ item.badge }}
                </span>
              </router-link>
            </li>
          </ul>
        </div>
      </nav>

      <!-- User Profile Section -->
      <div class="p-4 border-t border-gray-100">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 text-white flex items-center justify-center text-sm font-medium shadow-sm">
            {{ userInitials }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-semibold text-gray-900 truncate">{{ authStore.user?.name }}</p>
            <p class="text-xs text-gray-500 truncate">{{ getRoleLabel(authStore.userType) }}</p>
          </div>
          <button 
            @click="logout" 
            class="p-2 rounded-lg text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
            title="로그아웃"
          >
            <span class="mdi mdi-logout text-lg" />
          </button>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="lg:ml-64 min-h-screen pt-14 lg:pt-0">
      <!-- Desktop Header -->
      <header class="hidden lg:block bg-white border-b border-gray-100">
        <div class="flex items-center justify-between px-6 h-16">
          <!-- Breadcrumb & Title -->
          <div>
            <nav class="flex items-center gap-2 text-sm text-gray-500 mb-1">
              <router-link to="/app/dashboard" class="hover:text-gray-700">홈</router-link>
              <span class="mdi mdi-chevron-right text-xs" />
              <span class="text-gray-700">{{ $t($route.meta.title || 'navigation.dashboard') }}</span>
            </nav>
            <h2 class="text-lg font-semibold text-gray-900">
              {{ $t($route.meta.title || 'navigation.dashboard') }}
            </h2>
          </div>
          
          <div class="flex items-center gap-2">
            <!-- Search -->
            <div class="relative hidden xl:block">
              <input
                type="text"
                placeholder="검색..."
                class="w-64 pl-10 pr-4 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              <span class="absolute left-3 top-2.5 mdi mdi-magnify text-gray-400" />
            </div>
            
            <!-- Notifications -->
            <button class="relative p-2 rounded-lg text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors">
              <span class="mdi mdi-bell-outline text-xl" />
              <span v-if="notificationCount > 0" class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />
            </button>
            
            <!-- Profile Menu -->
            <div class="relative">
              <button 
                @click="showProfileMenu = !showProfileMenu" 
                class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 text-white flex items-center justify-center text-sm font-medium shadow-sm">
                  {{ userInitials }}
                </div>
                <div class="hidden lg:block text-left">
                  <p class="text-sm font-medium text-gray-900">{{ authStore.user?.name }}</p>
                  <p class="text-xs text-gray-500">{{ getRoleLabel(authStore.userType) }}</p>
                </div>
                <span class="mdi mdi-chevron-down text-gray-400 hidden lg:block" />
              </button>
              
              <!-- Profile Dropdown -->
              <transition
                enter-active-class="transition ease-out duration-100"
                enter-from-class="transform opacity-0 scale-95"
                enter-to-class="transform opacity-100 scale-100"
                leave-active-class="transition ease-in duration-75"
                leave-from-class="transform opacity-100 scale-100"
                leave-to-class="transform opacity-0 scale-95"
              >
                <div 
                  v-if="showProfileMenu" 
                  class="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-lg ring-1 ring-black ring-opacity-5 divide-y divide-gray-100"
                >
                  <div class="px-4 py-3">
                    <p class="text-sm text-gray-900 font-medium">{{ authStore.user?.name }}</p>
                    <p class="text-xs text-gray-500">{{ authStore.user?.email }}</p>
                  </div>
                  <div class="py-1">
                    <router-link 
                      to="/app/profile" 
                      class="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                    >
                      <span class="mdi mdi-account-outline text-gray-400" />
                      프로필 설정
                    </router-link>
                    <router-link 
                      to="/app/profile/security" 
                      class="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                    >
                      <span class="mdi mdi-lock-outline text-gray-400" />
                      보안 설정
                    </router-link>
                  </div>
                  <div class="py-1">
                    <button 
                      @click="logout" 
                      class="flex items-center gap-3 w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                    >
                      <span class="mdi mdi-logout text-gray-400" />
                      로그아웃
                    </button>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <div class="p-4 lg:p-6">
        <transition
          mode="out-in"
          enter-active-class="transition ease-out duration-200"
          enter-from-class="transform opacity-0"
          enter-to-class="transform opacity-100"
          leave-active-class="transition ease-in duration-150"
          leave-from-class="transform opacity-100"
          leave-to-class="transform opacity-0"
        >
          <router-view />
        </transition>
      </div>
    </main>

    <!-- Mobile Sidebar Overlay -->
    <transition
      enter-active-class="transition-opacity ease-linear duration-300"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity ease-linear duration-300"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div 
        v-if="sidebarOpen" 
        @click="sidebarOpen = false"
        class="fixed inset-0 z-40 bg-black bg-opacity-50 lg:hidden"
      />
    </transition>
    
    <!-- Mobile Profile Menu -->
    <transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="transform opacity-0 scale-95"
      enter-to-class="transform opacity-100 scale-100"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="transform opacity-100 scale-100"
      leave-to-class="transform opacity-0 scale-95"
    >
      <div 
        v-if="showMobileProfile" 
        class="lg:hidden fixed top-16 right-4 z-50 w-72 bg-white rounded-lg shadow-lg ring-1 ring-black ring-opacity-5"
      >
        <div class="p-4 border-b border-gray-100">
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 rounded-lg bg-gradient-to-br from-blue-500 to-blue-600 text-white flex items-center justify-center font-medium">
              {{ userInitials }}
            </div>
            <div>
              <p class="font-medium text-gray-900">{{ authStore.user?.name }}</p>
              <p class="text-sm text-gray-500">{{ authStore.user?.email }}</p>
            </div>
          </div>
        </div>
        <div class="py-2">
          <router-link 
            to="/app/profile" 
            class="flex items-center gap-3 px-4 py-3 text-gray-700 hover:bg-gray-50"
            @click="showMobileProfile = false"
          >
            <span class="mdi mdi-account-outline" />
            프로필 설정
          </router-link>
          <button 
            @click="logout" 
            class="flex items-center gap-3 w-full px-4 py-3 text-gray-700 hover:bg-gray-50"
          >
            <span class="mdi mdi-logout" />
            로그아웃
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// State
const sidebarOpen = ref(false)
const showProfileMenu = ref(false)
const showMobileProfile = ref(false)
const notificationCount = ref(3)

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

// Navigation sections with better organization
const navigationSections = computed(() => {
  const userType = authStore.userType || 'general'
  
  const sections = [
    {
      title: '',
      items: [
        { name: 'Dashboard', title: 'navigation.dashboard', icon: 'mdi-view-dashboard' }
      ]
    }
  ]
  
  // Main features
  const mainItems = []
  
  if (['general', 'corporate'].includes(userType)) {
    mainItems.push({ name: 'OrderList', title: 'navigation.orders', icon: 'mdi-package-variant' })
  }
  
  if (['warehouse', 'admin'].includes(userType)) {
    mainItems.push({ name: 'WarehouseDashboard', title: 'navigation.warehouse', icon: 'mdi-warehouse' })
  }
  
  mainItems.push(
    { name: 'TrackingList', title: 'navigation.tracking', icon: 'mdi-truck-delivery' },
    { name: 'EstimateList', title: 'navigation.estimates', icon: 'mdi-calculator' },
    { name: 'PaymentList', title: 'navigation.payments', icon: 'mdi-credit-card' }
  )
  
  if (mainItems.length > 0) {
    sections.push({
      title: '주요 기능',
      items: mainItems
    })
  }
  
  // Special sections
  const specialItems = []
  
  if (userType === 'partner') {
    specialItems.push({ name: 'PartnerDashboard', title: 'navigation.partner', icon: 'mdi-handshake' })
  }
  
  if (userType === 'admin') {
    specialItems.push({ name: 'AdminDashboard', title: 'navigation.admin', icon: 'mdi-shield-account' })
  }
  
  if (specialItems.length > 0) {
    sections.push({
      title: '전용 메뉴',
      items: specialItems
    })
  }
  
  // Settings
  sections.push({
    title: '설정',
    items: [
      { name: 'Profile', title: 'navigation.profile', icon: 'mdi-account-circle-outline' },
      { name: 'Notifications', title: 'navigation.notifications', icon: 'mdi-bell-outline', badge: notificationCount.value > 0 ? notificationCount.value : null }
    ]
  })
  
  return sections
})

// Check if route is active
const isActiveRoute = (routeName: string) => {
  return route.name === routeName || route.matched.some(r => r.name === routeName)
}

// Get role label
const getRoleLabel = (userType: string | undefined) => {
  const labels: Record<string, string> = {
    general: '일반회원',
    corporate: '기업회원',
    partner: '파트너',
    warehouse: '창고관리자',
    admin: '시스템관리자'
  }
  return labels[userType || 'general'] || '사용자'
}

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
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.relative')) {
    showProfileMenu.value = false
    showMobileProfile.value = false
  }
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