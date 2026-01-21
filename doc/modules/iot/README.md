# ems-iot 文档索引

本文仅提供 `ems-iot` 模块文档索引，具体内容已按主题拆分为独立文档，便于评审与维护。

## 1. 开发/设计类文档

- `docs/modules/iot/architecture-layering.md`：分层与模块边界（api → application → domain → infrastructure → plugins）。
- `docs/modules/iot/netty-multi-protocol.md`：Netty 多协议接入设计、首包探测、动态解码器、分包/粘包处理与多连接隔离。
- `docs/modules/iot/channel-manager.md`：通道会话与命令下发（串行队列、ACK 处理）。
- `docs/modules/iot/protocol-integration-guide.md`：新协议接入开发指南（目录结构与最小接入步骤）。
- `docs/modules/iot/vendor-extension-checklist.md`：新增厂商/协议/特殊产品接入清单。
- `docs/modules/iot/device-identity-mapping.md`：设备标识对齐与映射规范（iotId/deviceNo/portNo/meterAddress）。
- `docs/modules/iot/exception-handling-guidelines.md`：异常与空值处理规则（避免入站链路抛异常导致断链）。