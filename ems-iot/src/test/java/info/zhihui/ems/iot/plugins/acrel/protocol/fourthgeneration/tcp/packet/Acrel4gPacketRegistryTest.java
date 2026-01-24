package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.plugins.acrel.protocol.message.AcrelMessage;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition.Acrel4gPacketDefinition;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class Acrel4gPacketRegistryTest {

    @Test
    void testResolve_ShouldReturnDefinition() {
        Acrel4gPacketDefinition definition = new StubDefinition(Acrel4gPacketCode.commandKey((byte) 0x10));
        Acrel4gPacketRegistry registry = new Acrel4gPacketRegistry(List.of(definition));

        Assertions.assertSame(definition, registry.resolve(Acrel4gPacketCode.commandKey((byte) 0x10)));
        Assertions.assertNull(registry.resolve(Acrel4gPacketCode.commandKey((byte) 0x11)));
    }

    @Test
    void testConstructor_DuplicateCommand_ShouldThrow() {
        Acrel4gPacketDefinition first = new StubDefinition(Acrel4gPacketCode.commandKey((byte) 0x10));
        Acrel4gPacketDefinition second = new StubDefinition(Acrel4gPacketCode.commandKey((byte) 0x10));

        Assertions.assertThrows(BusinessRuntimeException.class,
                () -> new Acrel4gPacketRegistry(List.of(first, second)));
    }

    private static class StubDefinition implements Acrel4gPacketDefinition {

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
