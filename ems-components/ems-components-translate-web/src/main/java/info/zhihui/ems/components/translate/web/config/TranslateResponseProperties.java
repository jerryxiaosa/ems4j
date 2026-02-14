package info.zhihui.ems.components.translate.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 响应转换配置
 */
@Data
@ConfigurationProperties(prefix = "translate.response")
public class TranslateResponseProperties {

    /**
     * 是否启用响应转换
     */
    private boolean enabled = true;

    /**
     * 仅匹配白名单路径，未命中则不转换
     */
    private List<String> includePathPatterns = new ArrayList<>();

    /**
     * 排除路径，命中后不转换
     */
    private List<String> excludePathPatterns = new ArrayList<>();
}

