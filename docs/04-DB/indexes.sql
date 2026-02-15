-- ============================================
-- 零食电商平台 - 数据库索引优化脚本
-- 执行时间：Phase 2
-- ============================================

-- 订单表复合索引（高频查询场景）
CREATE INDEX IF NOT EXISTS idx_orders_user_status ON orders(user_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_store_status ON orders(store_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_create_time_status ON orders(create_time, status);
CREATE INDEX IF NOT EXISTS idx_orders_pickup_store ON orders(pickup_code, store_id);

-- 订单项表索引（fillOrderDetails 批量查询优化）
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

-- 门店商品表索引
CREATE INDEX IF NOT EXISTS idx_store_sku_store_status ON store_skus(store_id, status);

-- 购物车表索引
CREATE INDEX IF NOT EXISTS idx_cart_user_store ON cart_item(user_id, store_id);

-- 操作日志表索引
CREATE INDEX IF NOT EXISTS idx_operation_log_created_at ON operation_log(created_at);
CREATE INDEX IF NOT EXISTS idx_operation_log_user_id ON operation_log(user_id);
