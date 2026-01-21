# 新增厂商/协议接入清单

1) 新增 `NettyProtocolDetector`：根据协议魔数/特征返回 `ProtocolSignature`（包含 `transportType`）。  
2) 新增 `NettyFrameDecoderProvider`：签名命中后返回专用解码器链（分包/粘包在此解决）。  
3) 新增插件 `DeviceProtocolHandler`：解析完整帧，负责设备绑定、事件发布、命令回执归还（`completePending`）。  
4) 若有下发命令：提供协议编码器/帧封装工具，调用 `sendWithAck/sendFireAndForget`。  
5) 补齐单元测试：CRC、分包、ACK 等关键路径。  

## 同厂商新增特殊产品（productCode）接入要点

1) 设备录入时必须保存 `productCode`，并保证绑定后可通过 `deviceNo` 查询到该值。  
2) 命令差异：新增对应的 `DeviceCommandTranslator`，`productCode()` 返回特殊型号编码；注册后会优先命中该产品专用翻译器。  
3) Modbus 映射差异：在该产品专用翻译器中使用对应的地址/长度/倍率（不要复用默认映射）。  
4) 上报差异：若上报解析依赖 `productCode`，在 handler 解析出 `deviceNo` 后查询设备，再按产品分发到专用 parser；无法在 `NettyProtocolDetector` 阶段完成判定。  
5) 兼容回退：若未配置专用翻译器/解析器，继续回退到默认产品实现（`productCode` 为空的 translator/parser）。  
6) 测试补齐：为该 `productCode` 的 translator/parser 增加单测，覆盖“产品优先 + 默认回退”路径。  
