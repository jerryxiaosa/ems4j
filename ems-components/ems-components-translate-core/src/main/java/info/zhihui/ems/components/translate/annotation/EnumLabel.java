package info.zhihui.ems.components.translate.annotation;

import java.lang.annotation.*;

/**
 * 标记枚举展示字段转换规则
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumLabel {

    /**
     * 源字段名（通常为 code 字段）
     */
    String source();

    /**
     * 枚举类型（需实现 CodeEnum）
     */
    Class<? extends Enum<?>> enumClass();

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

