package info.zhihui.ems.foundation.integration.biz.command.service;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandExecuteRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandAddDto;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandCancelDto;
import info.zhihui.ems.foundation.integration.biz.command.entity.DeviceCommandExecuteRecordEntity;
import info.zhihui.ems.foundation.integration.biz.command.entity.DeviceCommandRecordEntity;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.repository.DeviceCommandExecuteRecordRepository;
import info.zhihui.ems.foundation.integration.biz.command.repository.DeviceCommandRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
@DisplayName("设备命令服务集成测试")
class DeviceCommandServiceImplIntegrationTest {

    @Autowired
    private DeviceCommandService deviceCommandService;

    @Autowired
    private DeviceCommandRecordRepository deviceCommandRecordRepository;

    @Autowired
    private DeviceCommandExecuteRecordRepository deviceCommandExecuteRecordRepository;

    private DeviceCommandAddDto buildAddDto() {
        return new DeviceCommandAddDto()
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setDeviceId(1)
                .setDeviceIotId("iot-1")
                .setDeviceNo("DEV-001")
                .setDeviceName("测试电表1")
                .setSpaceId(1)
                .setSpaceName("测试空间")
                .setAccountId(5)
                .setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON)
                .setCommandSource(CommandSourceEnum.USER)
                .setCommandData("{\"action\":\"turnOn\"}")
                .setEnsureSuccess(true)
                .setRemark("integration test")
                .setAreaId(1)
                .setOperateUser(1)
                .setOperateUserName("测试用户");
    }

    @Test
    @DisplayName("saveDeviceCommand - 保存设备命令成功")
    void testSaveDeviceCommand() {
        // When
        Integer commandId = deviceCommandService.saveDeviceCommand(buildAddDto());

        // Then
        assertThat(commandId).isNotNull();
        DeviceCommandRecordEntity entity = deviceCommandRecordRepository.selectById(commandId);
        assertThat(entity).isNotNull();
        assertThat(entity.getDeviceTypeKey()).isEqualTo(DeviceTypeEnum.ELECTRIC.getKey());
        assertThat(entity.getSuccess()).isFalse();
        assertThat(entity.getEnsureSuccess()).isTrue();
    }

    @Test
    @DisplayName("execDeviceCommand - 执行成功写入记录并更新状态")
    void testExecDeviceCommand() {
        // Given
        Integer commandId = deviceCommandService.saveDeviceCommand(buildAddDto());

        // When
        deviceCommandService.execDeviceCommand(commandId, CommandSourceEnum.USER);

        // Then - 执行记录
        List<DeviceCommandExecuteRecordBo> executeList = deviceCommandService.findCommandExecuteRecordList(commandId);
        assertThat(executeList).isNotEmpty();
        assertThat(executeList.get(0).getCommandId()).isEqualTo(commandId);
        assertThat(executeList.get(0).getSuccess()).isTrue();
        // 服务层返回的执行记录字段校验
        assertThat(executeList.get(0).getCommandSource()).isEqualTo(CommandSourceEnum.USER);
        assertThat(executeList.get(0).getRunTime()).isNotNull();

        // 仓库层查询执行记录校验
        List<DeviceCommandExecuteRecordEntity> repoExecuteList = deviceCommandExecuteRecordRepository.findList(commandId);
        assertThat(repoExecuteList).isNotEmpty();
        assertThat(repoExecuteList.get(0).getCommandId()).isEqualTo(commandId);
        assertThat(repoExecuteList.get(0).getSuccess()).isTrue();
        assertThat(repoExecuteList.get(0).getCommandSource()).isEqualTo(CommandSourceEnum.USER.getCode());
        assertThat(repoExecuteList.get(0).getExecuteTime()).isNotNull();

        // Then - 命令记录状态更新
        DeviceCommandRecordEntity record = deviceCommandRecordRepository.selectById(commandId);
        assertThat(record.getLastExecuteTime()).isNotNull();
        assertThat(record.getSuccess()).isTrue();
        assertThat(record.getSuccessTime()).isNotNull();
    }

    @Test
    @DisplayName("cancelDeviceCommand - 取消重试命令成功")
    void testCancelDeviceCommand() {
        // Given - 新增一条需要重试且未成功的命令
        Integer commandId = deviceCommandService.saveDeviceCommand(buildAddDto().setEnsureSuccess(true));

        // When - 取消该设备的相关重试命令
        DeviceCommandCancelDto cancelDto = new DeviceCommandCancelDto()
                .setDeviceId(1)
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON)
                .setReason("测试取消");
        deviceCommandService.cancelDeviceCommand(cancelDto);

        // Then - 验证命令记录的重试标志与备注
        DeviceCommandRecordEntity after = deviceCommandRecordRepository.selectById(commandId);
        assertThat(after).isNotNull();
        assertThat(after.getSuccess()).isFalse();
        assertThat(after.getEnsureSuccess()).isFalse();
        assertThat(after.getRemark()).isEqualTo("测试取消");
    }
}