-- 성능 최적화를 위한 인덱스 추가
-- 자주 조회되는 컬럼들에 대한 인덱스 생성

-- 1. 주문 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_orders_order_code ON orders(order_code);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_order_type ON orders(order_type);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_user_status ON orders(user_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders(status, created_at);

-- 2. 사용자 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_member_code ON users(member_code);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_role_status ON users(role, status);

-- 3. 주문 아이템 인덱스
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order_item_order ON order_items(order_id, item_order);
CREATE INDEX IF NOT EXISTS idx_order_items_hs_code ON order_items(hs_code);
CREATE INDEX IF NOT EXISTS idx_order_items_ems_code ON order_items(ems_code);

-- 4. 주문 박스 인덱스
CREATE INDEX IF NOT EXISTS idx_order_boxes_order_id ON order_boxes(order_id);
CREATE INDEX IF NOT EXISTS idx_order_boxes_label_code ON order_boxes(label_code);
CREATE INDEX IF NOT EXISTS idx_order_boxes_box_number ON order_boxes(order_id, box_number);
CREATE INDEX IF NOT EXISTS idx_order_boxes_warehouse_id ON order_boxes(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_order_boxes_status ON order_boxes(status);

-- 5. 창고 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_scan_events_box_id ON scan_events(box_id);
CREATE INDEX IF NOT EXISTS idx_scan_events_scan_type ON scan_events(scan_type);
CREATE INDEX IF NOT EXISTS idx_scan_events_created_at ON scan_events(created_at);
CREATE INDEX IF NOT EXISTS idx_scan_events_box_type_created ON scan_events(box_id, scan_type, created_at);

-- 6. 배송 추적 인덱스
CREATE INDEX IF NOT EXISTS idx_shipment_tracking_order_id ON shipment_tracking(order_id);
CREATE INDEX IF NOT EXISTS idx_shipment_tracking_tracking_number ON shipment_tracking(tracking_number);
CREATE INDEX IF NOT EXISTS idx_shipment_tracking_carrier ON shipment_tracking(carrier);
CREATE INDEX IF NOT EXISTS idx_shipment_tracking_status ON shipment_tracking(tracking_status);

-- 7. 결제 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(payment_status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);

-- 8. 견적 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_estimates_order_id ON estimates(order_id);
CREATE INDEX IF NOT EXISTS idx_estimates_estimate_type ON estimates(estimate_type);
CREATE INDEX IF NOT EXISTS idx_estimates_status ON estimates(status);

-- 9. 파트너 추천 인덱스
CREATE INDEX IF NOT EXISTS idx_partner_referrals_partner_id ON partner_referrals(partner_id);
CREATE INDEX IF NOT EXISTS idx_partner_referrals_referred_user_id ON partner_referrals(referred_user_id);
CREATE INDEX IF NOT EXISTS idx_partner_referrals_created_at ON partner_referrals(created_at);

-- 10. 감사 로그 인덱스 (대용량 테이블)
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_action_created ON audit_logs(user_id, action, created_at);

-- 11. 복합 비즈니스 로직 인덱스
-- CBM 관련 조회 최적화
CREATE INDEX IF NOT EXISTS idx_orders_cbm_threshold ON orders(total_cbm_m3) WHERE total_cbm_m3 > 29;

-- THB 금액 관련 조회 최적화  
CREATE INDEX IF NOT EXISTS idx_orders_thb_threshold ON orders(total_amount) WHERE currency = 'THB' AND total_amount > 1500;

-- 승인 대기 사용자 조회 최적화
CREATE INDEX IF NOT EXISTS idx_users_pending_approval ON users(status, created_at) WHERE status = 'pending_approval';

-- 지연 주문 조회 최적화
CREATE INDEX IF NOT EXISTS idx_orders_delayed ON orders(status, created_at) WHERE status = 'delayed';

-- 12. 프로파일 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_enterprise_profiles_user_id ON enterprise_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_partner_profiles_user_id ON partner_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_partner_profiles_referral_code ON partner_profiles(referral_code);
CREATE INDEX IF NOT EXISTS idx_warehouse_profiles_user_id ON warehouse_profiles(user_id);

-- 13. 주소 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_addresses_user_id ON addresses(user_id);
CREATE INDEX IF NOT EXISTS idx_addresses_address_type ON addresses(address_type);
CREATE INDEX IF NOT EXISTS idx_addresses_is_default ON addresses(user_id, is_default);

-- 14. 설정 및 구성 인덱스
CREATE INDEX IF NOT EXISTS idx_config_config_key ON config(config_key);
CREATE INDEX IF NOT EXISTS idx_config_category ON config(category);

-- 코멘트로 인덱스 목적 설명
COMMENT ON INDEX idx_orders_order_code IS '주문 코드로 빠른 검색';
COMMENT ON INDEX idx_orders_user_status IS '사용자별 주문 상태 필터링';
COMMENT ON INDEX idx_order_boxes_label_code IS '라벨 코드로 박스 스캔 시 빠른 조회';
COMMENT ON INDEX idx_users_member_code IS '회원 코드 검증 및 조회';
COMMENT ON INDEX idx_orders_cbm_threshold IS 'CBM 29m³ 초과 주문 조회';
COMMENT ON INDEX idx_orders_thb_threshold IS 'THB 1,500 초과 주문 조회';