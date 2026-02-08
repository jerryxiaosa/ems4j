package info.zhihui.ems.iot.application;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.config.IotOnlineProperties;
import info.zhihui.ems.iot.domain.command.concrete.DailyEnergySlot;
import info.zhihui.ems.iot.domain.command.concrete.DatePlanItem;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.vo.electric.ElectricDateDurationVo;
import info.zhihui.ems.iot.vo.electric.ElectricDurationVo;
import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class DeviceVendorFacadeTest {

    @Test
    void testGetOnline_whenLastOnlineAtNull_shouldReturnFalse() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotOnlineProperties onlineProperties = new IotOnlineProperties();
        onlineProperties.setTimeoutSeconds(30);
        CommandAppService commandAppService = Mockito.mock(CommandAppService.class);
        Device device = new Device()
                .setId(1)
                .setProduct(new Product().setAccessMode(DeviceAccessModeEnum.DIRECT))
                .setLastOnlineAt(null);
        Mockito.when(deviceRegistry.getById(1)).thenReturn(device);
        DeviceVendorFacade facade = new DeviceVendorFacade(commandAppService, deviceRegistry, onlineProperties);

        Assertions.assertFalse(facade.getOnline(1));
    }

    @Test
    void testGetOnline_whenLastOnlineAtInFuture_shouldReturnFalse() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotOnlineProperties onlineProperties = new IotOnlineProperties();
        onlineProperties.setTimeoutSeconds(30);
        CommandAppService commandAppService = Mockito.mock(CommandAppService.class);
        Device device = new Device()
                .setId(1)
                .setProduct(new Product().setAccessMode(DeviceAccessModeEnum.DIRECT))
                .setLastOnlineAt(LocalDateTime.now().plusMinutes(5));
        Mockito.when(deviceRegistry.getById(1)).thenReturn(device);
        DeviceVendorFacade facade = new DeviceVendorFacade(commandAppService, deviceRegistry, onlineProperties);

        Assertions.assertFalse(facade.getOnline(1));
    }

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

    @Test
    void testGetDuration_WhenCommandSuccess_ShouldReturnDurations() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotOnlineProperties onlineProperties = new IotOnlineProperties();
        CommandAppService commandAppService = Mockito.mock(CommandAppService.class);
        List<DailyEnergySlot> slots = List.of(
                new DailyEnergySlot()
                        .setPeriod(ElectricPricePeriodEnum.HIGHER)
                        .setTime(LocalTime.of(6, 0)),
                new DailyEnergySlot()
                        .setPeriod(ElectricPricePeriodEnum.LOW)
                        .setTime(LocalTime.of(23, 59))
        );
        DeviceCommandResult result = new DeviceCommandResult()
                .setSuccess(true)
                .setData(slots);
        Mockito.when(commandAppService.sendCommand(Mockito.eq(1), Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(result));
        DeviceVendorFacade facade = new DeviceVendorFacade(commandAppService, deviceRegistry, onlineProperties);

        List<ElectricDurationVo> durations = facade.getDuration(1, 1);

        Assertions.assertEquals(2, durations.size());
        Assertions.assertEquals(ElectricPricePeriodEnum.HIGHER.getCode(), durations.get(0).getPeriod());
        Assertions.assertEquals("06", durations.get(0).getHour());
        Assertions.assertEquals("00", durations.get(0).getMin());
        Assertions.assertEquals(ElectricPricePeriodEnum.LOW.getCode(), durations.get(1).getPeriod());
        Assertions.assertEquals("23", durations.get(1).getHour());
        Assertions.assertEquals("59", durations.get(1).getMin());
    }

    @Test
    void testGetDuration_WhenDataInvalid_ShouldThrow() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotOnlineProperties onlineProperties = new IotOnlineProperties();
        CommandAppService commandAppService = Mockito.mock(CommandAppService.class);
        DeviceCommandResult result = new DeviceCommandResult()
                .setSuccess(true)
                .setData(List.of("bad"));
        Mockito.when(commandAppService.sendCommand(Mockito.eq(1), Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(result));
        DeviceVendorFacade facade = new DeviceVendorFacade(commandAppService, deviceRegistry, onlineProperties);

        Assertions.assertThrows(BusinessRuntimeException.class, () -> facade.getDuration(1, 1));
    }

    @Test
    void testGetDateDuration_WhenCommandSuccess_ShouldReturnDurations() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotOnlineProperties onlineProperties = new IotOnlineProperties();
        CommandAppService commandAppService = Mockito.mock(CommandAppService.class);
        List<DatePlanItem> items = List.of(
                new DatePlanItem().setMonth("1").setDay("2").setDailyPlanId("3"),
                new DatePlanItem().setMonth("4").setDay("5").setDailyPlanId("6")
        );
        DeviceCommandResult result = new DeviceCommandResult()
                .setSuccess(true)
                .setData(items);
        Mockito.when(commandAppService.sendCommand(Mockito.eq(1), Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(result));
        DeviceVendorFacade facade = new DeviceVendorFacade(commandAppService, deviceRegistry, onlineProperties);

        List<ElectricDateDurationVo> durations = facade.getDateDuration(1);

        Assertions.assertEquals(2, durations.size());
        Assertions.assertEquals("1", durations.get(0).getMonth());
        Assertions.assertEquals("2", durations.get(0).getDay());
        Assertions.assertEquals("3", durations.get(0).getDailyPlanId());
        Assertions.assertEquals("4", durations.get(1).getMonth());
        Assertions.assertEquals("5", durations.get(1).getDay());
        Assertions.assertEquals("6", durations.get(1).getDailyPlanId());
    }

    @Test
    void testGetDateDuration_WhenDataInvalid_ShouldThrow() {
        DeviceRegistry deviceRegistry = Mockito.mock(DeviceRegistry.class);
        IotOnlineProperties onlineProperties = new IotOnlineProperties();
        CommandAppService commandAppService = Mockito.mock(CommandAppService.class);
        DeviceCommandResult result = new DeviceCommandResult()
                .setSuccess(true)
                .setData(List.of("bad"));
        Mockito.when(commandAppService.sendCommand(Mockito.eq(1), Mockito.any()))
                .thenReturn(CompletableFuture.completedFuture(result));
        DeviceVendorFacade facade = new DeviceVendorFacade(commandAppService, deviceRegistry, onlineProperties);

        Assertions.assertThrows(BusinessRuntimeException.class, () -> facade.getDateDuration(1));
    }
}
