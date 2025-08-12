-- YSC LMS Database Schema
-- MySQL 8.0+ with Flyway Migration Support
-- Created: 2024-01-15
-- Version: 1.0

-- ================================================================
-- 1. USERS & AUTHENTICATION
-- ================================================================

-- 기본 사용자 테이블
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role ENUM('individual', 'enterprise', 'partner', 'warehouse', 'admin') NOT NULL DEFAULT 'individual',
    status ENUM('active', 'pending_approval', 'suspended', 'inactive') NOT NULL DEFAULT 'pending_approval',
    member_code VARCHAR(50) UNIQUE, -- 승인 후 생성, NULL이면 지연 처리
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_secret VARCHAR(32),
    password_reset_token VARCHAR(255),
    password_reset_expires_at TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    agree_terms BOOLEAN NOT NULL DEFAULT FALSE,
    agree_privacy BOOLEAN NOT NULL DEFAULT FALSE,
    agree_marketing BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    INDEX idx_email (email),
    INDEX idx_member_code (member_code),
    INDEX idx_role_status (role, status),
    INDEX idx_created_at (created_at)
);

-- 기업 회원 추가 정보
CREATE TABLE enterprise_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    business_number VARCHAR(50) NOT NULL UNIQUE,
    company_address TEXT NOT NULL,
    ceo_name VARCHAR(100) NOT NULL,
    business_type VARCHAR(100),
    employee_count INT,
    annual_revenue DECIMAL(15,2),
    business_registration_file VARCHAR(500), -- S3 URL
    tax_certificate_file VARCHAR(500), -- S3 URL
    approval_note TEXT,
    approved_by BIGINT,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_business_number (business_number),
    INDEX idx_approval (approved_by, approved_at)
);

-- 파트너 회원 추가 정보
CREATE TABLE partner_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    partner_type ENUM('referral', 'campaign') NOT NULL DEFAULT 'referral',
    referral_code VARCHAR(50) NOT NULL UNIQUE,
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    account_holder VARCHAR(100),
    commission_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0500, -- 5%
    total_referrals INT NOT NULL DEFAULT 0,
    total_commission DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    pending_commission DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    last_settlement_at TIMESTAMP NULL,
    approval_note TEXT,
    approved_by BIGINT,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uk_referral_code (referral_code),
    INDEX idx_partner_type (partner_type),
    INDEX idx_commission (total_commission, pending_commission)
);

-- 창고 회원 추가 정보
CREATE TABLE warehouse_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    warehouse_name VARCHAR(255) NOT NULL,
    warehouse_address TEXT NOT NULL,
    capacity_description VARCHAR(500),
    operating_hours VARCHAR(100),
    contact_person VARCHAR(100),
    contact_phone VARCHAR(20),
    facilities TEXT, -- JSON format
    approval_note TEXT,
    approved_by BIGINT,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
);

-- 주소록 (사용자별 저장된 주소)
CREATE TABLE addresses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type ENUM('sender', 'recipient') NOT NULL DEFAULT 'recipient',
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address_line1 VARCHAR(500) NOT NULL,
    address_line2 VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country CHAR(2) NOT NULL, -- ISO 3166-1 alpha-2
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_type (user_id, type),
    INDEX idx_country (country)
);

-- ================================================================
-- 2. ORDERS & SHIPPING
-- ================================================================

-- 주문 테이블
CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_code VARCHAR(50) NOT NULL UNIQUE, -- ORD-2024-001
    user_id BIGINT NOT NULL,
    status ENUM(
        'requested', 'confirmed', 'payment_pending', 'payment_completed', 
        'preparing', 'ready_to_ship', 'shipped', 'in_transit', 
        'customs_clearance', 'out_for_delivery', 'delivered', 
        'delayed', 'cancelled', 'returned'
    ) NOT NULL DEFAULT 'requested',
    order_type ENUM('sea', 'air') NOT NULL DEFAULT 'sea',
    
    -- 수취인 정보
    recipient_name VARCHAR(100) NOT NULL,
    recipient_phone VARCHAR(20),
    recipient_address TEXT NOT NULL,
    recipient_zip_code VARCHAR(20),
    recipient_country CHAR(2) NOT NULL,
    
    -- 배송 정보
    urgency ENUM('normal', 'urgent') NOT NULL DEFAULT 'normal',
    needs_repacking BOOLEAN NOT NULL DEFAULT FALSE,
    special_instructions TEXT,
    
    -- 금액 정보
    total_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency CHAR(3) NOT NULL DEFAULT 'THB',
    
    -- 비즈니스 룰 플래그
    requires_extra_recipient BOOLEAN NOT NULL DEFAULT FALSE, -- THB 1,500 초과
    total_cbm_m3 DECIMAL(10,6) GENERATED ALWAYS AS (
        (SELECT COALESCE(SUM(cbm_m3), 0) FROM order_boxes WHERE order_id = orders.id)
    ) STORED, -- CBM 총합 가상컬럼
    
    -- 견적 및 결제
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    payment_method ENUM('prepaid', 'postpaid') NOT NULL DEFAULT 'prepaid',
    payment_status ENUM('pending', 'completed', 'failed', 'refunded') NOT NULL DEFAULT 'pending',
    
    -- 시스템 정보
    created_by BIGINT,
    assigned_warehouse_id BIGINT,
    estimated_cost DECIMAL(15,2),
    actual_cost DECIMAL(15,2),
    notes TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_warehouse_id) REFERENCES warehouse_profiles(id) ON DELETE SET NULL,
    
    UNIQUE KEY uk_order_code (order_code),
    INDEX idx_user_status (user_id, status),
    INDEX idx_order_type (order_type),
    INDEX idx_recipient_country (recipient_country),
    INDEX idx_created_at (created_at),
    INDEX idx_delivery_date (estimated_delivery_date),
    INDEX idx_cbm_threshold (total_cbm_m3), -- CBM 29 초과 검색용
    INDEX idx_amount_currency (total_amount, currency) -- THB 1500 초과 검색용
);

-- 주문 상품 테이블
CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    item_order INT NOT NULL DEFAULT 1, -- 순서
    name VARCHAR(500) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    quantity INT NOT NULL DEFAULT 1,
    unit_weight DECIMAL(8,3), -- kg
    unit_price DECIMAL(15,2),
    total_amount DECIMAL(15,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'THB',
    hs_code VARCHAR(20), -- Harmonized System Code
    ems_code VARCHAR(50), -- EMS 코드
    country_of_origin CHAR(2),
    brand VARCHAR(100),
    model VARCHAR(100),
    is_restricted BOOLEAN NOT NULL DEFAULT FALSE,
    restriction_note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_item (order_id, item_order),
    INDEX idx_hs_code (hs_code),
    INDEX idx_category (category),
    INDEX idx_amount_currency (total_amount, currency)
);

-- 주문 박스 테이블 (CBM 계산 핵심)
CREATE TABLE order_boxes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    box_number INT NOT NULL DEFAULT 1,
    label_code VARCHAR(100) NOT NULL UNIQUE, -- BOX-2024-001-01
    qr_code_url VARCHAR(500), -- QR 코드 이미지 URL
    
    -- 박스 치수 (cm)
    width_cm DECIMAL(8,2) NOT NULL,
    height_cm DECIMAL(8,2) NOT NULL,
    depth_cm DECIMAL(8,2) NOT NULL,
    
    -- CBM 자동 계산 (가상컬럼)
    cbm_m3 DECIMAL(10,6) GENERATED ALWAYS AS (
        (width_cm * height_cm * depth_cm) / 1000000
    ) STORED,
    
    -- 박스 무게
    weight_kg DECIMAL(8,3),
    
    -- 박스 상태
    status ENUM(
        'created', 'inbound_pending', 'inbound_completed', 
        'ready_for_outbound', 'outbound_completed', 
        'shipped', 'hold', 'mixbox'
    ) NOT NULL DEFAULT 'created',
    
    -- 창고 정보
    warehouse_id BIGINT,
    warehouse_location VARCHAR(100), -- A-01-15
    inbound_date TIMESTAMP NULL,
    outbound_date TIMESTAMP NULL,
    
    -- 배송 정보
    tracking_number VARCHAR(100),
    carrier VARCHAR(50),
    shipped_date TIMESTAMP NULL,
    
    -- 포함된 아이템 (JSON)
    item_ids JSON, -- [1, 2, 3]
    
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (warehouse_id) REFERENCES warehouse_profiles(id) ON DELETE SET NULL,
    
    UNIQUE KEY uk_label_code (label_code),
    INDEX idx_order_box (order_id, box_number),
    INDEX idx_status (status),
    INDEX idx_cbm (cbm_m3), -- CBM 기반 검색/정렬
    INDEX idx_warehouse_location (warehouse_id, warehouse_location),
    INDEX idx_tracking_number (tracking_number),
    INDEX idx_dates (inbound_date, outbound_date)
);

-- 리패킹 사진 테이블
CREATE TABLE repack_photos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    box_id BIGINT NOT NULL,
    type ENUM('before', 'after', 'damage', 'label') NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size INT,
    mime_type VARCHAR(100),
    uploaded_by BIGINT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (box_id) REFERENCES order_boxes(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_box_type (box_id, type),
    INDEX idx_uploaded_at (created_at)
);

-- ================================================================
-- 3. WAREHOUSE MANAGEMENT
-- ================================================================

-- 창고 테이블
CREATE TABLE warehouses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(20) NOT NULL UNIQUE, -- WH001
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    country CHAR(2) NOT NULL,
    manager_id BIGINT, -- warehouse_profiles.user_id
    capacity_description VARCHAR(500),
    operating_hours VARCHAR(200),
    contact_info JSON, -- 연락처 정보
    facilities JSON, -- 시설 정보
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uk_warehouse_code (code),
    INDEX idx_country_active (country, is_active)
);

-- 재고 테이블 (현재 창고 보관 현황)
CREATE TABLE inventory (
    id BIGINT NOT NULL AUTO_INCREMENT,
    warehouse_id BIGINT NOT NULL,
    box_id BIGINT NOT NULL,
    location_code VARCHAR(100), -- A-01-15
    status ENUM('stored', 'processing', 'shipped', 'hold') NOT NULL DEFAULT 'stored',
    inbound_date TIMESTAMP NOT NULL,
    expected_outbound_date DATE,
    last_scanned_at TIMESTAMP,
    days_in_warehouse INT GENERATED ALWAYS AS (
        DATEDIFF(COALESCE(last_scanned_at, CURRENT_TIMESTAMP), inbound_date)
    ) STORED,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE,
    FOREIGN KEY (box_id) REFERENCES order_boxes(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_warehouse_box (warehouse_id, box_id),
    INDEX idx_location (warehouse_id, location_code),
    INDEX idx_status (status),
    INDEX idx_days_in_warehouse (days_in_warehouse),
    INDEX idx_expected_outbound (expected_outbound_date)
);

-- 스캔 이벤트 로그
CREATE TABLE scan_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_type ENUM('inbound', 'outbound', 'hold', 'mixbox', 'move', 'count') NOT NULL,
    box_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    scanned_by BIGINT NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    location_code VARCHAR(100),
    batch_id VARCHAR(100), -- 일괄 처리시 배치 ID
    device_info VARCHAR(200), -- 스캔 디바이스 정보
    gps_coordinates VARCHAR(100), -- 위도,경도
    notes TEXT,
    scan_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (box_id) REFERENCES order_boxes(id) ON DELETE CASCADE,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE,
    FOREIGN KEY (scanned_by) REFERENCES users(id) ON DELETE RESTRICT,
    
    INDEX idx_box_events (box_id, scan_timestamp),
    INDEX idx_warehouse_events (warehouse_id, scan_timestamp),
    INDEX idx_batch_id (batch_id),
    INDEX idx_event_type (event_type, scan_timestamp),
    INDEX idx_scanned_by (scanned_by, scan_timestamp)
);

-- ================================================================
-- 4. SHIPPING & TRACKING
-- ================================================================

-- 배송 추적 테이블
CREATE TABLE shipment_tracking (
    id BIGINT NOT NULL AUTO_INCREMENT,
    box_id BIGINT NOT NULL,
    tracking_number VARCHAR(100) NOT NULL,
    carrier VARCHAR(50) NOT NULL, -- EMS, DHL, FedEx
    service_type VARCHAR(50), -- Standard, Express
    
    -- 현재 상태
    current_status ENUM(
        'dispatched', 'in_transit', 'customs_clearance', 
        'out_for_delivery', 'delivered', 'failed_delivery', 
        'returned', 'lost'
    ) NOT NULL DEFAULT 'dispatched',
    current_location VARCHAR(200),
    
    -- 예상 및 실제 배송일
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    
    -- API 동기화 정보
    last_api_sync TIMESTAMP,
    api_response JSON, -- 외부 API 전체 응답 저장
    sync_error_count INT NOT NULL DEFAULT 0,
    last_sync_error TEXT,
    
    -- 배송 이벤트 (JSON 배열)
    tracking_events JSON, -- [{status, location, timestamp, description}]
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (box_id) REFERENCES order_boxes(id) ON DELETE CASCADE,
    
    UNIQUE KEY uk_tracking_number (tracking_number),
    INDEX idx_box_tracking (box_id, tracking_number),
    INDEX idx_carrier_status (carrier, current_status),
    INDEX idx_delivery_date (estimated_delivery_date, actual_delivery_date),
    INDEX idx_api_sync (last_api_sync, sync_error_count)
);

-- ================================================================
-- 5. ESTIMATES & PAYMENTS
-- ================================================================

-- 견적 테이블
CREATE TABLE estimates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    version ENUM('first', 'second', 'third') NOT NULL DEFAULT 'first',
    estimate_type ENUM('shipping', 'additional', 'correction') NOT NULL DEFAULT 'shipping',
    
    -- 배송 정보
    shipping_method ENUM('sea', 'air') NOT NULL,
    carrier VARCHAR(50),
    service_level ENUM('standard', 'express') NOT NULL DEFAULT 'standard',
    estimated_days INT NOT NULL,
    
    -- 비용 breakdown
    shipping_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    local_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00, -- TODO: 로컬운임 체계 확정 필요
    repack_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    insurance_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    customs_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    fuel_surcharge DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    handling_fee DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    
    -- 총액
    subtotal DECIMAL(15,2) GENERATED ALWAYS AS (
        shipping_fee + local_fee + repack_fee + insurance_fee + 
        customs_fee + fuel_surcharge + handling_fee
    ) STORED,
    total_amount DECIMAL(15,2) GENERATED ALWAYS AS (
        shipping_fee + local_fee + repack_fee + insurance_fee + 
        customs_fee + fuel_surcharge + handling_fee + tax_amount
    ) STORED,
    currency CHAR(3) NOT NULL DEFAULT 'KRW',
    
    -- 견적 상태
    status ENUM('draft', 'sent', 'accepted', 'rejected', 'expired', 'cancelled') NOT NULL DEFAULT 'draft',
    valid_until TIMESTAMP NOT NULL,
    
    -- 응답 정보
    responded_at TIMESTAMP NULL,
    response_note TEXT,
    
    -- 생성 정보
    created_by BIGINT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    
    INDEX idx_order_version (order_id, version),
    INDEX idx_status (status, valid_until),
    INDEX idx_total_amount (total_amount, currency),
    INDEX idx_created_by (created_by, created_at)
);

-- 결제 테이블
CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    estimate_id BIGINT,
    payment_code VARCHAR(50) NOT NULL UNIQUE, -- PAY-2024-001
    
    -- 결제 정보
    amount DECIMAL(15,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'KRW',
    payment_method ENUM('credit_card', 'bank_transfer', 'paypal', 'prepaid_balance') NOT NULL,
    
    -- 상태
    status ENUM('pending', 'processing', 'completed', 'failed', 'cancelled', 'refunded') NOT NULL DEFAULT 'pending',
    
    -- 외부 결제 정보
    gateway_provider VARCHAR(50), -- PG사
    gateway_transaction_id VARCHAR(200),
    gateway_response JSON,
    
    -- 환불 정보
    refund_amount DECIMAL(15,2),
    refund_reason TEXT,
    refunded_at TIMESTAMP NULL,
    
    -- 날짜
    paid_at TIMESTAMP NULL,
    failed_at TIMESTAMP NULL,
    failure_reason TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT,
    FOREIGN KEY (estimate_id) REFERENCES estimates(id) ON DELETE SET NULL,
    
    UNIQUE KEY uk_payment_code (payment_code),
    INDEX idx_order_payment (order_id, status),
    INDEX idx_gateway_transaction (gateway_provider, gateway_transaction_id),
    INDEX idx_payment_dates (paid_at, failed_at),
    INDEX idx_amount_currency (amount, currency)
);

-- ================================================================
-- 6. PARTNER & REFERRAL SYSTEM
-- ================================================================

-- 파트너 추천 테이블
CREATE TABLE partner_referrals (
    id BIGINT NOT NULL AUTO_INCREMENT,
    partner_id BIGINT NOT NULL, -- partner_profiles.user_id
    referral_code VARCHAR(50) NOT NULL,
    campaign_name VARCHAR(255),
    
    -- 추천받은 사용자
    referred_user_id BIGINT,
    referred_email VARCHAR(255) NOT NULL,
    signup_completed BOOLEAN NOT NULL DEFAULT FALSE,
    first_order_id BIGINT,
    
    -- 커미션 정보
    commission_rate DECIMAL(5,4) NOT NULL, -- 추천 당시 비율
    order_amount DECIMAL(15,2),
    commission_amount DECIMAL(15,2),
    commission_status ENUM('pending', 'approved', 'paid', 'cancelled') NOT NULL DEFAULT 'pending',
    commission_paid_at TIMESTAMP NULL,
    
    -- 추천 링크 정보
    referral_url VARCHAR(500),
    utm_source VARCHAR(100),
    utm_medium VARCHAR(100),
    utm_campaign VARCHAR(100),
    
    -- 클릭 및 전환 추적
    clicked_at TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    signup_at TIMESTAMP,
    first_order_at TIMESTAMP,
    
    valid_until TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (partner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (referred_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (first_order_id) REFERENCES orders(id) ON DELETE SET NULL,
    
    INDEX idx_partner_referrals (partner_id, signup_completed),
    INDEX idx_referral_code (referral_code),
    INDEX idx_referred_user (referred_user_id, signup_at),
    INDEX idx_commission_status (commission_status, commission_paid_at),
    INDEX idx_campaign (campaign_name, created_at)
);

-- 파트너 정산 테이블
CREATE TABLE partner_settlements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    partner_id BIGINT NOT NULL,
    settlement_code VARCHAR(50) NOT NULL UNIQUE, -- SETTLE-2024-001
    
    -- 정산 기간
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    
    -- 정산 금액
    total_referrals INT NOT NULL DEFAULT 0,
    total_orders INT NOT NULL DEFAULT 0,
    total_order_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_commission DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    previous_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    adjustments DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    final_amount DECIMAL(15,2) GENERATED ALWAYS AS (
        total_commission + previous_balance + adjustments
    ) STORED,
    
    -- 지급 정보
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    account_holder VARCHAR(100),
    payment_date DATE,
    payment_method ENUM('bank_transfer', 'paypal', 'check') NOT NULL DEFAULT 'bank_transfer',
    payment_reference VARCHAR(200),
    
    -- 상태
    status ENUM('draft', 'confirmed', 'paid', 'cancelled') NOT NULL DEFAULT 'draft',
    
    -- 세금 정보
    tax_withheld DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    tax_rate DECIMAL(5,4) NOT NULL DEFAULT 0.00,
    
    notes TEXT,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (partner_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    
    UNIQUE KEY uk_settlement_code (settlement_code),
    INDEX idx_partner_period (partner_id, period_start, period_end),
    INDEX idx_status_payment (status, payment_date),
    INDEX idx_final_amount (final_amount)
);

-- ================================================================
-- 7. SYSTEM CONFIGURATION
-- ================================================================

-- 시스템 설정 테이블
CREATE TABLE config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category VARCHAR(100) NOT NULL, -- business_rules, fees, notifications, etc.
    config_key VARCHAR(200) NOT NULL,
    config_value JSON NOT NULL,
    value_type ENUM('string', 'number', 'boolean', 'json', 'encrypted') NOT NULL DEFAULT 'string',
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    
    UNIQUE KEY uk_category_key (category, config_key),
    INDEX idx_category_active (category, is_active)
);

-- 감사 로그
CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    table_name VARCHAR(100) NOT NULL,
    record_id BIGINT NOT NULL,
    action ENUM('CREATE', 'UPDATE', 'DELETE', 'VIEW') NOT NULL,
    user_id BIGINT,
    user_role VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent TEXT,
    old_values JSON, -- 변경 전 값
    new_values JSON, -- 변경 후 값
    changed_fields JSON, -- 변경된 필드 목록
    request_id VARCHAR(100), -- 요청 추적용
    session_id VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_table_record (table_name, record_id),
    INDEX idx_user_action (user_id, action, created_at),
    INDEX idx_created_at (created_at),
    INDEX idx_request_session (request_id, session_id)
) PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- ================================================================
-- 8. 인덱스 최적화 및 가상 컬럼
-- ================================================================

-- CBM 29 초과 자동 air 전환을 위한 트리거
DELIMITER $$
CREATE TRIGGER tr_orders_cbm_check 
    AFTER UPDATE ON orders 
    FOR EACH ROW 
BEGIN
    -- CBM이 29를 초과하고 현재 sea인 경우 air로 자동 전환
    IF NEW.total_cbm_m3 > 29.0 AND NEW.order_type = 'sea' THEN
        UPDATE orders 
        SET order_type = 'air',
            notes = CONCAT(COALESCE(notes, ''), '\n[AUTO] CBM ', NEW.total_cbm_m3, ' 초과로 항공 배송 전환')
        WHERE id = NEW.id;
    END IF;
END$$

-- member_code 없는 경우 지연 상태 처리
CREATE TRIGGER tr_orders_member_code_check
    BEFORE INSERT ON orders
    FOR EACH ROW
BEGIN
    DECLARE user_member_code VARCHAR(50);
    
    SELECT member_code INTO user_member_code 
    FROM users 
    WHERE id = NEW.user_id;
    
    -- member_code가 없으면 지연 상태로 설정
    IF user_member_code IS NULL OR user_member_code = '' THEN
        SET NEW.status = 'delayed';
        SET NEW.notes = CONCAT(COALESCE(NEW.notes, ''), '\n[AUTO] 회원코드 미기재로 지연 처리');
    END IF;
END$$

-- THB 1500 초과시 추가 정보 필요 플래그 설정
CREATE TRIGGER tr_orders_thb_amount_check
    BEFORE INSERT ON orders
    FOR EACH ROW
BEGIN
    -- THB 1500 초과시 플래그 설정
    IF NEW.total_amount > 1500.0 AND NEW.currency = 'THB' THEN
        SET NEW.requires_extra_recipient = TRUE;
    END IF;
END$$

CREATE TRIGGER tr_orders_thb_amount_check_update
    BEFORE UPDATE ON orders
    FOR EACH ROW
BEGIN
    -- THB 1500 초과시 플래그 설정
    IF NEW.total_amount > 1500.0 AND NEW.currency = 'THB' THEN
        SET NEW.requires_extra_recipient = TRUE;
    ELSEIF NEW.total_amount <= 1500.0 AND NEW.currency = 'THB' THEN
        SET NEW.requires_extra_recipient = FALSE;
    END IF;
END$$

DELIMITER ;

-- ================================================================
-- 9. 초기 데이터 삽입
-- ================================================================

-- 기본 시스템 설정
INSERT INTO config (category, config_key, config_value, value_type, description) VALUES
('business_rules', 'cbm_threshold', '29.0', 'number', 'CBM 임계값 (초과시 자동 air 전환)'),
('business_rules', 'thb_amount_threshold', '1500.0', 'number', 'THB 금액 임계값 (초과시 추가 정보 필요)'),
('business_rules', 'auto_air_conversion', 'true', 'boolean', 'CBM 초과시 자동 air 전환 활성화'),
('business_rules', 'require_member_code', 'true', 'boolean', '회원코드 필수 여부'),

('fees', 'default_shipping_rates', '{"sea": {"per_kg": 8000, "per_cbm": 120000}, "air": {"per_kg": 15000, "per_cbm": 180000}}', 'json', '기본 배송비 요율'),
('fees', 'local_fees', '{"thailand": {"standard": 5000, "express": 8000}}', 'json', '현지 배송비 (TODO: 정확한 요율 필요)'),
('fees', 'repacking_fee', '3000', 'number', '리패킹 수수료'),
('fees', 'insurance_rate', '0.02', 'number', '보험료율 (2%)'),

('notifications', 'email_enabled', 'true', 'boolean', '이메일 알림 활성화'),
('notifications', 'sms_enabled', 'false', 'boolean', 'SMS 알림 활성화'),
('notifications', 'webhook_url', '""', 'string', '웹훅 URL (설정 필요)'),

('api', 'ems_api_endpoint', '""', 'encrypted', 'EMS API 엔드포인트 (TODO)'),
('api', 'hs_code_api_endpoint', '""', 'encrypted', 'HS 코드 API 엔드포인트 (TODO)'),
('api', 'exchange_rate_api_endpoint', '""', 'encrypted', '환율 API 엔드포인트 (TODO)');

-- 기본 관리자 계정 (초기 설정용, 실제 환경에서는 별도 생성)
-- 비밀번호: admin123! (bcrypt)
INSERT INTO users (email, password_hash, name, role, status, member_code, email_verified, agree_terms, agree_privacy) 
VALUES ('admin@ysc-lms.com', '$2a$12$rQ5.5YfZJ9YLqLc.w6YLbO.8FYfKqF.X.ZnL5nFX.YqL5nFXnL5nF', '시스템 관리자', 'admin', 'active', 'ADMIN001', TRUE, TRUE, TRUE);

-- 기본 창고
INSERT INTO warehouses (code, name, address, country, capacity_description, operating_hours, is_active) 
VALUES 
('WH001', '서울 메인 창고', '서울시 구로구 디지털로 300', 'KR', '1000CBM 보관 가능', '09:00-18:00 (평일)', TRUE),
('WH002', '방콕 현지 창고', 'Bangkok, Thailand', 'TH', '500CBM 보관 가능', '09:00-17:00 (평일)', TRUE);

-- ================================================================
-- 10. 성능 최적화 인덱스
-- ================================================================

-- 주문 검색 최적화
CREATE INDEX idx_orders_search ON orders(user_id, status, created_at DESC, order_type);
CREATE INDEX idx_orders_amount_search ON orders(total_amount, currency, created_at DESC);
CREATE INDEX idx_orders_delivery_search ON orders(estimated_delivery_date, actual_delivery_date, status);

-- 박스 검색 최적화  
CREATE INDEX idx_boxes_warehouse_search ON order_boxes(warehouse_id, status, inbound_date DESC);
CREATE INDEX idx_boxes_cbm_search ON order_boxes(cbm_m3 DESC, created_at DESC);

-- 추적 정보 최적화
CREATE INDEX idx_tracking_api_sync ON shipment_tracking(last_api_sync, sync_error_count);
CREATE INDEX idx_tracking_delivery_search ON shipment_tracking(estimated_delivery_date, current_status);

-- 파트너 성과 최적화
CREATE INDEX idx_partner_performance ON partner_referrals(partner_id, signup_completed, first_order_at DESC);
CREATE INDEX idx_partner_commission ON partner_referrals(commission_status, commission_paid_at);

-- 감사 로그 조회 최적화 (파티션 테이블이므로 연도별 인덱스)
CREATE INDEX idx_audit_logs_2024_user ON audit_logs(user_id, created_at DESC);
CREATE INDEX idx_audit_logs_2024_table ON audit_logs(table_name, record_id, created_at DESC);

-- ================================================================
-- 11. 뷰 (View) 정의
-- ================================================================

-- 주문 요약 뷰 (대시보드용)
CREATE VIEW v_order_summary AS
SELECT 
    o.id,
    o.order_code,
    o.status,
    o.order_type,
    o.total_amount,
    o.currency,
    o.total_cbm_m3,
    o.requires_extra_recipient,
    o.recipient_name,
    o.recipient_country,
    o.estimated_delivery_date,
    o.created_at,
    u.name as user_name,
    u.member_code,
    u.role as user_role,
    COUNT(ob.id) as total_boxes,
    COUNT(CASE WHEN st.current_status = 'delivered' THEN 1 END) as delivered_boxes
FROM orders o
JOIN users u ON o.user_id = u.id
LEFT JOIN order_boxes ob ON o.id = ob.order_id
LEFT JOIN shipment_tracking st ON ob.id = st.box_id
GROUP BY o.id;

-- 창고 재고 현황 뷰
CREATE VIEW v_warehouse_inventory AS
SELECT 
    w.id as warehouse_id,
    w.name as warehouse_name,
    COUNT(i.id) as total_boxes,
    COUNT(CASE WHEN i.status = 'stored' THEN 1 END) as stored_boxes,
    COUNT(CASE WHEN i.status = 'processing' THEN 1 END) as processing_boxes,
    COUNT(CASE WHEN i.status = 'hold' THEN 1 END) as hold_boxes,
    ROUND(SUM(ob.cbm_m3), 3) as total_cbm,
    ROUND(AVG(i.days_in_warehouse), 1) as avg_days_stored
FROM warehouses w
LEFT JOIN inventory i ON w.id = i.warehouse_id
LEFT JOIN order_boxes ob ON i.box_id = ob.id
WHERE w.is_active = TRUE
GROUP BY w.id, w.name;

-- 파트너 성과 요약 뷰
CREATE VIEW v_partner_performance AS
SELECT 
    pp.user_id as partner_id,
    u.name as partner_name,
    pp.referral_code,
    COUNT(pr.id) as total_referrals,
    COUNT(CASE WHEN pr.signup_completed = TRUE THEN 1 END) as successful_referrals,
    COUNT(CASE WHEN pr.first_order_id IS NOT NULL THEN 1 END) as orders_generated,
    COALESCE(SUM(pr.order_amount), 0) as total_order_amount,
    COALESCE(SUM(pr.commission_amount), 0) as total_commission,
    COALESCE(SUM(CASE WHEN pr.commission_status = 'paid' THEN pr.commission_amount ELSE 0 END), 0) as paid_commission,
    COALESCE(SUM(CASE WHEN pr.commission_status = 'pending' THEN pr.commission_amount ELSE 0 END), 0) as pending_commission
FROM partner_profiles pp
JOIN users u ON pp.user_id = u.id
LEFT JOIN partner_referrals pr ON pp.user_id = pr.partner_id
WHERE u.status = 'active'
GROUP BY pp.user_id, u.name, pp.referral_code;

-- ================================================================
-- 12. 저장 프로시저 (주요 비즈니스 로직)
-- ================================================================

DELIMITER $$

-- CBM 기반 배송 방법 추천
CREATE PROCEDURE sp_recommend_shipping_method(
    IN p_total_cbm DECIMAL(10,6),
    OUT p_recommended_method VARCHAR(10),
    OUT p_reason TEXT
)
BEGIN
    DECLARE cbm_threshold DECIMAL(10,6);
    
    -- 설정에서 CBM 임계값 조회
    SELECT CAST(config_value AS DECIMAL(10,6)) 
    INTO cbm_threshold
    FROM config 
    WHERE category = 'business_rules' AND config_key = 'cbm_threshold';
    
    IF p_total_cbm > cbm_threshold THEN
        SET p_recommended_method = 'air';
        SET p_reason = CONCAT('총 CBM ', p_total_cbm, 'm³이 임계값 ', cbm_threshold, 'm³을 초과하여 항공배송을 권장합니다.');
    ELSE
        SET p_recommended_method = 'sea';  
        SET p_reason = CONCAT('총 CBM ', p_total_cbm, 'm³이 임계값 이하이므로 해상배송을 권장합니다.');
    END IF;
END$$

-- 주문 총 비용 계산 (견적서 생성용)
CREATE PROCEDURE sp_calculate_order_cost(
    IN p_order_id BIGINT,
    IN p_shipping_method VARCHAR(10),
    OUT p_shipping_cost DECIMAL(15,2),
    OUT p_total_cost DECIMAL(15,2)
)
BEGIN
    DECLARE v_total_weight DECIMAL(10,3);
    DECLARE v_total_cbm DECIMAL(10,6);
    DECLARE v_rate_per_kg DECIMAL(15,2);
    DECLARE v_rate_per_cbm DECIMAL(15,2);
    DECLARE v_local_fee DECIMAL(15,2) DEFAULT 0;
    DECLARE v_repack_fee DECIMAL(15,2) DEFAULT 0;
    
    -- 주문의 총 무게와 CBM 계산
    SELECT 
        COALESCE(SUM(ob.weight_kg), 0),
        COALESCE(SUM(ob.cbm_m3), 0)
    INTO v_total_weight, v_total_cbm
    FROM order_boxes ob
    WHERE ob.order_id = p_order_id;
    
    -- 설정에서 배송비 요율 조회 (TODO: 실제 요율 API 연동 필요)
    SELECT 
        JSON_UNQUOTE(JSON_EXTRACT(config_value, CONCAT('$.', p_shipping_method, '.per_kg'))),
        JSON_UNQUOTE(JSON_EXTRACT(config_value, CONCAT('$.', p_shipping_method, '.per_cbm')))
    INTO v_rate_per_kg, v_rate_per_cbm
    FROM config
    WHERE category = 'fees' AND config_key = 'default_shipping_rates';
    
    -- 배송비 계산 (무게 기준 vs CBM 기준 중 높은 값)
    SET p_shipping_cost = GREATEST(
        v_total_weight * v_rate_per_kg,
        v_total_cbm * v_rate_per_cbm
    );
    
    -- TODO: 로컬 배송비, 리패킹 비용 등 추가 계산
    SET p_total_cost = p_shipping_cost + v_local_fee + v_repack_fee;
END$$

DELIMITER ;

-- ================================================================
-- 마이그레이션 완료
-- ================================================================

-- 현재 스키마 버전 기록
INSERT INTO config (category, config_key, config_value, value_type, description) 
VALUES ('system', 'schema_version', '1.0', 'string', '데이터베이스 스키마 버전');

INSERT INTO config (category, config_key, config_value, value_type, description) 
VALUES ('system', 'migration_date', CONCAT('"', NOW(), '"'), 'string', '마이그레이션 완료 일시');

-- 테이블 및 인덱스 생성 완료 확인
SELECT 
    'Schema Migration Completed' as status,
    COUNT(*) as total_tables
FROM information_schema.TABLES 
WHERE table_schema = DATABASE();

-- ================================================================
-- END OF SCHEMA
-- ================================================================