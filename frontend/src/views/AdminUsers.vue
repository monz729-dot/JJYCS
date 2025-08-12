<template>
  <div class="admin-users">
    <div class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">사용자 관리</h1>
          <p class="text-gray-600 mt-1">전체 사용자 계정을 관리하고 모니터링하세요</p>
        </div>
        <router-link :to="{ name: 'AdminDashboard' }" class="btn btn-secondary">
          ← 관리자 대시보드
        </router-link>
      </div>
    </div>

    <!-- Filters -->
    <div class="bg-white p-6 rounded-lg shadow-sm border border-gray-200 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">검색</label>
          <input 
            v-model="filters.search"
            type="text" 
            placeholder="이름, 이메일 검색..."
            class="form-input"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">역할</label>
          <select v-model="filters.role" class="form-input">
            <option value="">전체 역할</option>
            <option value="individual">개인</option>
            <option value="enterprise">기업</option>
            <option value="partner">파트너</option>
            <option value="warehouse">창고</option>
            <option value="admin">관리자</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">상태</label>
          <select v-model="filters.status" class="form-input">
            <option value="">전체 상태</option>
            <option value="active">활성</option>
            <option value="pending_approval">승인대기</option>
            <option value="suspended">정지</option>
          </select>
        </div>
        <div class="flex items-end">
          <button @click="clearFilters" class="btn btn-secondary w-full">필터 초기화</button>
        </div>
      </div>
    </div>

    <!-- User Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
      <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
        <div class="flex items-center">
          <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center mr-3">
            <svg class="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm font-medium text-gray-600">전체 사용자</p>
            <p class="text-xl font-bold text-gray-900">{{ userStats.total }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
        <div class="flex items-center">
          <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center mr-3">
            <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm font-medium text-gray-600">활성 사용자</p>
            <p class="text-xl font-bold text-green-600">{{ userStats.active }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
        <div class="flex items-center">
          <div class="w-8 h-8 bg-orange-100 rounded-full flex items-center justify-center mr-3">
            <svg class="w-4 h-4 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm font-medium text-gray-600">승인 대기</p>
            <p class="text-xl font-bold text-orange-600">{{ userStats.pending }}</p>
          </div>
        </div>
      </div>
      <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
        <div class="flex items-center">
          <div class="w-8 h-8 bg-red-100 rounded-full flex items-center justify-center mr-3">
            <svg class="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728L5.636 5.636m12.728 12.728L18.364 5.636"></path>
            </svg>
          </div>
          <div>
            <p class="text-sm font-medium text-gray-600">정지됨</p>
            <p class="text-xl font-bold text-red-600">{{ userStats.suspended }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Users Table -->
    <div class="bg-white rounded-lg shadow-sm border border-gray-200">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-lg font-semibold text-gray-900">사용자 목록</h2>
      </div>
      
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">사용자</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">역할</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">가입일</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">마지막 로그인</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">작업</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="user in filteredUsers" :key="user.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div class="flex-shrink-0 h-10 w-10">
                    <div class="h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center">
                      <span class="text-sm font-medium text-blue-600">
                        {{ getUserInitials(user.name) }}
                      </span>
                    </div>
                  </div>
                  <div class="ml-4">
                    <div class="text-sm font-medium text-gray-900">{{ user.name }}</div>
                    <div class="text-sm text-gray-500">{{ user.email }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getRoleBadgeClass(user.role)" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium">
                  {{ getRoleText(user.role) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span :class="getStatusBadgeClass(user.status)" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium">
                  {{ getStatusText(user.status) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {{ formatDate(user.createdAt) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ user.lastLoginAt || '없음' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <div class="flex space-x-2">
                  <button 
                    v-if="user.status === 'pending_approval'"
                    @click="approveUser(user.id)"
                    class="text-green-600 hover:text-green-900"
                  >
                    승인
                  </button>
                  <button 
                    v-if="user.status === 'active'"
                    @click="suspendUser(user.id)"
                    class="text-red-600 hover:text-red-900"
                  >
                    정지
                  </button>
                  <button 
                    v-if="user.status === 'suspended'"
                    @click="activateUser(user.id)"
                    class="text-green-600 hover:text-green-900"
                  >
                    활성화
                  </button>
                  <button class="text-blue-600 hover:text-blue-900">상세</button>
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
import { ref, computed, onMounted } from 'vue'

interface User {
  id: string
  name: string
  email: string
  role: 'individual' | 'enterprise' | 'partner' | 'warehouse' | 'admin'
  status: 'active' | 'pending_approval' | 'suspended'
  createdAt: string
  lastLoginAt?: string
  companyName?: string
}

const filters = ref({
  search: '',
  role: '',
  status: ''
})

const users = ref<User[]>([
  {
    id: '1',
    name: '관리자',
    email: 'admin@ycs.com',
    role: 'admin',
    status: 'active',
    createdAt: '2024-01-01',
    lastLoginAt: '2024-01-15 14:30'
  },
  {
    id: '2',
    name: '김철수',
    email: 'user@example.com',
    role: 'individual',
    status: 'active',
    createdAt: '2024-01-05',
    lastLoginAt: '2024-01-14 09:15'
  },
  {
    id: '3',
    name: '박영희',
    email: 'enterprise@company.com',
    role: 'enterprise',
    status: 'pending_approval',
    createdAt: '2024-01-10',
    companyName: '(주)테크놀로지'
  },
  {
    id: '4',
    name: '이상무',
    email: 'warehouse@ycs.com',
    role: 'warehouse',
    status: 'active',
    createdAt: '2024-01-02',
    lastLoginAt: '2024-01-15 13:45'
  },
  {
    id: '5',
    name: '최민준',
    email: 'partner@logistics.com',
    role: 'partner',
    status: 'suspended',
    createdAt: '2024-01-08',
    lastLoginAt: '2024-01-12 16:20'
  }
])

const userStats = computed(() => ({
  total: users.value.length,
  active: users.value.filter(u => u.status === 'active').length,
  pending: users.value.filter(u => u.status === 'pending_approval').length,
  suspended: users.value.filter(u => u.status === 'suspended').length
}))

const filteredUsers = computed(() => {
  let filtered = users.value

  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    filtered = filtered.filter(user => 
      user.name.toLowerCase().includes(search) || 
      user.email.toLowerCase().includes(search)
    )
  }

  if (filters.value.role) {
    filtered = filtered.filter(user => user.role === filters.value.role)
  }

  if (filters.value.status) {
    filtered = filtered.filter(user => user.status === filters.value.status)
  }

  return filtered
})

const getUserInitials = (name: string) => {
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
}

const getRoleBadgeClass = (role: string) => {
  const classes = {
    individual: 'bg-blue-100 text-blue-800',
    enterprise: 'bg-purple-100 text-purple-800',
    partner: 'bg-green-100 text-green-800',
    warehouse: 'bg-yellow-100 text-yellow-800',
    admin: 'bg-red-100 text-red-800'
  }
  return classes[role as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getRoleText = (role: string) => {
  const texts = {
    individual: '개인',
    enterprise: '기업',
    partner: '파트너',
    warehouse: '창고',
    admin: '관리자'
  }
  return texts[role as keyof typeof texts] || role
}

const getStatusBadgeClass = (status: string) => {
  const classes = {
    active: 'bg-green-100 text-green-800',
    pending_approval: 'bg-orange-100 text-orange-800',
    suspended: 'bg-red-100 text-red-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status: string) => {
  const texts = {
    active: '활성',
    pending_approval: '승인대기',
    suspended: '정지됨'
  }
  return texts[status as keyof typeof texts] || status
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR')
}

const clearFilters = () => {
  filters.value = {
    search: '',
    role: '',
    status: ''
  }
}

const approveUser = (userId: string) => {
  const user = users.value.find(u => u.id === userId)
  if (user) {
    user.status = 'active'
    alert(`${user.name} 사용자가 승인되었습니다.`)
  }
}

const suspendUser = (userId: string) => {
  const user = users.value.find(u => u.id === userId)
  if (user) {
    user.status = 'suspended'
    alert(`${user.name} 사용자가 정지되었습니다.`)
  }
}

const activateUser = (userId: string) => {
  const user = users.value.find(u => u.id === userId)
  if (user) {
    user.status = 'active'
    alert(`${user.name} 사용자가 활성화되었습니다.`)
  }
}

onMounted(() => {
  console.log('관리자 사용자 관리 화면 로드됨')
})
</script>

<style scoped>
.admin-users {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .admin-users {
    padding: 1rem;
  }
}
</style>