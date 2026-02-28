package info.zhihui.ems.web.device;

import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.device.biz.DeviceTypeBiz;
import info.zhihui.ems.web.device.controller.DeviceTypeController;
import info.zhihui.ems.web.device.vo.DeviceTypeTreeVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceTypeController.class)
class DeviceTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceTypeBiz deviceTypeBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("查询设备品类树_成功返回树结构")
    void testFindDeviceTypeTree_Success() throws Exception {
        DeviceTypeTreeVo child = new DeviceTypeTreeVo()
                .setId(2)
                .setPid(1)
                .setTypeName("单相电表")
                .setTypeKey("single")
                .setLevel(2)
                .setChildren(Collections.emptyList());
        DeviceTypeTreeVo root = new DeviceTypeTreeVo()
                .setId(1)
                .setPid(0)
                .setTypeName("电表")
                .setTypeKey("electric")
                .setLevel(1)
                .setChildren(List.of(child));
        when(deviceTypeBiz.findDeviceTypeTree()).thenReturn(List.of(root));

        mockMvc.perform(get("/device/device-types/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].children[0].id").value(2));
    }
}
