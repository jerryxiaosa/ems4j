package info.zhihui.ems.components.translate.annotation;

import info.zhihui.ems.components.translate.resolver.BatchLabelResolver;

import java.lang.annotation.*;

/**
 * 标记业务主键展示字段转换规则
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BizLabel {

    /**
     * 源字段名（通常为 id 字段）
     */
    String source();

    /**
     * 业务解析器实现类型
     */
    Class<? extends BatchLabelResolver<?>> resolver();

    /**
     * 源字段为空时是否跳过
     */
    boolean whenNullSkip() default true;

    /**
     * 回退策略
     */
    TranslateFallbackEnum fallback() default TranslateFallbackEnum.NULL;

    /**
     * fallback=FIXED_TEXT 时生效
     */
    String fallbackText() default "";
}

