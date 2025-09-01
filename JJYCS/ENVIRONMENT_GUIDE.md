# YSC 물류관리시스템 환경별 설정 가이드

## 환경 구성 개요

YSC 물류관리시스템은 3가지 환경으로 구분되어 운영됩니다.

### 1. 환경 구분

| 환경 | 프로파일 | 포트 | 데이터베이스 | 용도 |
|------|---------|------|------------|------|
| 개발 | `dev` | 8081 | H2 파일 | 로컬 개발 |
| 테스트 | `test` | 8081 | H2 메모리 | 단위/통합 테스트 |
| 운영 | `prod` | 8080 | MySQL 8.0 | 실제 서비스 |

## 2. 백엔드 환경설정

### 2.1 개발환경 (`application-dev.yml`)

**특징:**
- H2 파일 데이터베이스 (데이터 영구보존)
- H2 콘솔 활성화
- 상세한 디버깅 로그
- 느슨한 보안 설정
- SMTP 디버깅 활성화

**실행 방법:**
```bash
# 방법 1: 스크립트 사용
./run-dev.cmd

# 방법 2: 직접 실행  
cd backend
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# 방법 3: 환경변수 설정
set SPRING_PROFILES_ACTIVE=dev
./mvnw.cmd spring-boot:run
```

**주요 설정:**
```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:file:./data/ycs-lms-dev
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create
logging:
  level:
    com.ysc.lms: DEBUG
```

### 2.2 테스트환경 (`application-test.yml`)

**특징:**
- H2 메모리 데이터베이스 (테스트 후 정리)
- 빠른 테스트 실행
- 최소한의 로깅
- 이메일 발송 비활성화

**실행 방법:**
```bash
./mvnw.cmd test -Dspring.profiles.active=test
```

**주요 설정:**
```yaml
spring:
  profiles:
    active: test  
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
  mail:
    host: localhost  # 실제 이메일 발송 안함
```

### 2.3 운영환경 (`application-prod.yml`)

**특징:**
- MySQL 8.0 데이터베이스
- HTTPS 강제
- 강화된 보안 설정
- 모든 민감정보 환경변수 처리
- 성능 최적화

**실행 방법:**
```bash
# 방법 1: 스크립트 사용 (권장)
./run-prod.cmd

# 방법 2: JAR 파일 직접 실행
java -jar target/ycs-lms-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# 방법 3: Docker
docker run -e SPRING_PROFILES_ACTIVE=prod ycs-lms:latest
```

**주요 설정:**
```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  h2:
    console:
      enabled: false  # 보안상 비활성화
security:
  require-ssl: true
```

## 3. 프론트엔드 환경설정

### 3.1 개발환경 (`.env.development`)

**특징:**
- 로컬 백엔드 연결
- 핫 모듈 리플레이스먼트
- 개발도구 활성화
- 소스맵 생성

```env
VITE_API_BASE_URL=http://localhost:8081/api
VITE_DEBUG=true
VITE_SOURCEMAP=true
VITE_HMR=true
```

### 3.2 운영환경 (`.env.production`)

**특징:**
- 운영 백엔드 연결
- 최적화된 빌드
- 디버깅 비활성화
- PWA 활성화

```env
VITE_API_BASE_URL=https://api.yourdomain.com/api
VITE_DEBUG=false
VITE_SOURCEMAP=false
VITE_PWA_ENABLED=true
```

## 4. 환경변수 관리

### 4.1 필수 환경변수

#### 공통
- `SPRING_PROFILES_ACTIVE`: 실행 프로파일
- `JWT_SECRET_KEY`: JWT 암호화 키 (64자 이상)

#### 데이터베이스 (운영환경)
- `DB_URL`: MySQL 연결 URL
- `DB_USERNAME`: 데이터베이스 사용자
- `DB_PASSWORD`: 데이터베이스 비밀번호

#### SMTP (이메일 발송)
- `SMTP_HOST`: SMTP 서버 주소
- `SMTP_PORT`: SMTP 포트
- `SMTP_USERNAME`: SMTP 사용자명  
- `SMTP_PASSWORD`: SMTP 비밀번호

#### 외부 API
- `HSCODE_SEARCH_API_KEY`: HS코드 검색 API 키
- `HSCODE_TARIFF_API_KEY`: 관세율 조회 API 키
- `HSCODE_EXCHANGE_API_KEY`: 환율 조회 API 키

### 4.2 환경변수 설정 방법

#### Windows
```cmd
REM 개발환경
set SPRING_PROFILES_ACTIVE=dev
set JWT_SECRET_KEY=your-secret-key

REM 운영환경  
set SPRING_PROFILES_ACTIVE=prod
set DB_PASSWORD=your-secure-password
```

#### Linux/Mac
```bash
# 개발환경
export SPRING_PROFILES_ACTIVE=dev
export JWT_SECRET_KEY=your-secret-key

# 운영환경
export SPRING_PROFILES_ACTIVE=prod
export DB_PASSWORD=your-secure-password
```

#### .env 파일 사용
```bash
# .env 파일 복사
cp .env.example .env

# 실제 값으로 수정
nano .env
```

## 5. 데이터베이스 설정

### 5.1 개발환경 (H2)

**설정:**
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/ycs-lms-dev
    username: sa
    password: ""
```

**접속 정보:**
- URL: http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:file:./data/ycs-lms-dev`
- Username: `sa`
- Password: (비어있음)

### 5.2 운영환경 (MySQL)

**사전 준비:**
```sql
-- 데이터베이스 생성
CREATE DATABASE ycs_lms 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER 'ycs_user'@'%' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON ycs_lms.* TO 'ycs_user'@'%';
FLUSH PRIVILEGES;
```

**설정:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ycs_lms?useSSL=true
    username: ycs_user
    password: ${DB_PASSWORD}
```

## 6. 배포 가이드

### 6.1 개발환경 → 운영환경 체크리스트

- [ ] 환경변수 설정 완료
- [ ] MySQL 데이터베이스 준비
- [ ] SSL 인증서 설정
- [ ] 방화벽 설정 (8080 포트)
- [ ] 로그 디렉토리 생성
- [ ] 백업 정책 수립

### 6.2 배포 자동화

**Docker 배포:**
```dockerfile
FROM openjdk:17-jre-slim

COPY target/ycs-lms-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Docker Compose:**
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=jdbc:mysql://db:3306/ycs_lms
    depends_on:
      - db
  
  db:
    image: mysql:8.0
    environment:
      - MYSQL_DATABASE=ycs_lms
      - MYSQL_USER=ycs_user
      - MYSQL_PASSWORD=${DB_PASSWORD}
```

## 7. 모니터링 및 로깅

### 7.1 로그 레벨 설정

| 환경 | 애플리케이션 | Spring Security | Hibernate |
|------|------------|----------------|-----------|
| 개발 | DEBUG | INFO | WARN |
| 테스트 | INFO | WARN | WARN |
| 운영 | INFO | WARN | ERROR |

### 7.2 헬스체크

**엔드포인트:**
- 개발: http://localhost:8081/api/actuator/health
- 운영: https://api.yourdomain.com/api/actuator/health

**응답 예시:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "mail": {"status": "UP"}
  }
}
```

## 8. 트러블슈팅

### 8.1 자주 발생하는 문제

**프로파일 설정 오류:**
```
Error: The following profiles are active: none
Solution: SPRING_PROFILES_ACTIVE 환경변수 설정
```

**JWT 시크릿 오류:**
```
Error: JWT secret key is not configured
Solution: JWT_SECRET_KEY 환경변수 설정 (64자 이상)
```

**데이터베이스 연결 오류:**
```
Error: Access denied for user 'ysc_user'@'localhost'
Solution: MySQL 사용자 권한 및 비밀번호 확인
```

### 8.2 문제 진단 명령어

```bash
# 현재 프로파일 확인
java -jar app.jar --debug | grep "active profiles"

# 환경변수 확인
echo $SPRING_PROFILES_ACTIVE

# 데이터베이스 연결 테스트
mysql -h localhost -u ycs_user -p ycs_lms
```

---

**마지막 업데이트**: 2024-08-29  
**문서 버전**: 1.0  
**대상 시스템**: YSC LMS v2.0