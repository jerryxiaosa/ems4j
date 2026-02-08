package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp;

import info.zhihui.ems.iot.domain.command.concrete.GetCtCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslator;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.Acrel4gPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslatorResolver;
import info.zhihui.ems.iot.protocol.port.outbound.ProtocolCommandTransport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

class Acrel4gTcpCommandSenderTest {

    @Test
    void sendCommand_whenDeviceNull_shouldThrow() {
        Acrel4gTcpCommandSender sender = buildSender();
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.GET_CT);

        Assertions.assertThrows(IllegalArgumentException.class, () -> sender.send(command));
    }

    @Test
    void sendCommand_whenAccessModeMissing_shouldThrow() {
        Acrel4gTcpCommandSender sender = buildSender();
        Device device = new Device().setDeviceNo("dev-1").setProduct(new Product().setVendor("ACREL"));
        DeviceCommand command = new DeviceCommand().setDevice(device).setType(DeviceCommandTypeEnum.GET_CT);

        Assertions.assertThrows(IllegalArgumentException.class, () -> sender.send(command));
    }

    @Test
    void sendCommand_whenAccessModeMismatch_shouldThrow() {
        Acrel4gTcpCommandSender sender = buildSender();
        Device device = new Device()
                .setDeviceNo("dev-1")
                .setProduct(new Product().setVendor("ACREL").setAccessMode(DeviceAccessModeEnum.GATEWAY));
        DeviceCommand command = new DeviceCommand().setDevice(device).setType(DeviceCommandTypeEnum.GET_CT);

        Assertions.assertThrows(IllegalArgumentException.class, () -> sender.send(command));
    }

    @Test
    void sendCommand_whenDirect_shouldSendAndReturnResult() {
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DeviceCommandTranslatorResolver translatorRegistry = Mockito.mock(DeviceCommandTranslatorResolver.class);
        Acrel4gTcpCommandSender sender = new Acrel4gTcpCommandSender(
                commandTransport, translatorRegistry, new Acrel4gFrameCodec());

        Device device = new Device()
                .setDeviceNo("dev-1")
                .setSlaveAddress(1)
                .setProduct(new Product()
                        .setVendor("ACREL")
                        .setCode("P1")
                        .setAccessMode(DeviceAccessModeEnum.DIRECT));
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_CT)
                .setPayload(new GetCtCommand());

        DeviceCommandTranslator<ModbusRtuRequest> translator = Mockito.mock(DeviceCommandTranslator.class);
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(1)
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(0x0001)
                .setQuantity(1);
        byte[] response = new byte[]{0x01, 0x03};
        DeviceCommandResult expected = new DeviceCommandResult()
                .setType(DeviceCommandTypeEnum.GET_CT)
                .setSuccess(true);

        Mockito.when(translatorRegistry.resolve("ACREL", "P1", DeviceCommandTypeEnum.GET_CT, ModbusRtuRequest.class))
                .thenReturn(translator);
        Mockito.when(translator.toRequest(command)).thenReturn(request);
        Mockito.when(commandTransport.sendWithAck(Mockito.eq("dev-1"), Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(response));
        Mockito.when(translator.parseResponse(command, response)).thenReturn(expected);

        DeviceCommandResult result = sender.send(command).join();

        Assertions.assertSame(expected, result);
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(commandTransport).sendWithAck(Mockito.eq("dev-1"), captor.capture());
        byte[] expectedFrame = new Acrel4gFrameCodec().encode(
                Acrel4gPacketCode.DOWNLINK, ModbusRtuBuilder.build(request));
        Assertions.assertArrayEquals(expectedFrame, captor.getValue());
    }

    @Test
    void sendCommand_whenDirectTimeout_shouldPropagateException() {
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DeviceCommandTranslatorResolver translatorRegistry = Mockito.mock(DeviceCommandTranslatorResolver.class);
        Acrel4gTcpCommandSender sender = new Acrel4gTcpCommandSender(
                commandTransport, translatorRegistry, new Acrel4gFrameCodec());

        Device device = new Device()
                .setDeviceNo("dev-1")
                .setSlaveAddress(1)
                .setProduct(new Product()
                        .setVendor("ACREL")
                        .setCode("P1")
                        .setAccessMode(DeviceAccessModeEnum.DIRECT));
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_CT)
                .setPayload(new GetCtCommand());

        DeviceCommandTranslator<ModbusRtuRequest> translator = Mockito.mock(DeviceCommandTranslator.class);
        ModbusRtuRequest request = new ModbusRtuRequest()
                .setSlaveAddress(1)
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(0x0001)
                .setQuantity(1);
        Mockito.when(translatorRegistry.resolve("ACREL", "P1", DeviceCommandTypeEnum.GET_CT, ModbusRtuRequest.class))
                .thenReturn(translator);
        Mockito.when(translator.toRequest(command)).thenReturn(request);

        CompletableFuture<byte[]> pending = new CompletableFuture<>();
        Mockito.when(commandTransport.sendWithAck(Mockito.eq("dev-1"), Mockito.any()))
                .thenReturn(pending);

        CompletableFuture<DeviceCommandResult> result = sender.send(command);

        TimeoutException timeout = new TimeoutException("timeout");
        pending.completeExceptionally(timeout);

        CompletionException exception = Assertions.assertThrows(CompletionException.class, result::join);
        Assertions.assertInstanceOf(TimeoutException.class, exception.getCause());
    }

    private Acrel4gTcpCommandSender buildSender() {
        return new Acrel4gTcpCommandSender(
                Mockito.mock(ProtocolCommandTransport.class),
                Mockito.mock(DeviceCommandTranslatorResolver.class),
                new Acrel4gFrameCodec()
        );
    }
}
