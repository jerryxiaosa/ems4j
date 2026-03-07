package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.account.service.MeterBalanceChangeService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterSwitchStatusDto;
import info.zhihui.ems.business.device.enums.ElectricSwitchStatusEnum;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.business.plan.util.WarnTypeCalculator;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.mq.api.message.finance.BalanceChangedMessage;
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

    private final AccountInfoService accountInfoService;
    private final ElectricMeterInfoService electricMeterInfoService;
    private final ElectricMeterManagerService electricMeterManagerService;
    private final WarnPlanService warnPlanService;

    @Override
    public void handleBalanceChange(@NotNull @Valid BalanceChangedMessage message) {
        try {
            if (!BalanceTypeEnum.ELECTRIC_METER.equals(message.getBalanceType())) {
                log.warn("非电表余额消息不处理，message={}", message);
                return;
            }
            handleMeterBalanceChange(message);
        } catch (Exception e) {
            log.error("处理电表余额变化失败，message={}", message, e);
        }
    }

    private void handleMeterBalanceChange(BalanceChangedMessage message) {
        ElectricMeterBo meterBo = electricMeterInfoService.getDetail(message.getBalanceRelationId());
        if (meterBo == null) {
            log.warn("电表不存在，meterId={}", message.getBalanceRelationId());
            return;
        }

        try {
            handleMeterWarnTypeChange(message, meterBo);
        } catch (Exception ex) {
            log.error("电表{}预警处理失败，继续执行开关闸逻辑", meterBo.getId(), ex);
        }
        try {
            handleMeterSwitchStatusChange(message, meterBo);
        } catch (Exception ex) {
            log.error("电表{}开关闸处理失败", meterBo.getId(), ex);
        }
    }

    private void handleMeterWarnTypeChange(BalanceChangedMessage message, ElectricMeterBo meterBo) {
        Integer warnPlanId = meterBo.getWarnPlanId();
        if (warnPlanId == null) {
            log.debug("电表{}未配置预警方案，跳过预警处理", meterBo.getId());
            return;
        }

        WarnPlanBo warnPlanBo;
        try {
            warnPlanBo = warnPlanService.getDetail(warnPlanId);
        } catch (Exception ex) {
            log.error("电表{}的预警方案{}不存在或不可用，跳过处理", meterBo.getId(), warnPlanId, ex);
            return;
        }
        WarnTypeEnum newWarnType = WarnTypeCalculator.compute(message.getNewBalance(), warnPlanBo);
        WarnTypeEnum currentWarnType = Objects.requireNonNullElse(meterBo.getWarnType(), WarnTypeEnum.NONE);

        if (newWarnType == currentWarnType) {
            log.debug("电表{}预警等级未变化，维持{}", meterBo.getId(), newWarnType);
            return;
        }

        electricMeterManagerService.setMeterWarnLevel(List.of(meterBo.getId()), newWarnType);
        log.info("电表{}预警等级更新：{} -> {}", meterBo.getId(), currentWarnType, newWarnType);
    }

    private void handleMeterSwitchStatusChange(BalanceChangedMessage message, ElectricMeterBo meterBo) {
        Integer accountId = meterBo.getAccountId();
        if (accountId == null) {
            log.debug("电表{}未绑定账户，跳过自动开关闸", meterBo.getId());
            return;
        }

        AccountBo accountBo;
        try {
            accountBo = accountInfoService.getById(accountId);
        } catch (Exception ex) {
            log.error("查询电表{}所属账户{}失败，跳过自动开关闸", meterBo.getId(), accountId, ex);
            return;
        }

        if (accountBo.getElectricAccountType() != ElectricAccountTypeEnum.QUANTITY) {
            log.debug("电表{}所属账户{}不是按需计费，跳过自动开关闸", meterBo.getId(), accountId);
            return;
        }

        ElectricSwitchStatusEnum targetSwitchStatus = resolveTargetSwitchStatus(message.getNewBalance());
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
                    message.getNewBalance());
        } catch (Exception ex) {
            log.error("电表{}自动{}闸失败，余额={}", meterBo.getId(),
                    ElectricSwitchStatusEnum.OFF.equals(targetSwitchStatus) ? "断" : "合",
                    message.getNewBalance(), ex);
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
