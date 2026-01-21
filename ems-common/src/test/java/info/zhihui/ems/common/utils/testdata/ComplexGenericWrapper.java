package info.zhihui.ems.common.utils.testdata;

import lombok.Data;

import java.util.List;

/**
 * 用于测试多层嵌套泛型反序列化的复杂包装类
 * 模拟类似Notification<Event<T>>的结构
 */
@Data
public class ComplexGenericWrapper<T> {
    private String method;
    private ComplexParams<T> params;

    public ComplexGenericWrapper() {
    }

    public ComplexGenericWrapper(String method, ComplexParams<T> params) {
        this.method = method;
        this.params = params;
    }

    /**
     * 内部参数类，模拟Params<T>
     */
    @Data
    public static class ComplexParams<T> {
        private String ability;
        private List<ComplexEvent<T>> events;

        public ComplexParams() {
        }

        public ComplexParams(String ability, List<ComplexEvent<T>> events) {
            this.ability = ability;
            this.events = events;
        }
    }

    /**
     * 内部事件类，模拟Event<T>
     */
    @Data
    public static class ComplexEvent<T> {
        private String eventId;
        private String eventType;
        private T data;

        public ComplexEvent() {
        }

        public ComplexEvent(String eventId, String eventType, T data) {
            this.eventId = eventId;
            this.eventType = eventType;
            this.data = data;
        }
    }
}