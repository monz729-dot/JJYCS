-- YCS LMS SQLite Database Schema
-- 사용자 테이블
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    user_type TEXT CHECK(user_type IN ('general', 'corporate', 'partner', 'warehouse', 'admin')) NOT NULL,
    status TEXT CHECK(status IN ('pending', 'approved', 'rejected')) DEFAULT 'pending',
    member_code VARCHAR(20) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 기업 프로필 테이블
CREATE TABLE enterprise_profiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    business_number VARCHAR(50) NOT NULL,
    representative VARCHAR(100) NOT NULL,
    address TEXT,
    vat_type TEXT CHECK(vat_type IN ('general', 'zero_rate')) DEFAULT 'general',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 파트너 프로필 테이블
CREATE TABLE partner_profiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    commission_rate DECIMAL(5,4) DEFAULT 0.0500,
    referral_code VARCHAR(20) UNIQUE,
    bank_account VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 창고 프로필 테이블
CREATE TABLE warehouse_profiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    warehouse_id INTEGER NOT NULL,
    permissions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 주소 테이블
CREATE TABLE addresses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    type TEXT CHECK(type IN ('shipping', 'billing')) NOT NULL,
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
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    order_type TEXT CHECK(order_type IN ('air', 'sea')) NOT NULL,
    status TEXT CHECK(status IN ('pending', 'processing', 'shipped', 'delivered')) DEFAULT 'pending',
    shipping_method VARCHAR(50),
    total_items INTEGER DEFAULT 0,
    total_weight_kg DECIMAL(10,3),
    total_cbm_m3 DECIMAL(10,6),
    estimated_amount DECIMAL(12,2),
    actual_amount DECIMAL(12,2),
    currency VARCHAR(3) DEFAULT 'KRW',
    requires_extra_recipient BOOLEAN DEFAULT FALSE,
    is_delayed_no_code BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 주문 아이템 테이블
CREATE TABLE order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    description TEXT,
    quantity INTEGER NOT NULL,
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
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    box_number INTEGER NOT NULL,
    length_cm DECIMAL(8,2) NOT NULL,
    width_cm DECIMAL(8,2) NOT NULL,
    height_cm DECIMAL(8,2) NOT NULL,
    weight_kg DECIMAL(8,3),
    cbm_m3 DECIMAL(10,6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 창고 테이블
CREATE TABLE warehouses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    type TEXT CHECK(type IN ('main', 'sub')) DEFAULT 'main',
    capacity_cbm DECIMAL(12,3),
    current_usage_cbm DECIMAL(12,3) DEFAULT 0,
    manager_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manager_id) REFERENCES users(id)
);

-- 재고 테이블
CREATE TABLE inventory (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    warehouse_id INTEGER NOT NULL,
    order_id INTEGER NOT NULL,
    label_code VARCHAR(50) UNIQUE NOT NULL,
    status TEXT CHECK(status IN ('created', 'inbound_completed', 'ready_for_outbound', 'outbound_completed', 'hold')) DEFAULT 'created',
    location_code VARCHAR(20),
    scan_count INTEGER DEFAULT 0,
    last_scan_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 스캔 이벤트 테이블
CREATE TABLE scan_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    warehouse_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    label_code VARCHAR(50) NOT NULL,
    scan_type TEXT CHECK(scan_type IN ('inbound', 'outbound', 'location_update', 'outbound_ready', 'hold', 'mixbox')) NOT NULL,
    location_code VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 견적 테이블
CREATE TABLE estimates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    estimate_type TEXT CHECK(estimate_type IN ('first', 'final')) NOT NULL,
    shipping_cost DECIMAL(12,2),
    customs_fee DECIMAL(12,2),
    handling_fee DECIMAL(12,2),
    insurance_fee DECIMAL(12,2),
    total_amount DECIMAL(12,2),
    currency VARCHAR(3) DEFAULT 'KRW',
    valid_until TIMESTAMP,
    status TEXT CHECK(status IN ('pending', 'approved', 'rejected')) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 결제 테이블
CREATE TABLE payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'KRW',
    payment_method TEXT CHECK(payment_method IN ('card', 'bank_transfer', 'virtual_account')) NOT NULL,
    payment_status TEXT CHECK(payment_status IN ('pending', 'completed', 'failed', 'cancelled')) DEFAULT 'pending',
    transaction_id VARCHAR(100),
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 배송 추적 테이블
CREATE TABLE shipment_tracking (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    tracking_number VARCHAR(100) UNIQUE,
    carrier VARCHAR(100),
    status TEXT CHECK(status IN ('preparing', 'in_transit', 'customs', 'delivered')) DEFAULT 'preparing',
    current_location VARCHAR(200),
    estimated_delivery TIMESTAMP NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 파트너 추천 테이블
CREATE TABLE partner_referrals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    partner_id INTEGER NOT NULL,
    referred_user_id INTEGER NOT NULL,
    referral_code VARCHAR(20) NOT NULL,
    commission_rate DECIMAL(5,4),
    order_id INTEGER,
    commission_amount DECIMAL(12,2),
    status TEXT CHECK(status IN ('pending', 'paid', 'cancelled')) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (partner_id) REFERENCES users(id),
    FOREIGN KEY (referred_user_id) REFERENCES users(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 설정 테이블
CREATE TABLE config (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    key_name VARCHAR(100) UNIQUE NOT NULL,
    value TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 감사 로그 테이블
CREATE TABLE audit_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id INTEGER,
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 인덱스 생성
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_member_code ON users(member_code);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_inventory_label_code ON inventory(label_code);
CREATE INDEX idx_inventory_warehouse_id ON inventory(warehouse_id);
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(payment_status);