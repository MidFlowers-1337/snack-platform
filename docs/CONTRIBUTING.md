# 🤝 贡献指南

> 感谢你对 Snack Platform 项目的关注！本指南将帮助你了解如何参与项目开发。

---

## 📋 目录

1. [行为准则](#行为准则)
2. [如何贡献](#如何贡献)
3. [开发流程](#开发流程)
4. [代码规范](#代码规范)
5. [提交规范](#提交规范)
6. [Pull Request 流程](#pull-request-流程)
7. [问题反馈](#问题反馈)

---

## 行为准则

参与本项目时，请遵守以下准则：

- 尊重所有参与者
- 接受建设性的批评
- 关注对社区最有利的事情
- 对其他社区成员表示同理心

---

## 如何贡献

### 贡献类型

| 类型 | 说明 | 难度 |
|------|------|------|
| 🐛 Bug 修复 | 修复已知问题 | ⭐ |
| 📝 文档改进 | 完善或修正文档 | ⭐ |
| ✨ 新功能 | 实现新的功能特性 | ⭐⭐⭐ |
| 🔧 代码优化 | 重构或优化现有代码 | ⭐⭐ |
| 🧪 测试用例 | 添加或改进测试 | ⭐⭐ |

### 适合新手的任务

查找标记为 `good first issue` 的 Issue，这些通常是：
- 简单的 Bug 修复
- 文档改进
- 小型功能增强

---

## 开发流程

### 1. Fork 仓库

点击 GitHub 页面右上角的 "Fork" 按钮，将仓库复制到你的账号下。

### 2. 克隆到本地

```bash
git clone https://github.com/YOUR_USERNAME/snack-platform.git
cd snack-platform
```

### 3. 添加上游仓库

```bash
git remote add upstream https://github.com/ORIGINAL_OWNER/snack-platform.git
```

### 4. 创建功能分支

```bash
# 同步最新代码
git fetch upstream
git checkout main
git merge upstream/main

# 创建新分支
git checkout -b feature/your-feature-name
```

### 5. 开发与测试

```bash
# 启动后端
cd snack-platform-backend
./mvnw spring-boot:run

# 启动前端
cd snack-platform-frontend
npm run dev

# 运行测试
./mvnw test  # 后端测试
npm run test # 前端测试（如有）
```

### 6. 提交代码

```bash
git add .
git commit -m "feat: add new feature description"
git push origin feature/your-feature-name
```

### 7. 创建 Pull Request

在 GitHub 上创建 Pull Request，等待代码审查。

---

## 代码规范

### 后端代码规范 (Java)

#### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | `OrderService`, `UserController` |
| 方法名 | camelCase | `createOrder()`, `getUserById()` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| 变量 | camelCase | `orderList`, `currentUser` |
| 包名 | 全小写 | `com.snackchain.module.order` |

#### 代码结构

```java
// 1. 包声明
package com.snackchain.snack_platform_backend.module.order.service.impl;

// 2. 导入语句（按字母顺序，分组）
import com.snackchain.snack_platform_backend.entity.Order;
import com.snackchain.snack_platform_backend.mapper.OrderMapper;
import org.springframework.stereotype.Service;

// 3. 类声明
@Service
public class OrderServiceImpl implements OrderService {
    
    // 4. 常量
    private static final int DEFAULT_PAGE_SIZE = 20;
    
    // 5. 成员变量
    private final OrderMapper orderMapper;
    
    // 6. 构造函数
    public OrderServiceImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }
    
    // 7. 公共方法
    @Override
    public Order getOrderById(Long id) {
        // 实现
    }
    
    // 8. 私有方法
    private void validateOrder(Order order) {
        // 实现
    }
}
```

#### 注释规范

```java
/**
 * 订单服务实现类
 * 
 * @author your-name
 * @since 1.0.0
 */
@Service
public class OrderServiceImpl implements OrderService {
    
    /**
     * 根据ID获取订单
     *
     * @param id 订单ID
     * @return 订单信息，不存在返回null
     * @throws BusinessException 当订单状态异常时抛出
     */
    @Override
    public Order getOrderById(Long id) {
        // TODO: 添加缓存支持
        return orderMapper.selectById(id);
    }
}
```

### 前端代码规范 (Vue.js)

#### 文件命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件文件 | PascalCase | `OrderList.vue`, `ProductCard.vue` |
| 工具文件 | camelCase | `request.js`, `formatDate.js` |
| 样式文件 | kebab-case | `order-list.css` |

#### 组件结构

```vue
<script setup>
// 1. 导入
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList } from '@/api/order'

// 2. Props 和 Emits
const props = defineProps({
  storeId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update', 'delete'])

// 3. 响应式数据
const loading = ref(false)
const orderList = ref([])

// 4. 计算属性
const totalAmount = computed(() => {
  return orderList.value.reduce((sum, order) => sum + order.amount, 0)
})

// 5. 方法
const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getOrderList(props.storeId)
    orderList.value = res.data
  } finally {
    loading.value = false
  }
}

const handleDelete = (id) => {
  emit('delete', id)
}

// 6. 生命周期
onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="order-list">
    <!-- 模板内容 -->
  </div>
</template>

<style scoped>
.order-list {
  /* 样式 */
}
</style>
```

### SQL 规范

```sql
-- 表名使用小写下划线
CREATE TABLE order_items (
    -- 主键放在第一位
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- 外键字段
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    
    -- 业务字段
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10,2) NOT NULL,
    
    -- 时间字段放在最后
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 索引定义
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';
```

---

## 提交规范

### Commit Message 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 类型

| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(order): add order cancel feature` |
| `fix` | Bug 修复 | `fix(auth): fix token expiration issue` |
| `docs` | 文档更新 | `docs: update API documentation` |
| `style` | 代码格式 | `style: format code with prettier` |
| `refactor` | 重构 | `refactor(user): simplify user service` |
| `test` | 测试 | `test(order): add order service tests` |
| `chore` | 构建/工具 | `chore: update dependencies` |

### Scope 范围

- `auth` - 认证模块
- `order` - 订单模块
- `product` - 商品模块
- `store` - 门店模块
- `sku` - 库存模块
- `report` - 报表模块
- `frontend` - 前端相关
- `backend` - 后端相关
- `docs` - 文档相关

### 示例

```bash
# 新功能
git commit -m "feat(order): add order export to Excel feature"

# Bug 修复
git commit -m "fix(auth): fix JWT token refresh issue

Token was not being refreshed correctly when expired.
Added automatic refresh logic in the interceptor.

Closes #123"

# 文档更新
git commit -m "docs: add API examples to README"
```

---

## Pull Request 流程

### PR 标题格式

```
[类型] 简短描述
```

示例：
- `[Feature] 添加订单导出功能`
- `[Fix] 修复登录Token过期问题`
- `[Docs] 更新API文档`

### PR 描述模板

```markdown
## 变更说明
简要描述本次变更的内容

## 变更类型
- [ ] 新功能 (feat)
- [ ] Bug 修复 (fix)
- [ ] 文档更新 (docs)
- [ ] 代码重构 (refactor)
- [ ] 测试 (test)
- [ ] 其他

## 相关 Issue
关联的 Issue 编号：#xxx

## 测试说明
描述如何测试这些变更

## 检查清单
- [ ] 代码已自测通过
- [ ] 已添加必要的测试用例
- [ ] 已更新相关文档
- [ ] 代码符合项目规范
```

### 代码审查要点

审查者会关注以下方面：

1. **功能正确性** - 代码是否实现了预期功能
2. **代码质量** - 是否符合代码规范
3. **测试覆盖** - 是否有足够的测试
4. **安全性** - 是否存在安全隐患
5. **性能** - 是否有性能问题
6. **文档** - 是否更新了相关文档

---

## 问题反馈

### 报告 Bug

创建 Issue 时请包含以下信息：

```markdown
## Bug 描述
清晰简洁地描述问题

## 复现步骤
1. 进入 '...'
2. 点击 '...'
3. 滚动到 '...'
4. 看到错误

## 期望行为
描述你期望发生的事情

## 实际行为
描述实际发生的事情

## 截图
如果适用，添加截图帮助解释问题

## 环境信息
- 操作系统: [例如 Windows 11]
- 浏览器: [例如 Chrome 120]
- 项目版本: [例如 1.0.0]

## 附加信息
添加任何其他相关信息
```

### 功能建议

```markdown
## 功能描述
清晰简洁地描述你想要的功能

## 使用场景
描述这个功能的使用场景

## 建议方案
如果有的话，描述你建议的实现方案

## 替代方案
描述你考虑过的替代方案

## 附加信息
添加任何其他相关信息或截图
```

---

## 开发资源

### 相关文档

- [快速开始指南](./QUICKSTART.md)
- [系统架构设计](./02-Architecture/Architecture.md)
- [REST API 文档](./03-API/openapi.md)
- [数据库设计](./04-DB/schema.md)

### 技术栈文档

- [Spring Boot 文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Vue.js 文档](https://vuejs.org/guide/introduction.html)
- [Element Plus 文档](https://element-plus.org/zh-CN/)
- [MyBatis-Plus 文档](https://baomidou.com/pages/24112f/)

---

## 联系方式

如有问题，可以通过以下方式联系：

- 提交 GitHub Issue
- 发送邮件至项目维护者

---

> 💡 **感谢你的贡献！** 每一个贡献都让这个项目变得更好。