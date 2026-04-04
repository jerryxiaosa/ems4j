# ems-iot-simulator 使用说明

## 模块定位

`ems-iot-simulator` 是独立的命令行模块，用来模拟电表作为 TCP 客户端接入 `ems-iot`。当前只实现 `安科瑞 + 4G 直连`，支持：

- 建链后注册
- 周期心跳
- 按协议格式上报电量
- 历史区间补投
- 下行 `拉闸 / 合闸 / 读总电量`
- 本地状态持久化，保证累计电量重启后不回退

## 启动前准备

启动前先完成下面几项：

1. 在前端先创建电表档案，拿到要模拟的 `deviceNo`。
2. 确认 `ems-iot` 的 Netty 监听地址和端口。
3. 为每个待模拟电表准备唯一的 `meterAddress`。
4. 确认当前只使用 `ACREL + ACREL_4G_DIRECT + DIRECT` 这一组配置。

## 配置文件

模拟器按 Spring Boot 默认规则读取配置。当前模块内置配置入口是 [application.yml](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/resources/application.yml)，默认激活 `example` profile，因此示例参数放在 [application-example.yml](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/resources/application-example.yml)。容器场景新增了 [application-docker.yml](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/resources/application-docker.yml)，通常通过 `SPRING_PROFILES_ACTIVE=docker` 启用。

最小可运行配置示例：

```yaml
simulator:
  target:
    host: 127.0.0.1
    port: 19500
    connect-timeout-ms: 5000
    reconnect-interval-ms: 3000
  runtime:
    heartbeat-interval-seconds: 60
    persistence-file: ./.data/iot-simulator-state.json
  replay:
    enabled: true
    send-interval-ms: 200
  devices:
    - device-no: SIM001
      vendor: ACREL
      product-code: ACREL_4G_DIRECT
      access-mode: DIRECT
      meter-address: "00000001"
      profile-type: OFFICE
      random-seed: 101
```

关键字段说明：

- `simulator.target.host` / `simulator.target.port`：`ems-iot` Netty 服务地址。
- `simulator.target.connect-timeout-ms`：单次建连超时时间。
- `simulator.target.reconnect-interval-ms`：建连失败后的重试间隔。
- `simulator.runtime.heartbeat-interval-seconds`：心跳间隔，单位秒。
- `simulator.runtime.persistence-file`：本地状态文件路径，默认 `./.data/iot-simulator-state.json`。
- `simulator.replay.enabled`：是否先执行历史补投。
- `simulator.replay.start-time` / `simulator.replay.end-time`：历史补投区间，只允许过去时间；两者都为空时，会默认补投“本月 1 号 00:00:00 到当前时间前 1 秒”。
- `simulator.replay.send-interval-ms`：补投时相邻两条数据的发送间隔。
- `simulator.devices[].device-no`：前端建档后得到的电表编号。
- `simulator.devices[].meter-address`：协议中的表地址。
- `simulator.devices[].profile-type`：当前支持 `OFFICE`、`FACTORY`。

注意：

- 本地 `spring-boot:run` 默认走 `example` profile。
- `docker compose` 和 Helm 部署默认走 `docker` profile，会连接 `iot:19500`。
- 如果直接使用仓库内默认示例配置启动，模拟器会按示例参数连接 `127.0.0.1:19500`。
- 正式联调时建议通过外部配置覆盖 `devices`、目标地址和历史补投参数。
- 状态文件存在时会优先按 `replayCursorTime` 断点续传；只有删除状态文件或关闭持久化后，才会再次从月初完整补投。

## 启动方式

本地启动：

```bash
mvn -pl ems-iot-simulator -am spring-boot:run
```

容器场景可直接通过环境变量切到 `docker` profile：

```bash
SPRING_PROFILES_ACTIVE=docker
```

打包后启动：

```bash
mvn -pl ems-iot-simulator -am -Dmaven.test.skip=true package
java -jar ems-iot-simulator/target/ems-iot-simulator-*.jar
```

如果需要使用外部配置文件，可通过 Spring Boot 标准参数覆盖：

```bash
java -jar ems-iot-simulator/target/ems-iot-simulator-*.jar \
  --spring.config.additional-location=file:./config/
```

## 启动后调用关系

### 总体调用链

```text
IotSimulator.main
  -> Spring Boot 启动
  -> 绑定 simulator.* 配置并创建 Bean
  -> SmartLifecycle 自动触发 SimulatorLifecycle.start()
  -> SimulatorLauncher.loadStateSnapshot()
  -> SimulatorLauncher.loadDeviceContexts()
  -> create SimulatorDeviceRuntime[]
  -> 每台设备启动：建连 -> 注册 -> 心跳 -> 上报循环
  -> 上报循环：先历史补投，再实时上报
  -> 收到下行命令：解析命令 -> 持久化运行态 -> 回 ACK
```

### ASCII 时序图

```text
+----------------+      +-------------------+      +--------------------+      +------------------------+      +---------------------+      +-----------------------+
| IotSimulator   |      | Spring Container  |      | SimulatorLifecycle |      | SimulatorDeviceRuntime |      | Acrel4gSocketSession|      | ems-iot(Netty Server) |
+----------------+      +-------------------+      +--------------------+      +------------------------+      +---------------------+      +-----------------------+
        |                           |                          |                            |                             |                               |
        | main()                    |                          |                            |                             |                               |
        |-------------------------->| create/refresh context   |                            |                             |                               |
        |                           | bind simulator.*         |                            |                             |                               |
        |                           | create beans             |                            |                             |                               |
        |                           |------------------------->| start()                    |                             |                               |
        |                           |                          | loadStateSnapshot()        |                             |                               |
        |                           |                          | loadDeviceContexts()       |                             |                               |
        |                           |                          | create SimulatorDeviceRuntime[]                         |                               |
        |                           |                          |--------------------------->| start()                     |                               |
        |                           |                          |                            | ensureConnectedAndRegistered()                             |
        |                           |                          |                            |---------------------------->| connect()                      |
        |                           |                          |                            |                             |------------------------------->| TCP connect
        |                           |                          |                            | sendRegisterFrame()         |                               |
        |                           |                          |                            |---------------------------->| send(register)                 |
        |                           |                          |                            |                             |------------------------------->| register
        |                           |                          |                            | startHeartbeat()            |                               |
        |                           |                          |                            | runReportLoop()             |                               |
        |                           |                          |                            | runReplayIfNecessary()      |                               |
        |                           |                          |                            | runReplayPoint()/runLivePoint()                            |
        |                           |                          |                            | runScheduledReport()        |                               |
        |                           |                          |                            |---------------------------->| send(upload/heartbeat)         |
        |                           |                          |                            |                             |------------------------------->| upload / heartbeat
        |                           |                          |                            |                             |<-------------------------------| downlink command
        |                           |                          |                            |<----------------------------| readLoop()/dispatchFrames()
        |                           |                          |                            | handleInboundFrame()        |                               |
        |                           |                          |                            | Acrel4gCommandResponder.handle()                          |
        |                           |                          |                            | persistRuntimeState()       |                               |
        |                           |                          |                            |---------------------------->| send(ack)                      |
        |                           |                          |                            |                             |------------------------------->| ack
        |                           |                          | stop()                     |                             |                               |
        |                           |------------------------->|                            |                             |                               |
        |                           |                          |--------------------------->| stop()                      |                               |
        |                           |                          |                            |---------------------------->| close()                        |
        |                           |                          |                            |                             |------------------------------->| TCP close
```

### 生命周期启动阶段

1. [SimulatorLifecycle.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java) 实现了 `SmartLifecycle`，容器刷新后会自动启动。
2. `SimulatorLifecycle.start()` 先通过 [SimulatorLauncher.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLauncher.java) 读取状态快照，并把配置中的 `devices` 与本地状态合并成 [SimulatorDeviceContext.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/runtime/SimulatorDeviceContext.java)。
3. 每个 `SimulatorDeviceContext` 会被包装成一个 [SimulatorDeviceRuntime.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorDeviceRuntime.java)，由它负责单台设备的完整运行编排。
4. 当前实现会在 `SmartLifecycle.start()` 阶段依次调用每台 `SimulatorDeviceRuntime.start()`，也就是启动时就会立即尝试建立连接并发送注册报文。

### 单台设备启动链路

1. `SimulatorDeviceRuntime.start()` 会依次执行 `ensureConnectedAndRegistered()`、`startHeartbeat()` 和 `reportExecutor.submit(this::runReportLoop)`。
2. `ensureConnectedAndRegistered()` 会先调用 [Acrel4gSocketSession.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gSocketSession.java) 建立 TCP 连接，再调用 [Acrel4gSimulatorClient.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gSimulatorClient.java) 组装注册帧并发送。
3. `startHeartbeat()` 会为每台设备启动独立的心跳定时线程，按 `heartbeat-interval-seconds` 周期发送心跳。
4. `runReportLoop()` 会先执行历史补投，再切换到实时上报循环。

### 上报链路

```text
runReportLoop
  -> runReplayIfNecessary
    -> ReplayScheduleService.buildReplayPoints
    -> runReplayPoint
      -> runScheduledReport
  -> runLiveLoop
    -> resolveFirstLivePoint
    -> nextLivePoint
    -> runLivePoint
      -> runScheduledReport
```

1. 历史补投时间点由 [ReplayScheduleService.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/ReplayScheduleService.java) 生成，规则是 `startTime + N 小时`。当 `startTime/endTime` 未显式配置时，会回退到“本月 1 号 00:00:00 到当前时间前 1 秒”。
2. 实时模式的首个时间点由 `resolveFirstLivePoint()` 和 `nextLivePoint()` 计算，锚点是 `SimulatorDeviceRuntime` 创建时记录的 `startupAnchorTime`。
3. 每个计划点最终都会进入 `runScheduledReport()`：
   - 先调用 [EnergySimulationService.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/EnergySimulationService.java) 生成 `EnergySnapshot`
   - 再更新 `DeviceRuntimeState`
   - 再把共享快照写回 [StateStore.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/state/StateStore.java)
   - 最后按需组装并发送 `DataUploadMessage`
4. 如果设备当前处于拉闸状态，`EnergySnapshot.shouldReport=false`，当前计划点只推进运行态和补投游标，不会发送上报帧。

### 下行命令处理链路

```text
Acrel4gSocketSession.readLoop
  -> dispatchFrames
  -> SimulatorDeviceRuntime.handleInboundFrame
  -> Acrel4gCommandResponder.handle
  -> persistRuntimeState
  -> encode DOWNLINK ack
  -> sendSimulatorFrame
```

1. 建链成功后，`Acrel4gSocketSession` 会启动独立 reader 线程持续读取服务端下行数据，并按 `7B7B ... 7D7D` 切分完整帧。
2. 每个完整帧都会回调到 `SimulatorDeviceRuntime.handleInboundFrame()`。
3. [Acrel4gCommandResponder.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gCommandResponder.java) 当前只处理三类命令：
   - `拉闸`
   - `合闸`
   - `读总电量`
4. 命令处理成功后，会先持久化当前运行态，再通过 [Acrel4gFrameCodec.java](/Users/jerry/Workspace/github/ems4j/ems-iot/src/main/java/info/zhihui/ems/iot/plugins/acrel/protocol/fourthgeneration/tcp/support/Acrel4gFrameCodec.java) 组装 `DOWNLINK` 响应帧并发回。

### 连接与重连说明

1. `sendSimulatorFrame()` 在真正发送前会再次执行 `ensureConnectedAndRegistered()`，用于处理断线后的自动重连。
2. `Acrel4gSocketSession.readLoop()` 的 reader 线程退出时，只回收自己持有的 `currentSocket/currentInputStream`。
3. 只有当退出的 reader 对应当前活跃连接时，才会清空共享 `socket/inputStream/outputStream` 并标记设备离线。
4. 这样可以避免旧连接的 reader 线程在退出时误关新连接，降低断线恢复后的连接抖动风险。

### 线程模型

- 每台模拟表对应一个独立的 `SimulatorDeviceRuntime`。
- 每台表至少会创建三个执行单元：
  - 一个 TCP reader 线程，负责收包
  - 一个心跳定时线程，负责保活
  - 一个上报线程，负责历史补投和实时上报
- 不同设备之间互不影响，一台表断线重连不会阻塞其他表。
- 持久化保存通过同一份 `SimulatorStateSnapshot` 完成，并在 `StateStore` 锁内写回，避免多设备并发覆盖。

### 停止流程

1. Spring 容器关闭时，会调用 `SimulatorLifecycle.stop()`。
2. `stop()` 会遍历所有 `SimulatorDeviceRuntime`，逐台执行 `stop()`。
3. 单台设备停止时会：
   - 把 `stopped` 置为 `true`
   - 关闭心跳线程
   - 关闭上报线程
   - 调用 `Acrel4gSocketSession.close()` 主动关闭 TCP 会话
4. `Acrel4gSocketSession.close()` 会显式标记设备离线，reader 线程则会在下一次循环或 IO 异常时自然退出。

## 行为规则

- 历史补投开启时，模拟器会先按 `start-time + N 小时` 顺序快速补投，再进入实时模式。
- 实时上报时间序列以“程序启动时间”为锚点，不按整点对齐。
- 如果补投完成时已经跨过若干实时计划点，模拟器会从“晚于当前时刻的下一个计划点”继续上报。
- `OFFICE` 在工作日 `09:00-18:00` 用电更高，其他时段接近低耗。
- `FACTORY` 全天持续用电，白天略高、夜间略低。
- 每小时数据带随机扰动，但累计电量始终单调递增。
- `拉闸` 后保持在线和心跳，但电量不增长，也不会触发周期上报。
- `合闸` 后从下一个计划点恢复上报，不补发拉闸期间跳过的数据。

## 联调步骤

建议按下面顺序联调：

1. 启动 `ems-iot`，确认 Netty 端口已监听。
2. 在前端建好电表档案，记录 `deviceNo`。
3. 本地调试时修改 [application-example.yml](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/resources/application-example.yml)；容器场景优先修改 [application-docker.yml](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/resources/application-docker.yml) 或通过环境变量覆盖。
4. 启动 `ems-iot-simulator`。
5. 在 `ems-iot` 侧确认设备注册、心跳和上报日志。
6. 从 `ems-iot` 侧下发 `拉闸 / 合闸 / 读总电量`，确认模拟器能正常回包。

## 自动化验证

当前与模拟器主链路直接相关的测试主要包括：

- [SimulatorPropertiesTest.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/test/java/info/zhihui/ems/iot/simulator/config/SimulatorPropertiesTest.java)：配置绑定
- [ReplayScheduleServiceTest.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/test/java/info/zhihui/ems/iot/simulator/service/ReplayScheduleServiceTest.java)：历史补投时间点计算
- [EnergySimulationServiceTest.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/test/java/info/zhihui/ems/iot/simulator/service/EnergySimulationServiceTest.java)：能耗快照生成
- [Acrel4gCommandResponderTest.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/test/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gCommandResponderTest.java)：下行命令识别与响应
- [Acrel4gSocketSessionTest.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/test/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gSocketSessionTest.java)：TCP 收发、切帧和旧 reader 不误关新连接
- [SimulatorDeviceRuntimeTest.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/test/java/info/zhihui/ems/iot/simulator/service/SimulatorDeviceRuntimeTest.java)：单设备补投、实时上报、拉合闸与持久化
- [SimulatorLifecycleTest.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/test/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycleTest.java)：启动装配与主链路联动

模块级验证命令：

```bash
mvn -pl ems-iot-simulator -am -DfailIfNoTests=false test -q
```

## 持久化说明

- `example` profile 下状态文件默认位置是 `./.data/iot-simulator-state.json`，`docker` profile 下默认位置是 `/app/.data/iot-simulator-state.json`。
- 文件中会按 `deviceNo` 保存累计电量、开关状态和补投进度。
- 进程重启后会从该文件恢复状态，保证总电量继续增长，不会从零开始。
