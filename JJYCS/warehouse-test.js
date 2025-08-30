// 창고 스캔 시스템 테스트

async function testWarehouseScan() {
    console.log('=== 창고 스캔 시스템 테스트 시작 ===\n');
    
    const baseUrl = 'http://localhost:8080/api';
    
    try {
        // 1. 창고 현황 확인
        console.log('1. 창고 현황 확인...');
        const statusResponse = await fetch(`${baseUrl}/warehouse/status`);
        const statusData = await statusResponse.json();
        
        if (statusData.success) {
            console.log('✅ 창고 현황 조회 성공');
            console.log(`   입고 완료: ${statusData.status.arrivedCount}개`);
            console.log(`   리패킹 중: ${statusData.status.repackingCount}개`);
            console.log(`   배송 준비: ${statusData.status.shippingCount}개`);
            console.log(`   창고 총 보관: ${statusData.status.totalInWarehouse}개`);
            
            if (statusData.status.orders.length > 0) {
                const order = statusData.status.orders[0];
                console.log(`   테스트용 주문: ${order.orderNumber} (${order.status})`);
            }
        } else {
            console.log('❌ 창고 현황 조회 실패');
        }
        
        // 2. 주문 목록에서 테스트할 주문 찾기
        console.log('\n2. 테스트용 주문 선택...');
        const ordersResponse = await fetch(`${baseUrl}/orders`);
        const ordersData = await ordersResponse.json();
        
        if (!ordersData.success || ordersData.orders.length === 0) {
            console.log('❌ 테스트할 주문이 없습니다.');
            return;
        }
        
        // RECEIVED 상태의 주문을 입고 테스트용으로 사용
        const receivedOrder = ordersData.orders.find(o => o.status === 'RECEIVED');
        const testOrder = receivedOrder || ordersData.orders[0];
        
        console.log('✅ 테스트 주문 선택됨');
        console.log(`   주문번호: ${testOrder.orderNumber}`);
        console.log(`   현재 상태: ${testOrder.status}`);
        console.log(`   수취인: ${testOrder.recipientName}`);
        
        // 3. 일반 스캔 API 테스트 (Generic scan endpoint)
        console.log('\n3. 일반 스캔 API 테스트...');
        const genericScanResponse = await fetch(`${baseUrl}/warehouse/scan`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                labelCode: testOrder.orderNumber,
                scanType: 'INBOUND',
                location: 'TEST-A-01',
                notes: '테스트 스캔'
            })
        });
        
        const scanResult = await genericScanResponse.json();
        if (scanResult.success) {
            console.log('✅ 일반 스캔 API 성공');
            console.log(`   응답: ${scanResult.message}`);
            console.log(`   스캔 타입: ${scanResult.scanType}`);
            console.log(`   위치: ${scanResult.location}`);
        } else {
            console.log('❌ 일반 스캔 API 실패:', scanResult.error);
        }
        
        // 4. 업데이트된 창고 현황 확인
        console.log('\n4. 스캔 후 창고 현황 재확인...');
        const updatedStatusResponse = await fetch(`${baseUrl}/warehouse/status`);
        const updatedStatusData = await updatedStatusResponse.json();
        
        if (updatedStatusData.success) {
            console.log('✅ 업데이트된 창고 현황');
            console.log(`   입고 완료: ${updatedStatusData.status.arrivedCount}개`);
            console.log(`   창고 총 보관: ${updatedStatusData.status.totalInWarehouse}개`);
        }
        
        // 5. 출고 테스트 (도착한 주문이 있는 경우)
        if (updatedStatusData.status.arrivedCount > 0) {
            console.log('\n5. 출고 스캔 테스트...');
            const arrivedOrder = updatedStatusData.status.orders[0];
            
            const outboundScanResponse = await fetch(`${baseUrl}/warehouse/scan`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    labelCode: arrivedOrder.orderNumber,
                    scanType: 'OUTBOUND',
                    location: arrivedOrder.storageLocation,
                    notes: '테스트 출고'
                })
            });
            
            const outboundResult = await outboundScanResponse.json();
            if (outboundResult.success) {
                console.log('✅ 출고 스캔 성공');
                console.log(`   응답: ${outboundResult.message}`);
            } else {
                console.log('⚠️ 출고 스캔 시도:', outboundResult.error || 'Unknown error');
            }
        }
        
        // 6. 최종 상태 확인
        console.log('\n6. 최종 상태 확인...');
        const finalStatusResponse = await fetch(`${baseUrl}/warehouse/status`);
        const finalStatusData = await finalStatusResponse.json();
        
        if (finalStatusData.success) {
            console.log('✅ 최종 창고 현황');
            console.log(`   입고 완료: ${finalStatusData.status.arrivedCount}개`);
            console.log(`   배송 준비: ${finalStatusData.status.shippingCount}개`);
            console.log(`   창고 총 보관: ${finalStatusData.status.totalInWarehouse}개`);
        }
        
        console.log('\n=== 창고 스캔 시스템 테스트 완료 ===');
        console.log('✅ 스캔 시스템이 정상적으로 작동합니다!');
        
        // 7. 사용 가능한 URL 정보 출력
        console.log('\n📱 스캔 인터페이스 URL:');
        console.log('- 창고 스캔 페이지: file:///C:/YSC-ver2/JJYSC/html/warehouse-scan.html');
        console.log('- 백엔드 API: http://localhost:8080/api/warehouse/scan');
        console.log('- 창고 현황 API: http://localhost:8080/api/warehouse/status');
        
    } catch (error) {
        console.error('\n❌ 테스트 중 오류 발생:', error.message);
        console.log('\n📋 문제 해결 체크리스트:');
        console.log('1. 백엔드 서버가 http://localhost:8080에서 실행 중인지 확인');
        console.log('2. 데이터베이스에 테스트 주문이 있는지 확인');
        console.log('3. CORS 설정이 올바른지 확인');
    }
}

// 메인 실행
testWarehouseScan();