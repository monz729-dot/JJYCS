<template>
  <span
    :class="getStatusClass(status)"
    class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
  >
    <component :is="getStatusIcon(status)" class="h-3 w-3 mr-1.5" />
    {{ getStatusText(status) }}
  </span>
</template>

<script setup lang="ts">
import {
  ClockIcon,
  ArrowPathIcon,
  CheckCircleIcon,
  XCircleIcon,
  ArrowUturnLeftIcon
} from '@heroicons/vue/24/outline'

interface Props {
  status: string
}

defineProps<Props>()

const getStatusClass = (status: string): string => {
  const classes = {
    pending: 'bg-yellow-100 text-yellow-800',
    processing: 'bg-blue-100 text-blue-800',
    completed: 'bg-green-100 text-green-800',
    failed: 'bg-red-100 text-red-800',
    refunded: 'bg-gray-100 text-gray-800',
    cancelled: 'bg-gray-100 text-gray-800'
  }
  return classes[status as keyof typeof classes] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status: string): string => {
  const texts = {
    pending: '대기중',
    processing: '처리중',
    completed: '완료',
    failed: '실패',
    refunded: '환불',
    cancelled: '취소'
  }
  return texts[status as keyof typeof texts] || status
}

const getStatusIcon = (status: string) => {
  const icons = {
    pending: ClockIcon,
    processing: ArrowPathIcon,
    completed: CheckCircleIcon,
    failed: XCircleIcon,
    refunded: ArrowUturnLeftIcon,
    cancelled: XCircleIcon
  }
  return icons[status as keyof typeof icons] || CheckCircleIcon
}
</script>