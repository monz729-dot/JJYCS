-- YCS LMS 더미 데이터 생성 스크립트
-- 테스트 계정별 연동 데이터

-- 사용자 테이블 더미 데이터
INSERT INTO users (id, username, email, password, name, phone, user_type, status, member_code, created_at, updated_at) VALUES
-- 일반사용자
(1, 'general_user', 'general@test.com', '$2a$10$7qvb7Mf8uO6nF3vN5xF5.uX1qY2zZ3A4B5C6D7E8F9G0H1I2J3K4L5', '김일반', '010-1234-5678', 'general', 'approved', 'GEN001', NOW(), NOW()),
-- 기업사용자  
(2, 'corporate_user', 'corporate@test.com', '$2a$10$7qvb7Mf8uO6nF3vN5xF5.uX1qY2zZ3A4B5C6D7E8F9G0H1I2J3K4L5', '박기업', '010-2345-6789', 'corporate', 'approved', 'COR001', NOW(), NOW()),
-- 파트너사용자
(3, 'partner_user', 'partner@test.com', '$2a$10$7qvb7Mf8uO6nF3vN5xF5.uX1qY2zZ3A4B5C6D7E8F9G0H1I2J3K4L5', '이파트너', '010-3456-7890', 'partner', 'approved', 'PAR001', NOW(), NOW()),
-- 창고관리자
(4, 'warehouse_user', 'warehouse@test.com', '$2a$10$7qvb7Mf8uO6nF3vN5xF5.uX1qY2zZ3A4B5C6D7E8F9G0H1I2J3K4L5', '최창고', '010-4567-8901', 'warehouse', 'approved', 'WH001', NOW(), NOW()),
-- 시스템관리자
(5, 'admin_user', 'admin@test.com', '$2a$10$7qvb7Mf8uO6nF3vN5xF5.uX1qY2zZ3A4B5C6D7E8F9G0H1I2J3K4L5', '정관리자', '010-5678-9012', 'admin', 'approved', 'ADM001', NOW(), NOW());

-- 기업 프로필 정보
INSERT INTO enterprise_profiles (user_id, company_name, business_number, representative, address, vat_type, created_at) VALUES
(2, '(주)테스트기업', '123-45-67890', '박기업', '서울시 강남구 테헤란로 123', 'general', NOW());

-- 파트너 프로필 정보
INSERT INTO partner_profiles (user_id, commission_rate, referral_code, bank_account, created_at) VALUES
(3, 0.05, 'REF-PAR001', '국민은행 123-456-789012', NOW());

-- 창고 프로필 정보
INSERT INTO warehouse_profiles (user_id, warehouse_id, permissions, created_at) VALUES
(4, 1, 'scan,inbound,outbound,label', NOW());

-- 주소 정보
INSERT INTO addresses (user_id, type, name, phone, country, province, district, sub_district, postal_code, address_line, is_default, created_at) VALUES
-- 일반사용자 주소
(1, 'shipping', '김일반', '010-1234-5678', 'TH', 'Bangkok', 'Bang Rak', 'Silom', '10500', '123 Silom Road', true, NOW()),
(1, 'billing', '김일반', '010-1234-5678', 'KR', '서울특별시', '강남구', '역삼동', '06234', '테헤란로 123', false, NOW()),
-- 기업사용자 주소
(2, 'shipping', '(주)테스트기업', '010-2345-6789', 'TH', 'Bangkok', 'Chatuchak', 'Lat Yao', '10900', '456 Phahonyothin Road', true, NOW()),
(2, 'billing', '(주)테스트기업', '010-2345-6789', 'KR', '서울특별시', '강남구', '삼성동', '06164', '영동대로 511', false, NOW());

-- 주문 데이터
INSERT INTO orders (id, user_id, order_number, order_type, status, shipping_method, total_items, total_weight_kg, total_cbm_m3, estimated_amount, actual_amount, currency, requires_extra_recipient, is_delayed_no_code, created_at, updated_at) VALUES
-- 일반사용자 주문
(1, 1, 'ORD-2024-001', 'sea', 'pending', 'sea_lcl', 5, 12.5, 0.025, 85000, NULL, 'KRW', false, false, NOW() - INTERVAL 2 DAY, NOW()),
(2, 1, 'ORD-2024-002', 'air', 'processing', 'air_express', 2, 3.2, 0.008, 125000, 118000, 'KRW', false, false, NOW() - INTERVAL 1 DAY, NOW()),
(3, 1, 'ORD-2024-003', 'sea', 'shipped', 'sea_lcl', 8, 18.7, 0.035, 156000, 149000, 'KRW', true, false, NOW() - INTERVAL 5 DAY, NOW()),
-- 기업사용자 주문  
(4, 2, 'ORD-2024-004', 'sea', 'delivered', 'sea_fcl', 50, 125.8, 2.5, 850000, 832000, 'KRW', false, false, NOW() - INTERVAL 10 DAY, NOW()),
(5, 2, 'ORD-2024-005', 'air', 'processing', 'air_cargo', 25, 68.3, 1.2, 520000, NULL, 'KRW', true, false, NOW() - INTERVAL 3 DAY, NOW()),
(6, 2, 'ORD-2024-006', 'sea', 'pending', 'sea_lcl', 15, 35.6, 0.68, 245000, NULL, 'KRW', false, true, NOW() - INTERVAL 1 DAY, NOW());

-- 주문 아이템
INSERT INTO order_items (order_id, item_name, description, quantity, unit_price, total_price, weight_kg, hs_code, ems_restricted, created_at) VALUES
-- ORD-2024-001 아이템
(1, '의류 (티셔츠)', '면 100% 반팔 티셔츠', 10, 15000, 150000, 2.5, '6109.10.00', false, NOW()),
(1, '신발', '운동화', 2, 50000, 100000, 1.8, '6404.11.00', false, NOW()),
-- ORD-2024-002 아이템  
(2, '전자제품 (이어폰)', '무선 블루투스 이어폰', 5, 80000, 400000, 0.5, '8518.30.00', false, NOW()),
-- ORD-2024-003 아이템
(3, '화장품', '스킨케어 세트', 8, 45000, 360000, 2.4, '3304.99.00', false, NOW()),
(3, '가방', '가죽 핸드백', 3, 120000, 360000, 4.2, '4202.21.00', false, NOW()),
-- ORD-2024-004 아이템 (기업 대량주문)
(4, '사무용품', '노트북', 20, 800000, 16000000, 40.0, '8471.30.00', false, NOW()),
(4, '의자', '사무용 의자', 30, 150000, 4500000, 85.8, '9401.30.00', false, NOW()),
-- ORD-2024-005 아이템
(5, '기계부품', '정밀기계 부품', 100, 25000, 2500000, 68.3, '8484.10.00', false, NOW()),
-- ORD-2024-006 아이템  
(6, '식품', '한국 전통차', 50, 12000, 600000, 35.6, '0902.30.00', false, NOW());

-- 주문 박스
INSERT INTO order_boxes (order_id, box_number, length_cm, width_cm, height_cm, weight_kg, created_at) VALUES
-- ORD-2024-001 박스
(1, 1, 40, 30, 20, 6.5, NOW()),
(1, 2, 35, 25, 18, 6.0, NOW()),
-- ORD-2024-002 박스
(2, 1, 25, 20, 15, 3.2, NOW()),
-- ORD-2024-003 박스
(3, 1, 45, 35, 25, 9.3, NOW()),
(3, 2, 42, 32, 22, 9.4, NOW()),
-- ORD-2024-004 박스 (기업)
(4, 1, 120, 80, 60, 62.9, NOW()),
(4, 2, 118, 78, 58, 62.9, NOW()),
-- ORD-2024-005 박스
(5, 1, 80, 60, 45, 68.3, NOW()),
-- ORD-2024-006 박스
(6, 1, 60, 40, 30, 35.6, NOW());

-- 창고 정보
INSERT INTO warehouses (id, name, location, type, capacity_cbm, current_usage_cbm, manager_id, created_at) VALUES
(1, '인천 메인 창고', '인천광역시 중구 운서동', 'main', 10000.0, 2500.5, 4, NOW()),
(2, '부산 서브 창고', '부산광역시 강서구 대저동', 'sub', 5000.0, 1200.3, 4, NOW());

-- 재고 정보
INSERT INTO inventory (id, warehouse_id, order_id, label_code, status, location_code, scan_count, last_scan_at, created_at, updated_at) VALUES
-- 창고에 있는 주문들
(1, 1, 1, 'LBL-001-001', 'inbound_completed', 'A-01-01', 3, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 2 DAY, NOW()),
(2, 1, 2, 'LBL-002-001', 'ready_for_outbound', 'A-01-02', 5, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 1 DAY, NOW()),
(3, 1, 3, 'LBL-003-001', 'outbound_completed', 'SHIP-001', 8, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 5 DAY, NOW()),
(4, 2, 4, 'LBL-004-001', 'outbound_completed', 'SHIP-002', 12, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 10 DAY, NOW()),
(5, 1, 5, 'LBL-005-001', 'inbound_completed', 'B-02-01', 2, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 3 DAY, NOW()),
(6, 1, 6, 'LBL-006-001', 'created', 'PENDING', 0, NULL, NOW() - INTERVAL 1 DAY, NOW());

-- 스캔 이벤트
INSERT INTO scan_events (id, warehouse_id, user_id, label_code, scan_type, location_code, notes, created_at) VALUES
(1, 1, 4, 'LBL-001-001', 'inbound', 'A-01-01', '정상 입고 처리', NOW() - INTERVAL 2 DAY),
(2, 1, 4, 'LBL-001-001', 'location_update', 'A-01-01', '위치 확인', NOW() - INTERVAL 1 DAY),
(3, 1, 4, 'LBL-002-001', 'inbound', 'A-01-02', '정상 입고 처리', NOW() - INTERVAL 1 DAY),
(4, 1, 4, 'LBL-002-001', 'outbound_ready', 'A-01-02', '출고 준비 완료', NOW() - INTERVAL 2 HOUR),
(5, 1, 4, 'LBL-003-001', 'outbound', 'SHIP-001', '출고 완료', NOW() - INTERVAL 3 DAY),
(6, 1, 4, 'LBL-005-001', 'inbound', 'B-02-01', '정상 입고 처리', NOW() - INTERVAL 3 DAY);

-- 견적 정보
INSERT INTO estimates (id, order_id, user_id, estimate_type, shipping_cost, customs_fee, handling_fee, insurance_fee, total_amount, currency, valid_until, status, created_at, updated_at) VALUES
-- 1차 견적
(1, 1, 1, 'first', 65000, 8000, 5000, 2000, 85000, 'KRW', NOW() + INTERVAL 7 DAY, 'approved', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY),
(2, 2, 1, 'first', 95000, 12000, 8000, 3000, 125000, 'KRW', NOW() + INTERVAL 7 DAY, 'approved', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
-- 2차 견적 (실제 비용)
(3, 2, 1, 'final', 89000, 11000, 8000, 3000, 118000, 'KRW', NOW() + INTERVAL 3 DAY, 'approved', NOW() - INTERVAL 1 DAY, NOW()),
(4, 3, 1, 'first', 135000, 15000, 6000, 4000, 156000, 'KRW', NOW() + INTERVAL 7 DAY, 'approved', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 4 DAY),
(5, 3, 1, 'final', 128000, 14000, 7000, 4000, 149000, 'KRW', NOW() + INTERVAL 3 DAY, 'approved', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 3 DAY),
-- 기업 주문 견적
(6, 4, 2, 'first', 720000, 85000, 25000, 15000, 850000, 'KRW', NOW() + INTERVAL 14 DAY, 'approved', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 9 DAY),
(7, 4, 2, 'final', 695000, 82000, 25000, 15000, 832000, 'KRW', NOW() + INTERVAL 10 DAY, 'approved', NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 8 DAY),
(8, 5, 2, 'first', 420000, 55000, 28000, 12000, 520000, 'KRW', NOW() + INTERVAL 7 DAY, 'pending', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(9, 6, 2, 'first', 195000, 28000, 15000, 7000, 245000, 'KRW', NOW() + INTERVAL 7 DAY, 'pending', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);

-- 결제 정보  
INSERT INTO payments (id, order_id, user_id, amount, currency, payment_method, payment_status, transaction_id, paid_at, created_at) VALUES
(1, 2, 1, 118000, 'KRW', 'card', 'completed', 'TXN-20240115-001', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(2, 3, 1, 149000, 'KRW', 'bank_transfer', 'completed', 'TXN-20240110-002', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
(3, 4, 2, 832000, 'KRW', 'bank_transfer', 'completed', 'TXN-20240105-003', NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY);

-- 배송 추적 정보
INSERT INTO shipment_tracking (id, order_id, tracking_number, carrier, status, current_location, estimated_delivery, last_updated, created_at) VALUES
(1, 2, 'AIR-2024-002-KR', 'Korean Air Cargo', 'in_transit', 'Bangkok Suvarnabhumi Airport', NOW() + INTERVAL 2 DAY, NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 1 DAY),
(2, 3, 'SEA-2024-003-TH', 'CJ Logistics', 'delivered', 'Bangkok Delivery Center', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 5 DAY),
(3, 4, 'FCL-2024-004-BK', 'Hanjin Shipping', 'delivered', 'Bangkok Port', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 10 DAY);

-- 파트너 추천 정보
INSERT INTO partner_referrals (id, partner_id, referred_user_id, referral_code, commission_rate, order_id, commission_amount, status, created_at) VALUES
(1, 3, 1, 'REF-PAR001', 0.05, 2, 5900, 'paid', NOW() - INTERVAL 1 DAY),
(2, 3, 1, 'REF-PAR001', 0.05, 3, 7450, 'paid', NOW() - INTERVAL 4 DAY),
(3, 3, 2, 'REF-PAR001', 0.05, 4, 41600, 'paid', NOW() - INTERVAL 9 DAY);

-- 시스템 설정
INSERT INTO config (key_name, value, description, created_at, updated_at) VALUES
('exchange_rate_usd_krw', '1320.50', 'USD to KRW exchange rate', NOW(), NOW()),
('exchange_rate_thb_krw', '38.75', 'THB to KRW exchange rate', NOW(), NOW()),
('cbm_threshold_air', '29.0', 'CBM threshold for auto air shipping', NOW(), NOW()),
('amount_threshold_thb', '1500.0', 'Amount threshold for extra recipient info', NOW(), NOW()),
('approval_processing_days', '2', 'Business days for approval processing', NOW(), NOW());

-- 감사 로그
INSERT INTO audit_logs (id, user_id, action, entity_type, entity_id, old_values, new_values, ip_address, user_agent, created_at) VALUES
(1, 4, 'SCAN_INBOUND', 'inventory', 1, NULL, '{"status": "inbound_completed", "location": "A-01-01"}', '192.168.1.100', 'Mozilla/5.0', NOW() - INTERVAL 2 DAY),
(2, 4, 'SCAN_OUTBOUND_READY', 'inventory', 2, '{"status": "inbound_completed"}', '{"status": "ready_for_outbound"}', '192.168.1.100', 'Mozilla/5.0', NOW() - INTERVAL 2 HOUR),
(3, 5, 'ORDER_STATUS_UPDATE', 'orders', 2, '{"status": "pending"}', '{"status": "processing"}', '192.168.1.200', 'Mozilla/5.0', NOW() - INTERVAL 1 DAY),
(4, 5, 'PAYMENT_APPROVED', 'payments', 1, '{"status": "pending"}', '{"status": "completed"}', '192.168.1.200', 'Mozilla/5.0', NOW() - INTERVAL 1 DAY),
(5, 1, 'ORDER_CREATED', 'orders', 1, NULL, '{"order_number": "ORD-2024-001", "status": "pending"}', '192.168.1.50', 'Mozilla/5.0', NOW() - INTERVAL 2 DAY);

-- 알림 데이터 (notifications 테이블이 있다면)
-- INSERT INTO notifications (user_id, type, title, message, is_read, created_at) VALUES
-- (1, 'order_update', '주문 상태 변경', 'ORD-2024-002 주문이 처리중 상태로 변경되었습니다.', false, NOW() - INTERVAL 6 HOUR),
-- (1, 'payment_completed', '결제 완료', 'ORD-2024-002 주문의 결제가 완료되었습니다.', true, NOW() - INTERVAL 1 DAY),
-- (2, 'order_delivered', '배송 완료', 'ORD-2024-004 주문이 성공적으로 배송 완료되었습니다.', true, NOW() - INTERVAL 5 DAY),
-- (3, 'commission_paid', '수수료 지급', '1월 수수료 54,950원이 지급되었습니다.', false, NOW() - INTERVAL 1 DAY),
-- (4, 'inventory_alert', '재고 처리 필요', '새로운 입고 대기 상품이 있습니다.', false, NOW() - INTERVAL 3 HOUR),
-- (5, 'system_maintenance', '시스템 점검', '2024-01-20 02:00-04:00 시스템 점검이 예정되어 있습니다.', false, NOW() - INTERVAL 12 HOUR);

COMMIT;