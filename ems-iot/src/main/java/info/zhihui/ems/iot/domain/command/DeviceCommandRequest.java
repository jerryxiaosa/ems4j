package info.zhihui.ems.iot.domain.command;

import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;

/**
 * 通用设备命令请求。
 */
public interface DeviceCommandRequest {

    /**
     * 命令类型。
     *
     * @return 命令类型
     */
    DeviceCommandTypeEnum type();

    /**
     * 参数校验。
     */
    default void validate() {
        // 默认不校验，由具体命令覆盖
    }
}
