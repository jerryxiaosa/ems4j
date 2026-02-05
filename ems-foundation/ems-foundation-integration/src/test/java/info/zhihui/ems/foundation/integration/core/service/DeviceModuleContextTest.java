package info.zhihui.ems.foundation.integration.core.service;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModuleConfigBo;
import info.zhihui.ems.foundation.integration.core.service.testdata.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class DeviceModuleContextTest {

    private final Map<String, CommonDeviceModule> serviceMap;

    @InjectMocks
    private DeviceModuleContext deviceModuleContext;
    private final DeviceModuleConfigService deviceConfigService = Mockito.mock(DeviceModuleConfigService.class);

    public DeviceModuleContextTest() {
        serviceMap = new HashMap<>();
        serviceMap.put(TestEnergy2ServiceImpl.class.getSimpleName().toLowerCase(), new TestEnergy2ServiceImpl());
        serviceMap.put(TestEnergy1Impl.class.getSimpleName().toLowerCase(), new TestEnergy1Impl());
        serviceMap.put(MockEnergy3ServiceImpl.class.getSimpleName().toLowerCase(), new MockEnergy3ServiceImpl());

        deviceModuleContext = new DeviceModuleContext(serviceMap, deviceConfigService);
    }

    @Test
    public void testGetServiceByModule() {
        ReflectionTestUtils.setField(deviceModuleContext, "useRealDevice", true);
        DeviceModuleConfigBo deviceModuleConfigBo = new DeviceModuleConfigBo().setImplName(TestEnergy2ServiceImpl.class.getSimpleName().toLowerCase());
        Mockito.when(deviceConfigService.getDeviceConfigByModule(TestEnergy2Service.class, 1)).thenReturn(deviceModuleConfigBo);

        TestEnergy2Service carService = deviceModuleContext.getService(TestEnergy2Service.class, 1);
        Assertions.assertEquals(serviceMap.get(TestEnergy2ServiceImpl.class.getSimpleName().toLowerCase()), carService);

        // 测试mock场景
        ReflectionTestUtils.setField(deviceModuleContext, "useRealDevice", false);
        TestEnergy3Service cameraService = deviceModuleContext.getService(TestEnergy3Service.class, 1);
        Assertions.assertEquals(serviceMap.get(MockEnergy3ServiceImpl.class.getSimpleName().toLowerCase()), cameraService);
    }


    @Test
    public void testGetModuleAndServiceName() {
        Map<String, List<String>> res = deviceModuleContext.getModuleAndServiceName();
        log.info("{}", res);

        Map<String, List<String>> expect = new HashMap<>();
        List<String> carService = new ArrayList<>();
        carService.add(TestEnergy2ServiceImpl.class.getSimpleName().toLowerCase());
        List<String> cameraService = new ArrayList<>();
        cameraService.add(MockEnergy3ServiceImpl.class.getSimpleName().toLowerCase());
        List<String> visitorService = new ArrayList<>();
        visitorService.add(TestEnergy1Impl.class.getSimpleName().toLowerCase());

        expect.put(TestEnergy2Service.class.getSimpleName(), carService);
        expect.put(TestEnergy3Service.class.getSimpleName(), cameraService);
        expect.put(TestEnergy1.class.getSimpleName(), visitorService);

        Assertions.assertEquals(expect, res);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            res.remove(TestEnergy2Service.class.getSimpleName());
        });

    }

    @Test
    public void testGetOnlyInterface_WithExtraInterface_ShouldNotThrow() {
        Map<String, CommonDeviceModule> map = new HashMap<>();
        map.put(TestEnergy1ExtraInterfaceImpl.class.getSimpleName().toLowerCase(), new TestEnergy1ExtraInterfaceImpl());
        DeviceModuleContext context = new DeviceModuleContext(map, deviceConfigService);

        Map<String, List<String>> res = context.getModuleAndServiceName();
        Assertions.assertTrue(res.containsKey(TestEnergy1.class.getSimpleName()));
    }

    @Test
    public void testGetOnlyInterface_WithMultipleModuleInterfaces_ShouldThrow() {
        Map<String, CommonDeviceModule> map = new HashMap<>();
        map.put(TestEnergyMultiModuleImpl.class.getSimpleName().toLowerCase(), new TestEnergyMultiModuleImpl());

        BusinessRuntimeException exception = Assertions.assertThrows(BusinessRuntimeException.class,
                () -> new DeviceModuleContext(map, deviceConfigService));
        Assertions.assertTrue(exception.getMessage().contains("只能实现一个模块接口"));
    }

}
