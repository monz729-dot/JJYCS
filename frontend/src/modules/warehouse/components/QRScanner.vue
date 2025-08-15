<template>
  <div class="qr-scanner" :class="{ 'scanner-active': isScanning }">
    <!-- Scanner Header -->
    <div class="scanner-header">
      <h2 class="scanner-title">{{ title || $t('scanner.title') }}</h2>
      <p class="scanner-subtitle">{{ $t('scanner.subtitle') }}</p>
    </div>

    <!-- Permission Request -->
    <div v-if="!hasPermission && !permissionError" class="permission-request">
      <div class="permission-icon">
        <CameraIcon class="icon" />
      </div>
      <h3 class="permission-title">{{ $t('scanner.permission_required') }}</h3>
      <p class="permission-message">{{ $t('scanner.permission_message') }}</p>
      <button 
        type="button" 
        class="permission-btn"
        @click="requestPermission"
        :disabled="requestingPermission"
      >
        <LoadingSpinner v-if="requestingPermission" size="small" />
        <CameraIcon v-else class="btn-icon" />
        {{ requestingPermission ? $t('scanner.requesting') : $t('scanner.allow_camera') }}
      </button>
    </div>

    <!-- Permission Error -->
    <div v-else-if="permissionError" class="permission-error">
      <div class="error-icon">
        <ExclamationTriangleIcon class="icon" />
      </div>
      <h3 class="error-title">{{ $t('scanner.permission_denied') }}</h3>
      <p class="error-message">{{ permissionError }}</p>
      <div class="error-actions">
        <button type="button" class="retry-btn" @click="requestPermission">
          {{ $t('scanner.retry') }}
        </button>
        <button type="button" class="manual-btn" @click="toggleManualInput">
          {{ $t('scanner.manual_input') }}
        </button>
      </div>
    </div>

    <!-- Scanner View -->
    <div v-else-if="hasPermission" class="scanner-view">
      <!-- Camera Preview -->
      <div class="camera-container" :class="{ 'scanning': isScanning }">
        <video 
          ref="videoElement" 
          autoplay 
          playsinline 
          muted
          class="camera-video"
        ></video>
        
        <!-- Scanner Overlay -->
        <div class="scanner-overlay">
          <div class="scan-area" :class="{ 'scan-success': recentScan }">
            <div class="scan-corners">
              <div class="corner corner-tl"></div>
              <div class="corner corner-tr"></div>
              <div class="corner corner-bl"></div>
              <div class="corner corner-br"></div>
            </div>
            <div v-if="isScanning" class="scan-line"></div>
          </div>
          
          <!-- Instructions -->
          <div class="scan-instructions">
            <p class="instruction-text">
              {{ scanInstructions || $t('scanner.instruction') }}
            </p>
          </div>
        </div>
        
        <!-- Controls -->
        <div class="scanner-controls">
          <button
            type="button"
            class="control-btn torch-btn"
            @click="toggleTorch"
            v-if="supportsTorch"
            :class="{ active: torchOn }"
          >
            <BoltIcon class="control-icon" />
          </button>
          
          <button
            type="button"
            class="control-btn pause-btn"
            @click="toggleScanning"
          >
            <PauseIcon v-if="isScanning" class="control-icon" />
            <PlayIcon v-else class="control-icon" />
          </button>
          
          <button
            type="button"
            class="control-btn manual-btn"
            @click="toggleManualInput"
          >
            <PencilIcon class="control-icon" />
          </button>
        </div>
      </div>
      
      <!-- Recent Scan Result -->
      <div v-if="recentScan" class="recent-scan">
        <div class="scan-result">
          <CheckCircleIcon class="result-icon" />
          <div class="result-info">
            <p class="result-label">{{ $t('scanner.scanned') }}</p>
            <p class="result-code">{{ recentScan.code }}</p>
          </div>
          <button type="button" class="clear-btn" @click="clearRecentScan">
            <XMarkIcon class="clear-icon" />
          </button>
        </div>
      </div>
    </div>

    <!-- Manual Input Modal -->
    <div v-if="showManualInput" class="manual-input-modal" @click.self="toggleManualInput">
      <div class="manual-input-container">
        <div class="manual-input-header">
          <h3 class="manual-title">{{ $t('scanner.manual_input') }}</h3>
          <button type="button" class="close-btn" @click="toggleManualInput">
            <XMarkIcon class="close-icon" />
          </button>
        </div>
        <div class="manual-input-body">
          <div class="input-group">
            <label class="input-label">{{ $t('scanner.code_label') }}</label>
            <input
              ref="manualInput"
              v-model="manualCode"
              type="text"
              class="manual-input-field"
              :placeholder="$t('scanner.code_placeholder')"
              @keyup.enter="submitManualCode"
              @paste="handlePaste"
            >
          </div>
          <div class="manual-actions">
            <button type="button" class="btn-cancel" @click="toggleManualInput">
              {{ $t('common.cancel') }}
            </button>
            <button 
              type="button" 
              class="btn-submit" 
              @click="submitManualCode"
              :disabled="!manualCode.trim()"
            >
              {{ $t('scanner.submit') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Scanner Status -->
    <div class="scanner-status">
      <div class="status-indicators">
        <div class="status-item" :class="{ active: hasPermission }">
          <div class="status-dot"></div>
          <span class="status-text">{{ $t('scanner.camera') }}</span>
        </div>
        <div class="status-item" :class="{ active: isScanning }">
          <div class="status-dot"></div>
          <span class="status-text">{{ $t('scanner.scanning') }}</span>
        </div>
        <div class="status-item" :class="{ active: !!recentScan }">
          <div class="status-dot"></div>
          <span class="status-text">{{ $t('scanner.detected') }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import {
  CameraIcon,
  ExclamationTriangleIcon,
  BoltIcon,
  PauseIcon,
  PlayIcon,
  PencilIcon,
  CheckCircleIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { Html5QrcodeScanner, Html5QrcodeScannerState, Html5QrcodeScanType } from 'html5-qrcode'

interface ScanResult {
  code: string
  timestamp: number
}

interface Props {
  title?: string
  scanInstructions?: string
  continuous?: boolean
  autoStart?: boolean
  supportedFormats?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  continuous: true,
  autoStart: true,
  supportedFormats: () => ['QR_CODE', 'CODE_128', 'CODE_39']
})

const emit = defineEmits<{
  scan: [code: string]
  error: [error: string]
  started: []
  stopped: []
}>()

// Refs
const videoElement = ref<HTMLVideoElement>()
const manualInput = ref<HTMLInputElement>()

// State
const hasPermission = ref(false)
const permissionError = ref<string | null>(null)
const requestingPermission = ref(false)
const isScanning = ref(false)
const supportsTorch = ref(false)
const torchOn = ref(false)
const recentScan = ref<ScanResult | null>(null)
const showManualInput = ref(false)
const manualCode = ref('')

// Scanner instance
let qrScanner: Html5QrcodeScanner | null = null
let mediaStream: MediaStream | null = null

// Lifecycle
onMounted(() => {
  if (props.autoStart) {
    requestPermission()
  }
})

onUnmounted(() => {
  stopScanning()
  cleanup()
})

// Methods
const requestPermission = async () => {
  requestingPermission.value = true
  permissionError.value = null
  
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { 
        facingMode: 'environment', // Prefer back camera
        width: { ideal: 1280 },
        height: { ideal: 720 }
      }
    })
    
    mediaStream = stream
    hasPermission.value = true
    
    // Check for torch support
    const track = stream.getVideoTracks()[0]
    const capabilities = track.getCapabilities()
    supportsTorch.value = 'torch' in capabilities
    
    await nextTick()
    await initializeScanner()
    
  } catch (err) {
    console.error('Camera permission error:', err)
    hasPermission.value = false
    
    if (err instanceof Error) {
      if (err.name === 'NotAllowedError') {
        permissionError.value = '카메라 접근 권한이 거부되었습니다. 브라우저 설정에서 권한을 허용해주세요.'
      } else if (err.name === 'NotFoundError') {
        permissionError.value = '카메라를 찾을 수 없습니다. 디바이스에 카메라가 연결되어 있는지 확인해주세요.'
      } else if (err.name === 'NotSupportedError') {
        permissionError.value = '이 브라우저에서는 카메라를 지원하지 않습니다.'
      } else {
        permissionError.value = `카메라 초기화 중 오류가 발생했습니다: ${err.message}`
      }
    } else {
      permissionError.value = '알 수 없는 오류가 발생했습니다.'
    }
    
    emit('error', permissionError.value)
  } finally {
    requestingPermission.value = false
  }
}

const initializeScanner = async () => {
  if (!videoElement.value || !mediaStream) return
  
  try {
    // Set video stream
    videoElement.value.srcObject = mediaStream
    await videoElement.value.play()
    
    // Initialize QR scanner
    qrScanner = new Html5QrcodeScanner(
      "qr-scanner-container",
      {
        fps: 10,
        qrbox: { width: 250, height: 250 },
        aspectRatio: 1.0,
        supportedScanTypes: [Html5QrcodeScanType.SCAN_TYPE_CAMERA]
      },
      false // verbose
    )
    
    await startScanning()
    
  } catch (err) {
    console.error('Scanner initialization error:', err)
    emit('error', 'QR 스캐너 초기화 중 오류가 발생했습니다.')
  }
}

const startScanning = async () => {
  if (!qrScanner || isScanning.value) return
  
  try {
    await qrScanner.render(onScanSuccess, onScanError)
    isScanning.value = true
    emit('started')
  } catch (err) {
    console.error('Scanner start error:', err)
    emit('error', '스캔 시작 중 오류가 발생했습니다.')
  }
}

const stopScanning = async () => {
  if (!qrScanner || !isScanning.value) return
  
  try {
    if (qrScanner.getState() === Html5QrcodeScannerState.SCANNING) {
      await qrScanner.clear()
    }
    isScanning.value = false
    emit('stopped')
  } catch (err) {
    console.error('Scanner stop error:', err)
  }
}

const onScanSuccess = (decodedText: string) => {
  console.log('QR Code scanned:', decodedText)
  
  // Store recent scan
  recentScan.value = {
    code: decodedText,
    timestamp: Date.now()
  }
  
  // Auto-clear recent scan after 3 seconds
  setTimeout(() => {
    if (recentScan.value && recentScan.value.timestamp === recentScan.value.timestamp) {
      recentScan.value = null
    }
  }, 3000)
  
  // Emit scan event
  emit('scan', decodedText)
  
  // If not continuous, pause scanning
  if (!props.continuous) {
    toggleScanning()
  }
}

const onScanError = (errorMessage: string) => {
  // Don't emit error for common scanning errors
  if (errorMessage.includes('NotFoundException')) {
    return // No QR code in frame
  }
  console.warn('Scan error:', errorMessage)
}

const toggleScanning = async () => {
  if (isScanning.value) {
    await stopScanning()
  } else {
    await startScanning()
  }
}

const toggleTorch = async () => {
  if (!mediaStream || !supportsTorch.value) return
  
  try {
    const track = mediaStream.getVideoTracks()[0]
    const constraints = { advanced: [{ torch: !torchOn.value }] }
    await track.applyConstraints(constraints)
    torchOn.value = !torchOn.value
  } catch (err) {
    console.error('Torch toggle error:', err)
    emit('error', '플래시 제어 중 오류가 발생했습니다.')
  }
}

const toggleManualInput = () => {
  showManualInput.value = !showManualInput.value
  if (showManualInput.value) {
    nextTick(() => {
      manualInput.value?.focus()
    })
  } else {
    manualCode.value = ''
  }
}

const submitManualCode = () => {
  const code = manualCode.value.trim()
  if (!code) return
  
  recentScan.value = {
    code,
    timestamp: Date.now()
  }
  
  emit('scan', code)
  toggleManualInput()
}

const handlePaste = (event: ClipboardEvent) => {
  const pastedText = event.clipboardData?.getData('text')
  if (pastedText) {
    manualCode.value = pastedText.trim()
    nextTick(() => submitManualCode())
  }
}

const clearRecentScan = () => {
  recentScan.value = null
}

const cleanup = () => {
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop())
    mediaStream = null
  }
  
  if (qrScanner) {
    try {
      qrScanner.clear()
    } catch (err) {
      console.warn('Scanner cleanup error:', err)
    }
    qrScanner = null
  }
}

// Watch for prop changes
watch(() => props.continuous, (newVal) => {
  if (!newVal && isScanning.value && recentScan.value) {
    toggleScanning()
  }
})
</script>

<style scoped>
.qr-scanner {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background: white;
  border-radius: 1rem;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.scanner-header {
  padding: 1.5rem 1.5rem 0;
  text-align: center;
}

.scanner-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.scanner-subtitle {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.permission-request,
.permission-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 2rem 1.5rem;
  text-align: center;
}

.permission-icon,
.error-icon {
  width: 4rem;
  height: 4rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.permission-icon {
  background: #dbeafe;
}

.error-icon {
  background: #fecaca;
}

.permission-icon .icon {
  width: 2rem;
  height: 2rem;
  color: #3b82f6;
}

.error-icon .icon {
  width: 2rem;
  height: 2rem;
  color: #ef4444;
}

.permission-title,
.error-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.permission-message,
.error-message {
  color: #6b7280;
  line-height: 1.6;
  margin: 0;
}

.permission-btn,
.retry-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.permission-btn:hover,
.retry-btn:hover {
  background: #2563eb;
}

.permission-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.btn-icon {
  width: 1rem;
  height: 1rem;
}

.error-actions {
  display: flex;
  gap: 0.75rem;
}

.manual-btn {
  padding: 0.75rem 1.5rem;
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.manual-btn:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.scanner-view {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.camera-container {
  position: relative;
  aspect-ratio: 4/3;
  background: #000;
  border-radius: 0.75rem;
  overflow: hidden;
  margin: 0 1.5rem;
}

.camera-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scanner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
}

.scan-area {
  position: relative;
  width: 250px;
  height: 250px;
  border: 2px solid rgba(255, 255, 255, 0.7);
  border-radius: 1rem;
  transition: all 0.3s;
}

.scan-area.scan-success {
  border-color: #10b981;
  box-shadow: 0 0 20px rgba(16, 185, 129, 0.5);
}

.scan-corners {
  position: relative;
  width: 100%;
  height: 100%;
}

.corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 3px solid #3b82f6;
}

.corner-tl {
  top: -3px;
  left: -3px;
  border-right: none;
  border-bottom: none;
}

.corner-tr {
  top: -3px;
  right: -3px;
  border-left: none;
  border-bottom: none;
}

.corner-bl {
  bottom: -3px;
  left: -3px;
  border-right: none;
  border-top: none;
}

.corner-br {
  bottom: -3px;
  right: -3px;
  border-left: none;
  border-top: none;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #3b82f6, transparent);
  animation: scanLine 2s linear infinite;
}

@keyframes scanLine {
  0% { transform: translateY(0); }
  100% { transform: translateY(246px); }
}

.scan-instructions {
  margin-top: 2rem;
}

.instruction-text {
  color: white;
  text-align: center;
  font-size: 0.875rem;
  margin: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.scanner-controls {
  position: absolute;
  bottom: 1rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 1rem;
}

.control-btn {
  width: 3rem;
  height: 3rem;
  background: rgba(0, 0, 0, 0.7);
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.control-btn:hover {
  background: rgba(0, 0, 0, 0.9);
  border-color: rgba(255, 255, 255, 0.5);
}

.control-btn.active {
  background: #3b82f6;
  border-color: #3b82f6;
}

.control-icon {
  width: 1.25rem;
  height: 1.25rem;
  color: white;
}

.recent-scan {
  margin: 0 1.5rem;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 0.5rem;
  padding: 1rem;
}

.scan-result {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.result-icon {
  width: 1.5rem;
  height: 1.5rem;
  color: #16a34a;
  flex-shrink: 0;
}

.result-info {
  flex: 1;
}

.result-label {
  font-size: 0.75rem;
  color: #166534;
  margin: 0 0 0.25rem 0;
  font-weight: 500;
}

.result-code {
  font-size: 0.875rem;
  color: #15803d;
  margin: 0;
  font-family: monospace;
  word-break: break-all;
}

.clear-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 0.25rem;
  transition: background-color 0.2s;
}

.clear-btn:hover {
  background: rgba(16, 185, 129, 0.1);
}

.clear-icon {
  width: 1rem;
  height: 1rem;
  color: #9ca3af;
}

.manual-input-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
  padding: 1rem;
}

.manual-input-container {
  background: white;
  border-radius: 1rem;
  max-width: 24rem;
  width: 100%;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.manual-input-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 1.5rem 0;
}

.manual-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 0.25rem;
  color: #9ca3af;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #6b7280;
}

.close-icon {
  width: 1.25rem;
  height: 1.25rem;
}

.manual-input-body {
  padding: 1rem 1.5rem 1.5rem;
}

.input-group {
  margin-bottom: 1.5rem;
}

.input-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.manual-input-field {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.manual-input-field:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.manual-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.btn-cancel,
.btn-submit {
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 500;
  font-size: 0.875rem;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-cancel:hover {
  background: #f9fafb;
}

.btn-submit {
  background: #3b82f6;
  color: white;
}

.btn-submit:hover {
  background: #2563eb;
}

.btn-submit:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.scanner-status {
  padding: 1rem 1.5rem 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.status-indicators {
  display: flex;
  justify-content: center;
  gap: 2rem;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.status-dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background: #d1d5db;
  transition: background-color 0.2s;
}

.status-item.active .status-dot {
  background: #10b981;
}

.status-text {
  font-size: 0.75rem;
  color: #6b7280;
  font-weight: 500;
}

.status-item.active .status-text {
  color: #374151;
}

@media (max-width: 640px) {
  .camera-container {
    margin: 0 1rem;
  }
  
  .recent-scan {
    margin: 0 1rem;
  }
  
  .status-indicators {
    gap: 1rem;
  }
  
  .manual-actions {
    flex-direction: column;
  }
}
</style>