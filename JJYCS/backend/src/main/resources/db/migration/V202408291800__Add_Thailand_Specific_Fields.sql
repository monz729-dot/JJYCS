-- 태국 전용 필드 및 다중 수취인 지원 추가
-- Version: V202408291800
-- Date: 2024-08-29 18:00

-- INBOUND_LOCATIONS 테이블 생성 (YCS 접수지 관리)
CREATE TABLE inbound_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(500) NOT NULL,
    postal_code VARCHAR(10),
    phone VARCHAR(20),
    contact_person VARCHAR(100),
    business_hours VARCHAR(100),
    special_instructions TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_inbound_locations_is_active (is_active),
    INDEX idx_inbound_locations_display_order (display_order)
);

-- ORDERS 테이블에 태국 전용 필드들 추가
ALTER TABLE orders 
ADD COLUMN repacking BOOLEAN NOT NULL DEFAULT FALSE COMMENT '리패킹 서비스 신청 여부',
ADD COLUMN dest_country VARCHAR(2) NOT NULL DEFAULT 'TH' COMMENT '도착 국가 코드 (태국 고정)',
ADD COLUMN dest_zip VARCHAR(5) COMMENT '태국 우편번호 (5자리)',
ADD COLUMN recipients_json JSON COMMENT '다중 수취인 정보 (JSON 배열)',
ADD COLUMN inbound_method ENUM('COURIER', 'QUICK', 'OTHER') NOT NULL DEFAULT 'COURIER' COMMENT '배대지 접수 방법',
ADD COLUMN courier_company VARCHAR(50) COMMENT '택배사명',
ADD COLUMN waybill_no VARCHAR(100) COMMENT '택배 송장번호',
ADD COLUMN quick_vendor VARCHAR(100) COMMENT '퀵서비스 업체명',
ADD COLUMN quick_phone VARCHAR(20) COMMENT '퀵서비스 연락처',
ADD COLUMN inbound_location_id BIGINT COMMENT 'YCS 접수지 ID',
ADD COLUMN inbound_note TEXT COMMENT '접수 관련 요청사항',
ADD COLUMN thailand_postal_verified BOOLEAN NOT NULL DEFAULT FALSE COMMENT '태국 우편번호 검증 완료 여부',
ADD COLUMN ems_verified BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'EMS 송장번호 검증 완료 여부';

-- ORDERS 테이블에 외래키 제약조건 추가
ALTER TABLE orders 
ADD CONSTRAINT fk_orders_inbound_location 
FOREIGN KEY (inbound_location_id) REFERENCES inbound_locations(id);

-- ORDERS 테이블에 인덱스 추가
ALTER TABLE orders 
ADD INDEX idx_orders_dest_country (dest_country),
ADD INDEX idx_orders_dest_zip (dest_zip),
ADD INDEX idx_orders_inbound_method (inbound_method),
ADD INDEX idx_orders_courier_company (courier_company),
ADD INDEX idx_orders_waybill_no (waybill_no),
ADD INDEX idx_orders_inbound_location_id (inbound_location_id);

-- COURIER_COMPANIES 테이블 생성 (택배사 관리)
CREATE TABLE courier_companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    website VARCHAR(200),
    tracking_url_template VARCHAR(500) COMMENT '송장 추적 URL 템플릿',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_courier_companies_code (code),
    INDEX idx_courier_companies_is_active (is_active),
    INDEX idx_courier_companies_display_order (display_order)
);

-- EMS_POSTAL_CODES 테이블 생성 (태국 우편번호 캐시)
CREATE TABLE ems_postal_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    country_code VARCHAR(2) NOT NULL DEFAULT 'TH',
    postal_code VARCHAR(5) NOT NULL,
    city VARCHAR(100) NOT NULL,
    district VARCHAR(100),
    province VARCHAR(100),
    city_en VARCHAR(100),
    district_en VARCHAR(100),
    province_en VARCHAR(100),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_postal_codes_country_code (country_code, postal_code),
    INDEX idx_ems_postal_codes_postal_code (postal_code),
    INDEX idx_ems_postal_codes_city (city),
    INDEX idx_ems_postal_codes_district (district),
    INDEX idx_ems_postal_codes_is_active (is_active)
);

-- ORDER_RECIPIENTS 테이블 생성 (정규화된 다중 수취인)
CREATE TABLE order_recipients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    recipient_order INT NOT NULL DEFAULT 1,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(500) NOT NULL,
    postal_code VARCHAR(5),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_recipients_order_id (order_id),
    INDEX idx_order_recipients_recipient_order (recipient_order),
    INDEX idx_order_recipients_is_primary (is_primary),
    INDEX idx_order_recipients_postal_code (postal_code)
);

-- 기존 데이터 마이그레이션을 위한 업데이트
-- 기존 주문들을 태국으로 설정하고 기존 수취인 정보를 새 구조로 이전
UPDATE orders SET 
    dest_country = 'TH',
    dest_zip = CASE 
        WHEN LENGTH(postal_code) = 5 AND postal_code REGEXP '^[0-9]{5}$' 
        THEN postal_code 
        ELSE NULL 
    END,
    repacking = repacking_requested,
    thailand_postal_verified = CASE 
        WHEN LENGTH(postal_code) = 5 AND postal_code REGEXP '^[0-9]{5}$' 
        THEN TRUE 
        ELSE FALSE 
    END,
    ems_verified = CASE 
        WHEN LENGTH(tracking_number) = 13 AND tracking_number REGEXP '^[A-Z]{2}[0-9]{9}[A-Z]{2}$' 
        THEN TRUE 
        ELSE FALSE 
    END
WHERE created_at IS NOT NULL;

-- 기존 수취인 정보를 order_recipients 테이블로 마이그레이션
INSERT INTO order_recipients (order_id, recipient_order, name, phone, address, postal_code, is_primary)
SELECT 
    id,
    1,
    recipient_name,
    COALESCE(recipient_phone, ''),
    COALESCE(recipient_address, ''),
    CASE 
        WHEN LENGTH(recipient_postal_code) = 5 AND recipient_postal_code REGEXP '^[0-9]{5}$' 
        THEN recipient_postal_code 
        ELSE NULL 
    END,
    TRUE
FROM orders 
WHERE recipient_name IS NOT NULL AND recipient_name != '';

-- recipients_json 컬럼 업데이트 (기존 수취인 정보를 JSON으로 저장)
UPDATE orders o
LEFT JOIN order_recipients r ON o.id = r.order_id
SET recipients_json = JSON_ARRAY(
    JSON_OBJECT(
        'name', COALESCE(r.name, o.recipient_name, ''),
        'phone', COALESCE(r.phone, o.recipient_phone, ''),
        'address', COALESCE(r.address, o.recipient_address, ''),
        'postalCode', COALESCE(r.postal_code, 
            CASE 
                WHEN LENGTH(o.recipient_postal_code) = 5 AND o.recipient_postal_code REGEXP '^[0-9]{5}$' 
                THEN o.recipient_postal_code 
                ELSE NULL 
            END, '')
    )
)
WHERE recipients_json IS NULL;