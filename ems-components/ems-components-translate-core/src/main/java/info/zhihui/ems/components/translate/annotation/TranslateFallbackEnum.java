package info.zhihui.ems.components.translate.annotation;

/**
 * 转换失败时的回退策略
 */
public enum TranslateFallbackEnum {
    /**
     * 不回填，保持 null
     */
    NULL,

    /**
     * 使用源字段原始值（toString）
     */
    RAW_VALUE,

    /**
     * 使用固定文案
     */
    FIXED_TEXT
}

