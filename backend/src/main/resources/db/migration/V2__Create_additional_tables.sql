-- V2__Create_additional_tables.sql
-- 추가 테이블: 배송추적, 결제, 견적, 파트너, 설정, 감사로그

-- 배송 추적 테이블
CREATE TABLE shipment_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    tracking_number VARCHAR(100) NOT NULL,
    carrier VARCHAR(100),
    service_type VARCHAR(50),
    
    status ENUM('pending', 'in_transit', 'customs', 'out_for_delivery', 'delivered', 'failed') DEFAULT 'pending',
    current_location VARCHAR(200),
    
    shipped_at TIMESTAMP NULL,
    estimated_delivery TIMESTAMP NULL,
    actual_delivery TIMESTAMP NULL,
    
    tracking_events JSON,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_shipment_tracking_order_id (order_id),
    INDEX idx_shipment_tracking_number (tracking_number),
    INDEX idx_shipment_tracking_status (status)
);

-- 결제 테이블
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_type ENUM('order', 'shipping', 'additional') NOT NULL,
    
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    
    payment_method ENUM('credit_card', 'bank_transfer', 'digital_wallet', 'cash') NOT NULL,
    payment_status ENUM('pending', 'completed', 'failed', 'refunded') DEFAULT 'pending',
    
    payment_reference VARCHAR(200),
    gateway_transaction_id VARCHAR(200),
    
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_status (payment_status),
    INDEX idx_payments_reference (payment_reference)
);

-- 견적 테이블
CREATE TABLE estimates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    estimate_version INT DEFAULT 1,
    estimate_type ENUM('preliminary', 'final') DEFAULT 'preliminary',
    
    -- 기본 비용
    base_shipping_cost DECIMAL(12,2) DEFAULT 0.00,
    local_delivery_cost DECIMAL(12,2) DEFAULT 0.00,
    repack_cost DECIMAL(12,2) DEFAULT 0.00,
    insurance_cost DECIMAL(12,2) DEFAULT 0.00,
    customs_fee DECIMAL(12,2) DEFAULT 0.00,
    handling_fee DECIMAL(12,2) DEFAULT 0.00,
    
    -- 총액
    subtotal DECIMAL(12,2) AS (base_shipping_cost + local_delivery_cost + repack_cost + insurance_cost + customs_fee + handling_fee) STORED,
    tax_amount DECIMAL(12,2) DEFAULT 0.00,
    total_amount DECIMAL(12,2) AS (subtotal + tax_amount) STORED,
    currency VARCHAR(3) DEFAULT 'THB',
    
    -- 견적 정보
    valid_until DATE,
    estimated_delivery_days INT,
    notes TEXT,
    
    status ENUM('draft', 'sent', 'accepted', 'rejected', 'expired') DEFAULT 'draft',
    created_by BIGINT,
    approved_by BIGINT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    INDEX idx_estimates_order_id (order_id),
    INDEX idx_estimates_status (status),
    INDEX idx_estimates_version (order_id, estimate_version)
);

-- 파트너 추천 테이블
CREATE TABLE partner_referrals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_user_id BIGINT NOT NULL,
    referred_user_id BIGINT NOT NULL,
    referral_code VARCHAR(20) NOT NULL,
    
    -- 추천 정보
    referred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    first_order_id BIGINT,
    first_order_at TIMESTAMP NULL,
    
    -- 커미션 정보
    commission_rate DECIMAL(5,2),
    commission_amount DECIMAL(12,2) DEFAULT 0.00,
    commission_currency VARCHAR(3) DEFAULT 'THB',
    commission_status ENUM('pending', 'earned', 'paid') DEFAULT 'pending',
    commission_paid_at TIMESTAMP NULL,
    
    FOREIGN KEY (partner_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (referred_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (first_order_id) REFERENCES orders(id),
    INDEX idx_partner_referrals_partner_id (partner_user_id),
    INDEX idx_partner_referrals_referred_id (referred_user_id),
    INDEX idx_partner_referrals_code (referral_code)
);

-- 시스템 설정 테이블
CREATE TABLE config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    config_type ENUM('string', 'number', 'boolean', 'json') DEFAULT 'string',
    description TEXT,
    
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT,
    
    FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_config_key (config_key),
    INDEX idx_config_system (is_system)
);

-- 감사 로그 테이블
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    action ENUM('CREATE', 'UPDATE', 'DELETE', 'VIEW', 'APPROVE', 'REJECT') NOT NULL,
    
    user_id BIGINT,
    user_email VARCHAR(255),
    user_role VARCHAR(50),
    
    old_values JSON,
    new_values JSON,
    changed_fields JSON,
    
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_id VARCHAR(100),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_logs_entity (entity_type, entity_id),
    INDEX idx_audit_logs_user_id (user_id),
    INDEX idx_audit_logs_action (action),
    INDEX idx_audit_logs_created_at (created_at)
);

-- 알림 테이블
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    
    -- 관련 엔티티
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    
    -- 알림 상태
    status ENUM('unread', 'read', 'archived') DEFAULT 'unread',
    priority ENUM('low', 'normal', 'high', 'urgent') DEFAULT 'normal',
    
    -- 발송 정보
    send_email BOOLEAN DEFAULT FALSE,
    send_sms BOOLEAN DEFAULT FALSE,
    email_sent_at TIMESTAMP NULL,
    sms_sent_at TIMESTAMP NULL,
    
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notifications_user_id (user_id),
    INDEX idx_notifications_status (status),
    INDEX idx_notifications_type (type),
    INDEX idx_notifications_created_at (created_at)
);

-- 파일 첨부 테이블
CREATE TABLE file_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    file_type ENUM('document', 'image', 'label', 'invoice', 'other') NOT NULL,
    
    original_name VARCHAR(500) NOT NULL,
    stored_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(200),
    
    uploaded_by BIGINT,
    upload_ip VARCHAR(45),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_file_attachments_entity (entity_type, entity_id),
    INDEX idx_file_attachments_type (file_type),
    INDEX idx_file_attachments_uploaded_by (uploaded_by)
);

-- 초기 설정 데이터 삽입
INSERT INTO config (config_key, config_value, config_type, description, is_system) VALUES
('cbm_threshold', '29.0', 'number', 'CBM 임계값 (m³) - 초과시 항공배송 자동 전환', TRUE),
('thb_amount_threshold', '1500.0', 'number', 'THB 금액 임계값 - 초과시 수취인 추가 정보 필요', TRUE),
('auto_air_conversion', 'true', 'boolean', 'CBM 초과시 자동 항공 전환 여부', TRUE),
('require_member_code', 'true', 'boolean', '회원 코드 필수 여부', TRUE),
('default_currency', 'THB', 'string', '기본 통화', FALSE),
('max_file_size_mb', '10', 'number', '최대 파일 크기 (MB)', FALSE),
('allowed_file_types', '["jpg","jpeg","png","pdf","doc","docx"]', 'json', '허용 파일 형식', FALSE),
('email_notification_enabled', 'true', 'boolean', '이메일 알림 활성화', FALSE),
('sms_notification_enabled', 'false', 'boolean', 'SMS 알림 활성화', FALSE);

-- 시스템 알림 템플릿
INSERT INTO config (config_key, config_value, config_type, description, is_system) VALUES
('notification_template_order_created', '주문이 생성되었습니다. 주문번호: {orderCode}', 'string', '주문 생성 알림 템플릿', TRUE),
('notification_template_order_confirmed', '주문이 확인되었습니다. 주문번호: {orderCode}', 'string', '주문 확인 알림 템플릿', TRUE),
('notification_template_order_shipped', '주문이 발송되었습니다. 추적번호: {trackingNumber}', 'string', '주문 발송 알림 템플릿', TRUE),
('notification_template_cbm_exceeded', 'CBM이 {cbm}m³로 임계값을 초과하여 항공배송으로 전환되었습니다.', 'string', 'CBM 초과 알림 템플릿', TRUE),
('notification_template_amount_exceeded', 'THB 금액이 {amount}로 임계값을 초과합니다. 수취인 추가 정보를 입력해주세요.', 'string', '금액 초과 알림 템플릿', TRUE);