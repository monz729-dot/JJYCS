-- H2 Database Initial Data for YCS LMS
-- This file will be loaded automatically when the application starts
-- All test accounts use password: "password123"

-- Insert test users (password for all: "password123")
INSERT INTO users (email, password_hash, name, phone, role, status, member_code, email_verified, agree_terms, agree_privacy, created_at, updated_at) VALUES
-- Admin accounts
('admin@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'System Admin', '010-1234-5678', 'admin', 'active', 'ADM001', true, true, true, NOW(), NOW()),
('admin2@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin Manager', '010-1234-5679', 'admin', 'active', 'ADM002', true, true, true, NOW(), NOW()),

-- Warehouse accounts
('warehouse@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Warehouse Manager', '010-2345-6789', 'warehouse', 'active', 'WH001', true, true, true, NOW(), NOW()),
('warehouse2@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Warehouse Staff A', '010-2345-6790', 'warehouse', 'active', 'WH002', true, true, true, NOW(), NOW()),

-- Individual users
('user1@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '김철수', '010-1111-2222', 'individual', 'active', 'USR001', true, true, true, NOW(), NOW()),
('user2@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '이영희', '010-1111-2223', 'individual', 'active', 'USR002', true, true, true, NOW(), NOW()),

-- Enterprise users
('corp@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '홍길동', '010-3333-4444', 'enterprise', 'active', 'ENT001', true, true, true, NOW(), NOW()),

-- Partner accounts
('partner@partner.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '박영희', '010-5555-6666', 'partner', 'active', 'PTR001', true, true, true, NOW(), NOW()),

-- Users without member codes (for testing "No code" rule)
('nocode@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '코드없는사용자', '010-9999-0000', 'individual', 'active', null, true, true, true, NOW(), NOW());

-- Insert basic test orders
INSERT INTO orders (order_code, user_id, status, order_type, recipient_name, recipient_phone, recipient_address, recipient_country, total_amount, currency, total_cbm_m3, requires_extra_recipient, created_at, updated_at) VALUES
('YCS-2024-00001', 6, 'requested', 'sea', '방콕수취인', '+66-812345678', '123 Bangkok Street, Bangkok', 'THA', 850.00, 'THB', 0.003000, false, NOW(), NOW()),
('YCS-2024-00002', 6, 'confirmed', 'air', '파타야수취인', '+66-823456789', '456 Pattaya Road, Pattaya', 'THA', 2250.50, 'THB', 0.012240, true, NOW(), NOW());

-- Insert basic test order items
INSERT INTO order_items (order_id, product_name, quantity, unit_price, amount, currency, ems_code, hs_code, weight_kg, created_at, updated_at) VALUES
(1, '삼성 갤럭시 스마트폰', 1, 850.00, 850.00, 'THB', 'EMS001', '8517.12', 0.200, NOW(), NOW()),
(2, '애플 아이폰', 1, 1200.50, 1200.50, 'THB', 'EMS002', '8517.12', 0.240, NOW(), NOW()),
(2, '무선 이어폰', 1, 1050.00, 1050.00, 'THB', 'EMS003', '8518.30', 0.150, NOW(), NOW());

-- Insert basic test order boxes
INSERT INTO order_boxes (order_id, box_number, width_cm, height_cm, depth_cm, cbm_m3, weight_kg, status, label_code, created_at, updated_at) VALUES
(1, 1, 25.0, 15.0, 8.0, 0.003000, 0.250, 'registered', 'BOX-2024-00001-01', NOW(), NOW()),
(2, 1, 30.0, 20.0, 12.0, 0.007200, 0.450, 'registered', 'BOX-2024-00002-01', NOW(), NOW()),
(2, 2, 28.0, 18.0, 10.0, 0.005040, 0.200, 'registered', 'BOX-2024-00002-02', NOW(), NOW());

-- Insert basic warehouses
INSERT INTO warehouses (warehouse_code, name, location, contact_phone, status, created_at, updated_at) VALUES
('WH001', 'Bangkok Main Warehouse', 'Bangkok', '+66-800-111-222', 'active', NOW(), NOW()),
('WH002', 'Pattaya Distribution Center', 'Pattaya', '+66-800-333-444', 'active', NOW(), NOW());

-- Insert basic inventory
INSERT INTO inventory (order_id, warehouse_id, status, received_at, created_at, updated_at) VALUES
(1, 1, 'inbound', NOW(), NOW(), NOW()),
(2, 1, 'stored', NOW(), NOW(), NOW());

-- Print summary
SELECT 'Database initialized successfully!' as status;
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as order_count FROM orders;
SELECT COUNT(*) as warehouse_count FROM warehouses;