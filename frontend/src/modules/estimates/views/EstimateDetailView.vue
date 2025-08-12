<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- Loading State -->
      <div v-if="isLoading" class="flex justify-center items-center py-12">
        <div class="flex items-center">
          <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span class="text-gray-600">{{ $t('estimates.detail.loading') }}</span>
        </div>
      </div>

      <!-- Error State -->
      <div v-else-if="!estimate" class="text-center py-12">
        <ExclamationTriangleIcon class="mx-auto h-12 w-12 text-gray-400" />
        <h3 class="mt-2 text-sm font-medium text-gray-900">{{ $t('estimates.detail.not_found') }}</h3>
        <p class="mt-1 text-sm text-gray-500">{{ $t('estimates.detail.not_found_description') }}</p>
        <div class="mt-6">
          <button
            type="button"
            class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
            @click="goBack"
          >
            {{ $t('estimates.detail.back_to_list') }}
          </button>
        </div>
      </div>

      <!-- Main Content -->
      <div v-else>
        <!-- Header -->
        <div class="mb-8">
          <div class="flex items-center justify-between">
            <div>
              <div class="flex items-center space-x-3">
                <button
                  type="button"
                  class="inline-flex items-center text-sm text-gray-500 hover:text-gray-700"
                  @click="goBack"
                >
                  <ArrowLeftIcon class="h-4 w-4 mr-1" />
                  {{ $t('common.back') }}
                </button>
              </div>
              <h1 class="mt-2 text-3xl font-bold text-gray-900">
                {{ estimate.estimateNumber }}
                <span class="ml-2 inline-flex px-3 py-1 text-sm font-semibold rounded-full"
                      :class="getStatusBadgeClass(estimate.status)">
                  {{ $t(`estimates.status.${estimate.status}`) }}
                </span>
              </h1>
              <p class="mt-1 text-sm text-gray-600">
                {{ $t(`estimates.version.${estimate.version === 1 ? 'first' : 'second'}`) }} 견적서
                <span v-if="estimate.isExpired" class="ml-2 text-red-600 font-medium">
                  ({{ $t('estimates.messages.expired_estimate') }})
                </span>
              </p>
            </div>
            
            <!-- Action Buttons -->
            <div class="flex space-x-3">
              <button
                type="button"
                class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                @click="downloadPDF"
              >
                <DocumentArrowDownIcon class="h-4 w-4 mr-2" />
                {{ $t('estimates.detail.download_pdf') }}
              </button>
              <button
                type="button"
                class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                @click="printEstimate"
              >
                <PrinterIcon class="h-4 w-4 mr-2" />
                {{ $t('estimates.detail.print_estimate') }}
              </button>
              <button
                v-if="canCreateSecondEstimate"
                type="button"
                class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                @click="createSecondEstimate"
              >
                <PlusIcon class="h-4 w-4 mr-2" />
                {{ $t('estimates.detail.create_second_estimate') }}
              </button>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <!-- Main Content -->
          <div class="lg:col-span-2 space-y-6">
            <!-- Basic Information -->
            <div class="bg-white shadow rounded-lg overflow-hidden">
              <div class="px-6 py-4 border-b border-gray-200">
                <h2 class="text-lg font-medium text-gray-900">견적 정보</h2>
              </div>
              <div class="px-6 py-4">
                <dl class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <dt class="text-sm font-medium text-gray-500">{{ $t('estimates.detail.estimate_number') }}</dt>
                    <dd class="mt-1 text-sm text-gray-900">{{ estimate.estimateNumber }}</dd>
                  </div>
                  <div>
                    <dt class="text-sm font-medium text-gray-500">{{ $t('estimates.detail.version') }}</dt>
                    <dd class="mt-1 text-sm text-gray-900">{{ $t(`estimates.version.${estimate.version === 1 ? 'first' : 'second'}`) }}</dd>
                  </div>
                  <div>
                    <dt class="text-sm font-medium text-gray-500">{{ $t('estimates.detail.created') }}</dt>
                    <dd class="mt-1 text-sm text-gray-900">{{ formatDate(estimate.createdAt) }}</dd>
                  </div>
                  <div>
                    <dt class="text-sm font-medium text-gray-500">{{ $t('estimates.detail.valid_until') }}</dt>
                    <dd class="mt-1 text-sm text-gray-900" :class="{ 'text-red-600': estimate.isExpired }">
                      {{ formatDate(estimate.validUntil) }}
                    </dd>
                  </div>
                </dl>
              </div>
            </div>

            <!-- Order Information -->
            <div class="bg-white shadow rounded-lg overflow-hidden">
              <div class="px-6 py-4 border-b border-gray-200">
                <h2 class="text-lg font-medium text-gray-900">{{ $t('estimates.detail.order_info') }}</h2>
              </div>
              <div class="px-6 py-4">
                <dl class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <dt class="text-sm font-medium text-gray-500">주문번호</dt>
                    <dd class="mt-1">
                      <router-link
                        :to="{ name: 'OrderDetail', params: { id: estimate.order.id } }"
                        class="text-sm text-blue-600 hover:text-blue-900"
                      >
                        {{ estimate.order.orderCode }}
                      </router-link>
                    </dd>
                  </div>
                  <div>
                    <dt class="text-sm font-medium text-gray-500">배송 방식</dt>
                    <dd class="mt-1 text-sm text-gray-900">
                      {{ estimate.order.orderType === 'sea' ? '해상배송' : '항공배송' }}
                    </dd>
                  </div>
                  <div>
                    <dt class="text-sm font-medium text-gray-500">총 CBM</dt>
                    <dd class="mt-1 text-sm text-gray-900">{{ estimate.order.totalCbm?.toFixed(3) }} m³</dd>
                  </div>
                  <div>
                    <dt class="text-sm font-medium text-gray-500">총 중량</dt>
                    <dd class="mt-1 text-sm text-gray-900">{{ estimate.order.totalWeight?.toFixed(2) }} kg</dd>
                  </div>
                </dl>
              </div>
            </div>

            <!-- Customer Information -->
            <div class="bg-white shadow rounded-lg overflow-hidden">
              <div class="px-6 py-4 border-b border-gray-200">
                <h2 class="text-lg font-medium text-gray-900">{{ $t('estimates.detail.customer_info') }}</h2>
              </div>
              <div class="px-6 py-4">
                <dl class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <dt class="text-sm font-medium text-gray-500">고객명</dt>
                    <dd class="mt-1 text-sm text-gray-900">{{ estimate.customer.name }}</dd>
                  </div>
                  <div>
                    <dt class="text-sm font-medium text-gray-500">회사</dt>
                    <dd class="mt-1 text-sm text-gray-900">{{ estimate.customer.company || '-' }}</dd>
                  </div>
                  <div>
                    <dt class="text-sm font-medium text-gray-500">이메일</dt>
                    <dd class="mt-1 text-sm text-gray-900">{{ estimate.customer.email }}</dd>
                  </div>
                  <div>
                    <dt class="text-sm font-medium text-gray-500">연락처</dt>
                    <dd class="mt-1 text-sm text-gray-900">{{ estimate.customer.phone }}</dd>
                  </div>
                </dl>
              </div>
            </div>

            <!-- Cost Breakdown -->
            <div class="bg-white shadow rounded-lg overflow-hidden">
              <div class="px-6 py-4 border-b border-gray-200">
                <h2 class="text-lg font-medium text-gray-900">{{ $t('estimates.detail.cost_breakdown') }}</h2>
              </div>
              <div class="px-6 py-4">
                <dl class="space-y-3">
                  <div class="flex justify-between">
                    <dt class="text-sm text-gray-600">{{ $t('estimates.detail.shipping_cost') }}</dt>
                    <dd class="text-sm font-medium text-gray-900">{{ formatCurrency(estimate.costs.shippingCost) }}</dd>
                  </div>
                  <div class="flex justify-between">
                    <dt class="text-sm text-gray-600">{{ $t('estimates.detail.local_delivery') }}</dt>
                    <dd class="text-sm font-medium text-gray-900">{{ formatCurrency(estimate.costs.localDelivery) }}</dd>
                  </div>
                  <div class="flex justify-between">
                    <dt class="text-sm text-gray-600">{{ $t('estimates.detail.handling_fee') }}</dt>
                    <dd class="text-sm font-medium text-gray-900">{{ formatCurrency(estimate.costs.handlingFee) }}</dd>
                  </div>
                  <div v-if="estimate.costs.repackingFee > 0" class="flex justify-between">
                    <dt class="text-sm text-gray-600">{{ $t('estimates.detail.repacking_fee') }}</dt>
                    <dd class="text-sm font-medium text-gray-900">{{ formatCurrency(estimate.costs.repackingFee) }}</dd>
                  </div>
                  <div class="flex justify-between">
                    <dt class="text-sm text-gray-600">{{ $t('estimates.detail.customs_fee') }}</dt>
                    <dd class="text-sm font-medium text-gray-900">{{ formatCurrency(estimate.costs.customsFee) }}</dd>
                  </div>
                  <div v-if="estimate.costs.insurance > 0" class="flex justify-between">
                    <dt class="text-sm text-gray-600">{{ $t('estimates.detail.insurance') }}</dt>
                    <dd class="text-sm font-medium text-gray-900">{{ formatCurrency(estimate.costs.insurance) }}</dd>
                  </div>
                  
                  <div class="border-t pt-3">
                    <div class="flex justify-between">
                      <dt class="text-sm text-gray-600">{{ $t('estimates.detail.subtotal') }}</dt>
                      <dd class="text-sm font-medium text-gray-900">{{ formatCurrency(estimate.subtotal) }}</dd>
                    </div>
                  </div>
                  
                  <div v-if="estimate.tax > 0" class="flex justify-between">
                    <dt class="text-sm text-gray-600">{{ $t('estimates.detail.tax') }} (7%)</dt>
                    <dd class="text-sm font-medium text-gray-900">{{ formatCurrency(estimate.tax) }}</dd>
                  </div>
                  
                  <div v-if="estimate.discount > 0" class="flex justify-between">
                    <dt class="text-sm text-gray-600">{{ $t('estimates.detail.discount') }}</dt>
                    <dd class="text-sm font-medium text-red-600">-{{ formatCurrency(estimate.discount) }}</dd>
                  </div>
                  
                  <div class="border-t pt-3">
                    <div class="flex justify-between">
                      <dt class="text-lg font-bold text-gray-900">{{ $t('estimates.detail.total_amount') }}</dt>
                      <dd class="text-lg font-bold text-blue-600">{{ formatCurrency(estimate.totalAmount) }}</dd>
                    </div>
                  </div>
                </dl>
              </div>
            </div>

            <!-- Additional Services -->
            <div v-if="estimate.additionalServices?.length > 0" class="bg-white shadow rounded-lg overflow-hidden">
              <div class="px-6 py-4 border-b border-gray-200">
                <h2 class="text-lg font-medium text-gray-900">{{ $t('estimates.detail.additional_services') }}</h2>
              </div>
              <div class="px-6 py-4">
                <ul class="space-y-2">
                  <li v-for="service in estimate.additionalServices" :key="service.name" class="flex items-center">
                    <CheckIcon class="h-4 w-4 text-green-500 mr-2" />
                    <span class="text-sm text-gray-900">{{ service.name }}</span>
                    <span v-if="service.cost > 0" class="ml-auto text-sm font-medium text-gray-900">
                      {{ formatCurrency(service.cost) }}
                    </span>
                  </li>
                </ul>
              </div>
            </div>

            <!-- Special Notes -->
            <div v-if="estimate.specialNotes" class="bg-white shadow rounded-lg overflow-hidden">
              <div class="px-6 py-4 border-b border-gray-200">
                <h2 class="text-lg font-medium text-gray-900">{{ $t('estimates.detail.special_notes') }}</h2>
              </div>
              <div class="px-6 py-4">
                <p class="text-sm text-gray-700 whitespace-pre-wrap">{{ estimate.specialNotes }}</p>
              </div>
            </div>
          </div>

          <!-- Sidebar -->
          <div class="space-y-6">
            <!-- Approval Section -->
            <div v-if="showApprovalSection" class="bg-white shadow rounded-lg overflow-hidden">
              <div class="px-6 py-4 border-b border-gray-200">
                <h2 class="text-lg font-medium text-gray-900">{{ $t('estimates.detail.approval_section') }}</h2>
              </div>
              <div class="px-6 py-4 space-y-4">
                <!-- Approval Note -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    {{ $t('estimates.detail.approval_note') }}
                  </label>
                  <textarea
                    v-model="approvalNote"
                    rows="3"
                    class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                    :placeholder="$t('estimates.detail.approval_note_placeholder')"
                  />
                </div>

                <!-- Action Buttons -->
                <div class="space-y-2">
                  <button
                    type="button"
                    class="w-full inline-flex justify-center items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
                    :disabled="isProcessing"
                    @click="showApprovalModal = true"
                  >
                    <CheckIcon class="h-4 w-4 mr-2" />
                    {{ isProcessing ? $t('estimates.detail.approving') : $t('estimates.detail.approve_estimate') }}
                  </button>
                  
                  <button
                    type="button"
                    class="w-full inline-flex justify-center items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                    :disabled="isProcessing"
                    @click="showRevisionModal = true"
                  >
                    <PencilIcon class="h-4 w-4 mr-2" />
                    {{ $t('estimates.detail.request_revision') }}
                  </button>
                  
                  <button
                    type="button"
                    class="w-full inline-flex justify-center items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-red-700 bg-white hover:bg-red-50"
                    :disabled="isProcessing"
                    @click="showRejectionModal = true"
                  >
                    <XMarkIcon class="h-4 w-4 mr-2" />
                    {{ isProcessing ? $t('estimates.detail.rejecting') : $t('estimates.detail.reject_estimate') }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div v-if="canEditEstimate || canCancelEstimate" class="bg-white shadow rounded-lg overflow-hidden">
              <div class="px-6 py-4 border-b border-gray-200">
                <h2 class="text-lg font-medium text-gray-900">{{ $t('estimates.detail.actions') }}</h2>
              </div>
              <div class="px-6 py-4 space-y-2">
                <button
                  v-if="canEditEstimate"
                  type="button"
                  class="w-full inline-flex justify-center items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                  @click="editEstimate"
                >
                  <PencilIcon class="h-4 w-4 mr-2" />
                  {{ $t('estimates.detail.edit_estimate') }}
                </button>
                
                <button
                  v-if="canCancelEstimate"
                  type="button"
                  class="w-full inline-flex justify-center items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-red-700 bg-white hover:bg-red-50"
                  @click="showCancelModal = true"
                >
                  <TrashIcon class="h-4 w-4 mr-2" />
                  {{ $t('estimates.detail.cancel_estimate') }}
                </button>
              </div>
            </div>

            <!-- Approval History -->
            <div v-if="estimate.approvalHistory?.length > 0" class="bg-white shadow rounded-lg overflow-hidden">
              <div class="px-6 py-4 border-b border-gray-200">
                <h2 class="text-lg font-medium text-gray-900">{{ $t('estimates.detail.approval_history') }}</h2>
              </div>
              <div class="px-6 py-4">
                <div class="space-y-3">
                  <div v-for="history in estimate.approvalHistory" :key="history.id" class="flex items-start space-x-3">
                    <div class="flex-shrink-0">
                      <CheckIcon v-if="history.action === 'approved'" class="h-5 w-5 text-green-500" />
                      <XMarkIcon v-else-if="history.action === 'rejected'" class="h-5 w-5 text-red-500" />
                      <PencilIcon v-else class="h-5 w-5 text-orange-500" />
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-900">
                        {{ $t(`estimates.detail.${history.action}_by`, { name: history.userName }) }}
                      </p>
                      <p class="text-sm text-gray-500">{{ formatDate(history.createdAt) }}</p>
                      <p v-if="history.note" class="text-sm text-gray-700 mt-1">{{ history.note }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Modals -->
        <!-- Approval Modal -->
        <div v-if="showApprovalModal" class="fixed inset-0 z-50 overflow-y-auto">
          <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="showApprovalModal = false"></div>
            <div class="relative inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
              <div class="flex items-center">
                <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100">
                  <CheckIcon class="h-6 w-6 text-green-600" />
                </div>
                <div class="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left">
                  <h3 class="text-lg leading-6 font-medium text-gray-900">견적 승인</h3>
                  <div class="mt-2">
                    <p class="text-sm text-gray-500">{{ $t('estimates.messages.confirm_approve') }}</p>
                  </div>
                </div>
              </div>
              <div class="mt-5 sm:mt-4 sm:flex sm:flex-row-reverse">
                <button
                  type="button"
                  class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-green-600 text-base font-medium text-white hover:bg-green-700 sm:ml-3 sm:w-auto sm:text-sm"
                  :disabled="isProcessing"
                  @click="approveEstimate"
                >
                  {{ isProcessing ? $t('estimates.detail.approving') : $t('estimates.detail.approve_estimate') }}
                </button>
                <button
                  type="button"
                  class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 sm:mt-0 sm:w-auto sm:text-sm"
                  @click="showApprovalModal = false"
                >
                  {{ $t('common.cancel') }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Rejection Modal -->
        <div v-if="showRejectionModal" class="fixed inset-0 z-50 overflow-y-auto">
          <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="showRejectionModal = false"></div>
            <div class="relative inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
              <div class="flex items-center">
                <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
                  <XMarkIcon class="h-6 w-6 text-red-600" />
                </div>
                <div class="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left">
                  <h3 class="text-lg leading-6 font-medium text-gray-900">견적 거부</h3>
                  <div class="mt-2">
                    <p class="text-sm text-gray-500">{{ $t('estimates.messages.confirm_reject') }}</p>
                  </div>
                </div>
              </div>
              <div class="mt-5 sm:mt-4 sm:flex sm:flex-row-reverse">
                <button
                  type="button"
                  class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-red-600 text-base font-medium text-white hover:bg-red-700 sm:ml-3 sm:w-auto sm:text-sm"
                  :disabled="isProcessing"
                  @click="rejectEstimate"
                >
                  {{ isProcessing ? $t('estimates.detail.rejecting') : $t('estimates.detail.reject_estimate') }}
                </button>
                <button
                  type="button"
                  class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 sm:mt-0 sm:w-auto sm:text-sm"
                  @click="showRejectionModal = false"
                >
                  {{ $t('common.cancel') }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Revision Modal -->
        <div v-if="showRevisionModal" class="fixed inset-0 z-50 overflow-y-auto">
          <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="showRevisionModal = false"></div>
            <div class="relative inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
              <div>
                <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-orange-100">
                  <PencilIcon class="h-6 w-6 text-orange-600" />
                </div>
                <div class="mt-3 text-center sm:mt-5">
                  <h3 class="text-lg leading-6 font-medium text-gray-900">수정 요청</h3>
                  <div class="mt-2">
                    <textarea
                      v-model="revisionNote"
                      rows="4"
                      class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                      :placeholder="$t('estimates.detail.revision_note_placeholder')"
                    />
                  </div>
                </div>
              </div>
              <div class="mt-5 sm:mt-6 sm:grid sm:grid-cols-2 sm:gap-3 sm:grid-flow-row-dense">
                <button
                  type="button"
                  class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-orange-600 text-base font-medium text-white hover:bg-orange-700 sm:col-start-2 sm:text-sm"
                  :disabled="isProcessing || !revisionNote.trim()"
                  @click="requestRevision"
                >
                  {{ isProcessing ? $t('estimates.detail.requesting_revision') : $t('estimates.detail.request_revision') }}
                </button>
                <button
                  type="button"
                  class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 sm:mt-0 sm:col-start-1 sm:text-sm"
                  @click="showRevisionModal = false"
                >
                  {{ $t('common.cancel') }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Cancel Modal -->
        <div v-if="showCancelModal" class="fixed inset-0 z-50 overflow-y-auto">
          <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="showCancelModal = false"></div>
            <div class="relative inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full sm:p-6">
              <div class="flex items-center">
                <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
                  <TrashIcon class="h-6 w-6 text-red-600" />
                </div>
                <div class="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left">
                  <h3 class="text-lg leading-6 font-medium text-gray-900">견적 취소</h3>
                  <div class="mt-2">
                    <p class="text-sm text-gray-500">{{ $t('estimates.messages.confirm_cancel') }}</p>
                  </div>
                </div>
              </div>
              <div class="mt-5 sm:mt-4 sm:flex sm:flex-row-reverse">
                <button
                  type="button"
                  class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-red-600 text-base font-medium text-white hover:bg-red-700 sm:ml-3 sm:w-auto sm:text-sm"
                  :disabled="isProcessing"
                  @click="cancelEstimate"
                >
                  {{ $t('estimates.detail.cancel_estimate') }}
                </button>
                <button
                  type="button"
                  class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 sm:mt-0 sm:w-auto sm:text-sm"
                  @click="showCancelModal = false"
                >
                  {{ $t('common.cancel') }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import {
  ArrowLeftIcon,
  CheckIcon,
  XMarkIcon,
  PencilIcon,
  TrashIcon,
  PlusIcon,
  DocumentArrowDownIcon,
  PrinterIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'

// Composables
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

// Props
const estimateId = computed(() => route.params.id)

// State
const estimate = ref(null)
const isLoading = ref(false)
const isProcessing = ref(false)

const approvalNote = ref('')
const revisionNote = ref('')

const showApprovalModal = ref(false)
const showRejectionModal = ref(false)
const showRevisionModal = ref(false)
const showCancelModal = ref(false)

// Mock data
const mockEstimate = {
  id: 1,
  estimateNumber: 'EST-2024-0001',
  version: 1,
  status: 'pending',
  totalAmount: 245000,
  subtotal: 229000,
  tax: 16030,
  discount: 0,
  currency: 'THB',
  validUntil: '2024-02-15T23:59:59Z',
  createdAt: '2024-01-15T09:00:00Z',
  isExpired: false,
  specialNotes: '긴급 처리가 필요한 화물입니다.\n포장 시 충격 방지에 특별한 주의가 필요합니다.',
  costs: {
    shippingCost: 185000,
    localDelivery: 12000,
    handlingFee: 15000,
    repackingFee: 8000,
    customsFee: 3750,
    insurance: 5250
  },
  additionalServices: [
    { name: '긴급 처리', cost: 500 },
    { name: '취급주의', cost: 300 },
    { name: '사진 서비스', cost: 200 }
  ],
  order: {
    id: 1,
    orderCode: 'ORD-2024-0001',
    orderType: 'sea',
    totalCbm: 18.5,
    totalWeight: 125.8
  },
  customer: {
    name: '김철수',
    company: 'ABC무역',
    email: 'customer@abc.com',
    phone: '010-1234-5678'
  },
  approvalHistory: [
    {
      id: 1,
      action: 'created',
      userName: '관리자',
      createdAt: '2024-01-15T09:00:00Z',
      note: '1차 견적서가 생성되었습니다.'
    }
  ]
}

// Computed
const showApprovalSection = computed(() => {
  const userRole = authStore.user?.role
  const canApprove = ['individual', 'enterprise', 'partner'].includes(userRole)
  const isApprovable = ['pending', 'revision_requested'].includes(estimate.value?.status)
  return canApprove && isApprovable && !estimate.value?.isExpired
})

const canEditEstimate = computed(() => {
  const userRole = authStore.user?.role
  const canEdit = ['admin', 'warehouse'].includes(userRole)
  const isEditable = ['draft', 'revision_requested'].includes(estimate.value?.status)
  return canEdit && isEditable
})

const canCancelEstimate = computed(() => {
  const userRole = authStore.user?.role
  return ['admin'].includes(userRole) && estimate.value?.status !== 'cancelled'
})

const canCreateSecondEstimate = computed(() => {
  const userRole = authStore.user?.role
  const canCreate = ['admin', 'warehouse'].includes(userRole)
  const needsSecond = estimate.value?.version === 1 && ['rejected', 'revision_requested'].includes(estimate.value?.status)
  return canCreate && needsSecond
})

// Methods
const loadEstimate = async () => {
  isLoading.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    estimate.value = mockEstimate
  } catch (error) {
    notificationStore.error('오류', '견적서를 불러오는데 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const approveEstimate = async () => {
  isProcessing.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    estimate.value.status = 'approved'
    estimate.value.approvalHistory.push({
      id: Date.now(),
      action: 'approved',
      userName: authStore.user?.name || '사용자',
      createdAt: new Date().toISOString(),
      note: approvalNote.value
    })
    
    notificationStore.success('승인 완료', '견적서를 승인했습니다.')
    showApprovalModal.value = false
    approvalNote.value = ''
  } catch (error) {
    notificationStore.error('오류', '견적서 승인에 실패했습니다.')
  } finally {
    isProcessing.value = false
  }
}

const rejectEstimate = async () => {
  isProcessing.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    estimate.value.status = 'rejected'
    estimate.value.approvalHistory.push({
      id: Date.now(),
      action: 'rejected',
      userName: authStore.user?.name || '사용자',
      createdAt: new Date().toISOString(),
      note: approvalNote.value
    })
    
    notificationStore.success('거부 완료', '견적서를 거부했습니다.')
    showRejectionModal.value = false
    approvalNote.value = ''
  } catch (error) {
    notificationStore.error('오류', '견적서 거부에 실패했습니다.')
  } finally {
    isProcessing.value = false
  }
}

const requestRevision = async () => {
  isProcessing.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    estimate.value.status = 'revision_requested'
    estimate.value.approvalHistory.push({
      id: Date.now(),
      action: 'revision_requested',
      userName: authStore.user?.name || '사용자',
      createdAt: new Date().toISOString(),
      note: revisionNote.value
    })
    
    notificationStore.success('수정 요청 완료', '견적서 수정을 요청했습니다.')
    showRevisionModal.value = false
    revisionNote.value = ''
  } catch (error) {
    notificationStore.error('오류', '수정 요청에 실패했습니다.')
  } finally {
    isProcessing.value = false
  }
}

const cancelEstimate = async () => {
  isProcessing.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    estimate.value.status = 'cancelled'
    
    notificationStore.success('취소 완료', '견적서가 취소되었습니다.')
    showCancelModal.value = false
    
    // Navigate back to list
    router.push({ name: 'EstimateList' })
  } catch (error) {
    notificationStore.error('오류', '견적서 취소에 실패했습니다.')
  } finally {
    isProcessing.value = false
  }
}

const editEstimate = () => {
  router.push({ 
    name: 'EstimateCreate', 
    params: { orderId: estimate.value.order.id },
    query: { edit: estimate.value.id }
  })
}

const createSecondEstimate = () => {
  router.push({ 
    name: 'EstimateCreate', 
    params: { orderId: estimate.value.order.id },
    query: { baseEstimate: estimate.value.id, version: 2 }
  })
}

const downloadPDF = async () => {
  try {
    // Simulate PDF generation
    await new Promise(resolve => setTimeout(resolve, 1000))
    notificationStore.success('PDF 생성 완료', 'PDF 파일이 다운로드됩니다.')
  } catch (error) {
    notificationStore.error('오류', 'PDF 생성에 실패했습니다.')
  }
}

const printEstimate = () => {
  window.print()
}

const goBack = () => {
  router.go(-1)
}

const getStatusBadgeClass = (status: string) => {
  const classes = {
    draft: 'bg-gray-100 text-gray-800',
    pending: 'bg-yellow-100 text-yellow-800',
    approved: 'bg-green-100 text-green-800',
    rejected: 'bg-red-100 text-red-800',
    revision_requested: 'bg-orange-100 text-orange-800',
    expired: 'bg-red-100 text-red-800',
    cancelled: 'bg-gray-100 text-gray-800'
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

const formatCurrency = (amount: number, currency: string = 'THB') => {
  return new Intl.NumberFormat('th-TH', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 0
  }).format(amount)
}

const formatDate = (dateString: string) => {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(dateString))
}

// Lifecycle
onMounted(() => {
  loadEstimate()
})
</script>

<style scoped>
@media print {
  .no-print {
    display: none;
  }
}
</style>