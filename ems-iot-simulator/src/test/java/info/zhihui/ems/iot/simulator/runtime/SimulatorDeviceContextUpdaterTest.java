package info.zhihui.ems.iot.simulator.runtime;

import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulatorDeviceContextUpdaterTest {

    @Test
    void shouldBeStaticUtilityClass() {
        Constructor<?>[] constructors = SimulatorDeviceContextUpdater.class.getDeclaredConstructors();

        assertNull(SimulatorDeviceContextUpdater.class.getAnnotation(Component.class));
        assertTrue(Modifier.isFinal(SimulatorDeviceContextUpdater.class.getModifiers()));
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));
    }

    @Test
    void markConnected_whenCalled_shouldUpdateConnectionFields() {
        SimulatorDeviceContext deviceContext = buildDeviceContext();
        LocalDateTime connectTime = LocalDateTime.of(2026, 3, 27, 10, 0, 0);

        SimulatorDeviceContextUpdater.markConnected(deviceContext, "connection-1", connectTime);

        assertEquals("connection-1", deviceContext.getConnectionId());
        assertEquals(connectTime, deviceContext.getConnectedAt());
        assertEquals(connectTime, deviceContext.getLastCommunicationAt());
    }

    @Test
    void touch_whenCalled_shouldUpdateLastCommunicationTime() {
        SimulatorDeviceContext deviceContext = buildDeviceContext();
        LocalDateTime communicationTime = LocalDateTime.of(2026, 3, 27, 11, 0, 0);

        SimulatorDeviceContextUpdater.touch(deviceContext, communicationTime);

        assertEquals(communicationTime, deviceContext.getLastCommunicationAt());
    }

    @Test
    void markDisconnected_whenCalled_shouldClearConnectionFields() {
        SimulatorDeviceContext deviceContext = buildDeviceContext()
                .setConnectionId("connection-1")
                .setConnectedAt(LocalDateTime.of(2026, 3, 27, 10, 0, 0))
                .setLastCommunicationAt(LocalDateTime.of(2026, 3, 27, 10, 30, 0));

        SimulatorDeviceContextUpdater.markDisconnected(deviceContext);

        assertNull(deviceContext.getConnectionId());
        assertNull(deviceContext.getConnectedAt());
        assertEquals(LocalDateTime.of(2026, 3, 27, 10, 30, 0), deviceContext.getLastCommunicationAt());
    }

    private SimulatorDeviceContext buildDeviceContext() {
        SimulatorDeviceProperties deviceProperties = new SimulatorDeviceProperties();
        deviceProperties.setDeviceNo("SIM001");
        return new SimulatorDeviceContext().setDeviceProperties(deviceProperties);
    }
}
