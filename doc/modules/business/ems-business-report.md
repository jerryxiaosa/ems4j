# ems-business-report 模块文档

## 1. 模块概述

`ems-business-report` 是报表读模型与日报构建模块，负责将设备、账户、计费、充值等源事实沉淀为稳定的日报快照，并对外提供电费报表查询能力。

当前版本重点覆盖两类能力：

- 日报构建：生成单日电表日报、单日账户日报和构建任务日志
- 电费报表查询：按统计区间查询账户级电费报表列表与单账户详情

## 2. 核心功能

| 功能 | 说明 |
|------|------|
| 单日电表日报构建 | 基于上报、开户、销户、充值、补正等事实生成 `energy_report_daily_meter` |
| 单日账户日报构建 | 基于电表日报、账户流水、包月扣费等事实生成 `energy_report_daily_account` |
| 任务日志 | 记录手工/定时重算的执行区间、状态、当前处理日期和错误信息 |
| 电费报表列表 | 支持账户名称 + 统计日期区间查询，返回本期电量、本期电费、充值金额、补正金额、合计费用 |
| 电费报表详情 | 返回账户汇总信息与区间内参与统计的去重电表汇总列表 |

## 3. 模块依赖关系

```
+----------------------------------+
|      ems-business-report         |
+----------------------------------+
                 |
                 +---> ems-business-account
                 |
                 +---> ems-business-device
                 |
                 +---> ems-business-billing
                 |
                 +---> ems-business-order
                 |
                 +---> ems-components-datasource
                 |
                 +---> ems-components-lock
                 |
                 +---> ems-common
```

依赖说明：

- `ems-business-account`：账户当前信息、账户开户快照
- `ems-business-device`：开户/销户记录与当前电表展示快照
- `ems-business-billing`：用电消费、补正、账户流水
- `ems-business-order`：充值到账与服务费事实
- `ems-components-lock`：日报全局互斥锁

## 4. 核心 Service

| Service | 职责 |
|---------|------|
| `DailyReportBuildService` | 统一驱动手工/定时日报重算 |
| `DailyMeterReportBuilder` | 构建单日电表日报 |
| `DailyAccountReportBuilder` | 构建单日账户日报 |
| `ElectricBillReportQueryService` | 电费报表列表与详情查询 |

## 5. 关键数据模型

| 表 | 说明 |
|----|------|
| `energy_report_daily_meter` | 电表日报快照，保存分时读数、分时电费、余额、补正、充值等字段 |
| `energy_report_daily_account` | 账户日报快照，保存账户聚合口径下的电量、电费、补正、充值、余额和累计值 |
| `energy_report_job_log` | 报表重算任务日志 |
| `energy_account_open_record` | 账户开户快照，提供稳定的账户类型与主体归属信息 |

## 6. 日报构建规则

### 6.1 触发方式

- 定时任务：每天凌晨 `1:30` 处理前一自然日
- 手工接口：`POST /v1/report/daily/build`

### 6.2 构建流程

1. 获取全局锁，避免并发重算任务同时运行
2. 按日期顺序逐天重建
3. 单日先删除旧快照，再按账户批次加载源事实
4. 先生成电表日报，再基于电表日报生成账户日报
5. 更新任务日志状态、完成时间和错误信息

### 6.3 账户批次与候选口径

- 按账户批次构建，默认每批 `200` 个账户
- 账户候选来源：
  - 前一日账户日报
  - 当日上报记录
  - 当日账户流水
  - 当日开户记录
  - 当日销户记录
- 电表候选来源：
  - 前一日电表日报
  - 当日电表上报快照
  - 当日开户/销户记录
  - 当日电表充值到账
  - 当日补正事实

## 7. 电费报表查询口径

### 7.1 列表接口

- 路径：`GET /v1/report/electric-bill/page`
- 查询条件：
  - `accountNameLike`
  - `startDate`
  - `endDate`
  - `pageNum`
  - `pageSize`

返回字段包括：

- 账户名称
- 电价计费类型
- 电表数量
- 本期电量
- 本期电费
- 充值金额
- 补正金额
- 合计费用

### 7.2 详情接口

- 路径：`GET /v1/report/electric-bill/{accountId}/detail`
- 查询条件：
  - `startDate`
  - `endDate`

详情分为两部分：

- 账户信息：联系人、联系方式、包月费用、统计结束日余额、本期汇总数据
- 电表信息：区间内参与统计的去重电表汇总列表

### 7.3 展示与计算规则

- 统计结束日不能选择今天
- 统计区间最大跨度 `65` 天
- 账户余额口径：统计结束日余额
- `MONTHLY` 账户的本期电费：统计区间内包月费用累计
- 合计费用：`本期电费 + 本期补正净额 + 充值服务费`
- 补正净额：`补缴金额 - 退费金额`
- `MERGED` / `MONTHLY` 账户电表详情中的单价、电费、充值、补正统一显示为空

## 8. 测试覆盖

当前模块已补充以下测试：

- builder 单测：日电表/日账户构建规则
- service 单测：电费报表列表/详情查询
- repository 集成测试：日报聚合 SQL 与源事实查询
- bootstrap 集成测试：日报构建、仓储与 H2 schema
