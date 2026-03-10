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
     * 源字段名（通常为金额/时间等原始字段）。
     * <p>
     * 当 {@link #self()} 为 {@code true} 时，建议显式声明为当前字段名；
     * 留空时会按当前字段处理，以兼容旧用法。
     */
    String source() default "";

    /**
     * 是否以当前字段自身作为 source。
     * <p>
     * 仅建议用于手机号/邮箱/证件号等本地字符串脱敏场景，不应用于枚举编码或业务主键的覆盖。
     */
    boolean self() default false;

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
