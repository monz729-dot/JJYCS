# 더미 데이터 로드 가이드

## 🔧 데이터베이스 설정 및 더미 데이터 로드

### 1. MySQL 데이터베이스 생성
```sql
-- MySQL 접속 후 실행
CREATE DATABASE ycs_lms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ycs_lms;
```

### 2. 테이블 스키마 생성
먼저 테이블을 생성해야 합니다. 다음 순서로 실행:

```sql
-- 사용자 테이블
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    user_type ENUM('general', 'corporate', 'partner', 'warehouse', 'admin') NOT NULL,
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    member_code VARCHAR(20) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 기업 프로필 테이블
CREATE TABLE enterprise_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    business_number VARCHAR(50) NOT NULL,
    representative VARCHAR(100) NOT NULL,
    address TEXT,
    vat_type ENUM('general', 'zero_rate') DEFAULT 'general',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 파트너 프로필 테이블
CREATE TABLE partner_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    commission_rate DECIMAL(5,4) DEFAULT 0.0500,
    referral_code VARCHAR(20) UNIQUE,
    bank_account VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 창고 프로필 테이블
CREATE TABLE warehouse_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    permissions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 주소 테이블
CREATE TABLE addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type ENUM('shipping', 'billing') NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    country VARCHAR(2) NOT NULL,
    province VARCHAR(100),
    district VARCHAR(100),
    sub_district VARCHAR(100),
    postal_code VARCHAR(20),
    address_line TEXT,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 주문 테이블
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    order_type ENUM('air', 'sea') NOT NULL,
    status ENUM('pending', 'processing', 'shipped', 'delivered') DEFAULT 'pending',
    shipping_method VARCHAR(50),
    total_items INT DEFAULT 0,
    total_weight_kg DECIMAL(10,3),
    total_cbm_m3 DECIMAL(10,6),
    estimated_amount DECIMAL(12,2),
    actual_amount DECIMAL(12,2),
    currency VARCHAR(3) DEFAULT 'KRW',
    requires_extra_recipient BOOLEAN DEFAULT FALSE,
    is_delayed_no_code BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 주문 아이템 테이블
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    description TEXT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2),
    total_price DECIMAL(12,2),
    weight_kg DECIMAL(8,3),
    hs_code VARCHAR(20),
    ems_restricted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 주문 박스 테이블
CREATE TABLE order_boxes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    box_number INT NOT NULL,
    length_cm DECIMAL(8,2) NOT NULL,
    width_cm DECIMAL(8,2) NOT NULL,
    height_cm DECIMAL(8,2) NOT NULL,
    weight_kg DECIMAL(8,3),
    cbm_m3 DECIMAL(10,6) GENERATED ALWAYS AS ((length_cm * width_cm * height_cm) / 1000000) STORED,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 창고 테이블
CREATE TABLE warehouses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    type ENUM('main', 'sub') DEFAULT 'main',
    capacity_cbm DECIMAL(12,3),
    current_usage_cbm DECIMAL(12,3) DEFAULT 0,
    manager_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manager_id) REFERENCES users(id)
);

-- 재고 테이블
CREATE TABLE inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    label_code VARCHAR(50) UNIQUE NOT NULL,
    status ENUM('created', 'inbound_completed', 'ready_for_outbound', 'outbound_completed', 'hold') DEFAULT 'created',
    location_code VARCHAR(20),
    scan_count INT DEFAULT 0,
    last_scan_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 스캔 이벤트 테이블
CREATE TABLE scan_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    label_code VARCHAR(50) NOT NULL,
    scan_type ENUM('inbound', 'outbound', 'location_update', 'outbound_ready', 'hold', 'mixbox') NOT NULL,
    location_code VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 견적 테이블
CREATE TABLE estimates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    estimate_type ENUM('first', 'final') NOT NULL,
    shipping_cost DECIMAL(12,2),
    customs_fee DECIMAL(12,2),
    handling_fee DECIMAL(12,2),
    insurance_fee DECIMAL(12,2),
    total_amount DECIMAL(12,2),
    currency VARCHAR(3) DEFAULT 'KRW',
    valid_until TIMESTAMP,
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 결제 테이블
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'KRW',
    payment_method ENUM('card', 'bank_transfer', 'virtual_account') NOT NULL,
    payment_status ENUM('pending', 'completed', 'failed', 'cancelled') DEFAULT 'pending',
    transaction_id VARCHAR(100),
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 배송 추적 테이블
CREATE TABLE shipment_tracking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    tracking_number VARCHAR(100) UNIQUE,
    carrier VARCHAR(100),
    status ENUM('preparing', 'in_transit', 'customs', 'delivered') DEFAULT 'preparing',
    current_location VARCHAR(200),
    estimated_delivery TIMESTAMP NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 파트너 추천 테이블
CREATE TABLE partner_referrals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    partner_id BIGINT NOT NULL,
    referred_user_id BIGINT NOT NULL,
    referral_code VARCHAR(20) NOT NULL,
    commission_rate DECIMAL(5,4),
    order_id BIGINT,
    commission_amount DECIMAL(12,2),
    status ENUM('pending', 'paid', 'cancelled') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (partner_id) REFERENCES users(id),
    FOREIGN KEY (referred_user_id) REFERENCES users(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 설정 테이블
CREATE TABLE config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    key_name VARCHAR(100) UNIQUE NOT NULL,
    value TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 감사 로그 테이블
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 3. 더미 데이터 로드
테이블 생성 후 더미 데이터를 로드:

```bash
# MySQL CLI를 통해 더미 데이터 로드
mysql -u [username] -p ycs_lms < src/main/resources/db/dummy_data.sql

# 또는 MySQL Workbench에서 직접 실행
# File > Open SQL Script > dummy_data.sql 선택 후 실행
```

### 4. 데이터 확인
```sql
-- 사용자 수 확인
SELECT user_type, COUNT(*) as count FROM users GROUP BY user_type;

-- 주문 상태별 확인
SELECT status, COUNT(*) as count FROM orders GROUP BY status;

-- 재고 상태별 확인  
SELECT status, COUNT(*) as count FROM inventory GROUP BY status;

-- 결제 완료 주문 확인
SELECT o.order_number, o.actual_amount, p.payment_status 
FROM orders o 
JOIN payments p ON o.id = p.order_id 
WHERE p.payment_status = 'completed';
```

## 📊 예상 결과

### 사용자 데이터
| user_type | count |
|-----------|-------|
| general   | 1     |
| corporate | 1     |
| partner   | 1     |
| warehouse | 1     |
| admin     | 1     |

### 주문 데이터
| status     | count |
|------------|-------|
| pending    | 2     |
| processing | 2     |
| shipped    | 1     |
| delivered  | 1     |

### 재고 데이터
| status              | count |
|---------------------|-------|
| created             | 1     |
| inbound_completed   | 2     |
| ready_for_outbound  | 1     |
| outbound_completed  | 2     |

### 결제 데이터
- 총 결제 완료: 3건
- 총 매출: ₩1,099,000 (118,000 + 149,000 + 832,000)

## 🧪 API 테스트

더미 데이터 로드 후 다음 API들을 테스트할 수 있습니다:

```bash
# 대시보드 통계
curl http://localhost:8080/api/admin/dashboard/stats

# 주문 상태 분포
curl http://localhost:8080/api/admin/dashboard/order-status

# 최근 주문
curl http://localhost:8080/api/admin/dashboard/recent-orders

# 최근 사용자
curl http://localhost:8080/api/admin/dashboard/recent-users

# 시스템 상태
curl http://localhost:8080/api/admin/dashboard/system-status
```

## 🔧 Spring Boot 설정

더미 데이터를 자동으로 로드하려면 `application.yml`에 추가:

```yaml
spring:
  sql:
    init:
      mode: always
      data-locations: classpath:db/dummy_data.sql
      continue-on-error: true
```

**주의**: 운영 환경에서는 `mode: never`로 설정하여 실제 데이터가 덮어써지지 않도록 해야 합니다.

## 🚀 테스트 계정 로그인

더미 데이터 로드 후 다음 계정들로 로그인 테스트:

1. **일반회원**: general@test.com / Test123!@#
2. **기업회원**: corporate@test.com / Test123!@#  
3. **파트너**: partner@test.com / Test123!@#
4. **창고관리자**: warehouse@test.com / Test123!@#
5. **시스템관리자**: admin@test.com / Test123!@#

각 계정별로 연동된 실제 데이터(주문, 재고, 결제 등)를 확인할 수 있습니다.