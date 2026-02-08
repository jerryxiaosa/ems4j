# ems-iot 新协议接入开发指南

本文档描述在现有架构下接入一个新协议（新厂商/新产品）的编码方式、目录结构与关键文件。

## 预备
一般情况下，我们通过设备网关转发或者直接与4G（NB）设备通信。网关透传Modbus RTU或者DLT645协议的数据给电表。

网关可以通过配置，接入不同品牌的设备。所以电表命令和网关行为可以相互独立。

对应到系统里，电表命令对应DeviceCommandTranslator；网关行为与4G表行为对应DeviceProtocolHandler

## 1. 设备最小接入

只要实现协议探测和设备的上下行命令处理即可完成接入：

### 协议识别与解帧（基于tcp协议）：让系统能识别协议，路由到对应的插件目录
- 实现`NettyFrameDecoderProvider`：协议探测（首包识别）与解码器安装。

### 实现DeviceProtocolHandler，处理上行和下发数据

   1. 命令解析与处理：解析设备主动上报的指令
   - 每一个上行的命令，按命令字/类型定义 `PacketDefinition + Parser + Handler`。
   - 通过`PacketDefinition + Parser + Handler`，实现命令解析与处理。
   - 因为响应设备上报是从底层往应用层传输，理论上可以各自实现onMessage方法，不一定需要统一处理。

   2. 下发与回执：实现系统定义的需要下发的命令，从而使上层可以统一管理、操作设备
   - 单步命令：按命令字/类型定义 `DeviceCommandTranslator + DeviceCommandResult`。
   - 多步命令：实现 `MultiStepDeviceCommandTranslator`，通过 `firstRequest/parseStep` 组织“请求-响应-再请求”的链路。
   - 通过调用 `ProtocolCommandTransport` 实现下发命令的回复回执。
   - 下发命令从应用层往底层传输，需要通过 `DeviceCommandTranslatorResolver` 找到对应的命令转换器，再转换成各自设备的命令进行发送。

## 2. 更复杂的情况
如果不单单对接一个设备，而且要对接某个厂商的多个设备，情况会复杂一些。 总的来说还是处理设备上下行的协议。

典型的场景会是
* 首先是会有多个不同的通信协议，同一种通信协议下命令大多是相同的
* 其次是会有多个不同的产品型号，可能会有少量的差异
* 第三是同一产品同一个命令，也可能会有新旧多个实现方式

那么就需要对这个插件下的实现进行更加细致的分层。
- 上行命令因为是响应客户端传来的数据，所以只要能被路由到就可以，不需要分的很细
- 下行命令要符合统一的命令规范，才能被DeviceCommandTranslatorResolver找到

**参考 plugins/acrel/的实现**

## 3. 目录结构示例

以 `acrel` 为例，当前目录结构如下（按接入方式拆分为 4G 直连与网关）：

```
plugins/
  acrel/
    Acrel4gProtocolHandler.java
    AcrelGatewayProtocolHandler.java
    protocol/
      constant/
        AcrelProtocolConstants.java
      common/
        message/
          AcrelMessage.java
      support/
        AbstractAcrelInboundHandler.java
        DeviceCommandSupport.java
      fourthgeneration/
        tcp/
          Acrel4gTcpInboundHandler.java
          Acrel4gTcpCommandSender.java
          message/
          packet/
            definition/
            parser/
            handler/
          support/
            Acrel4gFrameCodec.java
        transport/
          netty/
            decoder/
              Acrel4gFrameDecoderProvider.java
              AcrelDelimitedFrameDecoder.java
      gateway/
        tcp/
          AcrelGatewayTcpInboundHandler.java
          AcrelGatewayTcpCommandSender.java
          message/
          packet/
            definition/
            parser/
            handler/
          support/
            AcrelGatewayFrameCodec.java
            AcrelGatewayXmlParser.java
            AcrelGatewayDeviceResolver.java
            AcrelGatewayMeterIdCodec.java
            AcrelGatewayTransparentCodec.java
            AcrelGatewayCryptoService.java
        transport/
          netty/
            decoder/
              AcrelGatewayFrameDecoderProvider.java
              AcrelGatewayFrameDecoder.java
        mqtt/
    command/
      constant/
        AcrelRegisterMappingEnum.java
      support/
        AcrelTripleSlotParser.java
      translator/
        standard/
          AbstractAcrelCommandTranslator.java
          AcrelGetTotalEnergyTranslator.java
          AcrelSetCtTranslator.java
          ...
```

说明：
- `protocol/port/outbound`：协议层公共基类（如 `AbstractEnergyCommandTranslator`），供各厂商能量类命令复用。
- `protocol/constant`：协议帧相关常量（起止符、帧头等）。
- `protocol/common/message`：协议公共报文对象。
- `protocol/*/tcp`：协议的 TCP 处理入口，按接入方式拆分（4G 直连/网关）。
- `protocol/*/transport/netty/decoder`：Netty 传输层解码器与探测入口。
- `packet/*`：命令层的定义/解析/处理，按命令字拆分。
- `support/*FrameCodec`：负责编解码，`decode` 统一输出 `FrameDecodeResult`。
- `command/translator/standard`：默认命令转换器，实现 `DeviceCommandTranslator` 接口。

## 4. 关键文件职责

- `NettyFrameDecoderProvider`：首包探测并返回 `ProtocolSignature`，同时提供对应解码器链（长度型/起止符型等）。
- `*FrameCodec`：帧编解码，`decode` 返回 `FrameDecodeResult`。
- `*TcpInboundHandler`：协议入口，处理上行消息。
- `*TcpCommandSender`：命令发送器，处理下行消息。
- `PacketDefinition/Parser/Handler`：命令定义、解析与处理（入参统一为 `ProtocolMessageContext`，接口位于 `protocol/packet`）。
- `ProtocolInboundPublisher`：协议上行事件发布（如 `ProtocolEnergyReportInboundEvent`）。
- `ProtocolInboundEventListener`：监听协议上行事件，构建领域事件（如 `DeviceEnergyReportEvent`）并处理业务逻辑（当前为日志输出）。
- `DeviceCommandTranslator`：命令下发转换与响应解析。
- `MultiStepDeviceCommandTranslator`：多步下发命令的转换与分步解析，配合 `StepContext/StepResult` 使用。
- `DeviceProtocolHandler`：设备协议处理器，实现 `DeviceProtocolHandler` 接口，负责上行解析与命令下发。

## 5. 建议的落地顺序

1) 确定协议的首包特征、分包策略与帧格式。
2) 实现 `NettyFrameDecoderProvider`（探测 + 解码器链）+ `FrameCodec`。
3) 实现 `DeviceProtocolHandler`，继承相应的基类处理上行消息。
4) 完成命令层 `definition/parser/handler` 并注册到 registry。
5) 完成下发 `DeviceCommandTranslator` 与回执处理。
6) 补充单元测试（解帧、解析、handler、translator）。

## 6. 示例：新增 VendorX 直连协议（TCP）

以下示例仅展示骨架，具体字段请按协议调整。

### 6.1 协议探测与解码器链

```java
@Component
public class VendorxFrameDecoderProvider implements NettyFrameDecoderProvider {
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

    @Override
    public List<ChannelHandler> createDecoders(ProtocolSignature signature) {
        return List.of(new LengthFieldBasedFrameDecoder(65535, 2, 2));
    }
}
```

### 6.2 探测优先级（可选）

```java
@Override
public int getOrder() {
    return 100; // 数值越小越优先
}
```

### 6.3 设备协议处理器

```java
@Component
public class VendorxProtocolHandler implements DeviceProtocolHandler {
    
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

### 6.4 命令转换器

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

## 7. 代码流向（下发命令 / 上报解析）

### 7.1 下发命令（DeviceCommand → 设备）

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

补充：
- `completePending` 在“设备未绑定会话”或“无挂起命令”时会抛出异常，默认采用 fail-fast，便于快速暴露 ACK 时序/状态问题。

### 7.2 上报解析（设备 → 业务事件）

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
   +--> 异常上报（AbnormalEvent）
   +--> ProtocolInboundPublisher.publish(ProtocolEnergyReportInboundEvent)
          |
          v
       ProtocolInboundEventListener.handleEnergyReport
          |
          v
       构建 DeviceEnergyReportEvent -> 当前仅日志输出（后续可接入业务服务）
```

补充说明：
- 能耗上报事件中的电量字段统一使用 `BigDecimal`。
- 电量字段包含总电量与尖/峰/平/谷/深谷分时电量。
- 4G 设备上报电量为 100 倍整数，需在对应的 `PacketHandler` 中除以 100（保留 2 位小数）。
- 网关上报电量为 XML 解析出的 `BigDecimal`，可直接透传到事件中。

## 8. 新增厂商/协议接入清单

1) 新增 `NettyFrameDecoderProvider`：实现 `detectTcp` 负责首包探测，命中后返回 `ProtocolSignature`（包含 `transportType`）。  
2) 在 `NettyFrameDecoderProvider.createDecoders` 中返回专用解码器链（分包/粘包在此解决）。  
3) 新增插件 `DeviceProtocolHandler`：解析完整帧，负责设备绑定、事件发布、命令回执归还（`completePending`）。  
4) 若有下发命令：提供协议编码器/帧封装工具，优先调用 `sendWithAck`。  
   对于确实不需要 ACK 的场景，可在传输实现内部使用 `ChannelManager.sendInQueueWithoutWaiting`。  
5) 补齐单元测试：CRC、分包、ACK 等关键路径。  

### 8.1 同厂商新增特殊产品（productCode）接入要点

1) 设备录入时必须保存 `productCode`，并保证绑定后可通过 `deviceNo` 查询到该值。  
2) 命令差异：新增对应的 `DeviceCommandTranslator`，`productCode()` 返回特殊型号编码；注册后会优先命中该产品专用翻译器。  
3) Modbus 映射差异：在该产品专用翻译器中使用对应的地址/长度/倍率（不要复用默认映射）。  
4) 上报差异：若上报解析依赖 `productCode`，在 handler 解析出 `deviceNo` 后查询设备，再按产品分发到专用 parser；无法在首包探测阶段完成判定。  
5) 兼容回退：若未配置专用翻译器/解析器，继续回退到默认产品实现（`productCode` 为空的 translator/parser）。  
6) 测试补齐：为该 `productCode` 的 translator/parser 增加单测，覆盖"产品优先 + 默认回退"路径。
