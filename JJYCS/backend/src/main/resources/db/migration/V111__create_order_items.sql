-- V111__create_order_items.sql
-- 주문 아이템 테이블 생성

CREATE TABLE IF NOT EXISTS public.order_items (
  id bigserial PRIMARY KEY,
  order_id bigint NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
  product_id bigint,
  name varchar(255) NOT NULL,
  qty integer NOT NULL DEFAULT 1,
  unit_price numeric(18,2) NOT NULL DEFAULT 0,
  created_at timestamptz DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ix_order_items_order_id ON public.order_items(order_id);