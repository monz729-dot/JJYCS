# YCS LMS 백업 및 복구 절차

## 개요

YCS LMS 시스템의 데이터베이스 백업과 복구를 위한 절차서입니다.

## 자동 백업

### 스케줄링
- **주기**: 매일 새벽 2시 (cron: `0 0 2 * * ?`)
- **백업 위치**: `./backups` 디렉터리
- **파일명 형식**: `ycs_lms_backup_yyyyMMdd_HHmmss.sql.gz`
- **보관 기간**: 30일 (설정 변경 가능)

### 백업 내용
- 모든 데이터베이스 테이블
- 스토어드 프로시저 및 함수
- 트리거
- 데이터베이스 구조 및 데이터

## 수동 백업

### API를 통한 백업
```bash
# 수동 백업 실행
POST /api/admin/backup/database
Authorization: Bearer {admin_token}

# 응답
{
  "success": true,
  "message": "데이터베이스 백업이 생성되었습니다.",
  "backupPath": "./backups/ycs_lms_backup_20250823_121500.sql.gz"
}
```

### 명령줄을 통한 백업
```bash
# mysqldump를 사용한 직접 백업
mysqldump --host=localhost --port=3306 \
  --user=ycs_user --password=password \
  --single-transaction --routines --triggers \
  --add-drop-database --databases ycs_lms \
  | gzip > ycs_lms_backup_$(date +%Y%m%d_%H%M%S).sql.gz
```

## 백업 검증

### API를 통한 검증
```bash
# 백업 파일 무결성 검사
GET /api/admin/backup/database/verify?backupFilePath=/path/to/backup.sql.gz
Authorization: Bearer {admin_token}

# 응답
{
  "success": true,
  "valid": true,
  "message": "백업 파일이 유효합니다."
}
```

### 수동 검증
```bash
# 압축 파일 무결성 확인
gzip -t ycs_lms_backup_20250823_121500.sql.gz

# 백업 내용 확인 (일부만)
zcat ycs_lms_backup_20250823_121500.sql.gz | head -20
```

## 복구 절차

### ⚠️ 주의사항
- **복구는 모든 기존 데이터를 삭제합니다**
- 복구 전에 현재 데이터베이스를 백업하세요
- 서비스 중단 시간을 고려하세요
- 복구 후 애플리케이션 재시작이 필요할 수 있습니다

### API를 통한 복구
```bash
# 데이터베이스 복구
POST /api/admin/backup/database/restore
Authorization: Bearer {admin_token}
Content-Type: application/x-www-form-urlencoded

backupFilePath=/path/to/backup.sql.gz

# 응답
{
  "success": true,
  "message": "데이터베이스 복원이 완료되었습니다."
}
```

### 명령줄을 통한 복구
```bash
# 1. 애플리케이션 중지
sudo systemctl stop ycs-lms

# 2. 현재 데이터베이스 백업 (선택사항)
mysqldump --host=localhost --port=3306 \
  --user=ycs_user --password=password \
  --single-transaction ycs_lms \
  | gzip > current_backup_$(date +%Y%m%d_%H%M%S).sql.gz

# 3. 백업에서 복구
zcat ycs_lms_backup_20250823_121500.sql.gz | \
  mysql --host=localhost --port=3306 \
  --user=ycs_user --password=password

# 4. 애플리케이션 시작
sudo systemctl start ycs-lms
```

## 연결 테스트

```bash
# 데이터베이스 연결 상태 확인
GET /api/admin/backup/database/test-connection
Authorization: Bearer {admin_token}

# 응답
{
  "success": true,
  "message": "데이터베이스 연결이 정상입니다."
}
```

## 백업 모니터링

### 로그 확인
```bash
# 백업 관련 로그
tail -f logs/ycs-lms.log | grep -i backup

# 스케줄러 로그
tail -f logs/ycs-lms.log | grep -i scheduled
```

### 감사 로그
- 모든 백업/복구 작업은 `audit_logs` 테이블에 기록됩니다
- `action_type`: `DATA_EXPORT` (백업), `DATA_IMPORT` (복구)
- `performed_by`: `SYSTEM` (자동) 또는 관리자 사용자명

## 재해 복구 시나리오

### 시나리오 1: 데이터 손상
1. 서비스 중지
2. 손상된 데이터베이스 확인
3. 최신 백업 파일 선택
4. 백업 무결성 검증
5. 데이터베이스 복구
6. 서비스 재시작
7. 기능 테스트

### 시나리오 2: 서버 장애
1. 새 서버 준비
2. YCS LMS 애플리케이션 설치
3. MySQL 설치 및 설정
4. 백업 파일 복사
5. 데이터베이스 복구
6. 설정 파일 복원
7. 서비스 시작
8. DNS 전환

### 시나리오 3: 실수로 데이터 삭제
1. 즉시 서비스 중지 (추가 손상 방지)
2. 삭제 시점 이전의 백업 파일 식별
3. 백업 검증
4. 별도 환경에서 복구 테스트
5. 운영 환경 복구
6. 데이터 검증

## 설정

### application-prod.yml
```yaml
backup:
  directory: ./backups
  retention:
    days: 30
  mysql:
    bin-path: /usr/bin
```

### 환경 변수
```bash
# 백업 디렉터리
BACKUP_DIRECTORY=./backups

# 보관 기간 (일)
BACKUP_RETENTION_DAYS=30

# MySQL 도구 경로
MYSQL_BIN_PATH=/usr/bin
```

## 최적화 팁

### 백업 성능
- `--single-transaction`: InnoDB 테이블의 일관성 보장
- `--quick`: 메모리 사용량 최소화
- `--lock-tables=false`: MyISAM 테이블 락 방지
- 압축 사용: 저장 공간 절약

### 복구 성능
- `mysql --quick`: 빠른 복구
- 인덱스 재생성: 복구 후 OPTIMIZE TABLE 실행
- 임시 디스크 공간: 백업 파일 크기의 2배 이상 확보

### 보안
- 백업 파일 암호화 고려
- 접근 권한 제한 (600)
- 백업 파일 원격 저장소 복사
- 백업 무결성 정기 검사

## 장애 대응

### 백업 실패 시
1. 디스크 공간 확인
2. MySQL 서비스 상태 확인
3. 권한 확인
4. 로그 분석
5. 수동 백업 시도

### 복구 실패 시
1. 백업 파일 무결성 검사
2. MySQL 에러 로그 확인
3. 디스크 공간 확인
4. 권한 확인
5. 별도 환경에서 테스트

## 정기 점검

### 월간
- 백업 파일 무결성 검사
- 복구 테스트 (개발 환경)
- 백업 용량 모니터링
- 보관 정책 검토

### 분기별
- 재해 복구 훈련
- 백업 전략 검토
- 성능 최적화
- 문서 업데이트