package info.zhihui.ems.business.device.service.impl;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterSwitchStatusDto;
import info.zhihui.ems.business.device.dto.MeterBalanceChangeDto;
import info.zhihui.ems.business.device.enums.ElectricSwitchStatusEnum;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeterBalanceChangeServiceImplTest {

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @Mock
    private ElectricMeterManagerService electricMeterManagerService;

    @Mock
    private WarnPlanService warnPlanService;

    @InjectMocks
    private MeterBalanceChangeServiceImpl meterBalanceChangeService;

    @Test
    void testHandleBalanceChange_WhenNeedHandleSwitchStatusIsFalse_ShouldOnlyHandleWarnType() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(6)
                .setWarnPlanId(8)
                .setWarnType(WarnTypeEnum.NONE)
                .setIsOnline(Boolean.TRUE)
                .setIsCutOff(Boolean.FALSE)
                .setProtectedModel(Boolean.FALSE);
        when(electricMeterInfoService.getDetail(6)).thenReturn(meterBo);
        when(warnPlanService.getDetail(8)).thenReturn(new WarnPlanBo()
                .setFirstLevel(new BigDecimal("100"))
                .setSecondLevel(new BigDecimal("30")));

        MeterBalanceChangeDto dto = new MeterBalanceChangeDto()
                .setMeterId(6)
                .setNewBalance(new BigDecimal("50"))
                .setNeedHandleSwitchStatus(false);

        meterBalanceChangeService.handleBalanceChange(dto);

        verify(electricMeterManagerService).setMeterWarnLevel(eq(List.of(6)), eq(WarnTypeEnum.FIRST));
        verify(electricMeterManagerService, never()).setSwitchStatus(any(ElectricMeterSwitchStatusDto.class));
    }

    @Test
    void testHandleBalanceChange_WhenNeedHandleSwitchStatusIsTrueAndBalanceLessOrEqualZero_ShouldSwitchOff() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(10)
                .setIsOnline(Boolean.TRUE)
                .setIsCutOff(Boolean.FALSE)
                .setProtectedModel(Boolean.FALSE)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(10)).thenReturn(meterBo);

        MeterBalanceChangeDto dto = new MeterBalanceChangeDto()
                .setMeterId(10)
                .setNewBalance(BigDecimal.ZERO)
                .setNeedHandleSwitchStatus(true);

        meterBalanceChangeService.handleBalanceChange(dto);

        verify(electricMeterManagerService).setSwitchStatus(argThat((ElectricMeterSwitchStatusDto switchStatusDto) ->
                switchStatusDto != null
                        && Integer.valueOf(10).equals(switchStatusDto.getId())
                        && ElectricSwitchStatusEnum.OFF.equals(switchStatusDto.getSwitchStatus())
                        && CommandSourceEnum.SYSTEM.equals(switchStatusDto.getCommandSource())));
    }

    @Test
    void testHandleBalanceChange_WhenProtectedModelEnabled_ShouldSkipAutoSwitchOff() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(12)
                .setIsOnline(Boolean.TRUE)
                .setIsCutOff(Boolean.FALSE)
                .setProtectedModel(Boolean.TRUE)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(12)).thenReturn(meterBo);

        MeterBalanceChangeDto dto = new MeterBalanceChangeDto()
                .setMeterId(12)
                .setNewBalance(new BigDecimal("-1"))
                .setNeedHandleSwitchStatus(true);

        meterBalanceChangeService.handleBalanceChange(dto);

        verify(electricMeterManagerService, never()).setSwitchStatus(any(ElectricMeterSwitchStatusDto.class));
    }

    @Test
    void testHandleBalanceChange_WhenMeterOffline_ShouldSkipAutoSwitch() {
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(13)
                .setIsOnline(Boolean.FALSE)
                .setIsCutOff(Boolean.FALSE)
                .setProtectedModel(Boolean.FALSE)
                .setWarnPlanId(null);
        when(electricMeterInfoService.getDetail(13)).thenReturn(meterBo);

        MeterBalanceChangeDto dto = new MeterBalanceChangeDto()
                .setMeterId(13)
                .setNewBalance(new BigDecimal("-1"))
                .setNeedHandleSwitchStatus(true);

        assertDoesNotThrow(() -> meterBalanceChangeService.handleBalanceChange(dto));

        verify(electricMeterManagerService, never()).setSwitchStatus(any(ElectricMeterSwitchStatusDto.class));
    }
}
