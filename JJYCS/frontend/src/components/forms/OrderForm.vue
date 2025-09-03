<template>
  <div class="order-form-container">
    <form @submit.prevent="submitOrder" class="order-form">
      <div class="form-section">
        <h3 class="section-title">주문 정보</h3>
        
        <!-- 주문 메모 -->
        <div class="form-group">
          <label for="note" class="form-label">주문 메모 (선택사항)</label>
          <textarea
            id="note"
            v-model="orderForm.note"
            class="form-input"
            rows="3"
            maxlength="1000"
            placeholder="주문에 대한 특별한 요청사항이 있으시면 입력해주세요"
          ></textarea>
          <div class="character-count">{{ orderForm.note?.length || 0 }} / 1000</div>
        </div>
      </div>

      <!-- 주문 품목 섹션 -->
      <div class="form-section">
        <h3 class="section-title">
          주문 품목
          <span class="item-count">({{ orderForm.items.length }}개)</span>
        </h3>
        
        <!-- 품목 목록 -->
        <div class="order-items">
          <div
            v-for="(item, index) in orderForm.items"
            :key="index"
            class="order-item"
          >
            <div class="item-header">
              <span class="item-number">{{ index + 1 }}.</span>
              <button
                type="button"
                @click="removeItem(index)"
                class="remove-item-btn"
                :disabled="orderForm.items.length <= 1"
                title="품목 삭제"
              >
                ×
              </button>
            </div>
            
            <div class="item-fields">
              <div class="form-group">
                <label :for="`item-name-${index}`" class="form-label">상품명 *</label>
                <input
                  :id="`item-name-${index}`"
                  v-model="item.name"
                  type="text"
                  class="form-input"
                  :class="{ 'error': itemErrors[index]?.name }"
                  maxlength="200"
                  placeholder="상품명을 입력하세요"
                  @blur="validateItem(index)"
                  required
                />
                <div v-if="itemErrors[index]?.name" class="error-message">
                  {{ itemErrors[index].name }}
                </div>
              </div>
              
              <div class="form-row">
                <div class="form-group half-width">
                  <label :for="`item-qty-${index}`" class="form-label">수량 *</label>
                  <input
                    :id="`item-qty-${index}`"
                    v-model.number="item.qty"
                    type="number"
                    class="form-input"
                    :class="{ 'error': itemErrors[index]?.qty }"
                    min="1"
                    max="9999"
                    placeholder="1"
                    @blur="validateItem(index)"
                    @input="calculateItemAmount(index)"
                    required
                  />
                  <div v-if="itemErrors[index]?.qty" class="error-message">
                    {{ itemErrors[index].qty }}
                  </div>
                </div>
                
                <div class="form-group half-width">
                  <label :for="`item-price-${index}`" class="form-label">단가 *</label>
                  <input
                    :id="`item-price-${index}`"
                    v-model.number="item.unitPrice"
                    type="number"
                    class="form-input"
                    :class="{ 'error': itemErrors[index]?.unitPrice }"
                    min="0"
                    step="0.01"
                    placeholder="0"
                    @blur="validateItem(index)"
                    @input="calculateItemAmount(index)"
                    required
                  />
                  <div v-if="itemErrors[index]?.unitPrice" class="error-message">
                    {{ itemErrors[index].unitPrice }}
                  </div>
                </div>
              </div>
              
              <!-- 품목별 금액 표시 -->
              <div class="item-total">
                품목 금액: <span class="amount">{{ formatCurrency(getItemAmount(item)) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 품목 추가 버튼 -->
        <button
          type="button"
          @click="addItem"
          class="add-item-btn"
          :disabled="orderForm.items.length >= 10"
        >
          <span class="plus-icon">+</span>
          품목 추가
        </button>
        <div v-if="orderForm.items.length >= 10" class="info-message">
          최대 10개 품목까지 추가할 수 있습니다.
        </div>
      </div>

      <!-- 주문 요약 -->
      <div class="form-section order-summary">
        <h3 class="section-title">주문 요약</h3>
        <div class="summary-content">
          <div class="summary-row">
            <span>총 품목 수:</span>
            <span>{{ orderForm.items.length }}개</span>
          </div>
          <div class="summary-row">
            <span>총 수량:</span>
            <span>{{ getTotalQuantity() }}개</span>
          </div>
          <div class="summary-row total-amount">
            <span>총 주문 금액:</span>
            <span class="amount">{{ formatCurrency(getTotalAmount()) }}</span>
          </div>
        </div>
      </div>

      <!-- 제출 버튼 -->
      <div class="form-actions">
        <button
          type="button"
          @click="saveDraft"
          class="btn btn-secondary"
          :disabled="isLoading || !isValidForm()"
        >
          임시저장
        </button>
        <button
          type="submit"
          class="btn btn-primary"
          :disabled="isLoading || !isValidForm()"
        >
          {{ isLoading ? '처리중...' : '주문 제출' }}
        </button>
      </div>
      
      <!-- 에러 메시지 -->
      <div v-if="formError" class="form-error">
        {{ formError }}
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ordersApi } from '@/utils/api'

const router = useRouter()
const authStore = useAuthStore()

// 상태 관리
const isLoading = ref(false)
const formError = ref('')

// 주문 폼 데이터
const orderForm = reactive({
  note: '',
  currency: 'KRW',
  items: [
    {
      name: '',
      qty: 1,
      unitPrice: 0,
      productId: null
    }
  ]
})

// 품목별 에러
const itemErrors = ref([])

// 초기화
onMounted(() => {
  // 로그인 상태 확인
  if (!authStore.user) {
    router.push('/login')
    return
  }
  
  // 권한 확인 - PENDING 상태면 접근 차단
  if (authStore.user.status === 'PENDING') {
    formError.value = '승인 대기 중인 계정은 주문을 생성할 수 없습니다. 승인 완료 후 이용해 주세요.'
    return
  }
  
  if (['REJECTED', 'SUSPENDED'].includes(authStore.user.status)) {
    formError.value = '현재 계정 상태로는 주문을 생성할 수 없습니다.'
    return
  }
  
  // 에러 배열 초기화
  resetItemErrors()
})

// 품목 추가
const addItem = () => {
  if (orderForm.items.length < 10) {
    orderForm.items.push({
      name: '',
      qty: 1,
      unitPrice: 0,
      productId: null
    })
    resetItemErrors()
  }
}

// 품목 삭제
const removeItem = (index) => {
  if (orderForm.items.length > 1) {
    orderForm.items.splice(index, 1)
    resetItemErrors()
  }
}

// 에러 배열 리셋
const resetItemErrors = () => {
  itemErrors.value = orderForm.items.map(() => ({}))
}

// 품목별 금액 계산
const calculateItemAmount = (index) => {
  const item = orderForm.items[index]
  // 실시간 계산은 getItemAmount에서 처리
  validateItem(index)
}

// 품목 금액 계산
const getItemAmount = (item) => {
  const qty = Number(item.qty) || 0
  const price = Number(item.unitPrice) || 0
  return qty * price
}

// 총 수량 계산
const getTotalQuantity = () => {
  return orderForm.items.reduce((total, item) => {
    return total + (Number(item.qty) || 0)
  }, 0)
}

// 총 금액 계산
const getTotalAmount = () => {
  return orderForm.items.reduce((total, item) => {
    return total + getItemAmount(item)
  }, 0)
}

// 통화 포맷팅
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: orderForm.currency,
    minimumFractionDigits: 0
  }).format(amount)
}

// 품목 유효성 검사
const validateItem = (index) => {
  const item = orderForm.items[index]
  const errors = {}
  
  if (!item.name || item.name.trim().length === 0) {
    errors.name = '상품명을 입력해주세요'
  } else if (item.name.length > 200) {
    errors.name = '상품명은 200자 이하로 입력해주세요'
  }
  
  if (!item.qty || item.qty < 1) {
    errors.qty = '수량은 1 이상이어야 합니다'
  } else if (item.qty > 9999) {
    errors.qty = '수량은 9999 이하로 입력해주세요'
  }
  
  if (item.unitPrice === null || item.unitPrice === undefined || item.unitPrice < 0) {
    errors.unitPrice = '단가는 0 이상이어야 합니다'
  }
  
  itemErrors.value[index] = errors
  return Object.keys(errors).length === 0
}

// 전체 폼 유효성 검사
const isValidForm = () => {
  let isValid = true
  
  // 모든 품목 검증
  orderForm.items.forEach((_, index) => {
    if (!validateItem(index)) {
      isValid = false
    }
  })
  
  // 최소 1개 품목 필요
  if (orderForm.items.length === 0) {
    isValid = false
  }
  
  // 총 금액이 0보다 큰지 확인
  if (getTotalAmount() <= 0) {
    isValid = false
  }
  
  return isValid
}

// 임시저장
const saveDraft = async () => {
  if (!isValidForm()) {
    formError.value = '입력 정보를 확인해주세요'
    return
  }
  
  isLoading.value = true
  formError.value = ''
  
  try {
    const requestData = {
      note: orderForm.note || null,
      currency: orderForm.currency,
      totalAmount: getTotalAmount(),
      items: orderForm.items.map(item => ({
        name: item.name.trim(),
        qty: Number(item.qty),
        unitPrice: Number(item.unitPrice),
        productId: item.productId
      }))
    }
    
    const response = await ordersApi.createOrder(requestData)
    
    if (response.success) {
      // 임시저장 성공
      alert('주문이 임시저장되었습니다.')
      router.push('/orders')
    } else {
      throw new Error(response.error || '주문 임시저장에 실패했습니다.')
    }
    
  } catch (error) {
    console.error('임시저장 실패:', error)
    formError.value = error.message || '주문 임시저장 중 오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 주문 제출
const submitOrder = async () => {
  if (!isValidForm()) {
    formError.value = '입력 정보를 확인해주세요'
    return
  }
  
  // 제출 확인
  const confirmed = confirm(
    `총 ${getTotalQuantity()}개 품목, ${formatCurrency(getTotalAmount())} 주문을 제출하시겠습니까?\n` +
    '제출 후에는 수정이 제한될 수 있습니다.'
  )
  
  if (!confirmed) return
  
  isLoading.value = true
  formError.value = ''
  
  try {
    const requestData = {
      note: orderForm.note || null,
      currency: orderForm.currency,
      totalAmount: getTotalAmount(),
      items: orderForm.items.map(item => ({
        name: item.name.trim(),
        qty: Number(item.qty),
        unitPrice: Number(item.unitPrice),
        productId: item.productId
      }))
    }
    
    // 1. 주문 생성 (DRAFT 상태)
    const createResponse = await ordersApi.createOrder(requestData)
    
    if (!createResponse.success) {
      throw new Error(createResponse.error || '주문 생성에 실패했습니다.')
    }
    
    const orderId = createResponse.data.id
    
    // 2. 주문 제출 (SUBMITTED 상태로 변경)
    const submitResponse = await ordersApi.submitOrder(orderId)
    
    if (!submitResponse.success) {
      throw new Error(submitResponse.error || '주문 제출에 실패했습니다.')
    }
    
    // 제출 성공
    alert('주문이 성공적으로 제출되었습니다!')
    router.push('/orders')
    
  } catch (error) {
    console.error('주문 제출 실패:', error)
    formError.value = error.message || '주문 제출 중 오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 권한 확인 computed
const canCreateOrder = computed(() => {
  if (!authStore.user) return false
  return !['PENDING', 'REJECTED', 'SUSPENDED'].includes(authStore.user.status)
})
</script>

<style scoped>
.order-form-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.order-form {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.form-section {
  padding: 24px;
  border-bottom: 1px solid #eee;
}

.form-section:last-child {
  border-bottom: none;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #333;
}

.item-count {
  font-size: 14px;
  color: #666;
  font-weight: normal;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #007bff;
}

.form-input.error {
  border-color: #dc3545;
}

.character-count {
  text-align: right;
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.order-items {
  margin-bottom: 16px;
}

.order-item {
  border: 1px solid #eee;
  border-radius: 6px;
  margin-bottom: 16px;
  overflow: hidden;
}

.item-header {
  background: #f8f9fa;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-number {
  font-weight: 600;
  color: #333;
}

.remove-item-btn {
  background: #dc3545;
  color: white;
  border: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.remove-item-btn:hover:not(:disabled) {
  background: #c82333;
}

.remove-item-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.item-fields {
  padding: 16px;
}

.form-row {
  display: flex;
  gap: 12px;
}

.half-width {
  flex: 1;
}

.item-total {
  margin-top: 12px;
  text-align: right;
  font-weight: 500;
  color: #333;
}

.amount {
  color: #007bff;
  font-weight: 600;
}

.add-item-btn {
  width: 100%;
  padding: 12px;
  border: 2px dashed #ddd;
  background: none;
  border-radius: 6px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.add-item-btn:hover:not(:disabled) {
  border-color: #007bff;
  color: #007bff;
}

.add-item-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.plus-icon {
  font-size: 18px;
  font-weight: bold;
}

.info-message {
  font-size: 12px;
  color: #666;
  text-align: center;
  margin-top: 8px;
}

.order-summary {
  background: #f8f9fa;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-amount {
  font-size: 16px;
  font-weight: 600;
  padding-top: 8px;
  border-top: 1px solid #ddd;
}

.form-actions {
  padding: 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #5a6268;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0056b3;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  font-size: 12px;
  color: #dc3545;
  margin-top: 4px;
}

.form-error {
  background: #f8d7da;
  color: #721c24;
  padding: 12px;
  border-radius: 4px;
  margin-top: 16px;
  border: 1px solid #f5c6cb;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .order-form-container {
    padding: 10px;
  }
  
  .form-section {
    padding: 16px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 8px;
  }
  
  .half-width {
    width: 100%;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
  }
}
</style>