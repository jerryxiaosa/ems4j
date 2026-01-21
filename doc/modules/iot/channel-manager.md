# ems-iot ChannelManager 设计说明

## 1. 目标

对同一 IoT 设备（同一 TCP Channel）实现：

- **顺序发送**（串行队列）
- **可选等待回执**（`sendWithAck`）
- **无需回执直接完成**（`sendFireAndForget`）
- 通道断开时清理队列，避免 Future 悬挂/泄漏

## 2. 会话数据结构

- `ChannelSession`（按 channel 维度）：
  - `deviceNo` / `deviceType` / `channel`
  - `queue`：待发送队列（FIFO）
  - `sending`：是否有在途发送
  - `pendingFuture`：仅在“需要回执”的场景存在

## 3. 串行发送与等待示意（ASCII）

```
[外层调用线程]
  |
  | sendWithAck(deviceNo, message) -> CompletableFuture<byte[]>
  | sendFireAndForget(deviceNo, message) -> CompletableFuture<byte[]>
  v
ChannelManager
  |-- 查找 ChannelSession（按 deviceNo 定位）
  |-- 校验通道可用/队列未满
  |-- 入队 PendingTask(deviceNo, message, future, requireAck)
  |-- tryDispatch(channelId)
          |
          |-- session.sending=false ? YES -> 出队队首任务并 writeAndFlush
          |                         NO  -> 返回（等待前一个任务释放）
          v
       [写出任务]
          |
          |-- requireAck=true:
          |     - write 成功：等待协议 handler 调用 completePending
          |     - write 失败：failPending -> future 异常完成，并继续调度
          |
          |-- requireAck=false:
          |     - write 成功：future.complete(empty) 并释放 sending，继续调度
          |     - write 失败：future.completeExceptionally 并释放 sending，继续调度

[设备响应到达 - Netty 事件线程]
  |
  | 插件 handler 判定为“响应帧”：
  |   -> channelManager.completePending(deviceNo, rawPayload)
  |      （完成 pendingFuture，释放 sending，继续调度）
```

## 4. 关键约束与注意事项

- 队列容量需限制（当前实现为 5），防止无界堆积导致内存压力。
- 同一 `deviceNo` 允许新通道替换旧通道，避免双连接并存带来的业务错乱。
- `sendWithAck` 必须由协议 handler 在识别响应帧时调用 `completePending` 归还结果。
