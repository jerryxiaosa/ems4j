package info.zhihui.ems.schedule.task.business.device;

import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 设备命令重试计划任务
 *
 * @author jerryxiaosa
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryDeviceCommand {

    private final DeviceCommandRetryService deviceCommandRetryService;

    @Scheduled(cron = "0 * * * * ?")
    public void retryEnsureSuccessCommands() {
        log.info("开始执行设备命令重试任务");
        deviceCommandRetryService.retryEnsureSuccessCommands();
        log.info("设备命令重试任务执行完成");
    }
}
