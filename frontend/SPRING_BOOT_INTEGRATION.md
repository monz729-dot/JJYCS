# Spring Boot 백엔드 통합 가이드

## 🎯 개요

YCS LMS 프론트엔드를 Supabase에서 Spring Boot 백엔드로 전환하기 위한 가이드입니다.

## 🔄 변경 사항

### 새로운 API 서비스들

1. **authApiService.ts** - Spring Boot 인증 API
2. **ordersApiService.ts** - 주문 관리 API  
3. **warehouseApiService.ts** - 창고 관리 API
4. **estimatesApiService.ts** - 견적 관리 API

### 기존 서비스와의 차이점

| 기능 | Supabase | Spring Boot |
|------|----------|------------|
| 인증 | JWT + 세션 | JWT 토큰 |
| 데이터베이스 | PostgreSQL | H2 (개발) / MySQL (운영) |
| 파일 업로드 | Supabase Storage | Multipart 업로드 |
| 실시간 업데이트 | Realtime | WebSocket (미구현) |

## 🔧 통합 단계

### 1단계: 인증 서비스 교체

기존 `authService.ts` 대신 `authApiService.ts` 사용:

```typescript
// 기존 (Supabase)
import { AuthService } from '@/services/authService'

// 새로운 (Spring Boot)
import SpringBootAuthService from '@/services/authApiService'

// 사용법
const result = await SpringBootAuthService.signUp({
  email: 'user@example.com',
  password: 'password123',
  name: '홍길동',
  role: 'INDIVIDUAL',
  agreeTerms: true,
  agreePrivacy: true
})
```

### 2단계: Stores 업데이트

Pinia 스토어들을 새로운 API 서비스와 연동:

```typescript
// stores/auth.ts 예시
import SpringBootAuthService from '@/services/authApiService'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const isAuthenticated = computed(() => !!user.value)

  const login = async (credentials) => {
    const result = await SpringBootAuthService.signIn(credentials)
    if (result.success) {
      user.value = result.data.user
      return result
    }
    throw new Error(result.error)
  }

  return { user, isAuthenticated, login }
})
```

### 3단계: 컴포넌트 업데이트

Vue 컴포넌트에서 새로운 서비스 사용:

```vue
<script setup lang="ts">
import SpringBootOrdersService from '@/services/ordersApiService'
import { ref, onMounted } from 'vue'

const orders = ref([])

onMounted(async () => {
  const result = await SpringBootOrdersService.getOrders()
  if (result.success) {
    orders.value = result.data.content
  }
})
</script>
```

## 🏗️ 주요 API 엔드포인트

### 인증 API
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인
- `GET /api/auth/verify-email` - 이메일 인증

### 주문 API
- `GET /api/orders` - 주문 목록
- `POST /api/orders` - 주문 생성
- `GET /api/orders/{id}` - 주문 상세
- `PUT /api/orders/{id}/status` - 상태 업데이트

### 창고 API
- `POST /api/warehouse/scan` - QR 스캔
- `GET /api/warehouse/inventory` - 재고 조회
- `POST /api/warehouse/batch-process` - 일괄 처리

### 견적 API
- `POST /api/orders/{orderId}/estimates` - 견적 생성
- `GET /api/orders/{orderId}/estimates` - 견적 목록
- `POST /api/estimates/{id}/respond` - 견적 승인/거부

## 🔐 인증 처리

### JWT 토큰 관리

```typescript
// 자동 토큰 관리 (axios interceptor에서 처리)
// localStorage에 'accessToken' 키로 저장
// 모든 API 요청에 자동으로 Authorization 헤더 추가

// 토큰 만료 시 자동 로그아웃
// 401 응답 시 자동으로 로그인 페이지로 리다이렉트
```

### 역할 기반 접근 제어

```typescript
// 사용자 역할 확인
if (SpringBootAuthService.hasRole('ADMIN')) {
  // 관리자 기능 표시
}

// 승인 상태 확인
if (!SpringBootAuthService.isApproved()) {
  // 승인 대기 메시지 표시
}

// 이메일 인증 상태 확인
if (!SpringBootAuthService.isEmailVerified()) {
  // 이메일 인증 안내 표시
}
```

## 📱 PWA 및 오프라인 기능

### 현재 상태
- PWA 기본 설정 완료 (vite.config.ts)
- 오프라인 모드는 캐시된 데이터 사용
- 온라인 복구 시 데이터 동기화 필요

### 추가 고려사항
- Service Worker에서 API 요청 캐싱
- 오프라인 스캔 데이터 로컬 저장
- 온라인 복구 시 배치 동기화

## 🚀 배포 설정

### 개발 환경
```bash
# 백엔드 (Spring Boot)
cd backend
./run-backend-dev-real.bat

# 프론트엔드 (Vue.js)
cd frontend
npm run dev
```

### 프록시 설정 확인
```typescript
// vite.config.ts - 이미 설정됨
server: {
  port: 5175,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## 🧪 테스트

### 백엔드 테스트 데이터
- H2 Console: http://localhost:8080/api/h2-console
- 테스트 사용자들이 자동으로 생성됨
- 비밀번호: `password123`

### 프론트엔드 테스트
```bash
# 개발 서버 실행
npm run dev

# 빌드 테스트
npm run build

# 프리뷰
npm run preview
```

## 📋 TODO

### 우선순위 높음
1. ✅ Auth Store를 Spring Boot API로 전환
2. ✅ Orders Store를 Spring Boot API로 전환  
3. ✅ Warehouse Store를 Spring Boot API로 전환
4. ⏳ 기존 Supabase 코드 제거/주석 처리

### 우선순위 중간
1. ⏳ 파일 업로드 로직 구현 (리패킹 사진)
2. ⏳ WebSocket 실시간 업데이트 (선택사항)
3. ⏳ 오프라인 모드 개선

### 우선순위 낮음
1. ⏳ 단위 테스트 추가
2. ⏳ E2E 테스트 추가
3. ⏳ 성능 최적화

## 🔍 디버깅 팁

### API 요청 확인
1. 브라우저 개발자 도구 → Network 탭
2. 요청 URL이 `/api/...`로 시작하는지 확인
3. Authorization 헤더에 JWT 토큰이 있는지 확인
4. 응답 상태 코드 및 에러 메시지 확인

### 백엔드 로그 확인
1. 백엔드 콘솔에서 SQL 쿼리 로그 확인
2. 비즈니스 룰 검증 로그 확인
3. API 요청/응답 로그 확인

### 일반적인 문제 해결
1. **CORS 에러**: 백엔드 CORS 설정 확인
2. **401 Unauthorized**: JWT 토큰 만료 또는 유효하지 않음
3. **403 Forbidden**: 권한 부족 (역할 또는 승인 상태 확인)
4. **404 Not Found**: API 엔드포인트 경로 확인

---

## 💡 참고사항

- 모든 API는 RESTful 설계 원칙 따름
- JWT 토큰은 stateless 방식으로 관리
- 비즈니스 룰은 백엔드에서 검증
- 프론트엔드는 사용자 경험에 집중