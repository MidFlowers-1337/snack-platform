# 修复报告（2026-02-17）

## 🎯 一句话结论

当前系统的主要问题不是单点代码错误，而是前后端接口契约不一致，已定位 13 个缺陷（Critical 2 个、High 6 个、Medium 5 个）。

## 🔍 审查范围

1. 前端接口层：`snack-platform-frontend/src/api/*.js`
2. 前端页面调用层：`snack-platform-frontend/src/views/**`
3. 后端控制器与服务层：`snack-platform-backend/src/main/java/**/controller/**`、`snack-platform-backend/src/main/java/**/service/**`
4. 数据库脚本与文档：`docs/04-DB/schema.sql`、`docs/04-DB/schema.md`、`docs/04-DB/promotion_and_points.sql`
5. 接口文档与用户手册：`docs/03-API/openapi.md`、`docs/07-UserManual/UserManual.md`

## 🚨 Critical（必须立即修复）

1. 核销链路三重冲突：核销码位数、请求参数形式、返回体结构不一致。证据：`snack-platform-frontend/src/views/store/Verify.vue:17`、`snack-platform-frontend/src/views/store/Verify.vue:30`、`snack-platform-frontend/src/views/store/Verify.vue:175`、`snack-platform-frontend/src/api/order.js:237`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/module/order/controller/StoreAdminOrderController.java:101`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/module/order/service/impl/OrderServiceImpl.java:374`。修复要求：统一为 6 位取货码，统一核销接口入参协议，统一成功返回结构。
2. 门店“确认订单”按钮调用了不存在的后端接口。证据：`snack-platform-frontend/src/api/order.js:225`、`snack-platform-frontend/src/views/store/Dashboard.vue:250`、`snack-platform-frontend/src/views/store/OrderList.vue:321`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/module/order/controller/StoreAdminOrderController.java:63`。修复要求：前端改为调用现有接单接口，或后端补齐 confirm 接口并更新文档。

## ⚠️ High（高优先修复）

1. 分页参数全链路错配，前端大量使用 `page/size`，后端约定 `pageNum/pageSize`。证据：`snack-platform-frontend/src/views/consumer/OrderList.vue:271`、`snack-platform-frontend/src/views/store/OrderList.vue:379`、`snack-platform-frontend/src/views/store/SkuList.vue:375`、`snack-platform-frontend/src/views/admin/ProductList.vue:346`、`snack-platform-frontend/src/views/admin/StoreList.vue:294`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/module/order/controller/OrderController.java:47`。修复要求：统一参数命名，或在请求层做兼容转换。
2. 管理端门店列表误用消费者接口，且管理员列表为硬编码假数据。证据：`snack-platform-frontend/src/views/admin/StoreList.vue:140`、`snack-platform-frontend/src/views/admin/StoreList.vue:298`、`snack-platform-frontend/src/api/store.js:6`、`snack-platform-frontend/src/api/store.js:38`、`snack-platform-frontend/src/views/admin/StoreList.vue:308`。修复要求：改为 `getAdminStores`，移除 mock 数据并接后端真实接口。
3. 门店订单筛选字段前端已传，但后端未实现，筛选实际失效。证据：`snack-platform-frontend/src/views/store/OrderList.vue:381`、`snack-platform-frontend/src/views/store/OrderList.vue:386`、`snack-platform-frontend/src/views/store/Verify.vue:212`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/module/order/controller/StoreAdminOrderController.java:36`。修复要求：后端新增 `orderNo/startDate/endDate` 过滤支持，或前端移除无效筛选项。
4. 门店仪表盘查询参数无效：复合状态字符串与低库存参数不被后端支持。证据：`snack-platform-frontend/src/views/store/Dashboard.vue:304`、`snack-platform-frontend/src/views/store/Dashboard.vue:313`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/module/order/controller/StoreAdminOrderController.java:126`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/module/sku/controller/StoreAdminSkuController.java:34`。修复要求：后端支持多状态和低库存筛选，或前端改为可支持的查询方式。
5. SKU 详情接口存在越权读取风险：公开路由可通过 `skuId` 读取非本店商品。证据：`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/module/sku/controller/StoreSkuController.java:52`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/module/sku/service/impl/StoreSkuServiceImpl.java:134`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/common/config/SecurityConfig.java:53`。修复要求：按 `storeId + skuId` 联合校验，查不到则返回 404。
6. 取货码未建立唯一约束，存在碰撞核销风险。证据：`docs/04-DB/schema.sql:150`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/module/order/service/impl/OrderServiceImpl.java:374`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/mapper/OrderMapper.java:27`。修复要求：增加唯一索引并在生成逻辑中处理重试。

## ℹ️ Medium（中优先修复）

1. 状态模型不一致：前端有 `REJECTED`，后端只存 `CANCELLED(5)`。证据：`snack-platform-frontend/src/utils/constants.js:13`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/enums/OrderStatus.java:15`。修复要求：统一状态定义，明确“拒单”和“取消单”是否合并。
2. 全局异常未显式处理类型转换异常，前端会看到“服务器错误”。证据：`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/common/exception/GlobalExceptionHandler.java:113`。修复要求：新增 `MethodArgumentTypeMismatchException` 处理，返回 400。
3. 数据库初始化文档与实际建表脚本分裂，容易漏建促销和积分表。证据：`README.md:88`、`docs/QUICKSTART.md:104`、`docs/08-Deployment/Deployment.md:216`、`docs/04-DB/promotion_and_points.sql:2`、`docs/04-DB/promotion_and_points.sql:20`。修复要求：统一初始化入口脚本，文档只保留单一执行路径。
4. 订单详情页面使用了不存在字段 `confirmTime`。证据：`snack-platform-frontend/src/views/consumer/OrderDetail.vue:34`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/entity/Order.java:64`。修复要求：改为 `acceptTime` 或后端补充字段并同步文档。
5. 存在死接口定义，未来易误用。证据：`snack-platform-frontend/src/api/store.js:27`、`snack-platform-frontend/src/api/order.js:175`。修复要求：删除未使用且不存在的接口封装，防止后续接入踩坑。

## 🧩 根因归纳

1. 接口契约管理缺失：前后端和文档各自演进，缺少统一来源和自动校验。
2. 页面层重复拼参数：同类分页、筛选在多个页面重复实现，导致批量偏差。
3. 回归测试不足：缺少跨端联调用例和契约测试，问题只能在运行期暴露。

## ✅ 推荐修复顺序

1. P0：先打通下单到核销主链路（核销契约 + 订单确认接口 + 分页参数统一）。
2. P1：修复管理端/门店端数据正确性（门店列表接口、筛选条件、仪表盘查询）。
3. P1：修复安全与一致性（SKU 越权、取货码唯一约束、状态模型统一）。
4. P2：修复异常处理与文档链路（400 语义化错误、初始化脚本合并、死接口清理）。

## 🧪 验证与当前状态

1. 前端构建验证：`npm -C snack-platform-frontend run build` 已通过。
2. 后端编译与自动化测试：当前环境缺少 Java/JAVA_HOME，未完成可执行验证。
3. 工作区已有候选修复改动，需做统一回归再决定是否合入。涉及：`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/module/order/controller/OrderController.java`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/module/order/controller/StoreAdminOrderController.java`、`snack-platform-frontend/src/api/order.js`、`snack-platform-backend/src/main/java/com/snackchain/snack_platform_backend/common/config/MyBatisPlusConfig.java`。

## 📌 报告结论

本次问题属于“契约错配型故障群”，建议以“统一契约 + 集中改造 + 一次性联调回归”方式处理，不建议逐页面零散修补。
