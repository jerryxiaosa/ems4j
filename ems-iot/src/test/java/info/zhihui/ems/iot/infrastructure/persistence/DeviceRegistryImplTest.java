package info.zhihui.ems.iot.infrastructure.persistence;

import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class DeviceRegistryImplTest {

    private final DeviceRegistryImpl deviceRegistry = new DeviceRegistryImpl(null);

    @Test
    void testSave_WhenDeviceIsNull_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> deviceRegistry.save(null)
        );

        Assertions.assertEquals("设备不能为空", exception.getMessage());
    }

    @Test
    void testSave_WhenProductCodeIsBlank_ShouldThrowIllegalArgumentException() {
        Device device = new Device()
                .setDeviceNo("dev-1")
                .setProduct(new Product().setCode(" "));

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> deviceRegistry.save(device)
        );

        Assertions.assertEquals("产品编码不能为空", exception.getMessage());
    }

    @Test
    void testRequireProductEnum_WhenProductCodeIsBlank_ShouldThrowIllegalArgumentException() {
        Throwable throwable = invokeRequireProductEnum(" ");

        Assertions.assertInstanceOf(IllegalArgumentException.class, throwable);
        Assertions.assertEquals("产品编码不能为空", throwable.getMessage());
    }

    private Throwable invokeRequireProductEnum(String productCode) {
        try {
            Method method = DeviceRegistryImpl.class.getDeclaredMethod("requireProductEnum", String.class);
            method.setAccessible(true);
            method.invoke(deviceRegistry, productCode);
            return null;
        } catch (InvocationTargetException exception) {
            return exception.getTargetException();
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("反射调用 requireProductEnum 失败", exception);
        }
    }
}

