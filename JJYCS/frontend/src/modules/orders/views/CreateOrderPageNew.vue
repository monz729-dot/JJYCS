<template>
  <div class="create-order-page">
    <!-- Header -->
    <div class="page-header">
      <button @click="goBack" class="back-button">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      
      <div class="header-content">
        <h1 class="page-title">새 주문 접수</h1>
        <p class="page-subtitle">주문 정보를 입력해주세요</p>
      </div>
    </div>

    <!-- Progress Steps -->
    <div class="progress-container">
      <div class="progress-steps">
        <div 
          v-for="(step, index) in steps" 
          :key="index"
          class="step-item"
          :class="{ 
            'active': currentStep === index,
            'completed': currentStep > index 
          }"
        >
          <div class="step-circle">
            <svg v-if="currentStep > index" class="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span v-else>{{ index + 1 }}</span>
          </div>
          <span class="step-label">{{ step.label }}</span>
        </div>
      </div>
    </div>

    <!-- Alert Messages -->
    <div v-if="alertMessage" class="alert-container">
      <div class="alert" :class="alertType">
        <div class="alert-icon">
          <svg v-if="alertType === 'warning'" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
          </svg>
          <svg v-else-if="alertType === 'error'" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
          </svg>
          <svg v-else-if="alertType === 'success'" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
          </svg>
          <svg v-else class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
          </svg>
        </div>
        <div class="alert-content">
          <p class="alert-title">{{ alertMessage.title }}</p>
          <p class="alert-description">{{ alertMessage.description }}</p>
        </div>
        <button @click="dismissAlert" class="alert-dismiss">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- Form Container -->
    <div class="form-container">
      <!-- Step 1: Basic Information -->
      <div v-show="currentStep === 0" class="form-step">
        <div class="step-header">
          <div class="step-icon">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <div>
            <h2 class="step-title">기본 정보</h2>
            <p class="step-description">주문의 기본 정보를 입력해주세요</p>
          </div>
        </div>

        <div class="form-content">
          <!-- Member Code Warning -->
          <div v-if="!authStore.user?.memberCode || authStore.user.memberCode === 'No code'" class="warning-card">
            <div class="warning-header">
              <svg class="w-5 h-5 text-orange-500" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
              </svg>
              <span class="text-orange-700 font-medium">회원코드 미등록 안내</span>
            </div>
            <p class="text-sm text-orange-600 mt-2">
              회원코드가 등록되지 않은 경우 발송이 지연될 수 있습니다.
              빠른 처리를 원하시면 고객센터에 문의해주세요.
            </p>
          </div>

          <!-- User Information Display -->
          <div class="user-info-card">
            <div class="user-avatar">
              <div class="avatar-placeholder">
                {{ (authStore.user?.name || 'U').charAt(0).toUpperCase() }}
              </div>
            </div>
            <div class="user-details">
              <h3 class="user-name">{{ authStore.user?.name }}</h3>
              <p class="user-type">{{ getUserTypeLabel(authStore.user?.userType) }}</p>
              <p class="user-code">
                회원코드: {{ authStore.user?.memberCode || 'No code' }}
              </p>
            </div>
          </div>

          <!-- Tracking Number Input -->
          <div class="form-group">
            <label class="form-label required">
              우체국 송장번호
              <span class="required-mark">*</span>
            </label>
            <input
              v-model="orderForm.trackingNumber"
              type="text"
              class="form-input"
              placeholder="EE123456789KR 형식으로 입력해주세요"
              pattern="^[A-Z]{2}[0-9]{9}[A-Z]{2}$"
              required
              @blur="validateTrackingNumber"
            />
            <p class="input-help">
              EMS 송장번호가 없으면 주문을 접수할 수 없습니다.
            </p>
          </div>

          <!-- Order Type Selection -->
          <div class="form-group">
            <label class="form-label">배송 방식</label>
            <div class="radio-group">
              <label class="radio-option">
                <input
                  v-model="orderForm.orderType"
                  type="radio"
                  value="sea"
                  class="radio-input"
                />
                <div class="radio-content">
                  <div class="radio-title">해상 배송</div>
                  <div class="radio-description">경제적, 2-3주 소요</div>
                </div>
              </label>
              <label class="radio-option">
                <input
                  v-model="orderForm.orderType"
                  type="radio"
                  value="air"
                  class="radio-input"
                />
                <div class="radio-content">
                  <div class="radio-title">항공 배송</div>
                  <div class="radio-description">빠름, 5-7일 소요</div>
                </div>
              </label>
            </div>
            <p class="input-help">
              CBM이 29를 초과하면 자동으로 항공 배송으로 변경됩니다.
            </p>
          </div>

          <!-- Special Instructions -->
          <div class="form-group">
            <label class="form-label">특수 처리 요청</label>
            <div class="checkbox-group">
              <label class="checkbox-option">
                <input
                  v-model="orderForm.fragile"
                  type="checkbox"
                  class="checkbox-input"
                />
                <span class="checkbox-label">취급주의 (Fragile)</span>
              </label>
              <label class="checkbox-option">
                <input
                  v-model="orderForm.urgent"
                  type="checkbox"
                  class="checkbox-input"
                />
                <span class="checkbox-label">긴급배송</span>
              </label>
            </div>
          </div>

          <!-- Notes -->
          <div class="form-group">
            <label class="form-label">추가 요청사항</label>
            <textarea
              v-model="orderForm.notes"
              class="form-textarea"
              rows="3"
              placeholder="배송 시 요청사항이 있으면 입력해주세요"
            ></textarea>
          </div>
        </div>
      </div>

      <!-- Step 2: Recipient Information -->
      <div v-show="currentStep === 1" class="form-step">
        <div class="step-header">
          <div class="step-icon">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
            </svg>
          </div>
          <div>
            <h2 class="step-title">수취인 정보</h2>
            <p class="step-description">태국 현지 수취인 정보를 입력해주세요</p>
          </div>
        </div>

        <div class="form-content">
          <div class="form-group">
            <label class="form-label required">
              수취인 성명 (태국어/영어)
              <span class="required-mark">*</span>
            </label>
            <input
              v-model="orderForm.recipient.name"
              type="text"
              class="form-input"
              placeholder="Somchai Jaidee"
              required
            />
          </div>

          <div class="form-group">
            <label class="form-label required">
              전화번호
              <span class="required-mark">*</span>
            </label>
            <input
              v-model="orderForm.recipient.phone"
              type="tel"
              class="form-input"
              placeholder="066-123-4567"
              pattern="[0-9]{3}-[0-9]{3}-[0-9]{4}"
              required
            />
          </div>

          <div class="form-group">
            <label class="form-label required">
              주소
              <span class="required-mark">*</span>
            </label>
            <textarea
              v-model="orderForm.recipient.address"
              class="form-textarea"
              rows="3"
              placeholder="123/45 Sukhumvit Road, Wattana, Bangkok 10110"
              required
            ></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">이메일</label>
            <input
              v-model="orderForm.recipient.email"
              type="email"
              class="form-input"
              placeholder="somchai@example.com"
            />
          </div>

          <!-- Extra Recipient Info for THB 1500+ -->
          <div v-if="requiresExtraRecipientInfo" class="extra-recipient-info">
            <div class="warning-card">
              <div class="warning-header">
                <svg class="w-5 h-5 text-orange-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
                </svg>
                <span class="text-orange-700 font-medium">추가 수취인 정보 필요</span>
              </div>
              <p class="text-sm text-orange-600 mt-2">
                품목 가격이 THB 1,500을 초과하여 추가 정보가 필요합니다.
              </p>
            </div>

            <div class="form-group">
              <label class="form-label required">
                신분증 번호
                <span class="required-mark">*</span>
              </label>
              <input
                v-model="orderForm.recipient.idNumber"
                type="text"
                class="form-input"
                placeholder="1234567890123"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">직업</label>
              <input
                v-model="orderForm.recipient.occupation"
                type="text"
                class="form-input"
                placeholder="직업을 입력해주세요"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Step 3: Items -->
      <div v-show="currentStep === 2" class="form-step">
        <div class="step-header">
          <div class="step-icon">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
            </svg>
          </div>
          <div>
            <h2 class="step-title">품목 정보</h2>
            <p class="step-description">배송할 품목을 등록해주세요</p>
          </div>
        </div>

        <div class="form-content">
          <!-- Items List -->
          <div class="items-container">
            <div
              v-for="(item, index) in orderForm.items"
              :key="index"
              class="item-card"
            >
              <div class="item-header">
                <span class="item-number">품목 {{ index + 1 }}</span>
                <button
                  v-if="orderForm.items.length > 1"
                  @click="removeItem(index)"
                  type="button"
                  class="remove-item-btn"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                  </svg>
                </button>
              </div>

              <div class="item-content">
                <div class="form-row">
                  <div class="form-group">
                    <label class="form-label required">
                      품목명
                      <span class="required-mark">*</span>
                    </label>
                    <input
                      v-model="item.name"
                      type="text"
                      class="form-input"
                      placeholder="예: 화장품"
                      required
                    />
                  </div>

                  <div class="form-group">
                    <label class="form-label required">
                      수량
                      <span class="required-mark">*</span>
                    </label>
                    <input
                      v-model.number="item.quantity"
                      type="number"
                      min="1"
                      class="form-input"
                      required
                      @input="calculateItemTotals(index)"
                    />
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label class="form-label required">
                      단가 (KRW)
                      <span class="required-mark">*</span>
                    </label>
                    <input
                      v-model.number="item.unitPrice"
                      type="number"
                      min="0"
                      class="form-input"
                      placeholder="원 단위로 입력"
                      required
                      @input="calculateItemTotals(index)"
                    />
                  </div>

                  <div class="form-group">
                    <label class="form-label">
                      HS 코드
                    </label>
                    <input
                      v-model="item.hsCode"
                      type="text"
                      class="form-input"
                      placeholder="6자리 HS 코드"
                      pattern="[0-9]{6,10}"
                      @blur="validateHsCode(index)"
                    />
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label class="form-label required">
                      무게 (kg)
                      <span class="required-mark">*</span>
                    </label>
                    <input
                      v-model.number="item.weight"
                      type="number"
                      step="0.1"
                      min="0"
                      class="form-input"
                      required
                      @input="calculateItemTotals(index)"
                    />
                  </div>

                  <div class="form-group">
                    <label class="form-label">
                      치수 (cm)
                    </label>
                    <div class="dimensions-input">
                      <input
                        v-model.number="item.length"
                        type="number"
                        min="0"
                        class="form-input dimension-input"
                        placeholder="길이"
                        @input="calculateItemTotals(index)"
                      />
                      <span class="dimension-separator">×</span>
                      <input
                        v-model.number="item.width"
                        type="number"
                        min="0"
                        class="form-input dimension-input"
                        placeholder="너비"
                        @input="calculateItemTotals(index)"
                      />
                      <span class="dimension-separator">×</span>
                      <input
                        v-model.number="item.height"
                        type="number"
                        min="0"
                        class="form-input dimension-input"
                        placeholder="높이"
                        @input="calculateItemTotals(index)"
                      />
                    </div>
                  </div>
                </div>

                <!-- Item Summary -->
                <div v-if="item.quantity && item.unitPrice" class="item-summary">
                  <div class="summary-row">
                    <span>소계:</span>
                    <span class="font-semibold">{{ formatCurrency(item.quantity * item.unitPrice) }}</span>
                  </div>
                  <div v-if="item.length && item.width && item.height" class="summary-row">
                    <span>CBM:</span>
                    <span class="font-semibold">{{ calculateItemCBM(item).toFixed(6) }} m³</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Add Item Button -->
          <button
            @click="addItem"
            type="button"
            class="add-item-btn"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
            </svg>
            품목 추가
          </button>

          <!-- Order Summary -->
          <div v-if="orderSummary" class="order-summary">
            <h3 class="summary-title">주문 요약</h3>
            <div class="summary-content">
              <div class="summary-row">
                <span>총 품목 수:</span>
                <span>{{ orderForm.items.length }}개</span>
              </div>
              <div class="summary-row">
                <span>총 금액:</span>
                <span class="font-semibold">{{ formatCurrency(orderSummary.subtotal) }}</span>
              </div>
              <div class="summary-row">
                <span>총 무게:</span>
                <span>{{ orderSummary.totalWeight }} kg</span>
              </div>
              <div class="summary-row">
                <span>총 CBM:</span>
                <span class="font-semibold">{{ orderSummary.totalCBM.toFixed(6) }} m³</span>
              </div>
              <div class="summary-row">
                <span>배송 방식:</span>
                <span class="font-semibold text-blue-600">{{ orderSummary.shippingType === 'air' ? '항공' : '해상' }}</span>
              </div>
            </div>

            <!-- CBM Warning -->
            <div v-if="orderSummary.cbmExceeded" class="cbm-warning">
              <div class="warning-header">
                <svg class="w-5 h-5 text-orange-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
                </svg>
                <span class="text-orange-700 font-medium">CBM 초과 안내</span>
              </div>
              <p class="text-sm text-orange-600 mt-2">
                총 CBM이 29m³을 초과하여 자동으로 항공 배송으로 변경됩니다.
              </p>
            </div>

            <!-- THB Warning -->
            <div v-if="orderSummary.requiresExtraRecipientInfo" class="thb-warning">
              <div class="warning-header">
                <svg class="w-5 h-5 text-orange-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
                </svg>
                <span class="text-orange-700 font-medium">추가 정보 필요</span>
              </div>
              <p class="text-sm text-orange-600 mt-2">
                품목 가격이 THB 1,500을 초과하여 추가 수취인 정보가 필요합니다.
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Step 4: Review -->
      <div v-show="currentStep === 3" class="form-step">
        <div class="step-header">
          <div class="step-icon">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <div>
            <h2 class="step-title">주문 확인</h2>
            <p class="step-description">입력하신 정보를 확인해주세요</p>
          </div>
        </div>

        <div class="form-content">
          <!-- Review Content -->
          <div class="review-sections">
            <div class="review-section">
              <h4 class="review-title">기본 정보</h4>
              <div class="review-content">
                <div class="review-item">
                  <span class="review-label">송장번호:</span>
                  <span class="review-value">{{ orderForm.trackingNumber }}</span>
                </div>
                <div class="review-item">
                  <span class="review-label">배송 방식:</span>
                  <span class="review-value">{{ orderForm.orderType === 'air' ? '항공' : '해상' }}</span>
                </div>
              </div>
            </div>

            <div class="review-section">
              <h4 class="review-title">수취인 정보</h4>
              <div class="review-content">
                <div class="review-item">
                  <span class="review-label">성명:</span>
                  <span class="review-value">{{ orderForm.recipient.name }}</span>
                </div>
                <div class="review-item">
                  <span class="review-label">전화번호:</span>
                  <span class="review-value">{{ orderForm.recipient.phone }}</span>
                </div>
                <div class="review-item">
                  <span class="review-label">주소:</span>
                  <span class="review-value">{{ orderForm.recipient.address }}</span>
                </div>
              </div>
            </div>

            <div class="review-section">
              <h4 class="review-title">품목 정보</h4>
              <div class="review-content">
                <div v-for="(item, index) in orderForm.items" :key="index" class="review-item-card">
                  <div class="item-header">
                    <span class="item-name">{{ item.name }}</span>
                    <span class="item-price">{{ formatCurrency(item.quantity * item.unitPrice) }}</span>
                  </div>
                  <div class="item-details">
                    <span>수량: {{ item.quantity }}개</span>
                    <span>무게: {{ item.weight }}kg</span>
                    <span v-if="item.length && item.width && item.height">
                      치수: {{ item.length }}×{{ item.width }}×{{ item.height }}cm
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Final Summary -->
          <div v-if="orderSummary" class="final-summary">
            <h4 class="summary-title">최종 요약</h4>
            <div class="summary-content">
              <div class="summary-row major">
                <span>총 금액:</span>
                <span class="font-bold text-lg">{{ formatCurrency(orderSummary.subtotal) }}</span>
              </div>
              <div class="summary-row">
                <span>총 CBM:</span>
                <span>{{ orderSummary.totalCBM.toFixed(6) }} m³</span>
              </div>
              <div class="summary-row">
                <span>배송 방식:</span>
                <span class="font-semibold">{{ orderSummary.shippingType === 'air' ? '항공 배송' : '해상 배송' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Navigation Buttons -->
    <div class="navigation-buttons">
      <button
        v-if="currentStep > 0"
        @click="previousStep"
        type="button"
        class="nav-button secondary"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
        이전
      </button>
      
      <button
        v-if="currentStep < steps.length - 1"
        @click="nextStep"
        type="button"
        class="nav-button primary"
        :disabled="!isCurrentStepValid"
      >
        다음
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
        </svg>
      </button>
      
      <button
        v-if="currentStep === steps.length - 1"
        @click="submitOrder"
        type="button"
        class="nav-button primary"
        :disabled="loading || !isFormValid"
      >
        <svg v-if="loading" class="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <span v-if="!loading">주문 접수</span>
        <span v-else>처리 중...</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../../stores/auth'
import { useOrdersStore } from '../../../stores/orders'
import { useToast } from '../../../composables/useToast'

const router = useRouter()
const authStore = useAuthStore()
const ordersStore = useOrdersStore()
const { showToast } = useToast()

// 현재 스텝
const currentStep = ref(0)
const loading = ref(false)

// 스텝 정의
const steps = [
  { label: '기본 정보', icon: 'info' },
  { label: '수취인 정보', icon: 'location' },
  { label: '품목 등록', icon: 'package' },
  { label: '주문 확인', icon: 'check' }
]

// 폼 데이터
const orderForm = ref({
  trackingNumber: '',
  orderType: 'sea',
  fragile: false,
  urgent: false,
  notes: '',
  recipient: {
    name: '',
    phone: '',
    address: '',
    email: '',
    idNumber: '',
    occupation: ''
  },
  items: [
    {
      name: '',
      quantity: 1,
      unitPrice: 0,
      hsCode: '',
      weight: 0,
      length: 0,
      width: 0,
      height: 0
    }
  ]
})

// 알림 메시지
const alertMessage = ref<{title: string, description: string} | null>(null)
const alertType = ref<'info' | 'warning' | 'error' | 'success'>('info')

// 주문 요약 계산
const orderSummary = computed(() => {
  if (!orderForm.value.items.length) return null
  
  return ordersStore.calculateOrderTotals(orderForm.value.items)
})

// 추가 수취인 정보 필요 여부
const requiresExtraRecipientInfo = computed(() => {
  return orderSummary.value?.requiresExtraRecipientInfo || false
})

// 현재 스텝 유효성 검사
const isCurrentStepValid = computed(() => {
  switch (currentStep.value) {
    case 0: // 기본 정보
      return orderForm.value.trackingNumber.length > 0
    case 1: // 수취인 정보
      return orderForm.value.recipient.name &&
             orderForm.value.recipient.phone &&
             orderForm.value.recipient.address &&
             (!requiresExtraRecipientInfo.value || orderForm.value.recipient.idNumber)
    case 2: // 품목 정보
      return orderForm.value.items.every(item => 
        item.name && item.quantity > 0 && item.unitPrice > 0 && item.weight > 0
      )
    case 3: // 확인
      return true
    default:
      return false
  }
})

// 전체 폼 유효성 검사
const isFormValid = computed(() => {
  return steps.every((_, index) => {
    const temp = currentStep.value
    currentStep.value = index
    const valid = isCurrentStepValid.value
    currentStep.value = temp
    return valid
  })
})

// 사용자 타입 라벨
const getUserTypeLabel = (userType?: string) => {
  switch (userType) {
    case 'ADMIN': return '관리자'
    case 'PARTNER': return '파트너'
    case 'CORPORATE': return '기업회원'
    case 'WAREHOUSE': return '창고직원'
    default: return '일반회원'
  }
}

// 금액 포맷팅
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(amount)
}

// CBM 계산
const calculateItemCBM = (item: any) => {
  if (!item.length || !item.width || !item.height) return 0
  return (item.length * item.width * item.height * item.quantity) / 1000000
}

// 아이템 총계 계산
const calculateItemTotals = (index: number) => {
  // 실시간으로 CBM 재계산 및 배송 방식 업데이트 트리거
  // Vue의 반응성에 의해 자동으로 계산됨
}

// HS 코드 검증
const validateHsCode = async (index: number) => {
  const hsCode = orderForm.value.items[index].hsCode
  if (!hsCode || hsCode.length < 6) return
  
  // TODO: HS 코드 검증 API 호출
  try {
    // const result = await hsCodeApi.validate(hsCode)
    console.log('HS 코드 검증:', hsCode)
  } catch (error) {
    showToast('HS 코드 검증에 실패했습니다.', 'warning')
  }
}

// 송장번호 검증
const validateTrackingNumber = async () => {
  const trackingNumber = orderForm.value.trackingNumber
  if (!trackingNumber) return
  
  const pattern = /^[A-Z]{2}[0-9]{9}[A-Z]{2}$/
  if (!pattern.test(trackingNumber)) {
    showAlert('올바르지 않은 송장번호 형식', 'EMS 송장번호는 EE123456789KR 형식이어야 합니다.', 'warning')
    return
  }
  
  // TODO: EMS 송장번호 존재 여부 확인
  try {
    // const result = await emsApi.validate(trackingNumber)
    console.log('송장번호 검증:', trackingNumber)
  } catch (error) {
    showAlert('송장번호 검증 실패', '송장번호를 확인할 수 없습니다. 다시 확인해주세요.', 'error')
  }
}

// 품목 추가
const addItem = () => {
  orderForm.value.items.push({
    name: '',
    quantity: 1,
    unitPrice: 0,
    hsCode: '',
    weight: 0,
    length: 0,
    width: 0,
    height: 0
  })
}

// 품목 제거
const removeItem = (index: number) => {
  if (orderForm.value.items.length > 1) {
    orderForm.value.items.splice(index, 1)
  }
}

// 다음 스텝
const nextStep = () => {
  if (!isCurrentStepValid.value) {
    showToast('필수 정보를 입력해주세요.', 'warning')
    return
  }
  
  if (currentStep.value < steps.length - 1) {
    currentStep.value++
  }
}

// 이전 스텝
const previousStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 알림 표시
const showAlert = (title: string, description: string, type: typeof alertType.value) => {
  alertMessage.value = { title, description }
  alertType.value = type
}

// 알림 닫기
const dismissAlert = () => {
  alertMessage.value = null
}

// 주문 제출
const submitOrder = async () => {
  if (!isFormValid.value) {
    showToast('모든 필수 정보를 입력해주세요.', 'error')
    return
  }
  
  loading.value = true
  
  try {
    const orderData = {
      ...orderForm.value,
      summary: orderSummary.value
    }
    
    const result = await ordersStore.createOrder(orderData)
    
    if (result.success) {
      showToast('주문이 성공적으로 접수되었습니다.', 'success')
      router.push(`/orders/${result.data.id}`)
    } else {
      showToast(result.error || '주문 접수에 실패했습니다.', 'error')
    }
  } catch (error) {
    showToast('주문 접수 중 오류가 발생했습니다.', 'error')
    console.error('주문 접수 오류:', error)
  } finally {
    loading.value = false
  }
}

// 뒤로가기
const goBack = () => {
  router.go(-1)
}

// CBM 초과 감지 및 경고
watch(orderSummary, (newSummary) => {
  if (newSummary?.cbmExceeded) {
    orderForm.value.orderType = 'air'
    showAlert(
      'CBM 초과 안내',
      '총 CBM이 29m³을 초과하여 자동으로 항공 배송으로 변경되었습니다.',
      'warning'
    )
  }
  
  if (newSummary?.requiresExtraRecipientInfo && currentStep.value === 1) {
    showAlert(
      '추가 정보 필요',
      '품목 가격이 THB 1,500을 초과하여 추가 수취인 정보가 필요합니다.',
      'info'
    )
  }
}, { deep: true })

// 컴포넌트 마운트
onMounted(() => {
  // 회원코드 없음 경고
  if (!authStore.user?.memberCode || authStore.user.memberCode === 'No code') {
    showAlert(
      '회원코드 미등록 안내',
      '회원코드가 등록되지 않은 경우 발송이 지연될 수 있습니다.',
      'warning'
    )
  }
})
</script>

<style scoped>
.create-order-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
  background: #f8fafc;
  min-height: 100vh;
}

/* Header */
.page-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.back-button {
  padding: 0.5rem;
  border-radius: 0.5rem;
  color: #3b82f6;
  transition: all 0.2s;
}

.back-button:hover {
  background: #eff6ff;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.page-subtitle {
  color: #64748b;
  font-size: 0.875rem;
  margin: 0.25rem 0 0 0;
}

/* Progress Steps */
.progress-container {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.progress-steps {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.progress-steps::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 2px;
  background: #e2e8f0;
  transform: translateY(-50%);
  z-index: 1;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
}

.step-circle {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  background: #e2e8f0;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 0.5rem;
  transition: all 0.3s;
}

.step-item.active .step-circle {
  background: #3b82f6;
  color: white;
}

.step-item.completed .step-circle {
  background: #10b981;
  color: white;
}

.step-label {
  font-size: 0.75rem;
  text-align: center;
  color: #64748b;
  transition: all 0.3s;
}

.step-item.active .step-label,
.step-item.completed .step-label {
  color: #1e293b;
  font-weight: 500;
}

/* Alert Messages */
.alert-container {
  margin-bottom: 1.5rem;
}

.alert {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: 0.75rem;
  border: 1px solid;
}

.alert.info {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #1d4ed8;
}

.alert.warning {
  background: #fffbeb;
  border-color: #fde68a;
  color: #d97706;
}

.alert.error {
  background: #fef2f2;
  border-color: #fecaca;
  color: #dc2626;
}

.alert.success {
  background: #f0fdf4;
  border-color: #bbf7d0;
  color: #16a34a;
}

.alert-icon {
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: 600;
  margin: 0 0 0.25rem 0;
}

.alert-description {
  margin: 0;
  opacity: 0.9;
}

.alert-dismiss {
  flex-shrink: 0;
  padding: 0.25rem;
  border-radius: 0.25rem;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.alert-dismiss:hover {
  opacity: 1;
}

/* Form Container */
.form-container {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.form-step {
  min-height: 400px;
}

/* Step Header */
.step-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
}

.step-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 0.75rem;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.step-description {
  color: #64748b;
  margin: 0.25rem 0 0 0;
}

/* Form Content */
.form-content {
  space-y: 1.5rem;
}

/* Warning Cards */
.warning-card {
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 0.75rem;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.warning-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

/* User Info Card */
.user-info-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: #f8fafc;
  border-radius: 0.75rem;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.user-avatar .avatar-placeholder {
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1.25rem;
}

.user-name {
  font-weight: 600;
  margin: 0;
  color: #1e293b;
}

.user-type {
  color: #3b82f6;
  font-size: 0.875rem;
  margin: 0.25rem 0;
}

.user-code {
  color: #64748b;
  font-size: 0.75rem;
  margin: 0;
}

/* Form Groups */
.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.form-label.required {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.required-mark {
  color: #ef4444;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  transition: all 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  resize: vertical;
  min-height: 80px;
  transition: all 0.2s;
}

.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.input-help {
  font-size: 0.75rem;
  color: #64748b;
  margin-top: 0.25rem;
}

/* Form Rows */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

/* Radio Groups */
.radio-group {
  display: grid;
  gap: 0.75rem;
}

.radio-option {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.radio-option:hover {
  border-color: #3b82f6;
  background: #f8fafc;
}

.radio-option:has(.radio-input:checked) {
  border-color: #3b82f6;
  background: #eff6ff;
}

.radio-input {
  margin-top: 0.125rem;
}

.radio-content {
  flex: 1;
}

.radio-title {
  font-weight: 500;
  color: #1e293b;
}

.radio-description {
  font-size: 0.875rem;
  color: #64748b;
  margin-top: 0.25rem;
}

/* Checkbox Groups */
.checkbox-group {
  display: grid;
  gap: 0.5rem;
}

.checkbox-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.checkbox-input {
  /* Styled by browser default */
}

.checkbox-label {
  color: #374151;
}

/* Dimensions Input */
.dimensions-input {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.dimension-input {
  flex: 1;
  min-width: 0;
}

.dimension-separator {
  color: #64748b;
  font-weight: 500;
}

/* Items */
.items-container {
  space-y: 1.5rem;
  margin-bottom: 1.5rem;
}

.item-card {
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  padding: 1.5rem;
  background: #fafafa;
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.item-number {
  font-weight: 600;
  color: #374151;
}

.remove-item-btn {
  padding: 0.25rem;
  color: #ef4444;
  border-radius: 0.25rem;
  transition: all 0.2s;
}

.remove-item-btn:hover {
  background: #fef2f2;
}

.item-content {
  space-y: 1rem;
}

.item-summary {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.25rem 0;
}

.add-item-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.75rem;
  border: 2px dashed #d1d5db;
  border-radius: 0.5rem;
  color: #64748b;
  font-weight: 500;
  transition: all 0.2s;
}

.add-item-btn:hover {
  border-color: #3b82f6;
  color: #3b82f6;
  background: #f8fafc;
}

/* Order Summary */
.order-summary {
  background: #f8fafc;
  border-radius: 0.75rem;
  padding: 1.5rem;
  margin-top: 2rem;
}

.summary-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 1rem 0;
}

.summary-content {
  space-y: 0.5rem;
}

.cbm-warning,
.thb-warning {
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 0.5rem;
  padding: 1rem;
  margin-top: 1rem;
}

.cbm-warning .warning-header,
.thb-warning .warning-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* Review Sections */
.review-sections {
  space-y: 1.5rem;
}

.review-section {
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  padding: 1.5rem;
}

.review-title {
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 1rem 0;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.review-content {
  space-y: 0.75rem;
}

.review-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-label {
  color: #64748b;
  font-weight: 500;
}

.review-value {
  color: #1e293b;
  text-align: right;
}

.review-item-card {
  background: #f8fafc;
  border-radius: 0.5rem;
  padding: 1rem;
  margin-bottom: 0.75rem;
}

.review-item-card .item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.item-name {
  font-weight: 500;
  color: #1e293b;
}

.item-price {
  font-weight: 600;
  color: #3b82f6;
}

.item-details {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
  color: #64748b;
}

/* Final Summary */
.final-summary {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
  border-radius: 0.75rem;
  padding: 1.5rem;
  margin-top: 2rem;
}

.final-summary .summary-title {
  color: white;
  margin-bottom: 1rem;
}

.final-summary .summary-row.major {
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 0.5rem;
}

/* Navigation Buttons */
.navigation-buttons {
  display: flex;
  gap: 1rem;
  justify-content: space-between;
  padding: 1rem 0;
}

.nav-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 500;
  transition: all 0.2s;
  min-width: 120px;
  justify-content: center;
}

.nav-button.primary {
  background: #3b82f6;
  color: white;
}

.nav-button.primary:hover:not(:disabled) {
  background: #2563eb;
}

.nav-button.primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.nav-button.secondary {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.nav-button.secondary:hover {
  background: #e5e7eb;
}

/* 모바일 최적화 */
@media (max-width: 640px) {
  .create-order-page {
    padding: 0.75rem;
  }
  
  .form-container {
    padding: 1rem;
  }
  
  .step-header {
    flex-direction: column;
    text-align: center;
  }
  
  .progress-steps {
    flex-direction: column;
    gap: 1rem;
  }
  
  .progress-steps::before {
    display: none;
  }
  
  .navigation-buttons {
    flex-direction: column;
  }
  
  .user-info-card {
    flex-direction: column;
    text-align: center;
  }
  
  .item-details {
    flex-direction: column;
    gap: 0.25rem;
  }
}

/* 애니메이션 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-step {
  animation: fadeIn 0.3s ease;
}

/* 스크롤바 */
.items-container::-webkit-scrollbar {
  width: 4px;
}

.items-container::-webkit-scrollbar-track {
  background: #f1f5f9;
}

.items-container::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}
</style>