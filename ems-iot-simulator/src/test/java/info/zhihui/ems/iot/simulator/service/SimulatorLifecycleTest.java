package info.zhihui.ems.iot.simulator.service;

import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.VendorEnum;
import info.zhihui.ems.iot.plugins.acrel.command.constant.AcrelRegisterMappingEnum;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.support.AcrelPacketKeySupport;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.protocol.modbus.ModbusCrcUtil;
import info.zhihui.ems.iot.protocol.modbus.ModbusMapping;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.simulator.config.*;
import info.zhihui.ems.iot.simulator.protocol.acrel.Acrel4gCommandResponder;
import info.zhihui.ems.iot.simulator.protocol.acrel.Acrel4gMessageFactory;
import info.zhihui.ems.iot.simulator.protocol.acrel.Acrel4gSimulatorClient;
import info.zhihui.ems.iot.simulator.state.DeviceRuntimeState;
import info.zhihui.ems.iot.simulator.state.SimulatorStateSnapshot;
import info.zhihui.ems.iot.simulator.state.StateStore;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorLifecycleTest {

    private final Acrel4gFrameCodec frameCodec = new Acrel4gFrameCodec();

    @Test
    void testLifecycle_WhenDownlinkCutOffReceived_ExpectedPersistStateAndReplyAck() throws Exception {
        try (SocketStubServer socketStubServer = new SocketStubServer()) {
            InMemoryStateStore stateStore = new InMemoryStateStore(buildInitialSnapshot());
            SimulatorProperties simulatorProperties = buildSimulatorProperties(socketStubServer.getPort());
            SimulatorLauncher simulatorLauncher = new SimulatorLauncher(simulatorProperties, stateStore);
            SimulatorLifecycle simulatorLifecycle = new SimulatorLifecycle(
                    simulatorProperties,
                    stateStore,
                    new EnergySimulationService(List.of()),
                    new ReplayScheduleService(),
                    new Acrel4gSimulatorClient(new Acrel4gMessageFactory(frameCodec)),
                    new Acrel4gCommandResponder(frameCodec),
                    frameCodec,
                    simulatorLauncher);

            simulatorLifecycle.start();

            byte[] registerFrame = socketStubServer.readFrame(Duration.ofSeconds(2));
            assertEquals(AcrelPacketKeySupport.commandKey(Acrel4gCommandConstants.REGISTER), frameCodec.decode(registerFrame).commandKey());

            socketStubServer.writeFrame(buildCutOffDownlinkFrame());

            byte[] ackFrame = socketStubServer.readFrame(Duration.ofSeconds(2));
            assertArrayEquals(buildExpectedCutOffAckFrame(), ackFrame);
            assertEquals("OFF", stateStore.snapshot().getDeviceStateMap().get("SIM001").getSwitchStatus());
            assertTrue(stateStore.getSaveCount() > 0);

            simulatorLifecycle.stop();
        }
    }

    private SimulatorProperties buildSimulatorProperties(int port) {
        SimulatorTargetProperties targetProperties = new SimulatorTargetProperties();
        targetProperties.setHost("127.0.0.1");
        targetProperties.setPort(port);
        targetProperties.setConnectTimeoutMs(2000);
        targetProperties.setReconnectIntervalMs(100);

        SimulatorRuntimeProperties runtimeProperties = new SimulatorRuntimeProperties();
        runtimeProperties.setHeartbeatIntervalSeconds(3600);

        SimulatorReplayProperties replayProperties = new SimulatorReplayProperties();
        replayProperties.setEnabled(true);

        SimulatorDeviceProperties deviceProperties = new SimulatorDeviceProperties();
        deviceProperties.setDeviceNo("SIM001");
        deviceProperties.setVendor(VendorEnum.ACREL);
        deviceProperties.setProductCode("ACREL_4G_DIRECT");
        deviceProperties.setAccessMode(DeviceAccessModeEnum.DIRECT);
        deviceProperties.setMeterAddress("00000001");
        deviceProperties.setProfileType(ProfileTypeEnum.FACTORY);
        deviceProperties.setRandomSeed(1L);

        SimulatorProperties simulatorProperties = new SimulatorProperties();
        simulatorProperties.setTarget(targetProperties);
        simulatorProperties.setRuntime(runtimeProperties);
        simulatorProperties.setReplay(replayProperties);
        simulatorProperties.setDevices(List.of(deviceProperties));
        return simulatorProperties;
    }

    private SimulatorStateSnapshot buildInitialSnapshot() {
        DeviceRuntimeState runtimeState = new DeviceRuntimeState()
                .setDeviceNo("SIM001")
                .setVendor(VendorEnum.ACREL)
                .setProductCode("ACREL_4G_DIRECT")
                .setAccessMode(DeviceAccessModeEnum.DIRECT)
                .setSwitchStatus("ON")
                .setLastTotalEnergy(java.math.BigDecimal.valueOf(12.34))
                .setReplayCompleted(true);
        return new SimulatorStateSnapshot().setDeviceStateMap(new java.util.LinkedHashMap<>(java.util.Map.of("SIM001", runtimeState)));
    }

    private byte[] buildCutOffDownlinkFrame() {
        ModbusMapping mapping = AcrelRegisterMappingEnum.CONTROL.toMapping();
        byte[] modbusFrame = ModbusRtuBuilder.build(new ModbusRtuRequest()
                .setSlaveAddress(0x01)
                .setFunction(ModbusRtuBuilder.FUNCTION_WRITE)
                .setStartRegister(mapping.getStartRegister())
                .setQuantity(mapping.getQuantity())
                .setData(new byte[]{0x00, 0x01, 0x00, 0x01}));
        return frameCodec.encode(Acrel4gCommandConstants.DOWNLINK, modbusFrame);
    }

    private byte[] buildExpectedCutOffAckFrame() {
        ModbusMapping mapping = AcrelRegisterMappingEnum.CONTROL.toMapping();
        byte[] body = new byte[]{
                0x01,
                (byte) ModbusRtuBuilder.FUNCTION_WRITE,
                (byte) ((mapping.getStartRegister() >> 8) & 0xFF),
                (byte) (mapping.getStartRegister() & 0xFF),
                (byte) ((mapping.getQuantity() >> 8) & 0xFF),
                (byte) (mapping.getQuantity() & 0xFF)
        };
        byte[] crc = ModbusCrcUtil.crc(body);
        byte[] modbusAckFrame = java.util.Arrays.copyOf(body, body.length + 2);
        modbusAckFrame[body.length] = crc[0];
        modbusAckFrame[body.length + 1] = crc[1];
        return frameCodec.encode(Acrel4gCommandConstants.DOWNLINK, modbusAckFrame);
    }

    private static final class InMemoryStateStore implements StateStore {

        private SimulatorStateSnapshot stateSnapshot;
        private int saveCount;

        private InMemoryStateStore(SimulatorStateSnapshot stateSnapshot) {
            this.stateSnapshot = stateSnapshot;
        }

        @Override
        public synchronized SimulatorStateSnapshot load() {
            return stateSnapshot;
        }

        @Override
        public synchronized void save(SimulatorStateSnapshot snapshot) {
            this.stateSnapshot = snapshot;
            this.saveCount++;
        }

        private synchronized SimulatorStateSnapshot snapshot() {
            return stateSnapshot;
        }

        private synchronized int getSaveCount() {
            return saveCount;
        }
    }

    private static final class SocketStubServer implements AutoCloseable {

        private final ServerSocket serverSocket;
        private final BlockingQueue<Socket> socketQueue = new LinkedBlockingQueue<>();
        private volatile Socket acceptedSocket;

        private SocketStubServer() throws IOException {
            this.serverSocket = new ServerSocket(0);
            Thread acceptThread = new Thread(this::acceptLoop, "lifecycle-socket-stub-accept");
            acceptThread.setDaemon(true);
            acceptThread.start();
        }

        private int getPort() {
            return serverSocket.getLocalPort();
        }

        private void acceptLoop() {
            try {
                socketQueue.offer(serverSocket.accept());
            } catch (IOException ignored) {
                return;
            }
        }

        private byte[] readFrame(Duration timeout) throws Exception {
            Socket socket = getSocket(timeout);
            InputStream inputStream = socket.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            long deadlineTime = System.nanoTime() + timeout.toNanos();
            while (System.nanoTime() < deadlineTime) {
                int currentByte = inputStream.read();
                if (currentByte < 0) {
                    break;
                }
                byteArrayOutputStream.write(currentByte);
                byte[] currentBytes = byteArrayOutputStream.toByteArray();
                int length = currentBytes.length;
                if (length >= 4 && currentBytes[length - 2] == 0x7d && currentBytes[length - 1] == 0x7d) {
                    return currentBytes;
                }
            }
            throw new IllegalStateException("未在超时时间内读到完整帧");
        }

        private void writeFrame(byte[] frame) throws Exception {
            Socket socket = getSocket(Duration.ofSeconds(2));
            socket.getOutputStream().write(frame);
            socket.getOutputStream().flush();
        }

        private Socket getSocket(Duration timeout) throws Exception {
            if (acceptedSocket != null) {
                return acceptedSocket;
            }
            acceptedSocket = socketQueue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            assertNotNull(acceptedSocket);
            return acceptedSocket;
        }

        @Override
        public void close() throws Exception {
            if (acceptedSocket != null) {
                acceptedSocket.close();
            }
            for (Socket socket : socketQueue) {
                socket.close();
            }
            serverSocket.close();
        }
    }
}
