# 测试计划与用例

> **产品名称**：面向多门店协同的连锁零食电商平台  
> **文档版本**：v1.0  
> **编写日期**：2024年  
> **文档性质**：测试计划与测试用例文档

---

## 目录

1. [测试概述](#1-测试概述)
2. [测试范围](#2-测试范围)
3. [测试策略](#3-测试策略)
4. [权限隔离测试](#4-权限隔离测试)
5. [库存边界测试](#5-库存边界测试)
6. [订单状态机测试](#6-订单状态机测试)
7. [核销一次性测试](#7-核销一次性测试)
8. [幂等性测试](#8-幂等性测试)
9. [测试环境与执行计划](#9-测试环境与执行计划)

---

## 1. 测试概述

### 1.1 测试目标

本测试计划旨在验证"面向多门店协同的连锁零食电商平台"的核心业务功能正确性，重点关注：

- **权限隔离**：确保不同角色、不同门店之间的数据隔离
- **库存一致性**：确保库存扣减、回滚的正确性和边界处理
- **订单状态机**：确保订单状态流转符合业务规则
- **核销安全性**：确保自提码核销的一次性和安全性
- **幂等性**：确保重复操作不会产生副作用

### 1.2 测试类型

| 测试类型 | 说明 | 工具 |
|----------|------|------|
| 单元测试 | 测试Service层业务逻辑 | JUnit 5 + Mockito |
| 集成测试 | 测试API接口完整流程 | Spring Boot Test + MockMvc |
| 数据库测试 | 测试数据一致性 | H2内存数据库 / TestContainers |
| 并发测试 | 测试高并发场景 | JMeter / 多线程测试 |

### 1.3 测试用例格式

每条测试用例包含：
- **用例编号**：唯一标识
- **用例名称**：简要描述
- **前置条件**：执行测试前需要满足的条件
- **测试步骤**：详细的操作步骤
- **期望结果**：预期的系统行为
- **优先级**：P0（必须通过）/ P1（重要）/ P2（一般）

### 1.4 测试账号矩阵

| 账号 | 角色 | 绑定门店 | 用途 |
|------|------|----------|------|
| consumer1 | CONSUMER | - | 消费者测试 |
| store_admin_a | STORE_ADMIN | 门店A(id=1) | 门店A管理员测试 |
| store_admin_b | STORE_ADMIN | 门店B(id=2) | 门店B管理员测试 |
| system_admin | SYSTEM_ADMIN | - | 系统管理员测试 |

---

## 2. 测试范围

### 2.1 功能测试范围

| 模块 | 测试重点 | 优先级 |
|------|----------|--------|
| 认证模块 | 登录、注册、Token验证 | P0 |
| 门店模块 | 门店CRUD、状态管理 | P1 |
| 商品模块 | 商品CRUD、上下架 | P1 |
| 门店商品模块 | 库存管理、价格设置 | P0 |
| 订单模块 | 下单、支付、状态流转 | P0 |
| 核销模块 | 自提码生成、核销验证 | P0 |
| 统计模块 | 数据统计准确性 | P2 |

### 2.2 非功能测试范围

| 测试类型 | 测试内容 |
|----------|----------|
| 性能测试 | 接口响应时间 < 500ms |
| 并发测试 | 支持100并发下单 |
| 安全测试 | SQL注入、XSS防护 |

---

## 3. 测试策略

### 3.1 测试数据准备

```sql
-- 测试数据初始化脚本
INSERT INTO stores (id, name, address, status) VALUES 
(1, '门店A', '地址A', 1),
(2, '门店B', '地址B', 1);

INSERT INTO users (id, username, password, role, store_id) VALUES
(1, 'consumer1', 'xxx', 'CONSUMER', NULL),
(2, 'store_admin_a', 'xxx', 'STORE_ADMIN', 1),
(3, 'store_admin_b', 'xxx', 'STORE_ADMIN', 2),
(4, 'system_admin', 'xxx', 'SYSTEM_ADMIN', NULL);

INSERT INTO products (id, name, status) VALUES (1, '测试商品', 1);

INSERT INTO store_products (id, store_id, product_id, price, stock, status) VALUES
(1, 1, 1, 10.00, 100, 1),
(2, 2, 1, 11.00, 50, 1);
```

---

## 4. 权限隔离测试

### TC-AUTH-001：门店A管理员无法查看门店B订单

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-AUTH-001 |
| **用例名称** | 门店A管理员无法查看门店B订单 |
| **优先级** | P0 |
| **前置条件** | 1. 门店A、门店B均已创建<br>2. 门店A管理员账号已登录<br>3. 门店B存在订单（order_id=100, store_id=2） |
| **测试步骤** | 1. 使用门店A管理员Token<br>2. 调用 `GET /api/store/orders/100` |
| **期望结果** | 返回 403 Forbidden，错误信息："该订单不属于本店" |

```java
@Test
void testStoreAdminCannotAccessOtherStoreOrder() {
    // Given: 门店B的订单
    Order orderOfStoreB = createOrder(storeB.getId());
    
    // When: 门店A管理员尝试访问
    mockMvc.perform(get("/api/store/orders/" + orderOfStoreB.getId())
            .header("Authorization", "Bearer " + storeAdminAToken))
    // Then: 返回403
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403))
            .andExpect(jsonPath("$.message").value("该订单不属于本店"));
}
```

---

### TC-AUTH-002：门店A管理员无法修改门店B库存

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-AUTH-002 |
| **用例名称** | 门店A管理员无法修改门店B库存 |
| **优先级** | P0 |
| **前置条件** | 1. 门店B存在商品（store_product_id=2, store_id=2）<br>2. 门店A管理员账号已登录 |
| **测试步骤** | 1. 使用门店A管理员Token<br>2. 调用 `PUT /api/store/products/2/stock`<br>3. 请求体：`{"adjustment": 10, "reason": "补货"}` |
| **期望结果** | 返回 403 Forbidden，错误信息："无权操作该商品"，库存未变化 |

```java
@Test
void testStoreAdminCannotModifyOtherStoreStock() {
    // Given: 门店B的商品
    StoreProduct productOfStoreB = storeProductMapper.selectById(2L);
    int originalStock = productOfStoreB.getStock();
    
    // When: 门店A管理员尝试修改库存
    mockMvc.perform(put("/api/store/products/2/stock")
            .header("Authorization", "Bearer " + storeAdminAToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"adjustment\": 10, \"reason\": \"补货\"}"))
    // Then: 返回403
            .andExpect(status().isForbidden());
    
    // And: 库存未变化
    StoreProduct afterProduct = storeProductMapper.selectById(2L);
    assertEquals(originalStock, afterProduct.getStock());
}
```

---

### TC-AUTH-003：门店A管理员无法核销门店B订单

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-AUTH-003 |
| **用例名称** | 门店A管理员无法核销门店B订单 |
| **优先级** | P0 |
| **前置条件** | 1. 门店B存在待自提订单（pickup_code="ABCD1234"）<br>2. 门店A管理员账号已登录 |
| **测试步骤** | 1. 使用门店A管理员Token<br>2. 调用 `POST /api/store/pickup/verify`<br>3. 请求体：`{"pickupCode": "ABCD1234"}` |
| **期望结果** | 返回错误码 30005："该订单不属于本店" |

```java
@Test
void testStoreAdminCannotVerifyOtherStorePickup() {
    // Given: 门店B的待自提订单
    Order orderOfStoreB = createReadyForPickupOrder(storeB.getId());
    
    // When: 门店A管理员尝试核销
    mockMvc.perform(post("/api/store/pickup/verify")
            .header("Authorization", "Bearer " + storeAdminAToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"pickupCode\": \"" + orderOfStoreB.getPickupCode() + "\"}"))
    // Then: 返回错误
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(30005))
            .andExpect(jsonPath("$.message").value("该订单不属于本店"));
}
```

---

### TC-AUTH-004：消费者无法访问门店管理接口

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-AUTH-004 |
| **用例名称** | 消费者无法访问门店管理接口 |
| **优先级** | P0 |
| **前置条件** | 消费者账号已登录 |
| **测试步骤** | 1. 使用消费者Token<br>2. 调用 `GET /api/store/orders` |
| **期望结果** | 返回 403 Forbidden |

```java
@Test
void testConsumerCannotAccessStoreAdminApi() {
    mockMvc.perform(get("/api/store/orders")
            .header("Authorization", "Bearer " + consumerToken))
            .andExpect(status().isForbidden());
}
```

---

### TC-AUTH-005：门店管理员无法访问系统管理接口

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-AUTH-005 |
| **用例名称** | 门店管理员无法访问系统管理接口 |
| **优先级** | P0 |
| **前置条件** | 门店管理员账号已登录 |
| **测试步骤** | 1. 使用门店管理员Token<br>2. 调用 `POST /api/admin/products` |
| **期望结果** | 返回 403 Forbidden |

```java
@Test
void testStoreAdminCannotAccessSystemAdminApi() {
    mockMvc.perform(post("/api/admin/products")
            .header("Authorization", "Bearer " + storeAdminAToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\": \"新商品\"}"))
            .andExpect(status().isForbidden());
}
```

---

### TC-AUTH-006：消费者只能查看自己的订单

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-AUTH-006 |
| **用例名称** | 消费者只能查看自己的订单 |
| **优先级** | P0 |
| **前置条件** | 1. 消费者A创建了订单（order_id=1）<br>2. 消费者B已登录 |
| **测试步骤** | 1. 使用消费者B的Token<br>2. 调用 `GET /api/orders/1` |
| **期望结果** | 返回 404 Not Found 或 403 Forbidden |

```java
@Test
void testConsumerCanOnlyViewOwnOrders() {
    // Given: 消费者A的订单
    Order orderOfConsumerA = createOrder(consumerA.getId());
    
    // When: 消费者B尝试查看
    mockMvc.perform(get("/api/orders/" + orderOfConsumerA.getId())
            .header("Authorization", "Bearer " + consumerBToken))
    // Then: 返回404或403
            .andExpect(status().isNotFound());
}
```

---

## 5. 库存边界测试

### TC-STOCK-001：库存为0时无法下单

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-STOCK-001 |
| **用例名称** | 库存为0时无法下单 |
| **优先级** | P0 |
| **前置条件** | 1. 门店A商品库存为0<br>2. 消费者已登录 |
| **测试步骤** | 1. 调用 `POST /api/orders`<br>2. 请求体包含库存为0的商品 |
| **期望结果** | 返回错误码 20003："库存不足" |

```java
@Test
void testCannotOrderWhenStockIsZero() {
    // Given: 库存为0的商品
    storeProductMapper.updateStock(storeProductId, 0);
    
    // When: 尝试下单
    CreateOrderDTO dto = new CreateOrderDTO();
    dto.setStoreId(1L);
    dto.setItems(List.of(new OrderItemDTO(storeProductId, 1)));
    
    mockMvc.perform(post("/api/orders")
            .header("Authorization", "Bearer " + consumerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
    // Then: 返回库存不足
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(20003))
            .andExpect(jsonPath("$.message").value("库存不足"));
}
```

---

### TC-STOCK-002：购买数量超过库存时无法下单

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-STOCK-002 |
| **用例名称** | 购买数量超过库存时无法下单 |
| **优先级** | P0 |
| **前置条件** | 1. 门店A商品库存为10<br>2. 消费者已登录 |
| **测试步骤** | 1. 调用 `POST /api/orders`<br>2. 购买数量设为15 |
| **期望结果** | 返回错误码 20003："库存不足" |

```java
@Test
void testCannotOrderWhenQuantityExceedsStock() {
    // Given: 库存为10
    storeProductMapper.updateStock(storeProductId, 10);
    
    // When: 尝试购买15件
    CreateOrderDTO dto = new CreateOrderDTO();
    dto.setStoreId(1L);
    dto.setItems(List.of(new OrderItemDTO(storeProductId, 15)));
    
    mockMvc.perform(post("/api/orders")
            .header("Authorization", "Bearer " + consumerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
    // Then: 返回库存不足
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(20003));
}
```

---

### TC-STOCK-003：恰好购买全部库存成功

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-STOCK-003 |
| **用例名称** | 恰好购买全部库存成功 |
| **优先级** | P0 |
| **前置条件** | 1. 门店A商品库存为10<br>2. 消费者已登录 |
| **测试步骤** | 1. 调用 `POST /api/orders`<br>2. 购买数量设为10 |
| **期望结果** | 1. 下单成功<br>2. 库存变为0 |

```java
@Test
void testOrderExactlyAllStock() {
    // Given: 库存为10
    storeProductMapper.updateStock(storeProductId, 10);
    
    // When: 购买10件
    CreateOrderDTO dto = new CreateOrderDTO();
    dto.setStoreId(1L);
    dto.setItems(List.of(new OrderItemDTO(storeProductId, 10)));
    
    mockMvc.perform(post("/api/orders")
            .header("Authorization", "Bearer " + consumerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
    // Then: 下单成功
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    
    // And: 库存变为0
    StoreProduct product = storeProductMapper.selectById(storeProductId);
    assertEquals(0, product.getStock());
}
```

---

### TC-STOCK-004：订单取消后库存回滚

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-STOCK-004 |
| **用例名称** | 订单取消后库存回滚 |
| **优先级** | P0 |
| **前置条件** | 1. 门店A商品初始库存为100<br>2. 消费者已下单购买10件（库存变为90）<br>3. 订单状态为"待支付" |
| **测试步骤** | 1. 调用 `POST /api/orders/{id}/cancel`<br>2. 取消原因："不想要了" |
| **期望结果** | 1. 订单状态变为"已取消"<br>2. 库存回滚为100 |

```java
@Test
void testStockRollbackOnOrderCancel() {
    // Given: 初始库存100，下单10件后库存90
    storeProductMapper.updateStock(storeProductId, 100);
    Order order = createPendingPaymentOrder(10);
    
    StoreProduct beforeCancel = storeProductMapper.selectById(storeProductId);
    assertEquals(90, beforeCancel.getStock());
    
    // When: 取消订单
    mockMvc.perform(post("/api/orders/" + order.getId() + "/cancel")
            .header("Authorization", "Bearer " + consumerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"reason\": \"不想要了\"}"))
            .andExpect(status().isOk());
    
    // Then: 库存回滚为100
    StoreProduct afterCancel = storeProductMapper.selectById(storeProductId);
    assertEquals(100, afterCancel.getStock());
    
    // And: 订单状态为已取消
    Order cancelledOrder = orderMapper.selectById(order.getId());
    assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
}
```

---

### TC-STOCK-005：门店拒单后库存回滚

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-STOCK-005 |
| **用例名称** | 门店拒单后库存回滚 |
| **优先级** | P0 |
| **前置条件** | 1. 门店A商品初始库存为100<br>2. 消费者已下单并支付（库存变为90）<br>3. 订单状态为"待接单" |
| **测试步骤** | 1. 使用门店A管理员Token<br>2. 调用 `POST /api/store/orders/{id}/reject`<br>3. 拒单原因："商品缺货" |
| **期望结果** | 1. 订单状态变为"已取消"<br>2. 库存回滚为100 |

```java
@Test
void testStockRollbackOnStoreReject() {
    // Given: 待接单订单
    storeProductMapper.updateStock(storeProductId, 100);
    Order order = createPendingAcceptOrder(10);
    
    StoreProduct beforeReject = storeProductMapper.selectById(storeProductId);
    assertEquals(90, beforeReject.getStock());
    
    // When: 门店拒单
    mockMvc.perform(post("/api/store/orders/" + order.getId() + "/reject")
            .header("Authorization", "Bearer " + storeAdminAToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"reason\": \"商品缺货\"}"))
            .andExpect(status().isOk());
    
    // Then: 库存回滚为100
    StoreProduct afterReject = storeProductMapper.selectById(storeProductId);
    assertEquals(100, afterReject.getStock());
}
```

---

### TC-STOCK-006：支付超时后库存自动回滚

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-STOCK-006 |
| **用例名称** | 支付超时后库存自动回滚 |
| **优先级** | P0 |
| **前置条件** | 1. 门店A商品初始库存为100<br>2. 消费者已下单购买10件（库存变为90）<br>3. 订单创建时间超过30分钟 |
| **测试步骤** | 1. 触发定时任务扫描超时订单<br>2. 或调用超时处理方法 |
| **期望结果** | 1. 订单状态变为"已取消"<br>2. 库存回滚为100 |

```java
@Test
void testStockRollbackOnPaymentTimeout() {
    // Given: 超时的待支付订单
    storeProductMapper.updateStock(storeProductId, 100);
    Order order = createPendingPaymentOrder(10);
    // 模拟订单创建时间为31分钟前
    orderMapper.updateCreateTime(order.getId(), LocalDateTime.now().minusMinutes(31));
    
    // When: 执行超时处理
    orderTimeoutTask.handlePaymentTimeout();
    
    // Then: 库存回滚为100
    StoreProduct afterTimeout = storeProductMapper.selectById(storeProductId);
    assertEquals(100, afterTimeout.getStock());
    
    // And: 订单状态为已取消
    Order timeoutOrder = orderMapper.selectById(order.getId());
    assertEquals(OrderStatus.CANCELLED, timeoutOrder.getStatus());
}
```

---

### TC-STOCK-007：并发下单不超卖

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-STOCK-007 |
| **用例名称** | 并发下单不超卖 |
| **优先级** | P0 |
| **前置条件** | 门店A商品库存为10 |
| **测试步骤** | 1. 同时发起20个下单请求<br>2. 每个请求购买1件 |
| **期望结果** | 1. 最多10个订单创建成功<br>2. 其余返回库存不足<br>3. 最终库存为0（不为负数） |

```java
@Test
void testConcurrentOrderNoOversell() throws InterruptedException {
    // Given: 库存为10
    storeProductMapper.updateStock(storeProductId, 10);
    
    // When: 20个线程并发下单
    int threadCount = 20;
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);
    
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                boolean success = orderService.createOrder(createOrderDTO(1));
                if (success) successCount.incrementAndGet();
            } catch (BusinessException e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });
    }
    latch.await();
    
    // Then: 最多10个成功
    assertEquals(10, successCount.get());
    assertEquals(10, failCount.get());
    
    // And: 库存为0，不为负数
    StoreProduct product = storeProductMapper.selectById(storeProductId);
    assertEquals(0, product.getStock());
    assertTrue(product.getStock() >= 0);
}
```

---

## 6. 订单状态机测试

### TC-ORDER-001：待支付→待接单（支付成功）

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-ORDER-001 |
| **用例名称** | 待支付→待接单（支付成功） |
| **优先级** | P0 |
| **前置条件** | 订单状态为"待支付" |
| **测试步骤** | 调用 `POST /api/orders/{id}/pay` |
| **期望结果** | 1. 订单状态变为"待接单"<br>2. 生成自提码<br>3. 记录支付时间 |

```java
@Test
void testPaymentSuccess() {
    // Given: 待支付订单
    Order order = createPendingPaymentOrder(1);
    assertNull(order.getPickupCode());
    
    // When: 支付
    mockMvc.perform(post("/api/orders/" + order.getId() + "/pay")
            .header("Authorization", "Bearer " + consumerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.pickupCode").isNotEmpty());
    
    // Then: 状态变为待接单
    Order paidOrder = orderMapper.selectById(order.getId());
    assertEquals(OrderStatus.PENDING_ACCEPT, paidOrder.getStatus());
    assertNotNull(paidOrder.getPickupCode());
    assertNotNull(paidOrder.getPayTime());
}
```

---

### TC-ORDER-002：待接单→已接单（门店接单）

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-ORDER-002 |
| **用例名称** | 待接单→已接单（门店接单） |
| **优先级** | P0 |
| **前置条件** | 订单状态为"待接单"，属于门店A |
| **测试步骤** | 使用门店A管理员调用 `POST /api/store/orders/{id}/accept` |
| **期望结果** | 1. 订单状态变为"已接单"<br>2. 记录接单时间 |

```java
@Test
void testStoreAcceptOrder() {
    // Given: 待接单订单
    Order order = createPendingAcceptOrder(1);
    
    // When: 门店接单
    mockMvc.perform(post("/api/store/orders/" + order.getId() + "/accept")
            .header("Authorization", "Bearer " + storeAdminAToken))
            .andExpect(status().isOk());
    
    // Then: 状态变为已接单
    Order acceptedOrder = orderMapper.selectById(order.getId());
    assertEquals(OrderStatus.ACCEPTED, acceptedOrder.getStatus());
    assertNotNull(acceptedOrder.getAcceptTime());
}
```

---

### TC-ORDER-003：已接单→待自提（备货完成）

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-ORDER-003 |
| **用例名称** | 已接单→待自提（备货完成） |
| **优先级** | P0 |
| **前置条件** | 订单状态为"已接单"，属于门店A |
| **测试步骤** | 使用门店A管理员调用 `POST /api/store/orders/{id}/ready` |
| **期望结果** | 1. 订单状态变为"待自提"<br>2. 记录备货完成时间 |

```java
@Test
void testStoreMarkOrderReady() {
    Order order = createAcceptedOrder(1);
    mockMvc.perform(post("/api/store/orders/" + order.getId() + "/ready")
            .header("Authorization", "Bearer " + storeAdminAToken))
            .andExpect(status().isOk());
    Order readyOrder = orderMapper.selectById(order.getId());
    assertEquals(OrderStatus.READY_FOR_PICKUP, readyOrder.getStatus());
}
```

---

### TC-ORDER-004：待自提→已完成（核销成功）

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-ORDER-004 |
| **用例名称** | 待自提→已完成（核销成功） |
| **优先级** | P0 |
| **前置条件** | 订单状态为"待自提" |
| **测试步骤** | 使用门店管理员调用 `POST /api/store/pickup/verify`，提交自提码 |
| **期望结果** | 1. 订单状态变为"已完成"<br>2. 创建核销记录 |

---

### TC-ORDER-005：待支付状态不能接单（非法流转禁止）

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-ORDER-005 |
| **用例名称** | 待支付状态不能接单 |
| **优先级** | P0 |
| **前置条件** | 订单状态为"待支付" |
| **测试步骤** | 使用门店管理员调用 `POST /api/store/orders/{id}/accept` |
| **期望结果** | 返回错误码 30002："订单状态不允许此操作" |

```java
@Test
void testCannotAcceptPendingPaymentOrder() {
    Order order = createPendingPaymentOrder(1);
    mockMvc.perform(post("/api/store/orders/" + order.getId() + "/accept")
            .header("Authorization", "Bearer " + storeAdminAToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(30002));
}
```

---

### TC-ORDER-006：已完成订单不能取消（非法流转禁止）

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-ORDER-006 |
| **用例名称** | 已完成订单不能取消 |
| **优先级** | P0 |
| **前置条件** | 订单状态为"已完成" |
| **测试步骤** | 调用 `POST /api/orders/{id}/cancel` |
| **期望结果** | 返回错误码 30002："订单状态不允许此操作" |

```java
@Test
void testCannotCancelCompletedOrder() {
    Order order = createCompletedOrder(1);
    mockMvc.perform(post("/api/orders/" + order.getId() + "/cancel")
            .header("Authorization", "Bearer " + consumerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"reason\": \"不想要了\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(30002));
}
```

---

### TC-ORDER-007：已取消订单不能支付（非法流转禁止）

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-ORDER-007 |
| **用例名称** | 已取消订单不能支付 |
| **优先级** | P0 |
| **前置条件** | 订单状态为"已取消" |
| **测试步骤** | 调用 `POST /api/orders/{id}/pay` |
| **期望结果** | 返回错误码 30002："订单状态不允许此操作" |

---

### TC-ORDER-008：已接单状态不能核销（非法流转禁止）

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-ORDER-008 |
| **用例名称** | 已接单状态不能核销 |
| **优先级** | P0 |
| **前置条件** | 订单状态为"已接单"（未备货完成） |
| **测试步骤** | 使用门店管理员调用 `POST /api/store/pickup/verify` |
| **期望结果** | 返回错误码 30002："订单状态不允许核销" |

---

## 7. 核销一次性测试

### TC-PICKUP-001：自提码核销成功

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-PICKUP-001 |
| **用例名称** | 自提码核销成功 |
| **优先级** | P0 |
| **前置条件** | 订单状态为"待自提"，自提码有效 |
| **测试步骤** | 调用 `POST /api/store/pickup/verify`，提交正确的自提码 |
| **期望结果** | 1. 返回核销成功<br>2. 订单状态变为"已完成"<br>3. 创建核销记录 |

---

### TC-PICKUP-002：重复核销失败

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-PICKUP-002 |
| **用例名称** | 重复核销失败 |
| **优先级** | P0 |
| **前置条件** | 订单已核销完成，自提码已使用 |
| **测试步骤** | 再次调用 `POST /api/store/pickup/verify`，提交相同的自提码 |
| **期望结果** | 返回错误码 30004："订单已核销" |

```java
@Test
void testDuplicatePickupVerifyFail() {
    Order order = createReadyForPickupOrder(1);
    String pickupCode = order.getPickupCode();
    // 第一次核销成功
    pickupVerifyService.verify(pickupCode, storeAdminA.getId());
    // 再次核销
    mockMvc.perform(post("/api/store/pickup/verify")
            .header("Authorization", "Bearer " + storeAdminAToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"pickupCode\": \"" + pickupCode + "\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(30004))
            .andExpect(jsonPath("$.message").value("订单已核销"));
}
```

---

### TC-PICKUP-003：无效自提码核销失败

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-PICKUP-003 |
| **用例名称** | 无效自提码核销失败 |
| **优先级** | P0 |
| **前置条件** | 无 |
| **测试步骤** | 调用 `POST /api/store/pickup/verify`，提交不存在的自提码 |
| **期望结果** | 返回错误码 30003："自提码无效" |

---

### TC-PICKUP-004：并发核销只有一个成功

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-PICKUP-004 |
| **用例名称** | 并发核销只有一个成功 |
| **优先级** | P0 |
| **前置条件** | 订单状态为"待自提" |
| **测试步骤** | 同时发起10个核销请求，使用相同的自提码 |
| **期望结果** | 1. 只有1个请求成功<br>2. 其余返回"订单已核销"<br>3. 只创建1条核销记录 |

---

## 8. 幂等性测试

### TC-IDEMPOTENT-001：重复提交订单不重复创建

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-IDEMPOTENT-001 |
| **用例名称** | 重复提交订单不重复创建 |
| **优先级** | P0 |
| **前置条件** | 消费者已登录，商品库存充足 |
| **测试步骤** | 使用相同的幂等键，连续调用2次 `POST /api/orders` |
| **期望结果** | 1. 只创建1个订单<br>2. 库存只扣减1次<br>3. 第二次返回已存在的订单 |

---

### TC-IDEMPOTENT-002：重复支付回调不重复处理

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-IDEMPOTENT-002 |
| **用例名称** | 重复支付回调不重复处理 |
| **优先级** | P0 |
| **前置条件** | 订单状态为"待支付" |
| **测试步骤** | 连续调用2次支付成功回调 |
| **期望结果** | 1. 订单状态只变更1次<br>2. 自提码只生成1个 |

---

### TC-IDEMPOTENT-003：重复扣库存请求不重复扣减

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-IDEMPOTENT-003 |
| **用例名称** | 重复扣库存请求不重复扣减 |
| **优先级** | P0 |
| **前置条件** | 商品库存为100 |
| **测试步骤** | 使用相同的订单ID，连续调用2次库存扣减 |
| **期望结果** | 库存只扣减1次，最终为99 |

---

### TC-IDEMPOTENT-004：重复核销请求返回已核销

| 项目 | 内容 |
|------|------|
| **用例编号** | TC-IDEMPOTENT-004 |
| **用例名称** | 重复核销请求返回已核销 |
| **优先级** | P0 |
| **前置条件** | 订单已核销完成 |
| **测试步骤** | 再次调用核销接口 |
| **期望结果** | 返回错误码 30004，核销记录不重复创建 |

---

## 9. 测试环境与执行计划

### 9.1 测试环境配置

| 环境 | 用途 | 数据库 |
|------|------|--------|
| 本地开发 | 开发调试 | H2内存数据库 |
| 测试环境 | 集成测试 | MySQL测试库 |
| 预发布环境 | 验收测试 | MySQL预发布库 |

### 9.2 测试用例汇总

| 模块 | 用例数 | P0 | P1 | P2 |
|------|--------|----|----|----|
| 权限隔离 | 6 | 6 | 0 | 0 |
| 库存边界 | 7 | 7 | 0 | 0 |
| 订单状态机 | 8 | 8 | 0 | 0 |
| 核销一次性 | 4 | 4 | 0 | 0 |
| 幂等性 | 4 | 4 | 0 | 0 |
| **合计** | **29** | **29** | **0** | **0** |

### 9.3 测试执行计划

| 阶段 | 时间 | 测试内容 | 通过标准 |
|------|------|----------|----------|
| 单元测试 | Sprint 1-4 | Service层逻辑 | 覆盖率 > 80% |
| 集成测试 | Sprint 2-4 | API接口测试 | 全部P0用例通过 |
| 系统测试 | Sprint 4 | 端到端测试 | 全部功能可用 |
| 验收测试 | Sprint 4 | 用户验收 | 满足需求 |

---

## 附录：修订记录

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|----------|--------|
| v1.0 | 2024-XX-XX | 初始版本 | QA |

---

> **文档说明**：本文档定义了系统核心功能的测试计划和测试用例，重点覆盖权限隔离、库存边界、订单状态机、核销一次性和幂等性五大测试领域。所有P0级别用例必须在发布前全部通过。