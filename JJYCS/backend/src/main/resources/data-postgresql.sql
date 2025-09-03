-- PostgreSQL용 초기 데이터
-- 관리자 계정 생성 (비밀번호: password)
INSERT INTO users (email, password, name, user_type, status, member_code, email_verified, created_at, updated_at)
SELECT 'admin@ycs.com', 
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
       'Administrator', 
       'ADMIN', 
       'ACTIVE', 
       'ADM001', 
       true,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@ycs.com');

-- 테스트용 기업 사용자 생성 (비밀번호: password)
INSERT INTO users (email, password, name, user_type, status, member_code, email_verified, created_at, updated_at)
SELECT 'corporate@example.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
       'Corporate User',
       'CORPORATE',
       'ACTIVE',
       'COR001',
       true,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'corporate@example.com');

-- 테스트용 창고 사용자 생성 (비밀번호: password)
INSERT INTO users (email, password, name, user_type, status, member_code, email_verified, created_at, updated_at)
SELECT 'warehouse@example.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
       'Warehouse Manager',
       'WAREHOUSE',
       'ACTIVE',
       'WHS001',
       true,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'warehouse@example.com');