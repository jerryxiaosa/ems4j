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

模拟器按 Spring Boot 默认规则读取配置。当前模块内置配置入口是 [application.yml](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/resources/application.yml)，并默认激活 `example` profile，因此示例参数实际放在 [application-example.yml](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/resources/application-example.yml)。

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
    start-time: 2026-02-01T17:00:08
    end-time: 2026-02-18T02:33:18
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
- `simulator.runtime.heartbeat-interval-seconds`：心跳间隔，单位秒。
- `simulator.runtime.persistence-file`：本地状态文件路径，默认 `./.data/iot-simulator-state.json`。
- `simulator.replay.enabled`：是否先执行历史补投。
- `simulator.replay.start-time` / `simulator.replay.end-time`：历史补投区间，只允许过去时间。
- `simulator.replay.send-interval-ms`：补投时相邻两条数据的发送间隔。
- `simulator.devices[].device-no`：前端建档后得到的电表编号。
- `simulator.devices[].meter-address`：协议中的表地址。
- `simulator.devices[].profile-type`：当前支持 `OFFICE`、`FACTORY`。

## 启动方式

本地启动：

```bash
mvn -pl ems-iot-simulator -am spring-boot:run
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
  -> 读取本地状态文件
  -> 按 devices 配置构造每台设备的运行上下文
  -> 每台设备启动：TCP 建链 -> 发送注册 -> 启动心跳 -> 启动上报循环
  -> 上报循环：先历史补投，再实时上报
  -> 收到下行命令：解析命令 -> 更新运行状态 -> 持久化 -> 回 ACK
```

### ASCII 时序图

```text
+----------------+      +-------------------+      +--------------------+      +---------------------+      +---------------------+      +-----------------------+
| IotSimulator   |      | Spring Container  |      | SimulatorLifecycle |      | DeviceRuntime       |      | Acrel4gSocketSession|      | ems-iot(Netty Server) |
+----------------+      +-------------------+      +--------------------+      +---------------------+      +---------------------+      +-----------------------+
        |                           |                          |                           |                            |                               |
        | main()                    |                          |                           |                            |                               |
        |-------------------------->| create/refresh context   |                           |                            |                               |
        |                           | bind simulator.*         |                           |                            |                               |
        |                           | create beans             |                           |                            |                               |
        |                           |------------------------->| start()                   |                            |                               |
        |                           |                          | loadStateSnapshot()       |                            |                               |
        |                           |                          | loadDeviceContexts()      |                            |                               |
        |                           |                          | create DeviceRuntime[]    |                            |                               |
        |                           |                          |-------------------------->| start()                    |                               |
        |                           |                          |                           | ensureConnectedAndRegistered()                           |
        |                           |                          |                           |--------------------------->| connect()                      |
        |                           |                          |                           |                            |------------------------------->| TCP connect
        |                           |                          |                           | sendRegisterFrame()        |                               |
        |                           |                          |                           |--------------------------->| send(register)                 |
        |                           |                          |                           |                            |------------------------------->| register
        |                           |                          |                           | startHeartbeat()           |                               |
        |                           |                          |                           | runReportLoop()            |                               |
        |                           |                          |                           | runReplayIfNecessary()     |                               |
        |                           |                          |                           | runReplayPoint()/runLivePoint()                           |
        |                           |                          |                           | buildDataUploadFrame()     |                               |
        |                           |                          |                           |--------------------------->| send(upload/heartbeat)         |
        |                           |                          |                           |                            |------------------------------->| upload / heartbeat
        |                           |                          |                           |                            |<-------------------------------| downlink command
        |                           |                          |                           |<---------------------------| readLoop()/dispatchFrames()
        |                           |                          | handleInboundFrame()      |                            |                               |
        |                           |                          | parse command             |                            |                               |
        |                           |                          | persistRuntimeState()     |                            |                               |
        |                           |                          |-------------------------->| update switch/status       |                               |
        |                           |                          | encode ack                |                            |                               |
        |                           |                          |--------------------------->| sendSimulatorFrame()      |                               |
        |                           |                          |                           |--------------------------->| send(ack)                      |
        |                           |                          |                           |                            |------------------------------->| ack
        |                           |                          | stop()                    |                            |                               |
        |                           |------------------------->|                           |                            |                               |
        |                           |                          |-------------------------->| stop()                     |                               |
        |                           |                          |                           |--------------------------->| close()                        |
        |                           |                          |                           |                            |------------------------------->| TCP close
```

### 启动阶段

1. 命令行入口是 [IotSimulator.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/IotSimulator.java)。
2. [IotSimulator.java:20](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/IotSimulator.java#L20) 调用 `SpringApplication.run(...)` 启动容器。
3. [IotSimulator.java:22](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/IotSimulator.java#L22) 把应用固定成 `WebApplicationType.NONE`，因此它是常驻命令行进程，不启动 Web 容器。
4. Spring 读取 [application.yml](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/resources/application.yml) 并默认激活 `example` profile，然后再读取 [application-example.yml](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/resources/application-example.yml)。
5. `simulator.*` 配置绑定到 [SimulatorProperties.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/config/SimulatorProperties.java)，协议编解码器由 [SimulatorConfiguration.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/config/SimulatorConfiguration.java) 提供。

### 生命周期启动阶段

1. [SimulatorLifecycle.java](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java) 实现了 `SmartLifecycle`。
2. [SimulatorLifecycle.java:95](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L95) 返回 `isAutoStartup=true`，所以 Spring 容器刷新完成后会自动调用 [SimulatorLifecycle.java:56](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L56) 的 `start()`。
3. `start()` 先调用 [SimulatorLauncher.java:28](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLauncher.java#L28) 读取本地状态快照，底层由 [FileStateStore.java:38](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/state/FileStateStore.java#L38) 从 `persistence-file` 指定的 json 文件中恢复状态。
4. 然后调用 [SimulatorLauncher.java:33](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLauncher.java#L33)，把配置中的 `devices` 和持久化状态合并成 `SimulatorDeviceContext`，同时补齐默认 `switchStatus=ON`、`replayCompleted` 等运行态字段。
5. 每个 `SimulatorDeviceContext` 会被包装成一个 `DeviceRuntime`，并在 [SimulatorLifecycle.java:67](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L67) 逐台启动。

### 单台设备启动链路

1. 单台设备的入口是 [SimulatorLifecycle.java:127](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L127) 的 `DeviceRuntime.start()`。
2. 第一步 [SimulatorLifecycle.java:128](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L128) 调用 `ensureConnectedAndRegistered()`：
   - 先通过 [Acrel4gSocketSession.java:49](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gSocketSession.java#L49) 建立 TCP 连接。
   - 连接成功后回到 [SimulatorLifecycle.java:209](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L209) 构造注册报文。
   - 注册报文由 [Acrel4gSimulatorClient.java:39](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gSimulatorClient.java#L39) 组帧，再经 [Acrel4gSocketSession.java:73](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gSocketSession.java#L73) 发送。
3. 第二步 [SimulatorLifecycle.java:129](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L129) 启动心跳调度，最终由 [SimulatorLifecycle.java:200](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L200) 周期发送心跳报文。
4. 第三步 [SimulatorLifecycle.java:130](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L130) 提交上报线程，进入 [SimulatorLifecycle.java:146](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L146) 的 `runReportLoop()`。

### 上报链路

```text
runReportLoop
  -> runReplayIfNecessary
    -> ReplayScheduleService.buildReplayPoints
    -> SimulatorDeviceRunner.runReplayPoint
  -> runLiveLoop
    -> LiveScheduleService.nextPoint
    -> SimulatorDeviceRunner.runLivePoint
```

1. 如果开启历史补投，[SimulatorLifecycle.java:155](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L155) 会先执行 `runReplayIfNecessary()`。
2. 补投时间点由 [ReplayScheduleService.java:31](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/ReplayScheduleService.java#L31) 生成，规则是 `startTime + N 小时`，并且 [ReplayScheduleService.java:43](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/ReplayScheduleService.java#L43) 会校验补投结束时间必须是历史时间。
3. 实时模式的首个时间点由 [LiveScheduleService.java:13](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/LiveScheduleService.java#L13) 计算，锚点是当前 `DeviceRuntime` 创建时记录的 `startupAnchorTime`。
4. 每个时间点最终都会走到 [SimulatorDeviceRunner.java:75](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorDeviceRunner.java#L75)。
5. [SimulatorDeviceRunner.java:77](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorDeviceRunner.java#L77) 先调用 [EnergySimulationService.java:36](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/EnergySimulationService.java#L36) 生成本次 `EnergySnapshot`。
6. 如果设备当前处于拉闸状态，[EnergySimulationService.java:38](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/EnergySimulationService.java#L38) 会返回 `shouldReport=false`，于是 [SimulatorDeviceRunner.java:81](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorDeviceRunner.java#L81) 直接跳过发送。
7. 无论是否真正发送，本次电量累计值和补投游标都会先在 [SimulatorDeviceRunner.java:135](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorDeviceRunner.java#L135) 落到本地状态文件。
8. 需要上报时，[SimulatorDeviceRunner.java:142](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorDeviceRunner.java#L142) 组装 `DataUploadMessage`，再通过 [Acrel4gSimulatorClient.java:49](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gSimulatorClient.java#L49) 编码成协议帧，最后回到 [SimulatorLifecycle.java:219](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L219) 发出。

### 下行命令处理链路

```text
Acrel4gSocketSession.readLoop
  -> dispatchFrames
  -> SimulatorLifecycle.handleInboundFrame
  -> Acrel4gCommandResponder.handle
  -> persistRuntimeState
  -> encode DOWNLINK ack
  -> sendSimulatorFrame
```

1. 建链成功后，[Acrel4gSocketSession.java:98](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gSocketSession.java#L98) 会启动 reader 线程。
2. reader 线程在 [Acrel4gSocketSession.java:106](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gSocketSession.java#L106) 中持续读流，并在 [Acrel4gSocketSession.java:135](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gSocketSession.java#L135) 里按 `7B7B ... 7D7D` 切帧。
3. 每个完整帧会回调到 [SimulatorLifecycle.java:257](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L257) 的 `handleInboundFrame()`。
4. 这里直接调用 [Acrel4gCommandResponder.java:38](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gCommandResponder.java#L38) 解析命令。
5. 目前只处理三类命令：
   - [Acrel4gCommandResponder.java:53](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gCommandResponder.java#L53) 拉闸，把 `switchStatus` 改成 `OFF`
   - [Acrel4gCommandResponder.java:58](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gCommandResponder.java#L58) 合闸，把 `switchStatus` 改成 `ON`
   - [Acrel4gCommandResponder.java:63](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gCommandResponder.java#L63) 读总电量，按当前累计值生成响应
6. 命令处理成功后，[SimulatorLifecycle.java:265](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L265) 会先持久化，再在 [SimulatorLifecycle.java:266](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L266) 用 `Acrel4gFrameCodec` 组装 `DOWNLINK` 响应帧并发回。

### 线程模型

- 每台模拟表都有一个独立的 `DeviceRuntime`。
- 每台表至少会创建三类执行单元：
  - 一个 TCP reader 线程，负责持续收包
  - 一个心跳定时线程，负责周期发送心跳
  - 一个上报线程，负责执行历史补投和实时上报
- 不同设备之间互不影响，一台表断线重连不会阻塞其他表。
- 所有需要持久化状态的地方，都会同步调用 `StateStore.save(...)`，保证重启后电量、开关状态和补投进度可恢复。

### 停止流程

1. Spring 容器关闭时，会回调 [SimulatorLifecycle.java:74](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L74) 的 `stop()`。
2. `stop()` 会遍历所有 `DeviceRuntime`，逐个调用 [SimulatorLifecycle.java:133](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/service/SimulatorLifecycle.java#L133) 的 `DeviceRuntime.stop()`。
3. 每台设备停止时会：
   - 把 `stopped` 置为 `true`
   - 关闭心跳线程
   - 关闭上报线程
   - 调用 [Acrel4gSocketSession.java:93](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gSocketSession.java#L93) 关闭 TCP 连接
4. 连接关闭后，reader 线程会在 [Acrel4gSocketSession.java:128](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/java/info/zhihui/ems/iot/simulator/protocol/acrel/Acrel4gSocketSession.java#L128) 的 `finally` 中标记设备离线并释放底层流资源。

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
3. 修改 [application-example.yml](/Users/jerry/Workspace/github/ems4j/ems-iot-simulator/src/main/resources/application-example.yml) 中的 `target`、`devices` 和 `replay` 配置，或通过外部配置覆盖默认值。
4. 启动 `ems-iot-simulator`。
5. 在 `ems-iot` 侧确认设备注册、心跳和上报日志。
6. 从 `ems-iot` 侧下发 `拉闸 / 合闸 / 读总电量`，确认模拟器能正常回包。

## 持久化说明

- 状态文件默认位置是 `./.data/iot-simulator-state.json`。
- 文件中会按 `deviceNo` 保存累计电量、开关状态和补投进度。
- 进程重启后会从该文件恢复状态，保证总电量继续增长，不会从零开始。
