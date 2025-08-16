<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center items-center px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Error Icon -->
      <div class="text-center">
        <div class="mx-auto flex items-center justify-center h-20 w-20 rounded-full"
             :class="getErrorIconBackground(errorCode)">
          <component :is="getErrorIcon(errorCode)" 
                     class="h-10 w-10" 
                     :class="getErrorIconColor(errorCode)" />
        </div>
      </div>

      <!-- Error Content -->
      <div class="text-center">
        <h1 class="text-4xl font-bold text-gray-900 mb-2">
          {{ errorCode || '오류' }}
        </h1>
        <h2 class="text-xl font-semibold text-gray-700 mb-4">
          {{ errorTitle }}
        </h2>
        <p class="text-gray-600 mb-8 leading-relaxed">
          {{ errorMessage }}
        </p>

        <!-- Error Details (for development) -->
        <div v-if="showDetails && errorDetails" class="mb-8">
          <button
            @click="detailsExpanded = !detailsExpanded"
            class="text-sm text-gray-500 hover:text-gray-700 mb-2 flex items-center mx-auto"
          >
            <ChevronRightIcon 
              class="h-4 w-4 mr-1 transition-transform" 
              :class="{ 'rotate-90': detailsExpanded }" 
            />
            오류 세부사항
          </button>
          
          <div v-if="detailsExpanded" class="text-left bg-gray-100 rounded-lg p-4 text-xs font-mono text-gray-700">
            <pre class="whitespace-pre-wrap">{{ errorDetails }}</pre>
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="space-y-4">
        <div class="flex flex-col sm:flex-row gap-3 justify-center">
          <!-- Primary Action -->
          <button
            v-if="primaryAction"
            @click="handlePrimaryAction"
            :disabled="loading"
            class="inline-flex items-center justify-center px-6 py-3 border border-transparent rounded-md shadow-sm text-base font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <svg v-if="loading" class="animate-spin h-4 w-4 mr-2" viewBox="0 0 24 24">
              <path d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/>
            </svg>
            <component v-else-if="primaryAction.icon" :is="primaryAction.icon" class="h-4 w-4 mr-2" />
            {{ primaryAction.text }}
          </button>

          <!-- Secondary Actions -->
          <button
            v-for="action in secondaryActions"
            :key="action.key"
            @click="handleAction(action)"
            class="inline-flex items-center justify-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            <component v-if="action.icon" :is="action.icon" class="h-4 w-4 mr-2" />
            {{ action.text }}
          </button>
        </div>

        <!-- Additional Help -->
        <div v-if="showContactSupport" class="text-center pt-4 border-t border-gray-200">
          <p class="text-sm text-gray-500 mb-3">
            문제가 계속되면 고객 지원팀에 문의해주세요
          </p>
          <div class="flex justify-center space-x-4 text-sm">
            <a
              href="tel:+82-2-1234-5678"
              class="inline-flex items-center text-blue-600 hover:text-blue-500"
            >
              <PhoneIcon class="h-4 w-4 mr-1" />
              02-1234-5678
            </a>
            <a
              href="mailto:support@ycs-logistics.com"
              class="inline-flex items-center text-blue-600 hover:text-blue-500"
            >
              <EnvelopeIcon class="h-4 w-4 mr-1" />
              고객지원
            </a>
          </div>
        </div>

        <!-- Error Reporting -->
        <div v-if="canReportError" class="text-center pt-2">
          <button
            @click="reportError"
            :disabled="errorReported"
            class="text-xs text-gray-400 hover:text-gray-600 disabled:cursor-not-allowed"
          >
            {{ errorReported ? '오류 신고 완료' : '이 오류 신고하기' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Error Report Modal -->
    <div v-if="showReportModal" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div class="fixed inset-0 transition-opacity" @click="showReportModal = false">
          <div class="absolute inset-0 bg-gray-500 opacity-75"></div>
        </div>

        <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div class="sm:flex sm:items-start">
              <div class="w-full">
                <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4">
                  오류 신고
                </h3>
                
                <div class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">
                      오류 설명 (선택)
                    </label>
                    <textarea
                      v-model="errorReport.description"
                      rows="4"
                      class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                      placeholder="오류가 발생했을 때의 상황을 설명해주세요..."
                    ></textarea>
                  </div>
                  
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">
                      연락처 (선택)
                    </label>
                    <input
                      v-model="errorReport.contact"
                      type="email"
                      class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                      placeholder="답변을 받을 이메일 주소"
                    />
                  </div>

                  <div class="flex items-start">
                    <div class="flex items-center h-5">
                      <input
                        v-model="errorReport.includeDetails"
                        type="checkbox"
                        class="focus:ring-blue-500 h-4 w-4 text-blue-600 border-gray-300 rounded"
                      />
                    </div>
                    <div class="ml-3 text-sm">
                      <label class="font-medium text-gray-700">
                        기술적 세부사항 포함
                      </label>
                      <p class="text-gray-500">
                        오류 해결을 위해 브라우저 정보, URL 등을 함께 전송합니다.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button
              @click="submitErrorReport"
              :disabled="submittingReport"
              type="button"
              class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-blue-600 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 sm:ml-3 sm:w-auto sm:text-sm"
            >
              <span v-if="!submittingReport">신고하기</span>
              <span v-else class="flex items-center">
                <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                전송 중...
              </span>
            </button>
            <button
              @click="showReportModal = false"
              type="button"
              class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            >
              취소
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ExclamationTriangleIcon,
  XCircleIcon,
  ShieldExclamationIcon,
  WifiIcon,
  HomeIcon,
  ArrowLeftIcon,
  ArrowPathIcon,
  PhoneIcon,
  EnvelopeIcon,
  ChevronRightIcon
} from '@heroicons/vue/24/outline'

interface ErrorAction {
  key: string
  text: string
  action: () => void
  icon?: any
}

interface ErrorReport {
  description: string
  contact: string
  includeDetails: boolean
}

// Composables
const route = useRoute()
const router = useRouter()

// State
const loading = ref(false)
const detailsExpanded = ref(false)
const showReportModal = ref(false)
const errorReported = ref(false)
const submittingReport = ref(false)

const errorReport = ref<ErrorReport>({
  description: '',
  contact: '',
  includeDetails: true
})

// Get error info from route query or default values
const errorCode = computed(() => {
  return route.query.code as string || route.params.code as string || '500'
})

const errorMessage = computed(() => {
  return route.query.message as string || getDefaultErrorMessage(errorCode.value)
})

const errorDetails = computed(() => {
  return route.query.details as string || null
})

const showDetails = computed(() => {
  return process.env.NODE_ENV === 'development' && errorDetails.value
})

// Computed properties for error presentation
const errorTitle = computed(() => {
  const titles = {
    '400': '잘못된 요청',
    '401': '인증이 필요합니다',
    '403': '접근 권한이 없습니다',
    '404': '페이지를 찾을 수 없습니다',
    '408': '요청 시간 초과',
    '429': '너무 많은 요청',
    '500': '서버 오류가 발생했습니다',
    '502': '게이트웨이 오류',
    '503': '서비스를 사용할 수 없습니다',
    '504': '게이트웨이 시간 초과',
    'network': '네트워크 연결 오류',
    'timeout': '연결 시간 초과',
    'unknown': '알 수 없는 오류'
  }
  return titles[errorCode.value] || '오류가 발생했습니다'
})

const primaryAction = computed((): ErrorAction | null => {
  const code = errorCode.value
  
  if (['500', '502', '503', '504', 'network', 'timeout'].includes(code)) {
    return {
      key: 'retry',
      text: '다시 시도',
      action: handleRetry,
      icon: ArrowPathIcon
    }
  }
  
  if (code === '404') {
    return {
      key: 'home',
      text: '홈으로 가기',
      action: goHome,
      icon: HomeIcon
    }
  }
  
  if (['401', '403'].includes(code)) {
    return {
      key: 'login',
      text: '로그인하기',
      action: goToLogin,
      icon: null
    }
  }
  
  return {
    key: 'back',
    text: '이전 페이지',
    action: goBack,
    icon: ArrowLeftIcon
  }
})

const secondaryActions = computed((): ErrorAction[] => {
  const actions: ErrorAction[] = []
  
  // Always show "Go Home" unless it's the primary action
  if (primaryAction.value?.key !== 'home') {
    actions.push({
      key: 'home',
      text: '홈으로',
      action: goHome,
      icon: HomeIcon
    })
  }
  
  // Always show "Go Back" unless it's the primary action
  if (primaryAction.value?.key !== 'back' && window.history.length > 1) {
    actions.push({
      key: 'back',
      text: '뒤로가기',
      action: goBack,
      icon: ArrowLeftIcon
    })
  }
  
  return actions
})

const showContactSupport = computed(() => {
  return ['500', '502', '503', '504', 'unknown'].includes(errorCode.value)
})

const canReportError = computed(() => {
  return ['500', '502', '503', '504', 'unknown'].includes(errorCode.value)
})

// Methods
const getDefaultErrorMessage = (code: string): string => {
  const messages = {
    '400': '요청에 문제가 있습니다. 입력 정보를 확인해주세요.',
    '401': '로그인이 필요한 서비스입니다. 로그인 후 다시 시도해주세요.',
    '403': '이 페이지에 접근할 권한이 없습니다. 관리자에게 문의하세요.',
    '404': '요청하신 페이지를 찾을 수 없습니다. URL을 확인하거나 홈페이지에서 원하는 정보를 찾아보세요.',
    '408': '요청 처리 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.',
    '429': '너무 많은 요청을 보내셨습니다. 잠시 후 다시 시도해주세요.',
    '500': '서버에서 오류가 발생했습니다. 잠시 후 다시 시도하거나 고객지원팀에 문의해주세요.',
    '502': '게이트웨이 오류가 발생했습니다. 서버가 일시적으로 사용할 수 없는 상태입니다.',
    '503': '서비스를 일시적으로 사용할 수 없습니다. 시스템 점검 중이거나 과부하 상태일 수 있습니다.',
    '504': '서버 응답 시간이 초과되었습니다. 네트워크 연결을 확인하고 다시 시도해주세요.',
    'network': '네트워크 연결에 문제가 있습니다. 인터넷 연결을 확인하고 다시 시도해주세요.',
    'timeout': '연결 시간이 초과되었습니다. 네트워크 상태를 확인하고 다시 시도해주세요.',
    'unknown': '알 수 없는 오류가 발생했습니다. 문제가 계속되면 고객지원팀에 문의해주세요.'
  }
  return messages[code] || '오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
}

const getErrorIcon = (code: string) => {
  const icons = {
    '401': ShieldExclamationIcon,
    '403': ShieldExclamationIcon,
    '404': ExclamationTriangleIcon,
    'network': WifiIcon,
    'timeout': WifiIcon
  }
  return icons[code] || XCircleIcon
}

const getErrorIconBackground = (code: string) => {
  const backgrounds = {
    '401': 'bg-yellow-100',
    '403': 'bg-red-100',
    '404': 'bg-blue-100',
    'network': 'bg-purple-100',
    'timeout': 'bg-purple-100'
  }
  return backgrounds[code] || 'bg-red-100'
}

const getErrorIconColor = (code: string) => {
  const colors = {
    '401': 'text-yellow-600',
    '403': 'text-red-600',
    '404': 'text-blue-600',
    'network': 'text-purple-600',
    'timeout': 'text-purple-600'
  }
  return colors[code] || 'text-red-600'
}

const handlePrimaryAction = async () => {
  if (primaryAction.value) {
    loading.value = true
    try {
      await primaryAction.value.action()
    } catch (error) {
      console.error('Primary action failed:', error)
    } finally {
      loading.value = false
    }
  }
}

const handleAction = async (action: ErrorAction) => {
  try {
    await action.action()
  } catch (error) {
    console.error('Action failed:', error)
  }
}

const handleRetry = async () => {
  // Reload the current page or retry the last action
  window.location.reload()
}

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    goHome()
  }
}

const goToLogin = () => {
  router.push('/login')
}

const reportError = () => {
  if (!errorReported.value) {
    showReportModal.value = true
  }
}

const submitErrorReport = async () => {
  submittingReport.value = true
  
  try {
    // Collect error report data
    const reportData = {
      ...errorReport.value,
      errorCode: errorCode.value,
      errorMessage: errorMessage.value,
      url: window.location.href,
      userAgent: navigator.userAgent,
      timestamp: new Date().toISOString(),
      includeDetails: errorReport.value.includeDetails
    }
    
    // TODO: Send error report to API
    await new Promise(resolve => setTimeout(resolve, 2000)) // Mock API call
    
    errorReported.value = true
    showReportModal.value = false
    
    // Show success message
    alert('오류 신고가 접수되었습니다. 빠른 시일 내에 확인하겠습니다.')
  } catch (error) {
    console.error('Failed to submit error report:', error)
    alert('오류 신고 중 문제가 발생했습니다. 다시 시도해주세요.')
  } finally {
    submittingReport.value = false
  }
}

// Set page title
onMounted(() => {
  document.title = `${errorTitle.value} - YCS 물류관리시스템`
})
</script>

<style scoped>
/* Custom animations */
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-10px); }
  20%, 40%, 60%, 80% { transform: translateX(10px); }
}

.shake {
  animation: shake 0.5s ease-in-out;
}

/* Custom error icon pulse */
@keyframes pulse-error {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.pulse-error {
  animation: pulse-error 2s infinite;
}
</style>