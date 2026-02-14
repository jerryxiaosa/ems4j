package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.entity.AccountEntity;
import info.zhihui.ems.business.account.repository.AccountRepository;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.account.service.AccountBalanceAlertService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
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
 * 余额预警服务实现
 *
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class AccountBalanceAlertServiceImpl implements AccountBalanceAlertService {

    private final AccountInfoService accountInfoService;
    private final AccountRepository accountRepository;
    private final WarnPlanService warnPlanService;
    private final ElectricMeterInfoService electricMeterInfoService;
    private final ElectricMeterManagerService electricMeterManagerService;

    @Override
    public void handleBalanceChange(@NotNull @Valid BalanceChangedMessage message) {
        try {
            if (BalanceTypeEnum.ACCOUNT.equals(message.getBalanceType())) {
                handleAccountBalanceChange(message);
            } else if (BalanceTypeEnum.ELECTRIC_METER.equals(message.getBalanceType())) {
                handleMeterBalanceChange(message);
            } else {
                // @TODO
                log.warn("未支持的余额类型，message={}", message);
            }
        } catch (Exception e) {
            log.error("处理余额预警失败，message={}", message, e);
        }
    }

    private void handleAccountBalanceChange(BalanceChangedMessage message) {
        AccountBo accountBo = accountInfoService.getById(message.getBalanceRelationId());

        if (!ElectricAccountTypeEnum.MONTHLY.equals(accountBo.getElectricAccountType())
                && !ElectricAccountTypeEnum.MERGED.equals(accountBo.getElectricAccountType())) {
            log.debug("该账户类型不需要账户级预警, accountId={}, type={}", accountBo.getId(), accountBo.getElectricAccountType());
            return;
        }
        Integer warnPlanId = accountBo.getWarnPlanId();
        if (warnPlanId == null) {
            log.debug("账户{}未配置预警方案，跳过预警处理", accountBo.getId());
            return;
        }

        WarnPlanBo warnPlanBo = warnPlanService.getDetail(warnPlanId);
        WarnTypeEnum newWarnType = computeWarnType(message.getNewBalance(), warnPlanBo);
        WarnTypeEnum currentWarnType = Objects.requireNonNullElse(accountBo.getElectricWarnType(), WarnTypeEnum.NONE);

        if (newWarnType == currentWarnType) {
            log.debug("账户{}预警等级未变化，维持{}", accountBo.getId(), newWarnType);
            return;
        }

        AccountEntity updateEntity = new AccountEntity()
                .setId(accountBo.getId())
                .setElectricWarnType(newWarnType.getCode());
        accountRepository.updateById(updateEntity);
        log.info("账户{}预警等级更新：{} -> {}", accountBo.getId(), currentWarnType, newWarnType);
    }

    private void handleMeterBalanceChange(BalanceChangedMessage message) {
        ElectricMeterBo meterBo = electricMeterInfoService.getDetail(message.getBalanceRelationId());
        if (meterBo == null) {
            log.warn("电表不存在，meterId={}", message.getBalanceRelationId());
            return;
        }
        Integer warnPlanId = meterBo.getWarnPlanId();
        if (warnPlanId == null) {
            log.debug("电表{}未配置预警方案，跳过预警处理", meterBo.getId());
            return;
        }
        WarnPlanBo warnPlanBo = warnPlanService.getDetail(warnPlanId);
        WarnTypeEnum newWarnType = computeWarnType(message.getNewBalance(), warnPlanBo);
        WarnTypeEnum currentWarnType = Objects.requireNonNullElse(meterBo.getWarnType(), WarnTypeEnum.NONE);

        if (newWarnType == currentWarnType) {
            log.debug("电表{}预警等级未变化，维持{}", meterBo.getId(), newWarnType);
            return;
        }

        electricMeterManagerService.setMeterWarnLevel(List.of(meterBo.getId()), newWarnType);
        log.info("电表{}预警等级更新：{} -> {}", meterBo.getId(), currentWarnType, newWarnType);
    }

    private WarnTypeEnum computeWarnType(BigDecimal balance, WarnPlanBo warnPlanBo) {
        BigDecimal firstLevel = warnPlanBo.getFirstLevel();
        BigDecimal secondLevel = warnPlanBo.getSecondLevel();

        if (secondLevel != null && balance.compareTo(secondLevel) <= 0) {
            return WarnTypeEnum.SECOND;
        }
        if (firstLevel != null && balance.compareTo(firstLevel) <= 0) {
            return WarnTypeEnum.FIRST;
        }
        return WarnTypeEnum.NONE;
    }

}
