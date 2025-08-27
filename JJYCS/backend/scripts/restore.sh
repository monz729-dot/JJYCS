#!/bin/bash

# YCS LMS 데이터베이스 복구 스크립트
# 사용법: ./restore.sh <backup_file> [database_name]

set -e

# 매개변수 확인
if [ $# -lt 1 ]; then
    echo "사용법: $0 <backup_file> [database_name]"
    echo "예제: $0 ./backups/ycs_lms_backup_20250823_121500.sql.gz ycs_lms"
    exit 1
fi

BACKUP_FILE="$1"
DB_NAME="${2:-ycs_lms}"

# 환경변수에서 데이터베이스 연결 정보 가져오기
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-ycs_user}"
DB_PASSWORD="${DB_PASSWORD}"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${RED}⚠️  경고: 이 작업은 기존 데이터베이스를 완전히 덮어씁니다! ⚠️${NC}"
echo -e "${BLUE}백업 파일: ${BACKUP_FILE}${NC}"
echo -e "${BLUE}데이터베이스: ${DB_NAME}${NC}"
echo ""

# 백업 파일 존재 확인
if [ ! -f "${BACKUP_FILE}" ]; then
    echo -e "${RED}❌ 백업 파일을 찾을 수 없습니다: ${BACKUP_FILE}${NC}"
    exit 1
fi

# 백업 파일 무결성 검사
echo -e "${YELLOW}백업 파일 무결성 검사 중...${NC}"
if ! gzip -t "${BACKUP_FILE}" 2>/dev/null; then
    echo -e "${RED}❌ 백업 파일이 손상되었습니다${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 백업 파일 무결성 검사 통과${NC}"

# MySQL 연결 테스트
echo -e "${YELLOW}데이터베이스 연결 테스트 중...${NC}"
if ! mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" -e "SELECT 1" 2>/dev/null; then
    echo -e "${RED}❌ 데이터베이스 연결 실패${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 데이터베이스 연결 성공${NC}"

# 현재 데이터베이스 백업 (선택사항)
CURRENT_BACKUP_DIR="./emergency_backups"
CURRENT_BACKUP_FILE="${CURRENT_BACKUP_DIR}/emergency_backup_$(date +%Y%m%d_%H%M%S).sql.gz"

echo ""
echo -e "${YELLOW}현재 데이터베이스를 응급 백업하시겠습니까? (권장) [y/N]${NC}"
read -r EMERGENCY_BACKUP

if [[ "${EMERGENCY_BACKUP}" =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}응급 백업 생성 중...${NC}"
    mkdir -p "${CURRENT_BACKUP_DIR}"
    
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
        2>/dev/null | gzip > "${CURRENT_BACKUP_FILE}"; then
        
        backup_size=$(du -h "${CURRENT_BACKUP_FILE}" | cut -f1)
        echo -e "${GREEN}✅ 응급 백업 완료: ${CURRENT_BACKUP_FILE} (${backup_size})${NC}"
        chmod 600 "${CURRENT_BACKUP_FILE}"
    else
        echo -e "${RED}❌ 응급 백업 실패${NC}"
        exit 1
    fi
fi

# 최종 확인
echo ""
echo -e "${RED}정말로 복구를 진행하시겠습니까? 이 작업은 되돌릴 수 없습니다! [y/N]${NC}"
read -r CONFIRM

if [[ ! "${CONFIRM}" =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}복구 작업이 취소되었습니다${NC}"
    exit 0
fi

echo -e "${GREEN}=== YCS LMS 데이터베이스 복구 시작 ===${NC}"
echo "시작 시간: $(date)"

# 복구 실행
echo -e "${YELLOW}데이터베이스 복구 진행 중...${NC}"
start_time=$(date +%s)

if zcat "${BACKUP_FILE}" | mysql \
    --host="${DB_HOST}" \
    --port="${DB_PORT}" \
    --user="${DB_USER}" \
    --password="${DB_PASSWORD}" \
    2>/dev/null; then
    
    end_time=$(date +%s)
    duration=$((end_time - start_time))
    
    echo -e "${GREEN}✅ 데이터베이스 복구 완료${NC}"
    echo "소요 시간: ${duration}초"
    
else
    echo -e "${RED}❌ 데이터베이스 복구 실패${NC}"
    
    if [ -f "${CURRENT_BACKUP_FILE}" ]; then
        echo ""
        echo -e "${YELLOW}응급 백업으로 롤백하시겠습니까? [y/N]${NC}"
        read -r ROLLBACK
        
        if [[ "${ROLLBACK}" =~ ^[Yy]$ ]]; then
            echo -e "${YELLOW}롤백 진행 중...${NC}"
            if zcat "${CURRENT_BACKUP_FILE}" | mysql \
                --host="${DB_HOST}" \
                --port="${DB_PORT}" \
                --user="${DB_USER}" \
                --password="${DB_PASSWORD}"; then
                echo -e "${GREEN}✅ 롤백 완료${NC}"
            else
                echo -e "${RED}❌ 롤백 실패${NC}"
            fi
        fi
    fi
    
    exit 1
fi

# 복구 후 검증
echo -e "${YELLOW}복구 결과 검증 중...${NC}"

# 테이블 개수 확인
table_count=$(mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" \
    -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}'" \
    -s -N 2>/dev/null || echo "0")

if [ "${table_count}" -gt 0 ]; then
    echo -e "${GREEN}✅ 복구 검증 통과: ${table_count}개 테이블 확인${NC}"
else
    echo -e "${RED}❌ 복구 검증 실패: 테이블이 없습니다${NC}"
    exit 1
fi

echo -e "${GREEN}=== 복구 완료 ===${NC}"
echo "종료 시간: $(date)"
echo ""
echo -e "${YELLOW}⚠️  다음 단계:${NC}"
echo "1. 애플리케이션 재시작"
echo "2. 기능 테스트 수행"
echo "3. 로그 모니터링"

if [ -f "${CURRENT_BACKUP_FILE}" ]; then
    echo ""
    echo -e "${BLUE}응급 백업 파일이 보존됨: ${CURRENT_BACKUP_FILE}${NC}"
    echo "필요시 이 파일로 다시 롤백할 수 있습니다."
fi