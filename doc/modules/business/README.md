# ems-business 模块文档

## 1. 模块概述

`ems-business` 是业务服务层，提供能源管理系统的核心业务能力，包括设备管理、账户管理、账务处理、订单交易、租赁关系、计费方案，以及跨业务查询聚合能力等功能。

## 2. 子模块列表

| 模块 | 说明 |
|------|------|
| [ems-business-device](ems-business-device.md) | 电表、网关、设备档案管理 |
| [ems-business-account](ems-business-account.md) | 开户、销户、账户管理、账户读模型 |
| [ems-business-billing](ems-business-billing.md) | 余额、抄表消费、补正、账务流水 |
| [ems-business-order](ems-business-order.md) | 订单创建、支付回调、订单查询与完成处理 |
| [ems-business-lease](ems-business-lease.md) | 主体与空间租赁关系、租赁查询、退租校验 |
| [ems-business-plan](ems-business-plan.md) | 计费方案、费率、尖峰平谷时段 |
| [ems-business-aggregation](ems-business-aggregation.md) | 跨业务读聚合与应用层编排 |

## 3. 模块依赖关系

### 3.1 ems-business-account

```
+------------------------------+
|    ems-business-account      |
+------------------------------+
              |
              +---> ems-business-device
              |
              +---> ems-business-billing
              |
              +---> ems-business-order
              |
              +---> ems-business-lease
              |
              +---> ems-business-plan
              |
              +---> ems-foundation-user
              |
              +---> ems-foundation-organization
```

### 3.2 ems-business-device

```
+------------------------------+
|    ems-business-device       |
+------------------------------+
              |
              +---> ems-business-plan
              |
              +---> ems-business-billing
              |
              +---> ems-foundation-integration
              |
              +---> ems-foundation-space
```

### 3.3 ems-business-billing

```
+------------------------------+
|    ems-business-billing      |
+------------------------------+
              |
              +---> ems-business-plan
              |
              +---> ems-foundation-space
              |
              +---> ems-foundation-organization
              |
              +---> ems-foundation-system
              |
              +---> ems-mq-api
```

### 3.4 ems-business-order

```
+------------------------------+
|    ems-business-order        |
+------------------------------+
              |
              +---> ems-foundation-space
              |
              +---> ems-foundation-system
              |
              +---> ems-mq-api
```

### 3.5 ems-business-lease

```
+------------------------------+
|    ems-business-lease        |
+------------------------------+
              |
              +---> ems-foundation-organization
              |
              +---> ems-foundation-space
              |
              +---> ems-business-device
```

### 3.6 ems-business-plan

```
+------------------------------+
|    ems-business-plan         |
+------------------------------+
              |
              +---> ems-foundation-system
```

### 3.7 ems-business-aggregation

```
+----------------------------------+
|    ems-business-aggregation      |
+----------------------------------+
                 |
                 +---> ems-business-account
                 |
                 +---> ems-business-device
                 |
                 +---> ems-business-billing
                 |
                 +---> ems-business-order
```

## 4. 各模块核心 Service

| 模块 | Service | 职责 |
|------|---------|------|
| device | ElectricMeterManagerService | 电表管理、开表/销表、状态控制 |
| device | ElectricMeterInfoService | 电表信息查询 |
| device | GatewayService | 网关管理 |
| account | AccountManagerService | 开户、销户、账户管理 |
| account | AccountInfoService | 账户信息查询 |
| account | AccountAdditionalInfoService | 账户附加读信息（候选电表、可开户总数、电费余额） |
| account | AccountBalanceChangeService | 账户余额变化后的预警处理（MONTHLY/MERGED） |
| billing | BalanceService | 余额管理 |
| billing | MeterConsumeService | 电量消费计算 |
| billing | AccountConsumeService | 账户消费汇总 |
| billing | MeterCorrectionService | 电表补正 |
| order | OrderService | 订单管理 |
| order | OrderQueryService | 订单查询 |
| order | ServiceRateService | 服务费率管理 |
| lease | OwnerSpaceLeaseService | 主体空间租赁/退租 |
| lease | OwnerSpaceRelationQueryService | 主体空间租赁关系查询 |
| device | MeterBalanceChangeService | 电表余额变化后的预警与开关闸处理 |
| plan | ElectricPricePlanService | 电价方案管理 |
| plan | WarnPlanService | 预警方案管理 |
