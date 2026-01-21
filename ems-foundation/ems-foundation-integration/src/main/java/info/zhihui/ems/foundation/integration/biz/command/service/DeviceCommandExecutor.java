package info.zhihui.ems.foundation.integration.biz.command.service;

import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;

/**
 * 能被执行、重试的命令需要实现这个接口
 *
 * @author jerryxiaosa
 */
public interface DeviceCommandExecutor {
    CommandTypeEnum getCommandType();

    void execute(DeviceCommandRecordBo commandRecordBo);
}
