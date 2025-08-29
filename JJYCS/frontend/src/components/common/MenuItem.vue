<template>
  <button
    class="w-full flex items-center gap-3 px-4 py-2.5 hover:bg-gray-50 text-left transition-colors"
    @click="go"
  >
    <slot name="icon">
      <component :is="iconComponent" v-if="iconComponent" class="w-5 h-5 text-gray-500" />
      <span v-else class="inline-block w-5 h-5"></span>
    </slot>
    <span class="text-gray-700">{{ label }}</span>
  </button>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { computed, h } from 'vue'

const props = defineProps<{ 
  to?: any
  label: string
  icon?: string
}>()

const emit = defineEmits<{ 'done': [] }>()
const router = useRouter()

const go = () => { 
  if (props.to) {
    router.push(props.to)
  }
  emit('done')
}

// SVG 아이콘 컴포넌트들
const iconMap: Record<string, any> = {
  'home': () => h('svg', { 
    fill: 'none', 
    viewBox: '0 0 24 24', 
    stroke: 'currentColor' 
  }, [
    h('path', {
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      'stroke-width': '2',
      d: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6'
    })
  ]),
  
  'user': () => h('svg', { 
    fill: 'none', 
    viewBox: '0 0 24 24', 
    stroke: 'currentColor' 
  }, [
    h('path', {
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      'stroke-width': '2',
      d: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z'
    })
  ]),
  
  'file-plus': () => h('svg', { 
    fill: 'none', 
    viewBox: '0 0 24 24', 
    stroke: 'currentColor' 
  }, [
    h('path', {
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      'stroke-width': '2',
      d: 'M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z'
    })
  ]),
  
  'list': () => h('svg', { 
    fill: 'none', 
    viewBox: '0 0 24 24', 
    stroke: 'currentColor' 
  }, [
    h('path', {
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      'stroke-width': '2',
      d: 'M4 6h16M4 12h16M4 18h16'
    })
  ]),
  
  'bell': () => h('svg', { 
    fill: 'none', 
    viewBox: '0 0 24 24', 
    stroke: 'currentColor' 
  }, [
    h('path', {
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      'stroke-width': '2',
      d: 'M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9'
    })
  ]),
  
  'help-circle': () => h('svg', { 
    fill: 'none', 
    viewBox: '0 0 24 24', 
    stroke: 'currentColor' 
  }, [
    h('path', {
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      'stroke-width': '2',
      d: 'M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z'
    })
  ]),
  
  'log-out': () => h('svg', { 
    fill: 'none', 
    viewBox: '0 0 24 24', 
    stroke: 'currentColor' 
  }, [
    h('path', {
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      'stroke-width': '2',
      d: 'M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1'
    })
  ])
}

const iconComponent = computed(() => props.icon ? iconMap[props.icon] : null)
</script>