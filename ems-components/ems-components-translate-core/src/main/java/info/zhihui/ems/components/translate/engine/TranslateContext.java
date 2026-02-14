package info.zhihui.ems.components.translate.engine;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * 转换上下文
 */
@Data
@Accessors(chain = true)
public class TranslateContext {

    /**
     * 请求路径（HTTP场景可用）
     */
    private String requestPath;

    /**
     * 请求方法（HTTP场景可用）
     */
    private String requestMethod;

    /**
     * 预留扩展属性
     */
    private Map<String, Object> attributes = new HashMap<>();
}

