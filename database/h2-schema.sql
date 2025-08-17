-- YCS LMS H2 Database Schema
-- H2 Database용 DDL 스크립트

-- 기존 테이블 삭제 (초기화용)
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS config CASCADE;
DROP TABLE IF EXISTS partner_referrals CASCADE;
DROP TABLE IF EXISTS shipment_tracking CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS estimates CASCADE;
DROP TABLE IF EXISTS scan_events CASCADE;
DROP TABLE IF EXISTS inventory CASCADE;
DROP TABLE IF EXISTS warehouses CASCADE;
DROP TABLE IF EXISTS order_boxes CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS addresses CASCADE;
DROP TABLE IF EXISTS warehouse_profiles CASCADE;
DROP TABLE IF EXISTS partner_profiles CASCADE;
DROP TABLE IF EXISTS enterprise_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 사용자 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    member_code VARCHAR(20) UNIQUE,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expires_at TIMESTAMP,
    last_login_at TIMESTAMP,
    login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    agree_terms BOOLEAN DEFAULT FALSE,
    agree_privacy BOOLEAN DEFAULT FALSE,
    agree_marketing BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 기업 프로필 테이블
CREATE TABLE enterprise_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    business_number VARCHAR(50) NOT NULL,
    representative VARCHAR(100) NOT NULL,
    business_address TEXT,
    vat_type VARCHAR(20) DEFAULT 'GENERAL',
    tax_invoice_email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 파트너 프로필 테이블
CREATE TABLE partner_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(200),
    commission_rate DECIMAL(5,4) DEFAULT 0.0500,
    referral_code VARCHAR(20) UNIQUE,
    bank_name VARCHAR(50),
    bank_account VARCHAR(100),
    account_holder VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 창고 프로필 테이블
CREATE TABLE warehouse_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    permissions TEXT,
    department VARCHAR(100),
    position VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 주소 테이블
CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),
    phone VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 창고 테이블
CREATE TABLE warehouses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    type VARCHAR(20) DEFAULT 'MAIN',
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    capacity_cbm DECIMAL(12,3),
    current_usage_cbm DECIMAL(12,3) DEFAULT 0,
    manager_id BIGINT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    operating_hours VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (manager_id) REFERENCES users(id)
);

-- 주문 테이블
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'REQUESTED',
    order_type VARCHAR(20) DEFAULT 'SEA',
    recipient_name VARCHAR(100),
    recipient_phone VARCHAR(20),
    recipient_address TEXT,
    recipient_zip_code VARCHAR(20),
    recipient_country VARCHAR(100),
    urgency VARCHAR(20) DEFAULT 'NORMAL',
    needs_repacking BOOLEAN DEFAULT FALSE,
    special_instructions TEXT,
    total_amount DECIMAL(12,2),
    currency VARCHAR(3) DEFAULT 'THB',
    total_cbm_m3 DECIMAL(10,6),
    requires_extra_recipient BOOLEAN DEFAULT FALSE,
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    payment_method VARCHAR(20) DEFAULT 'PREPAID',
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    created_by BIGINT,
    assigned_warehouse_id BIGINT,
    estimated_cost DECIMAL(12,2),
    actual_cost DECIMAL(12,2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (assigned_warehouse_id) REFERENCES warehouses(id)
);

-- 주문 아이템 테이블
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_url VARCHAR(500),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2),
    total_price DECIMAL(12,2),
    options TEXT,
    weight_kg DECIMAL(8,3),
    dimensions VARCHAR(100),
    hs_code VARCHAR(20),
    is_prohibited BOOLEAN DEFAULT FALSE,
    requires_inspection BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 주문 박스 테이블
CREATE TABLE order_boxes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    box_number INT NOT NULL,
    length_cm DECIMAL(8,2) NOT NULL,
    width_cm DECIMAL(8,2) NOT NULL,
    height_cm DECIMAL(8,2) NOT NULL,
    weight_kg DECIMAL(8,3),
    cbm_m3 DECIMAL(10,6),
    contents TEXT,
    barcode VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 재고 테이블
CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    label_code VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING',
    location_code VARCHAR(20),
    zone VARCHAR(20),
    rack VARCHAR(20),
    shelf VARCHAR(20),
    bin VARCHAR(20),
    scan_count INT DEFAULT 0,
    last_scan_at TIMESTAMP,
    received_at TIMESTAMP,
    shipped_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 스캔 이벤트 테이블
CREATE TABLE scan_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    inventory_id BIGINT,
    label_code VARCHAR(50) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    location_code VARCHAR(20),
    previous_status VARCHAR(30),
    new_status VARCHAR(30),
    device_id VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (inventory_id) REFERENCES inventory(id)
);

-- 결제 테이블
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    payment_code VARCHAR(50) UNIQUE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'THB',
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    gateway VARCHAR(50),
    gateway_response TEXT,
    paid_at TIMESTAMP,
    failed_at TIMESTAMP,
    refunded_at TIMESTAMP,
    refund_amount DECIMAL(12,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 배송 추적 테이블
CREATE TABLE shipment_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    tracking_number VARCHAR(100) UNIQUE,
    carrier VARCHAR(100),
    carrier_code VARCHAR(20),
    status VARCHAR(30) DEFAULT 'PREPARING',
    current_location VARCHAR(200),
    destination VARCHAR(200),
    estimated_delivery TIMESTAMP,
    actual_delivery TIMESTAMP,
    tracking_url VARCHAR(500),
    events TEXT,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 설정 테이블
CREATE TABLE config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_name VARCHAR(100) UNIQUE NOT NULL,
    value TEXT,
    description TEXT,
    category VARCHAR(50),
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 감사 로그 테이블
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 인덱스 생성
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_member_code ON users(member_code);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_orders_order_code ON orders(order_code);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_inventory_label_code ON inventory(label_code);
CREATE INDEX idx_inventory_warehouse_id ON inventory(warehouse_id);
CREATE INDEX idx_inventory_status ON inventory(status);
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(payment_status);
CREATE INDEX idx_shipment_tracking_order_id ON shipment_tracking(order_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
