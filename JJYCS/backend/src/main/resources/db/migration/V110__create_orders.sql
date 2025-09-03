-- V110__create_orders.sql
-- 주문 테이블 생성 (최소 구조)

CREATE TABLE IF NOT EXISTS public.orders (
  id bigserial PRIMARY KEY,
  order_no varchar(20) NOT NULL UNIQUE,
  user_id bigint NOT NULL,
  status varchar(32) NOT NULL DEFAULT 'DRAFT',
  total_amount numeric(18,2) NOT NULL DEFAULT 0,
  currency varchar(8) NOT NULL DEFAULT 'KRW',
  note text,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);