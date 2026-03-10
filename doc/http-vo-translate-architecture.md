# HTTP返回值自动转换方案（注解 + 统一转换引擎 + 业务适配器）

## 1. 背景与目标

当前 Web 层接口经常需要把原始值转换为展示值，例如：
- 枚举：`ownerType -> ownerTypeName`
- 业务主键：`warnPlanId -> warnPlanName`
- 通用主键：`userId -> userName`
- 本地格式化：`monthlyPayAmount -> monthlyPayAmountText`

如果每个接口都在 `biz` 里手工转换，会有三个问题：
- 重复代码多，维护成本高。
- 列表场景容易出现 N+1 查询（每条记录查一次）。
- 不同接口的转换规则容易不一致。

本方案目标是把“展示值转换”收敛成一套可复用机制：
- VO 上声明“我要转换什么”。
- 引擎统一做批量查询和回填。
- 不同业务只实现自己的“批量解析适配器”。


## 2. 原理说明（三层结构）

### 2.1 第 1 层：注解声明层（在 VO 上定义转换规则）

职责：只描述规则，不做实际查询。

当前实现提供三类注解：
- `@EnumLabel`：用于枚举转换。
- `@BizLabel`：用于业务主键转换（如 `warnPlanId`、`userId`）。
- `@FormatText`：用于单源字段的本地格式化（如金额格式化）。

当前已落地的典型场景包括：
- `organizationId -> organizationName`
- `certificatesType -> certificatesTypeText`
- `userPhone -> 脱敏后的 userPhone`
- `createTime/updateTime -> 格式化后的时间字符串`

示例（VO）：

```java
public class AccountVo {

    private Integer ownerType;

    @EnumLabel(source = "ownerType", enumClass = OwnerTypeEnum.class)
    private String ownerTypeName;

    private Integer warnPlanId;

    @BizLabel(source = "warnPlanId", resolver = WarnPlanNameResolver.class)
    private String warnPlanName;

    private BigDecimal electricBalanceAmount;

    @FormatText(source = "electricBalanceAmount", formatter = MoneyScale2TextFormatter.class)
    private String electricBalanceAmountText;
}
```

注解推荐参数：
- `source`：源字段名（如 `warnPlanId`）。
- `resolver/enumClass`：使用哪个解析器或枚举。
- `fallback`：缺失时如何回填（`NULL`、`RAW_VALUE`、固定字符串）。
- `whenNullSkip`：源字段为空时是否跳过。

补充：
- `@FormatText` 当前适合“单源字段 + 本地纯格式化”场景，不负责外部 IO。
- 复杂动态展示值（例如依赖 `isOnline + lastOnlineTime + now` 的离线时长）仍建议放在 Web `biz` 层计算，不要强行塞进 translate 体系。
- 当前 `PhoneMaskFormatter` 已用于手机号脱敏展示；对于非字符串或空值输入，formatter 会直接返回 `null`。


### 2.2 第 2 层：统一转换引擎（核心在 components-translate-core，HTTP 适配在 components-translate-web）

职责：扫描注解、批量收集 key、调用适配器、回填展示字段。

模块落位（当前实现）：
- 引擎核心：`ems-components-translate-core`（与 HTTP 解耦）
- HTTP 挂载点：`ems-components-translate-web` 的 `ResponseTranslateAdvice`
- 业务应用装配：`ems-bootstrap` 通过依赖 `ems-components-translate-web` 自动装配

处理对象：
- 单对象：`RestResult<AccountVo>`
- 列表：`RestResult<List<AccountVo>>`
- 分页：`RestResult<PageResult<AccountVo>>`
- 嵌套列表/对象：通过 `@TranslateChild` 显式声明递归子节点

核心步骤：
1. 读取 `RestResult.data`，构建对象图。
2. 根据对象类型获取注解元数据（类级缓存，避免每次反射）。
3. 仅递归处理显式声明了 `@TranslateChild` 的字段，避免全对象图无边界扫描。
4. 按“解析器类型 + 源字段”聚合，收集去重后的 key 集合。
5. 每个解析器仅调用一次 `resolveBatch(keys, context)`。
6. 本地格式化字段走 `FieldTextFormatter`，不访问外部服务。
7. 将结果映射回目标字段（如 `warnPlanName`、`electricBalanceAmountText`）。
8. 返回给 Controller，Controller 无感知。

这样列表 100 条数据只会触发一次批量查，而不是 100 次单查。


### 2.3 第 3 层：业务适配器层（批量解析能力）

职责：提供“key -> label”的批量查询实现。

统一接口（当前实现）：

```java
public interface BatchLabelResolver<K> {
    Map<K, String> resolveBatch(Set<K> keys, TranslateContext context);
}
```

说明：
- 必须是“批量”接口，不提供单查入口，避免误用。
- 返回 `Map<K, String>`，缺失 key 不抛错，交给引擎按 `fallback` 处理。
- `TranslateContext` 可带租户/权限/语言等上下文（后续扩展）。
- 适配器可放在业务模块或 `ems-web` 编排层，按依赖边界选择；当前公共 resolver 统一收敛在 `ems-web/common/resolver`。


## 3. 统一引擎的关键设计点

### 3.1 防 N+1 的批量策略

- 同一次响应内，按 resolver 分组收集全部 key。
- 去重后一次查询。
- 回填阶段只做内存 `Map` 查找。
- 对超大集合按固定分片（如 500）查询，防止 SQL 过长。


### 3.2 缓存策略（建议两级）

- 一级：请求级缓存（必须）
  - 同一请求里多字段或多对象使用同一 resolver 时复用结果。
- 二级：短 TTL 本地缓存（可选）
  - 适用于变更频率低的数据，如方案名、组织名、角色名。
  - 不适用于强实时且频繁变更的数据。

当前实现补充：
- `TranslateMetadataCache` 负责类级元数据缓存。
- `TranslateEngine` 对 formatter 做了本地缓存与缺失缓存，避免重复扫描与重复告警。
- `ResponseTranslateAdvice` 已在 `WebMvcTest` 场景中显式导入，用于保证 Web 层接口测试与运行时行为一致。


### 3.3 失败与降级策略

- 解析失败不影响主业务数据返回（只影响展示字段）。
- 统一记录 warn 日志，便于排查。
- 缺失 label 按注解策略处理（`null` 或原值字符串）。
- `@FormatText` 的 formatter 仅做本地格式化；误配或格式化异常时按注解 fallback 降级。


### 3.4 与现有 MapStruct/Biz 的边界

- MapStruct 继续负责 VO/DTO/BO 的结构映射。
- 引擎负责“展示字段补充”。
- Biz 不再写分散的 `id -> name`、`code -> label` 转换逻辑。
- Biz 仍负责复杂动态展示值，例如：
  - `offlineDurationText`
  - `spaceParentNames`
  - 其他依赖当前时间或多个源字段的展示属性


## 4. 接入指南（新业务如何接入）

### 4.1 接入前检查

要接入某个“XxxId -> XxxName”转换，先确认：
- 是否存在稳定唯一的 key（如 `id`、`code`）。
- 是否有批量查询能力（必须）。
- 是否存在权限/租户约束（解析器要带上上下文过滤）。
- 目标字段是否只是展示用途（不参与核心业务判断）。


### 4.2 标准接入步骤

1. 在 VO 上新增展示字段，并加注解。
2. 编写或复用 `BatchLabelResolver`。
3. 若业务服务缺少批量能力，先补服务接口与 SQL。
4. 将 resolver 注册到 Spring 容器。
5. 添加单元测试/集成测试。
6. 从 Biz 中移除重复手工转换代码。


## 5. 示例：接入 `userId -> userName`

这里以“创建人 ID 显示创建人名称”为例。

### 5.1 VO 定义

```java
public class WarnPlanVo {
    private Integer createUser;

    @BizLabel(source = "createUser", resolver = UserNameResolver.class)
    private String createUserName;
}
```


### 5.2 适配器实现

`UserService` 已有 `findUserList(UserQueryDto)`，且 `UserQueryDto` 支持 `ids`，可直接复用做批量查询。

```java
@Component
@RequiredArgsConstructor
public class UserNameResolver implements BatchLabelResolver<Integer> {

    private final UserService userService;

    @Override
    public Map<Integer, String> resolveBatch(Set<Integer> keys, TranslateContext context) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        List<UserBo> userList = userService.findUserList(
                new UserQueryDto().setIds(new ArrayList<>(keys))
        );

        return userList.stream()
                .filter(user -> user.getId() != null)
                .collect(Collectors.toMap(UserBo::getId, UserBo::getUserName, (left, right) -> left));
    }
}
```


### 5.3 测试建议

- 单对象：`createUser=1`，能回填 `createUserName`。
- 列表：100 条记录只调用一次 resolver。
- 分页：`PageResult.list` 正常回填。
- 缺失用户：字段按降级策略返回 `null`（或约定占位文案）。


## 5.4 已落地示例：`UserVo` 的组织名、证件类型与手机号格式化

当前 `UserVo` 已接入三类典型转换：

- `organizationId -> organizationName`
  - 通过 `OrganizationNameResolver` 批量查询组织名称
- `certificatesType -> certificatesTypeText`
  - 通过 `@EnumLabel` 做枚举文案转换
- `userPhone -> 脱敏手机号`
  - 通过 `PhoneMaskFormatter` 做本地格式化

这类接入说明：
- 业务查询仍返回原始字段
- Web 返回前由统一引擎补齐展示字段
- `UserManageControllerTest` 中已显式覆盖这些转换链路，确保接口测试能够验证展示值回填结果


## 6. 示例：接入 `warnPlanId -> warnPlanName`

### 6.1 已落地的批量能力

当前实现通过 `WarnPlanService.findList(WarnPlanQueryDto)` 承载批量查询能力：
- `WarnPlanQueryDto` 新增 `ids` 参数。
- `WarnPlanQo` 同步新增 `ids` 参数。
- `WarnPlanRepository.xml` 增加 `id in (...)` 条件（并保留 `is_deleted=0` 过滤）。


### 6.2 VO 与适配器

```java
public class AccountVo {
    private Integer warnPlanId;

    @BizLabel(source = "warnPlanId", resolver = WarnPlanNameResolver.class)
    private String warnPlanName;
}
```

```java
@Component
@RequiredArgsConstructor
public class WarnPlanNameResolver implements BatchLabelResolver<Integer> {

    private final WarnPlanService warnPlanService;

    @Override
    public Map<Integer, String> resolveBatch(Set<Integer> keys, TranslateContext context) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Integer> idList = keys.stream().filter(Objects::nonNull).distinct().toList();
        if (idList.isEmpty()) {
            return Collections.emptyMap();
        }

        return warnPlanService.findList(new WarnPlanQueryDto().setIds(idList)).stream()
                .filter(plan -> plan.getId() != null)
                .collect(Collectors.toMap(WarnPlanBo::getId, WarnPlanBo::getName, (left, right) -> left));
    }
}
```


## 7. 模块落位（组件化）

### 7.1 模块职责拆分（当前实现）

- `ems-components-translate-core`：转换框架内核
  - 注解：`@EnumLabel`、`@BizLabel`
  - SPI：`BatchLabelResolver`
  - 核心：`TranslateEngine`、`TranslateMetadataCache`、`TranslateContext`
  - 默认能力：`EnumLabelResolver`
- `ems-components-translate-web`：HTTP 适配与运行时装配层
  - `ResponseTranslateAdvice`（拦截 `RestResult`）
  - `TranslateWebAutoConfiguration` + `TranslateResponseProperties`（开关、白名单、排除路径）
- 业务模块：业务适配器实现
  - `ems-web/common/resolver`：`WarnPlanNameResolver`
  - `ems-web/common/resolver`：`ElectricPricePlanNameResolver`
  - `ems-web/common/resolver`：`DeviceModelNameResolver`
  - 其他模块按同样模式扩展

### 7.2 当前包路径

- `ems-components/ems-components-translate-core`
  - `info.zhihui.ems.components.translate.annotation`
  - `info.zhihui.ems.components.translate.engine`
  - `info.zhihui.ems.components.translate.resolver`
- `ems-components/ems-components-translate-web`
  - `info.zhihui.ems.components.translate.web.advice`
  - `info.zhihui.ems.components.translate.web.config`
- `ems-web`
  - `info.zhihui.ems.web.common.resolver`
  - `info.zhihui.ems.web.common.util`

### 7.3 依赖边界（必须遵守）

- 允许：`web -> business/foundation -> components/common`
- 允许：`bootstrap -> web -> business/foundation -> components/common`
- 允许：`foundation-user`、`business-plan` 依赖 `components-translate-core`（仅为实现 resolver SPI）
- 禁止：`components-translate-core` 反向依赖 `bootstrap/web` 或任何 `business-*`
- 禁止：在 `business`/`foundation` 中引入 `ResponseBodyAdvice` 等 Web 特有组件

### 7.4 Bean 组装方式

- `TranslateEngine` 使用 Spring 注入 `List<BatchLabelResolver<?>>` 自动收集所有解析器实现。
- `TranslateEngine` 使用 Spring 注入 `List<FieldTextFormatter>` 自动收集所有本地格式化器实现。
- `ResponseTranslateAdvice` 只依赖 `TranslateEngine`，不感知具体业务解析器。
- 新增一个业务 resolver 后，不需要改动 `bootstrap` 代码。
- 新增一个 formatter 后，也不需要改动 `bootstrap` 代码。

## 8. 当前已落地能力

### 8.1 已落地的注解与能力

- `@EnumLabel`
- `@BizLabel`
- `@FormatText`
- `@TranslateChild`

### 8.2 已落地的典型场景

- `warnPlanId -> warnPlanName`
- `pricePlanId -> pricePlanName`
- `modelId -> modelName`
- `monthlyPayAmount -> monthlyPayAmountText`
- `electricBalanceAmount -> electricBalanceAmountText`
- 嵌套列表递归翻译：`AccountDetailVo.meterList`

### 8.3 当前不建议接入 translate 的场景

以下场景当前仍建议留在 `biz` 层：

- 依赖多个源字段的展示值
- 依赖 `now()` 的动态展示值
- 需要返回 `List<String>` 等复杂结构

典型例子：

- `offlineDurationText`
- `spaceParentNames`


## 9. 接入规范（当前版本）

建议按以下顺序接入新展示字段：

1. 确认是否属于“单源字段展示值”
- 是：优先考虑 `@EnumLabel` / `@BizLabel` / `@FormatText`
- 否：优先放在 `biz` 层

2. 若是业务主键展示值
- 优先补批量查询能力（如 `ids` 过滤）
- 再实现 resolver

3. 若是嵌套对象/列表
- 在父 VO 字段上显式加 `@TranslateChild`
- 不要依赖隐式递归

4. 接入后补测试
- resolver 测试
- `ResponseTranslateAdvice` 链路测试
- controller 或集成测试


## 10. 注意事项

- 该机制用于“展示字段补充”，不要替代核心业务校验。
- 对强权限隔离的数据，resolver 必须执行权限过滤。
- 对高频变化数据，谨慎开启长时缓存。
- 老接口迁移时保持向后兼容：先加 `xxxName`，不删原始字段。
- resolver 命名应稳定、语义明确，统一放到 `ems-web/common/resolver`。
- 工具型展示逻辑若未接入 translate，统一收敛到 `ems-web/common/util`，避免散落在各个 `biz`。


## 11. 与 `/system/enums` 的关系

- `GET /system/enums` 继续作为前端“下拉值数据源”。
- 本文方案解决的是“接口返回展示字段自动补全”，两者职责不同、可同时使用。
- 枚举场景建议使用同一份 `CodeEnum` 数据作为来源，确保下拉值与列表展示值一致。
