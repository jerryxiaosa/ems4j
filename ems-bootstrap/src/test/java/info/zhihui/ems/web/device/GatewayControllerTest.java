package info.zhihui.ems.web.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.device.biz.GatewayBiz;
import info.zhihui.ems.web.device.controller.GatewayController;
import info.zhihui.ems.web.device.vo.GatewayAddVo;
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
        GatewayVo vo = new GatewayVo().setId(1).setGatewayName("GatewayA");
        PageResult<GatewayVo> pageResult = new PageResult<GatewayVo>()
                .setList(List.of(vo))
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L);
        when(gatewayBiz.findGatewayPage(any(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/device/gateways/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].gatewayName").value("GatewayA"));
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

        mockMvc.perform(post("/device/gateways")
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

        mockMvc.perform(put("/device/gateways/1")
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

        mockMvc.perform(put("/device/gateways/online-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onlineStatusVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(gatewayBiz).syncOnlineStatus(any(GatewayOnlineStatusVo.class));
    }

    @Test
    @DisplayName("获取通信方式")
    void testFindCommunicationOptions() throws Exception {
        when(gatewayBiz.findCommunicationOptions()).thenReturn(List.of("4G", "NB"));

        mockMvc.perform(get("/device/gateways/communication-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0]").value("4G"));
    }
}
