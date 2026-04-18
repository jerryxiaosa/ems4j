package info.zhihui.ems.foundation.integration.biz.command.service.impl;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandExecuteRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.config.DeviceCommandRetryProperties;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandAddDto;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandCancelDto;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandQueryDto;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceCommandRetryServiceImplTest {

    @Test
    @DisplayName("手动重试设备命令_命令不支持重试时应拒绝执行")
    void testRetryDeviceCommand_WhenEnsureSuccessFalse_ShouldThrowException() {
        AtomicBoolean executed = new AtomicBoolean(false);
        DeviceCommandRecordBo commandRecordBo = new DeviceCommandRecordBo()
                .setId(18)
                .setSuccess(false)
                .setIsRunning(false)
                .setEnsureSuccess(false)
                .setExecuteTimes(1);

        DeviceCommandService deviceCommandService = new DeviceCommandService() {
            @Override
            public Integer saveDeviceCommand(DeviceCommandAddDto dto) {
                return null;
            }

            @Override
            public void execDeviceCommand(Integer commandId, CommandSourceEnum commandSource) {
                executed.set(true);
            }

            @Override
            public PageResult<DeviceCommandRecordBo> findDeviceCommandPage(DeviceCommandQueryDto query, PageParam pageParam) {
                return null;
            }

            @Override
            public DeviceCommandRecordBo getDeviceCommandDetail(Integer commandId) {
                return commandRecordBo;
            }

            @Override
            public List<DeviceCommandExecuteRecordBo> findCommandExecuteRecordList(Integer commandId) {
                return Collections.emptyList();
            }

            @Override
            public void cancelDeviceCommand(DeviceCommandCancelDto dto) {
            }
        };

        DeviceCommandRetryProperties retryProperties = new DeviceCommandRetryProperties();
        retryProperties.setMaxExecuteTimes(3);

        DeviceCommandRetryServiceImpl service = new DeviceCommandRetryServiceImpl(deviceCommandService, null, retryProperties);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.retryDeviceCommand(18, CommandSourceEnum.USER));

        assertEquals("当前命令不支持重试", exception.getMessage());
        assertTrue(!executed.get());
    }
}
