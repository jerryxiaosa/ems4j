package info.zhihui.ems.common.utils;

/**
 * 查询值处理工具类。
 */
public final class QueryValueUtil {

    private QueryValueUtil() {
    }

    /**
     * 将查询值标准化为可用于模糊匹配的值。
     *
     * @param value 原始查询值
     * @return 去除首尾空白后的值；若为空串则返回 null
     */
    public static String normalizeLikeValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
