<template>
  <div class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <!-- Background overlay -->
      <div 
        class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" 
        aria-hidden="true"
        @click="$emit('cancel')"
      ></div>

      <!-- This element is to trick the browser into centering the modal contents. -->
      <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>

      <!-- Modal panel -->
      <div class="relative inline-block align-bottom bg-white rounded-lg px-4 pt-5 pb-4 text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-sm sm:w-full sm:p-6">
        <div>
          <!-- Icon -->
          <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-blue-100">
            <svg class="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          </div>

          <!-- Content -->
          <div class="mt-3 text-center sm:mt-5">
            <h3 class="text-lg leading-6 font-medium text-gray-900" id="modal-title">
              {{ $t('auth.2fa.title') }}
            </h3>
            <div class="mt-2">
              <p class="text-sm text-gray-500">
                {{ $t('auth.2fa.description') }}
              </p>
            </div>

            <!-- 2FA Code Input -->
            <div class="mt-5">
              <label for="twoFactorCode" class="sr-only">{{ $t('auth.2fa.code') }}</label>
              <input
                id="twoFactorCode"
                v-model="code"
                ref="codeInput"
                type="text"
                inputmode="numeric"
                pattern="[0-9]{6}"
                maxlength="6"
                class="w-full text-center text-2xl font-mono border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                :class="{ 'border-red-300': error }"
                :placeholder="$t('auth.2fa.placeholder')"
                @input="handleInput"
                @keydown.enter="handleVerify"
              />
              <p v-if="error" class="mt-2 text-sm text-red-600">{{ error }}</p>
            </div>

            <!-- Loading state -->
            <div v-if="isLoading" class="mt-4 flex justify-center">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <span class="text-sm text-gray-600">{{ $t('auth.2fa.verifying') }}</span>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="mt-5 sm:mt-6 sm:grid sm:grid-cols-2 sm:gap-3 sm:grid-flow-row-dense">
          <button
            type="button"
            class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-blue-600 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:col-start-2 sm:text-sm disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="!isCodeValid || isLoading"
            @click="handleVerify"
          >
            {{ $t('auth.2fa.verify') }}
          </button>
          <button
            type="button"
            class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 sm:mt-0 sm:col-start-1 sm:text-sm"
            @click="$emit('cancel')"
          >
            {{ $t('auth.2fa.cancel') }}
          </button>
        </div>

        <!-- Help text -->
        <div class="mt-4 text-center">
          <p class="text-xs text-gray-500">
            {{ $t('auth.2fa.help_text') }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'

// Emits
const emit = defineEmits<{
  verify: [code: string]
  cancel: []
}>()

// State
const code = ref('')
const error = ref('')
const isLoading = ref(false)
const codeInput = ref<HTMLInputElement>()

// Computed
const isCodeValid = computed(() => {
  return code.value.length === 6 && /^\d{6}$/.test(code.value)
})

// Methods
const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  // Only allow numeric input
  const value = target.value.replace(/\D/g, '').slice(0, 6)
  code.value = value
  target.value = value
  
  // Clear error when user starts typing
  if (error.value) {
    error.value = ''
  }
  
  // Auto-submit when 6 digits entered
  if (value.length === 6) {
    handleVerify()
  }
}

const handleVerify = async () => {
  if (!isCodeValid.value) {
    error.value = 'Please enter a 6-digit code'
    return
  }

  isLoading.value = true
  error.value = ''

  try {
    emit('verify', code.value)
  } catch (err: any) {
    error.value = err.message || 'Verification failed. Please try again.'
  } finally {
    isLoading.value = false
  }
}

// Lifecycle
onMounted(() => {
  // Focus on input when modal opens
  nextTick(() => {
    if (codeInput.value) {
      codeInput.value.focus()
    }
  })
})
</script>

<style scoped>
/* Custom styles for 2FA modal */
</style>