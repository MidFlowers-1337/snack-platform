# 系统总体设计

> **产品名称**：面向多门店协同的连锁零食电商平台  
> **文档版本**：v1.0  
> **编写日期**：2024年  
> **文档性质**：系统架构设计文档

---

## 目录

1. [系统架构概览](#1-系统架构概览)
2. [模块划分与职责](#2-模块划分与职责)
3. [关键实体设计](#3-关键实体设计)
4. [关键流程时序](#4-关键流程时序)
5. [权限隔离实现方案](#5-权限隔离实现方案)
6. [库存一致性方案](#6-库存一致性方案)
7. [后端包结构设计](#7-后端包结构设计)

---

## 1. 系统架构概览

### 1.1 技术栈

| 层次 | 技术选型 | 说明 |
|------|----------|------|
| 前端框架 | Vue.js 3.x | 渐进式JavaScript框架 |
| UI组件库 | Element Plus | 基于Vue 3的组件库 |
| 后端框架 | Spring Boot 3.x | Java企业级开发框架 |
| 持久层 | MyBatis-Plus | ORM框架，简化数据库操作 |
| 数据库 | MySQL 8.0 | 关系型数据库 |
| 缓存 | Redis | 缓存、会话管理（可选） |
| 认证 | JWT | 无状态Token认证 |

### 1.2 系统架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              前端层                                      │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐         │
│  │   消费者端       │  │  门店管理端      │  │  系统管理端      │         │
│  │   (Vue.js)      │  │   (Vue.js)      │  │   (Vue.js)      │         │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘         │
└───────────┼─────────────────────┼─────────────────────┼─────────────────┘
            │                     │                     │
            └─────────────────────┼─────────────────────┘
                                  │ HTTP/HTTPS (RESTful API)
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                              网关层                                      │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    Spring Boot Application                       │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │   │
│  │  │ JWT Filter  │  │ CORS Filter │  │ Log Filter  │              │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘              │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                              业务层                                      │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │
│  │  Auth   │ │  Store  │ │ Product │ │StoreSku │ │  Order  │          │
│  │ Module  │ │ Module  │ │ Module  │ │ Module  │ │ Module  │          │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘          │
│  ┌─────────┐ ┌─────────┐                                              │
│  │ Pickup  │ │ Report  │                                              │
│  │ Verify  │ │ Module  │                                              │
│  └─────────┘ └─────────┘                                              │
└─────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                              数据层                                      │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                         MySQL 8.0                                │   │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐        │   │
│  │  │ users  │ │ stores │ │products│ │ orders │ │  ...   │        │   │
│  │  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘        │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.3 部署架构（MVP）

```
┌─────────────────────────────────────────────────────────────────┐
│                        单机部署                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Linux Server                          │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │   │
│  │  │   Nginx     │  │ Spring Boot │  │   MySQL     │      │   │
│  │  │  (前端静态)  │  │  (后端API)   │  │  (数据库)   │      │   │
│  │  │  Port:80    │  │  Port:8080  │  │  Port:3306  │      │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 模块划分与职责

### 2.1 模块总览

| 模块名称 | 模块标识 | 职责说明 | 核心功能 |
|----------|----------|----------|----------|
| 认证模块 | Auth | 用户认证与授权 | 注册、登录、JWT管理、权限校验 |
| 门店模块 | Store | 门店信息管理 | 门店CRUD、状态管理、管理员分配 |
| 商品模块 | Product | 平台商品管理 | 商品CRUD、分类管理、上下架 |
| 门店商品模块 | StoreSku | 门店库存价格管理 | 上架、定价、库存管理 |
| 订单模块 | Order | 订单全流程管理 | 下单、支付、接单、状态流转 |
| 核销模块 | PickupVerify | 自提核销管理 | 自提码生成、核销验证 |
| 统计模块 | Report | 数据统计分析 | 销售统计、门店对比、报表 |

### 2.2 模块详细说明

#### 2.2.1 Auth（认证模块）

**职责**：
- 用户注册与登录
- JWT Token生成与验证
- 角色权限管理
- 会话管理

**关键实体**：
- `User` - 用户信息
- `UserAddress` - 用户地址

**对外接口**：
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/logout` | POST | 用户登出 |
| `/api/auth/profile` | GET | 获取当前用户信息 |
| `/api/auth/profile` | PUT | 更新用户信息 |

---

#### 2.2.2 Store（门店模块）

**职责**：
- 门店信息的增删改查
- 门店状态管理（营业/停业）
- 门店管理员分配
- 门店列表查询（消费者端）

**关键实体**：
- `Store` - 门店信息

**对外接口**：
| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/stores` | GET | 获取门店列表 | 消费者/管理员 |
| `/api/stores/{id}` | GET | 获取门店详情 | 消费者/管理员 |
| `/api/admin/stores` | POST | 创建门店 | 系统管理员 |
| `/api/admin/stores/{id}` | PUT | 更新门店 | 系统管理员 |
| `/api/admin/stores/{id}/status` | PUT | 更新门店状态 | 系统管理员 |
| `/api/admin/stores/{id}/admin` | PUT | 分配门店管理员 | 系统管理员 |

---

#### 2.2.3 Product（商品模块）

**职责**：
- 商品分类管理
- 平台商品的增删改查
- 商品上下架管理
- 商品图片管理

**关键实体**：
- `Category` - 商品分类
- `Product` - 平台商品

**对外接口**：
| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/categories` | GET | 获取分类列表 | 公开 |
| `/api/admin/categories` | POST | 创建分类 | 系统管理员 |
| `/api/admin/categories/{id}` | PUT | 更新分类 | 系统管理员 |
| `/api/admin/products` | GET | 获取商品列表 | 系统管理员 |
| `/api/admin/products` | POST | 创建商品 | 系统管理员 |
| `/api/admin/products/{id}` | PUT | 更新商品 | 系统管理员 |
| `/api/admin/products/{id}/status` | PUT | 商品上下架 | 系统管理员 |

---

#### 2.2.4 StoreSku（门店商品模块）

**职责**：
- 门店商品上架/下架
- 门店商品定价
- 门店库存管理
- 消费者端商品展示

**关键实体**：
- `StoreProduct` - 门店商品（SKU）

**对外接口**：
| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/stores/{storeId}/products` | GET | 获取门店商品列表 | 消费者 |
| `/api/stores/{storeId}/products/{id}` | GET | 获取门店商品详情 | 消费者 |
| `/api/store/products` | GET | 获取本店商品列表 | 门店管理员 |
| `/api/store/products` | POST | 上架商品 | 门店管理员 |
| `/api/store/products/{id}` | PUT | 更新商品（价格/库存） | 门店管理员 |
| `/api/store/products/{id}/status` | PUT | 下架商品 | 门店管理员 |
| `/api/store/products/{id}/stock` | PUT | 调整库存 | 门店管理员 |

---

#### 2.2.5 Order（订单模块）

**职责**：
- 购物车管理
- 订单创建与支付
- 订单状态流转
- 门店订单处理（接单/拒单/备货）
- 订单取消与退款

**关键实体**：
- `CartItem` - 购物车项
- `Order` - 订单
- `OrderItem` - 订单明细

**对外接口**：
| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/cart` | GET | 获取购物车 | 消费者 |
| `/api/cart` | POST | 添加购物车 | 消费者 |
| `/api/cart/{id}` | PUT | 更新购物车项 | 消费者 |
| `/api/cart/{id}` | DELETE | 删除购物车项 | 消费者 |
| `/api/orders` | GET | 获取订单列表 | 消费者 |
| `/api/orders` | POST | 创建订单 | 消费者 |
| `/api/orders/{id}` | GET | 获取订单详情 | 消费者 |
| `/api/orders/{id}/pay` | POST | 订单支付 | 消费者 |
| `/api/orders/{id}/cancel` | POST | 取消订单 | 消费者 |
| `/api/store/orders` | GET | 获取门店订单列表 | 门店管理员 |
| `/api/store/orders/{id}/accept` | POST | 接单 | 门店管理员 |
| `/api/store/orders/{id}/reject` | POST | 拒单 | 门店管理员 |
| `/api/store/orders/{id}/ready` | POST | 备货完成 | 门店管理员 |

---

#### 2.2.6 PickupVerify（核销模块）

**职责**：
- 自提码生成
- 自提码验证
- 核销记录管理

**关键实体**：
- `PickupRecord` - 核销记录

**对外接口**：
| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/orders/{id}/pickup-code` | GET | 获取自提码 | 消费者 |
| `/api/store/verify` | POST | 核销验证 | 门店管理员 |
| `/api/store/verify/records` | GET | 核销记录列表 | 门店管理员 |

---

#### 2.2.7 Report（统计模块）

**职责**：
- 门店销售统计
- 全平台销售统计
- 门店业绩对比
- 库存预警

**关键实体**：
- 无独立实体，基于Order、StoreProduct聚合查询

**对外接口**：
| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/store/stats/today` | GET | 门店今日统计 | 门店管理员 |
| `/api/store/stats/stock-warning` | GET | 库存预警列表 | 门店管理员 |
| `/api/admin/stats/overview` | GET | 全平台统计概览 | 系统管理员 |
| `/api/admin/stats/stores` | GET | 门店业绩排名 | 系统管理员 |
| `/api/admin/stats/compare` | GET | 门店业绩对比 | 系统管理员 |

---

## 3. 关键实体设计

### 3.1 实体关系图（ER Diagram）

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   users     │       │   stores    │       │  products   │
│─────────────│       │─────────────│       │─────────────│
│ id (PK)     │       │ id (PK)     │       │ id (PK)     │
│ username    │◄──┐   │ name        │   ┌──▶│ name        │
│ password    │   │   │ address     │   │   │ category_id │──┐
│ role        │   │   │ admin_id(FK)│───┘   │ price       │  │
│ status      │   │   │ status      │       │ status      │  │
└──────┬──────┘   │   └──────┬──────┘       └─────────────┘  │
       │          │          │                               │
       │          │          │                               │
       ▼          │          ▼                               ▼
┌─────────────┐   │   ┌─────────────┐       ┌─────────────┐
│user_addresses│  │   │store_products│      │ categories  │
│─────────────│   │   │─────────────│       │─────────────│
│ id (PK)     │   │   │ id (PK)     │       │ id (PK)     │
│ user_id(FK) │───┘   │ store_id(FK)│       │ name        │
│ address     │       │ product_id  │       │ status      │
└─────────────┘       │ price       │       └─────────────┘
                      │ stock       │
       │              │ status      │
       │              └──────┬──────┘
       │                     │
       ▼                     ▼
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│ cart_items  │       │   orders    │       │pickup_records│
│─────────────│       │─────────────│       │─────────────│
│ id (PK)     │       │ id (PK)     │       │ id (PK)     │
│ user_id(FK) │       │ user_id(FK) │       │ order_id(FK)│
│ store_id(FK)│       │ store_id(FK)│       │ store_id(FK)│
│store_product│       │ status      │       │ pickup_code │
│ quantity    │       │ pickup_code │       │ operator_id │
└─────────────┘       │ total_amount│       │ pickup_time │
                      └──────┬──────┘       └─────────────┘
                             │
                             ▼
                      ┌─────────────┐
                      │ order_items │
                      │─────────────│
                      │ id (PK)     │
                      │ order_id(FK)│
                      │ product_id  │
                      │ price       │
                      │ quantity    │
                      └─────────────┘
```

### 3.2 核心实体字段设计

#### User（用户）
```java
public class User {
    private Long id;                    // 主键
    private String username;            // 用户名（唯一）
    private String password;            // 密码（BCrypt加密）
    private String nickname;            // 昵称
    private String phone;               // 手机号
    private String avatar;              // 头像URL
    private UserRole role;              // 角色：CONSUMER/STORE_ADMIN/SYSTEM_ADMIN
    private Integer status;             // 状态：0-禁用，1-启用
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}
```

#### Store（门店）
```java
public class Store {
    private Long id;                    // 主键
    private String name;                // 门店名称
    private String address;             // 详细地址
    private BigDecimal longitude;       // 经度
    private BigDecimal latitude;        // 纬度
    private String phone;               // 联系电话
    private String businessHours;       // 营业时间
    private Integer status;             // 状态：0-停业，1-营业
    private Long adminId;               // 门店管理员ID
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}
```

#### Product（平台商品）
```java
public class Product {
    private Long id;                    // 主键
    private String name;                // 商品名称
    private String description;         // 商品描述
    private String image;               // 主图URL
    private String images;              // 详情图（JSON数组）
    private Long categoryId;            // 分类ID
    private BigDecimal suggestedPrice;  // 建议零售价
    private BigDecimal minPrice;        // 最低限价
    private String unit;                // 计量单位
    private String spec;                // 规格说明
    private Integer status;             // 状态：0-禁用，1-启用
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}
```

#### StoreProduct（门店商品）
```java
public class StoreProduct {
    private Long id;                    // 主键
    private Long storeId;               // 门店ID
    private Long productId;             // 平台商品ID
    private BigDecimal price;           // 门店销售价格
    private Integer stock;              // 当前库存
    private Integer lockedStock;        // 锁定库存（进阶）
    private Integer status;             // 状态：0-下架，1-上架
    private Integer sortOrder;          // 排序权重
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}
```

#### Order（订单）
```java
public class Order {
    private Long id;                    // 主键
    private String orderNo;             // 订单编号
    private Long userId;                // 用户ID
    private Long storeId;               // 履约门店ID
    private OrderStatus status;         // 订单状态
    private BigDecimal totalAmount;     // 订单总金额
    private BigDecimal payAmount;       // 实付金额
    private String pickupCode;          // 自提码
    private String remark;              // 订单备注
    private String cancelReason;        // 取消原因
    private LocalDateTime payTime;      // 支付时间
    private LocalDateTime acceptTime;   // 接单时间
    private LocalDateTime readyTime;    // 备货完成时间
    private LocalDateTime completeTime; // 完成时间
    private LocalDateTime cancelTime;   // 取消时间
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}
```

---

## 4. 关键流程时序

### 4.1 下单→扣库存→门店接单→出货→核销 完整流程

```
┌────────┐     ┌────────┐     ┌────────┐     ┌────────┐     ┌────────┐
│ 消费者  │     │ 前端    │     │ 后端    │     │ 数据库  │     │门店管理员│
└───┬────┘     └───┬────┘     └───┬────┘     └───┬────┘     └───┬────┘
    │              │              │              │              │
    │ 1.提交订单   │              │              │              │
    │─────────────▶│              │              │              │
    │              │ 2.POST /orders              │              │
    │              │─────────────▶│              │              │
    │              │              │ 3.开启事务   │              │
    │              │              │─────────────▶│              │
    │              │              │ 4.校验库存   │              │
    │              │              │─────────────▶│              │
    │              │              │◀─────────────│              │
    │              │              │ 5.扣减库存   │              │
    │              │              │ UPDATE stock │              │
    │              │              │─────────────▶│              │
    │              │              │ 6.创建订单   │              │
    │              │              │ INSERT order │              │
    │              │              │─────────────▶│              │
    │              │              │ 7.提交事务   │              │
    │              │              │─────────────▶│              │
    │              │◀─────────────│              │              │
    │◀─────────────│ 8.返回订单   │              │              │
    │              │              │              │              │
    │ 9.支付订单   │              │              │              │
    │─────────────▶│              │              │              │
    │              │ 10.POST /orders/{id}/pay    │              │
    │              │─────────────▶│              │              │
    │              │              │ 11.更新状态  │              │
    │              │              │ →待接单      │              │
    │              │              │ 12.生成自提码│              │
    │              │              │─────────────▶│              │
    │              │◀─────────────│              │              │
    │◀─────────────│ 13.返回自提码│              │              │
    │              │              │              │              │
    │              │              │              │ 14.查看新订单│
    │              │              │              │◀─────────────│
    │              │              │◀─────────────│              │
    │              │              │ 15.接单      │              │
    │              │              │◀─────────────│──────────────│
    │              │              │ 16.更新状态  │              │
    │              │              │ →已接单      │              │
    │              │              │─────────────▶│              │
    │              │              │              │              │
    │              │              │ 17.备货完成  │              │
    │              │              │◀─────────────│──────────────│
    │              │              │ 18.更新状态  │              │
    │              │              │ →待自提      │              │
    │              │              │─────────────▶│              │
    │              │              │              │              │
    │ 19.到店出示自提码           │              │              │
    │─────────────────────────────────────────────────────────▶│
    │              │              │              │ 20.扫码核销  │
    │              │              │◀─────────────│──────────────│
    │              │              │ 21.校验自提码│              │
    │              │              │─────────────▶│              │
    │              │              │ 22.更新状态  │              │
    │              │              │ →已完成      │              │
    │              │              │ 23.记录核销  │              │
    │              │              │─────────────▶│              │
    │              │              │─────────────▶│──────────────│
    │◀─────────────────────────────────────────────────────────│
    │              │              │              │ 24.核销成功  │
    │              │              │              │              │
```

### 4.2 订单状态流转时序

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           订单状态流转                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────┐    支付成功    ┌──────────┐    门店接单    ┌──────────┐  │
│  │ 待支付   │───────────────▶│ 待接单   │───────────────▶│ 已接单   │  │
│  │ PENDING  │                │ PENDING  │                │ ACCEPTED │  │
│  │ PAYMENT  │                │ ACCEPT   │                │          │  │
│  └────┬─────┘                └────┬─────┘                └────┬─────┘  │
│       │                          │                            │        │
│       │ 用户取消/超时            │ 用户取消/拒单/超时          │ 备货完成│
│       ▼                          ▼                            ▼        │
│  ┌──────────┐              ┌──────────┐                ┌──────────┐   │
│  │ 已取消   │              │ 已取消   │                │ 待自提   │   │
│  │CANCELLED │              │CANCELLED │                │ READY_FOR│   │
│  │(库存回滚) │              │(退款+回滚)│                │ PICKUP   │   │
│  └──────────┘              └──────────┘                └────┬─────┘   │
│                                                             │         │
│                                                             │ 核销成功│
│                                                             ▼         │
│                                                       ┌──────────┐    │
│                                                       │ 已完成   │    │
│                                                       │COMPLETED │    │
│                                                       └──────────┘    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4.3 库存扣减时序（事务保证）

```
┌────────────┐     ┌────────────┐     ┌────────────┐
│ OrderService│    │StoreProduct│     │   MySQL    │
│            │     │  Mapper    │     │            │
└─────┬──────┘     └─────┬──────┘     └─────┬──────┘
      │                  │                  │
      │ 1.开启事务       │                  │
      │ @Transactional   │                  │
      │──────────────────│──────────────────│
      │                  │                  │
      │ 2.查询库存       │                  │
      │─────────────────▶│                  │
      │                  │ SELECT stock     │
      │                  │ WHERE id=?       │
      │                  │ FOR UPDATE       │
      │                  │─────────────────▶│
      │                  │◀─────────────────│
      │◀─────────────────│                  │
      │                  │                  │
      │ 3.校验库存>=购买数量                │
      │──────────────────│                  │
      │                  │                  │
      │ 4.扣减库存       │                  │
      │─────────────────▶│                  │
      │                  │ UPDATE stock=    │
      │                  │ stock-? WHERE    │
      │                  │ id=? AND stock>=?│
      │                  │─────────────────▶│
      │                  │◀─────────────────│
      │◀─────────────────│                  │
      │                  │                  │
      │ 5.创建订单       │                  │
      │──────────────────│──────────────────│
      │                  │                  │
      │ 6.提交事务       │                  │
      │──────────────────│──────────────────│
```

---

## 5. 权限隔离实现方案

### 5.1 RBAC权限模型

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           RBAC 权限模型                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────┐         ┌──────────┐         ┌──────────┐               │
│  │   用户   │────────▶│   角色   │────────▶│   权限   │               │
│  │  User    │  1:1    │   Role   │  1:N    │Permission│               │
│  └──────────┘         └──────────┘         └──────────┘               │
│                                                                         │
│  角色定义：                                                             │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │ CONSUMER     - 消费者：浏览商品、下单、查看自己订单              │   │
│  │ STORE_ADMIN  - 门店管理员：管理本店商品、处理本店订单            │   │
│  │ SYSTEM_ADMIN - 系统管理员：管理全平台商品、门店、用户            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5.2 接口权限控制

**基于注解的权限控制**：

```java
// 自定义权限注解
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    UserRole[] value();
}

// 使用示例
@RestController
@RequestMapping("/api/store")
@RequireRole(UserRole.STORE_ADMIN)
public class StoreProductController {
    
    @GetMapping("/products")
    public Result<List<StoreProductVO>> list() {
        // 自动校验角色权限
    }
}
```

### 5.3 数据隔离方案（store_id）

**核心原则**：门店管理员只能访问和操作自己门店的数据。

**实现方案**：

```java
// 1. 用户上下文 - 存储当前登录用户信息
public class UserContext {
    private Long userId;
    private String username;
    private UserRole role;
    private Long storeId;  // 门店管理员绑定的门店ID
}

// 2. Service层自动注入storeId
@Service
public class StoreOrderServiceImpl implements StoreOrderService {
    
    @Override
    public List<Order> getStoreOrders() {
        // 自动获取当前门店ID，确保数据隔离
        Long storeId = UserContextHolder.get().getStoreId();
        return orderMapper.selectByStoreId(storeId);
    }
    
    @Override
    public void acceptOrder(Long orderId) {
        Long storeId = UserContextHolder.get().getStoreId();
        Order order = orderMapper.selectById(orderId);
        
        // 校验订单归属
        if (!order.getStoreId().equals(storeId)) {
            throw new ForbiddenException("该订单不属于本店");
        }
        
        // 执行接单逻辑
        order.setStatus(OrderStatus.ACCEPTED);
        orderMapper.updateById(order);
    }
}
```

### 5.4 权限隔离矩阵

| 数据/操作 | 消费者 | 门店管理员 | 系统管理员 |
|-----------|--------|------------|------------|
| 用户自己的订单 | ✓ 读写 | ✗ | ✓ 只读 |
| 本店订单 | ✗ | ✓ 读写 | ✓ 只读 |
| 其他店订单 | ✗ | ✗ | ✓ 只读 |
| 本店商品库存 | ✗ | ✓ 读写 | ✗ |
| 平台商品 | ✓ 只读 | ✓ 只读 | ✓ 读写 |
| 门店信息 | ✓ 只读 | ✓ 只读(本店) | ✓ 读写 |

---

## 6. 库存一致性方案

### 6.1 MVP方案：下单即扣库存

**方案说明**：订单创建时立即扣减库存，订单取消时回滚库存。

**实现代码**：

```java
@Service
public class OrderServiceImpl implements OrderService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(CreateOrderDTO dto) {
        // 1. 校验并扣减库存（原子操作）
        for (OrderItemDTO item : dto.getItems()) {
            int affected = storeProductMapper.decreaseStock(
                item.getStoreProductId(),
                item.getQuantity()
            );
            if (affected == 0) {
                throw new BusinessException("商品库存不足");
            }
        }
        
        // 2. 创建订单
        Order order = buildOrder(dto);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        orderMapper.insert(order);
        
        return order;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        
        // 1. 回滚库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            storeProductMapper.increaseStock(
                item.getStoreProductId(),
                item.getQuantity()
            );
        }
        
        // 2. 更新订单状态
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        orderMapper.updateById(order);
    }
}
```

**Mapper层原子操作**：

```java
@Mapper
public interface StoreProductMapper {
    
    // 扣减库存（原子操作，防止超卖）
    @Update("UPDATE store_products SET stock = stock - #{qty} " +
            "WHERE id = #{id} AND stock >= #{qty}")
    int decreaseStock(@Param("id") Long id, @Param("qty") Integer qty);
    
    // 增加库存（订单取消时回滚）
    @Update("UPDATE store_products SET stock = stock + #{qty} " +
            "WHERE id = #{id}")
    int increaseStock(@Param("id") Long id, @Param("qty") Integer qty);
}
```

### 6.2 进阶方案：库存锁定机制

**方案说明**：下单时锁定库存，支付成功后正式扣减，超时则释放。

```sql
-- 数据库字段
ALTER TABLE store_products ADD COLUMN locked_stock INT DEFAULT 0;
-- 可用库存 = stock - locked_stock
```

```java
// 锁定库存
@Update("UPDATE store_products SET locked_stock = locked_stock + #{qty} " +
        "WHERE id = #{id} AND (stock - locked_stock) >= #{qty}")
int lockStock(@Param("id") Long id, @Param("qty") Integer qty);

// 确认扣减（支付成功）
@Update("UPDATE store_products SET stock = stock - #{qty}, " +
        "locked_stock = locked_stock - #{qty} WHERE id = #{id}")
int confirmStock(@Param("id") Long id, @Param("qty") Integer qty);

// 释放锁定（取消/超时）
@Update("UPDATE store_products SET locked_stock = locked_stock - #{qty} " +
        "WHERE id = #{id}")
int unlockStock(@Param("id") Long id, @Param("qty") Integer qty);
```

### 6.3 超时处理（定时任务）

```java
@Component
public class OrderTimeoutTask {
    
    @Scheduled(cron = "0 * * * * ?")  // 每分钟执行
    public void handlePaymentTimeout() {
        // 查询超过30分钟未支付的订单
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(30);
        List<Order> timeoutOrders = orderMapper.selectTimeoutOrders(
            OrderStatus.PENDING_PAYMENT, deadline
        );
        
        for (Order order : timeoutOrders) {
            orderService.cancelOrder(order.getId(), "支付超时");
        }
    }
}
```

---

## 7. 后端包结构设计

### 7.1 项目结构总览

```
snack-platform-backend/
├── src/main/java/com/snackchain/snack_platform_backend/
│   │
│   ├── SnackPlatformBackendApplication.java    # 启动类
│   │
│   ├── common/                                  # 公共模块
│   │   ├── config/                             # 配置类
│   │   │   ├── WebMvcConfig.java
│   │   │   ├── MyBatisPlusConfig.java
│   │   │   └── CorsConfig.java
│   │   ├── exception/                          # 异常处理
│   │   │   ├── BusinessException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── result/                             # 统一响应
│   │   │   ├── Result.java
│   │   │   └── ResultCode.java
│   │   └── util/                               # 工具类
│   │       ├── JwtUtil.java
│   │       └── PickupCodeUtil.java
│   │
│   ├── security/                                # 安全模块
│   │   ├── annotation/
│   │   │   └── RequireRole.java
│   │   ├── interceptor/
│   │   │   └── AuthInterceptor.java
│   │   └── context/
│   │       ├── UserContext.java
│   │       └── UserContextHolder.java
│   │
│   ├── module/                                  # 业务模块
│   │   ├── auth/                               # 认证模块
│   │   │   ├── controller/AuthController.java
│   │   │   ├── service/AuthService.java
│   │   │   ├── service/impl/AuthServiceImpl.java
│   │   │   ├── dto/LoginDTO.java
│   │   │   └── vo/LoginVO.java
│   │   │
│   │   ├── store/                              # 门店模块
│   │   │   ├── controller/
│   │   │   │   ├── StoreController.java
│   │   │   │   └── AdminStoreController.java
│   │   │   ├── service/StoreService.java
│   │   │   ├── dto/StoreDTO.java
│   │   │   └── vo/StoreVO.java
│   │   │
│   │   ├── product/                            # 商品模块
│   │   │   ├── controller/AdminProductController.java
│   │   │   ├── service/ProductService.java
│   │   │   ├── dto/ProductDTO.java
│   │   │   └── vo/ProductVO.java
│   │   │
│   │   ├── storesku/                           # 门店商品模块
│   │   │   ├── controller/
│   │   │   │   ├── StoreProductController.java
│   │   │   │   └── StoreAdminProductController.java
│   │   │   ├── service/StoreProductService.java
│   │   │   ├── dto/StockAdjustDTO.java
│   │   │   └── vo/StoreProductVO.java
│   │   │
│   │   ├── order/                              # 订单模块
│   │   │   ├── controller/
│   │   │   │   ├── CartController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   └── StoreOrderController.java
│   │   │   ├── service/
│   │   │   │   ├── CartService.java
│   │   │   │   └── OrderService.java
│   │   │   ├── dto/CreateOrderDTO.java
│   │   │   └── vo/OrderVO.java
│   │   │
│   │   ├── pickup/                             # 核销模块
│   │   │   ├── controller/PickupVerifyController.java
│   │   │   ├── service/PickupVerifyService.java
│   │   │   └── vo/VerifyResultVO.java
│   │   │
│   │   └── report/                             # 统计模块
│   │       ├── controller/
│   │       │   ├── StoreReportController.java
│   │       │   └── AdminReportController.java
│   │       ├── service/ReportService.java
│   │       └── vo/PlatformStatsVO.java
│   │
│   ├── entity/                                  # 实体类
│   │   ├── User.java
│   │   ├── Store.java
│   │   ├── Product.java
│   │   ├── StoreProduct.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   └── PickupRecord.java
│   │
│   ├── mapper/                                  # Mapper接口
│   │   ├── UserMapper.java
│   │   ├── StoreMapper.java
│   │   ├── ProductMapper.java
│   │   ├── StoreProductMapper.java
│   │   ├── OrderMapper.java
│   │   └── PickupRecordMapper.java
│   │
│   └── enums/                                   # 枚举类
│       ├── UserRole.java
│       ├── OrderStatus.java
│       └── CommonStatus.java
│
└── src/main/resources/
    ├── application.yaml
    ├── application-dev.yaml
    └── mapper/                                  # MyBatis XML
        └── OrderMapper.xml
```

### 7.2 分层职责说明

| 层次 | 包名 | 职责 |
|------|------|------|
| Controller | controller | 接收请求、参数校验、调用Service、返回响应 |
| Service | service | 业务逻辑处理、事务管理 |
| Mapper | mapper | 数据库访问、SQL执行 |
| Entity | entity | 数据库表映射实体 |
| DTO | dto | 接收前端请求参数 |
| VO | vo | 返回给前端的视图对象 |

### 7.3 命名规范

| 类型 | 命名规范 | 示例 |
|------|----------|------|
| Controller | XxxController | OrderController |
| Service接口 | XxxService | OrderService |
| Service实现 | XxxServiceImpl | OrderServiceImpl |
| Mapper | XxxMapper | OrderMapper |
| Entity | 单数名词 | Order, User |
| DTO | XxxDTO | CreateOrderDTO |
| VO | XxxVO | OrderVO |
| 枚举 | 单数名词 | OrderStatus |

---

## 附录A：API路径规范

| 前缀 | 说明 | 权限 |
|------|------|------|
| `/api/auth/*` | 认证相关 | 公开 |
| `/api/*` | 消费者端接口 | 消费者 |
| `/api/store/*` | 门店管理端接口 | 门店管理员 |
| `/api/admin/*` | 系统管理端接口 | 系统管理员 |

---

## 附录B：文档修订记录

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|----------|--------|
| v1.0 | 2024-XX-XX | 初始版本 | Architect |

---

> **文档说明**：本文档定义了系统的整体架构设计，包括模块划分、实体设计、关键流程、权限隔离和库存一致性方案。开发团队应严格按照本文档进行系统实现。