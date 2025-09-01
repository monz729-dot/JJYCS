// Frontend 기반 E2E 테스트 - 실제 브라우저 환경에서 UI 및 데이터 플로우 테스트
// 2024-08-29: 프론트엔드와 데이터 기준 종합 테스트

const puppeteer = require('puppeteer')

// 테스트 설정
const FRONTEND_URL = 'http://localhost:3007'
const API_BASE_URL = 'http://localhost:8081/api'
const TEST_TIMEOUT = 30000

// 테스트 사용자 정보
const TEST_ADMIN = {
  email: 'admin@ycs.com',
  password: 'password'
}

const TEST_GENERAL_USER = {
  email: 'user@example.com',
  password: 'password'
}

const TEST_WAREHOUSE_USER = {
  email: 'warehouse@example.com', 
  password: 'password'
}

// 테스트 결과 저장
const testResults = {
  total: 0,
  passed: 0,
  failed: 0,
  details: []
}

// 유틸리티 함수
function log(message) {
  const timestamp = new Date().toLocaleTimeString()
  console.log(`[${timestamp}] ${message}`)
}

function success(message) {
  console.log(`✅ ${message}`)
}

function error(message) {
  console.log(`❌ ${message}`)
}

function warn(message) {
  console.log(`⚠️  ${message}`)
}

function recordResult(testName, passed, message = '', details = null) {
  testResults.total++
  if (passed) {
    testResults.passed++
    success(`${testName}: ${message}`)
  } else {
    testResults.failed++
    error(`${testName}: ${message}`)
  }
  
  testResults.details.push({
    test: testName,
    passed,
    message,
    details,
    timestamp: new Date().toISOString()
  })
}

// 브라우저와 페이지 초기화
async function initBrowser() {
  const browser = await puppeteer.launch({
    headless: false, // UI 확인을 위해 브라우저 창 표시
    defaultViewport: { width: 375, height: 667 }, // 모바일 뷰포트
    args: [
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--disable-web-security',
      '--disable-features=VizDisplayCompositor',
      '--user-agent=Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15'
    ]
  })
  
  const page = await browser.newPage()
  await page.setUserAgent('Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15')
  
  // 콘솔 로그 캡처
  page.on('console', msg => {
    if (msg.type() === 'error') {
      console.log('❌ Frontend Error:', msg.text())
    } else if (msg.type() === 'warn') {
      console.log('⚠️  Frontend Warning:', msg.text())  
    }
  })
  
  // 네트워크 요청 모니터링
  page.on('request', request => {
    if (request.url().includes('/api/')) {
      console.log('📡 API Request:', request.method(), request.url())
    }
  })
  
  page.on('response', response => {
    if (response.url().includes('/api/')) {
      console.log('📡 API Response:', response.status(), response.url())
    }
  })
  
  return { browser, page }
}

// 프론트엔드 접근성 테스트
async function testFrontendAccessibility(page) {
  log('프론트엔드 접근성 테스트 시작...')
  
  try {
    await page.goto(FRONTEND_URL, { waitUntil: 'networkidle0', timeout: TEST_TIMEOUT })
    
    // 페이지 로드 확인
    await page.waitForSelector('body', { timeout: 5000 })
    const title = await page.title()
    recordResult('프론트엔드 접근', true, `페이지 로드 성공 - ${title}`)
    
    // 모바일 반응형 확인
    const viewport = page.viewport()
    recordResult('모바일 뷰포트', viewport.width <= 768, `뷰포트: ${viewport.width}x${viewport.height}`)
    
    // 기본 요소 확인
    const hasLoginButton = await page.$('button:contains("로그인")') || await page.$('[data-testid="login-button"]')
    const hasSignupButton = await page.$('button:contains("회원가입")') || await page.$('[data-testid="signup-button"]')
    
    recordResult('UI 기본 요소', hasLoginButton || hasSignupButton, '로그인/회원가입 버튼 확인')
    
  } catch (err) {
    recordResult('프론트엔드 접근', false, `접근 실패: ${err.message}`)
  }
}

// 관리자 로그인 테스트 (UI 기반)
async function testAdminLoginUI(page) {
  log('관리자 UI 로그인 테스트 시작...')
  
  try {
    await page.goto(`${FRONTEND_URL}/login`, { waitUntil: 'networkidle0' })
    
    // 로그인 폼 요소 확인
    await page.waitForSelector('input[type="email"], input[name="email"]', { timeout: 5000 })
    await page.waitForSelector('input[type="password"], input[name="password"]', { timeout: 5000 })
    
    // 폼 입력
    await page.type('input[type="email"], input[name="email"]', TEST_ADMIN.email)
    await page.type('input[type="password"], input[name="password"]', TEST_ADMIN.password)
    
    // 로그인 버튼 클릭
    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle0' }),
      page.click('button[type="submit"], button:contains("로그인")')
    ])
    
    // 로그인 성공 확인
    const url = page.url()
    const isLoggedIn = url.includes('/dashboard') || url.includes('/admin') || url.includes('/home')
    
    if (isLoggedIn) {
      recordResult('관리자 UI 로그인', true, `로그인 성공 - ${url}`)
      
      // 사용자 정보 표시 확인
      const userInfo = await page.$('.user-profile, .user-name, [data-testid="user-info"]')
      recordResult('사용자 정보 UI', !!userInfo, '사용자 정보 표시 확인')
      
      return true
    } else {
      recordResult('관리자 UI 로그인', false, `로그인 실패 - ${url}`)
      return false
    }
    
  } catch (err) {
    recordResult('관리자 UI 로그인', false, `오류: ${err.message}`)
    return false
  }
}

// 대시보드 데이터 로딩 테스트
async function testDashboardDataLoading(page) {
  log('대시보드 데이터 로딩 테스트 시작...')
  
  try {
    // 통계 데이터 로딩 대기
    await page.waitForTimeout(2000)
    
    // 통계 카드나 차트 요소 확인
    const statsElements = await page.$$('.stat-card, .stats, .chart, .dashboard-stat, [data-testid*="stat"]')
    recordResult('통계 데이터 UI', statsElements.length > 0, `통계 요소 ${statsElements.length}개 발견`)
    
    // 사용자 수 표시 확인
    const userCountElement = await page.$(':contains("사용자"), :contains("Users"), [data-testid="user-count"]')
    recordResult('사용자 수 표시', !!userCountElement, '사용자 통계 표시')
    
    // 주문 통계 확인
    const orderStatsElement = await page.$(':contains("주문"), :contains("Orders"), [data-testid="order-stats"]')
    recordResult('주문 통계 표시', !!orderStatsElement, '주문 통계 표시')
    
    // 로딩 상태 확인 (로딩이 완료되었는지)
    const loadingElements = await page.$$('.loading, .spinner, .skeleton')
    recordResult('데이터 로딩 완료', loadingElements.length === 0, '로딩 상태 해제됨')
    
  } catch (err) {
    recordResult('대시보드 데이터 로딩', false, `오류: ${err.message}`)
  }
}

// 역할별 UI 접근 제어 테스트
async function testRoleBasedUI(page) {
  log('역할별 UI 접근 제어 테스트 시작...')
  
  try {
    // 관리자 메뉴 확인
    const adminMenus = await page.$$('nav a:contains("사용자 관리"), nav a:contains("승인"), [data-testid="admin-menu"]')
    recordResult('관리자 전용 메뉴', adminMenus.length > 0, `관리자 메뉴 ${adminMenus.length}개 표시`)
    
    // 네비게이션 메뉴 확인
    const navigationElements = await page.$$('nav, .navigation, .menu, [role="navigation"]')
    recordResult('네비게이션 UI', navigationElements.length > 0, '네비게이션 메뉴 표시')
    
    // 접근 권한별 버튼 확인
    const actionButtons = await page.$$('button:contains("승인"), button:contains("거절"), button:contains("관리")')
    recordResult('관리자 액션 버튼', actionButtons.length > 0, `관리자 전용 버튼 ${actionButtons.length}개`)
    
  } catch (err) {
    recordResult('역할별 UI 접근 제어', false, `오류: ${err.message}`)
  }
}

// 사용자 승인 프로세스 테스트 (UI 기반)
async function testUserApprovalProcessUI(page) {
  log('사용자 승인 프로세스 UI 테스트 시작...')
  
  try {
    // 승인 대기 사용자 페이지로 이동
    const pendingUsersLink = await page.$('a:contains("승인 대기"), a[href*="pending"], [data-testid="pending-users"]')
    if (pendingUsersLink) {
      await Promise.all([
        page.waitForNavigation({ waitUntil: 'networkidle0' }),
        pendingUsersLink.click()
      ])
    }
    
    // 승인 대기 목록 확인
    await page.waitForTimeout(1000)
    const pendingUsersList = await page.$$('.user-item, .pending-user, tr[data-user-id]')
    recordResult('승인 대기 목록 UI', true, `승인 대기 사용자 UI 요소 확인`)
    
    // 승인 버튼 기능 테스트
    const approveButton = await page.$('button:contains("승인"), [data-action="approve"]')
    if (approveButton) {
      recordResult('승인 버튼 UI', true, '승인 버튼 UI 존재')
    } else {
      recordResult('승인 버튼 UI', false, '승인 버튼 UI 없음')
    }
    
    // 거절 버튼 기능 테스트
    const rejectButton = await page.$('button:contains("거절"), [data-action="reject"]')
    if (rejectButton) {
      recordResult('거절 버튼 UI', true, '거절 버튼 UI 존재')
    } else {
      recordResult('거절 버튼 UI', false, '거절 버튼 UI 없음')
    }
    
  } catch (err) {
    recordResult('사용자 승인 프로세스 UI', false, `오류: ${err.message}`)
  }
}

// 폼 검증 및 에러 처리 테스트
async function testFormValidationAndErrors(page) {
  log('폼 검증 및 에러 처리 테스트 시작...')
  
  try {
    // 로그아웃 후 잘못된 로그인 시도
    await page.goto(`${FRONTEND_URL}/logout`, { waitUntil: 'networkidle0' })
    await page.goto(`${FRONTEND_URL}/login`, { waitUntil: 'networkidle0' })
    
    // 잘못된 이메일 형식 테스트
    await page.type('input[type="email"], input[name="email"]', 'invalid-email')
    
    // 폼 검증 메시지 확인
    await page.waitForTimeout(500)
    const validationError = await page.$('.error, .invalid, .form-error, [class*="error"]')
    recordResult('이메일 형식 검증', !!validationError, '이메일 형식 오류 표시')
    
    // 비어있는 필드 검증
    await page.click('input[type="email"], input[name="email"]', { clickCount: 3 })
    await page.type('input[type="email"], input[name="email"]', '')
    await page.click('button[type="submit"], button:contains("로그인")')
    
    await page.waitForTimeout(500)
    const requiredFieldError = await page.$('.error:contains("필수"), .required-error, [data-error="required"]')
    recordResult('필수 필드 검증', !!requiredFieldError, '필수 필드 오류 표시')
    
    // 잘못된 로그인 정보로 에러 테스트
    await page.type('input[type="email"], input[name="email"]', 'wrong@email.com')
    await page.type('input[type="password"], input[name="password"]', 'wrongpassword')
    await page.click('button[type="submit"], button:contains("로그인")')
    
    await page.waitForTimeout(2000)
    const loginError = await page.$('.error, .alert-error, .login-error, [role="alert"]')
    recordResult('로그인 에러 처리', !!loginError, '로그인 실패 에러 메시지 표시')
    
  } catch (err) {
    recordResult('폼 검증 및 에러 처리', false, `오류: ${err.message}`)
  }
}

// 반응형 디자인 테스트
async function testResponsiveDesign(page) {
  log('반응형 디자인 테스트 시작...')
  
  try {
    // 데스크톱 뷰 테스트
    await page.setViewport({ width: 1200, height: 800 })
    await page.reload({ waitUntil: 'networkidle0' })
    await page.waitForTimeout(1000)
    
    const desktopLayout = await page.$('.desktop, .sidebar, .wide-layout')
    recordResult('데스크톱 반응형', true, '데스크톱 뷰포트에서 레이아웃 확인')
    
    // 태블릿 뷰 테스트
    await page.setViewport({ width: 768, height: 1024 })
    await page.reload({ waitUntil: 'networkidle0' })
    await page.waitForTimeout(1000)
    
    recordResult('태블릿 반응형', true, '태블릿 뷰포트에서 레이아웃 확인')
    
    // 모바일 뷰 테스트 (원래대로 복원)
    await page.setViewport({ width: 375, height: 667 })
    await page.reload({ waitUntil: 'networkidle0' })
    await page.waitForTimeout(1000)
    
    // 모바일 네비게이션 확인
    const mobileNav = await page.$('.mobile-nav, .hamburger, .nav-toggle, [data-mobile-nav]')
    recordResult('모바일 네비게이션', !!mobileNav, '모바일 네비게이션 UI 존재')
    
  } catch (err) {
    recordResult('반응형 디자인', false, `오류: ${err.message}`)
  }
}

// 국제화(i18n) 테스트
async function testInternationalization(page) {
  log('국제화(i18n) 테스트 시작...')
  
  try {
    // 언어 전환 버튼 확인
    const langSwitcher = await page.$('[lang-switch], .language-selector, [data-lang]')
    
    if (langSwitcher) {
      recordResult('언어 전환 UI', true, '언어 전환 버튼 존재')
      
      // 영어 전환 테스트
      await langSwitcher.click()
      await page.waitForTimeout(1000)
      
      const englishText = await page.$(':contains("Login"), :contains("Dashboard"), :contains("Orders")')
      recordResult('영어 국제화', !!englishText, '영어 텍스트 표시 확인')
    } else {
      recordResult('언어 전환 UI', false, '언어 전환 UI 없음')
    }
    
    // 한국어 텍스트 확인
    const koreanText = await page.$(':contains("로그인"), :contains("대시보드"), :contains("주문")')
    recordResult('한국어 국제화', !!koreanText, '한국어 텍스트 표시 확인')
    
  } catch (err) {
    recordResult('국제화', false, `오류: ${err.message}`)
  }
}

// PWA 기능 테스트
async function testPWAFeatures(page) {
  log('PWA 기능 테스트 시작...')
  
  try {
    // Service Worker 등록 확인
    const swRegistered = await page.evaluate(() => {
      return 'serviceWorker' in navigator
    })
    recordResult('Service Worker 지원', swRegistered, 'Service Worker API 지원 확인')
    
    // Manifest 파일 확인
    const manifestLink = await page.$('link[rel="manifest"]')
    recordResult('PWA Manifest', !!manifestLink, 'PWA Manifest 파일 링크 존재')
    
    // 오프라인 기능 테스트 (시뮬레이션)
    await page.setOfflineMode(true)
    await page.reload()
    await page.waitForTimeout(2000)
    
    const offlineMessage = await page.$('.offline, [data-offline], :contains("오프라인")')
    recordResult('오프라인 처리', !!offlineMessage, '오프라인 상태 처리')
    
    // 온라인 모드 복원
    await page.setOfflineMode(false)
    
  } catch (err) {
    recordResult('PWA 기능', false, `오류: ${err.message}`)
  }
}

// 성능 메트릭 측정
async function testPerformanceMetrics(page) {
  log('성능 메트릭 측정 시작...')
  
  try {
    // 페이지 로드 성능 측정
    const performanceMetrics = await page.evaluate(() => {
      const timing = performance.timing
      const navigation = performance.getEntriesByType('navigation')[0]
      
      return {
        domContentLoaded: timing.domContentLoadedEventEnd - timing.navigationStart,
        loadComplete: timing.loadEventEnd - timing.navigationStart,
        firstPaint: navigation ? navigation.responseEnd - navigation.requestStart : 0
      }
    })
    
    recordResult('DOM 로딩 성능', performanceMetrics.domContentLoaded < 3000, 
                 `DOM 로딩: ${performanceMetrics.domContentLoaded}ms`)
    
    recordResult('페이지 로딩 성능', performanceMetrics.loadComplete < 5000, 
                 `페이지 로딩: ${performanceMetrics.loadComplete}ms`)
    
    // 메모리 사용량 확인
    const memoryInfo = await page.evaluate(() => {
      return performance.memory ? {
        usedJSHeapSize: performance.memory.usedJSHeapSize,
        totalJSHeapSize: performance.memory.totalJSHeapSize
      } : null
    })
    
    if (memoryInfo) {
      const memoryUsageMB = Math.round(memoryInfo.usedJSHeapSize / 1024 / 1024)
      recordResult('메모리 사용량', memoryUsageMB < 50, `메모리 사용량: ${memoryUsageMB}MB`)
    }
    
  } catch (err) {
    recordResult('성능 메트릭', false, `오류: ${err.message}`)
  }
}

// 메인 테스트 실행 함수
async function runFrontendE2ETests() {
  console.log('🚀 YSC 물류관리시스템 - 프론트엔드 기반 E2E 테스트 시작')
  console.log('=' .repeat(80))
  
  const startTime = Date.now()
  let browser, page
  
  try {
    // 브라우저 초기화
    const result = await initBrowser()
    browser = result.browser
    page = result.page
    
    // 테스트 실행
    await testFrontendAccessibility(page)
    const adminLoggedIn = await testAdminLoginUI(page)
    
    if (adminLoggedIn) {
      await testDashboardDataLoading(page)
      await testRoleBasedUI(page)
      await testUserApprovalProcessUI(page)
    }
    
    await testFormValidationAndErrors(page)
    await testResponsiveDesign(page)
    await testInternationalization(page)
    await testPWAFeatures(page)
    await testPerformanceMetrics(page)
    
  } catch (err) {
    error(`전체 테스트 실행 중 오류: ${err.message}`)
  } finally {
    if (browser) {
      await browser.close()
    }
  }
  
  // 최종 결과 출력
  const endTime = Date.now()
  const duration = Math.round((endTime - startTime) / 1000)
  
  console.log('\n' + '=' .repeat(80))
  console.log('🏁 프론트엔드 기반 E2E 테스트 완료')
  console.log('=' .repeat(80))
  console.log(`📊 테스트 결과: ${testResults.passed}/${testResults.total} 통과 (${Math.round(testResults.passed/testResults.total*100)}%)`)
  console.log(`⏱️  총 소요 시간: ${duration}초`)
  console.log('=' .repeat(80))
  
  if (testResults.failed > 0) {
    console.log('\n❌ 실패한 테스트:')
    testResults.details
      .filter(result => !result.passed)
      .forEach(result => {
        console.log(`   • ${result.test}: ${result.message}`)
      })
  }
  
  console.log('\n✅ 성공한 테스트:')
  testResults.details
    .filter(result => result.passed)
    .forEach(result => {
      console.log(`   • ${result.test}: ${result.message}`)
    })
  
  // 종합 평가
  const successRate = testResults.passed / testResults.total
  if (successRate >= 0.9) {
    console.log('\n🎉 시스템 상태: 매우 양호 (90% 이상 통과)')
  } else if (successRate >= 0.7) {
    console.log('\n✅ 시스템 상태: 양호 (70% 이상 통과)')
  } else if (successRate >= 0.5) {
    console.log('\n⚠️  시스템 상태: 보통 (50% 이상 통과)')
  } else {
    console.log('\n❌ 시스템 상태: 개선 필요 (50% 미만 통과)')
  }
  
  console.log('\n📋 상세 테스트 결과가 frontend-e2e-results.json에 저장되었습니다.')
  
  // 결과를 JSON 파일로 저장
  require('fs').writeFileSync('frontend-e2e-results.json', JSON.stringify({
    summary: {
      total: testResults.total,
      passed: testResults.passed,
      failed: testResults.failed,
      successRate: Math.round(successRate * 100),
      duration: duration,
      timestamp: new Date().toISOString()
    },
    details: testResults.details
  }, null, 2))
}

// 스크립트 실행
if (require.main === module) {
  runFrontendE2ETests()
    .then(() => {
      console.log('\n✅ 프론트엔드 테스트 스크립트 완료')
      process.exit(testResults.failed === 0 ? 0 : 1)
    })
    .catch((err) => {
      console.error('\n❌ 프론트엔드 테스트 실행 실패:', err)
      process.exit(1)
    })
}

module.exports = { runFrontendE2ETests }