# 数据库设计文档

> **产品名称**：面向多门店协同的连锁零食电商平台  
> **文档版本**：v1.0  
> **数据库**：MySQL 8.0  
> **字符集**：utf8mb4  
> **排序规则**：utf8mb4_general_ci

---

## 目录

1. [数据库概览](#1-数据库概览)
2. [表结构设计](#2-表结构设计)
3. [索引设计](#3-索引设计)
4. [关联关系](#4-关联关系)

---

## 1. 数据库概览

### 1.1 表清单

| 序号 | 表名 | 中文名 | 说明 |
|------|------|--------|------|
| 1 | users | 用户表 | 存储所有用户信息（消费者/门店管理员/系统管理员） |
| 2 | user_addresses | 用户地址表 | 消费者收货地址 |
| 3 | stores | 门店表 | 门店基本信息 |
| 4 | categories | 商品分类表 | 平台商品分类 |
| 5 | products | 平台商品表 | 平台统一管理的商品（SPU） |
| 6 | store_products | 门店商品表 | 门店上架的商品，含库存和价格（SKU） |
| 7 | cart_items | 购物车表 | 消费者购物车 |
| 8 | orders | 订单表 | 订单主表 |
| 9 | order_items | 订单明细表 | 订单商品明细 |
| 10 | pickup_records | 核销记录表 | 自提核销记录 |

### 1.2 ER关系图

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   users     │       │   stores    │       │  categories │
│─────────────│       │─────────────│       │─────────────│
│ id (PK)     │◄──┐   │ id (PK)     │       │ id (PK)     │
│ username    │   │   │ name        │       │ name        │
│ role        │   │   │ admin_id(FK)│───┐   └──────┬──────┘
└──────┬──────┘   │   └──────┬──────┘   │          │
       │          │          │          │          │
       ▼          │          │          │          ▼
┌─────────────┐   │          │          │   ┌─────────────┐
│user_addresses│  │          │          │   │  products   │
│─────────────│   │          │          │   │─────────────│
│ user_id(FK) │───┘          │          └──▶│ category_id │
└─────────────┘              │              └──────┬──────┘
                             │                     │
                             ▼                     ▼
                      ┌─────────────┐       ┌─────────────┐
                      │store_products│◄─────│  (关联)     │
                      │─────────────│       └─────────────┘
                      │ store_id(FK)│
                      │ product_id  │
                      │ price       │  ◄── 门店独立定价
                      │ stock       │  ◄── 门店独立库存
                      └──────┬──────┘
                             │
       ┌─────────────────────┼─────────────────────┐
       │                     │                     │
       ▼                     ▼                     ▼
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│ cart_items  │       │   orders    │       │pickup_records│
│─────────────│       │─────────────│       │─────────────│
│ user_id     │       │ user_id     │       │ order_id(FK)│
│ store_id    │       │ store_id    │       │ pickup_code │
│store_product│       │ status      │  ◄── 订单状态
│             │       │ pickup_code │  ◄── 自提码
│             │       └──────┬──────┘
│             │              │
│             │              ▼
│             │       ┌─────────────┐
│             │       │ order_items │
│             │       │─────────────│
│             │       │ order_id(FK)│
│             │       │ product_id  │
│             │       │ price       │  ◄── 下单时价格快照
└─────────────┘       └─────────────┘
```

---

## 2. 表结构设计

### 2.1 users（用户表）

| 字段名 | 类型 | 允许空 | 默认值 | 说明 |
|--------|------|--------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) | NO | - | 用户名，唯一 |
| password | VARCHAR(100) | NO | - | 密码（BCrypt加密） |
| nickname | VARCHAR(50) | YES | NULL | 昵称 |
| phone | VARCHAR(20) | YES | NULL | 手机号 |
| avatar | VARCHAR(255) | YES | NULL | 头像URL |
| role | VARCHAR(20) | NO | 'CONSUMER' | 角色：CONSUMER/STORE_ADMIN/SYSTEM_ADMIN |
| status | TINYINT | NO | 1 | 状态：0-禁用，1-启用 |
| create_time | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**：
| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | 主键 | id | 主键索引 |
| uk_username | 唯一 | username | 用户名唯一索引 |
| idx_role | 普通 | role | 角色查询索引 |
| idx_phone | 普通 | phone | 手机号查询索引 |

---

### 2.2 user_addresses（用户地址表）

| 字段名 | 类型 | 允许空 | 默认值 | 说明 |
|--------|------|--------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NO | - | 用户ID |
| receiver_name | VARCHAR(50) | NO | - | 收货人姓名 |
| receiver_phone | VARCHAR(20) | NO | - | 收货人电话 |
| province | VARCHAR(50) | NO | - | 省份 |
| city | VARCHAR(50) | NO | - | 城市 |
| district | VARCHAR(50) | NO | - | 区县 |
| detail_address | VARCHAR(200) | NO | - | 详细地址 |
| is_default | TINYINT | NO | 0 | 是否默认：0-否，1-是 |
| create_time | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**：
| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | 主键 | id | 主键索引 |
| idx_user_id | 普通 | user_id | 用户ID索引 |

**关联**：
- `user_id` → `users.id`（逻辑外键，用户删除时级联删除地址）

---

### 2.3 stores（门店表）

| 字段名 | 类型 | 允许空 | 默认值 | 说明 |
|--------|------|--------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | 主键 |
| name | VARCHAR(100) | NO | - | 门店名称 |
| address | VARCHAR(255) | NO | - | 详细地址 |
| longitude | DECIMAL(10,7) | NO | - | 经度 |
| latitude | DECIMAL(10,7) | NO | - | 纬度 |
| phone | VARCHAR(20) | NO | - | 联系电话 |
| business_hours | VARCHAR(100) | NO | '09:00-22:00' | 营业时间 |
| status | TINYINT | NO | 1 | 状态：0-停业，1-营业 |
| admin_id | BIGINT | YES | NULL | 门店管理员ID |
| create_time | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**：
| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | 主键 | id | 主键索引 |
| idx_status | 普通 | status | 状态查询索引 |
| idx_admin_id | 普通 | admin_id | 管理员查询索引 |
| idx_location | 普通 | longitude, latitude | 位置查询索引 |

**关联**：
- `admin_id` → `users.id`（逻辑外键，门店管理员）

---

### 2.4 categories（商品分类表）

| 字段名 | 类型 | 允许空 | 默认值 | 说明 |
|--------|------|--------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | 主键 |
| name | VARCHAR(50) | NO | - | 分类名称 |
| icon | VARCHAR(255) | YES | NULL | 分类图标URL |
| sort_order | INT | NO | 0 | 排序权重，值越大越靠前 |
| status | TINYINT | NO | 1 | 状态：0-禁用，1-启用 |
| create_time | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**：
| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | 主键 | id | 主键索引 |
| idx_status_sort | 普通 | status, sort_order | 状态+排序复合索引 |

---

### 2.5 products（平台商品表 - SPU）

| 字段名 | 类型 | 允许空 | 默认值 | 说明 |
|--------|------|--------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | 主键 |
| name | VARCHAR(100) | NO | - | 商品名称 |
| description | TEXT | YES | NULL | 商品描述 |
| image | VARCHAR(255) | NO | - | 商品主图URL |
| images | TEXT | YES | NULL | 详情图列表（JSON数组） |
| category_id | BIGINT | NO | - | 分类ID |
| suggested_price | DECIMAL(10,2) | NO | - | 建议零售价 |
| min_price | DECIMAL(10,2) | YES | NULL | 最低限价（门店价格不得低于此值） |
| unit | VARCHAR(20) | NO | '件' | 计量单位 |
| spec | VARCHAR(100) | YES | NULL | 规格说明 |
| status | TINYINT | NO | 0 | 状态：0-禁用，1-启用 |
| create_time | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**：
| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | 主键 | id | 主键索引 |
| idx_category_id | 普通 | category_id | 分类查询索引 |
| idx_status | 普通 | status | 状态查询索引 |
| idx_name | 普通 | name | 名称查询索引（支持模糊搜索） |

**关联**：
- `category_id` → `categories.id`（逻辑外键）

---

### 2.6 store_products（门店商品表 - SKU）⭐重点

| 字段名 | 类型 | 允许空 | 默认值 | 说明 |
|--------|------|--------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | 主键 |
| store_id | BIGINT | NO | - | 门店ID |
| product_id | BIGINT | NO | - | 平台商品ID |
| price | DECIMAL(10,2) | NO | - | **门店销售价格**（独立定价） |
| stock | INT | NO | 0 | **当前库存数量** |
| locked_stock | INT | NO | 0 | 锁定库存（进阶：已下单未支付） |
| status | TINYINT | NO | 1 | 上架状态：0-下架，1-上架 |
| sort_order | INT | NO | 0 | 排序权重 |
| create_time | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**：
| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | 主键 | id | 主键索引 |
| **uk_store_product** | **唯一** | store_id, product_id | **门店+商品唯一约束** |
| idx_store_id | 普通 | store_id | 门店查询索引 |
| idx_product_id | 普通 | product_id | 商品查询索引 |
| idx_store_status | 普通 | store_id, status | 门店+状态复合索引 |

**关联**：
- `store_id` → `stores.id`（逻辑外键）
- `product_id` → `products.id`（逻辑外键）

**业务说明**：
- 每个门店可以独立设置商品价格（`price`字段）
- 每个门店独立管理库存（`stock`字段）
- `locked_stock`用于进阶库存锁定机制
- 可用库存 = `stock` - `locked_stock`

---

### 2.7 cart_items（购物车表）

| 字段名 | 类型 | 允许空 | 默认值 | 说明 |
|--------|------|--------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | 主键 |
| user_id | BIGINT | NO | - | 用户ID |
| store_id | BIGINT | NO | - | 门店ID |
| store_product_id | BIGINT | NO | - | 门店商品ID |
| quantity | INT | NO | 1 | 商品数量 |
| selected | TINYINT | NO | 1 | 是否选中：0-否，1-是 |
| create_time | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引**：
| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | 主键 | id | 主键索引 |
| uk_user_store_product | 唯一 | user_id, store_product_id | 用户+门店商品唯一 |
| idx_user_store | 普通 | user_id, store_id | 用户+门店复合索引 |

**关联**：
- `user_id` → `users.id`
- `store_id` → `stores.id`
- `store_product_id` → `store_products.id`

---

### 2.8 orders（订单表）⭐重点

| 字段名 | 类型 | 允许空 | 默认值 | 说明 |
|--------|------|--------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | 主键 |
| order_no | VARCHAR(32) | NO | - | 订单编号（唯一） |
| user_id | BIGINT | NO | - | 下单用户ID |
| store_id | BIGINT | NO | - | 履约门店ID |
| **status** | VARCHAR(20) | NO | 'PENDING_PAYMENT' | **订单状态**（见状态枚举） |
| total_amount | DECIMAL(10,2) | NO | - | 订单总金额 |
| pay_amount | DECIMAL(10,2) | NO | - | 实付金额 |
| **pickup_code** | VARCHAR(16) | YES | NULL | **自提码**（支付后生成） |
| remark | VARCHAR(500) | YES | NULL | 订单备注 |
| cancel_reason | VARCHAR(200) | YES | NULL | 取消原因 |
| pay_time | DATETIME | YES | NULL | 支付时间 |
| accept_time | DATETIME | YES | NULL | 门店接单时间 |
| ready_time | DATETIME | YES | NULL | 备货完成时间 |
| complete_time | DATETIME | YES | NULL | 订单完成时间 |
| cancel_time | DATETIME | YES | NULL | 取消时间 |
| create_time | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**订单状态枚举**：
| 状态值 | 说明 | 后续操作 |
|--------|------|----------|
| PENDING_PAYMENT | 待支付 | 支付/取消/超时取消 |
| PENDING_ACCEPT | 待接单 | 接单/拒单/取消/超时取消 |
| ACCEPTED | 已接单 | 备货完成 |
| READY_FOR_PICKUP | 待自提 | 核销/超时取消 |
| COMPLETED | 已完成 | 终态 |
| CANCELLED | 已取消 | 终态 |

**索引**：
| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | 主键 | id | 主键索引 |
| **uk_order_no** | **唯一** | order_no | 订单编号唯一索引 |
| **uk_pickup_code** | **唯一** | pickup_code | 自提码唯一索引 |
| idx_user_id | 普通 | user_id | 用户订单查询 |
| idx_store_id | 普通 | store_id | 门店订单查询 |
| idx_status | 普通 | status | 状态查询索引 |
| idx_store_status | 普通 | store_id, status | 门店+状态复合索引 |
| idx_create_time | 普通 | create_time | 时间范围查询 |

**关联**：
- `user_id` → `users.id`
- `store_id` → `stores.id`

---

### 2.9 order_items（订单明细表）

| 字段名 | 类型 | 允许空 | 默认值 | 说明 |
|--------|------|--------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | 主键 |
| order_id | BIGINT | NO | - | 订单ID |
| product_id | BIGINT | NO | - | 平台商品ID |
| store_product_id | BIGINT | NO | - | 门店商品ID |
| product_name | VARCHAR(100) | NO | - | 商品名称（快照） |
| product_image | VARCHAR(255) | NO | - | 商品图片（快照） |
| price | DECIMAL(10,2) | NO | - | 下单时单价（快照） |
| quantity | INT | NO | - | 购买数量 |
| subtotal | DECIMAL(10,2) | NO | - | 小计金额 |
| create_time | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |

**索引**：
| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | 主键 | id | 主键索引 |
| idx_order_id | 普通 | order_id | 订单查询索引 |

**关联**：
- `order_id` → `orders.id`
- `product_id` → `products.id`
- `store_product_id` → `store_products.id`

**说明**：
- `product_name`、`product_image`、`price` 为下单时的快照，防止商品信息变更影响历史订单

---

### 2.10 pickup_records（核销记录表）⭐重点

| 字段名 | 类型 | 允许空 | 默认值 | 说明 |
|--------|------|--------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | 主键 |
| order_id | BIGINT | NO | - | 订单ID（唯一，一个订单只能核销一次） |
| order_no | VARCHAR(32) | NO | - | 订单编号（冗余，便于查询） |
| store_id | BIGINT | NO | - | 核销门店ID |
| **pickup_code** | VARCHAR(16) | NO | - | **自提码** |
| operator_id | BIGINT | NO | - | 核销操作人ID（门店管理员） |
| operator_name | VARCHAR(50) | NO | - | 核销操作人姓名（冗余） |
| pickup_time | DATETIME | NO | - | 核销时间 |
| create_time | DATETIME | NO | CURRENT_TIMESTAMP | 创建时间 |

**索引**：
| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| PRIMARY | 主键 | id | 主键索引 |
| **uk_order_id** | **唯一** | order_id | 订单唯一（防止重复核销） |
| idx_store_id | 普通 | store_id | 门店核销记录查询 |
| idx_pickup_code | 普通 | pickup_code | 自提码查询 |
| idx_pickup_time | 普通 | pickup_time | 核销时间查询 |

**关联**：
- `order_id` → `orders.id`
- `store_id` → `stores.id`
- `operator_id` → `users.id`

**业务说明**：
- 核销时通过 `pickup_code` 查找订单
- `uk_order_id` 唯一约束确保一个订单只能核销一次
- 核销成功后同时更新 `orders.status` 为 `COMPLETED`

---

## 3. 索引设计

### 3.1 索引设计原则

1. **主键索引**：所有表使用自增BIGINT作为主键
2. **唯一索引**：业务唯一字段（用户名、订单号、自提码）
3. **外键索引**：所有外键字段建立索引
4. **复合索引**：高频查询条件组合（如 store_id + status）
5. **覆盖索引**：常用查询尽量使用索引覆盖

### 3.2 关键索引说明

| 表 | 索引 | 用途 |
|-----|------|------|
| users | uk_username | 登录时用户名查询 |
| store_products | uk_store_product | 确保门店商品唯一 |
| orders | uk_order_no | 订单号查询 |
| orders | uk_pickup_code | 自提码核销查询 |
| orders | idx_store_status | 门店订单列表查询 |
| pickup_records | uk_order_id | 防止重复核销 |

---

## 4. 关联关系

### 4.1 关联关系图

```
users (1) ──────────────────────────────────────┐
  │                                              │
  │ 1:N                                          │ 1:1
  ▼                                              ▼
user_addresses                                 stores
                                                 │
                                                 │ 1:N
                                                 ▼
categories (1) ──▶ products (1) ──▶ store_products
                                         │
                    ┌────────────────────┼────────────────────┐
                    │                    │                    │
                    ▼                    ▼                    ▼
              cart_items              orders              (库存扣减)
                                        │
                                        │ 1:N
                                        ▼
                                   order_items
                                        │
                                        │ 1:1
                                        ▼
                                  pickup_records
```

### 4.2 关联关系说明

| 主表 | 从表 | 关系 | 说明 |
|------|------|------|------|
| users | user_addresses | 1:N | 一个用户多个地址 |
| users | stores | 1:1 | 一个用户最多管理一个门店 |
| users | orders | 1:N | 一个用户多个订单 |
| stores | store_products | 1:N | 一个门店多个商品 |
| stores | orders | 1:N | 一个门店多个订单 |
| categories | products | 1:N | 一个分类多个商品 |
| products | store_products | 1:N | 一个商品在多个门店上架 |
| orders | order_items | 1:N | 一个订单多个商品 |
| orders | pickup_records | 1:1 | 一个订单一条核销记录 |

---

## 附录A：字段命名规范

| 规范 | 说明 | 示例 |
|------|------|------|
| 主键 | id | id |
| 外键 | 表名_id | user_id, store_id |
| 状态 | status | status |
| 时间 | xxx_time | create_time, pay_time |
| 金额 | xxx_amount 或 price | total_amount, price |
| 数量 | xxx 或 quantity | stock, quantity |
| 布尔 | is_xxx | is_default |

---

## 附录B：文档修订记录

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|----------|--------|
| v1.0 | 2024-XX-XX | 初始版本 | DBA |

---

> **文档说明**：本文档定义了系统的数据库表结构设计，开发团队应严格按照本文档创建数据库表。如有结构变更需求，须先更新本文档并经过评审。