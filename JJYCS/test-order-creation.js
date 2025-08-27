// 주문 생성 테스트 스크립트

async function testOrderCreation() {
    const baseUrl = 'http://localhost:8081/api';
    
    console.log('=== 주문 생성 테스트 시작 ===\n');
    
    // 1. 로그인
    console.log('1. 사용자 로그인...');
    const loginResponse = await fetch(`${baseUrl}/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            email: 'user@test.com',
            password: 'Test1234!'
        })
    });
    
    const loginData = await loginResponse.json();
    if (!loginData.success) {
        console.error('❌ 로그인 실패:', loginData.error);
        return;
    }
    
    const token = loginData.token;
    console.log('✅ 로그인 성공');
    console.log('   사용자:', loginData.user.name);
    console.log('   회원코드:', loginData.user.memberCode);
    
    // 2. 주문 생성
    console.log('\n2. 주문 생성 중...');
    
    const orderData = {
        trackingNumber: 'EQ123456789KR',
        shippingType: 'sea',  // 초기값은 해상운송
        country: 'thailand',
        postalCode: '10110',
        recipientName: '김태국',
        recipientPhone: '+66-2-123-4567',
        recipientAddress: '123 Sukhumvit Road, Bangkok',
        recipientPostalCode: '10110',
        specialRequests: '깨지기 쉬운 물품이니 조심히 다뤄주세요',
        repackingRequested: true,
        orderItems: [
            {
                hsCode: '1905.31',
                description: '빼빼로 초코맛',
                quantity: 10,
                weight: 5.0,
                width: 30,
                height: 20,
                depth: 15,
                unitPrice: 25.00,
                totalPrice: 250.00
            },
            {
                hsCode: '8517.12',
                description: '삼성 갤럭시 S24',
                quantity: 2,
                weight: 0.5,
                width: 15,
                height: 8,
                depth: 1,
                unitPrice: 1800.00,  // THB 1,500 초과
                totalPrice: 3600.00
            },
            {
                hsCode: '3304.99',
                description: '설화수 화장품 세트',
                quantity: 5,
                weight: 2.5,
                width: 25,
                height: 20,
                depth: 10,
                unitPrice: 800.00,
                totalPrice: 4000.00
            }
        ]
    };
    
    // CBM 계산
    let totalCBM = 0;
    orderData.orderItems.forEach(item => {
        const cbm = (item.width * item.height * item.depth) / 1000000;
        totalCBM += cbm * item.quantity;
        console.log(`   - ${item.description}: ${cbm.toFixed(6)} m³ × ${item.quantity} = ${(cbm * item.quantity).toFixed(6)} m³`);
    });
    console.log(`   총 CBM: ${totalCBM.toFixed(6)} m³`);
    
    // THB 1,500 초과 체크
    const highValueItems = orderData.orderItems.filter(item => item.unitPrice > 1500);
    if (highValueItems.length > 0) {
        console.log(`   ⚠️ THB 1,500 초과 품목 ${highValueItems.length}개 - 수취인 추가 정보 필요`);
    }
    
    // 주문 API 호출
    try {
        const orderResponse = await fetch(`${baseUrl}/orders`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(orderData)
        });
        
        const result = await orderResponse.json();
        
        if (orderResponse.ok) {
            console.log('\n✅ 주문 생성 성공!');
            console.log('   주문번호:', result.orderNumber);
            console.log('   상태:', result.status);
            console.log('   배송타입:', result.shippingType);
            console.log('   총 CBM:', result.totalCbm);
            console.log('   추가 수취인 정보 필요:', result.requiresExtraRecipient);
            
            // CBM 29 초과 확인
            if (result.shippingType === 'AIR' && totalCBM > 29) {
                console.log('   🛩️ CBM 29 초과로 자동 항공운송 전환됨');
            }
            
            return result.id;
        } else {
            console.error('❌ 주문 생성 실패:', result);
        }
    } catch (error) {
        console.error('❌ 주문 생성 중 오류:', error.message);
    }
}

// 주문 조회 테스트
async function testOrderRetrieval(orderId) {
    const baseUrl = 'http://localhost:8081/api';
    
    console.log('\n3. 생성된 주문 조회...');
    
    // 로그인 (관리자)
    const loginResponse = await fetch(`${baseUrl}/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            email: 'admin@test.com',
            password: 'Admin1234!'
        })
    });
    
    const loginData = await loginResponse.json();
    const token = loginData.token;
    
    // 주문 목록 조회
    const ordersResponse = await fetch(`${baseUrl}/orders`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    
    const orders = await ordersResponse.json();
    console.log(`   전체 주문 수: ${orders.length || orders.content?.length || 0}개`);
    
    // 특정 주문 조회
    if (orderId) {
        const orderResponse = await fetch(`${baseUrl}/orders/${orderId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (orderResponse.ok) {
            const order = await orderResponse.json();
            console.log('\n   주문 상세 정보:');
            console.log('   - 주문번호:', order.orderNumber);
            console.log('   - 수취인:', order.recipientName);
            console.log('   - 품목 수:', order.items?.length || 0);
            console.log('   - 상태:', order.status);
        }
    }
}

// H2 DB 확인 쿼리 출력
function printH2Queries() {
    console.log('\n=== H2 콘솔에서 확인할 쿼리 ===');
    console.log('URL: http://localhost:8081/h2-console');
    console.log('JDBC URL: jdbc:h2:mem:testdb');
    console.log('Username: SA\n');
    
    console.log('-- 사용자 조회');
    console.log("SELECT * FROM users;");
    console.log('\n-- 주문 조회');
    console.log("SELECT * FROM orders;");
    console.log('\n-- 주문 아이템 조회');
    console.log("SELECT * FROM order_items;");
    console.log('\n-- 승인 대기 사용자');
    console.log("SELECT * FROM users WHERE status = 'PENDING';");
}

// 메인 실행
async function main() {
    try {
        const orderId = await testOrderCreation();
        await testOrderRetrieval(orderId);
        printH2Queries();
    } catch (error) {
        console.error('테스트 실패:', error);
    }
}

main();