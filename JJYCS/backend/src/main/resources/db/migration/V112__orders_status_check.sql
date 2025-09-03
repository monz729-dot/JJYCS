-- V112__orders_status_check.sql
-- 주문 상태 제약 조건 제거 (비즈니스 레이어에서만 검증)

-- 기존 체크 제약 제거 - 운영 유연성을 위해 DB 레벨 제약 제거
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;

-- 상태별 조회 성능 향상을 위한 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);