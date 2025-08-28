<template>
  <nav class="fixed bottom-0 left-0 right-0 bg-white/95 border-t border-blue-100 shadow-lg z-50 backdrop-blur-md">
    <div class="grid grid-cols-3 h-16">
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
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

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

const FileIcon = () => h('svg', { 
  class: 'h-5 w-5', 
  fill: 'currentColor', 
  viewBox: '0 0 20 20' 
}, [
  h('path', {
    'fill-rule': 'evenodd',
    d: 'M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z',
    'clip-rule': 'evenodd'
  })
])


const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentRoute = computed(() => route.name as string)

// 네비게이션 아이템들 (사용자 유형에 따라 다름)
const navigationItems = computed(() => {
  const userType = authStore.user?.userType || 'GENERAL'
  
  const baseItems = [
    {
      id: 'dashboard',
      label: '홈',
      icon: HomeIcon,
      route: 'Dashboard',
      description: '메인 대시보드'
    },
    {
      id: 'orders',
      label: '주문',
      icon: PackageIcon,
      route: 'Orders',
      description: '주문 관리',
      badge: 0 // 예: 진행중인 주문 개수
    },
    {
      id: 'history',
      label: '내역',
      icon: FileIcon,
      route: 'OrderHistory',
      description: '주문 내역'
    }
  ]

  // 기업회원의 경우 추가 기능
  if (userType === 'CORPORATE') {
    baseItems[1].badge = 3 // 예: 승인 대기중인 주문
  }

  // 파트너의 경우 주문 관리 중심 메뉴 구성
  if (userType === 'PARTNER') {
    return [
      {
        id: 'dashboard',
        label: '홈',
        icon: HomeIcon,
        route: 'partner-dashboard',
        description: '파트너 대시보드'
      },
      {
        id: 'orders',
        label: '주문',
        icon: PackageIcon,
        route: 'orders',
        description: '주문 관리'
      },
      {
        id: 'new-order',
        label: '새주문',
        icon: FileIcon,
        route: 'order-create',
        description: '새 주문 접수'
      }
    ]
  }

  return baseItems
})

const navigateTo = (routeName: string) => {
  if (route.name !== routeName) {
    router.push({ name: routeName })
  }
}

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