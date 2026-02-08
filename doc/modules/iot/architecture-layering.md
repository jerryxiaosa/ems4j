# ems-iot 分层与模块边界（api → application → domain → protocol → infrastructure → plugins）

## 1. 分层职责

- **api**：对外 HTTP API；仅做参数校验/组装并调用 application。主要包括 `DeviceController` 和 `CommandController`。
- **application**：用例编排（设备管理、命令下发、状态查询）；负责事务边界与跨领域协调。主要包括 `DeviceAppService`、`CommandAppService`、`DeviceVendorFacade` 与事件监听的 `ProtocolInboundEventListener`。
- **domain**：领域模型与端口契约（如 `Device`、`DeviceCommand`、`DeviceCommandRequest`），不依赖技术细节。
  - `model/`：领域模型与聚合根（设备、产品等）。
  - `command/`：领域命令与命令参数模型，`concrete/` 放具体命令。
  - `port/`：领域端口接口（如仓储、注册器、网关访问）。
  - `event/`：领域事件对象（供应用层编排与外部发布）。
- **protocol**：协议层契约与抽象（如 `ProtocolSignature`、`DeviceProtocolHandler`、`ProtocolMessageContext`），不依赖基础设施实现。
  - `port/`：协议端口定义，含 `inbound/`、`outbound/`、`registry/`、`session/`。
  - `packet/`：协议报文模型与解析入口。
  - `decode/`：协议解码结果与解析流程抽象。
  - `event/`：协议层事件模型（上行、异常等）。
  - `modbus/`：通用 Modbus 协议模型与请求/响应结构。
- **infrastructure**：Netty/MQ、设备持久化、路由注册、会话管理等技术实现；协议扩展点集中在 `infrastructure.transport.netty.spi`。
  - `persistence/`：设备数据持久化（`entity/`、`repository/`）。
  - `registry/`：设备注册、协议处理器注册等实现。
  - `transport/`：传输层实现（`netty/`、`mqtt/` 等）。
  - `event/`：基础设施事件发布/订阅与适配。
- **plugins**：厂商插件（如安科瑞），实现 `DeviceProtocolHandler`，并可提供协议探测/解码器扩展（`ProtocolDetector`、`FrameDecoderProvider`）。

## 2. 边界与依赖约束

- api 只依赖 application，不直接依赖 infrastructure/plugins。
- application 依赖 domain/protocol 与基础服务，不直接依赖 plugins 实现。
- domain 不依赖 protocol/infrastructure/plugins，保持纯模型定义。
- protocol 依赖 domain，不依赖 infrastructure/plugins。
- infrastructure 通过 domain/protocol 端口访问业务能力，不直接耦合具体厂商。
- plugins 仅实现 protocol 端口，不反向依赖 application。


## 3. 典型依赖路径

- **HTTP 请求链路**：`api/` → `application/` → `domain/port` → `infrastructure/persistence`
- **协议上行链路**：`protocol/port/inbound` → `plugins/` → `protocol/event` → `listener/`
- **协议下行链路**：`application/` → `domain/command` → `protocol/packet` → `plugins/` → `infrastructure/transport`
- **网关设备解析**：`plugins/*/parser` → `protocol/packet` → `protocol/event/inbound`
- **设备注册读取**：`application/` → `domain/port` → `infrastructure/registry`
