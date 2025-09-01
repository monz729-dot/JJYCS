<template>
  <div class="app-layout" :class="{ 'has-bottom-nav': hasBottomNav }">
    <!-- App Header -->
    <header v-if="showHeader" class="app-header">
      <div class="header-content">
        <button 
          v-if="showMenuButton"
          @click="toggleSidebar"
          class="menu-button"
          aria-label="메뉴 열기"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
          </svg>
        </button>
        
        <h1 class="app-title">YSC LMS</h1>
        
        <div class="header-actions">
          <!-- 알림 버튼 -->
          <button 
            v-if="authStore.isAuthenticated"
            @click="toggleNotifications"
            class="notification-button"
            :class="{ 'has-unread': unreadNotifications > 0 }"
            aria-label="알림"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path>
            </svg>
            <span v-if="unreadNotifications > 0" class="notification-badge">{{ unreadNotifications }}</span>
          </button>
          
          <!-- 햄버거 메뉴 버튼 -->
          <button 
            v-if="authStore.isAuthenticated"
            @click="showHamburgerMenu = !showHamburgerMenu"
            class="menu-button"
            aria-label="메뉴 열기"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"></path>
            </svg>
          </button>
        </div>
      </div>
    </header>

    <!-- Sidebar (데스크톱용) -->
    <aside 
      v-if="showSidebar" 
      class="sidebar" 
      :class="{ 'sidebar-open': sidebarOpen }"
    >
      <div class="sidebar-content">
        <AdminNavigation 
          v-if="canAccessAdmin" 
          :compact="true"
        />
        <PartnerNavigation 
          v-else-if="canAccessPartner && !canAccessAdmin" 
          :compact="true"
        />
      </div>
    </aside>

    <!-- Main Content -->
    <main class="main-content" :class="{ 'with-sidebar': showSidebar && sidebarOpen }">
      <!-- Loading Overlay -->
      <div v-if="loading" class="loading-overlay">
        <div class="loading-spinner">
          <svg class="animate-spin -ml-1 mr-3 h-8 w-8 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span>로딩 중...</span>
        </div>
      </div>
      
      <!-- 실제 페이지 콘텐츠 -->
      <div class="page-content">
        <router-view />
      </div>
    </main>

    <!-- Hamburger Menu Dropdown -->
    <transition name="fade">
      <div
        v-if="showHamburgerMenu"
        class="fixed right-4 top-14 w-64 rounded-xl border bg-white shadow-lg overflow-hidden z-50"
        @click.outside="showHamburgerMenu = false"
      >
        <nav class="py-1">
          <MenuItem label="홈" :to="{name:'dashboard'}" icon="home" @done="closeHamburgerMenu" />
          <MenuItem label="마이페이지" :to="{name:'mypage'}" icon="user" @done="closeHamburgerMenu" />
          <MenuItem v-if="canManageOrders" label="주문서 작성" :to="{name:'order-create'}" icon="file-plus" @done="closeHamburgerMenu" />
          <MenuItem label="주문내역" :to="{name:'orders'}" icon="list" @done="closeHamburgerMenu" />
          <MenuItem 
            v-if="canAccessCorporate || canAccessPartner"
            label="일괄 등록" 
            :to="{name:'bulk-management'}" 
            icon="upload" 
            @done="closeHamburgerMenu" 
          />
          <MenuItem v-if="canAccessWarehouse" label="창고 관리" :to="{name:'warehouse-dashboard'}" icon="package" @done="closeHamburgerMenu" />
          <MenuItem v-if="canAccessAdmin" label="관리자" :to="{name:'admin-dashboard'}" icon="settings" @done="closeHamburgerMenu" />
          <MenuItem label="공지사항" :to="{name:'notices'}" icon="bell" @done="closeHamburgerMenu" />
          <MenuItem label="FAQ" :to="{name:'faq'}" icon="help-circle" @done="closeHamburgerMenu" />
          <div class="border-t my-1"></div>
          <MenuItem label="로그아웃" icon="log-out" @done="handleLogout" />
        </nav>
      </div>
    </transition>

    <!-- Notification Panel -->
    <div 
      v-if="showNotifications"
      class="notification-panel"
      @click.self="showNotifications = false"
    >
      <div class="notification-content">
        <div class="notification-header">
          <h3>알림</h3>
          <button @click="showNotifications = false" class="close-button">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
        <div class="notification-list">
          <div v-if="notifications.length === 0" class="no-notifications">
            새 알림이 없습니다.
          </div>
          <div 
            v-for="notification in notifications" 
            :key="notification.id"
            class="notification-item"
            :class="{ 'unread': !notification.read }"
          >
            <div class="notification-content">
              <h4>{{ notification.title }}</h4>
              <p>{{ notification.message }}</p>
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>


    <!-- Backdrop for panels -->
    <div 
      v-if="showNotifications || sidebarOpen || showHamburgerMenu"
      class="backdrop"
      @click="closeAllPanels"
    ></div>

    <!-- Toast Notifications -->
    <ToastContainer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watchEffect } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useToast } from '../../composables/useToast'
import { useRoleCheck } from '../../composables/useRoleCheck'
import ToastContainer from './ToastContainer.vue'
import { AdminNavigation, PartnerNavigation } from '../navigation'
import MenuItem from '../common/MenuItem.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { showToast } = useToast()
const { canAccessAdmin, canAccessWarehouse, canAccessPartner, canAccessCorporate, canManageOrders } = useRoleCheck()

// 반응형 상태
const sidebarOpen = ref(false)
const showNotifications = ref(false)
const showHamburgerMenu = ref(false)
const loading = ref(false)
const unreadNotifications = ref(0)
const notifications = ref<any[]>([])

// 화면 크기 감지
const isMobile = ref(window.innerWidth < 768)
const isTablet = ref(window.innerWidth >= 768 && window.innerWidth < 1024)
const isDesktop = ref(window.innerWidth >= 1024)

// 레이아웃 계산
const showHeader = computed(() => {
  // 관리자 라우트일 때는 전역 헤더 숨김
  if (route.meta?.admin) {
    return false
  }
  return authStore.isAuthenticated && !route.meta?.hideHeader
})

const showMenuButton = computed(() => {
  return isDesktop.value && (authStore.isAdmin || authStore.isPartner)
})

const showSidebar = computed(() => {
  return isDesktop.value && (authStore.isAdmin || authStore.isPartner)
})

// 하단 네비게이션 제거
const hasBottomNav = computed(() => false)

// 사용자 타입 라벨
const getUserTypeLabel = (userType?: string) => {
  switch (userType) {
    case 'ADMIN': return '관리자'
    case 'PARTNER': return '파트너'
    case 'CORPORATE': return '기업회원'
    case 'WAREHOUSE': return '창고직원'
    default: return '일반회원'
  }
}

// 시간 포맷팅
const formatTime = (date: string) => {
  const now = new Date()
  const time = new Date(date)
  const diff = now.getTime() - time.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  if (hours < 24) return `${hours}시간 전`
  if (days < 7) return `${days}일 전`
  
  return time.toLocaleDateString('ko-KR')
}

// 액션 함수들
const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const toggleNotifications = async () => {
  showNotifications.value = !showNotifications.value
  
  // 알림 패널을 열 때마다 최신 알림 데이터 로드
  if (showNotifications.value && authStore.isAuthenticated) {
    await loadNotifications()
  }
}

const closeAllPanels = () => {
  sidebarOpen.value = false
  showNotifications.value = false
  showHamburgerMenu.value = false
}

const closeHamburgerMenu = () => {
  showHamburgerMenu.value = false
}

const handleLogout = async () => {
  closeHamburgerMenu()
  try {
    await authStore.logout()
    showToast('로그아웃되었습니다.', 'success')
    // 강제로 로그인 페이지로 이동하고 히스토리 초기화
    window.location.href = '/login'
  } catch (error) {
    showToast('로그아웃 중 오류가 발생했습니다.', 'error')
    // 에러가 있어도 로그인 페이지로 이동
    window.location.href = '/login'
  }
}

// 화면 크기 변화 감지
const updateScreenSize = () => {
  isMobile.value = window.innerWidth < 768
  isTablet.value = window.innerWidth >= 768 && window.innerWidth < 1024
  isDesktop.value = window.innerWidth >= 1024
  
  // 모바일에서 데스크톱으로 전환 시 사이드바 자동 닫기
  if (isMobile.value || isTablet.value) {
    sidebarOpen.value = false
  }
}

// 알림 로드
const loadNotifications = async () => {
  try {
    // 사용자 정보 확인
    const userId = authStore.user?.id
    if (!userId) {
      // 로그인하지 않은 경우 조용히 종료
      notifications.value = []
      unreadNotifications.value = 0
      return
    }

    // 실제 알림 API 호출 - 올바른 엔드포인트 사용
    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/notifications/user/${userId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      // 404 에러는 알림이 없는 경우이므로 정상 처리
      if (response.status === 404) {
        notifications.value = []
        unreadNotifications.value = 0
        return
      }
      
      // 401 에러는 인증 문제이므로 조용히 처리
      if (response.status === 401) {
        notifications.value = []
        unreadNotifications.value = 0
        return
      }
      
      // 기타 에러만 로그에 기록
      console.warn(`Notification API returned status: ${response.status}`)
      notifications.value = []
      unreadNotifications.value = 0
      return
    }

    const result = await response.json()
    
    if (result.success && result.data) {
      // API 응답 데이터를 프론트엔드 형식으로 변환
      notifications.value = result.data.map(notification => ({
        id: notification.id,
        title: notification.title,
        message: notification.message,
        createdAt: notification.createdAt,
        read: notification.isRead || false,
        type: notification.type,
        actionUrl: notification.actionUrl
      }))
      
      // 읽지 않은 알림 개수 업데이트
      unreadNotifications.value = result.unreadCount || notifications.value.filter(n => !n.read).length
    } else {
      // API 응답은 성공했지만 데이터가 없는 경우 - 정상 상황
      notifications.value = []
      unreadNotifications.value = 0
    }
  } catch (error) {
    // 네트워크 에러 등은 조용히 처리
    console.debug('Notification loading skipped:', error.message)
    notifications.value = []
    unreadNotifications.value = 0
    
    // 사용자에게 에러 메시지를 표시하지 않음
    // 알림은 부가 기능이므로 실패해도 주요 기능에 영향 없음
  }
}

// PWA 업데이트 감지
const checkForUpdates = () => {
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.addEventListener('controllerchange', () => {
      showToast('앱이 업데이트되었습니다. 새로고침하시겠습니까?', 'info', {
        duration: 0,
        action: {
          label: '새로고침',
          handler: () => window.location.reload()
        }
      })
    })
  }
}

// 네트워크 상태 감지
const handleOnlineStatus = () => {
  if (!navigator.onLine) {
    showToast('인터넷 연결이 끊어졌습니다.', 'warning')
  } else {
    showToast('인터넷에 다시 연결되었습니다.', 'success')
  }
}

onMounted(() => {
  updateScreenSize()
  window.addEventListener('resize', updateScreenSize)
  window.addEventListener('online', handleOnlineStatus)
  window.addEventListener('offline', handleOnlineStatus)
  
  if (authStore.isAuthenticated) {
    loadNotifications()
  }
  
  checkForUpdates()
})

// 인증 상태 변화 감지
watchEffect(() => {
  if (authStore.isAuthenticated) {
    loadNotifications()
  } else {
    notifications.value = []
    unreadNotifications.value = 0
  }
})

// 라우트 변화 감지하여 패널 닫기
watchEffect(() => {
  route.path // 라우트 변화 감지용
  closeAllPanels()
})
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
  position: relative;
}

/* 하단 네비게이션 제거 */

/* 헤더 */
.app-header {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 40;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  max-width: 1200px;
  margin: 0 auto;
}

.menu-button {
  padding: 0.5rem;
  border-radius: 0.375rem;
  transition: all 0.2s;
  color: #64748b;
}

.menu-button:hover {
  background-color: #f1f5f9;
  color: #334155;
}

.app-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.notification-button,
.profile-button {
  position: relative;
  padding: 0.5rem;
  border-radius: 0.375rem;
  transition: all 0.2s;
  color: #64748b;
}

.notification-button:hover,
.profile-button:hover {
  background-color: #f1f5f9;
  color: #334155;
}

.notification-button.has-unread {
  color: #3b82f6;
}

.notification-badge {
  position: absolute;
  top: 0.125rem;
  right: 0.125rem;
  background: #ef4444;
  color: white;
  font-size: 0.75rem;
  border-radius: 9999px;
  width: 1.25rem;
  height: 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.avatar {
  width: 2rem;
  height: 2rem;
  border-radius: 9999px;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

/* 사이드바 */
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  width: 16rem;
  height: 100vh;
  background: white;
  border-right: 1px solid #e2e8f0;
  transform: translateX(-100%);
  transition: transform 0.3s ease;
  z-index: 50;
  overflow-y: auto;
}

.sidebar-open {
  transform: translateX(0);
}

.sidebar-content {
  padding: 1rem;
  margin-top: 4rem; /* 헤더 높이만큼 여백 */
}

/* 메인 콘텐츠 */
.main-content {
  flex: 1;
  position: relative;
  min-height: calc(100vh - 4rem); /* 헤더 높이 제외 */
  transition: margin-left 0.3s ease;
}

.with-sidebar {
  margin-left: 16rem;
}

.page-content {
  padding: 1rem;
  max-width: 1200px;
  margin: 0 auto;
}

/* 로딩 오버레이 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 30;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  color: #3b82f6;
}

/* 햄버거 메뉴 트랜지션 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 알림 패널 */
.notification-panel {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  max-width: 24rem;
  background: rgba(0, 0, 0, 0.5);
  z-index: 60;
  display: flex;
  justify-content: flex-end;
}

.notification-content {
  background: white;
  width: 100%;
  max-width: 20rem;
  height: 100%;
  overflow-y: auto;
  box-shadow: -4px 0 6px -1px rgba(0, 0, 0, 0.1);
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.notification-header h3 {
  font-weight: 600;
  margin: 0;
}

.close-button {
  padding: 0.25rem;
  border-radius: 0.25rem;
  color: #64748b;
}

.close-button:hover {
  background: #e2e8f0;
}

.notification-list {
  padding: 0.5rem;
}

.no-notifications {
  text-align: center;
  padding: 2rem;
  color: #64748b;
}

.notification-item {
  padding: 0.75rem;
  border-radius: 0.5rem;
  margin-bottom: 0.5rem;
  border: 1px solid #f1f5f9;
  transition: all 0.2s;
}

.notification-item:hover {
  border-color: #e2e8f0;
  background: #f8fafc;
}

.notification-item.unread {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.notification-item h4 {
  font-weight: 600;
  margin: 0 0 0.25rem 0;
  font-size: 0.875rem;
}

.notification-item p {
  color: #64748b;
  margin: 0 0 0.5rem 0;
  font-size: 0.875rem;
  line-height: 1.4;
}

.notification-time {
  font-size: 0.75rem;
  color: #94a3b8;
}

/* 프로필 패널 */
.profile-panel {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  max-width: 20rem;
  background: rgba(0, 0, 0, 0.5);
  z-index: 60;
  display: flex;
  justify-content: flex-end;
}

.profile-content {
  background: white;
  width: 100%;
  height: 100%;
  overflow-y: auto;
  box-shadow: -4px 0 6px -1px rgba(0, 0, 0, 0.1);
}

.profile-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.avatar-large {
  width: 4rem;
  height: 4rem;
  border-radius: 9999px;
  overflow: hidden;
  margin-bottom: 1rem;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.profile-info h3 {
  font-weight: 600;
  margin: 0 0 0.25rem 0;
}

.profile-info p {
  opacity: 0.9;
  margin: 0 0 0.5rem 0;
  font-size: 0.875rem;
}

.user-type {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.profile-actions {
  padding: 1rem;
}

.profile-action-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.75rem;
  text-align: left;
  border-radius: 0.5rem;
  transition: all 0.2s;
  margin-bottom: 0.5rem;
}

.profile-action-button:hover {
  background: #f1f5f9;
}

.profile-action-button.logout:hover {
  background: #fef2f2;
  color: #dc2626;
}

/* 백드롭 */
.backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 45;
}

/* 모바일 최적화 */
@media (max-width: 768px) {
  .sidebar {
    width: 100%;
  }
  
  .with-sidebar {
    margin-left: 0;
  }
  
  .page-content {
    padding: 0.75rem;
  }
  
  .header-content {
    padding: 0.5rem 1rem;
  }
  
  .notification-panel,
  .profile-panel {
    width: 100%;
    max-width: none;
  }
  
  .notification-content,
  .profile-content {
    max-width: none;
  }
}

/* 태블릿 최적화 */
@media (min-width: 768px) and (max-width: 1024px) {
  .sidebar {
    width: 20rem;
  }
  
  .with-sidebar {
    margin-left: 20rem;
  }
}

/* 애니메이션 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.notification-item,
.profile-action-button {
  animation: fadeIn 0.3s ease;
}

/* 포커스 스타일 */
button:focus {
  outline: 2px solid #3b82f6;
  outline-offset: 2px;
}

button:focus:not(:focus-visible) {
  outline: none;
}

/* 스크롤바 스타일링 */
.notification-content::-webkit-scrollbar,
.profile-content::-webkit-scrollbar,
.sidebar::-webkit-scrollbar {
  width: 6px;
}

.notification-content::-webkit-scrollbar-track,
.profile-content::-webkit-scrollbar-track,
.sidebar::-webkit-scrollbar-track {
  background: #f1f5f9;
}

.notification-content::-webkit-scrollbar-thumb,
.profile-content::-webkit-scrollbar-thumb,
.sidebar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.notification-content::-webkit-scrollbar-thumb:hover,
.profile-content::-webkit-scrollbar-thumb:hover,
.sidebar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>