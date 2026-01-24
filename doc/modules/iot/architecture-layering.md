# ems-iot 分层与模块边界（api → application → domain → protocol → infrastructure → plugins）

## 1. 分层职责

- **api**：对外 HTTP API；仅做参数校验/组装并调用 application。主要包括 `DeviceController` 和 `CommandController`。
- **application**：用例编排（设备管理、命令下发、状态查询）；负责事务边界与跨领域协调。主要包括 `DeviceAppService`、`CommandAppService`、`EnergyReportAppService` 和 `DeviceVendorFacade`。
- **domain**：领域模型与端口契约（如 `Device`、`DeviceCommand`、`DeviceCommandRequest`），不依赖技术细节。包含 `port`、`model`、`command` 等子包。
- **protocol**：协议层契约与抽象（如 `ProtocolSignature`、`DeviceProtocolHandler`、`ProtocolMessageContext`），不依赖基础设施实现。包含 `port`、`packet`、`inbound`、`outbound` 等子包。
- **infrastructure**：Netty/MQ、设备持久化、路由注册、会话管理等技术实现；协议扩展点集中在 `infrastructure.transport.netty.spi`。包含 `persistence`、`registry`、`transport` 等子包。
- **plugins**：厂商插件（如安科瑞），实现 `DeviceProtocolHandler`，并可提供协议探测/解码器扩展（`ProtocolDetector`、`FrameDecoderProvider`）。

## 2. 边界与依赖约束

- api 只依赖 application，不直接依赖 infrastructure/plugins。
- application 依赖 domain/protocol 与基础服务，不直接依赖 plugins 实现。
- domain 不依赖 protocol/infrastructure/plugins，保持纯模型定义。
- protocol 依赖 domain，不依赖 infrastructure/plugins。
- infrastructure 通过 domain/protocol 端口访问业务能力，不直接耦合具体厂商。
- plugins 仅实现 protocol 端口，不反向依赖 application。
