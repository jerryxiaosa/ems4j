package info.zhihui.ems.foundation.integration.biz.command.service.impl.concrete;

import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.BaseElectricDeviceDto;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceCommandTurnOnTest {

    @Mock
    private DeviceModuleContext deviceModuleContext;

    @Mock
    private EnergyService energyService;

    @Test
    void execute_shouldCallRecoverWithCorrectDto() {
        DeviceCommandTurnOn executor = new DeviceCommandTurnOn(deviceModuleContext);

        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(123)
                .setAreaId(1000);

        when(deviceModuleContext.getService(eq(EnergyService.class), eq(1000))).thenReturn(energyService);

        assertDoesNotThrow(() -> executor.execute(bo));

        ArgumentCaptor<BaseElectricDeviceDto> captor = ArgumentCaptor.forClass(BaseElectricDeviceDto.class);
        verify(energyService).recover(captor.capture());
        BaseElectricDeviceDto dto = captor.getValue();
        assertEquals(123, dto.getDeviceId());
        assertEquals(1000, dto.getAreaId());
    }
}