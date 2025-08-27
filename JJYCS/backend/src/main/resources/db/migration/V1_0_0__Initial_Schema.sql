-- YCS LMS 초기 데이터베이스 스키마
-- Version: 1.0.0
-- Date: 2025-08-23

-- 사용자 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    user_type ENUM('GENERAL', 'CORPORATE', 'PARTNER', 'ADMIN', 'WAREHOUSE') NOT NULL,
    status ENUM('PENDING', 'ACTIVE', 'INACTIVE', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    member_code VARCHAR(20),
    
    -- 기업 사용자 추가 정보
    company_name VARCHAR(200),
    company_address VARCHAR(100),
    business_number VARCHAR(50),
    
    -- 파트너 사용자 추가 정보
    partner_region VARCHAR(100),
    commission_rate DOUBLE,
    
    -- 승인 관련
    approved_by VARCHAR(100),
    approved_at TIMESTAMP NULL,
    rejected_at TIMESTAMP NULL,
    rejection_reason VARCHAR(500),
    approval_notes VARCHAR(500),
    
    -- 이메일 인증
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_users_email (email),
    INDEX idx_users_user_type (user_type),
    INDEX idx_users_status (status),
    INDEX idx_users_member_code (member_code)
);

-- 주문 테이블
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    
    -- 주문 상태
    status ENUM('RECEIVED', 'ARRIVED', 'REPACKING', 'SHIPPING', 'DELIVERED', 
                'BILLING', 'PAYMENT_PENDING', 'PAYMENT_CONFIRMED', 'COMPLETED') NOT NULL DEFAULT 'RECEIVED',
    shipping_type ENUM('SEA', 'AIR') NOT NULL,
    
    -- 배송지 정보
    country VARCHAR(50) NOT NULL,
    postal_code VARCHAR(10),
    
    -- 수취인 정보
    recipient_name VARCHAR(100) NOT NULL,
    recipient_phone VARCHAR(20),
    recipient_address VARCHAR(500),
    recipient_postal_code VARCHAR(10),
    
    -- 송장 정보
    tracking_number VARCHAR(50),
    
    -- CBM 및 무게 정보
    total_cbm DECIMAL(10,6) DEFAULT 0,
    total_weight DECIMAL(8,2) DEFAULT 0,
    
    -- 비즈니스 규칙 플래그
    requires_extra_recipient BOOLEAN NOT NULL DEFAULT FALSE,
    no_member_code BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- 창고 정보
    storage_location VARCHAR(20),
    storage_area VARCHAR(50),
    arrived_at TIMESTAMP NULL,
    actual_weight DECIMAL(8,2),
    
    -- 리패킹 정보
    repacking_requested BOOLEAN NOT NULL DEFAULT FALSE,
    repacking_completed BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- 배송 정보
    shipped_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    
    -- 기타
    special_requests VARCHAR(1000),
    warehouse_notes VARCHAR(1000),
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_order_number (order_number),
    INDEX idx_orders_tracking_number (tracking_number),
    INDEX idx_orders_created_at (created_at),
    INDEX idx_orders_shipping_type (shipping_type)
);

-- 주문 품목 테이블
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    hs_code VARCHAR(20) NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity INT NOT NULL,
    weight DECIMAL(8,2) NOT NULL,
    width DECIMAL(8,2) NOT NULL,
    height DECIMAL(8,2) NOT NULL,
    depth DECIMAL(8,2) NOT NULL,
    cbm DECIMAL(10,6) GENERATED ALWAYS AS (width * height * depth / 1000000) STORED,
    unit_price DECIMAL(10,2),
    total_price DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_items_order_id (order_id),
    INDEX idx_order_items_hs_code (hs_code)
);

-- 주문 히스토리 테이블
CREATE TABLE order_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    previous_status ENUM('RECEIVED', 'ARRIVED', 'REPACKING', 'SHIPPING', 'DELIVERED', 
                         'BILLING', 'PAYMENT_PENDING', 'PAYMENT_CONFIRMED', 'COMPLETED') NOT NULL,
    new_status ENUM('RECEIVED', 'ARRIVED', 'REPACKING', 'SHIPPING', 'DELIVERED', 
                    'BILLING', 'PAYMENT_PENDING', 'PAYMENT_CONFIRMED', 'COMPLETED') NOT NULL,
    change_reason VARCHAR(1000),
    changed_by VARCHAR(100),
    notes VARCHAR(500),
    storage_location VARCHAR(20),
    storage_area VARCHAR(50),
    tracking_number VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_history_order_id (order_id),
    INDEX idx_order_history_created_at (created_at)
);

-- 청구서 테이블
CREATE TABLE billing (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    
    -- 청구서 발행 정보
    proforma_issued BOOLEAN NOT NULL DEFAULT FALSE,
    proforma_date TIMESTAMP NULL,
    final_issued BOOLEAN NOT NULL DEFAULT FALSE,
    final_date TIMESTAMP NULL,
    
    -- 요금 정보
    shipping_fee DECIMAL(10,2),
    local_delivery_fee DECIMAL(10,2),
    repacking_fee DECIMAL(10,2),
    handling_fee DECIMAL(10,2),
    insurance_fee DECIMAL(10,2),
    customs_fee DECIMAL(10,2),
    tax DECIMAL(10,2),
    total DECIMAL(10,2),
    
    -- 결제 정보
    payment_method ENUM('BANK_TRANSFER', 'CREDIT_CARD', 'PAYPAL', 'CASH') NOT NULL DEFAULT 'BANK_TRANSFER',
    payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    depositor_name VARCHAR(100),
    payment_reference VARCHAR(100),
    payment_date TIMESTAMP NULL,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_billing_payment_status (payment_status),
    INDEX idx_billing_created_at (created_at)
);

-- 알림 테이블
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_id BIGINT NULL,
    
    -- 알림 내용
    type ENUM('ORDER_STATUS_CHANGE', 'PAYMENT_REQUIRED', 'PAYMENT_CONFIRMED',
              'SHIPMENT_UPDATE', 'CBM_THRESHOLD_EXCEEDED', 'THB_THRESHOLD_EXCEEDED',
              'MEMBER_CODE_MISSING', 'SYSTEM_ANNOUNCEMENT', 'WAREHOUSE_NOTIFICATION') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    action_url VARCHAR(100),
    
    -- 읽음 상태
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    
    -- 이메일/SMS
    is_email_sent BOOLEAN NOT NULL DEFAULT FALSE,
    email_sent_at TIMESTAMP NULL,
    is_sms_required BOOLEAN NOT NULL DEFAULT FALSE,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_notifications_user_id (user_id),
    INDEX idx_notifications_order_id (order_id),
    INDEX idx_notifications_is_read (is_read),
    INDEX idx_notifications_created_at (created_at)
);

-- 파일 테이블
CREATE TABLE files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uploaded_by BIGINT,
    
    -- 파일 정보
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    
    -- 파일 분류
    file_type ENUM('IMAGE', 'DOCUMENT', 'ARCHIVE', 'VIDEO', 'AUDIO', 'OTHER') NOT NULL,
    category ENUM('ORDER_DOCUMENT', 'REPACK_PHOTO', 'DAMAGE_PHOTO', 'INVOICE',
                  'USER_AVATAR', 'BUSINESS_LICENSE', 'ID_DOCUMENT',
                  'SYSTEM_LOGO', 'BANNER', 'NOTICE_ATTACHMENT',
                  'TEMP', 'OTHER') NOT NULL,
    
    -- 연관 엔티티 정보
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    
    -- 이미지 메타데이터
    width INT,
    height INT,
    thumbnail_path VARCHAR(500),
    
    -- 파일 상태
    is_active BOOLEAN DEFAULT TRUE,
    is_public BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NULL,
    
    -- 다운로드 통계
    download_count BIGINT DEFAULT 0,
    last_download_at TIMESTAMP NULL,
    
    -- 기타
    description VARCHAR(1000),
    tags VARCHAR(500),
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    INDEX idx_files_uploaded_by (uploaded_by),
    INDEX idx_files_category (category),
    INDEX idx_files_related_entity (related_entity_type, related_entity_id),
    INDEX idx_files_created_at (created_at),
    INDEX idx_files_expires_at (expires_at)
);

-- 감사 로그 테이블
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- 작업 수행자 정보
    performed_by VARCHAR(100) NOT NULL,
    performer_ip VARCHAR(50),
    user_agent VARCHAR(500),
    
    -- 작업 정보
    action_type ENUM('USER_LOGIN', 'USER_LOGOUT', 'USER_REGISTER', 'USER_APPROVE', 'USER_REJECT',
                     'USER_STATUS_CHANGE', 'USER_UPDATE', 'USER_DELETE',
                     'ORDER_CREATE', 'ORDER_UPDATE', 'ORDER_STATUS_CHANGE', 'ORDER_DELETE',
                     'PAYMENT_CREATE', 'PAYMENT_UPDATE', 'PAYMENT_CONFIRM',
                     'NOTIFICATION_SEND', 'NOTIFICATION_READ',
                     'SYSTEM_CONFIG_CHANGE', 'DATA_EXPORT', 'DATA_IMPORT', 'BULK_OPERATION',
                     'FILE_UPLOAD', 'FILE_DOWNLOAD', 'FILE_DELETE',
                     'API_ACCESS', 'SECURITY_EVENT') NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    description VARCHAR(200),
    
    -- 변경 데이터
    old_values TEXT,
    new_values TEXT,
    
    -- 메타데이터
    session_id VARCHAR(100),
    request_id VARCHAR(50),
    result_status ENUM('SUCCESS', 'FAILURE', 'ERROR') NOT NULL DEFAULT 'SUCCESS',
    error_message VARCHAR(1000),
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_audit_logs_performed_by (performed_by),
    INDEX idx_audit_logs_action_type (action_type),
    INDEX idx_audit_logs_entity (entity_type, entity_id),
    INDEX idx_audit_logs_created_at (created_at),
    INDEX idx_audit_logs_result_status (result_status)
);

-- 은행 계좌 테이블
CREATE TABLE bank_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_name VARCHAR(50) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_holder VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_bank_accounts_is_active (is_active),
    INDEX idx_bank_accounts_is_default (is_default)
);

-- 초기 관리자 사용자 생성
INSERT INTO users (email, password, name, phone, user_type, status, member_code, email_verified, created_at, updated_at) 
VALUES ('admin@ycs.com', '$2a$10$E1B/mG0ib9VmjNJ2YdX9N.YxPqI9GK6dxVl.k3rQpF8JG8F7BF7z2', 
        '시스템 관리자', '02-1234-5678', 'ADMIN', 'ACTIVE', 'ADM001', true, NOW(), NOW());

-- 기본 은행 계좌 정보 생성
INSERT INTO bank_accounts (bank_name, account_number, account_holder, is_active, is_default, display_order, description, created_at, updated_at) 
VALUES 
    ('KB국민은행', '123-456-789012', 'YCS물류(주)', true, true, 1, '주계좌', NOW(), NOW()),
    ('신한은행', '110-123-456789', 'YCS물류(주)', true, false, 2, '부계좌', NOW(), NOW());