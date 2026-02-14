package info.zhihui.ems.components.translate.resolver;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.components.translate.engine.TranslateContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 枚举展示值解析器
 */
@Slf4j
@Component
public class EnumLabelResolver {

    /**
     * 批量解析枚举展示值
     */
    public Map<Object, String> resolveBatch(Set<Object> keys,
                                            Class<? extends Enum<?>> enumClass,
                                            TranslateContext context) {
        if (keys == null || keys.isEmpty() || enumClass == null) {
            return Collections.emptyMap();
        }

        if (!CodeEnum.class.isAssignableFrom(enumClass)) {
            log.warn("枚举{}未实现CodeEnum，跳过枚举转换", enumClass.getName());
            return Collections.emptyMap();
        }

        Object[] constants = enumClass.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return Collections.emptyMap();
        }

        Map<Object, String> allMap = new HashMap<>();
        for (Object constant : constants) {
            CodeEnum<?> codeEnum = (CodeEnum<?>) constant;
            allMap.put(codeEnum.getCode(), codeEnum.getInfo());
        }

        Map<Object, String> result = new HashMap<>();
        for (Object key : keys) {
            String label = allMap.get(key);
            if (label != null) {
                result.put(key, label);
            }
        }
        return result;
    }
}

