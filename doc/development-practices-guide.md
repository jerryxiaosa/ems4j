# 开发实践指南（对齐代码与约定）

> 结合当前代码与工作区约束，沉淀可执行的工程实践，便于后续扩展与团队协作。

## 代码设计原则
- SOLID：实现高内聚、低耦合的软件设计
- KISS：保持实现简单直接，减少不必要抽象与层次。
- YAGNI：仅在确有需求时增加新能力（例如新增支付渠道）。

## 分层与命名

### 包结构分层
- 遵循 `biz/bo/constant/controller/dto/entity/enums/mapstruct/qo/repository/service/vo/utils` 标准目录结构
- 模块内部严格按职责划分，controller 不直接访问 repository，需通过 service 层
- biz 层：业务服务层，实现业务逻辑编排
- bo 层：业务对象，承载核心业务数据
- dto 层：数据传输对象，用于接口入参出参
- vo 层：视图对象，前端展示专用
- entity 层：实体对象，与数据库表映射
- qo 层：查询对象，封装查询条件
- repository 层：数据访问层，只处理数据存取

### 命名约定
- 避免不规范缩写，采用完整词汇表达意图（如 `AccountOpeningService` 而非 `AccOpenService`）
- 包名使用小写字母，点分隔符分隔，统一使用单数形式（如 `info.zhihui.ems.common.util`）
- 类名使用帕斯卡命名法（`UserService`, `AccountOpeningRequest`）
- 方法名和变量名使用驼峰命名法（`getUserById`, `accountBalance`）
- 常量名使用全大写字母，单词间用下划线分隔（`MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT_MS`）
- 接口与实现类遵循 `XxxService`/`XxxServiceImpl` 命名规范

### 接口与实现规范
- Service 接口必须提供清晰的 Javadoc 注释，说明职责和使用场景
- 接口方法不加多余修饰符（如 public），保持代码简洁
- 实现类命名严格遵循 `XxxServiceImpl` 规范，便于识别
- 接口方法签名要准确反映业务语义（如 `openAccount`, `closeAccount`, `calculateBilling`）

### 枚举命名规范
- 枚举类名以 `Enum` 结尾（如 `AccountStatusEnum`, `PaymentChannelEnum`）
- 枚举成员全部大写，单词间用下划线分隔（如 `ACTIVE`, `INACTIVE`, `WECHAT_PAY`）
- 枚举构造方法私有化，避免外部实例化

### 常量命名规范
- 类级别常量放置在对应类的顶部，按功能分组排列
- 按照业务含义分组（如数据库配置、缓存配置、业务规则配置等）
- 重要常量需添加注释说明其业务含义

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

---
本指南与代码约束同步维护。提交前请自检是否满足 KISS/SOLID/YAGNI 与命名/分层约定，并确保对象映射、策略注册与文档更新齐备。