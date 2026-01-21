# 集成测试编写规范

面向系统业务流程与跨模块协作的验证，结合现有单元测试规范与业务分析师工作方法，指导如何在本项目中有效编写、组织与执行集成测试。

## 测试目标与范围
- 验证模块之间的协作与系统行为一致性：接口、服务、仓储、消息、配置中心等。
- 覆盖关键业务流：用户登录与权限、菜单查询、订单创建与查询、订单完成后消息消费、设备模块上下文选择与配置解析等。
- 相对单元测试，集成测试强调真实 Spring 上下文、真实数据存取、少量必要的外部依赖 Mock。

## 测试环境与工程约定
- 使用 `@SpringBootTest` 启动完整应用上下文。
- 使用 `@ActiveProfiles("integrationtest")` 指定测试运行环境（建议在 `application-integrationtest` 配置中隔离测试数据源与中间件配置）。
- 使用 `@Transactional` + `@Rollback` 保持测试数据隔离与幂等，避免污染数据库。
- 推荐使用 AssertJ/ JUnit 断言与 Mockito 交互验证，与单元测试规范保持一致。

示例（来自现有工程用法）：
```java
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
@DisplayName("菜单服务集成测试")
class MenuServiceImplIntegrationTest {
    @Autowired private MenuService menuService;
    @Test
    @DisplayName("查询菜单列表 - 成功场景")
    void testFindList_Success() {
        List<MenuBo> result = menuService.findList(new MenuQueryDto());
        assertThat(result).isNotEmpty();
    }
}
```

## Mock 策略（外部依赖与稳定性优先）
- 使用 `@MockitoBean` 对外部集成或不稳定依赖进行替换（如第三方支付 SDK、系统级配置服务、分布式事务对象等），保持测试可重复性。
- 遵循“只 Mock 边界依赖”原则：核心业务服务与仓储尽量真实，避免过度 Mock 破坏集成语义。

示例（来自现有工程用法）：
```java
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class OrderServiceImplIntegrationTest {
    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;

    @MockitoBean private WxMiniProgramPaySdk wxMiniProgramPaySdk;
    @MockitoBean private ConfigService configService;
    @MockitoBean private ServiceRateService serviceRateService;
    @MockitoBean private Transaction transaction;

    @Test
    void getDetail_ShouldReturnOrderBo_WhenOrderExists() {
        OrderEntity entity = buildOrderEntity("IT-ORDER-001")
            .setOrderStatus(OrderStatusEnum.NOT_PAY.name())
            .setPaymentChannel(PaymentChannelEnum.OFFLINE.name());
        orderRepository.insert(entity);

        OrderBo result = orderService.getDetail("IT-ORDER-001");
        assertThat(result).isNotNull();
        assertThat(result.getOrderSn()).isEqualTo("IT-ORDER-001");
    }
}
```

## 设备模块上下文与配置解析（真实/Mock 模式）
- `DeviceModuleContext` 根据 `useRealDevice` 切换真实设备与 Mock 实现：
  - `useRealDevice=true`：从 `sys_config.device_config` 读取区域配置，按 `implName` 与容器中实现类 `SimpleName`（大小写不敏感）匹配返回服务实现。
  - `useRealDevice=false`：返回以 `Mock` 前缀命名的唯一 Mock 实现。
- 集成测试建议：通过属性覆盖验证两种分支逻辑。

示例（属性覆盖真实/Mock 模式）：
```java
@SpringBootTest
@ActiveProfiles("integrationtest")
@TestPropertySource(properties = "useRealDevice=false")
class DeviceModuleContextIntegrationTest {
    @Autowired private DeviceModuleContext deviceModuleContext;

    @Test
    @DisplayName("Mock 模式下返回 MockEnergyService")
    void testGetService_MockMode() {
        EnergyService service = deviceModuleContext.getService(EnergyService.class, 1);
        assertThat(service.getClass().getSimpleName().toLowerCase()).startsWith("mock");
    }
}
```

## 控制器集成测试（HTTP 层）
- 推荐使用 `@AutoConfigureMockMvc` + `MockMvc` 进行 Web 层集成测试，不依赖真实网络端口。
- 验证入参校验、鉴权拦截、业务响应结构。

示例：
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
class ConfigControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("获取系统配置 - 成功")
    void testGetConfig_Success() throws Exception {
        mockMvc.perform(get("/api/config/device"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data").exists());
    }
}
```

## 测试数据准备与验证
- 数据准备：
  - 使用仓储 `insert`/`update` 插入最小必要数据，避免复杂依赖。
  - 或使用 Profile 对应的初始化 SQL/数据装载（如独立测试库）。
- 验证方式：
  - 断言返回对象关键字段；
  - 通过仓储查询回写结果验证持久化；
  - 对 Mock 依赖使用 `verify` 验证交互次数与参数。

示例（仓储数据验证）：
```java
OrderDetailTerminationEntity detail = // ... 业务调用后生成
OrderEntity orderEntity = orderRepository.selectOne(
    new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderSn, detail.getOrderSn()));
assertNotNull(orderEntity);
```

## 命名、结构与断言规范（与单元测试规范对齐）
- 类命名：`{被测类名}IntegrationTest`；方法命名：`methodName_场景_期望`。
- 为测试类与方法使用 `@DisplayName` 中文描述。
- 断言具体到字段与业务语义，配合异常场景 `assertThrows`/`assertThatThrownBy`。
- 使用 `ArgumentCaptor` 捕获复杂参数进行验证（可选）。

## 业务流矩阵与覆盖建议（结合业务分析师方法）
- 业务流拆解：按领域与模块（用户/权限、组织、设备、订单、消息）定义端到端用例。
- 典型覆盖：
  - 用户域：登录失败与成功、菜单加载（来源 `MenuServiceImplIntegrationTest`）。
  - 组织域：组织列表查询与分页、模糊查询（来源 `OrganizationServiceImplIntegrationTest`）。
  - 订单域：订单详情查询（正常/删除/异常枚举）、订单完成后的消息消费链路（建议通过直接调用消费端处理器方法 + 仿真消息载荷验证）。
  - 设备域：`DeviceModuleContext` 在不同 `areaId` 下的实现选择与 `configValue` 解析异常兜底。
  - 系统配置域：`device_config` 更新覆盖与读取异常（空值/未配置区域）。
- 输出规范：为每条用例标注业务目标、前置条件、步骤、期望结果、可视化（流程图/时序图可选）。

## 执行与失败策略
- 执行命令（Maven）：
  - 运行全部测试：`mvn -q test`
  - 仅运行集成测试：`mvn -q -Dtest=*IntegrationTest test`
- 失败处理：连续失败不超过 3 次，若仍失败需停止执行并反馈原因与阻断点（与单元测试规范一致）。

## 常见反模式与最佳实践
- 反模式：
  - 过度 Mock 导致集成意义丧失；
  - 在一个测试方法中编排过多步骤与断言；
  - 缺乏异常分支与边界条件覆盖。
- 最佳实践：
  - 使用最小数据驱动测试；
  - 关键外部依赖就地 Mock，核心路径真实；
  - 断言语义化、验证交互；
  - 对设备模块上下文增加真实/Mock 模式双分支用例。

## 附：与单元测试规范的关系
- 断言、命名、Mock、覆盖率等保持一致与继承；
- 集成测试不强求单元覆盖率指标，但需对关键业务流形成闭环验证；
- 建议将复杂断言下沉到单元测试，集成测试关注协作与流程正确性。

---
如需将本文扩展为“业务流矩阵 + 时序图”的可视化版本，可参考 `resources/prompt/业务分析师prompt.md` 的输出规范进行补充与绘图。