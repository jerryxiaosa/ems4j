package info.zhihui.ems.components.translate.annotation;

import java.lang.annotation.*;

/**
 * 标记接口或控制器跳过响应转换
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipResponseTranslate {
}

