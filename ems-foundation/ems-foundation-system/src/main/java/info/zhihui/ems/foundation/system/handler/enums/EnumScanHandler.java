package info.zhihui.ems.foundation.system.handler.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.system.dto.EnumItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 扫描所有实现了 CodeEnum 的枚举类型，并输出枚举名 -> 枚举项列表（值、描述）。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class EnumScanHandler {

    private final ApplicationContext applicationContext;

    // 内嵌本地缓存，仅暴露 getAll 供外部读取
    private static final String ALL_KEY = "ENUM_ALL";
    private final Cache<String, Map<String, List<EnumItemDto>>> cache = CacheBuilder.newBuilder().build();

    /**
     * 对外公开：获取全部枚举（若缓存不存在则内部扫描并填充）。
     */
    public Map<String, List<EnumItemDto>> getAll() {
        try {
            Map<String, List<EnumItemDto>> map = cache.get(ALL_KEY, this::scanAll);
            return Collections.unmodifiableMap(map);
        } catch (Exception e) {
            log.warn("EnumScanHandler.getAll: compute cache failed, fallback to scan", e);
            throw new BusinessRuntimeException("枚举获取失败");
        }
    }

    /**
     * 扫描并构建所有枚举的两级结构：枚举名 -> 枚举项（值、描述）。
     */
    private Map<String, List<EnumItemDto>> scanAll() {
        Map<String, List<EnumItemDto>> result = new TreeMap<>();

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(CodeEnum.class));

        // 动态解析基础包列表，避免写死
        List<String> basePackages = resolveBasePackages();
        Set<BeanDefinition> candidates = new LinkedHashSet<>();
        for (String basePkg : basePackages) {
            candidates.addAll(scanner.findCandidateComponents(basePkg));
        }

        for (var bd : candidates) {
            String className = bd.getBeanClassName();
            if (className == null) { continue; }
            try {
                Class<?> clazz = Class.forName(className);
                if (!clazz.isEnum()) { continue; }

                Object[] constants = clazz.getEnumConstants();
                List<EnumItemDto> items = Arrays.stream(constants)
                        .map(constant -> buildItem((Enum<?>) constant))
                        .collect(Collectors.toList());

                result.put(trimEnumSuffix(clazz.getSimpleName()), items);
            } catch (ClassNotFoundException e) {
                log.warn("EnumScanBiz: class not found {}", className, e);
            } catch (Throwable t) {
                log.warn("EnumScanBiz: error scanning {}", className, t);
            }
        }
        return result;
    }

    private List<String> resolveBasePackages() {
        try {
            if (AutoConfigurationPackages.has(applicationContext)) {
                List<String> pkgs = AutoConfigurationPackages.get(applicationContext);
                if (pkgs != null && !pkgs.isEmpty()) {
                    return pkgs;
                }
            }
        } catch (Throwable t) {
            log.debug("AutoConfigurationPackages resolve failed: {}", t.toString());
        }

        throw new IllegalStateException("Cannot resolve base packages");
    }

    private String trimEnumSuffix(String simpleName) {
        if (simpleName != null && simpleName.endsWith("Enum") && simpleName.length() > 4) {
            String trimmed = simpleName.substring(0, simpleName.length() - 4);
            // 将首字母转为小写
            return Character.toLowerCase(trimmed.charAt(0)) + trimmed.substring(1);
        }
        // 如果不以 Enum 结尾，也将首字母转为小写
        if (simpleName != null && !simpleName.isEmpty()) {
            return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
        }
        return simpleName;
    }

    private EnumItemDto buildItem(Enum<?> enumConstant) {
        CodeEnum<?> codeEnum = (CodeEnum<?>) enumConstant;
        Object value = codeEnum.getCode();
        String description = codeEnum.getInfo();
        return new EnumItemDto(value, description);
    }

}