package info.zhihui.ems.foundation.integration.biz.command.service;

import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jerryxiaosa
 */
@Component
public class DeviceCommandExecutorContext {
    private final Map<CommandTypeEnum, DeviceCommandExecutor> deviceCommandExecutorMap = new HashMap<>();

    @Autowired
    public DeviceCommandExecutorContext(List<DeviceCommandExecutor> deviceCommandExecutorList) {
        for (DeviceCommandExecutor executor : deviceCommandExecutorList) {
            deviceCommandExecutorMap.put(executor.getCommandType(), executor);
        }
    }

    public DeviceCommandExecutor getDeviceCommandExecutor(CommandTypeEnum commandType) {
        DeviceCommandExecutor executor = deviceCommandExecutorMap.get(commandType);
        if (executor == null) {
            throw new NotFoundException("没有找到对应的命令执行器");
        }

        return executor;
    }
}
