# YSC 물류관리시스템 배포 가이드

## 데이터베이스 설정

### 개발환경 (현재 상태)
- **데이터베이스**: H2 파일 기반 (./data/ycs-lms-dev.mv.db)
- **포트**: 8081
- **H2 콘솔**: http://localhost:8081/h2-console
- **JDBC URL**: `jdbc:h2:file:./data/ycs-lms-dev`
- **사용자명**: sa
- **패스워드**: (없음)

### 운영환경 MySQL 설정

#### 1. MySQL 데이터베이스 생성
```sql
CREATE DATABASE ycs_lms 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'ycs_user'@'%' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON ycs_lms.* TO 'ycs_user'@'%';
FLUSH PRIVILEGES;
```

#### 2. 환경변수 설정
`.env` 파일을 생성하고 다음과 같이 설정:

```bash
# 데이터베이스 설정
DB_URL=jdbc:mysql://localhost:3306/ycs_lms?useSSL=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=ycs_user
DB_PASSWORD=your_secure_password

# JWT 보안 설정
JWT_SECRET_KEY=your-64-character-minimum-secret-key-for-production-security-here

# SMTP 설정
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_gmail_app_password

# 이메일 발송 설정
NOTIFICATION_FROM_EMAIL=noreply@yourcompany.com

# 프론트엔드 URL (CORS)
FRONTEND_URL=https://yourdomain.com
ADMIN_FRONTEND_URL=https://admin.yourdomain.com

# API 키 설정
HSCODE_SEARCH_API_KEY=your_api_key
HSCODE_TARIFF_API_KEY=your_api_key
HSCODE_EXCHANGE_API_KEY=your_api_key
```

#### 3. 프로덕션 모드로 실행
```bash
# 환경 설정
export SPRING_PROFILES_ACTIVE=prod

# 애플리케이션 실행
java -jar backend/target/ycs-lms-0.0.1-SNAPSHOT.jar
```

## 배포 체크리스트

### 보안 설정
- [ ] JWT Secret Key 환경변수로 설정
- [ ] 데이터베이스 패스워드 환경변수로 설정
- [ ] SMTP 인증정보 환경변수로 설정
- [ ] H2 Console 비활성화 (prod 프로파일)
- [ ] CORS Origin 운영 도메인으로 제한
- [ ] HTTPS 설정

### 데이터베이스
- [ ] MySQL 8.0+ 설치 및 설정
- [ ] 데이터베이스 및 사용자 계정 생성
- [ ] Flyway 마이그레이션 파일 준비
- [ ] 연결 테스트

### 외부 API
- [ ] 관세청 Open API 키 발급 및 설정
- [ ] 공공데이터포털 EMS API 키 발급 및 설정
- [ ] API 연결 테스트

### 서버 설정
- [ ] Java 17 설치
- [ ] 서버 방화벽 설정 (8080 포트)
- [ ] SSL 인증서 설정
- [ ] 로그 로테이션 설정

### 모니터링
- [ ] 헬스체크 엔드포인트 설정
- [ ] 로그 모니터링 설정
- [ ] 성능 모니터링 도구 연동

## 주요 변경사항

### 개발 → 운영 전환 시
1. **데이터베이스**: H2 File → MySQL
2. **보안**: 하드코딩된 값 → 환경변수
3. **CORS**: 모든 Origin 허용 → 특정 도메인만 허용
4. **로깅**: DEBUG → INFO 레벨
5. **에러 처리**: 상세 스택트레이스 → 일반적인 에러 메시지

### 데이터 마이그레이션
H2에서 MySQL로 데이터 마이그레이션이 필요한 경우:
```sql
-- H2에서 데이터 익스포트
SCRIPT TO 'backup.sql';

-- MySQL에서 스키마 생성 후 데이터 임포트
-- (테이블 구조 조정 필요할 수 있음)
```

## 문제 해결

### 자주 발생하는 문제
1. **데이터베이스 연결 오류**: 환경변수 설정 확인
2. **JWT 토큰 오류**: Secret Key 설정 확인
3. **CORS 오류**: 허용된 Origin 설정 확인
4. **이메일 발송 실패**: SMTP 설정 및 App Password 확인

### 로그 확인
```bash
# 애플리케이션 로그
tail -f logs/application.log

# 시스템 로그
journalctl -u ycs-lms -f
```

## 성능 최적화

### MySQL 설정
```sql
-- my.cnf 권장 설정
[mysqld]
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
max_connections = 200
query_cache_size = 32M
```

### JVM 옵션
```bash
java -Xms1g -Xmx2g -XX:+UseG1GC \
     -Dspring.profiles.active=prod \
     -jar ycs-lms.jar
```

## 백업 정책
- **일일 백업**: 데이터베이스 자동 백업
- **주간 백업**: 전체 시스템 백업
- **업로드 파일**: 별도 스토리지에 백업

---

**마지막 업데이트**: 2024-08-29  
**문서 버전**: 1.0