package info.zhihui.ems.mq.rabbitmq.listener.device;

import info.zhihui.ems.mq.api.message.device.StandardEnergyReportMessage;
import info.zhihui.ems.mq.rabbitmq.constant.QueueConstant;
import info.zhihui.ems.mq.rabbitmq.exception.NonRetryableException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 标准电量上报消息监听器
 *
 * @author jerryxiaosa
 */
@Component
@Slf4j
@Validated
@RequiredArgsConstructor
public class StandardEnergyReportListener {

    private final EnergyReportProcessor standardEnergyReportProcessor;

    @RabbitListener(queues = QueueConstant.QUEUE_DEVICE_STANDARD_ENERGY_REPORT)
    public void handle(@Valid @NotNull StandardEnergyReportMessage message) {
        log.info("接收到标准电量上报消息，deviceNo={}, source={}, sourceReportId={}",
                message.getDeviceNo(), message.getSource(), message.getSourceReportId());
        try {
            standardEnergyReportProcessor.process(message);
        } catch (NonRetryableException exception) {
            log.error("处理标准电量上报消息失败，deviceNo={}, source={}, sourceReportId={}, error={}",
                    message.getDeviceNo(), message.getSource(), message.getSourceReportId(), exception.getMessage(), exception);
        }
    }
}
