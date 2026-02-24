package info.zhihui.ems.components.translate.annotation;

import info.zhihui.ems.components.translate.formatter.FieldTextFormatter;

import java.lang.annotation.*;

/**
 * 标记本地格式化展示字段转换规则
 * <p>
 * 用于金额/时间等本地纯格式化场景，不应在格式化器中执行数据库或远程查询。
 * 如需批量业务主键转名称，请使用 {@code @BizLabel}。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormatText {

    /**
     * 源字段名（通常为金额/时间等原始字段）
     */
    String source();

    /**
     * 格式化器实现类型（仅做本地纯格式化）
     */
    Class<? extends FieldTextFormatter> formatter();

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
