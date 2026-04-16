package info.zhihui.ems.iot.application;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.iot.config.ChannelManagerProperties;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelSession;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.PendingTask;
import info.zhihui.ems.iot.vo.IotClientDetailVo;
import info.zhihui.ems.iot.vo.IotClientSimpleVo;
import io.netty.channel.DefaultChannelId;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class IotDebugAppServiceTest {

    @Test
    void testFindClientList_ShouldMapSnapshotAndSortByDeviceNo() {
        ChannelManager channelManager = new ChannelManager(new ChannelManagerProperties());
        ChannelSession secondSession = new ChannelSession()
                .setDeviceNo("dev-2")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(new EmbeddedChannel(DefaultChannelId.newInstance()));
        ChannelSession firstSession = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(new EmbeddedChannel(DefaultChannelId.newInstance()));
        firstSession.setPendingFuture(new java.util.concurrent.CompletableFuture<>());
        firstSession.getQueue().offer(new PendingTask("dev-1", new byte[]{1}, new java.util.concurrent.CompletableFuture<>(), true));
        firstSession.getAbnormalTimestamps().addLast(1L);
        channelManager.register(secondSession);
        channelManager.register(firstSession);

        IotDebugAppService iotDebugAppService = new IotDebugAppService(channelManager, new FixedDeviceRegistry(null, false));

        List<IotClientSimpleVo> result = iotDebugAppService.findClientList();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("dev-1", result.get(0).getDeviceNo());
        Assertions.assertTrue(result.get(0).isPending());
        Assertions.assertEquals(1, result.get(0).getQueueSize());
        Assertions.assertEquals(1, result.get(0).getAbnormalCount());
        Assertions.assertEquals("dev-2", result.get(1).getDeviceNo());
    }

    @Test
    void testGetClientDetail_WhenDeviceExists_ShouldMergeRuntimeAndDeviceInfo() {
        ChannelManager channelManager = new ChannelManager(new ChannelManagerProperties());
        ChannelSession session = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(new EmbeddedChannel());
        session.setPendingFuture(new java.util.concurrent.CompletableFuture<>());
        channelManager.register(session);
        Device device = new Device()
                .setId(1)
                .setDeviceNo("dev-1")
                .setParentId(9)
                .setPortNo(2)
                .setMeterAddress(3)
                .setLastOnlineAt(LocalDateTime.of(2026, 4, 14, 21, 30))
                .setProduct(new Product()
                        .setCode("ACREL_DTSY_1352_4G")
                        .setAccessMode(DeviceAccessModeEnum.DIRECT));
        IotDebugAppService iotDebugAppService = new IotDebugAppService(channelManager, new FixedDeviceRegistry(device, false));

        IotClientDetailVo result = iotDebugAppService.getClientDetail("dev-1");

        Assertions.assertEquals("dev-1", result.getDeviceNo());
        Assertions.assertEquals("ACREL_DTSY_1352_4G", result.getProductCode());
        Assertions.assertEquals(DeviceAccessModeEnum.DIRECT, result.getAccessMode());
        Assertions.assertEquals(9, result.getParentId());
        Assertions.assertEquals(2, result.getPortNo());
        Assertions.assertEquals(3, result.getMeterAddress());
        Assertions.assertEquals(LocalDateTime.of(2026, 4, 14, 21, 30), result.getLastOnlineAt());
    }

    @Test
    void testGetClientDetail_WhenDeviceMissing_ShouldStillReturnRuntimeInfo() {
        ChannelManager channelManager = new ChannelManager(new ChannelManagerProperties());
        ChannelSession session = new ChannelSession()
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setChannel(new EmbeddedChannel());
        channelManager.register(session);
        IotDebugAppService iotDebugAppService = new IotDebugAppService(channelManager, new FixedDeviceRegistry(null, true));

        IotClientDetailVo result = iotDebugAppService.getClientDetail("dev-1");

        Assertions.assertEquals("dev-1", result.getDeviceNo());
        Assertions.assertNull(result.getProductCode());
        Assertions.assertNull(result.getAccessMode());
    }

    @Test
    void testGetClientDetail_WhenDeviceNoMissing_ShouldThrowNotFound() {
        IotDebugAppService iotDebugAppService = new IotDebugAppService(
                new ChannelManager(new ChannelManagerProperties()), new FixedDeviceRegistry(null, false));

        Assertions.assertThrows(NotFoundException.class, () -> iotDebugAppService.getClientDetail("missing"));
    }

    private static final class FixedDeviceRegistry implements DeviceRegistry {

        private final Device device;
        private final boolean throwNotFound;

        private FixedDeviceRegistry(Device device, boolean throwNotFound) {
            this.device = device;
            this.throwNotFound = throwNotFound;
        }

        @Override
        public Integer save(Device device) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(Device device) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(Integer id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Device getById(Integer id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Device getByDeviceNo(String deviceNo) {
            if (throwNotFound) {
                throw new NotFoundException("设备记录不存在");
            }
            return device;
        }

        @Override
        public Device getByParentIdAndPortNoAndMeterAddress(Integer parentId, Integer portNo, Integer meterAddress) {
            throw new UnsupportedOperationException();
        }
    }
}
