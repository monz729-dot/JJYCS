const sqlite3 = require('sqlite3').verbose();
const fs = require('fs');
const path = require('path');

// 데이터베이스 파일 경로
const dbPath = path.join(__dirname, 'database', 'ycs_lms.db');
const schemaPath = path.join(__dirname, 'database', 'schema.sql');
const dataPath = path.join(__dirname, 'database', 'dummy_data.sql');

// 디렉토리 생성
const dbDir = path.dirname(dbPath);
if (!fs.existsSync(dbDir)) {
    fs.mkdirSync(dbDir, { recursive: true });
}

// 기존 데이터베이스 파일 삭제 (새로 생성)
if (fs.existsSync(dbPath)) {
    fs.unlinkSync(dbPath);
    console.log('🗑️  기존 데이터베이스 파일 삭제');
}

// 데이터베이스 생성
const db = new sqlite3.Database(dbPath, (err) => {
    if (err) {
        console.error('❌ 데이터베이스 생성 실패:', err.message);
        return;
    }
    console.log('✅ SQLite 데이터베이스 생성 완료:', dbPath);
});

// 스키마 실행 함수 (순차 실행)
function executeSQL(filePath, description) {
    return new Promise((resolve, reject) => {
        const sql = fs.readFileSync(filePath, 'utf8');
        
        // SQL 명령들을 분리하여 실행
        const statements = sql.split(';').filter(stmt => stmt.trim().length > 0);
        
        let index = 0;
        
        function executeNext() {
            if (index >= statements.length) {
                console.log(`✅ ${description} 완료 (${statements.length}개 문장 실행)`);
                resolve();
                return;
            }
            
            const statement = statements[index].trim();
            if (statement) {
                db.run(statement + ';', function(err) {
                    if (err) {
                        console.error(`❌ ${description} 실행 중 오류 (문장 ${index + 1}):`, err.message);
                        console.error('문제 SQL:', statement.substring(0, 100) + '...');
                        reject(err);
                        return;
                    }
                    
                    index++;
                    executeNext();
                });
            } else {
                index++;
                executeNext();
            }
        }
        
        executeNext();
    });
}

// 데이터 확인 함수
function verifyData() {
    return new Promise((resolve, reject) => {
        console.log('\n📊 데이터 검증 시작...');
        
        const queries = [
            { sql: "SELECT user_type, COUNT(*) as count FROM users GROUP BY user_type", desc: "사용자 유형별 통계" },
            { sql: "SELECT status, COUNT(*) as count FROM orders GROUP BY status", desc: "주문 상태별 통계" },
            { sql: "SELECT payment_status, COUNT(*) as count FROM payments GROUP BY payment_status", desc: "결제 상태별 통계" },
            { sql: "SELECT status, COUNT(*) as count FROM inventory GROUP BY status", desc: "재고 상태별 통계" },
            { sql: "SELECT SUM(amount) as total_revenue FROM payments WHERE payment_status = 'completed'", desc: "총 매출" }
        ];
        
        let completed = 0;
        
        queries.forEach(query => {
            db.all(query.sql, [], (err, rows) => {
                if (err) {
                    console.error(`❌ ${query.desc} 확인 실패:`, err.message);
                    reject(err);
                    return;
                }
                
                console.log(`\n📈 ${query.desc}:`);
                rows.forEach(row => {
                    console.log('  ', JSON.stringify(row));
                });
                
                completed++;
                if (completed === queries.length) {
                    resolve();
                }
            });
        });
    });
}

// 메인 실행 함수
async function setupDatabase() {
    try {
        console.log('\n🚀 YCS LMS 데이터베이스 설정 시작\n');
        
        // 1. 스키마 생성
        await executeSQL(schemaPath, '테이블 스키마 생성');
        
        // 2. 더미 데이터 삽입
        await executeSQL(dataPath, '더미 데이터 삽입');
        
        // 3. 데이터 검증
        await verifyData();
        
        console.log('\n🎉 데이터베이스 설정 완료!');
        console.log('📍 데이터베이스 위치:', dbPath);
        console.log('\n📋 테스트 계정 정보:');
        console.log('  - 일반회원: general@test.com / Test123!@#');
        console.log('  - 기업회원: corporate@test.com / Test123!@#');
        console.log('  - 파트너: partner@test.com / Test123!@#');
        console.log('  - 창고관리자: warehouse@test.com / Test123!@#');
        console.log('  - 시스템관리자: admin@test.com / Test123!@#');
        
    } catch (error) {
        console.error('❌ 데이터베이스 설정 실패:', error);
    } finally {
        // 데이터베이스 연결 종료
        db.close((err) => {
            if (err) {
                console.error('❌ 데이터베이스 연결 종료 실패:', err.message);
            } else {
                console.log('\n✅ 데이터베이스 연결 종료');
            }
        });
    }
}

// 실행
setupDatabase();