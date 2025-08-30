/**
 * YSC LMS Real API Client
 * Spring Boot 백엔드와 연동하는 실제 API
 */

class YSCAPI {
    constructor() {
        // API 요청은 상대 경로로 - server.js의 프록시를 통해 백엔드로 전달됨
        this.baseUrl = '/api';
    }

    // HTTP 요청을 위한 기본 fetch wrapper
    async request(endpoint, options = {}) {
        try {
            const url = `${this.baseUrl}${endpoint}`;
            const defaultHeaders = {
                'Content-Type': 'application/json',
            };

            // JWT 토큰이 있으면 Authorization 헤더 추가
            const token = localStorage.getItem('auth_token');
            if (token) {
                defaultHeaders['Authorization'] = `Bearer ${token}`;
            }

            const response = await fetch(url, {
                headers: { ...defaultHeaders, ...options.headers },
                ...options
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            }
            return await response.text();
        } catch (error) {
            console.error('API 요청 오류:', error);
            throw error;
        }
    }

    // 사용자 관련 API
    async getUsers() {
        const response = await this.request('/users');
        return response.content || response;
    }

    async getUserById(id) {
        return await this.request(`/users/${id}`);
    }

    async createUser(userData) {
        return await this.request('/users', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
    }

    async updateUser(id, userData) {
        return await this.request(`/users/${id}`, {
            method: 'PUT',
            body: JSON.stringify(userData)
        });
    }

    async deleteUser(id) {
        return await this.request(`/users/${id}`, {
            method: 'DELETE'
        });
    }

    // 주문 관련 API
    async getOrders(params = {}) {
        let queryString = '';
        if (Object.keys(params).length > 0) {
            queryString = '?' + new URLSearchParams(params).toString();
        }
        const response = await this.request(`/orders${queryString}`);
        return response.content || response;
    }

    async getOrderById(id) {
        const orders = await this.getOrders();
        return orders.find(order => order.id == id || order.orderNumber === id);
    }

    async getOrderByNumber(orderNumber) {
        const orders = await this.getOrders();
        return orders.find(order => order.orderNumber === orderNumber);
    }

    async createOrder(orderData) {
        return await this.request('/orders', {
            method: 'POST',
            body: JSON.stringify(orderData)
        });
    }

    async updateOrder(id, orderData) {
        return await this.request(`/orders/${id}`, {
            method: 'PUT',
            body: JSON.stringify(orderData)
        });
    }

    // 은행 계좌 관련 API
    async getBankAccounts() {
        return await this.request('/bank-accounts');
    }

    async createBankAccount(accountData) {
        return await this.request('/bank-accounts', {
            method: 'POST',
            body: JSON.stringify(accountData)
        });
    }

    async updateBankAccount(id, accountData) {
        return await this.request(`/bank-accounts/${id}`, {
            method: 'PUT',
            body: JSON.stringify(accountData)
        });
    }

    async deleteBankAccount(id) {
        return await this.request(`/bank-accounts/${id}`, {
            method: 'DELETE'
        });
    }

    // 통계 및 대시보드용 API
    async getDashboardStats() {
        const orders = await this.getOrders();
        const users = await this.getUsers();

        // 주문 상태별 통계
        const orderStats = orders.reduce((acc, order) => {
            acc[order.status] = (acc[order.status] || 0) + 1;
            return acc;
        }, {});

        // 사용자 타입별 통계
        const userStats = users.reduce((acc, user) => {
            acc[user.userType] = (acc[user.userType] || 0) + 1;
            return acc;
        }, {});

        return {
            totalOrders: orders.length,
            totalUsers: users.length,
            ordersByStatus: orderStats,
            usersByType: userStats,
            recentOrders: orders.slice(-5).reverse()
        };
    }

    // 인증 관련 API (실제 백엔드 연동)
    async login(email, password) {
        try {
            console.log(`🔐 [AUTH] 로그인 시도: ${email}`);
            const response = await this.request('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email, password })
            });
            
            if (response.success && response.accessToken) {
                // JWT 토큰과 사용자 정보를 localStorage에 저장
                localStorage.setItem('auth_token', response.accessToken);
                localStorage.setItem('refresh_token', response.refreshToken || '');
                localStorage.setItem('currentUser', JSON.stringify(response.user));
                
                console.log('✅ [AUTH] 로그인 성공:', response.user.name);
                return { success: true, user: response.user, token: response.accessToken };
            } else {
                throw new Error(response.message || '로그인 실패');
            }
        } catch (error) {
            console.error('❌ [AUTH] 로그인 실패:', error);
            throw new Error('로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
        }
    }

    async logout() {
        try {
            // 백엔드에 로그아웃 요청 (선택적)
            await this.request('/auth/logout', { method: 'POST' });
        } catch (error) {
            console.warn('로그아웃 API 호출 실패:', error);
        }
        
        // 로컬 스토리지 정리
        localStorage.removeItem('auth_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('currentUser');
        
        console.log('🚪 [AUTH] 로그아웃 완료');
        return { success: true };
    }

    getCurrentUser() {
        const userStr = localStorage.getItem('currentUser');
        return userStr ? JSON.parse(userStr) : null;
    }

    getAuthToken() {
        return localStorage.getItem('auth_token');
    }

    isAuthenticated() {
        return !!this.getAuthToken() && !!this.getCurrentUser();
    }

    // 사용자별 주문 목록
    async getOrdersByUser(userId) {
        const orders = await this.getOrders();
        return orders.filter(order => order.user && order.user.id == userId);
    }

    // 승인 대기 사용자 목록 (관리자용)
    async getPendingUsers() {
        const users = await this.getUsers();
        return users.filter(user => user.status === 'PENDING');
    }

    // 사용자 승인 (관리자용)
    async approveUser(userId, memberCode) {
        return await this.updateUser(userId, {
            status: 'ACTIVE',
            memberCode: memberCode
        });
    }

    // 사용자 거부 (관리자용)
    async rejectUser(userId, reason) {
        return await this.updateUser(userId, {
            status: 'REJECTED',
            rejectionReason: reason
        });
    }
}

// 전역 API 인스턴스
window.ycsAPI = new YSCAPI();