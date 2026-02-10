# ems-iot 新协议接入开发指南

本文档描述在现有架构下接入一个新协议（新厂商/新产品）的编码方式、目录结构与关键文件。

## 预备
一般情况下，我们通过设备网关转发或者直接与4G（NB）设备通信。

网关透传Modbus RTU或者DLT645协议的数据给电表。 网关可以通过配置，接入不同品牌的设备。所以电表命令和网关行为可以相互独立（理解一下，这个很重要）。

对应到系统里，电表命令对应DeviceCommandTranslator；网关行为与4G表行为对应DeviceProtocolHandler

## 1. 设备最小接入

只要实现协议探测和设备的上下行命令处理即可完成接入：

### 协议识别与解帧（基于tcp协议）：让系统能识别协议，路由到对应的插件目录
- 实现`NettyFrameDecoderProvider`：协议探测（首包识别）与解码器安装。

### 实现DeviceProtocolHandler，处理上行和下发数据

   1. 命令解析与处理：解析设备主动上报的指令
   - 每一个上行的命令，按命令字/类型定义 `PacketDefinition + Parser + Handler`。
   - 通过`PacketDefinition + Parser + Handler`，实现命令解析与处理。
   - 响应设备上报是从底层往应用层传输，如果需要实现的响应很少只有一两个，理论上在onMessage方法if/else判断也可以。但强烈建议拆分成不同packet的解析和响应，便于以后的扩展。

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
### 上行的处理
上行命令因为是响应客户端传来的数据，所以只要能被路由到就可以，不需要分的很细

### 下行的处理
下行命令要符合统一的命令规范，才能被DeviceCommandTranslatorResolver找到。 所以需要在每个厂商的command的translator里再做产品型号的分类。

没有配置型号的实现，会认为是默认型号。一个厂商不同型号的命令大体是相同。所以只要再实现每个型号特定的命令。

一般默认型号的实现放在standard目录

## 3. 目录结构示例

以下仅描述厂商插件层目录（`info.zhihui.ems.iot.plugins.<vendor>`）。

### 3.1 厂商插件层（以 `acrel` 为例）

```text
info/zhihui/ems/iot/plugins/acrel
├── command
│   ├── constant
│   ├── support
│   └── translator
│       └── standard
└── protocol
    ├── common
    │   └── message
    ├── constant
    ├── support
    │   └── outbound
    │       └── modbus
    ├── fourthgeneration
    │   ├── tcp
    │   │   ├── message
    │   │   ├── packet
    │   │   │   ├── definition
    │   │   │   ├── parser
    │   │   │   └── handler
    │   │   └── support
    │   └── transport
    │       └── netty
    │           └── decoder
    └── gateway
        ├── mqtt
        ├── tcp
        │   ├── message
        │   ├── packet
        │   │   ├── definition
        │   │   ├── parser
        │   │   └── handler
        │   └── support
        └── transport
            └── netty
                └── decoder
```

放置规则建议：

- `command/translator/standard`：默认型号命令转换器。
- `command/translator/<productCode>`：特定型号差异实现（可按产品码扩展子目录）。
- `command/constant`：仅命令下发相关常量（寄存器地址、命令字等）。
- `protocol/constant`：仅上行帧结构相关常量（帧头、功能码、分包标记等）。
- `protocol/<accessMode>/tcp/packet/{definition,parser,handler}`：上行数据包的定义、解析、处理三段式实现。
- `protocol/<accessMode>/transport/netty/decoder`：协议探测和 Netty 解码器安装入口。
- `protocol/common/message`：当前厂商多个接入方式共享的报文对象。

### 3.2 仅命令适配场景（以 `sfere` 为例）

如果当前厂商不需要新增上行协议处理，只需要下发命令转换，可保持精简目录：

```text
info/zhihui/ems/iot/plugins/sfere
└── command
    ├── constant
    └── translator
        └── standard
```

### 4 MultiStepDeviceCommandTranslator 运行机制

在网关下发场景中，`AcrelGatewayTcpCommandSender` 会先解析命令对应的 translator。
如果 translator 实现了 `MultiStepDeviceCommandTranslator`，则进入多步模式；否则走单步模式。

执行时序如下：

```
resolve translator
  -> firstRequest(command)
  -> sendWithAck(request1)
  -> parseStep(command, payload1, stepContext)
     -> finished=true  -> return result
     -> finished=false -> nextRequest
                        -> sendWithAck(requestN)
                        -> parseStep(...)
```

多步模式的关键约束：

1. `StepContext` 用于在多个步骤之间传递中间状态（例如寄存器片段、偏移量、累计结果）。
2. 每一步都通过 `parseStep` 产出 `StepResult`，由 `finished/nextRequest/result` 驱动下一步。
3. 发送器会维护最大步数限制（`MULTI_STEP_MAX`），避免异常协议导致无限循环。

失败分支（直接失败并结束）：

1. 剩余步数耗尽。
2. `parseStep` 返回 `null`。
3. `finished=true` 但 `result=null`。
4. `finished=false` 但 `nextRequest=null`。

实现建议：

1. `firstRequest` 只负责生成第一跳请求，不做跨步状态推导。
2. `parseStep` 对同一输入保持确定性，状态变更集中写入 `StepContext`。
3. 对设备异常响应尽量返回可诊断的错误信息，避免只返回通用失败。

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
   +--> ApplicationEventPublisher.publishEvent(ProtocolEnergyReportInboundEvent)
          |
          v
       ProtocolInboundEventListener.handleEnergyReport
          |
          v
       构建 DeviceEnergyReportEvent -> 当前仅日志输出（后续可接入业务服务）
```

## 8 同厂商新增特殊产品（productCode）接入要点

1) 设备录入时必须保存 `productCode`，并保证绑定后可通过 `deviceNo` 查询到该值。  
2) 命令差异：新增对应的 `DeviceCommandTranslator`，`productCode()` 返回特殊型号编码；注册后会优先命中该产品专用翻译器。  
3) Modbus 映射差异：在该产品专用翻译器中使用对应的地址/长度/倍率（不要复用默认映射）。  
4) 上报差异：若上报解析依赖 `productCode`，在 handler 解析出 `deviceNo` 后查询设备，再按产品分发到专用 parser；无法在首包探测阶段完成判定。  
5) 兼容回退：若未配置专用翻译器/解析器，继续回退到默认产品实现（`productCode` 为空的 translator/parser）。  
6) 测试补齐：为该 `productCode` 的 translator/parser 增加单测，覆盖"产品优先 + 默认回退"路径。
