package info.zhihui.ems.mq.rabbitmq.exception;

/**
 * MQ 消费不可重试异常。
 * 用于标记消息本身或关联基础数据存在问题，继续重试也无法恢复的场景。
 */
public class NonRetryableException extends RuntimeException {

    public NonRetryableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonRetryableException(String message) {
        super(message);
    }
}
