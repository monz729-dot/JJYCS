<template>
  <div v-if="hasWarnings" class="business-rule-warnings space-y-3">
    <!-- CBM Warnings -->
    <div v-for="warning in warnings" :key="warning.id" 
         :class="getWarningClasses(warning.severity)"
         class="rounded-lg border p-4 space-y-3">
      
      <!-- Header -->
      <div class="flex items-start justify-between">
        <div class="flex items-center space-x-3">
          <!-- Icon -->
          <div :class="getIconClasses(warning.severity)" 
               class="flex-shrink-0 w-6 h-6 flex items-center justify-center rounded-full">
            <component :is="getWarningIcon(warning.type, warning.severity)" 
                      class="w-4 h-4" />
          </div>
          
          <!-- Content -->
          <div class="flex-1 min-w-0">
            <h4 :class="getTitleClasses(warning.severity)" class="text-sm font-medium">
              {{ warning.title }}
            </h4>
            <p :class="getMessageClasses(warning.severity)" class="text-sm mt-1">
              {{ warning.message }}
            </p>
          </div>
        </div>
        
        <!-- Close button -->
        <button @click="dismissWarning(warning.id)" 
                class="flex-shrink-0 text-gray-400 hover:text-gray-600 transition-colors">
          <XMarkIcon class="w-4 h-4" />
        </button>
      </div>
      
      <!-- Details -->
      <div v-if="warning.details" :class="getDetailsClasses(warning.severity)" 
           class="text-sm pl-9">
        {{ warning.details }}
      </div>
      
      <!-- Actions -->
      <div v-if="warning.actions && warning.actions.length > 0" 
           class="flex items-center space-x-3 pl-9">
        <button v-for="action in warning.actions" 
                :key="action.label"
                @click="action.action"
                :class="getActionClasses(warning.severity, action.primary)"
                class="inline-flex items-center px-3 py-1.5 text-xs font-medium rounded-md transition-colors">
          {{ action.label }}
        </button>
      </div>
      
      <!-- Timestamp -->
      <div v-if="showTimestamp" class="text-xs text-gray-400 pl-9">
        {{ formatTimestamp(warning.timestamp) }}
      </div>
    </div>

    <!-- Summary Stats -->
    <div v-if="showSummary && warnings.length > 1" 
         class="bg-gray-50 rounded-lg p-3 text-sm text-gray-600">
      <div class="flex items-center justify-between">
        <span>총 {{ warnings.length }}개의 비즈니스 룰 검증 결과</span>
        <button @click="dismissAllWarnings" 
                class="text-gray-500 hover:text-gray-700 font-medium">
          모두 해제
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineProps, defineEmits } from 'vue'
import { 
  XMarkIcon,
  ExclamationTriangleIcon,
  ExclamationCircleIcon,
  InformationCircleIcon,
  ScaleIcon,
  CurrencyDollarIcon,
  IdentificationIcon,
  WeightIcon
} from '@heroicons/vue/24/outline'

import type { BusinessRuleWarning } from '../../composables/useBusinessRules'

interface Props {
  warnings: BusinessRuleWarning[]
  showTimestamp?: boolean
  showSummary?: boolean
  dismissible?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showTimestamp: false,
  showSummary: true,
  dismissible: true
})

const emit = defineEmits<{
  dismiss: [warningId: string]
  dismissAll: []
}>()

const hasWarnings = computed(() => props.warnings.length > 0)

// Warning type icons
const getWarningIcon = (type: BusinessRuleWarning['type'], severity: BusinessRuleWarning['severity']) => {
  const iconMap = {
    cbm: ScaleIcon,
    thb: CurrencyDollarIcon,
    memberCode: IdentificationIcon,
    weight: WeightIcon,
    dimensions: ScaleIcon
  }
  
  return iconMap[type] || InformationCircleIcon
}

// Styling functions
const getWarningClasses = (severity: BusinessRuleWarning['severity']) => {
  const classes = {
    error: 'bg-red-50 border-red-200',
    warning: 'bg-amber-50 border-amber-200',
    info: 'bg-blue-50 border-blue-200'
  }
  return classes[severity] || classes.info
}

const getIconClasses = (severity: BusinessRuleWarning['severity']) => {
  const classes = {
    error: 'bg-red-100 text-red-600',
    warning: 'bg-amber-100 text-amber-600',
    info: 'bg-blue-100 text-blue-600'
  }
  return classes[severity] || classes.info
}

const getTitleClasses = (severity: BusinessRuleWarning['severity']) => {
  const classes = {
    error: 'text-red-800',
    warning: 'text-amber-800',
    info: 'text-blue-800'
  }
  return classes[severity] || classes.info
}

const getMessageClasses = (severity: BusinessRuleWarning['severity']) => {
  const classes = {
    error: 'text-red-700',
    warning: 'text-amber-700',
    info: 'text-blue-700'
  }
  return classes[severity] || classes.info
}

const getDetailsClasses = (severity: BusinessRuleWarning['severity']) => {
  const classes = {
    error: 'text-red-600',
    warning: 'text-amber-600',
    info: 'text-blue-600'
  }
  return classes[severity] || classes.info
}

const getActionClasses = (severity: BusinessRuleWarning['severity'], isPrimary?: boolean) => {
  if (isPrimary) {
    const primaryClasses = {
      error: 'bg-red-600 text-white hover:bg-red-700',
      warning: 'bg-amber-600 text-white hover:bg-amber-700',
      info: 'bg-blue-600 text-white hover:bg-blue-700'
    }
    return primaryClasses[severity] || primaryClasses.info
  }
  
  const secondaryClasses = {
    error: 'bg-red-100 text-red-700 hover:bg-red-200',
    warning: 'bg-amber-100 text-amber-700 hover:bg-amber-200',
    info: 'bg-blue-100 text-blue-700 hover:bg-blue-200'
  }
  return secondaryClasses[severity] || secondaryClasses.info
}

// Utility functions
const formatTimestamp = (timestamp: Date) => {
  return new Intl.DateTimeFormat('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(timestamp)
}

const dismissWarning = (warningId: string) => {
  if (props.dismissible) {
    emit('dismiss', warningId)
  }
}

const dismissAllWarnings = () => {
  if (props.dismissible) {
    emit('dismissAll')
  }
}
</script>

<style scoped>
.business-rule-warnings {
  /* Custom animations */
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.business-rule-warnings > div {
  animation: slideInDown 0.3s ease-out;
}</style>