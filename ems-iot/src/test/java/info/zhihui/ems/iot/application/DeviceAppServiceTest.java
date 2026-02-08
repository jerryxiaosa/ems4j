package info.zhihui.ems.iot.application;

import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.vo.DeviceSaveVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeviceAppServiceTest {

    @Mock
    private DeviceRegistry deviceRegistry;

    @InjectMocks
    private DeviceAppService deviceAppService;

    @Test
    void testUpdateDevice_ShouldMapBodyAndCallUpdate() {
        DeviceSaveVo body = new DeviceSaveVo()
                .setDeviceNo("dev-1")
                .setPortNo(1)
                .setMeterAddress(2)
                .setDeviceSecret("secret")
                .setSlaveAddress(3)
                .setProductCode("acrel_gateway")
                .setParentId(10);

        deviceAppService.updateDevice(1, body);

        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        org.mockito.Mockito.verify(deviceRegistry).update(captor.capture());
        org.mockito.Mockito.verify(deviceRegistry, org.mockito.Mockito.never()).getById(org.mockito.Mockito.anyInt());
        Device updated = captor.getValue();
        Assertions.assertNull(updated.getLastOnlineAt());
        Assertions.assertEquals(1, updated.getId());
        Assertions.assertEquals("dev-1", updated.getDeviceNo());
        Assertions.assertEquals("acrel_gateway", updated.getProduct().getCode());
    }

    @Test
    void testUpdateDevice_WhenRegistryUpdateThrowsNotFound_ShouldPropagate() {
        DeviceSaveVo body = new DeviceSaveVo()
                .setDeviceNo("dev-missing")
                .setProductCode("acrel_gateway");
        NotFoundException notFound = new NotFoundException("设备记录不存在，id=999");
        org.mockito.Mockito.doThrow(notFound).when(deviceRegistry).update(org.mockito.Mockito.any(Device.class));

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> deviceAppService.updateDevice(999, body));

        Assertions.assertEquals("设备记录不存在，id=999", exception.getMessage());
    }
}
