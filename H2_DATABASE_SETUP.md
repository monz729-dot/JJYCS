# H2 Database 로컬 설치 및 설정 가이드

## 목차
1. [H2 Database 소개](#1-h2-database-소개)
2. [H2 Database 설치](#2-h2-database-설치)
3. [H2 Database 설정](#3-h2-database-설정)
4. [데이터베이스 초기화](#4-데이터베이스-초기화)
5. [Spring Boot 연동](#5-spring-boot-연동)
6. [H2 Console 사용법](#6-h2-console-사용법)
7. [문제 해결](#7-문제-해결)

---

## 1. H2 Database 소개

H2는 Java로 작성된 경량 인메모리 관계형 데이터베이스입니다.
- **특징**: 빠른 속도, 작은 용량 (약 2.5MB JAR 파일)
- **모드**: 임베디드 모드, 서버 모드, 인메모리 모드 지원
- **호환성**: MySQL, PostgreSQL 호환 모드 지원

---

## 2. H2 Database 설치

### 방법 1: 수동 설치 (권장)

1. **H2 Database 다운로드**
   ```bash
   # H2 공식 웹사이트 방문
   https://www.h2database.com/html/download.html
   
   # 또는 직접 다운로드 (2024년 최신 버전)
   https://github.com/h2database/h2database/releases/download/version-2.2.224/h2-2024-08-11.zip
   ```

2. **압축 해제**
   ```bash
   # 다운로드한 파일을 원하는 위치에 압축 해제
   # 예시: C:\h2 또는 ~/h2
   unzip h2-2024-08-11.zip -d C:\h2
   ```

3. **환경 변수 설정 (선택사항)**
   ```bash
   # Windows
   setx H2_HOME "C:\h2"
   setx PATH "%PATH%;%H2_HOME%\bin"
   
   # macOS/Linux
   export H2_HOME=~/h2
   export PATH=$PATH:$H2_HOME/bin
   ```

### 방법 2: Maven 의존성 (Spring Boot 프로젝트용)

```xml
<!-- pom.xml에 이미 추가되어 있음 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## 3. H2 Database 설정

### 3.1 서버 모드 실행

```batch
:: Windows - start-h2-server.bat
@echo off
echo Starting H2 Database Server...
cd /d C:\h2\bin
java -cp h2*.jar org.h2.tools.Server -tcp -tcpAllowOthers -tcpPort 9092 -baseDir C:\h2\data
pause
```

```bash
# macOS/Linux - start-h2-server.sh
#!/bin/bash
echo "Starting H2 Database Server..."
cd ~/h2/bin
java -cp h2*.jar org.h2.tools.Server -tcp -tcpAllowOthers -tcpPort 9092 -baseDir ~/h2/data
```

### 3.2 H2 Console 실행

```batch
:: Windows - start-h2-console.bat
@echo off
echo Starting H2 Console...
cd /d C:\h2\bin
java -jar h2*.jar
pause
```

```bash
# macOS/Linux - start-h2-console.sh
#!/bin/bash
echo "Starting H2 Console..."
cd ~/h2/bin
java -jar h2*.jar
```

### 3.3 데이터베이스 연결 설정

H2 Console 실행 후 브라우저에서 자동으로 열립니다 (http://localhost:8082)

**연결 정보:**
- **JDBC URL**: 
  - 서버 모드: `jdbc:h2:tcp://localhost:9092/~/ycs`
  - 인메모리: `jdbc:h2:mem:testdb`
  - 파일: `jdbc:h2:file:~/ycs`
- **User Name**: `sa`
- **Password**: `ycs` (또는 비워둠)

---

## 4. 데이터베이스 초기화

### 4.1 스키마 생성

1. H2 Console에 접속
2. 아래 SQL 파일을 순서대로 실행:

```sql
-- 1단계: 스키마 생성 (database/h2-schema.sql 파일 내용 실행)
-- H2 Console에서 파일 열기 또는 복사-붙여넣기

-- 2단계: 초기 데이터 삽입 (database/h2-init-data.sql 파일 내용 실행)
-- H2 Console에서 파일 열기 또는 복사-붙여넣기
```

### 4.2 자동 초기화 스크립트

```batch
:: Windows - init-h2-database.bat
@echo off
echo Initializing H2 Database...
set H2_JAR=C:\h2\bin\h2-2.2.224.jar
set DB_URL=jdbc:h2:tcp://localhost:9092/~/ycs
set DB_USER=sa
set DB_PASS=ycs

echo Creating schema...
java -cp %H2_JAR% org.h2.tools.RunScript -url %DB_URL% -user %DB_USER% -password %DB_PASS% -script database\h2-schema.sql

echo Loading initial data...
java -cp %H2_JAR% org.h2.tools.RunScript -url %DB_URL% -user %DB_USER% -password %DB_PASS% -script database\h2-init-data.sql

echo Database initialization complete!
pause
```

---

## 5. Spring Boot 연동

### 5.1 application-local.yml 설정 확인

```yaml
spring:
  datasource:
    # 인메모리 모드 (개발용)
    url: jdbc:h2:mem:testdb
    # 또는 서버 모드 (데이터 유지)
    # url: jdbc:h2:tcp://localhost:9092/~/ycs
    username: sa
    password: ycs
    driver-class-name: org.h2.Driver
    
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: true
```

### 5.2 Spring Boot 애플리케이션 실행

```batch
:: backend 디렉토리에서 실행
cd backend
mvnw spring-boot:run -Dspring.profiles.active=local
```

---

## 6. H2 Console 사용법

### 6.1 접속 방법

1. **Spring Boot 내장 Console**: http://localhost:8080/api/h2-console
2. **독립 실행 Console**: http://localhost:8082

### 6.2 주요 기능

- **SQL 실행**: SQL 명령어 직접 실행
- **테이블 조회**: 좌측 트리에서 테이블 구조 확인
- **데이터 편집**: 테이블 데이터 직접 수정
- **백업/복원**: 데이터베이스 백업 및 복원

### 6.3 유용한 SQL 명령어

```sql
-- 모든 테이블 조회
SHOW TABLES;

-- 테이블 구조 확인
SHOW COLUMNS FROM users;

-- 데이터 조회
SELECT * FROM users LIMIT 10;

-- 테이블 크기 확인
SELECT 
    TABLE_NAME,
    ROW_COUNT_ESTIMATE 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'PUBLIC';

-- 현재 연결 정보
SELECT * FROM INFORMATION_SCHEMA.SESSIONS;
```

---

## 7. 문제 해결

### 문제 1: H2 Console 접속 불가
```yaml
# application-local.yml에 추가
spring:
  h2:
    console:
      settings:
        web-allow-others: true
        trace: true
```

### 문제 2: 데이터베이스 파일 찾을 수 없음
```batch
:: 데이터베이스 파일 위치 확인
dir %USERPROFILE%\*.db
dir %USERPROFILE%\ycs.*
```

### 문제 3: 포트 충돌
```batch
:: 사용 중인 포트 확인 (Windows)
netstat -ano | findstr :9092
netstat -ano | findstr :8082

:: 프로세스 종료
taskkill /PID [프로세스ID] /F
```

### 문제 4: 메모리 부족
```batch
:: JVM 메모리 증가
java -Xmx1024m -cp h2*.jar org.h2.tools.Server
```

---

## 테스트 계정 정보

초기 데이터에 포함된 테스트 계정:

| 역할 | 이메일 | 비밀번호 | 회원코드 |
|------|--------|----------|----------|
| 관리자 | admin@ycs.com | password123 | ADM001 |
| 개인회원 | user1@example.com | password123 | IND001 |
| 기업회원 | company1@example.com | password123 | ENT001 |
| 파트너 | partner1@example.com | password123 | PRT001 |
| 창고직원 | warehouse1@ycs.com | password123 | WHS001 |

---

## 다음 단계

1. H2 Database 서버 시작
2. 데이터베이스 초기화 (스키마 + 데이터)
3. Spring Boot 애플리케이션 실행
4. H2 Console에서 데이터 확인
5. API 테스트 (Postman 또는 프론트엔드)

---

## 참고 자료

- [H2 Database 공식 문서](http://www.h2database.com/html/main.html)
- [H2 Database Tutorial](http://www.h2database.com/html/tutorial.html)
- [Spring Boot H2 Database](https://www.baeldung.com/spring-boot-h2-database)
