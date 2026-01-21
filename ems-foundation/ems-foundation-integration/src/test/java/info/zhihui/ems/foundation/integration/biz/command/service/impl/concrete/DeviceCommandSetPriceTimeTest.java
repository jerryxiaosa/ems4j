package info.zhihui.ems.foundation.integration.biz.command.service.impl.concrete;

import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricPriceTimeUpdateDto;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricTimeStartDto;
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
    void execute_withValidJson_shouldCallSetElectricTime() {
        DeviceCommandSetPriceTime executor = new DeviceCommandSetPriceTime(deviceModuleContext);
        String json = "{\"plan\":1,\"electricDurations\":[{\"type\":2,\"hour\":8,\"min\":0}]}";

        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(123)
                .setAreaId(1000)
                .setCommandData(json);

        when(deviceModuleContext.getService(eq(EnergyService.class), eq(1000))).thenReturn(energyService);

        assertDoesNotThrow(() -> executor.execute(bo));

        ArgumentCaptor<ElectricPriceTimeUpdateDto> captor = ArgumentCaptor.forClass(ElectricPriceTimeUpdateDto.class);
        verify(energyService).setElectricTime(captor.capture());
        ElectricPriceTimeUpdateDto dto = captor.getValue();
        assertEquals(123, dto.getDeviceId());
        assertEquals(1000, dto.getAreaId());
        assertEquals(1, dto.getPlan());
        assertNotNull(dto.getElectricDurations());
        assertEquals(1, dto.getElectricDurations().size());
        ElectricTimeStartDto t = dto.getElectricDurations().get(0);
        assertEquals(2, t.getType());
        assertEquals(8, t.getHour());
        assertEquals(0, t.getMin());
    }

    @Test
    void execute_withBlankJson_shouldThrowParamEmpty() {
        DeviceCommandSetPriceTime executor = new DeviceCommandSetPriceTime(deviceModuleContext);
        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(123)
                .setAreaId(1000)
                .setCommandData("");

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> executor.execute(bo));
        assertEquals("命令参数不能为空", ex.getMessage());

        verify(energyService, never()).setElectricTime(any());
    }

    @Test
    void execute_withInvalidJson_shouldThrowFormatError() {
        DeviceCommandSetPriceTime executor = new DeviceCommandSetPriceTime(deviceModuleContext);
        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(123)
                .setAreaId(1000)
                .setCommandData("{invalid}");

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> executor.execute(bo));
        assertEquals("命令参数格式错误", ex.getMessage());

        verify(energyService, never()).setElectricTime(any());
    }
}