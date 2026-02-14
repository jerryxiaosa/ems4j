package info.zhihui.ems.components.translate.engine;

import info.zhihui.ems.components.translate.annotation.BizLabel;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
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
     * 1. 仅处理声明了 @EnumLabel / @BizLabel 的目标字段。
     * 2. 同一字段同时声明两个注解时跳过并告警。
     * 3. 找不到 source 字段时跳过并告警。
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
        for (Field targetField : fieldList) {
            // 静态字段不参与实例对象的响应转换
            if (Modifier.isStatic(targetField.getModifiers())) {
                continue;
            }

            EnumLabel enumLabel = targetField.getAnnotation(EnumLabel.class);
            BizLabel bizLabel = targetField.getAnnotation(BizLabel.class);
            if (enumLabel == null && bizLabel == null) {
                continue;
            }

            if (enumLabel != null && bizLabel != null) {
                log.warn("字段{}同时声明@EnumLabel和@BizLabel，已跳过", buildFieldName(clazz, targetField));
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

        if (result.isEmpty()) {
            return TranslateMetadata.EMPTY;
        }

        return new TranslateMetadata(Collections.unmodifiableList(result));
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
}
