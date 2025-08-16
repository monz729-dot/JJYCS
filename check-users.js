const sqlite3 = require('sqlite3').verbose();
const path = require('path');

// 데이터베이스 파일 경로
const dbPath = path.join(__dirname, 'database', 'ycs_lms.db');

const db = new sqlite3.Database(dbPath, (err) => {
    if (err) {
        console.error('❌ 데이터베이스 연결 실패:', err.message);
        return;
    }
    console.log('✅ SQLite 데이터베이스 연결 성공');
});

console.log('\n📋 사용자 계정 정보:');
db.all("SELECT id, name, email, user_type, member_code, status FROM users ORDER BY id", [], (err, users) => {
    if (err) {
        console.error('사용자 조회 오류:', err.message);
        return;
    }
    
    users.forEach(user => {
        console.log(`${user.id}. ${user.name} (${user.email}) - ${user.user_type} - ${user.member_code} - ${user.status}`);
    });
    
    console.log('\n📊 사용자별 주문 데이터:');
    db.all(`
        SELECT 
            u.name, 
            u.email, 
            u.user_type,
            COUNT(o.id) as order_count,
            GROUP_CONCAT(o.order_number) as orders
        FROM users u
        LEFT JOIN orders o ON u.id = o.user_id
        GROUP BY u.id
        ORDER BY u.id
    `, [], (err, orderStats) => {
        if (err) {
            console.error('주문 통계 오류:', err.message);
            return;
        }
        
        orderStats.forEach(stat => {
            console.log(`${stat.name} (${stat.user_type}): ${stat.order_count}개 주문`);
            if (stat.orders) {
                console.log(`  주문: ${stat.orders}`);
            }
        });
        
        console.log('\n💰 결제 완료 주문:');
        db.all(`
            SELECT 
                u.name,
                o.order_number,
                p.amount,
                p.payment_status
            FROM payments p
            JOIN orders o ON p.order_id = o.id
            JOIN users u ON o.user_id = u.id
            WHERE p.payment_status = 'completed'
            ORDER BY p.paid_at DESC
        `, [], (err, payments) => {
            if (err) {
                console.error('결제 조회 오류:', err.message);
                return;
            }
            
            payments.forEach(payment => {
                console.log(`${payment.name}: ${payment.order_number} - ₩${Number(payment.amount).toLocaleString()}`);
            });
            
            console.log('\n📦 재고 현황:');
            db.all(`
                SELECT 
                    u.name,
                    o.order_number,
                    i.label_code,
                    i.status,
                    w.name as warehouse_name
                FROM inventory i
                JOIN orders o ON i.order_id = o.id
                JOIN users u ON o.user_id = u.id
                JOIN warehouses w ON i.warehouse_id = w.id
                ORDER BY u.id, o.id
            `, [], (err, inventory) => {
                if (err) {
                    console.error('재고 조회 오류:', err.message);
                    return;
                }
                
                inventory.forEach(item => {
                    console.log(`${item.name}: ${item.order_number} (${item.label_code}) - ${item.status} @ ${item.warehouse_name}`);
                });
                
                console.log('\n🤝 파트너 추천 현황:');
                db.all(`
                    SELECT 
                        partner.name as partner_name,
                        referred.name as referred_name,
                        pr.commission_amount,
                        pr.status,
                        o.order_number
                    FROM partner_referrals pr
                    JOIN users partner ON pr.partner_id = partner.id
                    JOIN users referred ON pr.referred_user_id = referred.id
                    LEFT JOIN orders o ON pr.order_id = o.id
                    ORDER BY pr.created_at
                `, [], (err, referrals) => {
                    if (err) {
                        console.error('추천 조회 오류:', err.message);
                        return;
                    }
                    
                    referrals.forEach(ref => {
                        console.log(`${ref.partner_name} → ${ref.referred_name}: ₩${Number(ref.commission_amount).toLocaleString()} (${ref.status}) [${ref.order_number}]`);
                    });
                    
                    // 데이터베이스 연결 종료
                    db.close((err) => {
                        if (err) {
                            console.error('❌ 데이터베이스 연결 종료 실패:', err.message);
                        } else {
                            console.log('\n✅ 데이터베이스 연결 종료');
                        }
                    });
                });
            });
        });
    });
});