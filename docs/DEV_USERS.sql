-- YCS LMS 개발용 사용자 데이터
-- 개발 환경에서 테스트용으로 사용할 사용자들

-- 기본 창고 생성
INSERT INTO warehouses (name, code, address, contact_info, capacity_description, operating_hours, is_active, created_at, updated_at) VALUES
('서울 메인 창고', 'SEL001', '서울특별시 강남구 테헤란로 123', '{"phone": "02-1234-5678", "email": "seoul@ycs.com"}', '대형 창고 (1000㎡)', '평일 9:00-18:00', TRUE, NOW(), NOW()),
('부산 지점 창고', 'BSN001', '부산광역시 해운대구 센텀로 456', '{"phone": "051-9876-5432", "email": "busan@ycs.com"}', '중형 창고 (500㎡)', '평일 9:00-17:00', TRUE, NOW(), NOW());

-- 1. 일반 사용자 (Individual User)
INSERT INTO users (email, password, name, phone, role, status, email_verified, created_at, updated_at, member_code) VALUES
('user@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '김일반', '010-1234-5678', 'individual', 'active', TRUE, NOW(), NOW(), 'USR001'),
('user2@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '박개인', '010-1111-2222', 'individual', 'active', TRUE, NOW(), NOW(), 'USR002');
-- 비밀번호: password123

-- 2. 기업 사용자 (Enterprise User) 
INSERT INTO users (email, password, name, phone, role, status, email_verified, created_at, updated_at, member_code) VALUES
('enterprise@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '이기업', '010-2345-6789', 'enterprise', 'active', TRUE, NOW(), NOW(), 'ENT001'),
('company2@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '최회사', '010-3333-4444', 'enterprise', 'pending_approval', TRUE, NOW(), NOW(), NULL);

-- 기업 프로필 생성
INSERT INTO enterprise_profiles (user_id, company_name, business_number, business_address, business_type, employee_count, annual_revenue, approval_status, created_at, updated_at) VALUES
(3, '(주)테스트기업', '123-45-67890', '서울특별시 강남구 역삼로 789', 'technology', '50-100', '1000000000', 'approved', NOW(), NOW()),
(4, '(주)승인대기회사', '987-65-43210', '서울특별시 서초구 강남대로 321', 'trading', '10-50', '500000000', 'pending', NOW(), NOW());

-- 3. 파트너 사용자 (Partner User)
INSERT INTO users (email, password, name, phone, role, status, email_verified, created_at, updated_at, member_code) VALUES
('partner@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '정파트너', '010-3456-7890', 'partner', 'active', TRUE, NOW(), NOW(), 'PTN001'),
('affiliate@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '한제휴', '010-5555-6666', 'partner', 'pending_approval', TRUE, NOW(), NOW(), NULL);

-- 파트너 프로필 생성
INSERT INTO partner_profiles (user_id, partner_type, referral_code, commission_rate, settlement_cycle, bank_info, approval_status, created_at, updated_at) VALUES
(5, 'affiliate', 'PTN001REF', 5.50, 'monthly', '{"bank": "KB국민은행", "account": "123-456-789012", "holder": "정파트너"}', 'approved', NOW(), NOW()),
(6, 'referral', 'AFF001REF', 3.00, 'monthly', '{"bank": "신한은행", "account": "987-654-321098", "holder": "한제휴"}', 'pending', NOW(), NOW());

-- 4. 창고 사용자 (Warehouse User) - 중간관리자급
INSERT INTO users (email, password, name, phone, role, status, email_verified, created_at, updated_at, member_code) VALUES
('warehouse@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '김창고', '010-4567-8901', 'warehouse', 'active', TRUE, NOW(), NOW(), 'WHS001'),
('warehouse2@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '이물류', '010-7777-8888', 'warehouse', 'active', TRUE, NOW(), NOW(), 'WHS002');

-- 창고 프로필 생성
INSERT INTO warehouse_profiles (user_id, warehouse_id, position, department, shift_schedule, permissions, hire_date, created_at, updated_at) VALUES
(7, 1, 'Warehouse Manager', 'Operations', '{"shift": "day", "hours": "09:00-18:00"}', '["scan", "batch_process", "inventory_management", "label_generation"]', '2023-01-15', NOW(), NOW()),
(8, 2, 'Logistics Coordinator', 'Operations', '{"shift": "day", "hours": "09:00-17:00"}', '["scan", "batch_process", "inventory_view"]', '2023-06-01', NOW(), NOW());

-- 5. 관리자 (Admin User) - 최고관리자
INSERT INTO users (email, password, name, phone, role, status, email_verified, created_at, updated_at, member_code) VALUES
('admin@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '최관리자', '010-5678-9012', 'admin', 'active', TRUE, NOW(), NOW(), 'ADM001'),
('superadmin@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '슈퍼관리자', '010-9999-0000', 'admin', 'active', TRUE, NOW(), NOW(), 'ADM000');

-- 샘플 주문 데이터 생성
INSERT INTO orders (user_id, order_code, recipient_name, recipient_phone, recipient_address, recipient_country, order_type, status, total_amount, currency, requires_extra_recipient_info, created_at, updated_at) VALUES
-- 일반 사용자 주문들
(1, 'ORD-2024-001', '홍길동', '+66-81-234-5678', '123 Sukhumvit Road, Bangkok, Thailand', 'TH', 'sea', 'pending', 850.00, 'THB', FALSE, NOW() - INTERVAL 2 DAY, NOW()),
(1, 'ORD-2024-002', '김영희', '+84-90-876-5432', '456 Nguyen Hue Street, Ho Chi Minh City, Vietnam', 'VN', 'air', 'approved', 2300.00, 'THB', TRUE, NOW() - INTERVAL 1 DAY, NOW()),
(2, 'ORD-2024-003', '이철수', '+86-138-0013-8000', '789 Nanjing Road, Shanghai, China', 'CN', 'sea', 'in_transit', 1200.00, 'THB', FALSE, NOW() - INTERVAL 5 DAY, NOW()),

-- 기업 사용자 주문들  
(3, 'ORD-2024-004', 'John Smith', '+1-555-123-4567', '123 Business Ave, New York, USA', 'US', 'air', 'delivered', 15000.00, 'THB', TRUE, NOW() - INTERVAL 10 DAY, NOW()),
(3, 'ORD-2024-005', 'Maria Garcia', '+34-600-123-456', 'Calle Gran Via 100, Madrid, Spain', 'ES', 'sea', 'pending', 8500.00, 'THB', TRUE, NOW() - INTERVAL 1 DAY, NOW()),

-- 파트너 사용자 주문들
(5, 'ORD-2024-006', 'Tanaka Hiroshi', '+81-90-1234-5678', '1-1-1 Shibuya, Tokyo, Japan', 'JP', 'air', 'approved', 3200.00, 'THB', TRUE, NOW() - INTERVAL 3 DAY, NOW());

-- 주문 아이템 생성
INSERT INTO order_items (order_id, name, description, quantity, unit_price, currency, weight_kg, ems_code, hs_code, created_at, updated_at) VALUES
-- ORD-2024-001의 아이템들
(1, '한국 라면 세트', '신라면, 짜파게티 등 10개 묶음', 3, 180.00, 'THB', 2.5, 'EMS001', '190230', NOW(), NOW()),
(1, '김치', '배추김치 500g', 2, 145.00, 'THB', 1.0, 'EMS002', '200410', NOW(), NOW()),

-- ORD-2024-002의 아이템들 (고가품 - THB 1500 초과)
(2, '삼성 갤럭시 스마트폰', 'Galaxy S24 Ultra 256GB', 1, 1800.00, 'THB', 0.3, 'EMS003', '851712', NOW(), NOW()),
(2, '무선 이어버드', 'Galaxy Buds Pro', 1, 500.00, 'THB', 0.1, 'EMS004', '851830', NOW(), NOW()),

-- ORD-2024-003의 아이템들
(3, '화장품 세트', 'K-뷰티 스킨케어 세트', 5, 240.00, 'THB', 1.2, 'EMS005', '330499', NOW(), NOW()),

-- 기업 주문들
(4, '산업용 부품', 'Precision mechanical parts', 50, 300.00, 'THB', 25.0, 'EMS006', '848390', NOW(), NOW()),
(5, '전자제품', 'Electronic components batch', 100, 85.00, 'THB', 15.0, 'EMS007', '854290', NOW(), NOW()),
(6, '의료기기', 'Medical diagnostic equipment', 2, 1600.00, 'THB', 8.0, 'EMS008', '901890', NOW(), NOW());

-- 주문 박스 생성 (CBM 계산 포함)
INSERT INTO order_boxes (order_id, width_cm, height_cm, depth_cm, weight_kg, label_code, status, warehouse_id, warehouse_location, created_at, updated_at) VALUES
-- 일반 주문들 (CBM < 29)
(1, 30.0, 20.0, 15.0, 3.5, 'BOX-001-A', 'ready_for_outbound', 1, 'A-01-01', NOW(), NOW()),
(2, 25.0, 15.0, 10.0, 0.4, 'BOX-002-A', 'inbound_completed', 1, 'B-02-03', NOW(), NOW()), -- CBM = 0.00375
(3, 40.0, 30.0, 20.0, 6.2, 'BOX-003-A', 'outbound_completed', 1, 'C-01-05', NOW(), NOW()), -- CBM = 0.024

-- 기업 주문들 (CBM 큰 박스들)
(4, 80.0, 60.0, 50.0, 25.0, 'BOX-004-A', 'delivered', 1, 'D-03-02', NOW(), NOW()), -- CBM = 0.24
(4, 70.0, 50.0, 40.0, 20.0, 'BOX-004-B', 'delivered', 1, 'D-03-03', NOW(), NOW()), -- CBM = 0.14
(5, 100.0, 80.0, 60.0, 15.0, 'BOX-005-A', 'inbound_completed', 1, 'E-01-01', NOW(), NOW()), -- CBM = 0.48
(6, 45.0, 35.0, 25.0, 8.0, 'BOX-006-A', 'ready_for_outbound', 1, 'F-02-04', NOW(), NOW()); -- CBM = 0.039

-- 대용량 박스 (CBM > 29 테스트용)
INSERT INTO orders (user_id, order_code, recipient_name, recipient_phone, recipient_address, recipient_country, order_type, status, total_amount, currency, requires_extra_recipient_info, created_at, updated_at) VALUES
(3, 'ORD-2024-007', 'Large Corp Warehouse', '+1-555-999-8888', '500 Industrial Blvd, Chicago, USA', 'US', 'air', 'pending', 25000.00, 'THB', TRUE, NOW(), NOW());

INSERT INTO order_items (order_id, name, description, quantity, unit_price, currency, weight_kg, ems_code, hs_code, created_at, updated_at) VALUES
(7, '대형 산업장비', 'Industrial machinery parts', 1, 25000.00, 'THB', 150.0, 'EMS009', '847989', NOW(), NOW());

-- CBM > 29인 박스 (자동 항공 전환 테스트)
INSERT INTO order_boxes (order_id, width_cm, height_cm, depth_cm, weight_kg, label_code, status, warehouse_id, warehouse_location, created_at, updated_at) VALUES
(7, 350.0, 250.0, 200.0, 150.0, 'BOX-007-A', 'pending', 1, 'G-01-01', NOW(), NOW()); -- CBM = 17.5 (개별적으로는 29 미만이지만 여러 개 합치면 초과하는 시나리오)

-- 추가 대형 박스들
INSERT INTO order_boxes (order_id, width_cm, height_cm, depth_cm, weight_kg, label_code, status, warehouse_id, warehouse_location, created_at, updated_at) VALUES
(7, 300.0, 200.0, 180.0, 80.0, 'BOX-007-B', 'pending', 1, 'G-01-02', NOW(), NOW()), -- CBM = 10.8
(7, 280.0, 220.0, 160.0, 70.0, 'BOX-007-C', 'pending', 1, 'G-01-03', NOW(), NOW()); -- 총 CBM = 17.5 + 10.8 + 9.856 = 38.156 > 29

-- 스캔 이벤트 이력 생성
INSERT INTO scan_events (event_type, box_id, warehouse_id, scanned_by, previous_status, new_status, location_code, device_info, scan_timestamp) VALUES
('INBOUND', 1, 1, 7, 'created', 'inbound_completed', 'A-01-01', 'Scanner-001', NOW() - INTERVAL 2 DAY),
('INVENTORY', 1, 1, 7, 'inbound_completed', 'ready_for_outbound', 'A-01-01', 'Scanner-001', NOW() - INTERVAL 1 DAY),
('INBOUND', 2, 1, 7, 'created', 'inbound_completed', 'B-02-03', 'Scanner-002', NOW() - INTERVAL 1 DAY),
('OUTBOUND', 3, 1, 8, 'ready_for_outbound', 'outbound_completed', 'C-01-05', 'Scanner-001', NOW() - INTERVAL 4 HOUR);

-- 재고 데이터 생성
INSERT INTO inventory (warehouse_id, box_id, location_code, status, inbound_date, expected_outbound_date, last_scanned_at) VALUES
(1, 1, 'A-01-01', 'stored', NOW() - INTERVAL 2 DAY, NOW() + INTERVAL 3 DAY, NOW() - INTERVAL 1 DAY),
(1, 2, 'B-02-03', 'stored', NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 5 DAY, NOW() - INTERVAL 1 DAY),
(1, 5, 'D-03-02', 'shipped', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(1, 6, 'D-03-03', 'shipped', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
(1, 7, 'E-01-01', 'stored', NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 4 DAY, NOW() - INTERVAL 1 DAY),
(1, 8, 'F-02-04', 'stored', NOW() - INTERVAL 3 DAY, NOW() + INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY);

-- 시스템 설정값들
INSERT INTO config (config_key, config_value, description, created_at, updated_at) VALUES
('cbm_threshold', '29.0', 'CBM threshold for air shipping conversion (m³)', NOW(), NOW()),
('high_value_threshold_thb', '1500.0', 'High value item threshold in THB requiring extra recipient info', NOW(), NOW()),
('exchange_rate_usd_thb', '35.0', 'USD to THB exchange rate', NOW(), NOW()),
('exchange_rate_krw_thb', '0.027', 'KRW to THB exchange rate', NOW(), NOW()),
('approval_processing_days', '1-2', 'Business days for enterprise/partner approval', NOW(), NOW()),
('warehouse_operating_hours', '09:00-18:00', 'Default warehouse operating hours', NOW(), NOW()),
('system_notification_email', 'system@ycs.com', 'System notification email', NOW(), NOW());

-- 감사 로그 샘플
INSERT INTO audit_logs (user_id, action, resource_type, resource_id, old_values, new_values, ip_address, user_agent, created_at) VALUES
(9, 'USER_LOGIN', 'User', 1, '{}', '{"last_login": "2024-01-15 10:30:00"}', '192.168.1.100', 'Mozilla/5.0...', NOW() - INTERVAL 2 HOUR),
(7, 'BOX_SCAN', 'OrderBox', 1, '{"status": "created"}', '{"status": "inbound_completed"}', '192.168.1.101', 'YCS-Scanner/1.0', NOW() - INTERVAL 2 DAY),
(9, 'USER_APPROVAL', 'User', 3, '{"status": "pending_approval"}', '{"status": "active"}', '192.168.1.102', 'Mozilla/5.0...', NOW() - INTERVAL 5 DAY);

-- 개발용 계정 정보 요약 출력
SELECT 
    '=== YCS LMS 개발용 계정 정보 ===' as info
UNION ALL SELECT ''
UNION ALL SELECT '1. 일반 사용자 (Individual):'
UNION ALL SELECT '   Email: user@ycs.com | Password: password123 | 회원코드: USR001'
UNION ALL SELECT '   Email: user2@ycs.com | Password: password123 | 회원코드: USR002'
UNION ALL SELECT ''
UNION ALL SELECT '2. 기업 사용자 (Enterprise):'
UNION ALL SELECT '   Email: enterprise@ycs.com | Password: password123 | 회원코드: ENT001 | 상태: 승인완료'
UNION ALL SELECT '   Email: company2@ycs.com | Password: password123 | 회원코드: 없음 | 상태: 승인대기'
UNION ALL SELECT ''
UNION ALL SELECT '3. 파트너 사용자 (Partner):'
UNION ALL SELECT '   Email: partner@ycs.com | Password: password123 | 회원코드: PTN001 | 상태: 승인완료'
UNION ALL SELECT '   Email: affiliate@ycs.com | Password: password123 | 회원코드: 없음 | 상태: 승인대기'
UNION ALL SELECT ''
UNION ALL SELECT '4. 창고 사용자 (Warehouse Manager - 중간관리자):'
UNION ALL SELECT '   Email: warehouse@ycs.com | Password: password123 | 회원코드: WHS001'
UNION ALL SELECT '   Email: warehouse2@ycs.com | Password: password123 | 회원코드: WHS002'
UNION ALL SELECT ''
UNION ALL SELECT '5. 관리자 (Admin - 최고관리자):'
UNION ALL SELECT '   Email: admin@ycs.com | Password: password123 | 회원코드: ADM001'
UNION ALL SELECT '   Email: superadmin@ycs.com | Password: password123 | 회원코드: ADM000'
UNION ALL SELECT ''
UNION ALL SELECT '=== 비즈니스 룰 테스트 데이터 ==='
UNION ALL SELECT '• CBM > 29 초과 주문: ORD-2024-007 (총 38.156m³ - 자동 항공 전환)'
UNION ALL SELECT '• THB 1,500 초과 주문: ORD-2024-002, ORD-2024-004, ORD-2024-005, ORD-2024-006'
UNION ALL SELECT '• 회원코드 없는 사용자: company2@ycs.com, affiliate@ycs.com'
UNION ALL SELECT '• 승인 대기 사용자: company2@ycs.com, affiliate@ycs.com';