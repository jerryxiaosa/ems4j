package info.zhihui.ems.foundation.integration.biz.command.service.impl.concrete;

import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandExecutor;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.BaseElectricDeviceDto;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author jerryxiaosa
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceCommandTurnOff implements DeviceCommandExecutor {

    private final DeviceModuleContext deviceModuleContext;

    @Override
    public CommandTypeEnum getCommandType() {
        return CommandTypeEnum.ENERGY_ELECTRIC_TURN_OFF;
    }

    @Override
    public void execute(DeviceCommandRecordBo commandRecordBo) {
        log.info("开始执行命令：断闸");

        EnergyService energyService = deviceModuleContext.getService(EnergyService.class, commandRecordBo.getAreaId());
        energyService.cutOff(new BaseElectricDeviceDto()
                .setDeviceId(commandRecordBo.getDeviceId())
                .setAreaId(commandRecordBo.getAreaId()));
    }
}