# EMS4J

EMS4J是一个专注于能耗管理的综合性业务平台，具备设备接入、计量计费、账户管理、财务核算与运维支持等核心能力。
该平台提供了高度抽象的设备对接方案，能够灵活适配各类物联网平台，同时也支持通过内置的IoT模块自主实现设备接入，具备良好的扩展性。

## 技术栈

- Java 17 / Spring Boot / Maven
- MyBatis / JUnit 5 / Mockito
- Netty（IoT 接入与协议处理）
- RabbitMQ（消息能力，可选）

## 模块功能说明

- `ems-bootstrap`：应用启动入口（Spring Boot）。
- `ems-web`：HTTP 接口层与 API 聚合。
- `ems-business/*`：核心业务域（设备、账户、计费、财务、汇总等）。
- `ems-foundation/*`：基础服务能力（系统、用户、区域、组织、集成、通知）。
- `ems-components/*`：通用组件（数据源、分布式锁、上下文等）。
- `ems-mq/*`：消息能力（API 与 RabbitMQ 实现）。
- `ems-iot`：设备接入、协议解析、命令下发与事件处理。
- `ems-schedule`：定时任务与后台作业。
- `doc/`：文档资料。
- `sql/`：数据库初始化脚本。

## 模块依赖关系（ASCII）

```
                      +-------------------+
                      |   ems-bootstrap   |
                      +---------+---------+
                                |
        +-----------------------+-----------------------+
        |                       |                       |
     +--v---+               +--v---+               +----v-----+
     |ems-web|              |ems-iot|              |ems-schedule|
     +--+---+               +--+---+               +----+-----+
        \                     |                       /
         \                    |                      /
          +--------------------v---------------------+
          |              ems-business                |
          +--------------------+---------------------+
                               |
                 +-------------+-------------+
                 |                           |
        +--------v--------+         +--------v--------+
        | ems-foundation  |         | ems-components  |
        +-----------------+         +-----------------+

```

## 快速开始

1) 初始化数据库

```
mysql -u <user> -p <db> < sql/ems.sql
```

2) 启动服务

```
mvn clean package -DskipTests
java -jar ems-bootstrap/target/ems-0.1.0.jar
```

## 构建与测试

```
# 全量构建（跳过测试）
mvn clean install -DskipTests

# 运行测试
mvn test

# 模块级构建/测试（示例）
mvn -pl ems-business/ems-business-device -am test
```

## IoT 平台对接说明

对接方式可分为两类：

1) **设备直连（自有平台）**
- 在 `ems-iot` 中实现协议接入、报文解析、命令翻译与事件发布。
- 推荐参考：
  - `doc/modules/iot/protocol-integration-guide.md`
  - `doc/modules/iot/vendor-extension-checklist.md`
  - `doc/modules/iot/netty-multi-protocol.md`

2) **第三方 IoT 平台接入**
- 建议在 `ems-foundation/integration` 侧实现平台对接与数据同步，再与 `ems-iot`/业务域协同。
- 推荐参考：
  - `doc/modules/integration/ems-foundation-integration-overview.md`

## IoT 模块说明
目前已实现安科瑞、斯菲尔、仪歌、燕赵等设备和表具的对接

- 设计与分层：`doc/modules/iot/architecture-layering.md`
- 协议接入指南：`doc/modules/iot/protocol-integration-guide.md`
- 设备标识映射：`doc/modules/iot/device-identity-mapping.md`
- Netty 多协议与通道管理：
  - `doc/modules/iot/netty-multi-protocol.md`
  - `doc/modules/iot/channel-manager.md`
- 异常处理规范：`doc/modules/iot/exception-handling-guidelines.md`

## 代码规范

- 开发规范：`doc/development-practices-guide.md`
- 单元测试规范：`doc/test/unit-test-guidelines.md`

## 许可证

- 本项目采用 MIT 许可证，详见 `LICENSE`。

## 联系方式

- 邮箱：jerryxiaoff@163.com
