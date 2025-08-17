-- YCS LMS H2 Database 초기 데이터
-- 테스트용 기본 데이터 삽입

-- 1. 시스템 설정 데이터
INSERT INTO config (key_name, value, description, category, is_public) VALUES
('system.version', '1.0.0', '시스템 버전', 'system', true),
('cbm.threshold', '29.0', 'CBM 임계값 (추가 수취인 필요)', 'business', false),
('high.value.threshold', '1500.0', '고가 상품 임계값 (THB)', 'business', false),
('member.code.grace.period', '30', '회원 코드 유예 기간 (일)', 'business', false),
('partner.default.commission', '0.05', '파트너 기본 수수료율', 'partner', false),
('email.from', 'noreply@ycs.com', '시스템 발송 이메일 주소', 'email', false),
('currency.default', 'THB', '기본 통화', 'system', true);

-- 2. 테스트용 사용자 데이터 (비밀번호는 모두 'password123' 해시값)
-- BCrypt 해시: $2a$10$N9qo8uLOickgx2ZMRZoMye9VzGGqpoTBhwLJrYZPZlN9K7LbJvmGW
INSERT INTO users (email, password_hash, name, phone, role, status, member_code, email_verified, agree_terms, agree_privacy, agree_marketing) VALUES
-- 관리자
('admin@ycs.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye9VzGGqpoTBhwLJrYZPZlN9K7LbJvmGW', '시스템 관리자', '02-1234-5678', 'ADMIN', 'ACTIVE', 'ADM001', true, true, true, false),

-- 일반 개인 회원
('user1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye9VzGGqpoTBhwLJrYZPZlN9K7LbJvmGW', '김철수', '010-1111-2222', 'INDIVIDUAL', 'ACTIVE', 'IND001', true, true, true, true),
('user2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye9VzGGqpoTBhwLJrYZPZlN9K7LbJvmGW', '이영희', '010-2222-3333', 'INDIVIDUAL', 'ACTIVE', 'IND002', true, true, true, false),
('user3@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye9VzGGqpoTBhwLJrYZPZlN9K7LbJvmGW', '박민수', '010-3333-4444', 'INDIVIDUAL', 'PENDING_APPROVAL', NULL, false, true, true, true),

-- 기업 회원
('company1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye9VzGGqpoTBhwLJrYZPZlN9K7LbJvmGW', '최대표', '02-1111-2222', 'ENTERPRISE', 'ACTIVE', 'ENT001', true, true, true, true),
('company2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye9VzGGqpoTBhwLJrYZPZlN9K7LbJvmGW', '정대표', '02-2222-3333', 'ENTERPRISE', 'ACTIVE', 'ENT002', true, true, true, false),

-- 파트너
('partner1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye9VzGGqpoTBhwLJrYZPZlN9K7LbJvmGW', '강파트너', '010-5555-6666', 'PARTNER', 'ACTIVE', 'PRT001', true, true, true, true),

-- 창고 직원
('warehouse1@ycs.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye9VzGGqpoTBhwLJrYZPZlN9K7LbJvmGW', '이창고', '02-7777-8888', 'WAREHOUSE', 'ACTIVE', 'WHS001', true, true, true, false),
('warehouse2@ycs.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye9VzGGqpoTBhwLJrYZPZlN9K7LbJvmGW', '박창고', '02-8888-9999', 'WAREHOUSE', 'ACTIVE', 'WHS002', true, true, true, false);

-- 3. 기업 프로필 데이터
INSERT INTO enterprise_profiles (user_id, company_name, business_number, representative, business_address, vat_type, tax_invoice_email) VALUES
(5, '(주)테크컴퍼니', '123-45-67890', '최대표', '서울시 강남구 테헤란로 123', 'GENERAL', 'tax@techcompany.com'),
(6, '글로벌무역(주)', '234-56-78901', '정대표', '서울시 서초구 서초대로 456', 'ZERO_RATE', 'tax@globaltrade.com');

-- 4. 파트너 프로필 데이터
INSERT INTO partner_profiles (user_id, company_name, commission_rate, referral_code, bank_name, bank_account, account_holder) VALUES
(7, '베스트파트너스', 0.0500, 'REF001', '국민은행', '123-456-789012', '강파트너');

-- 5. 창고 데이터
INSERT INTO warehouses (name, code, type, address, city, country, capacity_cbm, current_usage_cbm, manager_id, contact_phone, contact_email, operating_hours, is_active) VALUES
('YCS 방콕 메인 창고', 'BKK-MAIN', 'MAIN', '123 Sukhumvit Road, Klongtoey', 'Bangkok', 'Thailand', 10000.000, 2500.000, 8, '+66-2-1234-5678', 'bangkok@ycs.com', '08:00-18:00 (Mon-Sat)', true),
('YCS 서울 창고', 'SEL-SUB', 'SUB', '서울시 영등포구 선유로 123', 'Seoul', 'Korea', 5000.000, 1200.000, 9, '02-3456-7890', 'seoul@ycs.com', '09:00-18:00 (Mon-Fri)', true),
('YCS 인천 항구 창고', 'ICN-PORT', 'SUB', '인천시 중구 항동 456', 'Incheon', 'Korea', 8000.000, 3000.000, NULL, '032-1234-5678', 'incheon@ycs.com', '24/7', true);

-- 6. 창고 프로필 데이터
INSERT INTO warehouse_profiles (user_id, warehouse_id, permissions, department, position) VALUES
(8, 1, 'INBOUND,OUTBOUND,INVENTORY,REPORT', '물류관리팀', '팀장'),
(9, 2, 'INBOUND,OUTBOUND', '입출고팀', '직원');

-- 7. 주소 데이터
INSERT INTO addresses (user_id, type, name, address_line1, address_line2, city, state, zip_code, country, phone, is_default) VALUES
-- 일반 회원 주소
(2, 'SHIPPING', '자택', '서울시 강남구 역삼동 123-45', '행복아파트 101동 1001호', '서울', '서울특별시', '06234', 'Korea', '010-1111-2222', true),
(2, 'BILLING', '회사', '서울시 중구 을지로 100', '삼성빌딩 10층', '서울', '서울특별시', '04524', 'Korea', '02-1234-5678', false),
(3, 'BOTH', '자택', '경기도 성남시 분당구 정자동 20', '그린빌라 201호', '성남', '경기도', '13561', 'Korea', '010-2222-3333', true),

-- 기업 회원 주소
(5, 'BOTH', '본사', '서울시 강남구 테헤란로 123', '테크타워 15층', '서울', '서울특별시', '06234', 'Korea', '02-1111-2222', true),
(6, 'SHIPPING', '물류센터', '경기도 이천시 부발읍 경충대로 2000', '글로벌물류센터', '이천', '경기도', '17300', 'Korea', '031-1234-5678', true);

-- 8. 주문 데이터
INSERT INTO orders (order_code, user_id, status, order_type, recipient_name, recipient_phone, recipient_address, recipient_zip_code, recipient_country, 
                   urgency, needs_repacking, total_amount, currency, total_cbm_m3, payment_method, payment_status, created_by, assigned_warehouse_id) VALUES
-- 일반 회원 주문
('ORD202401001', 2, 'CONFIRMED', 'SEA', '김철수', '010-1111-2222', '서울시 강남구 역삼동 123-45 행복아파트 101동 1001호', '06234', 'Korea', 
 'NORMAL', false, 150000.00, 'THB', 2.500, 'PREPAID', 'COMPLETED', 2, 1),
 
('ORD202401002', 2, 'IN_PROGRESS', 'AIR', '김철수', '010-1111-2222', '서울시 중구 을지로 100 삼성빌딩 10층', '04524', 'Korea',
 'URGENT', true, 85000.00, 'THB', 0.800, 'PREPAID', 'COMPLETED', 2, 1),
 
('ORD202401003', 3, 'REQUESTED', 'SEA', '이영희', '010-2222-3333', '경기도 성남시 분당구 정자동 20 그린빌라 201호', '13561', 'Korea',
 'NORMAL', false, 320000.00, 'THB', 35.000, 'PREPAID', 'PENDING', 3, NULL),

-- 기업 회원 주문
('ORD202401004', 5, 'SHIPPED', 'SEA', '최대표', '02-1111-2222', '서울시 강남구 테헤란로 123 테크타워 15층', '06234', 'Korea',
 'NORMAL', false, 1250000.00, 'THB', 15.000, 'POSTPAID', 'PENDING', 5, 1),
 
('ORD202401005', 6, 'DELIVERED', 'SEA', '정대표', '031-1234-5678', '경기도 이천시 부발읍 경충대로 2000 글로벌물류센터', '17300', 'Korea',
 'NORMAL', true, 2500000.00, 'THB', 45.000, 'PREPAID', 'COMPLETED', 6, 2);

-- 9. 주문 아이템 데이터
INSERT INTO order_items (order_id, product_name, product_url, quantity, unit_price, total_price, weight_kg, hs_code) VALUES
-- 주문 1 아이템
(1, '태국 코코넛 오일 1L', 'http://shop.example.com/coconut-oil', 10, 500.00, 5000.00, 10.000, '1513.11'),
(1, '태국 향신료 세트', 'http://shop.example.com/spice-set', 5, 1200.00, 6000.00, 2.500, '0910.99'),

-- 주문 2 아이템
(2, '태국 실크 스카프', 'http://shop.example.com/silk-scarf', 3, 2500.00, 7500.00, 0.300, '6214.10'),
(2, '수공예 가방', 'http://shop.example.com/handmade-bag', 2, 3500.00, 7000.00, 0.800, '4202.22'),

-- 주문 3 아이템 (대량 주문 - CBM 초과)
(3, '태국 가구 세트', 'http://shop.example.com/furniture', 5, 15000.00, 75000.00, 150.000, '9403.60'),
(3, '대형 조각품', 'http://shop.example.com/sculpture', 2, 25000.00, 50000.00, 80.000, '9703.00'),

-- 주문 4 아이템 (기업 주문)
(4, '전자부품 A', 'http://b2b.example.com/part-a', 1000, 50.00, 50000.00, 50.000, '8542.31'),
(4, '전자부품 B', 'http://b2b.example.com/part-b', 500, 80.00, 40000.00, 30.000, '8542.32'),
(4, '전자부품 C', 'http://b2b.example.com/part-c', 2000, 30.00, 60000.00, 40.000, '8542.33'),

-- 주문 5 아이템 (완료된 기업 주문)
(5, '산업용 원자재', 'http://b2b.example.com/material', 100, 5000.00, 500000.00, 2000.000, '7204.41'),
(5, '기계 부품', 'http://b2b.example.com/machine-parts', 50, 10000.00, 500000.00, 500.000, '8483.10');

-- 10. 주문 박스 데이터
INSERT INTO order_boxes (order_id, box_number, length_cm, width_cm, height_cm, weight_kg, cbm_m3, barcode) VALUES
-- 주문 1 박스
(1, 1, 60.0, 40.0, 50.0, 6.5, 0.120, 'BOX202401001001'),
(1, 2, 50.0, 35.0, 40.0, 6.0, 0.070, 'BOX202401001002'),

-- 주문 2 박스
(2, 1, 40.0, 30.0, 20.0, 1.1, 0.024, 'BOX202401002001'),

-- 주문 3 박스 (대형)
(3, 1, 200.0, 150.0, 180.0, 100.0, 5.400, 'BOX202401003001'),
(3, 2, 180.0, 120.0, 150.0, 80.0, 3.240, 'BOX202401003002'),
(3, 3, 150.0, 100.0, 120.0, 50.0, 1.800, 'BOX202401003003'),

-- 주문 4 박스
(4, 1, 120.0, 80.0, 60.0, 40.0, 0.576, 'BOX202401004001'),
(4, 2, 120.0, 80.0, 60.0, 40.0, 0.576, 'BOX202401004002'),
(4, 3, 120.0, 80.0, 60.0, 40.0, 0.576, 'BOX202401004003'),

-- 주문 5 박스 (대형 산업용)
(5, 1, 240.0, 120.0, 220.0, 600.0, 6.336, 'BOX202401005001'),
(5, 2, 240.0, 120.0, 220.0, 600.0, 6.336, 'BOX202401005002'),
(5, 3, 240.0, 120.0, 220.0, 600.0, 6.336, 'BOX202401005003'),
(5, 4, 240.0, 120.0, 220.0, 600.0, 6.336, 'BOX202401005004'),
(5, 5, 200.0, 100.0, 150.0, 200.0, 3.000, 'BOX202401005005');

-- 11. 재고 데이터
INSERT INTO inventory (warehouse_id, order_id, label_code, status, location_code, zone, rack, shelf, bin, scan_count, received_at) VALUES
-- 창고 1 재고
(1, 1, 'LBL202401001', 'INBOUND_COMPLETED', 'A-01-02-03', 'A', '01', '02', '03', 2, CURRENT_TIMESTAMP - 5),
(1, 2, 'LBL202401002', 'READY_FOR_OUTBOUND', 'B-02-03-04', 'B', '02', '03', '04', 3, CURRENT_TIMESTAMP - 3),
(1, 4, 'LBL202401004', 'OUTBOUND_COMPLETED', 'C-03-04-05', 'C', '03', '04', '05', 4, CURRENT_TIMESTAMP - 10),

-- 창고 2 재고
(2, 5, 'LBL202401005', 'INBOUND_COMPLETED', 'D-01-01-01', 'D', '01', '01', '01', 5, CURRENT_TIMESTAMP - 15);

-- 12. 스캔 이벤트 데이터
INSERT INTO scan_events (warehouse_id, user_id, inventory_id, label_code, event_type, location_code, previous_status, new_status, device_id) VALUES
-- 입고 스캔
(1, 8, 1, 'LBL202401001', 'INBOUND', 'A-01-02-03', 'PENDING', 'INBOUND_COMPLETED', 'SCANNER001'),
(1, 8, 2, 'LBL202401002', 'INBOUND', 'B-02-03-04', 'PENDING', 'INBOUND_COMPLETED', 'SCANNER001'),

-- 출고 준비 스캔
(1, 8, 2, 'LBL202401002', 'LOCATION_UPDATE', 'B-02-03-04', 'INBOUND_COMPLETED', 'READY_FOR_OUTBOUND', 'SCANNER001'),

-- 출고 완료 스캔
(1, 9, 3, 'LBL202401004', 'OUTBOUND', 'C-03-04-05', 'READY_FOR_OUTBOUND', 'OUTBOUND_COMPLETED', 'SCANNER002');

-- 13. 결제 데이터
INSERT INTO payments (order_id, user_id, payment_code, amount, currency, payment_method, payment_status, transaction_id, gateway, paid_at) VALUES
-- 완료된 결제
(1, 2, 'PAY202401001', 150000.00, 'THB', 'CARD', 'COMPLETED', 'TXN123456789', 'STRIPE', CURRENT_TIMESTAMP - 10),
(2, 2, 'PAY202401002', 85000.00, 'THB', 'BANK_TRANSFER', 'COMPLETED', 'TXN234567890', 'BANK', CURRENT_TIMESTAMP - 8),
(5, 6, 'PAY202401005', 2500000.00, 'THB', 'BANK_TRANSFER', 'COMPLETED', 'TXN345678901', 'BANK', CURRENT_TIMESTAMP - 20),

-- 대기중 결제
(3, 3, 'PAY202401003', 320000.00, 'THB', 'VIRTUAL_ACCOUNT', 'PENDING', NULL, 'BANK', NULL),
(4, 5, 'PAY202401004', 1250000.00, 'THB', 'BANK_TRANSFER', 'PENDING', NULL, 'BANK', NULL);

-- 14. 배송 추적 데이터
INSERT INTO shipment_tracking (order_id, tracking_number, carrier, carrier_code, status, current_location, destination, estimated_delivery) VALUES
-- 배송 중
(4, 'TRK202401004', 'DHL Express', 'DHL', 'IN_TRANSIT', 'Hong Kong Hub', 'Seoul, Korea', CURRENT_TIMESTAMP + 3),

-- 배송 완료
(5, 'TRK202401005', 'Korea Express', 'KEXP', 'DELIVERED', 'Icheon, Korea', 'Icheon, Korea', CURRENT_TIMESTAMP - 5);

-- 15. 감사 로그 샘플 데이터
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, ip_address, user_agent) VALUES
(2, 'LOGIN', 'USER', 2, '192.168.1.100', 'Mozilla/5.0 Chrome/120.0.0.0'),
(2, 'CREATE_ORDER', 'ORDER', 1, '192.168.1.100', 'Mozilla/5.0 Chrome/120.0.0.0'),
(5, 'UPDATE_PROFILE', 'ENTERPRISE_PROFILE', 1, '192.168.1.101', 'Mozilla/5.0 Safari/17.2.1'),
(8, 'SCAN_INVENTORY', 'INVENTORY', 1, '192.168.1.102', 'YCS Scanner App v1.0'),
(1, 'UPDATE_CONFIG', 'CONFIG', 1, '192.168.1.1', 'Mozilla/5.0 Firefox/121.0');

ALTER TABLE users ADD COLUMN username VARCHAR(50);
UPDATE users SET username = SUBSTRING_INDEX(email, '@', 1) WHERE username IS NULL OR username = '';
ALTER TABLE users MODIFY username VARCHAR(50) NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_username ON users(username);

UPDATE users
SET username = SUBSTRING(email, 1, LOCATE('@', email) - 1)
WHERE username IS NULL OR username = '';

UPDATE users
SET username = 'user' || CAST(id AS VARCHAR)
WHERE username IS NULL OR username = '';

UPDATE users u
SET username = username || '_' || CAST(id AS VARCHAR)
WHERE EXISTS (
    SELECT 1 FROM users u2
    WHERE u2.username = u.username
      AND u2.id < u.id
);

ALTER TABLE users ALTER COLUMN username VARCHAR(50);
ALTER TABLE users ALTER COLUMN username SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_username ON users(username);

-- 데이터 삽입 완료
COMMIT;