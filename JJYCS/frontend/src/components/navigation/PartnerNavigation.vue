<template>
  <nav class="fixed bottom-0 left-0 right-0 bg-white/95 border-t border-blue-100 shadow-lg z-50 backdrop-blur-md">
    <div class="grid grid-cols-5 h-16">
      <button
        v-for="item in navigationItems"
        :key="item.id"
        :class="[
          'nav-item flex flex-col items-center justify-center gap-1 relative transition-all duration-300',
          currentRoute === item.route ? 'text-purple-600 bg-purple-50' : 'text-purple-400 hover:text-purple-600 hover:bg-purple-50'
        ]"
        @click="navigateTo(item.route)"
        :aria-label="item.description"
      >
        <!-- 활성 인디케이터 -->
        <div 
          :class="[
            'absolute top-0 left-1/2 w-8 h-1 bg-purple-600 rounded-b-full transform -translate-x-1/2 transition-all duration-300',
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
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// Props
interface Props {
  showLogoutButton?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showLogoutButton: false
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

const UserPlusIcon = () => h('svg', { 
  class: 'h-5 w-5', 
  fill: 'currentColor', 
  viewBox: '0 0 20 20' 
}, [
  h('path', {
    d: 'M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6zM16 7a1 1 0 10-2 0v1h-1a1 1 0 100 2h1v1a1 1 0 102 0v-1h1a1 1 0 100-2h-1V7z'
  })
])

const ClipboardIcon = () => h('svg', { 
  class: 'h-5 w-5', 
  fill: 'currentColor', 
  viewBox: '0 0 20 20' 
}, [
  h('path', {
    d: 'M8 3a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1z'
  }),
  h('path', {
    d: 'M6 3a2 2 0 00-2 2v11a2 2 0 002 2h8a2 2 0 002-2V5a2 2 0 00-2-2 3 3 0 01-3 3H9a3 3 0 01-3-3z'
  })
])

const CurrencyIcon = () => h('svg', { 
  class: 'h-5 w-5', 
  fill: 'currentColor', 
  viewBox: '0 0 20 20' 
}, [
  h('path', {
    'fill-rule': 'evenodd',
    d: 'M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z',
    'clip-rule': 'evenodd'
  })
])

const UserIcon = () => h('svg', { 
  class: 'h-5 w-5', 
  fill: 'currentColor', 
  viewBox: '0 0 20 20' 
}, [
  h('path', {
    'fill-rule': 'evenodd',
    d: 'M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z',
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

// 파트너 네비게이션 아이템들 (주문 관리 중심)
const navigationItems = computed(() => [
  {
    id: 'partner-dashboard',
    label: '대시보드',
    icon: HomeIcon,
    route: 'partner-dashboard',
    description: '파트너 메인',
    badge: 0
  },
  {
    id: 'partner-orders',
    label: '주문관리',
    icon: ClipboardIcon,
    route: 'orders',
    description: '주문 목록 및 관리',
    badge: 0
  },
  {
    id: 'order-create',
    label: '새주문',
    icon: UserPlusIcon,
    route: 'order-create',
    description: '새 주문 접수',
    badge: 0
  },
  {
    id: 'tracking',
    label: '배송조회',
    icon: CurrencyIcon,
    route: 'tracking',
    description: '주문 배송 추적',
    badge: 0
  },
  {
    id: 'partner-profile',
    label: '내정보',
    icon: UserIcon,
    route: 'profile',
    description: '파트너 설정',
    badge: 0
  }
])


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
@supports (backdrop-filter: blur(20px)) {
  nav {
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    background-color: rgba(255, 255, 255, 0.95);
  }
}

@supports not (backdrop-filter: blur(20px)) {
  nav {
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
</style>