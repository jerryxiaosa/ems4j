package info.zhihui.ems.components.translate.formatter;

import info.zhihui.ems.components.translate.engine.TranslateContext;

/**
 * 本地字段字符串格式化器
 * <p>
 * 职责边界：
 * 1. 仅做本地纯格式化（金额、时间、百分比等）。
 * 2. 不应访问数据库、远程服务或执行其他外部IO。
 * 3. 需要批量查询展示值时请使用 {@code BatchLabelResolver} + {@code @BizLabel}。
 */
public interface FieldTextFormatter {

    /**
     * 将源字段值格式化为展示字符串
     *
     * @param sourceValue 源字段值
     * @param context     转换上下文
     * @return 展示字符串
     */
    String format(Object sourceValue, TranslateContext context);
}
