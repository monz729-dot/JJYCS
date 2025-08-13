<template>
  <div class="min-h-screen bg-gray-100">
    <!-- Sidebar - PC optimized width -->
    <div class="fixed inset-y-0 left-0 z-50 w-56 lg:w-64 bg-slate-900 shadow-xl transform transition-transform duration-300 ease-in-out md:translate-x-0" 
         :class="{ '-translate-x-full': !sidebarOpen }">
      <!-- Logo Header - Compact height -->
      <div class="flex h-12 lg:h-14 items-center justify-between px-4 lg:px-6 bg-slate-800">
        <div class="flex items-center space-x-2">
          <div class="w-8 h-8 bg-blue-500 rounded-lg flex items-center justify-center">
            <span class="text-white font-bold text-sm">YCS</span>
          </div>
          <h1 class="text-base lg:text-lg font-bold text-white">관리자 시스템</h1>
        </div>
        <button @click="toggleSidebar" class="md:hidden">
          <XMarkIcon class="h-5 w-5 text-gray-400" />
        </button>
      </div>
      
      <!-- Navigation - Optimized spacing -->
      <nav class="flex-1 px-3 py-3 space-y-1 overflow-y-auto">
        <router-link
          v-for="item in navigationItems"
          :key="item.name"
          :to="item.route"
          class="group flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-all duration-200"
          :class="[
            $route.path === item.route
              ? 'bg-blue-600 text-white shadow-md'
              : 'text-gray-300 hover:bg-slate-800 hover:text-white'
          ]"
          @click="closeMobileSidebar"
        >
          <component :is="item.icon" class="mr-3 h-5 w-5 flex-shrink-0" 
                     :class="[$route.path === item.route ? 'text-white' : 'text-gray-400']" />
          <span>{{ item.name }}</span>
          <span v-if="item.badge" class="ml-auto px-2 py-0.5 text-xs rounded-full"
                :class="item.badgeClass || 'bg-red-500 text-white'">
            {{ item.badge }}
          </span>
        </router-link>
      </nav>
      
      <!-- User info - Compact -->
      <div class="p-3 border-t border-slate-700">
        <div class="flex items-center space-x-3 px-2">
          <div class="h-9 w-9 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center">
            <UserIcon class="h-5 w-5 text-white" />
          </div>
          <div class="flex-1">
            <p class="text-sm font-medium text-white">시스템 관리자</p>
            <p class="text-xs text-gray-400">admin@ycs.com</p>
          </div>
          <button @click="handleLogout" class="text-gray-400 hover:text-white transition-colors">
            <ArrowRightOnRectangleIcon class="h-5 w-5" />
          </button>
        </div>
      </div>
    </div>

    <!-- Mobile backdrop -->
    <div v-if="sidebarOpen" class="fixed inset-0 z-40 md:hidden bg-black bg-opacity-50" @click="toggleSidebar"></div>

    <!-- Main content area -->
    <div class="md:pl-56 lg:pl-64 flex flex-col min-h-screen">
      <!-- Top bar - Compact and functional -->
      <header class="bg-white shadow-sm border-b h-12 lg:h-14 sticky top-0 z-30">
        <div class="flex items-center justify-between px-4 lg:px-6 h-full">
          <!-- Left side -->
          <div class="flex items-center space-x-4">
            <button @click="toggleSidebar" class="md:hidden">
              <Bars3Icon class="h-6 w-6 text-gray-500" />
            </button>
            
            <!-- Breadcrumb for PC -->
            <div class="hidden md:flex items-center space-x-2 text-sm text-gray-600">
              <span>관리자</span>
              <ChevronRightIcon class="h-4 w-4" />
              <span class="font-medium text-gray-900">{{ currentPageName }}</span>
            </div>
          </div>
          
          <!-- Right side - Quick actions -->
          <div class="flex items-center space-x-2 lg:space-x-3">
            <!-- Search - PC only -->
            <div class="hidden lg:flex items-center bg-gray-100 rounded-lg px-3 py-1.5">
              <MagnifyingGlassIcon class="h-4 w-4 text-gray-400 mr-2" />
              <input type="text" placeholder="검색..." 
                     class="bg-transparent text-sm focus:outline-none w-48" />
            </div>
            
            <!-- Notifications -->
            <button class="relative p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors">
              <BellIcon class="h-5 w-5" />
              <span class="absolute top-1.5 right-1.5 h-2 w-2 bg-red-500 rounded-full animate-pulse"></span>
            </button>
            
            <!-- Quick Settings -->
            <button class="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors">
              <Cog6ToothIcon class="h-5 w-5" />
            </button>
            
            <!-- Full Screen Toggle - PC only -->
            <button @click="toggleFullScreen" class="hidden lg:block p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors">
              <ArrowsPointingOutIcon v-if="!isFullScreen" class="h-5 w-5" />
              <ArrowsPointingInIcon v-else class="h-5 w-5" />
            </button>
          </div>
        </div>
      </header>

      <!-- Page content - Optimized padding for PC -->
      <main class="flex-1 p-4 lg:p-6 xl:p-8">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Bars3Icon,
  XMarkIcon,
  HomeIcon,
  UsersIcon,
  CheckCircleIcon,
  ShoppingBagIcon,
  BuildingStorefrontIcon,
  Cog6ToothIcon,
  ChartBarIcon,
  DocumentChartBarIcon,
  WrenchScrewdriverIcon,
  BellIcon,
  UserIcon,
  ArrowRightOnRectangleIcon,
  ChevronRightIcon,
  MagnifyingGlassIcon,
  ArrowsPointingOutIcon,
  ArrowsPointingInIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()
const route = useRoute()
const sidebarOpen = ref(false)
const isFullScreen = ref(false)

const navigationItems = [
  {
    name: '대시보드',
    route: '/admin',
    icon: HomeIcon
  },
  {
    name: '사용자 관리',
    route: '/admin/users',
    icon: UsersIcon
  },
  {
    name: '승인 관리',
    route: '/admin/approvals',
    icon: CheckCircleIcon,
    badge: '2',
    badgeClass: 'bg-orange-500 text-white'
  },
  {
    name: '주문 관리',
    route: '/admin/orders',
    icon: ShoppingBagIcon
  },
  {
    name: '창고 관리',
    route: '/admin/warehouse',
    icon: BuildingStorefrontIcon
  },
  {
    name: '리포트',
    route: '/admin/reports',
    icon: ChartBarIcon
  },
  {
    name: '시스템 설정',
    route: '/admin/settings',
    icon: Cog6ToothIcon
  },
  {
    name: '환경 설정',
    route: '/admin/config',
    icon: WrenchScrewdriverIcon
  }
]

// Computed
const currentPageName = computed(() => {
  const item = navigationItems.find(item => item.route === route.path)
  return item ? item.name : ''
})

// Methods
const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const closeMobileSidebar = () => {
  if (window.innerWidth < 768) {
    sidebarOpen.value = false
  }
}

const handleLogout = () => {
  // 로그아웃 처리
  router.push('/auth/login')
}

const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullScreen.value = true
  } else {
    document.exitFullscreen()
    isFullScreen.value = false
  }
}
</script>