package info.zhihui.ems.iot.api;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.iot.api.handler.RuntimeExceptionHandler;
import info.zhihui.ems.iot.application.IotDebugAppService;
import info.zhihui.ems.iot.config.ChannelManagerProperties;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.vo.IotClientDetailVo;
import info.zhihui.ems.iot.vo.IotClientSimpleVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IotDebugControllerTest {

    private MockMvc mockMvc;
    private StubIotDebugAppService iotDebugAppService;

    @BeforeEach
    void setUp() {
        iotDebugAppService = new StubIotDebugAppService();
        mockMvc = MockMvcBuilders.standaloneSetup(new IotDebugController(iotDebugAppService))
                .setControllerAdvice(new RuntimeExceptionHandler())
                .build();
    }

    @Test
    void testFindClientList_ShouldReturnSuccess() throws Exception {
        iotDebugAppService.clientList = List.of(new IotClientSimpleVo()
                .setChannelId("channel-a")
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setActive(true)
                .setPending(false)
                .setQueueSize(0)
                .setAbnormalCount(0)
                .setRemoteAddress("127.0.0.1:1001"));

        mockMvc.perform(get("/api/debug/iot/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].channelId").value("channel-a"))
                .andExpect(jsonPath("$.data[0].deviceNo").value("dev-1"))
                .andExpect(jsonPath("$.data[0].remoteAddress").value("127.0.0.1:1001"));
    }

    @Test
    void testGetClientDetail_ShouldReturnSuccess() throws Exception {
        iotDebugAppService.clientDetail = new IotClientDetailVo()
                .setChannelId("channel-a")
                .setDeviceNo("dev-1")
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setActive(true)
                .setPending(true)
                .setQueueSize(1)
                .setAbnormalCount(2)
                .setRemoteAddress("127.0.0.1:1001")
                .setLocalAddress("127.0.0.1:8880")
                .setProductCode("ACREL_DTSY_1352_4G");

        mockMvc.perform(get("/api/debug/iot/clients/dev-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.channelId").value("channel-a"))
                .andExpect(jsonPath("$.data.deviceNo").value("dev-1"))
                .andExpect(jsonPath("$.data.productCode").value("ACREL_DTSY_1352_4G"));
    }

    private static final class StubIotDebugAppService extends IotDebugAppService {

        private List<IotClientSimpleVo> clientList = List.of();
        private IotClientDetailVo clientDetail;

        private StubIotDebugAppService() {
            super(new ChannelManager(new ChannelManagerProperties()), new EmptyDeviceRegistry());
        }

        @Override
        public List<IotClientSimpleVo> findClientList() {
            return clientList;
        }

        @Override
        public IotClientDetailVo getClientDetail(String deviceNo) {
            return clientDetail;
        }
    }

    private static final class EmptyDeviceRegistry implements DeviceRegistry {

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
            throw new UnsupportedOperationException();
        }

        @Override
        public Device getByParentIdAndPortNoAndMeterAddress(Integer parentId, Integer portNo, Integer meterAddress) {
            throw new UnsupportedOperationException();
        }
    }
}
