package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.entity.AccountEntity;
import info.zhihui.ems.business.account.repository.AccountRepository;
import info.zhihui.ems.business.account.service.AccountBalanceChangeService;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.business.plan.util.WarnTypeCalculator;
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

import java.util.Objects;

/**
 * 账户余额变化服务实现
 *
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class AccountBalanceChangeServiceImpl implements AccountBalanceChangeService {

    private final AccountInfoService accountInfoService;
    private final AccountRepository accountRepository;
    private final WarnPlanService warnPlanService;

    @Override
    public void handleBalanceChange(@NotNull @Valid BalanceChangedMessage message) {
        try {
            if (!BalanceTypeEnum.ACCOUNT.equals(message.getBalanceType())) {
                log.warn("非账户余额消息不处理，message={}", message);
                return;
            }
            handleAccountBalanceChange(message);
        } catch (Exception e) {
            log.error("处理账户余额变化失败，message={}", message, e);
        }
    }

    private void handleAccountBalanceChange(BalanceChangedMessage message) {
        AccountBo accountBo = accountInfoService.getById(message.getBalanceRelationId());

        if (!ElectricAccountTypeEnum.MONTHLY.equals(accountBo.getElectricAccountType())
                && !ElectricAccountTypeEnum.MERGED.equals(accountBo.getElectricAccountType())) {
            log.debug("该账户类型不需要账户级预警，accountId={}, type={}", accountBo.getId(), accountBo.getElectricAccountType());
            return;
        }

        Integer warnPlanId = accountBo.getWarnPlanId();
        if (warnPlanId == null) {
            log.debug("账户{}未配置预警方案，跳过预警处理", accountBo.getId());
            return;
        }

        WarnPlanBo warnPlanBo;
        try {
            warnPlanBo = warnPlanService.getDetail(warnPlanId);
        } catch (Exception ex) {
            log.error("账户{}的预警方案{}不存在或不可用，跳过处理", accountBo.getId(), warnPlanId, ex);
            return;
        }

        WarnTypeEnum newWarnType = WarnTypeCalculator.compute(message.getNewBalance(), warnPlanBo);
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
}
