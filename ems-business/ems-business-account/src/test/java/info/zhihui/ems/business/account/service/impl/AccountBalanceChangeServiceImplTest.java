package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.entity.AccountEntity;
import info.zhihui.ems.business.account.repository.AccountRepository;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.mq.api.message.finance.BalanceChangedMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountBalanceChangeServiceImplTest {

    @Mock
    private AccountInfoService accountInfoService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private WarnPlanService warnPlanService;

    @InjectMocks
    private AccountBalanceChangeServiceImpl accountBalanceChangeService;

    @Test
    void testHandleBalanceChange_WhenMonthlyAccountWarnTypeChanged_ShouldUpdateWarnLevel() {
        AccountBo accountBo = new AccountBo()
                .setId(10)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setWarnPlanId(3)
                .setElectricWarnType(WarnTypeEnum.NONE);
        when(accountInfoService.getById(10)).thenReturn(accountBo);
        when(warnPlanService.getDetail(3)).thenReturn(new WarnPlanBo()
                .setFirstLevel(new BigDecimal("100"))
                .setSecondLevel(new BigDecimal("20")));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setBalanceRelationId(10)
                .setNewBalance(new BigDecimal("15"));

        accountBalanceChangeService.handleBalanceChange(message);

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(10);
        assertThat(captor.getValue().getElectricWarnType()).isEqualTo(WarnTypeEnum.SECOND.getCode());
    }

    @Test
    void testHandleBalanceChange_WhenMergedAccountBalanceRecovered_ShouldClearWarnLevel() {
        AccountBo accountBo = new AccountBo()
                .setId(11)
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED)
                .setWarnPlanId(4)
                .setElectricWarnType(WarnTypeEnum.SECOND);
        when(accountInfoService.getById(11)).thenReturn(accountBo);
        when(warnPlanService.getDetail(4)).thenReturn(new WarnPlanBo()
                .setFirstLevel(new BigDecimal("100"))
                .setSecondLevel(new BigDecimal("40")));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setBalanceRelationId(11)
                .setNewBalance(new BigDecimal("500"));

        accountBalanceChangeService.handleBalanceChange(message);

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(11);
        assertThat(captor.getValue().getElectricWarnType()).isEqualTo(WarnTypeEnum.NONE.getCode());
    }

    @Test
    void testHandleBalanceChange_WhenQuantityAccount_ShouldSkipWarnProcessing() {
        AccountBo accountBo = new AccountBo()
                .setId(12)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setWarnPlanId(3)
                .setElectricWarnType(WarnTypeEnum.FIRST);
        when(accountInfoService.getById(12)).thenReturn(accountBo);

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setBalanceRelationId(12)
                .setNewBalance(new BigDecimal("50"));

        accountBalanceChangeService.handleBalanceChange(message);

        verifyNoInteractions(warnPlanService);
        verify(accountRepository, never()).updateById(any(AccountEntity.class));
    }
}
