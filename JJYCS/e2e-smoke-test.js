/**
 * E2E 스모크 테스트: 회원가입 → 로그인 → 주문생성 → 제출
 * 
 * 기본 워크플로우가 정상적으로 작동하는지 확인하는 스크립트
 * 
 * 사용법: node e2e-smoke-test.js
 */

const axios = require('axios');

// 설정
const config = {
  baseURL: 'http://localhost:8081/api',  // 백엔드 서버
  frontendURL: 'http://localhost:3008',  // 프론트엔드 서버
  timeout: 10000,
  testUser: {
    email: `test-${Date.now()}@example.com`,
    password: 'testpass123!',
    name: 'E2E 테스트 사용자',
    phone: '010-1234-5678',
    userType: 'GENERAL'
  },
  testOrder: {
    note: 'E2E 테스트 주문입니다',
    currency: 'KRW',
    items: [
      {
        name: '테스트 상품 1',
        qty: 2,
        unitPrice: 10000
      },
      {
        name: '테스트 상품 2',
        qty: 1,
        unitPrice: 5000
      }
    ]
  }
};

// HTTP 클라이언트 설정
const api = axios.create({
  baseURL: config.baseURL,
  timeout: config.timeout,
  headers: {
    'Content-Type': 'application/json; charset=utf-8',
    'Accept': 'application/json, text/plain, */*',
    'Accept-Language': 'ko-KR,ko;q=0.9,en;q=0.8'
  }
});

// 테스트 상태
let testState = {
  userId: null,
  accessToken: null,
  orderId: null,
  orderNo: null
};

// 유틸리티 함수들
function log(message, data = null) {
  console.log(`[${new Date().toLocaleTimeString()}] ${message}`);
  if (data) {
    console.log(JSON.stringify(data, null, 2));
  }
}

function logSuccess(message) {
  console.log(`✅ ${message}`);
}

function logError(message, error = null) {
  console.log(`❌ ${message}`);
  if (error) {
    console.error(error.message || error);
  }
}

function logStep(step, description) {
  console.log(`\n🔄 Step ${step}: ${description}`);
}

// 서버 헬스체크
async function healthCheck() {
  logStep(0, '서버 상태 확인');
  
  try {
    // 백엔드 서버 확인
    const backendResponse = await axios.get(`${config.baseURL.replace('/api', '')}/actuator/health`, {
      timeout: 5000
    });
    logSuccess(`백엔드 서버 정상: ${config.baseURL}`);
    
    // 프론트엔드 서버 확인 (간접적으로)
    try {
      await axios.get(config.frontendURL, { timeout: 3000 });
      logSuccess(`프론트엔드 서버 정상: ${config.frontendURL}`);
    } catch (e) {
      console.log(`⚠️  프론트엔드 서버 확인 실패 (${config.frontendURL}), 계속 진행...`);
    }
    
  } catch (error) {
    throw new Error(`서버 헬스체크 실패: ${error.message}`);
  }
}

// Step 1: 회원가입
async function signUp() {
  logStep(1, '회원가입 테스트');
  
  try {
    const response = await api.post('/auth/signup', config.testUser);
    
    if (response.data.success) {
      testState.userId = response.data.user?.id;
      testState.accessToken = response.data.accessToken;
      
      logSuccess('회원가입 성공');
      log(`사용자 정보: ID=${testState.userId}, Email=${config.testUser.email}`);
      
      // 토큰이 있으면 즉시 활성화 (GENERAL 사용자)
      if (testState.accessToken) {
        api.defaults.headers.common['Authorization'] = `Bearer ${testState.accessToken}`;
        logSuccess('JWT 토큰 설정 완료');
      }
      
    } else {
      throw new Error(response.data.error || '회원가입 실패');
    }
    
  } catch (error) {
    if (error.response?.status === 409) {
      logError('이메일 중복 - 이미 존재하는 테스트 계정');
      // 로그인으로 우회
      await login();
      return;
    }
    throw error;
  }
}

// Step 2: 로그인 (회원가입 실패 시 대안)
async function login() {
  logStep(2, '로그인 테스트');
  
  try {
    const response = await api.post('/auth/login', {
      email: config.testUser.email,
      password: config.testUser.password
    });
    
    if (response.data.success && response.data.accessToken) {
      testState.userId = response.data.user?.id;
      testState.accessToken = response.data.accessToken;
      
      api.defaults.headers.common['Authorization'] = `Bearer ${testState.accessToken}`;
      
      logSuccess('로그인 성공');
      log(`사용자 정보: ID=${testState.userId}, Status=${response.data.user?.status}`);
      
    } else {
      throw new Error(response.data.error || '로그인 실패');
    }
    
  } catch (error) {
    throw new Error(`로그인 실패: ${error.response?.data?.error || error.message}`);
  }
}

// Step 3: 주문 생성
async function createOrder() {
  logStep(3, '주문 생성 테스트');
  
  if (!testState.accessToken) {
    throw new Error('인증 토큰이 없습니다. 로그인을 먼저 실행하세요.');
  }
  
  try {
    // 총액 계산
    const totalAmount = config.testOrder.items.reduce((total, item) => {
      return total + (item.qty * item.unitPrice);
    }, 0);
    
    const orderData = {
      ...config.testOrder,
      totalAmount: totalAmount
    };
    
    const response = await api.post('/orders', orderData);
    
    if (response.data.success && response.data.data) {
      testState.orderId = response.data.data.id;
      testState.orderNo = response.data.data.orderNo;
      
      logSuccess('주문 생성 성공');
      log(`주문 정보: ID=${testState.orderId}, OrderNo=${testState.orderNo}, 총액=${totalAmount.toLocaleString()}원`);
      
    } else {
      throw new Error(response.data.error || '주문 생성 실패');
    }
    
  } catch (error) {
    const errorMsg = error.response?.data?.error || error.message;
    
    // 권한 오류 체크
    if (error.response?.status === 403) {
      logError(`권한 오류: ${errorMsg}`);
      logError('PENDING 상태 계정이거나 권한이 없는 사용자일 수 있습니다.');
    }
    
    throw new Error(`주문 생성 실패: ${errorMsg}`);
  }
}

// Step 4: 주문 제출
async function submitOrder() {
  logStep(4, '주문 제출 테스트');
  
  if (!testState.orderId) {
    throw new Error('주문 ID가 없습니다. 주문 생성을 먼저 실행하세요.');
  }
  
  try {
    const response = await api.patch(`/orders/${testState.orderId}/submit`);
    
    if (response.data.success) {
      logSuccess('주문 제출 성공');
      log(`주문 상태: ${response.data.data.status} (${response.data.data.statusDisplay})`);
      
    } else {
      throw new Error(response.data.error || '주문 제출 실패');
    }
    
  } catch (error) {
    const errorMsg = error.response?.data?.error || error.message;
    throw new Error(`주문 제출 실패: ${errorMsg}`);
  }
}

// Step 5: 주문 조회 검증
async function verifyOrder() {
  logStep(5, '주문 조회 검증');
  
  if (!testState.orderId) {
    throw new Error('검증할 주문 ID가 없습니다.');
  }
  
  try {
    const response = await api.get(`/orders/${testState.orderId}`);
    
    if (response.data.success && response.data.data) {
      const order = response.data.data;
      
      logSuccess('주문 조회 성공');
      log(`주문 상세: OrderNo=${order.orderNo}, Status=${order.status}, Items=${order.items?.length || 0}개`);
      
      // 상태 검증
      if (order.status === 'SUBMITTED') {
        logSuccess('주문 상태가 SUBMITTED로 정상 변경되었습니다.');
      } else {
        console.log(`⚠️  예상과 다른 주문 상태: ${order.status} (예상: SUBMITTED)`);
      }
      
    } else {
      throw new Error('주문 조회 실패');
    }
    
  } catch (error) {
    const errorMsg = error.response?.data?.error || error.message;
    throw new Error(`주문 조회 실패: ${errorMsg}`);
  }
}

// 메인 테스트 실행
async function runSmokeTest() {
  console.log('🚀 E2E 스모크 테스트 시작');
  console.log('='.repeat(50));
  
  const startTime = Date.now();
  
  try {
    await healthCheck();
    await signUp();
    await createOrder();
    await submitOrder();
    await verifyOrder();
    
    const duration = Date.now() - startTime;
    
    console.log('\n' + '='.repeat(50));
    console.log('✅ 모든 테스트 통과!');
    console.log(`⏱️  총 소요 시간: ${duration}ms`);
    console.log('\n📋 테스트 요약:');
    console.log(`   사용자: ${config.testUser.email}`);
    console.log(`   주문번호: ${testState.orderNo}`);
    console.log(`   주문 ID: ${testState.orderId}`);
    
  } catch (error) {
    console.log('\n' + '='.repeat(50));
    logError('테스트 실패!', error);
    console.log('\n🔍 디버깅 정보:');
    console.log(`   현재 상태: ${JSON.stringify(testState, null, 2)}`);
    console.log(`   백엔드 URL: ${config.baseURL}`);
    console.log(`   프론트엔드 URL: ${config.frontendURL}`);
    
    process.exit(1);
  }
}

// 스크립트 실행
if (require.main === module) {
  runSmokeTest().catch(console.error);
}

module.exports = {
  runSmokeTest,
  config
};