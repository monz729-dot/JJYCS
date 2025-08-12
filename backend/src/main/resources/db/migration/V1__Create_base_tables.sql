-- V1__Create_base_tables.sql
-- YCS LMS 기본 테이블 생성

-- 사용자 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role ENUM('individual', 'enterprise', 'partner', 'warehouse', 'admin') NOT NULL,
    status ENUM('pending_approval', 'active', 'suspended', 'deleted') DEFAULT 'active',
    member_code VARCHAR(20) NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    agree_terms BOOLEAN DEFAULT FALSE,
    agree_privacy BOOLEAN DEFAULT FALSE,
    agree_marketing BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    
    INDEX idx_users_email (email),
    INDEX idx_users_member_code (member_code),
    INDEX idx_users_role_status (role, status)
);

-- 기업 프로필 테이블
CREATE TABLE enterprise_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    business_number VARCHAR(50),
    business_address TEXT,
    representative_name VARCHAR(100),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    company_type VARCHAR(50),
    approval_status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    approval_date TIMESTAMP NULL,
    approved_by BIGINT NULL,
    rejection_reason TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id),
    INDEX idx_enterprise_user_id (user_id),
    INDEX idx_enterprise_approval_status (approval_status)
);

-- 파트너 프로필 테이블
CREATE TABLE partner_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    partner_type ENUM('affiliate', 'referral', 'corporate') NOT NULL,
    business_info TEXT,
    referral_code VARCHAR(20) UNIQUE,
    commission_rate DECIMAL(5,2) DEFAULT 0.00,
    approval_status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    approval_date TIMESTAMP NULL,
    approved_by BIGINT NULL,
    rejection_reason TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id),
    INDEX idx_partner_user_id (user_id),
    INDEX idx_partner_referral_code (referral_code),
    INDEX idx_partner_approval_status (approval_status)
);

-- 창고 프로필 테이블
CREATE TABLE warehouse_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    warehouse_code VARCHAR(20) NOT NULL UNIQUE,
    warehouse_name VARCHAR(200) NOT NULL,
    location VARCHAR(200),
    manager_name VARCHAR(100),
    manager_phone VARCHAR(20),
    capacity_info TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_warehouse_user_id (user_id),
    INDEX idx_warehouse_code (warehouse_code)
);

-- 주소 테이블
CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    address_type ENUM('pickup', 'delivery', 'billing') NOT NULL,
    recipient_name VARCHAR(100),
    recipient_phone VARCHAR(20),
    country_code VARCHAR(3) NOT NULL,
    postal_code VARCHAR(20),
    address_line1 VARCHAR(500) NOT NULL,
    address_line2 VARCHAR(500),
    city VARCHAR(100),
    state_province VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_addresses_user_id (user_id),
    INDEX idx_addresses_type (address_type)
);

-- 주문 테이블
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    status ENUM('requested', 'confirmed', 'in_progress', 'shipped', 'delivered', 'cancelled', 'delayed') DEFAULT 'requested',
    order_type ENUM('air', 'sea') DEFAULT 'sea',
    
    -- 수취인 정보
    recipient_name VARCHAR(100) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    recipient_address TEXT NOT NULL,
    recipient_country VARCHAR(3) NOT NULL,
    recipient_postal_code VARCHAR(20),
    
    -- 비즈니스 룰 결과
    total_amount DECIMAL(12,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'THB',
    total_cbm_m3 DECIMAL(10,6) AS (
        (SELECT COALESCE(SUM(width_cm * height_cm * depth_cm / 1000000), 0) 
         FROM order_boxes WHERE order_id = id)
    ) VIRTUAL,
    requires_extra_recipient BOOLEAN DEFAULT FALSE,
    
    -- 배송 정보
    pickup_address TEXT,
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    tracking_number VARCHAR(100),
    
    -- 비용 정보
    shipping_cost DECIMAL(12,2),
    local_shipping_cost DECIMAL(12,2),
    repack_cost DECIMAL(12,2),
    total_cost DECIMAL(12,2),
    
    -- 메타 정보
    notes TEXT,
    special_instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_orders_order_code (order_code),
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_order_type (order_type),
    INDEX idx_orders_created_at (created_at)
);

-- 주문 상품 테이블
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    amount DECIMAL(12,2) AS (quantity * unit_price) STORED,
    currency VARCHAR(3) DEFAULT 'THB',
    
    -- 분류 코드
    ems_code VARCHAR(20),
    hs_code VARCHAR(20),
    
    -- 상품 상세
    description TEXT,
    brand VARCHAR(100),
    model VARCHAR(100),
    weight_kg DECIMAL(8,3),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_ems_code (ems_code),
    INDEX idx_order_items_hs_code (hs_code)
);

-- 주문 박스 테이블
CREATE TABLE order_boxes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    box_number INT NOT NULL,
    
    -- 치수 (cm)
    width_cm DECIMAL(8,2) NOT NULL,
    height_cm DECIMAL(8,2) NOT NULL,
    depth_cm DECIMAL(8,2) NOT NULL,
    
    -- CBM 자동 계산
    cbm_m3 DECIMAL(10,6) AS (width_cm * height_cm * depth_cm / 1000000) STORED,
    
    -- 무게
    weight_kg DECIMAL(8,3),
    
    -- 박스 상태
    status ENUM('registered', 'packed', 'shipped', 'delivered') DEFAULT 'registered',
    label_code VARCHAR(50) UNIQUE,
    
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_boxes_order_id (order_id),
    INDEX idx_order_boxes_label_code (label_code),
    INDEX idx_order_boxes_status (status)
);

-- 리패킹 사진 테이블
CREATE TABLE repack_photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    box_id BIGINT,
    photo_type ENUM('before', 'during', 'after') NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    uploaded_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (box_id) REFERENCES order_boxes(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    INDEX idx_repack_photos_order_id (order_id),
    INDEX idx_repack_photos_box_id (box_id)
);

-- 창고 테이블
CREATE TABLE warehouses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    location VARCHAR(200),
    address TEXT,
    contact_phone VARCHAR(20),
    manager_user_id BIGINT,
    capacity_limit INT,
    status ENUM('active', 'maintenance', 'closed') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (manager_user_id) REFERENCES users(id),
    INDEX idx_warehouses_code (warehouse_code),
    INDEX idx_warehouses_status (status)
);

-- 재고 테이블
CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    box_id BIGINT,
    warehouse_id BIGINT NOT NULL,
    
    status ENUM('inbound', 'stored', 'processing', 'outbound', 'hold', 'mixbox') DEFAULT 'inbound',
    location_code VARCHAR(50),
    
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    shipped_at TIMESTAMP NULL,
    
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (box_id) REFERENCES order_boxes(id) ON DELETE CASCADE,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    INDEX idx_inventory_order_id (order_id),
    INDEX idx_inventory_warehouse_id (warehouse_id),
    INDEX idx_inventory_status (status)
);

-- 스캔 이벤트 테이블
CREATE TABLE scan_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    box_id BIGINT,
    label_code VARCHAR(50) NOT NULL,
    
    scan_type ENUM('inbound', 'outbound', 'hold', 'mixbox', 'check') NOT NULL,
    warehouse_id BIGINT NOT NULL,
    scanned_by BIGINT NOT NULL,
    
    location_code VARCHAR(50),
    scan_result ENUM('success', 'error', 'warning') DEFAULT 'success',
    error_message TEXT,
    
    scanned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (box_id) REFERENCES order_boxes(id) ON DELETE CASCADE,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (scanned_by) REFERENCES users(id),
    INDEX idx_scan_events_order_id (order_id),
    INDEX idx_scan_events_label_code (label_code),
    INDEX idx_scan_events_scan_type (scan_type),
    INDEX idx_scan_events_scanned_at (scanned_at)
);

-- 초기 데이터 삽입
INSERT INTO warehouses (warehouse_code, name, location, address, contact_phone, status) VALUES
('WH001', 'Bangkok Main Warehouse', 'Bangkok', '123 Sukhumvit Road, Bangkok 10110, Thailand', '+66-2-123-4567', 'active'),
('WH002', 'Chiang Mai Warehouse', 'Chiang Mai', '456 Chang Khlan Road, Chiang Mai 50100, Thailand', '+66-53-234-5678', 'active');

-- 기본 관리자 계정 (비밀번호: admin123!)
INSERT INTO users (email, password_hash, name, role, status, member_code, email_verified, agree_terms, agree_privacy) VALUES
('admin@ycs-lms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE.ByOKgdKoXwb2Pq', 'System Admin', 'admin', 'active', 'ADMIN001', TRUE, TRUE, TRUE);