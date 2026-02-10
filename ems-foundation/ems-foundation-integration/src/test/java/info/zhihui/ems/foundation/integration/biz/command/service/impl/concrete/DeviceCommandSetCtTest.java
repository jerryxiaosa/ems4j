package info.zhihui.ems.foundation.integration.biz.command.service.impl.concrete;

import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricDeviceCTDto;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceCommandSetCtTest {

    @Mock
    private DeviceModuleContext deviceModuleContext;

    @Mock
    private EnergyService energyService;

    @Test
    void execute_withValidJson_shouldCallSetElectricCt() {
        DeviceCommandSetCt executor = new DeviceCommandSetCt(deviceModuleContext);
        String json = "100";

        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(789)
                .setDeviceIotId("789")
                .setAreaId(3000)
                .setCommandData(json);

        when(deviceModuleContext.getService(eq(EnergyService.class), eq(3000))).thenReturn(energyService);

        assertDoesNotThrow(() -> executor.execute(bo));

        ArgumentCaptor<ElectricDeviceCTDto> captor = ArgumentCaptor.forClass(ElectricDeviceCTDto.class);
        verify(energyService).setElectricCt(captor.capture());
        ElectricDeviceCTDto dto = captor.getValue();
        assertEquals("789", dto.getDeviceId());
        assertEquals(3000, dto.getAreaId());
        assertEquals(100, dto.getCt());
    }

    @Test
    void execute_withBlankJson_shouldThrowParamEmpty() {
        DeviceCommandSetCt executor = new DeviceCommandSetCt(deviceModuleContext);
        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(789)
                .setDeviceIotId("789")
                .setAreaId(3000)
                .setCommandData("");

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> executor.execute(bo));
        assertEquals("命令参数不能为空", ex.getMessage());
        verify(energyService, never()).setElectricCt(any());
    }

    @Test
    void execute_withInvalidJson_shouldThrowFormatError() {
        DeviceCommandSetCt executor = new DeviceCommandSetCt(deviceModuleContext);
        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(789)
                .setDeviceIotId("789")
                .setAreaId(3000)
                .setCommandData("{invalid}");

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> executor.execute(bo));
        assertEquals("命令参数格式错误", ex.getMessage());
        verify(energyService, never()).setElectricCt(any());
    }

    @Test
    void execute_withNonPositiveCt_shouldThrowFormatError() {
        DeviceCommandSetCt executor = new DeviceCommandSetCt(deviceModuleContext);
        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(789)
                .setDeviceIotId("789")
                .setAreaId(3000)
                .setCommandData("0");

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> executor.execute(bo));
        assertEquals("命令参数格式错误", ex.getMessage());
        verify(energyService, never()).setElectricCt(any());
    }
}
