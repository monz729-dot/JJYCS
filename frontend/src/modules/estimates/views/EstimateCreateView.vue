<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Header -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">{{ $t('estimates.create.title') }}</h1>
            <p class="mt-1 text-sm text-gray-600">{{ $t('estimates.create.subtitle') }}</p>
          </div>
          <div class="flex space-x-3">
            <button
              type="button"
              class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
              @click="goBack"
            >
              <ArrowLeftIcon class="h-4 w-4 mr-2" />
              {{ $t('common.back') }}
            </button>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- Left Column: Order Info & Cost Breakdown -->
        <div class="space-y-6">
          <!-- Order Information -->
          <div class="bg-white shadow rounded-lg p-6">
            <h2 class="text-lg font-medium text-gray-900 mb-4">{{ $t('estimates.create.order_info') }}</h2>
            
            <div v-if="isLoading" class="animate-pulse">
              <div class="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
              <div class="h-4 bg-gray-200 rounded w-1/2 mb-2"></div>
              <div class="h-4 bg-gray-200 rounded w-2/3"></div>
            </div>
            
            <div v-else-if="orderInfo" class="space-y-3">
              <div class="flex justify-between">
                <span class="text-sm text-gray-600">{{ $t('estimates.create.order_code') }}</span>
                <span class="text-sm font-medium text-gray-900">{{ orderInfo.orderCode }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-600">{{ $t('estimates.create.shipping_method') }}</span>
                <span class="text-sm font-medium text-gray-900">
                  {{ orderInfo.orderType === 'sea' ? '해상배송' : '항공배송' }}
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-600">총 CBM</span>
                <span class="text-sm font-medium text-gray-900">{{ orderInfo.totalCbm?.toFixed(3) }} m³</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-600">총 중량</span>
                <span class="text-sm font-medium text-gray-900">{{ orderInfo.totalWeight?.toFixed(2) }} kg</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-600">품목 수</span>
                <span class="text-sm font-medium text-gray-900">{{ orderInfo.itemCount }}개</span>
              </div>
            </div>
          </div>

          <!-- Customer Information -->
          <div class="bg-white shadow rounded-lg p-6">
            <h2 class="text-lg font-medium text-gray-900 mb-4">{{ $t('estimates.create.customer_info') }}</h2>
            
            <div v-if="orderInfo?.customer" class="space-y-3">
              <div class="flex justify-between">
                <span class="text-sm text-gray-600">고객명</span>
                <span class="text-sm font-medium text-gray-900">{{ orderInfo.customer.name }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-600">회사</span>
                <span class="text-sm font-medium text-gray-900">{{ orderInfo.customer.company || '-' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-600">이메일</span>
                <span class="text-sm font-medium text-gray-900">{{ orderInfo.customer.email }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-gray-600">수취인</span>
                <span class="text-sm font-medium text-gray-900">{{ orderInfo.recipientName }}</span>
              </div>
            </div>
          </div>

          <!-- Cost Calculation Mode -->
          <div class="bg-white shadow rounded-lg p-6">
            <h2 class="text-lg font-medium text-gray-900 mb-4">{{ $t('estimates.create.cost_calculation') }}</h2>
            
            <div class="flex space-x-4 mb-4">
              <button
                type="button"
                class="flex-1 py-2 px-4 text-sm font-medium rounded-md border"
                :class="calculationMode === 'auto' ? 'bg-blue-50 border-blue-500 text-blue-700' : 'bg-white border-gray-300 text-gray-700'"
                @click="setCalculationMode('auto')"
              >
                {{ $t('estimates.create.auto_calculate') }}
              </button>
              <button
                type="button"
                class="flex-1 py-2 px-4 text-sm font-medium rounded-md border"
                :class="calculationMode === 'manual' ? 'bg-blue-50 border-blue-500 text-blue-700' : 'bg-white border-gray-300 text-gray-700'"
                @click="setCalculationMode('manual')"
              >
                {{ $t('estimates.create.manual_input') }}
              </button>
            </div>

            <button
              v-if="calculationMode === 'auto'"
              type="button"
              class="w-full inline-flex justify-center items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              :disabled="isCalculating"
              @click="calculateEstimate"
            >
              <CalculatorIcon class="h-4 w-4 mr-2" />
              {{ isCalculating ? $t('estimates.create.calculating') : $t('estimates.create.calculate_estimate') }}
            </button>
          </div>
        </div>

        <!-- Right Column: Cost Breakdown Form -->
        <div class="space-y-6">
          <!-- Cost Breakdown -->
          <div class="bg-white shadow rounded-lg p-6">
            <h2 class="text-lg font-medium text-gray-900 mb-4">{{ $t('estimates.create.cost_breakdown') }}</h2>
            
            <div class="space-y-4">
              <!-- Shipping Cost -->
              <div>
                <label class="block text-sm font-medium text-gray-700">
                  {{ $t('estimates.create.shipping_cost') }}
                  <span class="text-gray-400 text-xs ml-1">{{ $t('estimates.create.shipping_cost_help') }}</span>
                </label>
                <div class="mt-1 relative rounded-md shadow-sm">
                  <input
                    v-model.number="costs.shippingCost"
                    type="number"
                    step="0.01"
                    min="0"
                    class="block w-full pl-3 pr-16 border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    :readonly="calculationMode === 'auto'"
                  />
                  <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                    <span class="text-gray-500 sm:text-sm">THB</span>
                  </div>
                </div>
              </div>

              <!-- Local Delivery -->
              <div>
                <label class="block text-sm font-medium text-gray-700">
                  {{ $t('estimates.create.local_delivery') }}
                  <span class="text-gray-400 text-xs ml-1">{{ $t('estimates.create.local_delivery_help') }}</span>
                </label>
                <div class="mt-1 relative rounded-md shadow-sm">
                  <input
                    v-model.number="costs.localDelivery"
                    type="number"
                    step="0.01"
                    min="0"
                    class="block w-full pl-3 pr-16 border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    :readonly="calculationMode === 'auto'"
                  />
                  <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                    <span class="text-gray-500 sm:text-sm">THB</span>
                  </div>
                </div>
              </div>

              <!-- Handling Fee -->
              <div>
                <label class="block text-sm font-medium text-gray-700">
                  {{ $t('estimates.create.handling_fee') }}
                  <span class="text-gray-400 text-xs ml-1">{{ $t('estimates.create.handling_fee_help') }}</span>
                </label>
                <div class="mt-1 relative rounded-md shadow-sm">
                  <input
                    v-model.number="costs.handlingFee"
                    type="number"
                    step="0.01"
                    min="0"
                    class="block w-full pl-3 pr-16 border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    :readonly="calculationMode === 'auto'"
                  />
                  <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                    <span class="text-gray-500 sm:text-sm">THB</span>
                  </div>
                </div>
              </div>

              <!-- Repacking Fee -->
              <div>
                <label class="flex items-center">
                  <input
                    v-model="includeRepacking"
                    type="checkbox"
                    class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                  />
                  <span class="ml-2 text-sm font-medium text-gray-700">{{ $t('estimates.create.repacking_fee') }}</span>
                </label>
                <div v-if="includeRepacking" class="mt-2 relative rounded-md shadow-sm">
                  <input
                    v-model.number="costs.repackingFee"
                    type="number"
                    step="0.01"
                    min="0"
                    class="block w-full pl-3 pr-16 border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    :readonly="calculationMode === 'auto'"
                  />
                  <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                    <span class="text-gray-500 sm:text-sm">THB</span>
                  </div>
                </div>
              </div>

              <!-- Customs Fee -->
              <div>
                <label class="block text-sm font-medium text-gray-700">
                  {{ $t('estimates.create.customs_fee') }}
                  <span class="text-gray-400 text-xs ml-1">{{ $t('estimates.create.customs_fee_help') }}</span>
                </label>
                <div class="mt-1 relative rounded-md shadow-sm">
                  <input
                    v-model.number="costs.customsFee"
                    type="number"
                    step="0.01"
                    min="0"
                    class="block w-full pl-3 pr-16 border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    :readonly="calculationMode === 'auto'"
                  />
                  <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                    <span class="text-gray-500 sm:text-sm">THB</span>
                  </div>
                </div>
              </div>

              <!-- Insurance -->
              <div>
                <label class="flex items-center">
                  <input
                    v-model="includeInsurance"
                    type="checkbox"
                    class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                  />
                  <span class="ml-2 text-sm font-medium text-gray-700">{{ $t('estimates.create.insurance') }}</span>
                  <span class="text-gray-400 text-xs ml-1">{{ $t('estimates.create.insurance_help') }}</span>
                </label>
                <div v-if="includeInsurance" class="mt-2 relative rounded-md shadow-sm">
                  <input
                    v-model.number="costs.insurance"
                    type="number"
                    step="0.01"
                    min="0"
                    class="block w-full pl-3 pr-16 border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    :readonly="calculationMode === 'auto'"
                  />
                  <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                    <span class="text-gray-500 sm:text-sm">THB</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Additional Services -->
          <div class="bg-white shadow rounded-lg p-6">
            <h2 class="text-lg font-medium text-gray-900 mb-4">{{ $t('estimates.create.additional_services') }}</h2>
            
            <div class="space-y-3">
              <label class="flex items-center">
                <input
                  v-model="additionalServices.urgentProcessing"
                  type="checkbox"
                  class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                />
                <span class="ml-2 text-sm text-gray-700">{{ $t('estimates.create.urgent_processing') }}</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="additionalServices.fragileHandling"
                  type="checkbox"
                  class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                />
                <span class="ml-2 text-sm text-gray-700">{{ $t('estimates.create.fragile_handling') }}</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="additionalServices.photoService"
                  type="checkbox"
                  class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                />
                <span class="ml-2 text-sm text-gray-700">{{ $t('estimates.create.photo_service') }}</span>
              </label>
              
              <label class="flex items-center">
                <input
                  v-model="additionalServices.consolidation"
                  type="checkbox"
                  class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                />
                <span class="ml-2 text-sm text-gray-700">{{ $t('estimates.create.consolidation') }}</span>
              </label>
            </div>
          </div>

          <!-- Total Calculation -->
          <div class="bg-white shadow rounded-lg p-6">
            <h2 class="text-lg font-medium text-gray-900 mb-4">{{ $t('estimates.create.total_amount') }}</h2>
            
            <div class="space-y-2">
              <div class="flex justify-between text-sm">
                <span class="text-gray-600">{{ $t('estimates.create.base_cost') }}</span>
                <span class="text-gray-900">{{ formatCurrency(baseCost) }}</span>
              </div>
              <div v-if="additionalCost > 0" class="flex justify-between text-sm">
                <span class="text-gray-600">{{ $t('estimates.create.additional_cost') }}</span>
                <span class="text-gray-900">{{ formatCurrency(additionalCost) }}</span>
              </div>
              <div v-if="discount > 0" class="flex justify-between text-sm">
                <span class="text-gray-600">{{ $t('estimates.create.discount') }}</span>
                <span class="text-red-600">-{{ formatCurrency(discount) }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-600">{{ $t('estimates.create.tax') }}</span>
                <span class="text-gray-900">{{ formatCurrency(taxAmount) }}</span>
              </div>
              <div class="border-t pt-2">
                <div class="flex justify-between text-lg font-bold">
                  <span class="text-gray-900">{{ $t('estimates.create.total_amount') }}</span>
                  <span class="text-blue-600">{{ formatCurrency(totalAmount) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Estimate Details -->
          <div class="bg-white shadow rounded-lg p-6">
            <h2 class="text-lg font-medium text-gray-900 mb-4">{{ $t('estimates.create.estimate_details') }}</h2>
            
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700">{{ $t('estimates.create.version') }}</label>
                <select
                  v-model="estimateVersion"
                  class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                >
                  <option value="1">{{ $t('estimates.create.first_estimate') }}</option>
                  <option value="2">{{ $t('estimates.create.second_estimate') }}</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700">{{ $t('estimates.create.validity_period') }}</label>
                <select
                  v-model="validityDays"
                  class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                >
                  <option value="7">{{ $t('estimates.create.days', { count: 7 }) }}</option>
                  <option value="14">{{ $t('estimates.create.days', { count: 14 }) }}</option>
                  <option value="30">{{ $t('estimates.create.days', { count: 30 }) }}</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700">{{ $t('estimates.create.special_notes') }}</label>
                <textarea
                  v-model="specialNotes"
                  rows="4"
                  class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                  :placeholder="$t('estimates.create.special_notes_placeholder')"
                />
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex space-x-4">
            <button
              type="button"
              class="flex-1 inline-flex justify-center items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
              :disabled="isSaving"
              @click="saveDraft"
            >
              <DocumentIcon class="h-4 w-4 mr-2" />
              {{ $t('estimates.create.save_draft') }}
            </button>
            <button
              type="button"
              class="flex-1 inline-flex justify-center items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              :disabled="isSaving || !isValidEstimate"
              @click="createEstimate"
            >
              <PlusIcon class="h-4 w-4 mr-2" />
              {{ isSaving ? $t('estimates.create.creating') : $t('estimates.create.create_estimate') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useToast } from '@/composables/useToast'
import { SpringBootEstimatesService } from '@/services/estimatesApiService'
import { SpringBootOrdersService } from '@/services/ordersApiService'
import type { EstimateCreateRequest } from '@/services/estimatesApiService'
import {
  ArrowLeftIcon,
  CalculatorIcon,
  DocumentIcon,
  PlusIcon
} from '@heroicons/vue/24/outline'

// Composables
const router = useRouter()
const route = useRoute()
const { showToast } = useToast()

// Props
const orderId = computed(() => route.params.orderId)

// State
const isLoading = ref(false)
const isCalculating = ref(false)
const isSaving = ref(false)

const orderInfo = ref(null)
const calculationMode = ref('auto')

// Cost breakdown
const costs = ref({
  shippingCost: 0,
  localDelivery: 0,
  handlingFee: 0,
  repackingFee: 0,
  customsFee: 0,
  insurance: 0
})

// Additional options
const includeRepacking = ref(false)
const includeInsurance = ref(false)
const additionalServices = ref({
  urgentProcessing: false,
  fragileHandling: false,
  photoService: false,
  consolidation: false
})

// Estimate details
const estimateVersion = ref(1)
const validityDays = ref(14)
const specialNotes = ref('')

// Mock order data
const mockOrderInfo = {
  id: 1,
  orderCode: 'ORD-2024-0001',
  orderType: 'sea',
  totalCbm: 18.5,
  totalWeight: 125.8,
  itemCount: 5,
  totalValue: 2500,
  recipientName: '김철수',
  recipientCountry: 'Thailand',
  customer: {
    name: '이영희',
    company: 'ABC무역',
    email: 'customer@abc.com'
  }
}

// Computed
const baseCost = computed(() => {
  return costs.value.shippingCost + costs.value.localDelivery + costs.value.handlingFee + costs.value.customsFee
})

const additionalCost = computed(() => {
  let additional = 0
  
  if (includeRepacking.value) {
    additional += costs.value.repackingFee
  }
  
  if (includeInsurance.value) {
    additional += costs.value.insurance
  }
  
  // Additional services cost
  if (additionalServices.value.urgentProcessing) additional += 500
  if (additionalServices.value.fragileHandling) additional += 300
  if (additionalServices.value.photoService) additional += 200
  if (additionalServices.value.consolidation) additional += 400
  
  return additional
})

const discount = ref(0) // Could be set based on customer tier, promotions, etc.

const taxAmount = computed(() => {
  const subtotal = baseCost.value + additionalCost.value - discount.value
  return Math.round(subtotal * 0.07) // 7% VAT
})

const totalAmount = computed(() => {
  return baseCost.value + additionalCost.value - discount.value + taxAmount.value
})

const isValidEstimate = computed(() => {
  return totalAmount.value > 0 && orderInfo.value
})

// Methods
const loadOrderInfo = async () => {
  isLoading.value = true
  
  try {
    const result = await SpringBootOrdersService.getOrder(Number(orderId.value))
    if (result.success && result.data) {
      orderInfo.value = {
        id: result.data.id,
        orderCode: result.data.orderNumber || `ORD-${result.data.id}`,
        orderType: result.data.orderType || 'sea',
        totalCbm: result.data.totalCBM || 0,
        totalWeight: result.data.totalWeight || 0,
        itemCount: result.data.items?.length || 0,
        totalValue: result.data.totalAmount || 0,
        recipientName: result.data.recipientName,
        recipientCountry: result.data.recipientCountry,
        customer: {
          name: result.data.customerName || 'Unknown',
          company: result.data.customerCompany || '',
          email: result.data.customerEmail || ''
        }
      }
    } else {
      // Fallback to mock data if API fails
      orderInfo.value = mockOrderInfo
      showToast('주문 정보를 불러오는데 실패했습니다. 목 데이터를 사용합니다.', 'warning')
    }
  } catch (error: any) {
    console.error('Order info load error:', error)
    orderInfo.value = mockOrderInfo
    showToast('주문 정보를 불러오는데 실패했습니다. 목 데이터를 사용합니다.', 'warning')
  } finally {
    isLoading.value = false
  }
}

const setCalculationMode = (mode: 'auto' | 'manual') => {
  calculationMode.value = mode
  if (mode === 'auto') {
    calculateEstimate()
  }
}

const calculateEstimate = async () => {
  if (!orderInfo.value) return
  
  isCalculating.value = true
  
  try {
    const result = await SpringBootEstimatesService.calculateEstimate(
      orderInfo.value.id,
      orderInfo.value.orderType,
      undefined, // carrier
      'standard' // service level
    )
    
    if (result.success && result.data) {
      costs.value.shippingCost = result.data.shippingCost
      costs.value.localDelivery = result.data.localShippingCost
      costs.value.repackingFee = result.data.repackingCost
      
      // Calculate additional costs based on order
      const order = orderInfo.value
      costs.value.handlingFee = Math.round(order.itemCount * 150 + order.totalValue * 0.02)
      costs.value.customsFee = Math.round(order.totalValue * 0.015)
      
      // Auto-set options based on order characteristics
      if (order.itemCount > 3) {
        includeRepacking.value = true
      }
      
      if (order.totalValue > 2000) {
        includeInsurance.value = true
        costs.value.insurance = Math.round(order.totalValue * 0.01)
      }
      
      showToast('견적이 자동으로 계산되었습니다.', 'success')
    } else {
      throw new Error(result.error || '견적 계산에 실패했습니다')
    }
  } catch (error: any) {
    console.error('Estimate calculation error:', error)
    showToast(error.message || '견적 계산에 실패했습니다', 'error')
  } finally {
    isCalculating.value = false
  }
}

const saveDraft = async () => {
  isSaving.value = true
  
  try {
    const estimateData: EstimateCreateRequest = {
      shippingMethod: orderInfo.value?.orderType || 'sea',
      serviceLevel: 'standard',
      notes: `드래프트 - ${specialNotes.value}\n추가 서비스: ${Object.entries(additionalServices.value).filter(([_, v]) => v).map(([k]) => k).join(', ')}`
    }
    
    const result = await SpringBootEstimatesService.createEstimate(orderInfo.value!.id, estimateData)
    
    if (result.success) {
      showToast('견적 초안이 저장되었습니다.', 'success')
    } else {
      throw new Error(result.error || '견적 저장에 실패했습니다')
    }
  } catch (error: any) {
    console.error('Draft save error:', error)
    showToast(error.message || '견적 저장에 실패했습니다', 'error')
  } finally {
    isSaving.value = false
  }
}

const createEstimate = async () => {
  if (!isValidEstimate.value) return
  
  isSaving.value = true
  
  try {
    const estimateData: EstimateCreateRequest = {
      shippingMethod: orderInfo.value?.orderType || 'sea',
      serviceLevel: additionalServices.value.urgentProcessing ? 'urgent' : 'standard',
      notes: [
        specialNotes.value,
        `버전: ${estimateVersion.value}차 견적`,
        `유효기간: ${validityDays.value}일`,
        `추가 서비스: ${Object.entries(additionalServices.value).filter(([_, v]) => v).map(([k]) => k).join(', ')}`,
        `예상 총비용: ${formatCurrency(totalAmount.value)}`
      ].filter(Boolean).join('\n')
    }
    
    const result = await SpringBootEstimatesService.createEstimate(orderInfo.value!.id, estimateData)
    
    if (result.success && result.data) {
      showToast('견적서가 성공적으로 생성되었습니다.', 'success')
      router.push({ name: 'EstimateDetail', params: { id: result.data.id } })
    } else {
      throw new Error(result.error || '견적 생성에 실패했습니다')
    }
  } catch (error: any) {
    console.error('Estimate creation error:', error)
    showToast(error.message || '견적 생성에 실패했습니다', 'error')
  } finally {
    isSaving.value = false
  }
}

const goBack = () => {
  router.go(-1)
}

const formatCurrency = (amount: number, currency: string = 'THB') => {
  return new Intl.NumberFormat('th-TH', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 0
  }).format(amount)
}

// Watchers
watch(includeRepacking, (newValue) => {
  if (!newValue) {
    costs.value.repackingFee = 0
  }
})

watch(includeInsurance, (newValue) => {
  if (!newValue) {
    costs.value.insurance = 0
  }
})

// Lifecycle
onMounted(() => {
  loadOrderInfo()
})
</script>

<style scoped>
/* Custom styles */
</style>