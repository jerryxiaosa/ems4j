package info.zhihui.ems.iot.infrastructure.registry;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslator;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslatorResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 全局命令翻译器注册表（按厂商+命令类型路由）。
 */
@Component
public class DeviceCommandTranslatorRegistry implements DeviceCommandTranslatorResolver {

    private static final String DEFAULT_PRODUCT = "__DEFAULT__";

    private final Map<VendorProductKey, Map<DeviceCommandTypeEnum, DeviceCommandTranslator<?>>> translatorMap;
    private final Set<String> vendors;

    /**
     * 构建命令翻译器索引：vendor -> product(含默认) -> type。
     */
    public DeviceCommandTranslatorRegistry(List<? extends DeviceCommandTranslator<?>> translators) {
        Map<VendorProductKey, Map<DeviceCommandTypeEnum, DeviceCommandTranslator<?>>> map = new HashMap<>();
        Set<String> vendorSet = new HashSet<>();
        for (DeviceCommandTranslator<?> translator : translators) {
            // 统一厂商/产品编码，保证路由一致性
            String vendor = normalizeVendor(translator.vendor());
            if (!StringUtils.hasText(vendor)) {
                throw new BusinessRuntimeException("命令翻译器厂商标识为空");
            }
            String product = normalizeProduct(translator.productCode());
            String productKey = StringUtils.hasText(product) ? product : DEFAULT_PRODUCT;
            vendorSet.add(vendor);
            // 构建 vendor+product(含默认) -> type 的索引结构
            VendorProductKey key = new VendorProductKey(vendor, productKey);
            Map<DeviceCommandTypeEnum, DeviceCommandTranslator<?>> byType =
                    map.computeIfAbsent(key, ignored -> new EnumMap<>(DeviceCommandTypeEnum.class));
            DeviceCommandTypeEnum type = translator.type();
            if (byType.containsKey(type)) {
                throw new BusinessRuntimeException("重复的命令翻译器，vendor=" + vendor + ", product=" + productKey + ", type=" + type);
            }
            byType.put(type, translator);
        }
        // 封装为不可变结构，防止运行期被修改
        Map<VendorProductKey, Map<DeviceCommandTypeEnum, DeviceCommandTranslator<?>>> readonly = new HashMap<>();
        for (Map.Entry<VendorProductKey, Map<DeviceCommandTypeEnum, DeviceCommandTranslator<?>>> entry : map.entrySet()) {
            readonly.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
        }
        this.translatorMap = Collections.unmodifiableMap(readonly);
        this.vendors = Collections.unmodifiableSet(vendorSet);
    }

    /**
     * 按 vendor + product(可空) + type 解析翻译器，并校验请求类型。
     */
    @Override
    public <R> DeviceCommandTranslator<R> resolve(String vendor, String productCode,
                                                  DeviceCommandTypeEnum type, Class<R> requestType) {
        if (requestType == null) {
            throw new IllegalArgumentException("requestType 不能为空");
        }
        // 优先使用规范化的厂商键，避免大小写/空格导致无法匹配
        String normalizedVendor = normalizeVendor(vendor);
        if (!StringUtils.hasText(normalizedVendor) || !vendors.contains(normalizedVendor)) {
            throw new BusinessRuntimeException("未找到命令翻译器厂商，vendor=" + vendor);
        }
        String normalizedProduct = normalizeProduct(productCode);
        Map<DeviceCommandTypeEnum, DeviceCommandTranslator<?>> byType = null;
        if (StringUtils.hasText(normalizedProduct)) {
            byType = translatorMap.get(new VendorProductKey(normalizedVendor, normalizedProduct));
        }
        if (byType == null) {
            byType = translatorMap.get(new VendorProductKey(normalizedVendor, DEFAULT_PRODUCT));
        }
        if (byType == null) {
            throw new BusinessRuntimeException("未找到命令翻译器产品，vendor=" + vendor + ", product=" + productCode);
        }
        // 按命令类型取最终翻译器，若产品专用缺失则回退到默认产品
        DeviceCommandTranslator<?> translator = byType.get(type);
        if (translator == null && StringUtils.hasText(normalizedProduct)) {
            Map<DeviceCommandTypeEnum, DeviceCommandTranslator<?>> defaultByType =
                    translatorMap.get(new VendorProductKey(normalizedVendor, DEFAULT_PRODUCT));
            if (defaultByType != null) {
                translator = defaultByType.get(type);
            }
        }
        if (translator == null) {
            throw new BusinessRuntimeException("未找到命令翻译器，vendor=" + vendor + ", product=" + productCode + ", type=" + type);
        }
        Class<?> actualType = translator.requestType();
        if (actualType == null || !requestType.isAssignableFrom(actualType)) {
            throw new BusinessRuntimeException("命令翻译器请求类型不匹配，vendor=" + vendor
                    + ", product=" + productCode + ", type=" + type);
        }

        @SuppressWarnings("unchecked")
        DeviceCommandTranslator<R> typed = (DeviceCommandTranslator<R>) translator;
        return typed;
    }

    private String normalizeVendor(String vendor) {
        return StringUtils.hasText(vendor) ? vendor.trim().toUpperCase() : null;
    }

    private String normalizeProduct(String product) {
        return StringUtils.hasText(product) ? product.trim().toUpperCase() : null;
    }

}
