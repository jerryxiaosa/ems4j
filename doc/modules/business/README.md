# ems-business 模块文档

## 1. 模块概述

`ems-business` 是业务服务层，提供能源管理系统的核心业务能力，包括设备管理、账户管理、财务核算、计费方案，以及跨业务查询聚合能力等功能。

## 2. 子模块列表

| 模块 | 说明 |
|------|------|
| [ems-business-device](ems-business-device.md) | 电表、网关、设备档案管理 |
| [ems-business-account](ems-business-account.md) | 开户、销户、余额、充值 |
| [ems-business-finance](ems-business-finance.md) | 账单生成、财务流水、订单 |
| [ems-business-plan](ems-business-plan.md) | 计费方案、费率、尖峰平谷时段 |
| [ems-business-aggregation](ems-business-aggregation.md) | 跨业务读聚合、查询拼装、必要时的只读 join 查询 |

## 3. 模块依赖关系

### 3.1 ems-business-account

```
+------------------------------+
|    ems-business-account      |
+------------------------------+
              |
              +---> ems-business-device
              |
              +---> ems-business-finance
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
              +---> ems-business-finance
              |
              +---> ems-foundation-integration
              |
              +---> ems-foundation-space
```

### 3.3 ems-business-finance

```
+------------------------------+
|    ems-business-finance      |
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

### 3.4 ems-business-plan

```
+------------------------------+
|    ems-business-plan         |
+------------------------------+
              |
              +---> ems-foundation-system
```

### 3.5 ems-business-aggregation

```
+----------------------------------+
|    ems-business-aggregation      |
+----------------------------------+
                 |
                 +---> ems-business-finance
```

## 4. 各模块核心 Service

| 模块 | Service | 职责 |
|------|---------|------|
| device | ElectricMeterManagerService | 电表管理、开表/销表、状态控制 |
| device | ElectricMeterInfoService | 电表信息查询 |
| device | GatewayService | 网关管理 |
| account | AccountManagerService | 开户、销户、账户管理 |
| account | AccountInfoService | 账户信息查询 |
| account | AccountBalanceAlertService | 余额预警 |
| finance | BalanceService | 余额管理 |
| finance | OrderService | 订单管理 |
| finance | MeterConsumeService | 电量消费计算 |
| finance | AccountConsumeService | 账户消费汇总 |
| plan | ElectricPricePlanService | 电价方案管理 |
| plan | WarnPlanService | 预警方案管理 |
| aggregation | AccountElectricBalanceAggregateService | 账户列表展示电费余额聚合 |
