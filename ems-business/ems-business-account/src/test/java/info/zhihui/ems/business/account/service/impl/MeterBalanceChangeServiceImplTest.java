package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterSwitchStatusDto;
import info.zhihui.ems.business.device.enums.ElectricSwitchStatusEnum;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.mq.api.message.finance.BalanceChangedMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeterBalanceChangeServiceImplTest {

    @Mock
    private AccountInfoService accountInfoService;
    @Mock
    private ElectricMeterInfoService electricMeterInfoService;
    @Mock
    private ElectricMeterManagerService electricMeterManagerService;
    @Mock
    private WarnPlanService warnPlanService;

    @InjectMocks
    private MeterBalanceChangeServiceImpl meterBalanceChangeService;

    @Test
    void testHandleBalanceChange_WhenWarnPlanMissing_ShouldSkipProcessing() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(5)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(5)).thenReturn(meterBo);

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(5)
                .setNewBalance(BigDecimal.ONE);

        meterBalanceChangeService.handleBalanceChange(message);

        verifyNoInteractions(warnPlanService);
        verify(electricMeterManagerService, never()).setMeterWarnLevel(anyList(), any());
    }

    @Test
    void testHandleBalanceChange_WhenWarnTypeChanged_ShouldUpdateWarnLevel() {
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

        meterBalanceChangeService.handleBalanceChange(message);

        verify(electricMeterManagerService).setMeterWarnLevel(eq(List.of(6)), eq(WarnTypeEnum.FIRST));
    }

    @Test
    void testHandleBalanceChange_WhenWarnTypeUnchanged_ShouldSkipUpdate() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(7)
                .setWarnPlanId(9)
                .setWarnType(WarnTypeEnum.FIRST);
        when(electricMeterInfoService.getDetail(7)).thenReturn(meterBo);
        when(warnPlanService.getDetail(9)).thenReturn(new WarnPlanBo()
                .setFirstLevel(new BigDecimal("100"))
                .setSecondLevel(new BigDecimal("30")));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(7)
                .setNewBalance(new BigDecimal("80"));

        meterBalanceChangeService.handleBalanceChange(message);

        verify(electricMeterManagerService, never()).setMeterWarnLevel(anyList(), any());
    }

    @Test
    void testHandleBalanceChange_WhenWarnLevelUpdateThrows_ShouldStillHandleSwitchStatus() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(18)
                .setAccountId(108)
                .setWarnPlanId(20)
                .setWarnType(WarnTypeEnum.NONE)
                .setIsOnline(Boolean.TRUE)
                .setIsCutOff(Boolean.FALSE)
                .setProtectedModel(Boolean.FALSE);
        when(electricMeterInfoService.getDetail(18)).thenReturn(meterBo);
        when(warnPlanService.getDetail(20)).thenReturn(new WarnPlanBo()
                .setFirstLevel(new BigDecimal("100"))
                .setSecondLevel(new BigDecimal("30")));
        when(accountInfoService.getById(108)).thenReturn(new AccountBo()
                .setId(108)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY));
        doThrow(new RuntimeException("warn failed"))
                .when(electricMeterManagerService).setMeterWarnLevel(eq(List.of(18)), eq(WarnTypeEnum.SECOND));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(18)
                .setAccountId(108)
                .setNewBalance(new BigDecimal("-1"));

        assertDoesNotThrow(() -> meterBalanceChangeService.handleBalanceChange(message));

        verify(electricMeterManagerService).setSwitchStatus(argThat((ElectricMeterSwitchStatusDto dto) ->
                dto != null
                        && Integer.valueOf(18).equals(dto.getId())
                        && ElectricSwitchStatusEnum.OFF.equals(dto.getSwitchStatus())
                        && CommandSourceEnum.SYSTEM.equals(dto.getCommandSource())));
    }

    @Test
    void testHandleBalanceChange_WhenQuantityAndBalanceLessOrEqualZero_ShouldAutoSwitchOff() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(10)
                .setAccountId(100)
                .setIsOnline(Boolean.TRUE)
                .setIsCutOff(Boolean.FALSE)
                .setProtectedModel(Boolean.FALSE)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(10)).thenReturn(meterBo);
        when(accountInfoService.getById(100)).thenReturn(new AccountBo()
                .setId(100)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(10)
                .setAccountId(100)
                .setNewBalance(BigDecimal.ZERO);

        meterBalanceChangeService.handleBalanceChange(message);

        verify(electricMeterManagerService).setSwitchStatus(argThat((ElectricMeterSwitchStatusDto dto) ->
                dto != null
                        && Integer.valueOf(10).equals(dto.getId())
                        && ElectricSwitchStatusEnum.OFF.equals(dto.getSwitchStatus())
                        && CommandSourceEnum.SYSTEM.equals(dto.getCommandSource())));
    }

    @Test
    void testHandleBalanceChange_WhenQuantityAndBalanceGreaterThanZero_ShouldAutoSwitchOn() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(11)
                .setAccountId(101)
                .setIsOnline(Boolean.TRUE)
                .setIsCutOff(Boolean.TRUE)
                .setProtectedModel(Boolean.FALSE)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(11)).thenReturn(meterBo);
        when(accountInfoService.getById(101)).thenReturn(new AccountBo()
                .setId(101)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(11)
                .setAccountId(101)
                .setNewBalance(new BigDecimal("1"));

        meterBalanceChangeService.handleBalanceChange(message);

        verify(electricMeterManagerService).setSwitchStatus(argThat((ElectricMeterSwitchStatusDto dto) ->
                dto != null
                        && Integer.valueOf(11).equals(dto.getId())
                        && ElectricSwitchStatusEnum.ON.equals(dto.getSwitchStatus())
                        && CommandSourceEnum.SYSTEM.equals(dto.getCommandSource())));
    }

    @Test
    void testHandleBalanceChange_WhenProtectModelEnabled_ShouldSkipAutoSwitchOff() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(12)
                .setAccountId(102)
                .setIsOnline(Boolean.TRUE)
                .setIsCutOff(Boolean.FALSE)
                .setProtectedModel(Boolean.TRUE)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(12)).thenReturn(meterBo);
        when(accountInfoService.getById(102)).thenReturn(new AccountBo()
                .setId(102)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(12)
                .setAccountId(102)
                .setNewBalance(new BigDecimal("-1"));

        meterBalanceChangeService.handleBalanceChange(message);

        verify(electricMeterManagerService, never()).setSwitchStatus(any(ElectricMeterSwitchStatusDto.class));
    }

    @Test
    void testHandleBalanceChange_WhenMeterOffline_ShouldSkipAutoSwitch() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(13)
                .setAccountId(103)
                .setIsOnline(Boolean.FALSE)
                .setIsCutOff(Boolean.FALSE)
                .setProtectedModel(Boolean.FALSE)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(13)).thenReturn(meterBo);
        when(accountInfoService.getById(103)).thenReturn(new AccountBo()
                .setId(103)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(13)
                .setAccountId(103)
                .setNewBalance(new BigDecimal("-1"));

        meterBalanceChangeService.handleBalanceChange(message);

        verify(electricMeterManagerService, never()).setSwitchStatus(any(ElectricMeterSwitchStatusDto.class));
    }

    @Test
    void testHandleBalanceChange_WhenNotQuantityAccount_ShouldSkipAutoSwitch() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(14)
                .setAccountId(104)
                .setIsOnline(Boolean.TRUE)
                .setIsCutOff(Boolean.FALSE)
                .setProtectedModel(Boolean.FALSE)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(14)).thenReturn(meterBo);
        when(accountInfoService.getById(104)).thenReturn(new AccountBo()
                .setId(104)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(14)
                .setAccountId(104)
                .setNewBalance(new BigDecimal("-1"));

        meterBalanceChangeService.handleBalanceChange(message);

        verify(electricMeterManagerService, never()).setSwitchStatus(any(ElectricMeterSwitchStatusDto.class));
    }

    @Test
    void testHandleBalanceChange_WhenTargetStatusUnchanged_ShouldSkipAutoSwitch() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(15)
                .setAccountId(105)
                .setIsOnline(Boolean.TRUE)
                .setIsCutOff(Boolean.TRUE)
                .setProtectedModel(Boolean.FALSE)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(15)).thenReturn(meterBo);
        when(accountInfoService.getById(105)).thenReturn(new AccountBo()
                .setId(105)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(15)
                .setAccountId(105)
                .setNewBalance(new BigDecimal("-1"));

        meterBalanceChangeService.handleBalanceChange(message);

        verify(electricMeterManagerService, never()).setSwitchStatus(any(ElectricMeterSwitchStatusDto.class));
    }

    @Test
    void testHandleBalanceChange_WhenAccountQueryThrows_ShouldSkipAutoSwitch() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(16)
                .setAccountId(106)
                .setIsOnline(Boolean.TRUE)
                .setIsCutOff(Boolean.FALSE)
                .setProtectedModel(Boolean.FALSE)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(16)).thenReturn(meterBo);
        when(accountInfoService.getById(106)).thenThrow(new RuntimeException("db error"));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(16)
                .setAccountId(106)
                .setNewBalance(new BigDecimal("-1"));

        assertDoesNotThrow(() -> meterBalanceChangeService.handleBalanceChange(message));

        verify(electricMeterManagerService, never()).setSwitchStatus(any(ElectricMeterSwitchStatusDto.class));
    }

    @Test
    void testHandleBalanceChange_WhenSetSwitchStatusThrows_ShouldSwallowException() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(17)
                .setAccountId(107)
                .setIsOnline(Boolean.TRUE)
                .setIsCutOff(Boolean.FALSE)
                .setProtectedModel(Boolean.FALSE)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(17)).thenReturn(meterBo);
        when(accountInfoService.getById(107)).thenReturn(new AccountBo()
                .setId(107)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY));
        doThrow(new RuntimeException("switch failed"))
                .when(electricMeterManagerService).setSwitchStatus(any(ElectricMeterSwitchStatusDto.class));

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(17)
                .setAccountId(107)
                .setNewBalance(new BigDecimal("-1"));

        assertDoesNotThrow(() -> meterBalanceChangeService.handleBalanceChange(message));

        verify(electricMeterManagerService).setSwitchStatus(argThat((ElectricMeterSwitchStatusDto dto) ->
                dto != null
                        && Integer.valueOf(17).equals(dto.getId())
                        && ElectricSwitchStatusEnum.OFF.equals(dto.getSwitchStatus())
                        && CommandSourceEnum.SYSTEM.equals(dto.getCommandSource())));
    }
}
