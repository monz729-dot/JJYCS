# YSC LMS API 명세서

## 개요
YSC 물류관리시스템(LMS) REST API 명세서  
**Base URL**: `/api`  
**Version**: v1  
**Authentication**: Bearer Token (JWT)

---

## 공통 응답 형식

### 성공 응답
```json
{
  "success": true,
  "data": {...},
  "message": "요청이 성공적으로 처리되었습니다",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 오류 응답
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력값이 올바르지 않습니다",
    "details": [
      {
        "field": "email",
        "message": "이메일 형식이 올바르지 않습니다"
      }
    ]
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 페이징 응답
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 100,
      "totalPages": 5
    }
  }
}
```

---

## 1. Authentication API

### 1.1 회원가입
```http
POST /auth/signup
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123!",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "role": "individual", // individual | enterprise | partner | warehouse | admin
  "agreeTerms": true,
  "agreePrivacy": true,
  "agreeMarketing": false,
  
  // 역할별 추가 필드
  "additionalInfo": {
    // enterprise인 경우
    "companyName": "주식회사 테스트",
    "businessNumber": "123-45-67890",
    "companyAddress": "서울시 강남구...",
    "ceoName": "김대표",
    "businessType": "제조업",
    
    // partner인 경우  
    "partnerType": "referral", // referral | campaign
    "referralCode": "PART001",
    "bankAccount": "우리은행 1002-123-456789",
    "accountHolder": "홍길동",
    
    // warehouse인 경우
    "warehouseName": "서울창고",
    "warehouseAddress": "서울시 구로구...",
    "capacity": "1000"
  }
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "data": {
    "userId": 123,
    "email": "user@example.com",
    "name": "홍길동",
    "role": "enterprise",
    "status": "pending_approval", // active | pending_approval | suspended
    "approvalMessage": "기업 회원 승인은 평일 1-2일이 소요됩니다",
    "memberCode": null // 승인 후 생성
  },
  "message": "회원가입이 완료되었습니다. 이메일 인증을 진행해주세요."
}
```

### 1.2 로그인
```http
POST /auth/login
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123!",
  "recaptchaToken": "03AGdBq25..."
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 3600,
    "user": {
      "id": 123,
      "email": "user@example.com",
      "name": "홍길동",
      "role": "enterprise",
      "status": "active",
      "memberCode": "ENT001",
      "requiresTwoFactor": false
    }
  }
}
```

### 1.3 2FA 설정
```http
POST /auth/2fa/setup
Authorization: Bearer {token}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "qrCodeUrl": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "secret": "JBSWY3DPEHPK3PXP",
    "backupCodes": [
      "12345678",
      "87654321",
      "13579246"
    ]
  }
}
```

### 1.4 회원 승인 (관리자)
```http
POST /auth/approve/{userId}
Authorization: Bearer {admin-token}
```

**Request Body**:
```json
{
  "approved": true,
  "memberCode": "ENT001", // 승인시 생성
  "note": "승인 완료"
}
```

---

## 2. Orders API

### 2.1 주문 생성
```http
POST /orders
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "recipient": {
    "name": "김수취",
    "phone": "010-9876-5432",
    "address": "태국 방콕 수쿰빗...",
    "zipCode": "10110",
    "country": "TH"
  },
  "items": [
    {
      "name": "화장품 세트",
      "quantity": 2,
      "weight": 0.5,
      "amount": 2500.00,
      "currency": "THB",
      "category": "cosmetics",
      "hsCode": "3304.99.00", // 선택적
      "description": "기초화장품 세트"
    }
  ],
  "boxes": [
    {
      "width": 30.0, // cm
      "height": 20.0, // cm  
      "depth": 15.0, // cm
      "weight": 1.2, // kg
      "items": [0] // 아이템 인덱스 배열
    }
  ],
  "shipping": {
    "preferredType": "sea", // sea | air
    "urgency": "normal", // normal | urgent
    "needsRepacking": false,
    "specialInstructions": "깨지기 쉬운 물품"
  },
  "payment": {
    "method": "prepaid" // prepaid | postpaid
  }
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "data": {
    "orderId": 12345,
    "orderCode": "ORD-2024-001",
    "status": "requested",
    "orderType": "air", // CBM 29 초과로 자동 전환됨
    "totalCBM": 32.5,
    "totalAmount": 5000.00,
    "currency": "THB",
    "requiresExtraRecipient": true, // THB 1500 초과
    "warnings": [
      {
        "type": "CBM_EXCEEDED",
        "message": "총 CBM이 29를 초과하여 항공 배송으로 자동 전환되었습니다",
        "details": { "cbm": 32.5, "threshold": 29 }
      },
      {
        "type": "AMOUNT_EXCEEDED_THB_1500", 
        "message": "금액이 THB 1,500를 초과합니다. 수취인 추가 정보를 입력해주세요",
        "details": { "amount": 5000, "threshold": 1500 }
      }
    ],
    "estimatedDelivery": "2024-02-01",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

### 2.2 주문 조회
```http
GET /orders/{orderId}
Authorization: Bearer {token}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "orderId": 12345,
    "orderCode": "ORD-2024-001",
    "status": "in_transit",
    "orderType": "air",
    "user": {
      "id": 123,
      "name": "홍길동",
      "memberCode": "ENT001"
    },
    "recipient": {
      "name": "김수취",
      "phone": "010-****-5432",
      "address": "태국 방콕 수쿰빗...",
      "country": "TH"
    },
    "items": [...],
    "boxes": [
      {
        "boxId": 1,
        "labelCode": "BOX-2024-001-01",
        "qrCode": "data:image/png;base64,...",
        "width": 30.0,
        "height": 20.0, 
        "depth": 15.0,
        "cbm": 0.009, // 자동 계산됨
        "status": "shipped",
        "trackingNumber": "EMS123456789KR"
      }
    ],
    "totalCBM": 32.5,
    "timeline": [
      {
        "status": "requested",
        "timestamp": "2024-01-15T10:30:00Z",
        "note": "주문 접수"
      },
      {
        "status": "confirmed", 
        "timestamp": "2024-01-15T14:20:00Z",
        "note": "주문 확인 완료"
      }
    ]
  }
}
```

### 2.3 주문 목록 조회
```http
GET /orders?page=0&size=20&status=all&startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {token}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "orderId": 12345,
        "orderCode": "ORD-2024-001", 
        "status": "in_transit",
        "orderType": "air",
        "totalAmount": 5000.00,
        "currency": "THB",
        "recipient": {
          "name": "김수취",
          "country": "TH"
        },
        "createdAt": "2024-01-15T10:30:00Z",
        "estimatedDelivery": "2024-02-01"
      }
    ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 150,
      "totalPages": 8
    }
  }
}
```

### 2.4 주문 수정
```http
PUT /orders/{orderId}
Authorization: Bearer {token}
```

**Request Body**: (생성과 동일한 구조, 부분 수정 가능)

---

## 3. Warehouse API

### 3.1 창고 스캔
```http
POST /warehouse/scan
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "labelCode": "BOX-2024-001-01",
  "scanType": "inbound", // inbound | outbound | hold | mixbox
  "warehouseId": 1,
  "location": "A-01-15", // 선택적
  "note": "정상 입고 처리", // 선택적
  "photos": [ // 리패킹시 필수
    {
      "type": "before", // before | after | damage
      "url": "https://s3.../photo1.jpg",
      "timestamp": "2024-01-15T15:30:00Z"
    }
  ]
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "scanId": 9876,
    "box": {
      "boxId": 1,
      "labelCode": "BOX-2024-001-01",
      "orderId": 12345,
      "orderCode": "ORD-2024-001",
      "status": "inbound_completed",
      "items": [
        {
          "name": "화장품 세트",
          "quantity": 2
        }
      ]
    },
    "warehouse": {
      "id": 1,
      "name": "서울창고",
      "location": "A-01-15"
    },
    "timestamp": "2024-01-15T15:30:00Z",
    "nextAction": "ready_for_outbound" // 다음 가능한 액션
  }
}
```

### 3.2 일괄 처리
```http
POST /warehouse/batch-process
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "action": "outbound", // inbound | outbound | hold
  "labelCodes": [
    "BOX-2024-001-01",
    "BOX-2024-002-01", 
    "BOX-2024-003-01"
  ],
  "warehouseId": 1,
  "note": "일괄 출고 처리"
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "batchId": "BATCH-2024-001",
    "processed": 3,
    "failed": 0,
    "results": [
      {
        "labelCode": "BOX-2024-001-01",
        "status": "success",
        "newStatus": "outbound_completed"
      }
    ],
    "summary": {
      "totalBoxes": 3,
      "totalOrders": 2,
      "estimatedShippingCost": 15000.00
    }
  }
}
```

### 3.3 재고 조회
```http
GET /warehouse/{warehouseId}/inventory?page=0&size=20&status=all
Authorization: Bearer {token}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "boxId": 1,
        "labelCode": "BOX-2024-001-01",
        "orderId": 12345,
        "orderCode": "ORD-2024-001",
        "status": "inbound_completed",
        "location": "A-01-15",
        "inboundDate": "2024-01-15T15:30:00Z",
        "daysInWarehouse": 5,
        "items": [
          {
            "name": "화장품 세트",
            "quantity": 2
          }
        ]
      }
    ],
    "page": {...},
    "summary": {
      "totalBoxes": 150,
      "statusCounts": {
        "inbound_completed": 120,
        "ready_for_outbound": 25,
        "hold": 5
      }
    }
  }
}
```

---

## 4. Estimates API

### 4.1 견적 생성
```http
POST /orders/{orderId}/estimates
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "version": "first", // first | second
  "shipping": {
    "method": "air", // sea | air
    "carrier": "EMS", // EMS | DHL | FedEx
    "serviceLevel": "standard" // standard | express
  },
  "fees": {
    "shippingFee": 25000.00,
    "localFee": 5000.00, // TODO: 정확한 요율 정보 필요
    "repackingFee": 3000.00,
    "insuranceFee": 1000.00,
    "customsFee": 2000.00
  },
  "currency": "KRW",
  "estimatedDays": 7,
  "validUntil": "2024-01-30T23:59:59Z",
  "note": "항공 배송으로 빠른 배송 가능"
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "data": {
    "estimateId": 5678,
    "orderId": 12345,
    "version": "first",
    "totalAmount": 36000.00,
    "currency": "KRW",
    "breakdown": {
      "shippingFee": 25000.00,
      "localFee": 5000.00,
      "repackingFee": 3000.00,
      "insuranceFee": 1000.00,
      "customsFee": 2000.00,
      "tax": 0.00
    },
    "estimatedDays": 7,
    "validUntil": "2024-01-30T23:59:59Z",
    "status": "pending", // pending | accepted | rejected | expired
    "createdAt": "2024-01-15T16:00:00Z"
  }
}
```

### 4.2 견적 조회
```http
GET /orders/{orderId}/estimates
Authorization: Bearer {token}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "estimateId": 5678,
      "version": "first",
      "totalAmount": 36000.00,
      "status": "accepted",
      "acceptedAt": "2024-01-16T09:00:00Z"
    },
    {
      "estimateId": 5679, 
      "version": "second",
      "totalAmount": 42000.00,
      "status": "pending",
      "note": "리패킹 비용 추가"
    }
  ]
}
```

### 4.3 견적 승인/거부
```http
POST /estimates/{estimateId}/respond
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "action": "accept", // accept | reject
  "note": "견적 승인합니다"
}
```

---

## 5. Tracking API

### 5.1 EMS 추적 조회
```http
GET /tracking/ems/{trackingNumber}
Authorization: Bearer {token}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "trackingNumber": "EMS123456789KR",
    "status": "in_transit",
    "currentLocation": "방콕 국제우편교환소",
    "events": [
      {
        "status": "dispatched",
        "location": "서울국제우편물류센터",
        "timestamp": "2024-01-18T08:00:00Z",
        "description": "발송"
      },
      {
        "status": "in_transit",
        "location": "방콕 국제우편교환소",
        "timestamp": "2024-01-20T14:30:00Z", 
        "description": "도착"
      }
    ],
    "estimatedDelivery": "2024-01-25",
    "lastUpdated": "2024-01-20T14:30:00Z"
  }
}
```

### 5.2 추적 정보 동기화
```http
POST /tracking/sync/{orderId}
Authorization: Bearer {token}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "orderId": 12345,
    "syncedBoxes": 2,
    "updates": [
      {
        "boxId": 1,
        "trackingNumber": "EMS123456789KR",
        "previousStatus": "shipped",
        "currentStatus": "in_transit",
        "updated": true
      }
    ],
    "lastSyncAt": "2024-01-20T15:00:00Z"
  }
}
```

---

## 6. Partner API

### 6.1 파트너 대시보드
```http
GET /partner/dashboard
Authorization: Bearer {partner-token}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalReferrals": 25,
      "activeOrders": 15,
      "totalCommission": 150000.00,
      "pendingCommission": 45000.00,
      "thisMonthReferrals": 8
    },
    "recentReferrals": [
      {
        "userId": 456,
        "userName": "김고객",
        "joinedAt": "2024-01-15T10:30:00Z",
        "firstOrderAmount": 50000.00,
        "commissionAmount": 2500.00,
        "status": "active"
      }
    ],
    "commissionHistory": [
      {
        "orderId": 12345,
        "orderAmount": 50000.00,
        "commissionRate": 0.05,
        "commissionAmount": 2500.00,
        "status": "paid", // pending | paid
        "paidAt": "2024-01-20T00:00:00Z"
      }
    ]
  }
}
```

### 6.2 추천 링크 생성
```http
POST /partner/referral-link
Authorization: Bearer {partner-token}
```

**Request Body**:
```json
{
  "campaignName": "신년 특가 캠페인",
  "validUntil": "2024-03-31T23:59:59Z",
  "note": "신규 고객 대상"
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "data": {
    "referralLinkId": 789,
    "url": "https://ysc-lms.com/signup?ref=PART001_NY2024",
    "referralCode": "PART001_NY2024", 
    "campaignName": "신년 특가 캠페인",
    "validUntil": "2024-03-31T23:59:59Z",
    "createdAt": "2024-01-15T17:00:00Z"
  }
}
```

---

## 7. Admin API

### 7.1 사용자 승인 목록
```http
GET /admin/approvals?page=0&size=20&status=pending
Authorization: Bearer {admin-token}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "userId": 789,
        "email": "company@example.com",
        "name": "주식회사 테스트",
        "role": "enterprise",
        "requestedAt": "2024-01-15T10:30:00Z",
        "businessInfo": {
          "companyName": "주식회사 테스트",
          "businessNumber": "123-45-67890",
          "ceoName": "김대표"
        },
        "documents": [
          {
            "type": "business_registration",
            "url": "https://s3.../doc1.pdf",
            "uploadedAt": "2024-01-15T10:35:00Z"
          }
        ]
      }
    ],
    "page": {...}
  }
}
```

### 7.2 사용자 관리
```http
GET /admin/users?page=0&size=20&role=all&status=all&search=
Authorization: Bearer {admin-token}
```

### 7.3 시스템 설정
```http
GET /admin/config
Authorization: Bearer {admin-token}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "businessRules": {
      "cbmThreshold": 29.0,
      "thbAmountThreshold": 1500.0,
      "autoAirConversion": true,
      "requireMemberCode": true
    },
    "fees": {
      "defaultShippingRates": {
        "sea": {
          "perKg": 8000.00,
          "perCbm": 120000.00
        },
        "air": {
          "perKg": 15000.00,
          "perCbm": 180000.00
        }
      },
      "localFees": {
        // TODO: 정확한 로컬운임 체계 정의 필요
        "thailand": {
          "standard": 5000.00,
          "express": 8000.00
        }
      },
      "repackingFee": 3000.00,
      "insuranceRate": 0.02
    },
    "notifications": {
      "emailEnabled": true,
      "smsEnabled": false,
      "webhookUrl": "https://webhook.example.com/ysc-lms"
    }
  }
}
```

---

## 8. File Upload API

### 8.1 파일 업로드 (리패킹 사진, 문서)
```http
POST /files/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Request**:
```
file: (binary)
type: repack_photo | document | label
orderId: 12345 (선택적)
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "fileId": "uuid-123-456",
    "url": "https://s3.amazonaws.com/ysc-lms/files/uuid-123-456.jpg",
    "type": "repack_photo",
    "size": 1024000,
    "uploadedAt": "2024-01-15T18:00:00Z"
  }
}
```

---

## 9. Validation API

### 9.1 EMS 코드 검증
```http
GET /validation/ems/{code}
Authorization: Bearer {token}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "code": "EMS123456789KR",
    "valid": true,
    "status": "active",
    "destination": "Thailand",
    "estimatedDelivery": "2024-01-25"
  }
}
```

### 9.2 HS 코드 검증
```http
GET /validation/hs/{code}
Authorization: Bearer {token}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "code": "3304.99.00",
    "valid": true,
    "description": "기타 화장품 제조용품",
    "category": "cosmetics",
    "restrictions": [
      {
        "country": "TH",
        "type": "quantity_limit",
        "value": "개인용 소량"
      }
    ]
  }
}
```

**Fallback Response (외부 API 실패시)**:
```json
{
  "success": true,
  "data": {
    "code": "3304.99.00",
    "valid": true,
    "warning": "HS 코드 검증 서비스가 일시적으로 이용불가합니다. 나중에 다시 확인해주세요.",
    "fallbackUsed": true
  }
}
```

---

## 10. 오류 코드

### 공통 오류 코드
- `UNAUTHORIZED` (401): 인증 실패
- `FORBIDDEN` (403): 권한 없음  
- `NOT_FOUND` (404): 리소스 없음
- `VALIDATION_ERROR` (400): 입력값 검증 실패
- `RATE_LIMIT_EXCEEDED` (429): 요청 한도 초과
- `INTERNAL_ERROR` (500): 서버 내부 오류

### 비즈니스 로직 오류 코드
- `MEMBER_CODE_REQUIRED` (400): 회원코드 필수
- `CBM_CALCULATION_ERROR` (400): CBM 계산 오류
- `INVALID_ORDER_STATUS` (400): 잘못된 주문 상태
- `WAREHOUSE_CAPACITY_EXCEEDED` (400): 창고 용량 초과
- `INVALID_TRACKING_NUMBER` (400): 잘못된 운송장 번호
- `APPROVAL_REQUIRED` (403): 승인 대기 중
- `EXTERNAL_API_UNAVAILABLE` (503): 외부 API 서비스 불가

### 외부 서비스 오류 코드  
- `EMS_API_ERROR` (503): EMS API 오류
- `HS_API_ERROR` (503): HS 코드 API 오류
- `PAYMENT_GATEWAY_ERROR` (503): 결제 게이트웨이 오류

---

## 11. 웹훅 (Webhook)

### 11.1 주문 상태 변경
```http
POST {webhook_url}
Content-Type: application/json
X-Signature: sha256={signature}
```

**Payload**:
```json
{
  "event": "order.status_changed",
  "timestamp": "2024-01-20T15:30:00Z",
  "data": {
    "orderId": 12345,
    "orderCode": "ORD-2024-001",
    "previousStatus": "confirmed",
    "currentStatus": "shipped",
    "trackingNumbers": ["EMS123456789KR"],
    "updatedBy": "system"
  }
}
```

### 11.2 사용자 승인 완료
```http
POST {webhook_url}
```

**Payload**:
```json
{
  "event": "user.approved",
  "timestamp": "2024-01-20T15:30:00Z",
  "data": {
    "userId": 789,
    "email": "company@example.com",
    "role": "enterprise", 
    "memberCode": "ENT001",
    "approvedBy": 1
  }
}
```

---

## 12. 환율 및 요율 API (TODO)

> **주의**: 아래 API들은 정확한 비즈니스 정책이 확정되지 않아 TODO 상태입니다.

### 12.1 환율 조회
```http
GET /rates/exchange?from=KRW&to=THB
Authorization: Bearer {token}
```

**TODO**: 네이버 환율 API 또는 다른 신뢰할 수 있는 환율 제공업체 연동 필요

### 12.2 운임 계산
```http
POST /rates/shipping
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "origin": "KR",
  "destination": "TH", 
  "weight": 2.5,
  "cbm": 0.05,
  "method": "air"
}
```

**TODO**: 실제 운송업체별 요율표 및 계산 로직 정의 필요

---

## 13. 개발/테스트용 API

### 13.1 테스트 데이터 생성 (Development Only)
```http
POST /dev/seed-data
Authorization: Bearer {admin-token}
```

### 13.2 캐시 초기화
```http
POST /dev/clear-cache
Authorization: Bearer {admin-token}
```

---

## 부록

### A. 상태 코드 정의

#### 주문 상태
- `requested`: 주문 요청
- `confirmed`: 주문 확인
- `payment_pending`: 결제 대기
- `payment_completed`: 결제 완료
- `preparing`: 준비 중
- `ready_to_ship`: 발송 준비 완료
- `shipped`: 발송 완료
- `in_transit`: 배송 중
- `customs_clearance`: 통관 중
- `out_for_delivery`: 배송 출발
- `delivered`: 배송 완료
- `delayed`: 지연 (member_code 없는 경우)
- `cancelled`: 취소

#### 박스 상태
- `created`: 생성
- `inbound_pending`: 입고 대기
- `inbound_completed`: 입고 완료
- `ready_for_outbound`: 출고 준비
- `outbound_completed`: 출고 완료
- `shipped`: 발송
- `hold`: 보류
- `mixbox`: 믹스박스

### B. 국가 코드
- `KR`: 대한민국
- `TH`: 태국
- `VN`: 베트남
- `CN`: 중국
- `JP`: 일본

### C. 통화 코드
- `KRW`: 한국 원
- `THB`: 태국 바트
- `VND`: 베트남 동
- `CNY`: 중국 위안
- `USD`: 미국 달러