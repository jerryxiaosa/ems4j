package info.zhihui.ems.foundation.integration.biz.command.service;

import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;

/**
 * 设备命令重试服务
 *
 * @author jerryxiaosa
 */
public interface DeviceCommandRetryService {

    /**
     * 手动重试设备命令
     *
     * @param commandId 命令ID
     * @param commandSource 命令来源
     */
    void retryDeviceCommand(Integer commandId, CommandSourceEnum commandSource);

    /**
     * 自动重试确保成功的设备命令
     */
    void retryEnsureSuccessCommands();
}
