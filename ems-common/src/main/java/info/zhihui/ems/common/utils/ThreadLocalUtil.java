package info.zhihui.ems.common.utils;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ThreadLocalUtil {
    private ThreadLocalUtil() {
    }

    private final static TransmittableThreadLocal<Map<String, Object>> THREAD_CONTEXT = new MapThreadLocal();

    public static Object get(String key) {
        return getContextMap().get(key);
    }

    public static void put(String key, Object value) {
        if (value != null && !(value instanceof Serializable)) {
            throw new IllegalArgumentException("ThreadLocal value must implement Serializable");
        }
        getContextMap().put(key, value);
    }

    public static void remove(String key) {
        getContextMap().remove(key);
    }

    public static void clear() {
        getContextMap().clear();
    }

    private static Map<String, Object> getContextMap() {
        // TransmittableThreadLocal.get -> ThreadLocal.get
        return THREAD_CONTEXT.get();
    }

    private static class MapThreadLocal extends TransmittableThreadLocal<Map<String, Object>> {
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>(8);
        }

        @Override
        protected Map<String, Object> childValue(Map<String, Object> parentValue) {
            return ObjectUtil.cloneByStream(parentValue);
        }

        @Override
        public Map<String, Object> copy(Map<String, Object> parentValue) {
            return ObjectUtil.cloneByStream(parentValue);
        }
    }

}
