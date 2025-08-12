# 🚀 YCS LMS 시스템 현황 및 계정 정보

## 📋 시스템 구성 완료

### ✅ **완전 구현된 시스템**
- **백엔드**: Spring Boot 3.0.3 + MyBatis + JWT 인증
- **프론트엔드**: Vue 3 + TypeScript + Pinia + Router
- **데이터베이스**: MySQL 8.0 스키마 완성 (H2 개발용 포함)
- **비즈니스 로직**: CBM 계산, 자동 항공 전환, THB 경고 등 모든 룰 구현

---

## 👥 **개발용 계정 정보**

### 🟦 **1. 일반 사용자 (Individual Users)**
```
📧 Email: user@ycs.com
🔐 Password: password123
🏷️ 회원코드: USR001
✅ 상태: 활성화
🎯 권한: 주문 생성, 추적, 개인 대시보드

📧 Email: user2@ycs.com  
🔐 Password: password123
🏷️ 회원코드: USR002
✅ 상태: 활성화
```

### 🟩 **2. 기업 사용자 (Enterprise Users)**
```
📧 Email: enterprise@ycs.com
🔐 Password: password123
🏷️ 회원코드: ENT001
🏢 회사: (주)테스트기업
✅ 상태: 승인완료
🎯 권한: 대량 주문, 기업 대시보드, 견적 관리

📧 Email: company2@ycs.com
🔐 Password: password123  
🏷️ 회원코드: 없음 (지연 경고 테스트용)
🏢 회사: (주)승인대기회사
⏳ 상태: 승인대기 (1-2일 소요)
```

### 🟪 **3. 파트너 사용자 (Partner Users)**
```
📧 Email: partner@ycs.com
🔐 Password: password123
🏷️ 회원코드: PTN001
🤝 유형: 제휴 파트너
✅ 상태: 승인완료
💰 커미션: 5.5%
🎯 권한: 추천 관리, 정산 확인, 파트너 대시보드

📧 Email: affiliate@ycs.com
🔐 Password: password123
🏷️ 회원코드: 없음 (승인대기)
🤝 유형: 리퍼럴 파트너
⏳ 상태: 승인대기
```

### 🟨 **4. 창고 관리자 (Warehouse Managers - 중간관리자)**
```
📧 Email: warehouse@ycs.com
🔐 Password: password123
🏷️ 회원코드: WHS001
🏭 담당: 서울 메인 창고
👔 직급: Warehouse Manager
🎯 권한: 
  - QR/라벨 스캔 및 생성
  - 일괄 입출고 처리
  - 재고 관리 및 조회
  - 박스 보류/해제
  - 창고 대시보드

📧 Email: warehouse2@ycs.com
🔐 Password: password123
🏷️ 회원코드: WHS002
🏭 담당: 부산 지점 창고
👔 직급: Logistics Coordinator
```

### 🟥 **5. 시스템 관리자 (System Admins - 최고관리자)**
```
📧 Email: admin@ycs.com
🔐 Password: password123
🏷️ 회원코드: ADM001
👑 권한: 시스템 전체 관리
🎯 기능:
  - 사용자 승인/거부
  - 시스템 설정 변경
  - 전체 주문/재고 조회
  - 요율 관리
  - 감사 로그 확인

📧 Email: superadmin@ycs.com
🔐 Password: password123
🏷️ 회원코드: ADM000
👑 권한: 슈퍼 관리자
```

---

## 🔍 **비즈니스 룰 테스트 데이터**

### 📦 **CBM > 29m³ 초과 테스트**
- **주문코드**: ORD-2024-007
- **총 CBM**: 38.156m³ (29m³ 초과)
- **결과**: 자동으로 해상 → 항공 배송 전환
- **경고**: "CBM 한도 초과로 항공배송 전환됨" 알림 표시

### 💰 **THB 1,500 초과 테스트**
```
ORD-2024-002: THB 2,300 (삼성 갤럭시 + 이어버드)
ORD-2024-004: THB 15,000 (산업용 부품 대량)
ORD-2024-005: THB 8,500 (전자제품)
ORD-2024-006: THB 3,200 (의료기기)
```
**결과**: 수취인 추가 정보 입력 요구 (ID, 이메일 등)

### ⚠️ **회원코드 없는 사용자**
```
company2@ycs.com: 회원코드 미등록
affiliate@ycs.com: 회원코드 미등록
```
**결과**: "발송 지연 가능성" 경고 및 프로필 완성 유도

---

## 🌐 **접속 URL (실제 실행 시)**

```bash
# 백엔드 API 서버
http://localhost:8080/api

# 프론트엔드 웹 서버  
http://localhost:5173

# H2 데이터베이스 콘솔 (개발용)
http://localhost:8080/h2-console
```

---

## 🛠️ **실행 방법**

### 개발 환경 실행
```bash
# 1. 백엔드 실행 (Spring Boot)
cd backend
mvn spring-boot:run -Dspring.profiles.active=dev

# 2. 프론트엔드 실행 (Vue 3)
cd frontend
npm install
npm run dev
```

### 🐳 **Docker 실행 (권장)**
```bash
# 전체 시스템 실행
docker-compose up -d

# 특정 서비스만 실행
docker-compose up -d mysql redis
docker-compose up backend frontend
```

---

## 🎯 **테스트 시나리오**

### 시나리오 1: 일반 사용자 주문
1. `user@ycs.com`로 로그인
2. 새 주문 생성 (라면, 김치 등)
3. CBM < 29, THB < 1,500 확인
4. 해상배송 선택 가능 확인

### 시나리오 2: 기업 고가품 주문
1. `enterprise@ycs.com`로 로그인
2. 고가 전자제품 주문 (THB 5,000)
3. 수취인 추가 정보 요구 확인
4. 견적 요청 기능 테스트

### 시나리오 3: CBM 초과 주문
1. 대형 박스 여러 개 추가
2. 총 CBM > 29m³ 달성
3. 자동 항공배송 전환 경고 확인
4. 해상배송 선택 비활성화 확인

### 시나리오 4: 창고 관리
1. `warehouse@ycs.com`로 로그인
2. QR 코드 스캔 시뮬레이션
3. 일괄 입출고 처리
4. 재고 현황 확인

### 시나리오 5: 관리자 기능
1. `admin@ycs.com`로 로그인  
2. 대기 중인 기업/파트너 승인
3. 시스템 설정 변경
4. 전체 주문 현황 모니터링

---

## 🔧 **주요 기능 구현 상태**

### ✅ **완료된 기능**
- [x] JWT 기반 인증 시스템
- [x] 역할별 권한 제어 (RBAC)
- [x] CBM 자동 계산 및 29m³ 초과 시 항공 전환
- [x] THB 1,500 초과 시 추가 정보 요구
- [x] 회원코드 검증 및 지연 경고
- [x] EMS/HS 코드 검증 (폴백 포함)
- [x] 창고 QR/라벨 시스템
- [x] 일괄 입출고 처리
- [x] 실시간 재고 관리
- [x] 다단계 주문 생성 폼
- [x] 역할별 대시보드
- [x] 주문 추적 시스템
- [x] 견적 관리 시스템

### 🚧 **추가 개발 권장사항**
- [ ] 실제 결제 시스템 연동
- [ ] SMS/이메일 알림 서비스
- [ ] 실시간 배송 추적 API 연동
- [ ] 다국어 번역 파일 완성
- [ ] PWA 오프라인 기능
- [ ] 리포팅 및 통계 시스템

**🎉 모든 핵심 기능이 완성되어 즉시 테스트 및 개발이 가능합니다!**