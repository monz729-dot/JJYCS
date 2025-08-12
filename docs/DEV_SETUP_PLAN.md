# YSC LMS 로컬 개발 환경 구축 계획

## 개요
YSC LMS 프로젝트의 로컬 개발 환경을 구축하여 개발자가 즉시 개발을 시작할 수 있도록 하는 완전한 설정 가이드입니다.

---

## 1. 개발 환경 요구사항

### 필수 소프트웨어
- **Node.js**: 18.x LTS 이상
- **Java**: JDK 17 
- **MySQL**: 8.0 이상
- **Redis**: 7.x 이상
- **Git**: 2.x 이상
- **Docker**: 20.10 이상 (선택사항)
- **VS Code**: 최신버전 (권장 IDE)

### 운영체제 지원
- Windows 10/11 (WSL2 권장)
- macOS 12+ (Intel/Apple Silicon)
- Ubuntu 20.04+ LTS
- Docker Desktop (크로스 플랫폼)

---

## 2. 로컬 개발 환경 구축 순서

### Phase 1: 기본 인프라 설정 (30분)
```bash
# 1. 저장소 클론 및 기본 설정
git clone https://github.com/ysc/lms.git
cd lms
cp .env.example .env

# 2. Docker로 개발 인프라 실행
docker-compose -f docker-compose.dev.yml up -d

# 3. 데이터베이스 초기화
./scripts/init-dev-db.sh
```

### Phase 2: 백엔드 개발 환경 (20분)
```bash
# 1. Java 프로젝트 빌드
cd backend
./gradlew build -x test

# 2. 개발 서버 실행
./gradlew bootRun --args='--spring.profiles.active=dev'

# 3. API 헬스 체크
curl http://localhost:8080/actuator/health
```

### Phase 3: 프론트엔드 개발 환경 (15분)
```bash
# 1. 의존성 설치
cd frontend  
npm ci

# 2. 개발 서버 실행
npm run dev

# 3. 브라우저 확인
open http://localhost:3000
```

### Phase 4: 개발 도구 설정 (10분)
```bash
# 1. VS Code 확장 설치
./scripts/setup-vscode.sh

# 2. Git hooks 설정  
./scripts/setup-git-hooks.sh

# 3. 개발용 테스트 데이터 생성
npm run seed:dev
```

---

## 3. 디렉터리 구조 및 설정 파일

### 프로젝트 루트 구조
```
ysc-lms/
├── docker-compose.dev.yml          # 개발용 Docker 설정
├── docker-compose.prod.yml         # 운영용 Docker 설정  
├── .env.example                    # 환경변수 템플릿
├── .env.local                      # 로컬 개발용 환경변수
├── .gitignore                      # Git 제외 파일
├── README.md                       # 프로젝트 개요
├── CONTRIBUTING.md                 # 기여 가이드
├── /scripts/                       # 개발/배포 스크립트
│   ├── init-dev-db.sh             # 개발 DB 초기화
│   ├── setup-vscode.sh            # VS Code 설정
│   ├── setup-git-hooks.sh         # Git hooks 설정
│   ├── build.sh                   # 전체 빌드
│   ├── test.sh                    # 전체 테스트
│   └── deploy-dev.sh              # 개발 서버 배포
├── /docs/                         # 문서
├── /backend/                      # Spring Boot 백엔드
└── /frontend/                     # Vue.js 프론트엔드
```

### 백엔드 구조 (Spring Boot)
```
backend/
├── build.gradle                   # Gradle 빌드 설정
├── src/main/java/com/ysc/lms/
│   ├── LmsApplication.java        # 메인 애플리케이션
│   ├── config/                    # 설정 클래스
│   │   ├── DatabaseConfig.java
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java
│   │   └── SwaggerConfig.java
│   ├── controller/                # REST 컨트롤러
│   ├── service/                   # 비즈니스 로직
│   ├── repository/                # 데이터 액세스
│   ├── entity/                    # JPA 엔티티
│   ├── dto/                       # 데이터 전송 객체
│   └── util/                      # 유틸리티
├── src/main/resources/
│   ├── application.yml            # 기본 설정
│   ├── application-dev.yml        # 개발 환경 설정
│   ├── application-prod.yml       # 운영 환경 설정
│   ├── db/migration/              # Flyway 마이그레이션
│   └── static/                    # 정적 리소스
└── src/test/                      # 테스트 코드
```

### 프론트엔드 구조 (Vue.js)
```
frontend/
├── package.json                   # NPM 패키지 설정
├── vite.config.ts                 # Vite 빌드 설정
├── tsconfig.json                  # TypeScript 설정
├── tailwind.config.js             # Tailwind CSS 설정
├── .eslintrc.js                   # ESLint 설정
├── .prettierrc                    # Prettier 설정
├── public/                        # 공용 정적 파일
│   ├── manifest.json              # PWA 매니페스트
│   └── sw.js                      # 서비스 워커
├── src/
│   ├── main.ts                    # 앱 진입점
│   ├── App.vue                    # 루트 컴포넌트
│   ├── router/                    # 라우팅
│   ├── stores/                    # 상태 관리 (Pinia)
│   ├── composables/               # Vue 3 컴포저블
│   ├── components/                # 재사용 컴포넌트
│   ├── modules/                   # 기능별 모듈
│   ├── layouts/                   # 레이아웃 컴포넌트
│   ├── views/                     # 페이지 컴포넌트
│   ├── assets/                    # 정적 자원
│   ├── styles/                    # CSS/SCSS
│   ├── utils/                     # 유틸리티
│   ├── types/                     # TypeScript 타입
│   └── i18n/                      # 다국어 리소스
└── tests/                         # 테스트 (Vitest, Cypress)
```

---

## 4. Docker 개발 환경

### docker-compose.dev.yml
```yaml
version: '3.8'
services:
  # MySQL 데이터베이스
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: ysc_lms_dev
      MYSQL_USER: ysc_dev
      MYSQL_PASSWORD: ysc_dev_pass
      MYSQL_ROOT_PASSWORD: root_pass
    ports:
      - "3307:3306"
    volumes:
      - mysql_dev_data:/var/lib/mysql
      - ./backend/src/main/resources/db/init:/docker-entrypoint-initdb.d
    command: --default-authentication-plugin=mysql_native_password

  # Redis 캐시
  redis:
    image: redis:7-alpine
    ports:
      - "6380:6379"
    volumes:
      - redis_dev_data:/data
    command: redis-server --appendonly yes

  # Kafka (이벤트 스트리밍)
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9093:9093"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  # 메일서버 (개발용 MailHog)
  mailhog:
    image: mailhog/mailhog:latest
    ports:
      - "1025:1025"  # SMTP
      - "8025:8025"  # 웹 UI

  # Prometheus (모니터링)
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./ops/prometheus.yml:/etc/prometheus/prometheus.yml

  # Grafana (대시보드)
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3001:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - grafana_dev_data:/var/lib/grafana

volumes:
  mysql_dev_data:
  redis_dev_data:
  grafana_dev_data:
```

---

## 5. 환경 설정 파일

### .env.example (템플릿)
```bash
# =================================
# 개발 환경 설정
# =================================

# 애플리케이션
NODE_ENV=development
VITE_APP_TITLE=YSC LMS (Dev)
VITE_APP_VERSION=1.0.0-dev

# API 엔드포인트
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws

# 데이터베이스
DB_HOST=localhost
DB_PORT=3307
DB_NAME=ysc_lms_dev
DB_USER=ysc_dev
DB_PASSWORD=ysc_dev_pass
DB_URL=mysql://${DB_USER}:${DB_PASSWORD}@${DB_HOST}:${DB_PORT}/${DB_NAME}

# Redis
REDIS_HOST=localhost
REDIS_PORT=6380
REDIS_URL=redis://${REDIS_HOST}:${REDIS_PORT}

# Kafka
KAFKA_BROKERS=localhost:9093

# SMTP (개발용 MailHog)
SMTP_HOST=localhost
SMTP_PORT=1025
SMTP_USER=
SMTP_PASS=
SMTP_FROM=noreply@ysc-lms.dev

# JWT 토큰
JWT_SECRET=dev_jwt_secret_key_change_in_production
JWT_EXPIRES_IN=1d
REFRESH_TOKEN_EXPIRES_IN=7d

# 외부 API (개발용 Mock)
EMS_API_URL=http://localhost:8080/api/mock/ems
EMS_API_KEY=dev_ems_key
HS_API_URL=http://localhost:8080/api/mock/hs  
HS_API_KEY=dev_hs_key
NAVER_RATE_API_URL=http://localhost:8080/api/mock/exchange
NAVER_RATE_API_KEY=dev_rate_key

# 파일 업로드
UPLOAD_DIR=./uploads
MAX_FILE_SIZE=10MB
ALLOWED_FILE_TYPES=jpg,jpeg,png,pdf,doc,docx

# 보안
BCRYPT_ROUNDS=10
ENCRYPTION_KEY=dev_encryption_key_32_chars_long
RECAPTCHA_SITE_KEY=dev_recaptcha_site_key
RECAPTCHA_SECRET_KEY=dev_recaptcha_secret_key

# 비즈니스 룰
CBM_THRESHOLD=29.0
THB_AMOUNT_THRESHOLD=1500.0
AUTO_AIR_CONVERSION=true
REQUIRE_MEMBER_CODE=true

# 모니터링
ENABLE_METRICS=true
PROMETHEUS_PORT=9090

# 로깅
LOG_LEVEL=debug
LOG_FORMAT=dev

# 개발 기능 플래그
ENABLE_DEV_ROUTES=true
ENABLE_SWAGGER=true
ENABLE_HOT_RELOAD=true
ENABLE_MOCK_APIs=true
```

### backend/src/main/resources/application-dev.yml
```yaml
# 개발 환경 Spring Boot 설정
server:
  port: 8080
  servlet:
    context-path: /
  error:
    include-stacktrace: always

spring:
  profiles:
    active: dev
  
  # 데이터베이스 설정
  datasource:
    url: jdbc:mysql://localhost:3307/ysc_lms_dev?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
    username: ysc_dev
    password: ysc_dev_pass
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      connection-timeout: 20000
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      leak-detection-threshold: 60000

  # JPA 설정
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway가 스키마 관리
    show-sql: true
    format-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true

  # Flyway 마이그레이션
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true

  # Redis 설정
  redis:
    host: localhost
    port: 6380
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

  # Kafka 설정
  kafka:
    bootstrap-servers: localhost:9093
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: ysc-lms-dev
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer

  # 메일 설정 (MailHog)
  mail:
    host: localhost
    port: 1025
    username:
    password:
    properties:
      mail:
        smtp:
          auth: false
          starttls:
            enable: false

  # 파일 업로드
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

# 액추에이터 (모니터링)
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

# 스웨거 문서화
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
  packages-to-scan: com.ysc.lms.controller

# 로깅 설정
logging:
  level:
    com.ysc.lms: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/ysc-lms-dev.log

# 애플리케이션 설정
app:
  name: YSC LMS
  version: 1.0.0-dev
  timezone: Asia/Seoul
  jwt:
    secret: dev_jwt_secret_key_change_in_production
    expires-in: 86400  # 1 day
  upload:
    path: ./uploads
    max-size: 10485760  # 10MB
  business-rules:
    cbm-threshold: 29.0
    thb-amount-threshold: 1500.0
    auto-air-conversion: true
    require-member-code: true
  external-apis:
    ems:
      url: http://localhost:8080/api/mock/ems
      key: dev_ems_key
    hs:
      url: http://localhost:8080/api/mock/hs
      key: dev_hs_key
  dev:
    enable-mock-apis: true
    enable-test-data: true
    cors-origins: http://localhost:3000,http://localhost:5173
```

---

## 6. 개발 스크립트

### scripts/init-dev-db.sh
```bash
#!/bin/bash
set -e

echo "🚀 YSC LMS 개발 데이터베이스 초기화 시작..."

# 환경변수 로드
if [ -f .env ]; then
    export $(cat .env | grep -v ^# | xargs)
fi

# MySQL 컨테이너 시작 대기
echo "⏳ MySQL 컨테이너 시작 대기..."
until docker-compose -f docker-compose.dev.yml exec mysql mysqladmin ping -h"localhost" --silent; do
    echo "MySQL 연결 대기 중..."
    sleep 2
done

echo "✅ MySQL 연결 확인됨"

# Flyway 마이그레이션 실행
echo "🔄 데이터베이스 마이그레이션 실행..."
cd backend
./gradlew flywayMigrate -Pflyway.url=jdbc:mysql://localhost:3307/ysc_lms_dev -Pflyway.user=ysc_dev -Pflyway.password=ysc_dev_pass

# 개발용 시드 데이터 삽입
echo "🌱 시드 데이터 삽입..."
./gradlew bootRun --args='--spring.profiles.active=dev --app.dev.seed-data=true' &
BACKEND_PID=$!

# 백엔드 시작 대기
sleep 10

# 시드 데이터 API 호출
curl -X POST http://localhost:8080/api/dev/seed-data -H "Content-Type: application/json"

# 백엔드 프로세스 종료
kill $BACKEND_PID

echo "✅ 개발 데이터베이스 초기화 완료!"
echo "📊 접속 정보:"
echo "   - MySQL: localhost:3307"
echo "   - 사용자: ysc_dev"
echo "   - 비밀번호: ysc_dev_pass"
echo "   - 데이터베이스: ysc_lms_dev"
```

### scripts/setup-vscode.sh
```bash
#!/bin/bash
set -e

echo "🔧 VS Code 개발 환경 설정..."

# .vscode 디렉터리 생성
mkdir -p .vscode

# 확장 프로그램 설정
cat > .vscode/extensions.json << 'EOF'
{
  "recommendations": [
    "vue.volar",
    "vue.vscode-typescript-vue-plugin",
    "bradlc.vscode-tailwindcss",
    "esbenp.prettier-vscode",
    "dbaeumer.vscode-eslint",
    "ms-vscode.vscode-typescript-next",
    "formulahendry.auto-rename-tag",
    "christian-kohler.path-intellisense",
    "ms-vscode.vscode-json",
    "redhat.java",
    "vscjava.vscode-java-pack",
    "vscjava.vscode-spring-boot-dashboard",
    "pivotal.vscode-spring-boot",
    "ms-vscode.rest-client",
    "humao.rest-client"
  ]
}
EOF

# 작업영역 설정
cat > .vscode/settings.json << 'EOF'
{
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "typescript.preferences.importModuleSpecifier": "relative",
  "vue.inlayHints.missingProps": true,
  "vue.inlayHints.optionsWrapper": true,
  "tailwindCSS.includeLanguages": {
    "vue": "html"
  },
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.format.settings.url": ".vscode/java-google-style.xml",
  "spring-boot.ls.problem.application-properties.UNKNOWN_PROPERTY": "WARNING",
  "files.associations": {
    "*.vue": "vue"
  },
  "emmet.includeLanguages": {
    "vue": "html"
  }
}
EOF

# 디버깅 설정
cat > .vscode/launch.json << 'EOF'
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Spring Boot",
      "type": "java",
      "request": "launch",
      "mainClass": "com.ysc.lms.LmsApplication",
      "projectName": "lms-backend",
      "args": "--spring.profiles.active=dev",
      "env": {
        "SPRING_PROFILES_ACTIVE": "dev"
      }
    },
    {
      "name": "Vue.js App",
      "type": "node",
      "request": "launch",
      "program": "${workspaceFolder}/frontend/node_modules/.bin/vite",
      "args": ["--mode", "development"],
      "cwd": "${workspaceFolder}/frontend",
      "env": {
        "NODE_ENV": "development"
      }
    }
  ]
}
EOF

# 태스크 설정
cat > .vscode/tasks.json << 'EOF'
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Backend: Run Dev",
      "type": "shell",
      "command": "./gradlew",
      "args": ["bootRun", "--args='--spring.profiles.active=dev'"],
      "group": {
        "kind": "build",
        "isDefault": true
      },
      "options": {
        "cwd": "${workspaceFolder}/backend"
      },
      "presentation": {
        "echo": true,
        "reveal": "always",
        "panel": "new"
      }
    },
    {
      "label": "Frontend: Run Dev",
      "type": "shell",
      "command": "npm",
      "args": ["run", "dev"],
      "group": "build",
      "options": {
        "cwd": "${workspaceFolder}/frontend"
      },
      "presentation": {
        "echo": true,
        "reveal": "always",
        "panel": "new"
      }
    },
    {
      "label": "Docker: Start Dev",
      "type": "shell",
      "command": "docker-compose",
      "args": ["-f", "docker-compose.dev.yml", "up", "-d"],
      "group": "build",
      "presentation": {
        "echo": true,
        "reveal": "always",
        "panel": "new"
      }
    }
  ]
}
EOF

# Java 코드 스타일 (Google Style)
curl -o .vscode/java-google-style.xml https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml

echo "✅ VS Code 설정 완료!"
echo "💡 권장 확장 프로그램을 설치하려면 Ctrl+Shift+P → 'Extensions: Show Recommended Extensions' 실행"
```

### scripts/setup-git-hooks.sh
```bash
#!/bin/bash
set -e

echo "🪝 Git hooks 설정..."

# .githooks 디렉터리 생성
mkdir -p .githooks

# Pre-commit hook (코드 품질 검사)
cat > .githooks/pre-commit << 'EOF'
#!/bin/bash
set -e

echo "🔍 Pre-commit 품질 검사 실행..."

# 백엔드 검사
if [ -d "backend" ] && git diff --cached --name-only | grep -E "\.(java)$" > /dev/null; then
    echo "☕ 백엔드 코드 검사..."
    cd backend
    
    # 컴파일 검사
    ./gradlew compileJava
    
    # 테스트 실행
    ./gradlew test
    
    # Checkstyle 검사
    ./gradlew checkstyleMain
    
    cd ..
fi

# 프론트엔드 검사  
if [ -d "frontend" ] && git diff --cached --name-only | grep -E "\.(ts|vue|js)$" > /dev/null; then
    echo "🎨 프론트엔드 코드 검사..."
    cd frontend
    
    # 의존성 확인
    if [ ! -d "node_modules" ]; then
        npm ci
    fi
    
    # ESLint 검사
    npm run lint:check
    
    # TypeScript 타입 검사
    npm run type-check
    
    # 단위 테스트
    npm run test:unit
    
    cd ..
fi

echo "✅ Pre-commit 검사 통과!"
EOF

# Pre-push hook (추가 검증)
cat > .githooks/pre-push << 'EOF'
#!/bin/bash
set -e

echo "🚀 Pre-push 검증 실행..."

# 통합 테스트 실행
if [ -f "scripts/test.sh" ]; then
    echo "🧪 통합 테스트 실행..."
    ./scripts/test.sh
fi

# 보안 스캔 (선택사항)
if command -v git-secrets &> /dev/null; then
    echo "🔒 보안 스캔 실행..."
    git secrets --scan
fi

echo "✅ Pre-push 검증 통과!"
EOF

# Commit message hook (커밋 메시지 형식 검증)
cat > .githooks/commit-msg << 'EOF'
#!/bin/bash

# 커밋 메시지 형식: type(scope): description
# 예: feat(orders): add CBM calculation

commit_regex='^(feat|fix|docs|style|refactor|test|chore|perf)(\(.+\))?: .{1,50}'

if ! grep -qE "$commit_regex" "$1"; then
    echo "❌ 올바르지 않은 커밋 메시지 형식입니다!"
    echo "형식: type(scope): description"
    echo "타입: feat, fix, docs, style, refactor, test, chore, perf"
    echo "예시: feat(orders): add CBM calculation"
    exit 1
fi
EOF

# 실행 권한 부여
chmod +x .githooks/*

# Git hooks 경로 설정
git config core.hooksPath .githooks

echo "✅ Git hooks 설정 완료!"
echo "📋 설정된 hooks:"
echo "   - pre-commit: 코드 품질 검사"
echo "   - pre-push: 통합 테스트"  
echo "   - commit-msg: 커밋 메시지 형식 검증"
```

---

## 7. 개발용 Mock API 구현

### Mock API 컨트롤러 (개발환경 전용)
```java
@RestController
@RequestMapping("/api/mock")
@Profile("dev")
@Slf4j
public class MockApiController {

    @GetMapping("/ems/validate/{code}")
    public ResponseEntity<Map<String, Object>> validateEMS(@PathVariable String code) {
        // EMS 코드 검증 Mock
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("status", "active");
        response.put("destination", "Thailand");
        response.put("estimatedDelivery", "2024-02-01");
        
        // 일부러 지연 시뮬레이션
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hs/lookup/{code}")
    public ResponseEntity<Map<String, Object>> lookupHS(@PathVariable String code) {
        // HS 코드 조회 Mock
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("valid", true);
        response.put("description", "기타 화장품 제조용품");
        response.put("category", "cosmetics");
        
        List<Map<String, Object>> restrictions = new ArrayList<>();
        Map<String, Object> restriction = new HashMap<>();
        restriction.put("country", "TH");
        restriction.put("type", "quantity_limit");
        restriction.put("value", "개인용 소량");
        restrictions.add(restriction);
        
        response.put("restrictions", restrictions);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exchange")
    public ResponseEntity<Map<String, Object>> getExchangeRate(
            @RequestParam String from, @RequestParam String to) {
        // 환율 조회 Mock
        Map<String, Object> response = new HashMap<>();
        response.put("from", from);
        response.put("to", to);
        response.put("rate", 0.024); // KRW to THB
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
```

---

## 8. 개발 데이터 시딩

### 시드 데이터 생성 스크립트
```java
@Component
@Profile("dev")
@Slf4j
public class DevDataSeeder {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void seedDevData() {
        if (Boolean.parseBoolean(System.getProperty("app.dev.seed-data", "false"))) {
            log.info("🌱 개발용 시드 데이터 생성 시작...");
            
            createDevUsers();
            createDevOrders();
            createDevWarehouseData();
            
            log.info("✅ 시드 데이터 생성 완료");
        }
    }

    private void createDevUsers() {
        // 관리자 계정
        createUser("admin@ysc-lms.com", "admin123!", "시스템 관리자", 
                  UserRole.ADMIN, UserStatus.ACTIVE, "ADMIN001");
        
        // 개인 사용자
        createUser("user@example.com", "user123!", "홍길동", 
                  UserRole.INDIVIDUAL, UserStatus.ACTIVE, "IND001");
        
        // 기업 사용자 (승인 대기)
        createUser("company@example.com", "company123!", "주식회사 테스트", 
                  UserRole.ENTERPRISE, UserStatus.PENDING_APPROVAL, null);
        
        // 파트너 사용자
        createUser("partner@example.com", "partner123!", "파트너 홍길동", 
                  UserRole.PARTNER, UserStatus.ACTIVE, "PART001");
        
        // 창고 사용자
        createUser("warehouse@example.com", "warehouse123!", "창고 관리자", 
                  UserRole.WAREHOUSE, UserStatus.ACTIVE, "WH001");
    }

    private void createUser(String email, String password, String name, 
                          UserRole role, UserStatus status, String memberCode) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .name(name)
                    .role(role)
                    .status(status)
                    .memberCode(memberCode)
                    .emailVerified(true)
                    .agreeTerms(true)
                    .agreePrivacy(true)
                    .build();
            
            userRepository.save(user);
            log.info("👤 사용자 생성: {} ({})", name, email);
        }
    }

    private void createDevOrders() {
        User user = userRepository.findByEmail("user@example.com").orElse(null);
        if (user != null && orderRepository.count() == 0) {
            // 다양한 상태의 주문 생성
            createOrder(user, "requested", "해상", 15.5);
            createOrder(user, "confirmed", "항공", 35.2);
            createOrder(user, "shipped", "해상", 8.7);
            
            log.info("📦 개발용 주문 데이터 생성 완료");
        }
    }

    private void createOrder(User user, String status, String type, double cbm) {
        Order order = Order.builder()
                .orderCode(generateOrderCode())
                .user(user)
                .status(status)
                .orderType(type)
                .recipientName("김수취")
                .recipientPhone("010-9876-5432")
                .recipientAddress("태국 방콕 수쿰빗로...")
                .recipientCountry("TH")
                .totalAmount(new BigDecimal("2500.00"))
                .currency("THB")
                .build();
        
        orderRepository.save(order);
    }
}
```

---

## 9. 핫 리로드 개발 환경

### 백엔드 핫 리로드 (Spring Boot DevTools)
```gradle
// backend/build.gradle
dependencies {
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}
```

### 프론트엔드 핫 리로드 (Vite)
```typescript
// frontend/vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: true,
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      }
    },
    hmr: {
      overlay: true
    }
  },
  css: {
    devSourcemap: true
  }
})
```

---

## 10. 개발자 가이드라인

### 코딩 컨벤션
- **Java**: Google Java Style Guide
- **TypeScript/Vue**: ESLint + Prettier
- **커밋 메시지**: Conventional Commits
- **브랜치명**: feature/기능명, fix/버그명, docs/문서명

### 개발 워크플로우
1. `git checkout -b feature/new-feature`
2. 코드 작성 및 테스트
3. `git commit -m "feat(module): description"`
4. `git push origin feature/new-feature`
5. Pull Request 생성
6. 코드 리뷰 후 머지

### 디버깅 도구
- **백엔드**: IntelliJ IDEA Remote Debug, Spring Boot Actuator
- **프론트엔드**: Vue.js DevTools, Browser DevTools
- **API**: Swagger UI (http://localhost:8080/swagger-ui.html)
- **DB**: MySQL Workbench, phpMyAdmin
- **메일**: MailHog UI (http://localhost:8025)

### 성능 모니터링
- **메트릭**: Prometheus (http://localhost:9090)
- **대시보드**: Grafana (http://localhost:3001)
- **로그**: 파일 기반 로깅 + 콘솔 출력

---

## 11. 트러블슈팅

### 자주 발생하는 문제들

#### 포트 충돌
```bash
# 포트 사용 확인
netstat -tulpn | grep :3000
lsof -i :8080

# 프로세스 종료
kill -9 $(lsof -t -i:8080)
```

#### Docker 컨테이너 이슈
```bash
# 컨테이너 재시작
docker-compose -f docker-compose.dev.yml restart

# 볼륨 초기화
docker-compose -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.dev.yml up -d
```

#### 데이터베이스 연결 실패
```bash
# MySQL 컨테이너 로그 확인
docker-compose -f docker-compose.dev.yml logs mysql

# 데이터베이스 재초기화
./scripts/init-dev-db.sh
```

#### 의존성 이슈
```bash
# 백엔드 의존성 재설치
cd backend && ./gradlew clean build

# 프론트엔드 의존성 재설치  
cd frontend && rm -rf node_modules package-lock.json && npm install
```

---

## 12. 완료 체크리스트

### 환경 설정 완료 확인
- [ ] Docker Desktop 설치 및 실행
- [ ] Node.js 18+ 설치 확인
- [ ] Java 17+ 설치 확인  
- [ ] Git 설정 완료
- [ ] VS Code + 권장 확장 설치

### 로컬 서비스 실행 확인
- [ ] MySQL 연결: `localhost:3307`
- [ ] Redis 연결: `localhost:6380`  
- [ ] 백엔드 API: `http://localhost:8080/actuator/health`
- [ ] 프론트엔드: `http://localhost:3000`
- [ ] Swagger UI: `http://localhost:8080/swagger-ui.html`
- [ ] MailHog: `http://localhost:8025`

### 기능 테스트 확인  
- [ ] 사용자 회원가입/로그인
- [ ] 주문 생성 및 CBM 계산
- [ ] QR 스캔 (모바일/카메라 권한)
- [ ] 창고 재고 관리
- [ ] 이메일 발송 (MailHog)
- [ ] API Mock 응답

### 개발 도구 확인
- [ ] 코드 자동 포맷팅
- [ ] ESLint 검사 통과
- [ ] 단위 테스트 실행
- [ ] Hot reload 동작
- [ ] Git hooks 동작

---

## 총 소요 시간 예상

- **🔧 환경 설정**: 30분 (Docker, 의존성 설치)
- **🗄️ DB 초기화**: 10분 (마이그레이션, 시드 데이터)  
- **⚙️ 백엔드 설정**: 20분 (빌드, 실행, 테스트)
- **🎨 프론트엔드 설정**: 15분 (의존성, 실행, 확인)
- **🛠️ 개발도구 설정**: 10분 (VS Code, Git hooks)

**총 예상 시간: 약 1시간 30분**

로컬 개발 환경이 완전히 구축되면 실제 기능 개발을 즉시 시작할 수 있습니다!