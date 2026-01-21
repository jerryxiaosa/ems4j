# ems-iot 新协议接入开发指南

本文档描述在现有架构下接入一个新协议（新厂商/新产品）的编码方式、目录结构与关键文件。

## 1. 最小接入三步

只要完成下面 1/2/3，即可完成新协议接入：

1) 协议识别与解帧
- 实现协议探测（首包识别）与解码器安装。
- 实现 FrameCodec：`decode` 返回 `FrameDecodeResult(commandKey, payload, reason)`。

2) 命令解析与处理
- 按命令字/类型定义 `PacketDefinition + Parser + Handler + Registry`。
- Handler 中完成设备绑定、事件发布、回包下发等业务。

3) 下发与回执
- 增加 `DeviceCommandTranslator`（命令 -> 传输请求）。
- 处理回执/响应并填充 `DeviceCommandResult`。

## 2. 目录结构示例

以新增厂商 `vendorx` 为例，建议目录结构如下（可按产品/接入方式拆分）：

```
plugins/
  vendorx/
    VendorxProtocolHandler.java
    constants/
    message/
    model/
    protocol/
      devicea/
        detect/
          VendorxDeviceaProtocolDetector.java
        tcp/
          VendorxDeviceaTcpHandler.java
          message/
          packet/
            definition/
            parser/
            handler/
          support/
            VendorxDeviceaFrameCodec.java
      gateway/
        detect/
          VendorxGatewayProtocolDetector.java
        tcp/
          VendorxGatewayTcpInboundHandler.java
          message/
          packet/
            definition/
            parser/
            handler/
          support/
            VendorxGatewayFrameCodec.java
    command/
      translator/
        VendorxGetStatusTranslator.java
    transport/
      tcp/
        frame/
          VendorxFrameDecoderProvider.java
```

说明：
- `protocol/*/tcp`：协议的 TCP 处理入口，按接入方式拆分（直连/网关）。
- `packet/*`：命令层的定义/解析/处理，按命令字拆分。
- `support/*FrameCodec`：负责编解码，`decode` 统一输出 `FrameDecodeResult`。

## 3. 关键文件职责

- `NettyProtocolDetector`：只做识别，返回 `ProtocolSignature`。
- `NettyFrameDecoderProvider`：根据签名返回解码器链（长度型/起止符型等）。
- `*FrameCodec`：帧编解码，`decode` 返回 `FrameDecodeResult`。
- `*TcpHandler`：协议入口，路由到 `PacketDefinition`。
- `PacketDefinition/Parser/Handler`：命令定义、解析与处理（入参统一为 `ProtocolMessageContext`，接口位于 `protocol/packet`）。
- `DeviceCommandTranslator`：命令下发转换与响应解析。

## 4. 建议的落地顺序

1) 确定协议的首包特征、分包策略与帧格式。
2) 实现 `NettyProtocolDetector` + `NettyFrameDecoderProvider` + `FrameCodec`。
3) 完成命令层 `definition/parser/handler` 并注册到 registry。
4) 完成下发 translator 与回执处理。
5) 补充单元测试（解帧、解析、handler、translator）。

## 5. 示例：新增 VendorX 直连协议（TCP）

以下示例仅展示骨架，具体字段请按协议调整。`FrameDecodeResult` 统一使用
`info.zhihui.ems.iot.protocol.decode.FrameDecodeResult`。

### 5.1 协议探测

```java
@Component
public class VendorxProtocolDetector implements NettyProtocolDetector {
    @Override
    public ProtocolSignature detectTcp(byte[] payload) {
        if (payload == null || payload.length < 2) {
            return null;
        }
        if (payload[0] == (byte) 0x7B && payload[1] == (byte) 0x7B) {
            return new ProtocolSignature()
                    .setVendor("VENDORX")
                    .setAccessMode(DeviceAccessModeEnum.DIRECT)
                    .setTransportType(TransportProtocolEnum.TCP);
        }
        return null;
    }
}
```

### 5.2 解码器链

```java
@Component
public class VendorxFrameDecoderProvider implements NettyFrameDecoderProvider {
    @Override
    public boolean supports(ProtocolSignature signature) {
        return "VENDORX".equalsIgnoreCase(signature.getVendor());
    }

    @Override
    public List<ChannelHandler> createDecoders(ProtocolSignature signature) {
        return List.of(new LengthFieldBasedFrameDecoder(65535, 2, 2));
    }
}
```

### 5.3 编解码器

```java
@Component
public class VendorxFrameCodec {
    public FrameDecodeResult decode(byte[] frame) {
        // 校验帧头/长度/CRC 等
        String commandKey = String.format("%02x", Byte.toUnsignedInt(command));
        return new FrameDecodeResult(commandKey, payload, null);
    }

    public byte[] encode(byte command, byte[] payload) {
        // 拼装帧头/长度/CRC 等
        return frame;
    }
}
```

### 5.4 命令定义/解析/处理

```java
public interface VendorxPacketDefinition {
    String command();
    VendorxMessage parse(ProtocolMessageContext context, byte[] payload);
    void handle(ProtocolMessageContext context, VendorxMessage message);
}

@Component
public class VendorxHeartbeatPacketDefinition implements VendorxPacketDefinition {
    @Override
    public String command() {
        return "11";
    }

    @Override
    public VendorxMessage parse(ProtocolMessageContext context, byte[] payload) {
        return new VendorxHeartbeatMessage();
    }

    @Override
    public void handle(ProtocolMessageContext context, VendorxMessage message) {
        // 绑定设备/更新在线/下发响应
    }
}
```

### 5.5 协议入口与路由

```java
@Component
@RequiredArgsConstructor
public class VendorxTcpHandler {
    private final VendorxFrameCodec frameCodec;
    private final VendorxPacketRegistry registry;

    public void handle(ProtocolMessageContext context) {
        FrameDecodeResult frame = frameCodec.decode(context.getRawPayload());
        if (frame.reason() != null) {
            // 上报异常
            return;
        }
        VendorxPacketDefinition definition = registry.resolve(frame.commandKey());
        VendorxMessage message = definition.parse(context, frame.payload());
        definition.handle(context, message);
    }
}
```

### 5.6 下发与回执

```java
@Component
public class VendorxGetCtTranslator implements DeviceCommandTranslator<ModbusRtuRequest> {
    @Override
    public String vendor() {
        return "VENDORX";
    }

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_CT;
    }

    @Override
    public ModbusRtuRequest toRequest(DeviceCommand command) {
        // 命令转请求
        return request;
    }

    @Override
    public Class<ModbusRtuRequest> requestType() {
        return ModbusRtuRequest.class;
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        // 解析回执
        return result;
    }
}
```

## 6. 代码流向（下发命令 / 上报解析）

### 6.1 下发命令（DeviceCommand → 设备）

```
[API/业务]
   |
   v
DeviceVendorFacade
   |
   v
CommandAppService.sendCommand
   |  (DeviceRegistry.findById / Product)
   v
ProtocolHandlerRegistry.resolve(signature)
   |
   v
DeviceProtocolHandler.sendCommand (如 Acrel4g/AcrelGateway)
   |
   v
DeviceCommandTranslatorResolver.resolve(vendor, product, type, ModbusRtuRequest.class)
   |
   v
DeviceCommandTranslator.toRequest -> ModbusRtuBuilder
   |
   v
FrameCodec.encode (Acrel4gFrameCodec/AcrelGatewayFrameCodec)
   |
   v
ProtocolCommandTransport.sendWithAck -> Netty Channel.writeAndFlush
   |
   v
[设备返回响应帧]
   |
   v
ProtocolFrameDecoder -> MultiplexTcpHandler -> *TcpHandler
   |
   v
PacketRegistry -> PacketDefinition.parse(context, payload)
   |
   v
DownlinkAckPacketHandler.handle -> ProtocolCommandTransport.completePending
   |
   v
CompletableFuture 完成 -> translator.parseResponse -> DeviceCommandResult
```

### 6.2 上报解析（设备 → 业务事件）

```
[设备上报]
   |
   v
Netty Pipeline
  ProtocolFrameDecoder(识别+装载解码器)
   |
   v
MultiplexTcpHandler
   |
   v
DeviceProtocolHandler.onMessage
   |
   v
*TcpInboundHandler (Acrel4gTcpInboundHandler/AcrelGatewayTcpInboundHandler)
   |
   v
FrameCodec.decode -> FrameDecodeResult
   |
   v
PacketRegistry.resolve(commandKey)
   |
   v
PacketDefinition.parse(context, payload) -> ProtocolMessage
   |
   v
PacketDefinition.handle -> PacketHandler
   |
   +--> 设备绑定/在线状态更新
   +--> 发布 DeviceEnergyReportEvent / 其他业务事件
   +--> 异常上报（AbnormalEvent）
```
