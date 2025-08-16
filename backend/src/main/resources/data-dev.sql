-- YCS LMS Development Data (H2 Compatible)
-- Essential test data with individual INSERT statements

-- Essential test users (password: password123)
INSERT INTO users (email, password_hash, name, phone, role, status, email_verified, agree_terms, agree_privacy, agree_marketing, created_at, updated_at, member_code) VALUES ('general@test.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', 'General Test', '010-7777-8888', 'INDIVIDUAL', 'ACTIVE', TRUE, TRUE, TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'USR007');

INSERT INTO users (email, password_hash, name, phone, role, status, email_verified, agree_terms, agree_privacy, agree_marketing, created_at, updated_at, member_code) VALUES ('admin@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', 'Admin User', '010-5678-9012', 'ADMIN', 'ACTIVE', TRUE, TRUE, TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ADM001');

INSERT INTO users (email, password_hash, name, phone, role, status, email_verified, agree_terms, agree_privacy, agree_marketing, created_at, updated_at, member_code) VALUES ('user@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', 'Test User', '010-1234-5678', 'INDIVIDUAL', 'ACTIVE', TRUE, TRUE, TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'USR001');

INSERT INTO users (email, password_hash, name, phone, role, status, email_verified, agree_terms, agree_privacy, agree_marketing, created_at, updated_at, member_code) VALUES ('enterprise@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', 'Enterprise User', '010-2345-6789', 'ENTERPRISE', 'ACTIVE', TRUE, TRUE, TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ENT001');

INSERT INTO users (email, password_hash, name, phone, role, status, email_verified, agree_terms, agree_privacy, agree_marketing, created_at, updated_at, member_code) VALUES ('pending@ycs.com', '$2a$10$8K1p/a0dUrz1.NhfaXPCqeLN9/Y4Hh1Sb9Fc6LGCvPcbGYA4AjEYC', 'Pending User', '010-3333-4444', 'ENTERPRISE', 'PENDING_APPROVAL', TRUE, TRUE, TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL);

-- Sample orders
INSERT INTO orders (user_id, order_code, recipient_name, recipient_phone, recipient_address, recipient_country, order_type, status, total_amount, currency, requires_extra_recipient, created_at, updated_at) VALUES (1, 'ORD-2024-001', 'Test Customer', '+66-81-234-5678', '123 Test Street, Bangkok, Thailand', 'TH', 'SEA', 'REQUESTED', 850.00, 'THB', FALSE, DATEADD('DAY', -2, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

INSERT INTO orders (user_id, order_code, recipient_name, recipient_phone, recipient_address, recipient_country, order_type, status, total_amount, currency, requires_extra_recipient, created_at, updated_at) VALUES (1, 'ORD-2024-002', 'Another Customer', '+84-90-876-5432', '456 Test Road, Ho Chi Minh City, Vietnam', 'VN', 'AIR', 'REQUESTED', 2300.00, 'THB', TRUE, DATEADD('DAY', -1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

-- Order items
INSERT INTO order_items (order_id, name, description, quantity, unit_price, currency, unit_weight, ems_code, hs_code, restricted, created_at, updated_at) VALUES (1, 'Test Product', 'Sample product for testing', 2, 425.00, 'THB', 1.5, 'EMS001', '190230', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO order_items (order_id, name, description, quantity, unit_price, currency, unit_weight, ems_code, hs_code, restricted, created_at, updated_at) VALUES (2, 'Electronic Device', 'Sample electronic device', 1, 2300.00, 'THB', 0.5, 'EMS002', '851712', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Order boxes  
INSERT INTO order_boxes (order_id, box_number, width_cm, height_cm, depth_cm, weight_kg, label_code, status, created_at, updated_at) VALUES (1, 1, 30.0, 20.0, 15.0, 1.5, 'BOX-001-A', 'READY_FOR_OUTBOUND', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO order_boxes (order_id, box_number, width_cm, height_cm, depth_cm, weight_kg, label_code, status, created_at, updated_at) VALUES (2, 1, 25.0, 15.0, 10.0, 0.5, 'BOX-002-A', 'INBOUND_COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);