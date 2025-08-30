<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">사용자 승인 관리</h1>
        <p class="mt-1 text-sm text-gray-500">가입 신청한 사용자들을 검토하고 승인 처리합니다</p>
      </div>
      <div class="mt-4 sm:mt-0 flex items-center space-x-3">
        <div class="bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full text-sm font-medium">
          {{ pendingCount }}명 승인 대기
        </div>
        <button
          @click="loadPendingUsers"
          :disabled="loading"
          class="btn-secondary"
        >
          <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          새로고침
        </button>
      </div>
    </div>

    <!-- Filters -->
    <div class="bg-white rounded-lg shadow-sm p-4">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">검색</label>
          <input
            v-model="filters.search"
            type="text"
            placeholder="이름, 이메일, 회사명..."
            class="input-field"
            @input="debouncedFilter"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">사용자 유형</label>
          <select v-model="filters.userType" class="input-field" @change="filterUsers">
            <option value="">모든 유형</option>
            <option value="CORPORATE">기업</option>
            <option value="PARTNER">파트너</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">가입일</label>
          <input
            v-model="filters.dateFrom"
            type="date"
            class="input-field"
            @change="filterUsers"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">상태</label>
          <select v-model="filters.status" class="input-field" @change="filterUsers">
            <option value="">모든 상태</option>
            <option value="PENDING">승인 대기</option>
            <option value="APPROVED">승인됨</option>
            <option value="REJECTED">거절됨</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Pending Approvals -->
    <div class="bg-white rounded-lg shadow-sm">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-lg font-semibold text-gray-900">승인 대기 목록</h2>
      </div>

      <div v-if="loading" class="p-6">
        <div class="space-y-4">
          <div v-for="i in 3" :key="i" class="animate-pulse">
            <div class="flex items-center space-x-4 p-4">
              <div class="w-12 h-12 bg-gray-200 rounded-full"></div>
              <div class="flex-1 space-y-2">
                <div class="h-4 bg-gray-200 rounded w-1/3"></div>
                <div class="h-3 bg-gray-200 rounded w-1/2"></div>
              </div>
              <div class="flex space-x-2">
                <div class="h-8 w-20 bg-gray-200 rounded"></div>
                <div class="h-8 w-20 bg-gray-200 rounded"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="filteredUsers.length === 0" class="p-6 text-center">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"></path>
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">승인 대기 중인 사용자가 없습니다</h3>
        <p class="mt-1 text-sm text-gray-500">모든 사용자 가입 요청이 처리되었습니다.</p>
      </div>

      <div v-else class="divide-y divide-gray-200">
        <div
          v-for="user in filteredUsers"
          :key="user.id"
          class="p-6 hover:bg-gray-50"
        >
          <div class="flex items-start justify-between">
            <div class="flex items-start space-x-4">
              <!-- User Avatar -->
              <div class="flex-shrink-0">
                <div class="w-12 h-12 bg-primary-500 rounded-full flex items-center justify-center">
                  <span class="text-lg font-medium text-white">
                    {{ getUserInitials(user.name) }}
                  </span>
                </div>
              </div>

              <!-- User Info -->
              <div class="flex-1 min-w-0">
                <div class="flex items-center space-x-2 mb-1">
                  <h3 class="text-lg font-medium text-gray-900">{{ user.name }}</h3>
                  <span :class="getUserTypeClass(user.userType)" class="status-badge text-xs">
                    {{ user.userType }}
                  </span>
                  <span :class="getStatusClass(user.status)" class="status-badge text-xs">
                    {{ user.status }}
                  </span>
                </div>
                
                <div class="text-sm text-gray-600 space-y-1">
                  <div class="flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                    </svg>
                    {{ user.email }}
                  </div>
                  <div class="flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path>
                    </svg>
                    {{ user.phone }}
                  </div>
                  <div v-if="user.companyName" class="flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                    </svg>
                    {{ user.companyName }}
                    <span v-if="user.businessNumber" class="ml-1 text-gray-500">
                      ({{ user.businessNumber }})
                    </span>
                  </div>
                  <div class="flex items-center text-gray-500">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3a2 2 0 012-2h4a2 2 0 012 2v4m-6 0V6a2 2 0 012-2h2a2 2 0 012 2v1m-6 0h6m-6 0l-.5-1.5A2 2 0 0111.172 4H12.828a2 2 0 011.672 1l.5 1.5M15 7v8a2 2 0 01-2 2H9a2 2 0 01-2-2V7m8 0V6a2 2 0 00-2-2H11a2 2 0 00-2 2v1"></path>
                    </svg>
                    {{ formatDate(user.createdAt) }} 가입
                  </div>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div v-if="user.status === 'PENDING'" class="flex-shrink-0 flex space-x-2">
              <button
                @click="openApprovalModal(user, 'approve')"
                :disabled="processing"
                class="btn-primary text-sm"
              >
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                </svg>
                승인
              </button>
              <button
                @click="openApprovalModal(user, 'reject')"
                :disabled="processing"
                class="btn-secondary text-red-600 hover:text-red-700 text-sm"
              >
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
                거절
              </button>
            </div>
            
            <!-- Status Info for Processed Users -->
            <div v-else class="flex-shrink-0 text-right text-sm text-gray-500">
              <div v-if="user.approvedAt">
                승인됨 {{ formatDate(user.approvedAt) }}
                <div v-if="user.approvedBy">by {{ user.approvedBy }}</div>
              </div>
              <div v-else-if="user.rejectedAt">
                거절됨 {{ formatDate(user.rejectedAt) }}
                <div v-if="user.rejectionReason" class="text-red-600">{{ user.rejectionReason }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Approval Modal -->
    <div v-if="showModal" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div class="fixed inset-0 transition-opacity" aria-hidden="true">
          <div class="absolute inset-0 bg-gray-500 opacity-75" @click="closeModal"></div>
        </div>

        <div class="inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
          <div>
            <div :class="[
              'mx-auto flex items-center justify-center h-12 w-12 rounded-full',
              modalAction === 'approve' ? 'bg-green-100' : 'bg-red-100'
            ]">
              <svg v-if="modalAction === 'approve'" class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
              </svg>
              <svg v-else class="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </div>
            <div class="mt-3 text-center sm:mt-5">
              <h3 class="text-lg leading-6 font-medium text-gray-900">
                {{ modalAction === 'approve' ? '사용자 승인' : '사용자 거절' }}
              </h3>
              <div class="mt-2">
                <p class="text-sm text-gray-500">
                  {{ modalAction === 'approve' 
                    ? `${selectedUser?.name} (${selectedUser?.userType})님을 승인하시겠습니까?`
                    : `${selectedUser?.name} (${selectedUser?.userType})님을 거절하시겠습니까?`
                  }}
                </p>
              </div>
            </div>
            
            <div class="mt-4">
              <label class="block text-sm font-medium text-gray-700 mb-2">
                {{ modalAction === 'approve' ? '승인 메모 (선택사항)' : '거절 사유' }}
              </label>
              <textarea
                v-model="approvalNotes"
                rows="3"
                class="input-field"
                :placeholder="modalAction === 'approve' 
                  ? '승인에 대한 메모를 작성해주세요...'
                  : '거절 사유를 입력해주세요...'
                "
                :required="modalAction === 'reject'"
              ></textarea>
            </div>
          </div>
          
          <div class="mt-5 sm:mt-6 sm:grid sm:grid-cols-2 sm:gap-3 sm:grid-flow-row-dense">
            <button
              @click="processApproval"
              :disabled="processing || (modalAction === 'reject' && !approvalNotes.trim())"
              :class="[
                'w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 sm:col-start-2 sm:text-sm',
                modalAction === 'approve' 
                  ? 'bg-green-600 hover:bg-green-700 focus:ring-green-500' 
                  : 'bg-red-600 hover:bg-red-700 focus:ring-red-500',
                (processing || (modalAction === 'reject' && !approvalNotes.trim())) ? 'opacity-50 cursor-not-allowed' : ''
              ]"
            >
              <svg v-if="processing" class="animate-spin -ml-1 mr-3 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ processing ? '처리 중...' : (modalAction === 'approve' ? '승인' : '거절') }}
            </button>
            <button
              @click="closeModal"
              :disabled="processing"
              class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:col-start-1 sm:text-sm"
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
import { ref, reactive, onMounted, computed } from 'vue'
import { adminApi } from '@/utils/api'
import type { User } from '@/types'

const loading = ref(true)
const processing = ref(false)
const showModal = ref(false)
const modalAction = ref<'approve' | 'reject'>('approve')
const selectedUser = ref<User | null>(null)
const approvalNotes = ref('')

const users = ref<User[]>([])

const filters = reactive({
  search: '',
  userType: '',
  dateFrom: '',
  status: 'PENDING'
})

const pendingCount = computed(() => {
  return users.value.filter(user => user.status === 'PENDING').length
})

const filteredUsers = computed(() => {
  let filtered = [...users.value]

  // Filter by search
  if (filters.search) {
    const search = filters.search.toLowerCase()
    filtered = filtered.filter(user => 
      user.name.toLowerCase().includes(search) ||
      user.email.toLowerCase().includes(search) ||
      user.companyName?.toLowerCase().includes(search) ||
      user.phone.includes(search)
    )
  }

  // Filter by user type
  if (filters.userType) {
    filtered = filtered.filter(user => user.userType === filters.userType)
  }

  // Filter by status
  if (filters.status) {
    filtered = filtered.filter(user => user.status === filters.status)
  }

  // Filter by date
  if (filters.dateFrom) {
    const fromDate = new Date(filters.dateFrom)
    filtered = filtered.filter(user => new Date(user.createdAt) >= fromDate)
  }

  return filtered.sort((a, b) => {
    // Sort pending users first, then by creation date
    if (a.status === 'PENDING' && b.status !== 'PENDING') return -1
    if (a.status !== 'PENDING' && b.status === 'PENDING') return 1
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  })
})

const getUserInitials = (name: string) => {
  if (!name) return 'U'
  return name
    .split(' ')
    .map(n => n.charAt(0))
    .join('')
    .toUpperCase()
    .substring(0, 2)
}

const getUserTypeClass = (userType: string) => {
  const classes = {
    'CORPORATE': 'bg-blue-100 text-blue-800',
    'PARTNER': 'bg-green-100 text-green-800',
    'GENERAL': 'bg-gray-100 text-gray-800'
  }
  return classes[userType as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getStatusClass = (status: string) => {
  const statusClasses = {
    'PENDING': 'status-pending',
    'APPROVED': 'status-confirmed',
    'ACTIVE': 'status-confirmed',
    'REJECTED': 'status-cancelled'
  }
  return statusClasses[status as keyof typeof statusClasses] || 'status-pending'
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

let filterTimeout: NodeJS.Timeout

const debouncedFilter = () => {
  clearTimeout(filterTimeout)
  filterTimeout = setTimeout(() => {
    // Filter is reactive, no need to call anything
  }, 300)
}

const filterUsers = () => {
  // Filters are reactive, no need to implement
}

const loadPendingUsers = async () => {
  loading.value = true
  try {
    // Get both pending users and recently processed ones for admin review
    const response = await adminApi.getUsers({ status: '' }) // Get all users
    console.log('Approval Dashboard API Response:', response)
    
    if (response.success && response.data?.users) {
      users.value = response.data.users.filter((user: User) => 
        user.userType === 'CORPORATE' || user.userType === 'PARTNER'
      )
    } else if (response.data?.users) {
      // API response format might be different
      users.value = response.data.users.filter((user: User) => 
        user.userType === 'CORPORATE' || user.userType === 'PARTNER'
      )
    } else {
      console.warn('No users data found, falling back to mock data')
      // Mock data for testing
      users.value = [
        {
          id: 1,
          name: 'Samsung Electronics',
          email: 'procurement@samsung.co.kr',
          phone: '02-2255-0114',
          userType: 'CORPORATE',
          status: 'PENDING',
          companyName: 'Samsung Electronics Co., Ltd.',
          businessNumber: '124-81-00998',
          companyAddress: 'Seoul, South Korea',
          emailVerified: true,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          pendingApproval: true
        },
        {
          id: 2,
          name: 'Busan Trading Partner',
          email: 'info@busantrading.co.kr',
          phone: '051-123-4567',
          userType: 'PARTNER',
          status: 'PENDING',
          companyName: 'Busan Trading Co.',
          businessNumber: '456-88-12345',
          partnerRegion: 'Busan',
          emailVerified: true,
          createdAt: new Date(Date.now() - 86400000).toISOString(), // 1 day ago
          updatedAt: new Date(Date.now() - 86400000).toISOString(),
          pendingApproval: true
        }
      ] as User[]
    }
  } catch (error) {
    console.error('Failed to load users:', error)
  } finally {
    loading.value = false
  }
}

const openApprovalModal = (user: User, action: 'approve' | 'reject') => {
  selectedUser.value = user
  modalAction.value = action
  approvalNotes.value = ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  selectedUser.value = null
  approvalNotes.value = ''
  processing.value = false
}

const processApproval = async () => {
  if (!selectedUser.value) return
  
  processing.value = true
  
  try {
    const data = {
      notes: approvalNotes.value.trim()
    }
    
    let response
    if (modalAction.value === 'approve') {
      response = await adminApi.approveUser(selectedUser.value.id, data)
    } else {
      response = await adminApi.rejectUser(selectedUser.value.id, {
        reason: approvalNotes.value.trim()
      })
    }
    
    if (response.success) {
      // Update the user in the list
      const userIndex = users.value.findIndex(u => u.id === selectedUser.value!.id)
      if (userIndex !== -1) {
        users.value[userIndex] = {
          ...users.value[userIndex],
          status: modalAction.value === 'approve' ? 'APPROVED' : 'REJECTED',
          approvedAt: modalAction.value === 'approve' ? new Date().toISOString() : undefined,
          rejectedAt: modalAction.value === 'reject' ? new Date().toISOString() : undefined,
          approvalNotes: modalAction.value === 'approve' ? approvalNotes.value : undefined,
          rejectionReason: modalAction.value === 'reject' ? approvalNotes.value : undefined
        }
      }
      
      console.log(`✅ User ${modalAction.value}d successfully: ${selectedUser.value.name}`)
      closeModal()
    } else {
      throw new Error(response.error || 'Failed to process approval')
    }
  } catch (error: any) {
    console.error(`Failed to ${modalAction.value} user:`, error)
    alert(`Failed to ${modalAction.value} user. ${error.message || 'Please try again.'}`)
  } finally {
    processing.value = false
  }
}

onMounted(() => {
  loadPendingUsers()
})
</script>