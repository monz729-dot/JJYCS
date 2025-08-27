-- Performance optimization indexes for YCS LMS
-- Created: 2024-08-23
-- Purpose: Improve query performance for production workloads

-- Users table indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_last_login ON users(last_login_at);
CREATE INDEX IF NOT EXISTS idx_users_member_code ON users(member_code);

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_users_role_status ON users(role, status);
CREATE INDEX IF NOT EXISTS idx_users_active_status ON users(active, status);
CREATE INDEX IF NOT EXISTS idx_users_status_created ON users(status, created_at DESC);

-- Orders table indexes
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_order_type ON orders(order_type);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_updated_at ON orders(updated_at);
CREATE INDEX IF NOT EXISTS idx_orders_order_number ON orders(order_number);
CREATE INDEX IF NOT EXISTS idx_orders_tracking_number ON orders(tracking_number);
CREATE INDEX IF NOT EXISTS idx_orders_recipient_name ON orders(recipient_name);
CREATE INDEX IF NOT EXISTS idx_orders_sender_name ON orders(sender_name);

-- Composite indexes for order queries
CREATE INDEX IF NOT EXISTS idx_orders_user_status ON orders(user_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_user_created ON orders(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_type_status ON orders(order_type, status);
CREATE INDEX IF NOT EXISTS idx_orders_status_updated ON orders(status, updated_at DESC);

-- Order items indexes
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_name ON order_items(product_name);
CREATE INDEX IF NOT EXISTS idx_order_items_hs_code ON order_items(hs_code);

-- Order boxes indexes
CREATE INDEX IF NOT EXISTS idx_order_boxes_order_id ON order_boxes(order_id);
CREATE INDEX IF NOT EXISTS idx_order_boxes_label_code ON order_boxes(label_code);
CREATE INDEX IF NOT EXISTS idx_order_boxes_cbm ON order_boxes(cbm_m3);

-- Files table indexes
CREATE INDEX IF NOT EXISTS idx_files_entity_type_id ON files(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_files_category ON files(file_category);
CREATE INDEX IF NOT EXISTS idx_files_uploader ON files(uploader_id);
CREATE INDEX IF NOT EXISTS idx_files_created_at ON files(created_at);

-- Notifications indexes
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(notification_type);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created ON notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, is_read);

-- Audit logs indexes
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_email ON audit_logs(user_email);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action_type ON audit_logs(action_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_action ON audit_logs(user_email, action_type);

-- Enterprise profiles indexes
CREATE INDEX IF NOT EXISTS idx_enterprise_profiles_user_id ON enterprise_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_enterprise_profiles_company ON enterprise_profiles(company_name);
CREATE INDEX IF NOT EXISTS idx_enterprise_profiles_business_num ON enterprise_profiles(business_number);

-- Partner profiles indexes
CREATE INDEX IF NOT EXISTS idx_partner_profiles_user_id ON partner_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_partner_profiles_company ON partner_profiles(company_name);
CREATE INDEX IF NOT EXISTS idx_partner_profiles_business_num ON partner_profiles(business_number);

-- Warehouse profiles indexes
CREATE INDEX IF NOT EXISTS idx_warehouse_profiles_user_id ON warehouse_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_warehouse_profiles_name ON warehouse_profiles(warehouse_name);

-- Bank accounts indexes
CREATE INDEX IF NOT EXISTS idx_bank_accounts_user_id ON bank_accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_bank_accounts_currency ON bank_accounts(currency);

-- Additional performance indexes for specific use cases

-- Full-text search support (MySQL specific)
ALTER TABLE users ADD FULLTEXT(name, email, address);
ALTER TABLE orders ADD FULLTEXT(recipient_name, sender_name, description);
ALTER TABLE order_items ADD FULLTEXT(product_name, product_name_kr, description);

-- Partial indexes for active records only
CREATE INDEX IF NOT EXISTS idx_users_active_email ON users(email) WHERE active = true;
CREATE INDEX IF NOT EXISTS idx_orders_active_status ON orders(status) WHERE status NOT IN ('CANCELLED', 'DELIVERED');

-- Indexes for reporting and analytics
CREATE INDEX IF NOT EXISTS idx_orders_monthly_stats ON orders(DATE_FORMAT(created_at, '%Y-%m'), status);
CREATE INDEX IF NOT EXISTS idx_users_monthly_reg ON users(DATE_FORMAT(created_at, '%Y-%m'));

-- Foreign key performance indexes
CREATE INDEX IF NOT EXISTS idx_order_items_order_fk ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_boxes_order_fk ON order_boxes(order_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_fk ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_files_uploader_fk ON files(uploader_id);

-- Statistics update for MySQL optimizer
ANALYZE TABLE users;
ANALYZE TABLE orders;
ANALYZE TABLE order_items;
ANALYZE TABLE order_boxes;
ANALYZE TABLE files;
ANALYZE TABLE notifications;
ANALYZE TABLE audit_logs;

-- Add comments for documentation
ALTER TABLE users COMMENT = 'Users table with optimized indexes for role, status, and search queries';
ALTER TABLE orders COMMENT = 'Orders table with composite indexes for efficient filtering and sorting';
ALTER TABLE order_items COMMENT = 'Order items with full-text search and product lookups';
ALTER TABLE audit_logs COMMENT = 'Audit logs with time-based partitioning ready indexes';