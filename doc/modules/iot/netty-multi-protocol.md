# ems-iot Netty 多协议接入设计

## 1. 单实例 Netty + 动态解码器安装

默认使用**单实例 Netty Server（单端口）**，通过“首包探测 + 动态安装解码器”实现多厂商/多协议共存：

- Pipeline 初始仅包含：
  - `ProtocolFrameDecoder`：首包探测并安装专用解码器链。
  - `MultiplexTcpHandler`：消费“完整帧”，构造 `SimpleProtocolMessageContext` 并路由到插件 handler。
- `ProtocolFrameDecoder` 行为：
  1) 截取快照（前 N 字节）依次调用 `NettyProtocolDetector.detectTcp`
  2) 探测到 `ProtocolSignature` 后写入 `ChannelAttributes.PROTOCOL_SIGNATURE`
  3) 根据签名选择 `NettyFrameDecoderProvider`，把对应解码器链插入 pipeline（长度型/起止符型等）
  4) 将缓存字节重新投递给新解码器，再移除自身

## 2. 关键扩展点

- `NettyProtocolDetector`：只做“识别”，返回 `ProtocolSignature`（vendor/accessMode/productCode/transportType）。
- `NettyFrameDecoderProvider`：只做“签名 → 解码器链”，返回一组 `ChannelHandler`（可多个）。
- `ProtocolHandlerRegistry`：按 `ProtocolSignature`（vendor/accessMode/productCode/transportType）路由到 `DeviceProtocolHandler`。

## 3. 插件解码与路由流程（ASCII）

```
TCP Connection
   |
   v
ProtocolFrameDecoder  --首包探测-->  ProtocolSignature (vendor, accessMode, productCode, transportType)
   |                                     |
   |                                     v
   |                            NettyFrameDecoderProvider (由插件提供)
   |                                     |
   |                         +-----------+------------+
   |                         |                        |
   |                     网关协议                 电表/其他协议
   |                         |                        |
   v                         v                        v
pipeline add:           长度型解码器链           起止符解码器链
   |                         |                        |
   +-----------(解帧输出 byte[])----------------------+
                             |
                             v
                 MultiplexTcpHandler (封装 SimpleProtocolMessageContext)
                             |
                             v
           ProtocolHandlerRegistry.resolve(signature) -> 插件 DeviceProtocolHandler
                             |
                             v
                 业务处理 / 事件发布 / ACK 归还 ProtocolCommandTransport (ChannelManager)
```

## 4. 分包/粘包处理原则（解码器层解决）

分包/粘包统一在 Netty 解码器层处理，业务层只消费“完整帧”：

- **起止符型**（如 `7B7B...7D7D`）：
  - 允许首字节不是起始符：扫描找到 `7B7B` 再开始组帧
  - 未读到结束符 `7D7D` 时缓存等待后续字节
  - 超长保护：超过最大帧长丢弃并重置
  - CRC 校验失败：丢弃或继续重同步（策略由协议决定）
- **长度型**（如 `1F1F + Length`）：
  - 按长度字段聚合完整帧
  - 超长/非法长度直接丢弃并重置

新增协议时，建议实现新的解码器并通过 `NettyFrameDecoderProvider` 注册，不修改 `ProtocolFrameDecoder`。

## 5. 多连接并发与隔离

- 每个连接（Channel）天然隔离：解码缓存、签名探测结果、Channel Attribute、发送队列/回执 Future 均绑定到 `ChannelSession` 或 `Channel`。
- 连接关闭时必须清理：在 `channelInactive`/`exceptionCaught` 时触发 `ChannelManager.remove(...)`，将队列中的 Future 统一异常完成，避免泄漏与调用方永久等待。

## 6. 何时拆分多个 Netty 服务（多端口/多进程）

默认推荐单端口单实例，只有在以下场景才建议拆分：

- 不同厂商需要网络/安全域强隔离（不同防火墙策略、不同公网入口、不同 TLS/鉴权机制）。
- 团队/发布节奏完全独立，需要按厂商单独扩缩容与灰度。
- 协议/流量特性差异极大，单实例难以通过线程模型与限流解决性能/稳定性问题。
