package info.zhihui.ems.components.translate.resolver;

import info.zhihui.ems.components.translate.engine.TranslateContext;

import java.util.Map;
import java.util.Set;

/**
 * 批量标签解析器
 *
 * @param <K> key 类型
 */
public interface BatchLabelResolver<K> {

    /**
     * 批量解析 key 对应的展示值
     *
     * @param keys    需要解析的 key 集合
     * @param context 转换上下文
     * @return key -> label 映射
     */
    Map<K, String> resolveBatch(Set<K> keys, TranslateContext context);
}

