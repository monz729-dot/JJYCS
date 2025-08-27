-- Insert test users
INSERT INTO users (id, email, password, name, user_type, status, member_code, created_at) VALUES 
(1, 'kimcs@email.com', '$2a$10$8X9QKlILRlAzQXk0HQ8UkO8vXtqGZI.EfYXN.x3QNmXeGNmMO', 'Kim Cheolsu', 'GENERAL', 'ACTIVE', 'KC001', CURRENT_TIMESTAMP),
(2, 'lee@company.com', '$2a$10$8X9QKlILRlAzQXk0HQ8UkO8vXtqGZI.EfYXN.x3QNmXeGNmMO', 'Lee Younghee', 'CORPORATE', 'ACTIVE', 'LY002', CURRENT_TIMESTAMP),
(3, 'park@partner.com', '$2a$10$8X9QKlILRlAzQXk0HQ8UkO8vXtqGZI.EfYXN.x3QNmXeGNmMO', 'Park Minsu', 'PARTNER', 'ACTIVE', 'PM003', CURRENT_TIMESTAMP),
(4, 'admin@ycs.com', '$2a$10$8X9QKlILRlAzQXk0HQ8UkO8vXtqGZI.EfYXN.x3QNmXeGNmMO', 'Administrator', 'ADMIN', 'ACTIVE', 'AD004', CURRENT_TIMESTAMP);