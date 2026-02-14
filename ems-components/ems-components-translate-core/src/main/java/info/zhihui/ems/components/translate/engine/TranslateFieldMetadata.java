package info.zhihui.ems.components.translate.engine;

import info.zhihui.ems.components.translate.annotation.TranslateFallbackEnum;
import info.zhihui.ems.components.translate.resolver.BatchLabelResolver;
import lombok.Getter;

import java.lang.reflect.Field;

/**
 * 单字段转换元信息
 *
 * 说明：
 * 1. sourceField 读取源值（如 ownerType / userId）。
 * 2. targetField 回填展示值（如 ownerTypeName / userName）。
 * 3. enumClass 与 resolverClass 二选一，由 type 决定生效分支。
 */
@Getter
class TranslateFieldMetadata {

    private final TranslateFieldTypeEnum type;

    private final Field sourceField;

    private final Field targetField;

    private final Class<? extends Enum<?>> enumClass;

    private final Class<? extends BatchLabelResolver<?>> resolverClass;

    private final boolean whenNullSkip;

    private final TranslateFallbackEnum fallback;

    private final String fallbackText;

    private TranslateFieldMetadata(TranslateFieldTypeEnum type,
                                   Field sourceField,
                                   Field targetField,
                                   Class<? extends Enum<?>> enumClass,
                                   Class<? extends BatchLabelResolver<?>> resolverClass,
                                   boolean whenNullSkip,
                                   TranslateFallbackEnum fallback,
                                   String fallbackText) {
        this.type = type;
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.enumClass = enumClass;
        this.resolverClass = resolverClass;
        this.whenNullSkip = whenNullSkip;
        this.fallback = fallback;
        this.fallbackText = fallbackText;
    }

    /**
     * 构建枚举转换元信息
     */
    static TranslateFieldMetadata enumMetadata(Field sourceField,
                                               Field targetField,
                                               Class<? extends Enum<?>> enumClass,
                                               boolean whenNullSkip,
                                               TranslateFallbackEnum fallback,
                                               String fallbackText) {
        return new TranslateFieldMetadata(
                TranslateFieldTypeEnum.ENUM,
                sourceField,
                targetField,
                enumClass,
                null,
                whenNullSkip,
                fallback,
                fallbackText
        );
    }

    /**
     * 构建业务解析器转换元信息
     */
    static TranslateFieldMetadata bizMetadata(Field sourceField,
                                              Field targetField,
                                              Class<? extends BatchLabelResolver<?>> resolverClass,
                                              boolean whenNullSkip,
                                              TranslateFallbackEnum fallback,
                                              String fallbackText) {
        return new TranslateFieldMetadata(
                TranslateFieldTypeEnum.BIZ,
                sourceField,
                targetField,
                null,
                resolverClass,
                whenNullSkip,
                fallback,
                fallbackText
        );
    }
}
