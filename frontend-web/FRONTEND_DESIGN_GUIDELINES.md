# 前端设计规范

本文档整理 `frontend-web` 当前已经落地并验证过的前端设计规范，目标不是定义一套全新的理想规范，而是沉淀当前代码库实际在用、后续应继续遵循的约定。

适用范围：

- 新增页面
- 新增弹窗
- 新增列表页
- 新增详情页
- 新增接口适配层
- 重构现有前端页面

---

## 1. 总体原则

### 1.1 风格基线

当前前端统一沿用后台管理系统风格，不做独立视觉体系。整体基线来自：

- [base.css](/Users/jerry/Workspace/github/ems4j/frontend-web/src/styles/base.css)
- [MainLayout.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/layouts/MainLayout.vue)

必须遵循：

- 页面背景、卡片、边框、阴影、按钮、表格表头统一使用全局变量
- 页面应看起来像系统内原生页面，而不是单独设计的一套样式
- 优先复用已有样式结构，不新增相似但不同名的 CSS 体系

### 1.2 设计目标

前端实现优先满足以下目标：

- 风格统一
- 结构稳定
- 接口分层清晰
- 列表页和弹窗交互一致
- 权限控制一致
- loading / 空状态表现一致

---

## 2. 目录与分层规范

### 2.1 目录职责

当前目录职责如下：

- `src/views`
  - 页面级入口组件
  - 负责页面布局、查询条件、列表展示、分页、打开弹窗
- `src/components`
  - 通用组件或跨页面复用组件
  - 典型场景包括详情弹窗、表单弹窗、通用状态组件
- `src/modules`
  - 某一业务域下的复用能力
  - 包括 composable、局部类型、局部组件
- `src/api/raw`
  - 只负责 HTTP 请求定义
  - 不做业务归一化
- `src/api/adapters`
  - 负责解包、兼容后端字段、数据归一化
  - 页面层优先使用 adapter，不直接调 raw
- `src/types`
  - 页面和 adapter 使用的前端类型定义
- `src/stores`
  - 全局状态，如认证、权限
- `src/directives`
  - 全局指令，如菜单权限

### 2.2 推荐依赖方向

前端建议依赖方向如下：

- `views -> components/modules -> api/adapters -> api/raw`
- `views/components/modules -> types`
- `views/components -> composables/store/directive`

避免：

- `view` 直接依赖 `raw`
- `component` 内直接写复杂接口解包逻辑
- 在多个页面重复实现同一类查询/提示/分页逻辑

---

## 3. 全局视觉基线

### 3.1 设计令牌

统一使用 [base.css](/Users/jerry/Workspace/github/ems4j/frontend-web/src/styles/base.css) 中的变量，不应在页面里随意硬编码另一套主色、字体和圆角。

重点变量：

- 字体：
  - `--es-font-family`
  - `--es-font-size-xs/sm/md/lg`
- 圆角：
  - `--es-radius-sm/md/lg`
- 阴影：
  - `--es-shadow-card`
  - `--es-shadow-floating`
- 颜色：
  - `--es-color-bg-page`
  - `--es-color-bg-elevated`
  - `--es-color-border`
  - `--es-color-text-primary`
  - `--es-color-text-secondary`
  - `--es-color-primary`
  - `--es-color-table-header-bg`

### 3.2 通用外观规则

必须保持一致：

- 表格表头使用蓝色标题字 + 浅蓝背景
- 详情区输入式展示统一走 `es-detail-*` 样式
- 弹窗关闭按钮统一走 `icon-btn`
- 主要操作按钮用 `btn btn-primary`
- 次要操作按钮用 `btn btn-secondary`
- 行内文字操作用 `btn-link`

---

## 4. 页面结构规范

### 4.1 主列表页骨架

主列表页统一优先采用：

1. `page`
2. `search-card`
3. `table-card`
4. `table-toolbar`
5. `table-wrap`
6. `CommonPagination`

典型参考：

- [UserManagementView.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/views/system/UserManagementView.vue)
- [DeviceGatewayView.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/views/devices/DeviceGatewayView.vue)
- [ElectricBillReportView.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/views/reports/ElectricBillReportView.vue)

列表页必须遵循：

- 搜索区与列表区分成两个 card
- 主列表页禁止再使用单个 `workspace-card` 同时包裹搜索区和列表区
- 主列表页的 `search-card`、`table-card`、`table-wrap` 统一使用 `5px` 圆角
- 页面主卡片不要使用 `var(--es-radius-lg)`，`12px` 只保留给少量浮层或组件内部特殊场景
- 分页使用 [CommonPagination.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/common/CommonPagination.vue)
- 空状态与 loading 统一使用 [UiTableStateOverlay.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/common/UiTableStateOverlay.vue)
- `table-wrap` 使用 `position: relative`
- `table-card` 如果内部是“标题工具栏 + 表格/列表主体”两段结构，必须显式写 `grid-template-rows: auto minmax(0, 1fr)`，避免 grid 默认拉伸把标题区撑高

### 4.2 详情弹窗骨架

详情弹窗统一优先采用：

1. `modal-mask`
2. `modal-panel`
3. `modal-head`
4. `modal-title`
5. `modal-actions`
6. `modal-body`
7. `section-card`
8. `section-title`

典型参考：

- [DeviceOperationDetailModal.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/devices/DeviceOperationDetailModal.vue)
- [DeviceGatewayDetailModal.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/devices/DeviceGatewayDetailModal.vue)
- [ElectricBillReportDetailModal.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/reports/ElectricBillReportDetailModal.vue)

要求：

- 弹窗头部只放标题和关闭类轻操作
- 详情弹窗外层 `modal-panel` 圆角统一使用 `5px`
- 业务动作优先放内容区，而不是塞在右上角工具栏
- 内容主体分 section 组织
- 详情字段优先用 `es-detail-label` + `es-detail-value-box`
- 文本框左侧的详情字段标签不额外加粗，不覆盖 `es-detail-label` 的默认字重和颜色
- 详情弹窗中如果字段标签包含金额、编号、方案名称等较长文案，标签列宽不要低于 `120px`，标签和值之间的间距不要小于 `8px`
- 详情弹窗里的长文本、报文、JSON、指令原文等大块内容必须设置独立的滚动容器，优先用“外层容器负责 `max-height + overflow: auto`，内层文本块负责排版”的方式实现局部滚动，不依赖整个弹窗 body 滚动
- 当详情弹窗同时包含固定信息区和长文本区时，长文本区必须放在“剩余高度”布局里，例如 `grid-template-rows: auto minmax(0, 1fr)`，并显式补 `min-height: 0`
- 详情弹窗主标题统一使用 `var(--es-font-size-md)` 和 `font-weight: 600`
- 详情弹窗分区标题统一使用 `14px` 和 `font-weight: 600`

### 4.3 页面通知

当前页面级通知统一采用：

- 页面内顶部 notice 区
- 或业务域封装的 notice composable

典型参考：

- [AccountInfoView.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/views/accounts/AccountInfoView.vue)
- [UserManagementView.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/views/system/UserManagementView.vue)

不建议：

- 直接在页面里新增另一套 toast 机制
- 同一页面同时混用多种反馈方式

---

## 5. 状态展示规范

### 5.1 空状态与 loading

当前已经统一的状态组件：

- [UiEmptyState.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/common/UiEmptyState.vue)
- [UiLoadingState.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/common/UiLoadingState.vue)
- [UiTableStateOverlay.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/common/UiTableStateOverlay.vue)

### 5.2 高度规范

当前应按场景统一，不允许页面自由散写高度。

- 主列表页：`120px`
- 详情内小表格：`72px`
- 详情主体：`180px`
- 图表区域：`280px`

说明：

- 横向滚动宽表不能再把空状态放在 `td[colspan]` 里居中
- 一律优先使用 `UiTableStateOverlay`
- 这是为了解决超宽表格下状态偏右的问题

### 5.3 宽表格规范

凡是存在横向滚动的表格：

- 容器使用 `table-wrap`
- 空状态 / loading 必须基于 overlay 居中
- 不要继续依赖 `td[colspan] + UiEmptyState`

典型参考：

- [DeviceGatewayView.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/views/devices/DeviceGatewayView.vue)
- [DeviceElectricMeterTableSection.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/modules/devices/electric-meters/components/DeviceElectricMeterTableSection.vue)

---

## 6. 表格规范

### 6.1 列表表格

统一约定：

- 表头使用蓝色标题色和浅蓝背景
- 金额、电量、数量列尽量右对齐
- 操作列使用 `btn-link`
- 主列表页操作列文字样式以 [AccountInfoView.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/views/accounts/AccountInfoView.vue) 为准：
  - 常规动作 `font-weight: 500`
  - `line-height: 1.2`
  - 相邻动作间距 `12px`
  - 危险动作使用 `btn-link-danger`
  - hover 行为保持轻量，不做加粗或按钮化
- 超长文本使用省略或 `title`
- 表格下方统一接 `CommonPagination`

### 6.2 详情内嵌表格

统一约定：

- 尽量使用 `es-detail-table` 或当前详情弹窗内部统一样式
- 空状态高度统一 `72px`
- 详情里的表格不再单独发明一套表头风格

### 6.3 固定列

如果存在 sticky 固定列：

- 优先用单表方案
- 必须保证 `border-collapse`、背景、偏移量、层级正确
- 固定列出问题时，优先修偏移和背景，不要直接拆成三张表

典型参考：

- [ElectricBillReportDetailModal.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/reports/ElectricBillReportDetailModal.vue)

---

## 7. 详情信息规范

### 7.1 字段展示

详情字段展示优先使用：

- 标签：`es-detail-label`
- 值容器：`es-detail-value-box`

推荐结构：

- 基本信息区：网格布局
- 指令/备注区：单独 section
- 表格区：独立 section

### 7.2 操作按钮位置

当前规范：

- 关闭类按钮放头部右侧
- 与业务状态强相关的按钮，放在内容区
- 不要把重要业务动作和关闭按钮并排塞在标题栏

例如：

- 设备操作详情中的 `重试` 放在“操作状态”这一行后面
- 而不是放在头部右上角

---

## 8. 权限规范

权限显示统一使用 [menuPermission.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/directives/menuPermission.ts) 提供的 `v-menu-permission` 指令。

要求：

- 页面按钮显隐优先使用 `v-menu-permission`
- 权限 key 使用菜单 `menuKey`
- 不要在模板里自行拼复杂的权限判断逻辑

典型写法：

```vue
<button
  v-menu-permission="userPermissionKeys.create"
  class="btn btn-primary"
  type="button"
>
  添加
</button>
```

说明：

- `v-menu-permission` 控制的是菜单权限，不是接口权限码
- 页面显示逻辑应与当前用户菜单树保持一致

---

## 9. 接口分层规范

### 9.1 HTTP 层

[http.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/api/http.ts) 提供：

- `request`
- `requestV1`
- `requestV2`

职责：

- axios 实例创建
- token 注入
- 401 重定向登录
- 基础 HTTP 错误处理

页面层不应直接处理这些通用逻辑。

### 9.2 raw 层

`src/api/raw` 只负责：

- 请求路径
- method
- params / body
- raw response 类型

不负责：

- 解包业务 envelope
- 兼容多种后端字段风格
- 业务字段归一化

典型参考：

- [device-operation.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/api/raw/device-operation.ts)
- [report.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/api/raw/report.ts)

### 9.3 adapter 层

`src/api/adapters` 负责：

- `unwrapEnvelope`
- `normalizePageResult`
- 后端字段兼容
- 文本值与布尔值归一化
- 页面实际使用的数据结构整理

典型参考：

- [report.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/api/adapters/report.ts)
- [device-operation.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/api/adapters/device-operation.ts)

### 9.4 类型放置

推荐做法：

- raw 类型定义放 `api/raw/*.ts`
- 页面使用的类型放 `types/*.ts`

不要让页面直接依赖 raw DTO。

---

## 10. 类型与格式化规范

### 10.1 页面类型

页面使用的类型放在 `src/types` 或局部 `modules/*/types.ts`。

要求：

- 页面只消费归一化后的类型
- 不直接消费后端不稳定字段
- 对于文本展示字段，优先在 adapter 层收口

### 10.2 格式化责任

当前建议：

- 后端已经明确提供文本值的字段，前端直接展示
- 其他需要兼容后端字段差异的场景，在 adapter 层做标准化
- 页面层只做轻量 UI 拼装，不做复杂业务格式化

不要：

- 在多个页面重复写 `toText` / `toBool` / `normalize`
- 在模板里写大量三元表达式处理字段兼容

---

## 11. 交互规范

### 11.1 列表操作

列表操作统一用 `btn-link`。

规则：

- 主列表页操作列动作尽量少
- 不展示“状态型假按钮”
- 只有当前动作真的可执行，才显示按钮

例如：

- 设备操作列表页中，`详情` 始终显示
- `重试` 只在当前任务可重试时显示
- 不再显示 `已成功`、`不可重试` 这类伪操作按钮

### 11.2 弹窗操作

规则：

- 关闭按钮统一使用 `icon-btn`
- 危险操作不要和关闭按钮并列抢视觉重心
- 上下文强相关操作优先放内容区

---

## 12. 组件复用规范

优先复用已有组件，不要重复造轮子。

优先复用清单：

- 分页：
  - [CommonPagination.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/common/CommonPagination.vue)
- 空状态：
  - [UiEmptyState.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/common/UiEmptyState.vue)
- loading：
  - [UiLoadingState.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/common/UiLoadingState.vue)
- 表格状态 overlay：
  - [UiTableStateOverlay.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/common/UiTableStateOverlay.vue)
- 权限：
  - [menuPermission.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/directives/menuPermission.ts)
- 机构选择：
  - [OrganizationPicker.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/common/OrganizationPicker.vue)

---

## 13. 测试规范

前端改动应至少覆盖以下一种或多种测试：

- adapter 单测
- 组件单测
- 页面交互单测
- router / guard 单测

典型参考：

- [device-operation.test.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/api/adapters/__tests__/device-operation.test.ts)
- [DeviceOperationsView.test.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/views/device/DeviceOperationsView.test.ts)
- [DeviceOperationDetailModal.test.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/devices/DeviceOperationDetailModal.test.ts)

要求：

- 有行为变化时必须补测试
- 优先测页面实际行为，不只测实现细节
- 状态/权限/弹窗开关这种回归点要落测试

---

## 14. 新功能如何实现

下面是当前前端新增一个标准页面的推荐步骤。

### 14.1 新增列表页

1. 在 `src/types` 或 `src/modules/*/types.ts` 定义页面类型
2. 在 `src/api/raw` 定义 raw 请求
3. 在 `src/api/adapters` 定义 adapter，完成解包和归一化
4. 在 `src/views` 新建页面
5. 页面骨架按：
   - `search-card`
   - `table-card`
   - `table-wrap`
   - `UiTableStateOverlay`
   - `CommonPagination`
6. 页面按钮使用 `v-menu-permission`
7. 如有详情，优先用弹窗而不是新增独立详情页
8. 补 adapter / 页面测试

### 14.2 新增详情弹窗

1. 放到 `src/components/<domain>`
2. 使用：
   - `modal-mask`
   - `modal-panel`
   - `modal-head`
   - `modal-body`
   - `section-card`
3. 基本信息使用 `es-detail-label` + `es-detail-value-box`
4. 业务操作按钮不要先塞头部，优先放内容区
5. 小表格空状态统一 `72px`
6. 补组件测试

### 14.3 新增接口适配

1. raw 层只写请求
2. adapter 层统一：
   - `unwrapEnvelope`
   - `normalizePageResult`
   - 字段兼容
3. 页面只调用 adapter
4. 页面不要重复实现 normalize 逻辑

### 14.4 新增权限按钮

1. 明确页面使用的 `menuKey`
2. 在页面里用 `v-menu-permission`
3. 不要直接写角色判断
4. 后端与菜单种子数据保持一致

---

## 15. 禁止事项

以下做法应避免：

- 页面直接调用 raw 接口
- 页面模板中堆积大量数据清洗逻辑
- 宽表格继续用 `td[colspan]` 做空状态居中
- 一个功能同时保留独立详情页和详情弹窗两套入口，除非确有业务需要
- 在头部右上角同时堆主业务动作和关闭按钮
- 新增页面重新定义一套表格、按钮、卡片、表头色值
- 用“成功 / 不可重试 / 已达上限”这类伪操作按钮占据操作列

---

## 16. 推荐参考页

如果需要照着现有页面实现，优先参考：

- 主列表页：
  - [UserManagementView.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/views/system/UserManagementView.vue)
  - [DeviceGatewayView.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/views/devices/DeviceGatewayView.vue)
  - [ElectricBillReportView.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/views/reports/ElectricBillReportView.vue)
- 弹窗详情：
  - [DeviceGatewayDetailModal.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/devices/DeviceGatewayDetailModal.vue)
  - [DeviceOperationDetailModal.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/devices/DeviceOperationDetailModal.vue)
  - [ElectricBillReportDetailModal.vue](/Users/jerry/Workspace/github/ems4j/frontend-web/src/components/reports/ElectricBillReportDetailModal.vue)
- raw + adapter：
  - [device-operation.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/api/raw/device-operation.ts)
  - [device-operation.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/api/adapters/device-operation.ts)
  - [report.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/api/raw/report.ts)
  - [report.ts](/Users/jerry/Workspace/github/ems4j/frontend-web/src/api/adapters/report.ts)

---

## 17. 落地结论

后续前端开发，应优先遵循以下最小规则：

1. 页面结构先对齐现有骨架，不先设计新框架。
2. 所有列表页统一用 `UiTableStateOverlay`。
3. 所有接口都走 `raw + adapter` 双层。
4. 权限统一用 `v-menu-permission`。
5. 详情优先用弹窗，避免无必要的独立详情页。
6. 业务动作靠近业务信息，不抢头部关闭区。
7. 有行为变化必须补单测。

这 7 条是当前前端代码最核心、最值得继续坚持的规范。
