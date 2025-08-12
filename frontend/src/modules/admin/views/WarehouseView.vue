<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">창고 관리</h1>
        <p class="text-sm text-gray-600 mt-1">창고 운영 현황과 설정을 관리합니다</p>
      </div>
      <div class="mt-4 sm:mt-0 flex space-x-3">
        <button @click="exportData" 
                class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50">
          <ArrowDownTrayIcon class="h-4 w-4 mr-2" />
          데이터 내보내기
        </button>
        <button @click="openAddWarehouseModal" 
                class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700">
          <PlusIcon class="h-4 w-4 mr-2" />
          창고 추가
        </button>
      </div>
    </div>

    <!-- Overview Stats -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div v-for="stat in warehouseStats" :key="stat.title" class="bg-white rounded-lg shadow p-6">
        <div class="flex items-center">
          <div class="flex-shrink-0">
            <component :is="stat.icon" class="h-8 w-8" :class="stat.iconColor" />
          </div>
          <div class="ml-5">
            <p class="text-sm font-medium text-gray-500">{{ stat.title }}</p>
            <p class="text-2xl font-semibold text-gray-900">{{ stat.value }}</p>
            <p class="text-xs" :class="stat.changeType === 'increase' ? 'text-green-600' : 'text-red-600'">
              {{ stat.change }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Warehouse Locations -->
    <div class="bg-white rounded-lg shadow">
      <div class="px-6 py-4 border-b border-gray-200">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-medium text-gray-900">창고 현황</h3>
          <div class="flex space-x-2">
            <button @click="refreshWarehouses" 
                    class="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200">
              새로고침
            </button>
          </div>
        </div>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                창고 정보
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                위치
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                용량
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                현재 재고
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                담당자
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                작업
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="warehouse in warehouses" :key="warehouse.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 whitespace-nowrap">
                <div>
                  <div class="text-sm font-medium text-gray-900">{{ warehouse.name }}</div>
                  <div class="text-sm text-gray-500">{{ warehouse.code }}</div>
                </div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-900">{{ warehouse.location }}</div>
                <div class="text-sm text-gray-500">{{ warehouse.address }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-900">{{ warehouse.capacity }} m³</div>
                <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
                  <div class="bg-blue-500 h-2 rounded-full" 
                       :style="{ width: warehouse.utilizationRate + '%' }"></div>
                </div>
                <div class="text-xs text-gray-500 mt-1">{{ warehouse.utilizationRate }}% 사용중</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-900">{{ warehouse.currentStock }} 박스</div>
                <div class="text-sm text-gray-500">{{ warehouse.pendingInbound }} 입고 예정</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="inline-flex px-2 py-1 text-xs font-medium rounded-full"
                      :class="getWarehouseStatusClass(warehouse.status)">
                  {{ getWarehouseStatusText(warehouse.status) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <div class="text-sm text-gray-900">{{ warehouse.manager }}</div>
                <div class="text-sm text-gray-500">{{ warehouse.contact }}</div>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                <div class="flex items-center justify-end space-x-2">
                  <button @click="viewWarehouse(warehouse)" 
                          class="text-blue-600 hover:text-blue-900">
                    상세
                  </button>
                  <button @click="editWarehouse(warehouse)" 
                          class="text-indigo-600 hover:text-indigo-900">
                    편집
                  </button>
                  <button @click="manageInventory(warehouse)" 
                          class="text-green-600 hover:text-green-900">
                    재고관리
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Recent Activities -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Recent Scans -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900">최근 스캔 활동</h3>
        </div>
        <div class="divide-y divide-gray-200 max-h-64 overflow-y-auto">
          <div v-for="scan in recentScans" :key="scan.id" class="px-6 py-4">
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div class="flex-shrink-0">
                  <component :is="getScanIcon(scan.type)" class="h-5 w-5" :class="getScanIconColor(scan.type)" />
                </div>
                <div>
                  <p class="text-sm font-medium text-gray-900">{{ scan.orderNumber }}</p>
                  <p class="text-sm text-gray-500">{{ scan.warehouseName }}</p>
                </div>
              </div>
              <div class="text-right">
                <p class="text-sm text-gray-900">{{ getScanTypeText(scan.type) }}</p>
                <p class="text-xs text-gray-500">{{ formatTime(scan.timestamp) }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Inventory Alerts -->
      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-medium text-gray-900">재고 알림</h3>
        </div>
        <div class="divide-y divide-gray-200 max-h-64 overflow-y-auto">
          <div v-for="alert in inventoryAlerts" :key="alert.id" class="px-6 py-4">
            <div class="flex items-start space-x-3">
              <div class="flex-shrink-0 mt-0.5">
                <component :is="alert.icon" class="h-5 w-5" :class="alert.iconColor" />
              </div>
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900">{{ alert.title }}</p>
                <p class="text-sm text-gray-500">{{ alert.message }}</p>
                <p class="text-xs text-gray-400 mt-1">{{ formatTime(alert.timestamp) }}</p>
              </div>
              <div class="flex-shrink-0">
                <button @click="resolveAlert(alert)" 
                        class="text-blue-600 hover:text-blue-900 text-sm">
                  처리
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Warehouse Performance -->
    <div class="bg-white rounded-lg shadow p-6">
      <h3 class="text-lg font-medium text-gray-900 mb-4">창고별 성능 지표</h3>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div v-for="performance in warehousePerformance" :key="performance.warehouse" 
             class="border border-gray-200 rounded-lg p-4">
          <h4 class="font-medium text-gray-900 mb-3">{{ performance.warehouse }}</h4>
          <div class="space-y-3">
            <div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">입고 처리율</span>
                <span class="font-medium">{{ performance.inboundRate }}%</span>
              </div>
              <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
                <div class="bg-green-500 h-2 rounded-full" 
                     :style="{ width: performance.inboundRate + '%' }"></div>
              </div>
            </div>
            <div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">출고 처리율</span>
                <span class="font-medium">{{ performance.outboundRate }}%</span>
              </div>
              <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
                <div class="bg-blue-500 h-2 rounded-full" 
                     :style="{ width: performance.outboundRate + '%' }"></div>
              </div>
            </div>
            <div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-500">정확도</span>
                <span class="font-medium">{{ performance.accuracy }}%</span>
              </div>
              <div class="w-full bg-gray-200 rounded-full h-2 mt-1">
                <div class="bg-purple-500 h-2 rounded-full" 
                     :style="{ width: performance.accuracy + '%' }"></div>
              </div>
            </div>
          </div>
          <div class="mt-4 pt-3 border-t border-gray-200">
            <div class="flex justify-between text-sm">
              <span class="text-gray-500">오늘 처리량</span>
              <span class="font-medium">{{ performance.todayProcessed }}건</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Warehouse Modal -->
    <div v-if="showAddWarehouseModal" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="closeAddWarehouseModal"></div>
        <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <form @submit.prevent="submitWarehouse">
            <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
              <h3 class="text-lg font-medium text-gray-900 mb-4">새 창고 추가</h3>
              <div class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700">창고명</label>
                    <input v-model="warehouseForm.name" 
                           type="text" 
                           required 
                           class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700">창고 코드</label>
                    <input v-model="warehouseForm.code" 
                           type="text" 
                           required 
                           class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700">위치</label>
                  <input v-model="warehouseForm.location" 
                         type="text" 
                         required 
                         class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700">주소</label>
                  <textarea v-model="warehouseForm.address" 
                            rows="2"
                            class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500"></textarea>
                </div>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700">용량 (m³)</label>
                    <input v-model.number="warehouseForm.capacity" 
                           type="number" 
                           required 
                           class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700">상태</label>
                    <select v-model="warehouseForm.status" 
                            class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500">
                      <option value="active">운영중</option>
                      <option value="maintenance">점검중</option>
                      <option value="inactive">비운영</option>
                    </select>
                  </div>
                </div>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700">담당자</label>
                    <input v-model="warehouseForm.manager" 
                           type="text" 
                           required 
                           class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700">연락처</label>
                    <input v-model="warehouseForm.contact" 
                           type="text" 
                           class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
              </div>
            </div>
            <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
              <button type="submit" 
                      :disabled="loading"
                      class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-blue-600 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:ml-3 sm:w-auto sm:text-sm disabled:opacity-50">
                {{ loading ? '추가 중...' : '추가' }}
              </button>
              <button type="button" 
                      @click="closeAddWarehouseModal"
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  BuildingStorefrontIcon,
  CubeIcon,
  TruckIcon,
  ExclamationTriangleIcon,
  ArrowDownTrayIcon,
  PlusIcon,
  ArrowRightIcon,
  ArrowLeftIcon,
  ClockIcon,
  CheckCircleIcon,
  XCircleIcon,
  InformationCircleIcon
} from '@heroicons/vue/24/outline'

interface Warehouse {
  id: number
  name: string
  code: string
  location: string
  address: string
  capacity: number
  currentStock: number
  pendingInbound: number
  utilizationRate: number
  status: string
  manager: string
  contact: string
}

const router = useRouter()

// State
const loading = ref(false)
const showAddWarehouseModal = ref(false)

const warehouseForm = ref({
  name: '',
  code: '',
  location: '',
  address: '',
  capacity: 0,
  status: 'active',
  manager: '',
  contact: ''
})

// Stats
const warehouseStats = ref([
  {
    title: '총 창고',
    value: '12',
    change: '+2 이번달',
    changeType: 'increase',
    icon: BuildingStorefrontIcon,
    iconColor: 'text-blue-600'
  },
  {
    title: '총 재고',
    value: '2,847',
    change: '+156 오늘',
    changeType: 'increase',
    icon: CubeIcon,
    iconColor: 'text-green-600'
  },
  {
    title: '입고 대기',
    value: '89',
    change: '-12 어제대비',
    changeType: 'decrease',
    icon: TruckIcon,
    iconColor: 'text-yellow-600'
  },
  {
    title: '용량 사용률',
    value: '67%',
    change: '+3% 이번주',
    changeType: 'increase',
    icon: ExclamationTriangleIcon,
    iconColor: 'text-purple-600'
  }
])

// Mock data
const warehouses = ref<Warehouse[]>([
  {
    id: 1,
    name: 'YCS 메인 창고',
    code: 'YCS-MAIN',
    location: '방콕',
    address: '123 Warehouse District, Bangkok, Thailand',
    capacity: 5000,
    currentStock: 2847,
    pendingInbound: 156,
    utilizationRate: 67,
    status: 'active',
    manager: '김창고',
    contact: '+66-2-123-4567'
  },
  {
    id: 2,
    name: 'YCS 보조 창고',
    code: 'YCS-SUB1',
    location: '치앙마이',
    address: '456 Storage Ave, Chiang Mai, Thailand',
    capacity: 3000,
    currentStock: 1245,
    pendingInbound: 67,
    utilizationRate: 45,
    status: 'active',
    manager: '박물류',
    contact: '+66-53-987-6543'
  },
  {
    id: 3,
    name: 'YCS 임시 창고',
    code: 'YCS-TEMP',
    location: '푸켓',
    address: '789 Temp Storage Rd, Phuket, Thailand',
    capacity: 1500,
    currentStock: 890,
    pendingInbound: 23,
    utilizationRate: 78,
    status: 'maintenance',
    manager: '이관리',
    contact: '+66-76-555-0123'
  }
])

const recentScans = ref([
  {
    id: 1,
    orderNumber: 'YCS240001',
    warehouseName: 'YCS 메인 창고',
    type: 'inbound',
    timestamp: '2024-01-20T14:30:00'
  },
  {
    id: 2,
    orderNumber: 'YCS240002',
    warehouseName: 'YCS 메인 창고',
    type: 'outbound',
    timestamp: '2024-01-20T14:25:00'
  },
  {
    id: 3,
    orderNumber: 'YCS240003',
    warehouseName: 'YCS 보조 창고',
    type: 'hold',
    timestamp: '2024-01-20T14:20:00'
  },
  {
    id: 4,
    orderNumber: 'YCS240004',
    warehouseName: 'YCS 메인 창고',
    type: 'mixbox',
    timestamp: '2024-01-20T14:15:00'
  }
])

const inventoryAlerts = ref([
  {
    id: 1,
    title: '용량 부족 경고',
    message: 'YCS 임시 창고의 사용률이 78%에 도달했습니다.',
    timestamp: '2024-01-20T14:30:00',
    icon: ExclamationTriangleIcon,
    iconColor: 'text-yellow-500'
  },
  {
    id: 2,
    title: '입고 지연',
    message: '주문 #YCS240005의 입고가 예상보다 지연되고 있습니다.',
    timestamp: '2024-01-20T14:00:00',
    icon: ClockIcon,
    iconColor: 'text-orange-500'
  },
  {
    id: 3,
    title: '재고 확인 필요',
    message: 'YCS 보조 창고에서 재고 불일치가 발견되었습니다.',
    timestamp: '2024-01-20T13:45:00',
    icon: InformationCircleIcon,
    iconColor: 'text-blue-500'
  }
])

const warehousePerformance = ref([
  {
    warehouse: 'YCS 메인 창고',
    inboundRate: 94,
    outboundRate: 87,
    accuracy: 98,
    todayProcessed: 156
  },
  {
    warehouse: 'YCS 보조 창고',
    inboundRate: 89,
    outboundRate: 92,
    accuracy: 96,
    todayProcessed: 89
  },
  {
    warehouse: 'YCS 임시 창고',
    inboundRate: 76,
    outboundRate: 82,
    accuracy: 94,
    todayProcessed: 34
  }
])

// Methods
const openAddWarehouseModal = () => {
  warehouseForm.value = {
    name: '',
    code: '',
    location: '',
    address: '',
    capacity: 0,
    status: 'active',
    manager: '',
    contact: ''
  }
  showAddWarehouseModal.value = true
}

const closeAddWarehouseModal = () => {
  showAddWarehouseModal.value = false
}

const submitWarehouse = async () => {
  loading.value = true
  try {
    // Mock warehouse creation
    const newWarehouse: Warehouse = {
      id: Math.max(...warehouses.value.map(w => w.id)) + 1,
      ...warehouseForm.value,
      currentStock: 0,
      pendingInbound: 0,
      utilizationRate: 0
    }
    warehouses.value.push(newWarehouse)
    closeAddWarehouseModal()
  } catch (error) {
    console.error('Error creating warehouse:', error)
  } finally {
    loading.value = false
  }
}

const exportData = () => {
  console.log('Exporting warehouse data...')
}

const refreshWarehouses = () => {
  console.log('Refreshing warehouse data...')
}

const viewWarehouse = (warehouse: Warehouse) => {
  console.log('View warehouse:', warehouse.id)
}

const editWarehouse = (warehouse: Warehouse) => {
  console.log('Edit warehouse:', warehouse.id)
}

const manageInventory = (warehouse: Warehouse) => {
  console.log('Manage inventory for warehouse:', warehouse.id)
}

const resolveAlert = (alert: any) => {
  console.log('Resolve alert:', alert.id)
  const index = inventoryAlerts.value.findIndex(a => a.id === alert.id)
  if (index !== -1) {
    inventoryAlerts.value.splice(index, 1)
  }
}

const formatTime = (timestamp: string) => {
  return new Date(timestamp).toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getWarehouseStatusClass = (status: string) => {
  const classes = {
    'active': 'bg-green-100 text-green-800',
    'maintenance': 'bg-yellow-100 text-yellow-800',
    'inactive': 'bg-red-100 text-red-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getWarehouseStatusText = (status: string) => {
  const texts = {
    'active': '운영중',
    'maintenance': '점검중',
    'inactive': '비운영'
  }
  return texts[status as keyof typeof texts] || '알수없음'
}

const getScanIcon = (type: string) => {
  const icons = {
    'inbound': ArrowRightIcon,
    'outbound': ArrowLeftIcon,
    'hold': ClockIcon,
    'mixbox': CubeIcon
  }
  return icons[type as keyof typeof icons] || InformationCircleIcon
}

const getScanIconColor = (type: string) => {
  const colors = {
    'inbound': 'text-green-500',
    'outbound': 'text-blue-500',
    'hold': 'text-yellow-500',
    'mixbox': 'text-purple-500'
  }
  return colors[type as keyof typeof colors] || 'text-gray-500'
}

const getScanTypeText = (type: string) => {
  const texts = {
    'inbound': '입고',
    'outbound': '출고',
    'hold': '보류',
    'mixbox': '믹스박스'
  }
  return texts[type as keyof typeof texts] || '알수없음'
}
</script>