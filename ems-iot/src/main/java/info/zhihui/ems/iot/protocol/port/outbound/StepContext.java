package info.zhihui.ems.iot.protocol.port.outbound;

import java.util.HashMap;
import java.util.Map;

/**
 * 多步骤命令执行上下文。
 */
public class StepContext {

    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * 存储步骤间共享数据。
     *
     * @param key   键
     * @param value 值
     */
    public void put(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 读取步骤间共享数据。
     *
     * @param key 键
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) attributes.get(key);
    }
}
