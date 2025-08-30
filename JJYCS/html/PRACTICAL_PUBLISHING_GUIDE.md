# YSC 물류 시스템 - 실용적 퍼블리싱 가이드

> 실제 TSX 컴포넌트 패턴을 기반으로 한 유연한 HTML/CSS 구현 가이드

## 🎯 핵심 원칙

### 1. 실제 구현 패턴 기반
- **TSX 원본**: `class-variance-authority`로 variant 기반 컴포넌트 설계
- **HTML 적용**: CSS 클래스 조합으로 동일한 variant 시스템 구현
- **유연성 우선**: 경직된 디자인 토큰보다 실용적 활용 중심

### 2. 컴포넌트 중심 설계
```typescript
// TSX 원본 패턴
const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium transition-all",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-white hover:bg-destructive/90",
        outline: "border bg-background text-foreground hover:bg-accent hover:text-accent-foreground",
        secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline"
      },
      size: {
        default: "h-9 px-4 py-2",
        sm: "h-8 rounded-md gap-1.5 px-3",
        lg: "h-10 rounded-md px-6",
        icon: "size-9 rounded-md"
      }
    }
  }
);
```

```html
<!-- HTML 구현 -->
<button class="btn btn-primary btn-default">기본 버튼</button>
<button class="btn btn-destructive btn-sm">작은 위험 버튼</button>
<button class="btn btn-outline btn-lg">큰 아웃라인 버튼</button>
```

## 📱 사용자 타입별 브랜딩

### User Type System (실제 구현)
```typescript
// types/index.ts 기반
export type UserType = 'general' | 'corporate' | 'partner' | 'admin';

// MobileNavigation.tsx 기반 Badge 시스템
{user?.type === 'corporate' && (
  <Badge variant="outline" className="ml-2 border-orange-300 text-orange-700">
    기업
  </Badge>
)}
{user?.type === 'general' && (
  <Badge variant="outline" className="ml-2 border-blue-300 text-blue-700">
    개인
  </Badge>
)}
```

### CSS 구현
```css
/* 사용자 타입별 테마 변수 */
.user-general {
  --user-primary: #3b82f6;
  --user-primary-light: #dbeafe;
  --user-border: #93c5fd;
}

.user-corporate {
  --user-primary: #f97316;
  --user-primary-light: #fed7aa;
  --user-border: #fdba74;
}

.user-partner {
  --user-primary: #8b5cf6;
  --user-primary-light: #e9d5ff;
  --user-border: #c4b5fd;
}

.user-admin {
  --user-primary: #ef4444;
  --user-primary-light: #fecaca;
  --user-border: #fca5a5;
}
```

## 🎨 컴포넌트 시스템

### 1. Card 컴포넌트 (Card.tsx 기반)

**TSX 구조 분석:**
```typescript
// 실제 Card 컴포넌트 구조
<Card>
  <CardHeader>
    <CardTitle>제목</CardTitle>
    <CardDescription>설명</CardDescription>
    <CardAction>액션</CardAction>
  </CardHeader>
  <CardContent>내용</CardContent>
  <CardFooter>푸터</CardFooter>
</Card>
```

**HTML 구현:**
```html
<div class="card">
  <div class="card-header">
    <h4 class="card-title">주문 현황</h4>
    <p class="card-description">최근 주문 내역을 확인하세요</p>
    <div class="card-action">
      <button class="btn btn-ghost btn-icon">⋯</button>
    </div>
  </div>
  <div class="card-content">
    <!-- 카드 내용 -->
  </div>
  <div class="card-footer">
    <button class="btn btn-primary">자세히 보기</button>
  </div>
</div>
```

**CSS 구현:**
```css
.card {
  @apply bg-card text-card-foreground flex flex-col gap-6 rounded-xl border;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.9);
}

.card-header {
  @apply grid auto-rows-min grid-rows-[auto_auto] items-start gap-1.5 px-6 pt-6;
  grid-template-columns: 1fr auto;
}

.card-title {
  @apply leading-none font-semibold text-lg;
}

.card-description {
  @apply text-muted-foreground text-sm;
}

.card-action {
  @apply col-start-2 row-span-2 row-start-1 self-start justify-self-end;
}

.card-content {
  @apply px-6;
}

.card-footer {
  @apply flex items-center px-6 pb-6 gap-3;
}
```

### 2. Badge 컴포넌트 (Badge.tsx 기반)

**CSS 구현:**
```css
.badge {
  @apply inline-flex items-center justify-center rounded-md border px-2 py-0.5 text-xs font-medium w-fit whitespace-nowrap shrink-0 gap-1 transition-[color,box-shadow];
}

.badge-default {
  @apply border-transparent bg-primary text-primary-foreground;
}

.badge-outline {
  @apply text-foreground border-current;
}

.badge-destructive {
  @apply border-transparent bg-destructive text-white;
}

/* 사용자 타입별 뱃지 */
.badge-user-general {
  @apply border-blue-300 text-blue-700 bg-blue-50;
}

.badge-user-corporate {
  @apply border-orange-300 text-orange-700 bg-orange-50;
}

.badge-user-partner {
  @apply border-purple-300 text-purple-700 bg-purple-50;
}
```

### 3. 네비게이션 (MobileNavigation.tsx 기반)

**실제 구현 패턴:**
```typescript
// MobileNavigation.tsx의 실제 네비게이션 아이템
const navigationItems = [
  { id: 'dashboard', label: '홈', icon: <Home className="h-5 w-5" /> },
  { id: 'order-form', label: '접수', icon: <Package className="h-5 w-5" /> },
  { id: 'order-history', label: '내역', icon: <FileText className="h-5 w-5" /> },
  { id: 'mypage', label: '내정보', icon: <UserIcon className="h-5 w-5" /> },
  { id: 'page-list', label: '전체', icon: <List className="h-5 w-5" /> }
];
```

**HTML 구현:**
```html
<!-- 상단 헤더 (실제 구조 기반) -->
<header class="mobile-header">
  <div class="flex items-center justify-between px-4 py-3">
    <div class="flex items-center gap-3">
      <div class="ycs-logo">
        <span class="text-white font-bold">YSC</span>
      </div>
      <div>
        <h1 class="font-semibold text-blue-900">
          YSC 물류 시스템
          <span class="badge badge-user-corporate ml-2">기업</span>
        </h1>
        <p class="text-xs text-blue-600">기업 고객 대시보드</p>
      </div>
    </div>
    <div class="flex gap-2">
      <button class="nav-action-btn nav-action-purple">페이지목록</button>
      <button class="nav-action-btn nav-action-red">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
        </svg>
      </button>
    </div>
  </div>
</header>

<!-- 하단 네비게이션 (실제 구조 기반) -->
<nav class="mobile-nav">
  <div class="grid grid-cols-5 h-16">
    <button class="nav-item nav-item-active" data-page="dashboard">
      <div class="nav-icon">🏠</div>
      <span class="nav-label">홈</span>
      <div class="nav-indicator"></div>
    </button>
    <button class="nav-item" data-page="order-form">
      <div class="nav-icon">📦</div>
      <span class="nav-label">접수</span>
    </button>
    <button class="nav-item" data-page="order-history">
      <div class="nav-icon">📄</div>
      <span class="nav-label">내역</span>
    </button>
    <button class="nav-item" data-page="mypage">
      <div class="nav-icon">👤</div>
      <span class="nav-label">내정보</span>
    </button>
    <button class="nav-item" data-page="page-list">
      <div class="nav-icon">📋</div>
      <span class="nav-label">전체</span>
    </button>
  </div>
</nav>
```

**CSS 구현:**
```css
.mobile-header {
  @apply bg-white border-b border-blue-100 shadow-sm sticky top-0 z-40;
}

.ycs-logo {
  @apply w-10 h-10 bg-gradient-to-r from-blue-500 to-blue-600 rounded-full flex items-center justify-center shadow-lg;
}

.nav-action-btn {
  @apply text-sm px-3 py-1 rounded-full transition-colors;
}

.nav-action-purple {
  @apply text-purple-600 hover:text-purple-700 hover:bg-purple-50;
}

.nav-action-red {
  @apply text-red-600 hover:text-red-700 hover:bg-red-50;
}

.mobile-nav {
  @apply fixed bottom-0 left-0 right-0 bg-white border-t border-blue-100 shadow-lg z-50;
}

.nav-item {
  @apply flex flex-col items-center justify-center gap-1 transition-all duration-300 relative;
  @apply text-blue-400 hover:text-blue-600 hover:bg-blue-50;
}

.nav-item-active {
  @apply text-blue-600 bg-blue-50;
}

.nav-icon {
  @apply transition-transform duration-300;
  font-size: 1.25rem;
}

.nav-item-active .nav-icon {
  @apply scale-110;
}

.nav-label {
  @apply text-xs transition-all duration-300;
}

.nav-item-active .nav-label {
  @apply font-medium;
}

.nav-indicator {
  @apply absolute top-0 left-1/2 transform -translate-x-1/2 w-8 h-1 bg-blue-600 rounded-full;
}
```

## 📋 실제 데이터 기반 폼 구현

### 주문 폼 (OrderForm.tsx 기반)

**실제 타입 정의:**
```typescript
// types/index.ts 기반 실제 데이터 구조
export interface OrderItem {
  id: string;
  name: string;
  quantity: number;
  declaredValue: number;
  currency: 'THB' | 'USD' | 'KRW';
  category: string;
  hsCode?: string;
  weight: number; // 그램 단위
  dimensions?: {
    width: number;  // cm
    height: number; // cm
    depth: number;  // cm
    cbm?: number;   // 자동계산됨
  };
  shippingType: 'air' | 'sea';
}
```

**HTML 구현:**
```html
<form class="order-form">
  <div class="form-section">
    <h3 class="form-section-title">배송 품목 정보</h3>
    
    <div class="form-group">
      <label class="form-label required">품목명</label>
      <input type="text" class="form-input" placeholder="배송할 품목명을 입력하세요" required>
    </div>

    <div class="form-row">
      <div class="form-group">
        <label class="form-label required">수량</label>
        <input type="number" class="form-input" min="1" value="1" required>
      </div>
      <div class="form-group">
        <label class="form-label required">무게(g)</label>
        <input type="number" class="form-input" min="1" placeholder="그램 단위" required>
      </div>
    </div>

    <div class="form-group">
      <label class="form-label required">신고가격</label>
      <div class="form-input-group">
        <input type="number" class="form-input" min="0" step="0.01" required>
        <select class="form-select">
          <option value="THB">THB</option>
          <option value="USD">USD</option>
          <option value="KRW">KRW</option>
        </select>
      </div>
      <div class="form-hint">
        THB 1,500 초과 시 수취인 추가 정보가 필요합니다
      </div>
    </div>

    <div class="form-section cbm-section">
      <h4 class="form-subsection-title">박스 치수 (CBM 자동계산)</h4>
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">가로(cm)</label>
          <input type="number" class="form-input cbm-input" data-dimension="width" min="0" step="0.1">
        </div>
        <div class="form-group">
          <label class="form-label">세로(cm)</label>
          <input type="number" class="form-input cbm-input" data-dimension="height" min="0" step="0.1">
        </div>
        <div class="form-group">
          <label class="form-label">높이(cm)</label>
          <input type="number" class="form-input cbm-input" data-dimension="depth" min="0" step="0.1">
        </div>
      </div>
      
      <div class="cbm-result">
        <div class="cbm-display">
          <span class="cbm-label">CBM:</span>
          <span class="cbm-value" id="cbmValue">0.000</span>
          <span class="cbm-unit">m³</span>
        </div>
        <div class="shipping-type-auto">
          <span class="badge badge-default" id="shippingTypeBadge">항공</span>
          <span class="shipping-note" id="shippingNote">CBM 29 초과 시 자동으로 항공 배송으로 전환됩니다</span>
        </div>
      </div>
    </div>
  </div>

  <div class="form-actions">
    <button type="button" class="btn btn-outline">임시저장</button>
    <button type="submit" class="btn btn-primary">주문 접수</button>
  </div>
</form>
```

**CSS 구현:**
```css
.order-form {
  @apply space-y-6 p-6 max-w-2xl mx-auto;
}

.form-section {
  @apply space-y-4 p-4 bg-white rounded-lg border;
}

.form-section-title {
  @apply text-lg font-semibold text-gray-900 border-b pb-2 mb-4;
}

.form-subsection-title {
  @apply text-md font-medium text-gray-700 mb-3;
}

.form-group {
  @apply space-y-2;
}

.form-row {
  @apply grid grid-cols-2 gap-4;
}

.form-label {
  @apply block text-sm font-medium text-gray-700;
}

.form-label.required::after {
  content: ' *';
  @apply text-red-500;
}

.form-input {
  @apply w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm;
  @apply focus:border-blue-500 focus:ring-blue-500 focus:ring-1;
  @apply transition-colors;
}

.form-input-group {
  @apply flex rounded-md shadow-sm;
}

.form-input-group .form-input {
  @apply rounded-r-none border-r-0;
}

.form-select {
  @apply px-3 py-2 border border-gray-300 rounded-r-md;
  @apply focus:border-blue-500 focus:ring-blue-500 focus:ring-1;
  @apply bg-white text-sm;
  min-width: 80px;
}

.form-hint {
  @apply text-xs text-amber-600 bg-amber-50 px-2 py-1 rounded;
}

.cbm-section {
  @apply bg-blue-50 border-blue-200;
}

.cbm-result {
  @apply mt-4 p-3 bg-white rounded border space-y-2;
}

.cbm-display {
  @apply flex items-center gap-2 text-lg;
}

.cbm-label {
  @apply font-medium text-gray-700;
}

.cbm-value {
  @apply font-bold text-blue-600;
}

.cbm-unit {
  @apply text-gray-500;
}

.shipping-type-auto {
  @apply flex items-center gap-2;
}

.shipping-note {
  @apply text-xs text-gray-600;
}

.form-actions {
  @apply flex gap-3 pt-4 border-t;
}

.form-actions button {
  @apply flex-1;
}
```

**JavaScript 구현 (CBM 자동계산):**
```javascript
// CBM 자동계산 및 배송방식 전환
document.addEventListener('DOMContentLoaded', function() {
  const cbmInputs = document.querySelectorAll('.cbm-input');
  const cbmValue = document.getElementById('cbmValue');
  const shippingTypeBadge = document.getElementById('shippingTypeBadge');
  const shippingNote = document.getElementById('shippingNote');

  function calculateCBM() {
    const width = parseFloat(document.querySelector('[data-dimension="width"]').value) || 0;
    const height = parseFloat(document.querySelector('[data-dimension="height"]').value) || 0;
    const depth = parseFloat(document.querySelector('[data-dimension="depth"]').value) || 0;
    
    const cbm = (width * height * depth) / 1000000; // cm³ → m³
    cbmValue.textContent = cbm.toFixed(3);
    
    // CBM 29 초과 시 항공 배송 전환
    if (cbm > 29) {
      shippingTypeBadge.textContent = '항공';
      shippingTypeBadge.className = 'badge badge-destructive';
      shippingNote.textContent = 'CBM이 29를 초과하여 항공 배송으로 자동 전환되었습니다';
      shippingNote.className = 'text-xs text-red-600 font-medium';
    } else {
      shippingTypeBadge.textContent = '해상';
      shippingTypeBadge.className = 'badge badge-default';
      shippingNote.textContent = 'CBM 29 초과 시 자동으로 항공 배송으로 전환됩니다';
      shippingNote.className = 'text-xs text-gray-600';
    }
  }

  cbmInputs.forEach(input => {
    input.addEventListener('input', calculateCBM);
  });
});
```

## 📊 대시보드 구현 (Dashboard.tsx 기반)

**실제 TSX 패턴 기반 대시보드:**
```html
<div class="dashboard">
  <!-- 사용자별 환영 메시지 -->
  <div class="dashboard-header">
    <h1 class="dashboard-title">안녕하세요, <span class="user-name">김기업</span>님</h1>
    <p class="dashboard-subtitle">
      <span class="badge badge-user-corporate">기업 고객</span>
      오늘도 YSC와 함께 성공적인 배송을 시작하세요!
    </p>
  </div>

  <!-- 통계 카드 (실제 데이터 기반) -->
  <div class="stats-grid">
    <div class="stat-card stat-primary">
      <div class="stat-icon">📦</div>
      <div class="stat-content">
        <div class="stat-value">12</div>
        <div class="stat-label">진행중인 주문</div>
      </div>
    </div>
    
    <div class="stat-card stat-success">
      <div class="stat-icon">✅</div>
      <div class="stat-content">
        <div class="stat-value">45</div>
        <div class="stat-label">완료된 배송</div>
      </div>
    </div>
    
    <div class="stat-card stat-warning">
      <div class="stat-icon">⏳</div>
      <div class="stat-content">
        <div class="stat-value">3</div>
        <div class="stat-label">승인 대기</div>
      </div>
    </div>
    
    <div class="stat-card stat-info">
      <div class="stat-icon">💰</div>
      <div class="stat-content">
        <div class="stat-value">₩2,450,000</div>
        <div class="stat-label">이번 달 금액</div>
      </div>
    </div>
  </div>

  <!-- 최근 주문 목록 -->
  <div class="card">
    <div class="card-header">
      <h3 class="card-title">최근 주문</h3>
      <div class="card-action">
        <button class="btn btn-outline btn-sm">전체보기</button>
      </div>
    </div>
    <div class="card-content">
      <div class="order-list">
        <div class="order-item">
          <div class="order-info">
            <div class="order-number">#YSC240822001</div>
            <div class="order-description">전자제품 5개 항목</div>
          </div>
          <div class="order-status">
            <span class="badge badge-warning">배송중</span>
          </div>
        </div>
        
        <div class="order-item">
          <div class="order-info">
            <div class="order-number">#YSC240821003</div>
            <div class="order-description">의류 3개 항목</div>
          </div>
          <div class="order-status">
            <span class="badge badge-success">배송완료</span>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 빠른 액션 -->
  <div class="quick-actions">
    <button class="action-btn action-primary">
      <div class="action-icon">📝</div>
      <div class="action-text">새 주문 접수</div>
    </button>
    
    <button class="action-btn action-secondary">
      <div class="action-icon">📋</div>
      <div class="action-text">주문 내역</div>
    </button>
    
    <button class="action-btn action-tertiary">
      <div class="action-icon">💳</div>
      <div class="action-text">결제 관리</div>
    </button>
  </div>
</div>
```

## 🎛️ 실용적 구현 팁

### 1. 상태 관리 패턴
```javascript
// 실제 TSX의 상태 관리 패턴을 모방
class StateManager {
  constructor() {
    this.state = {
      user: null,
      currentPage: 'dashboard',
      loading: false,
      orders: []
    };
    this.listeners = [];
  }

  setState(newState) {
    this.state = { ...this.state, ...newState };
    this.notify();
  }

  subscribe(listener) {
    this.listeners.push(listener);
  }

  notify() {
    this.listeners.forEach(listener => listener(this.state));
  }
}

const appState = new StateManager();
```

### 2. 컴포넌트 시스템 활용
```javascript
// 재사용 가능한 컴포넌트 생성 함수
function createCard(title, content, actions = []) {
  return `
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">${title}</h3>
        ${actions.length > 0 ? `<div class="card-action">${actions.join('')}</div>` : ''}
      </div>
      <div class="card-content">${content}</div>
    </div>
  `;
}

function createBadge(text, variant = 'default') {
  return `<span class="badge badge-${variant}">${text}</span>`;
}

function createButton(text, variant = 'default', size = 'default') {
  return `<button class="btn btn-${variant} btn-${size}">${text}</button>`;
}
```

### 3. 반응형 및 접근성
```css
/* 실제 사용된 반응형 패턴 */
.stats-grid {
  @apply grid gap-4;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
}

/* 접근성 향상 */
.btn:focus {
  @apply ring-2 ring-offset-2 ring-blue-500;
}

.form-input:focus {
  @apply ring-2 ring-blue-500 border-blue-500;
}

/* 키보드 네비게이션 */
.nav-item:focus {
  @apply bg-blue-100 outline-none ring-2 ring-blue-500;
}
```

## 🚀 실제 사용 예제

### 페이지 구성 예제
```html
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>YSC 물류 시스템 - 기업 대시보드</title>
  <link rel="stylesheet" href="css/globals.css">
</head>
<body class="user-corporate">
  <!-- 헤더 컴포넌트 -->
  <header class="mobile-header">...</header>
  
  <!-- 메인 콘텐츠 -->
  <main class="main-content">
    <div class="dashboard">...</div>
  </main>
  
  <!-- 하단 네비게이션 -->
  <nav class="mobile-nav">...</nav>
  
  <!-- JavaScript -->
  <script src="js/components.js"></script>
  <script src="js/app.js"></script>
</body>
</html>
```

## 📝 결론

이 가이드는 실제 TSX 컴포넌트 패턴을 기반으로 작성되어:

1. **실용성**: 이론적 설계가 아닌 실제 구현된 패턴 활용
2. **유연성**: 경직된 디자인 시스템이 아닌 적응 가능한 컴포넌트 시스템
3. **확장성**: variant 기반 시스템으로 새로운 스타일 쉽게 추가
4. **일관성**: TSX 원본과 동일한 구조와 네이밍 패턴 유지

**실제 TSX 파일들과 1:1 대응되는 HTML/CSS 구현**으로 더욱 실용적이고 유연한 퍼블리싱이 가능합니다.