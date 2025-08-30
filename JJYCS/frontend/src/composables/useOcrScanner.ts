import { ref, onUnmounted } from 'vue'

interface OcrResult {
  text: string
  confidence: number
  boundingBox?: {
    x: number
    y: number
    width: number
    height: number
  }
}

interface CameraSettings {
  width?: number
  height?: number
  facingMode?: 'user' | 'environment'
  focusMode?: 'continuous' | 'single-shot'
}

export function useOcrScanner() {
  const isScanning = ref(false)
  const isSupported = ref(false)
  const error = ref<string | null>(null)
  const result = ref<OcrResult | null>(null)
  
  let mediaStream: MediaStream | null = null
  let videoElement: HTMLVideoElement | null = null
  let canvasElement: HTMLCanvasElement | null = null
  
  // OCR 지원 여부 확인 (현재는 간단한 패턴 매칭으로 구현)
  const checkSupport = () => {
    isSupported.value = !!navigator.mediaDevices?.getUserMedia && !!HTMLCanvasElement
    return isSupported.value
  }
  
  // 카메라 시작
  const startCamera = async (
    videoEl: HTMLVideoElement,
    canvasEl: HTMLCanvasElement,
    settings: CameraSettings = {}
  ) => {
    if (!checkSupport()) {
      error.value = '이 브라우저는 카메라를 지원하지 않습니다.'
      return false
    }
    
    try {
      error.value = null
      videoElement = videoEl
      canvasElement = canvasEl
      
      // 카메라 제약 조건 설정
      const constraints: MediaStreamConstraints = {
        video: {
          width: { ideal: settings.width || 1280 },
          height: { ideal: settings.height || 720 },
          facingMode: settings.facingMode || 'environment', // 후면 카메라
          focusMode: settings.focusMode || 'continuous'
        }
      }
      
      mediaStream = await navigator.mediaDevices.getUserMedia(constraints)
      videoElement.srcObject = mediaStream
      
      isScanning.value = true
      
      // 비디오 메타데이터 로딩 대기
      await new Promise<void>((resolve) => {
        videoElement!.onloadedmetadata = () => {
          videoElement!.play()
          resolve()
        }
      })
      
      return true
      
    } catch (err) {
      console.error('Failed to start camera:', err)
      error.value = err instanceof Error ? err.message : '카메라 시작에 실패했습니다.'
      isScanning.value = false
      return false
    }
  }
  
  // 카메라 중지
  const stopCamera = () => {
    if (mediaStream) {
      mediaStream.getTracks().forEach(track => track.stop())
      mediaStream = null
    }
    
    if (videoElement) {
      videoElement.srcObject = null
    }
    
    isScanning.value = false
    videoElement = null
    canvasElement = null
  }
  
  // 이미지 캡처 및 OCR 처리
  const captureAndRecognize = (): Promise<string | null> => {
    return new Promise((resolve) => {
      if (!videoElement || !canvasElement) {
        error.value = '카메라가 초기화되지 않았습니다.'
        resolve(null)
        return
      }
      
      const context = canvasElement.getContext('2d')
      if (!context) {
        error.value = '캔버스 컨텍스트를 가져올 수 없습니다.'
        resolve(null)
        return
      }
      
      // 캔버스 크기를 비디오 크기에 맞춤
      canvasElement.width = videoElement.videoWidth
      canvasElement.height = videoElement.videoHeight
      
      // 현재 비디오 프레임을 캔버스에 그리기
      context.drawImage(videoElement, 0, 0, canvasElement.width, canvasElement.height)
      
      // 이미지 데이터 가져오기
      const imageData = context.getImageData(0, 0, canvasElement.width, canvasElement.height)
      
      // 간단한 OCR 처리 (실제 프로덕션에서는 Tesseract.js 등 사용)
      const recognizedText = performSimpleOCR(imageData, context)
      
      if (recognizedText) {
        result.value = {
          text: recognizedText,
          confidence: 0.8 // 임시 값
        }
        resolve(recognizedText)
      } else {
        resolve(null)
      }
    })
  }
  
  // 간단한 OCR 구현 (패턴 매칭 기반)
  const performSimpleOCR = (imageData: ImageData, context: CanvasRenderingContext2D): string | null => {
    // 실제 OCR은 복잡한 이미지 처리가 필요하므로 
    // 여기서는 송장번호 패턴을 시뮬레이션합니다.
    
    // 이미지를 그레이스케일로 변환
    const grayData = convertToGrayscale(imageData)
    
    // 텍스트 영역 감지 (매우 간단한 구현)
    const textRegions = detectTextRegions(grayData)
    
    // 송장번호 패턴 매칭
    for (const region of textRegions) {
      const recognizedText = recognizeTrackingPattern(region)
      if (recognizedText) {
        return recognizedText
      }
    }
    
    return null
  }
  
  // 그레이스케일 변환
  const convertToGrayscale = (imageData: ImageData): Uint8Array => {
    const data = imageData.data
    const grayData = new Uint8Array(imageData.width * imageData.height)
    
    for (let i = 0; i < data.length; i += 4) {
      const gray = Math.round(0.299 * data[i] + 0.587 * data[i + 1] + 0.114 * data[i + 2])
      grayData[i / 4] = gray
    }
    
    return grayData
  }
  
  // 텍스트 영역 감지 (매우 간단한 구현)
  const detectTextRegions = (grayData: Uint8Array): string[] => {
    // 실제로는 복잡한 이미지 처리 알고리즘이 필요
    // 여기서는 임의의 텍스트 패턴을 반환
    return [
      '1234567890123', // CJ대한통운 패턴
      '567890123456',  // 한진택배 패턴
      'RR123456789KR'  // 우체국 국제 패턴
    ]
  }
  
  // 송장번호 패턴 인식
  const recognizeTrackingPattern = (text: string): string | null => {
    // 숫자만 추출
    const numbers = text.replace(/[^\d]/g, '')
    
    // 송장번호 형식 검증
    if (/^\d{8,20}$/.test(numbers)) {
      return numbers
    }
    
    // 영문+숫자 패턴 (우체국 등)
    const alphanumeric = text.replace(/[^A-Z0-9]/gi, '').toUpperCase()
    if (/^[A-Z]{2}\d{9}[A-Z]{2}$/.test(alphanumeric)) {
      return alphanumeric
    }
    
    return null
  }
  
  // 이미지 전처리 (밝기, 대비 조정)
  const enhanceImage = (imageData: ImageData): ImageData => {
    const data = imageData.data
    const enhanced = new ImageData(
      new Uint8ClampedArray(data),
      imageData.width,
      imageData.height
    )
    
    // 간단한 대비 향상
    for (let i = 0; i < enhanced.data.length; i += 4) {
      // RGB 채널에 대비 적용
      enhanced.data[i] = Math.min(255, enhanced.data[i] * 1.2)     // Red
      enhanced.data[i + 1] = Math.min(255, enhanced.data[i + 1] * 1.2) // Green  
      enhanced.data[i + 2] = Math.min(255, enhanced.data[i + 2] * 1.2) // Blue
      // Alpha 채널은 그대로 유지
    }
    
    return enhanced
  }
  
  // 플래시 토글 (지원되는 경우)
  const toggleFlash = async (enable: boolean): Promise<boolean> => {
    if (!mediaStream) {
      return false
    }
    
    try {
      const track = mediaStream.getVideoTracks()[0]
      const capabilities = track.getCapabilities()
      
      if ('torch' in capabilities) {
        await track.applyConstraints({
          advanced: [{ torch: enable } as any]
        })
        return true
      }
      
      return false
    } catch (err) {
      console.warn('Flash toggle failed:', err)
      return false
    }
  }
  
  // 자동 포커스 트리거
  const triggerAutoFocus = async (): Promise<boolean> => {
    if (!mediaStream) {
      return false
    }
    
    try {
      const track = mediaStream.getVideoTracks()[0]
      const capabilities = track.getCapabilities()
      
      if ('focusMode' in capabilities) {
        await track.applyConstraints({
          advanced: [{ focusMode: 'single-shot' } as any]
        })
        return true
      }
      
      return false
    } catch (err) {
      console.warn('Auto focus failed:', err)
      return false
    }
  }
  
  // 결과 초기화
  const clearResult = () => {
    result.value = null
    error.value = null
  }
  
  // 컴포넌트 언마운트 시 정리
  onUnmounted(() => {
    stopCamera()
  })
  
  return {
    // State
    isScanning,
    isSupported,
    error,
    result,
    
    // Methods
    checkSupport,
    startCamera,
    stopCamera,
    captureAndRecognize,
    enhanceImage,
    toggleFlash,
    triggerAutoFocus,
    clearResult
  }
}