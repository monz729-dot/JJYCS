<template>
  <div class="order-edit-view">
    <div class="container mx-auto p-6">
      <div class="max-w-4xl mx-auto">
        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">주문 수정</h1>
            <p class="text-gray-600">주문 정보를 수정하세요</p>
          </div>
          <div class="flex space-x-3">
            <button
              @click="goBack"
              class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            >
              취소
            </button>
            <button
              @click="saveOrder"
              :disabled="loading"
              class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
              {{ loading ? '저장 중...' : '저장' }}
            </button>
          </div>
        </div>

        <!-- Order Info -->
        <div v-if="order" class="bg-white rounded-lg shadow p-6 mb-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold">주문 정보</h2>
            <span
              class="px-3 py-1 text-sm rounded-full"
              :class="getStatusClass(order.status)"
            >
              {{ getStatusText(order.status) }}
            </span>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
            <div>
              <span class="font-medium text-gray-700">주문번호:</span>
              <span class="ml-2">{{ order.orderCode }}</span>
            </div>
            <div>
              <span class="font-medium text-gray-700">주문일:</span>
              <span class="ml-2">{{ formatDate(order.createdAt) }}</span>
            </div>
            <div>
              <span class="font-medium text-gray-700">배송 방식:</span>
              <span class="ml-2">{{ order.orderType === 'air' ? '항공' : '해상' }}</span>
            </div>
            <div>
              <span class="font-medium text-gray-700">총 CBM:</span>
              <span class="ml-2">{{ calculateTotalCBM() }}m³</span>
            </div>
          </div>
        </div>

        <div v-if="order" class="space-y-6">
          <!-- Recipient Info -->
          <div class="bg-white rounded-lg shadow p-6">
            <h3 class="text-lg font-semibold mb-4">수취인 정보</h3>
            <form @submit.prevent="saveOrder">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">수취인 이름</label>
                  <input
                    v-model="order.recipientName"
                    type="text"
                    required
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">전화번호</label>
                  <input
                    v-model="order.recipientPhone"
                    type="tel"
                    required
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                <div class="md:col-span-2">
                  <label class="block text-sm font-medium text-gray-700 mb-2">배송 주소</label>
                  <textarea
                    v-model="order.recipientAddress"
                    rows="3"
                    required
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  ></textarea>
                </div>
              </div>
            </form>
          </div>

          <!-- Order Items -->
          <div class="bg-white rounded-lg shadow p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-semibold">주문 상품</h3>
              <button
                @click="addItem"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                상품 추가
              </button>
            </div>

            <div class="space-y-4">
              <div
                v-for="(item, index) in order.items"
                :key="index"
                class="border border-gray-200 rounded-lg p-4"
              >
                <div class="flex items-center justify-between mb-4">
                  <h4 class="font-medium">상품 {{ index + 1 }}</h4>
                  <button
                    v-if="order.items.length > 1"
                    @click="removeItem(index)"
                    class="text-red-600 hover:text-red-700"
                  >
                    <TrashIcon class="w-4 h-4" />
                  </button>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">상품명</label>
                    <input
                      v-model="item.productName"
                      type="text"
                      required
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">수량</label>
                    <input
                      v-model.number="item.quantity"
                      type="number"
                      min="1"
                      required
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">가격 (THB)</label>
                    <input
                      v-model.number="item.price"
                      type="number"
                      min="0"
                      step="0.01"
                      required
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                </div>

                <!-- THB Warning -->
                <div v-if="item.price > 1500" class="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                  <div class="flex items-center">
                    <ExclamationTriangleIcon class="w-5 h-5 text-yellow-600 mr-2" />
                    <p class="text-sm text-yellow-700">
                      1,500 THB 초과 상품: 수취인 추가 정보가 필요할 수 있습니다.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Order Boxes -->
          <div class="bg-white rounded-lg shadow p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-semibold">박스 정보</h3>
              <button
                @click="addBox"
                class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
              >
                박스 추가
              </button>
            </div>

            <div class="space-y-4">
              <div
                v-for="(box, index) in order.boxes"
                :key="index"
                class="border border-gray-200 rounded-lg p-4"
              >
                <div class="flex items-center justify-between mb-4">
                  <h4 class="font-medium">박스 {{ index + 1 }}</h4>
                  <button
                    v-if="order.boxes.length > 1"
                    @click="removeBox(index)"
                    class="text-red-600 hover:text-red-700"
                  >
                    <TrashIcon class="w-4 h-4" />
                  </button>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">가로 (cm)</label>
                    <input
                      v-model.number="box.width"
                      type="number"
                      min="1"
                      required
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      @input="updateCBM(index)"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">세로 (cm)</label>
                    <input
                      v-model.number="box.height"
                      type="number"
                      min="1"
                      required
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      @input="updateCBM(index)"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">높이 (cm)</label>
                    <input
                      v-model.number="box.depth"
                      type="number"
                      min="1"
                      required
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      @input="updateCBM(index)"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-2">CBM (m³)</label>
                    <input
                      :value="calculateBoxCBM(box)"
                      type="text"
                      readonly
                      class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- CBM Warning -->
            <div v-if="calculateTotalCBM() > 29" class="mt-4 p-4 bg-orange-50 border border-orange-200 rounded-lg">
              <div class="flex items-center">
                <ExclamationTriangleIcon class="w-5 h-5 text-orange-600 mr-2" />
                <div>
                  <p class="text-sm font-medium text-orange-700">CBM 29m³ 초과</p>
                  <p class="text-sm text-orange-600">자동으로 항공 배송으로 변경됩니다.</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Loading State -->
        <div v-else class="flex justify-center py-12">
          <LoadingSpinner />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import {
  TrashIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()
const toast = useToast()

// Props
const orderId = route.params.id as string

// State
const loading = ref(false)
const order = ref<any>(null)

// Methods
const loadOrder = async () => {
  try {
    // Mock order data
    order.value = {
      id: orderId,
      orderCode: `ORD-${orderId}`,
      status: 'requested',
      orderType: 'sea',
      recipientName: '홍길동',
      recipientPhone: '010-1234-5678',
      recipientAddress: '태국 방콕 수쿰빗 123',
      createdAt: '2025-08-12T10:00:00Z',
      items: [
        {
          id: '1',
          productName: '한국 과자',
          quantity: 10,
          price: 500,
          emsCode: '1905'
        }
      ],
      boxes: [
        {
          id: '1',
          width: 30,
          height: 20,
          depth: 15,
          cbm: 0.009
        }
      ]
    }
  } catch (error) {
    toast.error('주문 정보를 불러오는데 실패했습니다.')
    console.error('Load order error:', error)
  }
}

const saveOrder = async () => {
  try {
    loading.value = true
    
    // Update CBM and order type based on total volume
    const totalCBM = calculateTotalCBM()
    if (totalCBM > 29) {
      order.value.orderType = 'air'
    }
    
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    toast.success('주문이 성공적으로 수정되었습니다.')
    router.push({ name: 'OrderDetail', params: { id: orderId } })
    
  } catch (error) {
    toast.error('주문 수정 중 오류가 발생했습니다.')
    console.error('Save order error:', error)
  } finally {
    loading.value = false
  }
}

const addItem = () => {
  order.value.items.push({
    id: Date.now().toString(),
    productName: '',
    quantity: 1,
    price: 0,
    emsCode: ''
  })
}

const removeItem = (index: number) => {
  order.value.items.splice(index, 1)
}

const addBox = () => {
  order.value.boxes.push({
    id: Date.now().toString(),
    width: 30,
    height: 20,
    depth: 15,
    cbm: 0.009
  })
}

const removeBox = (index: number) => {
  order.value.boxes.splice(index, 1)
}

const calculateBoxCBM = (box: any) => {
  return ((box.width || 0) * (box.height || 0) * (box.depth || 0) / 1000000).toFixed(6)
}

const updateCBM = (index: number) => {
  const box = order.value.boxes[index]
  box.cbm = parseFloat(calculateBoxCBM(box))
}

const calculateTotalCBM = () => {
  if (!order.value?.boxes) return 0
  return order.value.boxes.reduce((total: number, box: any) => {
    return total + parseFloat(calculateBoxCBM(box))
  }, 0).toFixed(3)
}

const getStatusClass = (status: string) => {
  const classes = {
    'requested': 'bg-blue-100 text-blue-800',
    'confirmed': 'bg-green-100 text-green-800',
    'in_progress': 'bg-yellow-100 text-yellow-800',
    'shipped': 'bg-purple-100 text-purple-800',
    'delivered': 'bg-green-100 text-green-800',
    'cancelled': 'bg-red-100 text-red-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status: string) => {
  const texts = {
    'requested': '요청',
    'confirmed': '확인',
    'in_progress': '처리중',
    'shipped': '배송중',
    'delivered': '배송완료',
    'cancelled': '취소'
  }
  return texts[status as keyof typeof texts] || status
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ko-KR')
}

const goBack = () => {
  router.go(-1)
}

// Lifecycle
onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.order-edit-view {
  min-height: 100vh;
  background-color: #f8fafc;
}
</style>