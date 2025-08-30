# YSC 물류 시스템 퍼블리싱 가이드

## 📋 목차
1. [디자인 원칙](#1-디자인-원칙)
2. [색상 시스템](#2-색상-시스템)
3. [타이포그래피](#3-타이포그래피)
4. [컴포넌트 가이드](#4-컴포넌트-가이드)
5. [모바일 최적화](#5-모바일-최적화)
6. [접근성 표준](#6-접근성-표준)
7. [성능 최적화](#7-성능-최적화)
8. [코드 컨벤션](#8-코드-컨벤션)

---

## 1. 디자인 원칙

### 1.1 핵심 가치
- **신뢰성**: 물류 시스템의 정확성과 안정성을 시각적으로 전달
- **효율성**: 빠른 작업 처리를 위한 직관적인 인터페이스
- **일관성**: 모든 페이지에서 동일한 사용자 경험 제공
- **접근성**: 다양한 환경과 사용자를 고려한 포용적 디자인

### 1.2 디자인 철학
```
"Simple, Clear, Professional"
- 불필요한 장식 제거
- 명확한 정보 계층 구조
- 전문적이고 신뢰할 수 있는 외관
```

---

## 2. 색상 시스템

### 2.1 주요 색상 팔레트

```css
/* Primary Colors - 브랜드 아이덴티티 */
--primary-600: #2563eb;  /* 메인 브랜드 색상 */
--primary-500: #3b82f6;  /* 호버 상태 */
--primary-400: #60a5fa;  /* 액티브 상태 */
--primary-100: #dbeafe;  /* 배경색 */
--primary-50:  #eff6ff;  /* 연한 배경 */

/* Semantic Colors - 의미별 색상 */
--success-600: #16a34a;  /* 성공/완료 */
--warning-600: #d97706;  /* 경고/주의 */
--danger-600:  #dc2626;  /* 오류/위험 */
--info-600:    #0891b2;  /* 정보/안내 */

/* Neutral Colors - 중립 색상 */
--gray-900: #111827;     /* 주요 텍스트 */
--gray-700: #374151;     /* 보조 텍스트 */
--gray-500: #6b7280;     /* 비활성 텍스트 */
--gray-300: #d1d5db;     /* 테두리 */
--gray-100: #f3f4f6;     /* 배경 */
--white:    #ffffff;     /* 기본 배경 */
```

### 2.2 색상 사용 규칙

#### 사용자 타입별 색상
- **일반 회원**: Blue (#3b82f6)
- **기업 회원**: Orange (#f97316)
- **파트너**: Indigo (#6366f1)
- **관리자**: Red (#ef4444)

#### 상태별 색상
- **대기중**: Gray (#6b7280)
- **진행중**: Blue (#3b82f6)
- **완료**: Green (#10b981)
- **지연**: Orange (#f59e0b)
- **취소**: Red (#ef4444)

---

## 3. 타이포그래피

### 3.1 폰트 시스템

```css
/* Font Family */
--font-primary: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
--font-mono: 'JetBrains Mono', 'Courier New', monospace;

/* Font Sizes - Mobile First */
--text-xs:   0.75rem;   /* 12px - 보조 정보 */
--text-sm:   0.875rem;  /* 14px - 일반 본문 */
--text-base: 1rem;      /* 16px - 기본 크기 */
--text-lg:   1.125rem;  /* 18px - 부제목 */
--text-xl:   1.25rem;   /* 20px - 제목 */
--text-2xl:  1.5rem;    /* 24px - 페이지 제목 */
--text-3xl:  1.875rem;  /* 30px - 대제목 */

/* Font Weight */
--font-normal:   400;
--font-medium:   500;
--font-semibold: 600;
--font-bold:     700;

/* Line Height */
--leading-tight:   1.25;
--leading-normal:  1.5;
--leading-relaxed: 1.75;
```

### 3.2 텍스트 계층 구조

```html
<!-- 페이지 제목 -->
<h1 class="text-2xl font-bold text-gray-900">주문 관리</h1>

<!-- 섹션 제목 -->
<h2 class="text-xl font-semibold text-gray-900">배송 정보</h2>

<!-- 카드 제목 -->
<h3 class="text-lg font-medium text-gray-900">수취인 정보</h3>

<!-- 본문 텍스트 -->
<p class="text-sm text-gray-700">배송 주소를 입력해주세요.</p>

<!-- 보조 텍스트 -->
<span class="text-xs text-gray-500">필수 입력 항목</span>
```

---

## 4. 컴포넌트 가이드

### 4.1 버튼 (Buttons)

```html
<!-- Primary Button -->
<button class="btn-primary">
  주문 접수
</button>

<!-- Secondary Button -->
<button class="btn-secondary">
  취소
</button>

<!-- Danger Button -->
<button class="btn-danger">
  삭제
</button>

<!-- Icon Button -->
<button class="btn-icon">
  <i data-lucide="plus" class="w-4 h-4"></i>
  추가
</button>
```

**CSS 정의:**
```css
.btn-primary {
  background: linear-gradient(to right, #3b82f6, #2563eb);
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.btn-primary:active {
  transform: translateY(0);
}

/* 모바일 터치 영역 최적화 */
@media (max-width: 768px) {
  .btn-primary {
    min-height: 48px;
    width: 100%;
  }
}
```

### 4.2 입력 필드 (Input Fields)

```html
<!-- Text Input -->
<div class="form-group">
  <label class="form-label">
    수취인 이름
    <span class="text-red-500">*</span>
  </label>
  <input type="text" class="form-input" placeholder="이름을 입력하세요">
  <span class="form-helper">한글 또는 영문으로 입력</span>
</div>

<!-- Select -->
<div class="form-group">
  <label class="form-label">배송 방법</label>
  <select class="form-select">
    <option>항공 배송</option>
    <option>해상 배송</option>
  </select>
</div>
```

**CSS 정의:**
```css
.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px; /* iOS 줌 방지 */
  transition: all 0.2s;
}

.form-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  outline: none;
}

/* 에러 상태 */
.form-input.error {
  border-color: #ef4444;
  background-color: #fef2f2;
}
```

### 4.3 카드 (Cards)

```html
<!-- Order Card -->
<div class="card">
  <div class="card-header">
    <h3 class="card-title">주문번호: YSC-2024-001</h3>
    <span class="status-badge status-progress">진행중</span>
  </div>
  <div class="card-body">
    <div class="info-row">
      <span class="info-label">수취인:</span>
      <span class="info-value">김철수</span>
    </div>
    <div class="info-row">
      <span class="info-label">배송지:</span>
      <span class="info-value">서울시 강남구</span>
    </div>
  </div>
  <div class="card-footer">
    <button class="btn-sm btn-primary">상세보기</button>
  </div>
</div>
```

### 4.4 상태 배지 (Status Badges)

```html
<!-- Status Badges -->
<span class="badge badge-waiting">대기중</span>
<span class="badge badge-progress">진행중</span>
<span class="badge badge-complete">완료</span>
<span class="badge badge-delay">지연</span>
<span class="badge badge-cancel">취소</span>
```

---

## 5. 모바일 최적화

### 5.1 반응형 브레이크포인트

```css
/* Breakpoints */
--mobile:  320px;   /* 최소 지원 너비 */
--tablet:  768px;   /* 태블릿 */
--desktop: 1024px;  /* 데스크톱 */
--wide:    1280px;  /* 와이드 스크린 */
```

### 5.2 모바일 우선 설계 원칙

#### 터치 타겟 최적화
```css
/* 최소 터치 영역: 48x48px */
.touch-target {
  min-height: 48px;
  min-width: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 터치 영역 간격 */
.touch-spacing {
  margin: 8px 0;
}
```

#### 스크롤 최적화
```css
/* 모멘텀 스크롤 활성화 */
.scroll-container {
  -webkit-overflow-scrolling: touch;
  overflow-y: auto;
}

/* 수평 스크롤 테이블 */
.table-container {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.table {
  min-width: 600px; /* 최소 너비 보장 */
}
```

#### 입력 최적화
```css
/* iOS 확대 방지 */
input, select, textarea {
  font-size: 16px;
}

/* 자동완성 스타일 */
input:-webkit-autofill {
  -webkit-box-shadow: 0 0 0 30px white inset;
}
```

### 5.3 제스처 지원

```javascript
// Swipe 감지
let touchStartX = 0;
let touchEndX = 0;

element.addEventListener('touchstart', (e) => {
  touchStartX = e.changedTouches[0].screenX;
});

element.addEventListener('touchend', (e) => {
  touchEndX = e.changedTouches[0].screenX;
  handleSwipe();
});

function handleSwipe() {
  if (touchEndX < touchStartX - 50) {
    // 왼쪽 스와이프
    nextPage();
  }
  if (touchEndX > touchStartX + 50) {
    // 오른쪽 스와이프
    prevPage();
  }
}
```

### 5.4 PWA 최적화

```html
<!-- Manifest -->
<link rel="manifest" href="/manifest.json">

<!-- iOS 지원 -->
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="default">
<link rel="apple-touch-icon" href="/icon-192.png">

<!-- 스플래시 스크린 -->
<link rel="apple-touch-startup-image" href="/splash.png">
```

---

## 6. 접근성 표준

### 6.1 WCAG 2.1 AA 준수

#### 색상 대비
- 일반 텍스트: 4.5:1 이상
- 큰 텍스트(18px+): 3:1 이상
- 비활성 요소: 예외

#### 키보드 네비게이션
```css
/* Focus 스타일 */
:focus-visible {
  outline: 2px solid #3b82f6;
  outline-offset: 2px;
}

/* Skip to content */
.skip-link {
  position: absolute;
  top: -40px;
  left: 0;
  background: #3b82f6;
  color: white;
  padding: 8px;
  text-decoration: none;
}

.skip-link:focus {
  top: 0;
}
```

#### ARIA 레이블
```html
<!-- 버튼 -->
<button aria-label="주문 삭제" aria-pressed="false">
  <i data-lucide="trash" aria-hidden="true"></i>
</button>

<!-- 로딩 -->
<div role="status" aria-live="polite">
  <span class="sr-only">로딩 중...</span>
</div>

<!-- 폼 -->
<label for="email">이메일</label>
<input id="email" type="email" aria-required="true" aria-invalid="false">
```

### 6.2 스크린 리더 지원

```css
/* 스크린 리더 전용 텍스트 */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
```

---

## 7. 성능 최적화

### 7.1 CSS 최적화

```css
/* Critical CSS - 인라인 처리 */
.above-the-fold {
  /* 초기 렌더링에 필요한 스타일만 */
}

/* Lazy Load CSS */
<link rel="preload" href="styles.css" as="style" onload="this.onload=null;this.rel='stylesheet'">
```

### 7.2 이미지 최적화

```html
<!-- Responsive Images -->
<picture>
  <source media="(max-width: 768px)" srcset="image-mobile.webp">
  <source media="(min-width: 769px)" srcset="image-desktop.webp">
  <img src="image.jpg" alt="설명" loading="lazy">
</picture>

<!-- Lazy Loading -->
<img src="placeholder.jpg" data-src="actual-image.jpg" loading="lazy" alt="설명">
```

### 7.3 JavaScript 최적화

```javascript
// Debounce
function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

// Throttle
function throttle(func, limit) {
  let inThrottle;
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  }
}
```

---

## 8. 코드 컨벤션

### 8.1 HTML 구조

```html
<!-- BEM 명명법 -->
<div class="order-card">
  <div class="order-card__header">
    <h3 class="order-card__title">주문 정보</h3>
    <span class="order-card__status order-card__status--active">활성</span>
  </div>
  <div class="order-card__body">
    <!-- 내용 -->
  </div>
</div>
```

### 8.2 CSS 구조

```css
/* 컴포넌트별 분리 */
/* components/button.css */
.btn {
  /* 기본 스타일 */
}

.btn--primary {
  /* 변형 스타일 */
}

.btn--large {
  /* 크기 변형 */
}

/* 상태 클래스 */
.is-active {}
.is-disabled {}
.is-loading {}
```

### 8.3 JavaScript 패턴

```javascript
// 모듈 패턴
const OrderManager = (function() {
  // Private
  let orders = [];
  
  function validateOrder(order) {
    // 검증 로직
  }
  
  // Public
  return {
    addOrder(order) {
      if (validateOrder(order)) {
        orders.push(order);
      }
    },
    getOrders() {
      return [...orders];
    }
  };
})();

// 이벤트 위임
document.addEventListener('click', (e) => {
  if (e.target.matches('.btn-delete')) {
    handleDelete(e.target.dataset.id);
  }
});
```

---

## 9. 테스트 체크리스트

### 9.1 브라우저 호환성
- [ ] Chrome (최신)
- [ ] Safari (iOS 14+)
- [ ] Firefox (최신)
- [ ] Edge (최신)
- [ ] Samsung Internet

### 9.2 디바이스 테스트
- [ ] iPhone SE (375px)
- [ ] iPhone 12/13 (390px)
- [ ] iPad (768px)
- [ ] Desktop (1920px)

### 9.3 성능 지표
- [ ] FCP < 1.8s
- [ ] LCP < 2.5s
- [ ] CLS < 0.1
- [ ] FID < 100ms

### 9.4 접근성 테스트
- [ ] 키보드만으로 조작 가능
- [ ] 스크린 리더 테스트 (NVDA/JAWS)
- [ ] 색상 대비 검증
- [ ] 확대/축소 200% 지원

---

## 10. 주요 참고 사항

### 10.1 물류 시스템 특화 요구사항

#### CBM 계산 UI
```html
<div class="cbm-calculator">
  <div class="cbm-inputs">
    <input type="number" id="width" placeholder="가로(cm)">
    <input type="number" id="height" placeholder="세로(cm)">
    <input type="number" id="depth" placeholder="높이(cm)">
  </div>
  <div class="cbm-result">
    <span class="cbm-value">0.00</span> m³
    <span class="cbm-warning" hidden>29m³ 초과 - 항공배송 전환</span>
  </div>
</div>
```

#### 바코드/QR 스캔
```javascript
// 카메라 권한 요청
async function requestCamera() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ 
      video: { facingMode: 'environment' } 
    });
    // QR 스캔 로직
  } catch (error) {
    // 폴백: 수동 입력
    showManualInput();
  }
}
```

#### 실시간 상태 업데이트
```javascript
// WebSocket 연결
const ws = new WebSocket('wss://api.ycs-logistics.com/tracking');

ws.onmessage = (event) => {
  const update = JSON.parse(event.data);
  updateOrderStatus(update.orderId, update.status);
  showNotification(`주문 ${update.orderId} 상태 변경: ${update.status}`);
};
```

### 10.2 다국어 지원

```javascript
// i18n 구조
const translations = {
  ko: {
    order: '주문',
    shipping: '배송',
    complete: '완료'
  },
  en: {
    order: 'Order',
    shipping: 'Shipping',
    complete: 'Complete'
  }
};

// 사용
function t(key) {
  const lang = localStorage.getItem('language') || 'ko';
  return translations[lang][key] || key;
}
```

---

## 📚 추가 리소스

- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Lucide Icons](https://lucide.dev/icons)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Google Material Design](https://material.io/design)
- [Apple Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)

---

**작성일**: 2024.12.27  
**버전**: 1.0.0  
**담당**: YSC 물류 시스템 개발팀