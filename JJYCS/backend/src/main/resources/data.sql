-- 기본 관리자 계정 생성 (비밀번호: password) - Hibernate가 테이블을 생성한 후 실행
INSERT INTO users (email, password, name, user_type, status, member_code, email_verified)
SELECT 'admin@ycs.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrator', 'ADMIN', 'ACTIVE', 'ADM001', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@ycs.com');