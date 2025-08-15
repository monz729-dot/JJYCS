<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="[
      'btn',
      sizeClasses,
      variantClasses,
      fullWidth ? 'w-full' : '',
      className
    ]"
    @click="handleClick"
  >
    <!-- Loading spinner -->
    <span v-if="loading" class="loading-spinner mr-2" />

    <!-- Icon -->
    <span v-if="icon && !loading" :class="['mdi', icon, text ? 'mr-2' : '']" />

    <!-- Text -->
    <span v-if="text">{{ text }}</span>
    <slot v-else />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'ghost' | 'link'
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  type?: 'button' | 'submit' | 'reset'
  text?: string
  icon?: string
  loading?: boolean
  disabled?: boolean
  fullWidth?: boolean
  className?: string
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  type: 'button',
  loading: false,
  disabled: false,
  fullWidth: false,
  className: ''
})

const emit = defineEmits(['click'])

const sizeClasses = computed(() => {
  const sizes = {
    xs: 'btn-xs',
    sm: 'btn-sm',
    md: 'btn-md',
    lg: 'btn-lg',
    xl: 'btn-xl'
  }
  return sizes[props.size]
})

const variantClasses = computed(() => {
  const variants = {
    primary: 'btn-primary',
    secondary: 'btn-secondary',
    success: 'btn-success',
    danger: 'btn-error',
    warning: 'btn-warning',
    ghost: 'btn-secondary bg-transparent hover:bg-gray-50',
    link: 'btn-secondary bg-transparent text-blue-600 hover:text-blue-700 hover:underline hover:bg-transparent'
  }
  return variants[props.variant]
})

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>