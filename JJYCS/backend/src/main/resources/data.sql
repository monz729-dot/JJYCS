-- Sample User Data (English names for encoding compatibility)
INSERT INTO users (email, password, name, phone, user_type, status, member_code, company_name, business_number, email_verified, created_at, updated_at) VALUES
('admin@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Administrator', '02-1234-5678', 'ADMIN', 'ACTIVE', 'ADM001', null, null, true, NOW(), NOW()),
('kimcs@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Kim Cheolsu', '010-1234-5678', 'GENERAL', 'ACTIVE', 'GEN001', null, null, true, NOW(), NOW()),
('lee@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Lee Younghee', '010-2345-6789', 'CORPORATE', 'ACTIVE', 'COR001', 'ABC Trading Co.', '123-45-67890', true, NOW(), NOW()),
('park@partner.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Park Minsu', '010-3456-7890', 'PARTNER', 'ACTIVE', 'PAR001', null, null, true, NOW(), NOW());

-- Bank Account Information
INSERT INTO bank_accounts (bank_name, account_number, account_holder, is_active, is_default, display_order, description, created_at, updated_at) VALUES
('KB Kookmin Bank', '123-456-789012', 'YCS Logistics Co.', true, true, 1, 'Main Account', NOW(), NOW()),
('Shinhan Bank', '110-123-456789', 'YCS Logistics Co.', true, false, 2, 'Sub Account', NOW(), NOW()),
('Woori Bank', '1002-123-456789', 'YCS Logistics Co.', true, false, 3, 'Backup Account', NOW(), NOW());

-- 샘플 주문 데이터 (핵심 비즈니스 로직 포함)
INSERT INTO orders (order_number, user_id, status, shipping_type, country, postal_code, recipient_name, recipient_phone, recipient_address, recipient_postal_code, tracking_number, total_cbm, total_weight, requires_extra_recipient, no_member_code, storage_location, storage_area, arrived_at, actual_weight, repacking_requested, repacking_completed, warehouse_notes, shipped_at, delivered_at, estimated_delivery, special_requests, created_at, updated_at) VALUES
('YCS-240115-001', 2, 'DELIVERED', 'SEA', 'thailand', '10110', 'Somchai Jaidee', '+66-81-234-5678', '123 Sukhumvit Road, Watthana District, Bangkok', '10110', 'EE123456789KR', 0.003900, 2.30, false, false, 'A-01-03', 'A line 1 row 3', '2024-01-18 10:30:00', 2.50, true, true, 'Good condition, divided into 3 packages of 1kg each', '2024-01-22 09:00:00', '2024-01-25 14:30:00', '2024-01-25 14:30:00', 'Fragile item, please pack carefully', '2024-01-15 14:20:00', NOW()),
('YCS-240120-002', 3, 'IN_TRANSIT', 'AIR', 'thailand', '10500', 'Niran Patel', '+66-82-345-6789', '456 Silom Road, Bang Rak District, Bangkok', '10500', 'EE234567890KR', 0.028500, 3.20, false, false, 'B-02-15', 'B line 2 row 15', '2024-01-23 11:15:00', 3.50, false, false, 'Package condition good', '2024-01-28 08:30:00', null, '2024-08-28 16:45:00', null, '2024-01-20 09:45:00', NOW()),
('YCS-240125-003', 2, 'PROCESSING', 'SEA', 'thailand', '10400', 'Araya Wongsak', '+66-83-456-7890', '789 Phetchaburi Road, Ratchathewi District, Bangkok', '10400', 'EE345678901KR', 0.012500, 1.80, false, false, 'A-03-07', 'A line 3 row 7', '2024-01-28 13:20:00', 1.95, true, false, 'Repacking in progress', null, null, '2024-08-30 13:20:00', 'Handle with care', '2024-01-25 16:30:00', NOW()),
('YCS-240201-004', 2, 'PENDING', 'SEA', 'thailand', '10200', 'Thirawat Somjai', '+66-84-567-8901', '321 Ratchadaphisek Road, Huai Khwang District, Bangkok', '10200', null, 0.005400, 1.20, false, false, null, null, null, null, false, false, null, null, null, '2024-09-05 10:15:00', 'Separate packaging material requested', '2024-02-01 10:15:00', NOW()),
('YCS-240203-005', 3, 'SHIPPED', 'AIR', 'thailand', '10600', 'Kanya Phuket', '+66-85-678-9012', '654 Phahonyothin Road, Chatuchak District, Bangkok', '10600', 'EE456789012KR', 0.018000, 2.80, true, false, 'C-01-12', 'C line 1 row 12', '2024-02-05 14:30:00', 2.95, false, false, 'Product inspection completed', '2024-02-10 08:00:00', null, '2024-08-27 14:30:00', 'THB 1500 exceeded, additional recipient info required', '2024-02-03 11:20:00', NOW()),
-- Test orders for tracking demo
('YCS202401001', 2, 'IN_TRANSIT', 'SEA', 'thailand', '10110', 'Hong Gildong', '+66-81-111-2222', 'Seoul Gangnam-gu Teheran-ro 123', '10110', 'KR1234567890', 0.015000, 3.50, false, false, 'A-02-05', 'A line 2 row 5', '2024-08-20 10:00:00', 3.70, false, false, 'Korean logistics center shipping start', '2024-08-22 09:00:00', null, '2024-08-28 18:00:00', null, '2024-01-15 11:20:00', NOW()),
('YCS202401002', 2, 'DELIVERED', 'AIR', 'thailand', '48000', 'Kim Younghee', '+66-82-222-3333', 'Busan Haeundae-gu Haeundae-ro 456', '48000', 'KR1234567891', 0.008500, 2.20, false, false, 'B-01-08', 'B line 1 row 8', '2024-08-15 14:00:00', 2.35, false, false, 'Delivery completed', '2024-08-18 08:30:00', '2024-08-20 14:20:00', '2024-08-20 14:20:00', null, '2024-01-10 15:30:00', NOW()),
('YCS-240205-006', 2, 'SHIPPING', 'SEA', 'thailand', '10300', 'Manee Sarawat', '+66-86-789-0123', '987 Lat Phrao Road, Wang Thonglang District, Bangkok', '10300', 'EE567890123KR', 0.009600, 1.60, false, true, 'A-05-09', 'A line 5 row 9', '2024-02-08 09:45:00', 1.75, true, true, 'Delayed processing due to missing member code', '2024-02-12 10:00:00', null, '2024-08-29 10:00:00', 'Request to add member code YCS001', '2024-02-05 13:40:00', NOW()),
('YCS-240208-007', 3, 'PAYMENT_PENDING', 'AIR', 'thailand', '10900', 'Prasert Bangkok', '+66-87-890-1234', '147 Ramkhamhaeng Road, Saphan Phut District, Bangkok', '10900', 'EE678901234KR', 0.032000, 4.50, false, false, 'B-03-21', 'B line 3 row 21', '2024-02-10 16:20:00', 4.80, false, false, 'Additional fee due to air conversion', '2024-02-14 08:15:00', '2024-02-16 12:30:00', '2024-02-16 12:30:00', 'Automatically converted to air due to CBM 29 exceeded', '2024-02-08 15:50:00', NOW()),
-- Additional test cases for various scenarios
('CBM-EXCEED-001', 2, 'PROCESSING', 'AIR', 'thailand', '10110', 'Test User CBM', '+66-88-111-2222', 'Bangkok Test Address 1', '10110', null, 35.500000, 150.00, false, false, 'A-10-01', 'A line 10 row 1', '2024-08-25 10:00:00', 155.00, false, false, 'CBM exceeds 29m³ - auto converted to AIR shipping', null, null, '2024-08-30 15:00:00', 'CBM test case - large volume shipment', '2024-08-25 09:00:00', NOW()),
('THB-EXCEED-001', 2, 'PENDING', 'SEA', 'thailand', '10200', 'Expensive Item User', '+66-88-222-3333', 'Bangkok Expensive Test', '10200', null, 0.008500, 2.20, true, false, null, null, null, null, false, false, null, null, null, '2024-08-30 12:00:00', 'THB 1500+ value requires extra recipient info', '2024-08-25 10:30:00', NOW()),
('NO-CODE-001', 2, 'DELAYED', 'SEA', 'thailand', '10300', 'Missing Code User', '+66-88-333-4444', 'Bangkok No Code Test', '10300', null, 0.012000, 3.50, false, true, 'B-05-15', 'B line 5 row 15', '2024-08-23 08:00:00', 3.75, false, false, 'Processing delayed due to missing member code', null, null, '2024-09-01 08:00:00', 'No member code test case', '2024-08-22 14:00:00', NOW()),
('MULTI-ISSUE-001', 3, 'IN_TRANSIT', 'AIR', 'thailand', '10400', 'Complex Case User', '+66-88-444-5555', 'Bangkok Complex Test', '10400', 'EE999888777KR', 31.200000, 85.50, true, true, 'C-08-22', 'C line 8 row 22', '2024-08-20 09:30:00', 87.80, true, false, 'Multiple issues: CBM exceeded, THB exceeded, No member code', '2024-08-24 11:00:00', null, '2024-08-29 16:30:00', 'Complex test case with multiple business rule violations', '2024-08-19 16:45:00', NOW()),
('NORMAL-CASE-001', 2, 'SHIPPED', 'SEA', 'thailand', '10500', 'Normal User', '+66-88-555-6666', 'Bangkok Normal Test', '10500', 'EE111222333KR', 0.015500, 5.20, false, false, 'A-12-08', 'A line 12 row 8', '2024-08-22 14:20:00', 5.45, false, false, 'Standard shipping case - no issues', '2024-08-25 10:15:00', null, '2024-08-28 12:00:00', 'Normal test case - all requirements met', '2024-08-21 11:15:00', NOW()),
('CANCELLED-001', 2, 'CANCELLED', 'SEA', 'thailand', '10600', 'Cancelled User', '+66-88-666-7777', 'Bangkok Cancelled Test', '10600', null, 0.009200, 2.10, false, false, null, null, null, null, false, false, null, null, null, null, 'Cancelled by customer request', '2024-08-20 13:45:00', NOW()),
('REPACK-CASE-001', 3, 'ARRIVED', 'SEA', 'thailand', '10700', 'Repack Test User', '+66-88-777-8888', 'Bangkok Repack Test', '10700', 'EE444555666KR', 0.022000, 12.30, false, false, 'D-03-11', 'D line 3 row 11', '2024-08-24 16:45:00', 12.80, true, false, 'Repacking requested - fragile items', null, null, '2024-08-29 14:30:00', 'Repacking test case - customer requested repackaging', '2024-08-23 09:20:00', NOW()),
('CORPORATE-001', 3, 'BILLING', 'AIR', 'thailand', '10800', 'Corporate Test Client', '+66-88-888-9999', 'Bangkok Corporate Test', '10800', 'EE777888999KR', 0.045000, 25.60, false, false, 'E-01-05', 'E line 1 row 5', '2024-08-21 11:30:00', 26.20, false, false, 'Corporate account - priority processing', '2024-08-23 08:45:00', '2024-08-25 15:20:00', '2024-08-25 15:20:00', 'Corporate customer test case', '2024-08-20 15:10:00', NOW());

-- Order Items Data
INSERT INTO order_items (order_id, hs_code, description, quantity, weight, width, height, depth, cbm, unit_price, total_price, created_at) VALUES
(1, '1905.31', 'Korean Chocolate Sticks', 10, 1.50, 30.00, 20.00, 5.00, 0.003000, 25.00, 250.00, NOW()),
(1, '1806.32', 'Chocolate Snacks', 5, 0.80, 20.00, 15.00, 3.00, 0.000900, 15.00, 75.00, NOW()),
(2, '3304.10', 'Lipstick Set', 20, 2.00, 25.00, 30.00, 20.00, 0.015000, 80.00, 1600.00, NOW()),
(2, '3304.20', 'Foundation', 15, 1.20, 20.00, 25.00, 15.00, 0.007500, 120.00, 1800.00, NOW()),
(3, '0902.30', 'Korean Traditional Tea Set', 8, 1.80, 25.00, 25.00, 20.00, 0.012500, 95.00, 760.00, NOW()),
(4, '6204.42', 'Women Clothing Set', 12, 1.20, 30.00, 20.00, 9.00, 0.005400, 45.00, 540.00, NOW()),
(5, '3401.11', 'Toiletries Set', 15, 2.80, 30.00, 40.00, 15.00, 0.018000, 85.00, 1275.00, NOW()),
(6, '0902.10', 'Green Tea Gift Set', 8, 1.60, 20.00, 30.00, 16.00, 0.009600, 75.00, 600.00, NOW()),
(7, '8517.12', 'Smartphone Cases', 50, 4.50, 40.00, 50.00, 16.00, 0.032000, 25.00, 1250.00, NOW()),
-- Test case order items
(8, '9401.80', 'Large Furniture Set', 5, 150.00, 200.00, 150.00, 120.00, 3.600000, 500.00, 2500.00, NOW()),
(8, '9403.70', 'Office Desk', 3, 80.00, 180.00, 90.00, 80.00, 1.296000, 800.00, 2400.00, NOW()),
(8, '9404.90', 'Bed Mattress', 2, 45.00, 200.00, 180.00, 25.00, 0.900000, 1200.00, 2400.00, NOW()),
(9, '7113.19', 'Luxury Jewelry Set', 1, 0.50, 20.00, 15.00, 5.00, 0.001500, 2500.00, 2500.00, NOW()),
(9, '7116.20', 'Precious Gemstones', 3, 0.30, 10.00, 10.00, 5.00, 0.000150, 1800.00, 5400.00, NOW()),
(10, '6204.62', 'Fashion Clothing', 20, 3.50, 30.00, 40.00, 10.00, 0.012000, 45.00, 900.00, NOW()),
(11, '9401.61', 'Luxury Sofa Set', 2, 85.50, 250.00, 120.00, 90.00, 2.700000, 3500.00, 7000.00, NOW()),
(11, '7113.11', 'Gold Jewelry', 5, 2.50, 15.00, 10.00, 8.00, 0.001200, 2200.00, 11000.00, NOW()),
(12, '6403.99', 'Leather Shoes', 10, 5.20, 35.00, 25.00, 18.00, 0.015750, 85.00, 850.00, NOW()),
(13, '9999.99', 'Cancelled Item', 1, 2.10, 20.00, 15.00, 10.00, 0.003000, 100.00, 100.00, NOW()),
(14, '7013.49', 'Fragile Glassware', 25, 12.30, 40.00, 30.00, 20.00, 0.024000, 120.00, 3000.00, NOW()),
(15, '8471.30', 'Computer Equipment', 8, 25.60, 50.00, 40.00, 30.00, 0.060000, 1500.00, 12000.00, NOW());

-- Billing Data
INSERT INTO billing (order_id, proforma_issued, proforma_date, final_issued, final_date, shipping_fee, local_delivery_fee, repacking_fee, handling_fee, insurance_fee, customs_fee, tax, total, payment_method, payment_status, depositor_name, payment_date, created_at, updated_at) VALUES
(1, true, '2024-01-26 10:00:00', false, null, 85000.00, 25000.00, 15000.00, 10000.00, 2000.00, 5000.00, 9975.00, 151975.00, 'BANK_TRANSFER', 'COMPLETED', 'Kim Cheolsu', '2024-01-27 15:30:00', NOW(), NOW()),
(2, true, '2024-01-31 11:30:00', false, null, 120000.00, 30000.00, 0.00, 12000.00, 3000.00, 6000.00, 11970.00, 182970.00, 'BANK_TRANSFER', 'PENDING', 'Lee Younghee', null, NOW(), NOW()),
(3, false, null, false, null, 0.00, 0.00, 8000.00, 8000.00, 1000.00, 3000.00, 0.00, 0.00, 'BANK_TRANSFER', 'PENDING', null, null, NOW(), NOW()),
(4, false, null, false, null, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'BANK_TRANSFER', 'PENDING', null, null, NOW(), NOW()),
(5, false, null, false, null, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'BANK_TRANSFER', 'PENDING', null, null, NOW(), NOW()),
(6, true, '2024-02-13 14:20:00', false, null, 95000.00, 28000.00, 12000.00, 9500.00, 1500.00, 4500.00, 10535.00, 160535.00, 'BANK_TRANSFER', 'PENDING', null, null, NOW(), NOW()),
(7, true, '2024-02-17 09:30:00', true, '2024-02-18 15:45:00', 145000.00, 35000.00, 0.00, 14500.00, 2500.00, 7500.00, 14315.00, 218815.00, 'BANK_TRANSFER', 'PENDING', 'Lee Younghee', null, NOW(), NOW());

-- Additional User Data
INSERT INTO users (email, password, name, phone, user_type, status, member_code, company_name, business_number, email_verified, referred_by, created_at, updated_at) VALUES
('user1@test.com', '$2a$10$ZlxXEPJsSVH2eYlusenHjOlcklUUMf/W24peb7CTXYEmK023O.ibe', 'Hong Gildong', '010-1111-2222', 'GENERAL', 'ACTIVE', 'GEN002', null, null, true, 4, NOW(), NOW()),
('user2@test.com', '$2a$10$ZlxXEPJsSVH2eYlusenHjOlcklUUMf/W24peb7CTXYEmK023O.ibe', 'Kim Youngsu', '010-3333-4444', 'GENERAL', 'ACTIVE', 'GEN003', null, null, true, 4, NOW(), NOW()),
('corp1@company.com', '$2a$10$ZlxXEPJsSVH2eYlusenHjOlcklUUMf/W24peb7CTXYEmK023O.ibe', 'Choi CEO', '02-1111-2222', 'CORPORATE', 'PENDING', null, 'Global Trade Co.', '567-89-12345', false, 9, NOW(), NOW()),
('partner2@partner.com', '$2a$10$ZlxXEPJsSVH2eYlusenHjOlcklUUMf/W24peb7CTXYEmK023O.ibe', 'Jung Partner', '010-5555-6666', 'PARTNER', 'ACTIVE', 'PAR002', null, null, true, null, NOW(), NOW());

-- 비밀번호는 모두 'password123'으로 bcrypt 해싱됨 ($2a$10$ZlxXEPJsSVH2eYlusenHjOlcklUUMf/W24peb7CTXYEmK023O.ibe)