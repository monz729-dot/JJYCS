-- YCS LMS 더미 데이터 삽입

-- 1. 사용자 데이터 (비밀번호는 bcrypt 해시: Test123!@#)
INSERT INTO users (id, username, email, password, name, phone, user_type, status, member_code, created_at) VALUES
(1, 'general_user', 'general@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTsJlylvMZqqhnd/M9A9dQvDSA.LQweOh', '김일반', '010-1111-2222', 'general', 'approved', 'GEN001', datetime('now', '-7 days')),
(2, 'corporate_user', 'corporate@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTsJlylvMZqqhnd/M9A9dQvDSA.LQweO', '박기업', '010-2222-3333', 'corporate', 'approved', 'COR001', datetime('now', '-6 days')),
(3, 'partner_user', 'partner@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTsJlylvMZqqhnd/M9A9dQvDSA.LQweO', '이파트너', '010-3333-4444', 'partner', 'approved', 'PAR001', datetime('now', '-5 days')),
(4, 'warehouse_user', 'warehouse@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTsJlylvMZqqhnd/M9A9dQvDSA.LQweO', '최창고', '010-4444-5555', 'warehouse', 'approved', 'WH001', datetime('now', '-4 days')),
(5, 'admin_user', 'admin@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTsJlylvMZqqhnd/M9A9dQvDSA.LQweO', '정관리자', '010-5555-6666', 'admin', 'approved', 'ADM001', datetime('now', '-3 days'));

-- 2. 기업 프로필
INSERT INTO enterprise_profiles (user_id, company_name, business_number, representative, address, vat_type) VALUES
(2, '(주)테스트기업', '123-45-67890', '박기업', '서울특별시 강남구 테헤란로 123', 'general');

-- 3. 파트너 프로필
INSERT INTO partner_profiles (user_id, commission_rate, referral_code, bank_account) VALUES
(3, 0.0500, 'REF-PAR001', '국민은행 123-456-789012');

-- 4. 창고 프로필
INSERT INTO warehouse_profiles (user_id, warehouse_id, permissions) VALUES
(4, 1, 'scan,inbound,outbound,label'),
(4, 2, 'scan,inbound,outbound');

-- 5. 주소 데이터
INSERT INTO addresses (user_id, type, name, phone, country, province, district, sub_district, postal_code, address_line, is_default) VALUES
(1, 'shipping', '김일반', '010-1111-2222', 'TH', 'Bangkok', 'Chatuchak', 'Lat Mayom', '10900', '123 Sukhumvit Road', 1),
(1, 'billing', '김일반', '010-1111-2222', 'KR', '서울특별시', '강남구', '삼성동', '06292', '서울 강남구 테헤란로 212', 0),
(2, 'shipping', '박기업', '010-2222-3333', 'TH', 'Bangkok', 'Khlong Toei', 'Khlong Tan', '10110', '456 Asok Road', 1),
(2, 'billing', '박기업', '010-2222-3333', 'KR', '서울특별시', '강남구', '역삼동', '06253', '서울 강남구 테헤란로 123', 0);

-- 6. 창고 데이터
INSERT INTO warehouses (id, name, location, type, capacity_cbm, current_usage_cbm, manager_id) VALUES
(1, '인천 메인 창고', '인천광역시 중구 항동', 'main', 10000.000, 2500.000, 4),
(2, '부산 서브 창고', '부산광역시 강서구 대저동', 'sub', 5000.000, 1200.000, 4);

-- 7. 주문 데이터
INSERT INTO orders (id, user_id, order_number, order_type, status, shipping_method, total_items, total_weight_kg, total_cbm_m3, estimated_amount, actual_amount, currency, requires_extra_recipient, is_delayed_no_code, created_at) VALUES
(1, 1, 'ORD-2024-001', 'sea', 'pending', 'LCL', 3, 15.500, 0.850000, 85000.00, 85000.00, 'KRW', 0, 0, datetime('now', '-3 days')),
(2, 1, 'ORD-2024-002', 'air', 'processing', 'Express', 2, 5.200, 0.120000, 118000.00, 118000.00, 'KRW', 0, 0, datetime('now', '-2 days')),
(3, 1, 'ORD-2024-003', 'sea', 'delivered', 'LCL', 1, 8.750, 0.350000, 149000.00, 149000.00, 'KRW', 0, 0, datetime('now', '-5 days')),
(4, 2, 'ORD-2024-004', 'sea', 'delivered', 'FCL', 50, 1250.000, 28.500000, 832000.00, 832000.00, 'KRW', 1, 0, datetime('now', '-4 days')),
(5, 2, 'ORD-2024-005', 'air', 'processing', 'Cargo', 30, 180.000, 15.200000, 520000.00, 421000.00, 'KRW', 1, 0, datetime('now', '-2 days')),
(6, 2, 'ORD-2024-006', 'sea', 'pending', 'LCL', 5, 25.000, 1.200000, 245000.00, 245000.00, 'KRW', 0, 1, datetime('now', '-1 day'));

-- 8. 주문 아이템 데이터
INSERT INTO order_items (order_id, item_name, description, quantity, unit_price, total_price, weight_kg, hs_code, ems_restricted) VALUES
-- ORD-2024-001 아이템들
(1, '휴대폰 케이스', '실리콘 재질 투명 케이스', 10, 5000.00, 50000.00, 2.000, '3926.90.9000', 0),
(1, '충전기', 'USB-C 고속충전기', 3, 8000.00, 24000.00, 1.500, '8504.40.9090', 0),
(1, '이어폰', '무선 블루투스 이어폰', 1, 11000.00, 11000.00, 0.500, '8518.30.0000', 0),

-- ORD-2024-002 아이템들
(2, '노트북', '15인치 업무용 노트북', 1, 89000.00, 89000.00, 2.200, '8471.30.0000', 0),
(2, '마우스', '무선 옵티컬 마우스', 1, 29000.00, 29000.00, 0.300, '8471.60.9000', 0),

-- ORD-2024-003 아이템들
(3, '운동화', '런닝화 280mm', 1, 149000.00, 149000.00, 0.750, '6403.91.0000', 0),

-- ORD-2024-004 아이템들 (대량 주문)
(4, '사무용 의자', '인체공학적 오피스 체어', 30, 15000.00, 450000.00, 720.000, '9401.30.0000', 0),
(4, '책상', '1200x600mm 사무용 책상', 20, 19100.00, 382000.00, 530.000, '9403.30.9000', 0),

-- ORD-2024-005 아이템들
(5, '태블릿', '10인치 안드로이드 태블릿', 15, 21000.00, 315000.00, 105.000, '8471.30.0000', 0),
(5, '키보드', '기계식 키보드', 15, 6933.33, 104000.00, 75.000, '8471.60.9000', 0),

-- ORD-2024-006 아이템들
(6, '가습기', '초음파 가습기 5L', 5, 49000.00, 245000.00, 25.000, '8509.80.0000', 0);

-- 9. 주문 박스 데이터 (CBM 자동 계산)
INSERT INTO order_boxes (order_id, box_number, length_cm, width_cm, height_cm, weight_kg, cbm_m3) VALUES
-- ORD-2024-001 박스들
(1, 1, 30.0, 25.0, 20.0, 8.0, 0.015000),
(1, 2, 40.0, 35.0, 25.0, 7.5, 0.035000),

-- ORD-2024-002 박스들
(2, 1, 45.0, 30.0, 8.0, 3.5, 0.010800),
(2, 2, 25.0, 20.0, 15.0, 1.7, 0.007500),

-- ORD-2024-003 박스들
(3, 1, 35.0, 25.0, 15.0, 8.75, 0.013125),

-- ORD-2024-004 박스들 (대량)
(4, 1, 120.0, 80.0, 90.0, 400.0, 0.864000),
(4, 2, 150.0, 100.0, 80.0, 450.0, 1.200000),
(4, 3, 140.0, 90.0, 85.0, 400.0, 1.071000),

-- ORD-2024-005 박스들
(5, 1, 80.0, 60.0, 40.0, 90.0, 0.192000),
(5, 2, 70.0, 50.0, 35.0, 90.0, 0.122500),

-- ORD-2024-006 박스들
(6, 1, 50.0, 40.0, 30.0, 25.0, 0.060000);

-- 10. 재고 데이터
INSERT INTO inventory (warehouse_id, order_id, label_code, status, location_code, scan_count, last_scan_at) VALUES
(1, 1, 'LBL-ORD-001-01', 'inbound_completed', 'A1-001', 2, datetime('now', '-2 days')),
(1, 2, 'LBL-ORD-002-01', 'ready_for_outbound', 'A1-002', 3, datetime('now', '-1 day')),
(1, 3, 'LBL-ORD-003-01', 'outbound_completed', 'SHIP', 4, datetime('now', '-3 days')),
(2, 4, 'LBL-ORD-004-01', 'outbound_completed', 'SHIP', 5, datetime('now', '-2 days')),
(1, 5, 'LBL-ORD-005-01', 'inbound_completed', 'B2-001', 2, datetime('now', '-1 day')),
(1, 6, 'LBL-ORD-006-01', 'created', NULL, 0, NULL);

-- 11. 스캔 이벤트 데이터
INSERT INTO scan_events (warehouse_id, user_id, label_code, scan_type, location_code, notes) VALUES
(1, 4, 'LBL-ORD-001-01', 'inbound', 'A1-001', '정상 입고 처리'),
(1, 4, 'LBL-ORD-002-01', 'inbound', 'A1-002', '정상 입고 처리'),
(1, 4, 'LBL-ORD-002-01', 'outbound_ready', 'A1-002', '출고 준비 완료'),
(1, 4, 'LBL-ORD-003-01', 'inbound', 'A1-003', '정상 입고 처리'),
(1, 4, 'LBL-ORD-003-01', 'outbound', 'SHIP', '배송 완료 출고'),
(2, 4, 'LBL-ORD-004-01', 'inbound', 'B1-001', 'FCL 컨테이너 입고'),
(2, 4, 'LBL-ORD-004-01', 'outbound', 'SHIP', 'FCL 배송 완료'),
(1, 4, 'LBL-ORD-005-01', 'inbound', 'B2-001', '항공화물 입고');

-- 12. 견적 데이터
INSERT INTO estimates (order_id, user_id, estimate_type, shipping_cost, customs_fee, handling_fee, insurance_fee, total_amount, currency, valid_until, status) VALUES
-- 1차 견적들
(1, 1, 'first', 65000.00, 8500.00, 7500.00, 4000.00, 85000.00, 'KRW', datetime('now', '+7 days'), 'approved'),
(2, 1, 'first', 89000.00, 14800.00, 10200.00, 4000.00, 118000.00, 'KRW', datetime('now', '+7 days'), 'approved'),
(3, 1, 'first', 119000.00, 14900.00, 12100.00, 3000.00, 149000.00, 'KRW', datetime('now', '+7 days'), 'approved'),
(4, 2, 'first', 650000.00, 89200.00, 82800.00, 10000.00, 832000.00, 'KRW', datetime('now', '+7 days'), 'approved'),
(5, 2, 'first', 421000.00, 52000.00, 42000.00, 5000.00, 520000.00, 'KRW', datetime('now', '+7 days'), 'pending'),
(6, 2, 'first', 195000.00, 24500.00, 22500.00, 3000.00, 245000.00, 'KRW', datetime('now', '+7 days'), 'pending'),

-- 최종 견적들 (결제완료 주문들)
(2, 1, 'final', 89000.00, 14800.00, 10200.00, 4000.00, 118000.00, 'KRW', datetime('now', '+30 days'), 'approved'),
(3, 1, 'final', 119000.00, 14900.00, 12100.00, 3000.00, 149000.00, 'KRW', datetime('now', '+30 days'), 'approved'),
(4, 2, 'final', 650000.00, 89200.00, 82800.00, 10000.00, 832000.00, 'KRW', datetime('now', '+30 days'), 'approved');

-- 13. 결제 데이터
INSERT INTO payments (order_id, user_id, amount, currency, payment_method, payment_status, transaction_id, paid_at) VALUES
(2, 1, 118000.00, 'KRW', 'card', 'completed', 'TXN-20240814-001', datetime('now', '-1 day')),
(3, 1, 149000.00, 'KRW', 'bank_transfer', 'completed', 'TXN-20240813-002', datetime('now', '-2 days')),
(4, 2, 832000.00, 'KRW', 'bank_transfer', 'completed', 'TXN-20240812-003', datetime('now', '-3 days'));

-- 14. 배송 추적 데이터
INSERT INTO shipment_tracking (order_id, tracking_number, carrier, status, current_location, estimated_delivery) VALUES
(2, 'AIR-2024-002-KR', 'Thai Airways Cargo', 'in_transit', '방콕 수완나품 공항', datetime('now', '+3 days')),
(3, 'SEA-2024-003-TH', 'YCS Logistics', 'delivered', '방콕 배송센터', datetime('now', '-1 day')),
(4, 'FCL-2024-004-BK', 'Ocean Network Express', 'delivered', '방콕 항구', datetime('now', '-1 day'));

-- 15. 파트너 추천 데이터
INSERT INTO partner_referrals (partner_id, referred_user_id, referral_code, commission_rate, order_id, commission_amount, status) VALUES
(3, 1, 'REF-PAR001', 0.0500, 2, 5900.00, 'paid'),
(3, 1, 'REF-PAR001', 0.0500, 3, 7450.00, 'paid'),
(3, 2, 'REF-PAR001', 0.0500, 4, 41600.00, 'pending');

-- 16. 시스템 설정
INSERT INTO config (key_name, value, description) VALUES
('cbm_threshold', '29.0', 'CBM 임계값 - 초과시 자동 항공전환'),
('thb_amount_threshold', '1500.0', 'THB 금액 임계값 - 초과시 수취인 추가정보 필요'),
('approval_processing_days', '2', '승인 처리 영업일'),
('usd_krw_rate', '1320.50', 'USD/KRW 환율'),
('thb_krw_rate', '38.75', 'THB/KRW 환율'),
('system_maintenance_mode', 'false', '시스템 점검 모드'),
('max_file_upload_size', '10485760', '최대 파일 업로드 크기 (10MB)'),
('session_timeout_minutes', '30', '세션 타임아웃 (분)');

-- 17. 감사 로그 데이터
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, old_values, new_values, ip_address, user_agent) VALUES
(5, 'USER_LOGIN', 'users', 5, NULL, '{"login_time": "2025-08-14T15:00:00"}', '192.168.1.100', 'Mozilla/5.0 Windows Browser'),
(4, 'SCAN_EVENT', 'inventory', 2, '{"status": "inbound_completed"}', '{"status": "ready_for_outbound"}', '192.168.1.101', 'Scanner App v1.0'),
(1, 'ORDER_CREATE', 'orders', 6, NULL, '{"order_number": "ORD-2024-006", "status": "pending"}', '192.168.1.102', 'Mozilla/5.0 Windows Browser'),
(2, 'PAYMENT_COMPLETE', 'payments', 3, '{"status": "pending"}', '{"status": "completed"}', '192.168.1.103', 'Mobile App v2.1'),
(5, 'USER_APPROVE', 'users', 2, '{"status": "pending"}', '{"status": "approved"}', '192.168.1.100', 'Admin Portal v1.0');