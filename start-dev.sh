#!/bin/bash
# YCS LMS 개발 환경 실행 스크립트

echo "🚀 YCS LMS 개발 환경 시작"
echo "============================="

# 현재 디렉토리 확인
if [ ! -f "backend/pom.xml" ] && [ ! -f "frontend/package.json" ]; then
    echo "❌ 프로젝트 루트 디렉토리에서 실행해주세요"
    exit 1
fi

# 함수 정의
start_backend() {
    echo "📦 백엔드 서버 시작 중..."
    cd backend
    
    # Maven Wrapper 실행 권한 확인
    if [ ! -x "./mvnw" ]; then
        chmod +x ./mvnw
    fi
    
    # 백엔드 빌드 및 실행
    echo "🔨 Maven 빌드 중..."
    ./mvnw clean compile
    
    if [ $? -eq 0 ]; then
        echo "✅ 빌드 성공"
        echo "🌟 Spring Boot 애플리케이션 시작 중..."
        ./mvnw spring-boot:run -Dspring-boot.run.profiles=local &
        BACKEND_PID=$!
        echo "📝 백엔드 PID: $BACKEND_PID"
        cd ..
    else
        echo "❌ 백엔드 빌드 실패"
        cd ..
        exit 1
    fi
}

start_frontend() {
    echo "🎨 프론트엔드 서버 시작 중..."
    cd frontend
    
    # Node.js 모듈 설치 확인
    if [ ! -d "node_modules" ]; then
        echo "📦 npm 패키지 설치 중..."
        npm install
    fi
    
    # 프론트엔드 개발 서버 실행
    echo "🌟 Vite 개발 서버 시작 중..."
    npm run dev &
    FRONTEND_PID=$!
    echo "📝 프론트엔드 PID: $FRONTEND_PID"
    cd ..
}

check_ports() {
    echo "🔍 포트 사용 현황 확인..."
    
    # 백엔드 포트 (8080) 확인
    if lsof -i:8080 >/dev/null 2>&1; then
        echo "⚠️  포트 8080이 이미 사용 중입니다"
        echo "   기존 프로세스를 종료하거나 다른 포트를 사용하세요"
        lsof -i:8080
    fi
    
    # 프론트엔드 포트 (5173) 확인
    if lsof -i:5173 >/dev/null 2>&1; then
        echo "⚠️  포트 5173이 이미 사용 중입니다"
        lsof -i:5173
    fi
}

show_info() {
    echo ""
    echo "🎉 YCS LMS 개발 환경이 시작되었습니다!"
    echo "====================================="
    echo "📱 프론트엔드: http://localhost:5173"
    echo "🔧 백엔드 API: http://localhost:8080"
    echo "📚 API 문서: http://localhost:8080/swagger-ui.html"
    echo ""
    echo "📋 테스트 계정:"
    echo "   관리자: admin@ycs.com / password123"
    echo "   창고: warehouse@ycs.com / password123"
    echo "   개인: user1@example.com / password123"
    echo "   기업: company@corp.com / password123"
    echo ""
    echo "⏹️  종료하려면 Ctrl+C를 누르거나 ./stop-dev.sh를 실행하세요"
}

wait_for_services() {
    echo "⏳ 서비스 시작 대기 중..."
    
    # 백엔드 시작 대기 (최대 60초)
    echo "🔍 백엔드 서버 확인 중..."
    for i in {1..60}; do
        if curl -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
            echo "✅ 백엔드 서버 준비 완료"
            break
        fi
        if [ $i -eq 60 ]; then
            echo "❌ 백엔드 서버 시작 실패 (60초 타임아웃)"
            exit 1
        fi
        sleep 1
        echo -n "."
    done
    
    # 프론트엔드 시작 대기 (최대 30초)
    echo "🔍 프론트엔드 서버 확인 중..."
    for i in {1..30}; do
        if curl -s http://localhost:5173 >/dev/null 2>&1; then
            echo "✅ 프론트엔드 서버 준비 완료"
            break
        fi
        if [ $i -eq 30 ]; then
            echo "❌ 프론트엔드 서버 시작 실패 (30초 타임아웃)"
            # 프론트엔드는 실패해도 계속 진행
            break
        fi
        sleep 1
        echo -n "."
    done
}

cleanup() {
    echo ""
    echo "🛑 서비스 종료 중..."
    
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null
        echo "✅ 백엔드 서버 종료"
    fi
    
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null
        echo "✅ 프론트엔드 서버 종료"
    fi
    
    # 포트를 사용하는 프로세스 강제 종료
    pkill -f "spring-boot:run" 2>/dev/null
    pkill -f "vite" 2>/dev/null
    
    echo "👋 YCS LMS 개발 환경이 종료되었습니다"
    exit 0
}

# SIGINT (Ctrl+C) 핸들러 등록
trap cleanup SIGINT

# 메인 실행 로직
main() {
    case "${1:-start}" in
        "start")
            check_ports
            start_backend
            sleep 5  # 백엔드가 완전히 시작할 때까지 대기
            start_frontend
            wait_for_services
            show_info
            
            # 사용자 입력 대기 (서비스 유지)
            echo "Press any key to stop services..."
            read -n 1
            cleanup
            ;;
        "backend")
            check_ports
            start_backend
            wait_for_services
            echo "✅ 백엔드만 실행 중 - http://localhost:8080"
            echo "Press any key to stop..."
            read -n 1
            cleanup
            ;;
        "frontend")
            start_frontend
            echo "✅ 프론트엔드만 실행 중 - http://localhost:5173"
            echo "Press any key to stop..."
            read -n 1
            cleanup
            ;;
        "stop")
            pkill -f "spring-boot:run" 2>/dev/null
            pkill -f "vite" 2>/dev/null
            echo "✅ 모든 서비스가 종료되었습니다"
            ;;
        "status")
            echo "🔍 서비스 상태 확인:"
            if lsof -i:8080 >/dev/null 2>&1; then
                echo "✅ 백엔드: 실행 중 (포트 8080)"
            else
                echo "❌ 백엔드: 중지됨"
            fi
            
            if lsof -i:5173 >/dev/null 2>&1; then
                echo "✅ 프론트엔드: 실행 중 (포트 5173)"
            else
                echo "❌ 프론트엔드: 중지됨"
            fi
            ;;
        "help"|"-h"|"--help")
            echo "YCS LMS 개발 환경 스크립트"
            echo ""
            echo "사용법:"
            echo "  ./start-dev.sh [명령어]"
            echo ""
            echo "명령어:"
            echo "  start     - 백엔드와 프론트엔드 모두 시작 (기본값)"
            echo "  backend   - 백엔드만 시작"
            echo "  frontend  - 프론트엔드만 시작"
            echo "  stop      - 모든 서비스 종료"
            echo "  status    - 서비스 상태 확인"
            echo "  help      - 도움말 표시"
            echo ""
            echo "예시:"
            echo "  ./start-dev.sh"
            echo "  ./start-dev.sh backend"
            echo "  ./start-dev.sh stop"
            ;;
        *)
            echo "❌ 알 수 없는 명령어: $1"
            echo "사용 가능한 명령어: start, backend, frontend, stop, status, help"
            exit 1
            ;;
    esac
}

# 스크립트 실행
main "$@"