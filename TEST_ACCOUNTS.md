# YCS LMS 테스트 계정 정보

## 🔑 로그인 정보
**모든 계정의 비밀번호: `password123`**

## 👥 사용자 계정

### 🔧 관리자 (Admin)
| 이메일 | 이름 | 역할 | 상태 | 회원코드 |
|--------|------|------|------|----------|
| admin@ycs.com | System Admin | ADMIN | ACTIVE | ADM001 |
| admin2@ycs.com | Admin Manager | ADMIN | ACTIVE | ADM002 |

### 🏭 창고 관리자 (Warehouse)
| 이메일 | 이름 | 역할 | 상태 | 회원코드 |
|--------|------|------|------|----------|
| warehouse@ycs.com | Warehouse Manager | WAREHOUSE | ACTIVE | WH001 |
| warehouse2@ycs.com | Warehouse Staff A | WAREHOUSE | ACTIVE | WH002 |
| warehouse3@ycs.com | Warehouse Staff B | WAREHOUSE | ACTIVE | WH003 |

### 👤 일반 사용자 (Individual)
| 이메일 | 이름 | 역할 | 상태 | 회원코드 |
|--------|------|------|------|----------|
| user1@test.com | 김철수 | INDIVIDUAL | ACTIVE | USR001 |
| user2@test.com | 이영희 | INDIVIDUAL | ACTIVE | USR002 |
| user3@test.com | 박민수 | INDIVIDUAL | ACTIVE | USR003 |
| user4@test.com | 최지은 | INDIVIDUAL | ACTIVE | USR004 |
| user5@test.com | 정대호 | INDIVIDUAL | ACTIVE | USR005 |

### 🏢 기업 사용자 (Enterprise)
| 이메일 | 이름 | 역할 | 상태 | 회원코드 |
|--------|------|------|------|----------|
| corp@company.com | 홍길동 | ENTERPRISE | ACTIVE | ENT001 |
| corp2@company.com | 김사장 | ENTERPRISE | ACTIVE | ENT002 |
| corp3@bizcorp.com | 이대표 | ENTERPRISE | ACTIVE | ENT003 |
| corp4@trading.com | 박부장 | ENTERPRISE | ACTIVE | ENT004 |

### 🤝 파트너 (Partner)
| 이메일 | 이름 | 역할 | 상태 | 회원코드 |
|--------|------|------|------|----------|
| partner@partner.com | 박영희 | PARTNER | ACTIVE | PTR001 |
| partner2@logistics.com | 김파트너 | PARTNER | ACTIVE | PTR002 |
| partner3@shipping.com | 이협력사 | PARTNER | ACTIVE | PTR003 |

### ⏳ 승인 대기 사용자
| 이메일 | 이름 | 역할 | 상태 | 회원코드 |
|--------|------|------|------|----------|
| pending@test.com | 이승기 | ENTERPRISE | PENDING_APPROVAL | - |
| pending2@newcompany.com | 신규기업 | ENTERPRISE | PENDING_APPROVAL | - |
| pending3@startup.com | 스타트업대표 | PARTNER | PENDING_APPROVAL | - |

### ⚠️ 회원코드 미등록 (지연 테스트용)
| 이메일 | 이름 | 역할 | 상태 | 회원코드 |
|--------|------|------|------|----------|
| nocode@test.com | 코드없는사용자 | INDIVIDUAL | ACTIVE | - |
| nocode2@test.com | 미등록사용자 | INDIVIDUAL | ACTIVE | - |

## 📦 테스트 데이터 요약

### 주문 데이터
- **총 23개 주문**: 다양한 상태 (요청, 확인, 진행중, 배송, 완료, 취소, 보류)
- **개인 주문**: 5개 (일반 사용자)
- **기업 주문**: 5개 (대량 주문)
- **파트너 주문**: 3개 (물류 용품)
- **최근 주문**: 5개 (최근 7일)
- **특수 테스트 주문**: 
  - CBM 35.5m³ 초과 주문 (자동 항공 전환 테스트)
  - 회원코드 없는 주문 (지연 처리 테스트)
  - THB 35,000 고가 상품 (추가 수취인 정보 필요)

### 상품 데이터
- **25개 상품**: 전자제품, 패션, 뷰티, 럭셔리, 용품, 가전 등
- **브랜드**: Samsung, Apple, Nike, Rolex, Hermes, Canon, HP 등
- **가격대**: 500 THB ~ 35,000 THB

### 박스 데이터
- **29개 박스**: 다양한 크기와 CBM
- **상태**: 생성, 입고 완료, 출고 준비, 배송, 완료, 취소, 보류
- **라벨 코드**: BOX-2024-XXXXX-XX 형식

### 창고 데이터
- **3개 창고**: 방콕, 파타야, 치앙마이
- **재고 현황**: 8개 품목별 재고 관리
- **스캔 이벤트**: 7개 스캔 기록

### 배송 추적
- **4개 추적**: Thai Express, Thailand Post, DHL, FedEx
- **상태**: 배송완료, 운송중

### 견적 및 결제
- **5개 견적**: 승인 대기 및 승인됨
- **5개 결제**: 신용카드, 계좌이체 (완료/대기)

### 파트너 리퍼럴
- **4개 리퍼럴**: 파트너별 수수료 관리

## 🧪 테스트 시나리오

### 1. 비즈니스 룰 테스트
- **CBM 29 초과**: YCS-2024-00021 (35.562 m³)
- **THB 1,500 초과**: 파텍 필립 시계 (35,000 THB)
- **회원코드 없음**: nocode@test.com, nocode2@test.com

### 2. 역할별 테스트
- **관리자**: 사용자 승인, 시스템 관리
- **창고**: 스캔, 입출고 관리
- **일반사용자**: 주문 생성, 추적
- **기업사용자**: 대량 주문, 후불 결제
- **파트너**: 리퍼럴, 수수료 관리

### 3. 상태별 테스트
- **신규 주문**: 요청 상태
- **진행 중**: 입고, 출고 준비
- **완료**: 배송 완료
- **예외 상황**: 취소, 보류

## 🚀 접속 방법

1. **프론트엔드**: http://localhost:5175
2. **백엔드 API**: http://localhost:8080 (Java 설치 후)

## 📝 참고사항

- H2 인메모리 DB 사용 (애플리케이션 재시작 시 데이터 초기화)
- 모든 비밀번호는 bcrypt로 해시됨
- JWT 토큰 기반 인증
- 실시간 데이터 변경 시 DB 트리거 및 이벤트 발생