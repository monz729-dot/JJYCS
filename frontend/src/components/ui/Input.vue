<template>
  <div class="w-full">
    <label v-if="label" :for="id" class="block text-sm font-medium text-gray-700 mb-1">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>
    
    <div class="relative">
      <div v-if="icon" class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
        <span :class="['mdi', icon, 'text-gray-400']" />
      </div>
      
      <input
        :id="id"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        :class="[
          'block w-full rounded-lg border-gray-300 shadow-sm transition-colors duration-200',
          'focus:ring-2 focus:ring-blue-500 focus:border-blue-500',
          'disabled:bg-gray-50 disabled:text-gray-500',
          icon ? 'pl-10' : 'pl-3',
          rightIcon ? 'pr-10' : 'pr-3',
          error ? 'border-red-300 focus:ring-red-500 focus:border-red-500' : '',
          sizeClasses,
          className
        ]"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
      />
      
      <div v-if="rightIcon" class="absolute inset-y-0 right-0 pr-3 flex items-center">
        <button
          v-if="clickableRightIcon"
          @click="emit('icon-click')"
          type="button"
          class="text-gray-400 hover:text-gray-600"
        >
          <span :class="['mdi', rightIcon]" />
        </button>
        <span v-else :class="['mdi', rightIcon, 'text-gray-400']" />
      </div>
    </div>
    
    <p v-if="hint && !error" class="mt-1 text-sm text-gray-500">{{ hint }}</p>
    <p v-if="error" class="mt-1 text-sm text-red-600">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  modelValue?: string | number
  type?: string
  label?: string
  placeholder?: string
  hint?: string
  error?: string
  icon?: string
  rightIcon?: string
  clickableRightIcon?: boolean
  disabled?: boolean
  readonly?: boolean
  required?: boolean
  size?: 'sm' | 'md' | 'lg'
  className?: string
  id?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  size: 'md',
  disabled: false,
  readonly: false,
  required: false,
  clickableRightIcon: false,
  className: '',
  id: () => `input-${Math.random().toString(36).substr(2, 9)}`
})

const emit = defineEmits(['update:modelValue', 'blur', 'focus', 'icon-click'])

const sizeClasses = computed(() => {
  const sizes = {
    sm: 'py-1.5 text-sm',
    md: 'py-2 text-base',
    lg: 'py-3 text-lg'
  }
  return sizes[props.size]
})

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

const handleBlur = (event: FocusEvent) => {
  emit('blur', event)
}

const handleFocus = (event: FocusEvent) => {
  emit('focus', event)
}
</script>