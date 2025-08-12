import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'

export const useUiStore = defineStore('ui', () => {
  // State
  const sidebarCollapsed = ref(false)
  const darkMode = ref(false)
  const language = ref<'ko' | 'en'>('ko')
  const loading = ref(false)
  const globalLoading = ref(false)
  const isMobile = ref(false)
  const screenSize = ref<'xs' | 'sm' | 'md' | 'lg' | 'xl'>('md')

  // Modal state
  const modals = ref<Record<string, boolean>>({})
  const activeModal = ref<string | null>(null)

  // Page state
  const pageTitle = ref('')
  const breadcrumbs = ref<Array<{ label: string; to?: string }>>([])

  // Theme state
  const theme = ref<'light' | 'dark' | 'auto'>('auto')

  // Getters
  const isLoading = computed(() => loading.value || globalLoading.value)
  const currentTheme = computed(() => {
    if (theme.value === 'auto') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    return theme.value
  })

  // Actions
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const setSidebarCollapsed = (collapsed: boolean) => {
    sidebarCollapsed.value = collapsed
  }

  const toggleDarkMode = () => {
    darkMode.value = !darkMode.value
    updateTheme()
  }

  const setTheme = (newTheme: 'light' | 'dark' | 'auto') => {
    theme.value = newTheme
    updateTheme()
    localStorage.setItem('theme', newTheme)
  }

  const updateTheme = () => {
    const root = document.documentElement
    const isDark = currentTheme.value === 'dark'
    
    if (isDark) {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }
    
    // Update meta theme-color
    const metaThemeColor = document.querySelector('meta[name="theme-color"]')
    if (metaThemeColor) {
      metaThemeColor.setAttribute('content', isDark ? '#1a1a1a' : '#ffffff')
    }
  }

  const setLanguage = (lang: 'ko' | 'en') => {
    language.value = lang
    localStorage.setItem('language', lang)
    document.documentElement.lang = lang
  }

  const setLoading = (isLoading: boolean) => {
    loading.value = isLoading
  }

  const setGlobalLoading = (isLoading: boolean) => {
    globalLoading.value = isLoading
  }

  const setMobile = (mobile: boolean) => {
    isMobile.value = mobile
  }

  const setScreenSize = (size: 'xs' | 'sm' | 'md' | 'lg' | 'xl') => {
    screenSize.value = size
  }

  // Modal management
  const openModal = (modalId: string) => {
    modals.value[modalId] = true
    activeModal.value = modalId
    
    // Prevent body scroll
    document.body.style.overflow = 'hidden'
  }

  const closeModal = (modalId: string) => {
    modals.value[modalId] = false
    
    // If this was the active modal, clear it
    if (activeModal.value === modalId) {
      activeModal.value = null
    }
    
    // Restore body scroll if no modals are open
    if (!Object.values(modals.value).some(Boolean)) {
      document.body.style.overflow = ''
    }
  }

  const closeAllModals = () => {
    Object.keys(modals.value).forEach(modalId => {
      modals.value[modalId] = false
    })
    activeModal.value = null
    document.body.style.overflow = ''
  }

  const isModalOpen = (modalId: string) => {
    return modals.value[modalId] === true
  }

  // Page management
  const setPageTitle = (title: string) => {
    pageTitle.value = title
    document.title = title ? `${title} - YCS LMS` : 'YCS LMS'
  }

  const setBreadcrumbs = (crumbs: Array<{ label: string; to?: string }>) => {
    breadcrumbs.value = crumbs
  }

  const addBreadcrumb = (crumb: { label: string; to?: string }) => {
    breadcrumbs.value.push(crumb)
  }

  const clearBreadcrumbs = () => {
    breadcrumbs.value = []
  }

  // Responsive helpers
  const updateResponsiveState = () => {
    const width = window.innerWidth
    
    if (width < 640) {
      setScreenSize('xs')
      setMobile(true)
    } else if (width < 768) {
      setScreenSize('sm')
      setMobile(true)
    } else if (width < 1024) {
      setScreenSize('md')
      setMobile(false)
    } else if (width < 1280) {
      setScreenSize('lg')
      setMobile(false)
    } else {
      setScreenSize('xl')
      setMobile(false)
    }
    
    // Auto collapse sidebar on mobile
    if (isMobile.value) {
      setSidebarCollapsed(true)
    }
  }

  // Initialize from localStorage
  const initializeFromStorage = () => {
    const savedTheme = localStorage.getItem('theme') as 'light' | 'dark' | 'auto'
    if (savedTheme) {
      setTheme(savedTheme)
    }
    
    const savedLanguage = localStorage.getItem('language') as 'ko' | 'en'
    if (savedLanguage) {
      setLanguage(savedLanguage)
    }
    
    const savedSidebarState = localStorage.getItem('sidebarCollapsed')
    if (savedSidebarState) {
      setSidebarCollapsed(JSON.parse(savedSidebarState))
    }
  }

  // Initialize responsive listeners
  const initializeResponsive = () => {
    updateResponsiveState()
    window.addEventListener('resize', updateResponsiveState)
  }

  // Cleanup
  const cleanup = () => {
    window.removeEventListener('resize', updateResponsiveState)
  }

  // Keyboard shortcuts
  const handleKeyboardShortcut = (event: KeyboardEvent) => {
    // Cmd/Ctrl + K for search
    if ((event.metaKey || event.ctrlKey) && event.key === 'k') {
      event.preventDefault()
      openModal('search')
    }
    
    // Escape to close modals
    if (event.key === 'Escape' && activeModal.value) {
      closeModal(activeModal.value)
    }
  }

  const initializeKeyboardShortcuts = () => {
    document.addEventListener('keydown', handleKeyboardShortcut)
  }

  const cleanupKeyboardShortcuts = () => {
    document.removeEventListener('keydown', handleKeyboardShortcut)
  }

  return {
    // State
    sidebarCollapsed: readonly(sidebarCollapsed),
    darkMode: readonly(darkMode),
    language: readonly(language),
    loading: readonly(loading),
    globalLoading: readonly(globalLoading),
    isMobile: readonly(isMobile),
    screenSize: readonly(screenSize),
    modals: readonly(modals),
    activeModal: readonly(activeModal),
    pageTitle: readonly(pageTitle),
    breadcrumbs: readonly(breadcrumbs),
    theme: readonly(theme),
    
    // Getters
    isLoading,
    currentTheme,
    
    // Actions
    toggleSidebar,
    setSidebarCollapsed,
    toggleDarkMode,
    setTheme,
    updateTheme,
    setLanguage,
    setLoading,
    setGlobalLoading,
    setMobile,
    setScreenSize,
    
    // Modal management
    openModal,
    closeModal,
    closeAllModals,
    isModalOpen,
    
    // Page management
    setPageTitle,
    setBreadcrumbs,
    addBreadcrumb,
    clearBreadcrumbs,
    
    // Responsive
    updateResponsiveState,
    
    // Initialization
    initializeFromStorage,
    initializeResponsive,
    cleanup,
    initializeKeyboardShortcuts,
    cleanupKeyboardShortcuts
  }
})