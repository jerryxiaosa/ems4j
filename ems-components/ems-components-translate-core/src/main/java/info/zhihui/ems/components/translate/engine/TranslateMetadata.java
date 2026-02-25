package info.zhihui.ems.components.translate.engine;

import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * 对象转换元信息
 *
 * 约束：
 * 1. EMPTY 作为无转换配置的共享对象，避免重复创建空实例。
 * 2. fieldList 由 TranslateMetadataCache 预构建并缓存。
 */
@Getter
class TranslateMetadata {

    static final TranslateMetadata EMPTY = new TranslateMetadata(Collections.emptyList(), Collections.emptyList());

    private final List<TranslateFieldMetadata> fieldList;

    /**
     * 显式声明递归转换入口的子字段列表（如 meterList）。
     */
    private final List<Field> childFieldList;

    TranslateMetadata(List<TranslateFieldMetadata> fieldList, List<Field> childFieldList) {
        this.fieldList = fieldList;
        this.childFieldList = childFieldList;
    }

    /**
     * 当前对象类型是否没有可执行的转换配置（字段转换 + 递归子字段）。
     */
    boolean isEmpty() {
        return (fieldList == null || fieldList.isEmpty())
                && (childFieldList == null || childFieldList.isEmpty());
    }
}
