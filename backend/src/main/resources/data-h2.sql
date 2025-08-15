-- H2 Database Initial Data for YCS LMS
-- This file will be loaded automatically when the application starts
-- All test accounts use password: "password123"

-- Insert test users (password for all: "password123")
INSERT INTO users (email, password_hash, name, phone, role, status, member_code, email_verified, agree_terms, agree_privacy, created_at, updated_at) VALUES
-- Admin accounts
('admin@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'System Admin', '010-1234-5678', 'ADMIN', 'ACTIVE', 'ADM001', true, true, true, NOW(), NOW()),
('admin2@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin Manager', '010-1234-5679', 'ADMIN', 'ACTIVE', 'ADM002', true, true, true, NOW(), NOW()),

-- Warehouse accounts
('warehouse@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Warehouse Manager', '010-2345-6789', 'WAREHOUSE', 'ACTIVE', 'WH001', true, true, true, NOW(), NOW()),
('warehouse2@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Warehouse Staff A', '010-2345-6790', 'WAREHOUSE', 'ACTIVE', 'WH002', true, true, true, NOW(), NOW()),
('warehouse3@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Warehouse Staff B', '010-2345-6791', 'WAREHOUSE', 'ACTIVE', 'WH003', true, true, true, NOW(), NOW()),

-- Individual users
('user1@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '김철수', '010-1111-2222', 'INDIVIDUAL', 'ACTIVE', 'USR001', true, true, true, NOW(), NOW()),
('user2@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '이영희', '010-1111-2223', 'INDIVIDUAL', 'ACTIVE', 'USR002', true, true, true, NOW(), NOW()),
('user3@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '박민수', '010-1111-2224', 'INDIVIDUAL', 'ACTIVE', 'USR003', true, true, true, NOW(), NOW()),
('user4@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '최지은', '010-1111-2225', 'INDIVIDUAL', 'ACTIVE', 'USR004', true, true, true, NOW(), NOW()),
('user5@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '정대호', '010-1111-2226', 'INDIVIDUAL', 'ACTIVE', 'USR005', true, true, true, NOW(), NOW()),

-- Enterprise users
('corp@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '홍길동', '010-3333-4444', 'ENTERPRISE', 'ACTIVE', 'ENT001', true, true, true, NOW(), NOW()),
('corp2@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '김사장', '010-3333-4445', 'ENTERPRISE', 'ACTIVE', 'ENT002', true, true, true, NOW(), NOW()),
('corp3@bizcorp.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '이대표', '010-3333-4446', 'ENTERPRISE', 'ACTIVE', 'ENT003', true, true, true, NOW(), NOW()),
('corp4@trading.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '박부장', '010-3333-4447', 'ENTERPRISE', 'ACTIVE', 'ENT004', true, true, true, NOW(), NOW()),

-- Partner accounts
('partner@partner.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '박영희', '010-5555-6666', 'PARTNER', 'ACTIVE', 'PTR001', true, true, true, NOW(), NOW()),
('partner2@logistics.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '김파트너', '010-5555-6667', 'PARTNER', 'ACTIVE', 'PTR002', true, true, true, NOW(), NOW()),
('partner3@shipping.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '이협력사', '010-5555-6668', 'PARTNER', 'ACTIVE', 'PTR003', true, true, true, NOW(), NOW()),

-- Pending approval users
('pending@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '이승기', '010-7777-8888', 'ENTERPRISE', 'PENDING_APPROVAL', null, true, true, true, NOW(), NOW()),
('pending2@newcompany.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '신규기업', '010-7777-8889', 'ENTERPRISE', 'PENDING_APPROVAL', null, true, true, true, NOW(), NOW()),
('pending3@startup.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '스타트업대표', '010-7777-8890', 'PARTNER', 'PENDING_APPROVAL', null, true, true, true, NOW(), NOW()),

-- Users without member codes (for testing "No code" rule)
('nocode@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '코드없는사용자', '010-9999-0000', 'INDIVIDUAL', 'ACTIVE', null, true, true, true, NOW(), NOW()),
('nocode2@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '미등록사용자', '010-9999-0001', 'INDIVIDUAL', 'ACTIVE', null, true, true, true, NOW(), NOW());

-- Insert test orders
INSERT INTO orders (order_code, user_id, status, order_type, recipient_name, recipient_phone, recipient_address, recipient_country, total_amount, currency, total_cbm_m3, requires_extra_recipient, payment_method, payment_status, created_at, updated_at) VALUES
-- Individual user orders
('YCS-2024-00001', 6, 'REQUESTED', 'SEA', '방콕수취인', '+66-812345678', '123 Bangkok Street, Bangkok', 'Thailand', 850.00, 'THB', 0.003000, false, 'PREPAID', 'PENDING', NOW(), NOW()),
('YCS-2024-00002', 6, 'CONFIRMED', 'AIR', '파타야수취인', '+66-823456789', '456 Pattaya Road, Pattaya', 'Thailand', 2250.50, 'THB', 0.012240, true, 'PREPAID', 'COMPLETED', NOW(), NOW()),
('YCS-2024-00003', 7, 'IN_PROGRESS', 'SEA', '치앙마이수취인', '+66-834567890', '789 Chiang Mai Avenue, Chiang Mai', 'Thailand', 1650.00, 'THB', 0.013125, false, 'POSTPAID', 'PENDING', NOW(), NOW()),
('YCS-2024-00004', 8, 'SHIPPED', 'AIR', '푸켓수취인', '+66-845678901', '321 Phuket Beach Road, Phuket', 'Thailand', 980.25, 'THB', 0.003960, false, 'PREPAID', 'COMPLETED', NOW(), NOW()),
('YCS-2024-00005', 9, 'DELIVERED', 'SEA', '후아힌수취인', '+66-856789012', '654 Hua Hin Street, Hua Hin', 'Thailand', 3200.00, 'THB', 0.024000, true, 'PREPAID', 'COMPLETED', NOW(), NOW()),

-- Enterprise user orders
('YCS-2024-00006', 11, 'REQUESTED', 'SEA', '기업수취인1', '+66-867890123', '888 Corporate Plaza, Bangkok', 'Thailand', 5500.00, 'THB', 0.060000, true, 'POSTPAID', 'PENDING', NOW(), NOW()),
('YCS-2024-00007', 12, 'CONFIRMED', 'AIR', '기업수취인2', '+66-878901234', '999 Business Center, Pattaya', 'Thailand', 12500.00, 'THB', 0.083275, true, 'PREPAID', 'COMPLETED', NOW(), NOW()),
('YCS-2024-00008', 13, 'IN_PROGRESS', 'SEA', '기업수취인3', '+66-889012345', '777 Trade Tower, Chiang Mai', 'Thailand', 8750.50, 'THB', 0.086250, true, 'POSTPAID', 'PENDING', NOW(), NOW()),
('YCS-2024-00009', 14, 'SHIPPED', 'AIR', '기업수취인4', '+66-890123456', '555 Commerce Building, Phuket', 'Thailand', 15200.75, 'THB', 0.122500, true, 'PREPAID', 'COMPLETED', NOW(), NOW()),
('YCS-2024-00010', 11, 'DELIVERED', 'SEA', '기업수취인5', '+66-901234567', '333 Export Hub, Bangkok', 'Thailand', 22800.00, 'THB', 0.348750, true, 'PREPAID', 'COMPLETED', NOW(), NOW()),

-- Partner orders
('YCS-2024-00011', 15, 'REQUESTED', 'SEA', '파트너수취인1', '+66-912345678', '111 Partner Street, Bangkok', 'Thailand', 3500.00, 'THB', 0.060000, false, 'PREPAID', 'PENDING', NOW(), NOW()),
('YCS-2024-00012', 16, 'CONFIRMED', 'AIR', '파트너수취인2', '+66-923456789', '222 Alliance Road, Pattaya', 'Thailand', 7200.50, 'THB', 0.164900, true, 'POSTPAID', 'COMPLETED', NOW(), NOW()),
('YCS-2024-00013', 17, 'IN_PROGRESS', 'SEA', '파트너수취인3', '+66-934567890', '444 Cooperation Ave, Chiang Mai', 'Thailand', 4850.25, 'THB', 0.097500, false, 'PREPAID', 'PENDING', NOW(), NOW()),

-- Various status orders for testing
('YCS-2024-00014', 7, 'CANCELLED', 'SEA', '취소된주문', '+66-945678901', '666 Cancel Street, Bangkok', 'Thailand', 1200.00, 'THB', 0.015000, false, 'PREPAID', 'REFUNDED', NOW(), NOW()),
('YCS-2024-00015', 8, 'ON_HOLD', 'AIR', '보류된주문', '+66-956789012', '777 Hold Avenue, Pattaya', 'Thailand', 2800.00, 'THB', 0.035000, false, 'PREPAID', 'PENDING', NOW(), NOW()),

-- Recent orders (last 7 days)
('YCS-2024-00016', 6, 'REQUESTED', 'SEA', '최근주문1', '+66-967890123', '101 Recent Street, Bangkok', 'Thailand', 950.00, 'THB', 0.003000, false, 'PREPAID', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('YCS-2024-00017', 7, 'CONFIRMED', 'AIR', '최근주문2', '+66-978901234', '202 Fresh Road, Pattaya', 'Thailand', 1850.50, 'THB', 0.007500, false, 'PREPAID', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('YCS-2024-00018', 11, 'IN_PROGRESS', 'SEA', '최근주문3', '+66-989012345', '303 New Avenue, Chiang Mai', 'Thailand', 6500.00, 'THB', 0.216000, true, 'POSTPAID', 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('YCS-2024-00019', 12, 'SHIPPED', 'AIR', '최근주문4', '+66-990123456', '404 Today Plaza, Phuket', 'Thailand', 11200.75, 'THB', 0.315000, true, 'PREPAID', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('YCS-2024-00020', 8, 'DELIVERED', 'SEA', '최근주문5', '+66-991234567', '505 Latest Street, Bangkok', 'Thailand', 3750.25, 'THB', 0.086625, false, 'PREPAID', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- Special test data for business rule validation
-- Large CBM order (> 29 m³) for auto AIR conversion testing
('YCS-2024-00021', 6, 'REQUESTED', 'AIR', '대용량주문', '+66-867890123', '999 Large Order Street, Bangkok', 'Thailand', 35000.00, 'THB', 35.562000, true, 'PREPAID', 'PENDING', NOW(), NOW()),

-- Orders for no-code users to test delay rule
('YCS-2024-00022', 21, 'REQUESTED', 'SEA', '지연테스트1', '+66-878901234', '111 Delay Test Road, Bangkok', 'Thailand', 500.00, 'THB', 0.003600, false, 'PREPAID', 'PENDING', NOW(), NOW()),
('YCS-2024-00023', 22, 'ON_HOLD', 'SEA', '지연테스트2', '+66-879012345', '222 No Code Street, Pattaya', 'Thailand', 750.00, 'THB', 0.006300, false, 'PREPAID', 'PENDING', NOW(), NOW());

-- Insert test order items
INSERT INTO order_items (order_id, item_order, name, description, category, quantity, unit_weight, unit_price, total_amount, currency, hs_code, ems_code, country_of_origin, brand, restricted, created_at, updated_at) VALUES
-- Individual user order items
(1, 1, '삼성 갤럭시 스마트폰', '최신 갤럭시 모델', 'Electronics', 1, 0.200, 850.00, 850.00, 'THB', '8517.12', 'EMS001', 'South Korea', 'Samsung', false, NOW(), NOW()),
(2, 1, '애플 아이폰', '아이폰 14 프로', 'Electronics', 1, 0.240, 1200.50, 1200.50, 'THB', '8517.12', 'EMS002', 'USA', 'Apple', false, NOW(), NOW()),
(2, 2, '무선 이어폰', '에어팟 프로', 'Electronics', 1, 0.150, 1050.00, 1050.00, 'THB', '8518.30', 'EMS003', 'USA', 'Apple', false, NOW(), NOW()),
(3, 1, '나이키 운동화', '에어맥스 시리즈', 'Fashion', 2, 0.800, 825.00, 1650.00, 'THB', '6403.99', 'EMS004', 'Vietnam', 'Nike', false, NOW(), NOW()),
(4, 1, '화장품 세트', '프리미엄 스킨케어', 'Beauty', 1, 0.500, 980.25, 980.25, 'THB', '3304.99', 'EMS005', 'France', 'Chanel', false, NOW(), NOW()),
(5, 1, '노트북 컴퓨터', '맥북 프로 16인치', 'Electronics', 1, 2.100, 3200.00, 3200.00, 'THB', '8471.30', 'EMS006', 'USA', 'Apple', false, NOW(), NOW()),

-- Enterprise order items
(6, 1, '비즈니스 노트북', '델 비즈니스 노트북', 'Electronics', 5, 2.200, 1100.00, 5500.00, 'THB', '8471.30', 'EMS007', 'China', 'Dell', false, NOW(), NOW()),
(7, 1, '프리미엄 시계', '롤렉스 데이토나', 'Luxury', 1, 0.200, 4500.00, 4500.00, 'THB', '9101.21', 'EMS008', 'Switzerland', 'Rolex', false, NOW(), NOW()),
(7, 2, '디자이너 가방', '에르메스 버킨백', 'Fashion', 2, 1.200, 4000.00, 8000.00, 'THB', '4202.22', 'EMS009', 'France', 'Hermes', false, NOW(), NOW()),
(8, 1, '전문 카메라', '캐논 EOS R5', 'Electronics', 3, 0.800, 2916.83, 8750.50, 'THB', '9006.53', 'EMS010', 'Japan', 'Canon', false, NOW(), NOW()),
(9, 1, '고급 주방기기', '브레비에 커피머신', 'Appliances', 2, 8.500, 7600.375, 15200.75, 'THB', '8516.71', 'EMS011', 'Italy', 'Breville', false, NOW(), NOW()),
(10, 1, '사무용 프린터', 'HP 레이저젯 프린터', 'Electronics', 4, 15.200, 5700.00, 22800.00, 'THB', '8443.32', 'EMS012', 'Malaysia', 'HP', false, NOW(), NOW()),

-- Partner order items
(11, 1, '물류 용품', '포장재 및 라벨', 'Supplies', 100, 0.050, 35.00, 3500.00, 'THB', '3920.10', 'EMS013', 'Korea', 'LogiPack', false, NOW(), NOW()),
(12, 1, '배송 용품', '에어버블 및 포장지', 'Supplies', 200, 0.025, 36.00, 7200.00, 'THB', '3920.20', 'EMS014', 'Korea', 'PackPro', false, NOW(), NOW()),
(13, 1, '창고 용품', '선반 및 정리함', 'Supplies', 50, 2.500, 97.005, 4850.25, 'THB', '9403.20', 'EMS015', 'Korea', 'StoragePlus', false, NOW(), NOW()),

-- Test items for various scenarios
(14, 1, '취소된 상품', '취소 테스트용', 'Test', 1, 1.000, 1200.00, 1200.00, 'THB', '9999.99', 'EMS016', 'Korea', 'Test', false, NOW(), NOW()),
(15, 1, '보류된 상품', '보류 테스트용', 'Test', 1, 2.000, 2800.00, 2800.00, 'THB', '9999.99', 'EMS017', 'Korea', 'Test', false, NOW(), NOW()),

-- Recent order items
(16, 1, '최신 스마트폰', '갤럭시 S24', 'Electronics', 1, 0.180, 950.00, 950.00, 'THB', '8517.12', 'EMS018', 'South Korea', 'Samsung', false, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(17, 1, '무선 충전기', '삼성 무선충전기', 'Electronics', 1, 0.300, 1850.50, 1850.50, 'THB', '8504.40', 'EMS019', 'South Korea', 'Samsung', false, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(18, 1, '비즈니스 모니터', 'LG 울트라와이드 모니터', 'Electronics', 5, 5.200, 1300.00, 6500.00, 'THB', '8528.52', 'EMS020', 'South Korea', 'LG', false, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(19, 1, '프리미엄 커피머신', '네스프레소 커피머신', 'Appliances', 8, 4.500, 1400.09375, 11200.75, 'THB', '8516.71', 'EMS021', 'Switzerland', 'Nespresso', false, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(20, 1, '운동용품 세트', '나이키 운동복 세트', 'Fashion', 5, 1.500, 750.05, 3750.25, 'THB', '6112.20', 'EMS022', 'Vietnam', 'Nike', false, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- Special test items
(21, 1, '최고급 시계', '파텍 필립 노틸러스', 'Luxury', 1, 0.200, 35000.00, 35000.00, 'THB', '9101.21', 'EMS023', 'Switzerland', 'Patek Philippe', false, NOW(), NOW()),
(22, 1, '일반 상품 1', '회원코드 없는 주문 1', 'General', 1, 0.300, 500.00, 500.00, 'THB', '9999.99', 'EMS024', 'Korea', 'Test', false, NOW(), NOW()),
(23, 1, '일반 상품 2', '회원코드 없는 주문 2', 'General', 1, 0.400, 750.00, 750.00, 'THB', '9999.99', 'EMS025', 'Korea', 'Test', false, NOW(), NOW());

-- Insert test order boxes
INSERT INTO order_boxes (order_id, box_number, label_code, width_cm, height_cm, depth_cm, cbm_m3, weight_kg, status, created_at, updated_at) VALUES
-- Individual user order boxes
(1, 1, 'BOX-2024-00001-01', 25.0, 15.0, 8.0, 0.003000, 0.250, 'INBOUND_COMPLETED', NOW(), NOW()),
(2, 1, 'BOX-2024-00002-01', 30.0, 20.0, 12.0, 0.007200, 0.450, 'READY_FOR_OUTBOUND', NOW(), NOW()),
(2, 2, 'BOX-2024-00002-02', 28.0, 18.0, 10.0, 0.005040, 0.200, 'READY_FOR_OUTBOUND', NOW(), NOW()),
(3, 1, 'BOX-2024-00003-01', 35.0, 25.0, 15.0, 0.013125, 0.900, 'INBOUND_PENDING', NOW(), NOW()),
(4, 1, 'BOX-2024-00004-01', 22.0, 18.0, 10.0, 0.003960, 0.550, 'SHIPPED', NOW(), NOW()),
(5, 1, 'BOX-2024-00005-01', 40.0, 30.0, 20.0, 0.024000, 2.200, 'DELIVERED', NOW(), NOW()),

-- Enterprise order boxes
(6, 1, 'BOX-2024-00006-01', 60.0, 40.0, 25.0, 0.060000, 11.000, 'INBOUND_COMPLETED', NOW(), NOW()),
(7, 1, 'BOX-2024-00007-01', 35.0, 25.0, 20.0, 0.017500, 5.700, 'READY_FOR_OUTBOUND', NOW(), NOW()),
(7, 2, 'BOX-2024-00007-02', 45.0, 35.0, 25.0, 0.039375, 9.200, 'READY_FOR_OUTBOUND', NOW(), NOW()),
(7, 3, 'BOX-2024-00007-03', 40.0, 30.0, 22.0, 0.026400, 6.400, 'READY_FOR_OUTBOUND', NOW(), NOW()),
(8, 1, 'BOX-2024-00008-01', 50.0, 35.0, 30.0, 0.052500, 7.200, 'INBOUND_PENDING', NOW(), NOW()),
(8, 2, 'BOX-2024-00008-02', 45.0, 30.0, 25.0, 0.033750, 5.800, 'INBOUND_PENDING', NOW(), NOW()),
(9, 1, 'BOX-2024-00009-01', 70.0, 50.0, 35.0, 0.122500, 25.000, 'SHIPPED', NOW(), NOW()),
(10, 1, 'BOX-2024-00010-01', 80.0, 60.0, 40.0, 0.192000, 35.600, 'DELIVERED', NOW(), NOW()),
(10, 2, 'BOX-2024-00010-02', 75.0, 55.0, 38.0, 0.156750, 32.200, 'DELIVERED', NOW(), NOW()),

-- Partner order boxes
(11, 1, 'BOX-2024-00011-01', 50.0, 40.0, 30.0, 0.060000, 5.000, 'INBOUND_COMPLETED', NOW(), NOW()),
(12, 1, 'BOX-2024-00012-01', 60.0, 45.0, 35.0, 0.094500, 8.000, 'READY_FOR_OUTBOUND', NOW(), NOW()),
(12, 2, 'BOX-2024-00012-02', 55.0, 40.0, 32.0, 0.070400, 7.200, 'READY_FOR_OUTBOUND', NOW(), NOW()),
(13, 1, 'BOX-2024-00013-01', 65.0, 50.0, 30.0, 0.097500, 12.500, 'INBOUND_PENDING', NOW(), NOW()),

-- Test boxes for various scenarios
(14, 1, 'BOX-2024-00014-01', 30.0, 25.0, 20.0, 0.015000, 1.500, 'CANCELLED', NOW(), NOW()),
(15, 1, 'BOX-2024-00015-01', 40.0, 35.0, 25.0, 0.035000, 3.000, 'ON_HOLD', NOW(), NOW()),

-- Recent order boxes
(16, 1, 'BOX-2024-00016-01', 20.0, 15.0, 10.0, 0.003000, 0.200, 'INBOUND_COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(17, 1, 'BOX-2024-00017-01', 25.0, 20.0, 15.0, 0.007500, 0.350, 'READY_FOR_OUTBOUND', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(18, 1, 'BOX-2024-00018-01', 80.0, 60.0, 45.0, 0.216000, 26.000, 'INBOUND_PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(19, 1, 'BOX-2024-00019-01', 90.0, 70.0, 50.0, 0.315000, 36.000, 'SHIPPED', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(20, 1, 'BOX-2024-00020-01', 55.0, 45.0, 35.0, 0.086625, 7.500, 'DELIVERED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- Large CBM test boxes (> 29 m³ total)
(21, 1, 'BOX-2024-00021-01', 150.0, 120.0, 100.0, 1.800000, 25.000, 'CREATED', NOW(), NOW()),
(21, 2, 'BOX-2024-00021-02', 200.0, 150.0, 120.0, 3.600000, 35.000, 'CREATED', NOW(), NOW()),
(21, 3, 'BOX-2024-00021-03', 180.0, 140.0, 110.0, 2.772000, 30.000, 'CREATED', NOW(), NOW()),
(21, 4, 'BOX-2024-00021-04', 300.0, 200.0, 150.0, 9.000000, 55.000, 'CREATED', NOW(), NOW()),
(21, 5, 'BOX-2024-00021-05', 250.0, 180.0, 140.0, 6.300000, 45.000, 'CREATED', NOW(), NOW()),
(21, 6, 'BOX-2024-00021-06', 220.0, 160.0, 130.0, 4.576000, 38.000, 'CREATED', NOW(), NOW()),
(21, 7, 'BOX-2024-00021-07', 280.0, 190.0, 145.0, 7.714000, 52.000, 'CREATED', NOW(), NOW()),

-- No-code user boxes
(22, 1, 'BOX-2024-00022-01', 20.0, 15.0, 12.0, 0.003600, 0.350, 'CREATED', NOW(), NOW()),
(23, 1, 'BOX-2024-00023-01', 25.0, 18.0, 14.0, 0.006300, 0.450, 'ON_HOLD', NOW(), NOW());

-- Insert warehouse data
INSERT INTO warehouses (name, location, address, contact_person, contact_phone, contact_email, capacity_m3, status, created_at, updated_at) VALUES
('Bangkok Main Warehouse', 'Bangkok', '123 Warehouse District, Bangkok, Thailand', 'John Manager', '+66-800-111-222', 'bangkok@ycs.com', 10000.0, 'ACTIVE', NOW(), NOW()),
('Pattaya Distribution Center', 'Pattaya', '456 Logistics Park, Pattaya, Thailand', 'Sarah Supervisor', '+66-800-333-444', 'pattaya@ycs.com', 5000.0, 'ACTIVE', NOW(), NOW()),
('Chiang Mai Hub', 'Chiang Mai', '789 Northern Logistics, Chiang Mai, Thailand', 'Mike Coordinator', '+66-800-555-666', 'chiangmai@ycs.com', 3000.0, 'ACTIVE', NOW(), NOW());

-- Insert inventory data
INSERT INTO inventory (warehouse_id, item_code, item_name, category, quantity, unit_weight, location_code, last_updated, created_at, updated_at) VALUES
(1, 'PHONE-001', 'Samsung Galaxy', 'Electronics', 150, 0.200, 'A1-01', NOW(), NOW(), NOW()),
(1, 'PHONE-002', 'iPhone 14', 'Electronics', 120, 0.240, 'A1-02', NOW(), NOW(), NOW()),
(1, 'LAPTOP-001', 'MacBook Pro', 'Electronics', 75, 2.100, 'B2-01', NOW(), NOW(), NOW()),
(1, 'WATCH-001', 'Rolex Submariner', 'Luxury', 25, 0.150, 'S1-01', NOW(), NOW(), NOW()),
(2, 'SHOES-001', 'Nike Air Max', 'Fashion', 200, 0.800, 'C3-01', NOW(), NOW(), NOW()),
(2, 'BAG-001', 'Hermes Birkin', 'Fashion', 15, 1.200, 'L4-01', NOW(), NOW(), NOW()),
(3, 'SUPPLY-001', 'Packaging Materials', 'Supplies', 1000, 0.050, 'S5-01', NOW(), NOW(), NOW()),
(3, 'SUPPLY-002', 'Shipping Boxes', 'Supplies', 500, 2.500, 'S5-02', NOW(), NOW(), NOW());

-- Insert scan events for testing
INSERT INTO scan_events (label_code, scan_type, warehouse_id, user_id, notes, created_at) VALUES
('BOX-2024-00001-01', 'INBOUND', 1, 3, 'Package received and processed', DATE_SUB(NOW(), INTERVAL 2 DAY)),
('BOX-2024-00002-01', 'INBOUND', 1, 4, 'Package inspected and stored', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('BOX-2024-00002-02', 'INBOUND', 1, 4, 'Package inspected and stored', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('BOX-2024-00004-01', 'OUTBOUND', 1, 3, 'Package shipped to customer', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
('BOX-2024-00005-01', 'DELIVERY_CONFIRMED', 2, 4, 'Package delivered successfully', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
('BOX-2024-00016-01', 'INBOUND', 1, 3, 'Recent package received', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('BOX-2024-00017-01', 'QUALITY_CHECK', 1, 4, 'Quality inspection passed', DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- Insert shipment tracking data
INSERT INTO shipment_tracking (order_id, tracking_number, carrier, status, location, estimated_delivery, actual_delivery, notes, created_at, updated_at) VALUES
(4, 'TRK-001-2024', 'Thai Express', 'DELIVERED', 'Phuket Distribution Center', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 6 HOUR), 'Package delivered successfully', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(5, 'TRK-002-2024', 'Thailand Post', 'DELIVERED', 'Hua Hin Post Office', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'Package delivered to recipient', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(19, 'TRK-003-2024', 'DHL Express', 'IN_TRANSIT', 'Bangkok Sorting Facility', NOW(), NULL, 'Package in transit to destination', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
(20, 'TRK-004-2024', 'FedEx', 'DELIVERED', 'Bangkok Central', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 12 HOUR), 'Package delivered on time', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- Insert estimates data
INSERT INTO estimates (order_id, estimate_number, version, total_shipping_cost, local_delivery_cost, repack_cost, insurance_cost, total_cost, currency, valid_until, status, notes, created_at, updated_at) VALUES
(1, 'EST-2024-00001', 1, 150.00, 50.00, 25.00, 15.00, 240.00, 'THB', DATE_ADD(NOW(), INTERVAL 7 DAY), 'PENDING_APPROVAL', 'Initial estimate for small package', NOW(), NOW()),
(2, 'EST-2024-00002', 1, 450.00, 80.00, 35.00, 45.00, 610.00, 'THB', DATE_ADD(NOW(), INTERVAL 7 DAY), 'APPROVED', 'Express shipping estimate', NOW(), NOW()),
(6, 'EST-2024-00006', 1, 2200.00, 150.00, 85.00, 110.00, 2545.00, 'THB', DATE_ADD(NOW(), INTERVAL 7 DAY), 'PENDING_APPROVAL', 'Large enterprise shipment', NOW(), NOW()),
(7, 'EST-2024-00007', 1, 4500.00, 200.00, 125.00, 250.00, 5075.00, 'THB', DATE_ADD(NOW(), INTERVAL 7 DAY), 'APPROVED', 'Premium service estimate', NOW(), NOW()),
(11, 'EST-2024-00011', 1, 875.00, 75.00, 45.00, 70.00, 1065.00, 'THB', DATE_ADD(NOW(), INTERVAL 7 DAY), 'PENDING_APPROVAL', 'Partner shipment estimate', NOW(), NOW());

-- Insert payments data
INSERT INTO payments (order_id, payment_method, amount, currency, payment_reference, payment_date, status, notes, created_at, updated_at) VALUES
(2, 'CREDIT_CARD', 610.00, 'THB', 'PAY-CC-001-2024', DATE_SUB(NOW(), INTERVAL 1 DAY), 'COMPLETED', 'Payment processed successfully', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'BANK_TRANSFER', 450.00, 'THB', 'PAY-BT-002-2024', DATE_SUB(NOW(), INTERVAL 2 DAY), 'COMPLETED', 'Bank transfer confirmed', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'CREDIT_CARD', 1250.00, 'THB', 'PAY-CC-003-2024', DATE_SUB(NOW(), INTERVAL 3 DAY), 'COMPLETED', 'Corporate card payment', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(7, 'BANK_TRANSFER', 5075.00, 'THB', 'PAY-BT-004-2024', DATE_SUB(NOW(), INTERVAL 1 DAY), 'COMPLETED', 'Large payment confirmed', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'CREDIT_CARD', 240.00, 'THB', 'PAY-CC-005-2024', NOW(), 'PENDING', 'Payment authorization pending', NOW(), NOW());

-- Insert partner referrals data
INSERT INTO partner_referrals (partner_id, referred_user_id, referral_code, commission_rate, commission_amount, currency, status, created_at, updated_at) VALUES
(15, 6, 'REF-PTR001-001', 5.00, 12.75, 'THB', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
(15, 7, 'REF-PTR001-002', 5.00, 18.50, 'THB', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
(16, 11, 'REF-PTR002-001', 3.00, 165.00, 'THB', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY)),
(17, 8, 'REF-PTR003-001', 4.00, 39.21, 'THB', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY));

-- Insert audit logs
INSERT INTO audit_logs (user_id, action, table_name, record_id, old_values, new_values, ip_address, user_agent, created_at) VALUES
(1, 'CREATE', 'orders', 1, NULL, '{"order_code":"YCS-2024-00001","status":"REQUESTED"}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'UPDATE', 'order_boxes', 1, '{"status":"CREATED"}', '{"status":"INBOUND_COMPLETED"}', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X)', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(4, 'CREATE', 'scan_events', 1, NULL, '{"label_code":"BOX-2024-00001-01","scan_type":"INBOUND"}', '192.168.1.102', 'Mozilla/5.0 (iPhone; CPU iPhone OS)', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 'UPDATE', 'users', 18, '{"status":"PENDING_APPROVAL"}', '{"status":"ACTIVE"}', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', NOW());

-- Print summary and test account information
SELECT 'Database initialized successfully!' as status;
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as order_count FROM orders;
SELECT COUNT(*) as item_count FROM order_items;
SELECT COUNT(*) as box_count FROM order_boxes;
SELECT COUNT(*) as warehouse_count FROM warehouses;
SELECT COUNT(*) as scan_event_count FROM scan_events;
SELECT COUNT(*) as tracking_count FROM shipment_tracking;
SELECT COUNT(*) as estimate_count FROM estimates;
SELECT COUNT(*) as payment_count FROM payments;

-- Display test account information
SELECT '=== TEST ACCOUNTS (Password: password123) ===' as info;
SELECT email, name, role, status, member_code FROM users ORDER BY role, id;