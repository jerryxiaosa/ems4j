# ems-iot 异常与空值处理规则

本文档用于统一 `ems-iot` 的异常语义与空值边界，目标是：

- 避免设备入站异常直接冲击 Netty 线程。
- 让 API 参数错误、业务错误、系统错误语义清晰。
- 对 ACK 时序错乱等并发问题保持 fail-fast，而不是静默吞错。

## 1. 总原则

1. 外部输入链路默认不抛出未捕获异常（解码/解析/普通上行处理）。
2. 业务边界允许抛出明确异常类型（`BusinessRuntimeException`、`NotFoundException`、`IllegalArgumentException`）。
3. 参数错误优先在 Controller 层通过注解校验拦截，减少 500 误判。
4. ACK 匹配属于特例：发现状态不一致时允许抛异常，优先暴露时序问题。

## 2. 分层规则

### 2.1 Transport / Netty 层

- `ByteToMessageDecoder`、`ChannelInboundHandler` 不应抛出未捕获异常。
- 非法帧/无法识别协议：记录日志，必要时关闭通道，避免脏连接持续占用资源。

### 2.2 协议解帧 / Parser 层

- 解帧失败通过 `FrameDecodeResult` 的 `reason` 表达，不直接抛异常。
- `Parser` 层对可预期坏数据（格式不合法、字段缺失）返回 `null`，并记录 `warn` 日志。
- 仅开发期编程错误（空指针、类型错误）可抛异常，但应尽快修复为可控分支。

### 2.3 Packet Handler / Listener 层

- 普通上行处理（心跳/上报）默认“吞异常 + 打日志 + 返回”。
- 事件发布使用 `ApplicationEventPublisher`，发布失败需捕获并记录，不影响通道线程。
- 监听器（如 `info.zhihui.ems.iot.listener.ProtocolInboundEventListener`）内部异常应捕获，避免反向影响上行链路。
- ACK 处理器是特例：调用 `ProtocolCommandTransport.completePending` 时不应静默吞掉状态错误。

### 2.4 Application / Domain 层

- 允许抛出：
  - `IllegalArgumentException`：参数不合法。
  - `NotFoundException`：资源不存在。
  - `BusinessRuntimeException`：设备返回失败、命令执行失败、数据格式错误等。
- 在线状态判定基于持久化 `lastOnlineAt`，请求体不应直接驱动该字段。

### 2.5 Repository / Persistence 层

- 查询未命中可返回 `null`，由 `DeviceRegistry` 统一转换为 `NotFoundException`。
- 更新操作必须校验影响行数：`affected == 0` 视为未命中并抛异常（避免“假成功”）。

### 2.6 HTTP API 层

- API 统一返回 `RestResult`。
- Controller 使用 `@Validated` + `@NotNull/@Positive/@Valid` 等注解做参数前置校验。
- `RuntimeExceptionHandler` 统一映射：
  - `MethodArgumentNotValidException`/`ConstraintViolationException`/`IllegalArgumentException` -> 参数错误码。
  - `BusinessRuntimeException` -> 业务错误码（或自定义 code）。
  - `NotFoundException` -> 业务错误码。
  - 未知 `RuntimeException` -> HTTP 500 + 通用提示。

## 3. 何时返回 null

- 协议解析阶段遇到坏数据，且属于可预期分支（格式错误、字段不全）。
- 入站处理中设备未绑定/无法路由时，选择“记录并返回”而非抛错中断线程。
- 不用于业务服务对外返回（应用层/领域层应返回明确结果或抛异常）。

## 4. 何时抛异常

- 应用层参数非法、业务失败、资源不存在。
- 命令发送前置校验失败（无会话、通道不活跃、队列满）。
- ACK 关联失败（`completePending` 找不到会话或无挂起命令）需抛 `IllegalStateException`。
- 持久化更新未命中（影响行数为 0）需抛异常。

## 5. 推荐处理流程（入站链路）

1. 解码失败：返回 `FrameDecodeResult.reason`，记录日志并结束。
2. 解析失败：`parser` 返回 `null`，记录日志并结束。
3. 普通上行处理：捕获异常，记录后结束（必要时做连接治理）。
4. ACK 回执处理：调用 `completePending`，若状态异常允许抛错并由上层统一感知。
5. 事件发布：`publishEvent` 失败仅记录，避免阻断核心处理流程。

## 6. 当前固定策略（ChannelManager）

- `MAX_QUEUE_SIZE = 5`：单通道待发送队列上限，超限直接失败。
- `COMMAND_TIMEOUT_MILLIS = 15000`：等待 ACK 超时后失败当前命令并关闭通道。
- `EVENT_LOOP_WAIT_TIMEOUT_MILLIS = 3000`：跨线程投递 EventLoop 的等待上限。
- `ABNORMAL_WINDOW_MILLIS = 30000`、`ABNORMAL_MAX_COUNT = 5`：异常频率控制阈值。

## 7. PR 自检清单

- 是否把可预期坏数据留在解析层处理（返回 `null`），而不是抛到 Netty？
- 是否给 Controller 入参加了必要校验注解？
- 是否将 ACK 状态错乱保持 fail-fast，而不是 `try/catch` 后静默返回？
- 是否校验了 `updateById/deleteById` 的影响行数语义？
- 日志是否包含 `deviceNo/sessionId/channelId` 等排障字段？
