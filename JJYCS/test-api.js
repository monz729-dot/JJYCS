#!/usr/bin/env node
/**
 * YCS LMS API Demo Test Script
 * 이 스크립트는 백엔드 API의 주요 기능들을 테스트하고 데모 데이터를 생성합니다.
 */

const axios = require('axios')

const API_BASE = 'http://localhost:8081/api'

// API 클라이언트 설정
const client = axios.create({
  baseURL: API_BASE,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

let authTokens = {}

// 색상 출력을 위한 유틸리티
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m'
}

function log(message, color = 'reset') {
  console.log(`${colors[color]}${message}${colors.reset}`)
}

function logSuccess(message) {
  log(`✅ ${message}`, 'green')
}

function logError(message) {
  log(`❌ ${message}`, 'red')
}

function logInfo(message) {
  log(`ℹ️  ${message}`, 'blue')
}

function logWarning(message) {
  log(`⚠️  ${message}`, 'yellow')
}

// API 요청 헬퍼
async function apiRequest(method, url, data = null, token = null) {
  try {
    const config = {
      method,
      url,
      ...(data && { data }),
      ...(token && { headers: { Authorization: `Bearer ${token}` } })
    }
    
    const response = await client(config)
    return { success: true, data: response.data }
  } catch (error) {
    return { 
      success: false, 
      error: error.response?.data || error.message,
      status: error.response?.status
    }
  }
}

// 1. 사용자 회원가입 테스트
async function testUserRegistration() {
  log('\n🧪 사용자 회원가입 테스트', 'cyan')
  
  const users = [
    {
      type: 'GENERAL',
      email: 'demo@ycs.com',
      password: 'demo123456',
      name: 'Demo User',
      phone: '+82-10-1234-5678',
      personalInfo: {
        birthDate: '1990-01-01',
        gender: 'MALE',
        address: {
          line1: '123 Demo Street',
          city: 'Seoul',
          state: 'Seoul',
          postalCode: '12345',
          country: 'KR'
        }
      }
    },
    {
      type: 'CORPORATE',
      email: 'corporate@ycs.com',
      password: 'corp123456',
      name: 'Corporate Manager',
      phone: '+82-10-2345-6789',
      enterpriseProfile: {
        companyName: 'YCS Demo Corp',
        businessNumber: '123-45-67890',
        companyAddress: {
          line1: '456 Business Ave',
          city: 'Seoul',
          state: 'Seoul',
          postalCode: '54321',
          country: 'KR'
        },
        businessType: 'LOGISTICS',
        numberOfEmployees: 50,
        annualRevenue: 1000000000
      }
    },
    {
      type: 'PARTNER',
      email: 'partner@ycs.com',
      password: 'partner123456',
      name: 'Partner Agent',
      phone: '+82-10-3456-7890',
      partnerRegion: 'SEOUL',
      commissionRate: 0.05
    }
  ]

  for (const user of users) {
    const userData = {
      email: user.email,
      password: user.password,
      name: user.name,
      phone: user.phone,
      userType: user.type,
      ...(user.personalInfo && { personalInfo: user.personalInfo }),
      ...(user.enterpriseProfile && { enterpriseProfile: user.enterpriseProfile }),
      ...(user.partnerRegion && { partnerRegion: user.partnerRegion }),
      ...(user.commissionRate && { commissionRate: user.commissionRate })
    }

    const result = await apiRequest('POST', '/auth/signup', userData)
    if (result.success) {
      logSuccess(`${user.type} 사용자 생성 완료: ${user.email}`)
    } else {
      if (result.status === 400 && result.error?.message?.includes('already exists')) {
        logWarning(`사용자 이미 존재: ${user.email}`)
      } else {
        logError(`${user.type} 사용자 생성 실패: ${JSON.stringify(result.error)}`)
      }
    }
  }
}

// 2. 로그인 테스트
async function testLogin() {
  log('\n🧪 로그인 테스트', 'cyan')
  
  const loginData = [
    { email: 'demo@ycs.com', password: 'demo123456', type: 'GENERAL' },
    { email: 'corporate@ycs.com', password: 'corp123456', type: 'CORPORATE' },
    { email: 'partner@ycs.com', password: 'partner123456', type: 'PARTNER' }
  ]

  for (const login of loginData) {
    const result = await apiRequest('POST', '/auth/login', {
      email: login.email,
      password: login.password
    })

    if (result.success && result.data.token) {
      authTokens[login.type] = result.data.token
      logSuccess(`${login.type} 로그인 성공: ${login.email}`)
      logInfo(`토큰: ${result.data.token.substring(0, 50)}...`)
    } else {
      logError(`${login.type} 로그인 실패: ${JSON.stringify(result.error)}`)
    }
  }
}

// 3. 주문 생성 테스트 (CBM & THB 룰 포함)
async function testOrderCreation() {
  log('\n🧪 주문 생성 테스트', 'cyan')
  
  const token = authTokens.GENERAL
  if (!token) {
    logError('일반 사용자 토큰이 없습니다. 로그인을 먼저 수행하세요.')
    return
  }

  // 일반 주문
  const normalOrder = {
    userId: 1,
    shippingType: 'SEA',
    country: 'KR',
    postalCode: '12345',
    recipientName: 'John Doe',
    recipientPhone: '+82-10-9876-5432',
    recipientAddress: '789 Recipient St, Apt 101, Busan',
    recipientPostalCode: '67890',
    specialRequests: '일반 주문 테스트',
    orderItems: [
      {
        hsCode: '6109.10.00',
        description: 'T-Shirt Nike',
        quantity: 2,
        weight: 0.2,
        width: 30,
        height: 20,
        depth: 10,
        unitPrice: 25.99
      }
    ]
  }

  let result = await apiRequest('POST', '/orders', normalOrder, token)
  if (result.success) {
    logSuccess('일반 주문 생성 완료')
    logInfo(`주문 번호: ${result.data.orderNumber}`)
  } else {
    logError(`일반 주문 생성 실패: ${JSON.stringify(result.error)}`)
  }

  // CBM 29 초과 주문 (자동 항공 전환) - 총 CBM: 30+ CBM으로 설정
  const largeCBMOrder = {
    userId: 1,
    shippingType: 'SEA',
    country: 'KR',
    postalCode: '12345',
    recipientName: 'Jane Smith',
    recipientPhone: '+82-10-8765-4321',
    recipientAddress: '321 Large Order Ave, Seoul',
    recipientPostalCode: '12345',
    specialRequests: 'CBM 테스트 - 해상에서 항공으로 자동 전환 예상',
    orderItems: [
      {
        hsCode: '8517.12.00',
        description: 'Large Electronics',
        quantity: 1,
        weight: 2.0,
        width: 320, // 320cm × 100cm × 100cm = 3.2m³
        height: 100,
        depth: 100,
        unitPrice: 199.99
      },
      {
        hsCode: '8517.12.00',
        description: 'More Large Electronics',
        quantity: 10, // 10개 × 3.0m³ = 30m³ (29 초과)
        weight: 2.0,
        width: 300, // 300cm × 100cm × 100cm = 3.0m³
        height: 100,
        depth: 100,
        unitPrice: 199.99
      }
    ]
  }

  result = await apiRequest('POST', '/orders', largeCBMOrder, token)
  if (result.success) {
    logSuccess('대형 CBM 주문 생성 완료')
    logInfo(`주문 번호: ${result.data.order?.orderNumber || result.data.orderNumber}`)
    if (result.data.order?.shippingType === 'AIR' || result.data.cbmExceeded) {
      logSuccess('✈️ CBM 초과로 항공 배송으로 자동 전환됨!')
    }
  } else {
    logError(`대형 CBM 주문 생성 실패: ${JSON.stringify(result.error)}`)
  }

  // THB 1500 초과 주문
  const highValueOrder = {
    userId: 1,
    shippingType: 'AIR',
    country: 'KR',
    postalCode: '06000',
    recipientName: 'High Value Customer',
    recipientPhone: '+82-10-5555-1234',
    recipientAddress: '555 Premium St, Gangnam',
    recipientPostalCode: '06000',
    specialRequests: 'THB 1500 초과 테스트 + 회원코드 없음',
    orderItems: [
      {
        hsCode: '9102.11.00',
        description: 'Luxury Watch Rolex',
        quantity: 1,
        weight: 0.1,
        width: 15,
        height: 10,
        depth: 5,
        unitPrice: 2000.00 // THB 2000 - 1500 초과
      }
    ]
  }

  result = await apiRequest('POST', '/orders', highValueOrder, token)
  if (result.success) {
    logSuccess('고가 상품 주문 생성 완료')
    logInfo(`주문 번호: ${result.data.order?.orderNumber || result.data.orderNumber}`)
    if (result.data.requiresExtraRecipient || result.data.order?.requiresExtraRecipient) {
      logWarning('💰 THB 1500 초과로 추가 수취인 정보 필요!')
    }
    if (result.data.noMemberCode || result.data.order?.noMemberCode) {
      logWarning('📋 회원코드 미기재로 지연 처리 예정')
    }
  } else {
    logError(`고가 상품 주문 생성 실패: ${JSON.stringify(result.error)}`)
  }
}

// 4. 주문 목록 조회
async function testOrderList() {
  log('\n🧪 주문 목록 조회 테스트', 'cyan')
  
  const token = authTokens.GENERAL
  if (!token) {
    logError('일반 사용자 토큰이 없습니다.')
    return
  }

  const result = await apiRequest('GET', '/orders/user/1', null, token)
  if (result.success) {
    logSuccess(`주문 목록 조회 완료: ${result.data.orders?.length || 0}개`)
    result.data.orders?.forEach((order, index) => {
      logInfo(`${index + 1}. ${order.orderNumber} - ${order.status} (${order.shippingType || order.orderType})`)
    })
  } else {
    logError(`주문 목록 조회 실패: ${JSON.stringify(result.error)}`)
  }
}

// 5. 창고 스캔 테스트
async function testWarehouseScan() {
  log('\n🧪 창고 스캔 테스트', 'cyan')
  
  // 먼저 주문 목록을 가져와서 스캔할 주문 번호를 찾음
  const token = authTokens.GENERAL
  if (!token) {
    logError('사용자 토큰이 없습니다.')
    return
  }

  const ordersResult = await apiRequest('GET', '/orders/user/1', null, token)
  if (!ordersResult.success || !ordersResult.data.orders?.length) {
    logWarning('스캔할 주문이 없습니다.')
    return
  }

  const orderNumber = ordersResult.data.orders[0].orderNumber
  
  const scanData = {
    labelCode: orderNumber,
    scanType: 'INBOUND',
    location: 'A1-01-01',
    notes: 'API 테스트를 통한 입고 처리'
  }

  const result = await apiRequest('POST', '/warehouse/scan', scanData, token)
  if (result.success) {
    logSuccess(`창고 스캔 완료: ${orderNumber}`)
    logInfo(`위치: ${scanData.location}, 타입: ${scanData.scanType}`)
  } else {
    logError(`창고 스캔 실패: ${JSON.stringify(result.error)}`)
  }
}

// 6. 시스템 상태 확인
async function testSystemHealth() {
  log('\n🧪 시스템 상태 확인', 'cyan')
  
  const result = await apiRequest('GET', '/actuator/health')
  if (result.success) {
    logSuccess('시스템 상태: ' + result.data.status)
    if (result.data.components) {
      Object.entries(result.data.components).forEach(([component, status]) => {
        const statusText = typeof status === 'object' ? status.status : status
        logInfo(`${component}: ${statusText}`)
      })
    }
  } else {
    logError(`시스템 상태 확인 실패: ${JSON.stringify(result.error)}`)
  }
}

// 메인 테스트 실행
async function runAllTests() {
  log('🚀 YCS LMS API 데모 테스트 시작', 'bright')
  log('='.repeat(50), 'bright')
  
  try {
    await testUserRegistration()
    await new Promise(resolve => setTimeout(resolve, 1000)) // 1초 대기
    
    await testLogin()
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    await testOrderCreation()
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    await testOrderList()
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    await testWarehouseScan()
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    await testSystemHealth()
    
    log('\n🎉 모든 테스트 완료!', 'green')
    log('='.repeat(50), 'bright')
    
    // 접속 정보 안내
    log('\n📋 시스템 접속 정보:', 'cyan')
    log('프론트엔드: http://localhost:3001', 'blue')
    log('백엔드 API: http://localhost:8081/api', 'blue')
    log('H2 데이터베이스: http://localhost:8081/api/h2-console', 'blue')
    log('  - JDBC URL: jdbc:h2:mem:ycsdb', 'blue')
    log('  - Username: sa', 'blue')
    log('  - Password: (비어있음)', 'blue')
    
    log('\n🔐 테스트 계정 정보:', 'cyan')
    log('일반 사용자: demo@ycs.com / demo123456', 'blue')
    log('기업 사용자: corporate@ycs.com / corp123456', 'blue')
    log('파트너: partner@ycs.com / partner123456', 'blue')
    
  } catch (error) {
    logError(`테스트 실행 중 오류: ${error.message}`)
  }
}

// 스크립트 실행
if (require.main === module) {
  runAllTests()
}

module.exports = {
  runAllTests,
  testUserRegistration,
  testLogin,
  testOrderCreation,
  testOrderList,
  testWarehouseScan,
  testSystemHealth
}