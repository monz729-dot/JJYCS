-- YCS LMS Development Data (H2 Compatible)
-- 개발 환경용 초기 데이터

-- 기본 창고 생성
INSERT INTO warehouses (name, code, address, contact_info, capacity_description, operating_hours, is_active, created_at, updated_at) VALUES
('서울 메인 창고', 'SEL001', '서울특별시 강남구 테헤란로 123', '{"phone": "02-1234-5678", "email": "seoul@ycs.com"}', '대형 창고 (1000㎡)', '평일 9:00-18:00', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('부산 지점 창고', 'BSN001', '부산광역시 해운대구 센텀로 456', '{"phone": "051-9876-5432", "email": "busan@ycs.com"}', '중형 창고 (500㎡)', '평일 9:00-17:00', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 개발용 사용자들 (비밀번호: password123)
INSERT INTO users (email, password, name, phone, role, status, email_verified, created_at, updated_at, member_code) VALUES
-- 일반 사용자
('user@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '김일반', '010-1234-5678', 'INDIVIDUAL', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'USR001'),
('user2@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '박개인', '010-1111-2222', 'INDIVIDUAL', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'USR002'),

-- 기업 사용자
('enterprise@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '이기업', '010-2345-6789', 'ENTERPRISE', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ENT001'),
('company2@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '최회사', '010-3333-4444', 'ENTERPRISE', 'PENDING_APPROVAL', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- 파트너 사용자
('partner@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '정파트너', '010-3456-7890', 'PARTNER', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'PTN001'),
('affiliate@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '한제휴', '010-5555-6666', 'PARTNER', 'PENDING_APPROVAL', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- 창고 사용자 (중간관리자)
('warehouse@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '김창고', '010-4567-8901', 'WAREHOUSE', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'WHS001'),
('warehouse2@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '이물류', '010-7777-8888', 'WAREHOUSE', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'WHS002'),

-- 관리자 (최고관리자)
('admin@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '최관리자', '010-5678-9012', 'ADMIN', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ADM001'),
('superadmin@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', '슈퍼관리자', '010-9999-0000', 'ADMIN', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ADM000');

-- 기업 프로필
INSERT INTO enterprise_profiles (user_id, company_name, business_number, business_address, business_type, employee_count, annual_revenue, approval_status, created_at, updated_at) VALUES
(3, '(주)테스트기업', '123-45-67890', '서울특별시 강남구 역삼로 789', 'TECHNOLOGY', '50-100', 1000000000, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '(주)승인대기회사', '987-65-43210', '서울특별시 서초구 강남대로 321', 'TRADING', '10-50', 500000000, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 파트너 프로필  
INSERT INTO partner_profiles (user_id, partner_type, referral_code, commission_rate, settlement_cycle, bank_info, approval_status, created_at, updated_at) VALUES
(5, 'AFFILIATE', 'PTN001REF', 5.50, 'MONTHLY', '{"bank": "KB국민은행", "account": "123-456-789012", "holder": "정파트너"}', 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'REFERRAL', 'AFF001REF', 3.00, 'MONTHLY', '{"bank": "신한은행", "account": "987-654-321098", "holder": "한제휴"}', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 창고 프로필
INSERT INTO warehouse_profiles (user_id, warehouse_id, position, department, shift_schedule, permissions, hire_date, created_at, updated_at) VALUES
(7, 1, 'Warehouse Manager', 'Operations', '{"shift": "day", "hours": "09:00-18:00"}', '["SCAN", "BATCH_PROCESS", "INVENTORY_MANAGEMENT", "LABEL_GENERATION"]', '2023-01-15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 2, 'Logistics Coordinator', 'Operations', '{"shift": "day", "hours": "09:00-17:00"}', '["SCAN", "BATCH_PROCESS", "INVENTORY_VIEW"]', '2023-06-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 샘플 주문들
INSERT INTO orders (user_id, order_code, recipient_name, recipient_phone, recipient_address, recipient_country, order_type, status, total_amount, currency, requires_extra_recipient_info, created_at, updated_at) VALUES
(1, 'ORD-2024-001', '홍길동', '+66-81-234-5678', '123 Sukhumvit Road, Bangkok, Thailand', 'TH', 'SEA', 'PENDING', 850.00, 'THB', FALSE, DATEADD('DAY', -2, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
(1, 'ORD-2024-002', '김영희', '+84-90-876-5432', '456 Nguyen Hue Street, Ho Chi Minh City, Vietnam', 'VN', 'AIR', 'APPROVED', 2300.00, 'THB', TRUE, DATEADD('DAY', -1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
(3, 'ORD-2024-003', 'John Smith', '+1-555-123-4567', '123 Business Ave, New York, USA', 'US', 'AIR', 'DELIVERED', 15000.00, 'THB', TRUE, DATEADD('DAY', -10, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

-- 주문 아이템들
INSERT INTO order_items (order_id, name, description, quantity, unit_price, currency, weight_kg, ems_code, hs_code, created_at, updated_at) VALUES
(1, '한국 라면 세트', '신라면, 짜파게티 등 10개 묶음', 3, 180.00, 'THB', 2.5, 'EMS001', '190230', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '김치', '배추김치 500g', 2, 145.00, 'THB', 1.0, 'EMS002', '200410', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '삼성 갤럭시 스마트폰', 'Galaxy S24 Ultra 256GB', 1, 1800.00, 'THB', 0.3, 'EMS003', '851712', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '무선 이어버드', 'Galaxy Buds Pro', 1, 500.00, 'THB', 0.1, 'EMS004', '851830', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '산업용 부품', 'Precision mechanical parts', 50, 300.00, 'THB', 25.0, 'EMS006', '848390', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 주문 박스들
INSERT INTO order_boxes (order_id, width_cm, height_cm, depth_cm, weight_kg, label_code, status, warehouse_id, warehouse_location, created_at, updated_at) VALUES
(1, 30.0, 20.0, 15.0, 3.5, 'BOX-001-A', 'READY_FOR_OUTBOUND', 1, 'A-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 25.0, 15.0, 10.0, 0.4, 'BOX-002-A', 'INBOUND_COMPLETED', 1, 'B-02-03', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 80.0, 60.0, 50.0, 25.0, 'BOX-003-A', 'DELIVERED', 1, 'C-01-05', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 시스템 설정
INSERT INTO config (config_key, config_value, description, created_at, updated_at) VALUES
('CBM_THRESHOLD', '29.0', 'CBM threshold for air shipping conversion (m³)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HIGH_VALUE_THRESHOLD_THB', '1500.0', 'High value item threshold in THB', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('EXCHANGE_RATE_USD_THB', '35.0', 'USD to THB exchange rate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('EXCHANGE_RATE_KRW_THB', '0.027', 'KRW to THB exchange rate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);