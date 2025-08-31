<template>
  <div class="min-h-screen bg-gray-50 pb-20">
    <!-- Fixed Header -->
    <div class="sticky top-0 bg-white border-b border-gray-200 z-50">
      <div class="flex items-center px-4 py-3">
        <button @click="goBack" class="p-2 -ml-2 text-blue-600 hover:bg-blue-50 rounded-lg">
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z"/>
          </svg>
        </button>
        <div class="flex-1 ml-3">
          <h1 class="text-lg font-bold text-gray-900">새 주문 접수</h1>
          <p class="text-sm text-gray-500">🇹🇭 태국 전용</p>
        </div>
        <div v-if="lastAutoSave" class="text-xs text-green-600 flex items-center gap-1">
          <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
            <path d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"/>
          </svg>
          저장됨
        </div>
      </div>
    </div>

    <!-- Validation Errors Snackbar -->
    <div v-if="validationErrors.length > 0" class="mx-4 mt-4">
      <div class="bg-red-50 border border-red-200 rounded-lg p-3">
        <div class="flex">
          <svg class="w-5 h-5 text-red-400 mt-0.5 mr-3" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
          </svg>
          <div class="flex-1">
            <h3 class="text-sm font-medium text-red-800">입력 오류</h3>
            <ul class="text-sm text-red-700 mt-1 list-disc list-inside">
              <li v-for="error in validationErrors" :key="error">{{ error }}</li>
            </ul>
          </div>
          <button @click="clearValidationErrors" class="text-red-400 hover:text-red-600">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Business Warnings -->
    <div v-if="businessWarnings.length > 0" class="mx-4 mt-4">
      <div v-for="(warning, index) in businessWarnings" :key="index" class="bg-amber-50 border border-amber-200 rounded-lg p-3 mb-2">
        <div class="flex">
          <svg class="w-5 h-5 text-amber-400 mt-0.5 mr-3" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
          </svg>
          <div class="flex-1">
            <h3 class="text-sm font-medium text-amber-800">{{ warning.title }}</h3>
            <p class="text-sm text-amber-700 mt-1">{{ warning.message }}</p>
          </div>
          <button @click="removeWarning(index)" class="text-amber-400 hover:text-amber-600">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 주문자 정보 카드 (상단 고정) -->
    <div class="mx-4 mt-4">
      <div class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
            <svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
              <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"/>
            </svg>
          </div>
          <div class="flex-1">
            <h2 class="text-base font-semibold text-gray-900">{{ authStore.user?.name }}</h2>
            <div class="flex items-center gap-2 mt-1">
              <span class="text-xs px-2 py-1 bg-blue-100 text-blue-700 rounded-full">{{ getUserTypeText }}</span>
              <span class="text-xs text-gray-500">{{ authStore.user?.memberCode || 'No code' }}</span>
            </div>
          </div>
          <div v-if="!authStore.user?.memberCode || authStore.user?.memberCode === 'No code'" class="text-orange-500">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <!-- Order Form -->
    <form @submit.prevent="submitOrder">
      <!-- 배송 정보 카드 -->
      <div class="mx-4 mt-4">
        <div class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm">
          <div class="flex items-center gap-2 mb-4">
            <svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
              <path d="M8 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0zM15 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0z"/>
              <path d="M3 4a1 1 0 00-1 1v10a1 1 0 001 1h1.05a2.5 2.5 0 014.9 0H10a1 1 0 001-1V5a1 1 0 00-1-1H3zM14 7a1 1 0 00-1 1v6.05A2.5 2.5 0 0115.95 16H17a1 1 0 001-1V8a1 1 0 00-1-1h-3z"/>
            </svg>
            <h3 class="text-base font-semibold text-gray-900">배송 정보</h3>
            <div class="ml-auto">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                🇹🇭 태국 전용
              </span>
            </div>
          </div>
          

          <!-- 배송유형 토글 -->
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-3">배송 유형</label>
            <div class="flex bg-gray-100 rounded-lg p-1">
              <button 
                type="button"
                @click="orderForm.shippingType = 'sea'"
                :class="['flex-1 py-2 px-3 rounded-md text-sm font-medium transition-all text-center', orderForm.shippingType === 'sea' ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500']"
              >
                해상운송
              </button>
              <button 
                type="button"
                @click="orderForm.shippingType = 'air'"
                :class="['flex-1 py-2 px-3 rounded-md text-sm font-medium transition-all ml-1 text-center', orderForm.shippingType === 'air' ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500']"
              >
                항공운송
              </button>
            </div>
          </div>

          <!-- 리패킹 여부 토글 -->
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-3">
              리패킹 서비스
              <button type="button" class="ml-1 text-gray-400 hover:text-gray-600" @click="showRepackingTooltip = !showRepackingTooltip">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"/>
                </svg>
              </button>
            </label>
            <div v-if="showRepackingTooltip" class="text-xs text-gray-600 bg-gray-50 p-2 rounded mb-2">
              포장 상태가 불량하거나 더 안전한 포장이 필요한 경우 별도 요금으로 재포장 서비스를 제공합니다.
            </div>
            <div class="flex bg-gray-100 rounded-lg p-1">
              <button 
                type="button"
                @click="orderForm.repacking = false"
                :class="['flex-1 py-2 px-3 rounded-md text-sm font-medium transition-all', !orderForm.repacking ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500']"
              >
                기본 포장
              </button>
              <button 
                type="button"
                @click="orderForm.repacking = true"
                :class="['flex-1 py-2 px-3 rounded-md text-sm font-medium transition-all ml-1', orderForm.repacking ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500']"
              >
                리패킹 신청
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 수취인 정보 카드 (다중 수취인 지원) -->
      <div class="mx-4 mt-4">
        <div class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"/>
              </svg>
              <h3 class="text-base font-semibold text-gray-900">수취인 정보</h3>
              <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                {{ orderForm.recipients.length }}명
              </span>
            </div>
            <button 
              type="button"
              @click="addRecipient"
              :disabled="orderForm.recipients.length >= maxRecipients"
              :class="['flex items-center gap-1 px-3 py-1.5 text-xs font-medium rounded-lg transition-colors', orderForm.recipients.length >= maxRecipients ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-blue-600 text-white hover:bg-blue-700']"
            >
              <svg class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 20 20">
                <path d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"/>
              </svg>
              추가
            </button>
          </div>

          <!-- 수취인 목록 -->
          <div class="space-y-2">
            <div 
              v-for="(recipient, index) in orderForm.recipients" 
              :key="index" 
              :class="['border rounded-lg transition-all', expandedRecipients.includes(index) ? 'border-blue-300 bg-blue-50/30' : 'border-gray-200 bg-white']"
            >
              <div class="p-3">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                      <span class="text-xs font-semibold text-blue-600">{{ index + 1 }}</span>
                    </div>
                    <div v-if="!expandedRecipients.includes(index) && recipient.name">
                      <p class="text-sm font-medium text-gray-900">{{ recipient.name }}</p>
                      <p class="text-xs text-gray-500">{{ recipient.phone }}</p>
                    </div>
                    <div v-else-if="!expandedRecipients.includes(index)">
                      <p class="text-sm text-gray-500">수취인 정보를 입력해주세요</p>
                    </div>
                  </div>
                  <div class="flex items-center gap-1">
                    <button 
                      type="button"
                      @click="toggleRecipientCard(index)"
                      class="p-1.5 text-gray-400 hover:text-gray-600 rounded hover:bg-gray-100"
                    >
                      <svg 
                        class="w-4 h-4 transform transition-transform"
                        :class="{ 'rotate-180': expandedRecipients.includes(index) }"
                        fill="currentColor" 
                        viewBox="0 0 20 20"
                      >
                        <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"/>
                      </svg>
                    </button>
                    <button 
                      v-if="orderForm.recipients.length > 1"
                      type="button" 
                      @click="removeRecipient(index)"
                      class="p-1.5 text-red-400 hover:text-red-600 rounded hover:bg-red-50"
                    >
                      <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
                      </svg>
                    </button>
                  </div>
                </div>

                <div v-show="expandedRecipients.includes(index)" class="mt-3 space-y-3">
                  <div class="grid grid-cols-2 gap-3">
                    <div>
                      <label class="block text-xs font-medium text-gray-700 mb-1">
                        이름 <span class="text-red-500">*</span>
                      </label>
                      <input 
                        type="text" 
                        class="w-full h-9 px-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm" 
                        v-model="recipient.name"
                        placeholder="수취인 이름"
                        required
                      />
                    </div>
                    
                    <div>
                      <label class="block text-xs font-medium text-gray-700 mb-1">
                        연락처 <span class="text-red-500">*</span>
                      </label>
                      <div class="relative">
                        <span class="absolute left-3 top-1/2 transform -translate-y-1/2 text-xs text-gray-500">+66</span>
                        <input 
                          type="tel" 
                          class="w-full h-9 pl-10 pr-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm" 
                          v-model="recipient.phone"
                          placeholder="8-xxxx-xxxx"
                          inputmode="tel"
                          required
                        />
                      </div>
                    </div>
                  </div>
                  
                  <div>
                    <label class="block text-xs font-medium text-gray-700 mb-1">
                      주소 <span class="text-red-500">*</span>
                    </label>
                    <textarea 
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm resize-none" 
                      v-model="recipient.address"
                      placeholder="예: 123/45 Sukhumvit Rd, Klongtoey, Bangkok"
                      rows="2"
                      required
                    ></textarea>
                  </div>
                  
                  <div>
                    <label class="block text-xs font-medium text-gray-700 mb-1">
                      우편번호 <span class="text-xs text-gray-500">(선택)</span>
                    </label>
                    <input 
                      type="text" 
                      class="w-full h-9 px-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm" 
                      v-model="recipient.postalCode"
                      placeholder="10110 (방콕)"
                      inputmode="numeric"
                      maxlength="5"
                      pattern="[1-9][0-9]{4}"
                    />
                    <p class="text-xs text-gray-500 mt-1">태국 우편번호는 10xxx ~ 96xxx 범위입니다</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 수취인 추가 안내 -->
          <div v-if="orderForm.recipients.length >= maxRecipients" class="mt-3 text-xs text-gray-500 bg-gray-50 p-2 rounded">
            최대 {{ maxRecipients }}명까지 수취인을 등록할 수 있습니다.
          </div>
        </div>
      </div>

      <!-- 배대지 접수 정보 카드 -->
      <div class="mx-4 mt-4">
        <div class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm">
          <div class="flex items-center gap-2 mb-4">
            <svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
              <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z"/>
            </svg>
            <h3 class="text-base font-semibold text-gray-900">배대지 접수 정보</h3>
          </div>
          
          <!-- 접수 방법 선택 -->
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-3">접수 방법 <span class="text-red-500">*</span></label>
            <div class="space-y-2">
              <label :class="['flex items-center p-3 border rounded-lg cursor-pointer transition-all', orderForm.inboundMethod === 'courier' ? 'border-blue-500 bg-blue-50' : 'border-gray-300 hover:border-blue-300']">
                <input 
                  type="radio" 
                  name="inboundMethod" 
                  value="courier" 
                  v-model="orderForm.inboundMethod"
                  class="sr-only"
                />
                <div class="flex items-center gap-3 flex-1">
                  <div :class="['w-4 h-4 rounded-full border-2 flex items-center justify-center', orderForm.inboundMethod === 'courier' ? 'border-blue-500' : 'border-gray-300']">
                    <div v-if="orderForm.inboundMethod === 'courier'" class="w-2 h-2 bg-blue-500 rounded-full"></div>
                  </div>
                  <div>
                    <div class="font-medium text-gray-900 flex items-center gap-2">
                      <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 110 2H4a1 1 0 01-1-1zM14 9a1 1 0 100 2h2a1 1 0 100-2h-2zM3 15a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z"/>
                      </svg>
                      택배 발송
                    </div>
                    <div class="text-sm text-gray-600">우체국, 한진, 롯데 등</div>
                  </div>
                </div>
              </label>
              
              <label :class="['flex items-center p-3 border rounded-lg cursor-pointer transition-all', orderForm.inboundMethod === 'quick' ? 'border-blue-500 bg-blue-50' : 'border-gray-300 hover:border-blue-300']">
                <input 
                  type="radio" 
                  name="inboundMethod" 
                  value="quick" 
                  v-model="orderForm.inboundMethod"
                  class="sr-only"
                />
                <div class="flex items-center gap-3 flex-1">
                  <div :class="['w-4 h-4 rounded-full border-2 flex items-center justify-center', orderForm.inboundMethod === 'quick' ? 'border-blue-500' : 'border-gray-300']">
                    <div v-if="orderForm.inboundMethod === 'quick'" class="w-2 h-2 bg-blue-500 rounded-full"></div>
                  </div>
                  <div>
                    <div class="font-medium text-gray-900">🚗 퀵서비스</div>
                    <div class="text-sm text-gray-600">즉시 픽업 서비스</div>
                  </div>
                </div>
              </label>
              
              <label :class="['flex items-center p-3 border rounded-lg cursor-pointer transition-all', orderForm.inboundMethod === 'other' ? 'border-blue-500 bg-blue-50' : 'border-gray-300 hover:border-blue-300']">
                <input 
                  type="radio" 
                  name="inboundMethod" 
                  value="other" 
                  v-model="orderForm.inboundMethod"
                  class="sr-only"
                />
                <div class="flex items-center gap-3 flex-1">
                  <div :class="['w-4 h-4 rounded-full border-2 flex items-center justify-center', orderForm.inboundMethod === 'other' ? 'border-blue-500' : 'border-gray-300']">
                    <div v-if="orderForm.inboundMethod === 'other'" class="w-2 h-2 bg-blue-500 rounded-full"></div>
                  </div>
                  <div>
                    <div class="font-medium text-gray-900">🚶 직접 방문</div>
                    <div class="text-sm text-gray-600">기타 방법</div>
                  </div>
                </div>
              </label>
            </div>
          </div>

          <!-- 택배사 정보 (택배 선택 시) -->
          <div v-if="orderForm.inboundMethod === 'courier'" class="space-y-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">택배사 <span class="text-red-500">*</span></label>
              <select 
                class="w-full h-11 px-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-base" 
                v-model="orderForm.courierCompany"
                required
              >
                <option value="">택배사를 선택하세요</option>
                <option value="korea_post">우체국 택배</option>
                <option value="hanjin">한진택배</option>
                <option value="lotte">롯데택배</option>
                <option value="cj">CJ대한통운</option>
                <option value="gs25">GS25 택배</option>
                <option value="other">기타</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">송장번호 <span class="text-red-500">*</span></label>
              <input 
                type="text" 
                class="w-full h-11 px-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-base" 
                v-model="orderForm.waybillNo"
                placeholder="택배 송장번호"
                inputmode="numeric"
                required
              />
            </div>
          </div>

          <!-- 퀵서비스 정보 (퀵 선택 시) -->
          <div v-if="orderForm.inboundMethod === 'quick'" class="space-y-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">퀵업체명 <span class="text-red-500">*</span></label>
              <input 
                type="text" 
                class="w-full h-11 px-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-base" 
                v-model="orderForm.quickVendor"
                placeholder="퀵서비스 업체명"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">퀵 연락처 <span class="text-red-500">*</span></label>
              <input 
                type="tel" 
                class="w-full h-11 px-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-base" 
                v-model="orderForm.quickPhone"
                placeholder="퀵서비스 연락처"
                inputmode="tel"
                required
              />
            </div>
          </div>


          <!-- 접수 관련 요청사항 -->
          <div class="mt-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">접수 관련 요청사항</label>
            <textarea 
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm resize-none" 
              v-model="orderForm.inboundNote"
              placeholder="접수 시 특별히 주의사항이나 요청사항이 있으시면 입력해주세요"
              rows="2"
            ></textarea>
          </div>
        </div>
      </div>

      <!-- 품목 정보 카드 -->
      <div class="mx-4 mt-4">
        <div class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm">
          <div class="flex items-center gap-2 mb-4">
            <svg class="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
              <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4z"/>
            </svg>
            <h3 class="text-base font-semibold text-gray-900">품목 정보</h3>
            <span class="text-xs text-gray-500">({{ orderForm.items.length }}개)</span>
          </div>
          <!-- 품목 목록 -->
          <div class="space-y-3">
            <div v-for="(item, index) in orderForm.items" :key="index" class="border border-gray-200 rounded-lg p-3">
              <div class="flex justify-between items-center mb-3">
                <h4 class="text-sm font-medium text-gray-900">품목 {{ index + 1 }}</h4>
                <button 
                  v-if="orderForm.items.length > 1"
                  type="button" 
                  class="p-1 text-red-400 hover:text-red-600"
                  @click="removeItem(index)"
                >
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
                  </svg>
                </button>
              </div>

              <!-- HS Code 검색 -->
              <div class="mb-3">
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  HS Code <span class="text-red-500">*</span>
                </label>
                <div class="flex gap-2">
                  <input 
                    type="text" 
                    class="flex-1 h-10 px-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm" 
                    v-model="item.hsCode"
                    placeholder="HS Code"
                    required
                  />
                  <button 
                    type="button" 
                    class="px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"
                    @click="openHSCodeSearch(index)"
                  >
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M9 3.5a5.5 5.5 0 100 11 5.5 5.5 0 000-11zM2 9a7 7 0 1112.452 4.391l3.328 3.329a.75.75 0 11-1.06 1.06l-3.329-3.328A7 7 0 012 9z" clip-rule="evenodd"/>
                    </svg>
                  </button>
                </div>
              </div>

              <!-- 품목 설명 -->
              <div class="mb-3">
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  품목 설명 <span class="text-red-500">*</span>
                </label>
                <input 
                  type="text" 
                  class="w-full h-10 px-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm" 
                  v-model="item.description"
                  placeholder="품목 설명"
                  required
                />
              </div>

              <!-- 수량과 중량 -->
              <div class="grid grid-cols-2 gap-3 mb-3">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">
                    수량 <span class="text-red-500">*</span>
                  </label>
                  <div class="relative">
                    <input 
                      type="number" 
                      class="w-full h-10 px-3 pr-8 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm" 
                      v-model.number="item.quantity"
                      min="1"
                      inputmode="numeric"
                      required
                    />
                    <span class="absolute right-3 top-1/2 transform -translate-y-1/2 text-xs text-gray-500">개</span>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">
                    중량 <span class="text-red-500">*</span>
                  </label>
                  <div class="relative">
                    <input 
                      type="number" 
                      class="w-full h-10 px-3 pr-8 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm" 
                      v-model.number="item.weight"
                      step="0.01"
                      min="0.01"
                      inputmode="decimal"
                      required
                    />
                    <span class="absolute right-3 top-1/2 transform -translate-y-1/2 text-xs text-gray-500">kg</span>
                  </div>
                </div>
              </div>

              <!-- 치수 (가로/세로/높이) -->
              <div class="mb-3">
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  치수 (cm) <span class="text-red-500">*</span>
                </label>
                <div class="grid grid-cols-3 gap-2">
                  <div class="relative">
                    <input 
                      type="number" 
                      class="w-full h-10 px-3 pr-8 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm text-center" 
                      v-model.number="item.width"
                      step="0.1"
                      min="0.1"
                      inputmode="decimal"
                      placeholder="가로"
                      required
                    />
                    <span class="absolute right-2 top-1/2 transform -translate-y-1/2 text-xs text-gray-500">W</span>
                  </div>
                  <div class="relative">
                    <input 
                      type="number" 
                      class="w-full h-10 px-3 pr-8 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm text-center" 
                      v-model.number="item.height"
                      step="0.1"
                      min="0.1"
                      inputmode="decimal"
                      placeholder="세로"
                      required
                    />
                    <span class="absolute right-2 top-1/2 transform -translate-y-1/2 text-xs text-gray-500">H</span>
                  </div>
                  <div class="relative">
                    <input 
                      type="number" 
                      class="w-full h-10 px-3 pr-8 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm text-center" 
                      v-model.number="item.depth"
                      step="0.1"
                      min="0.1"
                      inputmode="decimal"
                      placeholder="높이"
                      required
                    />
                    <span class="absolute right-2 top-1/2 transform -translate-y-1/2 text-xs text-gray-500">D</span>
                  </div>
                </div>
              </div>

              <!-- 단가와 총액 -->
              <div class="grid grid-cols-2 gap-3 mb-3">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">
                    단가 <span class="text-red-500">*</span>
                  </label>
                  <div class="relative">
                    <input 
                      type="number" 
                      class="w-full h-10 px-3 pr-12 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm" 
                      v-model.number="item.unitPrice"
                      step="0.01"
                      min="0.01"
                      inputmode="decimal"
                      required
                    />
                    <span class="absolute right-3 top-1/2 transform -translate-y-1/2 text-xs text-gray-500">THB</span>
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">총액</label>
                  <div class="relative">
                    <input 
                      type="text" 
                      class="w-full h-10 px-3 pr-12 border border-gray-300 rounded-lg bg-gray-50 text-sm" 
                      :value="((item.quantity || 0) * (item.unitPrice || 0)).toFixed(2)"
                      readonly
                    />
                    <span class="absolute right-3 top-1/2 transform -translate-y-1/2 text-xs text-gray-500">THB</span>
                  </div>
                </div>
              </div>

              <!-- CBM 표시 -->
              <div v-if="item.width && item.height && item.depth" class="bg-blue-50 rounded-lg p-2">
                <div class="flex items-center justify-between">
                  <div class="text-xs text-gray-600">
                    {{ item.width }} × {{ item.height }} × {{ item.depth }} cm
                  </div>
                  <div class="text-sm font-semibold text-blue-700">
                    {{ calculateCBM(item) }} m³
                  </div>
                </div>
              </div>

              <!-- 관세 정보 (간소화) -->
              <div v-if="item.tariffInfo" class="bg-green-50 rounded-lg p-2 mt-2">
                <div class="text-xs text-green-800">
                  <strong>관세율:</strong> {{ getAppliedTariffRate(item.tariffInfo) }}%
                </div>
              </div>
            </div>

            <!-- 품목 추가 버튼 -->
            <button 
              type="button" 
              class="w-full p-3 border-2 border-dashed border-gray-300 rounded-lg text-gray-500 hover:border-blue-500 hover:text-blue-600 flex items-center justify-center gap-2 transition-all text-sm"
              @click="addItem"
            >
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z"/>
              </svg>
              품목 추가
            </button>
          </div>
        </div>
      </div>

      <!-- 실시간 요약 정보 카드 -->
      <div class="mx-4 mt-4">
        <div class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm">
          <h3 class="text-base font-semibold text-gray-900 mb-3">주문 요약</h3>
          
          <!-- CBM 정보 -->
          <div class="mb-3">
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-600">총 CBM</span>
              <div class="flex items-center gap-2">
                <span :class="['text-lg font-bold', isCBMExceeded ? 'text-red-600' : 'text-gray-900']">
                  {{ totalCBM.toFixed(3) }} m³
                </span>
                <span v-if="!isCBMExceeded" class="text-xs text-gray-500">/ 29 m³</span>
              </div>
            </div>
            <div v-if="isCBMExceeded" class="mt-2 p-2 bg-red-50 rounded-lg">
              <div class="flex items-start gap-2">
                <svg class="w-5 h-5 text-red-600 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
                </svg>
                <div class="flex-1">
                  <p class="text-sm font-medium text-red-800">CBM 한도 초과</p>
                  <p class="text-xs text-red-700 mt-1">
                    29m³를 초과하여 자동으로 항공운송으로 전환됩니다.
                  </p>
                </div>
              </div>
            </div>
            <div class="mt-2 bg-gray-50 rounded p-2">
              <div class="w-full bg-gray-200 rounded-full h-2">
                <div 
                  :class="['h-2 rounded-full transition-all', isCBMExceeded ? 'bg-red-600' : 'bg-blue-600']"
                  :style="`width: ${Math.min((totalCBM / 29) * 100, 100)}%`"
                ></div>
              </div>
            </div>
          </div>

          <!-- THB 정보 -->
          <div class="mb-3">
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-600">총 상품가액</span>
              <span :class="['text-lg font-bold', isTHBExceeded ? 'text-orange-600' : 'text-gray-900']">
                ฿ {{ totalTHBValue.toLocaleString() }}
              </span>
            </div>
            <div v-if="isTHBExceeded" class="mt-2 p-2 bg-orange-50 rounded-lg">
              <div class="flex items-start gap-2">
                <svg class="w-5 h-5 text-orange-600 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
                </svg>
                <div class="flex-1">
                  <p class="text-sm font-medium text-orange-800">THB 1,500 초과</p>
                  <p class="text-xs text-orange-700 mt-1">
                    수취인 추가 정보 입력이 필요할 수 있습니다.
                  </p>
                  <button 
                    @click="showTHBModal = true"
                    class="mt-2 text-xs text-orange-800 underline hover:text-orange-900"
                  >
                    추가 정보 입력하기
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 회원 코드 경고 -->
          <div v-if="!authStore.user?.memberCode || authStore.user?.memberCode === 'No code'" class="mb-3">
            <div class="p-2 bg-yellow-50 rounded-lg">
              <div class="flex items-start gap-2">
                <svg class="w-5 h-5 text-yellow-600 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
                </svg>
                <div class="flex-1">
                  <p class="text-sm font-medium text-yellow-800">회원코드 미등록</p>
                  <p class="text-xs text-yellow-700 mt-1">
                    회원코드가 없어 발송이 지연될 수 있습니다.
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- 요약 정보 -->
          <div class="border-t pt-3">
            <div class="grid grid-cols-2 gap-3 text-sm">
              <div>
                <span class="text-gray-500">배송 방식</span>
                <p class="font-medium">{{ orderForm.shippingType === 'air' ? '항공운송' : '해상운송' }}</p>
              </div>
              <div>
                <span class="text-gray-500">리패킹</span>
                <p class="font-medium">{{ orderForm.repacking ? '신청' : '미신청' }}</p>
              </div>
              <div>
                <span class="text-gray-500">수취인</span>
                <p class="font-medium">{{ orderForm.recipients.length }}명</p>
              </div>
              <div>
                <span class="text-gray-500">품목</span>
                <p class="font-medium">{{ orderForm.items.length }}개</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 하단 여백 (고정 버튼을 위한 공간) -->
      <div class="h-24"></div>
    </form>

    <!-- 하단 고정 버튼 -->
    <div class="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 p-4 safe-area-pb">
      <div class="flex gap-3">
        <button 
          type="button" 
          class="flex-1 h-12 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-medium text-sm"
          @click="saveDraft"
          :disabled="loading"
        >
          <div class="flex items-center justify-center gap-2">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path d="M7.707 10.293a1 1 0 10-1.414 1.414l3 3a1 1 0 001.414 0l3-3a1 1 0 00-1.414-1.414L11 11.586V6h5a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2V8a2 2 0 012-2h5v5.586l-1.293-1.293zM9 4a1 1 0 012 0v2H9V4z"/>
            </svg>
            {{ loading ? '저장 중...' : '임시저장' }}
          </div>
        </button>
        <button 
          type="button"
          @click="submitOrder"
          class="flex-2 h-12 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium relative overflow-hidden transition-all text-sm"
          :disabled="isSubmitting"
          :class="{'opacity-75 cursor-not-allowed': isSubmitting}"
          style="flex: 2;"
        >
          <span v-if="!isSubmitting" class="flex items-center justify-center gap-2">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path d="M3.5 6L12 2l8.5 4-8.5 4L3.5 6z"/>
              <path d="M3 10l9-4 9 4-9 4-9-4z"/>
              <path d="M3 14l9-4 9 4-9 4-9-4z"/>
            </svg>
            접수완료
          </span>
          <div v-else class="flex items-center justify-center gap-2">
            <div class="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent"></div>
            <span>{{ savingProgress || '처리 중...' }}</span>
          </div>
        </button>
      </div>
    </div>

    <!-- HS Code Search Modal -->
    <HSCodeSearchModal 
      :show="showHSModal" 
      @close="showHSModal = false"
      @select="selectHSCodeFromModal"
    />

    <!-- THB Threshold Modal -->
    <div v-if="showTHBModal" class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
      <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" aria-hidden="true" @click="showTHBModal = false"></div>
        
        <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
        
        <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div class="sm:flex sm:items-start">
              <div class="mx-auto flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-orange-100 sm:mx-0 sm:h-10 sm:w-10">
                <svg class="h-6 w-6 text-orange-600" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
                </svg>
              </div>
              <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left flex-1">
                <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-title">
                  수취인 추가 정보 입력
                </h3>
                <div class="mt-2">
                  <p class="text-sm text-gray-500">
                    품목 가액이 THB 1,500을 초과하여 세관 신고를 위한 수취인 추가 정보 입력이 필요합니다.
                  </p>
                </div>
                
                <!-- 추가 정보 입력 폼 -->
                <div class="mt-4 space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">
                      수취인 신분증 번호 <span class="text-red-500">*</span>
                    </label>
                    <input 
                      type="text" 
                      v-model="thbAdditionalInfo.nationalId"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                      placeholder="태국 신분증 번호 13자리"
                      maxlength="13"
                      pattern="[0-9]{13}"
                    />
                    <p class="text-xs text-gray-500 mt-1">예: 1234567890123</p>
                  </div>
                  
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">
                      수취인 직업 <span class="text-red-500">*</span>
                    </label>
                    <select 
                      v-model="thbAdditionalInfo.occupation"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                    >
                      <option value="">직업을 선택하세요</option>
                      <option value="employee">회사원</option>
                      <option value="business">사업자</option>
                      <option value="student">학생</option>
                      <option value="housewife">주부</option>
                      <option value="freelancer">프리랜서</option>
                      <option value="retired">퇴직자</option>
                      <option value="other">기타</option>
                    </select>
                  </div>
                  
                  <div v-if="thbAdditionalInfo.occupation === 'other'">
                    <label class="block text-sm font-medium text-gray-700 mb-1">
                      기타 직업 명시 <span class="text-red-500">*</span>
                    </label>
                    <input 
                      type="text" 
                      v-model="thbAdditionalInfo.occupationOther"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                      placeholder="직업을 입력하세요"
                    />
                  </div>
                  
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">
                      수취인과의 관계 <span class="text-red-500">*</span>
                    </label>
                    <select 
                      v-model="thbAdditionalInfo.relationship"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                    >
                      <option value="">관계를 선택하세요</option>
                      <option value="self">본인</option>
                      <option value="family">가족</option>
                      <option value="friend">친구</option>
                      <option value="business">비즈니스</option>
                      <option value="other">기타</option>
                    </select>
                  </div>
                  
                  <div v-if="thbAdditionalInfo.relationship === 'other'">
                    <label class="block text-sm font-medium text-gray-700 mb-1">
                      기타 관계 명시 <span class="text-red-500">*</span>
                    </label>
                    <input 
                      type="text" 
                      v-model="thbAdditionalInfo.relationshipOther"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                      placeholder="관계를 입력하세요"
                    />
                  </div>
                  
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">
                      발송 목적 <span class="text-red-500">*</span>
                    </label>
                    <select 
                      v-model="thbAdditionalInfo.purpose"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                    >
                      <option value="">목적을 선택하세요</option>
                      <option value="gift">선물</option>
                      <option value="personal">개인 사용</option>
                      <option value="business">상업적 목적</option>
                      <option value="sample">샘플</option>
                      <option value="other">기타</option>
                    </select>
                  </div>
                  
                  <div v-if="thbAdditionalInfo.purpose === 'other'">
                    <label class="block text-sm font-medium text-gray-700 mb-1">
                      기타 목적 명시 <span class="text-red-500">*</span>
                    </label>
                    <input 
                      type="text" 
                      v-model="thbAdditionalInfo.purposeOther"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                      placeholder="목적을 입력하세요"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button 
              type="button" 
              @click="saveTHBAdditionalInfo"
              :disabled="!isThbFormValid"
              :class="[
                'w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 text-base font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:ml-3 sm:w-auto sm:text-sm',
                isThbFormValid ? 'bg-blue-600 hover:bg-blue-700' : 'bg-gray-400 cursor-not-allowed'
              ]"
            >
              저장
            </button>
            <button 
              type="button" 
              @click="showTHBModal = false"
              class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            >
              취소
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useErrorHandler } from '@/composables/useErrorHandler'
import { useToast } from '@/composables/useToast'
import { ordersApi } from '@/utils/api'
import { USER_TYPE } from '@/types'
import HSCodeSearchModal from '@/components/order/HSCodeSearchModal.vue'
import InlineMessage from '@/components/ui/InlineMessage.vue'

const router = useRouter()
const authStore = useAuthStore()
const { handleApiError, handleCBMExceeded, handleTHBThresholdExceeded, handleMemberCodeMissing, validateForm, validationRules } = useErrorHandler()
const { success: showSuccess, businessWarning } = useToast()

const loading = ref(false)
const isSubmitting = ref(false)
const savingProgress = ref('')
const autoSaveTimer = ref<number | null>(null)
const lastAutoSave = ref<Date | null>(null)
const validationErrors = ref<string[]>([])
const businessWarnings = ref<Array<{title: string, message: string}>>([])

const showHSModal = ref(false)
const hsSearchTerm = ref('')
const currentItemIndex = ref<number>(-1)

// THB Modal variables
const showTHBModal = ref(false)
const thbAdditionalInfo = reactive({
  nationalId: '',
  occupation: '',
  occupationOther: '',
  relationship: '',
  relationshipOther: '',
  purpose: '',
  purposeOther: ''
})

// Helper functions for error handling
const removeWarning = (index: number) => {
  businessWarnings.value.splice(index, 1)
}

const clearValidationErrors = () => {
  validationErrors.value = []
}

const addBusinessWarning = (title: string, message: string) => {
  // Avoid duplicates
  if (!businessWarnings.value.some(w => w.title === title && w.message === message)) {
    businessWarnings.value.push({ title, message })
  }
}

const checkBusinessRules = () => {
  businessWarnings.value = [] // Clear previous warnings
  
  // CBM check
  const cbm = totalCBM.value
  if (cbm > 29) {
    addBusinessWarning(
      'CBM 임계값 초과',
      `총 CBM이 ${cbm.toFixed(2)}m³로 29m³를 초과하여 해상운송에서 항공운송으로 자동 전환됩니다.`
    )
    // Auto-switch to air shipping
    orderForm.shippingType = 'air'
  }
  
  // THB value check
  const totalThb = orderForm.items.reduce((sum, item) => sum + ((item.unitPrice || 0) * (item.quantity || 0)), 0)
  if (totalThb > 1500) {
    addBusinessWarning(
      'THB 임계값 초과',
      `품목 가액이 ${totalThb.toLocaleString()} THB로 1,500 THB를 초과하여 수취인 추가 정보 입력이 필요합니다.`
    )
  }
  
  // Member code check
  if (!authStore.user?.memberCode || authStore.user?.memberCode === 'No code') {
    addBusinessWarning(
      '회원코드 미기재',
      '회원코드가 없어 발송이 지연될 수 있습니다. 관리자에게 문의해주세요.'
    )
  }
}

const orderForm = reactive({
  trackingNumber: '',
  shippingType: 'sea',
  country: 'TH', // 태국 고정
  repacking: false, // 리패킹 여부
  postalCode: '',
  recipientName: '',
  recipientPhone: '',
  recipientAddress: '',
  recipientPostalCode: '',
  recipients: [ // 다중 수취인 지원
    {
      name: '',
      phone: '',
      address: '',
      postalCode: ''
    }
  ],
  items: [{
    hsCode: '',
    description: '',
    quantity: 1,
    weight: 0,
    width: 0,
    height: 0,
    depth: 0,
    unitPrice: 0,
    cbm: 0
  }],
  // 배대지 접수 정보
  inboundMethod: 'courier', // courier, quick, other
  courierCompany: '',
  waybillNo: '',
  quickVendor: '',
  quickPhone: '',
  inboundLocationId: null,
  inboundNote: '',
  specialRequests: '',
  // THB 1,500 초과 시 추가 정보
  thbAdditionalInfo: null
})

// 새 상태 변수들
const showRepackingTooltip = ref(false)
const showRecipientModal = ref(false)
const currentRecipientIndex = ref(0)
const expandedRecipients = ref([0]) // 첫 번째 수취인은 펼쳐진 상태로 시작
const maxRecipients = ref(3) // 최대 수취인 수 (어드민에서 설정 가능)

const hsCodes = [
  { code: '1905.31', description: 'Korean Chocolate Sticks' },
  { code: '1806.32', description: 'Chocolate Snacks' },
  { code: '3304.10', description: 'Lipstick Set' },
  { code: '3304.20', description: 'Foundation' },
  { code: '0902.30', description: 'Korean Traditional Tea Set' },
  { code: '6204.42', description: 'Women Clothing Set' },
  { code: '3401.11', description: 'Toiletries Set' },
  { code: '0902.10', description: 'Green Tea Gift Set' }
]

const filteredHSCodes = computed(() => {
  if (!hsSearchTerm.value) return hsCodes
  return hsCodes.filter(code => 
    code.description.toLowerCase().includes(hsSearchTerm.value.toLowerCase()) ||
    code.code.includes(hsSearchTerm.value)
  )
})

const getUserTypeText = computed(() => {
  const userType = authStore.user?.userType
  switch(userType) {
    case USER_TYPE.GENERAL:
      return '개인 고객'
    case USER_TYPE.CORPORATE:
      return '기업 고객'
    case USER_TYPE.PARTNER:
      return '파트너'
    case USER_TYPE.ADMIN:
      return '관리자'
    default:
      return '일반 사용자'
  }
})

const totalCBM = computed(() => {
  return orderForm.items.reduce((sum, item) => {
    const cbm = item.width && item.height && item.depth
      ? (item.width * item.height * item.depth) / 1000000
      : 0
    return sum + cbm
  }, 0)
})

const isCBMExceeded = computed(() => totalCBM.value > 29)
const totalTHBValue = computed(() => {
  return orderForm.items.reduce((sum, item) => sum + ((item.unitPrice || 0) * (item.quantity || 0)), 0)
})
const isTHBExceeded = computed(() => totalTHBValue.value > 1500)

// THB Modal validation
const isThbFormValid = computed(() => {
  const info = thbAdditionalInfo
  return (
    info.nationalId && 
    info.nationalId.length === 13 && 
    /^[0-9]{13}$/.test(info.nationalId) &&
    info.occupation &&
    (info.occupation !== 'other' || info.occupationOther) &&
    info.relationship &&
    (info.relationship !== 'other' || info.relationshipOther) &&
    info.purpose &&
    (info.purpose !== 'other' || info.purposeOther)
  )
})

// THB Modal functions
const saveTHBAdditionalInfo = () => {
  if (!isThbFormValid.value) {
    businessWarning('validation', '모든 필수 정보를 입력해주세요.')
    return
  }
  
  // Add to order form or save separately
  orderForm.thbAdditionalInfo = { ...thbAdditionalInfo }
  
  showTHBModal.value = false
  showSuccess('수취인 추가 정보가 저장되었습니다.')
}

// 태국 전용 시스템이므로 getPostalCodeGuide 함수 제거됨 - 불필요

const calculateCBM = (item) => {
  if (item.width && item.height && item.depth) {
    return ((item.width * item.height * item.depth) / 1000000).toFixed(6)
  }
  return 0
}

const addItem = () => {
  orderForm.items.push({
    hsCode: '',
    description: '',
    quantity: 1,
    weight: 0,
    width: 0,
    height: 0,
    depth: 0,
    unitPrice: 0,
    cbm: 0
  })
}

const removeItem = (index) => {
  if (orderForm.items.length > 1) {
    orderForm.items.splice(index, 1)
  }
}

// 수취인 관리 함수들
const addRecipient = () => {
  if (orderForm.recipients.length < maxRecipients.value) {
    const newIndex = orderForm.recipients.length
    orderForm.recipients.push({
      name: '',
      phone: '',
      address: '',
      postalCode: ''
    })
    // 새 수취인 카드를 펼쳐진 상태로 추가
    expandedRecipients.value.push(newIndex)
  }
}

const removeRecipient = (index) => {
  if (orderForm.recipients.length > 1) {
    orderForm.recipients.splice(index, 1)
    // 펼쳐진 목록에서도 제거하고 인덱스 조정
    expandedRecipients.value = expandedRecipients.value
      .filter(i => i !== index)
      .map(i => i > index ? i - 1 : i)
  }
}

const toggleRecipientCard = (index) => {
  const expandedIndex = expandedRecipients.value.indexOf(index)
  if (expandedIndex > -1) {
    expandedRecipients.value.splice(expandedIndex, 1)
  } else {
    expandedRecipients.value.push(index)
  }
}

const openHSCodeSearch = (itemIndex: number) => {
  currentItemIndex.value = itemIndex
  showHSModal.value = true
}

const selectHSCodeFromModal = (selectedItem: any) => {
  if (currentItemIndex.value >= 0 && currentItemIndex.value < orderForm.items.length) {
    const item = orderForm.items[currentItemIndex.value]
    
    // HS Code 정보 업데이트
    item.hsCode = selectedItem.hsCode
    item.description = selectedItem.koreanName || selectedItem.description
    
    // 관세율 정보가 있으면 저장
    if (selectedItem.tariffInfo) {
      item.tariffInfo = selectedItem.tariffInfo
    }
    
    // 관세 계산 결과가 있으면 저장
    if (selectedItem.dutyResult) {
      item.dutyResult = selectedItem.dutyResult
    }
    
    // 영문명이 있으면 저장
    if (selectedItem.englishName) {
      item.englishName = selectedItem.englishName
    }
  }
  
  showHSModal.value = false
  currentItemIndex.value = -1
}

const getAppliedTariffRate = (tariffInfo: any): number => {
  // WTO 관세율이 있으면 우선 적용, 없으면 기본 관세율
  return tariffInfo.wtoRate > 0 ? tariffInfo.wtoRate : tariffInfo.basicRate
}

// Field validation helper (unused - validation now handled by validateOrderForm)
// Removed unused validateField function and fieldValidation references

// Enhanced form validation using the new error handler  
const validateOrderForm = (): { isValid: boolean; errors: Record<string, string> } => {
  const formData = {
    recipientName: orderForm.recipientName,
    recipientPhone: orderForm.recipientPhone,
    recipientAddress: orderForm.recipientAddress,
    country: orderForm.country,
    recipientPostalCode: orderForm.recipientPostalCode,
    items: orderForm.items
  }

  const rules = {
    recipients: [{
      validator: (recipients) => {
        if (!recipients || recipients.length === 0) return '최소 1명의 수취인을 등록해주세요.'
        
        for (let i = 0; i < recipients.length; i++) {
          const recipient = recipients[i]
          if (!recipient.name || recipient.name.trim().length < 2) return `수취인 ${i + 1}: 이름을 2자 이상 입력해주세요.`
          if (!recipient.phone) return `수취인 ${i + 1}: 연락처를 입력해주세요.`
          if (!validationRules.thailandPhone().validator(recipient.phone)) return `수취인 ${i + 1}: 올바른 태국 전화번호를 입력해주세요.`
          if (!recipient.address || recipient.address.trim().length < 10) return `수취인 ${i + 1}: 주소를 자세히 입력해주세요.`
          if (recipient.postalCode && !validationRules.thailandPostalCode().validator(recipient.postalCode)) return `수취인 ${i + 1}: 올바른 태국 우편번호를 입력해주세요.`
        }
        return true
      },
      message: '수취인 정보를 확인해주세요.'
    }],
    inboundMethod: [validationRules.required('접수 방법을 선택해주세요.')],
    items: [{
      validator: (items) => {
        if (!items || items.length === 0) return '최소 1개 이상의 품목을 추가해주세요.'
        
        for (let i = 0; i < items.length; i++) {
          const item = items[i]
          if (!item.hsCode) return `품목 ${i + 1}: HS 코드를 입력해주세요.`
          if (!validationRules.hsCode().validator(item.hsCode)) return `품목 ${i + 1}: 올바른 HS 코드 형식이 아닙니다.`
          if (!item.description) return `품목 ${i + 1}: 품목 설명을 입력해주세요.`
          if (!item.quantity || item.quantity <= 0) return `품목 ${i + 1}: 수량을 올바르게 입력해주세요.`
          if (!item.weight || item.weight <= 0) return `품목 ${i + 1}: 중량을 올바르게 입력해주세요.`
          if (!item.width || item.width <= 0) return `품목 ${i + 1}: 가로 치수를 입력해주세요.`
          if (!item.height || item.height <= 0) return `품목 ${i + 1}: 세로 치수를 입력해주세요.`
          if (!item.depth || item.depth <= 0) return `품목 ${i + 1}: 높이 치수를 입력해주세요.`
          if (!item.unitPrice || item.unitPrice <= 0) return `품목 ${i + 1}: 단가를 올바르게 입력해주세요.`
        }
        return true
      },
      message: '품목 정보를 확인해주세요.'
    }]
  }

  return validateForm(formData, rules)
}

const submitOrder = async () => {
  isSubmitting.value = true
  loading.value = true
  savingProgress.value = '데이터 검증 중...'
  validationErrors.value = []
  businessWarnings.value = []
  
  try {
    // Form validation using new error handler
    const validation = validateOrderForm()
    if (!validation.isValid) {
      validationErrors.value = Object.values(validation.errors)
      loading.value = false
      isSubmitting.value = false
      savingProgress.value = ''
      return
    }

    // Calculate CBM for all items
    orderForm.items.forEach(item => {
      item.cbm = parseFloat(calculateCBM(item))
    })
    
    // Check business rules and show warnings
    checkBusinessRules()
    
    // Prepare order data for backend API
    const orderData = {
      userId: authStore.user?.id,
      shippingType: orderForm.shippingType,
      country: orderForm.country,
      // 첫 번째 수취인을 메인 수취인으로 설정
      recipientName: orderForm.recipients[0]?.name || '',
      recipientPhone: orderForm.recipients[0]?.phone || '',
      recipientAddress: orderForm.recipients[0]?.address || '',
      recipientPostalCode: orderForm.recipients[0]?.postalCode || '',
      // 추가 수취인 정보
      additionalRecipients: orderForm.recipients.slice(1).map(r => ({
        name: r.name,
        phone: r.phone,
        address: r.address,
        postalCode: r.postalCode
      })),
      // 배대지 접수 정보
      inboundMethod: orderForm.inboundMethod,
      courierCompany: orderForm.courierCompany,
      waybillNo: orderForm.waybillNo,
      quickVendor: orderForm.quickVendor,
      quickPhone: orderForm.quickPhone,
      inboundNote: orderForm.inboundNote,
      // 리패킹 정보
      repacking: orderForm.repacking,
      // 품목 정보
      orderItems: orderForm.items.map(item => ({
        hsCode: item.hsCode,
        description: item.description,
        englishName: item.englishName || '',
        quantity: item.quantity,
        weight: item.weight,
        width: item.width,
        height: item.height,
        depth: item.depth,
        unitPrice: item.unitPrice,
        cbm: parseFloat(calculateCBM(item)),
        tariffInfo: item.tariffInfo || null,
        dutyResult: item.dutyResult || null
      })),
      specialRequests: orderForm.specialRequests || '',
      // 비즈니스 규칙 플래그
      totalCbm: totalCBM.value,
      totalThbValue: totalTHBValue.value,
      cbmExceeded: isCBMExceeded.value,
      thbExceeded: isTHBExceeded.value,
      noMemberCode: !authStore.user?.memberCode || authStore.user?.memberCode === 'No code'
    }

    console.log('Submitting order data:', orderData)

    // Submit order to backend
    const response = await ordersApi.createOrder(orderData)
    
    console.log('Order creation response:', response)

    if (response.success) {
      showSuccess('주문 접수 완료', '주문이 성공적으로 접수되었습니다.')
      
      // Clear draft data on success
      localStorage.removeItem('orderDraft')
      
      setTimeout(() => {
        // Navigate to order detail page using order number
        if (response.order && response.order.orderNumber) {
          router.push('/orders/' + response.order.orderNumber)
        } else {
          router.push('/orders')
        }
      }, 2000)
    } else {
      handleApiError({ response: { data: response, status: 400 } }, '주문 접수')
    }
  } catch (err) {
    console.error('Order submission error:', err)
    handleApiError(err, '주문 접수')
  } finally {
    loading.value = false
    isSubmitting.value = false
    savingProgress.value = ''
  }
}

const saveDraft = async () => {
  try {
    localStorage.setItem('orderDraft', JSON.stringify(orderForm))
    lastAutoSave.value = new Date()
    success.value = '임시저장되었습니다.'
    setTimeout(() => success.value = '', 3000)
  } catch (error) {
    console.error('임시저장 실패:', error)
  }
}

// Auto-save functionality
const autoSave = () => {
  if (autoSaveTimer.value) {
    clearTimeout(autoSaveTimer.value)
  }
  
  autoSaveTimer.value = setTimeout(() => {
    localStorage.setItem('orderDraft', JSON.stringify(orderForm))
    lastAutoSave.value = new Date()
    console.log('자동 저장됨:', new Date().toLocaleTimeString())
  }, 3000) as unknown as number
}

// Watch for changes and auto-save
watch(
  () => orderForm,
  () => {
    autoSave()
  },
  { deep: true }
)

// CBM 초과 시 자동으로 항공운송 전환
watch(isCBMExceeded, (exceeded) => {
  if (exceeded && orderForm.shippingType === 'sea') {
    orderForm.shippingType = 'air'
    businessWarnings.value = []
    addBusinessWarning(
      'CBM 한도 초과로 배송방식 변경',
      `총 CBM이 ${totalCBM.value.toFixed(2)}m³로 29m³를 초과하여 항공운송으로 자동 전환되었습니다.`
    )
  }
})

// THB 1,500 초과 시 추가 정보 입력 안내 (한 번만 표시)
const thbModalShown = ref(false)
watch(isTHBExceeded, (exceeded) => {
  if (exceeded && !orderForm.thbAdditionalInfo && !thbModalShown.value) {
    thbModalShown.value = true
    setTimeout(() => {
      if (!orderForm.thbAdditionalInfo) {
        businessWarning('thb', `품목 가액이 THB 1,500을 초과했습니다. 수취인 추가 정보 입력을 권장합니다.`)
      }
    }, 1000)
  }
  
  if (!exceeded) {
    thbModalShown.value = false
  }
})

// 품목 변경 시 실시간 CBM 계산
watch(() => orderForm.items, () => {
  updateItemCBM()
}, { deep: true })

const goBack = () => {
  router.go(-1)
}

// Calculate CBM on mounted and when dimensions change
const updateItemCBM = () => {
  orderForm.items.forEach(item => {
    if (item.width && item.height && item.depth) {
      item.cbm = parseFloat(calculateCBM(item))
    }
  })
}

onMounted(() => {
  // Restore draft if exists
  try {
    const draftData = localStorage.getItem('orderDraft')
    if (draftData) {
      const draft = JSON.parse(draftData)
      Object.assign(orderForm, draft)
    }
  } catch (error) {
    console.error('임시저장 데이터 복원 실패:', error)
  }
  
  // Initial CBM calculation
  updateItemCBM()
})
</script>

