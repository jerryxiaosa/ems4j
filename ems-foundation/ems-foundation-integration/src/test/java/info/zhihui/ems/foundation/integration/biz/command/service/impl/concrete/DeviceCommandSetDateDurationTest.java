package info.zhihui.ems.foundation.integration.biz.command.service.impl.concrete;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.model.energy.DatePlanItem;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.DateEnergyPlanUpdateDto;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.MonthDay;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceCommandSetDateDurationTest {

    @Mock
    private DeviceModuleContext deviceModuleContext;

    @Mock
    private EnergyService energyService;

    @Test
    void execute_withValidJson_shouldCallSetDateDuration() {
        DeviceCommandSetDateDuration executor = new DeviceCommandSetDateDuration(deviceModuleContext);
        String json = "{\"items\":[{\"date\":\"08-01\",\"dailyPlanId\":\"2\"}]}";

        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(456)
                .setDeviceIotId("456")
                .setAreaId(2000)
                .setCommandData(json);

        when(deviceModuleContext.getService(eq(EnergyService.class), eq(2000))).thenReturn(energyService);

        assertDoesNotThrow(() -> executor.execute(bo));

        ArgumentCaptor<DateEnergyPlanUpdateDto> captor = ArgumentCaptor.forClass(DateEnergyPlanUpdateDto.class);
        verify(energyService).setDateDuration(captor.capture());
        DateEnergyPlanUpdateDto dto = captor.getValue();
        assertEquals("456", dto.getDeviceId());
        assertEquals(2000, dto.getAreaId());
        assertNotNull(dto.getItems());
        assertEquals(1, dto.getItems().size());
        DatePlanItem item = dto.getItems().get(0);
        assertEquals(MonthDay.of(8, 1), item.getDate());
        assertEquals("2", item.getDailyPlanId());
    }

    @Test
    void execute_withLegacyMonthDayFormat_shouldCallSetDateDuration() {
        DeviceCommandSetDateDuration executor = new DeviceCommandSetDateDuration(deviceModuleContext);
        String json = "{\"items\":[{\"date\":\"--08-01\",\"dailyPlanId\":\"2\"}]}";

        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(456)
                .setDeviceIotId("456")
                .setAreaId(2000)
                .setCommandData(json);

        when(deviceModuleContext.getService(eq(EnergyService.class), eq(2000))).thenReturn(energyService);

        assertDoesNotThrow(() -> executor.execute(bo));

        ArgumentCaptor<DateEnergyPlanUpdateDto> captor = ArgumentCaptor.forClass(DateEnergyPlanUpdateDto.class);
        verify(energyService).setDateDuration(captor.capture());
        DateEnergyPlanUpdateDto dto = captor.getValue();
        assertNotNull(dto.getItems());
        assertEquals(1, dto.getItems().size());
        assertEquals(MonthDay.of(8, 1), dto.getItems().get(0).getDate());
    }

    @Test
    void execute_withBlankJson_shouldThrowParamEmpty() {
        DeviceCommandSetDateDuration executor = new DeviceCommandSetDateDuration(deviceModuleContext);
        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(456)
                .setDeviceIotId("456")
                .setAreaId(2000)
                .setCommandData("");

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> executor.execute(bo));
        assertEquals("命令参数不能为空", ex.getMessage());

        verify(energyService, never()).setDateDuration(any());
    }

    @Test
    void execute_withInvalidJson_shouldThrowFormatError() {
        DeviceCommandSetDateDuration executor = new DeviceCommandSetDateDuration(deviceModuleContext);
        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(456)
                .setDeviceIotId("456")
                .setAreaId(2000)
                .setCommandData("{invalid}");

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> executor.execute(bo));
        assertEquals("命令参数格式错误", ex.getMessage());

        verify(energyService, never()).setDateDuration(any());
    }
}
