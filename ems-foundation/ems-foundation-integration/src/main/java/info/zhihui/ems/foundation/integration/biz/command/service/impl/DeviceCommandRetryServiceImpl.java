package info.zhihui.ems.foundation.integration.biz.command.service.impl;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.utils.RetryBackoffUtil;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.config.DeviceCommandRetryProperties;
import info.zhihui.ems.foundation.integration.biz.command.entity.DeviceCommandRecordEntity;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.qo.DeviceCommandRetryQo;
import info.zhihui.ems.foundation.integration.biz.command.repository.DeviceCommandRecordRepository;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandRetryService;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jerryxiaosa
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceCommandRetryServiceImpl implements DeviceCommandRetryService {

    private final DeviceCommandService deviceCommandService;
    private final DeviceCommandRecordRepository commandRecordRepository;
    private final DeviceCommandRetryProperties retryProperties;

    @Override
    public void retryDeviceCommand(Integer commandId, CommandSourceEnum commandSource) {
        DeviceCommandRecordBo commandRecord = deviceCommandService.getDeviceCommandDetail(commandId);
        validateRetry(commandRecord);
        deviceCommandService.execDeviceCommand(commandId, commandSource);
    }

    @Override
    public void retryEnsureSuccessCommands() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(retryProperties.getRunningTimeoutMinutes());
        commandRecordRepository.recoverTimeoutRunningCommands(timeoutTime);

        DeviceCommandRetryQo query = new DeviceCommandRetryQo()
                .setFetchSize(retryProperties.getFetchSize())
                .setMaxExecuteTimes(retryProperties.getMaxExecuteTimes());
        List<DeviceCommandRecordEntity> candidateList = commandRecordRepository.findAutoRetryList(query);
        if (CollectionUtils.isEmpty(candidateList)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (DeviceCommandRecordEntity candidate : candidateList) {
            if (!RetryBackoffUtil.shouldRetryNow(candidate.getLastExecuteTime(), candidate.getExecuteTimes(), now)) {
                continue;
            }
            try {
                deviceCommandService.execDeviceCommand(candidate.getId(), CommandSourceEnum.SYSTEM);
            } catch (Exception exception) {
                log.error("自动重试设备命令失败: commandId={}", candidate.getId(), exception);
            }
        }
    }

    private void validateRetry(DeviceCommandRecordBo commandRecord) {
        if (Boolean.TRUE.equals(commandRecord.getSuccess())) {
            throw new BusinessRuntimeException("设备命令已执行成功，无需重试");
        }
        if (Boolean.TRUE.equals(commandRecord.getIsRunning())) {
            throw new BusinessRuntimeException("设备命令正在处理中，请稍后重试");
        }
        if (!Boolean.TRUE.equals(commandRecord.getEnsureSuccess())) {
            throw new BusinessRuntimeException("当前命令不支持重试");
        }
        if (commandRecord.getExecuteTimes() != null
                && commandRecord.getExecuteTimes() >= retryProperties.getMaxExecuteTimes()) {
            throw new BusinessRuntimeException("设备命令已达到最大执行次数，无法重试");
        }
    }
}
