// Gmail SMTP 이메일 발송 테스트

async function testEmail() {
    console.log('🔍 Gmail SMTP 이메일 발송 테스트 시작...\n');
    
    const baseUrl = 'http://localhost:8080/api';
    const testEmail = 'monz729@gmail.com'; // 실제 테스트할 이메일 주소
    
    try {
        // 1. 간단한 테스트 이메일 발송
        console.log('1. 간단한 테스트 이메일 발송...');
        const simpleResponse = await fetch(`${baseUrl}/test/email`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: testEmail
            })
        });
        
        const simpleData = await simpleResponse.json();
        if (simpleData.success) {
            console.log('✅ 간단한 테스트 이메일 발송 성공');
            console.log(`   메시지: ${simpleData.message}`);
        } else {
            console.log('❌ 간단한 테스트 이메일 발송 실패:', simpleData.error);
        }
        
        // 2. 이메일 인증 메일 테스트
        console.log('\n2. 이메일 인증 메일 테스트...');
        const verificationResponse = await fetch(`${baseUrl}/test/verification-email`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: testEmail,
                name: '테스트 사용자'
            })
        });
        
        const verificationData = await verificationResponse.json();
        if (verificationData.success) {
            console.log('✅ 이메일 인증 메일 발송 성공');
            console.log(`   메시지: ${verificationData.message}`);
            console.log(`   토큰: ${verificationData.token}`);
        } else {
            console.log('❌ 이메일 인증 메일 발송 실패:', verificationData.error);
        }
        
        // 3. 비밀번호 재설정 메일 테스트
        console.log('\n3. 비밀번호 재설정 메일 테스트...');
        const resetResponse = await fetch(`${baseUrl}/test/password-reset-email`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: testEmail,
                name: '테스트 사용자'
            })
        });
        
        const resetData = await resetResponse.json();
        if (resetData.success) {
            console.log('✅ 비밀번호 재설정 메일 발송 성공');
            console.log(`   메시지: ${resetData.message}`);
            console.log(`   토큰: ${resetData.token}`);
        } else {
            console.log('❌ 비밀번호 재설정 메일 발송 실패:', resetData.error);
        }
        
        // 4. 실제 회원가입 플로우 테스트
        console.log('\n4. 실제 회원가입 이메일 인증 플로우 테스트...');
        const signupResponse = await fetch(`${baseUrl}/auth/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: 'test' + Date.now() + '@gmail.com', // 고유한 이메일
                password: 'Test1234!',
                name: '이메일 테스트 사용자',
                phone: '010-1234-5678',
                userType: 'GENERAL'
            })
        });
        
        const signupData = await signupResponse.json();
        if (signupData.success) {
            console.log('✅ 회원가입 이메일 인증 플로우 성공');
            console.log(`   메시지: ${signupData.message}`);
            console.log(`   사용자: ${signupData.user.name} (${signupData.user.email})`);
        } else {
            console.log('❌ 회원가입 이메일 인증 플로우 실패:', signupData.error);
        }
        
        console.log('\n=== Gmail SMTP 테스트 완료 ===');
        console.log('📧 이메일 수신함을 확인하세요!');
        console.log('   - 간단한 테스트 메일');
        console.log('   - HTML 형식의 이메일 인증 메일 (링크 포함)');
        console.log('   - HTML 형식의 비밀번호 재설정 메일 (링크 포함)');
        console.log('   - 실제 회원가입 인증 메일');
        
    } catch (error) {
        console.error('\n❌ 이메일 테스트 중 오류 발생:', error.message);
        console.log('\n📋 문제 해결 체크리스트:');
        console.log('1. 백엔드 서버가 http://localhost:8080에서 실행 중인지 확인');
        console.log('2. Gmail SMTP 설정 확인 (application-dev.yml)');
        console.log('3. 앱 비밀번호가 올바른지 확인');
        console.log('4. 방화벽/네트워크 설정 확인');
    }
}

// 메인 실행
testEmail();