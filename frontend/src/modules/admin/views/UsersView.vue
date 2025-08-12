<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">사용자 관리</h1>
        <p class="text-sm text-gray-600 mt-1">시스템의 모든 사용자를 관리합니다</p>
      </div>
      <div class="mt-4 sm:mt-0 flex space-x-3">
        <button @click="exportUsers" 
                class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50">
          <ArrowDownTrayIcon class="h-4 w-4 mr-2" />
          내보내기
        </button>
        <button @click="openCreateModal" 
                class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700">
          <PlusIcon class="h-4 w-4 mr-2" />
          사용자 생성
        </button>
      </div>
    </div>

    <!-- Filters -->
    <div class="bg-white rounded-lg shadow p-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">검색</label>
          <div class="relative">
            <MagnifyingGlassIcon class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
            <input v-model="filters.search" 
                   type="text" 
                   placeholder="이름, 이메일 검색..."
                   class="pl-10 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-blue-500" />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">역할</label>
          <select v-model="filters.role" class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-blue-500">
            <option value="">전체</option>
            <option value="individual">일반 사용자</option>
            <option value="enterprise">기업 사용자</option>
            <option value="partner">파트너</option>
            <option value="warehouse">창고</option>
            <option value="admin">관리자</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">상태</label>
          <select v-model="filters.status" class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-blue-500">
            <option value="">전체</option>
            <option value="active">활성</option>
            <option value="pending_approval">승인 대기</option>
            <option value="suspended">정지</option>
          </select>
        </div>
        <div class="flex items-end">
          <button @click="resetFilters" 
                  class="w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50">
            초기화
          </button>
        </div>
      </div>
    </div>

    <!-- Users Table -->
    <div class="bg-white rounded-lg shadow overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
        <h3 class="text-lg font-medium text-gray-900">
          사용자 목록 ({{ filteredUsers.length }}명)
        </h3>
        <div v-if="selectedUsers.length > 0" class="flex items-center space-x-2">
          <span class="text-sm text-gray-600">{{ selectedUsers.length }}명 선택됨</span>
          <button @click="bulkAction('activate')" 
                  class="px-3 py-1 text-xs bg-green-600 text-white rounded-md hover:bg-green-700">
            활성화
          </button>
          <button @click="bulkAction('deactivate')" 
                  class="px-3 py-1 text-xs bg-red-600 text-white rounded-md hover:bg-red-700">
            비활성화
          </button>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th scope="col" class="px-6 py-3 text-left">
                <input type="checkbox" 
                       :checked="allSelected" 
                       @change="toggleSelectAll"
                       class="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                사용자
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                역할
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                가입일
              </th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                최근 로그인
              </th>
              <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="user in paginatedUsers" :key="user.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap">
                <input type="checkbox" 
                       :value="user.id" 
                       v-model="selectedUsers"
                       class="rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="flex items-center">
                  <div class="h-10 w-10 flex-shrink-0">
                    <div class="h-10 w-10 rounded-full bg-gray-300 flex items-center justify-center">
                      <UserIcon class="h-6 w-6 text-gray-500" />
                    </div>
                  </div>
                  <div class="ml-4">
                    <div class="text-sm font-medium text-gray-900">{{ user.name }}</div>
                    <div class="text-sm text-gray-500">{{ user.email }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex px-2 py-1 text-xs font-medium rounded-full"
                      :class="getRoleClass(user.role)">
                  {{ getRoleText(user.role) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex px-2 py-1 text-xs font-medium rounded-full"
                      :class="getStatusClass(user.status)">
                  {{ getStatusText(user.status) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ formatDate(user.createdAt) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ user.lastLoginAt ? formatDate(user.lastLoginAt) : '없음' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <div class="flex items-center justify-end space-x-2">
                  <router-link :to="`/admin/users/${user.id}`"
                               class="text-blue-600 hover:text-blue-900">
                    상세보기
                  </router-link>
                  <button @click="editUser(user)" 
                          class="text-indigo-600 hover:text-indigo-900">
                    수정
                  </button>
                  <button @click="deleteUser(user)" 
                          class="text-red-600 hover:text-red-900">
                    삭제
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="bg-white px-4 py-3 border-t border-gray-200 sm:px-6">
        <div class="flex-1 flex justify-between sm:hidden">
          <button @click="previousPage" 
                  :disabled="currentPage === 1"
                  class="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50">
            이전
          </button>
          <button @click="nextPage" 
                  :disabled="currentPage === totalPages"
                  class="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50">
            다음
          </button>
        </div>
        <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
          <div>
            <p class="text-sm text-gray-700">
              <span class="font-medium">{{ (currentPage - 1) * itemsPerPage + 1 }}</span>
              -
              <span class="font-medium">{{ Math.min(currentPage * itemsPerPage, filteredUsers.length) }}</span>
              / 총 
              <span class="font-medium">{{ filteredUsers.length }}</span>
              개
            </p>
          </div>
          <div>
            <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
              <button @click="previousPage" 
                      :disabled="currentPage === 1"
                      class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50">
                <ChevronLeftIcon class="h-5 w-5" />
              </button>
              <button v-for="page in visiblePages" 
                      :key="page"
                      @click="goToPage(page)"
                      class="relative inline-flex items-center px-4 py-2 border text-sm font-medium"
                      :class="currentPage === page ? 
                        'z-10 bg-blue-50 border-blue-500 text-blue-600' : 
                        'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'">
                {{ page }}
              </button>
              <button @click="nextPage" 
                      :disabled="currentPage === totalPages"
                      class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50">
                <ChevronRightIcon class="h-5 w-5" />
              </button>
            </nav>
          </div>
        </div>
      </div>
    </div>

    <!-- 사용자 생성/수정 모달 -->
    <div v-if="showCreateModal" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="closeCreateModal"></div>
        <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <form @submit.prevent="submitUser">
            <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
              <h3 class="text-lg font-medium text-gray-900 mb-4">
                {{ editingUser ? '사용자 편집' : '새 사용자 생성' }}
              </h3>
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700">이름</label>
                  <input v-model="userForm.name" 
                         type="text" 
                         required 
                         class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700">이메일</label>
                  <input v-model="userForm.email" 
                         type="email" 
                         required 
                         class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700">역할</label>
                  <select v-model="userForm.role" 
                          required 
                          class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500">
                    <option value="individual">일반 사용자</option>
                    <option value="enterprise">기업 사용자</option>
                    <option value="partner">파트너</option>
                    <option value="warehouse">창고</option>
                    <option value="admin">관리자</option>
                  </select>
                </div>
                <div v-if="!editingUser">
                  <label class="block text-sm font-medium text-gray-700">임시 비밀번호</label>
                  <input v-model="userForm.password" 
                         type="password" 
                         required 
                         class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
              </div>
            </div>
            <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
              <button type="submit" 
                      :disabled="loading"
                      class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-blue-600 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:ml-3 sm:w-auto sm:text-sm disabled:opacity-50">
                {{ loading ? '저장 중...' : (editingUser ? '수정' : '생성') }}
              </button>
              <button type="button" 
                      @click="closeCreateModal"
                      class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">
                취소
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  UserIcon,
  MagnifyingGlassIcon,
  PlusIcon,
  ArrowDownTrayIcon,
  ChevronLeftIcon,
  ChevronRightIcon
} from '@heroicons/vue/24/outline'

interface User {
  id: number
  name: string
  email: string
  role: string
  status: string
  createdAt: string
  lastLoginAt?: string
  memberCode?: string
  phone?: string
}

const router = useRouter()
const authStore = useAuthStore()

// State
const loading = ref(false)
const showCreateModal = ref(false)
const editingUser = ref<User | null>(null)
const selectedUsers = ref<number[]>([])
const currentPage = ref(1)
const itemsPerPage = ref(10)

// Filters
const filters = ref({
  search: '',
  role: '',
  status: ''
})

// Form
const userForm = ref({
  name: '',
  email: '',
  role: 'individual',
  password: ''
})

// Users data
const users = ref<User[]>([])
const loadUsers = async () => {
  try {
    users.value = await authStore.getAllUsers()
  } catch (error) {
    console.error('Failed to load users:', error)
  }
}

// Computed
const filteredUsers = computed(() => {
  return users.value.filter(user => {
    const matchesSearch = !filters.value.search || 
      user.name.toLowerCase().includes(filters.value.search.toLowerCase()) ||
      user.email.toLowerCase().includes(filters.value.search.toLowerCase())
    
    const matchesRole = !filters.value.role || user.role === filters.value.role
    const matchesStatus = !filters.value.status || user.status === filters.value.status
    
    return matchesSearch && matchesRole && matchesStatus
  })
})

const totalPages = computed(() => Math.ceil(filteredUsers.value.length / itemsPerPage.value))

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredUsers.value.slice(start, end)
})

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  
  for (let i = Math.max(1, current - 2); i <= Math.min(total, current + 2); i++) {
    pages.push(i)
  }
  
  return pages
})

const allSelected = computed(() => {
  return paginatedUsers.value.length > 0 && 
    paginatedUsers.value.every(user => selectedUsers.value.includes(user.id))
})

// Methods
const resetFilters = () => {
  filters.value = {
    search: '',
    role: '',
    status: ''
  }
  currentPage.value = 1
}

const openCreateModal = () => {
  editingUser.value = null
  userForm.value = {
    name: '',
    email: '',
    role: 'individual',
    password: ''
  }
  showCreateModal.value = true
}

const closeCreateModal = () => {
  showCreateModal.value = false
  editingUser.value = null
}

const editUser = (user: User) => {
  editingUser.value = user
  userForm.value = {
    name: user.name,
    email: user.email,
    role: user.role,
    password: ''
  }
  showCreateModal.value = true
}

const submitUser = async () => {
  loading.value = true
  try {
    if (editingUser.value) {
      // Update existing user
      const index = users.value.findIndex(u => u.id === editingUser.value!.id)
      if (index !== -1) {
        users.value[index] = {
          ...users.value[index],
          ...userForm.value
        }
      }
    } else {
      // Create new user
      const newUser: User = {
        id: Math.max(...users.value.map(u => u.id)) + 1,
        ...userForm.value,
        status: 'active',
        createdAt: new Date().toISOString().split('T')[0]
      }
      users.value.unshift(newUser)
    }
    closeCreateModal()
  } catch (error) {
    console.error('Error saving user:', error)
  } finally {
    loading.value = false
  }
}

const deleteUser = (user: User) => {
  if (confirm(`정말로 ${user.name}님을 삭제하시겠습니까?`)) {
    const index = users.value.findIndex(u => u.id === user.id)
    if (index !== -1) {
      users.value.splice(index, 1)
    }
  }
}

const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedUsers.value = []
  } else {
    selectedUsers.value = paginatedUsers.value.map(user => user.id)
  }
}

const bulkAction = async (action: string) => {
  if (!selectedUsers.value.length) return
  
  const message = action === 'activate' ? '활성화' : '비활성화'
  if (!confirm(`선택한 ${selectedUsers.value.length}명의 사용자를 ${message}하시겠습니까?`)) {
    return
  }
  
  loading.value = true
  try {
    selectedUsers.value.forEach(userId => {
      const user = users.value.find(u => u.id === userId)
      if (user) {
        user.status = action === 'activate' ? 'active' : 'inactive'
      }
    })
    selectedUsers.value = []
  } catch (error) {
    console.error('Error performing bulk action:', error)
  } finally {
    loading.value = false
  }
}

const exportUsers = () => {
  // Mock export functionality
  console.log('Exporting users...')
}

const previousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const goToPage = (page: number) => {
  currentPage.value = page
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('ko-KR')
}

const getRoleClass = (role: string) => {
  const classes = {
    'individual': 'bg-gray-100 text-gray-800',
    'enterprise': 'bg-blue-100 text-blue-800',
    'partner': 'bg-green-100 text-green-800',
    'warehouse': 'bg-purple-100 text-purple-800',
    'admin': 'bg-red-100 text-red-800'
  }
  return classes[role as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getRoleText = (role: string) => {
  const texts = {
    'individual': '일반',
    'enterprise': '기업',
    'partner': '파트너',
    'warehouse': '창고',
    'admin': '관리자'
  }
  return texts[role as keyof typeof texts] || '알수없음'
}

const getStatusClass = (status: string) => {
  const classes = {
    'active': 'bg-green-100 text-green-800',
    'pending_approval': 'bg-yellow-100 text-yellow-800',
    'suspended': 'bg-red-100 text-red-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status: string) => {
  const texts = {
    'active': '활성',
    'pending_approval': '승인 대기',
    'suspended': '정지'
  }
  return texts[status as keyof typeof texts] || '알수없음'
}

// Load users on mount
onMounted(() => {
  loadUsers()
})
</script>