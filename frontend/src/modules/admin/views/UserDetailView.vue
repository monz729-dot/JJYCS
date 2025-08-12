<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center space-x-4">
      <button @click="goBack" class="p-2 text-gray-500 hover:text-gray-700">
        <ArrowLeftIcon class="h-5 w-5" />
      </button>
      <div class="flex-1">
        <h1 class="text-2xl font-bold text-gray-900">사용자 상세</h1>
        <p class="text-sm text-gray-600 mt-1">사용자 정보와 활동 내역을 확인할 수 있습니다</p>
      </div>
      <div class="flex space-x-3">
        <button @click="toggleEdit" 
                class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50">
          <PencilIcon class="h-4 w-4 mr-2" />
          {{ isEditing ? '편집 취소' : '편집' }}
        </button>
        <button @click="resetPassword" 
                class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700">
          <KeyIcon class="h-4 w-4 mr-2" />
          비밀번호 재설정
        </button>
      </div>
    </div>

    <!-- User Info Cards -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Basic Info -->
      <div class="lg:col-span-2 bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900">기본 정보</h3>
        </div>
        <div class="p-6">
          <form @submit.prevent="saveUser" v-if="isEditing">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">이름</label>
                <input v-model="editForm.name" 
                       type="text" 
                       required 
                       class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">이메일</label>
                <input v-model="editForm.email" 
                       type="email" 
                       required 
                       class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">전화번호</label>
                <input v-model="editForm.phone" 
                       type="text" 
                       class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">회원 코드</label>
                <input v-model="editForm.memberCode" 
                       type="text" 
                       class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">역할</label>
                <select v-model="editForm.role" 
                        class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500">
                  <option value="general">일반 사용자</option>
                  <option value="enterprise">기업 사용자</option>
                  <option value="partner">파트너</option>
                  <option value="warehouse">창고</option>
                  <option value="admin">관리자</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">상태</label>
                <select v-model="editForm.status" 
                        class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500">
                  <option value="active">활성</option>
                  <option value="inactive">비활성</option>
                  <option value="pending">승인 대기</option>
                  <option value="suspended">정지</option>
                </select>
              </div>
            </div>
            <div class="mt-6 flex justify-end space-x-3">
              <button type="button" 
                      @click="cancelEdit" 
                      class="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50">
                취소
              </button>
              <button type="submit" 
                      :disabled="loading"
                      class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50">
                {{ loading ? '저장 중...' : '저장' }}
              </button>
            </div>
          </form>

          <!-- Read-only view -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-sm font-medium text-gray-500">이름</label>
              <p class="mt-1 text-sm text-gray-900">{{ user?.name }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-500">이메일</label>
              <p class="mt-1 text-sm text-gray-900">{{ user?.email }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-500">전화번호</label>
              <p class="mt-1 text-sm text-gray-900">{{ user?.phone || '없음' }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-500">회원 코드</label>
              <p class="mt-1 text-sm text-gray-900">{{ user?.memberCode || '없음' }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-500">역할</label>
              <span class="mt-1 inline-flex px-2 py-1 text-xs font-medium rounded-full"
                    :class="getRoleClass(user?.role || '')">
                {{ getRoleText(user?.role || '') }}
              </span>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-500">상태</label>
              <span class="mt-1 inline-flex px-2 py-1 text-xs font-medium rounded-full"
                    :class="getStatusClass(user?.status || '')">
                {{ getStatusText(user?.status || '') }}
              </span>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-500">가입일</label>
              <p class="mt-1 text-sm text-gray-900">{{ formatDate(user?.createdAt || '') }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-500">최근 로그인</label>
              <p class="mt-1 text-sm text-gray-900">{{ user?.lastLoginAt ? formatDate(user.lastLoginAt) : '없음' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Profile Summary -->
      <div class="space-y-6">
        <!-- Profile Card -->
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center space-x-3">
            <div class="h-16 w-16 bg-gray-300 rounded-full flex items-center justify-center">
              <UserIcon class="h-10 w-10 text-gray-500" />
            </div>
            <div>
              <h3 class="text-lg font-medium text-gray-900">{{ user?.name }}</h3>
              <p class="text-sm text-gray-500">{{ user?.email }}</p>
              <span class="inline-flex mt-1 px-2 py-1 text-xs font-medium rounded-full"
                    :class="getStatusClass(user?.status || '')">
                {{ getStatusText(user?.status || '') }}
              </span>
            </div>
          </div>
        </div>

        <!-- Quick Stats -->
        <div class="bg-white rounded-lg shadow p-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">활동 통계</h3>
          <div class="space-y-3">
            <div class="flex justify-between">
              <span class="text-sm text-gray-500">총 주문</span>
              <span class="text-sm font-medium text-gray-900">{{ userStats.totalOrders }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-sm text-gray-500">완료된 주문</span>
              <span class="text-sm font-medium text-gray-900">{{ userStats.completedOrders }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-sm text-gray-500">총 결제 금액</span>
              <span class="text-sm font-medium text-gray-900">{{ userStats.totalAmount }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-sm text-gray-500">평균 주문 금액</span>
              <span class="text-sm font-medium text-gray-900">{{ userStats.averageAmount }}</span>
            </div>
          </div>
        </div>

        <!-- Quick Actions -->
        <div class="bg-white rounded-lg shadow p-6">
          <h3 class="text-lg font-medium text-gray-900 mb-4">빠른 작업</h3>
          <div class="space-y-2">
            <button @click="sendNotification" 
                    class="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 rounded-md">
              알림 발송
            </button>
            <button @click="viewOrders" 
                    class="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 rounded-md">
              주문 내역 보기
            </button>
            <button @click="exportUserData" 
                    class="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 rounded-md">
              데이터 내보내기
            </button>
            <button @click="suspendUser" 
                    class="w-full text-left px-3 py-2 text-sm text-red-700 hover:bg-red-50 rounded-md">
              계정 정지
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Activity History -->
    <div class="bg-white rounded-lg shadow">
      <div class="px-6 py-4 border-b border-gray-200">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-medium text-gray-900">활동 내역</h3>
          <div class="flex space-x-2">
            <button 
              v-for="tab in activityTabs" 
              :key="tab.key"
              @click="activeTab = tab.key"
              class="px-3 py-1 text-sm font-medium rounded-md"
              :class="activeTab === tab.key ? 'bg-blue-100 text-blue-700' : 'text-gray-500 hover:text-gray-700'"
            >
              {{ tab.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- Activity Content -->
      <div class="p-6">
        <!-- Orders Tab -->
        <div v-if="activeTab === 'orders'" class="space-y-4">
          <div v-for="order in recentOrders" :key="order.id" 
               class="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
            <div>
              <p class="text-sm font-medium text-gray-900">#{{ order.orderNumber }}</p>
              <p class="text-sm text-gray-500">{{ order.items }}개 품목</p>
              <p class="text-xs text-gray-400">{{ formatDate(order.createdAt) }}</p>
            </div>
            <div class="text-right">
              <p class="text-sm font-medium text-gray-900">{{ order.amount }}</p>
              <span class="inline-flex px-2 py-1 text-xs font-medium rounded-full"
                    :class="getStatusClass(order.status)">
                {{ getStatusText(order.status) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Payments Tab -->
        <div v-if="activeTab === 'payments'" class="space-y-4">
          <div v-for="payment in recentPayments" :key="payment.id" 
               class="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
            <div>
              <p class="text-sm font-medium text-gray-900">{{ payment.type }}</p>
              <p class="text-sm text-gray-500">주문 #{{ payment.orderNumber }}</p>
              <p class="text-xs text-gray-400">{{ formatDate(payment.createdAt) }}</p>
            </div>
            <div class="text-right">
              <p class="text-sm font-medium text-gray-900">{{ payment.amount }}</p>
              <span class="inline-flex px-2 py-1 text-xs font-medium rounded-full"
                    :class="payment.status === 'completed' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'">
                {{ payment.status === 'completed' ? '완료' : '대기' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Activity Logs Tab -->
        <div v-if="activeTab === 'logs'" class="space-y-4">
          <div v-for="log in activityLogs" :key="log.id" 
               class="flex items-start space-x-3 p-4 border border-gray-200 rounded-lg">
            <div class="flex-shrink-0 mt-0.5">
              <div class="h-2 w-2 bg-blue-500 rounded-full"></div>
            </div>
            <div class="flex-1">
              <p class="text-sm text-gray-900">{{ log.action }}</p>
              <p class="text-xs text-gray-500">{{ formatDateTime(log.createdAt) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeftIcon,
  PencilIcon,
  KeyIcon,
  UserIcon
} from '@heroicons/vue/24/outline'

interface User {
  id: number
  name: string
  email: string
  phone?: string
  memberCode?: string
  role: string
  status: string
  createdAt: string
  lastLoginAt?: string
}

const route = useRoute()
const router = useRouter()

// State
const user = ref<User | null>(null)
const loading = ref(false)
const isEditing = ref(false)
const activeTab = ref('orders')

const editForm = ref({
  name: '',
  email: '',
  phone: '',
  memberCode: '',
  role: '',
  status: ''
})

const userStats = ref({
  totalOrders: 24,
  completedOrders: 18,
  totalAmount: '₩2,450,000',
  averageAmount: '₩102,083'
})

const activityTabs = [
  { key: 'orders', label: '주문 내역' },
  { key: 'payments', label: '결제 내역' },
  { key: 'logs', label: '활동 로그' }
]

const recentOrders = ref([
  {
    id: 1,
    orderNumber: 'YCS240001',
    items: 3,
    amount: '₩485,000',
    status: 'completed',
    createdAt: '2024-01-20'
  },
  {
    id: 2,
    orderNumber: 'YCS240002',
    items: 1,
    amount: '₩125,000',
    status: 'shipping',
    createdAt: '2024-01-18'
  },
  {
    id: 3,
    orderNumber: 'YCS240003',
    items: 5,
    amount: '₩750,000',
    status: 'processing',
    createdAt: '2024-01-15'
  }
])

const recentPayments = ref([
  {
    id: 1,
    type: '배송비 결제',
    orderNumber: 'YCS240001',
    amount: '₩45,000',
    status: 'completed',
    createdAt: '2024-01-20'
  },
  {
    id: 2,
    type: '리패킹 비용',
    orderNumber: 'YCS240002',
    amount: '₩15,000',
    status: 'completed',
    createdAt: '2024-01-18'
  }
])

const activityLogs = ref([
  {
    id: 1,
    action: '주문 #YCS240001을 생성했습니다',
    createdAt: '2024-01-20T09:30:00'
  },
  {
    id: 2,
    action: '프로필 정보를 수정했습니다',
    createdAt: '2024-01-19T14:20:00'
  },
  {
    id: 3,
    action: '로그인했습니다',
    createdAt: '2024-01-19T08:15:00'
  }
])

// Methods
onMounted(() => {
  loadUser()
})

const loadUser = async () => {
  const userId = route.params.id
  // Mock data - replace with actual API call
  user.value = {
    id: 1,
    name: '김철수',
    email: 'kim@example.com',
    phone: '010-1234-5678',
    memberCode: 'YCS001',
    role: 'general',
    status: 'active',
    createdAt: '2024-01-15',
    lastLoginAt: '2024-01-20'
  }
}

const goBack = () => {
  router.push('/admin/users')
}

const toggleEdit = () => {
  if (!isEditing.value) {
    // Start editing - populate form
    editForm.value = {
      name: user.value?.name || '',
      email: user.value?.email || '',
      phone: user.value?.phone || '',
      memberCode: user.value?.memberCode || '',
      role: user.value?.role || '',
      status: user.value?.status || ''
    }
  }
  isEditing.value = !isEditing.value
}

const cancelEdit = () => {
  isEditing.value = false
  editForm.value = {
    name: '',
    email: '',
    phone: '',
    memberCode: '',
    role: '',
    status: ''
  }
}

const saveUser = async () => {
  loading.value = true
  try {
    // Mock save - replace with actual API call
    if (user.value) {
      Object.assign(user.value, editForm.value)
    }
    isEditing.value = false
  } catch (error) {
    console.error('Error saving user:', error)
  } finally {
    loading.value = false
  }
}

const resetPassword = () => {
  if (confirm('비밀번호를 재설정하시겠습니까? 새 비밀번호가 사용자에게 이메일로 전송됩니다.')) {
    // Mock password reset
    console.log('Password reset for user:', user.value?.id)
  }
}

const sendNotification = () => {
  console.log('Sending notification to user:', user.value?.id)
}

const viewOrders = () => {
  router.push(`/admin/orders?userId=${user.value?.id}`)
}

const exportUserData = () => {
  console.log('Exporting user data for:', user.value?.id)
}

const suspendUser = () => {
  if (confirm('정말로 이 사용자의 계정을 정지하시겠습니까?')) {
    // Mock suspend user
    if (user.value) {
      user.value.status = 'suspended'
    }
  }
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('ko-KR')
}

const formatDateTime = (date: string) => {
  return new Date(date).toLocaleString('ko-KR')
}

const getRoleClass = (role: string) => {
  const classes = {
    'general': 'bg-gray-100 text-gray-800',
    'enterprise': 'bg-blue-100 text-blue-800',
    'partner': 'bg-green-100 text-green-800',
    'warehouse': 'bg-purple-100 text-purple-800',
    'admin': 'bg-red-100 text-red-800'
  }
  return classes[role as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getRoleText = (role: string) => {
  const texts = {
    'general': '일반 사용자',
    'enterprise': '기업 사용자',
    'partner': '파트너',
    'warehouse': '창고',
    'admin': '관리자'
  }
  return texts[role as keyof typeof texts] || '알수없음'
}

const getStatusClass = (status: string) => {
  const classes = {
    'active': 'bg-green-100 text-green-800',
    'inactive': 'bg-gray-100 text-gray-800',
    'pending': 'bg-yellow-100 text-yellow-800',
    'suspended': 'bg-red-100 text-red-800',
    'processing': 'bg-blue-100 text-blue-800',
    'shipping': 'bg-purple-100 text-purple-800',
    'completed': 'bg-green-100 text-green-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status: string) => {
  const texts = {
    'active': '활성',
    'inactive': '비활성',
    'pending': '승인 대기',
    'suspended': '정지',
    'processing': '처리중',
    'shipping': '배송중',
    'completed': '완료'
  }
  return texts[status as keyof typeof texts] || '알수없음'
}
</script>