-- Simple test data for YCS LMS - H2 Compatible
-- Password for all users: password123 (BCrypt hash: $2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha)

-- 1. Admin account
INSERT INTO users (id, email, name, phone, password, user_type, status, email_verified, created_at, updated_at) VALUES
(1, 'admin@ycs.com', 'System Admin', '02-1234-5678', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'ADMIN', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Corporate users (PENDING approval)
INSERT INTO users (id, email, name, phone, password, user_type, status, email_verified, created_at, updated_at) VALUES
(2, 'corporate1@samsung.co.kr', 'Samsung Electronics', '02-2255-0114', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'CORPORATE', 'PENDING', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'procurement@lg.co.kr', 'LG Electronics', '02-3777-1114', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'CORPORATE', 'PENDING', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'logistics@hyundai.com', 'Hyundai Motor', '02-3464-1114', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'CORPORATE', 'ACTIVE', true, DATEADD('DAY', -10, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

-- 3. Partner users (PENDING approval)  
INSERT INTO users (id, email, name, phone, password, user_type, status, email_verified, created_at, updated_at) VALUES
(5, 'partner@busantrading.co.kr', 'Busan Trading Co', '051-123-4567', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'PARTNER', 'PENDING', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'info@seoullogistics.com', 'Seoul Logistics', '02-9876-5432', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'PARTNER', 'PENDING', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'partner@approved.co.kr', 'Approved Partner Co', '031-555-7777', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'PARTNER', 'ACTIVE', true, DATEADD('DAY', -5, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

-- 4. General users
INSERT INTO users (id, email, name, phone, password, user_type, status, email_verified, created_at, updated_at) VALUES
(8, 'user@example.com', 'General User', '010-1234-5678', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'GENERAL', 'ACTIVE', true, DATEADD('DAY', -15, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
(9, 'inactive@test.com', 'Inactive User', '010-9999-0000', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'GENERAL', 'INACTIVE', true, DATEADD('DAY', -30, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

-- 5. Warehouse staff  
INSERT INTO users (id, email, name, phone, password, user_type, status, email_verified, created_at, updated_at) VALUES
(10, 'warehouse@ycs.com', 'Warehouse Manager', '02-5555-1234', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'WAREHOUSE', 'ACTIVE', true, DATEADD('DAY', -60, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);