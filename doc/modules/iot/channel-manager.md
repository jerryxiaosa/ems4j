# ems-iot ChannelManager 设计说明

## 1. 目标

`ChannelManager` 负责 IoT 连接运行态管理与单连接命令编排，核心职责包括：

- 维护 `channelId -> ChannelSession` 与 `deviceNo -> channelId` 两张索引
- 保证同一连接上的命令串行发送
- 对需要 ACK 的命令维护 `pendingFuture`
- 在超时、写失败、连接移除时统一清理 pending 与队列
- 提供在线客户端运行态快照，支撑 `/api/debug/iot/**` 调试接口

当前协议层对外端口 `ProtocolCommandTransport` 暴露：

- `sendWithAck(deviceNo, payload)`
- `completePending(session, payload)`

其中 ACK 收口已经按**当前连接 session**完成，不再按 `deviceNo` 直接完成 pending。

## 2. 会话模型

`ChannelSession` 按连接维度保存运行态：

- `deviceNo` / `deviceType` / `channel`
- `queue`：待发送命令队列
- `sending`：当前是否存在在途发送
- `pendingFuture`：当前等待 ACK 的命令
- `pendingTimeoutFuture`：当前 pending 的超时任务句柄
- `abnormalTimestamps`：异常频次统计窗口

调试接口对外暴露的快照对象为 `ChannelClientSnapshot`，用于汇总连接状态、队列长度、异常次数、远端地址等信息。

## 3. 发送与 ACK 流程

### 3.1 发送流程

```text
业务线程
  -> sendInQueue(deviceNo, message)
  -> 按 deviceNo 找当前活跃 session
  -> executeSyncInEventLoop(...)
  -> enqueueTaskInLoop(...)
  -> dispatchNextInLoop(...)
  -> writeTaskInLoop(...)
```

发送规则：

- 每个连接同一时刻最多只有一个 `pendingFuture`
- `sending=false` 时才会真正从队列取出下一条
- 不需要 ACK 的命令，写结果就是最终结果
- 需要 ACK 的命令，写成功后仅表示“已发出”，真正完成要等 `completePending...`

### 3.2 ACK 收口流程

```text
设备 ACK 到达
  -> PacketHandler 取当前 ProtocolSession
  -> ProtocolCommandTransport.completePending(session, payload)
  -> NettyCommandTransport.completePending(...)
  -> ChannelManager.completePendingByChannelId(channelId, payload)
  -> completePendingInLoop(...)
```

当前 ACK 收口键是 `channelId/session`，不是 `deviceNo`。
这样在设备重连时，旧连接晚到的 ACK 只会命中旧连接自己的 pending，不会误完成新连接上的命令。

## 4. 重连策略

同一 `deviceNo` 重连时，当前策略是：

1. 先找到旧会话
2. 先发布新 `deviceNo -> channelId` 路由
3. 再异步关闭旧连接

这样做的前提就是 ACK 已经改成按当前 session 收口。
因此不再需要“同步等待旧连接先完全关闭，再绑定新连接”，从而避免在重连路径里阻塞旧 EventLoop。

如果并发注册导致 `deviceNo -> channelId` 被意外覆盖，`bindSessionIndexes(...)` 会识别非预期旧值，并异步清理遗留的 orphan session。

## 5. 超时与失败语义

### 5.1 需要 ACK 的命令

- 发送成功后注册超时任务
- 超时时间来自 `ChannelManagerProperties.commandTimeoutMillis`
- 超时后：
  - 当前 pending 以 `TimeoutException` 失败
  - 当前通道关闭并移除
  - 队列中剩余命令统一失败

### 5.2 超时任务取消

`pendingTimeoutFuture` 会在以下路径统一取消：

- `finishPendingSuccessInLoop(...)`
- `finishPendingFailureInLoop(...)`
- 通道移除时的 pending 清理

这样可以避免命令已经结束后，旧超时任务继续留在 EventLoop 中等待触发。

### 5.3 迟到 ACK 语义

当前语义已经收紧为“尝试完成”：

- 找不到会话：返回 `false`
- 当前没有 pending：返回 `false`
- 真正完成 pending：返回 `true`

只有明确的参数错误，例如 `deviceNo` 为空，才继续抛 `IllegalArgumentException`。
因此迟到 ACK、重复 ACK、重连后的旧 ACK 都不会再作为异常往上抛。

## 6. 调试能力

`ChannelManager` 提供两个运行态查询方法：

- `findClientSnapshotList()`
- `getClientSnapshotByDeviceNo(deviceNo)`

查询结果采用 `ChannelClientSnapshot` 表示，并被 `IotDebugAppService` 进一步转换为：

- `IotClientSimpleVo`
- `IotClientDetailVo`

调试快照采用 best-effort 读取，不会逐个切换 EventLoop 去取数据，避免调试接口因为某个连接卡顿而串行放大延迟。

## 7. 运行参数

`ChannelManager` 的关键参数已经从硬编码改为 `ChannelManagerProperties`，默认值与旧行为保持一致，即使不配置也能运行：

| 配置项 | 默认值 | 说明 |
|---|---:|---|
| `iot.channel-manager.max-queue-size` | `5` | 单连接待发送队列上限 |
| `iot.channel-manager.command-timeout-millis` | `15000` | 等待 ACK 超时毫秒数 |
| `iot.channel-manager.event-loop-wait-timeout-millis` | `3000` | 跨线程等待 EventLoop 结果超时 |
| `iot.channel-manager.abnormal-window-millis` | `30000` | 异常统计时间窗口 |
| `iot.channel-manager.abnormal-max-count` | `5` | 窗口内异常阈值 |

## 8. 关键注意事项

- `executeSyncInEventLoop(...)` 仍然是同步等待语义，不应从其他 Netty IO 线程滥用；当前代码会在这种场景下输出 `warn` 日志。
- `sendDirectly(...)` 只做原始写出，不参与队列与 pending 管理。
- `completePending(deviceNo, payload)` 仍然保留，主要用于旧路径和按设备查找的场景；协议 ACK 主路径应优先使用按 session 收口。
- 连接关闭或移除时，队列中未完成的命令会统一失败，避免 Future 长时间悬挂。
