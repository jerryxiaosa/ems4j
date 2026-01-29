# ems-iot 异常与空值处理规则

本规则用于统一 `ems-iot` 模块中**抛异常**与**返回 null/结果对象**的边界，避免异常冒泡导致 Netty 连接断开或线程被动退出。

## 1. 总原则

1) **外部输入链路不抛异常**：任何来自设备/网络的入站处理（解码/解析/处理）不允许抛出未捕获异常。  
2) **业务边界抛异常**：应用/领域服务对调用方暴露时，允许抛出业务异常（例如参数不合法、资源不存在）。  
3) **库/基础设施异常要被转换**：仓储查询失败、解析失败、解密失败等，应在入站链路转换为“可控结果”，而不是抛出。

## 2. 分层规则

### 2.1 Transport / Netty 层

- **不得抛异常**（ByteToMessageDecoder、ChannelInboundHandler）。  
- 协议不识别/非法帧：记录日志 + 计数/限流 + 必要时主动关闭通道。  
- 示例：`ProtocolFrameDecoder`、`AcrelGatewayFrameDecoder`。

### 2.2 协议解帧 / Codec 层

- **不得抛异常**，返回 `FrameDecodeResult` 并填充 `reason`。  
- 仅当是**开发期编码错误**（如必填字段为空且无法继续）才允许抛出，但推荐改为 `reason`。

### 2.3 Packet Parser 层

- **不得抛异常**；必须 `try/catch` 全量异常并返回 `null`。  
- 同时记录 `warn` 日志，必要时发布异常事件（如 `PAYLOAD_PARSE_ERROR`）。  
- 解析失败即 `null`，由上层模板统一处理。

### 2.4 Packet Handler / Listener 层

- **不得抛异常**；对缺失设备、未绑定、非法报文直接返回。  
- 访问仓储或服务时要捕获 `NotFoundException/BusinessRuntimeException` 并降级处理（日志 + 事件）。

### 2.5 Application / Domain 服务层

- **允许抛异常**：  
  - `NotFoundException`：资源不存在  
  - `BusinessRuntimeException`：业务失败（设备返回失败、命令失败等）  
  - `IllegalArgumentException`：参数不合法  
- 注意：若被入站 Handler 调用，必须在 Handler 内捕获并转换为“可控结果”。

### 2.6 Repository / Persistence 层

- 可返回 `null`（ORM 查询未命中），由上层 `DeviceRegistry` 统一转换为 `NotFoundException`。  
- 入站链路仍需捕获该异常，避免冒泡到 Netty 线程。

### 2.7 HTTP API 层

- API 统一返回 `RestResult`，成功与失败均通过 `ResultUtil` 封装。  
- 全局异常由 `RuntimeExceptionHandler` 处理并转换为标准错误码与错误信息（如参数校验、业务异常、系统异常）。  
- 对外错误信息应避免泄露内部实现细节，系统异常统一返回通用提示。

## 3. 何时返回 null

- **解析失败**（解密失败、XML 解析失败、字段缺失）：返回 `null`。  
- **未绑定设备或无法映射设备**：返回 `null`。  
- **协议命令未定义/无法路由**：返回 `null`，并发布异常事件或记录日志。

## 4. 何时抛异常

- **控制层/应用层入参非法**：抛 `IllegalArgumentException`。  
- **资源不存在**：抛 `NotFoundException`。  
- **命令下发失败/业务失败**：抛 `BusinessRuntimeException`。  
> 注意：这些异常不得从入站处理链路直接冒泡到 Netty。

## 5. 推荐处理流程（入站链路）

解码失败 → 返回 `FrameDecodeResult.reason` → 记录异常事件 → 结束  
解析失败 → `parser` 返回 `null` → 统一处理（日志 + 事件） → 结束  
处理异常 → 捕获并记录 → 结束（必要时关闭通道）

## 6. 设备协议处理器异常处理

- `DeviceProtocolHandler` 的 `onMessage` 方法不应抛出异常，应在内部捕获并处理。
- 命令下发失败时，通过 `CompletableFuture` 返回 `DeviceCommandResult` 表示失败状态。
- 通过 `DeviceProtocolHandlerRegistry` 解析协议时，异常应被捕获并转换为适当的错误响应。
