# YCS LMS 개발 환경 빠른 시작 가이드

## 🚀 빠른 시작

### 1단계: 개발 환경 설정
```batch
# 개발 환경 자동 설정 (한 번만 실행)
setup-dev-env.bat
```

### 2단계: 개발 서버 시작  
```batch
# 전체 개발 환경 시작 (백엔드 + 프론트엔드)
start-full-dev.bat

# 또는 개별 실행
start-backend.bat
start-frontend.bat
```

## 📋 필수 요구사항

- **Node.js**: 18+ LTS
- **Java**: JDK 17+
- **Docker Desktop**: 권장 (DB/Redis 자동 설정)

## 🌐 개발 서버 URL

- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080  
- **API 문서**: http://localhost:8080/swagger-ui.html
- **MySQL**: localhost:3306 (root/root123)
- **Redis**: localhost:6379

## 📁 프로젝트 구조

```
YCS VER.2/
├── backend/          # Spring Boot 3 백엔드
├── frontend/         # Vue 3 + TypeScript 프론트엔드  
├── docs/            # 프로젝트 문서
├── ops/             # 운영/모니터링 설정
└── docker-compose.yml # Docker 인프라 설정
```

## 🔧 개발 명령어

### 백엔드 (Spring Boot)
```batch
cd backend
mvnw.cmd spring-boot:run    # 개발 서버 시작
mvnw.cmd test              # 테스트 실행
mvnw.cmd clean compile     # 컴파일
```

### 프론트엔드 (Vue 3)
```batch
cd frontend  
npm run dev      # 개발 서버 시작
npm run build    # 프로덕션 빌드
npm run lint     # 코드 검사
```

### Docker (인프라)
```batch
docker compose up -d        # 전체 인프라 시작
docker compose up -d mysql  # MySQL만 시작
docker compose down         # 인프라 종료
```

## 📚 주요 문서

- **CLAUDE.md**: 프로젝트 전체 가이드 및 비즈니스 룰
- **docs/PLAN.md**: 상세 개발 계획서
- **docs/API.md**: API 명세서  
- **docs/DB_SCHEMA.sql**: 데이터베이스 스키마
- **docs/DEV_SETUP_PLAN.md**: 상세 개발 환경 구축 가이드

## 🎯 핵심 비즈니스 기능

1. **CBM 29 초과 시 자동 항공 전환**
2. **THB 1,500 초과 시 수취인 추가 정보 경고**  
3. **회원코드 미기재 시 지연 처리**
4. **EMS/HS 코드 검증**
5. **QR/라벨 스캔 시스템**
6. **일괄 입출고 처리**

## 🔄 개발 워크플로우

1. 기능 개발 → 테스트 작성
2. 백엔드 API 완성 → 프론트엔드 연동
3. 비즈니스 룰 검증
4. 모바일 UI/UX 최적화
5. E2E 테스트

## ❓ 문제 해결

### Maven/Java 이슈
- JDK 17+ 설치 확인: `java -version`
- `JAVA_HOME` 환경변수 설정

### Node.js/NPM 이슈  
- Node.js 18+ 설치: `node --version`
- 의존성 재설치: `cd frontend && rm -rf node_modules && npm install`

### Docker 이슈
- Docker Desktop 실행 확인
- 포트 충돌 확인: `netstat -an | findstr :3306`

## 📞 지원

개발 중 문제가 발생하면:
1. 문서 확인: `docs/` 폴더
2. 로그 확인: 백엔드/프론트엔드 콘솔
3. GitHub Issues 등록

---

**Happy Coding! 🎉**