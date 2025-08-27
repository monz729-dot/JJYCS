#!/bin/bash

# YCS LMS 데이터베이스 백업 스크립트
# 사용법: ./backup.sh [database_name] [backup_directory]

set -e

# 기본값 설정
DB_NAME="${1:-ycs_lms}"
BACKUP_DIR="${2:-./backups}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/ycs_lms_backup_${TIMESTAMP}.sql.gz"

# 환경변수에서 데이터베이스 연결 정보 가져오기
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-ycs_user}"
DB_PASSWORD="${DB_PASSWORD}"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== YCS LMS 데이터베이스 백업 시작 ===${NC}"
echo "데이터베이스: ${DB_NAME}"
echo "백업 위치: ${BACKUP_FILE}"
echo "시작 시간: $(date)"

# 백업 디렉터리 생성
mkdir -p "${BACKUP_DIR}"

# MySQL 연결 테스트
echo -e "${YELLOW}데이터베이스 연결 테스트 중...${NC}"
if ! mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" -e "SELECT 1" "${DB_NAME}" >/dev/null 2>&1; then
    echo -e "${RED}❌ 데이터베이스 연결 실패${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 데이터베이스 연결 성공${NC}"

# 백업 실행
echo -e "${YELLOW}백업 진행 중...${NC}"
start_time=$(date +%s)

if mysqldump \
    --host="${DB_HOST}" \
    --port="${DB_PORT}" \
    --user="${DB_USER}" \
    --password="${DB_PASSWORD}" \
    --single-transaction \
    --routines \
    --triggers \
    --add-drop-database \
    --databases "${DB_NAME}" \
    --verbose \
    2>/dev/null | gzip > "${BACKUP_FILE}"; then
    
    end_time=$(date +%s)
    duration=$((end_time - start_time))
    backup_size=$(du -h "${BACKUP_FILE}" | cut -f1)
    
    echo -e "${GREEN}✅ 백업 완료${NC}"
    echo "백업 파일: ${BACKUP_FILE}"
    echo "파일 크기: ${backup_size}"
    echo "소요 시간: ${duration}초"
    
    # 백업 파일 무결성 검사
    echo -e "${YELLOW}백업 파일 무결성 검사 중...${NC}"
    if gzip -t "${BACKUP_FILE}" 2>/dev/null; then
        echo -e "${GREEN}✅ 백업 파일 무결성 검사 통과${NC}"
    else
        echo -e "${RED}❌ 백업 파일 손상됨${NC}"
        exit 1
    fi
    
else
    echo -e "${RED}❌ 백업 실패${NC}"
    exit 1
fi

# 권한 설정 (보안)
chmod 600 "${BACKUP_FILE}"

# 오래된 백업 파일 정리 (30일 이상)
echo -e "${YELLOW}오래된 백업 파일 정리 중...${NC}"
find "${BACKUP_DIR}" -name "ycs_lms_backup_*.sql.gz" -mtime +30 -type f -print0 | \
while IFS= read -r -d '' file; do
    echo "삭제: $(basename "$file")"
    rm -f "$file"
done

echo -e "${GREEN}=== 백업 완료 ===${NC}"
echo "종료 시간: $(date)"
echo ""
echo "복구 명령어:"
echo "zcat ${BACKUP_FILE} | mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD}"