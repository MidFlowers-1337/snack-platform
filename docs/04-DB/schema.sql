
-- ============================================================
-- 面向多门店协同的连锁零食电商平台 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_general_ci
-- 
-- 作者: Snack Platform Team
-- 版本: 1.0.0
-- 更新时间: 2025-12-13
-- ============================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS snack_platform 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;

USE snack_platform;

-- ============================================================
-- 删除已存在的表（按依赖顺序倒序删除）
-- ============================================================
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS store_skus;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS stores;

-- ============================================================
-- 1. 门店表 (stores)
-- 说明：存储连锁门店的基本信息，包括地理位置和营业状态
-- ============================================================
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
-- 2. 用户表 (users)
-- 说明：存储所有用户信息，包括消费者、门店管理员和系统管理员
-- 角色类型：CONSUMER-消费者, STORE_ADMIN-门店管理员, SYSTEM_ADMIN-系统管理员
-- ============================================================
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
-- 3. 商品分类表 (categories)
-- 说明：商品分类信息，支持排序和状态控制
-- ============================================================
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
-- 说明：平台统一管理的商品信息（SPU），包含建议零售价
-- ============================================================
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
-- 说明：门店级别的商品信息，支持门店独立定价和库存管理
-- 每个门店可以对同一商品设置不同的价格和库存
-- ============================================================
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
-- 说明：订单主表，记录订单基本信息和状态流转
-- 订单状态：0-待支付, 1-待接单, 2-已接单, 3-待自提, 4-已完成, 5-已取消
-- ============================================================
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
-- 说明：订单商品明细，记录下单时的商品快照信息
-- ============================================================
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
-- ============================================================
-- 初始化测试数据
-- ============================================================
-- ============================================================

-- ============================================================
-- 插入门店数据（3个门店：北京、上海、广州）
-- ============================================================
INSERT INTO stores (id, name, address, phone, image, longitude, latitude, business_hours, status) VALUES
(1, '零食优选·北京中关村店', '北京市海淀区中关村大街1号', '010-12345678', '/images/stores/beijing-zgc.jpg', 116.3252000, 39.9833000, '09:00-22:00', 1),
(2, '零食优选·上海南京路店', '上海市黄浦区南京东路100号', '021-87654321', '/images/stores/shanghai-njl.jpg', 121.4737000, 31.2304000, '08:30-22:30', 1),
(3, '零食优选·广州天河店', '广州市天河区天河路200号', '020-11112222', '/images/stores/guangzhou-th.jpg', 113.3245000, 23.1291000, '09:00-23:00', 1);

-- ============================================================
-- 插入用户数据（3个用户）
-- 密码统一为: 123456
-- BCrypt加密值: $2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQb9tLqbzTCzIIWjW0oRf7G0fKaG
-- ============================================================
INSERT INTO users (id, username, password, nickname, phone, avatar, role, store_id, status) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQb9tLqbzTCzIIWjW0oRf7G0fKaG', '系统管理员', '13800000001', '/images/avatars/admin.png', 'SYSTEM_ADMIN', NULL, 1),
(2, 'store1', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQb9tLqbzTCzIIWjW0oRf7G0fKaG', '北京店长', '13800000002', '/images/avatars/store1.png', 'STORE_ADMIN', 1, 1),
(3, 'user1', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQb9tLqbzTCzIIWjW0oRf7G0fKaG', '测试用户', '13800000003', '/images/avatars/user1.png', 'CONSUMER', NULL, 1);

-- ============================================================
-- 插入商品分类数据（5个分类）
-- ============================================================
INSERT INTO categories (id, name, icon, sort, status) VALUES
(1, '膨化食品', '/images/categories/puffed.png', 1, 1),
(2, '糖果', '/images/categories/candy.png', 2, 1),
(3, '饼干', '/images/categories/biscuit.png', 3, 1),
(4, '坚果', '/images/categories/nuts.png', 4, 1),
(5, '饮料', '/images/categories/beverage.png', 5, 1);

-- ============================================================
-- 插入商品数据（每个分类3-4个商品，共18个商品）
-- ============================================================

-- 膨化食品（分类ID: 1）
INSERT INTO products (id, category_id, name, description, image, price, status) VALUES
(1, 1, '乐事薯片原味', '经典原味薯片，香脆可口，休闲必备零食', '/images/products/lays-original.jpg', 8.90, 1),
(2, 1, '乐事薯片黄瓜味', '清新黄瓜味薯片，口感清爽，夏日首选', '/images/products/lays-cucumber.jpg', 8.90, 1),
(3, 1, '上好佳虾条', '经典虾味条，酥脆鲜香，童年回忆', '/images/products/oishi-shrimp.jpg', 6.50, 1),
(4, 1, '旺旺仙贝', '日式米饼，香脆可口，老少皆宜', '/images/products/wangwang-senbei.jpg', 9.90, 1);

-- 糖果（分类ID: 2）
INSERT INTO products (id, category_id, name, description, image, price, status) VALUES
(5, 2, '德芙丝滑牛奶巧克力', '丝滑牛奶巧克力，入口即化，浓郁醇香', '/images/products/dove-milk.jpg', 25.00, 1),
(6, 2, '徐福记酥心糖', '传统酥心糖，香甜酥脆，节日送礼佳品', '/images/products/xufuji-candy.jpg', 18.80, 1),
(7, 2, '阿尔卑斯棒棒糖', '经典棒棒糖，多种口味，儿童最爱', '/images/products/alps-lollipop.jpg', 12.00, 1),
(8, 2, '大白兔奶糖', '经典国民奶糖，浓郁奶香，怀旧经典', '/images/products/white-rabbit.jpg', 15.00, 1);

-- 饼干（分类ID: 3）
INSERT INTO products (id, category_id, name, description, image, price, status) VALUES
(9, 3, '奥利奥原味夹心饼干', '经典夹心饼干，扭一扭泡一泡，美味无限', '/images/products/oreo-original.jpg', 12.50, 1),
(10, 3, '好丽友派', '巧克力派，松软蛋糕夹心，下午茶首选', '/images/products/chocopie.jpg', 16.80, 1),
(11, 3, '嘉士利果乐果香', '水果夹心饼干，酸甜可口，营养美味', '/images/products/jiashili-fruit.jpg', 9.90, 1);

-- 坚果（分类ID: 4）
INSERT INTO products (id, category_id, name, description, image, price, status) VALUES
(12, 4, '三只松鼠每日坚果', '每日坚果混合装，营养均衡，健康零食', '/images/products/3squirrels-daily.jpg', 39.90, 1),
(13, 4, '百草味夏威夷果', '奶香夏威夷果，香脆可口，高端坚果', '/images/products/bcw-macadamia.jpg', 35.00, 1),
(14, 4, '洽洽瓜子原味', '精选葵花籽，颗粒饱满，越嗑越香', '/images/products/qiaqia-sunflower.jpg', 12.80, 1),
(15, 4, '良品铺子腰果', '越南腰果仁，香脆可口，营养丰富', '/images/products/lppz-cashew.jpg', 28.00, 1);

-- 饮料（分类ID: 5）
INSERT INTO products (id, category_id, name, description, image, price, status) VALUES
(16, 5, '农夫山泉矿泉水', '天然矿泉水550ml，甘甜可口，健康之选', '/images/products/nfsq-water.jpg', 2.00, 1),
(17, 5, '可口可乐', '经典可乐330ml，畅爽解渴，快乐源泉', '/images/products/coca-cola.jpg', 3.50, 1),
(18, 5, '元气森林气泡水', '0糖0脂气泡水，清爽解腻，健康饮品', '/images/products/genki-sparkling.jpg', 5.50, 1);

-- ============================================================
-- 插入门店商品库存数据（每个门店的商品SKU）
-- ============================================================

-- 北京中关村店（门店ID: 1）- 全部商品
INSERT INTO store_skus (id, store_id, product_id, price, stock, status) VALUES
(1, 1, 1, 8.90, 100, 1),
(2, 1, 2, 8.90, 80, 1),
(3, 1, 3, 6.50, 120, 1),
(4, 1, 4, 9.90, 90, 1),
(5, 1, 5, 25.00, 50, 1),
(6, 1, 6, 18.80, 60, 1),
(7, 1, 7, 12.00, 100, 1),
(8, 1, 8, 15.00, 80, 1),
(9, 1, 9, 12.50, 70, 1),
(10, 1, 10, 16.80, 55, 1),
(11, 1, 11, 9.90, 85, 1),
(12, 1, 12, 39.90, 40, 1),
(13, 1, 13, 35.00, 35, 1),
(14, 1, 14, 12.80, 150, 1),
(15, 1, 15, 28.00, 45, 1),
(16, 1, 16, 2.00, 300, 1),
(17, 1, 17, 3.50, 200, 1),
(18, 1, 18, 5.50, 150, 1);

-- 上海南京路店（门店ID: 2）- 部分商品，价格略有不同
INSERT INTO store_skus (id, store_id, product_id, price, stock, status) VALUES
(19, 2, 1, 9.50, 90, 1),
(20, 2, 2, 9.50, 75, 1),
(21, 2, 3, 7.00, 100, 1),
(22, 2, 4, 10.50, 80, 1),
(23, 2, 5, 26.00, 45, 1),
(24, 2, 6, 19.80, 55, 1),
(25, 2, 7, 12.50, 90, 1),
(26, 2, 8, 15.80, 70, 1),
(27, 2, 9, 13.00, 65, 1),
(28, 2, 10, 17.50, 50, 1),
(29, 2, 11, 10.50, 80, 1),
(30, 2, 12, 42.00, 35, 1),
(31, 2, 13, 36.00, 30, 1),
(32, 2, 14, 13.50, 140, 1),
(33, 2, 15, 29.00, 40, 1),
(34, 2, 16, 2.50, 280, 1),
(35, 2, 17, 4.00, 180, 1),
(36, 2, 18, 6.00, 130, 1);

-- 广州天河店（门店ID: 3）- 部分商品
INSERT INTO store_skus (id, store_id, product_id, price, stock, status) VALUES
(37, 3, 1, 8.50, 110, 1),
(38, 3, 2, 8.50, 95, 1),
(39, 3, 3, 6.00, 130, 1),
(40, 3, 4, 9.50, 100, 1),
(41, 3, 5, 24.00, 55, 1),
(42, 3, 6, 17.80, 65, 1),
(43, 3, 7, 11.50, 110, 1),
(44, 3, 8, 14.50, 85, 1),
(45, 3, 9, 12.00, 75, 1),
(46, 3, 10, 16.00, 60, 1),
(47, 3, 11, 9.50, 90, 1),
(48, 3, 12, 38.00, 45, 1),
(49, 3, 13, 33.00, 40, 1),
(50, 3, 14, 12.00, 160, 1),
(51, 3, 15, 27.00, 50, 1),
(52, 3, 16, 2.00, 320, 1),
(53, 3, 17, 3.50, 220, 1),
(54, 3, 18, 5.00, 160, 1);

-- ============================================================
-- 插入示例订单数据
-- ============================================================

-- 订单1：已完成订单（用户user1在北京店购买）
INSERT INTO orders (id, order_no, user_id, store_id, total_amount, status, pickup_code, remark, pay_time, accept_time, ready_time, complete_time, cancel_time, create_time, update_time) VALUES
(1, 'ORD20251210001', 3, 1, 46.30, 4, '123456', '请多给几张纸巾', '2025-12-10 10:05:00', '2025-12-10 10:08:00', '2025-12-10 10:15:00', '2025-12-10 10:30:00', NULL, '2025-12-10 10:00:00', '2025-12-10 10:30:00');

-- 订单1的商品明细
INSERT INTO order_items (id, order_id, sku_id, product_name, product_image, price, quantity, subtotal, create_time) VALUES
(1, 1, 1, '乐事薯片原味', '/images/products/lays-original.jpg', 8.90, 2, 17.80, '2025-12-10 10:00:00'),
(2, 1, 5, '德芙丝滑牛奶巧克力', '/images/products/dove-milk.jpg', 25.00, 1, 25.00, '2025-12-10 10:00:00'),
(3, 1, 17, '可口可乐', '/images/products/coca-cola.jpg', 3.50, 1, 3.50, '2025-12-10 10:00:00');

-- 订单2：待自提订单（用户user1在北京店购买）
INSERT INTO orders (id, order_no, user_id, store_id, total_amount, status, pickup_code, remark, pay_time, accept_time, ready_time, complete_time, cancel_time, create_time, update_time) VALUES
(2, 'ORD20251212001', 3, 1, 72.40, 3, '654321', NULL, '2025-12-12 14:05:00', '2025-12-12 14:10:00', '2025-12-12 14:20:00', NULL, NULL, '2025-12-12 14:00:00', '2025-12-12 14:20:00');

-- 订单2的商品明细
INSERT INTO order_items (id, order_id, sku_id, product_name, product_image, price, quantity, subtotal, create_time) VALUES
(4, 2, 12, '三只松鼠每日坚果', '/images/products/3squirrels-daily.jpg', 39.90, 1, 39.90, '2025-12-12 14:00:00'),
(5, 2, 9, '奥利奥原味夹心饼干', '/images/products/oreo-original.jpg', 12.50, 2, 25.00, '2025-12-12 14:00:00'),
(6, 2, 16, '农夫山泉矿泉水', '/images/products/nfsq-water.jpg', 2.00, 1, 2.00, '2025-12-12 14:00:00'),
(7, 2, 18, '元气森林气泡水', '/images/products/genki-sparkling.jpg', 5.50, 1, 5.50, '2025-12-12 14:00:00');

-- 订单3：待接单订单（用户user1在上海店购买）
INSERT INTO orders (id, order_no, user_id, store_id, total_amount, status, pickup_code, remark, pay_time, accept_time, ready_time, complete_time, cancel_time, create_time, update_time) VALUES
(3, 'ORD20251213001', 3, 2, 57.80, 1, '789012', '下班后来取', '2025-12-13 09:35:00', NULL, NULL, NULL, NULL, '2025-12-13 09:30:00', '2025-12-13 09:35:00');

-- 订单3的商品明细
INSERT INTO order_items (id, order_id, sku_id, product_name, product_image, price, quantity, subtotal, create_time) VALUES
(8, 3, 23, '德芙丝滑牛奶巧克力', '/images/products/dove-milk.jpg', 26.00, 1, 26.00, '2025-12-13 09:30:00'),
(9, 3, 26, '大白兔奶糖', '/images/products/white-rabbit.jpg', 15.80, 2, 31.60, '2025-12-13 09:30:00');

-- 订单4：已取消订单（用户user1在广州店购买）
INSERT INTO orders (id, order_no, user_id, store_id, total_amount, status, pickup_code, remark, pay_time, accept_time, ready_time, complete_time, cancel_time, create_time, update_time) VALUES
(4, 'ORD20251211001', 3, 3, 33.00, 5, NULL, '临时有事取消', NULL, NULL, NULL, NULL, '2025-12-11 16:30:00', '2025-12-11 16:00:00', '2025-12-11 16:30:00');

-- 订单4的商品明细
INSERT INTO order_items (id, order_id, sku_id, product_name, product_image, price, quantity, subtotal, create_time) VALUES
(10, 4, 49, '百草味夏威夷果', '/images/products/bcw-macadamia.jpg', 33.00, 1, 33.00, '2025-12-11 16:00:00');

-- ============================================================
-- 8. 购物车表 (cart_item)
-- 说明：存储用户在特定门店的购物车商品信息
-- ============================================================
CREATE TABLE cart_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车项ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID（门店商品ID）',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_store_sku (user_id, store_id, sku_id) COMMENT '用户+门店+SKU唯一约束',
    KEY idx_user_id (user_id) COMMENT '用户ID索引',
    KEY idx_store_id (store_id) COMMENT '门店ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='购物车表';

-- ============================================================
-- 9. 操作日志表 (operation_log)
-- 说明：记录系统关键业务操作，便于审计追踪
-- ============================================================
CREATE TABLE operation_log (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- ============================================================
-- ============================================================
-- 说明文档
-- ============================================================
-- ============================================================

-- ============================================================
-- 数据库设计说明
-- ============================================================
-- 1. 所有表使用InnoDB引擎，支持事务和外键
-- 2. 字符集统一使用utf8mb4，支持emoji和特殊字符
-- 3. 主键使用BIGINT自增，支持大数据量
-- 4. 时间字段使用DATETIME，自动维护create_time和update_time
-- 5. 金额字段使用DECIMAL(10,2)，精确到分
-- 6. 状态字段使用TINYINT/INT，0表示禁用/下架，1表示启用/上架
-- 7. 逻辑外键不创建物理外键约束，由应用层保证数据一致性
-- 8. 适当添加索引以优化查询性能

-- ============================================================
-- 测试账号信息
-- ============================================================
-- 所有账号密码统一为: 123456
--
-- | 用户名  | 密码   | 角色         | 说明                    |
-- |---------|--------|--------------|-------------------------|
-- | admin   | 123456 | SYSTEM_ADMIN | 系统管理员，可管理所有数据 |
-- | store1  | 123456 | STORE_ADMIN  | 门店管理员，绑定北京店    |
-- | user1   | 123456 | CONSUMER     | 普通消费者，用于测试下单  |

-- ============================================================
-- 订单状态说明
-- ============================================================
-- | 状态码 | 状态名称 | 说明                           |
-- |--------|----------|--------------------------------|
-- | 0      | 待支付   | 订单已创建，等待用户支付        |
-- | 1      | 待接单   | 用户已支付，等待门店接单        |
-- | 2      | 已接单   | 门店已接单，正在备货            |
-- | 3      | 待自提   | 备货完成，等待用户到店自提      |
-- | 4      | 已完成   | 用户已取货，订单完成            |
-- | 5      | 已取消   | 订单已取消（用户取消或超时取消）|

-- ============================================================
-- 门店数据说明
-- ============================================================
-- | 门店ID | 门店名称               | 城市   | 状态   |
-- |--------|------------------------|--------|--------|
-- | 1      | 零食优选·北京中关村店  | 北京   | 营业中 |
-- | 2      | 零食优选·上海南京路店  | 上海   | 营业中 |
-- | 3      | 零食优选·广州天河店    | 广州   | 营业中 |

-- ============================================================
-- 商品分类说明
-- ============================================================
-- | 分类ID | 分类名称 | 商品数量 |
-- |--------|----------|----------|
-- | 1      | 膨化食品 | 4        |
-- | 2      | 糖果     | 4        |
-- | 3      | 饼干     | 3        |
-- | 4      | 坚果     | 4        |
-- | 5      | 饮料     | 3        |

-- ============================================================
-- 示例订单说明
-- ============================================================
-- | 订单ID | 订单编号        | 门店     | 状态   | 金额   |
-- |--------|-----------------|----------|--------|--------|
-- | 1      | ORD20251210001  | 北京店   | 已完成 | 46.30  |
-- | 2      | ORD20251212001  | 北京店   | 待自提 | 72.40  |
-- | 3      | ORD20251213001  | 上海店   | 待接单 | 57.80  |
-- | 4      | ORD20251211001  | 广州店   | 已取消 | 33.00  |

-- ============================================================
-- END OF FILE
-- ============================================================