<template>
  <div class="overflow-hidden">
    <div v-if="title || $slots.header" class="mb-4">
      <slot name="header">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold text-gray-900">{{ title }}</h3>
          <div v-if="$slots.action">
            <slot name="action" />
          </div>
        </div>
      </slot>
    </div>

    <div class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th
              v-for="column in columns"
              :key="column.key"
              :class="[
                'px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider',
                column.align === 'center' ? 'text-center' : '',
                column.align === 'right' ? 'text-right' : '',
                column.className
              ]"
              :style="column.width ? `width: ${column.width}` : ''"
            >
              <div class="flex-start gap-xs">
                {{ column.label }}
                <button
                  v-if="column.sortable"
                  @click="handleSort(column.key)"
                  class="text-muted hover:text-primary"
                >
                  <span class="mdi mdi-sort" />
                </button>
              </div>
            </th>
            <th v-if="$slots.actions" class="relative px-6 py-3">
              <span class="sr-only">Actions</span>
            </th>
          </tr>
        </thead>
        
        <tbody class="bg-white divide-y divide-gray-200">
          <tr
            v-for="(row, index) in data"
            :key="row.id || index"
            :class="[
              hoverable ? 'hover:bg-gray-50' : '',
              selectable ? 'cursor-pointer' : ''
            ]"
            @click="handleRowClick(row)"
          >
            <td
              v-for="column in columns"
              :key="column.key"
              :class="[
                'px-6 py-4 whitespace-nowrap text-sm',
                column.align === 'center' ? 'text-center' : '',
                column.align === 'right' ? 'text-right' : '',
                column.className
              ]"
            >
              <slot :name="`cell-${column.key}`" :row="row" :value="row[column.key]">
                <span :class="column.valueClass">
                  {{ formatValue(row[column.key], column) }}
                </span>
              </slot>
            </td>
            <td v-if="$slots.actions" class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <slot name="actions" :row="row" :index="index" />
            </td>
          </tr>

          <!-- Empty State -->
          <tr v-if="!data || data.length === 0">
            <td :colspan="columns.length + ($slots.actions ? 1 : 0)" class="px-6 py-12 text-center">
              <slot name="empty">
                <div class="text-muted">
                  <span class="mdi mdi-table-off text-4xl spacing-sm block" />
                  <p class="text-body">데이터가 없습니다</p>
                </div>
              </slot>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="pagination" class="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
      <div class="flex-1 flex justify-between sm:hidden">
        <Button
          variant="secondary"
          size="sm"
          :disabled="pagination.currentPage === 1"
          @click="emit('page-change', pagination.currentPage - 1)"
        >
          이전
        </Button>
        <Button
          variant="secondary"
          size="sm"
          :disabled="pagination.currentPage === pagination.totalPages"
          @click="emit('page-change', pagination.currentPage + 1)"
        >
          다음
        </Button>
      </div>
      
      <div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
        <div>
          <p class="text-sm text-gray-700">
            총 <span class="font-medium">{{ pagination.total }}</span>개 중
            <span class="font-medium">{{ pagination.from }}</span> -
            <span class="font-medium">{{ pagination.to }}</span> 표시
          </p>
        </div>
        <div>
          <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
            <button
              :disabled="pagination.currentPage === 1"
              @click="emit('page-change', pagination.currentPage - 1)"
              class="btn btn-sm btn-secondary rounded-l-md rounded-r-none"
            >
              <span class="mdi mdi-chevron-left" />
            </button>
            
            <button
              v-for="page in visiblePages"
              :key="page"
              @click="emit('page-change', page)"
              :class="[
                'btn btn-sm rounded-none',
                page === pagination.currentPage
                  ? 'btn-primary'
                  : 'btn-secondary'
              ]"
            >
              {{ page }}
            </button>
            
            <button
              :disabled="pagination.currentPage === pagination.totalPages"
              @click="emit('page-change', pagination.currentPage + 1)"
              class="btn btn-sm btn-secondary rounded-r-md rounded-l-none"
            >
              <span class="mdi mdi-chevron-right" />
            </button>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Button from './Button.vue'

interface Column {
  key: string
  label: string
  sortable?: boolean
  align?: 'left' | 'center' | 'right'
  width?: string
  className?: string
  valueClass?: string
  format?: (value: any) => string
}

interface Pagination {
  currentPage: number
  totalPages: number
  total: number
  from: number
  to: number
  perPage: number
}

interface Props {
  title?: string
  columns: Column[]
  data: any[]
  hoverable?: boolean
  selectable?: boolean
  pagination?: Pagination
}

const props = withDefaults(defineProps<Props>(), {
  hoverable: true,
  selectable: false
})

const emit = defineEmits(['row-click', 'sort', 'page-change'])

const handleRowClick = (row: any) => {
  if (props.selectable) {
    emit('row-click', row)
  }
}

const handleSort = (key: string) => {
  emit('sort', key)
}

const formatValue = (value: any, column: Column) => {
  if (column.format) {
    return column.format(value)
  }
  return value ?? '-'
}

const visiblePages = computed(() => {
  if (!props.pagination) return []
  
  const { currentPage, totalPages } = props.pagination
  const pages: number[] = []
  const maxVisible = 5
  
  let start = Math.max(1, currentPage - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages, start + maxVisible - 1)
  
  if (end - start < maxVisible - 1) {
    start = Math.max(1, end - maxVisible + 1)
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})
</script>