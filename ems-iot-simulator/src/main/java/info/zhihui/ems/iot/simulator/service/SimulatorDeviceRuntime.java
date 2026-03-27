package info.zhihui.ems.iot.simulator.service;

import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.DataUploadMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.HeartbeatMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.message.RegisterMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;
import info.zhihui.ems.iot.simulator.config.SimulatorProperties;
import info.zhihui.ems.iot.simulator.config.SimulatorReplayProperties;
import info.zhihui.ems.iot.simulator.model.EnergySnapshot;
import info.zhihui.ems.iot.simulator.protocol.acrel.Acrel4gCommandResponder;
import info.zhihui.ems.iot.simulator.protocol.acrel.Acrel4gSimulatorClient;
import info.zhihui.ems.iot.simulator.protocol.acrel.Acrel4gSocketSession;
import info.zhihui.ems.iot.simulator.protocol.acrel.result.CommandHandleResult;
import info.zhihui.ems.iot.simulator.runtime.SimulatorDeviceContext;
import info.zhihui.ems.iot.simulator.state.DeviceRuntimeState;
import info.zhihui.ems.iot.simulator.state.SimulatorStateSnapshot;
import info.zhihui.ems.iot.simulator.state.StateStore;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 单台模拟电表的运行编排器。
 */
@Slf4j
final class SimulatorDeviceRuntime {

    private static final String SWITCH_ON = "ON";

    private final SimulatorProperties simulatorProperties;
    private final StateStore stateStore;
    private final EnergySimulationService energySimulationService;
    private final ReplayScheduleService replayScheduleService;
    private final Acrel4gSimulatorClient acrel4gSimulatorClient;
    private final Acrel4gCommandResponder acrel4gCommandResponder;
    private final Acrel4gFrameCodec acrel4gFrameCodec;
    private final SimulatorDeviceContext deviceContext;
    private final SimulatorStateSnapshot stateSnapshot;
    private final Acrel4gSocketSession socketSession;
    private final ScheduledExecutorService heartbeatExecutor;
    private final ExecutorService reportExecutor;
    private final Object connectionLock = new Object();
    private final LocalDateTime startupAnchorTime = LocalDateTime.now();

    private volatile boolean stopped;

    SimulatorDeviceRuntime(SimulatorProperties simulatorProperties,
                           StateStore stateStore,
                           EnergySimulationService energySimulationService,
                           ReplayScheduleService replayScheduleService,
                           Acrel4gSimulatorClient acrel4gSimulatorClient,
                           Acrel4gCommandResponder acrel4gCommandResponder,
                           Acrel4gFrameCodec acrel4gFrameCodec,
                           SimulatorDeviceContext deviceContext,
                           SimulatorStateSnapshot stateSnapshot) {
        this(simulatorProperties, stateStore, energySimulationService, replayScheduleService, acrel4gSimulatorClient,
                acrel4gCommandResponder, acrel4gFrameCodec, deviceContext, stateSnapshot, null);
    }

    SimulatorDeviceRuntime(SimulatorProperties simulatorProperties,
                           StateStore stateStore,
                           EnergySimulationService energySimulationService,
                           ReplayScheduleService replayScheduleService,
                           Acrel4gSimulatorClient acrel4gSimulatorClient,
                           Acrel4gCommandResponder acrel4gCommandResponder,
                           Acrel4gFrameCodec acrel4gFrameCodec,
                           SimulatorDeviceContext deviceContext,
                           SimulatorStateSnapshot stateSnapshot,
                           Acrel4gSocketSession socketSession) {
        this.simulatorProperties = Objects.requireNonNull(simulatorProperties, "simulatorProperties cannot be null");
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore cannot be null");
        this.energySimulationService = Objects.requireNonNull(energySimulationService, "energySimulationService cannot be null");
        this.replayScheduleService = Objects.requireNonNull(replayScheduleService, "replayScheduleService cannot be null");
        this.acrel4gSimulatorClient = Objects.requireNonNull(acrel4gSimulatorClient, "acrel4gSimulatorClient cannot be null");
        this.acrel4gCommandResponder = Objects.requireNonNull(acrel4gCommandResponder, "acrel4gCommandResponder cannot be null");
        this.acrel4gFrameCodec = Objects.requireNonNull(acrel4gFrameCodec, "acrel4gFrameCodec cannot be null");
        this.deviceContext = Objects.requireNonNull(deviceContext, "deviceContext cannot be null");
        this.stateSnapshot = Objects.requireNonNull(stateSnapshot, "stateSnapshot cannot be null");
        this.socketSession = socketSession == null
                ? new Acrel4gSocketSession(simulatorProperties.getTarget(), deviceContext, this::handleInboundFrame)
                : socketSession;
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(buildThreadFactory("heartbeat"));
        this.reportExecutor = Executors.newSingleThreadExecutor(buildThreadFactory("report"));
    }

    /**
     * 启动单台设备的完整运行链路：建连注册、心跳和上报循环。
     */
    void start() {
        ensureConnectedAndRegistered();
        startHeartbeat();
        reportExecutor.submit(this::runReportLoop);
    }

    /**
     * 停止当前设备的所有后台任务，并主动关闭 TCP 会话。
     */
    void stop() {
        stopped = true;
        heartbeatExecutor.shutdownNow();
        reportExecutor.shutdownNow();
        socketSession.close();
    }

    /**
     * 启动固定频率的心跳调度，维持设备在线状态。
     */
    private void startHeartbeat() {
        long heartbeatIntervalSeconds = Math.max(1, simulatorProperties.getRuntime().getHeartbeatIntervalSeconds());
        heartbeatExecutor.scheduleWithFixedDelay(this::sendHeartbeat, heartbeatIntervalSeconds, heartbeatIntervalSeconds,
                TimeUnit.SECONDS);
    }

    /**
     * 执行单台设备的主上报循环，先补历史再切换到实时模式。
     */
    private void runReportLoop() {
        try {
            runReplayIfNecessary();
            runLiveLoop();
        } catch (RuntimeException ex) {
            log.error("模拟设备上报循环异常 deviceNo={}", deviceContext.getDeviceProperties().getDeviceNo(), ex);
        }
    }

    /**
     * 按配置补发历史时间段数据，补投完成后更新完成标记。
     */
    private void runReplayIfNecessary() {
        SimulatorReplayProperties replayProperties = simulatorProperties.getReplay();
        DeviceRuntimeState runtimeState = deviceContext.getRuntimeState();
        if (!Boolean.TRUE.equals(replayProperties.getEnabled()) || Boolean.TRUE.equals(runtimeState.getReplayCompleted())) {
            return;
        }
        log.info("模拟设备开始历史补投 deviceNo={} startTime={} endTime={} sendIntervalMs={}",
                deviceContext.getDeviceProperties().getDeviceNo(),
                replayProperties.getStartTime(),
                replayProperties.getEndTime(),
                replayProperties.getSendIntervalMs());
        List<LocalDateTime> replayPoints = replayScheduleService.buildReplayPoints(
                replayProperties.getStartTime(),
                replayProperties.getEndTime(),
                runtimeState.getReplayCursorTime());
        for (LocalDateTime replayPointTime : replayPoints) {
            if (stopped) {
                return;
            }
            runReplayPoint(replayPointTime);
            sleepQuietly(replayProperties.getSendIntervalMs());
        }
        markReplayCompleted();
        log.info("模拟设备完成历史补投 deviceNo={}", deviceContext.getDeviceProperties().getDeviceNo());
    }

    /**
     * 进入实时上报循环，按启动锚点每小时顺序发送一次。
     */
    private void runLiveLoop() {
        LocalDateTime nextPointTime = resolveFirstLivePoint();
        while (!stopped) {
            sleepUntil(nextPointTime);
            if (stopped) {
                return;
            }
            runLivePoint(nextPointTime);
            nextPointTime = nextPointTime.plusHours(1);
        }
    }

    /**
     * 计算实时模式的首个发送时间点；若先补投，则取晚于当前时间的首个锚点。
     */
    private LocalDateTime resolveFirstLivePoint() {
        if (Boolean.TRUE.equals(simulatorProperties.getReplay().getEnabled())) {
            return nextLivePoint(startupAnchorTime, LocalDateTime.now());
        }
        return nextLivePoint(startupAnchorTime, null);
    }

    /**
     * 发送一条心跳报文，心跳失败时由底层发送链路负责重连。
     */
    private void sendHeartbeat() {
        if (stopped) {
            return;
        }
        log.debug("模拟设备发送心跳 deviceNo={}", deviceContext.getDeviceProperties().getDeviceNo());
        byte[] heartbeatFrame = acrel4gSimulatorClient.buildHeartbeatFrame(deviceContext, new HeartbeatMessage());
        sendSimulatorFrame(heartbeatFrame);
    }

    /**
     * 构造并发送注册报文，让 IoT 侧建立该设备与当前连接的绑定关系。
     */
    private void sendRegisterFrame() throws IOException {
        RegisterMessage registerMessage = buildRegisterMessage(deviceContext.getDeviceProperties());
        byte[] registerFrame = acrel4gSimulatorClient.buildRegisterFrame(deviceContext, registerMessage);
        log.info("模拟设备发送注册报文 deviceNo={} iccid={} reportIntervalMinutes={}",
                deviceContext.getDeviceProperties().getDeviceNo(),
                registerMessage.getIccid(),
                registerMessage.getReportIntervalMinutes());
        socketSession.send(registerFrame);
    }

    /**
     * 发送任意模拟器报文；如果连接断开，会先尝试重连并重新注册。
     */
    private void sendSimulatorFrame(byte[] frame) {
        if (frame == null || frame.length == 0 || stopped) {
            return;
        }
        try {
            synchronized (connectionLock) {
                ensureConnectedAndRegistered();
                socketSession.send(frame);
            }
        } catch (IOException ex) {
            log.warn("模拟设备发送失败 deviceNo={}", deviceContext.getDeviceProperties().getDeviceNo(), ex);
            socketSession.close();
        }
    }

    /**
     * 确保当前设备已经连上 IoT 端口，并在每次重连成功后重新发送注册报文。
     */
    private void ensureConnectedAndRegistered() {
        synchronized (connectionLock) {
            while (!stopped) {
                if (socketSession.isConnected()) {
                    return;
                }
                try {
                    socketSession.connect();
                    sendRegisterFrame();
                    return;
                } catch (IOException ex) {
                    socketSession.close();
                    log.warn("模拟设备连接失败 deviceNo={} host={} port={}",
                            deviceContext.getDeviceProperties().getDeviceNo(),
                            simulatorProperties.getTarget().getHost(),
                            simulatorProperties.getTarget().getPort(),
                            ex);
                    sleepQuietly(simulatorProperties.getTarget().getReconnectIntervalMs());
                }
            }
        }
    }

    /**
     * 处理 IoT 侧下发的完整帧，必要时更新状态并返回下行响应。
     */
    private void handleInboundFrame(byte[] inboundFrame) {
        CommandHandleResult commandHandleResult = acrel4gCommandResponder.handle(deviceContext.getRuntimeState(), inboundFrame);
        if (!commandHandleResult.isHandled() || commandHandleResult.getResponseFrame() == null) {
            log.debug("模拟设备忽略入站帧 deviceNo={} bytes={}",
                    deviceContext.getDeviceProperties().getDeviceNo(),
                    inboundFrame == null ? 0 : inboundFrame.length);
            return;
        }
        persistRuntimeState();
        byte[] ackFrame = acrel4gFrameCodec.encode(Acrel4gCommandConstants.DOWNLINK, commandHandleResult.getResponseFrame());
        log.info("模拟设备发送下行响应 deviceNo={} commandName={} responseBytes={}",
                deviceContext.getDeviceProperties().getDeviceNo(),
                commandHandleResult.getCommandName(),
                ackFrame.length);
        sendSimulatorFrame(ackFrame);
    }

    /**
     * 执行一次历史补投点的上报。
     */
    void runReplayPoint(LocalDateTime reportTime) {
        runScheduledReport(reportTime, true);
    }

    /**
     * 执行一次实时上报点的上报。
     */
    void runLivePoint(LocalDateTime reportTime) {
        runScheduledReport(reportTime, false);
    }

    /**
     * 标记历史补投已经完成，并持久化运行状态。
     */
    void markReplayCompleted() {
        DeviceRuntimeState runtimeState = ensureRuntimeState();
        runtimeState.setReplayCompleted(true);
        saveRuntimeState(runtimeState);
    }

    /**
     * 执行某一个计划时间点的完整上报流程：生成快照、更新状态、持久化并发送报文。
     */
    private void runScheduledReport(LocalDateTime reportTime, boolean replayMode) {
        DeviceRuntimeState runtimeState = ensureRuntimeState();
        EnergySnapshot energySnapshot = energySimulationService.generateSnapshot(
                runtimeState, deviceContext.getDeviceProperties(), reportTime);
        updateRuntimeState(runtimeState, energySnapshot, replayMode);
        saveRuntimeState(runtimeState);
        if (!Boolean.TRUE.equals(energySnapshot.getShouldReport())) {
            log.info("模拟设备跳过{}上报 deviceNo={} reportTime={} switchStatus={}",
                    resolveModeName(replayMode),
                    runtimeState.getDeviceNo(),
                    reportTime,
                    runtimeState.getSwitchStatus());
            return;
        }
        DataUploadMessage dataUploadMessage = buildDataUploadMessage(energySnapshot, deviceContext.getDeviceProperties());
        log.info("模拟设备发送{}上报 deviceNo={} meterAddress={} reportTime={} totalEnergy={} increment={}",
                resolveModeName(replayMode),
                dataUploadMessage.getSerialNumber(),
                dataUploadMessage.getMeterAddress(),
                dataUploadMessage.getTime(),
                energySnapshot.getTotalEnergy(),
                energySnapshot.getIncrement() == null ? null : energySnapshot.getIncrement().getTotalEnergyIncrement());
        byte[] uploadFrame = acrel4gSimulatorClient.buildDataUploadFrame(deviceContext, dataUploadMessage);
        sendSimulatorFrame(uploadFrame);
    }

    /**
     * 获取当前设备的运行态；若尚未初始化，则按配置创建默认运行态。
     */
    private DeviceRuntimeState ensureRuntimeState() {
        DeviceRuntimeState runtimeState = deviceContext.getRuntimeState();
        if (runtimeState != null) {
            return runtimeState;
        }
        SimulatorDeviceProperties deviceProperties = deviceContext.getDeviceProperties();
        runtimeState = new DeviceRuntimeState()
                .setDeviceNo(deviceProperties.getDeviceNo())
                .setVendor(deviceProperties.getVendor())
                .setProductCode(deviceProperties.getProductCode())
                .setAccessMode(deviceProperties.getAccessMode())
                .setSwitchStatus(SWITCH_ON);
        deviceContext.setRuntimeState(runtimeState);
        return runtimeState;
    }

    /**
     * 把本次生成的能耗快照回写到运行态，供下次上报和读数命令继续使用。
     */
    private void updateRuntimeState(DeviceRuntimeState runtimeState, EnergySnapshot energySnapshot, boolean replayMode) {
        runtimeState
                .setLastTotalEnergy(energySnapshot.getTotalEnergy())
                .setLastHigherEnergy(energySnapshot.getHigherEnergy())
                .setLastHighEnergy(energySnapshot.getHighEnergy())
                .setLastLowEnergy(energySnapshot.getLowEnergy())
                .setLastLowerEnergy(energySnapshot.getLowerEnergy())
                .setLastDeepLowEnergy(energySnapshot.getDeepLowEnergy());
        if (replayMode) {
            runtimeState.setReplayCursorTime(energySnapshot.getReportTime());
        }
        if (Boolean.TRUE.equals(energySnapshot.getShouldReport())) {
            runtimeState.setLastReportedAt(energySnapshot.getReportTime());
        }
    }

    /**
     * 将能耗快照转换为安科瑞 4G 数据上报报文模型。
     */
    private DataUploadMessage buildDataUploadMessage(EnergySnapshot energySnapshot,
                                                     SimulatorDeviceProperties deviceProperties) {
        return new DataUploadMessage()
                .setSerialNumber(deviceProperties.getDeviceNo())
                .setMeterAddress(deviceProperties.getMeterAddress())
                .setTime(energySnapshot.getReportTime())
                .setTotalEnergy(toProtocolValue(energySnapshot.getTotalEnergy()))
                .setHigherEnergy(toProtocolValue(energySnapshot.getHigherEnergy()))
                .setHighEnergy(toProtocolValue(energySnapshot.getHighEnergy()))
                .setLowEnergy(toProtocolValue(energySnapshot.getLowEnergy()))
                .setLowerEnergy(toProtocolValue(energySnapshot.getLowerEnergy()))
                .setDeepLowEnergy(toProtocolValue(energySnapshot.getDeepLowEnergy()));
    }

    /**
     * 将业务侧 kWh 小数值转换成协议要求的整型分值。
     */
    private int toProtocolValue(BigDecimal energyValue) {
        BigDecimal safeEnergyValue = energyValue == null ? BigDecimal.ZERO : energyValue;
        return safeEnergyValue.movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    /**
     * 根据当前模式返回日志展示名称。
     */
    private String resolveModeName(boolean replayMode) {
        return replayMode ? "历史补投" : "实时";
    }

    /**
     * 将当前运行态落盘，通常用于下行命令处理后立即保存开关状态。
     */
    private void persistRuntimeState() {
        DeviceRuntimeState runtimeState = deviceContext.getRuntimeState();
        if (runtimeState == null) {
            return;
        }
        saveRuntimeState(runtimeState);
    }

    /**
     * 在同一把锁内更新共享快照并保存到状态文件，避免多设备并发覆盖。
     */
    private void saveRuntimeState(DeviceRuntimeState runtimeState) {
        synchronized (stateStore) {
            stateSnapshot.getDeviceStateMap().put(runtimeState.getDeviceNo(), runtimeState);
            stateStore.save(stateSnapshot);
        }
    }

    /**
     * 根据设备档案生成注册消息，固定填充当前模拟器约定的默认值。
     */
    private RegisterMessage buildRegisterMessage(SimulatorDeviceProperties deviceProperties) {
        String deviceNo = deviceProperties.getDeviceNo();
        return new RegisterMessage()
                .setSerialNumber(deviceNo)
                .setIccid(buildIccid(deviceNo))
                .setRssi(20)
                .setFirmware1("0001")
                .setFirmware2("0000")
                .setFirmware3("0000")
                .setReportIntervalMinutes(60);
    }

    /**
     * 从设备编号中提取或补齐 20 位 ICCID，用于模拟注册报文。
     */
    private String buildIccid(String deviceNo) {
        String source = deviceNo == null ? "" : deviceNo.replaceAll("[^0-9]", "");
        if (source.isEmpty()) {
            source = "89860000000000000000";
        }
        return (source + "000000000000000000000000000000").substring(0, 20);
    }

    /**
     * 阻塞等待到目标时间点，按秒级轮询以便及时响应停止信号。
     */
    private void sleepUntil(LocalDateTime targetTime) {
        while (!stopped) {
            long waitMillis = java.time.Duration.between(LocalDateTime.now(), targetTime).toMillis();
            if (waitMillis <= 0) {
                return;
            }
            sleepQuietly(Math.min(waitMillis, 1000));
        }
    }

    /**
     * 安静休眠指定时长；若线程被打断，则同步标记当前运行器停止。
     */
    private void sleepQuietly(long sleepMillis) {
        if (sleepMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            stopped = true;
        }
    }

    /**
     * 依据启动锚点和参考时间，计算下一次实时上报应该落在哪个整小时偏移点。
     */
    private LocalDateTime nextLivePoint(LocalDateTime startupTime, LocalDateTime referenceTime) {
        if (startupTime == null) {
            throw new IllegalArgumentException("实时上报启动时间不能为空");
        }
        if (referenceTime == null || referenceTime.isBefore(startupTime)) {
            return startupTime;
        }
        LocalDateTime nextPointTime = startupTime;
        while (!nextPointTime.isAfter(referenceTime)) {
            nextPointTime = nextPointTime.plusHours(1);
        }
        return nextPointTime;
    }

    /**
     * 为心跳线程和上报线程生成可识别的线程名。
     */
    private ThreadFactory buildThreadFactory(String threadType) {
        String threadName = "iot-simulator-" + threadType + "-" + deviceContext.getDeviceProperties().getDeviceNo();
        return runnable -> {
            Thread thread = new Thread(runnable, threadName);
            thread.setDaemon(false);
            return thread;
        };
    }
}
