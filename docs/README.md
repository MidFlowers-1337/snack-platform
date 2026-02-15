# Snack Platform 文档中心

> 面向多门店协同的连锁零食电商平台 - 技术文档

---

## 文档导航

### 需求与规划

| 文档 | 说明 | 适用读者 |
|------|------|----------|
| [产品需求文档 (PRD)](./01-Requirements/PRD.md) | 产品背景、目标、功能范围、里程碑 | 产品经理、开发团队 |
| [业务规则](./01-Requirements/BusinessRules.md) | 角色权限、订单状态机、库存策略 | 开发团队、测试团队 |
| [产品待办列表](./06-Plan/Backlog.md) | Epic、User Story、验收标准 | 产品经理、开发团队 |

### 架构与设计

| 文档 | 说明 | 适用读者 |
|------|------|----------|
| [系统架构设计](./02-Architecture/Architecture.md) | 技术选型、模块设计、核心流程 | 架构师、开发团队 |
| [数据库设计](./04-DB/schema.md) | 表结构、索引设计、关联关系 | 后端开发、DBA |
| [数据库脚本](./04-DB/schema.sql) | 建表 SQL 脚本 | 后端开发、DBA |
| [索引优化脚本](./04-DB/indexes.sql) | 性能索引补充 | 后端开发、DBA |
| [促销积分表结构](./04-DB/promotion_and_points.sql) | 促销活动与积分模块建表脚本 | 后端开发、DBA |

### 接口文档

| 文档 | 说明 | 适用读者 |
|------|------|----------|
| [REST API 文档](./03-API/openapi.md) | 所有 API 接口定义与示例 | 前端开发、后端开发 |

### 测试文档

| 文档 | 说明 | 适用读者 |
|------|------|----------|
| [测试计划与用例](./05-Test/TestPlan.md) | 测试策略、测试用例、执行计划 | 测试团队、开发团队 |

### 质量保证

| 文档 | 说明 | 适用读者 |
|------|------|----------|
| [项目质量评估](./qa/project-quality-assessment.md) | 代码质量、测试覆盖率评估 | 技术负责人 |
| [安全审查报告](./qa/security-review-report.md) | 安全漏洞分析与修复建议 | 安全工程师、开发团队 |

### 优化与演进

| 文档 | 说明 | 适用读者 |
|------|------|----------|
| [优化方案总览](./10-Optimization/OptimizationPlan.md) | 后端增强、前端 UI 重构、部署方案 | 架构师、开发团队 |

---

## 快速入口

### 我是新加入的开发者
1. 阅读 [产品需求文档](./01-Requirements/PRD.md) 了解项目背景
2. 阅读 [系统架构设计](./02-Architecture/Architecture.md) 了解技术方案
3. 参考 [快速开始指南](./QUICKSTART.md) 搭建开发环境
4. 查看 [贡献指南](./CONTRIBUTING.md) 了解开发规范

### 我是后端开发
1. 查看 [数据库设计](./04-DB/schema.md) 了解数据模型
2. 参考 [REST API 文档](./03-API/openapi.md) 了解接口规范
3. 阅读 [业务规则](./01-Requirements/BusinessRules.md) 了解业务逻辑
4. 查看 [后端 README](../snack-platform-backend/README.md) 了解项目结构

### 我是前端开发
1. 参考 [REST API 文档](./03-API/openapi.md) 了解接口调用
2. 阅读 [业务规则](./01-Requirements/BusinessRules.md) 了解业务流程
3. 查看 [前端 README](../snack-platform-frontend/README.md) 了解项目结构

### 我是测试工程师
1. 阅读 [测试计划与用例](./05-Test/TestPlan.md) 了解测试范围
2. 参考 [业务规则](./01-Requirements/BusinessRules.md) 设计测试场景
3. 查看 [产品待办列表](./06-Plan/Backlog.md) 了解验收标准

---

## 文档目录结构

```
docs/
├── README.md                    # 当前文档 - 文档索引
├── QUICKSTART.md               # 快速开始指南
├── CONTRIBUTING.md             # 贡献指南
├── 01-Requirements/            # 需求文档
│   ├── PRD.md                  # 产品需求文档
│   └── BusinessRules.md        # 业务规则
├── 02-Architecture/            # 架构文档
│   └── Architecture.md         # 系统架构设计
├── 03-API/                     # API 文档
│   └── openapi.md              # REST API 文档
├── 04-DB/                      # 数据库文档
│   ├── schema.md               # 数据库设计文档
│   ├── schema.sql              # 建表脚本
│   ├── indexes.sql             # 索引优化脚本
│   └── promotion_and_points.sql # 促销积分建表脚本
├── 05-Test/                    # 测试文档
│   └── TestPlan.md             # 测试计划与用例
├── 06-Plan/                    # 项目计划
│   └── Backlog.md              # 产品待办列表
├── 10-Optimization/            # 优化方案
│   └── OptimizationPlan.md     # 优化方案总览
└── qa/                         # 质量保证
    ├── project-quality-assessment.md
    └── security-review-report.md
```

---

## 相关链接

- [项目主页 README](../README.md)
- [后端项目 README](../snack-platform-backend/README.md)
- [前端项目 README](../snack-platform-frontend/README.md)

---

## 文档维护

### 文档版本
- 当前版本：v2.0
- 最后更新：2025年2月

### 文档规范
- 所有文档使用 Markdown 格式
- 文档标题使用中文，代码和技术术语保持英文
- 每个文档包含目录导航
- 重要概念使用表格或列表展示
