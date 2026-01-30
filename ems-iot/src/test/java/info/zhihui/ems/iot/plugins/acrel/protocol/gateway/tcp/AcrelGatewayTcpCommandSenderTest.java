package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp;

import info.zhihui.ems.iot.config.IotCommandProperties;
import info.zhihui.ems.iot.domain.command.concrete.GetCtCommand;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslator;
import info.zhihui.ems.iot.protocol.port.outbound.MultiStepDeviceCommandTranslator;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuBuilder;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.GatewayPacketCode;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayCryptoService;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayFrameCodec;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.support.AcrelGatewayTransparentCodec;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslatorResolver;
import info.zhihui.ems.iot.protocol.port.outbound.ProtocolCommandTransport;
import info.zhihui.ems.iot.protocol.port.outbound.StepResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

class AcrelGatewayTcpCommandSenderTest {

    @Test
    void send_whenDeviceNull_shouldThrow() {
        AcrelGatewayTcpCommandSender sender = buildSender();
        DeviceCommand command = new DeviceCommand().setType(DeviceCommandTypeEnum.GET_CT);

        Assertions.assertThrows(IllegalArgumentException.class, () -> sender.send(command));
    }

    @Test
    void send_whenAccessModeMissing_shouldThrow() {
        AcrelGatewayTcpCommandSender sender = buildSender();
        Device device = new Device().setDeviceNo("dev-1").setProduct(new Product().setVendor("ACREL"));
        DeviceCommand command = new DeviceCommand().setDevice(device).setType(DeviceCommandTypeEnum.GET_CT);

        Assertions.assertThrows(IllegalArgumentException.class, () -> sender.send(command));
    }

    @Test
    void send_whenAccessModeMismatch_shouldThrow() {
        AcrelGatewayTcpCommandSender sender = buildSender();
        Device device = new Device()
                .setDeviceNo("dev-1")
                .setProduct(new Product().setVendor("ACREL").setAccessMode(DeviceAccessModeEnum.DIRECT));
        DeviceCommand command = new DeviceCommand().setDevice(device).setType(DeviceCommandTypeEnum.GET_CT);

        Assertions.assertThrows(IllegalArgumentException.class, () -> sender.send(command));
    }

    @Test
    void send_whenGatewayMissing_shouldThrow() {
        AcrelGatewayTcpCommandSender sender = buildSender();
        Device device = new Device()
                .setDeviceNo("dev-1")
                .setProduct(new Product().setVendor("ACREL").setAccessMode(DeviceAccessModeEnum.GATEWAY));
        DeviceCommand command = new DeviceCommand().setDevice(device).setType(DeviceCommandTypeEnum.GET_CT);

        Assertions.assertThrows(IllegalArgumentException.class, () -> sender.send(command));
    }

    @Test
    void send_whenGatewaySecretMissing_shouldThrow() {
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DeviceCommandTranslatorResolver translatorRegistry = Mockito.mock(DeviceCommandTranslatorResolver.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotCommandProperties properties = new IotCommandProperties();
        properties.setTimeoutMillis(0);
        AcrelGatewayTcpCommandSender sender = new AcrelGatewayTcpCommandSender(
                commandTransport, translatorRegistry, properties, deviceRegistry,
                new AcrelGatewayFrameCodec(), new AcrelGatewayCryptoService(), new AcrelGatewayTransparentCodec());

        Device device = new Device()
                .setDeviceNo("dev-1")
                .setParentId(1)
                .setPortNo(1)
                .setMeterAddress(2)
                .setProduct(new Product().setVendor("ACREL").setAccessMode(DeviceAccessModeEnum.GATEWAY));
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_CT)
                .setPayload(new GetCtCommand());
        Device gateway = new Device().setDeviceNo("gw-1").setDeviceSecret("");
        Mockito.when(deviceRegistry.getById(1)).thenReturn(gateway);

        Assertions.assertThrows(IllegalArgumentException.class, () -> sender.send(command));
        Mockito.verifyNoInteractions(translatorRegistry, commandTransport);
    }

    @Test
    void send_whenGatewayMeterIdentityMissing_shouldThrow() {
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DeviceCommandTranslatorResolver translatorRegistry = Mockito.mock(DeviceCommandTranslatorResolver.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotCommandProperties properties = new IotCommandProperties();
        properties.setTimeoutMillis(0);
        AcrelGatewayTcpCommandSender sender = new AcrelGatewayTcpCommandSender(
                commandTransport, translatorRegistry, properties, deviceRegistry,
                new AcrelGatewayFrameCodec(), new AcrelGatewayCryptoService(), new AcrelGatewayTransparentCodec());

        Device device = new Device()
                .setDeviceNo("dev-1")
                .setParentId(1)
                .setProduct(new Product().setVendor("ACREL").setAccessMode(DeviceAccessModeEnum.GATEWAY));
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_CT)
                .setPayload(new GetCtCommand());
        Device gateway = new Device().setDeviceNo("gw-1").setDeviceSecret("1234567890abcdef");
        Mockito.when(deviceRegistry.getById(1)).thenReturn(gateway);

        Assertions.assertThrows(IllegalArgumentException.class, () -> sender.send(command));
        Mockito.verifyNoInteractions(translatorRegistry, commandTransport);
    }

    @Test
    void send_whenGateway_shouldSendAndReturnResult() {
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DeviceCommandTranslatorResolver translatorRegistry = Mockito.mock(DeviceCommandTranslatorResolver.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotCommandProperties properties = new IotCommandProperties();
        properties.setTimeoutMillis(0);
        AcrelGatewayCryptoService gatewayCryptoService = new AcrelGatewayCryptoService();
        AcrelGatewayTransparentCodec gatewayTransparentCodec = new AcrelGatewayTransparentCodec();
        AcrelGatewayFrameCodec gatewayFrameCodec = new AcrelGatewayFrameCodec();
        AcrelGatewayTcpCommandSender sender = new AcrelGatewayTcpCommandSender(
                commandTransport, translatorRegistry, properties, deviceRegistry,
                gatewayFrameCodec, gatewayCryptoService, gatewayTransparentCodec);

        Device device = new Device()
                .setDeviceNo("dev-1")
                .setParentId(1)
                .setPortNo(1)
                .setMeterAddress(2)
                .setSlaveAddress(1)
                .setProduct(new Product().setVendor("ACREL").setCode("P1").setAccessMode(DeviceAccessModeEnum.GATEWAY));
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_CT)
                .setPayload(new GetCtCommand());
        Device gateway = new Device().setDeviceNo("gw-1").setDeviceSecret("1234567890abcdef");
        Mockito.when(deviceRegistry.getById(1)).thenReturn(gateway);

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
        Mockito.when(commandTransport.sendWithAck(Mockito.eq("gw-1"), Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(response));
        Mockito.when(translator.parseResponse(command, response)).thenReturn(expected);

        DeviceCommandResult result = sender.send(command).join();

        Assertions.assertSame(expected, result);
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(commandTransport).sendWithAck(Mockito.eq("gw-1"), captor.capture());
        byte[] rtuFrame = ModbusRtuBuilder.build(request);
        byte[] transparent = gatewayTransparentCodec.encode(device.getPortNo(), device.getMeterAddress(), rtuFrame);
        byte[] encrypted = gatewayCryptoService.encrypt(
                transparent, "1234567890abcdef");
        byte[] expectedFrame = gatewayFrameCodec.encode(GatewayPacketCode.DOWNLINK, encrypted);
        Assertions.assertArrayEquals(expectedFrame, captor.getValue());
    }

    @Test
    void send_whenMultiStepTranslator_shouldSendMultipleSteps() {
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DeviceCommandTranslatorResolver translatorRegistry = Mockito.mock(DeviceCommandTranslatorResolver.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotCommandProperties properties = new IotCommandProperties();
        properties.setTimeoutMillis(0);
        AcrelGatewayCryptoService gatewayCryptoService = new AcrelGatewayCryptoService();
        AcrelGatewayTransparentCodec gatewayTransparentCodec = new AcrelGatewayTransparentCodec();
        AcrelGatewayFrameCodec gatewayFrameCodec = new AcrelGatewayFrameCodec();
        AcrelGatewayTcpCommandSender sender = new AcrelGatewayTcpCommandSender(
                commandTransport, translatorRegistry, properties, deviceRegistry,
                gatewayFrameCodec, gatewayCryptoService, gatewayTransparentCodec);

        Device device = new Device()
                .setDeviceNo("dev-1")
                .setParentId(1)
                .setPortNo(1)
                .setMeterAddress(2)
                .setSlaveAddress(1)
                .setProduct(new Product().setVendor("ACREL").setCode("P1").setAccessMode(DeviceAccessModeEnum.GATEWAY));
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_CT)
                .setPayload(new GetCtCommand());
        Device gateway = new Device().setDeviceNo("gw-1").setDeviceSecret("1234567890abcdef");
        Mockito.when(deviceRegistry.getById(1)).thenReturn(gateway);

        @SuppressWarnings("unchecked")
        MultiStepDeviceCommandTranslator<ModbusRtuRequest> translator =
                Mockito.mock(MultiStepDeviceCommandTranslator.class);
        ModbusRtuRequest firstRequest = new ModbusRtuRequest()
                .setSlaveAddress(1)
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(0x0001)
                .setQuantity(1);
        ModbusRtuRequest secondRequest = new ModbusRtuRequest()
                .setSlaveAddress(1)
                .setFunction(ModbusRtuBuilder.FUNCTION_READ)
                .setStartRegister(0x0002)
                .setQuantity(1);
        byte[] response1 = new byte[]{0x01, 0x03};
        byte[] response2 = new byte[]{0x01, 0x03, 0x02};
        DeviceCommandResult expected = new DeviceCommandResult()
                .setType(DeviceCommandTypeEnum.GET_CT)
                .setSuccess(true);

        Mockito.when(translatorRegistry.resolve("ACREL", "P1", DeviceCommandTypeEnum.GET_CT, ModbusRtuRequest.class))
                .thenReturn((DeviceCommandTranslator<ModbusRtuRequest>) translator);
        Mockito.when(translator.firstRequest(command)).thenReturn(firstRequest);
        Mockito.when(commandTransport.sendWithAck(Mockito.eq("gw-1"), Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(response1), CompletableFuture.completedFuture(response2));
        Mockito.when(translator.parseStep(Mockito.eq(command), Mockito.eq(response1), Mockito.any()))
                .thenReturn(StepResult.next(secondRequest));
        Mockito.when(translator.parseStep(Mockito.eq(command), Mockito.eq(response2), Mockito.any()))
                .thenReturn(StepResult.done(expected));

        DeviceCommandResult result = sender.send(command).join();

        Assertions.assertSame(expected, result);
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(commandTransport, Mockito.times(2)).sendWithAck(Mockito.eq("gw-1"), captor.capture());
        byte[] frame1 = buildExpectedFrame(gatewayFrameCodec, gatewayCryptoService, gatewayTransparentCodec,
                device, gateway, firstRequest);
        byte[] frame2 = buildExpectedFrame(gatewayFrameCodec, gatewayCryptoService, gatewayTransparentCodec,
                device, gateway, secondRequest);
        Assertions.assertEquals(2, captor.getAllValues().size());
        Assertions.assertArrayEquals(frame1, captor.getAllValues().get(0));
        Assertions.assertArrayEquals(frame2, captor.getAllValues().get(1));
    }

    @Test
    void send_whenGatewayTimeout_shouldFailPending() {
        ProtocolCommandTransport commandTransport = Mockito.mock(ProtocolCommandTransport.class);
        DeviceCommandTranslatorResolver translatorRegistry = Mockito.mock(DeviceCommandTranslatorResolver.class);
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotCommandProperties properties = new IotCommandProperties();
        properties.setTimeoutMillis(1);
        AcrelGatewayTcpCommandSender sender = new AcrelGatewayTcpCommandSender(
                commandTransport, translatorRegistry, properties, deviceRegistry,
                new AcrelGatewayFrameCodec(), new AcrelGatewayCryptoService(), new AcrelGatewayTransparentCodec());

        Device device = new Device()
                .setDeviceNo("dev-1")
                .setParentId(1)
                .setPortNo(1)
                .setMeterAddress(2)
                .setSlaveAddress(1)
                .setProduct(new Product().setVendor("ACREL").setCode("P1").setAccessMode(DeviceAccessModeEnum.GATEWAY));
        DeviceCommand command = new DeviceCommand()
                .setDevice(device)
                .setType(DeviceCommandTypeEnum.GET_CT)
                .setPayload(new GetCtCommand());
        Device gateway = new Device().setDeviceNo("gw-1").setDeviceSecret("1234567890abcdef");
        Mockito.when(deviceRegistry.getById(1)).thenReturn(gateway);

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
        Mockito.when(commandTransport.sendWithAck(Mockito.eq("gw-1"), Mockito.any()))
                .thenReturn(pending);

        CompletableFuture<DeviceCommandResult> result = sender.send(command);

        TimeoutException timeout = new TimeoutException("timeout");
        pending.completeExceptionally(timeout);

        Assertions.assertThrows(CompletionException.class, result::join);
        ArgumentCaptor<Throwable> exCaptor = ArgumentCaptor.forClass(Throwable.class);
        Mockito.verify(commandTransport).failPending(Mockito.eq("gw-1"), exCaptor.capture());
        Assertions.assertInstanceOf(TimeoutException.class, exCaptor.getValue());
    }

    private AcrelGatewayTcpCommandSender buildSender() {
        IotCommandProperties properties = new IotCommandProperties();
        properties.setTimeoutMillis(0);
        return new AcrelGatewayTcpCommandSender(
                Mockito.mock(ProtocolCommandTransport.class),
                Mockito.mock(DeviceCommandTranslatorResolver.class),
                properties,
                Mockito.mock(DeviceRegistry.class),
                new AcrelGatewayFrameCodec(),
                new AcrelGatewayCryptoService(),
                new AcrelGatewayTransparentCodec()
        );
    }

    /**
     * 构造期望的网关下行帧。
     */
    private byte[] buildExpectedFrame(AcrelGatewayFrameCodec gatewayFrameCodec,
                                      AcrelGatewayCryptoService gatewayCryptoService,
                                      AcrelGatewayTransparentCodec gatewayTransparentCodec,
                                      Device device,
                                      Device gateway,
                                      ModbusRtuRequest request) {
        byte[] rtuFrame = ModbusRtuBuilder.build(request);
        byte[] transparent = gatewayTransparentCodec.encode(device.getPortNo(), device.getMeterAddress(), rtuFrame);
        byte[] encrypted = gatewayCryptoService.encrypt(transparent, gateway.getDeviceSecret());
        return gatewayFrameCodec.encode(GatewayPacketCode.DOWNLINK, encrypted);
    }

}
