package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.plugins.acrel.protocol.common.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.definition.GatewayPacketDefinition;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class GatewayPacketRegistryTest {

    @Test
    void testResolve_ShouldReturnDefinition() {
        GatewayPacketDefinition definition = new StubDefinition(GatewayPacketCode.commandKey((byte) 0x10));
        GatewayPacketRegistry registry = new GatewayPacketRegistry(List.of(definition));

        Assertions.assertSame(definition, registry.resolve(GatewayPacketCode.commandKey((byte) 0x10)));
        Assertions.assertNull(registry.resolve(GatewayPacketCode.commandKey((byte) 0x11)));
    }

    @Test
    void testConstructor_DuplicateCommand_ShouldThrow() {
        GatewayPacketDefinition first = new StubDefinition(GatewayPacketCode.commandKey((byte) 0x10));
        GatewayPacketDefinition second = new StubDefinition(GatewayPacketCode.commandKey((byte) 0x10));

        Assertions.assertThrows(BusinessRuntimeException.class,
                () -> new GatewayPacketRegistry(List.of(first, second)));
    }

    private static class StubDefinition implements GatewayPacketDefinition {

        private final String command;

        private StubDefinition(String command) {
            this.command = command;
        }

        @Override
        public String command() {
            return command;
        }

        @Override
        public AcrelMessage parse(ProtocolMessageContext context, byte[] payload) {
            return null;
        }

        @Override
        public void handle(ProtocolMessageContext context, AcrelMessage message) {
        }
    }
}
