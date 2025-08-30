import { ref, onUnmounted } from 'vue'
import { BrowserMultiFormatReader } from '@zxing/library'

interface BarcodeScanResult {
  text: string
  format: string
  timestamp: number
}

export function useBarcodeScanner() {
  const isScanning = ref(false)
  const isSupported = ref(false)
  const error = ref<string | null>(null)
  const result = ref<BarcodeScanResult | null>(null)
  
  let codeReader: BrowserMultiFormatReader | null = null
  let videoElement: HTMLVideoElement | null = null
  
  // 바코드 스캐너 지원 여부 확인
  const checkSupport = () => {
    isSupported.value = !!navigator.mediaDevices?.getUserMedia
    return isSupported.value
  }
  
  // 스캐너 시작
  const startScanning = async (videoEl: HTMLVideoElement) => {
    if (!checkSupport()) {
      error.value = '이 브라우저는 카메라를 지원하지 않습니다.'
      return false
    }
    
    try {
      error.value = null
      videoElement = videoEl
      codeReader = new BrowserMultiFormatReader()
      
      // 사용 가능한 비디오 장치 목록 가져오기
      const videoInputDevices = await codeReader.listVideoInputDevices()
      
      if (videoInputDevices.length === 0) {
        throw new Error('카메라를 찾을 수 없습니다.')
      }
      
      // 후면 카메라 우선 선택 (모바일)
      const backCamera = videoInputDevices.find(device => 
        device.label.toLowerCase().includes('back') ||
        device.label.toLowerCase().includes('rear') ||
        device.label.toLowerCase().includes('environment')
      )
      
      const selectedDeviceId = backCamera ? backCamera.deviceId : videoInputDevices[0].deviceId
      
      isScanning.value = true
      
      // 스캔 시작
      await codeReader.decodeFromVideoDevice(
        selectedDeviceId,
        videoElement,
        (resultData, error) => {
          if (resultData) {
            result.value = {
              text: resultData.getText(),
              format: resultData.getBarcodeFormat().toString(),
              timestamp: Date.now()
            }
            
            // 스캔 성공 시 자동 중지 (선택적)
            // stopScanning()
          }
          
          if (error && !(error instanceof Error && error.name === 'NotFoundException')) {
            console.warn('Barcode scanning error:', error)
          }
        }
      )
      
      return true
      
    } catch (err) {
      console.error('Failed to start barcode scanner:', err)
      error.value = err instanceof Error ? err.message : '바코드 스캐너 시작에 실패했습니다.'
      isScanning.value = false
      return false
    }
  }
  
  // 스캐너 중지
  const stopScanning = () => {
    if (codeReader) {
      codeReader.reset()
      codeReader = null
    }
    
    isScanning.value = false
    videoElement = null
  }
  
  // 결과 초기화
  const clearResult = () => {
    result.value = null
    error.value = null
  }
  
  // 송장번호 형식 유효성 검사
  const validateTrackingNumber = (text: string) => {
    // 일반적인 송장번호 패턴 (숫자 8-20자리)
    const patterns = [
      /^\d{8,20}$/, // 숫자만 8-20자리
      /^[A-Z]{2}\d{9}[A-Z]{2}$/, // 우체국 국제 (예: RR123456789KR)
      /^[A-Z]\d{10,12}$/, // 문자+숫자 조합
    ]
    
    return patterns.some(pattern => pattern.test(text.trim()))
  }
  
  // 송장번호에서 택배사 추정
  const guessCarrier = (trackingNumber: string): string | null => {
    const number = trackingNumber.trim()
    
    // CJ대한통운: 10-12자리 숫자
    if (/^\d{10,12}$/.test(number) && number.length <= 12) {
      return 'CJ'
    }
    
    // 한진택배: 10-13자리 숫자
    if (/^\d{10,13}$/.test(number) && number.length >= 12) {
      return 'HANJIN'
    }
    
    // 롯데택배: 11-12자리 숫자
    if (/^\d{11,12}$/.test(number)) {
      return 'LOTTE'
    }
    
    // 우체국: 국제 패턴
    if (/^[A-Z]{2}\d{9}[A-Z]{2}$/.test(number)) {
      return 'POST'
    }
    
    return null
  }
  
  // 스캔된 텍스트에서 송장번호 추출
  const extractTrackingNumber = (scannedText: string) => {
    // 여러 줄이 있을 경우 각 줄을 검사
    const lines = scannedText.split(/[\n\r]+/).map(line => line.trim()).filter(Boolean)
    
    for (const line of lines) {
      // 숫자만 추출
      const numbers = line.replace(/[^\d]/g, '')
      
      if (validateTrackingNumber(numbers)) {
        return {
          trackingNumber: numbers,
          carrier: guessCarrier(numbers),
          originalText: line
        }
      }
      
      // 문자+숫자 패턴 검사 (우체국 등)
      const alphanumeric = line.replace(/[^A-Z0-9]/gi, '').toUpperCase()
      if (validateTrackingNumber(alphanumeric)) {
        return {
          trackingNumber: alphanumeric,
          carrier: guessCarrier(alphanumeric),
          originalText: line
        }
      }
    }
    
    return null
  }
  
  // 컴포넌트 언마운트 시 정리
  onUnmounted(() => {
    stopScanning()
  })
  
  return {
    // State
    isScanning,
    isSupported,
    error,
    result,
    
    // Methods
    checkSupport,
    startScanning,
    stopScanning,
    clearResult,
    validateTrackingNumber,
    guessCarrier,
    extractTrackingNumber
  }
}