# EMS4J：开源 EMS 管理系统

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

[English](README_EN.md)

EMS4J 是一款基于 Spring Boot 多模块架构的开源 EMS 管理系统（能源管理系统），支持预付费、能耗分析、IoT 设备接入、远程控表、账户管理和财务核算。系统支持按需、合并、包月等多种计费方式，兼容多协议设备接入，适合落地为校园宿舍、园区楼宇等典型场景。
**它同时也是一个适合学习复杂业务建模与 Spring Boot 多模块架构拆分的开源项目。**

## 核心功能

- 多协议设备接入
- 计费模式（按需付费 / 合并付费 / 包月付费）
- 计量计费（尖峰平谷 / 阶梯电价）
- 账户管理（开户 / 销户 / 充值）
- 日报快照与电费报表（单日电表/账户日报、电费报表列表与详情）
- 远程控制（合闸 / 分闸 / 复费率设置）
- 财务核算（账单 / 流水 / 对账）

## 适用场景

- 宿舍 EMS 管理系统
- 园区 EMS 管理系统
- 预付费能源管理系统
- 带 IoT 远程控表的 EMS 系统
- 需要参考多模块架构和复杂业务建模的 Java 项目

## 快速体验

### 在线体验

前端页面：[http://119.45.165.253:30080](http://119.45.165.253:30080)

演示账号：
- 用户名：`admin`
- 密码：`Abc123!@#`

### 本地运行

```bash
cp deploy/env.example .env
docker compose -f deploy/compose/docker-compose.full.yml up -d --build
```

默认访问地址：
- 前端页面：`http://127.0.0.1:4173`
- 后端 API 文档：`http://127.0.0.1:8080/doc.html`

说明：
- `docker-compose.full.yml` 会同时启动 frontend、backend、iot、iot-simulator 以及 MySQL / Redis / RabbitMQ
- 如果是首次启动，镜像构建和依赖初始化会稍慢一些
- 如果你更希望分开调试前后端，可以查看下方"开发与部署"章节

## 项目亮点与设计看点

- **完整的预付费业务闭环**
  覆盖开户、充值、扣费、预警、销户结算、订单支付与远程控制，不是单纯的 CRUD 后台。

- **清晰的多模块分层结构**
  采用 Spring Boot 多模块架构，`device / account / billing / order / lease / plan / aggregation` 等业务域职责明确，便于二次开发与阅读。

- **适合参考复杂业务建模**
  包含按需、合并、包月等计费模式，以及账户、电表、订单、账务之间的协同关系。

- **不止业务后台，还包含 IoT 接入链路**
  提供独立 `ems-iot` 模块，支持多协议接入、报文解析、命令下发与事件发布。

- **异步处理与幂等保护完整**
  包含 MQ、事务消息、重复上报处理与唯一索引兜底等工程实践。

- **可直接本地运行和体验**
  提供前后端工程、Docker 依赖环境、数据库脚本和运行说明，不是概念仓库。

## 核心页面预览

### 核心业务闭环

| 页面 | 截图 |
|------|------|
| 账户详情 | ![账户详情](resource/images/account-detail.png) |
| 销户结算 | ![销户结算](resource/images/clear-account.png) |
| 订单列表 | ![订单列表](resource/images/order-list.png) |
| 订单创建 | ![订单创建](resource/images/order-create.png) |

### 设备与计费能力

| 页面     | 截图                                             |
|--------|------------------------------------------------|
| 电表详情   | ![电表详情](resource/images/electric-detail.png)   |
| 用电趋势   | ![用电趋势](resource/images/meter-power-trend.png) |
| 电费报表   | ![电费报表](resource/images/energy-report.png)     |
| 电价方案详情 | ![电价方案详情](resource/images/electirc-detail.png) |
| 预警方案详情 | ![预警方案详情](resource/images/warn-detial.png)     |

## 预付费模式说明

系统支持按需、合并与包月三种计费模式。其中按需与合并模式都是根据实际的使用对余额扣费。按需模式每个水电表独立核算。合并模式是把金额充值在一个水电表上，其他的表都用这个金额。包月按周期固定金额结算。

典型流程为开户后进行充值，扣费并持续更新余额，余额不足或触达预警阈值时可联动通知与远程断闸。全量销户时会进行清算并生成结算订单，产生退费或补缴。

## 环境要求

| 组件 | 版本     | 必需 |
|------|--------|------|
| JDK | 17+    | 是 |
| Maven | 3.8+   | 是 |
| MySQL | 8.0+   | 是 |
| Redis | 6.0+   | 是 |
| RabbitMQ | 4.1+   | 否 |
| Node.js | 18.18+ | 前端开发/构建需要 |
| pnpm | 10.32+ | 前端开发/构建需要 |

## 开发与部署

先克隆项目：

```bash
git clone <repository-url>
cd ems4j
```

### 方式 A：Docker 本地开发模式

后端依赖中间件可使用 Docker Compose 拉起：

```bash
cp deploy/env.example .env
docker compose -f deploy/compose/docker-compose.infra.yml up -d
```

然后分别启动后端和前端：

```bash
# 后端
mvn clean package -DskipTests
java -jar ems-bootstrap/target/ems-*.jar --spring.profiles.active=dev

# 前端
cd frontend-web
pnpm install
pnpm dev
```

### 方式 B：Docker 全量运行

```bash
cp deploy/env.example .env
### 改模式运行模式使用：`ems-bootstrap/src/main/resources/application-docker.yml`
docker compose -f deploy/compose/docker-compose.full.yml up -d --build
```

说明：
- `deploy/compose/docker-compose.infra.yml`：只启动 MySQL / Redis / RabbitMQ
- `deploy/compose/docker-compose.full.yml`：同时启动 backend / frontend / iot / iot-simulator / middleware
- RabbitMQ 镜像已集成 `x-delayed-message` 插件
- `iot` 默认使用 `docker,netty` profile，对外监听 `8880` 和 `19500`
- `iot-simulator` 默认使用 `docker` profile，通过 `iot:19500` 连接 `iot`
- `iot-simulator` 默认把运行状态写入 `/app/.data/iot-simulator-state.json`
- 如果没有显式指定历史补投起止时间，`iot-simulator` 会默认补投“本月 1 号 00:00:00 到当前时间前 1 秒”的数据；若状态文件已存在，则按断点续传，不会每次从月初重放

### 方式 C：Helm / K3s 部署

项目已提供单机 K3s 场景下的 Helm Chart，适合把 `backend / frontend / iot / iot-simulator / mysql / redis / rabbitmq` 一起部署到 Kubernetes。

入口文档：
- [deploy/helm/README.md](/Users/jerry/Workspace/github/ems4j/deploy/helm/README.md)

当前 Helm 方案包含：
- `ems-infra`：部署 MySQL、Redis、RabbitMQ
- `ems-app`：部署 Backend、Frontend、IOT、IOT Simulator

使用前建议先准备：
- Harbor 或其他可访问的镜像仓库
- K3s 集群与命名空间 `ems-infra`、`ems-app`
- 对应命名空间下的镜像拉取密钥 `harbor-pull-secret`

Helm 文档中已经包含：
- 镜像构建与推送命令
- `ems-infra` / `ems-app` 安装命令
- 部署后检查命令
- `iot` 与 `iot-simulator` 的日志排查方式

### 方式 D：手工搭建环境

```bash
### 导入数据库
mysql -u <user> -p <db> < deploy/mysql/init/001-ems.sql
mysql -u <user> -p <db> < deploy/mysql/init/002-menu.sql
mysql -u <user> -p <db> < deploy/mysql/init/003-example.sql
mysql -u <user> -p <db> < deploy/mysql/init/101-iot.sql

### 安装RabbitMQ x-delayed-message 插件
### @see https://github.com/rabbitmq/rabbitmq-delayed-message-exchange
```

编辑 `ems-bootstrap/src/main/resources/application-dev.yml`：
- 数据库连接（`spring.datasource`）
- Redis 连接（`spring.data.redis`）
- RabbitMQ 连接（`spring.rabbitmq`，可选）

前端代理目标默认是 `http://127.0.0.1:8080`，可通过环境变量覆盖：

```bash
cd frontend-web
VITE_PROXY_TARGET=http://127.0.0.1:18080 pnpm dev
```

构建并启动：

```bash
mvn clean package -DskipTests
java -jar ems-bootstrap/target/ems-*.jar --spring.profiles.active=dev
```

## 构建与测试

```bash
# 全量构建（跳过测试）
mvn clean install -DskipTests

# 运行测试
mvn test

# 模块级构建/测试（示例）
mvn -pl ems-business/ems-business-device -am test

# 前端
cd frontend-web
pnpm typecheck
pnpm test:unit
pnpm test:unit:coverage
pnpm test:e2e
```

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言/框架 | Java 17 / Spring Boot 3.5 |
| 持久层 | MyBatis-Plus / MySQL 8.0 |
| 缓存 | Redis / Redisson |
| 消息队列 | RabbitMQ（可选） |
| IoT 接入 | Netty |
| 认证 | Sa-Token + JWT |
| API 文档 | Knife4j / SpringDoc OpenAPI |

## 模块分层架构

```
+-------------------------------+          +-------------------------------+
|        ems-bootstrap          |          |           ems-iot             |
|       (Web Service Entry)     |          |      (IoT Service Standalone) |
+-------------------------------+          +-------------------------------+
               |                                          |
   +-----------+-----------+-----------+                  |
   |           |           |           |                  |
+--v-----+ +---v----+ +----v-------+   |                  |
| ems-web| | ems-mq | |ems-schedule|   |                  |
|(HTTP   | | (Msg)  | |  (Schedule)|   |                  |
| API)   | |        | |            |   |                  |
+--+-----+ +---+----+ +-----+------+   |                  |
   |           |            |          |                  |
   +-----------+------------+----------+------------------+
               |
+------------------------------------------------------------------+
|                        ems-business                               |
|    +------------+  +------------+  +------------+  +------------+ |
|    |   device   |  |  account   |  |  billing   |  |    order   | |
|    | (Device Mgmt)| | (Account   |  | (Balance & |  | (Trade &   | |
|    |            |  |  Mgmt)     |  |  Consume)  |  |  Payment)  | |
|    +------------+  +------------+  +------------+  +------------+ |
|    |    lease   |  |    plan    |  | aggregation|                 |
|    | (Owner &   |  | (Pricing   |  | (Cross-    |                 |
|    |  Space)    |  |  Plan)     |  | domain)    |                 |
|    +------------+  +------------+  +------------+                 |
+------------------------------------------------------------------+
               |
   +-----------+-----------+
   |                       |
+--v-------------------+  +v-----------------------+
|    ems-foundation    |  |    ems-components      |
| +------+ +---------+ |  | +----------+ +------+  |
| | user | |integrat.| |  | |datasource| | lock |  |
| +------+ +---------+ |  | +----------+ +------+  |
| +------+ +---------+ |  | +---------+ +-------+  |
| | space| | system  | |  | | context | | redis |  |
| +------+ +---------+ |  | +---------+ +-------+  |
| +------+ +---------+ |  +------------------------+
| | org  | | notifi. | |
| +------+ +---------+ |
+----------------------+
               |
       +-------v-------+
       |  ems-common   |
       | (Common Utils)|
       +---------------+
```

说明：
- ems-web 可直接依赖 ems-business 与 ems-foundation（用户/组织/空间/系统等基础域）。
- ems-web 仅依赖 service/dto，避免直接引用 repository/entity/mapper。
- ems-foundation 不反向依赖 ems-business/ems-web，保持基础域可复用。

## 数据流向

```
+----------+    命令下发    +----------+    协议转换    +----------+
|  ems-web |-------------->|  ems-iot |-------------->|   设备    |
+----------+               +----------+               +----------+
     ^                          |                          |
     |                          | 数据上报                  |
     |                          v                          |
     |                   +----------+                      |
     +-------------------|  业务层   |<---------------------+
        业务结果          +----------+
                              |
                              v
                        +----------+
                        |   MySQL  |
                        +----------+
```


## 模块说明

| 模块                            | 职责                                               |
|-------------------------------|--------------------------------------------------|
| `ems-bootstrap`               | 应用启动入口（Spring Boot）                              |
| `ems-web`                     | HTTP 接口层                                         |
| `ems-business-device`         | 电表、网关、设备档案管理                                     |
| `ems-business-account`        | 开户、销户、余额、充值                                      |
| `ems-business-billing`        | 余额、抄表消费、补正、账务流水                                 |
| `ems-business-order`          | 订单创建、支付回调、订单查询与完成处理                              |
| `ems-business-lease`          | 主体与空间租赁关系、租赁查询与退租校验                             |
| `ems-business-plan`           | 计费方案、费率、尖峰平谷时段                                   |
| `ems-business-aggregation`    | 跨域读聚合与应用层编排                                        |
| `ems-foundation-user`         | 用户认证、权限、角色                                       |
| `ems-foundation-organization` | 多租户、组织架构                                         |
| `ems-foundation-space`        | 空间/区域管理                                          |
| `ems-foundation-system`       | 系统配置                                             |
| `ems-foundation-integration`  | 第三方平台对接                                          |
| `ems-components-*`            | 通用组件（数据源/锁/上下文）                                  |
| `ems-mq-*`                    | 消息基础设施 API（ems-mq-api）与业务消息应用层实现（ems-mq-rabbitmq） |
| `ems-iot`                     | Netty 设备接入、协议解析                                  |
| `ems-iot-simulator`           | IoT 设备模拟器，当前支持安科瑞 4G 直连电表的 TCP 接入、历史补投、实时上报与基础命令响应 |
| `ems-schedule`                | 定时任务                                             |
| `frontend-web` | Vue 3 + TypeScript 前端管理台，含 Vitest 单测与 Playwright 冒烟回归 |
| `deploy` ![TRY IT](https://img.shields.io/badge/TRY%20IT-brightgreen)      | 部署资产目录，包含 Docker Compose、Helm Chart、Dockerfile、初始化 SQL、环境示例与 K3s 部署说明 |

说明：
- ems-mq-api 提供消息契约与基础服务接口（基础设施层）。
- ems-mq-rabbitmq 属于业务消息应用层实现，承载业务消息监听与编排。
- 前端详细说明见 [`frontend-web/README.md`](frontend-web/README.md)。

## 已支持设备

| 厂商 | 类型 |
|------|------|
| 安科瑞 (Acrel) | 电表 / 网关 |
| 斯菲尔 (Sfere) | 电表 |
| 仪歌 (Yige) | 电表 |
| 燕赵 (Yke) | 电表 |

## IoT 平台对接

对接方式可分为两类：

1) **设备直连（自有平台）**
- 在 `ems-iot` 中实现协议接入、报文解析、命令翻译与事件发布。
- 推荐参考：
  - [协议接入指南](doc/modules/iot/protocol-integration-guide.md)
  - [Netty 多协议](doc/modules/iot/netty-multi-protocol.md)

2) **第三方 IoT 平台接入**
- 建议在 `ems-foundation/integration` 侧实现平台对接与数据同步，再与 `ems-iot`/业务域协同。
- 推荐参考：
  - [设备集成模块说明](doc/modules/foundation/ems-foundation-integration.md)

详细平台集成解决方案请参见：
- [IoT 平台集成解决方案](doc/iot-platform-integration-solutions.md)

## 开发文档

| 文档 | 说明 |
|------|------|
| [开发实践指南](doc/development-practices-guide.md) | 代码风格、命名约定及开发实践 |
| [业务模块文档](doc/modules/business/README.md) | 业务模块文档（设备、账户、财务、方案） |
| [基础模块文档](doc/modules/foundation/README.md) | 基础模块文档（用户、组织、空间、系统、集成） |
| [IoT 模块文档](doc/modules/iot/README.md) | IoT 模块文档，用于设备接入和协议集成 |
| [测试指南](doc/test-guidelines.md) | 单元测试和集成测试标准及最佳实践 |

## 动机

这个项目源于对真实复杂业务系统的一次持续整理与重构。

在能源预付费场景中，设备、账户、计费、订单、权限和远程控制之间存在大量交叉关系。随着业务演进，如果模块边界不清晰，代码很容易变得难以维护，也难以支持后续扩展。

EMS4J 试图解决的，不只是"把功能做出来"，而是把这些复杂关系拆清楚：哪些能力应该属于 `device`，哪些应该属于 `billing`，哪些应该从 `account` 中独立出去，哪些逻辑应该留在上层编排。

因此，这个项目既是一个闭环的能源预付费系统，也是一次关于复杂业务建模、模块边界治理和工程可维护性的实践。

如果这个项目能给你带来一些启发，欢迎点个 ⭐️ 支持一下。

## 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE)。

## 联系方式

- 添加我的微信，请注名"ems4j"：
  
  <img src="resource/images/wechat.png" alt="微信二维码" width="220" />

- 知乎专栏：[能源管理系统实践](https://www.zhihu.com/column/c_2017220125376401881)
