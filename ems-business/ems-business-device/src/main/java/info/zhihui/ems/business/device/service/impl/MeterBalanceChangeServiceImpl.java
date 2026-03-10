package info.zhihui.ems.business.device.service.impl;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterSwitchStatusDto;
import info.zhihui.ems.business.device.dto.MeterBalanceChangeDto;
import info.zhihui.ems.business.device.enums.ElectricSwitchStatusEnum;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.device.service.MeterBalanceChangeService;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.business.plan.util.WarnTypeCalculator;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 电表余额变化服务实现
 *
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class MeterBalanceChangeServiceImpl implements MeterBalanceChangeService {

    private final ElectricMeterInfoService electricMeterInfoService;
    private final ElectricMeterManagerService electricMeterManagerService;
    private final WarnPlanService warnPlanService;

    @Override
    public void handleBalanceChange(@NotNull @Valid MeterBalanceChangeDto meterBalanceChangeDto) {
        try {
            handleMeterBalanceChange(meterBalanceChangeDto);
        } catch (Exception exception) {
            log.error("处理电表余额变化失败，dto={}", meterBalanceChangeDto, exception);
        }
    }

    private void handleMeterBalanceChange(MeterBalanceChangeDto meterBalanceChangeDto) {
        ElectricMeterBo meterBo = electricMeterInfoService.getDetail(meterBalanceChangeDto.getMeterId());
        if (meterBo == null) {
            log.warn("电表不存在，meterId={}", meterBalanceChangeDto.getMeterId());
            return;
        }

        try {
            handleMeterWarnTypeChange(meterBalanceChangeDto, meterBo);
        } catch (Exception exception) {
            log.error("电表{}预警处理失败，继续执行开关闸逻辑", meterBo.getId(), exception);
        }

        try {
            handleMeterSwitchStatusChange(meterBalanceChangeDto, meterBo);
        } catch (Exception exception) {
            log.error("电表{}开关闸处理失败", meterBo.getId(), exception);
        }
    }

    private void handleMeterWarnTypeChange(MeterBalanceChangeDto meterBalanceChangeDto, ElectricMeterBo meterBo) {
        Integer warnPlanId = meterBo.getWarnPlanId();
        if (warnPlanId == null) {
            log.debug("电表{}未配置预警方案，跳过预警处理", meterBo.getId());
            return;
        }

        WarnPlanBo warnPlanBo;
        try {
            warnPlanBo = warnPlanService.getDetail(warnPlanId);
        } catch (Exception exception) {
            log.error("电表{}的预警方案{}不存在或不可用，跳过处理", meterBo.getId(), warnPlanId, exception);
            return;
        }

        WarnTypeEnum newWarnType = WarnTypeCalculator.compute(meterBalanceChangeDto.getNewBalance(), warnPlanBo);
        WarnTypeEnum currentWarnType = Objects.requireNonNullElse(meterBo.getWarnType(), WarnTypeEnum.NONE);
        if (newWarnType == currentWarnType) {
            log.debug("电表{}预警等级未变化，维持{}", meterBo.getId(), newWarnType);
            return;
        }

        electricMeterManagerService.setMeterWarnLevel(List.of(meterBo.getId()), newWarnType);
        log.info("电表{}预警等级更新：{} -> {}", meterBo.getId(), currentWarnType, newWarnType);
    }

    private void handleMeterSwitchStatusChange(MeterBalanceChangeDto meterBalanceChangeDto, ElectricMeterBo meterBo) {
        if (!Boolean.TRUE.equals(meterBalanceChangeDto.getNeedHandleSwitchStatus())) {
            log.debug("电表{}无需处理自动开关闸", meterBo.getId());
            return;
        }

        ElectricSwitchStatusEnum targetSwitchStatus = resolveTargetSwitchStatus(meterBalanceChangeDto.getNewBalance());
        if (targetSwitchStatus == null) {
            log.warn("电表{}余额为空，跳过自动开关闸", meterBo.getId());
            return;
        }

        if (ElectricSwitchStatusEnum.OFF.equals(targetSwitchStatus) && Boolean.TRUE.equals(meterBo.getProtectedModel())) {
            log.info("电表{}启用保电模式，跳过自动断闸", meterBo.getId());
            return;
        }

        if (!Boolean.TRUE.equals(meterBo.getIsOnline())) {
            log.info("电表{}离线，跳过自动{}闸", meterBo.getId(),
                    ElectricSwitchStatusEnum.OFF.equals(targetSwitchStatus) ? "断" : "合");
            return;
        }

        ElectricSwitchStatusEnum currentSwitchStatus = Boolean.TRUE.equals(meterBo.getIsCutOff())
                ? ElectricSwitchStatusEnum.OFF : ElectricSwitchStatusEnum.ON;
        if (currentSwitchStatus == targetSwitchStatus) {
            log.debug("电表{}开关状态未变化，维持{}", meterBo.getId(), currentSwitchStatus);
            return;
        }

        try {
            electricMeterManagerService.setSwitchStatus(new ElectricMeterSwitchStatusDto()
                    .setId(meterBo.getId())
                    .setSwitchStatus(targetSwitchStatus)
                    .setCommandSource(CommandSourceEnum.SYSTEM));
            log.info("电表{}自动{}闸成功，余额={}", meterBo.getId(),
                    ElectricSwitchStatusEnum.OFF.equals(targetSwitchStatus) ? "断" : "合",
                    meterBalanceChangeDto.getNewBalance());
        } catch (Exception exception) {
            log.error("电表{}自动{}闸失败，余额={}", meterBo.getId(),
                    ElectricSwitchStatusEnum.OFF.equals(targetSwitchStatus) ? "断" : "合",
                    meterBalanceChangeDto.getNewBalance(), exception);
        }
    }

    private ElectricSwitchStatusEnum resolveTargetSwitchStatus(BigDecimal newBalance) {
        if (newBalance == null) {
            return null;
        }
        return newBalance.compareTo(BigDecimal.ZERO) <= 0
                ? ElectricSwitchStatusEnum.OFF
                : ElectricSwitchStatusEnum.ON;
    }
}
