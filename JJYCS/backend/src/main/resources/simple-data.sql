-- Test users for different roles
INSERT INTO users (email, password, name, phone, user_type, status, member_code, email_verified, created_at, updated_at) VALUES
('admin@ycs.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Administrator', '02-1234-5678', 'ADMIN', 'ACTIVE', 'ADM001', true, NOW(), NOW()),
('kimcs@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Kim Cheolsu', '010-1234-5678', 'GENERAL', 'ACTIVE', 'GEN001', true, NOW(), NOW()),
('lee@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Lee Younghee', '010-2345-6789', 'CORPORATE', 'ACTIVE', 'COR001', true, NOW(), NOW()),
('park@partner.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Park Minsu', '010-3456-7890', 'PARTNER', 'ACTIVE', 'PAR001', true, NOW(), NOW());