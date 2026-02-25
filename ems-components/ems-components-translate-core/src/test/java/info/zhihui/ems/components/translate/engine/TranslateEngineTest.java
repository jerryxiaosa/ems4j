package info.zhihui.ems.components.translate.engine;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.components.translate.annotation.BizLabel;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import info.zhihui.ems.components.translate.annotation.FormatText;
import info.zhihui.ems.components.translate.annotation.TranslateChild;
import info.zhihui.ems.components.translate.annotation.TranslateFallbackEnum;
import info.zhihui.ems.components.translate.formatter.FieldTextFormatter;
import info.zhihui.ems.components.translate.formatter.MoneyScale2TextFormatter;
import info.zhihui.ems.components.translate.resolver.BatchLabelResolver;
import info.zhihui.ems.components.translate.resolver.EnumLabelResolver;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TranslateEngine测试")
class TranslateEngineTest {

    @Test
    @DisplayName("列表转换应批量调用业务解析器一次")
    void testTranslate_ListData_ShouldBatchResolve() {
        TestUserResolver resolver = new TestUserResolver();
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), List.of(resolver));

        List<TestVo> list = List.of(
                new TestVo().setStatus(1).setUserId(100),
                new TestVo().setStatus(0).setUserId(101),
                new TestVo().setStatus(1).setUserId(100)
        );

        engine.translate(list, new TranslateContext());

        assertEquals(1, resolver.invokeCount.get());
        assertEquals(Set.of(100, 101), resolver.latestKeys);
        assertEquals("启用", list.get(0).getStatusName());
        assertEquals("停用", list.get(1).getStatusName());
        assertEquals("用户-100", list.get(0).getUserName());
        assertEquals("用户-101", list.get(1).getUserName());
    }

    @Test
    @DisplayName("分页数据转换应正常生效")
    void testTranslate_PageResult_ShouldTranslateListData() {
        TestUserResolver resolver = new TestUserResolver();
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), List.of(resolver));

        TestVo item = new TestVo().setStatus(1).setUserId(200);
        PageResult<TestVo> pageResult = new PageResult<TestVo>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L)
                .setList(List.of(item));

        engine.translate(pageResult, null);

        assertEquals("启用", item.getStatusName());
        assertEquals("用户-200", item.getUserName());
        assertEquals(1, resolver.invokeCount.get());
        assertNotNull(resolver.latestContext);
    }

    @Test
    @DisplayName("应递归转换显式声明的子集合字段")
    void testTranslate_NestedChildCollection_ShouldTranslateRecursively() {
        TranslateEngine engine = buildEngine(
                new EnumLabelResolver(),
                Collections.emptyList(),
                List.of(new MoneyScale2TextFormatter())
        );

        NestedChildItemVo child = new NestedChildItemVo()
                .setStatus(1)
                .setAmount(new BigDecimal("45.6"));
        NestedContainerVo container = new NestedContainerVo().setChildList(List.of(child));

        engine.translate(container, new TranslateContext());

        assertEquals("启用", child.getStatusName());
        assertEquals("45.60", child.getAmountText());
    }

    @Test
    @DisplayName("应递归转换显式声明的子对象字段")
    void testTranslate_NestedChildObject_ShouldTranslateRecursively() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList());

        NestedSingleContainerVo container = new NestedSingleContainerVo()
                .setChild(new TestVo().setStatus(0).setUserId(1));

        engine.translate(container, new TranslateContext());

        assertEquals("停用", container.getChild().getStatusName());
    }

    @Test
    @DisplayName("递归转换遇到循环引用时应跳过重复节点")
    void testTranslate_RecursiveCycle_ShouldBypassRepeatedNode() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList());

        RecursiveNodeVo root = new RecursiveNodeVo().setStatus(1);
        RecursiveNodeVo child = new RecursiveNodeVo().setStatus(0);
        root.setChild(child);
        child.setChild(root);

        assertDoesNotThrow(() -> engine.translate(root, new TranslateContext()));
        assertEquals("启用", root.getStatusName());
        assertEquals("停用", child.getStatusName());
        assertTrue(root != child);
    }

    @Test
    @DisplayName("递归层级过深时应跳过超限节点且不抛异常")
    void testTranslate_RecursiveDepthExceeded_ShouldSkipDeeperNode() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList());

        RecursiveNodeVo root = new RecursiveNodeVo().setStatus(1);
        RecursiveNodeVo currentNode = root;
        for (int i = 0; i < 32; i++) {
            RecursiveNodeVo childNode = new RecursiveNodeVo().setStatus(i % 2 == 0 ? 0 : 1);
            currentNode.setChild(childNode);
            currentNode = childNode;
        }
        RecursiveNodeVo deepestNode = currentNode;

        assertDoesNotThrow(() -> engine.translate(root, new TranslateContext()));
        assertEquals("启用", root.getStatusName());
        assertNull(deepestNode.getStatusName());
    }

    @Test
    @DisplayName("空集合和空分页应直接返回")
    void testTranslate_EmptyCollection_ShouldBypass() {
        TestUserResolver resolver = new TestUserResolver();
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), List.of(resolver));

        engine.translate(Collections.emptyList(), new TranslateContext());
        engine.translate(new PageResult<TestVo>().setList(Collections.emptyList()), new TranslateContext());

        assertEquals(0, resolver.invokeCount.get());
    }

    @Test
    @DisplayName("空入参不应抛出异常")
    void testTranslate_NullData_ShouldNotThrow() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList());

        assertDoesNotThrow(() -> engine.translate(null, null));
    }

    @Test
    @DisplayName("缺失映射时应按RAW_VALUE回退")
    void testTranslate_MissingLabel_ShouldUseRawFallback() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList());

        FallbackVo vo = new FallbackVo().setStatus(99);
        engine.translate(vo, new TranslateContext());

        assertEquals("99", vo.getStatusName());
    }

    @Test
    @DisplayName("未注册业务解析器时应按固定文案回退")
    void testTranslate_MissingResolver_ShouldUseFixedTextFallback() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList());

        MissingResolverVo vo = new MissingResolverVo().setUserId(66);
        engine.translate(vo, new TranslateContext());

        assertEquals("未知用户", vo.getUserName());
    }

    @Test
    @DisplayName("业务解析器返回null时应按固定文案回退")
    void testTranslate_ResolverReturnNull_ShouldUseFixedTextFallback() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), List.of(new NullResultUserResolver()));

        NullMapResolverVo vo = new NullMapResolverVo().setUserId(77);
        engine.translate(vo, new TranslateContext());

        assertEquals("兜底用户", vo.getUserName());
    }

    @Test
    @DisplayName("源字段为空且不跳过时应执行固定文案回退")
    void testTranslate_NullSourceAndNotSkip_ShouldUseFixedTextFallback() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList());

        NullSourceFallbackVo vo = new NullSourceFallbackVo().setStatus(null);
        engine.translate(vo, new TranslateContext());

        assertEquals("未知状态", vo.getStatusName());
    }

    @Test
    @DisplayName("目标字段类型不支持时应跳过写入")
    void testTranslate_UnsupportedTargetFieldType_ShouldSkipWrite() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList());

        UnsupportedTargetTypeVo vo = new UnsupportedTargetTypeVo().setStatus(1);
        engine.translate(vo, new TranslateContext());

        assertNull(vo.getStatusName());
    }

    @Test
    @DisplayName("枚举解析器返回空映射时应按固定文案回退")
    void testTranslate_EnumResolverReturnNull_ShouldUseFixedTextFallback() {
        TranslateEngine engine = buildEngine(new NullEnumLabelResolver(), Collections.emptyList());

        EnumFixedFallbackVo vo = new EnumFixedFallbackVo().setStatus(1);
        engine.translate(vo, new TranslateContext());

        assertEquals("枚举兜底", vo.getStatusName());
    }

    @Test
    @DisplayName("列表包含空元素和无注解对象时应跳过并继续处理")
    void testTranslate_ListWithNullAndNoMetadataObject_ShouldSkipInvalidItem() {
        TestUserResolver resolver = new TestUserResolver();
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), List.of(resolver));

        TestVo translatableVo = new TestVo().setStatus(1).setUserId(300);
        List<Object> sourceList = Arrays.asList(null, new NoTranslateVo().setName("raw"), translatableVo);
        engine.translate(sourceList, new TranslateContext());

        assertEquals(1, resolver.invokeCount.get());
        assertEquals(Set.of(300), resolver.latestKeys);
        assertEquals("启用", translatableVo.getStatusName());
        assertEquals("用户-300", translatableVo.getUserName());
    }

    @Test
    @DisplayName("源字段为空且whenNullSkip为true时应保留原目标值")
    void testTranslate_NullSourceAndWhenNullSkip_ShouldKeepOriginalTargetValue() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList());

        WhenNullSkipVo vo = new WhenNullSkipVo().setStatusName("保留原值");
        engine.translate(vo, new TranslateContext());

        assertEquals("保留原值", vo.getStatusName());
    }

    @Test
    @DisplayName("业务解析器返回空映射时应按固定文案回退")
    void testTranslate_ResolverReturnEmptyMap_ShouldUseFixedTextFallback() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), List.of(new EmptyResultUserResolver()));

        EmptyMapResolverVo vo = new EmptyMapResolverVo().setUserId(78);
        engine.translate(vo, new TranslateContext());

        assertEquals("空映射兜底", vo.getUserName());
    }

    @Test
    @DisplayName("业务解析器返回null标签时应保持null")
    void testTranslate_ResolverReturnNullLabel_ShouldKeepNullValue() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), List.of(new NullValueUserResolver()));

        NullValueResolverVo vo = new NullValueResolverVo().setUserId(79);
        engine.translate(vo, new TranslateContext());

        assertNull(vo.getUserName());
    }

    @Test
    @DisplayName("源字段为空且RAW_VALUE回退时应保持null")
    void testTranslate_NullSourceAndRawFallback_ShouldKeepNullValue() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList());

        NullSourceRawFallbackVo vo = new NullSourceRawFallbackVo().setStatus(null);
        engine.translate(vo, new TranslateContext());

        assertNull(vo.getStatusName());
    }

    @Test
    @DisplayName("目标字段为CharSequence时应写入转换结果")
    void testTranslate_TargetFieldCharSequence_ShouldWriteValue() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList());

        CharSequenceTargetVo vo = new CharSequenceTargetVo().setStatus(1);
        engine.translate(vo, new TranslateContext());

        assertEquals("启用", vo.getStatusName());
    }

    @Test
    @DisplayName("目标字段为Object时应写入转换结果")
    void testTranslate_TargetFieldObject_ShouldWriteValue() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList());

        ObjectTargetVo vo = new ObjectTargetVo().setStatus(1);
        engine.translate(vo, new TranslateContext());

        assertEquals("启用", vo.getStatusName());
    }

    @Test
    @DisplayName("本地格式化注解应输出两位小数字符串")
    void testTranslate_FormatText_ShouldFormatMoneyScale2() {
        TranslateEngine engine = buildEngine(
                new EnumLabelResolver(),
                Collections.emptyList(),
                List.of(new MoneyScale2TextFormatter())
        );

        MoneyFormatVo vo = new MoneyFormatVo().setAmount(new BigDecimal("12.3"));
        engine.translate(vo, new TranslateContext());

        assertEquals("12.30", vo.getAmountText());

        MoneyFormatVo anotherVo = new MoneyFormatVo().setAmount(new BigDecimal("8"));
        engine.translate(anotherVo, new TranslateContext());

        assertEquals("8.00", anotherVo.getAmountText());
    }

    @Test
    @DisplayName("未注册格式化器时应按固定文案回退并支持重复调用")
    void testTranslate_MissingFormatter_ShouldUseFixedTextFallback() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), Collections.emptyList(), Collections.emptyList());

        MissingFormatterVo firstVo = new MissingFormatterVo().setAmount(new BigDecimal("12.3"));
        engine.translate(firstVo, new TranslateContext());
        assertEquals("金额异常", firstVo.getAmountText());

        MissingFormatterVo secondVo = new MissingFormatterVo().setAmount(new BigDecimal("5"));
        engine.translate(secondVo, new TranslateContext());
        assertEquals("金额异常", secondVo.getAmountText());
    }

    @Test
    @DisplayName("格式化器抛出异常时应按固定文案回退")
    void testTranslate_FormatterThrows_ShouldUseFixedTextFallback() {
        TranslateEngine engine = buildEngine(
                new EnumLabelResolver(),
                Collections.emptyList(),
                List.of(new ThrowingTextFormatter())
        );

        ThrowingFormatterVo vo = new ThrowingFormatterVo().setAmount(new BigDecimal("10"));
        engine.translate(vo, new TranslateContext());

        assertEquals("格式化失败", vo.getAmountText());
    }

    @Test
    @DisplayName("业务解析器返回非字符串值时应转换为字符串")
    void testTranslate_BizResolverReturnNonStringValue_ShouldConvertToString() {
        TranslateEngine engine = buildEngine(new EnumLabelResolver(), List.of(new NumberValueUserResolver()));

        NumberValueResolverVo vo = new NumberValueResolverVo().setUserId(66);
        engine.translate(vo, new TranslateContext());

        assertEquals("1066", vo.getUserName());
    }

    private TranslateEngine buildEngine(EnumLabelResolver enumLabelResolver, List<BatchLabelResolver<?>> resolverList) {
        return buildEngine(enumLabelResolver, resolverList, Collections.emptyList());
    }

    private TranslateEngine buildEngine(EnumLabelResolver enumLabelResolver,
                                        List<BatchLabelResolver<?>> resolverList,
                                        List<FieldTextFormatter> formatterList) {
        return new TranslateEngine(new TranslateMetadataCache(), enumLabelResolver, resolverList, formatterList);
    }

    private enum TestStatusEnum implements CodeEnum<Integer> {
        DISABLED(0, "停用"),
        ENABLED(1, "启用");

        private final Integer code;
        private final String info;

        TestStatusEnum(Integer code, String info) {
            this.code = code;
            this.info = info;
        }

        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public String getInfo() {
            return info;
        }
    }

    @Data
    @Accessors(chain = true)
    private static class TestVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        private String statusName;

        private Integer userId;

        @BizLabel(source = "userId", resolver = TestUserResolver.class)
        private String userName;
    }

    @Data
    @Accessors(chain = true)
    private static class FallbackVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class, fallback = TranslateFallbackEnum.RAW_VALUE)
        private String statusName;
    }

    @Data
    @Accessors(chain = true)
    private static class MissingResolverVo {
        private Integer userId;

        @BizLabel(source = "userId", resolver = MissingResolver.class,
                fallback = TranslateFallbackEnum.FIXED_TEXT, fallbackText = "未知用户")
        private String userName;
    }

    @Data
    @Accessors(chain = true)
    private static class NullMapResolverVo {
        private Integer userId;

        @BizLabel(source = "userId", resolver = NullResultUserResolver.class,
                fallback = TranslateFallbackEnum.FIXED_TEXT, fallbackText = "兜底用户")
        private String userName;
    }

    @Data
    @Accessors(chain = true)
    private static class NullSourceFallbackVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class,
                whenNullSkip = false, fallback = TranslateFallbackEnum.FIXED_TEXT, fallbackText = "未知状态")
        private String statusName;
    }

    @Data
    @Accessors(chain = true)
    private static class UnsupportedTargetTypeVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        private Integer statusName;
    }

    @Data
    @Accessors(chain = true)
    private static class EnumFixedFallbackVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class,
                fallback = TranslateFallbackEnum.FIXED_TEXT, fallbackText = "枚举兜底")
        private String statusName;
    }

    @Data
    @Accessors(chain = true)
    private static class NoTranslateVo {
        private String name;
    }

    @Data
    @Accessors(chain = true)
    private static class WhenNullSkipVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class,
                whenNullSkip = true, fallback = TranslateFallbackEnum.FIXED_TEXT, fallbackText = "不会执行")
        private String statusName;
    }

    @Data
    @Accessors(chain = true)
    private static class EmptyMapResolverVo {
        private Integer userId;

        @BizLabel(source = "userId", resolver = EmptyResultUserResolver.class,
                fallback = TranslateFallbackEnum.FIXED_TEXT, fallbackText = "空映射兜底")
        private String userName;
    }

    @Data
    @Accessors(chain = true)
    private static class NullValueResolverVo {
        private Integer userId;

        @BizLabel(source = "userId", resolver = NullValueUserResolver.class)
        private String userName;
    }

    @Data
    @Accessors(chain = true)
    private static class NullSourceRawFallbackVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class,
                whenNullSkip = false, fallback = TranslateFallbackEnum.RAW_VALUE)
        private String statusName;
    }

    @Data
    @Accessors(chain = true)
    private static class CharSequenceTargetVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        private CharSequence statusName;
    }

    @Data
    @Accessors(chain = true)
    private static class ObjectTargetVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        private Object statusName;
    }

    @Data
    @Accessors(chain = true)
    private static class MoneyFormatVo {
        private BigDecimal amount;

        @FormatText(source = "amount", formatter = MoneyScale2TextFormatter.class)
        private String amountText;
    }

    @Data
    @Accessors(chain = true)
    private static class NestedContainerVo {
        @TranslateChild
        private List<NestedChildItemVo> childList;
    }

    @Data
    @Accessors(chain = true)
    private static class NestedChildItemVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        private String statusName;

        private BigDecimal amount;

        @FormatText(source = "amount", formatter = MoneyScale2TextFormatter.class)
        private String amountText;
    }

    @Data
    @Accessors(chain = true)
    private static class NestedSingleContainerVo {
        @TranslateChild
        private TestVo child;
    }

    @Data
    @Accessors(chain = true)
    private static class RecursiveNodeVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        private String statusName;

        @TranslateChild
        private RecursiveNodeVo child;
    }

    @Data
    @Accessors(chain = true)
    private static class MissingFormatterVo {
        private BigDecimal amount;

        @FormatText(source = "amount", formatter = MissingTextFormatter.class,
                fallback = TranslateFallbackEnum.FIXED_TEXT, fallbackText = "金额异常")
        private String amountText;
    }

    @Data
    @Accessors(chain = true)
    private static class ThrowingFormatterVo {
        private BigDecimal amount;

        @FormatText(source = "amount", formatter = ThrowingTextFormatter.class,
                fallback = TranslateFallbackEnum.FIXED_TEXT, fallbackText = "格式化失败")
        private String amountText;
    }

    @Data
    @Accessors(chain = true)
    private static class NumberValueResolverVo {
        private Integer userId;

        @BizLabel(source = "userId", resolver = NumberValueUserResolver.class)
        private String userName;
    }

    private static class TestUserResolver implements BatchLabelResolver<Integer> {
        private final AtomicInteger invokeCount = new AtomicInteger(0);
        private Set<Integer> latestKeys = Collections.emptySet();
        private TranslateContext latestContext;

        @Override
        public Map<Integer, String> resolveBatch(Set<Integer> keys, TranslateContext context) {
            invokeCount.incrementAndGet();
            latestKeys = new LinkedHashSet<>(keys);
            latestContext = context;
            Map<Integer, String> result = new HashMap<>();
            for (Integer key : keys) {
                result.put(key, "用户-" + key);
            }
            return result;
        }
    }

    private static class MissingResolver implements BatchLabelResolver<Integer> {
        @Override
        public Map<Integer, String> resolveBatch(Set<Integer> keys, TranslateContext context) {
            return Collections.emptyMap();
        }
    }

    private static class NullResultUserResolver implements BatchLabelResolver<Integer> {
        @Override
        public Map<Integer, String> resolveBatch(Set<Integer> keys, TranslateContext context) {
            return null;
        }
    }

    private static class EmptyResultUserResolver implements BatchLabelResolver<Integer> {
        @Override
        public Map<Integer, String> resolveBatch(Set<Integer> keys, TranslateContext context) {
            return Collections.emptyMap();
        }
    }

    private static class NullValueUserResolver implements BatchLabelResolver<Integer> {
        @Override
        public Map<Integer, String> resolveBatch(Set<Integer> keys, TranslateContext context) {
            Map<Integer, String> result = new HashMap<>();
            for (Integer key : keys) {
                result.put(key, null);
            }
            return result;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static class NumberValueUserResolver implements BatchLabelResolver<Integer> {
        @Override
        public Map<Integer, String> resolveBatch(Set<Integer> keys, TranslateContext context) {
            Map rawResult = new HashMap();
            for (Integer key : keys) {
                rawResult.put(key, key + 1000);
            }
            return (Map<Integer, String>) rawResult;
        }
    }

    private static class NullEnumLabelResolver extends EnumLabelResolver {
        @Override
        public Map<Object, String> resolveBatch(Set<Object> keys,
                                                Class<? extends Enum<?>> enumClass,
                                                TranslateContext context) {
            return null;
        }
    }

    private static class MissingTextFormatter implements FieldTextFormatter {
        @Override
        public String format(Object sourceValue, TranslateContext context) {
            return "不会被调用";
        }
    }

    private static class ThrowingTextFormatter implements FieldTextFormatter {
        @Override
        public String format(Object sourceValue, TranslateContext context) {
            throw new IllegalStateException("mock formatter error");
        }
    }
}
