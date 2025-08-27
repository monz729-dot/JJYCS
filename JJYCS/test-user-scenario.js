const axios = require('axios');

// API 기본 설정
const API_BASE = 'http://localhost:8081/api';
const FRONTEND_BASE = 'http://localhost:3001';

// 색상 코드 (콘솔 출력용)
const colors = {
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    reset: '\x1b[0m',
    bold: '\x1b[1m'
};

// 로그 함수들
const log = {
    success: (msg) => console.log(`${colors.green}✅ ${msg}${colors.reset}`),
    error: (msg) => console.log(`${colors.red}❌ ${msg}${colors.reset}`),
    info: (msg) => console.log(`${colors.blue}ℹ️  ${msg}${colors.reset}`),
    warn: (msg) => console.log(`${colors.yellow}⚠️  ${msg}${colors.reset}`),
    header: (msg) => console.log(`\n${colors.bold}${colors.blue}🧪 ${msg}${colors.reset}\n`)
};

// 사용자 시나리오 테스트 클래스
class UserScenarioTest {
    constructor() {
        this.testResults = [];
    }

    async runTest(testName, testFunction) {
        try {
            log.info(`테스트 시작: ${testName}`);
            const result = await testFunction();
            this.testResults.push({ name: testName, status: 'PASS', result });
            log.success(`테스트 통과: ${testName}`);
            return result;
        } catch (error) {
            this.testResults.push({ name: testName, status: 'FAIL', error: error.message });
            log.error(`테스트 실패: ${testName} - ${error.message}`);
            throw error;
        }
    }

    // 1. 품목명으로 HS Code 검색 테스트
    async testSearchByProductName() {
        const testCases = [
            { productName: '전자', expectedHsCode: '850431' },
            { productName: 'electronic', expectedHsCode: '850431' },
            { productName: '가죽', expectedHsCode: '640399' }
        ];

        for (const testCase of testCases) {
            const url = `${API_BASE}/hscode/search/by-product?productName=${encodeURIComponent(testCase.productName)}`;
            const response = await axios.get(url);
            
            if (response.data.success && response.data.items.length > 0) {
                const foundItem = response.data.items.find(item => item.hsCode === testCase.expectedHsCode);
                if (foundItem) {
                    log.success(`"${testCase.productName}" → HS Code: ${foundItem.hsCode}, 품목명: ${foundItem.koreanName}`);
                } else {
                    log.warn(`"${testCase.productName}" 검색 결과: ${response.data.items.length}개 항목 찾음`);
                }
            } else {
                throw new Error(`검색 실패: ${testCase.productName}`);
            }
        }
        
        return '품목명 검색 테스트 완료';
    }

    // 2. HS Code로 품목 정보 검색 테스트
    async testSearchByHsCode() {
        const testCases = ['850431', '640399', '611020'];

        for (const hsCode of testCases) {
            const url = `${API_BASE}/hscode/search/by-hscode?hsCode=${hsCode}`;
            const response = await axios.get(url);
            
            if (response.data.success && response.data.items.length > 0) {
                const item = response.data.items[0];
                log.success(`HS Code ${hsCode} → ${item.koreanName} (${item.englishName})`);
            } else {
                throw new Error(`HS Code 검색 실패: ${hsCode}`);
            }
        }
        
        return 'HS Code 검색 테스트 완료';
    }

    // 3. 관세율 조회 테스트
    async testTariffRateLookup() {
        const testCases = ['850431', '640399'];

        for (const hsCode of testCases) {
            const url = `${API_BASE}/hscode/tariff/${hsCode}`;
            const response = await axios.get(url);
            
            if (response.data.success && response.data.items.length > 0) {
                const item = response.data.items[0];
                log.success(`HS Code ${hsCode} 관세율: 기본 ${item.basicRate}%, WTO ${item.wtoRate}%, 특혜 ${item.specialRate}%`);
            } else {
                throw new Error(`관세율 조회 실패: ${hsCode}`);
            }
        }
        
        return '관세율 조회 테스트 완료';
    }

    // 4. 관세 계산 테스트
    async testCustomsDutyCalculation() {
        const testCases = [
            { hsCode: '850431', quantity: 10, value: 1000, currency: 'USD' },
            { hsCode: '640399', quantity: 5, value: 500, currency: 'USD' }
        ];

        for (const testCase of testCases) {
            const url = `${API_BASE}/hscode/calculate-duty`;
            const response = await axios.post(url, testCase);
            
            if (response.data.hsCode) {
                log.success(`관세 계산 (${testCase.hsCode}): 신고가격 $${testCase.value} → 관세액 $${response.data.dutyAmount} → 총액 $${response.data.totalAmount}`);
                log.info(`적용 관세율: ${response.data.appliedRate}%`);
            } else {
                throw new Error(`관세 계산 실패: ${testCase.hsCode}`);
            }
        }
        
        return '관세 계산 테스트 완료';
    }

    // 5. 환율 정보 조회 테스트
    async testExchangeRateInfo() {
        const url = `${API_BASE}/hscode/exchange-rate`;
        const response = await axios.get(url);
        
        if (response.data.success) {
            const data = response.data.data;
            log.success(`환율 정보: ${data.baseCurrency}/${data.targetCurrency} = ${data.exchangeRate}`);
            log.info(`마지막 업데이트: ${new Date(data.lastUpdated).toLocaleString('ko-KR')}`);
        } else {
            throw new Error('환율 정보 조회 실패');
        }
        
        return '환율 정보 조회 테스트 완료';
    }

    // 6. 실제 주문 시나리오 테스트
    async testCompleteOrderScenario() {
        log.info('완전한 주문 시나리오 테스트 시작...');

        // Step 1: 품목명으로 HS Code 검색
        const productName = '전자제품';
        const searchUrl = `${API_BASE}/hscode/search/by-product?productName=${encodeURIComponent(productName)}`;
        const searchResponse = await axios.get(searchUrl);
        
        if (!searchResponse.data.success || searchResponse.data.items.length === 0) {
            throw new Error('품목 검색 실패');
        }

        const selectedItem = searchResponse.data.items[0];
        log.success(`Step 1: "${productName}" → HS Code ${selectedItem.hsCode} 선택`);

        // Step 2: 선택된 HS Code의 관세율 조회
        const tariffUrl = `${API_BASE}/hscode/tariff/${selectedItem.hsCode}`;
        const tariffResponse = await axios.get(tariffUrl);
        
        if (!tariffResponse.data.success) {
            throw new Error('관세율 조회 실패');
        }

        const tariffInfo = tariffResponse.data.items[0];
        log.success(`Step 2: 관세율 조회 완료 - 기본 ${tariffInfo.basicRate}%`);

        // Step 3: 관세 계산
        const orderData = {
            hsCode: selectedItem.hsCode,
            quantity: 5,
            value: 1500,
            currency: 'USD'
        };

        const dutyUrl = `${API_BASE}/hscode/calculate-duty`;
        const dutyResponse = await axios.post(dutyUrl, orderData);
        
        if (!dutyResponse.data.hsCode) {
            throw new Error('관세 계산 실패');
        }

        log.success(`Step 3: 관세 계산 완료`);
        log.info(`  - 신고가격: $${orderData.value}`);
        log.info(`  - 관세액: $${dutyResponse.data.dutyAmount}`);
        log.info(`  - 총 금액: $${dutyResponse.data.totalAmount}`);

        return {
            selectedHsCode: selectedItem.hsCode,
            productName: selectedItem.koreanName,
            orderValue: orderData.value,
            dutyAmount: dutyResponse.data.dutyAmount,
            totalAmount: dutyResponse.data.totalAmount
        };
    }

    // 전체 테스트 실행
    async runAllTests() {
        log.header('🚀 HS Code API 통합 테스트 시작');
        
        try {
            // 서버 상태 확인
            await this.runTest('API 서버 연결 확인', async () => {
                const response = await axios.get(`${API_BASE}/health`);
                return response.data.status === 'OK' ? '서버 정상 작동' : '서버 상태 이상';
            });

            // 개별 기능 테스트
            await this.runTest('품목명으로 HS Code 검색', () => this.testSearchByProductName());
            await this.runTest('HS Code로 품목 정보 검색', () => this.testSearchByHsCode());
            await this.runTest('관세율 조회', () => this.testTariffRateLookup());
            await this.runTest('관세 계산', () => this.testCustomsDutyCalculation());
            await this.runTest('환율 정보 조회', () => this.testExchangeRateInfo());
            
            // 통합 시나리오 테스트
            await this.runTest('완전한 주문 시나리오', () => this.testCompleteOrderScenario());

            // 결과 리포트
            this.printTestReport();

        } catch (error) {
            log.error(`테스트 중 치명적 오류: ${error.message}`);
            this.printTestReport();
        }
    }

    // 테스트 결과 리포트 출력
    printTestReport() {
        log.header('📊 테스트 결과 리포트');
        
        const passed = this.testResults.filter(r => r.status === 'PASS').length;
        const failed = this.testResults.filter(r => r.status === 'FAIL').length;
        const total = this.testResults.length;

        console.log(`${colors.bold}전체 테스트: ${total}개${colors.reset}`);
        console.log(`${colors.green}통과: ${passed}개${colors.reset}`);
        console.log(`${colors.red}실패: ${failed}개${colors.reset}`);
        console.log(`${colors.blue}성공률: ${((passed / total) * 100).toFixed(1)}%${colors.reset}\n`);

        // 실패한 테스트 상세 정보
        if (failed > 0) {
            log.header('❌ 실패한 테스트 상세');
            this.testResults
                .filter(r => r.status === 'FAIL')
                .forEach(test => {
                    console.log(`${colors.red}• ${test.name}: ${test.error}${colors.reset}`);
                });
        }

        // 성공한 테스트 요약
        if (passed > 0) {
            log.header('✅ 성공한 테스트');
            this.testResults
                .filter(r => r.status === 'PASS')
                .forEach(test => {
                    console.log(`${colors.green}• ${test.name}${colors.reset}`);
                });
        }

        log.header('🎯 테스트 완료');
        console.log(`${colors.yellow}프론트엔드: ${FRONTEND_BASE}${colors.reset}`);
        console.log(`${colors.yellow}API 서버: ${API_BASE}${colors.reset}`);
        console.log(`${colors.yellow}테스트 페이지: file://${process.cwd()}\\test-hscode-integration.html${colors.reset}\n`);
    }
}

// 테스트 실행
const test = new UserScenarioTest();
test.runAllTests().catch(console.error);