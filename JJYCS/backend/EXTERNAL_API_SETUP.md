# 외부 API 키 설정 가이드

## 필요한 외부 API 키 목록

### 1. 관세청 API (필수)
YSC 물류관리시스템에서 HS 코드 검색, 관세율 조회, 환율 정보를 위해 사용합니다.

#### 신청 방법
1. **관세청 Open API 포털** 접속: https://unipass.customs.go.kr/
2. 회원가입 후 API 키 신청
3. 사업자 등록증 및 API 사용 목적서 제출
4. 승인까지 2-3일 소요

#### 필요한 환경변수
```bash
# HS 코드 검색 API
HSCODE_SEARCH_API_KEY=your_hscode_search_api_key_here

# 관세율 조회 API  
HSCODE_TARIFF_API_KEY=your_hscode_tariff_api_key_here

# 환율 정보 API
HSCODE_EXCHANGE_API_KEY=your_hscode_exchange_api_key_here
```

### 2. EMS API (선택사항)
태국 우편번호 검증 및 배송 추적을 위해 사용합니다.

#### 신청 방법
1. **우정사업본부 Open API** 접속: https://openapi.epost.go.kr/
2. API 키 발급 신청
3. 승인 즉시 사용 가능

#### 필요한 환경변수
```bash
EMS_API_KEY=your_ems_api_key_here
EMS_API_BASE_URL=https://ems.epost.go.kr/api
```

### 3. Data.go.kr API (선택사항) 
정부 데이터 포털의 EMS 관련 데이터 조회용입니다.

#### 신청 방법
1. **공공데이터포털** 접속: https://www.data.go.kr/
2. 회원가입 후 EMS 관련 API 신청
3. 즉시 승인

#### 필요한 환경변수
```bash
DATA_GO_KR_SERVICE_KEY=your_data_go_kr_service_key_here
```

## 환경변수 설정 방법

### 개발 환경 (.env 파일)
프로젝트 루트에 `.env` 파일을 생성하고 다음과 같이 설정:

```bash
# 관세청 API 키들
HSCODE_SEARCH_API_KEY=s240o275s078n237g000a070s1
HSCODE_TARIFF_API_KEY=s240o275s078n237g000a070s2  
HSCODE_EXCHANGE_API_KEY=s240o275s078n237g000a070s3

# EMS API
EMS_API_KEY=your_actual_ems_key
EMS_API_BASE_URL=https://ems.epost.go.kr/api

# Data.go.kr
DATA_GO_KR_SERVICE_KEY=your_actual_data_go_kr_key

# 데이터베이스 (운영 환경)
DB_URL=jdbc:mysql://your-db-host:3306/ycs_lms
DB_USERNAME=your_db_username
DB_PASSWORD=your_secure_db_password

# JWT 보안 키 (64자 이상 권장)
JWT_SECRET_KEY=YSC-LMS-2024-PRODUCTION-SECRET-KEY-MUST-BE-VERY-LONG-AND-SECURE-64CHARS

# SMTP 설정 (Gmail App Password)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_gmail@gmail.com
SMTP_PASSWORD=your_16_char_app_password

# 프론트엔드 URL (CORS 설정용)
FRONTEND_URL=https://your-domain.com
ADMIN_FRONTEND_URL=https://admin.your-domain.com
```

### 운영 환경 (Docker/Kubernetes)
```yaml
# docker-compose.yml 예시
services:
  ycs-lms-backend:
    environment:
      - HSCODE_SEARCH_API_KEY=${HSCODE_SEARCH_API_KEY}
      - HSCODE_TARIFF_API_KEY=${HSCODE_TARIFF_API_KEY}
      - HSCODE_EXCHANGE_API_KEY=${HSCODE_EXCHANGE_API_KEY}
      - EMS_API_KEY=${EMS_API_KEY}
      - DATA_GO_KR_SERVICE_KEY=${DATA_GO_KR_SERVICE_KEY}
      - JWT_SECRET_KEY=${JWT_SECRET_KEY}
      - DB_URL=${DB_URL}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - SMTP_USERNAME=${SMTP_USERNAME}
      - SMTP_PASSWORD=${SMTP_PASSWORD}
```

### AWS/Cloud 환경
```bash
# AWS Parameter Store 또는 Secrets Manager 사용 권장
aws ssm put-parameter --name "/ycs-lms/hscode-search-api-key" --value "your_key" --type SecureString
aws ssm put-parameter --name "/ycs-lms/jwt-secret" --value "your_secret" --type SecureString
```

## API 키 테스트 방법

### 1. 관세청 API 테스트
```bash
# HS 코드 검색 테스트
curl "https://unipass.customs.go.kr:38010/ext/rest/hsCodeSearchService/search?key=YOUR_KEY&prdlstNm=computer"

# 관세율 조회 테스트  
curl "https://unipass.customs.go.kr:38010/ext/rest/tariffBasicService/search?key=YOUR_KEY&hsCode=8471"

# 환율 정보 테스트
curl "https://unipass.customs.go.kr:38010/ext/rest/tariffExchangeService/search?key=YOUR_KEY"
```

### 2. 애플리케이션에서 테스트
API 키가 설정되면 다음 엔드포인트로 테스트:

```bash
# 백엔드가 실행된 상태에서 (관리자 토큰 필요)
curl -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
     "http://localhost:8081/api/hscode/search?productName=computer"

curl -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
     "http://localhost:8081/api/hscode/tariff/8471"

curl -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
     "http://localhost:8081/api/hscode/exchange"
```

## 폴백 시스템

API 키가 없거나 외부 API가 실패하는 경우 시스템은 자동으로 폴백 데이터를 제공합니다:

- ✅ **HS 코드 검색**: 샘플 HS 코드 데이터 반환
- ✅ **관세율 조회**: 기본 관세율 정보 제공
- ✅ **환율 정보**: 고정 환율 데이터 사용
- ✅ **서비스 중단 방지**: 사용자 경험 유지

## 보안 고려사항

### ❌ 하지 말아야 할 것
- API 키를 코드에 하드코딩
- API 키를 Git 저장소에 커밋
- 프론트엔드에서 API 키 노출
- HTTP 요청으로 API 키 전송

### ✅ 해야 할 것
- 환경변수 또는 보안 저장소 사용
- API 키 정기적 교체
- HTTPS 통신 강제
- 액세스 로그 모니터링
- API 사용량 모니터링

## 문제 해결

### API 키가 작동하지 않는 경우
1. **키 형식 확인**: 공백이나 특수문자 포함 여부
2. **권한 확인**: 해당 API 사용 권한이 있는지 확인
3. **할당량 확인**: 일일/월간 사용 한도 초과 여부
4. **서비스 상태**: 외부 API 서비스 장애 여부

### 로그 확인
```bash
# 백엔드 로그에서 API 관련 오류 확인
grep -i "external api\|hscode\|ems" /var/log/ycs-lms/application.log

# 폴백 시스템 작동 확인
grep -i "fallback" /var/log/ycs-lms/application.log
```

## 지원 및 문의

- **관세청 API 지원**: https://unipass.customs.go.kr/
- **우정사업본부 지원**: https://openapi.epost.go.kr/
- **공공데이터포털 지원**: https://www.data.go.kr/

---

**중요**: 실제 운영 환경에 배포하기 전에 모든 API 키가 올바르게 설정되고 테스트되었는지 확인하세요.