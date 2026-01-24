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
- 实现 `DeviceProtocolHandler`（继承 `AbstractAcrelInboundHandler` 或类似基类）。
- 增加 `DeviceCommandTranslator`（命令 -> 传输请求）。
- 处理回执/响应并填充 `DeviceCommandResult`。

## 2. 目录结构示例

以新增厂商 `vendorx` 为例，建议目录结构如下（可按产品/接入方式拆分）：

```
plugins/
  vendorx/
    VendorxProtocolHandler.java
    protocol/
      devicea/
        detect/
          VendorxDeviceaProtocolDetector.java
        tcp/
          VendorxDeviceaTcpInboundHandler.java
          VendorxDeviceaTcpCommandSender.java
          message/
          packet/
            definition/
            parser/
            handler/
          support/
            VendorxDeviceaFrameCodec.java
            VendorxDeviceaDeviceResolver.java
      gateway/
        detect/
          VendorxGatewayProtocolDetector.java
        tcp/
          VendorxGatewayTcpInboundHandler.java
          VendorxGatewayTcpCommandSender.java
          message/
          packet/
            definition/
            parser/
            handler/
          support/
            VendorxGatewayFrameCodec.java
            VendorxGatewayDeviceResolver.java
    command/
      translator/
        AbstractVendorxCommandTranslator.java
        VendorxGetStatusTranslator.java
        VendorxSetCtTranslator.java
        ...
    transport/
      netty/
        decoder/
          VendorxFrameDecoderProvider.java
        detect/
          VendorxProtocolDetector.java
```

说明：
- `protocol/*/tcp`：协议的 TCP 处理入口，按接入方式拆分（直连/网关）。
- `packet/*`：命令层的定义/解析/处理，按命令字拆分。
- `support/*FrameCodec`：负责编解码，`decode` 统一输出 `FrameDecodeResult`。
- `command/translator`：命令转换器，实现 `DeviceCommandTranslator` 接口。
- `transport/netty`：Netty 传输层扩展，包括协议探测和解码器提供者。

## 3. 关键文件职责

- `NettyProtocolDetector`：只做识别，返回 `ProtocolSignature`。
- `NettyFrameDecoderProvider`：根据签名返回解码器链（长度型/起止符型等）。
- `*FrameCodec`：帧编解码，`decode` 返回 `FrameDecodeResult`。
- `*TcpInboundHandler`：协议入口，处理上行消息。
- `*TcpCommandSender`：命令发送器，处理下行消息。
- `PacketDefinition/Parser/Handler`：命令定义、解析与处理（入参统一为 `ProtocolMessageContext`，接口位于 `protocol/packet`）。
- `DeviceCommandTranslator`：命令下发转换与响应解析。
- `DeviceProtocolHandler`：设备协议处理器，实现 `DeviceProtocolHandler` 接口，负责上行解析与命令下发。

## 4. 建议的落地顺序

1) 确定协议的首包特征、分包策略与帧格式。
2) 实现 `NettyProtocolDetector` + `NettyFrameDecoderProvider` + `FrameCodec`。
3) 实现 `DeviceProtocolHandler`，继承相应的基类处理上行消息。
4) 完成命令层 `definition/parser/handler` 并注册到 registry。
5) 完成下发 `DeviceCommandTranslator` 与回执处理。
6) 补充单元测试（解帧、解析、handler、translator）。

## 5. 示例：新增 VendorX 直连协议（TCP）

以下示例仅展示骨架，具体字段请按协议调整。

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

### 5.3 设备协议处理器

```java
@Component
public class VendorxProtocolHandler extends AbstractAcrelInboundHandler implements DeviceProtocolHandler {
    
    @Override
    public String getVendor() {
        return "VENDORX";
    }

    @Override
    public DeviceAccessModeEnum getAccessMode() {
        return DeviceAccessModeEnum.DIRECT;
    }

    @Override
    public void onMessage(ProtocolMessageContext context) {
        // 处理上行消息
        // 1. 解析帧
        // 2. 路由到具体处理器
        // 3. 更新设备状态
        // 4. 发布事件
    }

    @Override
    public CompletableFuture<DeviceCommandResult> sendCommand(DeviceCommand command) {
        // 处理下行命令
        // 1. 通过 DeviceCommandTranslator 转换命令
        // 2. 通过 CommandSender 发送
        // 3. 返回异步结果
    }
}
```

### 5.4 命令转换器

```java
@Component
public class VendorxGetCtTranslator implements DeviceCommandTranslator {
    @Override
    public String vendor() {
        return "VENDORX";
    }

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_CT;
    }

    @Override
    public Object toRequest(DeviceCommand command) {
        // 将领域命令转换为协议特定请求
        return request;
    }

    @Override
    public Class<?> requestType() {
        return VendorxRequest.class;
    }

    @Override
    public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        // 解析响应并转换为命令结果
        return DeviceCommandResult.success(data);
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
DeviceProtocolHandlerRegistry.resolve(signature)
   |
   v
DeviceProtocolHandler.sendCommand (如 VendorxProtocolHandler)
   |
   v
DeviceCommandTranslatorResolver.resolve(vendor, type, requestType)
   |
   v
DeviceCommandTranslator.toRequest -> 协议特定请求
   |
   v
VendorxTcpCommandSender.sendWithAck -> Netty Channel.writeAndFlush
   |
   v
[设备返回响应帧]
   |
   v
ProtocolFrameDecoder -> MultiplexTcpHandler -> VendorxTcpInboundHandler
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
VendorxTcpInboundHandler.handle
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

## 7. 新增厂商/协议接入清单

1) 新增 `NettyProtocolDetector`：根据协议魔数/特征返回 `ProtocolSignature`（包含 `transportType`）。  
2) 新增 `NettyFrameDecoderProvider`：签名命中后返回专用解码器链（分包/粘包在此解决）。  
3) 新增插件 `DeviceProtocolHandler`：解析完整帧，负责设备绑定、事件发布、命令回执归还（`completePending`）。  
4) 若有下发命令：提供协议编码器/帧封装工具，调用 `sendWithAck/sendFireAndForget`。  
5) 补齐单元测试：CRC、分包、ACK 等关键路径。  

### 7.1 同厂商新增特殊产品（productCode）接入要点

1) 设备录入时必须保存 `productCode`，并保证绑定后可通过 `deviceNo` 查询到该值。  
2) 命令差异：新增对应的 `DeviceCommandTranslator`，`productCode()` 返回特殊型号编码；注册后会优先命中该产品专用翻译器。  
3) Modbus 映射差异：在该产品专用翻译器中使用对应的地址/长度/倍率（不要复用默认映射）。  
4) 上报差异：若上报解析依赖 `productCode`，在 handler 解析出 `deviceNo` 后查询设备，再按产品分发到专用 parser；无法在 `NettyProtocolDetector` 阶段完成判定。  
5) 兼容回退：若未配置专用翻译器/解析器，继续回退到默认产品实现（`productCode` 为空的 translator/parser）。  
6) 测试补齐：为该 `productCode` 的 translator/parser 增加单测，覆盖"产品优先 + 默认回退"路径。
