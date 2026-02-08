package info.zhihui.ems.foundation.integration.biz.command.service.impl.concrete;

import cn.hutool.core.util.StrUtil;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandExecutor;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.DailyEnergyPlanUpdateDto;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;
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
public class DeviceCommandSetPriceTime implements DeviceCommandExecutor {

    private final DeviceModuleContext deviceModuleContext;

    @Override
    public CommandTypeEnum getCommandType() {
        return CommandTypeEnum.ENERGY_ELECTRIC_PRICE_TIME;
    }

    @Override
    public void execute(DeviceCommandRecordBo commandRecordBo) {
        log.info("开始执行命令：下发尖峰平谷时间段");

        String json = commandRecordBo.getCommandData();
        if (StrUtil.isBlank(json)) {
            throw new BusinessRuntimeException("命令参数不能为空");
        }

        DailyEnergyPlanUpdateDto dto;
        try {
            dto = JacksonUtil.fromJson(json, DailyEnergyPlanUpdateDto.class);
        } catch (Exception e) {
            throw new BusinessRuntimeException("命令参数格式错误");
        }

        dto.setDeviceId(commandRecordBo.getDeviceId())
                .setAreaId(commandRecordBo.getAreaId());

        EnergyService energyService = deviceModuleContext.getService(EnergyService.class, commandRecordBo.getAreaId());
        energyService.setDuration(dto);
    }
}
