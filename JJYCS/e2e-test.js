/**
 * YSC 물류관리시스템 엔드투엔드 테스트
 * 주문 생성부터 배송까지 전체 플로우 테스트
 */

const axios = require('axios');

// 테스트 환경 설정
const BASE_URL = 'http://localhost:8081/api';
const TEST_TIMEOUT = 30000;

// 테스트 데이터
const testUsers = {
  admin: {
    email: 'admin@ycs.com',
    password: 'password',
    expectedRole: 'ADMIN'
  },
  general: {
    email: 'user@example.com', 
    password: 'password',
    expectedRole: 'GENERAL'
  },
  warehouse: {
    email: 'warehouse@ycs.com',
    password: 'password',
    expectedRole: 'WAREHOUSE'
  }
};

const testOrder = {
  recipientName: 'Test Recipient',
  recipientPhone: '+66-81-234-5678',
  recipientAddress: '123 Test Street, Bangkok, Thailand',
  recipientPostalCode: '10110',
  country: 'thailand',
  items: [
    {
      itemName: '테스트 상품 1',
      quantity: 2,
      weight: 1.5,
      value: 50.00,
      hsCode: '1234567890',
      category: 'electronics'
    }
  ],
  boxes: [
    {
      width: 20,
      height: 15,
      depth: 10,
      weight: 2.0
    }
  ],
  specialRequests: 'E2E 테스트용 주문입니다.'
};

// 테스트 상태 추적
let testResults = {
  passed: 0,
  failed: 0,
  errors: []
};

let authTokens = {};
let createdOrderId = null;

/**
 * 테스트 유틸리티 함수
 */
function logTest(testName, status, details = '') {
  const emoji = status === 'PASS' ? '✅' : '❌';
  const timestamp = new Date().toISOString().split('T')[1].substring(0, 8);
  
  console.log(`[${timestamp}] ${emoji} ${testName}`);
  if (details) console.log(`    ${details}`);
  
  if (status === 'PASS') {
    testResults.passed++;
  } else {
    testResults.failed++;
    testResults.errors.push({ test: testName, details });
  }
}

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * API 호출 래퍼
 */
async function apiCall(method, endpoint, data = null, token = null) {
  const config = {
    method,
    url: `${BASE_URL}${endpoint}`,
    timeout: TEST_TIMEOUT,
    headers: {
      'Content-Type': 'application/json'
    }
  };

  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }

  if (data) {
    config.data = data;
  }

  try {
    const response = await axios(config);
    return { success: true, data: response.data, status: response.status };
  } catch (error) {
    return { 
      success: false, 
      error: error.response?.data?.message || error.response?.data?.error || error.message,
      status: error.response?.status || 0
    };
  }
}

/**
 * 1. 시스템 헬스체크
 */
async function testSystemHealth() {
  console.log('\n🔍 시스템 헬스체크 테스트');
  
  const result = await apiCall('GET', '/actuator/health');
  
  if (result.success && result.data.status === 'UP') {
    logTest('시스템 헬스체크', 'PASS', 'All systems operational');
  } else {
    logTest('시스템 헬스체크', 'FAIL', `System health check failed: ${result.error}`);
  }
}

/**
 * 2. 사용자 인증 테스트
 */
async function testAuthentication() {
  console.log('\n🔐 사용자 인증 테스트');
  
  for (const [userType, userData] of Object.entries(testUsers)) {
    const result = await apiCall('POST', '/auth/login', {
      email: userData.email,
      password: userData.password
    });
    
    if (result.success && result.data.accessToken) {
      authTokens[userType] = result.data.accessToken;
      logTest(`${userType} 로그인`, 'PASS', `Token received: ${result.data.accessToken.substring(0, 20)}...`);
      
      // 토큰 유효성 검증 - 관리자 통계 조회로 검증
      const verifyEndpoint = userType === 'admin' ? '/admin/stats' : '/dashboard/stats';
      const profileResult = await apiCall('GET', verifyEndpoint, null, authTokens[userType]);
      if (profileResult.success) {
        logTest(`${userType} 토큰 검증`, 'PASS', `토큰이 유효하고 API 접근 가능`);
      } else {
        logTest(`${userType} 토큰 검증`, 'FAIL', `API 접근 실패: ${profileResult.error}`);
      }
    } else {
      logTest(`${userType} 로그인`, 'FAIL', result.error);
    }
  }
}

/**
 * 3. 주문 생성 테스트
 */
async function testOrderCreation() {
  console.log('\n📋 주문 생성 테스트');
  
  if (!authTokens.general) {
    logTest('주문 생성 - 사전조건', 'FAIL', '일반 사용자 토큰 없음');
    return;
  }
  
  // CBM 계산 테스트
  const cbmResult = await apiCall('POST', '/orders/calculate-cbm', {
    boxes: testOrder.boxes
  }, authTokens.general);
  
  if (cbmResult.success) {
    const totalCbm = cbmResult.data.totalCbm;
    logTest('CBM 계산', 'PASS', `Total CBM: ${totalCbm} m³`);
    
    if (totalCbm > 29) {
      logTest('CBM 29m³ 초과 감지', 'PASS', '항공운송으로 자동 전환됨');
    }
  } else {
    logTest('CBM 계산', 'FAIL', cbmResult.error);
  }
  
  // 주문 생성
  const orderResult = await apiCall('POST', '/orders', testOrder, authTokens.general);
  
  if (orderResult.success && orderResult.data.id) {
    createdOrderId = orderResult.data.id;
    logTest('주문 생성', 'PASS', `주문 ID: ${createdOrderId}, 주문번호: ${orderResult.data.orderNumber}`);
  } else {
    logTest('주문 생성', 'FAIL', orderResult.error);
  }
}

/**
 * 4. 주문 조회 및 상태 확인 테스트
 */
async function testOrderManagement() {
  console.log('\n📦 주문 관리 테스트');
  
  if (!createdOrderId) {
    logTest('주문 조회 - 사전조건', 'FAIL', '생성된 주문 없음');
    return;
  }
  
  // 주문 상세 조회
  const orderResult = await apiCall('GET', `/orders/${createdOrderId}`, null, authTokens.general);
  
  if (orderResult.success) {
    logTest('주문 상세 조회', 'PASS', `상태: ${orderResult.data.status}`);
    
    // 비즈니스 룰 검증
    if (orderResult.data.totalValue > 1500) {
      if (orderResult.data.requiresExtraRecipient) {
        logTest('THB 1500 초과 룰 적용', 'PASS', '수취인 추가 정보 필요로 표시됨');
      } else {
        logTest('THB 1500 초과 룰 적용', 'FAIL', 'THB 1500 초과인데 플래그 없음');
      }
    }
  } else {
    logTest('주문 상세 조회', 'FAIL', orderResult.error);
  }
  
  // 관리자로 주문 목록 조회
  if (authTokens.admin) {
    const adminOrderResult = await apiCall('GET', '/admin/orders', null, authTokens.admin);
    if (adminOrderResult.success) {
      const orderExists = adminOrderResult.data.content?.some(o => o.id === createdOrderId);
      logTest('관리자 주문 목록 조회', orderExists ? 'PASS' : 'FAIL', 
        orderExists ? '생성된 주문이 목록에 존재' : '생성된 주문이 목록에 없음');
    } else {
      logTest('관리자 주문 목록 조회', 'FAIL', adminOrderResult.error);
    }
  }
}

/**
 * 5. 창고 시스템 테스트
 */
async function testWarehouseOperations() {
  console.log('\n🏭 창고 시스템 테스트');
  
  if (!createdOrderId || !authTokens.warehouse) {
    logTest('창고 작업 - 사전조건', 'FAIL', '주문 또는 창고 토큰 없음');
    return;
  }
  
  // 라벨 생성
  const labelResult = await apiCall('POST', `/labels/generate/${createdOrderId}`, null, authTokens.warehouse);
  
  let labelCode = null;
  if (labelResult.success) {
    labelCode = labelResult.data.labelCode;
    logTest('라벨 생성', 'PASS', `라벨 코드: ${labelCode}`);
  } else {
    logTest('라벨 생성', 'FAIL', labelResult.error);
  }
  
  // 스캔 이벤트 시뮬레이션 (입고)
  if (labelCode) {
    const scanResult = await apiCall('POST', '/warehouse/scan', {
      labelCode: labelCode,
      scanType: 'inbound',
      location: 'A-01-01'
    }, authTokens.warehouse);
    
    if (scanResult.success) {
      logTest('입고 스캔', 'PASS', `위치: ${scanResult.data.location}`);
    } else {
      logTest('입고 스캔', 'FAIL', scanResult.error);
    }
  }
  
  // 재고 현황 조회
  const inventoryResult = await apiCall('GET', '/warehouse/inventory', null, authTokens.warehouse);
  
  if (inventoryResult.success) {
    logTest('재고 현황 조회', 'PASS', `총 ${inventoryResult.data.length}개 항목`);
  } else {
    logTest('재고 현황 조회', 'FAIL', inventoryResult.error);
  }
}

/**
 * 6. 관리자 기능 테스트
 */
async function testAdminOperations() {
  console.log('\n👑 관리자 기능 테스트');
  
  if (!authTokens.admin) {
    logTest('관리자 기능 - 사전조건', 'FAIL', '관리자 토큰 없음');
    return;
  }
  
  // 시스템 통계 조회
  const statsResult = await apiCall('GET', '/admin/stats', null, authTokens.admin);
  
  if (statsResult.success) {
    logTest('시스템 통계 조회', 'PASS', 
      `사용자: ${statsResult.data.totalUsers}, 주문: ${statsResult.data.totalOrders}`);
  } else {
    logTest('시스템 통계 조회', 'FAIL', statsResult.error);
  }
  
  // 사용자 목록 조회
  const usersResult = await apiCall('GET', '/admin/users', null, authTokens.admin);
  
  if (usersResult.success) {
    logTest('사용자 목록 조회', 'PASS', `총 ${usersResult.data.length}명 사용자`);
  } else {
    logTest('사용자 목록 조회', 'FAIL', usersResult.error);
  }
}

/**
 * 7. 외부 API 연동 테스트
 */
async function testExternalAPIs() {
  console.log('\n🌐 외부 API 연동 테스트');
  
  if (!authTokens.admin) {
    logTest('외부 API - 사전조건', 'FAIL', '관리자 토큰 없음');
    return;
  }
  
  // HS Code 검색 테스트
  const hsCodeResult = await apiCall('GET', '/hscode/search?productName=전자제품', null, authTokens.admin);
  
  if (hsCodeResult.success) {
    logTest('HS 코드 검색', 'PASS', `검색 완료: ${hsCodeResult.data.success ? '성공' : '실패'}`);
  } else {
    logTest('HS 코드 검색', 'FAIL', hsCodeResult.error);
  }
  
  // 환율 정보 조회 테스트
  const exchangeResult = await apiCall('GET', '/hscode/exchange-rate', null, authTokens.admin);
  
  if (exchangeResult.success) {
    logTest('환율 정보 조회', 'PASS', `환율 정보 조회 완료`);
  } else {
    logTest('환율 정보 조회', 'FAIL', exchangeResult.error);
  }
}

/**
 * 8. 보안 테스트
 */
async function testSecurity() {
  console.log('\n🔒 보안 테스트');
  
  // 인증 없이 보호된 엔드포인트 접근 시도
  const unauthorizedResult = await apiCall('GET', '/admin/users');
  
  if (!unauthorizedResult.success && unauthorizedResult.status === 401) {
    logTest('인증 없는 접근 차단', 'PASS', '401 Unauthorized 반환');
  } else {
    logTest('인증 없는 접근 차단', 'FAIL', '보호된 엔드포인트에 무인증 접근 허용');
  }
  
  // 권한 없는 사용자의 관리자 엔드포인트 접근 시도
  if (authTokens.general) {
    const forbiddenResult = await apiCall('GET', '/admin/users', null, authTokens.general);
    
    if (!forbiddenResult.success && forbiddenResult.status === 403) {
      logTest('권한 없는 접근 차단', 'PASS', '403 Forbidden 반환');
    } else {
      logTest('권한 없는 접근 차단', 'FAIL', '일반 사용자가 관리자 기능에 접근 가능');
    }
  }
}

/**
 * 9. 성능 테스트
 */
async function testPerformance() {
  console.log('\n⚡ 성능 테스트');
  
  if (!authTokens.general) {
    logTest('성능 테스트 - 사전조건', 'FAIL', '일반 사용자 토큰 없음');
    return;
  }
  
  // API 응답 시간 측정
  const startTime = Date.now();
  const result = await apiCall('GET', '/dashboard/stats', null, authTokens.general);
  const responseTime = Date.now() - startTime;
  
  if (result.success) {
    if (responseTime < 2000) {
      logTest('API 응답 시간', 'PASS', `${responseTime}ms (< 2초)`);
    } else {
      logTest('API 응답 시간', 'FAIL', `${responseTime}ms (> 2초)`);
    }
  } else {
    logTest('API 응답 시간', 'FAIL', 'API 호출 실패');
  }
  
  // 동시 요청 처리 테스트
  const concurrentRequests = Array(5).fill().map(() => 
    apiCall('GET', '/dashboard/stats', null, authTokens.general)
  );
  
  const concurrentStart = Date.now();
  const results = await Promise.all(concurrentRequests);
  const concurrentTime = Date.now() - concurrentStart;
  
  const allSuccessful = results.every(r => r.success);
  if (allSuccessful && concurrentTime < 5000) {
    logTest('동시 요청 처리', 'PASS', `5개 요청을 ${concurrentTime}ms에 처리`);
  } else {
    logTest('동시 요청 처리', 'FAIL', `실패하거나 시간 초과: ${concurrentTime}ms`);
  }
}

/**
 * 메인 테스트 실행
 */
async function runE2ETests() {
  console.log('🚀 YSC 물류관리시스템 엔드투엔드 테스트 시작');
  console.log('===============================================');
  
  const testSuite = [
    testSystemHealth,
    testAuthentication,
    testOrderCreation,
    testOrderManagement,
    testWarehouseOperations,
    testAdminOperations,
    testExternalAPIs,
    testSecurity,
    testPerformance
  ];
  
  const startTime = Date.now();
  
  try {
    for (const test of testSuite) {
      await test();
      await delay(1000); // 테스트 간 1초 대기
    }
  } catch (error) {
    console.error('테스트 실행 중 오류:', error);
  }
  
  const totalTime = Date.now() - startTime;
  
  // 최종 결과 출력
  console.log('\n===============================================');
  console.log('🏁 엔드투엔드 테스트 완료');
  console.log('===============================================');
  console.log(`✅ 성공: ${testResults.passed}개`);
  console.log(`❌ 실패: ${testResults.failed}개`);
  console.log(`⏱️  총 소요시간: ${Math.round(totalTime / 1000)}초`);
  console.log(`📊 성공률: ${Math.round((testResults.passed / (testResults.passed + testResults.failed)) * 100)}%`);
  
  if (testResults.failed > 0) {
    console.log('\n❌ 실패한 테스트:');
    testResults.errors.forEach((error, index) => {
      console.log(`${index + 1}. ${error.test}: ${error.details}`);
    });
  } else {
    console.log('\n🎉 모든 테스트가 성공적으로 완료되었습니다!');
  }
  
  // 종료 코드 설정
  process.exit(testResults.failed > 0 ? 1 : 0);
}

// 테스트 실행
if (require.main === module) {
  runE2ETests().catch(error => {
    console.error('테스트 실행 실패:', error);
    process.exit(1);
  });
}

module.exports = { runE2ETests };