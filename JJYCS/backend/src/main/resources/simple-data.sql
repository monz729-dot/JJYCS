-- Test users for different roles
INSERT INTO users (email, password, name, phone, user_type, status, member_code, email_verified, created_at, updated_at) VALUES
('admin@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Administrator', '02-1234-5678', 'ADMIN', 'ACTIVE', 'ADM001', true, NOW(), NOW()),
('kimcs@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Kim Cheolsu', '010-1234-5678', 'GENERAL', 'ACTIVE', 'GEN001', true, NOW(), NOW()),
('lee@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Lee Younghee', '010-2345-6789', 'CORPORATE', 'ACTIVE', 'COR001', true, NOW(), NOW()),
('park@partner.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Park Minsu', '010-3456-7890', 'PARTNER', 'ACTIVE', 'PAR001', true, NOW(), NOW());

-- Test orders for Kim Cheolsu (user_id = 2)
INSERT INTO orders (order_number, user_id, status, shipping_type, country, postal_code, recipient_name, recipient_phone, recipient_address, recipient_postal_code, total_cbm, total_weight, requires_extra_recipient, no_member_code, repacking_requested, repacking_completed, special_requests, created_at, updated_at) VALUES
('YCS-250828-001', 2, 'RECEIVED', 'SEA', 'Thailand', '10100', 'Kim Jiyoung', '010-1111-2222', 'Bangkok, Sukhumvit Road 123', '10100', 0.012000, 2.50, false, false, false, false, 'Electronics delivery', DATEADD('DAY', -7, NOW()), DATEADD('DAY', -7, NOW())),
('YCS-250828-002', 2, 'ARRIVED', 'SEA', 'Thailand', '10110', 'Park Minji', '010-2222-3333', 'Chiang Mai, Huay Kaew Road 456', '50200', 0.045000, 8.20, false, false, false, false, 'Clothing and accessories', DATEADD('DAY', -5, NOW()), DATEADD('DAY', -3, NOW())),
('YCS-250828-003', 2, 'REPACKING', 'AIR', 'Thailand', '10120', 'Lee Sooyoung', '010-3333-4444', 'Phuket, Patong Beach Road 789', '83150', 0.038000, 5.80, true, false, true, false, 'Cosmetics repack request', DATEADD('DAY', -4, NOW()), DATEADD('DAY', -2, NOW())),
('YCS-250828-004', 2, 'SHIPPING', 'SEA', 'Thailand', '10130', 'Choi Hyunwoo', '010-4444-5555', 'Pattaya, Beach Road 101', '20150', 0.028000, 4.10, false, false, false, false, 'Household items', DATEADD('DAY', -3, NOW()), DATEADD('DAY', -1, NOW())),
('YCS-250828-005', 2, 'DELIVERED', 'AIR', 'Thailand', '10140', 'Jung Minsu', '010-5555-6666', 'Krabi, Ao Nang Beach 202', '81000', 0.015000, 1.90, false, false, false, false, 'Sports equipment', DATEADD('DAY', -10, NOW()), DATEADD('DAY', -8, NOW())),
('YCS-250828-006', 2, 'BILLING', 'SEA', 'Thailand', '10150', 'Kim Sujin', '010-6666-7777', 'Hat Yai, Niphat Uthit Road 303', '90110', 0.052000, 7.30, false, true, false, false, 'Food and health supplements', DATEADD('DAY', -2, NOW()), DATEADD('DAY', -1, NOW())),
('YCS-250828-007', 2, 'PAYMENT_PENDING', 'AIR', 'Thailand', '10160', 'Oh Seunghwan', '010-7777-8888', 'Hua Hin, Petchkasem Road 404', '77110', 0.022000, 3.40, false, false, false, false, 'Accessories and watches', DATEADD('DAY', -1, NOW()), NOW());

-- Test order items for the orders above
INSERT INTO order_items (order_id, hs_code, description, quantity, weight, width, height, depth, cbm, unit_price, total_price, english_name, created_at) VALUES
-- Order 1 items
(1, '8517120000', 'Smartphone', 1, 0.20, 15.0, 7.5, 1.0, 0.0001125, 800000, 800000, 'Smartphone', DATEADD('DAY', -7, NOW())),
(1, '8518300000', 'Earphones', 2, 0.30, 12.0, 8.0, 3.0, 0.000288, 150000, 300000, 'Earphones', DATEADD('DAY', -7, NOW())),

-- Order 2 items  
(2, '6203420000', 'Men Trousers', 3, 1.50, 30.0, 25.0, 8.0, 0.006000, 80000, 240000, 'Men Trousers', DATEADD('DAY', -5, NOW())),
(2, '6204620000', 'Women Blouse', 4, 1.20, 25.0, 20.0, 6.0, 0.003000, 60000, 240000, 'Women Blouse', DATEADD('DAY', -5, NOW())),

-- Order 3 items
(3, '3304990000', 'Basic Cosmetics Set', 2, 2.80, 20.0, 15.0, 12.0, 0.0036, 350000, 700000, 'Basic Cosmetics Set', DATEADD('DAY', -4, NOW())),

-- Order 4 items
(4, '3924100000', 'Kitchen Utensils', 5, 3.10, 25.0, 20.0, 10.0, 0.005, 45000, 225000, 'Kitchen Utensils', DATEADD('DAY', -3, NOW())),

-- Order 5 items  
(5, '9506620000', 'Tennis Racket', 1, 0.35, 68.0, 25.0, 3.0, 0.0051, 180000, 180000, 'Tennis Racket', DATEADD('DAY', -10, NOW())),
(5, '6307900000', 'Sports Towel', 3, 1.55, 30.0, 20.0, 8.0, 0.0048, 25000, 75000, 'Sports Towel', DATEADD('DAY', -10, NOW())),

-- Order 6 items
(6, '2106909000', 'Health Supplements', 10, 5.00, 30.0, 25.0, 20.0, 0.015, 80000, 800000, 'Health Supplements', DATEADD('DAY', -2, NOW())),
(6, '0901210000', 'Coffee Beans', 5, 2.30, 25.0, 15.0, 10.0, 0.00375, 60000, 300000, 'Coffee Beans', DATEADD('DAY', -2, NOW())),

-- Order 7 items
(7, '7113190000', 'Silver Ring', 2, 0.10, 8.0, 8.0, 3.0, 0.000192, 250000, 500000, 'Silver Ring', DATEADD('DAY', -1, NOW())),
(7, '9101210000', 'Wrist Watch', 1, 0.30, 12.0, 10.0, 4.0, 0.00048, 450000, 450000, 'Wrist Watch', DATEADD('DAY', -1, NOW()));

-- Test order boxes for the orders above  
INSERT INTO order_boxes (order_id, box_number, width, height, depth, weight, cbm, created_at) VALUES
-- Order 1 boxes
(1, 'BOX-001-001', 30.0, 20.0, 15.0, 2.50, 0.009000, DATEADD('DAY', -7, NOW())),

-- Order 2 boxes  
(2, 'BOX-002-001', 40.0, 30.0, 25.0, 4.20, 0.030000, DATEADD('DAY', -5, NOW())),
(2, 'BOX-002-002', 35.0, 25.0, 20.0, 4.00, 0.017500, DATEADD('DAY', -5, NOW())),

-- Order 3 boxes
(3, 'BOX-003-001', 25.0, 20.0, 18.0, 5.80, 0.009000, DATEADD('DAY', -4, NOW())),

-- Order 4 boxes
(4, 'BOX-004-001', 30.0, 25.0, 20.0, 4.10, 0.015000, DATEADD('DAY', -3, NOW())),

-- Order 5 boxes
(5, 'BOX-005-001', 70.0, 30.0, 8.0, 1.90, 0.016800, DATEADD('DAY', -10, NOW())),

-- Order 6 boxes  
(6, 'BOX-006-001', 35.0, 30.0, 25.0, 3.80, 0.026250, DATEADD('DAY', -2, NOW())),
(6, 'BOX-006-002', 30.0, 25.0, 20.0, 3.50, 0.015000, DATEADD('DAY', -2, NOW())),

-- Order 7 boxes
(7, 'BOX-007-001', 20.0, 15.0, 12.0, 3.40, 0.003600, DATEADD('DAY', -1, NOW()));