<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 헤더 -->
    <div class="bg-white shadow-sm border-b">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="py-6">
          <h1 class="text-2xl font-bold text-gray-900">일괄 등록 관리</h1>
          <p class="mt-2 text-sm text-gray-600">수취인과 품목 정보를 CSV 파일로 일괄 등록하고 관리할 수 있습니다.</p>
        </div>
      </div>
    </div>

    <!-- 탭 메뉴 -->
    <div class="bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <nav class="flex space-x-8" aria-label="Tabs">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="activeTab = tab.id"
            :class="[
              activeTab === tab.id
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
              'whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm'
            ]"
          >
            {{ tab.name }}
          </button>
        </nav>
      </div>
    </div>

    <!-- 컨텐츠 영역 -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 수취인 관리 탭 -->
      <div v-if="activeTab === 'recipients'" class="space-y-6">
        <!-- 업로드 섹션 -->
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-medium text-gray-900">수취인 정보 업로드</h3>
            <button
              @click="downloadRecipientTemplate"
              class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              📄 템플릿 다운로드
            </button>
          </div>
          
          <div class="border-2 border-dashed border-gray-300 rounded-lg p-6">
            <div class="text-center">
              <svg class="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48">
                <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
              <div class="mt-4">
                <label for="recipient-file" class="cursor-pointer">
                  <span class="mt-2 block text-sm font-medium text-gray-900">CSV 파일을 선택하거나 드래그해서 놓으세요</span>
                  <input
                    id="recipient-file"
                    ref="recipientFileInput"
                    type="file"
                    accept=".csv"
                    @change="handleRecipientFileUpload"
                    class="sr-only"
                  />
                </label>
                <p class="mt-2 text-xs text-gray-500">CSV 파일만 지원됩니다</p>
              </div>
            </div>
          </div>

          <!-- 업로드 결과 -->
          <div v-if="recipientUploadResult" class="mt-4">
            <div :class="[
              recipientUploadResult.errorCount > 0 ? 'bg-yellow-50 border-yellow-200' : 'bg-green-50 border-green-200',
              'border rounded-md p-4'
            ]">
              <div class="flex">
                <div class="flex-shrink-0">
                  <svg v-if="recipientUploadResult.errorCount === 0" class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                  </svg>
                  <svg v-else class="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                  </svg>
                </div>
                <div class="ml-3">
                  <h3 class="text-sm font-medium text-gray-900">업로드 결과</h3>
                  <div class="mt-2 text-sm text-gray-700">
                    <p>총 {{ recipientUploadResult.totalRows }}개 중 {{ recipientUploadResult.successCount }}개 성공, {{ recipientUploadResult.errorCount }}개 오류</p>
                  </div>
                  <div v-if="recipientUploadResult.errors.length > 0" class="mt-4">
                    <h4 class="text-sm font-medium text-gray-900 mb-2">오류 목록:</h4>
                    <ul class="text-sm text-gray-700 space-y-1 max-h-32 overflow-y-auto">
                      <li v-for="error in recipientUploadResult.errors" :key="error" class="text-red-600">• {{ error }}</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 수취인 목록 -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">등록된 수취인 목록</h3>
          </div>
          <div class="p-6">
            <div v-if="recipientLoading" class="text-center py-4">
              <div class="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
              <span class="ml-2">로딩 중...</span>
            </div>
            <div v-else-if="recipients.length === 0" class="text-center py-8 text-gray-500">
              등록된 수취인이 없습니다.
            </div>
            <div v-else>
              <div class="overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">수취인명</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">전화번호</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">주소</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">국가</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">등록일</th>
                      <th class="relative px-6 py-3"><span class="sr-only">Actions</span></th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-gray-200">
                    <tr v-for="recipient in recipients" :key="recipient.id">
                      <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {{ recipient.recipientName }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {{ recipient.recipientPhone || '-' }}
                      </td>
                      <td class="px-6 py-4 text-sm text-gray-500 max-w-xs truncate">
                        {{ recipient.recipientAddress }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {{ recipient.country }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {{ formatDate(recipient.createdAt) }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <button
                          @click="deleteRecipient(recipient.id)"
                          class="text-red-600 hover:text-red-900"
                        >
                          삭제
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              
              <!-- 페이징 -->
              <div v-if="recipientPagination.totalPages > 1" class="mt-6 flex items-center justify-between">
                <div class="text-sm text-gray-700">
                  총 {{ recipientPagination.totalElements }}개 중 {{ ((recipientPagination.currentPage) * recipientPagination.size) + 1 }}-{{ Math.min((recipientPagination.currentPage + 1) * recipientPagination.size, recipientPagination.totalElements) }}개 표시
                </div>
                <div class="flex space-x-2">
                  <button
                    @click="loadRecipients(recipientPagination.currentPage - 1)"
                    :disabled="recipientPagination.currentPage === 0"
                    class="px-3 py-2 text-sm border rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    이전
                  </button>
                  <button
                    @click="loadRecipients(recipientPagination.currentPage + 1)"
                    :disabled="recipientPagination.currentPage >= recipientPagination.totalPages - 1"
                    class="px-3 py-2 text-sm border rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    다음
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 품목 관리 탭 -->
      <div v-if="activeTab === 'items'" class="space-y-6">
        <!-- 업로드 섹션 -->
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-medium text-gray-900">품목 정보 업로드</h3>
            <button
              @click="downloadItemTemplate"
              class="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              📄 템플릿 다운로드
            </button>
          </div>
          
          <div class="border-2 border-dashed border-gray-300 rounded-lg p-6">
            <div class="text-center">
              <svg class="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48">
                <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
              <div class="mt-4">
                <label for="item-file" class="cursor-pointer">
                  <span class="mt-2 block text-sm font-medium text-gray-900">CSV 파일을 선택하거나 드래그해서 놓으세요</span>
                  <input
                    id="item-file"
                    ref="itemFileInput"
                    type="file"
                    accept=".csv"
                    @change="handleItemFileUpload"
                    class="sr-only"
                  />
                </label>
                <p class="mt-2 text-xs text-gray-500">CSV 파일만 지원됩니다</p>
              </div>
            </div>
          </div>

          <!-- 업로드 결과 -->
          <div v-if="itemUploadResult" class="mt-4">
            <div :class="[
              itemUploadResult.errorCount > 0 ? 'bg-yellow-50 border-yellow-200' : 'bg-green-50 border-green-200',
              'border rounded-md p-4'
            ]">
              <div class="flex">
                <div class="flex-shrink-0">
                  <svg v-if="itemUploadResult.errorCount === 0" class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                  </svg>
                  <svg v-else class="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                  </svg>
                </div>
                <div class="ml-3">
                  <h3 class="text-sm font-medium text-gray-900">업로드 결과</h3>
                  <div class="mt-2 text-sm text-gray-700">
                    <p>총 {{ itemUploadResult.totalRows }}개 중 {{ itemUploadResult.successCount }}개 성공, {{ itemUploadResult.errorCount }}개 오류</p>
                  </div>
                  <div v-if="itemUploadResult.errors.length > 0" class="mt-4">
                    <h4 class="text-sm font-medium text-gray-900 mb-2">오류 목록:</h4>
                    <ul class="text-sm text-gray-700 space-y-1 max-h-32 overflow-y-auto">
                      <li v-for="error in itemUploadResult.errors" :key="error" class="text-red-600">• {{ error }}</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 품목 목록 -->
        <div class="bg-white rounded-lg shadow">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">등록된 품목 목록</h3>
          </div>
          <div class="p-6">
            <div v-if="itemLoading" class="text-center py-4">
              <div class="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
              <span class="ml-2">로딩 중...</span>
            </div>
            <div v-else-if="items.length === 0" class="text-center py-8 text-gray-500">
              등록된 품목이 없습니다.
            </div>
            <div v-else>
              <div class="overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
                  <thead class="bg-gray-50">
                    <tr>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">HS코드</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">품목명</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">영문명</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">기본수량</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">기본단가</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">카테고리</th>
                      <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">등록일</th>
                      <th class="relative px-6 py-3"><span class="sr-only">Actions</span></th>
                    </tr>
                  </thead>
                  <tbody class="bg-white divide-y divide-gray-200">
                    <tr v-for="item in items" :key="item.id">
                      <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {{ item.hsCode }}
                      </td>
                      <td class="px-6 py-4 text-sm text-gray-900 max-w-xs">
                        {{ item.description }}
                      </td>
                      <td class="px-6 py-4 text-sm text-gray-500 max-w-xs">
                        {{ item.englishName }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {{ item.defaultQuantity }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {{ item.defaultUnitPrice ? `${Number(item.defaultUnitPrice).toLocaleString()} THB` : '-' }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {{ item.category || '-' }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {{ formatDate(item.createdAt) }}
                      </td>
                      <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <button
                          @click="deleteItem(item.id)"
                          class="text-red-600 hover:text-red-900"
                        >
                          삭제
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              
              <!-- 페이징 -->
              <div v-if="itemPagination.totalPages > 1" class="mt-6 flex items-center justify-between">
                <div class="text-sm text-gray-700">
                  총 {{ itemPagination.totalElements }}개 중 {{ ((itemPagination.currentPage) * itemPagination.size) + 1 }}-{{ Math.min((itemPagination.currentPage + 1) * itemPagination.size, itemPagination.totalElements) }}개 표시
                </div>
                <div class="flex space-x-2">
                  <button
                    @click="loadItems(itemPagination.currentPage - 1)"
                    :disabled="itemPagination.currentPage === 0"
                    class="px-3 py-2 text-sm border rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    이전
                  </button>
                  <button
                    @click="loadItems(itemPagination.currentPage + 1)"
                    :disabled="itemPagination.currentPage >= itemPagination.totalPages - 1"
                    class="px-3 py-2 text-sm border rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    다음
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 탭 관리
const activeTab = ref('recipients')
const tabs = [
  { id: 'recipients', name: '수취인 관리' },
  { id: 'items', name: '품목 관리' }
]

// 수취인 관련 상태
const recipients = ref([])
const recipientLoading = ref(false)
const recipientUploadResult = ref(null)
const recipientPagination = ref({
  currentPage: 0,
  totalPages: 0,
  totalElements: 0,
  size: 20
})

// 품목 관련 상태
const items = ref([])
const itemLoading = ref(false)
const itemUploadResult = ref(null)
const itemPagination = ref({
  currentPage: 0,
  totalPages: 0,
  totalElements: 0,
  size: 20
})

const apiBaseUrl = computed(() => import.meta.env.VITE_API_BASE_URL || '/api')

const authHeaders = computed(() => ({
  'Authorization': `Bearer ${authStore.token}`,
  'Content-Type': 'application/json'
}))

const loadRecipients = async (page = 0) => {
  recipientLoading.value = true
  try {
    const response = await fetch(`${apiBaseUrl.value}/bulk/recipients/list?page=${page}&size=${recipientPagination.value.size}`, {
      headers: authHeaders.value
    })
    const result = await response.json()
    
    if (result.success) {
      recipients.value = result.data.recipients || []
      recipientPagination.value = {
        currentPage: result.data.currentPage || 0,
        totalPages: result.data.totalPages || 0,
        totalElements: result.data.totalElements || 0,
        size: result.data.size || 20
      }
    }
  } catch (error) {
    console.error('수취인 목록 로딩 실패:', error)
  } finally {
    recipientLoading.value = false
  }
}

const handleRecipientFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  try {
    const response = await fetch(`${apiBaseUrl.value}/bulk/recipients/upload`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      },
      body: formData
    })
    
    const result = await response.json()
    recipientUploadResult.value = result.data || result
    
    if (result.success) {
      // 업로드 성공 후 목록 새로고침
      await loadRecipients()
    }
  } catch (error) {
    console.error('파일 업로드 실패:', error)
    recipientUploadResult.value = {
      errorCount: 1,
      errors: ['파일 업로드 중 오류가 발생했습니다.']
    }
  }
  
  // 파일 입력 초기화
  event.target.value = ''
}

const downloadRecipientTemplate = async () => {
  try {
    const response = await fetch(`${apiBaseUrl.value}/bulk/recipients/template`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    })
    
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'recipient_template.csv'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('템플릿 다운로드 실패:', error)
  }
}

const deleteRecipient = async (id) => {
  if (!confirm('이 수취인 정보를 삭제하시겠습니까?')) return
  
  try {
    const response = await fetch(`${apiBaseUrl.value}/bulk/recipients/${id}`, {
      method: 'DELETE',
      headers: authHeaders.value
    })
    
    if (response.ok) {
      await loadRecipients()
    }
  } catch (error) {
    console.error('수취인 삭제 실패:', error)
  }
}

// 품목 관련 함수들
const loadItems = async (page = 0) => {
  itemLoading.value = true
  try {
    const response = await fetch(`${apiBaseUrl.value}/bulk/items/list?page=${page}&size=${itemPagination.value.size}`, {
      headers: authHeaders.value
    })
    const result = await response.json()
    
    if (result.success) {
      items.value = result.data.items || []
      itemPagination.value = {
        currentPage: result.data.currentPage || 0,
        totalPages: result.data.totalPages || 0,
        totalElements: result.data.totalElements || 0,
        size: result.data.size || 20
      }
    }
  } catch (error) {
    console.error('품목 목록 로딩 실패:', error)
  } finally {
    itemLoading.value = false
  }
}

const handleItemFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  try {
    const response = await fetch(`${apiBaseUrl.value}/bulk/items/upload`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      },
      body: formData
    })
    
    const result = await response.json()
    itemUploadResult.value = result.data || result
    
    if (result.success) {
      // 업로드 성공 후 목록 새로고침
      await loadItems()
    }
  } catch (error) {
    console.error('파일 업로드 실패:', error)
    itemUploadResult.value = {
      errorCount: 1,
      errors: ['파일 업로드 중 오류가 발생했습니다.']
    }
  }
  
  // 파일 입력 초기화
  event.target.value = ''
}

const downloadItemTemplate = async () => {
  try {
    const response = await fetch(`${apiBaseUrl.value}/bulk/items/template`, {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    })
    
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'item_template.csv'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('템플릿 다운로드 실패:', error)
  }
}

const deleteItem = async (id) => {
  if (!confirm('이 품목 정보를 삭제하시겠습니까?')) return
  
  try {
    const response = await fetch(`${apiBaseUrl.value}/bulk/items/${id}`, {
      method: 'DELETE',
      headers: authHeaders.value
    })
    
    if (response.ok) {
      await loadItems()
    }
  } catch (error) {
    console.error('품목 삭제 실패:', error)
  }
}

// 유틸리티 함수
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR') + ' ' + date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
}

// 초기화
onMounted(() => {
  loadRecipients()
  loadItems()
})
</script>