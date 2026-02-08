# ems-iot ChannelManager 设计说明

## 1. 目标

`ChannelManager` 负责同一设备通道上的命令发送编排，核心目标：

- 顺序发送（串行队列）
- 等待回执发送（`sendInQueue`）
- 不等待回执发送（`sendInQueueWithoutWaiting`）
- 在超时/通道失效时快速清理状态，避免 Future 悬挂
- 通过 EventLoop 串行化关键状态读写，降低跨线程并发风险

> 说明：协议层对外端口 `ProtocolCommandTransport` 当前暴露的是 `sendWithAck` + `completePending`，内部由 `ChannelManager` 承接队列与会话状态。

## 2. 会话数据结构

- `ChannelSession`（按 channel 维度）：
  - `deviceNo` / `deviceType` / `channel`
  - `queue`：待发送队列（FIFO）
  - `sending`：是否有在途发送
  - `pendingFuture`：当前等待应答的 Future（同一时刻最多一个）
  - `abnormalTimestamps`：异常时间窗口，用于异常频次统计

## 3. 串行发送与应答流程（ASCII）

```
[业务线程]
  |
  | sendInQueue(deviceNo, message) / sendInQueueWithoutWaiting(deviceNo, message)
  v
ChannelManager.enqueue(...)
  |
  |-- callInEventLoop(session, supplier)
      |-- 若当前线程已在 EventLoop：直接执行
      |-- 否则：投递到 EventLoop 串行执行并等待结果
  |
  |-- enqueueInLoop(...)：入队 PendingTask
  |-- tryDispatchInLoop(...)
          |
          |-- sending=false ? YES -> 出队并发送
          |                  NO  -> 返回（等待前一个任务释放）
          v
       sendTaskInLoop(...)
          |
          |-- requireAck=true:
          |     - write 成功：注册 10s 超时任务，等待 completePending(...)
          |     - write 失败：failPendingInLoop(...)，释放 sending 并继续调度
          |
          |-- requireAck=false:
          |     - write 成功：future.complete(empty)，释放 sending 并继续调度
          |     - write 失败：future.completeExceptionally(...)，释放 sending 并继续调度

[设备 ACK 到达 - Netty 线程]
  |
  | Downlink handler -> completePending(deviceNo, payload)
  v
completePendingInLoop(...)
  |
  |-- 完成 pendingFuture
  |-- 释放 sending
  |-- 继续调度下一条
```

## 4. 超时与失败语义

- 下发等待回执时，会注册超时任务（`COMMAND_TIMEOUT_MILLIS = 15_000`）。
- 超时后执行：
  - 当前 pendingFuture 异常完成（`TimeoutException`）
  - 关闭并移除通道（`closeAndRemoveInLoop`）
  - 队列中剩余任务统一异常完成
- 旧超时任务不会误伤新任务：通过 `expectedFuture == currentFuture` 进行匹配保护。

## 5. 入参与异常约束

- `register/remove/closeAndRemove/sendInQueue/completePending` 对关键入参做非空校验，不满足时抛 `IllegalArgumentException`。
- `completePending` 在“会话不存在”或“无挂起任务”时抛 `IllegalStateException`，避免静默吞错。
- `sendInQueueWithoutWaiting` 不再吞异常，发送失败会通过 Future 回调记录日志。

## 6. 固定参数

- `MAX_QUEUE_SIZE = 5`
- `COMMAND_TIMEOUT_MILLIS = 15_000`
- `EVENT_LOOP_WAIT_TIMEOUT_MILLIS = 3_000`

## 7. 关键注意事项

- 同一 `deviceNo` 重绑新通道时，旧通道会被关闭并清理，避免双通道并存导致状态错乱。
- `sendDirectly` 当前仅执行 `writeAndFlush`，未统一到 EventLoop 编排；如扩展逻辑（队列/状态）应避免直接复用该方法。
- 协议 ACK handler 在识别到响应帧后必须调用 `completePending`，否则等待中的命令无法释放。
