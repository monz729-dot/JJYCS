<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">사용자 관리</h1>
        <p class="mt-1 text-sm text-gray-500">모든 시스템 사용자와 권한 관리</p>
      </div>
      <div class="mt-4 sm:mt-0 flex items-center space-x-3">
        <div class="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium">
          {{ filteredUsers.length }}명
        </div>
        <button
          @click="loadUsers"
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
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
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
          <select v-model="filters.userType" class="input-field">
            <option value="">전체 유형</option>
            <option value="GENERAL">일반</option>
            <option value="CORPORATE">기업</option>
            <option value="PARTNER">파트너</option>
            <option value="WAREHOUSE">창고</option>
            <option value="ADMIN">관리자</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">상태</label>
          <select v-model="filters.status" class="input-field">
            <option value="">전체 상태</option>
            <option value="ACTIVE">활성</option>
            <option value="PENDING">대기</option>
            <option value="INACTIVE">비활성</option>
            <option value="REJECTED">거절</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">이메일 인증</label>
          <select v-model="filters.emailVerified" class="input-field">
            <option value="">전체</option>
            <option value="true">인증됨</option>
            <option value="false">미인증</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">가입일</label>
          <input
            v-model="filters.dateFrom"
            type="date"
            class="input-field"
          />
        </div>
      </div>
    </div>

    <!-- Users Table -->
    <div class="bg-white rounded-lg shadow-sm">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-lg font-semibold text-gray-900">All Users</h2>
      </div>

      <div v-if="loading" class="p-6">
        <div class="space-y-4">
          <div v-for="i in 5" :key="i" class="animate-pulse">
            <div class="flex items-center space-x-4 p-4">
              <div class="w-12 h-12 bg-gray-200 rounded-full"></div>
              <div class="flex-1 space-y-2">
                <div class="h-4 bg-gray-200 rounded w-1/3"></div>
                <div class="h-3 bg-gray-200 rounded w-1/2"></div>
              </div>
              <div class="flex space-x-2">
                <div class="h-8 w-16 bg-gray-200 rounded"></div>
                <div class="h-8 w-16 bg-gray-200 rounded"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="filteredUsers.length === 0" class="p-6 text-center">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"></path>
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">No users found</h3>
        <p class="mt-1 text-sm text-gray-500">Try adjusting your search filters.</p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                User
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Type
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Company
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Registered
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="user in filteredUsers" :key="user.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div class="flex-shrink-0 h-10 w-10">
                    <div class="h-10 w-10 bg-primary-500 rounded-full flex items-center justify-center">
                      <span class="text-sm font-medium text-white">
                        {{ getUserInitials(user.name) }}
                      </span>
                    </div>
                  </div>
                  <div class="ml-4">
                    <div class="text-sm font-medium text-gray-900">{{ user.name }}</div>
                    <div class="text-sm text-gray-500">{{ user.email }}</div>
                    <div v-if="user.phone" class="text-xs text-gray-400">{{ user.phone }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getUserTypeClass(user.userType)" class="status-badge text-xs">
                  {{ user.userType }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center space-x-2">
                  <span :class="getStatusClass(user.status)" class="status-badge text-xs">
                    {{ user.status }}
                  </span>
                  <span v-if="user.emailVerified" class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                    <svg class="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path>
                    </svg>
                    Verified
                  </span>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <div v-if="user.companyName">
                  <div class="font-medium">{{ user.companyName }}</div>
                  <div v-if="user.businessNumber" class="text-xs text-gray-500">{{ user.businessNumber }}</div>
                </div>
                <div v-else class="text-gray-400">-</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ formatDate(user.createdAt) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <div class="flex justify-end space-x-2">
                  <button
                    @click="viewUser(user)"
                    class="text-indigo-600 hover:text-indigo-900"
                  >
                    View
                  </button>
                  <button
                    v-if="user.status === 'ACTIVE'"
                    @click="toggleUserStatus(user)"
                    class="text-yellow-600 hover:text-yellow-900"
                  >
                    Suspend
                  </button>
                  <button
                    v-else-if="user.status === 'INACTIVE'"
                    @click="toggleUserStatus(user)"
                    class="text-green-600 hover:text-green-900"
                  >
                    Activate
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { adminApi } from '@/utils/api'
import type { User } from '@/types'

const loading = ref(true)
const users = ref<User[]>([])

const filters = reactive({
  search: '',
  userType: '',
  status: '',
  emailVerified: '',
  dateFrom: ''
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
      user.phone?.includes(search)
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

  // Filter by email verified
  if (filters.emailVerified) {
    const isVerified = filters.emailVerified === 'true'
    filtered = filtered.filter(user => user.emailVerified === isVerified)
  }

  // Filter by date
  if (filters.dateFrom) {
    const fromDate = new Date(filters.dateFrom)
    filtered = filtered.filter(user => new Date(user.createdAt) >= fromDate)
  }

  return filtered.sort((a, b) => {
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
    'GENERAL': 'bg-gray-100 text-gray-800',
    'CORPORATE': 'bg-blue-100 text-blue-800',
    'PARTNER': 'bg-green-100 text-green-800',
    'WAREHOUSE': 'bg-purple-100 text-purple-800',
    'ADMIN': 'bg-red-100 text-red-800'
  }
  return classes[userType as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getStatusClass = (status: string) => {
  const statusClasses = {
    'ACTIVE': 'status-confirmed',
    'PENDING': 'status-pending',
    'INACTIVE': 'status-cancelled',
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

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await adminApi.getUsers()
    
    if (response.success && response.data?.users) {
      users.value = response.data.users
    } else {
      // Mock data for testing
      users.value = [
        {
          id: 1,
          name: 'Administrator',
          email: 'admin@ycs.com',
          phone: '02-1234-5678',
          userType: 'ADMIN',
          status: 'ACTIVE',
          emailVerified: true,
          createdAt: '2024-01-01T00:00:00Z',
          updatedAt: '2024-01-01T00:00:00Z',
          pendingApproval: false
        },
        {
          id: 2,
          name: 'Samsung Electronics',
          email: 'procurement@samsung.co.kr',
          phone: '02-2255-0114',
          userType: 'CORPORATE',
          status: 'ACTIVE',
          companyName: 'Samsung Electronics Co., Ltd.',
          businessNumber: '124-81-00998',
          emailVerified: true,
          createdAt: '2024-08-20T00:00:00Z',
          updatedAt: '2024-08-20T00:00:00Z',
          pendingApproval: false
        },
        {
          id: 3,
          name: 'Busan Trading Partner',
          email: 'info@busantrading.co.kr',
          phone: '051-123-4567',
          userType: 'PARTNER',
          status: 'PENDING',
          companyName: 'Busan Trading Co.',
          businessNumber: '456-88-12345',
          partnerRegion: 'Busan',
          emailVerified: true,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          pendingApproval: true
        },
        {
          id: 4,
          name: 'John Doe',
          email: 'john@example.com',
          phone: '+82-10-1234-5678',
          userType: 'GENERAL',
          status: 'ACTIVE',
          emailVerified: true,
          createdAt: '2024-08-15T00:00:00Z',
          updatedAt: '2024-08-15T00:00:00Z',
          pendingApproval: false
        },
        {
          id: 5,
          name: 'Warehouse Staff',
          email: 'warehouse@ycs.com',
          phone: '02-9876-5432',
          userType: 'WAREHOUSE',
          status: 'ACTIVE',
          emailVerified: true,
          createdAt: '2024-07-01T00:00:00Z',
          updatedAt: '2024-07-01T00:00:00Z',
          pendingApproval: false
        }
      ] as User[]
    }
  } catch (error) {
    console.error('Failed to load users:', error)
  } finally {
    loading.value = false
  }
}

const viewUser = (user: User) => {
  // TODO: Implement user detail view or modal
  console.log('Viewing user:', user)
  alert(`User Details:\nName: ${user.name}\nEmail: ${user.email}\nType: ${user.userType}\nStatus: ${user.status}`)
}

const toggleUserStatus = async (user: User) => {
  const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const action = newStatus === 'ACTIVE' ? 'activate' : 'suspend'
  
  if (confirm(`Are you sure you want to ${action} ${user.name}?`)) {
    try {
      const response = await adminApi.updateUser(user.id, { status: newStatus })
      
      if (response.success) {
        // Update user in list
        const userIndex = users.value.findIndex(u => u.id === user.id)
        if (userIndex !== -1) {
          users.value[userIndex] = {
            ...users.value[userIndex],
            status: newStatus
          }
        }
        console.log(`✅ User ${action}d successfully: ${user.name}`)
      } else {
        throw new Error(response.error || `Failed to ${action} user`)
      }
    } catch (error: any) {
      console.error(`Failed to ${action} user:`, error)
      alert(`Failed to ${action} user. ${error.message || 'Please try again.'}`)
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>