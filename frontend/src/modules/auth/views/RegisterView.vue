<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-2xl w-full space-y-8">
      <!-- Header -->
      <div>
        <div class="mx-auto h-12 w-12 text-center">
          <svg class="w-12 h-12 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 0C4.477 0 0 4.477 0 10s4.477 10 10 10 10-4.477 10-10S15.523 0 10 0zM8 5a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1zm1 3a1 1 0 100 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
          </svg>
        </div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          {{ $t('auth.register.title') }}
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          {{ $t('auth.register.subtitle') }}
          <router-link 
            to="/auth/login" 
            class="font-medium text-blue-600 hover:text-blue-500 ml-1"
          >
            {{ $t('auth.register.signin_link') }}
          </router-link>
        </p>
      </div>

      <!-- Registration Form -->
      <form class="mt-8 space-y-6" @submit.prevent="handleSubmit">
        <!-- Role Selection -->
        <div class="space-y-4">
          <h3 class="text-lg font-medium text-gray-900">{{ $t('auth.register.role_selection') }}</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div
              v-for="roleOption in roleOptions"
              :key="roleOption.value"
              class="relative border rounded-lg p-4 cursor-pointer transition-all duration-200"
              :class="{
                'border-blue-500 bg-blue-50': form.role === roleOption.value,
                'border-gray-300 hover:border-gray-400': form.role !== roleOption.value
              }"
              @click="selectRole(roleOption.value)"
            >
              <input
                :id="roleOption.value"
                v-model="form.role"
                :value="roleOption.value"
                name="role"
                type="radio"
                class="sr-only"
              />
              <label :for="roleOption.value" class="cursor-pointer">
                <div class="flex items-center space-x-3">
                  <div class="flex-shrink-0">
                    <component :is="roleOption.icon" class="h-6 w-6 text-gray-600" />
                  </div>
                  <div>
                    <p class="font-medium text-gray-900">{{ $t(roleOption.title) }}</p>
                    <p class="text-sm text-gray-500">{{ $t(roleOption.description) }}</p>
                  </div>
                </div>
              </label>
            </div>
          </div>
          <p v-if="errors.role" class="text-sm text-red-600">{{ errors.role }}</p>
        </div>

        <!-- Basic Information -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Email -->
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700">
              {{ $t('auth.register.email') }} <span class="text-red-500">*</span>
            </label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              required
              class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              :class="{ 'border-red-300': errors.email }"
              @blur="validateField('email')"
            />
            <p v-if="errors.email" class="mt-1 text-sm text-red-600">{{ errors.email }}</p>
          </div>

          <!-- Password -->
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700">
              {{ $t('auth.register.password') }} <span class="text-red-500">*</span>
            </label>
            <div class="relative mt-1">
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                required
                class="block w-full pr-10 border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                :class="{ 'border-red-300': errors.password }"
                @blur="validateField('password')"
              />
              <button
                type="button"
                class="absolute inset-y-0 right-0 pr-3 flex items-center"
                @click="showPassword = !showPassword"
              >
                <EyeIcon v-if="!showPassword" class="h-5 w-5 text-gray-400" />
                <EyeSlashIcon v-else class="h-5 w-5 text-gray-400" />
              </button>
            </div>
            <p v-if="errors.password" class="mt-1 text-sm text-red-600">{{ errors.password }}</p>
          </div>

          <!-- Name -->
          <div>
            <label for="name" class="block text-sm font-medium text-gray-700">
              {{ $t('auth.register.name') }} <span class="text-red-500">*</span>
            </label>
            <input
              id="name"
              v-model="form.name"
              type="text"
              required
              class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              :class="{ 'border-red-300': errors.name }"
              @blur="validateField('name')"
            />
            <p v-if="errors.name" class="mt-1 text-sm text-red-600">{{ errors.name }}</p>
          </div>

          <!-- Phone -->
          <div>
            <label for="phone" class="block text-sm font-medium text-gray-700">
              {{ $t('auth.register.phone') }} <span class="text-red-500">*</span>
            </label>
            <input
              id="phone"
              v-model="form.phone"
              type="tel"
              required
              class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              :class="{ 'border-red-300': errors.phone }"
              placeholder="010-1234-5678"
              @blur="validateField('phone')"
            />
            <p v-if="errors.phone" class="mt-1 text-sm text-red-600">{{ errors.phone }}</p>
          </div>
        </div>

        <!-- Role-specific Information -->
        <div v-if="form.role && form.role !== 'individual'" class="space-y-6">
          <h3 class="text-lg font-medium text-gray-900">
            {{ $t(`auth.register.${form.role}_info`) }}
          </h3>

          <!-- Enterprise specific fields -->
          <div v-if="form.role === 'enterprise'" class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label for="companyName" class="block text-sm font-medium text-gray-700">
                {{ $t('auth.register.company_name') }} <span class="text-red-500">*</span>
              </label>
              <input
                id="companyName"
                v-model="form.companyName"
                type="text"
                required
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                :class="{ 'border-red-300': errors.companyName }"
                @blur="validateField('companyName')"
              />
              <p v-if="errors.companyName" class="mt-1 text-sm text-red-600">{{ errors.companyName }}</p>
            </div>

            <div>
              <label for="businessNumber" class="block text-sm font-medium text-gray-700">
                {{ $t('auth.register.business_number') }} <span class="text-red-500">*</span>
              </label>
              <input
                id="businessNumber"
                v-model="form.businessNumber"
                type="text"
                required
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                :class="{ 'border-red-300': errors.businessNumber }"
                placeholder="123-45-67890"
                @blur="validateField('businessNumber')"
              />
              <p v-if="errors.businessNumber" class="mt-1 text-sm text-red-600">{{ errors.businessNumber }}</p>
            </div>

            <div class="md:col-span-2">
              <label for="businessAddress" class="block text-sm font-medium text-gray-700">
                {{ $t('auth.register.business_address') }} <span class="text-red-500">*</span>
              </label>
              <textarea
                id="businessAddress"
                v-model="form.businessAddress"
                required
                rows="3"
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                :class="{ 'border-red-300': errors.businessAddress }"
                @blur="validateField('businessAddress')"
              />
              <p v-if="errors.businessAddress" class="mt-1 text-sm text-red-600">{{ errors.businessAddress }}</p>
            </div>
          </div>

          <!-- Partner specific fields -->
          <div v-if="form.role === 'partner'" class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label for="referralCode" class="block text-sm font-medium text-gray-700">
                {{ $t('auth.register.referral_code') }}
              </label>
              <input
                id="referralCode"
                v-model="form.referralCode"
                type="text"
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                placeholder="Optional"
              />
              <p class="mt-1 text-sm text-gray-500">
                {{ $t('auth.register.referral_code_help') }}
              </p>
            </div>

            <div>
              <label for="partnerType" class="block text-sm font-medium text-gray-700">
                {{ $t('auth.register.partner_type') }} <span class="text-red-500">*</span>
              </label>
              <select
                id="partnerType"
                v-model="form.partnerType"
                required
                class="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                :class="{ 'border-red-300': errors.partnerType }"
              >
                <option value="">{{ $t('auth.register.select_partner_type') }}</option>
                <option value="affiliate">{{ $t('auth.register.affiliate_partner') }}</option>
                <option value="referral">{{ $t('auth.register.referral_partner') }}</option>
                <option value="corporate">{{ $t('auth.register.corporate_partner') }}</option>
              </select>
              <p v-if="errors.partnerType" class="mt-1 text-sm text-red-600">{{ errors.partnerType }}</p>
            </div>
          </div>
        </div>

        <!-- Terms and Conditions -->
        <div class="space-y-4">
          <div class="flex items-start">
            <input
              id="termsAccepted"
              v-model="form.termsAccepted"
              type="checkbox"
              class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              :class="{ 'border-red-300': errors.termsAccepted }"
            />
            <label for="termsAccepted" class="ml-2 text-sm text-gray-900">
              {{ $t('auth.register.terms_prefix') }}
              <a href="/terms" target="_blank" class="text-blue-600 hover:text-blue-500">
                {{ $t('auth.register.terms_of_service') }}
              </a>
              {{ $t('auth.register.terms_and') }}
              <a href="/privacy" target="_blank" class="text-blue-600 hover:text-blue-500">
                {{ $t('auth.register.privacy_policy') }}
              </a>
              {{ $t('auth.register.terms_suffix') }}
            </label>
          </div>
          <p v-if="errors.termsAccepted" class="text-sm text-red-600">{{ errors.termsAccepted }}</p>

          <div class="flex items-start">
            <input
              id="marketingConsent"
              v-model="form.marketingConsent"
              type="checkbox"
              class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label for="marketingConsent" class="ml-2 text-sm text-gray-900">
              {{ $t('auth.register.marketing_consent') }}
            </label>
          </div>
        </div>

        <!-- Submit Button -->
        <div>
          <button
            type="submit"
            :disabled="isLoading || !isFormValid"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span class="absolute left-0 inset-y-0 flex items-center pl-3">
              <svg 
                v-if="isLoading"
                class="animate-spin h-5 w-5 text-white" 
                fill="none" 
                viewBox="0 0 24 24"
              >
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <UserPlusIcon v-else class="h-5 w-5 text-blue-500 group-hover:text-blue-400" />
            </span>
            {{ isLoading ? $t('auth.register.creating_account') : $t('auth.register.create_account') }}
          </button>
        </div>

        <!-- Info Messages -->
        <div v-if="form.role === 'enterprise' || form.role === 'partner'" class="rounded-md bg-blue-50 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <InformationCircleIcon class="h-5 w-5 text-blue-400" />
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-blue-800">
                {{ $t('auth.register.approval_required') }}
              </h3>
              <div class="mt-2 text-sm text-blue-700">
                <p>{{ $t('auth.register.approval_notice') }}</p>
              </div>
            </div>
          </div>
        </div>
      </form>

      <!-- Error Display -->
      <div v-if="authStore.error" class="rounded-md bg-red-50 p-4">
        <div class="flex">
          <div class="flex-shrink-0">
            <XCircleIcon class="h-5 w-5 text-red-400" />
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">
              {{ $t('auth.register.error_title') }}
            </h3>
            <div class="mt-2 text-sm text-red-700">
              {{ authStore.error }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import {
  UserIcon,
  BuildingOfficeIcon,
  UserGroupIcon,
  EyeIcon,
  EyeSlashIcon,
  UserPlusIcon,
  InformationCircleIcon,
  XCircleIcon
} from '@heroicons/vue/24/outline'
import type { RegisterRequest } from '@/types/auth'

// Composables
const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

// Form state
const form = ref<RegisterRequest>({
  email: '',
  password: '',
  name: '',
  phone: '',
  role: 'individual',
  companyName: '',
  businessNumber: '',
  businessAddress: '',
  referralCode: '',
  partnerType: '',
  termsAccepted: false,
  marketingConsent: false
})

const showPassword = ref(false)
const errors = ref<Record<string, string>>({})

// Role options
const roleOptions = [
  {
    value: 'individual',
    title: 'auth.register.individual',
    description: 'auth.register.individual_desc',
    icon: UserIcon
  },
  {
    value: 'enterprise',
    title: 'auth.register.enterprise',
    description: 'auth.register.enterprise_desc',
    icon: BuildingOfficeIcon
  },
  {
    value: 'partner',
    title: 'auth.register.partner',
    description: 'auth.register.partner_desc',
    icon: UserGroupIcon
  }
]

// Computed
const isLoading = computed(() => authStore.isLoading)
const isFormValid = computed(() => {
  const basicValid = form.value.email && 
                    form.value.password && 
                    form.value.name && 
                    form.value.phone &&
                    form.value.role &&
                    form.value.termsAccepted

  if (!basicValid) return false

  // Role-specific validation
  if (form.value.role === 'enterprise') {
    return form.value.companyName && 
           form.value.businessNumber && 
           form.value.businessAddress
  }

  if (form.value.role === 'partner') {
    return form.value.partnerType
  }

  return Object.keys(errors.value).length === 0
})

// Methods
const selectRole = (role: string) => {
  form.value.role = role
  // Clear role-specific errors
  if (role !== 'enterprise') {
    delete errors.value.companyName
    delete errors.value.businessNumber
    delete errors.value.businessAddress
  }
  if (role !== 'partner') {
    delete errors.value.partnerType
  }
}

const validateField = (field: string) => {
  switch (field) {
    case 'email':
      if (!form.value.email) {
        errors.value.email = 'Email is required'
      } else if (!/\S+@\S+\.\S+/.test(form.value.email)) {
        errors.value.email = 'Please enter a valid email'
      } else {
        delete errors.value.email
      }
      break

    case 'password':
      if (!form.value.password) {
        errors.value.password = 'Password is required'
      } else if (form.value.password.length < 8) {
        errors.value.password = 'Password must be at least 8 characters'
      } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(form.value.password)) {
        errors.value.password = 'Password must contain uppercase, lowercase, and number'
      } else {
        delete errors.value.password
      }
      break

    case 'name':
      if (!form.value.name) {
        errors.value.name = 'Name is required'
      } else if (form.value.name.length < 2) {
        errors.value.name = 'Name must be at least 2 characters'
      } else {
        delete errors.value.name
      }
      break

    case 'phone':
      if (!form.value.phone) {
        errors.value.phone = 'Phone number is required'
      } else if (!/^01[016789]-?\d{3,4}-?\d{4}$/.test(form.value.phone)) {
        errors.value.phone = 'Please enter a valid phone number'
      } else {
        delete errors.value.phone
      }
      break

    case 'companyName':
      if (form.value.role === 'enterprise' && !form.value.companyName) {
        errors.value.companyName = 'Company name is required'
      } else {
        delete errors.value.companyName
      }
      break

    case 'businessNumber':
      if (form.value.role === 'enterprise') {
        if (!form.value.businessNumber) {
          errors.value.businessNumber = 'Business number is required'
        } else if (!/^\d{3}-?\d{2}-?\d{5}$/.test(form.value.businessNumber)) {
          errors.value.businessNumber = 'Please enter a valid business number'
        } else {
          delete errors.value.businessNumber
        }
      }
      break

    case 'businessAddress':
      if (form.value.role === 'enterprise' && !form.value.businessAddress) {
        errors.value.businessAddress = 'Business address is required'
      } else {
        delete errors.value.businessAddress
      }
      break

    case 'partnerType':
      if (form.value.role === 'partner' && !form.value.partnerType) {
        errors.value.partnerType = 'Partner type is required'
      } else {
        delete errors.value.partnerType
      }
      break
  }
}

const validateForm = () => {
  // Validate all fields
  validateField('email')
  validateField('password')
  validateField('name')
  validateField('phone')

  if (form.value.role === 'enterprise') {
    validateField('companyName')
    validateField('businessNumber')
    validateField('businessAddress')
  }

  if (form.value.role === 'partner') {
    validateField('partnerType')
  }

  if (!form.value.termsAccepted) {
    errors.value.termsAccepted = 'You must accept the terms and conditions'
  } else {
    delete errors.value.termsAccepted
  }

  return Object.keys(errors.value).length === 0
}

const handleSubmit = async () => {
  if (!validateForm()) return

  try {
    const response = await authStore.register(form.value)
    
    if (response.success) {
      // Show appropriate success message based on role
      if (form.value.role === 'enterprise' || form.value.role === 'partner') {
        notificationStore.info(
          'Registration Successful',
          'Your account has been created and is pending approval. You will receive an email notification within 1-2 business days.'
        )
        // Redirect to approval page
        await router.push('/auth/approval')
      } else {
        notificationStore.success(
          'Registration Successful',
          'Welcome to YCS LMS! Please check your email to verify your account.'
        )
        // Redirect to dashboard for individual users
        await router.push('/dashboard')
      }
    }
  } catch (error: any) {
    console.error('Registration error:', error)
  }
}

// Lifecycle
onMounted(() => {
  authStore.clearError()
})
</script>

<style scoped>
/* Custom styles */
</style>