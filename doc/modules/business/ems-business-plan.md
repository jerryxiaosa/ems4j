# ems-business-plan 模块文档

## 1. 模块概述

`ems-business-plan` 是计费方案管理模块，负责电价方案、预警方案的配置和管理，为电量消费计算提供费率支持。

## 2. 核心功能

| 功能 | 说明 |
|------|------|
| 电价方案管理 | 电价方案 CRUD、默认方案设置 |
| 尖峰平谷配置 | 分时电价时段配置 |
| 阶梯电价配置 | 阶梯电价档位配置 |
| 预警方案管理 | 余额预警方案配置 |

## 3. Service 类说明

### 3.1 ElectricPricePlanService

电价方案服务，管理电价方案的增删改查和费率计算。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `findList()` | 查询电价方案列表 |
| `getDetail()` | 获取方案详情 |
| `addPlan()` | 新增电价方案 |
| `updatePlan()` | 更新电价方案 |
| `deletePlan()` | 删除电价方案 |
| `setDefaultPlan()` | 设置默认方案 |
| `getDefaultPlan()` | 获取默认方案 |
| `getPriceByPeriod()` | 按时段获取电价 |
| `getTimePeriods()` | 获取时段配置 |

### 3.2 WarnPlanService

预警方案服务，管理余额预警方案。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `findList()` | 查询预警方案列表 |
| `getDetail()` | 获取方案详情 |
| `addPlan()` | 新增预警方案 |
| `updatePlan()` | 更新预警方案 |
| `deletePlan()` | 删除预警方案 |
| `getDefaultPlan()` | 获取默认方案 |

## 4. 模块依赖关系

```
+---------------------------+
|    ems-business-plan      |
+---------------------------+
            |
            | 依赖
            v
+---------------------------+
|  ems-foundation-system    |
|    (系统配置服务)          |
+---------------------------+
            |
            v
+---------------------------+
| ems-components-datasource |
+---------------------------+
            |
            v
+---------------------------+
|       ems-common          |
+---------------------------+
```

## 5. Service 内部依赖

### 5.1 ElectricPricePlanServiceImpl 依赖

```
+-------------------------------------+
|    ElectricPricePlanServiceImpl     |
+-------------------------------------+
                |
                v
+---------------------------+
|   ems-foundation-system   |
+---------------------------+
| ConfigService             |
| (读写默认方案配置)         |
+---------------------------+
```

### 5.2 WarnPlanServiceImpl 依赖

```
+-------------------------------------+
|       WarnPlanServiceImpl           |
+-------------------------------------+
                |
                v
+---------------------------+
|   (无外部模块依赖)         |
|   仅依赖本模块 Repository  |
+---------------------------+
```

## 6. 数据实体

| 实体 | 说明 |
|------|------|
| `ElectricPricePlanEntity` | 电价方案表 |
| `WarnPlanEntity` | 预警方案表 |

## 7. 电价时段类型

| 时段 | 枚举值 | 说明 |
|------|--------|------|
| 尖峰 | `HIGHER` | 最高电价时段 |
| 高峰 | `HIGH` | 高电价时段 |
| 平段 | `NORMAL` | 平价时段 |
| 低谷 | `LOW` | 低电价时段 |
| 深谷 | `DEEP_LOW` | 最低电价时段 |

## 8. 电价方案数据结构

### 8.1 分时电价配置

```json
{
  "timePeriods": [
    { "startTime": "00:00", "endTime": "06:00", "period": "DEEP_LOW" },
    { "startTime": "06:00", "endTime": "08:00", "period": "LOW" },
    { "startTime": "08:00", "endTime": "11:00", "period": "HIGH" },
    { "startTime": "11:00", "endTime": "13:00", "period": "NORMAL" },
    { "startTime": "13:00", "endTime": "17:00", "period": "HIGH" },
    { "startTime": "17:00", "endTime": "19:00", "period": "HIGHER" },
    { "startTime": "19:00", "endTime": "22:00", "period": "HIGH" },
    { "startTime": "22:00", "endTime": "24:00", "period": "LOW" }
  ]
}
```

### 8.2 费率配置

```json
{
  "prices": {
    "HIGHER": 1.20,
    "HIGH": 1.00,
    "NORMAL": 0.80,
    "LOW": 0.50,
    "DEEP_LOW": 0.30
  }
}
```

### 8.3 阶梯电价配置

```json
{
  "stepPrices": [
    { "minDegree": 0, "maxDegree": 200, "price": 0.50 },
    { "minDegree": 200, "maxDegree": 400, "price": 0.55 },
    { "minDegree": 400, "maxDegree": null, "price": 0.60 }
  ]
}
```

## 9. 预警方案数据结构

```json
{
  "warnLevels": [
    { "level": 1, "threshold": 100, "action": "NOTIFY" },
    { "level": 2, "threshold": 50, "action": "NOTIFY" },
    { "level": 3, "threshold": 20, "action": "SWITCH_OFF" }
  ]
}
```

## 10. 系统配置项

| 配置 Key | 说明 |
|----------|------|
| `DEFAULT_PRICE_PLAN_ID` | 默认电价方案 ID |
| `DEFAULT_WARN_PLAN_ID` | 默认预警方案 ID |

## 11. 电价计算流程

```
calculatePrice(meterId, degree, dateTime)
        |
        v
+-------+-------+
| 1. 获取电价方案 |
| (绑定方案或默认)|
+-------+-------+
        |
        v
+-------+-------+
| 2. 判断计费类型 |
| (分时/阶梯)    |
+-------+-------+
        |
   +----+----+
   |         |
   v         v
+-------+ +-------+
|分时计费| |阶梯计费|
+-------+ +-------+
   |         |
   v         v
+-------+ +-------+
|按时段  | |按档位  |
|查价格  | |查价格  |
+-------+ +-------+
   |         |
   +----+----+
        |
        v
+-------+-------+
| 3. 计算金额    |
| degree × price|
+-------+-------+
```
