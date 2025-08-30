# YSC LMS 개발 가이드

## 🚀 서버 실행 방법

### 개발 모드 (권장)
```bash
# 1. 백엔드 (dev 프로필)
cd backend
./mvnw.cmd spring-boot:run

# 2. 프론트엔드 (Vite 개발 서버)
cd frontend  
npm run dev
```

**접속 URL**: http://localhost:3006/login

### 프로덕션 모드 (빌드 테스트용)
```bash  
# 1. 백엔드 (prod 프로필)
cd backend
./mvnw.cmd spring-boot:run -Dspring.profiles.active=prod

# 2. 프론트엔드 빌드 + Express 서버
cd frontend
npm run build
cd ..
node server.js
```

**접속 URL**: http://localhost:3001/login

## 📋 테스트 계정

| 역할 | 이메일 | 비밀번호 | 회원코드 |
|------|-------|----------|----------|
| 관리자 | admin@ycs.com | password | ADM001 |
| 일반고객 | kimcs@email.com | password | GEN001 |
| 기업고객 | lee@company.com | password | COR001 |
| 파트너 | park@partner.com | password | PAR001 |

## 🔧 환경별 설정

### 개발 환경 (dev 프로필)
- **데이터베이스**: H2 인메모리
- **H2 콘솔**: http://localhost:8080/api/h2-console (활성화)
- **로깅**: DEBUG 레벨
- **데이터 초기화**: simple-data.sql 자동 로딩

### 프로덕션 환경 (prod 프로필)  
- **데이터베이스**: MySQL (환경변수 설정 필요)
- **H2 콘솔**: 비활성화
- **로깅**: INFO 레벨  
- **데이터 초기화**: 비활성화, Flyway 사용

## 🌐 API 엔드포인트

- **백엔드 API**: http://localhost:8080/api
- **인증**: POST /api/auth/login
- **사용자 정보**: GET /api/auth/me
- **주문**: GET/POST /api/orders

## 🎯 빠른 테스트 체크리스트

✅ **로그인 테스트**: http://localhost:3006/login → admin@ycs.com/password

✅ **API 테스트**: 
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@ycs.com","password":"password"}'
```

✅ **H2 콘솔**: http://localhost:8080/api/h2-console (dev 프로필만)