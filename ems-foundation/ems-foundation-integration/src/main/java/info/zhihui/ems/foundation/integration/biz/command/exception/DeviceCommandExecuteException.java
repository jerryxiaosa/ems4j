package info.zhihui.ems.foundation.integration.biz.command.exception;

import info.zhihui.ems.common.exception.BusinessRuntimeException;

/**
 * 设备命令执行异常
 *
 * @author jerryxiaosa
 */
public class DeviceCommandExecuteException extends BusinessRuntimeException {

    public DeviceCommandExecuteException(String message) {
        super(message);
    }

    public DeviceCommandExecuteException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
