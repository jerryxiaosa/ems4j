# HTTP返回值自动转换方案（注解 + 统一转换引擎 + 业务适配器）

## 1. 背景与目标

当前 Web 层接口经常需要把原始值转换为展示值，例如：
- 枚举：`ownerType -> ownerTypeName`
- 业务主键：`warnPlanId -> warnPlanName`
- 通用主键：`userId -> userName`

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

建议提供两类注解：
- `@EnumLabel`：用于枚举转换。
- `@BizLabel`：用于业务主键转换（如 `warnPlanId`、`userId`）。

示例（VO）：

```java
public class AccountVo {

    private Integer ownerType;

    @EnumLabel(source = "ownerType", enumClass = OwnerTypeEnum.class)
    private String ownerTypeName;

    private Integer warnPlanId;

    @BizLabel(source = "warnPlanId", resolver = WarnPlanNameResolver.class)
    private String warnPlanName;
}
```

注解推荐参数：
- `source`：源字段名（如 `warnPlanId`）。
- `resolver/enumClass`：使用哪个解析器或枚举。
- `fallback`：缺失时如何回填（`NULL`、`RAW_VALUE`、固定字符串）。
- `whenNullSkip`：源字段为空时是否跳过。


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

核心步骤：
1. 读取 `RestResult.data`，拉平成对象集合。
2. 根据对象类型获取注解元数据（类级缓存，避免每次反射）。
3. 按“解析器类型 + 源字段”聚合，收集去重后的 key 集合。
4. 每个解析器仅调用一次 `resolveBatch(keys, context)`。
5. 将结果映射回目标字段（如 `warnPlanName`）。
6. 返回给 Controller，Controller 无感知。

这样列表 100 条数据只会触发一次批量查，而不是 100 次单查。


### 2.3 第 3 层：业务适配器层（批量解析能力）

职责：提供“key -> label”的批量查询实现。

统一接口建议：

```java
public interface BatchLabelResolver<K> {
    Map<K, String> resolveBatch(Set<K> keys, TranslateContext context);
}
```

说明：
- 必须是“批量”接口，不提供单查入口，避免误用。
- 返回 `Map<K, String>`，缺失 key 不抛错，交给引擎按 `fallback` 处理。
- `TranslateContext` 可带租户/权限/语言等上下文（后续扩展）。
- 适配器可放在业务模块或 `ems-web` 编排层，按依赖边界选择；当前 `warnPlanId -> warnPlanName` 落在 `ems-web`。


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


### 3.3 失败与降级策略

- 解析失败不影响主业务数据返回（只影响展示字段）。
- 统一记录 warn 日志，便于排查。
- 缺失 label 按注解策略处理（`null` 或原值字符串）。


### 3.4 与现有 MapStruct/Biz 的边界

- MapStruct 继续负责 VO/DTO/BO 的结构映射。
- 引擎负责“展示字段补充”。
- Biz 不再写分散的 `id -> name`、`code -> label` 转换逻辑。


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

### 7.1 模块职责拆分

- `ems-components-translate-core`：转换框架内核
  - 注解：`@EnumLabel`、`@BizLabel`
  - SPI：`BatchLabelResolver`
  - 核心：`TranslateEngine`、`TranslateMetadataCache`、`TranslateContext`
  - 默认能力：`EnumLabelResolver`
- `ems-components-translate-web`：HTTP 适配与运行时装配层
  - `ResponseTranslateAdvice`（拦截 `RestResult`）
  - `TranslateWebAutoConfiguration` + `TranslateResponseProperties`（开关、白名单、排除路径）
- 业务模块：业务适配器实现
  - `ems-foundation-user`：`UserNameResolver`（`userId -> userName`）
  - `ems-web`：`WarnPlanNameResolver`（`warnPlanId -> warnPlanName`）
  - 其他模块按同样模式扩展

### 7.2 建议包路径

- `ems-components/ems-components-translate-core`
  - `info.zhihui.ems.components.translate.annotation`
  - `info.zhihui.ems.components.translate.engine`
  - `info.zhihui.ems.components.translate.resolver`
- `ems-components/ems-components-translate-web`
  - `info.zhihui.ems.components.translate.web.advice`
  - `info.zhihui.ems.components.translate.web.config`
- `ems-foundation/ems-foundation-user`
  - `info.zhihui.ems.foundation.user.translate.resolver`
- `ems-web`
  - `info.zhihui.ems.web.account.resolver`

### 7.3 依赖边界（必须遵守）

- 允许：`web -> business/foundation -> components/common`
- 允许：`bootstrap -> web -> business/foundation -> components/common`
- 允许：`foundation-user`、`business-plan` 依赖 `components-translate-core`（仅为实现 resolver SPI）
- 禁止：`components-translate-core` 反向依赖 `bootstrap/web` 或任何 `business-*`
- 禁止：在 `business`/`foundation` 中引入 `ResponseBodyAdvice` 等 Web 特有组件

### 7.4 Bean 组装方式

- `TranslateEngine` 使用 Spring 注入 `List<BatchLabelResolver<?>>` 自动收集所有解析器实现。
- `ResponseTranslateAdvice` 只依赖 `TranslateEngine`，不感知具体业务解析器。
- 新增一个业务 resolver 后，不需要改动 `bootstrap` 代码。


## 8. 推进建议（分阶段）

### 阶段一（内核落地）
- 在 `ems-components-translate-core` 建立注解、SPI、引擎、元数据缓存。
- 在 `ems-components-translate-web` 建立 `ResponseTranslateAdvice`，仅接通枚举转换。
- 完成单对象/列表/分页三类返回形态测试。

### 阶段二（业务适配器接入）
- `ems-foundation-user` 接入 `userId -> userName`。
- `ems-web` 接入 `warnPlanId -> warnPlanName`。
- `WarnPlanService.findList` 支持 `ids` 批量过滤并复用到 resolver。

### 阶段三（治理）
- 增加接入规范文档与脚手架模板。
- 增加转换耗时、命中率、失败率监控。


## 9. 落地顺序（执行清单）

建议按以下顺序实施，降低联调风险：

1. 先落基础内核（`ems-components-translate-core`）
- 新增注解：`@EnumLabel`、`@BizLabel`
- 新增 SPI：`BatchLabelResolver`
- 新增引擎：`TranslateEngine`、`TranslateMetadataCache`、`TranslateContext`
- 先实现 `EnumLabelResolver`，打通最小可用链路

2. 再落 HTTP 出口接入（`ems-components-translate-web` + `ems-bootstrap`）
- 新增 `ResponseTranslateAdvice` 与自动配置
- 增加配置开关（建议：`translate.response.enabled`）
- 增加排除机制（路径白名单、方法注解跳过）
- 仅对 `RestResult` 生效，避免误处理下载流等特殊响应

3. 接入第一个真实业务（建议先 `userId -> userName`）
- 在 VO 加注解字段（如 `createUserName`）
- 在 `ems-foundation-user` 实现 `UserNameResolver`
- 验证单对象、列表、分页均为一次批量查询

4. 接入 `warnPlanId -> warnPlanName`
- 在 `WarnPlanQueryDto/WarnPlanQo` 增加 `ids`，并更新 `WarnPlanRepository.xml` 查询条件
- 在对应 VO 上加注解并实现 `WarnPlanNameResolver`（当前落位：`ems-web/account/resolver`）
- 用 `WarnPlanService.findList(new WarnPlanQueryDto().setIds(...))` 完成批量名称查询

5. 清理历史手工转换代码
- 从 `biz`/`mapper` 中删除重复的 `id -> name` 逻辑
- 保留旧字段，新增 `xxxName` 字段，确保接口兼容

6. 最后补齐质量保障
- 增加单元测试：引擎、resolver、失败降级
- 增加集成测试：`RestResult<T>`、`RestResult<List<T>>`、`RestResult<PageResult<T>>`
- 增加日志与指标：转换耗时、批量命中、异常计数


## 10. 注意事项

- 该机制用于“展示字段补充”，不要替代核心业务校验。
- 对强权限隔离的数据，resolver 必须执行权限过滤。
- 对高频变化数据，谨慎开启长时缓存。
- 老接口迁移时保持向后兼容：先加 `xxxName`，不删原始字段。


## 11. 与 `/system/enums` 的关系

- `GET /system/enums` 继续作为前端“下拉值数据源”。
- 本文方案解决的是“接口返回展示字段自动补全”，两者职责不同、可同时使用。
- 枚举场景建议使用同一份 `CodeEnum` 数据作为来源，确保下拉值与列表展示值一致。
