package info.zhihui.ems.components.translate.engine;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.components.translate.annotation.BizLabel;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import info.zhihui.ems.components.translate.annotation.TranslateFallbackEnum;
import info.zhihui.ems.components.translate.resolver.BatchLabelResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TranslateMetadataCache测试")
class TranslateMetadataCacheTest {

    @Test
    @DisplayName("传入null类应返回空元数据")
    void testGetMetadata_NullClass_ShouldReturnEmptyMetadata() {
        TranslateMetadataCache cache = new TranslateMetadataCache();

        TranslateMetadata metadata = cache.getMetadata(null);

        assertSame(TranslateMetadata.EMPTY, metadata);
        assertTrue(metadata.isEmpty());
    }

    @Test
    @DisplayName("无注解类应返回空元数据")
    void testGetMetadata_NoAnnotation_ShouldReturnEmptyMetadata() {
        TranslateMetadataCache cache = new TranslateMetadataCache();

        TranslateMetadata metadata = cache.getMetadata(NoAnnotationVo.class);

        assertTrue(metadata.isEmpty());
    }

    @Test
    @DisplayName("应正确构建枚举和业务转换元数据")
    void testGetMetadata_ValidFields_ShouldBuildEnumAndBizMetadata() {
        TranslateMetadataCache cache = new TranslateMetadataCache();

        TranslateMetadata metadata = cache.getMetadata(StandardVo.class);

        assertFalse(metadata.isEmpty());
        assertEquals(2, metadata.getFieldList().size());

        TranslateFieldMetadata enumMetadata = metadata.getFieldList().stream()
                .filter(item -> "statusName".equals(item.getTargetField().getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(enumMetadata);
        assertEquals(TranslateFieldTypeEnum.ENUM, enumMetadata.getType());
        assertEquals("status", enumMetadata.getSourceField().getName());
        assertEquals(TestStatusEnum.class, enumMetadata.getEnumClass());

        TranslateFieldMetadata bizMetadata = metadata.getFieldList().stream()
                .filter(item -> "userName".equals(item.getTargetField().getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(bizMetadata);
        assertEquals(TranslateFieldTypeEnum.BIZ, bizMetadata.getType());
        assertEquals("userId", bizMetadata.getSourceField().getName());
        assertEquals(TestUserResolver.class, bizMetadata.getResolverClass());
    }

    @Test
    @DisplayName("静态字段注解应被忽略")
    void testGetMetadata_StaticTargetField_ShouldSkip() {
        TranslateMetadataCache cache = new TranslateMetadataCache();

        TranslateMetadata metadata = cache.getMetadata(StaticTargetFieldVo.class);

        assertTrue(metadata.isEmpty());
    }

    @Test
    @DisplayName("源字段缺失时应忽略该转换声明")
    void testGetMetadata_MissingSourceField_ShouldSkip() {
        TranslateMetadataCache cache = new TranslateMetadataCache();

        TranslateMetadata metadata = cache.getMetadata(MissingSourceFieldVo.class);

        assertTrue(metadata.isEmpty());
    }

    @Test
    @DisplayName("业务注解源字段缺失时应忽略")
    void testGetMetadata_MissingBizSourceField_ShouldSkip() {
        TranslateMetadataCache cache = new TranslateMetadataCache();

        TranslateMetadata metadata = cache.getMetadata(MissingBizSourceFieldVo.class);

        assertTrue(metadata.isEmpty());
    }

    @Test
    @DisplayName("同一字段同时声明两种注解时应忽略")
    void testGetMetadata_DualAnnotation_ShouldSkip() {
        TranslateMetadataCache cache = new TranslateMetadataCache();

        TranslateMetadata metadata = cache.getMetadata(DualAnnotationVo.class);

        assertTrue(metadata.isEmpty());
    }

    @Test
    @DisplayName("应支持读取父类源字段")
    void testGetMetadata_InheritedSourceField_ShouldBuildMetadata() {
        TranslateMetadataCache cache = new TranslateMetadataCache();

        TranslateMetadata metadata = cache.getMetadata(InheritedVo.class);

        assertFalse(metadata.isEmpty());
        assertEquals(1, metadata.getFieldList().size());
        TranslateFieldMetadata fieldMetadata = metadata.getFieldList().get(0);
        assertEquals("status", fieldMetadata.getSourceField().getName());
        assertEquals(ParentVo.class, fieldMetadata.getSourceField().getDeclaringClass());
    }

    @Test
    @DisplayName("同一类多次读取应命中缓存")
    void testGetMetadata_SameClass_ShouldHitCache() {
        TranslateMetadataCache cache = new TranslateMetadataCache();

        TranslateMetadata firstMetadata = cache.getMetadata(StandardVo.class);
        TranslateMetadata secondMetadata = cache.getMetadata(StandardVo.class);

        assertSame(firstMetadata, secondMetadata);
    }

    @Test
    @DisplayName("子类与父类同名字段应优先使用子类字段")
    void testGetMetadata_ShadowedSourceField_ShouldPreferChildField() {
        TranslateMetadataCache cache = new TranslateMetadataCache();

        TranslateMetadata metadata = cache.getMetadata(ShadowFieldChildVo.class);

        assertFalse(metadata.isEmpty());
        assertEquals(1, metadata.getFieldList().size());
        TranslateFieldMetadata fieldMetadata = metadata.getFieldList().get(0);
        assertEquals("status", fieldMetadata.getSourceField().getName());
        assertEquals(ShadowFieldChildVo.class, fieldMetadata.getSourceField().getDeclaringClass());
    }

    @Test
    @DisplayName("应保留注解中的回退配置")
    void testGetMetadata_FallbackConfig_ShouldKeepAnnotationValues() {
        TranslateMetadataCache cache = new TranslateMetadataCache();

        TranslateMetadata metadata = cache.getMetadata(FallbackConfigVo.class);

        assertFalse(metadata.isEmpty());
        assertEquals(2, metadata.getFieldList().size());

        TranslateFieldMetadata enumMetadata = metadata.getFieldList().stream()
                .filter(item -> "statusName".equals(item.getTargetField().getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(enumMetadata);
        assertFalse(enumMetadata.isWhenNullSkip());
        assertEquals(TranslateFallbackEnum.RAW_VALUE, enumMetadata.getFallback());
        assertEquals("", enumMetadata.getFallbackText());

        TranslateFieldMetadata bizMetadata = metadata.getFieldList().stream()
                .filter(item -> "userName".equals(item.getTargetField().getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(bizMetadata);
        assertFalse(bizMetadata.isWhenNullSkip());
        assertEquals(TranslateFallbackEnum.FIXED_TEXT, bizMetadata.getFallback());
        assertEquals("默认用户", bizMetadata.getFallbackText());
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

    private static class StandardVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        private String statusName;

        private Integer userId;

        @BizLabel(source = "userId", resolver = TestUserResolver.class)
        private String userName;
    }

    private static class NoAnnotationVo {
        private Integer status;
        private String statusName;
    }

    private static class StaticTargetFieldVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        private static String statusName;
    }

    private static class MissingSourceFieldVo {
        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        private String statusName;
    }

    private static class MissingBizSourceFieldVo {
        @BizLabel(source = "userId", resolver = TestUserResolver.class)
        private String userName;
    }

    private static class DualAnnotationVo {
        private Integer status;
        private Integer userId;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        @BizLabel(source = "userId", resolver = TestUserResolver.class)
        private String mixedName;
    }

    private static class ParentVo {
        private Integer status;
    }

    private static class InheritedVo extends ParentVo {
        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        private String statusName;
    }

    private static class ShadowFieldParentVo {
        private Integer status;
    }

    private static class ShadowFieldChildVo extends ShadowFieldParentVo {
        private Integer status;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class)
        private String statusName;
    }

    private static class FallbackConfigVo {
        private Integer status;
        private Integer userId;

        @EnumLabel(source = "status", enumClass = TestStatusEnum.class,
                whenNullSkip = false, fallback = TranslateFallbackEnum.RAW_VALUE)
        private String statusName;

        @BizLabel(source = "userId", resolver = TestUserResolver.class,
                whenNullSkip = false, fallback = TranslateFallbackEnum.FIXED_TEXT, fallbackText = "默认用户")
        private String userName;
    }

    private static class TestUserResolver implements BatchLabelResolver<Integer> {
        @Override
        public Map<Integer, String> resolveBatch(Set<Integer> keys, TranslateContext context) {
            return Collections.emptyMap();
        }
    }
}
