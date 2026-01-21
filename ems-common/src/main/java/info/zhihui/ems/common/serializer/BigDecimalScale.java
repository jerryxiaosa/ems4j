package info.zhihui.ems.common.serializer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定 BigDecimal 序列化时的小数位数。
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BigDecimalScale {

    /**
     * 保留的小数位，默认 2 位。
     */
    int scale() default 2;
}
