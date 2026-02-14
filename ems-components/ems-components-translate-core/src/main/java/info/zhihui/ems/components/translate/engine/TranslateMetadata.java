package info.zhihui.ems.components.translate.engine;

import lombok.Getter;

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

    static final TranslateMetadata EMPTY = new TranslateMetadata(Collections.emptyList());

    private final List<TranslateFieldMetadata> fieldList;

    TranslateMetadata(List<TranslateFieldMetadata> fieldList) {
        this.fieldList = fieldList;
    }

    /**
     * 当前对象类型是否没有可执行的转换字段。
     */
    boolean isEmpty() {
        return fieldList == null || fieldList.isEmpty();
    }
}
