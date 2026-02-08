package info.zhihui.ems.iot.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.iot.application.DeviceAppService;
import info.zhihui.ems.iot.application.DeviceVendorFacade;
import info.zhihui.ems.iot.vo.DeviceSaveVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeviceAppService deviceAppService;

    @MockitoBean
    private DeviceVendorFacade deviceVendorFacade;

    @Test
    @DisplayName("新增设备")
    void testAddDevice_ShouldReturnSuccess() throws Exception {
        DeviceSaveVo body = new DeviceSaveVo()
                .setDeviceNo("dev-1")
                .setProductCode("acrel-4g");
        when(deviceAppService.addDevice(any(DeviceSaveVo.class))).thenReturn(1001);

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1001));

        verify(deviceAppService).addDevice(any(DeviceSaveVo.class));
    }

    @Test
    @DisplayName("更新设备")
    void testUpdateDevice_ShouldReturnSuccess() throws Exception {
        DeviceSaveVo body = new DeviceSaveVo()
                .setDeviceNo("dev-2")
                .setProductCode("acrel-gateway");

        mockMvc.perform(put("/api/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(deviceAppService).updateDevice(eq(1), any(DeviceSaveVo.class));
    }

    @Test
    @DisplayName("删除设备")
    void testDeleteDevice_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/devices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(deviceAppService).deleteDevice(1);
    }

    @Test
    @DisplayName("查询在线状态")
    void testGetOnline_ShouldReturnSuccess() throws Exception {
        when(deviceVendorFacade.getOnline(1)).thenReturn(Boolean.TRUE);

        mockMvc.perform(get("/api/devices/1/online"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));

        verify(deviceVendorFacade).getOnline(1);
    }

    @Test
    @DisplayName("查询在线状态-设备ID非法时返回参数错误")
    void testGetOnline_WhenDeviceIdInvalid_ShouldReturnParameterError() throws Exception {
        mockMvc.perform(get("/api/devices/0/online"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ResultCode.PARAMETER_ERROR.getCode()));

        verifyNoInteractions(deviceVendorFacade);
    }

    @Test
    @DisplayName("更新设备-设备ID非法时返回参数错误")
    void testUpdateDevice_WhenDeviceIdInvalid_ShouldReturnParameterError() throws Exception {
        DeviceSaveVo body = new DeviceSaveVo()
                .setDeviceNo("dev-1")
                .setProductCode("acrel-4g");

        mockMvc.perform(put("/api/devices/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ResultCode.PARAMETER_ERROR.getCode()));

        verifyNoInteractions(deviceAppService);
    }
}
