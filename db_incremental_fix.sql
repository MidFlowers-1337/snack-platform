-- 增量修复：仅补齐缺失表和缺失列，不做删除操作
CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车项ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID（门店商品ID）',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_store_sku (user_id, store_id, sku_id),
    KEY idx_user_id (user_id),
    KEY idx_store_id (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    module VARCHAR(50) NOT NULL COMMENT '操作模块',
    operation VARCHAR(100) NOT NULL COMMENT '操作描述',
    method VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    params TEXT DEFAULT NULL COMMENT '请求参数',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态：1成功 0失败',
    error_msg TEXT DEFAULT NULL COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_module (module),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS promotions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '关联商品ID',
    title VARCHAR(100) NOT NULL COMMENT '活动标题',
    original_price DECIMAL(10, 2) NOT NULL COMMENT '原价',
    promotion_price DECIMAL(10, 2) NOT NULL COMMENT '促销价',
    stock INT NOT NULL DEFAULT 0 COMMENT '促销库存（限量）',
    sold_count INT NOT NULL DEFAULT 0 COMMENT '已售数量',
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    end_time DATETIME NOT NULL COMMENT '活动结束时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id),
    INDEX idx_status_time (status, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销活动表';

CREATE TABLE IF NOT EXISTS points_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    points INT NOT NULL COMMENT '积分变动（正数为获得，负数为消费）',
    type VARCHAR(20) NOT NULL COMMENT '类型：EARN-获得，SPEND-消费',
    description VARCHAR(200) COMMENT '描述',
    order_id BIGINT COMMENT '关联订单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

SET @add_points_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN points INT NOT NULL DEFAULT 0 COMMENT ''积分余额''',
        'SELECT ''users.points 已存在'' AS message'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'points'
);
PREPARE stmt FROM @add_points_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 修复 pickup_code 重复数据，保留每组重复中的首条记录，其余置空（MySQL 唯一索引允许多个 NULL）
UPDATE orders o
JOIN (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (PARTITION BY pickup_code ORDER BY id) AS rn
        FROM orders
        WHERE pickup_code IS NOT NULL
          AND pickup_code <> ''
    ) duplicated
    WHERE duplicated.rn > 1
) need_clear ON need_clear.id = o.id
SET o.pickup_code = NULL;

-- 如果存在旧的普通索引 idx_pickup_code，则先删除
SET @drop_pickup_idx_sql = (
    SELECT IF(
        COUNT(*) > 0,
        'ALTER TABLE orders DROP INDEX idx_pickup_code',
        'SELECT ''orders.idx_pickup_code 不存在，无需删除'' AS message'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND index_name = 'idx_pickup_code'
);
PREPARE stmt FROM @drop_pickup_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 pickup_code 唯一索引，避免后续出现重复核销码
SET @add_pickup_unique_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE orders ADD UNIQUE KEY uk_pickup_code (pickup_code)',
        'SELECT ''orders.uk_pickup_code 已存在'' AS message'
    )
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND index_name = 'uk_pickup_code'
);
PREPARE stmt FROM @add_pickup_unique_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
