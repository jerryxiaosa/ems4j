package info.zhihui.ems.components.translate.annotation;

import java.lang.annotation.*;

/**
 * 标记需要递归执行响应转换的子字段。
 * <p>
 * 当前仅用于显式声明子对象/子集合递归转换入口，避免引擎无边界扫描整个对象图。
 * 支持字段类型：
 * 1. 普通对象（VO）
 * 2. {@link java.util.Collection} 子类
 * 3. {@link info.zhihui.ems.common.paging.PageResult}
 * <p>
 * 不支持类型（会告警并跳过）：
 * 1. {@link java.util.Map}
 * 2. 数组
 * 3. {@link java.util.Optional}
 * 4. 字符串/数字/布尔/枚举等简单值类型
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TranslateChild {
}
