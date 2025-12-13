# REST API 接口文档

> **产品名称**：面向多门店协同的连锁零食电商平台
> **文档版本**：v1.0
> **Base URL**：`http://localhost:8080/api`
> **认证方式**：JWT Bearer Token
>
> 📚 相关文档：[架构设计](../02-Architecture/Architecture.md) | [数据库设计](../04-DB/schema.md) | [业务规则](../01-Requirements/BusinessRules.md)

---

## 目录

1. [通用约定](#1-通用约定)
2. [认证模块 (Auth)](#2-认证模块-auth)
3. [门店模块 (Store)](#3-门店模块-store)
4. [商品模块 (Product)](#4-商品模块-product)
5. [门店商品模块 (StoreSku)](#5-门店商品模块-storesku)
6. [订单模块 (Order)](#6-订单模块-order)
7. [核销模块 (PickupVerify)](#7-核销模块-pickupverify)
8. [统计模块 (Report)](#8-统计模块-report)

---

## 1. 通用约定

### 1.1 统一响应格式

所有接口返回统一的JSON格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码 |
| message | String | 提示信息 |
| data | Object/Array | 响应数据（可为null） |

### 1.2 状态码定义

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录/Token无效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 1.3 业务错误码

| 错误码 | 说明 |
|--------|------|
| 10001 | 用户名已存在 |
| 10002 | 用户名或密码错误 |
| 10003 | 账号已被禁用 |
| 20001 | 商品不存在 |
| 20002 | 商品已下架 |
| 20003 | 库存不足 |
| 30001 | 订单不存在 |
| 30002 | 订单状态不允许此操作 |
| 30003 | 自提码无效 |
| 30004 | 订单已核销 |
| 30005 | 该订单不属于本店 |

### 1.4 认证方式

除公开接口外，所有接口需要在Header中携带Token：

```
Authorization: Bearer <token>
```

### 1.5 权限角色

| 角色 | 标识 | 说明 |
|------|------|------|
| 消费者 | CONSUMER | 普通用户 |
| 门店管理员 | STORE_ADMIN | 管理单个门店 |
| 系统管理员 | SYSTEM_ADMIN | 管理全平台 |

### 1.6 分页参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | Integer | 1 | 页码 |
| size | Integer | 20 | 每页条数 |

分页响应格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  }
}
```

---

## 2. 认证模块 (Auth)

### 2.1 用户注册

**POST** `/auth/register`

**权限**：公开

**请求体**：
```json
{
  "username": "zhangsan",
  "password": "123456",
  "nickname": "张三",
  "phone": "13800138000"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名，4-20位 |
| password | String | 是 | 密码，6-20位，需包含数字和字母 |
| nickname | String | 否 | 昵称 |
| phone | String | 否 | 手机号 |

**响应**：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "role": "CONSUMER",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**错误码**：
- 10001: 用户名已存在

---

### 2.2 用户登录

**POST** `/auth/login`

**权限**：公开

**请求体**：
```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "role": "CONSUMER",
    "storeId": null,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**错误码**：
- 10002: 用户名或密码错误
- 10003: 账号已被禁用

---

### 2.3 获取当前用户信息

**GET** `/auth/profile`

**权限**：登录用户

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "phone": "13800138000",
    "avatar": "https://xxx.com/avatar.jpg",
    "role": "CONSUMER",
    "storeId": null
  }
}
```

---

### 2.4 修改个人信息

**PUT** `/auth/profile`

**权限**：登录用户

**请求体**：
```json
{
  "nickname": "张三三",
  "phone": "13900139000",
  "avatar": "https://xxx.com/new-avatar.jpg"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

---

## 3. 门店模块 (Store)

### 3.1 获取门店列表（消费者）

**GET** `/stores`

**权限**：公开

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| longitude | Double | 否 | 经度（用于距离排序） |
| latitude | Double | 否 | 纬度（用于距离排序） |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "中关村店",
      "address": "北京市海淀区中关村大街1号",
      "phone": "010-12345678",
      "businessHours": "09:00-22:00",
      "status": 1,
      "distance": 1.5
    }
  ]
}
```

---

### 3.2 获取门店详情

**GET** `/stores/{id}`

**权限**：公开

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "中关村店",
    "address": "北京市海淀区中关村大街1号",
    "longitude": 116.310003,
    "latitude": 39.991957,
    "phone": "010-12345678",
    "businessHours": "09:00-22:00",
    "status": 1
  }
}
```

---

### 3.3 创建门店（管理员）

**POST** `/admin/stores`

**权限**：SYSTEM_ADMIN

**请求体**：
```json
{
  "name": "望京店",
  "address": "北京市朝阳区望京街道xxx",
  "longitude": 116.470098,
  "latitude": 40.002376,
  "phone": "010-87654321",
  "businessHours": "08:00-23:00"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2
  }
}
```

---

### 3.4 更新门店信息（管理员）

**PUT** `/admin/stores/{id}`

**权限**：SYSTEM_ADMIN

**请求体**：
```json
{
  "name": "望京旗舰店",
  "address": "北京市朝阳区望京街道xxx",
  "phone": "010-87654321",
  "businessHours": "08:00-24:00"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 3.5 更新门店状态（管理员）

**PUT** `/admin/stores/{id}/status`

**权限**：SYSTEM_ADMIN

**请求体**：
```json
{
  "status": 0
}
```

| 字段 | 说明 |
|------|------|
| status | 0-停业，1-营业 |

**响应**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 3.6 分配门店管理员（管理员）

**PUT** `/admin/stores/{id}/admin`

**权限**：SYSTEM_ADMIN

**请求体**：
```json
{
  "adminId": 5
}
```

**响应**：
```json
{
  "code": 200,
  "message": "分配成功",
  "data": null
}
```

---

## 4. 商品模块 (Product)

### 4.1 获取分类列表

**GET** `/categories`

**权限**：公开

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "膨化食品",
      "icon": "https://xxx.com/icon1.png",
      "sortOrder": 100
    },
    {
      "id": 2,
      "name": "饼干糕点",
      "icon": "https://xxx.com/icon2.png",
      "sortOrder": 90
    }
  ]
}
```

---

### 4.2 创建分类（管理员）

**POST** `/admin/categories`

**权限**：SYSTEM_ADMIN

**请求体**：
```json
{
  "name": "进口零食",
  "icon": "https://xxx.com/icon.png",
  "sortOrder": 50
}
```

**响应**：
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 9
  }
}
```

---

### 4.3 更新分类（管理员）

**PUT** `/admin/categories/{id}`

**权限**：SYSTEM_ADMIN

**请求体**：
```json
{
  "name": "进口零食精选",
  "icon": "https://xxx.com/new-icon.png",
  "sortOrder": 55,
  "status": 1
}
```

---

### 4.4 获取平台商品列表（管理员）

**GET** `/admin/products`

**权限**：SYSTEM_ADMIN

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 商品名称（模糊搜索） |
| categoryId | Long | 否 | 分类ID |
| status | Integer | 否 | 状态：0-禁用，1-启用 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页条数 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "乐事薯片原味",
        "image": "https://xxx.com/product1.jpg",
        "categoryId": 1,
        "categoryName": "膨化食品",
        "suggestedPrice": 8.50,
        "minPrice": 7.00,
        "unit": "袋",
        "status": 1,
        "storeCount": 5
      }
    ],
    "total": 50,
    "page": 1,
    "size": 20,
    "pages": 3
  }
}
```

---

### 4.5 创建平台商品（管理员）

**POST** `/admin/products`

**权限**：SYSTEM_ADMIN

**请求体**：
```json
{
  "name": "乐事薯片原味",
  "description": "经典原味薯片，香脆可口",
  "image": "https://xxx.com/product1.jpg",
  "images": ["https://xxx.com/detail1.jpg", "https://xxx.com/detail2.jpg"],
  "categoryId": 1,
  "suggestedPrice": 8.50,
  "minPrice": 7.00,
  "unit": "袋",
  "spec": "75g/袋"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1
  }
}
```

---

### 4.6 更新平台商品（管理员）

**PUT** `/admin/products/{id}`

**权限**：SYSTEM_ADMIN

**请求体**：
```json
{
  "name": "乐事薯片原味（新包装）",
  "description": "经典原味薯片，香脆可口，全新包装",
  "image": "https://xxx.com/product1-new.jpg",
  "suggestedPrice": 9.00,
  "minPrice": 7.50
}
```

---

### 4.7 更新商品状态（管理员）

**PUT** `/admin/products/{id}/status`

**权限**：SYSTEM_ADMIN

**请求体**：
```json
{
  "status": 1
}
```

| 字段 | 说明 |
|------|------|
| status | 0-禁用（所有门店自动下架），1-启用 |

---

## 5. 门店商品模块 (StoreSku)

### 5.1 获取门店商品列表（消费者）

**GET** `/stores/{storeId}/products`

**权限**：公开

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Long | 否 | 分类ID |
| keyword | String | 否 | 搜索关键词 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "name": "乐事薯片原味",
      "image": "https://xxx.com/product1.jpg",
      "price": 8.50,
      "stock": 100,
      "unit": "袋",
      "spec": "75g/袋",
      "status": 1
    }
  ]
}
```

---

### 5.2 获取门店商品详情（消费者）

**GET** `/stores/{storeId}/products/{id}`

**权限**：公开

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "productId": 1,
    "name": "乐事薯片原味",
    "description": "经典原味薯片，香脆可口",
    "image": "https://xxx.com/product1.jpg",
    "images": ["https://xxx.com/detail1.jpg"],
    "price": 8.50,
    "stock": 100,
    "unit": "袋",
    "spec": "75g/袋",
    "categoryId": 1,
    "categoryName": "膨化食品"
  }
}
```

---

### 5.3 获取本店商品列表（门店管理员）

**GET** `/store/products`

**权限**：STORE_ADMIN

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 状态：0-下架，1-上架 |
| keyword | String | 否 | 搜索关键词 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "name": "乐事薯片原味",
      "image": "https://xxx.com/product1.jpg",
      "suggestedPrice": 8.50,
      "price": 8.50,
      "stock": 100,
      "status": 1
    }
  ]
}
```

---

### 5.4 获取可上架商品列表（门店管理员）

**GET** `/store/products/available`

**权限**：STORE_ADMIN

**说明**：获取平台已启用但本店未上架的商品

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "productId": 5,
      "name": "奥利奥饼干",
      "image": "https://xxx.com/product5.jpg",
      "suggestedPrice": 12.00,
      "minPrice": 10.00,
      "categoryName": "饼干糕点"
    }
  ]
}
```

---

### 5.5 上架商品（门店管理员）

**POST** `/store/products`

**权限**：STORE_ADMIN

**请求体**：
```json
{
  "productId": 5,
  "price": 12.00,
  "stock": 50
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | Long | 是 | 平台商品ID |
| price | BigDecimal | 是 | 门店销售价格（不得低于最低限价） |
| stock | Integer | 是 | 初始库存 |

**响应**：
```json
{
  "code": 200,
  "message": "上架成功",
  "data": {
    "id": 10
  }
}
```

**错误码**：
- 20001: 商品不存在
- 400: 价格不能低于最低限价

---

### 5.6 更新商品价格（门店管理员）

**PUT** `/store/products/{id}/price`

**权限**：STORE_ADMIN

**请求体**：
```json
{
  "price": 11.50
}
```

**响应**：
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 5.7 调整库存（门店管理员）

**PUT** `/store/products/{id}/stock`

**权限**：STORE_ADMIN

**请求体**：
```json
{
  "adjustment": 20,
  "reason": "补货"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| adjustment | Integer | 是 | 调整数量（正数增加，负数减少） |
| reason | String | 是 | 调整原因 |

**响应**：
```json
{
  "code": 200,
  "message": "调整成功",
  "data": {
    "currentStock": 120
  }
}
```

---

### 5.8 下架商品（门店管理员）

**PUT** `/store/products/{id}/status`

**权限**：STORE_ADMIN

**请求体**：
```json
{
  "status": 0
}
```

---

## 6. 订单模块 (Order)

### 6.1 获取购物车

**GET** `/cart`

**权限**：CONSUMER

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "storeId": 1,
    "storeName": "中关村店",
    "items": [
      {
        "id": 1,
        "storeProductId": 1,
        "productName": "乐事薯片原味",
        "productImage": "https://xxx.com/product1.jpg",
        "price": 8.50,
        "quantity": 2,
        "stock": 100,
        "selected": true,
        "subtotal": 17.00
      }
    ],
    "totalAmount": 17.00,
    "totalQuantity": 2
  }
}
```

---

### 6.2 添加购物车

**POST** `/cart`

**权限**：CONSUMER

**请求体**：
```json
{
  "storeId": 1,
  "storeProductId": 1,
  "quantity": 1
}
```

**响应**：
```json
{
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

---

### 6.3 更新购物车商品数量

**PUT** `/cart/{id}`

**权限**：CONSUMER

**请求体**：
```json
{
  "quantity": 3
}
```

---

### 6.4 删除购物车商品

**DELETE** `/cart/{id}`

**权限**：CONSUMER

---

### 6.5 清空购物车

**DELETE** `/cart`

**权限**：CONSUMER

---

### 6.6 创建订单

**POST** `/orders`

**权限**：CONSUMER

**请求体**：
```json
{
  "storeId": 1,
  "items": [
    {
      "storeProductId": 1,
      "quantity": 2
    },
    {
      "storeProductId": 3,
      "quantity": 1
    }
  ],
  "remark": "少放辣"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "下单成功",
  "data": {
    "id": 1,
    "orderNo": "202401010001",
    "totalAmount": 29.50,
    "status": "PENDING_PAYMENT"
  }
}
```

**错误码**：
- 20002: 商品已下架
- 20003: 库存不足

---

### 6.7 订单支付

**POST** `/orders/{id}/pay`

**权限**：CONSUMER

**说明**：MVP阶段为模拟支付

**响应**：
```json
{
  "code": 200,
  "message": "支付成功",
  "data": {
    "orderNo": "202401010001",
    "pickupCode": "A1B2C3D4",
    "status": "PENDING_ACCEPT"
  }
}
```

---

### 6.8 获取订单列表（消费者）

**GET** `/orders`

**权限**：CONSUMER

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 否 | 订单状态 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页条数 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "orderNo": "202401010001",
        "storeName": "中关村店",
        "status": "PENDING_ACCEPT",
        "statusText": "待接单",
        "totalAmount": 29.50,
        "itemCount": 2,
        "firstProductImage": "https://xxx.com/product1.jpg",
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 10,
    "page": 1,
    "size": 20
  }
}
```

---

### 6.9 获取订单详情（消费者）

**GET** `/orders/{id}`

**权限**：CONSUMER

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "202401010001",
    "status": "READY_FOR_PICKUP",
    "statusText": "待自提",
    "totalAmount": 29.50,
    "payAmount": 29.50,
    "pickupCode": "A1B2C3D4",
    "remark": "少放辣",
    "store": {
      "id": 1,
      "name": "中关村店",
      "address": "北京市海淀区中关村大街1号",
      "phone": "010-12345678"
    },
    "items": [
      {
        "productName": "乐事薯片原味",
        "productImage": "https://xxx.com/product1.jpg",
        "price": 8.50,
        "quantity": 2,
        "subtotal": 17.00
      }
    ],
    "createTime": "2024-01-01 10:00:00",
    "payTime": "2024-01-01 10:05:00",
    "acceptTime": "2024-01-01 10:10:00",
    "readyTime": "2024-01-01 10:20:00"
  }
}
```

---

### 6.10 取消订单（消费者）

**POST** `/orders/{id}/cancel`

**权限**：CONSUMER

**请求体**：
```json
{
  "reason": "不想要了"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "取消成功",
  "data": null
}
```

**错误码**：
- 30002: 当前状态不允许取消

---

### 6.11 获取门店订单列表（门店管理员）

**GET** `/store/orders`

**权限**：STORE_ADMIN

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 否 | 订单状态 |
| orderNo | String | 否 | 订单编号 |
| page | Integer | 否 | 页码 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "orderNo": "202401010001",
        "status": "PENDING_ACCEPT",
        "statusText": "待接单",
        "totalAmount": 29.50,
        "itemCount": 2,
        "userName": "张三",
        "userPhone": "138****8000",
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 5
  }
}
```

---

### 6.12 接单（门店管理员）

**POST** `/store/orders/{id}/accept`

**权限**：STORE_ADMIN

**响应**：
```json
{
  "code": 200,
  "message": "接单成功",
  "data": null
}
```

**错误码**：
- 30005: 该订单不属于本店
- 30002: 订单状态不允许此操作

---

### 6.13 拒单（门店管理员）

**POST** `/store/orders/{id}/reject`

**权限**：STORE_ADMIN

**请求体**：
```json
{
  "reason": "商品缺货"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "拒单成功，已自动退款",
  "data": null
}
```

---

### 6.14 备货完成（门店管理员）

**POST** `/store/orders/{id}/ready`

**权限**：STORE_ADMIN

**响应**：
```json
{
  "code": 200,
  "message": "已通知顾客取货",
  "data": null
}
```

---

## 7. 核销模块 (PickupVerify)

### 7.1 获取自提码信息

**GET** `/orders/{id}/pickup-code`

**权限**：CONSUMER（仅限订单所有者）

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderNo": "202401010001",
    "pickupCode": "A1B2C3D4",
    "qrCode": "data:image/png;base64,iVBORw0KGgo...",
    "storeName": "中关村店",
    "storeAddress": "北京市海淀区中关村大街1号",
    "status": "READY_FOR_PICKUP",
    "statusText": "待自提"
  }
}
```

---

### 7.2 核销自提码（门店管理员）

**POST** `/store/pickup/verify`

**权限**：STORE_ADMIN

**请求体**：
```json
{
  "pickupCode": "A1B2C3D4"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "核销成功",
  "data": {
    "orderId": 1,
    "orderNo": "202401010001",
    "userName": "张三",
    "totalAmount": 29.50,
    "items": [
      {
        "productName": "乐事薯片原味",
        "quantity": 2
      }
    ],
    "pickupTime": "2024-01-01 15:30:00"
  }
}
```

**错误码**：
- 30003: 自提码无效
- 30004: 订单已核销
- 30005: 该订单不属于本店
- 30002: 订单状态不允许核销（需为READY_FOR_PICKUP）

---

### 7.3 获取核销记录（门店管理员）

**GET** `/store/pickup/records`

**权限**：STORE_ADMIN

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| date | String | 否 | 日期（YYYY-MM-DD），默认今天 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页条数 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "orderNo": "202401010001",
        "userName": "张三",
        "totalAmount": 29.50,
        "itemCount": 2,
        "pickupTime": "2024-01-01 15:30:00",
        "operatorName": "店员小王"
      }
    ],
    "total": 20,
    "todayCount": 5,
    "todayAmount": 150.00
  }
}
```

---

## 8. 统计模块 (Report)

### 8.1 门店销售统计（门店管理员）

**GET** `/store/stats/sales`

**权限**：STORE_ADMIN

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 是 | 开始日期（YYYY-MM-DD） |
| endDate | String | 是 | 结束日期（YYYY-MM-DD） |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summary": {
      "totalOrders": 150,
      "completedOrders": 140,
      "cancelledOrders": 10,
      "totalAmount": 4500.00,
      "averageOrderAmount": 32.14
    },
    "dailyStats": [
      {
        "date": "2024-01-01",
        "orderCount": 20,
        "amount": 650.00
      },
      {
        "date": "2024-01-02",
        "orderCount": 25,
        "amount": 780.00
      }
    ],
    "topProducts": [
      {
        "productId": 1,
        "productName": "乐事薯片原味",
        "salesCount": 50,
        "salesAmount": 425.00
      }
    ]
  }
}
```

---

### 8.2 门店库存预警（门店管理员）

**GET** `/store/stats/stock-warning`

**权限**：STORE_ADMIN

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| threshold | Integer | 否 | 预警阈值，默认10 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "productName": "乐事薯片原味",
      "image": "https://xxx.com/product1.jpg",
      "stock": 5,
      "avgDailySales": 8,
      "estimatedDays": 0.6
    },
    {
      "id": 3,
      "productId": 5,
      "productName": "奥利奥饼干",
      "image": "https://xxx.com/product5.jpg",
      "stock": 8,
      "avgDailySales": 3,
      "estimatedDays": 2.7
    }
  ]
}
```

---

### 8.3 平台数据概览（系统管理员）

**GET** `/admin/stats/overview`

**权限**：SYSTEM_ADMIN

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "today": {
      "orderCount": 120,
      "orderAmount": 3600.00,
      "newUsers": 15,
      "activeStores": 8
    },
    "total": {
      "userCount": 5000,
      "storeCount": 10,
      "productCount": 200,
      "orderCount": 15000
    },
    "recentTrend": [
      {
        "date": "2024-01-01",
        "orderCount": 100,
        "orderAmount": 3200.00
      }
    ]
  }
}
```

---

### 8.4 门店排行榜（系统管理员）

**GET** `/admin/stats/store-ranking`

**权限**：SYSTEM_ADMIN

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |
| sortBy | String | 否 | 排序字段：amount（默认）/count |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "rank": 1,
      "storeId": 1,
      "storeName": "中关村店",
      "orderCount": 500,
      "orderAmount": 15000.00,
      "completionRate": 95.5
    },
    {
      "rank": 2,
      "storeId": 2,
      "storeName": "望京店",
      "orderCount": 450,
      "orderAmount": 13500.00,
      "completionRate": 93.2
    }
  ]
}
```

---

### 8.5 门店对比分析（系统管理员）

**GET** `/admin/stats/store-comparison`

**权限**：SYSTEM_ADMIN

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| storeIds | String | 是 | 门店ID列表，逗号分隔 |
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "stores": [
      {
        "storeId": 1,
        "storeName": "中关村店",
        "metrics": {
          "orderCount": 500,
          "orderAmount": 15000.00,
          "avgOrderAmount": 30.00,
          "completionRate": 95.5,
          "productCount": 80
        }
      },
      {
        "storeId": 2,
        "storeName": "望京店",
        "metrics": {
          "orderCount": 450,
          "orderAmount": 13500.00,
          "avgOrderAmount": 30.00,
          "completionRate": 93.2,
          "productCount": 75
        }
      }
    ],
    "dailyComparison": [
      {
        "date": "2024-01-01",
        "store_1": { "orderCount": 20, "amount": 600.00 },
        "store_2": { "orderCount": 18, "amount": 540.00 }
      }
    ]
  }
}
```

---

## 附录A：接口汇总表

| 模块 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| **认证** | POST | /auth/register | 公开 | 用户注册 |
| | POST | /auth/login | 公开 | 用户登录 |
| | GET | /auth/profile | 登录用户 | 获取个人信息 |
| | PUT | /auth/profile | 登录用户 | 修改个人信息 |
| **门店** | GET | /stores | 公开 | 门店列表 |
| | GET | /stores/{id} | 公开 | 门店详情 |
| | POST | /admin/stores | SYSTEM_ADMIN | 创建门店 |
| | PUT | /admin/stores/{id} | SYSTEM_ADMIN | 更新门店 |
| | PUT | /admin/stores/{id}/status | SYSTEM_ADMIN | 更新门店状态 |
| | PUT | /admin/stores/{id}/admin | SYSTEM_ADMIN | 分配管理员 |
| **商品** | GET | /categories | 公开 | 分类列表 |
| | POST | /admin/categories | SYSTEM_ADMIN | 创建分类 |
| | PUT | /admin/categories/{id} | SYSTEM_ADMIN | 更新分类 |
| | GET | /admin/products | SYSTEM_ADMIN | 平台商品列表 |
| | POST | /admin/products | SYSTEM_ADMIN | 创建商品 |
| | PUT | /admin/products/{id} | SYSTEM_ADMIN | 更新商品 |
| | PUT | /admin/products/{id}/status | SYSTEM_ADMIN | 更新商品状态 |
| **门店商品** | GET | /stores/{storeId}/products | 公开 | 门店商品列表 |
| | GET | /stores/{storeId}/products/{id} | 公开 | 门店商品详情 |
| | GET | /store/products | STORE_ADMIN | 本店商品列表 |
| | GET | /store/products/available | STORE_ADMIN | 可上架商品 |
| | POST | /store/products | STORE_ADMIN | 上架商品 |
| | PUT | /store/products/{id}/price | STORE_ADMIN | 更新价格 |
| | PUT | /store/products/{id}/stock | STORE_ADMIN | 调整库存 |
| | PUT | /store/products/{id}/status | STORE_ADMIN | 下架商品 |
| **订单** | GET | /cart | CONSUMER | 获取购物车 |
| | POST | /cart | CONSUMER | 添加购物车 |
| | PUT | /cart/{id} | CONSUMER | 更新数量 |
| | DELETE | /cart/{id} | CONSUMER | 删除商品 |
| | DELETE | /cart | CONSUMER | 清空购物车 |
| | POST | /orders | CONSUMER | 创建订单 |
| | POST | /orders/{id}/pay | CONSUMER | 订单支付 |
| | GET | /orders | CONSUMER | 订单列表 |
| | GET | /orders/{id} | CONSUMER | 订单详情 |
| | POST | /orders/{id}/cancel | CONSUMER | 取消订单 |
| | GET | /store/orders | STORE_ADMIN | 门店订单列表 |
| | POST | /store/orders/{id}/accept | STORE_ADMIN | 接单 |
| | POST | /store/orders/{id}/reject | STORE_ADMIN | 拒单 |
| | POST | /store/orders/{id}/ready | STORE_ADMIN | 备货完成 |
| **核销** | GET | /orders/{id}/pickup-code | CONSUMER | 获取自提码 |
| | POST | /store/pickup/verify | STORE_ADMIN | 核销自提码 |
| | GET | /store/pickup/records | STORE_ADMIN | 核销记录 |
| **统计** | GET | /store/stats/sales | STORE_ADMIN | 门店销售统计 |
| | GET | /store/stats/stock-warning | STORE_ADMIN | 库存预警 |
| | GET | /admin/stats/overview | SYSTEM_ADMIN | 平台概览 |
| | GET | /admin/stats/store-ranking | SYSTEM_ADMIN | 门店排行 |
| | GET | /admin/stats/store-comparison | SYSTEM_ADMIN | 门店对比 |

---

## 附录B：修订记录

| 版本 | 日期 | 修订内容 | 作者 |
|------|------|----------|------|
| v1.0 | 2024-01-01 | 初始版本 | 架构师 |

---

> **文档结束**