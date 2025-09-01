// Admin Users API 직접 테스트
const axios = require('axios')

const API_BASE_URL = 'http://localhost:8081/api'
const ADMIN_USER = {
    email: 'admin@ycs.com',
    password: 'password'
}

async function testAdminUsersAPI() {
    console.log('🧪 Admin Users API 직접 테스트 시작')
    console.log('=' .repeat(50))
    
    try {
        // 1. 관리자 로그인
        console.log('1️⃣ 관리자 로그인 시도...')
        const loginResponse = await axios.post(`${API_BASE_URL}/auth/login`, ADMIN_USER)
        
        if (!loginResponse.data.success) {
            console.log('❌ 로그인 실패:', loginResponse.data.error)
            return
        }
        
        const token = loginResponse.data.token || loginResponse.data.accessToken
        console.log('✅ 로그인 성공, 토큰 획득:', token.substring(0, 20) + '...')
        console.log('사용자 정보:', JSON.stringify({
            email: loginResponse.data.user?.email,
            userType: loginResponse.data.user?.userType,
            status: loginResponse.data.user?.status,
            name: loginResponse.data.user?.name
        }, null, 2))
        
        // 2. /admin/users 엔드포인트 테스트 (수정된 경로)
        console.log('\n2️⃣ /admin/users 엔드포인트 테스트...')
        console.log('토큰 확인:', token ? `${token.substring(0, 20)}...` : 'No token')
        try {
            const usersResponse = await axios.get(`${API_BASE_URL}/admin/users`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                validateStatus: function (status) {
                    return status < 500; // 500 미만은 모두 허용
                }
            })
            
            if (usersResponse.status === 200) {
                console.log('✅ /admin/users 성공!')
                console.log('📊 응답 데이터:', JSON.stringify(usersResponse.data, null, 2))
            } else {
                console.log('⚠️ /admin/users 비정상 응답')
                console.log('상태 코드:', usersResponse.status)
                console.log('응답 데이터:', JSON.stringify(usersResponse.data, null, 2))
            }
            
        } catch (error) {
            console.log('❌ /admin/users 실패')
            console.log('상태 코드:', error.response?.status)
            console.log('에러 헤더:', error.response?.headers)
            console.log('에러 응답:', JSON.stringify(error.response?.data, null, 2))
            console.log('에러 메시지:', error.message)
        }
        
        // 3. /admin/users/all 엔드포인트 테스트 (기존)
        console.log('\n3️⃣ /admin/users/all 엔드포인트 테스트...')
        try {
            const allUsersResponse = await axios.get(`${API_BASE_URL}/admin/users/all`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            })
            
            console.log('✅ /admin/users/all 성공!')
            console.log('📊 사용자 수:', allUsersResponse.data.totalElements)
            
        } catch (error) {
            console.log('❌ /admin/users/all 실패')
            console.log('상태 코드:', error.response?.status)
            console.log('에러 응답:', JSON.stringify(error.response?.data, null, 2))
        }
        
        // 4. HS Code API 테스트
        console.log('\n4️⃣ HS Code API 권한 테스트...')
        try {
            const hsCodeResponse = await axios.get(`${API_BASE_URL}/hscode/search?productName=computer`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            })
            
            console.log('✅ HS Code 검색 성공!')
            console.log('📊 응답:', hsCodeResponse.data.success ? '성공' : '실패')
            console.log('폴백 데이터:', hsCodeResponse.data.fallback ? 'Yes' : 'No')
            
        } catch (error) {
            console.log('❌ HS Code 검색 실패')
            console.log('상태 코드:', error.response?.status)
            console.log('에러 응답:', JSON.stringify(error.response?.data, null, 2))
        }
        
        // 5. 잘못된 토큰으로 401 테스트
        console.log('\n5️⃣ 잘못된 토큰으로 인증 실패 테스트...')
        try {
            await axios.get(`${API_BASE_URL}/admin/stats`, {
                headers: {
                    'Authorization': 'Bearer invalid-token-12345',
                    'Content-Type': 'application/json'
                }
            })
        } catch (error) {
            console.log('✅ 예상된 인증 실패')
            console.log('상태 코드:', error.response?.status, '(401 예상)')
            console.log('에러 메시지:', error.response?.data?.error)
        }
        
        // 6. 토큰 없이 403 테스트
        console.log('\n6️⃣ 토큰 없이 권한 오류 테스트...')
        try {
            await axios.get(`${API_BASE_URL}/admin/stats`)
        } catch (error) {
            console.log('✅ 예상된 권한 오류')
            console.log('상태 코드:', error.response?.status)
            console.log('에러 메시지:', error.response?.data?.error)
        }
        
    } catch (error) {
        console.log('❌ 전체 테스트 실패:', error.message)
    }
}

testAdminUsersAPI().then(() => {
    console.log('\n🏁 테스트 완료')
}).catch(error => {
    console.error('테스트 실행 오류:', error)
})