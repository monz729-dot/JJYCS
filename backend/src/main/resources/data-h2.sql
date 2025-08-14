-- H2 Database Initial Data for YCS LMS
-- This file will be loaded automatically when the application starts

-- Insert test users
INSERT INTO users (email, password_hash, name, phone, role, status, member_code, email_verified, agree_terms, agree_privacy, created_at, updated_at) VALUES
('admin@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'System Admin', '010-1234-5678', 'ADMIN', 'ACTIVE', 'ADM001', true, true, true, NOW(), NOW()),
('warehouse@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Warehouse Manager', '010-2345-6789', 'WAREHOUSE', 'ACTIVE', 'WH001', true, true, true, NOW(), NOW()),
('user1@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '김철수', '010-1111-2222', 'INDIVIDUAL', 'ACTIVE', 'USR001', true, true, true, NOW(), NOW()),
('corp@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '홍길동', '010-3333-4444', 'ENTERPRISE', 'ACTIVE', 'ENT001', true, true, true, NOW(), NOW()),
('partner@partner.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '박영희', '010-5555-6666', 'PARTNER', 'ACTIVE', 'PTR001', true, true, true, NOW(), NOW()),
('pending@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '이승기', '010-7777-8888', 'ENTERPRISE', 'PENDING_APPROVAL', null, true, true, true, NOW(), NOW());

-- Insert test orders
INSERT INTO orders (order_code, user_id, status, order_type, recipient_name, recipient_phone, recipient_address, recipient_country, total_amount, currency, total_cbm_m3, requires_extra_recipient, payment_method, payment_status, created_at, updated_at) VALUES
('YCS-2024-00001', 3, 'REQUESTED', 'SEA', '수취인1', '+66-812345678', '123 Bangkok Street, Bangkok', 'Thailand', 850.00, 'THB', 0.025000, false, 'PREPAID', 'PENDING', NOW(), NOW()),
('YCS-2024-00002', 3, 'CONFIRMED', 'AIR', '수취인2', '+66-823456789', '456 Pattaya Road, Pattaya', 'Thailand', 2250.50, 'THB', 0.035000, true, 'PREPAID', 'COMPLETED', NOW(), NOW()),
('YCS-2024-00003', 4, 'IN_PROGRESS', 'SEA', '수취인3', '+66-834567890', '789 Chiang Mai Avenue, Chiang Mai', 'Thailand', 1650.75, 'THB', 0.028000, true, 'POSTPAID', 'PENDING', NOW(), NOW()),
('YCS-2024-00004', 3, 'SHIPPED', 'AIR', '수취인4', '+66-845678901', '321 Phuket Beach Road, Phuket', 'Thailand', 980.25, 'THB', 0.015000, false, 'PREPAID', 'COMPLETED', NOW(), NOW()),
('YCS-2024-00005', 4, 'DELIVERED', 'SEA', '수취인5', '+66-856789012', '654 Hua Hin Street, Hua Hin', 'Thailand', 3200.00, 'THB', 0.045000, true, 'PREPAID', 'COMPLETED', NOW(), NOW());

-- Insert test order items
INSERT INTO order_items (order_id, item_order, name, description, category, quantity, unit_weight, unit_price, total_amount, currency, hs_code, ems_code, country_of_origin, brand, restricted, created_at, updated_at) VALUES
(1, 1, '삼성 갤럭시 스마트폰', '최신 갤럭시 모델', 'Electronics', 1, 0.200, 850.00, 850.00, 'THB', '8517.12', 'EMS001', 'South Korea', 'Samsung', false, NOW(), NOW()),
(2, 1, '애플 아이폰', '아이폰 14 프로', 'Electronics', 1, 0.240, 1200.50, 1200.50, 'THB', '8517.12', 'EMS002', 'USA', 'Apple', false, NOW(), NOW()),
(2, 2, '무선 이어폰', '에어팟 프로', 'Electronics', 1, 0.150, 1050.00, 1050.00, 'THB', '8518.30', 'EMS003', 'USA', 'Apple', false, NOW(), NOW()),
(3, 1, '나이키 운동화', '에어맥스 시리즈', 'Fashion', 2, 0.800, 825.00, 1650.00, 'THB', '6403.99', 'EMS004', 'Vietnam', 'Nike', false, NOW(), NOW()),
(4, 1, '화장품 세트', '프리미엄 스킨케어', 'Beauty', 1, 0.500, 980.25, 980.25, 'THB', '3304.99', 'EMS005', 'France', 'Chanel', false, NOW(), NOW()),
(5, 1, '노트북 컴퓨터', '맥북 프로 16인치', 'Electronics', 1, 2.100, 3200.00, 3200.00, 'THB', '8471.30', 'EMS006', 'USA', 'Apple', false, NOW(), NOW());

-- Insert test order boxes
INSERT INTO order_boxes (order_id, box_number, label_code, width_cm, height_cm, depth_cm, cbm_m3, weight_kg, status, created_at, updated_at) VALUES
(1, 1, 'BOX-2024-00001-01', 25.0, 15.0, 8.0, 0.003000, 0.250, 'INBOUND_COMPLETED', NOW(), NOW()),
(2, 1, 'BOX-2024-00002-01', 30.0, 20.0, 12.0, 0.007200, 0.450, 'READY_FOR_OUTBOUND', NOW(), NOW()),
(2, 2, 'BOX-2024-00002-02', 28.0, 18.0, 10.0, 0.005040, 0.200, 'READY_FOR_OUTBOUND', NOW(), NOW()),
(3, 1, 'BOX-2024-00003-01', 35.0, 25.0, 15.0, 0.013125, 0.900, 'INBOUND_PENDING', NOW(), NOW()),
(4, 1, 'BOX-2024-00004-01', 22.0, 18.0, 10.0, 0.003960, 0.550, 'SHIPPED', NOW(), NOW()),
(5, 1, 'BOX-2024-00005-01', 40.0, 30.0, 20.0, 0.024000, 2.200, 'DELIVERED', NOW(), NOW());

-- Update total CBM for orders (calculated from boxes)
UPDATE orders SET total_cbm_m3 = (SELECT SUM(cbm_m3) FROM order_boxes WHERE order_id = orders.id);

-- Insert some test data for business rule validation
INSERT INTO orders (order_code, user_id, status, order_type, recipient_name, recipient_phone, recipient_address, recipient_country, total_amount, currency, total_cbm_m3, requires_extra_recipient, payment_method, payment_status, created_at, updated_at) VALUES
('YCS-2024-00006', 3, 'REQUESTED', 'AIR', '대용량 주문', '+66-867890123', '999 Large Order Street, Bangkok', 'Thailand', 1800.00, 'THB', 30.500000, true, 'PREPAID', 'PENDING', NOW(), NOW());

-- Insert test box for CBM > 29 rule testing
INSERT INTO order_boxes (order_id, box_number, label_code, width_cm, height_cm, depth_cm, cbm_m3, weight_kg, status, created_at, updated_at) VALUES
(6, 1, 'BOX-2024-00006-01', 150.0, 120.0, 100.0, 1.800000, 25.000, 'CREATED', NOW(), NOW()),
(6, 2, 'BOX-2024-00006-02', 200.0, 150.0, 120.0, 3.600000, 35.000, 'CREATED', NOW(), NOW()),
(6, 3, 'BOX-2024-00006-03', 180.0, 140.0, 110.0, 2.772000, 30.000, 'CREATED', NOW(), NOW());

-- Update total CBM for the large order
UPDATE orders SET total_cbm_m3 = (SELECT SUM(cbm_m3) FROM order_boxes WHERE order_id = 6) WHERE id = 6;

-- Insert test item for THB > 1500 rule testing  
INSERT INTO order_items (order_id, item_order, name, description, category, quantity, unit_weight, unit_price, total_amount, currency, hs_code, ems_code, country_of_origin, brand, restricted, created_at, updated_at) VALUES
(6, 1, '고급 시계', '롤렉스 서브마리너', 'Luxury', 1, 0.150, 1800.00, 1800.00, 'THB', '9101.21', 'EMS007', 'Switzerland', 'Rolex', false, NOW(), NOW());

-- Insert test user without member code for "No code" rule testing
INSERT INTO users (email, password_hash, name, phone, role, status, member_code, email_verified, agree_terms, agree_privacy, created_at, updated_at) VALUES
('nocode@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '코드없는사용자', '010-9999-0000', 'INDIVIDUAL', 'ACTIVE', null, true, true, true, NOW(), NOW());

-- Insert order for no-code user to test delay rule
INSERT INTO orders (order_code, user_id, status, order_type, recipient_name, recipient_phone, recipient_address, recipient_country, total_amount, currency, total_cbm_m3, requires_extra_recipient, payment_method, payment_status, created_at, updated_at) VALUES
('YCS-2024-00007', 7, 'REQUESTED', 'SEA', '지연 테스트', '+66-878901234', '111 Delay Test Road, Bangkok', 'Thailand', 500.00, 'THB', 0.010000, false, 'PREPAID', 'PENDING', NOW(), NOW());

INSERT INTO order_items (order_id, item_order, name, description, category, quantity, unit_weight, unit_price, total_amount, currency, hs_code, ems_code, country_of_origin, brand, restricted, created_at, updated_at) VALUES
(7, 1, '일반 상품', '회원코드 없는 주문', 'General', 1, 0.300, 500.00, 500.00, 'THB', '9999.99', 'EMS008', 'Korea', 'Test', false, NOW(), NOW());

INSERT INTO order_boxes (order_id, box_number, label_code, width_cm, height_cm, depth_cm, cbm_m3, weight_kg, status, created_at, updated_at) VALUES
(7, 1, 'BOX-2024-00007-01', 20.0, 15.0, 12.0, 0.003600, 0.350, 'CREATED', NOW(), NOW());

-- Print summary
SELECT 'Database initialized successfully!' as status;
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as order_count FROM orders;
SELECT COUNT(*) as item_count FROM order_items;
SELECT COUNT(*) as box_count FROM order_boxes;