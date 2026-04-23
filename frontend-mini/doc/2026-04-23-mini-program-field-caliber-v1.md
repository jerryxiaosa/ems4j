# EMS4J 用户端微信小程序字段口径表（第一批）

- 文档版本：V1.0
- 创建日期：2026-04-23
- 适用范围：`frontend-mini` 第一阶段
- 对应文档：
  - `2026-04-20-mini-program-user-prd.md`
  - `2026-04-20-mini-program-low-fidelity-prototype.md`
  - `2026-04-22-prd-prototype-alignment-checklist.md`

## 1. 说明

本文件用于定义第一批核心接口的字段口径，作为后续接口清单与前后端联调的基础。

本轮只覆盖以下四类接口：

1. 首页摘要接口
2. 首页图表接口
3. 充值初始化接口
4. 订单列表接口

口径原则如下：

- 字段只保留当前页面真正需要的数据，不预埋过多扩展字段
- 不引入通用 `loginState` / `dataState` / `rechargeState` 等泛状态字段
- 正常空数据使用空列表或空值表达，必要时补充轻量提示字段
- 第一阶段充值仅支持自定义金额，不包含固定充值档位

## 2. 首页摘要接口

### 2.1 用途

用于首页首屏快速渲染上半部分信息，不包含图表数据。

### 2.2 入参

无业务入参，直接基于当前登录用户获取数据。

### 2.3 出参字段

| 字段名 | 类型 | 必填 | 含义 | 说明 |
|---|---|---|---|---|
| `accountName` | `string` | 是 | 账户名称 | 首页顶部主标题 |
| `electricAccountType` | `integer` | 是 | 账户类型 | 取值参见 `ElectricAccountTypeEnum` |
| `balance` | `number` | 是 | 当前余额 | 建议统一为元，保留两位小数 |
| `meterCount` | `number` | 是 | 电表数量 | 用于展示 `共 X 个电表 >` |

### 2.4 字段说明

- 首页已去掉机构名称，因此当前接口不返回 `organizationName`
- 若为按需模式且账户下存在多块表，`balance` 返回多表余额合计
- 页面上的余额说明由前端基于 `electricAccountType` 自行展示，不再单独返回 `balanceRemark`
- `electricAccountType` 建议按 `ElectricAccountTypeEnum` 返回整型值：
  - `0`：按需计费
  - `1`：包月计费
  - `2`：合并按需计费

## 3. 首页图表接口

### 3.1 用途

用于首页趋势图单独加载与切换显示。

### 3.2 入参

| 字段名 | 类型 | 必填 | 含义 | 说明 |
|---|---|---|---|---|
| `metric` | `string` | 是 | 图表维度 | 取值：`fee` / `energy` |

### 3.3 出参字段

| 字段名 | 类型 | 必填 | 含义 | 说明 |
|---|---|---|---|---|
| `metric` | `string` | 是 | 图表维度 | 与请求参数保持一致 |
| `unit` | `string` | 是 | 单位 | 电费建议为 `元`，电量建议为 `kWh` |
| `list` | `array` | 是 | 近七日数据列表 | 按日期升序或降序需在接口文档中固定 |
| `tip` | `string` | 否 | 提示文案 | 无数据时可返回，例如：`暂无近七日数据` |

### 3.4 `list` 子项字段

| 字段名 | 类型 | 必填 | 含义 | 说明 |
|---|---|---|---|---|
| `date` | `string` | 是 | 日期 | 建议格式：`MM-dd` 或 `yyyy-MM-dd` |
| `value` | `number` | 是 | 数值 | 与 `metric` 对应 |

### 3.5 字段说明

- 接口不额外引入通用状态字段
- 无数据时返回：
  - `list: []`
  - 可选 `tip`
- 首页默认请求 `metric=fee`
- 用户切换到电量时请求 `metric=energy`

## 4. 充值初始化接口

### 4.1 用途

用于充值页初始化页面数据。

### 4.2 入参

无业务入参，直接基于当前登录用户获取数据。

### 4.3 出参字段

| 字段名 | 类型 | 必填 | 含义 | 说明 |
|---|---|---|---|---|
| `electricAccountType` | `integer` | 是 | 账户类型 | 取值参见 `ElectricAccountTypeEnum` |
| `accountName` | `string` | 是 | 账户名称 | 非按需模式展示充值对象时使用 |
| `accountBalance` | `number` | 是 | 账户余额 | 单位为元 |
| `selectedMeterId` | `string` | 否 | 默认选中电表 ID | 仅按需模式下返回 |
| `meterOptionList` | `array` | 否 | 电表选项列表 | 仅按需模式下返回 |

### 4.4 `meterOptionList` 子项字段

| 字段名 | 类型 | 必填 | 含义 | 说明 |
|---|---|---|---|---|
| `meterId` | `string` | 是 | 电表 ID | 作为充值对象标识 |
| `meterName` | `string` | 是 | 电表名称 | 下拉框展示文案 |
| `meterBalance` | `number` | 是 | 电表余额 | 选中后展示当前余额 |

### 4.5 字段说明

- 第一阶段不返回固定充值档位
- 不设计 `canRecharge` / `rechargeState` 等状态字段
- `electricAccountType` 建议按 `ElectricAccountTypeEnum` 返回整型值：
  - `0`：按需计费
  - `1`：包月计费
  - `2`：合并按需计费
- 非按需模式：
  - 不返回 `meterOptionList`
  - 前端直接按账户充值
- 按需模式：
  - 前端通过 `meterOptionList` 渲染下拉框
  - 若返回 `selectedMeterId`，前端可默认选中对应电表

## 5. 订单列表接口

### 5.1 用途

用于“我的订单”页面分页展示充值订单列表。

### 5.2 入参

| 字段名 | 类型 | 必填 | 含义 | 说明 |
|---|---|---|---|---|
| `pageNum` | `number` | 是 | 当前页码 | 从 `1` 开始 |
| `pageSize` | `number` | 是 | 每页条数 | 第一阶段建议固定较小值 |

### 5.3 出参字段

| 字段名 | 类型 | 必填 | 含义 | 说明 |
|---|---|---|---|---|
| `pageNum` | `number` | 是 | 当前页码 | 回显当前页 |
| `pageSize` | `number` | 是 | 每页条数 | 回显分页大小 |
| `total` | `number` | 是 | 总条数 | 用于分页 |
| `list` | `array` | 是 | 订单列表 | 无数据时返回空数组 |

### 5.4 `list` 子项字段

| 字段名 | 类型 | 必填 | 含义 | 说明 |
|---|---|---|---|---|
| `orderSn` | `string` | 是 | 订单号 | 列表主标识 |
| `amount` | `number` | 是 | 订单金额 | 单位为元 |
| `status` | `string` | 是 | 状态编码 | 供前端逻辑判断使用 |
| `statusName` | `string` | 是 | 状态名称 | 例如：`已支付`、`待支付`、`支付失败` |
| `createTime` | `string` | 是 | 下单时间 | 列表展示时间 |

### 5.5 字段说明

- 第一阶段不返回失败原因、关闭原因
- 订单页当前只展示充值订单
- 建议复用项目现有 `PageResult` 结构，由前端根据 `pageNum`、`pageSize`、`total` 自行计算上一页 / 下一页是否可用

## 6. 下一步建议

第二批建议继续补充以下接口的字段口径：

1. 账单月列表接口
2. 账单日明细接口
3. 电表列表接口
4. 单表详情接口
