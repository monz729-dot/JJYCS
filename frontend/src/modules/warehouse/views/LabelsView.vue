<template>
  <div class="labels-view">
    <!-- 헤더 -->
    <div class="labels-header">
      <div class="header-info">
        <h1>라벨 관리</h1>
        <p class="labels-description">라벨 생성, 출력 및 관리</p>
      </div>
      <div class="header-actions">
        <button @click="showTemplateModal = true" class="btn-template">
          <i class="icon-layout"></i>
          템플릿 관리
        </button>
        <button @click="showBulkGenerateModal = true" class="btn-bulk">
          <i class="icon-layers"></i>
          일괄 생성
        </button>
      </div>
    </div>

    <!-- 빠른 액션 -->
    <div class="quick-actions">
      <div class="action-card" @click="quickGenerateShipping">
        <div class="action-icon shipping">
          <i class="icon-truck"></i>
        </div>
        <div class="action-content">
          <h3>배송 라벨</h3>
          <p>배송용 라벨을 빠르게 생성</p>
        </div>
        <i class="icon-arrow-right"></i>
      </div>

      <div class="action-card" @click="quickGenerateInventory">
        <div class="action-icon inventory">
          <i class="icon-package"></i>
        </div>
        <div class="action-content">
          <h3>재고 라벨</h3>
          <p>재고 관리용 라벨 생성</p>
        </div>
        <i class="icon-arrow-right"></i>
      </div>

      <div class="action-card" @click="quickGenerateQR">
        <div class="action-icon qr">
          <i class="icon-qr-code"></i>
        </div>
        <div class="action-content">
          <h3>QR 라벨</h3>
          <p>QR 코드가 포함된 라벨 생성</p>
        </div>
        <i class="icon-arrow-right"></i>
      </div>

      <div class="action-card" @click="quickPrintHistory">
        <div class="action-icon history">
          <i class="icon-history"></i>
        </div>
        <div class="action-content">
          <h3>출력 이력</h3>
          <p>최근 출력된 라벨 다시 인쇄</p>
        </div>
        <i class="icon-arrow-right"></i>
      </div>
    </div>

    <!-- 라벨 생성 섹션 -->
    <div class="label-generator">
      <div class="generator-header">
        <h2>라벨 생성</h2>
        <div class="generator-controls">
          <button
            v-for="type in labelTypes"
            :key="type.value"
            class="type-tab"
            :class="{ active: selectedLabelType === type.value }"
            @click="selectedLabelType = type.value"
          >
            <i :class="type.icon"></i>
            {{ type.label }}
          </button>
        </div>
      </div>

      <div class="generator-content">
        <!-- 단일 라벨 생성 -->
        <div v-if="selectedLabelType === 'single'" class="single-generator">
          <div class="generator-form">
            <div class="form-row">
              <div class="form-group">
                <label>주문/라벨 코드</label>
                <div class="input-with-scan">
                  <input
                    v-model="singleLabelForm.code"
                    type="text"
                    placeholder="주문코드 또는 라벨코드 입력..."
                    class="code-input"
                    @input="handleCodeInput"
                  />
                  <button @click="scanCode" class="btn-scan">
                    <i class="icon-qr-code"></i>
                    스캔
                  </button>
                </div>
              </div>
              
              <div class="form-group">
                <label>라벨 종류</label>
                <select v-model="singleLabelForm.type" class="label-type-select">
                  <option value="shipping">배송 라벨</option>
                  <option value="inventory">재고 라벨</option>
                  <option value="qr">QR 라벨</option>
                  <option value="barcode">바코드 라벨</option>
                </select>
              </div>
            </div>

            <!-- 라벨 정보 미리보기 -->
            <div v-if="previewData" class="label-preview">
              <h3>라벨 정보</h3>
              <div class="preview-content">
                <div class="preview-info">
                  <div class="info-row">
                    <span class="label">주문코드:</span>
                    <span class="value">{{ previewData.orderCode }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">라벨코드:</span>
                    <span class="value">{{ previewData.labelCode }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">상품명:</span>
                    <span class="value">{{ previewData.itemName }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">수취인:</span>
                    <span class="value">{{ previewData.recipientName }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">배송지:</span>
                    <span class="value">{{ previewData.shippingAddress }}</span>
                  </div>
                </div>
                <div class="preview-visual">
                  <div class="label-visual" :class="singleLabelForm.type">
                    <div class="visual-header">
                      <div class="company-logo">YCS</div>
                      <div class="label-title">{{ getLabelTypeTitle(singleLabelForm.type) }}</div>
                    </div>
                    <div class="visual-qr">
                      <div class="qr-placeholder">QR</div>
                    </div>
                    <div class="visual-info">
                      <div class="info-line">{{ previewData.labelCode }}</div>
                      <div class="info-line">{{ previewData.orderCode }}</div>
                      <div class="info-line">{{ previewData.itemName }}</div>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="preview-actions">
                <button @click="generateSingleLabel" class="btn-generate" :disabled="generating">
                  <LoadingSpinner v-if="generating" size="small" />
                  <i v-else class="icon-plus"></i>
                  라벨 생성
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 배치 라벨 생성 -->
        <div v-if="selectedLabelType === 'batch'" class="batch-generator">
          <div class="batch-controls">
            <div class="control-row">
              <div class="form-group">
                <label>필터 조건</label>
                <div class="filter-controls">
                  <select v-model="batchFilter.status" class="batch-filter">
                    <option value="">모든 상태</option>
                    <option value="ready_for_outbound">출고 준비</option>
                    <option value="inbound_completed">입고 완료</option>
                  </select>
                  <select v-model="batchFilter.shipType" class="batch-filter">
                    <option value="">모든 배송</option>
                    <option value="sea">해상</option>
                    <option value="air">항공</option>
                  </select>
                  <button @click="applyBatchFilter" class="btn-apply-filter">적용</button>
                </div>
              </div>
            </div>
            
            <div class="batch-selection">
              <div class="selection-header">
                <h3>선택된 항목 ({{ selectedBatchItems.length }}개)</h3>
                <div class="selection-actions">
                  <button @click="selectAllBatch" class="btn-select-all">
                    모두 선택
                  </button>
                  <button @click="clearBatchSelection" class="btn-clear-all">
                    선택 해제
                  </button>
                </div>
              </div>
              
              <div class="batch-items-list">
                <div
                  v-for="item in batchItems.slice(0, 10)"
                  :key="item.id"
                  class="batch-item"
                  :class="{ selected: selectedBatchItems.includes(item.id) }"
                  @click="toggleBatchItem(item.id)"
                >
                  <input
                    type="checkbox"
                    :checked="selectedBatchItems.includes(item.id)"
                    @click.stop
                    @change="toggleBatchItem(item.id)"
                  />
                  <div class="item-info">
                    <span class="item-code">{{ item.labelCode }}</span>
                    <span class="item-name">{{ item.itemName }}</span>
                  </div>
                  <span class="item-status" :class="item.status">
                    {{ getStatusText(item.status) }}
                  </span>
                </div>
                
                <div v-if="batchItems.length > 10" class="batch-more">
                  외 {{ batchItems.length - 10 }}개 더...
                </div>
              </div>
            </div>
          </div>

          <div class="batch-actions" v-if="selectedBatchItems.length > 0">
            <div class="batch-options">
              <div class="form-group">
                <label>라벨 종류</label>
                <select v-model="batchLabelType" class="batch-label-type">
                  <option value="shipping">배송 라벨</option>
                  <option value="inventory">재고 라벨</option>
                  <option value="qr">QR 라벨</option>
                </select>
              </div>
              
              <div class="form-group">
                <label>라벨 크기</label>
                <select v-model="batchLabelSize" class="batch-label-size">
                  <option value="standard">표준 (100x150mm)</option>
                  <option value="large">대형 (150x200mm)</option>
                  <option value="small">소형 (75x100mm)</option>
                </select>
              </div>
            </div>
            
            <button @click="generateBatchLabels" class="btn-generate-batch" :disabled="generatingBatch">
              <LoadingSpinner v-if="generatingBatch" size="small" />
              <i v-else class="icon-layers"></i>
              {{ selectedBatchItems.length }}개 라벨 생성
            </button>
          </div>
        </div>

        <!-- 템플릿 기반 생성 -->
        <div v-if="selectedLabelType === 'template'" class="template-generator">
          <div class="template-grid">
            <div
              v-for="template in labelTemplates"
              :key="template.id"
              class="template-card"
              :class="{ selected: selectedTemplate?.id === template.id }"
              @click="selectedTemplate = template"
            >
              <div class="template-preview">
                <img :src="template.previewImage" :alt="template.name" />
              </div>
              <div class="template-info">
                <h4>{{ template.name }}</h4>
                <p>{{ template.description }}</p>
                <div class="template-stats">
                  <span>{{ template.usageCount }}회 사용</span>
                </div>
              </div>
            </div>
          </div>
          
          <div v-if="selectedTemplate" class="template-actions">
            <button @click="useTemplate" class="btn-use-template">
              <i class="icon-play"></i>
              템플릿 사용
            </button>
            <button @click="editTemplate" class="btn-edit-template">
              <i class="icon-edit"></i>
              편집
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 생성된 라벨 목록 -->
    <div class="generated-labels">
      <div class="labels-header">
        <h2>생성된 라벨 ({{ generatedLabels.length }}개)</h2>
        <div class="labels-actions">
          <button @click="clearAllLabels" class="btn-clear-all">
            <i class="icon-trash"></i>
            모두 삭제
          </button>
          <button @click="printAllLabels" :disabled="generatedLabels.length === 0" class="btn-print-all">
            <i class="icon-printer"></i>
            모두 출력
          </button>
        </div>
      </div>

      <div v-if="generatedLabels.length === 0" class="empty-labels">
        <i class="icon-tag"></i>
        <h3>생성된 라벨이 없습니다</h3>
        <p>위의 라벨 생성 도구를 사용하여 라벨을 생성해보세요.</p>
      </div>

      <div v-else class="labels-grid">
        <div
          v-for="label in generatedLabels"
          :key="label.id"
          class="label-card"
        >
          <div class="label-preview-mini">
            <div class="preview-header">
              <span class="label-type" :class="label.type">{{ getLabelTypeTitle(label.type) }}</span>
              <span class="creation-time">{{ formatTime(label.createdAt) }}</span>
            </div>
            <div class="preview-content">
              <div class="label-code">{{ label.labelCode }}</div>
              <div class="order-code">{{ label.orderCode }}</div>
            </div>
          </div>
          
          <div class="label-actions">
            <button @click="previewLabel(label)" class="btn-action preview" title="미리보기">
              <i class="icon-eye"></i>
            </button>
            <button @click="printLabel(label)" class="btn-action print" title="출력">
              <i class="icon-printer"></i>
            </button>
            <button @click="downloadLabel(label)" class="btn-action download" title="다운로드">
              <i class="icon-download"></i>
            </button>
            <button @click="duplicateLabel(label)" class="btn-action duplicate" title="복제">
              <i class="icon-copy"></i>
            </button>
            <button @click="deleteLabel(label)" class="btn-action delete" title="삭제">
              <i class="icon-trash"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 출력 설정 -->
    <div class="print-settings" v-if="showPrintSettings">
      <div class="settings-header">
        <h3>출력 설정</h3>
        <button @click="showPrintSettings = false" class="btn-close">
          <i class="icon-x"></i>
        </button>
      </div>
      
      <div class="settings-form">
        <div class="form-row">
          <div class="form-group">
            <label>프린터</label>
            <select v-model="printSettings.printer" class="printer-select">
              <option value="default">기본 프린터</option>
              <option value="label_printer_1">라벨 프린터 1</option>
              <option value="label_printer_2">라벨 프린터 2</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>용지 크기</label>
            <select v-model="printSettings.paperSize" class="paper-select">
              <option value="100x150">100 x 150mm</option>
              <option value="150x200">150 x 200mm</option>
              <option value="75x100">75 x 100mm</option>
            </select>
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group">
            <label>출력 품질</label>
            <select v-model="printSettings.quality" class="quality-select">
              <option value="draft">Draft (빠름)</option>
              <option value="normal">Normal (보통)</option>
              <option value="high">High (높음)</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>매수</label>
            <input
              v-model.number="printSettings.copies"
              type="number"
              min="1"
              max="10"
              class="copies-input"
            />
          </div>
        </div>
        
        <div class="settings-actions">
          <button @click="savePrintSettings" class="btn-save">
            <i class="icon-save"></i>
            설정 저장
          </button>
          <button @click="resetPrintSettings" class="btn-reset">
            <i class="icon-refresh"></i>
            초기화
          </button>
        </div>
      </div>
    </div>

    <!-- 라벨 미리보기 모달 -->
    <LabelPreviewModal
      :show="showPreviewModal"
      :label="selectedLabel"
      @close="closePreviewModal"
      @print="printFromPreview"
      @download="downloadFromPreview"
    />

    <!-- 템플릿 관리 모달 -->
    <TemplateManageModal
      :show="showTemplateModal"
      :templates="labelTemplates"
      @close="closeTemplateModal"
      @create="createTemplate"
      @edit="editTemplate"
      @delete="deleteTemplate"
    />

    <!-- 일괄 생성 모달 -->
    <BulkGenerateModal
      :show="showBulkGenerateModal"
      @close="closeBulkGenerateModal"
      @generate="handleBulkGenerate"
    />

    <!-- QR 스캐너 모달 -->
    <QRScannerModal
      :show="showScannerModal"
      @close="closeScannerModal"
      @scan="handleScanResult"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useWarehouseStore } from '../stores/warehouseStore'
import { useToast } from '@/composables/useToast'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import LabelPreviewModal from '../components/LabelPreviewModal.vue'
import TemplateManageModal from '../components/TemplateManageModal.vue'
import BulkGenerateModal from '../components/BulkGenerateModal.vue'
import QRScannerModal from '../components/QRScannerModal.vue'

const { t } = useI18n()
const router = useRouter()
const warehouseStore = useWarehouseStore()
const { showToast } = useToast()

// 반응형 데이터
const selectedLabelType = ref('single')
const generating = ref(false)
const generatingBatch = ref(false)
const showPrintSettings = ref(false)
const showPreviewModal = ref(false)
const showTemplateModal = ref(false)
const showBulkGenerateModal = ref(false)
const showScannerModal = ref(false)
const selectedLabel = ref<any>(null)
const selectedTemplate = ref<any>(null)

// 단일 라벨 생성 폼
const singleLabelForm = ref({
  code: '',
  type: 'shipping'
})

// 배치 관련
const batchFilter = ref({
  status: '',
  shipType: ''
})
const selectedBatchItems = ref<number[]>([])
const batchLabelType = ref('shipping')
const batchLabelSize = ref('standard')

// 출력 설정
const printSettings = ref({
  printer: 'default',
  paperSize: '100x150',
  quality: 'normal',
  copies: 1
})

// 미리보기 데이터
const previewData = ref<any>(null)

// 라벨 타입
const labelTypes = [
  { value: 'single', label: '단일 생성', icon: 'icon-plus' },
  { value: 'batch', label: '일괄 생성', icon: 'icon-layers' },
  { value: 'template', label: '템플릿 사용', icon: 'icon-layout' }
]

// 생성된 라벨 목록
const generatedLabels = ref([
  {
    id: 1,
    labelCode: 'YCS240812001-L1',
    orderCode: 'YCS240812001',
    type: 'shipping',
    createdAt: new Date(),
    data: {
      itemName: '아이폰 케이스',
      recipientName: '홍길동',
      shippingAddress: '서울시 강남구 테헤란로 123'
    }
  },
  {
    id: 2,
    labelCode: 'YCS240812002-L1',
    orderCode: 'YCS240812002',
    type: 'inventory',
    createdAt: new Date(Date.now() - 10 * 60 * 1000),
    data: {
      itemName: '에어팟 충전기',
      recipientName: '김철수',
      shippingAddress: '부산시 해운대구 센텀로 456'
    }
  }
])

// 배치 아이템 (Mock 데이터)
const batchItems = ref([
  {
    id: 1,
    labelCode: 'YCS240812003-L1',
    itemName: '스마트워치',
    status: 'ready_for_outbound',
    shipType: 'air'
  },
  {
    id: 2,
    labelCode: 'YCS240812004-L1',
    itemName: '무선 이어폰',
    status: 'inbound_completed',
    shipType: 'sea'
  }
])

// 라벨 템플릿
const labelTemplates = ref([
  {
    id: 1,
    name: '표준 배송 라벨',
    description: '기본 배송용 라벨 템플릿',
    previewImage: '/images/template-shipping.png',
    usageCount: 245,
    type: 'shipping'
  },
  {
    id: 2,
    name: '재고 관리 라벨',
    description: '창고 재고용 라벨 템플릿',
    previewImage: '/images/template-inventory.png',
    usageCount: 128,
    type: 'inventory'
  },
  {
    id: 3,
    name: 'QR 코드 라벨',
    description: 'QR 코드가 포함된 라벨 템플릿',
    previewImage: '/images/template-qr.png',
    usageCount: 89,
    type: 'qr'
  }
])

// 컴포넌트 마운트
onMounted(() => {
  loadLabelData()
})

// 메서드
const loadLabelData = async () => {
  // TODO: API 호출로 실제 데이터 가져오기
}

const handleCodeInput = async () => {
  if (singleLabelForm.value.code.length < 3) {
    previewData.value = null
    return
  }

  // Mock 데이터로 미리보기 생성
  await new Promise(resolve => setTimeout(resolve, 300))
  
  previewData.value = {
    orderCode: 'YCS240812001',
    labelCode: singleLabelForm.value.code,
    itemName: '아이폰 케이스',
    recipientName: '홍길동',
    shippingAddress: '서울시 강남구 테헤란로 123'
  }
}

const scanCode = () => {
  showScannerModal.value = true
}

const handleScanResult = (code: string) => {
  singleLabelForm.value.code = code
  closeScannerModal()
  handleCodeInput()
}

const closeScannerModal = () => {
  showScannerModal.value = false
}

const generateSingleLabel = async () => {
  if (!previewData.value) return

  generating.value = true

  try {
    // TODO: 실제 API 호출
    await new Promise(resolve => setTimeout(resolve, 1000))

    const newLabel = {
      id: Date.now(),
      labelCode: previewData.value.labelCode,
      orderCode: previewData.value.orderCode,
      type: singleLabelForm.value.type,
      createdAt: new Date(),
      data: { ...previewData.value }
    }

    generatedLabels.value.unshift(newLabel)
    showToast('라벨이 생성되었습니다', 'success')

    // 폼 초기화
    singleLabelForm.value.code = ''
    previewData.value = null

  } catch (error: any) {
    console.error('Label generation error:', error)
    showToast('라벨 생성 중 오류가 발생했습니다', 'error')
  } finally {
    generating.value = false
  }
}

const applyBatchFilter = () => {
  // TODO: 필터 적용 로직
  showToast('필터가 적용되었습니다', 'info')
}

const toggleBatchItem = (itemId: number) => {
  const index = selectedBatchItems.value.indexOf(itemId)
  if (index === -1) {
    selectedBatchItems.value.push(itemId)
  } else {
    selectedBatchItems.value.splice(index, 1)
  }
}

const selectAllBatch = () => {
  selectedBatchItems.value = batchItems.value.map(item => item.id)
}

const clearBatchSelection = () => {
  selectedBatchItems.value = []
}

const generateBatchLabels = async () => {
  if (selectedBatchItems.value.length === 0) return

  generatingBatch.value = true

  try {
    // TODO: 실제 API 호출
    await new Promise(resolve => setTimeout(resolve, 2000))

    // Mock 라벨 생성
    const newLabels = selectedBatchItems.value.map((itemId, index) => {
      const item = batchItems.value.find(i => i.id === itemId)
      return {
        id: Date.now() + index,
        labelCode: item?.labelCode || '',
        orderCode: `ORDER-${itemId}`,
        type: batchLabelType.value,
        createdAt: new Date(),
        data: {
          itemName: item?.itemName || '',
          recipientName: '수취인',
          shippingAddress: '배송지 주소'
        }
      }
    })

    generatedLabels.value.unshift(...newLabels)
    showToast(`${selectedBatchItems.value.length}개 라벨이 생성되었습니다`, 'success')

    clearBatchSelection()

  } catch (error: any) {
    console.error('Batch label generation error:', error)
    showToast('일괄 라벨 생성 중 오류가 발생했습니다', 'error')
  } finally {
    generatingBatch.value = false
  }
}

const useTemplate = () => {
  if (!selectedTemplate.value) return
  
  showToast(`템플릿 "${selectedTemplate.value.name}"을 사용합니다`, 'info')
  selectedLabelType.value = 'single'
  singleLabelForm.value.type = selectedTemplate.value.type
}

const editTemplate = (template?: any) => {
  const targetTemplate = template || selectedTemplate.value
  if (!targetTemplate) return
  
  showToast(`템플릿 "${targetTemplate.name}"을 편집합니다`, 'info')
  // TODO: 템플릿 편집 기능 구현
}

const createTemplate = () => {
  showToast('새 템플릿을 생성합니다', 'info')
  // TODO: 템플릿 생성 기능 구현
}

const deleteTemplate = (templateId: number) => {
  const index = labelTemplates.value.findIndex(t => t.id === templateId)
  if (index !== -1) {
    labelTemplates.value.splice(index, 1)
    showToast('템플릿이 삭제되었습니다', 'success')
  }
}

// 라벨 액션
const previewLabel = (label: any) => {
  selectedLabel.value = label
  showPreviewModal.value = true
}

const closePreviewModal = () => {
  showPreviewModal.value = false
  selectedLabel.value = null
}

const printLabel = (label: any) => {
  showToast(`${label.labelCode} 라벨을 출력합니다`, 'info')
  // TODO: 실제 출력 기능 구현
}

const printFromPreview = () => {
  if (selectedLabel.value) {
    printLabel(selectedLabel.value)
  }
  closePreviewModal()
}

const downloadLabel = (label: any) => {
  showToast(`${label.labelCode} 라벨을 다운로드합니다`, 'info')
  // TODO: 라벨 다운로드 구현
}

const downloadFromPreview = () => {
  if (selectedLabel.value) {
    downloadLabel(selectedLabel.value)
  }
  closePreviewModal()
}

const duplicateLabel = (label: any) => {
  const duplicated = {
    ...label,
    id: Date.now(),
    labelCode: `${label.labelCode}-COPY`,
    createdAt: new Date()
  }
  generatedLabels.value.unshift(duplicated)
  showToast('라벨이 복제되었습니다', 'success')
}

const deleteLabel = (label: any) => {
  if (confirm('이 라벨을 삭제하시겠습니까?')) {
    const index = generatedLabels.value.findIndex(l => l.id === label.id)
    if (index !== -1) {
      generatedLabels.value.splice(index, 1)
      showToast('라벨이 삭제되었습니다', 'success')
    }
  }
}

const clearAllLabels = () => {
  if (confirm('모든 라벨을 삭제하시겠습니까?')) {
    generatedLabels.value = []
    showToast('모든 라벨이 삭제되었습니다', 'success')
  }
}

const printAllLabels = () => {
  if (generatedLabels.value.length === 0) return
  
  showPrintSettings.value = true
}

// 빠른 액션
const quickGenerateShipping = () => {
  selectedLabelType.value = 'single'
  singleLabelForm.value.type = 'shipping'
  showToast('배송 라벨 생성 모드로 전환되었습니다', 'info')
}

const quickGenerateInventory = () => {
  selectedLabelType.value = 'single'
  singleLabelForm.value.type = 'inventory'
  showToast('재고 라벨 생성 모드로 전환되었습니다', 'info')
}

const quickGenerateQR = () => {
  selectedLabelType.value = 'single'
  singleLabelForm.value.type = 'qr'
  showToast('QR 라벨 생성 모드로 전환되었습니다', 'info')
}

const quickPrintHistory = () => {
  showToast('출력 이력을 조회합니다', 'info')
  // TODO: 출력 이력 조회 기능 구현
}

// 모달 관련
const closeTemplateModal = () => {
  showTemplateModal.value = false
}

const closeBulkGenerateModal = () => {
  showBulkGenerateModal.value = false
}

const handleBulkGenerate = (data: any) => {
  showToast(`${data.count}개 라벨을 일괄 생성합니다`, 'info')
  closeBulkGenerateModal()
  // TODO: 일괄 생성 구현
}

// 출력 설정
const savePrintSettings = () => {
  showToast('출력 설정이 저장되었습니다', 'success')
  // TODO: 설정 저장 구현
}

const resetPrintSettings = () => {
  printSettings.value = {
    printer: 'default',
    paperSize: '100x150',
    quality: 'normal',
    copies: 1
  }
  showToast('출력 설정이 초기화되었습니다', 'info')
}

// 유틸리티 함수
const getLabelTypeTitle = (type: string): string => {
  const typeMap: Record<string, string> = {
    shipping: '배송',
    inventory: '재고',
    qr: 'QR',
    barcode: '바코드'
  }
  return typeMap[type] || type
}

const getStatusText = (status: string): string => {
  const statusMap: Record<string, string> = {
    created: '생성됨',
    inbound_completed: '입고완료',
    ready_for_outbound: '출고준비',
    outbound_completed: '출고완료',
    hold: '보류'
  }
  return statusMap[status] || status
}

const formatTime = (date: Date): string => {
  return new Intl.DateTimeFormat('ko-KR', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}
</script>

<style scoped>
.labels-view {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.labels-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 30px;
}

.header-info h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.labels-description {
  font-size: 14px;
  color: #6b7280;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-template,
.btn-bulk {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  color: #374151;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-template:hover,
.btn-bulk:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.action-card:hover {
  border-color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.action-icon.shipping {
  background: #3b82f6;
}

.action-icon.inventory {
  background: #10b981;
}

.action-icon.qr {
  background: #8b5cf6;
}

.action-icon.history {
  background: #f59e0b;
}

.action-content {
  flex: 1;
}

.action-content h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.action-content p {
  font-size: 14px;
  color: #6b7280;
}

.label-generator,
.generated-labels {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.generator-header,
.labels-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.generator-header h2,
.labels-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.generator-controls {
  display: flex;
  gap: 4px;
  background: #f3f4f6;
  padding: 4px;
  border-radius: 8px;
}

.type-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: none;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
  color: #6b7280;
}

.type-tab.active {
  background: white;
  color: #3b82f6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.single-generator {
  max-width: 800px;
}

.generator-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.input-with-scan {
  display: flex;
  gap: 8px;
}

.code-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 16px;
}

.code-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.btn-scan {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-scan:hover {
  background: #2563eb;
}

.label-type-select {
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 16px;
}

.label-preview {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  background: #fafafa;
}

.label-preview h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.preview-content {
  display: grid;
  grid-template-columns: 1fr 200px;
  gap: 20px;
  margin-bottom: 20px;
}

.preview-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.info-row .label {
  color: #6b7280;
}

.info-row .value {
  color: #1f2937;
  font-weight: 500;
}

.preview-visual {
  display: flex;
  justify-content: center;
}

.label-visual {
  width: 160px;
  height: 120px;
  border: 2px solid #d1d5db;
  border-radius: 8px;
  padding: 12px;
  background: white;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.visual-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 10px;
}

.company-logo {
  font-weight: bold;
  color: #3b82f6;
}

.label-title {
  color: #6b7280;
}

.visual-qr {
  display: flex;
  justify-content: center;
  margin: 8px 0;
}

.qr-placeholder {
  width: 40px;
  height: 40px;
  border: 1px solid #d1d5db;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #9ca3af;
}

.visual-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-line {
  font-size: 8px;
  color: #374151;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.preview-actions {
  text-align: center;
}

.btn-generate {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-generate:hover:not(:disabled) {
  background: #059669;
}

.btn-generate:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.batch-generator {
  max-width: 1000px;
}

.batch-controls {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 24px;
}

.control-row {
  display: flex;
  gap: 20px;
}

.filter-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.batch-filter {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  min-width: 120px;
}

.btn-apply-filter {
  padding: 8px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-apply-filter:hover {
  background: #2563eb;
}

.batch-selection {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.selection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.selection-header h3 {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
}

.selection-actions {
  display: flex;
  gap: 8px;
}

.btn-select-all,
.btn-clear-all {
  padding: 6px 12px;
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.15s ease;
}

.btn-select-all:hover,
.btn-clear-all:hover {
  background: #f3f4f6;
}

.batch-items-list {
  max-height: 300px;
  overflow-y: auto;
}

.batch-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.batch-item:hover {
  background: #f8fafc;
}

.batch-item.selected {
  background: #eff6ff;
  border-left: 4px solid #3b82f6;
}

.batch-item input[type="checkbox"] {
  margin: 0;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-code {
  font-weight: 500;
  color: #1f2937;
}

.item-name {
  font-size: 14px;
  color: #6b7280;
}

.item-status {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.item-status.ready_for_outbound {
  background: #dbeafe;
  color: #1e40af;
}

.item-status.inbound_completed {
  background: #dcfce7;
  color: #166534;
}

.batch-more {
  padding: 12px 20px;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

.batch-actions {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 20px;
  margin-top: 20px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 8px;
}

.batch-options {
  display: flex;
  gap: 16px;
}

.batch-label-type,
.batch-label-size {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

.btn-generate-batch {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-generate-batch:hover:not(:disabled) {
  background: #059669;
}

.btn-generate-batch:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.template-generator {
  max-width: 1000px;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.template-card {
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.template-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.template-card.selected {
  border-color: #3b82f6;
  background: #eff6ff;
}

.template-preview {
  width: 100%;
  height: 120px;
  background: #f3f4f6;
  border-radius: 8px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.template-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.template-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.template-info p {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.template-stats {
  font-size: 12px;
  color: #9ca3af;
}

.template-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.btn-use-template,
.btn-edit-template {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-use-template {
  background: #3b82f6;
  color: white;
  border: 1px solid #3b82f6;
}

.btn-edit-template {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.empty-labels {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
}

.empty-labels i {
  font-size: 48px;
  color: #d1d5db;
  margin-bottom: 16px;
}

.empty-labels h3 {
  font-size: 18px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.labels-actions {
  display: flex;
  gap: 12px;
}

.btn-clear-all,
.btn-print-all {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.15s ease;
}

.btn-clear-all {
  background: white;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.btn-print-all {
  background: #3b82f6;
  color: white;
  border: 1px solid #3b82f6;
}

.btn-print-all:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.labels-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.label-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
  transition: all 0.15s ease;
}

.label-card:hover {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.label-preview-mini {
  margin-bottom: 16px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.label-type {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.label-type.shipping {
  background: #dbeafe;
  color: #1e40af;
}

.label-type.inventory {
  background: #dcfce7;
  color: #166534;
}

.label-type.qr {
  background: #f3e8ff;
  color: #7c3aed;
}

.creation-time {
  font-size: 12px;
  color: #9ca3af;
}

.preview-content .label-code {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.preview-content .order-code {
  font-size: 14px;
  color: #6b7280;
}

.label-actions {
  display: flex;
  gap: 4px;
  justify-content: center;
}

.btn-action {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.15s ease;
}

.btn-action.preview {
  background: #eff6ff;
  color: #3b82f6;
}

.btn-action.print {
  background: #f0fdf4;
  color: #10b981;
}

.btn-action.download {
  background: #fef3c7;
  color: #f59e0b;
}

.btn-action.duplicate {
  background: #f3f4f6;
  color: #6b7280;
}

.btn-action.delete {
  background: #fef2f2;
  color: #dc2626;
}

.btn-action:hover {
  transform: scale(1.1);
}

.print-settings {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 400px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
  z-index: 50;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.settings-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.btn-close {
  width: 24px;
  height: 24px;
  border: none;
  background: none;
  color: #9ca3af;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.btn-close:hover {
  background: #f3f4f6;
  color: #6b7280;
}

.settings-form {
  padding: 20px;
}

.printer-select,
.paper-select,
.quality-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

.copies-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

.settings-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn-save,
.btn-reset {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-save {
  background: #3b82f6;
  color: white;
  border: 1px solid #3b82f6;
}

.btn-reset {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

/* 모바일 반응형 */
@media (max-width: 1024px) {
  .labels-view {
    padding: 16px;
  }

  .labels-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .preview-content {
    grid-template-columns: 1fr;
  }

  .batch-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .template-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .labels-grid {
    grid-template-columns: 1fr;
  }

  .print-settings {
    position: static;
    width: auto;
    margin-top: 20px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  }
}

@media (max-width: 768px) {
  .quick-actions {
    grid-template-columns: 1fr;
  }

  .generator-controls {
    flex-direction: column;
    width: 100%;
  }

  .type-tab {
    justify-content: center;
  }

  .input-with-scan {
    flex-direction: column;
  }

  .batch-options {
    flex-direction: column;
  }

  .template-grid {
    grid-template-columns: 1fr;
  }

  .template-actions {
    flex-direction: column;
  }

  .label-actions {
    flex-wrap: wrap;
    gap: 8px;
  }

  .btn-action {
    flex: 1;
    min-width: 40px;
  }
}
</style>