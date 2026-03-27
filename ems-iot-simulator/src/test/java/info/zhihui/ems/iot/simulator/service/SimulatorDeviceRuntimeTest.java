package info.zhihui.ems.iot.simulator.service;

import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.VendorEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.simulator.config.ProfileTypeEnum;
import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;
import info.zhihui.ems.iot.simulator.config.SimulatorProperties;
import info.zhihui.ems.iot.simulator.config.SimulatorReplayProperties;
import info.zhihui.ems.iot.simulator.config.SimulatorRuntimeProperties;
import info.zhihui.ems.iot.simulator.config.SimulatorTargetProperties;
import info.zhihui.ems.iot.simulator.protocol.acrel.Acrel4gCommandResponder;
import info.zhihui.ems.iot.simulator.protocol.acrel.Acrel4gMessageFactory;
import info.zhihui.ems.iot.simulator.protocol.acrel.Acrel4gSimulatorClient;
import info.zhihui.ems.iot.simulator.protocol.acrel.Acrel4gSocketSession;
import info.zhihui.ems.iot.simulator.runtime.SimulatorDeviceContext;
import info.zhihui.ems.iot.simulator.service.profile.impl.FactoryEnergyProfileGenerator;
import info.zhihui.ems.iot.simulator.service.profile.impl.OfficeEnergyProfileGenerator;
import info.zhihui.ems.iot.simulator.state.DeviceRuntimeState;
import info.zhihui.ems.iot.simulator.state.SimulatorStateSnapshot;
import info.zhihui.ems.iot.simulator.state.StateStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class SimulatorDeviceRuntimeTest {

    private final Acrel4gFrameCodec frameCodec = new Acrel4gFrameCodec();
    private final EnergySimulationService energySimulationService = new EnergySimulationService(
            List.of(new OfficeEnergyProfileGenerator(), new FactoryEnergyProfileGenerator()));
    private final ReplayScheduleService replayScheduleService = new ReplayScheduleService();
    private final Acrel4gSimulatorClient simulatorClient = new Acrel4gSimulatorClient(new Acrel4gMessageFactory(frameCodec));
    private final Acrel4gCommandResponder commandResponder = new Acrel4gCommandResponder(frameCodec);

    @Test
    void persistRuntimeState_whenSavingSharedSnapshot_shouldHoldStateStoreLock() throws Exception {
        LockCheckingStateStore stateStore = new LockCheckingStateStore();
        SimulatorStateSnapshot stateSnapshot = new SimulatorStateSnapshot()
                .setDeviceStateMap(new LockCheckingMap(stateStore));
        SimulatorDeviceRuntime simulatorDeviceRuntime = new SimulatorDeviceRuntime(
                buildSimulatorProperties(),
                stateStore,
                energySimulationService,
                replayScheduleService,
                simulatorClient,
                commandResponder,
                frameCodec,
                buildDeviceContext("SIM001", "00000001", "ON", 101L),
                stateSnapshot);

        Method persistMethod = SimulatorDeviceRuntime.class.getDeclaredMethod("persistRuntimeState");
        persistMethod.setAccessible(true);

        Assertions.assertDoesNotThrow(() -> persistMethod.invoke(simulatorDeviceRuntime));
        Assertions.assertEquals("SIM001", stateStore.lastSnapshot.getDeviceStateMap().get("SIM001").getDeviceNo());
    }

    @Test
    void runReplayPoint_whenSwitchOff_shouldSkipReportAndPersistReplayCursor() {
        InMemoryStateStore stateStore = new InMemoryStateStore();
        FrameCollector frameCollector = new FrameCollector();
        SimulatorDeviceContext deviceContext = buildDeviceContext("SIM001", "00000001", "OFF", 101L);
        SimulatorDeviceRuntime simulatorDeviceRuntime = new SimulatorDeviceRuntime(
                buildSimulatorProperties(),
                stateStore,
                energySimulationService,
                replayScheduleService,
                simulatorClient,
                commandResponder,
                frameCodec,
                deviceContext,
                new SimulatorStateSnapshot(),
                buildSocketSession(deviceContext, frameCollector));

        LocalDateTime reportTime = LocalDateTime.of(2026, 3, 24, 11, 23, 15);
        simulatorDeviceRuntime.runReplayPoint(reportTime);

        Assertions.assertEquals(0, frameCollector.frameCount);
        Assertions.assertEquals(reportTime, stateStore.lastSnapshot.getDeviceStateMap().get("SIM001").getReplayCursorTime());
        Assertions.assertNull(stateStore.lastSnapshot.getDeviceStateMap().get("SIM001").getLastReportedAt());
    }

    @Test
    void runLivePoint_whenSwitchOn_shouldReportAndPersistEnergyState() {
        InMemoryStateStore stateStore = new InMemoryStateStore();
        FrameCollector frameCollector = new FrameCollector();
        SimulatorDeviceContext deviceContext = buildDeviceContext("SIM001", "00000001", "ON", 101L);
        SimulatorDeviceRuntime simulatorDeviceRuntime = new SimulatorDeviceRuntime(
                buildSimulatorProperties(),
                stateStore,
                energySimulationService,
                replayScheduleService,
                simulatorClient,
                commandResponder,
                frameCodec,
                deviceContext,
                new SimulatorStateSnapshot(),
                buildSocketSession(deviceContext, frameCollector));

        LocalDateTime reportTime = LocalDateTime.of(2026, 3, 24, 11, 23, 15);
        simulatorDeviceRuntime.runLivePoint(reportTime);

        DeviceRuntimeState runtimeState = stateStore.lastSnapshot.getDeviceStateMap().get("SIM001");
        Assertions.assertEquals(1, frameCollector.frameCount);
        Assertions.assertEquals(reportTime, runtimeState.getLastReportedAt());
        Assertions.assertTrue(runtimeState.getLastTotalEnergy().compareTo(new BigDecimal("12.34")) > 0);
        Assertions.assertNull(runtimeState.getReplayCursorTime());
    }

    @Test
    void markReplayCompleted_whenCalled_shouldPersistReplayFlag() {
        InMemoryStateStore stateStore = new InMemoryStateStore();
        SimulatorDeviceContext deviceContext = buildDeviceContext("SIM001", "00000001", "ON", 101L);
        SimulatorDeviceRuntime simulatorDeviceRuntime = new SimulatorDeviceRuntime(
                buildSimulatorProperties(),
                stateStore,
                energySimulationService,
                replayScheduleService,
                simulatorClient,
                commandResponder,
                frameCodec,
                deviceContext,
                new SimulatorStateSnapshot(),
                buildSocketSession(deviceContext, new FrameCollector()));

        simulatorDeviceRuntime.markReplayCompleted();

        Assertions.assertEquals(Boolean.TRUE, stateStore.lastSnapshot.getDeviceStateMap().get("SIM001").getReplayCompleted());
    }

    @Test
    void runLivePoint_whenTwoDevicesShareSameServices_shouldPersistIndependentRuntimeState() {
        SimulatorStateSnapshot sharedStateSnapshot = new SimulatorStateSnapshot();
        InMemoryStateStore stateStore = new InMemoryStateStore(sharedStateSnapshot);
        FrameCollector frameCollector = new FrameCollector();
        SimulatorDeviceContext firstDeviceContext = buildDeviceContext("SIM001", "00000001", "ON", 101L);
        SimulatorDeviceContext secondDeviceContext = buildDeviceContext("SIM002", "00000002", "ON", 202L);
        SimulatorDeviceRuntime firstRuntime = new SimulatorDeviceRuntime(
                buildSimulatorProperties(),
                stateStore,
                energySimulationService,
                replayScheduleService,
                simulatorClient,
                commandResponder,
                frameCodec,
                firstDeviceContext,
                sharedStateSnapshot,
                buildSocketSession(firstDeviceContext, frameCollector));
        SimulatorDeviceRuntime secondRuntime = new SimulatorDeviceRuntime(
                buildSimulatorProperties(),
                stateStore,
                energySimulationService,
                replayScheduleService,
                simulatorClient,
                commandResponder,
                frameCodec,
                secondDeviceContext,
                sharedStateSnapshot,
                buildSocketSession(secondDeviceContext, frameCollector));

        LocalDateTime reportTime = LocalDateTime.of(2026, 3, 24, 11, 23, 15);
        firstRuntime.runLivePoint(reportTime);
        secondRuntime.runLivePoint(reportTime.plusHours(1));

        Map<String, DeviceRuntimeState> deviceStateMap = stateStore.lastSnapshot.getDeviceStateMap();
        Assertions.assertEquals(2, deviceStateMap.size());
        Assertions.assertTrue(deviceStateMap.containsKey("SIM001"));
        Assertions.assertTrue(deviceStateMap.containsKey("SIM002"));
        Assertions.assertEquals(reportTime, deviceStateMap.get("SIM001").getLastReportedAt());
        Assertions.assertEquals(reportTime.plusHours(1), deviceStateMap.get("SIM002").getLastReportedAt());
        Assertions.assertEquals(2, frameCollector.frameCount);
        Assertions.assertEquals(List.of("SIM001", "SIM002"), frameCollector.deviceNoList);
    }

    @Test
    void runLivePoint_whenSavingSharedSnapshot_shouldHoldStateStoreLock() {
        LockCheckingStateStore stateStore = new LockCheckingStateStore();
        SimulatorStateSnapshot stateSnapshot = new SimulatorStateSnapshot()
                .setDeviceStateMap(new LockCheckingMap(stateStore));
        FrameCollector frameCollector = new FrameCollector();
        SimulatorDeviceContext deviceContext = buildDeviceContext("SIM001", "00000001", "ON", 101L);
        SimulatorDeviceRuntime simulatorDeviceRuntime = new SimulatorDeviceRuntime(
                buildSimulatorProperties(),
                stateStore,
                energySimulationService,
                replayScheduleService,
                simulatorClient,
                commandResponder,
                frameCodec,
                deviceContext,
                stateSnapshot,
                buildSocketSession(deviceContext, frameCollector));

        LocalDateTime reportTime = LocalDateTime.of(2026, 3, 24, 11, 23, 15);

        Assertions.assertDoesNotThrow(() -> simulatorDeviceRuntime.runLivePoint(reportTime));
        Assertions.assertEquals(reportTime, stateStore.lastSnapshot.getDeviceStateMap().get("SIM001").getLastReportedAt());
    }

    @Test
    void nextLivePoint_whenReferenceTimeIsNull_shouldReturnStartupTime() throws Exception {
        LocalDateTime startupTime = LocalDateTime.of(2026, 3, 27, 10, 23, 15);
        SimulatorDeviceRuntime simulatorDeviceRuntime = new SimulatorDeviceRuntime(
                buildSimulatorProperties(),
                new InMemoryStateStore(),
                energySimulationService,
                replayScheduleService,
                simulatorClient,
                commandResponder,
                frameCodec,
                buildDeviceContext("SIM001", "00000001", "ON", 101L),
                new SimulatorStateSnapshot(),
                buildSocketSession(buildDeviceContext("SIM001", "00000001", "ON", 101L), new FrameCollector()));

        LocalDateTime nextPointTime = invokeNextLivePoint(simulatorDeviceRuntime, startupTime, null);

        Assertions.assertEquals(startupTime, nextPointTime);
    }

    @Test
    void nextLivePoint_whenReferenceTimeBeforeStartup_shouldReturnStartupTime() throws Exception {
        LocalDateTime startupTime = LocalDateTime.of(2026, 3, 27, 10, 23, 15);
        LocalDateTime referenceTime = startupTime.minusMinutes(30);
        SimulatorDeviceRuntime simulatorDeviceRuntime = new SimulatorDeviceRuntime(
                buildSimulatorProperties(),
                new InMemoryStateStore(),
                energySimulationService,
                replayScheduleService,
                simulatorClient,
                commandResponder,
                frameCodec,
                buildDeviceContext("SIM001", "00000001", "ON", 101L),
                new SimulatorStateSnapshot(),
                buildSocketSession(buildDeviceContext("SIM001", "00000001", "ON", 101L), new FrameCollector()));

        LocalDateTime nextPointTime = invokeNextLivePoint(simulatorDeviceRuntime, startupTime, referenceTime);

        Assertions.assertEquals(startupTime, nextPointTime);
    }

    @Test
    void nextLivePoint_whenReferenceTimeAtOrAfterStartup_shouldReturnNextFuturePoint() throws Exception {
        LocalDateTime startupTime = LocalDateTime.of(2026, 3, 27, 10, 23, 15);
        SimulatorDeviceRuntime simulatorDeviceRuntime = new SimulatorDeviceRuntime(
                buildSimulatorProperties(),
                new InMemoryStateStore(),
                energySimulationService,
                replayScheduleService,
                simulatorClient,
                commandResponder,
                frameCodec,
                buildDeviceContext("SIM001", "00000001", "ON", 101L),
                new SimulatorStateSnapshot(),
                buildSocketSession(buildDeviceContext("SIM001", "00000001", "ON", 101L), new FrameCollector()));

        LocalDateTime atStartupTime = invokeNextLivePoint(simulatorDeviceRuntime, startupTime, startupTime);
        LocalDateTime afterStartupTime = invokeNextLivePoint(simulatorDeviceRuntime, startupTime, startupTime.plusHours(1).plusMinutes(5));

        Assertions.assertEquals(startupTime.plusHours(1), atStartupTime);
        Assertions.assertEquals(startupTime.plusHours(2), afterStartupTime);
    }

    @Test
    void ensureConnectedAndRegistered_whenFirstConnectFails_shouldRetryAndSendRegister() throws Exception {
        SimulatorProperties simulatorProperties = buildSimulatorProperties();
        simulatorProperties.getTarget().setReconnectIntervalMs(1);
        FrameCollector frameCollector = new FrameCollector();
        SimulatorDeviceContext deviceContext = buildDeviceContext("SIM001", "00000001", "ON", 101L);
        RetryingSocketSession socketSession = new RetryingSocketSession(deviceContext, frameCollector, 1);
        SimulatorDeviceRuntime simulatorDeviceRuntime = new SimulatorDeviceRuntime(
                simulatorProperties,
                new InMemoryStateStore(),
                energySimulationService,
                replayScheduleService,
                simulatorClient,
                commandResponder,
                frameCodec,
                deviceContext,
                new SimulatorStateSnapshot(),
                socketSession);

        invokeEnsureConnectedAndRegistered(simulatorDeviceRuntime);

        Assertions.assertEquals(2, socketSession.connectAttemptCount);
        Assertions.assertEquals(1, frameCollector.frameCount);
    }

    private SimulatorProperties buildSimulatorProperties() {
        SimulatorTargetProperties targetProperties = new SimulatorTargetProperties();
        targetProperties.setHost("127.0.0.1");
        targetProperties.setPort(19500);
        targetProperties.setConnectTimeoutMs(1000);
        targetProperties.setReconnectIntervalMs(100);

        SimulatorRuntimeProperties runtimeProperties = new SimulatorRuntimeProperties();
        runtimeProperties.setHeartbeatIntervalSeconds(60);
        runtimeProperties.setPersistenceFile("./.data/iot-simulator-state-test.json");

        SimulatorReplayProperties replayProperties = new SimulatorReplayProperties();
        replayProperties.setEnabled(false);

        SimulatorProperties simulatorProperties = new SimulatorProperties();
        simulatorProperties.setTarget(targetProperties);
        simulatorProperties.setRuntime(runtimeProperties);
        simulatorProperties.setReplay(replayProperties);
        simulatorProperties.setDevices(List.of(buildDeviceProperties()));
        return simulatorProperties;
    }

    private SimulatorDeviceContext buildDeviceContext(String deviceNo, String meterAddress, String switchStatus, Long randomSeed) {
        return new SimulatorDeviceContext()
                .setDeviceProperties(buildDeviceProperties(deviceNo, meterAddress, ProfileTypeEnum.OFFICE, randomSeed))
                .setRuntimeState(new DeviceRuntimeState()
                        .setDeviceNo(deviceNo)
                        .setVendor(VendorEnum.ACREL)
                        .setProductCode("ACREL_4G_DIRECT")
                        .setAccessMode(DeviceAccessModeEnum.DIRECT)
                        .setSwitchStatus(switchStatus)
                        .setLastTotalEnergy(new BigDecimal("12.34"))
                        .setLastHigherEnergy(new BigDecimal("10.00"))
                        .setLastHighEnergy(new BigDecimal("20.00"))
                        .setLastLowEnergy(new BigDecimal("30.00"))
                        .setLastLowerEnergy(new BigDecimal("25.00"))
                        .setLastDeepLowEnergy(new BigDecimal("15.00"))
                        .setReplayCompleted(true));
    }

    private SimulatorDeviceProperties buildDeviceProperties() {
        return buildDeviceProperties("SIM001", "00000001", ProfileTypeEnum.FACTORY, 101L);
    }

    private SimulatorDeviceProperties buildDeviceProperties(String deviceNo,
                                                            String meterAddress,
                                                            ProfileTypeEnum profileType,
                                                            Long randomSeed) {
        SimulatorDeviceProperties deviceProperties = new SimulatorDeviceProperties();
        deviceProperties.setDeviceNo(deviceNo);
        deviceProperties.setVendor(VendorEnum.ACREL);
        deviceProperties.setProductCode("ACREL_4G_DIRECT");
        deviceProperties.setAccessMode(DeviceAccessModeEnum.DIRECT);
        deviceProperties.setMeterAddress(meterAddress);
        deviceProperties.setProfileType(profileType);
        deviceProperties.setRandomSeed(randomSeed);
        return deviceProperties;
    }

    private Acrel4gSocketSession buildSocketSession(SimulatorDeviceContext deviceContext, FrameCollector frameCollector) {
        return new FakeSocketSession(deviceContext, frameCollector);
    }

    private LocalDateTime invokeNextLivePoint(SimulatorDeviceRuntime simulatorDeviceRuntime,
                                              LocalDateTime startupTime,
                                              LocalDateTime referenceTime) throws Exception {
        Method nextLivePointMethod = SimulatorDeviceRuntime.class.getDeclaredMethod(
                "nextLivePoint", LocalDateTime.class, LocalDateTime.class);
        nextLivePointMethod.setAccessible(true);
        return (LocalDateTime) nextLivePointMethod.invoke(simulatorDeviceRuntime, startupTime, referenceTime);
    }

    private void invokeEnsureConnectedAndRegistered(SimulatorDeviceRuntime simulatorDeviceRuntime) throws Exception {
        Method ensureConnectedAndRegisteredMethod = SimulatorDeviceRuntime.class.getDeclaredMethod("ensureConnectedAndRegistered");
        ensureConnectedAndRegisteredMethod.setAccessible(true);
        ensureConnectedAndRegisteredMethod.invoke(simulatorDeviceRuntime);
    }

    private static final class InMemoryStateStore implements StateStore {

        private SimulatorStateSnapshot lastSnapshot;

        private InMemoryStateStore() {
            this(new SimulatorStateSnapshot());
        }

        private InMemoryStateStore(SimulatorStateSnapshot lastSnapshot) {
            this.lastSnapshot = lastSnapshot;
        }

        @Override
        public synchronized SimulatorStateSnapshot load() {
            return lastSnapshot;
        }

        @Override
        public synchronized void save(SimulatorStateSnapshot snapshot) {
            this.lastSnapshot = snapshot;
        }
    }

    private static final class FrameCollector {

        private int frameCount;
        private final List<String> deviceNoList = new ArrayList<>();

        private void collect(SimulatorDeviceContext deviceContext, byte[] frame) {
            frameCount++;
            Assertions.assertNotNull(deviceContext);
            Assertions.assertNotNull(frame);
            deviceNoList.add(deviceContext.getDeviceProperties().getDeviceNo());
        }
    }

    private static final class FakeSocketSession extends Acrel4gSocketSession {

        private final SimulatorDeviceContext deviceContext;
        private final FrameCollector frameCollector;

        private FakeSocketSession(SimulatorDeviceContext deviceContext, FrameCollector frameCollector) {
            super(new SimulatorTargetProperties(), deviceContext, new NoopInboundConsumer());
            this.deviceContext = deviceContext;
            this.frameCollector = frameCollector;
        }

        @Override
        public synchronized void connect() {
            // no-op
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public void send(byte[] frame) {
            frameCollector.collect(deviceContext, frame);
        }

        @Override
        public synchronized void close() {
            // no-op
        }
    }

    private static final class RetryingSocketSession extends Acrel4gSocketSession {

        private final SimulatorDeviceContext deviceContext;
        private final FrameCollector frameCollector;
        private final int failureCount;
        private int connectAttemptCount;
        private boolean connected;

        private RetryingSocketSession(SimulatorDeviceContext deviceContext,
                                      FrameCollector frameCollector,
                                      int failureCount) {
            super(new SimulatorTargetProperties(), deviceContext, new NoopInboundConsumer());
            this.deviceContext = deviceContext;
            this.frameCollector = frameCollector;
            this.failureCount = failureCount;
        }

        @Override
        public synchronized void connect() throws IOException {
            connectAttemptCount++;
            if (connectAttemptCount <= failureCount) {
                throw new IOException("connect failed");
            }
            connected = true;
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public void send(byte[] frame) {
            frameCollector.collect(deviceContext, frame);
        }

        @Override
        public synchronized void close() {
            connected = false;
        }
    }

    private static final class NoopInboundConsumer implements Consumer<byte[]> {

        @Override
        public void accept(byte[] bytes) {
            // no-op
        }
    }

    private static final class LockCheckingStateStore implements StateStore {

        private SimulatorStateSnapshot lastSnapshot = new SimulatorStateSnapshot();

        @Override
        public synchronized SimulatorStateSnapshot load() {
            return lastSnapshot;
        }

        @Override
        public synchronized void save(SimulatorStateSnapshot snapshot) {
            this.lastSnapshot = snapshot;
        }
    }

    private static final class LockCheckingMap extends HashMap<String, DeviceRuntimeState> {

        private final Object expectedLock;

        private LockCheckingMap(Object expectedLock) {
            this.expectedLock = expectedLock;
        }

        @Override
        public DeviceRuntimeState put(String key, DeviceRuntimeState value) {
            if (!Thread.holdsLock(expectedLock)) {
                throw new IllegalStateException("deviceStateMap.put 必须在 stateStore 锁内执行");
            }
            return super.put(key, value);
        }
    }
}
