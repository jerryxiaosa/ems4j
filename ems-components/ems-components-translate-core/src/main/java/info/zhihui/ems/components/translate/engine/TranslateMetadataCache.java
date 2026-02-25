package info.zhihui.ems.components.translate.engine;

import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.components.translate.annotation.BizLabel;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import info.zhihui.ems.components.translate.annotation.FormatText;
import info.zhihui.ems.components.translate.annotation.TranslateChild;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 转换元数据缓存
 * <p>
 * 作用：
 * 1. 把“反射扫描 + 注解解析”成本前置为一次构建。
 * 2. 后续同类型对象复用元数据，降低运行期开销。
 */
@Slf4j
@Component
public class TranslateMetadataCache {

    private final ConcurrentMap<Class<?>, TranslateMetadata> cache = new ConcurrentHashMap<>();

    /**
     * 获取指定类型的元数据；首次访问时构建并写入缓存。
     */
    TranslateMetadata getMetadata(Class<?> clazz) {
        if (clazz == null) {
            return TranslateMetadata.EMPTY;
        }
        return cache.computeIfAbsent(clazz, this::buildMetadata);
    }

    /**
     * 扫描类字段并生成转换元信息。
     * <p>
     * 规则：
     * 1. 处理声明了 @EnumLabel / @BizLabel / @FormatText 的目标字段。
     * 2. 处理声明了 @TranslateChild 的递归子字段。
     * 3. 同一字段同时声明多个转换注解时跳过并告警。
     * 4. 找不到 source 字段时跳过并告警。
     */
    private TranslateMetadata buildMetadata(Class<?> clazz) {
        List<Field> fieldList = collectFieldList(clazz);
        if (fieldList.isEmpty()) {
            return TranslateMetadata.EMPTY;
        }

        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : fieldList) {
            fieldMap.putIfAbsent(field.getName(), field);
        }

        List<TranslateFieldMetadata> result = new ArrayList<>();
        List<Field> childFieldList = new ArrayList<>();
        for (Field targetField : fieldList) {
            // 静态字段不参与实例对象的响应转换
            if (Modifier.isStatic(targetField.getModifiers())) {
                continue;
            }

            TranslateChild translateChild = targetField.getAnnotation(TranslateChild.class);
            if (translateChild != null) {
                if (isSupportedTranslateChildFieldType(targetField.getType())) {
                    targetField.setAccessible(true);
                    childFieldList.add(targetField);
                } else {
                    log.warn("@TranslateChild字段{}类型不受支持，仅支持普通对象/Collection/PageResult，已跳过。type={}",
                            buildFieldName(clazz, targetField), targetField.getType().getName());
                }
            }

            EnumLabel enumLabel = targetField.getAnnotation(EnumLabel.class);
            BizLabel bizLabel = targetField.getAnnotation(BizLabel.class);
            FormatText formatText = targetField.getAnnotation(FormatText.class);
            if (enumLabel == null && bizLabel == null && formatText == null) {
                continue;
            }

            int annotationCount = (enumLabel == null ? 0 : 1) + (bizLabel == null ? 0 : 1) + (formatText == null ? 0 : 1);
            if (annotationCount > 1) {
                log.warn("字段{}同时声明多个转换注解，已跳过", buildFieldName(clazz, targetField));
                continue;
            }

            if (enumLabel != null) {
                Field sourceField = fieldMap.get(enumLabel.source());
                if (sourceField == null) {
                    log.warn("枚举字段{}找不到源字段{}，已跳过", buildFieldName(clazz, targetField), enumLabel.source());
                    continue;
                }
                // 统一在构建阶段放开反射访问，运行期避免重复设置
                sourceField.setAccessible(true);
                targetField.setAccessible(true);
                result.add(TranslateFieldMetadata.enumMetadata(
                        sourceField,
                        targetField,
                        enumLabel.enumClass(),
                        enumLabel.whenNullSkip(),
                        enumLabel.fallback(),
                        enumLabel.fallbackText()
                ));
                continue;
            }

            if (formatText != null) {
                Field sourceField = fieldMap.get(formatText.source());
                if (sourceField == null) {
                    log.warn("格式化字段{}找不到源字段{}，已跳过", buildFieldName(clazz, targetField), formatText.source());
                    continue;
                }
                sourceField.setAccessible(true);
                targetField.setAccessible(true);
                result.add(TranslateFieldMetadata.formatMetadata(
                        sourceField,
                        targetField,
                        formatText.formatter(),
                        formatText.whenNullSkip(),
                        formatText.fallback(),
                        formatText.fallbackText()
                ));
                continue;
            }

            Field sourceField = fieldMap.get(bizLabel.source());
            if (sourceField == null) {
                log.warn("业务字段{}找不到源字段{}，已跳过", buildFieldName(clazz, targetField), bizLabel.source());
                continue;
            }
            // 统一在构建阶段放开反射访问，运行期避免重复设置
            sourceField.setAccessible(true);
            targetField.setAccessible(true);
            result.add(TranslateFieldMetadata.bizMetadata(
                    sourceField,
                    targetField,
                    bizLabel.resolver(),
                    bizLabel.whenNullSkip(),
                    bizLabel.fallback(),
                    bizLabel.fallbackText()
            ));
        }

        if (result.isEmpty() && childFieldList.isEmpty()) {
            return TranslateMetadata.EMPTY;
        }

        return new TranslateMetadata(
                Collections.unmodifiableList(result),
                Collections.unmodifiableList(childFieldList)
        );
    }

    /**
     * 收集当前类及其父类字段，支持继承场景下的注解配置。
     */
    private List<Field> collectFieldList(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            result.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return result;
    }

    private String buildFieldName(Class<?> clazz, Field field) {
        return clazz.getName() + "." + field.getName();
    }

    /**
     * @TranslateChild 仅允许递归对象/集合/分页容器，避免误标简单值或复杂容器产生“看似生效、实际无效”的隐式行为。
     */
    private boolean isSupportedTranslateChildFieldType(Class<?> fieldType) {
        if (fieldType == null) {
            return false;
        }
        if (fieldType.isPrimitive() || fieldType.isArray() || fieldType.isEnum()) {
            return false;
        }
        if (Map.class.isAssignableFrom(fieldType) || Optional.class.isAssignableFrom(fieldType)) {
            return false;
        }
        if (PageResult.class.isAssignableFrom(fieldType) || Collection.class.isAssignableFrom(fieldType)) {
            return true;
        }
        return !isSimpleValueType(fieldType);
    }

    private boolean isSimpleValueType(Class<?> fieldType) {
        return CharSequence.class.isAssignableFrom(fieldType)
                || Number.class.isAssignableFrom(fieldType)
                || Boolean.class == fieldType
                || Character.class == fieldType
                || Class.class == fieldType;
    }
}
