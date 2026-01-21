package info.zhihui.ems.iot.infrastructure.registry;

import info.zhihui.ems.iot.protocol.port.ProtocolSignature;
import info.zhihui.ems.iot.protocol.port.DeviceProtocolHandler;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

class ProtocolHandlerRegistryTest {

    @Test
    void resolve_whenAccessModeMatches_shouldReturnHandler() {
        DeviceProtocolHandler directHandler = Mockito.mock(DeviceProtocolHandler.class);
        Mockito.when(directHandler.getVendor()).thenReturn("ACREL");
        Mockito.when(directHandler.getAccessMode()).thenReturn(DeviceAccessModeEnum.DIRECT);
        Mockito.when(directHandler.getSupportedTransports()).thenReturn(Set.of(TransportProtocolEnum.TCP));
        DeviceProtocolHandler gatewayHandler = Mockito.mock(DeviceProtocolHandler.class);
        Mockito.when(gatewayHandler.getVendor()).thenReturn("ACREL");
        Mockito.when(gatewayHandler.getAccessMode()).thenReturn(DeviceAccessModeEnum.GATEWAY);
        Mockito.when(gatewayHandler.getSupportedTransports()).thenReturn(Set.of(TransportProtocolEnum.TCP));
        DeviceProtocolHandlerRegistry registry = new DeviceProtocolHandlerRegistry(List.of(directHandler, gatewayHandler));
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor("acrel")
                .setAccessMode(DeviceAccessModeEnum.GATEWAY)
                .setTransportType(TransportProtocolEnum.TCP);

        DeviceProtocolHandler resolved = registry.resolve(signature);

        Assertions.assertSame(gatewayHandler, resolved);
    }

    @Test
    void resolve_whenProductSpecificExists_shouldPreferSpecific() {
        DeviceProtocolHandler defaultHandler = Mockito.mock(DeviceProtocolHandler.class);
        Mockito.when(defaultHandler.getVendor()).thenReturn("ACREL");
        Mockito.when(defaultHandler.getAccessMode()).thenReturn(DeviceAccessModeEnum.DIRECT);
        Mockito.when(defaultHandler.getSupportedTransports()).thenReturn(Set.of(TransportProtocolEnum.TCP));
        DeviceProtocolHandler productHandler = Mockito.mock(DeviceProtocolHandler.class);
        Mockito.when(productHandler.getVendor()).thenReturn("ACREL");
        Mockito.when(productHandler.getAccessMode()).thenReturn(DeviceAccessModeEnum.DIRECT);
        Mockito.when(productHandler.getProductCode()).thenReturn("P1");
        Mockito.when(productHandler.getSupportedTransports()).thenReturn(Set.of(TransportProtocolEnum.TCP));
        DeviceProtocolHandlerRegistry registry = new DeviceProtocolHandlerRegistry(List.of(defaultHandler, productHandler));
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor("ACREL")
                .setProductCode("P1")
                .setAccessMode(DeviceAccessModeEnum.DIRECT)
                .setTransportType(TransportProtocolEnum.TCP);

        DeviceProtocolHandler resolved = registry.resolve(signature);

        Assertions.assertSame(productHandler, resolved);
    }

    @Test
    void resolve_whenUnsupportedVendor_shouldThrow() {
        DeviceProtocolHandler handler = Mockito.mock(DeviceProtocolHandler.class);
        Mockito.when(handler.getVendor()).thenReturn("ACREL");
        Mockito.when(handler.getSupportedTransports()).thenReturn(Set.of(TransportProtocolEnum.TCP));
        DeviceProtocolHandlerRegistry registry = new DeviceProtocolHandlerRegistry(List.of(handler));
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor("UNKNOWN")
                .setTransportType(TransportProtocolEnum.TCP);

        IllegalArgumentException ex = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> registry.resolve(signature)
        );
        Assertions.assertTrue(ex.getMessage().contains("UNKNOWN"));
    }

    @Test
    void resolve_whenTransportMatches_shouldReturnHandler() {
        DeviceProtocolHandler tcpHandler = Mockito.mock(DeviceProtocolHandler.class);
        Mockito.when(tcpHandler.getVendor()).thenReturn("ACREL");
        Mockito.when(tcpHandler.getSupportedTransports()).thenReturn(Set.of(TransportProtocolEnum.TCP));

        DeviceProtocolHandler mqttHandler = Mockito.mock(DeviceProtocolHandler.class);
        Mockito.when(mqttHandler.getVendor()).thenReturn("ACREL");
        Mockito.when(mqttHandler.getSupportedTransports()).thenReturn(Set.of(TransportProtocolEnum.MQTT));

        DeviceProtocolHandlerRegistry registry = new DeviceProtocolHandlerRegistry(List.of(tcpHandler, mqttHandler));
        ProtocolSignature signature = new ProtocolSignature()
                .setVendor("ACREL")
                .setTransportType(TransportProtocolEnum.MQTT);

        DeviceProtocolHandler resolved = registry.resolve(signature);

        Assertions.assertSame(mqttHandler, resolved);
    }
}
