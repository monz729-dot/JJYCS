-- Additional test data for better dashboard experience
-- More orders for each user

-- Orders for kimcs@email.com (user_id=2)
INSERT INTO orders (order_number, user_id, status, shipping_type, country, postal_code, recipient_name, recipient_phone, recipient_address, recipient_postal_code, tracking_number, total_cbm, total_weight, requires_extra_recipient, no_member_code, storage_location, storage_area, arrived_at, actual_weight, repacking_requested, repacking_completed, warehouse_notes, shipped_at, delivered_at, special_requests, created_at, updated_at) VALUES
('YCS-241220-001', 2, 'DELIVERED', 'SEA', 'thailand', '10110', 'Somchai Test', '+66-81-111-1111', '123 Test Road, Bangkok', '10110', 'EE111111111KR', 0.015000, 5.50, false, false, 'A-01-01', 'A', '2024-12-21 10:00:00', 5.70, false, false, 'Delivered successfully', '2024-12-22 09:00:00', '2024-12-25 14:00:00', null, '2024-12-20 09:00:00', NOW()),
('YCS-241222-002', 2, 'SHIPPING', 'AIR', 'thailand', '10200', 'Niran Test', '+66-82-222-2222', '456 Sample St, Bangkok', '10200', 'EE222222222KR', 0.008000, 3.20, false, false, 'B-02-01', 'B', '2024-12-23 11:00:00', 3.30, true, true, 'Ready for shipping', '2024-12-25 08:00:00', null, 'Express delivery', '2024-12-22 10:00:00', NOW()),
('YCS-241225-003', 2, 'ARRIVED', 'SEA', 'thailand', '10300', 'Araya Sample', '+66-83-333-3333', '789 Demo Avenue, Bangkok', '10300', null, 0.025000, 8.50, false, false, 'C-03-01', 'C', '2024-12-26 13:00:00', 8.70, false, false, 'Waiting for customs', null, null, null, '2024-12-25 15:00:00', NOW());

-- Orders for lee@company.com (user_id=3)
INSERT INTO orders (order_number, user_id, status, shipping_type, country, postal_code, recipient_name, recipient_phone, recipient_address, recipient_postal_code, tracking_number, total_cbm, total_weight, requires_extra_recipient, no_member_code, storage_location, storage_area, arrived_at, actual_weight, repacking_requested, repacking_completed, warehouse_notes, shipped_at, delivered_at, special_requests, created_at, updated_at) VALUES
('YCS-241221-004', 3, 'BILLING', 'AIR', 'thailand', '10400', 'Company Client 1', '+66-84-444-4444', '321 Business Center, Bangkok', '10400', 'EE444444444KR', 0.032000, 12.50, true, false, 'D-04-01', 'D', '2024-12-22 14:00:00', 12.80, false, false, 'Invoice issued', null, null, 'THB 1500+ warning', '2024-12-21 09:00:00', NOW()),
('YCS-241223-005', 3, 'DELIVERED', 'SEA', 'thailand', '10500', 'Company Client 2', '+66-85-555-5555', '654 Corporate Plaza, Bangkok', '10500', 'EE555555555KR', 0.045000, 15.20, false, false, 'E-05-01', 'E', '2024-12-24 10:00:00', 15.50, true, true, 'Successfully delivered', '2024-12-25 08:00:00', '2024-12-27 16:00:00', 'Bulk order', '2024-12-23 11:00:00', NOW()),
('YCS-241226-006', 3, 'RECEIVED', 'AIR', 'thailand', '10600', 'Company Client 3', '+66-86-666-6666', '987 Trade Tower, Bangkok', '10600', null, 0.018000, 6.80, false, false, null, null, null, null, false, false, null, null, null, 'Urgent delivery required', '2024-12-26 14:00:00', NOW());

-- More order items for new orders
INSERT INTO order_items (order_id, hs_code, description, quantity, weight, width, height, depth, cbm, unit_price, total_price, created_at) VALUES
(10, '6204.42', 'Winter Clothing Set', 20, 5.50, 40.00, 30.00, 25.00, 0.030000, 55.00, 1100.00, NOW()),
(11, '8517.12', 'Electronics Bundle', 15, 3.20, 25.00, 20.00, 16.00, 0.008000, 125.00, 1875.00, NOW()),
(12, '1905.31', 'Food Package', 30, 8.50, 35.00, 35.00, 20.00, 0.024500, 35.00, 1050.00, NOW()),
(13, '3304.10', 'Cosmetics Set', 25, 12.50, 45.00, 40.00, 18.00, 0.032400, 95.00, 2375.00, NOW()),
(14, '0902.30', 'Premium Tea Collection', 40, 15.20, 50.00, 45.00, 20.00, 0.045000, 85.00, 3400.00, NOW()),
(15, '6109.10', 'T-Shirt Bundle', 50, 6.80, 30.00, 25.00, 24.00, 0.018000, 25.00, 1250.00, NOW());

-- Billing data for new orders
INSERT INTO billing (order_id, proforma_issued, proforma_date, final_issued, final_date, shipping_fee, local_delivery_fee, repacking_fee, handling_fee, insurance_fee, customs_fee, tax, total, payment_method, payment_status, depositor_name, payment_date, created_at, updated_at) VALUES
(10, true, '2024-12-21 10:00:00', true, '2024-12-22 10:00:00', 85000.00, 25000.00, 0.00, 10000.00, 2000.00, 5000.00, 8890.00, 135890.00, 'BANK_TRANSFER', 'COMPLETED', 'Kim Cheolsu', '2024-12-23 15:00:00', NOW(), NOW()),
(11, true, '2024-12-24 11:00:00', false, null, 120000.00, 30000.00, 15000.00, 12000.00, 3000.00, 6000.00, 13020.00, 199020.00, 'BANK_TRANSFER', 'PENDING', null, null, NOW(), NOW()),
(12, false, null, false, null, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'BANK_TRANSFER', 'PENDING', null, null, NOW(), NOW()),
(13, true, '2024-12-23 14:00:00', false, null, 145000.00, 35000.00, 0.00, 14500.00, 3500.00, 7500.00, 14385.00, 219885.00, 'BANK_TRANSFER', 'PENDING', 'Lee Younghee', null, NOW(), NOW()),
(14, true, '2024-12-25 09:00:00', true, '2024-12-26 15:00:00', 95000.00, 28000.00, 12000.00, 9500.00, 2500.00, 5500.00, 10675.00, 163175.00, 'BANK_TRANSFER', 'COMPLETED', 'Lee Younghee', '2024-12-27 10:00:00', NOW(), NOW()),
(15, false, null, false, null, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 'BANK_TRANSFER', 'PENDING', null, null, NOW(), NOW());

-- Partner referral data
INSERT INTO users (email, password, name, phone, user_type, status, member_code, company_name, business_number, email_verified, referred_by, created_at, updated_at) VALUES
('ref1@test.com', '$2a$10$ZlxXEPJsSVH2eYlusenHjOlcklUUMf/W24peb7CTXYEmK023O.ibe', 'Referral User 1', '010-7777-8888', 'GENERAL', 'ACTIVE', 'GEN004', null, null, true, 4, NOW() - INTERVAL 30 DAY, NOW()),
('ref2@test.com', '$2a$10$ZlxXEPJsSVH2eYlusenHjOlcklUUMf/W24peb7CTXYEmK023O.ibe', 'Referral User 2', '010-8888-9999', 'GENERAL', 'ACTIVE', 'GEN005', null, null, true, 4, NOW() - INTERVAL 20 DAY, NOW()),
('ref3@company.com', '$2a$10$ZlxXEPJsSVH2eYlusenHjOlcklUUMf/W24peb7CTXYEmK023O.ibe', 'Referral Corp 1', '02-3333-4444', 'CORPORATE', 'ACTIVE', 'COR002', 'Referral Company Ltd.', '789-01-23456', true, 4, NOW() - INTERVAL 15 DAY, NOW());

-- Orders for partner's referrals
INSERT INTO orders (order_number, user_id, status, shipping_type, country, postal_code, recipient_name, recipient_phone, recipient_address, recipient_postal_code, tracking_number, total_cbm, total_weight, requires_extra_recipient, no_member_code, storage_location, storage_area, arrived_at, actual_weight, repacking_requested, repacking_completed, warehouse_notes, shipped_at, delivered_at, special_requests, created_at, updated_at) VALUES
('YCS-241210-007', 13, 'DELIVERED', 'SEA', 'thailand', '10110', 'Referral Client 1', '+66-87-777-7777', '111 Referral St, Bangkok', '10110', 'EE777777777KR', 0.012000, 4.50, false, false, 'F-01-01', 'F', '2024-12-11 10:00:00', 4.60, false, false, 'Partner referral order', '2024-12-12 09:00:00', '2024-12-15 14:00:00', null, '2024-12-10 09:00:00', NOW()),
('YCS-241215-008', 14, 'SHIPPING', 'AIR', 'thailand', '10200', 'Referral Client 2', '+66-88-888-8888', '222 Referral Ave, Bangkok', '10200', 'EE888888888KR', 0.009000, 3.20, false, false, 'G-02-01', 'G', '2024-12-16 11:00:00', 3.30, false, false, 'Partner referral order', '2024-12-18 08:00:00', null, null, '2024-12-15 10:00:00', NOW()),
('YCS-241220-009', 15, 'BILLING', 'SEA', 'thailand', '10300', 'Referral Corp Client', '+66-89-999-9999', '333 Corporate Ref Blvd, Bangkok', '10300', 'EE999999999KR', 0.025000, 8.50, false, false, 'H-03-01', 'H', '2024-12-21 13:00:00', 8.70, true, true, 'Corporate referral order', null, null, 'Bulk shipment', '2024-12-20 15:00:00', NOW());