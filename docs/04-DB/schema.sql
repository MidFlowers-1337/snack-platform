-- ============================================================
-- 面向多门店协同的连锁零食电商平台 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_general_ci
-- ============================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS snack_platform 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

USE snack_platform;

-- ============================================================
-- 1. 用户表 (users)
-- ============================================================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    role VARCHAR(20) NOT NULL DEFAULT 'CONSUMER' COMMENT '角色：CONSUMER-消费者/STORE_ADMIN-门店管理员/SYSTEM_ADMIN-系统管理员',
    store_id BIGINT DEFAULT NULL COMMENT '绑定门店ID（仅门店管理员）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role (role),
    KEY idx_phone (phone),
    KEY idx_store_id (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ============================================================
-- 2. 门店表 (stores)
-- ============================================================
DROP TABLE IF EXISTS stores;
CREATE TABLE stores (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '门店名称',
    address VARCHAR(255) NOT NULL COMMENT '详细地址',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    image VARCHAR(255) DEFAULT NULL COMMENT '门店图片URL',
    longitude DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
    latitude DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
    business_hours VARCHAR(100) DEFAULT '09:00-22:00' COMMENT '营业时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-休息中，1-营业中',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_location (longitude, latitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='门店表';

-- ============================================================
-- 3. 商品分类表 (categories)
-- ============================================================
DROP TABLE IF EXISTS categories;
CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_status_sort (status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类表';

-- ============================================================
-- 4. 平台商品表 (products) - SPU
-- ============================================================
DROP TABLE IF EXISTS products;
CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description TEXT DEFAULT NULL COMMENT '商品描述',
    image VARCHAR(255) DEFAULT NULL COMMENT '商品主图URL',
    price DECIMAL(10,2) NOT NULL COMMENT '建议零售价',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='平台商品表（SPU）';

-- ============================================================
-- 5. 门店商品表 (store_skus) - SKU ⭐核心表
-- 门店独立库存 & 门店独立定价
-- ============================================================
DROP TABLE IF EXISTS store_skus;
CREATE TABLE store_skus (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    product_id BIGINT NOT NULL COMMENT '平台商品ID',
    price DECIMAL(10,2) NOT NULL COMMENT '门店销售价格（独立定价）',
    stock INT NOT NULL DEFAULT 0 COMMENT '当前库存数量',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '上架状态：0-下架，1-上架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_store_product (store_id, product_id) COMMENT '门店+商品唯一约束',
    KEY idx_store_id (store_id),
    KEY idx_product_id (product_id),
    KEY idx_store_status (store_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='门店商品表（SKU）- 门店库存&价格';

-- ============================================================
-- 6. 订单表 (orders) ⭐核心表
-- 订单状态 & 自提码
-- ============================================================
DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号（唯一）',
    user_id BIGINT NOT NULL COMMENT '下单用户ID',
    store_id BIGINT NOT NULL COMMENT '履约门店ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status INT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付/1-待接单/2-已接单/3-待自提/4-已完成/5-已取消',
    pickup_code VARCHAR(16) DEFAULT NULL COMMENT '自提码（支付后生成，6位数字）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    accept_time DATETIME DEFAULT NULL COMMENT '门店接单时间',
    ready_time DATETIME DEFAULT NULL COMMENT '备货完成时间',
    complete_time DATETIME DEFAULT NULL COMMENT '订单完成时间',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no) COMMENT '订单编号唯一',
    KEY idx_pickup_code (pickup_code),
    KEY idx_user_id (user_id),
    KEY idx_store_id (store_id),
    KEY idx_status (status),
    KEY idx_store_status (store_id, status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- ============================================================
-- 7. 订单明细表 (order_items)
-- ============================================================
DROP TABLE IF EXISTS order_items;
CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    sku_id BIGINT NOT NULL COMMENT '门店商品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称（快照）',
    product_image VARCHAR(255) DEFAULT NULL COMMENT '商品图片（快照）',
    price DECIMAL(10,2) NOT NULL COMMENT '下单时单价（快照）',
    quantity INT NOT NULL COMMENT '购买数量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单明细表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 插入系统管理员账号（密码: admin123，BCrypt加密）
INSERT INTO users (username, password, nickname, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKhvKm7K', 'SystemAdmin', 'SYSTEM_ADMIN', 1);

-- 插入测试门店管理员（密码: store123）
INSERT INTO users (username, password, nickname, role, store_id, status) VALUES
('store1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKhvKm7K', 'Store1Admin', 'STORE_ADMIN', 1, 1);

-- 插入测试消费者（密码: user123）
INSERT INTO users (username, password, nickname, role, status) VALUES
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKhvKm7K', 'TestUser', 'CONSUMER', 1);

-- 插入默认商品分类
INSERT INTO categories (name, icon, sort, status) VALUES
('Puffed Snacks', NULL, 1, 1),
('Biscuits', NULL, 2, 1),
('Candy Chocolate', NULL, 3, 1),
('Nuts', NULL, 4, 1),
('Meat Snacks', NULL, 5, 1),
('Dried Fruits', NULL, 6, 1),
('Beverages', NULL, 7, 1),
('Instant Food', NULL, 8, 1);

-- 插入测试门店
INSERT INTO stores (name, address, phone, longitude, latitude, business_hours, status) VALUES
('Snack Shop Zhongguancun', 'No.1 Zhongguancun Street, Haidian, Beijing', '010-12345678', 116.3252, 39.9833, '09:00-22:00', 1),
('Snack Shop Wangjing', 'No.10 Wangjing Street, Chaoyang, Beijing', '010-87654321', 116.4716, 40.0020, '08:00-23:00', 1),
('Snack Shop Guomao', 'No.1 Jianguomenwai Street, Chaoyang, Beijing', '010-11112222', 116.4551, 39.9085, '10:00-22:00', 1);

-- 插入测试商品
INSERT INTO products (category_id, name, description, image, price, status) VALUES
(1, 'Lays Chips Original', 'Classic original flavor chips', 'https://example.com/chips1.jpg', 8.90, 1),
(1, 'Lays Chips Cucumber', 'Fresh cucumber flavor chips', 'https://example.com/chips2.jpg', 8.90, 1),
(2, 'Oreo Cookies', 'Classic sandwich cookies', 'https://example.com/oreo.jpg', 12.50, 1),
(3, 'Dove Chocolate', 'Silky milk chocolate', 'https://example.com/dove.jpg', 25.00, 1),
(4, 'Mixed Nuts', 'Daily mixed nuts pack', 'https://example.com/nuts.jpg', 39.90, 1),
(5, 'Pork Jerky', 'Original flavor pork jerky', 'https://example.com/pork.jpg', 28.00, 1),
(6, 'Dried Mango', 'Thai dried mango', 'https://example.com/mango.jpg', 19.90, 1),
(7, 'Mineral Water', 'Natural mineral water 550ml', 'https://example.com/water.jpg', 2.00, 1);

-- 插入门店商品（门店1的商品）
INSERT INTO store_skus (store_id, product_id, price, stock, status) VALUES 
(1, 1, 8.90, 100, 1),
(1, 2, 8.90, 80, 1),
(1, 3, 12.50, 50, 1),
(1, 4, 25.00, 30, 1),
(1, 5, 39.90, 40, 1),
(1, 6, 28.00, 25, 1),
(1, 7, 19.90, 60, 1),
(1, 8, 2.00, 200, 1);

-- 插入门店商品（门店2的商品，价格略有不同）
INSERT INTO store_skus (store_id, product_id, price, stock, status) VALUES 
(2, 1, 9.50, 120, 1),
(2, 2, 9.50, 90, 1),
(2, 3, 13.00, 60, 1),
(2, 4, 26.00, 35, 1),
(2, 5, 42.00, 45, 1);

-- ============================================================
-- 说明：
-- 1. 所有表使用InnoDB引擎，支持事务
-- 2. 字符集统一使用utf8mb4，支持emoji
-- 3. 主键使用BIGINT自增，支持大数据量
-- 4. 时间字段使用DATETIME，自动维护create_time和update_time
-- 5. 金额字段使用DECIMAL(10,2)，精确到分
-- 6. 状态字段使用TINYINT/INT，0表示禁用/下架，1表示启用/上架
-- 7. 逻辑外键不创建物理外键约束，由应用层保证数据一致性
-- 
-- 测试账号：
-- - 系统管理员: admin / admin123
-- - 门店管理员: store1 / store123 (绑定门店1)
-- - 普通用户: user1 / user123
-- ============================================================