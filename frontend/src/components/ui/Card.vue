<template>
  <div 
    :class="[
      'bg-white rounded-xl border transition-all duration-200',
      noPadding ? '' : 'p-6',
      hoverable ? 'hover:shadow-lg hover:border-gray-300' : '',
      clickable ? 'cursor-pointer' : '',
      borderless ? 'border-transparent shadow-sm' : 'border-gray-200',
      className
    ]"
    @click="handleClick"
  >
    <!-- Header -->
    <div v-if="title || $slots.header" class="mb-4">
      <slot name="header">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold text-gray-900">{{ title }}</h3>
          <div v-if="$slots.action">
            <slot name="action" />
          </div>
        </div>
        <p v-if="subtitle" class="mt-1 text-sm text-gray-500">{{ subtitle }}</p>
      </slot>
    </div>

    <!-- Content -->
    <div :class="contentClass">
      <slot />
    </div>

    <!-- Footer -->
    <div v-if="$slots.footer" class="mt-4 pt-4 border-t border-gray-100">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title?: string
  subtitle?: string
  noPadding?: boolean
  hoverable?: boolean
  clickable?: boolean
  borderless?: boolean
  className?: string
  contentClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  noPadding: false,
  hoverable: false,
  clickable: false,
  borderless: false,
  className: '',
  contentClass: ''
})

const emit = defineEmits(['click'])

const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>