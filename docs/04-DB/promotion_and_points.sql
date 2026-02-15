-- 促销活动表
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
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id),
    INDEX idx_status_time (status, start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销活动表';

-- 积分流水表
CREATE TABLE IF NOT EXISTS points_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    points INT NOT NULL COMMENT '积分变动（正数为获得，负数为消耗）',
    type VARCHAR(20) NOT NULL COMMENT '类型：EARN-获得，SPEND-消耗',
    description VARCHAR(200) COMMENT '描述',
    order_id BIGINT COMMENT '关联订单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

-- 用户表添加积分字段（如果没有的话）
ALTER TABLE users ADD COLUMN IF NOT EXISTS points INT NOT NULL DEFAULT 0 COMMENT '积分余额';
