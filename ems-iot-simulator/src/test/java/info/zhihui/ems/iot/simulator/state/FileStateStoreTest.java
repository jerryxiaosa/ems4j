package info.zhihui.ems.iot.simulator.state;

import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.VendorEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStateStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveAndLoad_StateSnapshot_ExpectedRoundTrip() {
        Path stateFile = tempDir.resolve("state").resolve("iot-simulator-state.json");
        FileStateStore store = new FileStateStore(stateFile.toString(), JacksonUtil.getObjectMapper());

        DeviceRuntimeState deviceState = new DeviceRuntimeState()
                .setDeviceNo("SIM001")
                .setVendor(VendorEnum.ACREL)
                .setProductCode("ACREL_4G_DIRECT")
                .setAccessMode(DeviceAccessModeEnum.DIRECT)
                .setSwitchStatus("ON")
                .setLastReportedAt(LocalDateTime.of(2026, 2, 1, 17, 0, 8))
                .setLastTotalEnergy(new BigDecimal("123.45"))
                .setLastHigherEnergy(new BigDecimal("12.34"))
                .setLastHighEnergy(new BigDecimal("23.45"))
                .setLastLowEnergy(new BigDecimal("34.56"))
                .setLastLowerEnergy(new BigDecimal("45.67"))
                .setLastDeepLowEnergy(new BigDecimal("7.43"))
                .setReplayCursorTime(LocalDateTime.of(2026, 2, 1, 17, 0, 8))
                .setReplayCompleted(false);

        SimulatorStateSnapshot snapshot = new SimulatorStateSnapshot()
                .setDeviceStateMap(Map.of("SIM001", deviceState));

        store.save(snapshot);
        SimulatorStateSnapshot result = store.load();

        assertTrue(stateFile.toFile().exists());
        assertEquals(new BigDecimal("123.45"), result.getDeviceStateMap().get("SIM001").getLastTotalEnergy());
        assertEquals(LocalDateTime.of(2026, 2, 1, 17, 0, 8),
                result.getDeviceStateMap().get("SIM001").getReplayCursorTime());
        assertEquals(VendorEnum.ACREL, result.getDeviceStateMap().get("SIM001").getVendor());
        assertEquals(DeviceAccessModeEnum.DIRECT, result.getDeviceStateMap().get("SIM001").getAccessMode());
        assertEquals("ON", result.getDeviceStateMap().get("SIM001").getSwitchStatus());
    }
}
