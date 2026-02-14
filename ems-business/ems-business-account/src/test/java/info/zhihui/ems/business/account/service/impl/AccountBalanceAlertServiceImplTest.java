package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.entity.AccountEntity;
import info.zhihui.ems.business.account.repository.AccountRepository;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountBalanceAlertServiceImplTest {

    @Mock
    private AccountInfoService accountInfoService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private WarnPlanService warnPlanService;
    @Mock
    private ElectricMeterInfoService electricMeterInfoService;
    @Mock
    private ElectricMeterManagerService electricMeterManagerService;

    @InjectMocks
    private AccountBalanceAlertServiceImpl balanceAlertService;

    @Test
    void handleBalanceChange_UpdateAccountWarnLevel() {
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

        balanceAlertService.handleBalanceChange(message);

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(10);
        assertThat(captor.getValue().getElectricWarnType()).isEqualTo(WarnTypeEnum.SECOND.getCode());
    }

    @Test
    void handleBalanceChange_ClearAccountWarnLevelWhenBalanceRecovered() {
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

        balanceAlertService.handleBalanceChange(message);

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(11);
        assertThat(captor.getValue().getElectricWarnType()).isEqualTo(WarnTypeEnum.NONE.getCode());
    }

    @Test
    void handleBalanceChange_SkipWhenNoWarnPlanOnMeter() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(5)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(5)).thenReturn(meterBo);

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(5)
                .setNewBalance(BigDecimal.ONE);

        balanceAlertService.handleBalanceChange(message);

        verifyNoInteractions(warnPlanService);
        verify(electricMeterManagerService, never()).setMeterWarnLevel(anyList(), any());
    }

    @Test
    void handleBalanceChange_UpdateMeterWarnLevel() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(6)
                .setWarnPlanId(8)
                .setWarnType(WarnTypeEnum.NONE);
        when(electricMeterInfoService.getDetail(6)).thenReturn(meterBo);
        when(warnPlanService.getDetail(8)).thenReturn(new WarnPlanBo()
                .setFirstLevel(new BigDecimal("100"))
                .setSecondLevel(new BigDecimal("30")));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(6)
                .setNewBalance(new BigDecimal("50"));

        balanceAlertService.handleBalanceChange(message);

        verify(electricMeterManagerService).setMeterWarnLevel(eq(List.of(6)), eq(WarnTypeEnum.FIRST));
    }

    @Test
    void handleBalanceChange_SkipAccountWarnWhenQuantity() {
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

        balanceAlertService.handleBalanceChange(message);

        verifyNoInteractions(warnPlanService);
        verify(accountRepository, never()).updateById(any(AccountEntity.class));
    }
}
