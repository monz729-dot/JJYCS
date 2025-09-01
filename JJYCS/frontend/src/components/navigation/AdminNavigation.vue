<template>
  <!-- PC용 사이드바 네비게이션 -->
  <nav v-if="compact" class="sidebar-navigation bg-white border-r border-gray-200 h-full w-64 flex flex-col">
    <div class="sidebar-header p-6 border-b border-gray-100">
      <h2 class="text-xl font-semibold text-gray-800">관리자 메뉴</h2>
      <p class="text-sm text-gray-600 mt-1">시스템 관리</p>
    </div>
    
    <div class="sidebar-menu flex-1 px-3 py-4 space-y-2">
      <button
        v-for="item in navigationItems"
        :key="item.id"
        :class="[
          'sidebar-item w-full flex items-center gap-3 px-3 py-3 rounded-lg text-left transition-all duration-200',
          currentRoute === item.route 
            ? 'text-blue-700 bg-blue-50 border-l-4 border-blue-700 shadow-sm' 
            : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
        ]"
        @click="navigateTo(item.route)"
        :aria-label="item.description"
      >
        <!-- 아이콘 -->
        <div :class="[
          'nav-icon flex-shrink-0 transition-all duration-200',
          currentRoute === item.route ? 'text-blue-700' : 'text-gray-500'
        ]">
          <component :is="item.icon" class="h-5 w-5" />
        </div>
        
        <!-- 라벨과 뱃지 -->
        <div class="flex-1 flex items-center justify-between">
          <span :class="[
            'font-medium transition-all duration-200',
            currentRoute === item.route ? 'text-blue-700' : 'text-gray-700'
          ]">
            {{ item.label }}
          </span>
          
          <!-- 뱃지 (알림 개수) -->
          <span 
            v-if="item.badge && item.badge > 0" 
            class="bg-red-500 text-white text-xs rounded-full px-2 py-1 min-w-[20px] h-5 flex items-center justify-center"
          >
            {{ item.badge > 99 ? '99+' : item.badge }}
          </span>
        </div>
      </button>
    </div>
    
    <!-- 사용자 정보 -->
    <div class="sidebar-footer p-4 border-t border-gray-100">
      <div class="flex items-center gap-3 mb-3">
        <div class="avatar w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
          <span class="text-sm font-medium text-blue-700">
            {{ (authStore.user?.name || 'A').charAt(0).toUpperCase() }}
          </span>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-gray-700 truncate">{{ authStore.user?.name || 'Admin' }}</p>
          <p class="text-xs text-gray-500 truncate">{{ authStore.user?.email }}</p>
        </div>
      </div>
      <button
        @click="handleLogout"
        class="w-full flex items-center gap-2 px-3 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors"
      >
        <LogOutIcon class="h-4 w-4" />
        로그아웃
      </button>
    </div>
  </nav>
  
  <!-- 모바일용 하단 네비게이션 -->
  <nav v-else class="fixed bottom-0 left-0 right-0 bg-white/95 border-t border-blue-100 shadow-lg z-50 backdrop-blur-md">
    <div class="grid grid-cols-5 h-16">
      <button
        v-for="item in navigationItems"
        :key="item.id"
        :class="[
          'nav-item flex flex-col items-center justify-center gap-1 relative transition-all duration-300',
          currentRoute === item.route ? 'text-blue-600 bg-blue-50' : 'text-blue-400 hover:text-blue-600 hover:bg-blue-50'
        ]"
        @click="navigateTo(item.route)"
        :aria-label="item.description"
      >
        <!-- 활성 인디케이터 -->
        <div 
          :class="[
            'absolute top-0 left-1/2 w-8 h-1 bg-blue-600 rounded-b-full transform -translate-x-1/2 transition-all duration-300',
            currentRoute === item.route ? 'opacity-100 scale-100' : 'opacity-0 scale-0'
          ]"
        ></div>
        
        <!-- 아이콘 -->
        <div 
          :class="[
            'nav-icon transition-transform duration-300',
            currentRoute === item.route ? 'scale-110' : ''
          ]"
        >
          <component :is="item.icon" class="h-5 w-5" />
        </div>
        
        <!-- 라벨 -->
        <span 
          :class="[
            'text-xs transition-all duration-300',
            currentRoute === item.route ? 'font-medium' : 'font-normal'
          ]"
        >
          {{ item.label }}
        </span>

        <!-- 뱃지 (알림 개수) -->
        <span 
          v-if="item.badge && item.badge > 0" 
          class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center"
        >
          {{ item.badge > 99 ? '99+' : item.badge }}
        </span>
      </button>
    </div>
  </nav>

  <!-- 로그아웃 버튼 (필요시 표시) -->
  <div v-if="showLogoutButton" class="fixed top-4 right-4 z-50">
    <button
      @click="handleLogout"
      class="p-2 bg-red-500 text-white rounded-full shadow-lg hover:bg-red-600 transition-colors"
      aria-label="로그아웃"
    >
      <LogOutIcon class="h-5 w-5" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// Props
interface Props {
  showLogoutButton?: boolean
  compact?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showLogoutButton: false,
  compact: false
})

// Icons (SVG 컴포넌트로 정의)
const HomeIcon = () => h('svg', { 
  class: 'h-5 w-5', 
  fill: 'currentColor', 
  viewBox: '0 0 20 20' 
}, [
  h('path', {
    d: 'M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z'
  })
])

const PackageIcon = () => h('svg', { 
  class: 'h-5 w-5', 
  fill: 'currentColor', 
  viewBox: '0 0 20 20' 
}, [
  h('path', {
    d: 'M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z'
  })
])

const BarChartIcon = () => h('svg', { 
  class: 'h-5 w-5', 
  fill: 'currentColor', 
  viewBox: '0 0 20 20' 
}, [
  h('path', {
    d: 'M2 11a1 1 0 011-1h2a1 1 0 011 1v5a1 1 0 01-1 1H3a1 1 0 01-1-1v-5zM8 7a1 1 0 011-1h2a1 1 0 011 1v9a1 1 0 01-1 1H9a1 1 0 01-1-1V7zM14 4a1 1 0 011-1h2a1 1 0 011 1v12a1 1 0 01-1 1h-2a1 1 0 01-1-1V4z'
  })
])

const ListIcon = () => h('svg', { 
  class: 'h-5 w-5', 
  fill: 'currentColor', 
  viewBox: '0 0 20 20' 
}, [
  h('path', {
    'fill-rule': 'evenodd',
    d: 'M3 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 8a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 12a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 16a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z',
    'clip-rule': 'evenodd'
  })
])

const SettingsIcon = () => h('svg', { 
  class: 'h-5 w-5', 
  fill: 'currentColor', 
  viewBox: '0 0 20 20' 
}, [
  h('path', {
    'fill-rule': 'evenodd',
    d: 'M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z',
    'clip-rule': 'evenodd'
  })
])

const LogOutIcon = () => h('svg', { 
  class: 'h-5 w-5', 
  fill: 'currentColor', 
  viewBox: '0 0 20 20' 
}, [
  h('path', {
    'fill-rule': 'evenodd',
    d: 'M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z',
    'clip-rule': 'evenodd'
  })
])

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentRoute = computed(() => route.name as string)

// 관리자 네비게이션 아이템들
const navigationItems = computed(() => [
  {
    id: 'admin-dashboard',
    label: '대시보드',
    icon: HomeIcon,
    route: 'AdminDashboard',
    description: '관리자 메인',
    badge: 0
  },
  {
    id: 'admin-orders',
    label: '주문관리',
    icon: PackageIcon,
    route: 'AdminOrders',
    description: '주문 관리',
    badge: getPendingOrdersCount() // 승인 대기중인 주문 개수
  },
  {
    id: 'admin-settings',
    label: '설정',
    icon: SettingsIcon,
    route: 'AdminSettings',
    description: '관리자 설정',
    badge: 0
  }
])

// 승인 대기중인 주문 개수 (실제 데이터로 교체)
function getPendingOrdersCount(): number {
  // TODO: 실제 API에서 데이터 가져오기
  return 5
}

const navigateTo = (routeName: string) => {
  if (route.name !== routeName) {
    router.push({ name: routeName })
  }
}

const handleLogout = () => {
  if (confirm('정말 로그아웃 하시겠습니까?')) {
    authStore.logout()
    router.push({ name: 'Login' })
  }
}

// 키보드 네비게이션 지원
const handleKeyboard = (event: KeyboardEvent) => {
  const items = navigationItems.value
  const currentIndex = items.findIndex(item => item.route === currentRoute.value)
  
  if (event.key === 'ArrowLeft' && currentIndex > 0) {
    navigateTo(items[currentIndex - 1].route)
    event.preventDefault()
  } else if (event.key === 'ArrowRight' && currentIndex < items.length - 1) {
    navigateTo(items[currentIndex + 1].route)
    event.preventDefault()
  }
}

// 키보드 이벤트 리스너
import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
  document.addEventListener('keydown', handleKeyboard)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyboard)
})
</script>

<style scoped>
/* 사이드바 스타일 */
.sidebar-navigation {
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar-item {
  position: relative;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-item:hover {
  transform: translateX(2px);
}

.sidebar-item.active {
  position: relative;
}

.sidebar-item.active::before {
  content: '';
  position: absolute;
  left: -12px;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(to bottom, #3b82f6, #1d4ed8);
  border-radius: 0 2px 2px 0;
}

.sidebar-header {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

.sidebar-footer {
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
}

/* 모바일 네비게이션 스타일 */
@supports (backdrop-filter: blur(20px)) {
  .fixed.bottom-0 {
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    background-color: rgba(255, 255, 255, 0.95);
  }
}

@supports not (backdrop-filter: blur(20px)) {
  .fixed.bottom-0 {
    background-color: white;
  }
}

.nav-item {
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-icon {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 반응형 디자인 */
@media (min-width: 768px) {
  .sidebar-navigation {
    min-height: 100vh;
  }
}

@media (max-width: 767px) {
  .sidebar-navigation {
    display: none;
  }
}

/* 애니메이션 */
.sidebar-item {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 사용자 아바타 스타일 */
.avatar {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border: 2px solid #e0f2fe;
}
</style>