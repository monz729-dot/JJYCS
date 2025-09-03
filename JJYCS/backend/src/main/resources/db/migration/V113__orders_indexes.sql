-- V113__orders_indexes.sql
-- 주문 관련 인덱스 최적화

-- 주문번호 유니크 제약 추가 (이미 존재하면 스킵)
DO $$ 
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'ux_orders_order_no'
  ) THEN
    ALTER TABLE orders ADD CONSTRAINT ux_orders_order_no UNIQUE (order_no);
  END IF;
END $$;

-- 사용자별 주문 조회 최적화 인덱스
CREATE INDEX IF NOT EXISTS ix_orders_user_id ON orders(user_id);

-- 생성일자 기반 조회 최적화 (대시보드/통계)
CREATE INDEX IF NOT EXISTS ix_orders_created_at ON orders(created_at DESC);