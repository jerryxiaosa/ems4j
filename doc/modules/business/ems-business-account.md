# ems-business-account 模块文档

## 1. 模块概述

`ems-business-account` 是账户管理核心模块，负责用电账户的全生命周期管理，包括开户、销户、账户信息维护、余额预警等功能。

## 2. 核心功能

| 功能 | 说明 |
|------|------|
| 开户 | 创建用电账户，绑定业主、电表、计费方案 |
| 销户 | 账户注销，余额清算，解绑电表 |
| 账户查询 | 分页查询、详情查询；分页支持 `ownerNameLike`（账户归属名称模糊搜索），返回 `openedMeterCount`（已开户电表数）、`totalOpenableMeterCount`（可开户电表总数）与展示字段（`ownerTypeName`、`electricAccountTypeName`），详情返回电表明细列表 |
| 账户附加信息 | 统一提供候选电表查询、可开户电表总数统计、展示电费余额聚合（按计费类型口径） |
| 余额预警 | 低余额预警、欠费预警 |
| 账户充值 | 通过订单模块进行充值 |

## 3. 模块依赖关系

```
+---------------------------+
|   ems-business-account    |
+---------------------------+
            |
            | 依赖
            v
+---------------------------+     +---------------------------+
|   ems-business-device     |     |   ems-business-finance    |
| (电表管理服务)             |     | (余额、订单、消费服务)      |
+---------------------------+     +---------------------------+
            |                               |
            v                               v
+---------------------------+     +---------------------------+
|   ems-business-plan       |     | ems-foundation-user       |
|  (电价方案、预警方案)       |     |     (用户服务)            |
+---------------------------+     +---------------------------+
                                            |
                                            v
                                  +---------------------------+
                                  | ems-foundation-organization|
                                  |     (组织服务)             |
                                  +---------------------------+
            |
            v
+---------------------------+     +---------------------------+
| ems-components-datasource |     |   ems-components-lock     |
+---------------------------+     +---------------------------+
            |
            v
+---------------------------+
|       ems-common          |
+---------------------------+
```

## 4. Service 内部依赖

### 4.1 AccountManagerServiceImpl 依赖

```
+-------------------------------------+
|     AccountManagerServiceImpl       |
+-------------------------------------+
                |
    +-----------+-----------+-----------+
    |           |           |           |
    v           v           v           v
+-------+  +--------+  +---------+  +--------+
|本模块  |  |device  |  | finance |  | plan   |
|Service|  | 模块    |  |  模块   |  |  模块   |
+-------+  +--------+  +---------+  +--------+
    |           |           |           |
    v           v           v           v
+------------------+  +------------------+  +------------------+
|AccountInfoService|  |ElectricMeter     |  | BalanceService   |
+------------------+  |  ManagerService  |  +------------------+
                      +------------------+  | OrderService     |
                      |ElectricMeterInfo |  +------------------+
                      |    Service       |  |AccountConsume    |
                      +------------------+  |    Service       |
                                            +------------------+
    |
    v
+------------------+     +------------------+
|  foundation      |     |   components     |
+------------------+     +------------------+
|OrganizationServ. |     | LockTemplate     |
+------------------+     +------------------+
                         | RequestContext   |
                         +------------------+
```

## 5. 关键业务流程

### 5.1 开户流程（`openAccount`）

该流程由 `AccountManagerServiceImpl.openAccount` 驱动，包含“账户创建 + 电表开表”两段，整体在事务中执行。

| 步骤 | 核心动作 | 关键校验/规则 | 失败处理 |
|------|----------|---------------|----------|
| 1 | 获取业主级分布式锁 `LOCK:OWNER:{ownerType}:{ownerId}` | 同一业主不允许并发开户 | 获取失败直接报错“账户正在操作，请稍后重试” |
| 2 | 校验开户参数 | 账户是否已存在；企业业主是否存在；按账户类型校验参数（包月必须有月租费，按量必须有电价方案+预警方案） | 抛业务异常，流程终止 |
| 3 | 创建账户主记录 | 包月账户清空电价/预警方案；按量账户清空月租费；初始化预警状态为 `NONE` | 抛业务异常，事务回滚 |
| 4 | 初始化账户余额 | 调用 `BalanceService.initAccountBalance` 创建账户余额记录 | 抛业务异常，事务回滚 |
| 5 | 包月首月扣费（仅包月） | 调用 `AccountConsumeService.monthlyConsume` 扣首月月租费 | 抛业务异常，事务回滚 |
| 6 | 组装开表参数并执行开表 | 构建 `MeterOpenDto`，按账户类型下发电价/预警参数，调用 `ElectricMeterManagerService.openMeterAccount` | 抛业务异常，事务回滚 |
| 7 | 返回开户结果 | 返回新建 `accountId` | finally 中释放业主锁 |

补充说明：

- 开户和开表在同一事务边界内，任一环节失败会整体回滚。
- 电表列表的具体业务校验和开表动作在 `ElectricMeterManagerService` 内继续细化执行。

### 5.2 销户流程（`cancelAccount`）

该流程由 `AccountManagerServiceImpl.cancelAccount` 驱动，支持“部分销户（销部分电表）”和“全部销户（账户软删除）”。

| 步骤 | 核心动作 | 关键校验/规则 | 失败处理 |
|------|----------|---------------|----------|
| 1 | 获取账户级分布式锁 `LOCK:ACCOUNT:{accountId}` | 同一账户不允许并发销户 | 获取失败直接报错“账户正在操作，请稍后重试” |
| 2 | 读取账户并校验销户电表 | 校验电表数量一致、且全部属于当前账户 | 抛业务异常，流程终止 |
| 3 | 生成销户编号 | 生成 `cancelNo`，用于串联销户记录与销户订单 | 编号生成异常则终止 |
| 4 | 执行销表 | 调用 `ElectricMeterManagerService.cancelMeterAccount`，返回每块电表清算结果 | 抛业务异常，事务回滚 |
| 5 | 判定是否“全销户” | 查询账户下是否仍有在用电表 | 仅无剩余电表时进入账户删除路径 |
| 6 | 计算清算金额 | 按量账户汇总电表余额；非按量且全销户时读取账户余额；金额按 2 位小数向下取整并计算忽略金额 | 数据缺失抛业务异常 |
| 7 | 写入销户记录 | 保存 `energy_account_cancel_record`（销户类型、金额、备注、是否全销户等） | 抛业务异常，事务回滚 |
| 8 | 创建线下销户订单 | 创建 `TerminationOrderCreationInfoDto`，支付渠道固定 `OFFLINE` | 抛业务异常，事务回滚 |
| 9 | 全销户时软删除账户 | 调用 `repository.softDelete` 标记账户删除 | 抛业务异常，事务回滚 |
| 10 | 返回销户结果 | 返回 `cancelNo`、清算方向（退款/补缴/跳过）和金额；Web 对外额外补充无符号展示金额文本 | finally 中释放账户锁 |

补充说明：

- 清算方向规则：金额 `>0` 为退款，`<0` 为补缴，`=0` 为跳过清算。
- 订单创建和销户记录均在同一事务内，保证业务数据一致性。
- 内部业务金额口径保持不变：`CancelAccountResponseDto.amount`、销户记录 `cleanBalanceReal`、销户订单金额均保留符号，用于表达清算方向。
- 对外接口口径：`POST /accounts/cancel`、`GET /accounts/cancel/page`、`GET /accounts/cancel/{cancelNo}` 在保留原有金额字段（兼容存量调用方）的同时，新增 `cleanBalanceAmountText` 作为前端展示金额。
- 销户时间字段 `cancelTime` 的对外返回格式为 `yyyy-MM-dd HH:mm:ss`，例如 `2026-02-28 10:58:46`，不再返回 ISO 默认的 `2026-02-28T10:58:46`。
- `cleanBalanceAmountText` 规则：取原金额绝对值、固定保留两位小数，例如 `100 -> "100.00"`、`-50 -> "50.00"`、`0 -> "0.00"`。
- 前端展示约束：金额方向必须依赖 `cleanBalanceType` 判断；金额展示值统一使用 `cleanBalanceAmountText`，不要再根据 `amount` / `cleanBalanceReal` 的正负号自行推导文案。
- `cleanBalanceAmountText` 由 Web 层格式化器 `AbsoluteMoneyScale2TextFormatter` 生成，属于展示字段，不参与业务计算。

### 5.3 账户分页查询流程（`findAccountPage`）

该流程由 `AccountBiz.findAccountPage` 驱动，目标是“分页结果 + 批量统计字段补齐”。

| 步骤 | 核心动作 | 关键校验/规则 | 失败处理 |
|------|----------|---------------|----------|
| 1 | 接收查询参数 | HTTP 入参为 `includeDeleted`、`ownerType`、`ownerNameLike`、`electricAccountType`、`warnPlanId` | 参数转换失败返回请求错误 |
| 2 | VO 转 DTO | `AccountQueryVo` 转为 `AccountQueryDto` | 若转换结果为空则兜底为默认 DTO |
| 3 | 按归属名称模糊查询 | 当 `ownerNameLike` 有值时，直接按 `energy_account.owner_name like` 过滤 | 无命中时返回空分页 |
| 4 | 执行分页查询 | 调用 `AccountInfoService.findPage` 查询账户数据 | 下游异常透传业务异常 |
| 5 | 批量填充已开户电表数 | 基于账户ID集合一次性查询电表列表并分组统计 `openedMeterCount` | 空列表直接跳过填充 |
| 6 | 批量填充可开户电表总数 | 调用 `AccountAdditionalInfoService.countTotalOpenableMeterByAccountOwnerInfoList` 一次性回填 `totalOpenableMeterCount`（分页链路复用已查询账户归属信息，避免账户查询 N+1） | 空列表直接跳过填充 |
| 7 | 批量填充展示电费余额 | 调用 `AccountAdditionalInfoService.findElectricBalanceAmountMap` 按账户计费类型口径回填 `electricBalanceAmount` | 空列表直接跳过填充 |

### 5.4 账户附加信息服务（`AccountAdditionalInfoService`）

该服务聚合账户列表/详情需要的附加读信息，统一收敛原候选电表、可开户统计与展示电费余额逻辑，避免 `ems-web` 编排层承载规则细节。

| 方法 | 说明 | 关键规则 |
|------|------|----------|
| `findCandidateMeterList` | 查询主体候选电表（租赁空间内、预付费、未开户） | 候选列表可展示离线表；当前仅对 `ENTERPRISE` 做主体存在性校验 |
| `countTotalOpenableMeterByAccountOwnerInfoList` | 按账户批量统计可开户电表总数 | 入参按 `accountId` 去重并默认返回 0；按 `owner_type + owner_id` 维度聚合租赁空间电表数 |
| `findElectricBalanceAmountMap` | 按账户批量计算展示电费余额 | `QUANTITY` 取电表余额合计，`MONTHLY/MERGED` 取账户余额，未知类型返回 0 |

## 6. 账户查询参数口径（`GET /accounts/page`）

| 参数名 | 是否必填 | 说明 |
|------|------|------|
| includeDeleted | 否 | 是否包含已删除账户 |
| ownerType | 否 | 业主类型枚举编码 |
| ownerNameLike | 否 | 账户归属名称模糊搜索（直接匹配 `owner_name`） |
| electricAccountType | 否 | 计费类型枚举编码 |
| warnPlanId | 否 | 预警方案 ID |

补充说明：

- `ownerId`、`ownerIds` 已从 HTTP 查询参数移除，不再由前端直接传入。
- 查询链路由账户仓储层直接按 `owner_name` 模糊匹配，不再依赖组织域做 `ownerIds` 拼装。

## 7. 主体租赁模型约束（当前版本）

当前版本已将租赁关系建模为主体维度（`owner_type + owner_id`，关系表为 `energy_owner_space_rel`），用于支撑开户前租赁、组织/个人统一租赁入口等场景。

重要约束（避免误用）：

- `OwnerTypeEnum.ENTERPRISE`：当前对应组织主体。
- `OwnerTypeEnum.PERSONAL`：表示个人主体类型，但 **不等价于“个人主体最终存储就是用户表（`sys_user`）”**。
- 后续版本计划为个人主体引入独立存储/独立主体模型，届时 `PERSONAL` 的校验与查询入口会调整。

实现约束建议：

- 不要在新的业务代码中固化 `PERSONAL -> UserService` 的长期依赖假设。
- 涉及主体存在性校验、主体信息读取的逻辑，应收敛到 owner 维度的统一适配层，不要直接依赖 `UserService` 作为个人主体存在性校验入口（便于后续切换存储实现）。
