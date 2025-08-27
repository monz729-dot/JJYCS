/**
 * YCS LMS Mock API
 * H2 데이터베이스 대신 로컬스토리지를 사용한 Mock API
 */

class MockAPI {
    constructor() {
        this.baseUrl = 'http://localhost:8080/api';
        this.initializeData();
    }

    // 초기 데이터 설정
    initializeData() {
        if (!localStorage.getItem('ycs_users')) {
            const users = [
                {
                    id: 1,
                    email: 'admin@ycs.com',
                    password: 'password123', // 실제로는 해싱됨
                    name: '관리자',
                    phone: '02-1234-5678',
                    userType: 'ADMIN',
                    status: 'ACTIVE',
                    memberCode: 'ADM001',
                    emailVerified: true,
                    createdAt: new Date('2024-01-01').toISOString(),
                    updatedAt: new Date().toISOString()
                },
                {
                    id: 2,
                    email: 'kimcs@email.com',
                    password: 'password123',
                    name: '김철수',
                    phone: '010-1234-5678',
                    userType: 'GENERAL',
                    status: 'ACTIVE',
                    memberCode: 'GEN001',
                    emailVerified: true,
                    createdAt: new Date('2024-01-10').toISOString(),
                    updatedAt: new Date().toISOString()
                },
                {
                    id: 3,
                    email: 'lee@company.com',
                    password: 'password123',
                    name: '이영희',
                    phone: '010-2345-6789',
                    userType: 'CORPORATE',
                    status: 'ACTIVE',
                    memberCode: 'COR001',
                    companyName: '(주)ABC무역',
                    businessNumber: '123-45-67890',
                    emailVerified: true,
                    createdAt: new Date('2024-01-12').toISOString(),
                    updatedAt: new Date().toISOString()
                }
            ];
            localStorage.setItem('ycs_users', JSON.stringify(users));
        }

        if (!localStorage.getItem('ycs_orders')) {
            const orders = [
                {
                    id: 1,
                    orderNumber: 'YCS-240115-001',
                    userId: 2,
                    status: 'DELIVERED',
                    shippingType: 'SEA',
                    country: 'thailand',
                    postalCode: '10110',
                    recipientName: 'Somchai Jaidee',
                    recipientPhone: '+66-81-234-5678',
                    recipientAddress: '123 Sukhumvit Road, Watthana District, Bangkok',
                    recipientPostalCode: '10110',
                    trackingNumber: 'EE123456789KR',
                    totalCbm: 0.003900,
                    totalWeight: 2.30,
                    requiresExtraRecipient: false,
                    noMemberCode: false,
                    storageLocation: 'A-01-03',
                    storageArea: 'A열 1행 3번',
                    arrivedAt: new Date('2024-01-18T10:30:00').toISOString(),
                    actualWeight: 2.50,
                    repackingRequested: true,
                    repackingCompleted: true,
                    warehouseNotes: '상품 상태 양호, 1kg씩 3개 포장으로 분할 완료',
                    shippedAt: new Date('2024-01-22T09:00:00').toISOString(),
                    deliveredAt: new Date('2024-01-25T14:30:00').toISOString(),
                    specialRequests: '깨지기 쉬운 상품이므로 신중히 포장 부탁드립니다.',
                    createdAt: new Date('2024-01-15T14:20:00').toISOString(),
                    updatedAt: new Date().toISOString()
                },
                {
                    id: 2,
                    orderNumber: 'YCS-240120-002',
                    userId: 3,
                    status: 'BILLING',
                    shippingType: 'AIR',
                    country: 'thailand',
                    postalCode: '10500',
                    recipientName: 'Niran Patel',
                    recipientPhone: '+66-82-345-6789',
                    recipientAddress: '456 Silom Road, Bang Rak District, Bangkok',
                    recipientPostalCode: '10500',
                    trackingNumber: 'EE234567890KR',
                    totalCbm: 0.028500,
                    totalWeight: 3.20,
                    requiresExtraRecipient: false,
                    noMemberCode: false,
                    storageLocation: 'B-02-15',
                    storageArea: 'B열 2행 15번',
                    arrivedAt: new Date('2024-01-23T11:15:00').toISOString(),
                    actualWeight: 3.50,
                    repackingRequested: false,
                    repackingCompleted: false,
                    warehouseNotes: '포장 상태 양호',
                    shippedAt: new Date('2024-01-28T08:30:00').toISOString(),
                    deliveredAt: new Date('2024-01-30T16:45:00').toISOString(),
                    specialRequests: null,
                    createdAt: new Date('2024-01-20T09:45:00').toISOString(),
                    updatedAt: new Date().toISOString()
                },
                {
                    id: 3,
                    orderNumber: 'YCS-240125-003',
                    userId: 2,
                    status: 'REPACKING',
                    shippingType: 'SEA',
                    country: 'thailand',
                    postalCode: '10400',
                    recipientName: 'Araya Wongsak',
                    recipientPhone: '+66-83-456-7890',
                    recipientAddress: '789 Phetchaburi Road, Ratchathewi District, Bangkok',
                    recipientPostalCode: '10400',
                    trackingNumber: 'EE345678901KR',
                    totalCbm: 0.012500,
                    totalWeight: 1.80,
                    requiresExtraRecipient: false,
                    noMemberCode: false,
                    storageLocation: 'A-03-07',
                    storageArea: 'A열 3행 7번',
                    arrivedAt: new Date('2024-01-28T13:20:00').toISOString(),
                    actualWeight: 1.95,
                    repackingRequested: true,
                    repackingCompleted: false,
                    warehouseNotes: '리패킹 진행 중',
                    shippedAt: null,
                    deliveredAt: null,
                    specialRequests: '취급 주의',
                    createdAt: new Date('2024-01-25T16:30:00').toISOString(),
                    updatedAt: new Date().toISOString()
                }
            ];
            localStorage.setItem('ycs_orders', JSON.stringify(orders));
        }

        if (!localStorage.getItem('ycs_bank_accounts')) {
            const bankAccounts = [
                {
                    id: 1,
                    bankName: 'KB국민은행',
                    accountNumber: '123-456-789012',
                    accountHolder: 'YCS물류(주)',
                    isActive: true,
                    isDefault: true,
                    displayOrder: 1,
                    description: '주계좌',
                    createdAt: new Date('2024-01-01').toISOString(),
                    updatedAt: new Date().toISOString()
                },
                {
                    id: 2,
                    bankName: '신한은행',
                    accountNumber: '110-123-456789',
                    accountHolder: 'YCS물류(주)',
                    isActive: true,
                    isDefault: false,
                    displayOrder: 2,
                    description: '부계좌',
                    createdAt: new Date('2024-01-01').toISOString(),
                    updatedAt: new Date().toISOString()
                },
                {
                    id: 3,
                    bankName: '우리은행',
                    accountNumber: '1002-123-456789',
                    accountHolder: 'YCS물류(주)',
                    isActive: true,
                    isDefault: false,
                    displayOrder: 3,
                    description: '예비계좌',
                    createdAt: new Date('2024-01-01').toISOString(),
                    updatedAt: new Date().toISOString()
                }
            ];
            localStorage.setItem('ycs_bank_accounts', JSON.stringify(bankAccounts));
        }
    }

    // API 시뮬레이션 지연
    delay(ms = 300) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    // GET 요청 시뮬레이션
    async get(endpoint) {
        await this.delay();
        
        try {
            if (endpoint.startsWith('/orders')) {
                return this.handleOrdersGet(endpoint);
            } else if (endpoint.startsWith('/bank-accounts')) {
                return this.handleBankAccountsGet(endpoint);
            }
            
            throw new Error(`Unknown endpoint: ${endpoint}`);
        } catch (error) {
            console.error('Mock API Error:', error);
            throw error;
        }
    }

    // 주문 관련 GET 처리
    handleOrdersGet(endpoint) {
        const orders = JSON.parse(localStorage.getItem('ycs_orders') || '[]');
        const users = JSON.parse(localStorage.getItem('ycs_users') || '[]');
        
        // 사용자 정보를 주문에 조인
        const ordersWithUsers = orders.map(order => ({
            ...order,
            user: users.find(user => user.id === order.userId)
        }));

        if (endpoint === '/orders') {
            return {
                content: ordersWithUsers,
                totalElements: ordersWithUsers.length,
                totalPages: 1,
                number: 0,
                size: 20
            };
        }

        if (endpoint.match(/\/orders\/(\d+)$/)) {
            const id = parseInt(endpoint.split('/').pop());
            const order = ordersWithUsers.find(o => o.id === id);
            if (!order) throw new Error('Order not found');
            return order;
        }

        if (endpoint.match(/\/orders\/number\/(.+)$/)) {
            const orderNumber = endpoint.split('/').pop();
            const order = ordersWithUsers.find(o => o.orderNumber === orderNumber);
            if (!order) throw new Error('Order not found');
            return order;
        }

        if (endpoint.match(/\/orders\/user\/(\d+)$/)) {
            const userId = parseInt(endpoint.split('/').pop());
            const userOrders = ordersWithUsers.filter(o => o.userId === userId);
            return {
                content: userOrders,
                totalElements: userOrders.length,
                totalPages: 1,
                number: 0,
                size: 20
            };
        }

        if (endpoint === '/orders/warehouse') {
            const warehouseOrders = ordersWithUsers.filter(o => 
                o.storageLocation && ['ARRIVED', 'REPACKING'].includes(o.status)
            );
            return warehouseOrders;
        }

        if (endpoint.match(/\/orders\/warehouse\/user\/(\d+)$/)) {
            const userId = parseInt(endpoint.split('/').pop());
            const warehouseOrders = ordersWithUsers.filter(o => 
                o.userId === userId && o.storageLocation && ['ARRIVED', 'REPACKING'].includes(o.status)
            );
            return warehouseOrders;
        }

        if (endpoint.match(/\/orders\/stats\/user\/(\d+)$/)) {
            const userId = parseInt(endpoint.split('/').pop());
            const userOrders = orders.filter(o => o.userId === userId);
            return {
                total: userOrders.length,
                pending: userOrders.filter(o => o.status === 'PAYMENT_PENDING').length,
                completed: userOrders.filter(o => o.status === 'COMPLETED').length,
                shipping: userOrders.filter(o => o.status === 'SHIPPING').length
            };
        }

        throw new Error(`Unknown orders endpoint: ${endpoint}`);
    }

    // 은행계좌 관련 GET 처리
    handleBankAccountsGet(endpoint) {
        const accounts = JSON.parse(localStorage.getItem('ycs_bank_accounts') || '[]');

        if (endpoint === '/bank-accounts' || endpoint === '/bank-accounts/') {
            return accounts.filter(a => a.isActive).sort((a, b) => a.displayOrder - b.displayOrder);
        }

        if (endpoint === '/bank-accounts/all') {
            return accounts;
        }

        if (endpoint === '/bank-accounts/default') {
            const defaultAccount = accounts.find(a => a.isDefault);
            if (!defaultAccount) throw new Error('Default account not found');
            return defaultAccount;
        }

        if (endpoint.match(/\/bank-accounts\/(\d+)$/)) {
            const id = parseInt(endpoint.split('/').pop());
            const account = accounts.find(a => a.id === id);
            if (!account) throw new Error('Account not found');
            return account;
        }

        throw new Error(`Unknown bank-accounts endpoint: ${endpoint}`);
    }

    // POST 요청 시뮬레이션
    async post(endpoint, data) {
        await this.delay();
        
        console.log(`Mock POST ${endpoint}:`, data);
        
        // 실제로는 데이터를 로컬스토리지에 저장하거나 업데이트
        return { success: true, message: 'Created successfully', data };
    }

    // PUT 요청 시뮬레이션
    async put(endpoint, data) {
        await this.delay();
        
        console.log(`Mock PUT ${endpoint}:`, data);
        
        // 실제로는 데이터를 로컬스토리지에서 업데이트
        return { success: true, message: 'Updated successfully', data };
    }

    // DELETE 요청 시뮬레이션
    async delete(endpoint) {
        await this.delay();
        
        console.log(`Mock DELETE ${endpoint}`);
        
        return { success: true, message: 'Deleted successfully' };
    }
}

// 전역 Mock API 인스턴스
window.mockAPI = new MockAPI();

// 실제 API 호출을 위한 유틸리티 함수
window.apiCall = async function(method, endpoint, data = null) {
    const isProduction = false; // 개발 중에는 false
    
    if (isProduction) {
        // 실제 API 호출
        const url = `http://localhost:8080/api${endpoint}`;
        const options = {
            method,
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        };
        
        if (data) {
            options.body = JSON.stringify(data);
        }
        
        const response = await fetch(url, options);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } else {
        // Mock API 호출
        switch (method.toUpperCase()) {
            case 'GET':
                return await window.mockAPI.get(endpoint);
            case 'POST':
                return await window.mockAPI.post(endpoint, data);
            case 'PUT':
                return await window.mockAPI.put(endpoint, data);
            case 'DELETE':
                return await window.mockAPI.delete(endpoint);
            default:
                throw new Error(`Unsupported method: ${method}`);
        }
    }
};