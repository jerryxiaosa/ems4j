# 开发实践指南（对齐代码与约定）

> 结合当前代码与工作区约束，沉淀可执行的工程实践，便于后续扩展与团队协作。

## 代码设计原则
- KISS：保持实现简单直接，减少不必要抽象与层次。
- SOLID：
  - 单一职责：Controller 仅处理协议/鉴权；Biz 负责编排；Service 承担领域逻辑。
  - 开闭原则：通过策略映射新增订单/支付类型，无需改动核心服务。
  - 里氏替换：接口化的 Handler/Service 可被新的实现替换。
  - 接口隔离：各层暴露最小必要接口（如 `OrderThirdPartyHandler`）。
  - 依赖倒置：上层依赖抽象（接口/上下文），下层提供实现。
- YAGNI：仅在确有需求时增加新能力（例如新增支付渠道）。

## 分层与命名
- 包结构：遵循 `biz/bo/constant/controller/dto/entity/enums/mapstruct/qo/repository/service/vo/utils` 划分。
- 命名：避免不规范缩写，采用完整词；包名小写点分隔，单数形式；接口与实现遵循 `XxxService`/`XxxServiceImpl`。
- Service 接口必须有注释；接口方法不加多余修饰符，Javadoc 注释有效。
- 枚举：类名以 `Enum` 结尾，枚举成员大写下划线分隔。

## 映射与对象转换
- MapStruct 是唯一的对象转换层：VO/DTO/BO/Entity 的转换集中维护，避免在 Controller/Service 中手写杂散转换。
- 自定义转换统一在 Mapper 中提供（如 `codeToEnum`、`intToMeterTypeEnum`）。

## 策略模式落地
- 订单域：
  - 创建：`OrderCreationHandler` 按入参类型选择实现。
  - 第三方：`OrderThirdPartyHandler` 按 `paymentChannel` 选择实现。
  - 完成：`OrderCompletionHandler` 按 `orderType` 选择实现并生成事务消息。
- 扩展：新增类型仅需实现对应接口并注册到映射表，无需修改核心 `OrderServiceImpl` 逻辑。

## 并发与一致性
- 订单完成使用锁模板保证幂等与并发安全；失败时记录并广播状态。
- 设备注册/同步通过 `DeviceModuleContext` 解析服务，确保不同平台能力的隔离与一致。

## MQ 契约管理
- 使用常量集中管理交换机/路由键/队列名称，避免魔法字符串。
- 事务消息需入库并具备重试机制；消费端保证幂等。

## 测试与准入
- 单元测试参考 `unit-test-guidelines.md`：
  - 先针对改动点编写/执行最小粒度测试，再扩大范围。
  - 失败重试不超过三次，超过则停止并分析根因。
- 变更需同步更新文档与 MapStruct 映射，保持端到端一致。

## 典型扩展建议
- 新增支付渠道（例如 Alipay）：
  - 新增 `AlipayHandler implements OrderThirdPartyHandler`。
  - 在构造函数中注入 `thirdPartyHandlerMap.put(PaymentChannelEnum.ALIPAY, handler)`。
  - 补充金额解析与状态映射，更新文档与测试。
- 新增设备通信模式/厂商：
  - 在 `DeviceModel` 能力定义中补充属性。
  - 在 `handleCommunicationMode` 中增加分支校验（保持 KISS 与单一职责）。
  - 在 `DeviceModuleContext` 提供新的解析实现。

---
本指南与代码约束同步维护。提交前请自检是否满足 KISS/SOLID/YAGNI 与命名/分层约定，并确保对象映射、策略注册与文档更新齐备。