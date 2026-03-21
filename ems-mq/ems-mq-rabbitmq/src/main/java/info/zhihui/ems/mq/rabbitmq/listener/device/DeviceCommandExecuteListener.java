package info.zhihui.ems.mq.rabbitmq.listener.device;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.exception.DeviceCommandExecuteException;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandService;
import info.zhihui.ems.mq.api.message.device.DeviceCommandExecuteMessage;
import info.zhihui.ems.mq.rabbitmq.constant.QueueConstant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 设备命令首次执行消息监听器
 *
 * @author jerryxiaosa
 */
@Component
@Slf4j
@Validated
@RequiredArgsConstructor
public class DeviceCommandExecuteListener {

    private final DeviceCommandService deviceCommandService;

    @RabbitListener(queues = QueueConstant.QUEUE_DEVICE_COMMAND_EXECUTE)
    public void handle(@Valid @NotNull DeviceCommandExecuteMessage message) {
        try {
            deviceCommandService.execDeviceCommand(message.getCommandId(), CommandSourceEnum.SYSTEM);
        } catch (BusinessRuntimeException exception) {
            log.error("执行设备命令消息失败，commandId={}, error={}",
                    message.getCommandId(), exception.getMessage(), exception);
        }
    }
}
