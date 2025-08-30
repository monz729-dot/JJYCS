<template>
  <div v-if="loading" class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-white to-blue-50">
    <div class="text-center">
      <div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center mx-auto mb-4 animate-bounce">
        <div class="w-8 h-8 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
      </div>
      <p class="text-blue-600">주문 정보를 불러오는 중...</p>
    </div>
  </div>

  <div v-else class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 p-4">
    <!-- 헤더 -->
    <div class="flex items-center gap-4 mb-6">
      <button @click="router.back()" class="text-blue-600 hover:text-blue-700 hover:bg-blue-50 p-2 rounded-lg transition-all flex items-center gap-2">
        <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z"/>
        </svg>
        주문내역으로
      </button>
      <div>
        <h1 class="text-xl font-bold text-blue-900">주문 상세</h1>
        <p class="text-sm text-blue-600">{{ orderDetail.orderNumber }} - 청구서 및 배송 정보</p>
      </div>
    </div>

    <!-- 워크플로우 현황 -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-2 text-xl font-semibold text-blue-900">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path d="M8 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0zM15 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0z"/>
            <path d="M3 4a1 1 0 00-1 1v10a1 1 0 001 1h1.05a2.5 2.5 0 014.9 0H10a1 1 0 001-1V5a1 1 0 00-1-1H3zM14 7a1 1 0 00-1 1v6.05A2.5 2.5 0 0115.95 16H17a1 1 0 001-1V8a1 1 0 00-1-1h-3z"/>
          </svg>
          배송 워크플로우
        </div>
        <span class="px-3 py-1 text-xs border border-gray-300 rounded-full">
          {{ completedSteps }}/{{ workflowSteps.length }} 단계 완료
        </span>
      </div>

      <div class="mb-4">
        <div class="flex justify-between text-sm mb-2">
          <span>진행률</span>
          <span>{{ progressPercent }}%</span>
        </div>
        <div class="w-full h-3 bg-gray-200 rounded-lg overflow-hidden">
          <div class="h-full bg-gradient-to-r from-blue-500 to-blue-600 rounded-lg transition-all duration-500" :style="`width: ${progressPercent}%`"></div>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div v-for="step in workflowSteps" :key="step.key" 
             :class="['p-3 rounded-xl border transition-all', getStepClass(step)]">
          <div class="flex items-center gap-2 mb-2">
            <div :class="['text-base', getStepIconClass(step)]" v-html="step.icon"></div>
            <span :class="['text-sm font-medium', getStepTitleClass(step)]">{{ step.label }}</span>
          </div>
          <p class="text-xs text-gray-600 mb-1">{{ step.description }}</p>
          <p v-if="step.date" class="text-xs text-gray-500">{{ step.date }}</p>
          <span v-if="step.active" class="inline-block mt-2 px-2 py-0.5 text-xs bg-indigo-100 text-indigo-700 rounded-md">진행중</span>
          <span v-else-if="step.completed" class="inline-block mt-2 px-2 py-0.5 text-xs bg-green-600 text-white rounded-md">완료</span>
        </div>
      </div>
    </div>

    <!-- 기본 정보 그리드 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <!-- 주문 정보 -->
      <div class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100">
        <div class="flex items-center gap-2 text-xl font-semibold text-blue-900 mb-6">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path d="M3 7v10a2 2 0 002 2h10a2 2 0 002-2V9a2 2 0 00-2-2H5a2 2 0 00-2 2v0z"/>
            <path d="M9 5a2 2 0 012-2h6a2 2 0 012 2v6a2 2 0 01-2 2H9a2 2 0 01-2-2V5z"/>
          </svg>
          주문 정보
        </div>

        <div class="space-y-3">
          <div class="flex justify-between items-center py-3">
            <span class="text-blue-700 text-sm">주문번호</span>
            <span class="font-medium font-mono">{{ orderDetail.orderNumber }}</span>
          </div>
          <div class="flex justify-between items-center py-3">
            <span class="text-blue-700 text-sm">고객 아이디</span>
            <span class="font-medium font-mono">{{ orderDetail.customerId }}</span>
          </div>
          <div class="flex justify-between items-center py-3">
            <span class="text-blue-700 text-sm">접수일</span>
            <span class="font-medium font-mono">{{ orderDetail.orderDate }}</span>
          </div>
          <div class="flex justify-between items-center py-3">
            <span class="text-blue-700 text-sm">송장번호</span>
            <span class="font-medium font-mono">{{ orderDetail.trackingNumber }}</span>
          </div>
          <div class="flex justify-between items-center py-3">
            <span class="text-blue-700 text-sm">배송유형</span>
            <span class="px-3 py-1 text-xs border border-gray-300 rounded-full">
              {{ orderDetail.shippingType === 'air' ? '항공운송' : '해상운송' }}
            </span>
          </div>
          <div class="flex justify-between items-center py-3">
            <span class="text-blue-700 text-sm">목적지</span>
            <span class="font-medium">{{ getCountryName(orderDetail.country) }} ({{ orderDetail.postalCode }})</span>
          </div>
        </div>
      </div>

      <!-- 고객 정보 -->
      <div class="bg-white rounded-2xl p-6 shadow-blue-100 shadow-lg border border-blue-100">
        <div class="flex items-center gap-2 text-xl font-semibold text-blue-900 mb-6">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"/>
          </svg>
          고객 정보
        </div>

        <div class="space-y-3">
          <div class="flex justify-between items-center py-3">
            <span class="text-blue-700 text-sm">이름</span>
            <span class="font-medium">{{ orderDetail.customerName }}</span>
          </div>
          <div class="flex justify-between items-center py-3">
            <span class="text-blue-700 text-sm">이메일</span>
            <span class="font-medium text-sm">{{ orderDetail.customerEmail }}</span>
          </div>
          <div class="flex justify-between items-center py-3">
            <span class="text-blue-700 text-sm">연락처</span>
            <span class="font-medium">{{ orderDetail.customerPhone }}</span>
          </div>
          <div class="flex justify-between items-center py-3">
            <span class="text-blue-700 text-sm">회원유형</span>
            <span class="px-3 py-1 text-xs border border-gray-300 rounded-full">
              {{ orderDetail.customerType === 'corporate' ? '기업회원' : '일반회원' }}
            </span>
          </div>
          <div v-if="orderDetail.companyName" class="flex justify-between items-center py-3">
            <span class="text-blue-700 text-sm">회사명</span>
            <span class="font-medium">{{ orderDetail.companyName }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 창고 정보 -->
    <div v-if="orderDetail.warehouse?.arrivedDate" class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <div class="flex items-center gap-2 text-xl font-semibold text-blue-900 mb-6">
        <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
          <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z"/>
        </svg>
        YSC 창고 정보
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
        <div class="flex justify-between items-center py-2">
          <span class="text-blue-700 text-sm">창고도착일</span>
          <span class="font-medium">{{ orderDetail.warehouse.arrivedDate }}</span>
        </div>
        <div class="flex justify-between items-center py-2">
          <span class="text-blue-700 text-sm">실제무게</span>
          <span class="font-medium">{{ orderDetail.warehouse.actualWeight }}kg</span>
        </div>
        <div class="flex justify-between items-center py-2">
          <span class="text-blue-700 text-sm">보관위치</span>
          <div class="flex items-center gap-2">
            <span class="px-2 py-1 bg-blue-500 text-white text-xs rounded font-mono font-bold">
              {{ orderDetail.warehouse.storageLocation }}
            </span>
            <span class="text-sm text-gray-600">({{ orderDetail.warehouse.storageArea }})</span>
          </div>
        </div>
        <div class="flex justify-between items-center py-2">
          <span class="text-blue-700 text-sm">리패킹</span>
          <div class="flex items-center gap-2">
            <span :class="['px-2 py-1 text-xs rounded', orderDetail.warehouse.repackingRequested ? 'bg-blue-500 text-white' : 'border border-gray-300']">
              {{ orderDetail.warehouse.repackingRequested ? '신청함' : '신청안함' }}
            </span>
            <span v-if="orderDetail.warehouse.repackingRequested" 
                  :class="['px-2 py-1 text-xs rounded', orderDetail.warehouse.repackingCompleted ? 'bg-blue-500 text-white' : 'border border-gray-300']">
              {{ orderDetail.warehouse.repackingCompleted ? '완료' : '진행중' }}
            </span>
          </div>
        </div>
      </div>

      <div v-if="orderDetail.warehouse.photos?.length > 0" class="mb-4">
        <span class="text-blue-700 text-sm block mb-2">업로드된 사진</span>
        <div class="flex flex-wrap gap-2">
          <span v-for="(photo, index) in orderDetail.warehouse.photos" :key="index" 
                class="px-2 py-1 text-xs border border-gray-300 rounded flex items-center gap-1">
            <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
              <path d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4z"/>
            </svg>
            {{ photo.includes('arrival') ? '도착사진' : photo.includes('repacking') ? '리패킹사진' : '사진' }}{{ index + 1 }}
          </span>
        </div>
      </div>

      <div v-if="orderDetail.warehouse.notes" class="p-3 bg-blue-50 rounded-xl">
        <p class="text-sm text-blue-700">
          <strong>창고 메모:</strong> {{ orderDetail.warehouse.notes }}
        </p>
      </div>
    </div>

    <!-- 청구서 정보 -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-2 text-xl font-semibold text-blue-900">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4z"/>
            <path d="M6 8a1 1 0 000 2h8a1 1 0 100-2H6zM6 12a1 1 0 000 2h4a1 1 0 100-2H6z"/>
          </svg>
          청구서 (Invoice)
        </div>
        <div class="flex gap-2">
          <span v-if="orderDetail.billing?.proformaIssued" class="px-2 py-1 bg-blue-500 text-white text-xs rounded">Proforma 발행</span>
          <span v-if="orderDetail.billing?.finalIssued" class="px-2 py-1 bg-blue-500 text-white text-xs rounded">Final 발행</span>
        </div>
      </div>

      <!-- 청구서 발행 현황 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <div class="p-3 border border-gray-200 rounded-xl">
          <div class="flex items-center justify-between mb-2">
            <span class="font-medium text-purple-700">Proforma 청구서</span>
            <span :class="['px-2 py-1 text-xs rounded', orderDetail.billing?.proformaIssued ? 'bg-blue-500 text-white' : 'border border-gray-300']">
              {{ orderDetail.billing?.proformaIssued ? '발행완료' : '미발행' }}
            </span>
          </div>
          <p v-if="orderDetail.billing?.proformaIssued && orderDetail.billing?.proformaDate" class="text-sm text-gray-600">
            발행일: {{ orderDetail.billing.proformaDate }}
          </p>
        </div>

        <div class="p-3 border border-gray-200 rounded-xl">
          <div class="flex items-center justify-between mb-2">
            <span class="font-medium text-purple-700">Final 청구서</span>
            <span :class="['px-2 py-1 text-xs rounded', orderDetail.billing?.finalIssued ? 'bg-blue-500 text-white' : 'border border-gray-300']">
              {{ orderDetail.billing?.finalIssued ? '발행완료' : '미발행' }}
            </span>
          </div>
          <p v-if="orderDetail.billing?.finalIssued && orderDetail.billing?.finalDate" class="text-sm text-gray-600">
            발행일: {{ orderDetail.billing.finalDate }}
          </p>
        </div>
      </div>

      <!-- 비용 상세 -->
      <div v-if="orderDetail.billing?.proformaIssued" class="mb-6">
        <h4 class="font-medium text-blue-900 mb-4">비용 상세</h4>
        <div class="space-y-2">
          <div class="flex justify-between items-center py-2 border-b border-gray-100">
            <span class="text-gray-600">기본 배송비</span>
            <span class="font-medium">{{ formatCurrency(orderDetail.billing.shippingFee) }}</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b border-gray-100">
            <span class="text-gray-600">현지 배송비</span>
            <span class="font-medium">{{ formatCurrency(orderDetail.billing.localDeliveryFee) }}</span>
          </div>
          <div v-if="orderDetail.billing.repackingFee > 0" class="flex justify-between items-center py-2 border-b border-gray-100">
            <span class="text-gray-600">리패킹 비용</span>
            <span class="font-medium">{{ formatCurrency(orderDetail.billing.repackingFee) }}</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b border-gray-100">
            <span class="text-gray-600">취급 수수료</span>
            <span class="font-medium">{{ formatCurrency(orderDetail.billing.handlingFee) }}</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b border-gray-100">
            <span class="text-gray-600">보험료</span>
            <span class="font-medium">{{ formatCurrency(orderDetail.billing.insuranceFee) }}</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b border-gray-100">
            <span class="text-gray-600">통관 수수료</span>
            <span class="font-medium">{{ formatCurrency(orderDetail.billing.customsFee) }}</span>
          </div>
          <div class="flex justify-between items-center py-2 border-b border-gray-200">
            <span class="text-gray-600">TAX 7%</span>
            <span class="font-medium">{{ formatCurrency(orderDetail.billing.tax) }}</span>
          </div>
          <div class="flex justify-between items-center p-3 bg-blue-50 rounded-xl">
            <span class="text-lg font-bold text-blue-900">총 결제금액</span>
            <span class="text-xl font-bold text-blue-900">{{ formatCurrency(orderDetail.billing.total) }}</span>
          </div>
        </div>
      </div>

      <!-- 결제 정보 -->
      <div>
        <h4 class="font-medium text-blue-900 mb-4">결제 정보</h4>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
          <div class="flex justify-between items-center py-2">
            <span class="text-blue-700 text-sm">결제 방식</span>
            <span class="px-3 py-1 text-xs border border-gray-300 rounded-full">무통장 입금</span>
          </div>
          <div class="flex justify-between items-center py-2">
            <span class="text-blue-700 text-sm">결제 상태</span>
            <span :class="['px-3 py-1 text-xs rounded-full', getPaymentStatusClass(orderDetail.billing?.paymentStatus)]">
              {{ getPaymentStatusText(orderDetail.billing?.paymentStatus) }}
            </span>
          </div>
          <div v-if="orderDetail.billing?.depositorName" class="flex justify-between items-center py-2">
            <span class="text-blue-700 text-sm">입금자명</span>
            <span class="font-medium">{{ orderDetail.billing.depositorName }}</span>
          </div>
          <div v-if="orderDetail.billing?.paymentDate" class="flex justify-between items-center py-2">
            <span class="text-blue-700 text-sm">입금일</span>
            <span class="font-medium">{{ orderDetail.billing.paymentDate }}</span>
          </div>
        </div>

        <!-- 입금 대기 알림 -->
        <div v-if="isPendingPayment" class="bg-yellow-50 border border-yellow-200 rounded-xl p-4 mb-4">
          <div class="flex items-center gap-2">
            <svg class="w-5 h-5 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
              <path d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-13a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.828a1 1 0 101.414-1.414L11 9.586V5z"/>
            </svg>
            <div>
              <h4 class="font-medium text-yellow-900">무통장 입금 대기 중</h4>
              <p class="text-sm text-yellow-700">결제금액: {{ formatCurrency(orderDetail.billing?.total || 0) }}</p>
            </div>
          </div>
        </div>

        <!-- 무통장입금 계좌 정보 -->
        <div v-if="isPendingPayment" class="bg-gray-50 border border-gray-200 rounded-xl p-6">
          <h4 class="font-semibold text-blue-900 mb-4 flex items-center gap-2">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path d="M4 4a2 2 0 00-2 2v1h16V6a2 2 0 00-2-2H4z"/>
              <path fill-rule="evenodd" d="M18 9H2v5a2 2 0 002 2h12a2 2 0 002-2V9zM4 13a1 1 0 011-1h1a1 1 0 110 2H5a1 1 0 01-1-1zm5-1a1 1 0 100 2h1a1 1 0 100-2H9z"/>
            </svg>
            입금 계좌 정보
          </h4>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="bg-white border border-gray-300 rounded-lg p-4">
              <div class="flex items-center justify-between mb-2">
                <span class="font-semibold text-gray-700">KB국민은행</span>
                <button @click="copyToClipboard('123456789012')" class="px-2 py-1 text-xs border border-gray-300 rounded hover:bg-gray-50">복사</button>
              </div>
              <div class="font-mono text-lg font-bold text-blue-600 mb-1">123-456-789012</div>
              <div class="text-sm text-gray-600">예금주: YSC물류(주)</div>
            </div>

            <div class="bg-white border border-gray-300 rounded-lg p-4">
              <div class="flex items-center justify-between mb-2">
                <span class="font-semibold text-gray-700">신한은행</span>
                <button @click="copyToClipboard('110123456789')" class="px-2 py-1 text-xs border border-gray-300 rounded hover:bg-gray-50">복사</button>
              </div>
              <div class="font-mono text-lg font-bold text-blue-600 mb-1">110-123-456789</div>
              <div class="text-sm text-gray-600">예금주: YSC물류(주)</div>
            </div>
          </div>

          <div class="bg-yellow-100 border border-yellow-300 rounded-lg p-3 mt-4">
            <div class="flex items-center gap-2 mb-1">
              <svg class="w-4 h-4 text-yellow-700" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"/>
              </svg>
              <span class="font-semibold text-yellow-900 text-sm">입금 안내</span>
            </div>
            <p class="text-sm text-yellow-700">정확한 금액으로 입금 후 1-2시간 내 자동 확인됩니다. 입금자명은 주문자명과 동일하게 해주세요.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 품목 정보 -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <div class="flex items-center gap-2 text-xl font-semibold text-blue-900 mb-6">
        <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
          <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4z"/>
        </svg>
        품목 정보
      </div>

      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-gray-200">
              <th class="text-left py-3 px-2 bg-blue-50/50 text-blue-700 font-semibold">품목</th>
              <th class="text-center py-3 px-2 bg-blue-50/50 text-blue-700 font-semibold">수량</th>
              <th class="text-center py-3 px-2 bg-blue-50/50 text-blue-700 font-semibold">중량</th>
              <th class="text-center py-3 px-2 bg-blue-50/50 text-blue-700 font-semibold">크기(cm)</th>
              <th class="text-center py-3 px-2 bg-blue-50/50 text-blue-700 font-semibold">CBM</th>
              <th class="text-center py-3 px-2 bg-blue-50/50 text-blue-700 font-semibold">부피무게</th>
              <th v-if="orderDetail.customerType === 'corporate'" class="text-right py-3 px-2 bg-blue-50/50 text-blue-700 font-semibold">단가</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in orderDetail.items" :key="item.id" class="border-b border-gray-100">
              <td class="py-3 px-2">
                <p v-if="orderDetail.customerType === 'corporate'" class="text-xs text-gray-600 mb-1 font-mono">{{ item.hscode }}</p>
                <p class="font-medium">{{ item.description }}</p>
              </td>
              <td class="text-center py-3 px-2">{{ item.quantity }}개</td>
              <td class="text-center py-3 px-2">{{ item.weight }}kg</td>
              <td class="text-center py-3 px-2">{{ item.width }}×{{ item.height }}×{{ item.depth }}</td>
              <td class="text-center py-3 px-2">
                <span class="inline-flex items-center gap-1 px-2 py-1 text-xs border border-gray-300 rounded">
                  <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                  {{ item.cbm }}m³
                </span>
              </td>
              <td class="text-center py-3 px-2">{{ getVolumetricWeight(item) }}kg</td>
              <td v-if="orderDetail.customerType === 'corporate'" class="text-right py-3 px-2">${{ item.unitPrice.toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 수취인 정보 -->
    <div class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <div class="flex items-center gap-2 text-xl font-semibold text-blue-900 mb-6">
        <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
          <path d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z"/>
        </svg>
        수취인 정보
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
        <div class="flex justify-between items-center py-2">
          <span class="text-blue-700 text-sm">수취인 이름</span>
          <span class="font-medium">{{ orderDetail.recipientName }}</span>
        </div>
        <div class="flex justify-between items-center py-2">
          <span class="text-blue-700 text-sm">연락처</span>
          <span class="font-medium">{{ orderDetail.recipientPhone }}</span>
        </div>
        <div class="flex justify-between items-center py-2">
          <span class="text-blue-700 text-sm">우편번호</span>
          <span class="font-medium">{{ orderDetail.recipientPostalCode }}</span>
        </div>
        <div class="flex justify-between items-center py-2">
          <span class="text-blue-700 text-sm">국가</span>
          <span class="font-medium">{{ getCountryName(orderDetail.country) }}</span>
        </div>
      </div>
      <div>
        <p class="text-blue-700 text-sm mb-1">배송지 주소</p>
        <p class="font-medium bg-gray-50 p-3 rounded-xl">{{ orderDetail.recipientAddress }}</p>
      </div>
    </div>

    <!-- 특별 요청사항 -->
    <div v-if="orderDetail.specialRequests" class="bg-white rounded-2xl p-6 mb-6 shadow-blue-100 shadow-lg border border-blue-100">
      <div class="flex items-center gap-2 text-xl font-semibold text-blue-900 mb-6">
        <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
          <path d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"/>
        </svg>
        특별 요청사항
      </div>

      <p class="text-gray-700 bg-yellow-50 p-3 rounded-xl border border-yellow-200">
        {{ orderDetail.specialRequests }}
      </p>
    </div>

    <!-- 하단 버튼 -->
    <div class="flex gap-4 pb-20">
      <button @click="router.back()" class="flex-1 h-14 border border-gray-300 text-gray-700 rounded-xl hover:bg-gray-50 font-medium">
        주문내역으로
      </button>
      <button v-if="orderDetail.billing?.proformaIssued" @click="downloadInvoice" class="flex-1 h-14 border border-gray-300 text-gray-700 rounded-xl hover:bg-gray-50 font-medium flex items-center justify-center gap-2">
        <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path d="M3 17a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1v-2zM6.293 6.707a1 1 0 010-1.414l3-3a1 1 0 011.414 0l3 3a1 1 0 01-1.414 1.414L11 5.414V13a1 1 0 11-2 0V5.414L7.707 6.707a1 1 0 01-1.414 0z"/>
        </svg>
        청구서 다운로드
      </button>
      <button v-if="isPendingPayment" @click="goToPayment" class="flex-1 h-14 bg-green-600 text-white rounded-xl hover:bg-green-700 font-medium flex items-center justify-center gap-2">
        <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zM14 6a2 2 0 012 2v6a2 2 0 01-2 2H6a2 2 0 01-2-2V8a2 2 0 012-2h8zM6 10a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1z"/>
        </svg>
        입금하기
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const orderDetail = ref<any>({
  orderNumber: '',
  customerId: '',
  orderDate: '',
  status: '',
  shippingType: '',
  country: '',
  postalCode: '',
  deliveryDate: null,
  customerName: '',
  customerEmail: '',
  customerPhone: '',
  customerType: 'general',
  companyName: null,
  recipientName: '',
  recipientPhone: '',
  recipientAddress: '',
  recipientPostalCode: '',
  items: [],
  trackingNumber: '',
  warehouse: {
    arrivedDate: null,
    actualWeight: 0,
    storageLocation: '',
    storageArea: '',
    repackingRequested: false,
    repackingCompleted: false,
    photos: [],
    notes: ''
  },
  billing: {
    proformaIssued: false,
    proformaDate: null,
    finalIssued: false,
    shippingFee: 0,
    localDeliveryFee: 0,
    repackingFee: 0,
    handlingFee: 0,
    insuranceFee: 0,
    customsFee: 0,
    tax: 0,
    total: 0,
    paymentMethod: 'manual_deposit',
    paymentStatus: 'pending',
    paymentDate: null,
    depositorName: ''
  },
  specialRequests: ''
})

const workflowSteps = computed(() => [
  {
    key: 'received',
    label: '접수완료',
    icon: '<svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"/></svg>',
    description: '우체국 송장번호 등록',
    completed: true,
    active: false,
    date: orderDetail.value.orderDate
  },
  {
    key: 'arrived',
    label: 'YSC창고도착',
    icon: '<svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20"><path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4z"/></svg>',
    description: '창고 도착, 무게 확인',
    completed: !!orderDetail.value.warehouse?.arrivedDate,
    active: orderDetail.value.status === 'arrived',
    date: orderDetail.value.warehouse?.arrivedDate
  },
  {
    key: 'repacking',
    label: '리패킹진행',
    icon: '<svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20"><path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793z"/></svg>',
    description: '리패킹 작업 및 사진 업로드',
    completed: orderDetail.value.warehouse?.repackingCompleted || !orderDetail.value.warehouse?.repackingRequested,
    active: orderDetail.value.status === 'repacking'
  },
  {
    key: 'shipping',
    label: '배송중',
    icon: '<svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20"><path d="M8 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0zM15 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0z"/></svg>',
    description: '태국 현지 배송 진행',
    completed: orderDetail.value.status === 'delivered',
    active: orderDetail.value.status === 'shipping'
  },
  {
    key: 'delivered',
    label: '배송완료',
    icon: '<svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"/></svg>',
    description: '최종 배송 완료',
    completed: orderDetail.value.status === 'delivered',
    active: false,
    date: orderDetail.value.deliveryDate
  },
  {
    key: 'billing',
    label: '청구서발행',
    icon: '<svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20"><path d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4z"/></svg>',
    description: '현지 배송비 포함 최종 정산',
    completed: orderDetail.value.billing?.proformaIssued,
    active: orderDetail.value.status === 'billing',
    date: orderDetail.value.billing?.proformaDate
  },
  {
    key: 'payment_pending',
    label: '입금대기',
    icon: '<svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-13a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.828a1 1 0 101.414-1.414L11 9.586V5z"/></svg>',
    description: '무통장 입금 대기',
    completed: orderDetail.value.billing?.paymentStatus === 'confirmed' || orderDetail.value.billing?.paymentStatus === 'completed',
    active: orderDetail.value.status === 'payment_pending'
  },
  {
    key: 'payment_confirmed',
    label: '입금확인',
    icon: '<svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20"><path d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4z"/></svg>',
    description: '입금 확인 및 정산 완료',
    completed: orderDetail.value.billing?.paymentStatus === 'completed',
    active: orderDetail.value.status === 'payment_confirmed',
    date: orderDetail.value.billing?.paymentDate
  }
])

const completedSteps = computed(() => workflowSteps.value.filter(s => s.completed).length)
const progressPercent = computed(() => Math.round((completedSteps.value / workflowSteps.value.length) * 100))
const isPendingPayment = computed(() => 
  (orderDetail.value.status === 'payment_pending' || orderDetail.value.status === 'billing') && 
  orderDetail.value.billing?.paymentStatus === 'pending'
)

// 스텝 클래스 헬퍼
const getStepClass = (step: any) => {
  if (step.completed) return 'bg-green-50 border-green-200'
  if (step.active) return 'bg-blue-50 border-blue-200'
  return 'bg-gray-50 border-gray-200'
}

const getStepIconClass = (step: any) => {
  if (step.completed) return 'text-green-600'
  if (step.active) return 'text-blue-600'
  return 'text-gray-400'
}

const getStepTitleClass = (step: any) => {
  if (step.completed) return 'text-green-700'
  if (step.active) return 'text-blue-700'
  return 'text-gray-600'
}

// 유틸리티 함수들
const getCountryName = (country: string) => {
  const countryNames: Record<string, string> = {
    'thailand': '태국',
    'vietnam': '베트남',
    'philippines': '필리핀',
    'indonesia': '인도네시아'
  }
  return countryNames[country] || country
}

const formatCurrency = (amount: number, currency = 'THB') => {
  if (currency === 'KRW') {
    return new Intl.NumberFormat('ko-KR').format(amount) + '원'
  }
  return new Intl.NumberFormat('ko-KR').format(amount) + ' THB'
}

const getPaymentStatusClass = (status: string) => {
  switch (status) {
    case 'completed':
    case 'confirmed':
      return 'bg-blue-500 text-white'
    case 'pending':
      return 'border border-gray-300'
    default:
      return 'border border-gray-300'
  }
}

const getPaymentStatusText = (status: string) => {
  switch (status) {
    case 'completed':
      return '입금완료'
    case 'confirmed':
      return '입금확인'
    case 'pending':
      return '입금대기'
    default:
      return '미준비'
  }
}

const getVolumetricWeight = (item: any) => {
  return ((item.width * item.height * item.depth) / 6000).toFixed(2)
}

// 이벤트 핸들러
const downloadInvoice = () => {
  window.print()
}

const goToPayment = () => {
  router.push(`/payment?orderId=${orderDetail.value.orderId}`)
}

const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    alert('계좌번호가 클립보드에 복사되었습니다!')
  } catch (err) {
    console.error('복사 실패:', err)
    alert('복사에 실패했습니다. 수동으로 복사해주세요.')
  }
}

// 주문 상세 데이터 로드
const loadOrderDetail = async (orderId: string) => {
  try {
    // API 호출 시뮬레이션
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 데모 데이터 설정
    orderDetail.value = {
      orderId: orderId,
      orderNumber: 'YSC-240115-001',
      customerId: 'KYP001',
      orderDate: '2024-01-15',
      status: 'delivered',
      shippingType: 'sea',
      country: 'thailand',
      postalCode: '10110',
      deliveryDate: '2024-01-25',
      
      customerName: '김철수',
      customerEmail: 'kimcs@email.com',
      customerPhone: '010-1234-5678',
      customerType: 'general',
      
      recipientName: 'Somchai Jaidee',
      recipientPhone: '+66-81-234-5678',
      recipientAddress: '123 Sukhumvit Road, Watthana District, Bangkok',
      recipientPostalCode: '10110',
      
      items: [
        {
          id: '1',
          hscode: '1905.31',
          description: '빼빼로 초콜릿',
          quantity: 10,
          weight: 1.5,
          width: 30,
          height: 20,
          depth: 5,
          unitPrice: 25.00,
          cbm: 0.003
        },
        {
          id: '2',
          hscode: '1806.32',
          description: '초콜릿 과자',
          quantity: 5,
          weight: 0.8,
          width: 20,
          height: 15,
          depth: 3,
          unitPrice: 15.00,
          cbm: 0.0009
        }
      ],
      
      trackingNumber: 'EE123456789KR',
      
      warehouse: {
        arrivedDate: '2024-01-18',
        actualWeight: 2.5,
        storageLocation: 'A-01-03',
        storageArea: 'A열 1행 3번',
        repackingRequested: true,
        repackingCompleted: true,
        photos: ['arrival_240118.jpg', 'repacking_240118_1.jpg', 'repacking_240118_2.jpg'],
        notes: '상품 상태 양호, 1kg씩 3개 포장으로 분할 완료'
      },
      
      billing: {
        proformaIssued: true,
        proformaDate: '2024-01-26',
        finalIssued: false,
        shippingFee: 85000,
        localDeliveryFee: 25000,
        repackingFee: 15000,
        handlingFee: 10000,
        insuranceFee: 2000,
        customsFee: 5000,
        tax: 9975,
        total: 151975,
        paymentMethod: 'manual_deposit',
        paymentStatus: 'completed',
        paymentDate: '2024-01-27',
        depositorName: '김철수'
      },
      
      specialRequests: '깨지기 쉬운 상품이므로 신중히 포장 부탁드립니다.'
    }
    
    loading.value = false
  } catch (error) {
    console.error('주문 데이터 로드 실패:', error)
    loading.value = false
  }
}

onMounted(() => {
  const orderId = route.params.id as string || route.query.id as string || 'YSC-2024-001'
  loadOrderDetail(orderId)
})
</script>