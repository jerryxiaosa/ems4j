package info.zhihui.ems.components.translate.engine;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.components.translate.annotation.BizLabel;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import info.zhihui.ems.components.translate.annotation.TranslateFallbackEnum;
import info.zhihui.ems.components.translate.resolver.BatchLabelResolver;
import info.zhihui.ems.components.translate.resolver.EnumLabelResolver;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    private TranslateEngine buildEngine(EnumLabelResolver enumLabelResolver, List<BatchLabelResolver<?>> resolverList) {
        return new TranslateEngine(new TranslateMetadataCache(), enumLabelResolver, resolverList);
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

    private static class NullEnumLabelResolver extends EnumLabelResolver {
        @Override
        public Map<Object, String> resolveBatch(Set<Object> keys,
                                                Class<? extends Enum<?>> enumClass,
                                                TranslateContext context) {
            return null;
        }
    }
}
