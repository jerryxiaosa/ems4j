# 能耗预付费管理系统前端

技术栈：`Vue 3 + TypeScript + Vite + Pinia + Vue Router + Axios + Vitest + Playwright`

这是 EMS 的前端管理台工程，目录位于 `frontend-web`。当前工程已经具备独立开发、单元测试和前端自带 mock 的 Playwright 冒烟回归能力。

## 1. 环境要求
- Node.js `>= 18.18.0`
- pnpm `10.32.1`

当前项目已在 `package.json` 中声明：

```json
{
  "packageManager": "pnpm@10.32.1",
  "engines": {
    "node": ">=18.18.0"
  }
}
```

## 2. 本地启动
在 `frontend-web` 目录执行：

```bash
pnpm install
pnpm dev
```

默认地址：`http://127.0.0.1:4173`

Vite 开发服务器配置：
- 监听地址：`0.0.0.0`
- 端口：`4173`
- 前端请求前缀：`/api`
- 默认代理目标：`http://localhost:8080`

如果本地后端不是 `8080`，可以在启动前覆盖代理目标：

```bash
VITE_PROXY_TARGET=http://127.0.0.1:18080 pnpm dev
```

当前代理规则在 `vite.config.ts` 中定义：
- `/api/** -> ${VITE_PROXY_TARGET}/**`
- 会移除 `/api` 前缀后再转发到后端

## 3. 常用命令
```bash
pnpm dev
pnpm build
pnpm preview
pnpm typecheck
pnpm test
pnpm test:unit
pnpm test:unit:coverage
pnpm test:e2e
```

说明：
- `pnpm test` 当前等价于 `pnpm test:unit`
- `pnpm build` 会先执行 `vue-tsc --noEmit` 再执行 Vite 生产构建
- `pnpm test:e2e` 运行 Playwright 冒烟测试

## 4. 当前工程结构
当前目录已经从纯技术分层，演进为“基础设施 + 模块化增强”的结构。

```text
src/
  api/
    raw/                 # 直接贴近后端接口契约
    adapters/            # 面向页面的数据归一与字段适配
  components/            # 通用组件与业务弹窗组件
  composables/           # 通用组合逻辑
  directives/            # 全局指令，如 v-menu-permission
  layouts/               # 主布局
  modules/               # 按业务模块收口的类型、composable、子组件
    devices/
      electric-meters/
        components/
        composables/
        types.ts
    system/
      users/
        composables/
        types.ts
      roles/
        types.ts
      menus/
        types.ts
      organizations/
        types.ts
      spaces/
        types.ts
  router/
    modules/             # 按业务拆分的路由声明
    guard.ts             # 可测试的路由守卫逻辑
    index.ts             # 路由汇总与装配
  stores/
    auth.ts
    permission.ts
  styles/
  types/                 # 仍保留跨模块共享类型
  utils/
  views/                 # 页面级视图，当前主要负责装配
```

### 4.1 当前分层约定
- `api/raw`：请求后端接口，不做页面语义包装
- `api/adapters`：负责 envelope 解包、分页归一、字段格式化、VO 转页面模型
- `views`：页面装配层，尽量不堆业务逻辑
- `modules/**/composables`：页面级业务逻辑和状态管理
- `modules/**/types.ts`：模块内类型定义，避免把所有类型堆到全局 `src/types`
- `components`：复用 UI 和表单/详情/确认弹窗

## 5. 路由与权限模型
### 5.1 路由结构
当前路由声明已拆到：
- `src/router/modules/accounts.ts`
- `src/router/modules/devices.ts`
- `src/router/modules/plans.ts`
- `src/router/modules/trades.ts`
- `src/router/modules/system.ts`
- `src/router/modules/reports.ts`
- `src/router/modules/operations.ts`

`src/router/index.ts` 只负责：
- 汇总 routes
- 创建 router
- 挂载 `beforeEach/afterEach`

路由守卫逻辑位于：
- `src/router/guard.ts`

其中已经抽出可测试函数：
- `resolveDocumentTitle`
- `normalizePath`
- `buildRegisteredProtectedPaths`
- `resolveFirstAccessiblePath`
- `handleMenuLoadFailure`
- `createRouteGuard`
- `updateDocumentTitle`

### 5.2 菜单来源
当前菜单接口：

```text
GET /v1/users/current/menus?source=1
```

前端在 `src/stores/permission.ts` 中完成：
- 平铺菜单转树
- 可访问页面路径收集
- 按钮权限 key 收集
- 首个可访问页面推导

### 5.3 菜单与按钮权限
当前约定：
- `menuType === 1`：页面菜单
- `menuType === 2`：按钮权限

前端权限控制分两层：
1. 路由访问控制
- 根据 `allowedPaths` 判断当前路由是否允许访问
- 菜单加载失败或当前路径无权限时，自动回退或跳登录

2. 按钮显隐控制
- 简单场景使用 `v-menu-permission`
- 复杂逻辑可使用 `stores/permission.ts` 暴露的菜单 key 数据

## 6. 接口与数据约定
### 6.1 统一响应 envelope
工程统一处理如下结构：

```json
{ "success": true, "code": 100001, "message": "", "data": ... }
```

关键逻辑位于：
- `src/api/raw/types.ts`

当前约定：
- 成功码：`100001`
- 未登录：`-103001`
- 权限不足：`-103002`

### 6.2 分页归一规范
分页会统一归一到：

```ts
{ list: T[]; total: number; pageNum?: number; pageSize?: number }
```

当前兼容的后端字段包括：
- `list`
- `records`
- `items`
- `total`
- `totalSize`
- `totalCount`

### 6.3 认证与跳转
- 鉴权头：`Authorization: <token>`
- token 存储工具：`src/utils/token.ts`
- `401` 会清理 token 并跳转 `/login`
- envelope 的未登录码 `-103001` 也会触发跳登录

## 7. 当前主要业务页面
### 7.1 已实现页面
- 账户管理
  - `/accounts/info`
  - `/accounts/cancel-records`
- 设备管理
  - `/devices/electric-meters`
  - `/devices/gateways`
  - `/devices/categories`
- 方案管理
  - `/plans/electric`
  - `/plans/warn`
- 交易管理
  - `/trade/recharge`
  - `/trade/order-flows`
  - `/trade/consumption-records`
- 系统管理
  - `/system/users`
  - `/system/roles`
  - `/system/menus`
  - `/system/spaces`
  - `/system/organizations`

### 7.2 占位页面
当前仍为占位页：
- `/reports/electric-bill`
- `/reports/daily-electricity`
- `/operations`

## 8. 当前前端重构状态
这轮前端收口已经完成两块高复杂度页面的逻辑拆分。

### 8.1 用户管理页
页面：
- `src/views/system/UserManagementView.vue`

已抽离逻辑：
- `src/modules/system/users/composables/useUserNotice.ts`
- `src/modules/system/users/composables/useUserQuery.ts`
- `src/modules/system/users/composables/useUserCrud.ts`
- `src/modules/system/users/composables/userShared.ts`

### 8.2 智能电表页
页面：
- `src/views/devices/DeviceElectricMeterView.vue`

已抽离逻辑：
- `src/modules/devices/electric-meters/composables/electricMeterShared.ts`
- `src/modules/devices/electric-meters/composables/useElectricMeterNotice.ts`
- `src/modules/devices/electric-meters/composables/useElectricMeterQuery.ts`
- `src/modules/devices/electric-meters/composables/useElectricMeterActions.ts`

已拆子组件：
- `src/modules/devices/electric-meters/components/DeviceElectricMeterSearchPanel.vue`
- `src/modules/devices/electric-meters/components/DeviceElectricMeterTableSection.vue`

## 9. 测试说明
### 9.1 单元测试
当前单元测试使用：
- `Vitest`
- `Vue Test Utils`
- `jsdom`

测试启动文件：
- `tests/unit/setup.ts`

覆盖率配置位于：
- `vitest.config.ts`

当前 coverage 阈值：
- lines `95`
- statements `95`
- branches `90`
- functions `90`

当前重点覆盖对象：
- `src/api/raw/types.ts`
- `src/stores/auth.ts`
- `src/stores/permission.ts`
- `src/api/adapters/user.ts`
- 用户管理 composables
- 电表管理 composables
- `src/router/guard.ts`

### 9.2 Playwright 冒烟测试
当前冒烟测试位于：
- `tests/energy_permission_regression.spec.cjs`
- `tests/device_electric_meter_smoke.spec.cjs`
- `tests/system_user_smoke.spec.cjs`
- `tests/system_organization_smoke.spec.cjs`

这些测试目前采用**前端自带请求拦截 mock**，目标是验证页面主流程，而不是跑真实后端集成环境。

运行方式：
1. 先启动前端 dev server
2. 再执行 Playwright

示例：

```bash
pnpm dev
```

另开终端：

```bash
TEST_BASE_URL=http://127.0.0.1:4173 pnpm test:e2e
```

如果只跑某一条 smoke：

```bash
TEST_BASE_URL=http://127.0.0.1:4173 pnpm exec playwright test tests/system_user_smoke.spec.cjs
```

## 10. 当前开发建议
1. 新页面优先沿用现有分层：`raw -> adapter -> composable -> view`
2. 不要把复杂业务逻辑重新堆回 `.vue` 页面文件
3. 新增高风险页面时，先补单元测试，再补 Playwright 冒烟
4. 如果模块继续变大，优先把类型、composable、子组件放进 `src/modules/<domain>` 收口

## 11. 常见问题
### 11.1 为什么浏览器请求前缀是 `/api`
因为开发环境通过 Vite proxy 转发到后端，浏览器只访问前端域名，不直接跨域访问后端。

### 11.2 为什么 Playwright 用例不依赖真实后端
当前 smoke 的目标是稳定验证页面主流程和权限/弹窗/提交流程，避免把数据库、MQ、后端启动一起引进前端 CI。

### 11.3 为什么有 `raw` 和 `adapters` 两层
因为后端接口契约和页面展示模型不是同一个概念。`raw` 负责贴近后端，`adapters` 负责把接口数据清洗成页面真正要用的结构，避免页面直接消费原始 VO。
