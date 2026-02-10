package info.zhihui.ems.foundation.integration.biz.command.service.impl.concrete;

import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.model.energy.DailyEnergySlot;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.DailyEnergyPlanUpdateDto;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceCommandSetPriceTimeTest {

    @Mock
    private DeviceModuleContext deviceModuleContext;

    @Mock
    private EnergyService energyService;

    @Test
    void execute_withValidJson_shouldCallSetDuration() {
        DeviceCommandSetPriceTime executor = new DeviceCommandSetPriceTime(deviceModuleContext);
        String json = "{\"dailyPlanId\":1,\"slots\":[{\"period\":2,\"time\":\"08:00:00\"}]}";

        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(123)
                .setDeviceIotId("123")
                .setAreaId(1000)
                .setCommandData(json);

        when(deviceModuleContext.getService(eq(EnergyService.class), eq(1000))).thenReturn(energyService);

        assertDoesNotThrow(() -> executor.execute(bo));

        ArgumentCaptor<DailyEnergyPlanUpdateDto> captor = ArgumentCaptor.forClass(DailyEnergyPlanUpdateDto.class);
        verify(energyService).setDuration(captor.capture());
        DailyEnergyPlanUpdateDto dto = captor.getValue();
        assertEquals("123", dto.getDeviceId());
        assertEquals(1000, dto.getAreaId());
        assertEquals(1, dto.getDailyPlanId());
        assertNotNull(dto.getSlots());
        assertEquals(1, dto.getSlots().size());
        DailyEnergySlot slot = dto.getSlots().get(0);
        assertEquals(ElectricPricePeriodEnum.HIGH, slot.getPeriod());
        assertEquals("08:00", slot.getTime().toString());
    }

    @Test
    void execute_withBlankJson_shouldThrowParamEmpty() {
        DeviceCommandSetPriceTime executor = new DeviceCommandSetPriceTime(deviceModuleContext);
        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(123)
                .setDeviceIotId("123")
                .setAreaId(1000)
                .setCommandData("");

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> executor.execute(bo));
        assertEquals("命令参数不能为空", ex.getMessage());

        verify(energyService, never()).setDuration(any());
    }

    @Test
    void execute_withInvalidJson_shouldThrowFormatError() {
        DeviceCommandSetPriceTime executor = new DeviceCommandSetPriceTime(deviceModuleContext);
        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(123)
                .setDeviceIotId("123")
                .setAreaId(1000)
                .setCommandData("{invalid}");

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> executor.execute(bo));
        assertEquals("命令参数格式错误", ex.getMessage());

        verify(energyService, never()).setDuration(any());
    }
}
