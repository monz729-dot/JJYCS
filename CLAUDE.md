# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Status

This repository appears to be newly initialized and currently contains no source code, build files, or project configuration files. 

## Next Steps

When code is added to this repository, this file should be updated to include:

- **Build Commands**: Instructions for building, testing, and linting the project
- **Architecture Overview**: High-level structure and key components once the codebase develops
- **Development Workflow**: Common commands and processes specific to this project

## Current Configuration

The repository includes Claude Code local settings in `.claude/settings.local.json` with permissions for directory listing commands.

---

# claude.md — YSC 물류관리 시스템(LMS) 구현 가이드

## 0) TL;DR

* 목표: **모바일 웹 중심 LMS**(주문/입출고/창고/파트너/어드민) MVP를 2스프린트 내 작동 상태로.
* 스택: **Spring Boot 3 + MySQL 8 + Redis + Kafka**, **Vue 3 + TS + Pinia + Vite**, PWA.
* 필수 요구: **EMS/HS 검증, CBM 자동계산 & 29 초과 시 해상→항공 전환, THB 1,500 초과 수취인 추가 경고, 회원코드 미기재(No code) 지연 처리, 일괄 입/출고 & 라벨/QR** 구현.

---

## 1) 레일가드(반드시 지켜)

* 기능 범위는 **PRD.md / PPT 설계서** 기준. 임의 확장 금지.
* **데이터 계약 우선**: API 스펙이 UI보다 우선. DB 스키마/엔드포인트 깨지면 실패.
* 외부 API 실패 시 **폴백/재시도** 넣고, 사용자에겐 친절 오류(토스트/모달).
* 보안: OAuth2, 2FA 훼손 금지. 비밀번호는 **bcrypt**, 통신은 **HTTPS** 강제.
* 성능: 목록 API는 **페이징** 기본, 테이블에는 적절 인덱스.

---

## 2) 작업 컨텍스트

### 2.1 비즈니스 규칙 (핵심)

* **CBM(m³) = (W×H×D)/1,000,000**, 박스/주문 단위 계산. 총 CBM \*\*29 초과 시 자동 ‘air’\*\*로 전환 + Alert.
* **THB 1,500 초과 품목**: 수취인 정보 **추가 입력 유도** 경고.
* **회원 코드 미기재(No code)**: 발송 **지연** 상태 처리 및 경고 UI.
* **EMS/HS 코드**: data.go.kr API/HS 조회로 **검증 필수**.
* **일괄 입/출고** 버튼, **믹스박스/보류** 상태, **라벨/QR 스캔** 플로우 포함.

### 2.2 사용자 역할

* 일반/기업/파트너/창고/어드민. 기업·파트너는 **승인(평일 1\~2일)** 안내 및 워크플로우 반영.

---

## 3) 시스템 아키텍처

* **Backend**: Spring Boot 3.0.3, MyBatis 3.0.3, JDK 17
* **DB**: MySQL 8.0+ (주), Redis(세션/캐시), DynamoDB(세션 가능), Kafka 3.2.0(이벤트)
* **Frontend**: Vue 3.4 + TS + Pinia + Vite, PWA
* **Infra**: AWS (EC2/S3/RDS/ECS), GitHub Actions, Prometheus/Grafana
* **Security**: OAuth2, 2FA, bcrypt/AES-256, ReCAPTCHA v3

---

## 4) 디렉터리 구조

```
/backend
  /src/main/java/...
  /src/main/resources/db/migration      # Flyway
  /src/main/resources/application.yml
/frontend
  /src
    /modules
      orders/                           # 주문/박스/라벨
      warehouse/                        # 스캔/입출고/로그
      partner/                          # 리퍼럴/캠페인
      admin/                            # 승인/요율/설정
      auth/                             # 로그인/회원가입/2FA
    /components/ui
    /i18n
  /public
/docs
  PRD.md
  API.md
  DB_SCHEMA.sql
  flows/ (PPT 캡처 + 주석)
/ops
  docker-compose.yml
  k8s/
```

---

## 5) 환경변수(.env 예시)

```
# backend
DB_URL=mysql://user:pass@localhost:3306/lms
REDIS_URL=redis://localhost:6379
KAFKA_BROKERS=localhost:9092
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=...
SMTP_PASS=...
OAUTH_CLIENT_ID=...
OAUTH_CLIENT_SECRET=...
RECAPTCHA_SITE=...
RECAPTCHA_SECRET=...
EMS_API_KEY=...
HS_API_KEY=...
NAVER_RATE_API_KEY=...

# frontend
VITE_API_BASE=/api
VITE_RECAPTCHA_SITE=...
```

---

## 6) DB 스키마 핵심 (요약)

* 테이블: `users`, `enterprise_profiles`, `partner_profiles`, `warehouse_profiles`, `addresses`,
  `orders`, `order_items`, `order_boxes`, `repack_photos`, `warehouses`, `inventory`,
  `scan_events`, `shipment_tracking`, `payments`, `estimates`, `partner_referrals`, `config`, `audit_logs`.
* 중요한 컬럼:

  * `orders.order_type`(‘air’|‘sea’) — **CBM 29 초과 시 자동 ‘air’** 전환.
  * `orders.requires_extra_recipient` — **THB 1,500 룰** 결과.
  * `users.member_code` — **No code 지연 룰**.
  * `order_boxes.cbm_m3` — 가상컬럼으로 자동 계산.
* 전체 DDL은 `/docs/DB_SCHEMA.sql` (본 문서에 맞춰 작성).

---

## 7) API 계약(발췌)

> 풀 버전은 `/docs/API.md`

* `POST /auth/signup` (역할별 추가필드, 기업/파트너 승인 pending)
* `POST /orders`

  * 입력: 수취인/아이템/박스(치수)
  * 처리: **CBM 계산 → 29 초과면 order\_type=air**, EMS/HS 검증, THB 1,500 경고 플래그.
* `POST /warehouse/scan` — `label_code`, `scan_type(inbound|outbound|hold|mixbox)` 기록.
* `POST /orders/{id}/estimate` — 1차/2차 견적 버전 관리.
* `GET /tracking/ems/{no}` — EMS 상태 동기화.

---

## 8) UIX 구현 체크리스트

* **상태 칩**: 요청/준비/완료/지연/로컬배송 색상 배지.
* **경고 UI**: THB 1,500/No code/CBM 29+ — **토스트 + 모달 병행**.
* **일괄 처리**: 다중 선택 후 **일괄 입고/출고** 버튼 활성.
* **라벨/QR**: 주문코드/박스 라벨 QR 생성·스캔 팝업.
* **촬영 업로드**: 리패킹 Y 시 **PWA 카메라 접근** 필수.
* i18n: ko/en 모두 지원.
* 접근성: 키보드/스크린리더/대비(4.5:1).

---

## 9) 스프린트 계획 (MVP 기준)

### Sprint 1 (백엔드 코어 + 회원/주문 생성)

* DB 마이그레이션(Flyway)
* Auth(이메일 인증, 기업/파트너 승인 pending)
* 주문 생성 API(+ CBM/EMS/HS/1500 룰)
* 창고 스캔 API(입고)
* 기본 대시보드 목록(사용자/관리자)

### Sprint 2 (출고/라벨/정산/파트너)

* 라벨/QR 생성·스캔·출력
* 일괄 입/출고, 보류/믹스박스
* 견적(1·2차) + 로컬운임/리패킹 합계
* 파트너 대시보드/정산 기초

**Done 정의**: 위 플로우 **모바일 실제 기기**에서 E2E로 동작, 오류 메시지 현지화 포함.

---

## 10) 테스트 전략

* 단위: 비즈니스 룰(29/1500/No code/EMS/HS) 테스트 필수.
* 통합: 주문→입고→출고→트래킹 시나리오.
* E2E: Cypress(모바일 뷰포트), BrowserStack 리얼 단말.
* 커버리지 목표: **80%+**.

---

## 11) 보안/개인정보

* 비번 bcrypt, 토큰/SMTP/App Password는 Secrets Manager.
* PII 최소 수집, **주소/전화**는 마스킹 조회 옵션 제공.
* 감사로그 `audit_logs` 전액 기록.

---

## 12) 코드 스타일 & 커밋

* BE: Java 17, 컨벤션 Google style 유사.
* FE: ESLint/Prettier, 컴포넌트당 1기능.
* 커밋: `feat(be/orders): create order with cbm->air auto switch` 형태.

---

## 13) 작업 명령(Claude Code용)

* “**DB\_SCHEMA.sql 생성**: 본 문서 6장 스키마대로 MySQL 8 호환 DDL 작성. 가상컬럼/인덱스 포함.”
* “**API.md 생성**: 7장 계약 전체 서술. 요청/응답/에러코드/예시(JSON).”
* “**/backend 엔드포인트 스텁**: `OrdersController`/`WarehouseController`/`EstimatesController` 생성, 서비스·매퍼 포함.”
* “**/frontend 화면 스켈레톤**: 사용자 대시보드/주문접수/창고 스캔/어드민 사용자승인. 상태칩 & 경고 UX 포함.”
* “**UserRegistration.vue 이식**: 제공 컴포넌트 `/frontend/src/modules/auth/UserRegistration.vue`로 추가, i18n 유지.”

  * (참고 컴포넌트는 별도 파일로 제공됨)

---

## 14) 수용 기준(AC)

* CBM 29.000001 m³ 합계 → 주문 자동 `air` 전환 & 경고 표시.
* 품목 합계 THB 1,500 초과 → 수취인 추가 입력 유도 경고.
* 회원코드 미기재 → 지연 상태/경고 노출.
* 라벨/QR 생성 → 창고 스캔으로 주문 팝업/입·출고 처리.
* 기업/파트너 가입 완료 후 승인 대기 안내(평일 1\~2일).

---

## 15) 참고(출처 트레이스)

* EMS/HS 코드 검증, CBM 계산·29 초과 전환, THB 1,500 경고, No code 지연, 라벨/QR/일괄입출고/믹스박스, 승인 1\~2일 안내 등은 **업로드한 PPT 설계서** 근거.

---

### 메모

* “스펙 빈칸 메우기” 금지. **모르는 값**(요율, 환율 키 등)은 `/docs/API.md`의 `TODO:`로 표시하고, 목업/폴백 로직 포함.
* 성급한 최적화 금지. **먼저 맞게 만들고**, 느리면 고쳐.

---