package info.zhihui.ems.iot.application;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.config.IotOnlineProperties;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

class DeviceVendorFacadeTest {

    @Test
    void testGetCt_WhenCommandSuccess_ShouldReturnValue() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotOnlineProperties onlineProperties = new IotOnlineProperties();
        CommandAppService commandAppService = Mockito.mock(CommandAppService.class);
        DeviceCommandResult result = new DeviceCommandResult()
                .setSuccess(true)
                .setData(5);
        Mockito.when(commandAppService.sendCommand(Mockito.eq(1), Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(result));
        DeviceVendorFacade facade = new DeviceVendorFacade(commandAppService, deviceRegistry, onlineProperties);

        Assertions.assertEquals(5, facade.getCt(1));
    }

    @Test
    void testSetCt_WhenCommandFailed_ShouldThrow() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotOnlineProperties onlineProperties = new IotOnlineProperties();
        CommandAppService commandAppService = Mockito.mock(CommandAppService.class);
        DeviceCommandResult result = new DeviceCommandResult()
                .setSuccess(false)
                .setErrorMessage("failed");
        Mockito.when(commandAppService.sendCommand(Mockito.eq(1), Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(result));
        DeviceVendorFacade facade = new DeviceVendorFacade(commandAppService, deviceRegistry, onlineProperties);

        Assertions.assertThrows(BusinessRuntimeException.class, () -> facade.setCt(1, 5));
    }

    @Test
    void testCutPower_WhenCommandThrows_ShouldThrow() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotOnlineProperties onlineProperties = new IotOnlineProperties();
        CommandAppService commandAppService = Mockito.mock(CommandAppService.class);
        CompletableFuture<DeviceCommandResult> future = new CompletableFuture<>();
        future.completeExceptionally(new IllegalStateException("send-fail"));
        Mockito.when(commandAppService.sendCommand(Mockito.eq(1), Mockito.any()))
                .thenReturn(future);
        DeviceVendorFacade facade = new DeviceVendorFacade(commandAppService, deviceRegistry, onlineProperties);

        Assertions.assertThrows(BusinessRuntimeException.class, () -> facade.cutPower(1));
    }
}
