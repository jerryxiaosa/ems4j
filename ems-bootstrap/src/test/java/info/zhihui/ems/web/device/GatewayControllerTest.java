package info.zhihui.ems.web.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.device.biz.GatewayBiz;
import info.zhihui.ems.web.device.controller.GatewayController;
import info.zhihui.ems.web.device.vo.GatewayAddVo;
import info.zhihui.ems.web.device.vo.GatewayDetailVo;
import info.zhihui.ems.web.device.vo.GatewayMeterVo;
import info.zhihui.ems.web.device.vo.GatewayOnlineStatusVo;
import info.zhihui.ems.web.device.vo.GatewayUpdateVo;
import info.zhihui.ems.web.device.vo.GatewayVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GatewayController.class)
class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GatewayBiz gatewayBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("分页查询网关")
    void testFindGatewayPage() throws Exception {
        GatewayVo vo = new GatewayVo()
                .setId(1)
                .setGatewayName("GatewayA")
                .setSpaceName("101房间")
                .setSpaceParentNames(List.of("1号楼", "1层"))
                .setModelName("网关型号A");
        PageResult<GatewayVo> pageResult = new PageResult<GatewayVo>()
                .setList(List.of(vo))
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L);
        when(gatewayBiz.findGatewayPage(any(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/v1/device/gateways/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].gatewayName").value("GatewayA"))
                .andExpect(jsonPath("$.data.list[0].spaceName").value("101房间"))
                .andExpect(jsonPath("$.data.list[0].spaceParentNames[0]").value("1号楼"))
                .andExpect(jsonPath("$.data.list[0].modelName").value("网关型号A"));
    }

    @Test
    @DisplayName("获取网关详情")
    void testGetGateway() throws Exception {
        GatewayDetailVo detailVo = new GatewayDetailVo();
        detailVo.setId(1);
        detailVo.setGatewayName("GatewayA");
        detailVo.setSpaceName("101房间");
        detailVo.setSpaceParentNames(List.of("1号楼", "1层"));
        detailVo.setModelName("网关型号A");
        detailVo.setMeterList(List.of(new GatewayMeterVo()
                .setId(100)
                .setMeterName("电表A")
                .setDeviceNo("DEVICE-001")
                .setIsOnline(true)
                .setPortNo(1)
                .setMeterAddress(11)));
        when(gatewayBiz.getGateway(1)).thenReturn(detailVo);

        mockMvc.perform(get("/v1/device/gateways/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gatewayName").value("GatewayA"))
                .andExpect(jsonPath("$.data.spaceName").value("101房间"))
                .andExpect(jsonPath("$.data.spaceParentNames[1]").value("1层"))
                .andExpect(jsonPath("$.data.modelName").value("网关型号A"))
                .andExpect(jsonPath("$.data.meterList[0].meterName").value("电表A"))
                .andExpect(jsonPath("$.data.meterList[0].deviceNo").value("DEVICE-001"))
                .andExpect(jsonPath("$.data.meterList[0].portNo").value(1))
                .andExpect(jsonPath("$.data.meterList[0].meterAddress").value(11));
    }

    @Test
    @DisplayName("新增网关")
    void testAddGateway() throws Exception {
        when(gatewayBiz.addGateway(any(GatewayAddVo.class))).thenReturn(200);

        GatewayAddVo addVo = new GatewayAddVo()
                .setSpaceId(1)
                .setGatewayName("GatewayA")
                .setConfigInfo("{}")
                .setModelId(2)
                .setDeviceNo("SN-001");

        mockMvc.perform(post("/v1/device/gateways")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(200));
    }

    @Test
    @DisplayName("更新网关")
    void testUpdateGateway() throws Exception {
        GatewayUpdateVo saveVo = new GatewayUpdateVo()
                .setSpaceId(1)
                .setGatewayName("GatewayA")
                .setConfigInfo("{}")
                .setModelId(2)
                .setDeviceNo("SN-001");

        mockMvc.perform(put("/v1/device/gateways/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("同步网关在线状态")
    void testSyncOnlineStatus() throws Exception {
        GatewayOnlineStatusVo onlineStatusVo = new GatewayOnlineStatusVo();
        onlineStatusVo.setGatewayId(1);
        onlineStatusVo.setForce(Boolean.TRUE);
        onlineStatusVo.setOnlineStatus(Boolean.FALSE);

        doNothing().when(gatewayBiz).syncOnlineStatus(any(GatewayOnlineStatusVo.class));

        mockMvc.perform(put("/v1/device/gateways/online-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlineStatusVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(gatewayBiz).syncOnlineStatus(any(GatewayOnlineStatusVo.class));
    }
}
