<template>
  <div 
    :class="[
      'card',
      noPadding ? '' : 'card-body',
      hoverable ? 'hover:shadow-lg hover:border-gray-300' : '',
      clickable ? 'cursor-pointer' : '',
      borderless ? 'border-transparent shadow-sm' : '',
      className
    ]"
    @click="handleClick"
  >
    <!-- Header -->
    <div v-if="title || $slots.header" class="card-header">
      <slot name="header">
        <div class="flex-between">
          <h3 class="text-heading-md">{{ title }}</h3>
          <div v-if="$slots.action">
            <slot name="action" />
          </div>
        </div>
        <p v-if="subtitle" class="text-body-sm text-muted spacing-xs">{{ subtitle }}</p>
      </slot>
    </div>

    <!-- Content -->
    <div :class="contentClass">
      <slot />
    </div>

    <!-- Footer -->
    <div v-if="$slots.footer" class="card-footer">
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