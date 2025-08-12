<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Sidebar -->
    <div class="fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-lg transform transition-transform duration-300 ease-in-out md:translate-x-0" 
         :class="{ '-translate-x-full': !sidebarOpen }">
      <div class="flex h-16 items-center justify-between px-6 border-b">
        <h1 class="text-xl font-bold text-gray-900">YCS 관리자</h1>
        <button @click="toggleSidebar" class="md:hidden">
          <XMarkIcon class="h-6 w-6 text-gray-500" />
        </button>
      </div>
      
      <nav class="flex-1 px-4 py-6 space-y-2">
        <router-link
          v-for="item in navigationItems"
          :key="item.name"
          :to="item.route"
          class="group flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors"
          :class="[
            $route.path === item.route
              ? 'bg-blue-50 text-blue-700 border-r-2 border-blue-700'
              : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
          ]"
          @click="closeMobileSidebar"
        >
          <component :is="item.icon" class="mr-3 h-5 w-5 flex-shrink-0" />
          {{ item.name }}
        </router-link>
      </nav>
      
      <!-- User info -->
      <div class="p-4 border-t">
        <div class="flex items-center space-x-3">
          <div class="h-8 w-8 rounded-full bg-blue-500 flex items-center justify-center">
            <UserIcon class="h-5 w-5 text-white" />
          </div>
          <div>
            <p class="text-sm font-medium text-gray-900">관리자</p>
            <p class="text-xs text-gray-500">admin@ycs.com</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Mobile backdrop -->
    <div v-if="sidebarOpen" class="fixed inset-0 z-40 md:hidden" @click="toggleSidebar">
      <div class="absolute inset-0 bg-gray-600 opacity-75"></div>
    </div>

    <!-- Main content -->
    <div class="md:pl-64">
      <!-- Top bar -->
      <header class="bg-white shadow-sm border-b h-16">
        <div class="flex items-center justify-between px-4 h-full">
          <button @click="toggleSidebar" class="md:hidden">
            <Bars3Icon class="h-6 w-6 text-gray-500" />
          </button>
          
          <div class="flex items-center space-x-4">
            <!-- Notifications -->
            <button class="relative p-2 text-gray-500 hover:text-gray-700">
              <BellIcon class="h-6 w-6" />
              <span class="absolute top-1 right-1 h-2 w-2 bg-red-500 rounded-full"></span>
            </button>
            
            <!-- Settings -->
            <button class="p-2 text-gray-500 hover:text-gray-700">
              <Cog6ToothIcon class="h-6 w-6" />
            </button>
          </div>
        </div>
      </header>

      <!-- Page content -->
      <main class="p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
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
  UserIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()
const sidebarOpen = ref(false)

const navigationItems = [
  {
    name: '대시보드',
    route: '/admin/dashboard',
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
    icon: CheckCircleIcon
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

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const closeMobileSidebar = () => {
  if (window.innerWidth < 768) {
    sidebarOpen.value = false
  }
}
</script>