-- YCS LMS 추가 데이터 - 태국 전용 설정
-- 2024-08-29: 태국 전용 기능을 위한 기본 데이터

-- YCS 접수지 정보 (INBOUND_LOCATIONS)
INSERT INTO inbound_locations (name, address, postal_code, phone, contact_person, business_hours, special_instructions, is_active, display_order) VALUES
('서울 본사', '서울특별시 강남구 테헤란로 123', '06234', '02-1234-5678', '김영희', '평일 09:00-18:00', '1층 로비에서 접수', true, 1),
('부산 지점', '부산광역시 해운대구 센텀중앙로 456', '48060', '051-9876-5432', '박철수', '평일 09:00-17:00', '지하 1층 물류센터', true, 2),
('인천공항 센터', '인천광역시 중구 공항로 789', '22382', '032-5555-1234', '이미영', '24시간 운영', '화물터미널 A동 3층', true, 3);

-- 택배사 정보 (COURIER_COMPANIES)
INSERT INTO courier_companies (code, name, name_en, website, tracking_url_template, is_active, display_order) VALUES
('CJ', 'CJ대한통운', 'CJ Logistics', 'https://www.cjlogistics.com', 'https://www.doortodoor.co.kr/parcel/pa_004.jsp?fsp_action=PANAVIGATION&fsp_cmd=retrieveInvNoACL&invc_no={trackingNumber}', true, 1),
('LOTTE', '롯데택배', 'Lotte Express', 'https://www.lotteglogis.com', 'https://www.lotteglogis.com/home/reservation/tracking/linkView?InvNo={trackingNumber}', true, 2),
('HANJIN', '한진택배', 'Hanjin Express', 'https://www.hanjin.co.kr', 'https://www.hanjin.co.kr/kor/CMS/DeliveryMgr/WaybillResult.do?mCode=MN038&schLang=KR&wblnumText2={trackingNumber}', true, 3),
('KDEXP', '경동택배', 'KD Express', 'https://kdexp.com', 'https://kdexp.com/service/delivery/delivery_result.asp?barcode={trackingNumber}', true, 4),
('CHUNIL', '천일택배', 'ChunIl Express', 'https://www.chunil.co.kr', 'https://www.chunil.co.kr/HTrace/HTrace.jsp?transNo={trackingNumber}', true, 5);

-- 태국 주요 우편번호 캐시 (EMS_POSTAL_CODES)
INSERT INTO ems_postal_codes (country_code, postal_code, city, district, province, city_en, district_en, province_en, is_active) VALUES
('TH', '10110', '방콕', '빠뚬완', '방콕', 'Bangkok', 'Pathum Wan', 'Bangkok', true),
('TH', '10120', '방콕', '두시트', '방콕', 'Bangkok', 'Dusit', 'Bangkok', true),
('TH', '10200', '방콕', '방락', '방콕', 'Bangkok', 'Bang Rak', 'Bangkok', true),
('TH', '10300', '방콕', '쁘라나콘', '방콕', 'Bangkok', 'Phra Nakhon', 'Bangkok', true),
('TH', '10400', '방콕', '왕통랑', '방콕', 'Bangkok', 'Wang Thonglang', 'Bangkok', true),
('TH', '20150', '촌부리', '므앙 촌부리', '촌부리', 'Chonburi', 'Mueang Chonburi', 'Chonburi', true),
('TH', '20260', '파타야', '방라뭉', '촌부리', 'Pattaya', 'Bang Lamung', 'Chonburi', true),
('TH', '50100', '치앙마이', '므앙 치앙마이', '치앙마이', 'Chiang Mai', 'Mueang Chiang Mai', 'Chiang Mai', true),
('TH', '50200', '치앙마이', '항동', '치앙마이', 'Chiang Mai', 'Hang Dong', 'Chiang Mai', true),
('TH', '80000', '나콘시탐마랏', '므앙 나콘시탐마랏', '나콘시탐마랏', 'Nakhon Si Thammarat', 'Mueang Nakhon Si Thammarat', 'Nakhon Si Thammarat', true),
('TH', '90110', '핫야이', '핫야이', '송클라', 'Hat Yai', 'Hat Yai', 'Songkhla', true),
('TH', '90112', '핫야이', '핫야이', '송클라', 'Hat Yai', 'Hat Yai', 'Songkhla', true),
('TH', '83000', '푸켓', '므앙 푸켓', '푸켓', 'Phuket', 'Mueang Phuket', 'Phuket', true),
('TH', '83110', '푸켓', '딸랑', '푸켓', 'Phuket', 'Thalang', 'Phuket', true),
('TH', '84000', '수랏타니', '므앙 수랏타니', '수랏타니', 'Surat Thani', 'Mueang Surat Thani', 'Surat Thani', true),
('TH', '84140', '꼬사무이', '꼬사무이', '수랏타니', 'Koh Samui', 'Ko Samui', 'Surat Thani', true);

-- 은행 계좌 정보 업데이트 (기존 데이터 유지, 태국 관련 추가)
UPDATE bank_accounts SET description = '한국 고객 입금용' WHERE bank_name = 'KB국민은행';
UPDATE bank_accounts SET description = '기업 고객 전용' WHERE bank_name = '신한은행';

-- 태국 현지 은행 계좌 추가
INSERT INTO bank_accounts (bank_name, account_number, account_holder, is_active, is_default, display_order, description) VALUES
('Kasikornbank (Thailand)', '123-4-56789-0', 'YCS LOGISTICS (THAILAND) CO.,LTD.', true, false, 3, '태국 현지 결제용'),
('Bangkok Bank', '987-6-54321-0', 'YCS LOGISTICS (THAILAND) CO.,LTD.', true, false, 4, '태국 THB 결제용');

-- 테스트용 사용자 데이터 (기업회원, 파트너 승인 테스트용)
-- 비밀번호는 모두 "password123"으로 설정 (BCrypt 해시)

-- 1. 관리자 계정
INSERT IGNORE INTO users (id, email, name, phone, password, user_type, status, email_verified, created_at, updated_at) VALUES
(1, 'admin@ycs.com', '시스템 관리자', '02-1234-5678', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'ADMIN', 'ACTIVE', true, NOW(), NOW());

-- 2. 승인 대기중인 기업회원들
INSERT IGNORE INTO users (id, email, name, phone, password, user_type, status, email_verified, created_at, updated_at) VALUES
(2, 'corporate1@samsung.co.kr', '삼성전자', '02-2255-0114', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'CORPORATE', 'PENDING', true, NOW(), NOW()),
(3, 'procurement@lg.co.kr', 'LG전자', '02-3777-1114', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'CORPORATE', 'PENDING', true, NOW(), NOW()),
(4, 'logistics@hyundai.com', '현대자동차', '02-3464-1114', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'CORPORATE', 'ACTIVE', true, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW());

-- 3. 승인 대기중인 파트너들
INSERT IGNORE INTO users (id, email, name, phone, password, user_type, status, email_verified, created_at, updated_at) VALUES
(5, 'partner@busantrading.co.kr', '부산무역상사', '051-123-4567', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'PARTNER', 'PENDING', true, NOW(), NOW()),
(6, 'info@seoullogistics.com', '서울로지스틱스', '02-9876-5432', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'PARTNER', 'PENDING', false, NOW(), NOW()),
(7, 'partner@approved.co.kr', '승인완료파트너', '031-555-7777', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'PARTNER', 'ACTIVE', true, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW());

-- 4. 일반 사용자들
INSERT IGNORE INTO users (id, email, name, phone, password, user_type, status, email_verified, created_at, updated_at) VALUES
(8, 'user@example.com', '김일반', '010-1234-5678', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'GENERAL', 'ACTIVE', true, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
(9, 'inactive@test.com', '정지된사용자', '010-9999-0000', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'GENERAL', 'INACTIVE', true, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW());

-- 5. 창고 직원
INSERT IGNORE INTO users (id, email, name, phone, password, user_type, status, email_verified, created_at, updated_at) VALUES
(10, 'warehouse@ycs.com', '창고관리자', '02-5555-1234', '$2a$10$8CkqQjB0X7kLYZ9fv7vQO.8JGtGzLn5v3vTqR5L2rYbDf4nMhGgha', 'WAREHOUSE', 'ACTIVE', true, DATE_SUB(NOW(), INTERVAL 60 DAY), NOW());

-- 기업 프로필 정보 추가
INSERT IGNORE INTO enterprise_profiles (user_id, company_name, business_number, address, contact_person, created_at, updated_at) VALUES
(2, 'Samsung Electronics Co., Ltd.', '124-81-00998', '경기도 수원시 영통구 삼성로 129', '김삼성', NOW(), NOW()),
(3, 'LG Electronics Inc.', '107-86-15024', '서울특별시 영등포구 여의대로 128', '박엘지', NOW(), NOW()),
(4, 'Hyundai Motor Company', '174-81-00087', '울산광역시 북구 현대로 700', '이현대', NOW(), NOW());

-- 파트너 프로필 정보 추가  
INSERT IGNORE INTO partner_profiles (user_id, company_name, business_number, address, contact_person, partner_region, created_at, updated_at) VALUES
(5, '부산무역상사', '456-88-12345', '부산광역시 해운대구 센텀중앙로 456', '최부산', 'BUSAN', NOW(), NOW()),
(6, '서울로지스틱스', '789-12-34567', '서울특별시 강남구 테헤란로 789', '김서울', 'SEOUL', NOW(), NOW()),
(7, '승인완료파트너', '321-54-98765', '경기도 성남시 분당구 판교로 321', '박승인', 'GYEONGGI', NOW(), NOW());

-- 참고: 시스템 설정값들은 application.yml에서 관리됩니다.
-- - thailand.default_currency=THB  
-- - thailand.customs_threshold=1500
-- - cbm.threshold.air_conversion=29.0
-- - ems.api.enabled=true
-- - order.default_dest_country=TH