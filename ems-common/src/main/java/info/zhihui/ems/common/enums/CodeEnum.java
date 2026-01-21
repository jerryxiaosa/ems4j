package info.zhihui.ems.common.enums;

/**
 * @author jerryxiaosa
 */
public interface CodeEnum<T> {
    T getCode();

    /**
     * 通用方法：根据 code 查找匹配的枚举实例
     *
     * @param code      要查找的 code 值
     * @param enumClass 枚举类型
     * @param <E>       实现了 CodeEnum 的枚举类型
     * @return 匹配的枚举实例，未找到返回 null
     */
    static <E extends Enum<E> & CodeEnum<T>, T> E fromCode(T code, Class<E> enumClass) {
        for (E enumConstant : enumClass.getEnumConstants()) {
            if ((code == null && enumConstant.getCode() == null) ||
                    (code != null && code.equals(enumConstant.getCode()))) {
                return enumConstant;
            }
        }
        return null;
    }

    // 统一信息接口：默认返回枚举名称，只有 code 时可用作 info
    default String getInfo() {
        return ((Enum<?>) this).name();
    }
}
