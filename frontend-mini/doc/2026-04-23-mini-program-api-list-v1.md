# EMS4J 用户端微信小程序接口清单（第一版）

- 文档版本：V1.0
- 创建日期：2026-04-23
- 适用范围：`frontend-mini` 第一阶段
- 对应文档：
  - `2026-04-20-mini-program-user-prd.md`
  - `2026-04-20-mini-program-low-fidelity-prototype.md`
  - `2026-04-23-mini-program-field-caliber-v1.md`
  - `2026-04-23-mini-program-field-caliber-v2.md`

## 1. 说明

本文件用于定义第一阶段用户端微信小程序的前后端接口清单。

目标：

- 明确前端页面与后端接口之间的映射关系
- 固定接口路径、请求方式、认证要求
- 为后续 controller / biz / vo 设计提供依据

本清单优先服务小程序前端调用，不展开后台管理端接口。

## 2. 公共约定

### 2.1 基础路径

- 接口版本前缀沿用现有约定：`/v1`
- 小程序用户端建议统一前缀：`/v1/mini`

### 2.2 响应结构

- 所有接口统一使用现有 `RestResult<T>`
- 列表分页接口建议复用项目现有 `PageResult<T>`

### 2.3 认证方式

- 登录接口：无需登录态
- 其余小程序接口：统一通过 `Authorization` 头传递 token
- 小程序登录态与后台 Web 登录态逻辑隔离，但底层继续复用现有 Sa-Token 体系

### 2.4 错误处理约定

- 技术错误、权限错误、参数错误：走统一错误响应
- 空数据优先通过空列表、空对象或轻量提示文案表达
- 不引入通用 `loginState` / `dataState` / `rechargeState` 等泛状态字段

## 3. 接口总览

| 模块 | 方法 | 路径 | 认证 | 用途 |
|---|---|---|---|---|
| 认证 | `POST` | `/v1/mini/auth/login` | 否 | 微信手机号快捷登录 |
| 认证 | `POST` | `/v1/mini/auth/logout` | 是 | 退出登录 |
| 首页 | `GET` | `/v1/mini/home/summary` | 是 | 获取首页摘要 |
| 首页 | `GET` | `/v1/mini/home/trend` | 是 | 获取首页图表数据 |
| 充值 | `GET` | `/v1/mini/recharge/init` | 是 | 获取充值页初始化数据 |
| 订单 | `POST` | `/v1/mini/orders/top-up` | 是 | 创建充值订单 |
| 订单 | `POST` | `/v1/mini/orders/{orderSn}/payment-params` | 是 | 获取微信支付参数 |
| 订单 | `GET` | `/v1/mini/orders` | 是 | 分页查询订单列表 |
| 账单 | `GET` | `/v1/mini/bills/months` | 是 | 查询账单月列表 |
| 账单 | `GET` | `/v1/mini/bills/days` | 是 | 查询账单日明细 |
| 电表 | `GET` | `/v1/mini/meters` | 是 | 查询电表列表 |
| 电表 | `GET` | `/v1/mini/meters/{meterId}` | 是 | 查询单表详情 |
| 我的 | `GET` | `/v1/mini/me` | 是 | 查询我的页基础信息 |

## 4. 接口明细

## 4.1 认证接口

### 4.1.1 微信手机号快捷登录

- 方法：`POST`
- 路径：`/v1/mini/auth/login`
- 认证：否

#### 请求参数

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `loginCode` | `string` | 是 | `wx.login` 返回的 code |
| `phoneCode` | `string` | 是 | 微信手机号能力返回的 code |

#### 成功响应

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `accessToken` | `string` | 是 | 登录 token |
| `expireIn` | `number` | 是 | token 过期时间（秒） |

#### 业务说明

- 成功时表示用户可正常进入业务区
- 对以下场景，建议通过明确的业务错误码返回，由前端进入引导页：
  - 未匹配到账户
  - 未开户
  - 账户状态异常

### 4.1.2 退出登录

- 方法：`POST`
- 路径：`/v1/mini/auth/logout`
- 认证：是

#### 请求参数

无

#### 成功响应

无业务响应体，返回 `RestResult<Void>`

## 4.2 首页接口

### 4.2.1 首页摘要

- 方法：`GET`
- 路径：`/v1/mini/home/summary`
- 认证：是

#### 请求参数

无

#### 响应字段

字段口径参见：
- `2026-04-23-mini-program-field-caliber-v1.md`
- `2. 首页摘要接口`

### 4.2.2 首页图表

- 方法：`GET`
- 路径：`/v1/mini/home/trend`
- 认证：是

#### 请求参数

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `metric` | `string` | 是 | 取值：`fee` / `energy` |

#### 响应字段

字段口径参见：
- `2026-04-23-mini-program-field-caliber-v1.md`
- `3. 首页图表接口`

## 4.3 充值与订单接口

### 4.3.1 充值初始化

- 方法：`GET`
- 路径：`/v1/mini/recharge/init`
- 认证：是

#### 请求参数

无

#### 响应字段

字段口径参见：
- `2026-04-23-mini-program-field-caliber-v1.md`
- `4. 充值初始化接口`

### 4.3.2 创建充值订单

- 方法：`POST`
- 路径：`/v1/mini/orders/top-up`
- 认证：是

#### 请求参数

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `orderAmount` | `number` | 是 | 充值金额，单位为元，保留两位小数 |
| `meterId` | `string` | 否 | 按需模式必填，非按需模式不传 |

#### 成功响应

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `orderSn` | `string` | 是 | 订单号 |
| `orderAmount` | `number` | 是 | 订单金额 |
| `orderPayStopTime` | `string` | 是 | 支付截止时间 |

#### 说明

- 小程序端不需要传用户信息、支付渠道、账户归属等字段
- 这些字段应由后端基于当前登录用户与账户信息补齐

### 4.3.3 获取微信支付参数

- 方法：`POST`
- 路径：`/v1/mini/orders/{orderSn}/payment-params`
- 认证：是

#### 路径参数

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `orderSn` | `string` | 是 | 订单号 |

#### 成功响应

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `timeStamp` | `string` | 是 | 微信支付时间戳 |
| `nonceStr` | `string` | 是 | 微信支付随机串 |
| `packageValue` | `string` | 是 | 微信支付 package 值 |
| `signType` | `string` | 是 | 签名类型 |
| `paySign` | `string` | 是 | 支付签名 |

#### 说明

- 该接口为第一阶段两步支付链路的第二步
- 前端拿到参数后直接调用 `wx.requestPayment`

### 4.3.4 查询订单列表

- 方法：`GET`
- 路径：`/v1/mini/orders`
- 认证：是

#### 请求参数

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `pageNum` | `number` | 否 | 默认 `1` |
| `pageSize` | `number` | 否 | 默认 `10` |

#### 响应字段

字段口径参见：
- `2026-04-23-mini-program-field-caliber-v1.md`
- `5. 订单列表接口`

## 4.4 账单接口

### 4.4.1 查询账单月列表

- 方法：`GET`
- 路径：`/v1/mini/bills/months`
- 认证：是

#### 请求参数

无

#### 响应字段

字段口径参见：
- `2026-04-23-mini-program-field-caliber-v2.md`
- `2. 账单月列表接口`

### 4.4.2 查询账单日明细

- 方法：`GET`
- 路径：`/v1/mini/bills/days`
- 认证：是

#### 请求参数

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `month` | `string` | 是 | 查询月份，格式建议为 `yyyy-MM` |

#### 响应字段

字段口径参见：
- `2026-04-23-mini-program-field-caliber-v2.md`
- `3. 账单日明细接口`

## 4.5 电表接口

### 4.5.1 查询电表列表

- 方法：`GET`
- 路径：`/v1/mini/meters`
- 认证：是

#### 请求参数

无

#### 响应字段

字段口径参见：
- `2026-04-23-mini-program-field-caliber-v2.md`
- `4. 电表列表接口`

### 4.5.2 查询单表详情

- 方法：`GET`
- 路径：`/v1/mini/meters/{meterId}`
- 认证：是

#### 路径参数

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `meterId` | `string` | 是 | 电表 ID |

#### 响应字段

字段口径参见：
- `2026-04-23-mini-program-field-caliber-v2.md`
- `5. 单表详情接口`

## 4.6 我的页接口

### 4.6.1 查询我的页基础信息

- 方法：`GET`
- 路径：`/v1/mini/me`
- 认证：是

#### 请求参数

无

#### 成功响应

| 字段名 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `userPhone` | `string` | 是 | 手机号 |
| `organizationName` | `string` | 是 | 所属机构名称 |
| `accountStatus` | `string` | 是 | 当前账户状态编码 |
| `accountStatusName` | `string` | 是 | 当前账户状态名称 |

#### 说明

- 我的页第一阶段不单独展示账户名称
- “我的订单”入口为前端静态入口，不需要单独接口字段支持

## 5. 支付回调说明

微信支付回调不属于小程序前端调用接口，建议继续复用现有后端回调入口：

- `POST /v1/orders/weixin/pay-notify`

小程序前端无需直接调用该接口。

## 6. 下一步建议

在接口清单确认后，建议继续输出：

1. 小程序用户端轻量技术方案正式版
2. controller / biz / vo 任务拆分清单
3. 前后端联调与测试验收清单
