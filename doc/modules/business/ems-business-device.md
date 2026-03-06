# ems-business-device 模块文档

## 1. 模块概述

`ems-business-device` 是设备管理核心模块，负责电表和网关的全生命周期管理，包括设备档案、状态控制、开表/销表等业务功能。

## 2. 核心功能

| 功能 | 说明 |
|------|------|
| 电表管理 | 电表记录 CRUD、开表、销表 |
| 网关管理 | 网关记录 CRUD、网关下挂电表管理 |
| 状态控制 | 合闸/分闸、功率限制、强制控制 |
| 状态同步 | 设备在线状态同步、电量数据同步 |
| 设备查询 | 分页查询、条件筛选、详情查询 |

## 2.1 设备编号统一规则（当前版本）

当前版本已将电表和网关的主编号统一为 `deviceNo`，不再保留独立的 `meterNo` / `gatewayNo` 字段。

- 电表主表 `energy_electric_meter` 仅保留 `device_no`
- 网关主表 `energy_gateway` 仅保留 `device_no`
- 历史快照表（销表记录、电量记录、余额消费记录、充值订单明细）中的设备编号字段也统一为 `device_no`

实现约束：

- 电表、网关的查询条件、接口返回字段、业务日志与异常文案统一使用 `deviceNo`
- 电表和网关不再生成独立的系统编号

## 3. 模块依赖关系

```
+---------------------------+
|   ems-business-device     |
+---------------------------+
            |
            | 依赖
            v
+---------------------------+     +---------------------------+
|   ems-business-plan       |     |   ems-business-finance    |
| (电价方案、预警方案)        |---->| (余额服务、消费记录)        |
+---------------------------+     +---------------------------+
            |                               |
            v                               v
+---------------------------+     +---------------------------+
| ems-foundation-integration|     |   ems-foundation-space    |
| (设备命令、能源数据)        |     |     (空间/区域管理)        |
+---------------------------+     +---------------------------+
            |
            v
+---------------------------+
|   ems-foundation-system   |
|      (系统配置)            |
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

## 4. Service 内部依赖

### 4.1 ElectricMeterManagerServiceImpl 依赖

```
+-------------------------------------+
|   ElectricMeterManagerServiceImpl   |
+-------------------------------------+
                |
    +-----------+-----------+
    |           |           |
    v           v           v
+-------+  +--------+  +---------+
|本模块  |  |finance |  |  plan   |
|Service|  | 模块    |  |  模块   |
+-------+  +--------+  +---------+
    |           |           |
    v           v           v
+------------------+  +------------------+  +------------------+
|ElectricMeterInfo |  | BalanceService   |  |ElectricPricePlan |
|    Service       |  |                  |  |    Service       |
+------------------+  +------------------+  +------------------+
| GatewayService   |  |MeterConsumeServ. |  | WarnPlanService  |
+------------------+  +------------------+  +------------------+
    |
    v
+------------------+     +------------------+     +------------------+
|  foundation      |     |  foundation      |     |  foundation      |
| -integration     |     |    -space        |     |    -system       |
+------------------+     +------------------+     +------------------+
| DeviceCommand    |     |  SpaceService    |     |  ConfigService   |
|    Service       |     |                  |     |                  |
+------------------+     +------------------+     +------------------+
| EnergyService    |
| DeviceModelServ. |
| DeviceModuleCtx  |
+------------------+
```

## 5. 关键业务流程

### 5.1 开表流程（`openMeterAccount`）

该流程由 `ElectricMeterManagerServiceImpl.openMeterAccount` 驱动，核心是“绑定账户 + 下发方案 + 建立计量基线”，整体在事务中执行。

| 步骤 | 核心动作 | 关键校验/规则 | 失败处理 |
|------|----------|---------------|----------|
| 1 | 参数与电表状态校验 | `QUANTITY` 账户必须有电价方案和预警方案；输入电表必须全部存在；电表必须未开户、预付费、在线 | 抛业务异常，流程终止 |
| 2 | 初始化电表账户关系 | 逐表写入 `accountId`；按量账户初始化电表余额 | 抛业务异常，事务回滚 |
| 3 | 配置电表方案 | 仅 `QUANTITY` 配置电价和预警方案；`MONTHLY/MERGED` 不下发电表侧 `price_plan_id/warn_plan_id/warn_type` | 抛业务异常，事务回滚 |
| 4 | 批量计算并写入预警等级 | 基于余额阈值分组计算 `NONE/FIRST/SECOND` 并批量更新 | 抛业务异常，事务回滚 |
| 5 | 建立开户计量基线 | 保存开表记录、阶梯起点记录、初始电量记录（初始记录时间顺延 1 秒） | 抛业务异常，事务回滚 |
| 6 | 返回结果并结束 | 返回开表完成状态 | 主流程结束 |

补充说明：

- 阶梯记录会先清理旧 `is_latest`，再写入新年度起点，避免同表多条“当前记录”。
- 若开启“继承历史电量”，仅在历史记录年度与当前年度一致时继承偏移量。
- 电表侧字段职责不变：只有 `QUANTITY` 会在电表上配置 `price_plan_id/warn_plan_id/warn_type`。

### 5.2 合闸/分闸流程（`setSwitchStatus`）

该流程由 `ElectricMeterManagerServiceImpl.setSwitchStatus` 驱动，采用“先落目标状态，再下发命令”的执行模型。

| 步骤 | 核心动作 | 关键校验/规则 | 失败处理 |
|------|----------|---------------|----------|
| 1 | 校验电表可操作性 | 电表必须存在、已同步 IoT、且在线 | 抛业务异常，流程终止 |
| 2 | 写入目标开关状态 | 根据目标状态更新 `is_cut_off` | 数据库更新异常抛出 |
| 3 | 构建设备命令 | 按目标状态映射 `ENERGY_ELECTRIC_TURN_ON/OFF`，生成命令数据 | 参数不合法抛业务异常 |
| 4 | 保存并执行命令 | 调用 `DeviceCommandService` 保存命令并执行下发 | 执行失败抛业务异常 |
| 5 | 记录操作日志 | 记录原状态、目标状态、命令信息 | 日志失败不影响主流程 |

补充说明：

- 无论数据库当前状态与目标状态是否一致，都会执行下发命令，避免设备状态漂移后无法纠正。
- 失败时可结合命令记录进行重试或人工排障。

### 5.3 销表流程（`cancelMeterAccount`）

该流程由 `ElectricMeterManagerServiceImpl.cancelMeterAccount` 驱动，支持批量销表，核心是“逐表结算 + 统一解绑”。

| 步骤 | 核心动作 | 关键校验/规则 | 失败处理 |
|------|----------|---------------|----------|
| 1 | 遍历处理每块电表 | 为每块电表构建详情并进入销表子流程 | 任一电表失败时整体失败 |
| 2 | 生成销表电量记录 | 在线电表读取实时电量；离线电表要求人工传入分时电量 | 缺少离线电量输入抛业务异常 |
| 3 | 查询销表余额 | 按量账户读取电表余额；非按量账户余额为空 | 余额读取异常抛业务异常 |
| 4 | 计算历史阶梯累计电量 | 使用“当前总电量 - 阶梯起点 + 历史偏移”，并保护不小于 0 | 计算异常抛业务异常 |
| 5 | 写入销表记录 | 持久化 `MeterCancelRecord`（余额、电量、空间路径等） | 抛业务异常，事务回滚 |
| 6 | 批量清理账户绑定 | 统一清空电表 `accountId` 及相关账户字段 | 抛业务异常，事务回滚 |
| 7 | 返回销表结果列表 | 返回每块电表的余额与历史累计电量 | 上层继续执行账户销户清算 |

补充说明：

- 该方法为账户销户流程提供输入（每块电表的清算基础数据）。
- 批量销表在同一事务内执行，保证“记录写入”和“解绑关系”一致性。

### 5.4 电量读取口径（实时值 vs 最近一次上报记录）

设备模块当前同时提供两类读数查询能力，语义必须区分：

- 实时电量：`POST /device/electric-meters/{id}/power`
  - 直接调用设备能源服务读取当前值。
  - 请求体通过 `types` 指定读取口径，`0` 表示总读数，`1-5` 分别表示尖/峰/平/谷/深谷。
  - 返回结果仅包含 `type` 与 `value`，不包含上报时间。
  - 该接口是只读接口，不会将本次实时查询结果回写到 `energy_electric_meter_power_record`。

- 最近一次上报记录：`GET /device/electric-meters/{id}/latest-power-record`
  - 查询 `energy_electric_meter_power_record` 中该电表最近一次正式落库的上报记录。
  - 返回 `recordTime`、`power`、`powerHigher`、`powerHigh`、`powerLow`、`powerLower`、`powerDeepLow`。
  - `recordTime` 返回格式为 `yyyy-MM-dd HH:mm:ss`。
  - 若电表存在但当前没有任何上报记录，则返回 `data = null`。

使用约束：

- 详情页如果要展示“当前实时值”，调用 `/power`。
- 详情页如果要展示“最近一次上报时间与读数快照”，调用 `/latest-power-record`。
- 不要将“实时查询值”与“最近一次正式上报记录”混为同一业务语义。

### 5.5 在线状态与离线时长口径

电表主表当前同时维护：

- `is_online`：最近一次同步的在线状态
- `last_online_time`：最近一次确认该电表在线的时间

同步规则：

- 当同步结果为在线时，无论 `is_online` 是否变化，都会刷新 `last_online_time`
- 当同步结果为离线时，仅更新 `is_online = false`，不刷新 `last_online_time`

对外展示规则（Web 层统一格式化）：

- `isOnline = true`：`offlineDurationText = null`
- `isOnline = false && lastOnlineTime = null`：`offlineDurationText = null`
- `isOnline = false && lastOnlineTime != null`
  - 小于 1 小时：展示 `xx分钟`，不足 1 分钟按 `1分钟`
  - 大于等于 1 小时且小于 1 天：展示 `xx小时`
  - 大于等于 1 天：展示 `xx天`

## 6. 对外接口补充

### 6.1 设备品类树

- 路径：`GET /device/device-types/tree`
- 说明：返回整棵设备品类树，孤儿节点（`pid` 指向不存在父节点）会被忽略，不返回

### 6.2 设备型号查询

- 列表：`GET /device/device-models`
- 分页：`GET /device/device-models/page`

查询参数：

- `typeIds`
- `typeKey`
- `manufacturerName`
- `modelName`
- `productCode`

### 6.3 电表列表/详情展示字段

电表列表与详情当前统一返回以下展示字段：

- 空间展示：`spaceName`、`spaceParentNames`
- 型号展示：`modelName`
- 计费方案展示：`pricePlanName`
- 预警方案展示：`warnPlanName`
- 电费预警级别展示：`electricWarnTypeName`
- 在线展示：`offlineDurationText`

其中：

- `modelName`、`pricePlanName`、`warnPlanName`、`electricWarnTypeName` 由 translate 组件自动翻译
- `spaceName`、`spaceParentNames`、`offlineDurationText` 由 Web 层批量 enrich 组装
