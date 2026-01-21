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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceCommandTurnOffTest {

    @Mock
    private DeviceModuleContext deviceModuleContext;

    @Mock
    private EnergyService energyService;

    @Test
    void execute_shouldCallCutOffWithCorrectDto() {
        DeviceCommandTurnOff executor = new DeviceCommandTurnOff(deviceModuleContext);

        DeviceCommandRecordBo bo = new DeviceCommandRecordBo()
                .setDeviceId(456)
                .setAreaId(2000);

        when(deviceModuleContext.getService(eq(EnergyService.class), eq(2000))).thenReturn(energyService);

        assertDoesNotThrow(() -> executor.execute(bo));

        ArgumentCaptor<BaseElectricDeviceDto> captor = ArgumentCaptor.forClass(BaseElectricDeviceDto.class);
        verify(energyService).cutOff(captor.capture());
        BaseElectricDeviceDto dto = captor.getValue();
        assertEquals(456, dto.getDeviceId());
        assertEquals(2000, dto.getAreaId());
    }
}