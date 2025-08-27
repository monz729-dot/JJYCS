// 테스트용 사용자 생성 스크립트

async function createUsers() {
    const baseUrl = 'http://localhost:8081/api';
    
    // 테스트 사용자 데이터
    const users = [
        {
            email: 'user@test.com',
            password: 'Test1234!',
            name: '일반사용자',
            phone: '010-1111-2222',
            userType: 'GENERAL'
        },
        {
            email: 'corp@test.com', 
            password: 'Test1234!',
            name: '기업담당자',
            phone: '010-3333-4444',
            userType: 'CORPORATE',
            companyName: '테스트물류(주)',
            businessNumber: '123-45-67890',
            companyAddress: '서울시 강남구 테스트로 123'
        },
        {
            email: 'partner@test.com',
            password: 'Test1234!',
            name: '파트너매니저',
            phone: '010-5555-6666',
            userType: 'PARTNER',
            partnerRegion: '방콕'
        },
        {
            email: 'admin@test.com',
            password: 'Admin1234!',
            name: '시스템관리자',
            phone: '010-7777-8888',
            userType: 'ADMIN'
        }
    ];

    console.log('=== 테스트 사용자 생성 시작 ===\n');

    for (const user of users) {
        try {
            const response = await fetch(`${baseUrl}/auth/signup`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(user)
            });

            const data = await response.json();
            
            if (response.ok && data.success) {
                console.log(`✅ ${user.userType} - ${user.email} 생성 완료`);
                if (data.isPending) {
                    console.log(`   ⏳ 승인 대기 중 (관리자 승인 필요)`);
                }
            } else {
                console.log(`❌ ${user.userType} - ${user.email} 생성 실패:`, data.error || data.message);
            }
        } catch (error) {
            console.log(`❌ ${user.userType} - ${user.email} 생성 실패:`, error.message);
        }
    }
    
    console.log('\n=== 테스트 사용자 생성 완료 ===');
    console.log('\n로그인 정보:');
    console.log('- 일반: user@test.com / Test1234!');
    console.log('- 기업: corp@test.com / Test1234! (승인 필요)');
    console.log('- 파트너: partner@test.com / Test1234! (승인 필요)');
    console.log('- 관리자: admin@test.com / Admin1234!');
}

// 스크립트 실행
createUsers().catch(console.error);