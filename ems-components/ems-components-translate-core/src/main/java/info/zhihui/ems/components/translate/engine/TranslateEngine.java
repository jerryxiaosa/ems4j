package info.zhihui.ems.components.translate.engine;

import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.components.translate.annotation.TranslateFallbackEnum;
import info.zhihui.ems.components.translate.formatter.FieldTextFormatter;
import info.zhihui.ems.components.translate.resolver.BatchLabelResolver;
import info.zhihui.ems.components.translate.resolver.EnumLabelResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 响应对象转换引擎
 * <p>
 * 设计要点：
 * 1. 统一入口：支持单对象、列表、分页列表。
 * 2. 批量解析：先收集key再批量查询，避免N+1。
 * 3. 失败降级：转换失败不抛出业务异常，仅记录日志并按fallback回填。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TranslateEngine {

    private final TranslateMetadataCache metadataCache;
    private final EnumLabelResolver enumLabelResolver;
    private final List<BatchLabelResolver<?>> resolverList;
    private final List<FieldTextFormatter> formatterList;
    private final ConcurrentMap<Class<? extends FieldTextFormatter>, FieldTextFormatter> formatterCache = new ConcurrentHashMap<>();
    private final Set<Class<? extends FieldTextFormatter>> missingFormatterClassSet = ConcurrentHashMap.newKeySet();

    /**
     * 对任意对象执行展示字段转换
     * <p>
     * 总流程：
     * 1) 拉平数据结构（单对象/集合/分页）。
     * 2) 构建待处理任务与批量key。
     * 3) 按枚举与业务resolver分别批量解析。
     * 4) 回填目标字段。
     */
    public void translate(Object data, TranslateContext context) {
        if (data == null) {
            return;
        }

        TranslateContext useContext = context == null ? new TranslateContext() : context;
        List<Object> targetList = flatten(data);
        if (targetList.isEmpty()) {
            return;
        }

        List<TranslateTask> taskList = new ArrayList<>();
        Map<Class<? extends Enum<?>>, Set<Object>> enumKeyMap = new LinkedHashMap<>();
        Map<Class<? extends BatchLabelResolver<?>>, Set<Object>> bizKeyMap = new LinkedHashMap<>();

        collectTasks(targetList, taskList, enumKeyMap, bizKeyMap);
        if (taskList.isEmpty()) {
            return;
        }

        Map<Class<? extends Enum<?>>, Map<Object, String>> enumLabelMap = resolveEnumLabels(enumKeyMap, useContext);
        Map<Class<? extends BatchLabelResolver<?>>, Map<Object, String>> bizLabelMap = resolveBizLabels(bizKeyMap, useContext);

        fillTargetField(taskList, enumLabelMap, bizLabelMap, useContext);
    }

    /**
     * 将返回体统一拉平成对象列表，便于后续同一流程处理。
     */
    private List<Object> flatten(Object data) {
        if (data instanceof PageResult<?> pageResult) {
            if (pageResult.getList() == null || pageResult.getList().isEmpty()) {
                return Collections.emptyList();
            }
            return new ArrayList<>(pageResult.getList());
        }
        if (data instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                return Collections.emptyList();
            }
            return new ArrayList<>(collection);
        }
        return Collections.singletonList(data);
    }

    /**
     * 扫描对象与注解元数据，生成转换任务，并同时聚合批量查询key。
     */
    private void collectTasks(List<Object> targetList,
                              List<TranslateTask> taskList,
                              Map<Class<? extends Enum<?>>, Set<Object>> enumKeyMap,
                              Map<Class<? extends BatchLabelResolver<?>>, Set<Object>> bizKeyMap) {
        for (Object target : targetList) {
            if (target == null) {
                continue;
            }
            TranslateMetadata metadata = metadataCache.getMetadata(target.getClass());
            if (metadata.isEmpty()) {
                continue;
            }
            for (TranslateFieldMetadata fieldMetadata : metadata.getFieldList()) {
                Object sourceValue = getFieldValue(fieldMetadata.getSourceField(), target);
                if (sourceValue == null && fieldMetadata.isWhenNullSkip()) {
                    continue;
                }

                taskList.add(new TranslateTask(target, fieldMetadata, sourceValue));
                if (sourceValue == null) {
                    continue;
                }

                if (fieldMetadata.getType() == TranslateFieldTypeEnum.ENUM) {
                    // LinkedHashSet 去重且保持稳定顺序，便于排查与测试断言
                    enumKeyMap.computeIfAbsent(fieldMetadata.getEnumClass(), ignored -> new LinkedHashSet<>()).add(sourceValue);
                    continue;
                }
                if (fieldMetadata.getType() == TranslateFieldTypeEnum.BIZ) {
                    bizKeyMap.computeIfAbsent(fieldMetadata.getResolverClass(), ignored -> new LinkedHashSet<>()).add(sourceValue);
                }
            }
        }
    }

    /**
     * 批量解析所有枚举字段的展示值。
     */
    private Map<Class<? extends Enum<?>>, Map<Object, String>> resolveEnumLabels(Map<Class<? extends Enum<?>>, Set<Object>> enumKeyMap,
                                                                                  TranslateContext context) {
        Map<Class<? extends Enum<?>>, Map<Object, String>> result = new HashMap<>();
        for (Map.Entry<Class<? extends Enum<?>>, Set<Object>> entry : enumKeyMap.entrySet()) {
            Map<Object, String> labels = enumLabelResolver.resolveBatch(entry.getValue(), entry.getKey(), context);
            result.put(entry.getKey(), labels == null ? Collections.emptyMap() : labels);
        }
        return result;
    }

    /**
     * 批量解析所有业务字段的展示值。
     */
    private Map<Class<? extends BatchLabelResolver<?>>, Map<Object, String>> resolveBizLabels(
            Map<Class<? extends BatchLabelResolver<?>>, Set<Object>> bizKeyMap,
            TranslateContext context) {
        Map<Class<? extends BatchLabelResolver<?>>, Map<Object, String>> result = new HashMap<>();
        for (Map.Entry<Class<? extends BatchLabelResolver<?>>, Set<Object>> entry : bizKeyMap.entrySet()) {
            Class<? extends BatchLabelResolver<?>> resolverClass = entry.getKey();
            BatchLabelResolver<?> resolver = findResolver(resolverClass);
            if (resolver == null) {
                log.warn("未找到业务解析器实现: {}", resolverClass.getName());
                result.put(resolverClass, Collections.emptyMap());
                continue;
            }

            Map<Object, String> labelMap = executeResolve(resolver, entry.getValue(), context);
            result.put(resolverClass, labelMap);
        }
        return result;
    }

    /**
     * 执行业务resolver并统一转换为 Map<Object, String>，屏蔽泛型差异。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Map<Object, String> executeResolve(BatchLabelResolver<?> resolver, Set<Object> keys, TranslateContext context) {
        Map resolveResult = ((BatchLabelResolver) resolver).resolveBatch(keys, context);
        if (resolveResult == null || resolveResult.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Object, String> result = new HashMap<>();
        for (Object key : resolveResult.keySet()) {
            Object value = resolveResult.get(key);
            result.put(key, value == null ? null : String.valueOf(value));
        }
        return result;
    }

    /**
     * 从容器注入列表中匹配目标resolver。
     */
    private BatchLabelResolver<?> findResolver(Class<? extends BatchLabelResolver<?>> resolverClass) {
        for (BatchLabelResolver<?> resolver : resolverList) {
            if (resolverClass.isAssignableFrom(resolver.getClass())) {
                return resolver;
            }
        }
        return null;
    }

    /**
     * 根据解析结果回填目标字段；未命中时执行fallback策略。
     */
    private void fillTargetField(List<TranslateTask> taskList,
                                 Map<Class<? extends Enum<?>>, Map<Object, String>> enumLabelMap,
                                 Map<Class<? extends BatchLabelResolver<?>>, Map<Object, String>> bizLabelMap,
                                 TranslateContext context) {
        for (TranslateTask task : taskList) {
            TranslateFieldMetadata metadata = task.fieldMetadata();
            Object sourceValue = task.sourceValue();
            String value = resolveValue(task, enumLabelMap, bizLabelMap, context);
            if (value == null) {
                value = fallback(metadata, sourceValue);
            }
            setFieldValue(metadata.getTargetField(), task.target(), value);
        }
    }

    private String resolveValue(TranslateTask task,
                                Map<Class<? extends Enum<?>>, Map<Object, String>> enumLabelMap,
                                Map<Class<? extends BatchLabelResolver<?>>, Map<Object, String>> bizLabelMap,
                                TranslateContext context) {
        TranslateFieldMetadata metadata = task.fieldMetadata();
        Object sourceValue = task.sourceValue();
        if (sourceValue == null) {
            return null;
        }
        if (metadata.getType() == TranslateFieldTypeEnum.ENUM) {
            Map<Object, String> map = enumLabelMap.get(metadata.getEnumClass());
            return map == null ? null : map.get(sourceValue);
        }
        if (metadata.getType() == TranslateFieldTypeEnum.FORMAT) {
            FieldTextFormatter formatter = findFormatter(metadata.getFormatterClass());
            if (formatter == null) {
                return null;
            }
            try {
                return formatter.format(sourceValue, context);
            } catch (RuntimeException e) {
                log.warn("格式化执行失败: {}", metadata.getFormatterClass().getName(), e);
                return null;
            }
        }
        Map<Object, String> map = bizLabelMap.get(metadata.getResolverClass());
        return map == null ? null : map.get(sourceValue);
    }

    /**
     * 从容器注入列表中匹配目标格式化器。
     */
    private FieldTextFormatter findFormatter(Class<? extends FieldTextFormatter> formatterClass) {
        FieldTextFormatter cachedFormatter = formatterCache.get(formatterClass);
        if (cachedFormatter != null) {
            return cachedFormatter;
        }
        if (missingFormatterClassSet.contains(formatterClass)) {
            return null;
        }
        for (FieldTextFormatter formatter : formatterList) {
            if (formatterClass.isAssignableFrom(formatter.getClass())) {
                formatterCache.putIfAbsent(formatterClass, formatter);
                return formatter;
            }
        }
        if (missingFormatterClassSet.add(formatterClass)) {
            log.warn("未找到格式化器实现: {}", formatterClass.getName());
        }
        return null;
    }

    /**
     * 统一回退策略：
     * NULL -> null
     * RAW_VALUE -> 源值字符串
     * FIXED_TEXT -> 固定文案
     */
    private String fallback(TranslateFieldMetadata metadata, Object sourceValue) {
        TranslateFallbackEnum fallback = metadata.getFallback();
        if (fallback == null || fallback == TranslateFallbackEnum.NULL) {
            return null;
        }
        if (fallback == TranslateFallbackEnum.RAW_VALUE) {
            return sourceValue == null ? null : String.valueOf(sourceValue);
        }
        return metadata.getFallbackText();
    }

    /**
     * 反射读取字段，读取失败时返回null并记日志，不中断主流程。
     */
    private Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            log.warn("读取字段失败: {}.{}", target.getClass().getName(), field.getName(), e);
            return null;
        }
    }

    /**
     * 反射写回字段，仅支持字符串类字段，避免把展示值写入非预期类型。
     */
    private void setFieldValue(Field field, Object target, Object value) {
        Class<?> fieldType = field.getType();
        if (!(fieldType == String.class || fieldType == Object.class || CharSequence.class.isAssignableFrom(fieldType))) {
            log.warn("目标字段类型不支持转换结果写入: {}.{}", target.getClass().getName(), field.getName());
            return;
        }

        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            log.warn("写入字段失败: {}.{}", target.getClass().getName(), field.getName(), e);
        }
    }

    /**
     * 单条转换任务：
     * target      -> 目标对象
     * fieldMetadata -> 注解元信息
     * sourceValue -> 源字段值
     */
    private record TranslateTask(Object target, TranslateFieldMetadata fieldMetadata, Object sourceValue) {
    }
}
