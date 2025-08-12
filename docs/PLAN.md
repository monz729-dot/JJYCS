# YSC LMS 프로젝트 구현 계획서

## 프로젝트 개요
**YSC 물류관리시스템(LMS)** - 모바일 웹 중심 물류 관리 MVP를 2스프린트 내 완성

### 핵심 목표
- 모바일 최적화된 주문/창고/파트너/어드민 시스템 구축
- 비즈니스 룰 자동화 (CBM 계산, 금액 기반 경고, 회원코드 검증)
- EMS/HS 코드 검증 및 실시간 트래킹
- PWA 기반 라벨/QR 스캔 기능

---

## 마일스톤 및 스프린트 계획

### Sprint 1 (2주) - 백엔드 코어 + 회원/주문 생성
**목표**: 기본 인프라 구축 및 핵심 비즈니스 로직 구현

#### Week 1
- [ ] **DB 스키마 설계 및 마이그레이션**
  - Flyway 기반 MySQL 8 스키마
  - 인덱스 최적화 및 가상컬럼 구현
  - 테스트 데이터 시딩

- [ ] **Authentication 시스템**
  - OAuth2 + 2FA 구현
  - 역할별 회원가입 (일반/기업/파트너/창고/어드민)
  - 기업/파트너 승인 워크플로우

#### Week 2
- [ ] **주문 관리 시스템**
  - 주문 생성 API (비즈니스 룰 포함)
  - CBM 자동 계산 및 29 초과시 air 전환
  - THB 1,500 초과시 추가 정보 경고
  - EMS/HS 코드 검증

- [ ] **창고 관리 기초**
  - 입고 스캔 API
  - 재고 관리 기본 기능

- [ ] **프론트엔드 기초 구조**
  - Vue3 + TypeScript 프로젝트 설정
  - 기본 라우팅 및 상태 관리 (Pinia)
  - 인증 관련 UI

### Sprint 2 (2주) - 출고/라벨/정산/파트너
**목표**: 전체 워크플로우 완성 및 고급 기능 구현

#### Week 3
- [ ] **라벨/QR 시스템**
  - QR 코드 생성 및 스캔
  - PWA 카메라 접근
  - 라벨 출력 기능

- [ ] **일괄 처리 기능**
  - 다중 선택 UI
  - 일괄 입고/출고 처리
  - 믹스박스/보류 상태 관리

#### Week 4
- [ ] **견적 및 정산 시스템**
  - 1차/2차 견적 관리
  - 로컬운임/리패킹 비용 계산
  - 파트너 정산 기초

- [ ] **통합 테스트 및 최적화**
  - E2E 시나리오 테스트
  - 모바일 최적화
  - 성능 튜닝

---

## 생성할 파일별 변경 목록

### Backend Structure
```
/backend
├── src/main/java/com/ysc/lms/
│   ├── config/
│   │   ├── SecurityConfig.java         [NEW] OAuth2 + 2FA 설정
│   │   ├── DatabaseConfig.java         [NEW] MyBatis 설정
│   │   └── RedisConfig.java            [NEW] 세션 관리
│   ├── controller/
│   │   ├── AuthController.java         [NEW] 인증/회원가입
│   │   ├── OrdersController.java       [NEW] 주문 관리
│   │   ├── WarehouseController.java    [NEW] 창고 스캔/재고
│   │   ├── EstimatesController.java    [NEW] 견적 관리
│   │   ├── TrackingController.java     [NEW] 배송 추적
│   │   └── AdminController.java        [NEW] 관리자 기능
│   ├── service/
│   │   ├── AuthService.java            [NEW] 인증 비즈니스 로직
│   │   ├── OrderService.java           [NEW] 주문 처리 + 비즈니스 룰
│   │   ├── WarehouseService.java       [NEW] 창고 관리
│   │   ├── ValidationService.java      [NEW] EMS/HS 검증
│   │   └── NotificationService.java    [NEW] 알림/이메일
│   ├── dto/
│   │   ├── auth/                       [NEW] 인증 관련 DTO
│   │   ├── orders/                     [NEW] 주문 관련 DTO
│   │   └── warehouse/                  [NEW] 창고 관련 DTO
│   ├── entity/                         [NEW] JPA 엔티티
│   ├── repository/                     [NEW] MyBatis 매퍼
│   └── util/
│       ├── CBMCalculator.java          [NEW] CBM 계산 유틸
│       ├── QRCodeGenerator.java        [NEW] QR 생성
│       └── ValidationUtil.java         [NEW] 검증 유틸
└── src/main/resources/
    ├── db/migration/                   [NEW] Flyway 마이그레이션
    ├── mapper/                         [NEW] MyBatis XML 매퍼
    └── application.yml                 [NEW] 설정 파일
```

### Frontend Structure
```
/frontend
├── src/
│   ├── modules/
│   │   ├── auth/
│   │   │   ├── views/
│   │   │   │   ├── LoginView.vue       [NEW] 로그인
│   │   │   │   ├── RegisterView.vue    [NEW] 회원가입
│   │   │   │   └── ApprovalView.vue    [NEW] 승인 대기
│   │   │   ├── components/
│   │   │   │   ├── UserRegistration.vue [NEW] 사용자 등록 폼
│   │   │   │   └── TwoFactorAuth.vue   [NEW] 2FA 컴포넌트
│   │   │   └── stores/
│   │   │       └── authStore.ts        [NEW] 인증 상태 관리
│   │   ├── orders/
│   │   │   ├── views/
│   │   │   │   ├── OrderListView.vue   [NEW] 주문 목록
│   │   │   │   ├── OrderCreateView.vue [NEW] 주문 생성
│   │   │   │   └── OrderDetailView.vue [NEW] 주문 상세
│   │   │   ├── components/
│   │   │   │   ├── OrderForm.vue       [NEW] 주문 폼
│   │   │   │   ├── CBMCalculator.vue   [NEW] CBM 계산기
│   │   │   │   ├── StatusChip.vue      [NEW] 상태 칩
│   │   │   │   └── WarningModal.vue    [NEW] 경고 모달
│   │   │   └── stores/
│   │   │       └── orderStore.ts       [NEW] 주문 상태 관리
│   │   ├── warehouse/
│   │   │   ├── views/
│   │   │   │   ├── ScanView.vue        [NEW] QR 스캔
│   │   │   │   ├── InventoryView.vue   [NEW] 재고 관리
│   │   │   │   └── BatchProcessView.vue [NEW] 일괄 처리
│   │   │   ├── components/
│   │   │   │   ├── QRScanner.vue       [NEW] QR 스캐너
│   │   │   │   ├── LabelPrinter.vue    [NEW] 라벨 출력
│   │   │   │   └── BatchSelector.vue   [NEW] 다중 선택
│   │   │   └── stores/
│   │   │       └── warehouseStore.ts   [NEW] 창고 상태 관리
│   │   ├── partner/
│   │   │   ├── views/
│   │   │   │   ├── DashboardView.vue   [NEW] 파트너 대시보드
│   │   │   │   └── SettlementView.vue  [NEW] 정산 관리
│   │   │   └── stores/
│   │   │       └── partnerStore.ts     [NEW] 파트너 상태 관리
│   │   └── admin/
│   │       ├── views/
│   │       │   ├── ApprovalView.vue    [NEW] 승인 관리
│   │       │   ├── UserManageView.vue  [NEW] 사용자 관리
│   │       │   └── ConfigView.vue      [NEW] 설정 관리
│   │       └── stores/
│   │           └── adminStore.ts       [NEW] 관리자 상태 관리
│   ├── components/ui/
│   │   ├── BaseButton.vue              [NEW] 기본 버튼
│   │   ├── BaseModal.vue               [NEW] 기본 모달
│   │   ├── BaseToast.vue               [NEW] 토스트
│   │   ├── LoadingSpinner.vue          [NEW] 로딩
│   │   └── MobileLayout.vue            [NEW] 모바일 레이아웃
│   ├── i18n/
│   │   ├── ko.json                     [NEW] 한국어
│   │   └── en.json                     [NEW] 영어
│   ├── router/
│   │   └── index.ts                    [NEW] 라우터 설정
│   ├── stores/
│   │   └── index.ts                    [NEW] Pinia 설정
│   ├── utils/
│   │   ├── api.ts                      [NEW] API 클라이언트
│   │   ├── validation.ts               [NEW] 검증 유틸
│   │   └── format.ts                   [NEW] 포매팅 유틸
│   ├── types/                          [NEW] TypeScript 타입 정의
│   ├── App.vue                         [NEW] 메인 앱
│   └── main.ts                         [NEW] 앱 진입점
├── public/
│   ├── manifest.json                   [NEW] PWA 매니페스트
│   └── sw.js                          [NEW] 서비스 워커
├── package.json                        [NEW] 의존성
├── vite.config.ts                      [NEW] Vite 설정
└── tsconfig.json                       [NEW] TypeScript 설정
```

---

## DB 마이그레이션 (MySQL 8, Flyway)

### 주요 테이블 구조
- **users**: 사용자 기본 정보 + member_code
- **enterprise_profiles**: 기업 회원 추가 정보
- **partner_profiles**: 파트너 회원 추가 정보  
- **orders**: 주문 정보 + order_type(air/sea) + requires_extra_recipient
- **order_items**: 주문 상품 정보 + 금액
- **order_boxes**: 박스 정보 + 치수 → CBM 가상컬럼
- **warehouses**: 창고 정보
- **scan_events**: 스캔 이벤트 로그
- **shipment_tracking**: 배송 추적

### 비즈니스 룰 구현
1. **CBM 계산**: `order_boxes.cbm_m3` 가상컬럼으로 자동 계산
2. **29 초과 감지**: 주문 생성/수정시 트리거로 order_type 자동 변경
3. **THB 1,500 체크**: 주문 총액 계산 후 requires_extra_recipient 플래그
4. **member_code 검증**: users.member_code NULL 체크

---

## 백엔드 엔드포인트

### 1. Orders API
```java
@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // 1. CBM 계산 및 29 초과시 air 전환
        // 2. THB 1,500 초과시 경고 플래그
        // 3. member_code 검증
        // 4. EMS/HS 코드 검증
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {}
    
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id, @Valid @RequestBody UpdateOrderRequest request) {}
    
    @GetMapping
    public ResponseEntity<PagedResponse<OrderSummary>> getOrders(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size) {}
}
```

### 2. Warehouse Scan API
```java
@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {
    
    @PostMapping("/scan")
    public ResponseEntity<ScanResponse> processScan(@Valid @RequestBody ScanRequest request) {
        // label_code로 주문/박스 조회
        // scan_type에 따른 처리 (inbound/outbound/hold/mixbox)
        // 상태 업데이트 및 로그 기록
    }
    
    @PostMapping("/batch-process")
    public ResponseEntity<BatchProcessResponse> batchProcess(@Valid @RequestBody BatchProcessRequest request) {}
    
    @GetMapping("/inventory")
    public ResponseEntity<PagedResponse<InventoryItem>> getInventory(@RequestParam(defaultValue = "0") int page) {}
}
```

### 3. Estimates API
```java
@RestController
@RequestMapping("/api/estimates")
public class EstimatesController {
    
    @PostMapping("/orders/{orderId}")
    public ResponseEntity<EstimateResponse> createEstimate(@PathVariable Long orderId,
                                                          @Valid @RequestBody EstimateRequest request) {}
    
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<List<EstimateResponse>> getEstimates(@PathVariable Long orderId) {}
}
```

### 4. Tracking API
```java
@RestController
@RequestMapping("/api/tracking")  
public class TrackingController {
    
    @GetMapping("/ems/{trackingNumber}")
    public ResponseEntity<TrackingResponse> getEMSTracking(@PathVariable String trackingNumber) {}
    
    @PostMapping("/sync/{orderId}")
    public ResponseEntity<SyncResponse> syncTracking(@PathVariable Long orderId) {}
}
```

### 5. Auth API
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        // 역할별 추가 정보 처리
        // 기업/파트너는 승인 대기 상태로 설정
    }
    
    @PostMapping("/approve/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApprovalResponse> approveUser(@PathVariable Long userId) {}
}
```

---

## 프론트엔드 라우트 및 컴포넌트

### 라우터 구조
```typescript
const routes = [
  {
    path: '/auth',
    component: () => import('@/layouts/AuthLayout.vue'),
    children: [
      { path: 'login', component: () => import('@/modules/auth/views/LoginView.vue') },
      { path: 'register', component: () => import('@/modules/auth/views/RegisterView.vue') },
      { path: 'approval', component: () => import('@/modules/auth/views/ApprovalView.vue') }
    ]
  },
  {
    path: '/orders',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', component: () => import('@/modules/orders/views/OrderListView.vue') },
      { path: 'create', component: () => import('@/modules/orders/views/OrderCreateView.vue') },
      { path: ':id', component: () => import('@/modules/orders/views/OrderDetailView.vue') }
    ]
  },
  {
    path: '/warehouse',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: 'scan', component: () => import('@/modules/warehouse/views/ScanView.vue') },
      { path: 'inventory', component: () => import('@/modules/warehouse/views/InventoryView.vue') },
      { path: 'batch', component: () => import('@/modules/warehouse/views/BatchProcessView.vue') }
    ]
  }
]
```

### 핵심 컴포넌트

#### 1. CBM Calculator Component
```vue
<template>
  <div class="cbm-calculator">
    <div class="input-group">
      <input v-model="width" @input="calculateCBM" placeholder="폭 (cm)" />
      <input v-model="height" @input="calculateCBM" placeholder="높이 (cm)" />
      <input v-model="depth" @input="calculateCBM" placeholder="깊이 (cm)" />
    </div>
    <div class="result">
      <span>CBM: {{ cbm }} m³</span>
      <div v-if="cbm > 29" class="warning">
        <i class="icon-warning"></i>
        {{ $t('cbm.warning.air_conversion') }}
      </div>
    </div>
  </div>
</template>
```

#### 2. Warning Modal Component
```vue
<template>
  <BaseModal :show="show" @close="$emit('close')">
    <div class="warning-modal">
      <div class="icon">
        <i :class="warningIcon"></i>
      </div>
      <h3>{{ warningTitle }}</h3>
      <p>{{ warningMessage }}</p>
      <div class="actions">
        <BaseButton @click="$emit('continue')">{{ $t('common.continue') }}</BaseButton>
        <BaseButton variant="secondary" @click="$emit('close')">{{ $t('common.cancel') }}</BaseButton>
      </div>
    </div>
  </BaseModal>
</template>
```

#### 3. QR Scanner Component
```vue
<template>
  <div class="qr-scanner">
    <div v-if="!hasPermission" class="permission-request">
      <p>{{ $t('scanner.permission_required') }}</p>
      <BaseButton @click="requestPermission">{{ $t('scanner.allow_camera') }}</BaseButton>
    </div>
    <div v-else class="scanner-container">
      <video ref="video" autoplay playsinline></video>
      <div class="scanner-overlay">
        <div class="scan-area"></div>
      </div>
    </div>
  </div>
</template>
```

---

## 비즈니스 규칙 검증

### 1. CBM 29 초과시 air 자동 전환
```typescript
// OrderService.ts
export class OrderService {
  async createOrder(orderData: CreateOrderRequest): Promise<OrderResponse> {
    // CBM 계산
    const totalCBM = this.calculateTotalCBM(orderData.boxes)
    
    // 29 초과시 자동 air 전환
    if (totalCBM > 29) {
      orderData.orderType = 'air'
      // 경고 플래그 설정
      this.addWarning('CBM_EXCEEDED', { cbm: totalCBM })
    }
    
    return this.repository.create(orderData)
  }
}
```

### 2. THB 1,500 초과시 수취인 추가 정보 경고
```typescript
// ValidationService.ts
export class ValidationService {
  validateOrderAmount(items: OrderItem[]): ValidationResult {
    const totalAmount = items.reduce((sum, item) => sum + item.amount, 0)
    
    if (totalAmount > 1500 && items.some(item => item.currency === 'THB')) {
      return {
        requiresExtraRecipient: true,
        warning: 'AMOUNT_EXCEEDED_THB_1500'
      }
    }
    
    return { requiresExtraRecipient: false }
  }
}
```

### 3. member_code 미기재시 지연 처리
```typescript
// AuthService.ts
export class AuthService {
  async validateMemberCode(userId: number): Promise<boolean> {
    const user = await this.userRepository.findById(userId)
    
    if (!user.memberCode) {
      // 지연 상태로 설정
      await this.orderRepository.updateStatus(userId, 'DELAYED')
      return false
    }
    
    return true
  }
}
```

### 4. EMS/HS 코드 검증
```typescript
// ValidationService.ts
export class ValidationService {
  async validateEMSCode(code: string): Promise<ValidationResult> {
    try {
      const response = await this.emsApiClient.validateCode(code)
      return { valid: response.valid, message: response.message }
    } catch (error) {
      // 폴백 처리
      return { valid: true, warning: 'EMS_VALIDATION_UNAVAILABLE' }
    }
  }
  
  async validateHSCode(code: string): Promise<ValidationResult> {
    // data.go.kr API 호출
    // TODO: API 키 및 엔드포인트 설정 필요
    return { valid: true, warning: 'HS_VALIDATION_TODO' }
  }
}
```

---

## 테스트 계획

### 단위 테스트 (80% 커버리지 목표)
```typescript
// CBMCalculator.test.ts
describe('CBM Calculator', () => {
  test('should calculate CBM correctly', () => {
    const result = calculateCBM(100, 100, 100) // 100cm x 100cm x 100cm
    expect(result).toBe(1.0) // 1 m³
  })
  
  test('should trigger air conversion when CBM > 29', () => {
    const result = calculateCBM(400, 400, 200) // 32 m³
    expect(result).toBeGreaterThan(29)
    expect(shouldConvertToAir(result)).toBe(true)
  })
})

// ValidationService.test.ts  
describe('Validation Service', () => {
  test('should require extra recipient info when THB > 1500', () => {
    const items = [{ amount: 2000, currency: 'THB' }]
    const result = validateOrderAmount(items)
    expect(result.requiresExtraRecipient).toBe(true)
  })
})
```

### 통합 테스트 (E2E 플로우)
```typescript
// order-flow.e2e.ts
describe('Order Flow E2E', () => {
  test('complete order workflow', async () => {
    // 1. 회원가입
    await page.goto('/auth/register')
    await fillRegistrationForm({ role: 'individual' })
    
    // 2. 주문 생성  
    await page.goto('/orders/create')
    await fillOrderForm({
      recipient: { name: 'Test User' },
      items: [{ name: 'Test Item', amount: 2000, currency: 'THB' }],
      boxes: [{ width: 50, height: 50, depth: 50 }]
    })
    
    // 3. 경고 확인
    await expect(page.locator('.warning-modal')).toBeVisible()
    await page.click('button:has-text("계속")')
    
    // 4. 주문 완료
    await page.click('button:has-text("주문하기")')
    await expect(page.locator('.success-message')).toBeVisible()
  })
})
```

### Cypress 모바일 뷰포트 테스트
```typescript
// cypress/e2e/mobile.cy.ts
describe('Mobile Viewport Tests', () => {
  beforeEach(() => {
    cy.viewport('iphone-x')
  })
  
  it('should scan QR code on mobile', () => {
    cy.visit('/warehouse/scan')
    cy.get('[data-cy=camera-permission]').click()
    cy.get('[data-cy=qr-scanner]').should('be.visible')
  })
})
```

---

## 환경 변수 및 시크릿 매핑

### Backend (.env)
```bash
# Database
DB_URL=mysql://user:pass@localhost:3306/lms
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_POOL_SIZE=20

# Cache & Session
REDIS_URL=redis://localhost:6379
REDIS_PASSWORD=${REDIS_PASSWORD}

# Message Queue
KAFKA_BROKERS=localhost:9092
KAFKA_GROUP_ID=lms-backend

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=${SMTP_USER}
SMTP_PASS=${SMTP_PASS}

# OAuth & Security
OAUTH_CLIENT_ID=${OAUTH_CLIENT_ID}
OAUTH_CLIENT_SECRET=${OAUTH_CLIENT_SECRET}
JWT_SECRET=${JWT_SECRET}
ENCRYPTION_KEY=${ENCRYPTION_KEY}

# External APIs
RECAPTCHA_SITE=${RECAPTCHA_SITE}
RECAPTCHA_SECRET=${RECAPTCHA_SECRET}
EMS_API_KEY=${EMS_API_KEY}
HS_API_KEY=${HS_API_KEY}
NAVER_RATE_API_KEY=${NAVER_RATE_API_KEY}

# Monitoring
PROMETHEUS_ENABLED=true
GRAFANA_URL=http://localhost:3000
```

### Frontend (.env)
```bash
# API
VITE_API_BASE=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws

# External Services  
VITE_RECAPTCHA_SITE=${RECAPTCHA_SITE}

# Feature Flags
VITE_ENABLE_2FA=true
VITE_ENABLE_PWA=true

# Development
VITE_MOCK_API=false
VITE_DEBUG_MODE=false
```

### 외부 API 실패시 폴백 처리
```typescript
// ApiClient.ts
export class ApiClient {
  async callExternalAPI<T>(endpoint: string, options: RequestOptions): Promise<T> {
    try {
      const response = await fetch(endpoint, options)
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      return response.json()
    } catch (error) {
      // 폴백 처리
      console.warn(`External API failed: ${endpoint}`, error)
      return this.getFallbackResponse(endpoint)
    }
  }
  
  private getFallbackResponse(endpoint: string): any {
    const fallbacks = {
      '/ems/validate': { valid: true, warning: 'EMS validation temporarily unavailable' },
      '/hs/lookup': { valid: true, warning: 'HS code lookup temporarily unavailable' },
      '/exchange-rate': { rate: 1, warning: 'Using cached exchange rate' }
    }
    
    return fallbacks[endpoint] || { error: 'Service temporarily unavailable' }
  }
}
```

---

## 모듈별 완료 정의 (Definition of Done)

### Sprint 1 완료 기준
1. **DB 스키마**: 모든 테이블 생성 완료, 인덱스 최적화, 테스트 데이터 시딩
2. **인증 시스템**: 로그인/회원가입/2FA 동작, 역할별 가입 승인 워크플로우
3. **주문 생성**: CBM 계산, 비즈니스 룰 검증, EMS/HS 검증 모두 동작
4. **창고 기본**: QR 스캔으로 입고 처리 완료
5. **프론트엔드**: 모바일 최적화된 기본 UI, 상태 관리 구축

### Sprint 2 완료 기준
1. **라벨/QR**: QR 생성/스캔/출력 PWA에서 실제 동작
2. **일괄 처리**: 다중 선택으로 일괄 입고/출고 처리
3. **견적 시스템**: 1차/2차 견적 관리, 비용 계산
4. **파트너 기능**: 파트너 대시보드, 기본 정산 기능
5. **E2E 테스트**: 전체 워크플로우가 실제 모바일 기기에서 동작

### 전체 완료 기준
- [ ] 모든 비즈니스 룰이 자동으로 동작 (CBM 29, THB 1500, member_code)
- [ ] 모바일 기기에서 PWA 카메라 접근 및 QR 스캔 동작
- [ ] 다국어 지원 (한국어/영어) 완료
- [ ] 단위 테스트 80% 커버리지 달성
- [ ] E2E 테스트 시나리오 모두 통과
- [ ] 외부 API 실패시 폴백 동작 확인
- [ ] 접근성 기준 (WCAG 2.1 AA) 준수
- [ ] 성능: 첫 페이지 로드 3초 이내, LCP 2.5초 이내

---

## 제약 조건 및 주의사항

1. **DB 스키마 준수**: claude.md의 테이블/필드명을 정확히 따를 것
2. **API 임의 생성 금지**: 불명확한 데이터는 TODO 표시 후 폴백 처리
3. **보안 준수**: 비밀번호 bcrypt, 통신 HTTPS, 민감 정보 마스킹
4. **모바일 우선**: 모든 UI는 모바일에서 먼저 테스트
5. **성능 최적화**: 페이징, 인덱스, 캐싱 적절히 적용
6. **오류 처리**: 사용자 친화적 메시지, 다국어 지원
7. **테스트 커버리지**: 비즈니스 룰 관련 로직은 반드시 단위 테스트

---

## 다음 단계

이 계획서를 바탕으로 다음 순서로 진행:
1. **docs/API.md** - 상세한 API 명세서 작성
2. **docs/DB_SCHEMA.sql** - 완전한 데이터베이스 스키마
3. **backend/** - Spring Boot 백엔드 스켈레톤 코드
4. **frontend/** - Vue3 프론트엔드 스켈레톤 코드

각 단계별로 스프린트 계획에 따라 점진적으로 기능을 구현하여 2스프린트 내에 동작하는 MVP를 완성합니다.