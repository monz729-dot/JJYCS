<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">시스템 환경설정</h1>
        <p class="text-sm text-gray-600 mt-1">비즈니스 규칙, 요금, 설정값을 관리합니다</p>
      </div>
      <div class="mt-4 sm:mt-0 flex space-x-3">
        <button @click="resetToDefaults" 
                class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50">
          <ArrowPathIcon class="h-4 w-4 mr-2" />
          기본값 복원
        </button>
        <button @click="saveAllConfig" 
                :disabled="loading"
                class="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50">
          <CheckIcon class="h-4 w-4 mr-2" />
          {{ loading ? '저장 중...' : '모든 설정 저장' }}
        </button>
      </div>
    </div>

    <!-- Configuration Tabs -->
    <div class="bg-white rounded-lg shadow">
      <div class="border-b border-gray-200">
        <nav class="-mb-px flex space-x-8 px-6" aria-label="Tabs">
          <button
            v-for="tab in configTabs"
            :key="tab.key"
            @click="activeTab = tab.key"
            class="py-4 px-1 border-b-2 font-medium text-sm whitespace-nowrap"
            :class="activeTab === tab.key
              ? 'border-blue-500 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'"
          >
            <component :is="tab.icon" class="h-5 w-5 mr-2 inline" />
            {{ tab.label }}
          </button>
        </nav>
      </div>

      <!-- Shipping Rates -->
      <div v-if="activeTab === 'shipping'" class="p-6 space-y-6">
        <div>
          <h3 class="text-lg font-medium text-gray-900 mb-4">배송 요금 설정</h3>
          
          <!-- Air Shipping -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">항공 배송 요금</h4>
            <div class="bg-gray-50 rounded-lg p-4">
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">기본 요금 (KG당)</label>
                  <div class="flex">
                    <span class="inline-flex items-center px-3 py-2 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">₩</span>
                    <input v-model.number="config.shipping.air.baseRate" 
                           type="number" 
                           class="flex-1 rounded-r-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">최소 중량 (KG)</label>
                  <input v-model.number="config.shipping.air.minWeight" 
                         type="number" 
                         step="0.1"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">최소 요금</label>
                  <div class="flex">
                    <span class="inline-flex items-center px-3 py-2 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">₩</span>
                    <input v-model.number="config.shipping.air.minRate" 
                           type="number" 
                           class="flex-1 rounded-r-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
              </div>
              
              <!-- Volume Weight Factor -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">부피 중량 계수</label>
                  <input v-model.number="config.shipping.air.volumeWeightFactor" 
                         type="number" 
                         step="1"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  <p class="text-xs text-gray-500 mt-1">CBM × 계수 = 부피중량(KG)</p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">배송 기간</label>
                  <input v-model="config.shipping.air.deliveryDays" 
                         type="text" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
              </div>
            </div>
          </div>

          <!-- Sea Shipping -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">해상 배송 요금</h4>
            <div class="bg-gray-50 rounded-lg p-4">
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">기본 요금 (CBM당)</label>
                  <div class="flex">
                    <span class="inline-flex items-center px-3 py-2 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">₩</span>
                    <input v-model.number="config.shipping.sea.baseRate" 
                           type="number" 
                           class="flex-1 rounded-r-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">최소 CBM</label>
                  <input v-model.number="config.shipping.sea.minCbm" 
                         type="number" 
                         step="0.1"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">최소 요금</label>
                  <div class="flex">
                    <span class="inline-flex items-center px-3 py-2 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">₩</span>
                    <input v-model.number="config.shipping.sea.minRate" 
                           type="number" 
                           class="flex-1 rounded-r-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">배송 기간</label>
                  <input v-model="config.shipping.sea.deliveryDays" 
                         type="text" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">CBM 임계치</label>
                  <input v-model.number="config.shipping.sea.cbmThreshold" 
                         type="number" 
                         step="0.1"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  <p class="text-xs text-gray-500 mt-1">이 값 초과시 자동 항공 전환</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Service Fees -->
      <div v-if="activeTab === 'fees'" class="p-6 space-y-6">
        <div>
          <h3 class="text-lg font-medium text-gray-900 mb-4">서비스 수수료</h3>

          <!-- Processing Fees -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">처리 수수료</h4>
            <div class="bg-gray-50 rounded-lg p-4">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">기본 처리 수수료</label>
                  <div class="flex">
                    <span class="inline-flex items-center px-3 py-2 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">₩</span>
                    <input v-model.number="config.fees.processing.base" 
                           type="number" 
                           class="flex-1 rounded-r-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">박스당 추가 수수료</label>
                  <div class="flex">
                    <span class="inline-flex items-center px-3 py-2 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">₩</span>
                    <input v-model.number="config.fees.processing.perBox" 
                           type="number" 
                           class="flex-1 rounded-r-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Repack Fees -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">리패킹 수수료</h4>
            <div class="bg-gray-50 rounded-lg p-4">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">기본 리패킹 수수료</label>
                  <div class="flex">
                    <span class="inline-flex items-center px-3 py-2 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">₩</span>
                    <input v-model.number="config.fees.repack.base" 
                           type="number" 
                           class="flex-1 rounded-r-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">박스 통합 수수료</label>
                  <div class="flex">
                    <span class="inline-flex items-center px-3 py-2 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">₩</span>
                    <input v-model.number="config.fees.repack.consolidation" 
                           type="number" 
                           class="flex-1 rounded-r-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Storage Fees -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">보관 수수료</h4>
            <div class="bg-gray-50 rounded-lg p-4">
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">무료 보관 기간 (일)</label>
                  <input v-model.number="config.fees.storage.freeDays" 
                         type="number" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">일일 보관료 (CBM당)</label>
                  <div class="flex">
                    <span class="inline-flex items-center px-3 py-2 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">₩</span>
                    <input v-model.number="config.fees.storage.dailyRate" 
                           type="number" 
                           class="flex-1 rounded-r-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">최대 보관료</label>
                  <div class="flex">
                    <span class="inline-flex items-center px-3 py-2 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">₩</span>
                    <input v-model.number="config.fees.storage.maxRate" 
                           type="number" 
                           class="flex-1 rounded-r-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Business Rules -->
      <div v-if="activeTab === 'business'" class="p-6 space-y-6">
        <div>
          <h3 class="text-lg font-medium text-gray-900 mb-4">비즈니스 규칙</h3>

          <!-- Order Rules -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">주문 규칙</h4>
            <div class="bg-gray-50 rounded-lg p-4 space-y-4">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">CBM 자동 전환 임계치</label>
                  <input v-model.number="config.business.cbmAutoSwitchThreshold" 
                         type="number" 
                         step="0.1"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  <p class="text-xs text-gray-500 mt-1">이 값 초과시 해상→항공 자동 전환</p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">THB 추가정보 임계치</label>
                  <input v-model.number="config.business.thbExtraInfoThreshold" 
                         type="number"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  <p class="text-xs text-gray-500 mt-1">이 값 초과시 수취인 추가 정보 필요</p>
                </div>
              </div>
              
              <div class="flex items-center justify-between">
                <div>
                  <h5 class="text-sm font-medium text-gray-900">자동 CBM 계산</h5>
                  <p class="text-sm text-gray-500">박스 치수를 기반으로 CBM을 자동 계산</p>
                </div>
                <button @click="config.business.autoCbmCalculation = !config.business.autoCbmCalculation"
                        class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                        :class="config.business.autoCbmCalculation ? 'bg-blue-600' : 'bg-gray-200'">
                  <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                        :class="config.business.autoCbmCalculation ? 'translate-x-5' : 'translate-x-0'"></span>
                </button>
              </div>
            </div>
          </div>

          <!-- Approval Rules -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">승인 규칙</h4>
            <div class="bg-gray-50 rounded-lg p-4 space-y-4">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">기업 계정 승인 소요일</label>
                  <input v-model.number="config.business.enterpriseApprovalDays" 
                         type="number"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">파트너 승인 소요일</label>
                  <input v-model.number="config.business.partnerApprovalDays" 
                         type="number"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
              </div>
              
              <div class="flex items-center justify-between">
                <div>
                  <h5 class="text-sm font-medium text-gray-900">자동 승인</h5>
                  <p class="text-sm text-gray-500">조건을 만족하는 계정 자동 승인</p>
                </div>
                <button @click="config.business.autoApproval = !config.business.autoApproval"
                        class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                        :class="config.business.autoApproval ? 'bg-blue-600' : 'bg-gray-200'">
                  <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                        :class="config.business.autoApproval ? 'translate-x-5' : 'translate-x-0'"></span>
                </button>
              </div>
            </div>
          </div>

          <!-- Delay Rules -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">지연 처리 규칙</h4>
            <div class="bg-gray-50 rounded-lg p-4 space-y-4">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">회원코드 누락 지연 시간 (시간)</label>
                  <input v-model.number="config.business.noCodeDelayHours" 
                         type="number"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                  <p class="text-xs text-gray-500 mt-1">회원코드 미기재시 이 시간 후 지연 처리</p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">자동 알림 간격 (시간)</label>
                  <input v-model.number="config.business.autoNotificationInterval" 
                         type="number"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tax & Exchange -->
      <div v-if="activeTab === 'tax'" class="p-6 space-y-6">
        <div>
          <h3 class="text-lg font-medium text-gray-900 mb-4">세금 및 환율</h3>

          <!-- Tax Rates -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">세율 설정</h4>
            <div class="bg-gray-50 rounded-lg p-4">
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">VAT (%)</label>
                  <input v-model.number="config.tax.vat" 
                         type="number" 
                         step="0.1"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">관세율 (%)</label>
                  <input v-model.number="config.tax.customs" 
                         type="number" 
                         step="0.1"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">기타 세금 (%)</label>
                  <input v-model.number="config.tax.other" 
                         type="number" 
                         step="0.1"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
              </div>
            </div>
          </div>

          <!-- Exchange Rates -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">환율 설정</h4>
            <div class="bg-gray-50 rounded-lg p-4 space-y-4">
              <div class="flex items-center justify-between">
                <div>
                  <h5 class="text-sm font-medium text-gray-900">자동 환율 업데이트</h5>
                  <p class="text-sm text-gray-500">외부 API를 통한 실시간 환율 적용</p>
                </div>
                <button @click="config.exchange.autoUpdate = !config.exchange.autoUpdate"
                        class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                        :class="config.exchange.autoUpdate ? 'bg-blue-600' : 'bg-gray-200'">
                  <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                        :class="config.exchange.autoUpdate ? 'translate-x-5' : 'translate-x-0'"></span>
                </button>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">KRW → THB 환율</label>
                  <input v-model.number="config.exchange.krwToThb" 
                         type="number" 
                         step="0.0001"
                         :disabled="config.exchange.autoUpdate"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500 disabled:bg-gray-100" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">업데이트 간격 (분)</label>
                  <input v-model.number="config.exchange.updateInterval" 
                         type="number"
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
              </div>

              <div class="flex items-center space-x-3">
                <button @click="updateExchangeRate" 
                        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm">
                  환율 업데이트
                </button>
                <span class="text-sm text-gray-500">
                  마지막 업데이트: {{ config.exchange.lastUpdated }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Integration -->
      <div v-if="activeTab === 'integration'" class="p-6 space-y-6">
        <div>
          <h3 class="text-lg font-medium text-gray-900 mb-4">외부 시스템 연동</h3>

          <!-- EMS Integration -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">EMS 연동</h4>
            <div class="bg-gray-50 rounded-lg p-4 space-y-4">
              <div class="flex items-center justify-between">
                <div>
                  <h5 class="text-sm font-medium text-gray-900">EMS API 연동</h5>
                  <p class="text-sm text-gray-500">EMS 코드 검증 및 배송 추적</p>
                </div>
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  연결됨
                </span>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">API 엔드포인트</label>
                  <input v-model="config.integration.ems.endpoint" 
                         type="url" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">타임아웃 (초)</label>
                  <input v-model.number="config.integration.ems.timeout" 
                         type="number" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
              </div>
              <button @click="testIntegration('ems')" 
                      class="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 text-sm">
                연결 테스트
              </button>
            </div>
          </div>

          <!-- HS Code Integration -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">HS 코드 연동</h4>
            <div class="bg-gray-50 rounded-lg p-4 space-y-4">
              <div class="flex items-center justify-between">
                <div>
                  <h5 class="text-sm font-medium text-gray-900">HS 코드 API 연동</h5>
                  <p class="text-sm text-gray-500">관세청 HS 코드 검증</p>
                </div>
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  연결됨
                </span>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">API 엔드포인트</label>
                  <input v-model="config.integration.hs.endpoint" 
                         type="url" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">타임아웃 (초)</label>
                  <input v-model.number="config.integration.hs.timeout" 
                         type="number" 
                         class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:ring-blue-500" />
                </div>
              </div>
              <button @click="testIntegration('hs')" 
                      class="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 text-sm">
                연결 테스트
              </button>
            </div>
          </div>

          <!-- Payment Integration -->
          <div class="mb-6">
            <h4 class="text-md font-medium text-gray-900 mb-3">결제 연동</h4>
            <div class="bg-gray-50 rounded-lg p-4 space-y-4">
              <div class="space-y-4">
                <div class="flex items-center justify-between">
                  <div>
                    <h5 class="text-sm font-medium text-gray-900">신용카드 결제</h5>
                    <p class="text-sm text-gray-500">VISA, MasterCard, JCB</p>
                  </div>
                  <button @click="config.integration.payment.creditCard = !config.integration.payment.creditCard"
                          class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                          :class="config.integration.payment.creditCard ? 'bg-blue-600' : 'bg-gray-200'">
                    <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                          :class="config.integration.payment.creditCard ? 'translate-x-5' : 'translate-x-0'"></span>
                  </button>
                </div>
                <div class="flex items-center justify-between">
                  <div>
                    <h5 class="text-sm font-medium text-gray-900">계좌 이체</h5>
                    <p class="text-sm text-gray-500">한국 은행 계좌</p>
                  </div>
                  <button @click="config.integration.payment.bankTransfer = !config.integration.payment.bankTransfer"
                          class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                          :class="config.integration.payment.bankTransfer ? 'bg-blue-600' : 'bg-gray-200'">
                    <span class="inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ease-in-out"
                          :class="config.integration.payment.bankTransfer ? 'translate-x-5' : 'translate-x-0'"></span>
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
import { ref } from 'vue'
import {
  CheckIcon,
  ArrowPathIcon,
  TruckIcon,
  CurrencyDollarIcon,
  Cog6ToothIcon,
  CalculatorIcon,
  LinkIcon
} from '@heroicons/vue/24/outline'

// State
const loading = ref(false)
const activeTab = ref('shipping')

const configTabs = [
  { key: 'shipping', label: '배송 요금', icon: TruckIcon },
  { key: 'fees', label: '서비스 수수료', icon: CurrencyDollarIcon },
  { key: 'business', label: '비즈니스 규칙', icon: Cog6ToothIcon },
  { key: 'tax', label: '세금/환율', icon: CalculatorIcon },
  { key: 'integration', label: '외부 연동', icon: LinkIcon }
]

const config = ref({
  shipping: {
    air: {
      baseRate: 8500,
      minWeight: 0.5,
      minRate: 15000,
      volumeWeightFactor: 167,
      deliveryDays: '3-5 영업일'
    },
    sea: {
      baseRate: 45000,
      minCbm: 0.1,
      minRate: 25000,
      deliveryDays: '15-25 영업일',
      cbmThreshold: 29.0
    }
  },
  fees: {
    processing: {
      base: 5000,
      perBox: 2000
    },
    repack: {
      base: 8000,
      consolidation: 3000
    },
    storage: {
      freeDays: 30,
      dailyRate: 500,
      maxRate: 50000
    }
  },
  business: {
    cbmAutoSwitchThreshold: 29.0,
    thbExtraInfoThreshold: 1500,
    autoCbmCalculation: true,
    enterpriseApprovalDays: 2,
    partnerApprovalDays: 1,
    autoApproval: false,
    noCodeDelayHours: 24,
    autoNotificationInterval: 12
  },
  tax: {
    vat: 10.0,
    customs: 8.0,
    other: 2.0
  },
  exchange: {
    autoUpdate: true,
    krwToThb: 0.0267,
    updateInterval: 60,
    lastUpdated: '2024-01-20 14:30:00'
  },
  integration: {
    ems: {
      endpoint: 'https://service.epost.go.kr/trace.RetrieveTraceNo.comm',
      timeout: 30
    },
    hs: {
      endpoint: 'https://www.customs.go.kr/api/hscode',
      timeout: 30
    },
    payment: {
      creditCard: true,
      bankTransfer: true
    }
  }
})

// Methods
const saveAllConfig = async () => {
  loading.value = true
  try {
    // Mock save - replace with actual API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    console.log('Configuration saved:', config.value)
    alert('설정이 성공적으로 저장되었습니다.')
  } catch (error) {
    console.error('Error saving configuration:', error)
    alert('설정 저장 중 오류가 발생했습니다.')
  } finally {
    loading.value = false
  }
}

const resetToDefaults = () => {
  if (confirm('모든 설정을 기본값으로 복원하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
    // Reset to default values
    console.log('Resetting to defaults...')
    alert('기본값으로 복원되었습니다.')
  }
}

const updateExchangeRate = async () => {
  try {
    // Mock exchange rate update
    await new Promise(resolve => setTimeout(resolve, 1000))
    config.value.exchange.krwToThb = 0.0265
    config.value.exchange.lastUpdated = new Date().toLocaleString('ko-KR')
    alert('환율이 업데이트되었습니다.')
  } catch (error) {
    alert('환율 업데이트에 실패했습니다.')
  }
}

const testIntegration = async (type: string) => {
  try {
    // Mock integration test
    await new Promise(resolve => setTimeout(resolve, 2000))
    alert(`${type.toUpperCase()} 연동 테스트가 성공했습니다.`)
  } catch (error) {
    alert(`${type.toUpperCase()} 연동 테스트가 실패했습니다.`)
  }
}
</script>