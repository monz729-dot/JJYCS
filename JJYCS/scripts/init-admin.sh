#!/bin/bash

# YCS 물류관리시스템 관리자 계정 초기화 스크립트
# Usage: ./init-admin.sh [environment]

set -e

ENVIRONMENT=${1:-dev}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "=== YCS LMS 관리자 계정 초기화 ==="
echo "환경: $ENVIRONMENT"
echo "프로젝트 루트: $PROJECT_ROOT"
echo ""

# 환경별 설정
case $ENVIRONMENT in
  "dev"|"development")
    ADMIN_EMAIL=${ADMIN_EMAIL:-"admin@ycs.com"}
    ADMIN_PASSWORD=${ADMIN_PASSWORD:-"password"}
    ADMIN_NAME=${ADMIN_NAME:-"Administrator"}
    ADMIN_PHONE=${ADMIN_PHONE:-"010-0000-0000"}
    ADMIN_MEMBER_CODE=${ADMIN_MEMBER_CODE:-"ADM001"}
    ;;
  "prod"|"production")
    ADMIN_EMAIL=${ADMIN_EMAIL:-"admin@ycs.com"}
    ADMIN_PASSWORD=${ADMIN_PASSWORD:-"YCS-Admin-2024!"}
    ADMIN_NAME=${ADMIN_NAME:-"System Administrator"}
    ADMIN_PHONE=${ADMIN_PHONE:-"010-0000-0000"}
    ADMIN_MEMBER_CODE=${ADMIN_MEMBER_CODE:-"ADM001"}
    ;;
  *)
    echo "❌ 지원하지 않는 환경입니다: $ENVIRONMENT"
    echo "사용 가능한 환경: dev, development, prod, production"
    exit 1
    ;;
esac

# 설정 확인
echo "관리자 계정 설정:"
echo "  - 이메일: $ADMIN_EMAIL"
echo "  - 이름: $ADMIN_NAME"
echo "  - 전화번호: $ADMIN_PHONE"
echo "  - 멤버코드: $ADMIN_MEMBER_CODE"
echo ""

# 비밀번호 보안 경고
if [ "$ENVIRONMENT" = "prod" ] || [ "$ENVIRONMENT" = "production" ]; then
  if [ "$ADMIN_PASSWORD" = "YCS-Admin-2024!" ]; then
    echo "⚠️  경고: 프로덕션 환경에서 기본 비밀번호를 사용하고 있습니다!"
    echo "   ADMIN_PASSWORD 환경변수를 설정하여 보안이 강화된 비밀번호를 사용하세요."
    echo ""
    read -p "계속하시겠습니까? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
      echo "초기화를 취소했습니다."
      exit 1
    fi
  fi
fi

# 백엔드 서버 실행 확인
echo "백엔드 서버 연결 확인..."
BACKEND_URL="http://localhost:8080/api"

if ! curl -s -f "$BACKEND_URL/actuator/health" > /dev/null 2>&1; then
  echo "❌ 백엔드 서버가 실행되고 있지 않습니다."
  echo "   다음 명령어로 백엔드를 실행하세요:"
  echo "   cd backend && ./mvnw spring-boot:run"
  exit 1
fi

echo "✅ 백엔드 서버가 실행 중입니다."

# 기존 관리자 계정 확인
echo ""
echo "기존 관리자 계정 확인 중..."

LOGIN_RESPONSE=$(curl -s -X POST "$BACKEND_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" \
  2>/dev/null || echo '{"success":false}')

if echo "$LOGIN_RESPONSE" | grep -q '"success":true'; then
  echo "✅ 관리자 계정이 이미 존재하고 로그인 가능합니다."
  
  # JWT 토큰 추출
  ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
  
  # 사용자 정보 확인
  USER_INFO=$(echo "$LOGIN_RESPONSE" | grep -o '"user":{[^}]*}')
  echo "계정 정보: $USER_INFO"
  
else
  echo "⚠️  관리자 계정이 존재하지 않거나 로그인할 수 없습니다."
  
  # 회원가입 시도
  echo "새로운 관리자 계정을 생성합니다..."
  
  SIGNUP_RESPONSE=$(curl -s -X POST "$BACKEND_URL/auth/signup" \
    -H "Content-Type: application/json" \
    -d "{
      \"email\":\"$ADMIN_EMAIL\",
      \"password\":\"$ADMIN_PASSWORD\",
      \"name\":\"$ADMIN_NAME\",
      \"phone\":\"$ADMIN_PHONE\",
      \"userType\":\"ADMIN\"
    }" 2>/dev/null || echo '{"success":false}')
  
  if echo "$SIGNUP_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 새로운 관리자 계정이 생성되었습니다."
    
    # 다시 로그인 시도
    echo "생성된 계정으로 로그인을 시도합니다..."
    LOGIN_RESPONSE=$(curl -s -X POST "$BACKEND_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" \
      2>/dev/null || echo '{"success":false}')
    
    if echo "$LOGIN_RESPONSE" | grep -q '"success":true'; then
      echo "✅ 관리자 계정 로그인 성공!"
    else
      echo "❌ 관리자 계정은 생성되었지만 로그인에 실패했습니다."
      echo "   이메일 인증이 필요할 수 있습니다."
    fi
    
  else
    echo "❌ 관리자 계정 생성에 실패했습니다."
    echo "응답: $SIGNUP_RESPONSE"
    exit 1
  fi
fi

echo ""
echo "=== 관리자 계정 초기화 완료 ==="
echo "로그인 정보:"
echo "  - URL: http://localhost:3001/login"
echo "  - 이메일: $ADMIN_EMAIL"
echo "  - 비밀번호: $ADMIN_PASSWORD"
echo ""

if [ "$ENVIRONMENT" = "prod" ] || [ "$ENVIRONMENT" = "production" ]; then
  echo "🔒 프로덕션 환경 보안 체크리스트:"
  echo "  [ ] 관리자 비밀번호를 강력한 비밀번호로 변경"
  echo "  [ ] 2단계 인증(2FA) 활성화"
  echo "  [ ] 관리자 계정의 IP 접근 제한 설정"
  echo "  [ ] 정기적인 비밀번호 변경 정책 적용"
  echo ""
fi

echo "관리자 대시보드: http://localhost:3001/admin"
echo "H2 데이터베이스 콘솔: http://localhost:8080/api/h2-console"